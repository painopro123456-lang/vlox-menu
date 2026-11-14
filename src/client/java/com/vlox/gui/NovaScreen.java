package com.vlox.gui;

import com.vlox.config.VloxConfig;
import com.vlox.feature.Category;
import com.vlox.feature.Feature;
import com.vlox.feature.Features;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Nova's compact three-column in-game module browser. */
public final class NovaScreen extends Screen {
    private static boolean open;
    public static boolean isOpen() { return open; }
    private static final int SIDEBAR = 150;
    private static final int CARD_HEIGHT = 78;
    private static final int CARD_GAP = 10;
    private static final int ROW_GAP = 10;
    private static final int PANEL_BG = 0xF20B1220;
    private static final int SIDEBAR_BG = 0xF909101B;
    private static final int CARD_BG = 0xD9162232;
    private static final int CARD_HOVER = 0xEE1D2B40;

    private enum Tab {
        ALL("All"), HUD("HUD"), VISUAL("Visuals"), PLAYER("Player"), CHAT("Chat"), UTILITY("Utility"), MODS("Mods");
        final String label;
        Tab(String label) { this.label = label; }
    }

    private static Tab lastTab = Tab.ALL;
    private Tab tab = Tab.ALL;
    private String query = "";
    private boolean searchFocused;
    private final List<Feature> modules = new ArrayList<>();
    private final List<ModContainer> installedMods = new ArrayList<>();
    private float scroll;
    private int panelX, panelY, panelW, panelH;
    private int contentX, contentW, gridY, gridH, cardW, columns;
    private int advancedX, advancedY, advancedW, advancedH;

    public NovaScreen() {
        super(Component.literal("Nova Mod Menu"));
    }

    @Override
    protected void init() {
        open = true;
        panelW = Math.min(860, width - 28);
        panelH = Math.min(520, height - 28);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        contentX = panelX + SIDEBAR + 18;
        contentW = panelW - SIDEBAR - 36;
        gridY = panelY + 112;
        gridH = panelH - 166;
        columns = 3;
        cardW = Math.max(120, (contentW - CARD_GAP * (columns - 1)) / columns);
        advancedW = 142;
        advancedH = 24;
        advancedX = panelX + panelW - advancedW - 18;
        advancedY = panelY + panelH - 38;
        installedMods.clear();
        installedMods.addAll(FabricLoader.getInstance().getAllMods());
        installedMods.sort(Comparator.comparing(m -> m.getMetadata().getName(), String.CASE_INSENSITIVE_ORDER));
        tab = VloxConfig.flag(Features.MENU_REMEMBER) ? lastTab : Tab.ALL;
        searchFocused = VloxConfig.flag(Features.MENU_SEARCH_FOCUS);
        refreshModules();
    }

    private void refreshModules() {
        modules.clear();
        for (Feature feature : Features.ALL) {
            if (feature.type != Feature.Type.TOGGLE) continue;
            if ((tab == Tab.ALL || matches(feature, tab)) && matchesQuery(feature)) modules.add(feature);
        }
        scroll = 0;
    }

    private boolean matchesQuery(Feature feature) {
        if (query.isBlank()) return true;
        String q = query.toLowerCase(java.util.Locale.ROOT);
        return feature.label.toLowerCase(java.util.Locale.ROOT).contains(q)
                || feature.description.toLowerCase(java.util.Locale.ROOT).contains(q)
                || feature.id.toLowerCase(java.util.Locale.ROOT).contains(q);
    }

    private int cardHeight() {
        return VloxConfig.flag(Features.MENU_COMPACT) ? 60 : CARD_HEIGHT;
    }

    private static boolean matches(Feature feature, Tab selected) {
        return switch (selected) {
            case HUD -> feature.category == Category.HUD;
            case VISUAL -> feature.category == Category.GRAPHICS;
            case PLAYER -> feature.category == Category.INPUT;
            case CHAT -> feature.category == Category.CHAT;
            case UTILITY -> feature.category == Category.MENU;
            case ALL, MODS -> false;
        };
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xB9080D16);
        Ui.panel(graphics, panelX, panelY, panelW, panelH, 13, PANEL_BG);
        Ui.rounded(graphics, panelX, panelY, SIDEBAR, panelH, 13, SIDEBAR_BG);
        graphics.fill(panelX + SIDEBAR - 1, panelY + 14, panelX + SIDEBAR, panelY + panelH - 14, 0x18FFFFFF);

        Ui.textScaled(graphics, font, "NOVA", panelX + 20, panelY + 25, Ui.TEXT, 1.9F);
        Ui.text(graphics, font, "MOD MENU", panelX + 21, panelY + 51, Ui.accent());

        int tabY = panelY + 82;
        for (Tab item : Tab.values()) {
            boolean active = item == tab;
            boolean hovered = Ui.inside(mouseX, mouseY, panelX + 12, tabY, SIDEBAR - 24, 30);
            if (active) {
                Ui.rounded(graphics, panelX + 12, tabY, SIDEBAR - 24, 30, 7, Ui.alpha(Ui.accent(), 34));
                graphics.fill(panelX + 12, tabY + 7, panelX + 14, tabY + 23, Ui.accent());
            } else if (hovered) {
                Ui.rounded(graphics, panelX + 12, tabY, SIDEBAR - 24, 30, 7, 0x16FFFFFF);
            }
            Ui.text(graphics, font, item.label, panelX + 25, tabY + 10,
                    active ? Ui.TEXT : hovered ? 0xFFDCE7F4 : Ui.MUTED);
            int count = item == Tab.MODS ? installedMods.size() : countFor(item);
            String countText = Integer.toString(count);
            Ui.text(graphics, font, countText, panelX + SIDEBAR - 28 - font.width(countText),
                    tabY + 10, active ? Ui.accent() : Ui.FAINT);
            tabY += 36;
        }

        String heading = tab == Tab.MODS ? "Installed mods" : "Modules";
        if (tab != Tab.MODS && VloxConfig.flag(Features.MENU_SHOW_FPS)) {
            heading += "  ·  " + Minecraft.getInstance().getFps() + " FPS";
        }
        String subtitle = tab == Tab.MODS
                ? "Fabric mods loaded for this Minecraft instance"
                : "Toggle Nova's client-side modules";
        Ui.textScaled(graphics, font, heading, contentX, panelY + 23, Ui.TEXT, 1.25F);
        Ui.text(graphics, font, subtitle, contentX, panelY + 47, Ui.MUTED);

        String count = tab == Tab.MODS ? installedMods.size() + " loaded" : modules.size() + " modules";
        Ui.text(graphics, font, count, contentX + contentW - font.width(count), panelY + 27, Ui.MUTED);

        graphics.fill(contentX, panelY + 76, contentX + contentW, panelY + 77, 0x20FFFFFF);
        drawSearch(graphics, mouseX, mouseY);

        if (tab == Tab.MODS) {
            drawMods(graphics, mouseX, mouseY);
        } else {
            drawModules(graphics, mouseX, mouseY);
        }

        boolean advancedHover = Ui.inside(mouseX, mouseY, advancedX, advancedY, advancedW, advancedH);
        Ui.rounded(graphics, advancedX, advancedY, advancedW, advancedH, 7,
                advancedHover ? Ui.alpha(Ui.accent(), 54) : 0xFF1A2637);
        Ui.text(graphics, font, "Advanced settings",
                advancedX + (advancedW - font.width("Advanced settings")) / 2,
                advancedY + 8, advancedHover ? Ui.TEXT : Ui.MUTED);

        Ui.text(graphics, font, "RIGHT SHIFT  ·  CLOSE",
                panelX + 19, panelY + panelH - 27, Ui.FAINT);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawSearch(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = contentX;
        int y = panelY + 82;
        int w = Math.min(250, contentW);
        boolean hover = Ui.inside(mouseX, mouseY, x, y, w, 22);
        int bg = searchFocused ? Ui.alpha(Ui.accent(), 28) : hover ? 0xFF1B293A : 0xFF121C2A;
        Ui.rounded(graphics, x, y, w, 22, 6, bg);
        if (searchFocused) graphics.fill(x + 1, y + 5, x + 3, y + 17, Ui.accent());
        String shown = query.isEmpty() ? "Search modules..." : query;
        Ui.text(graphics, font, Ui.fit(font, shown, w - 18), x + 10, y + 7,
                query.isEmpty() ? Ui.FAINT : Ui.TEXT);
        String hint = query.isEmpty() ? "Click to search" : modules.size() + " matches";
        Ui.text(graphics, font, hint, x + w + 10, y + 7, Ui.FAINT);
    }

    private int countFor(Tab selected) {
        int count = 0;
        for (Feature feature : Features.ALL) {
            if (feature.type == Feature.Type.TOGGLE && (selected == Tab.ALL || matches(feature, selected))) count++;
        }
        return count;
    }

    private void drawModules(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int totalRows = (modules.size() + columns - 1) / columns;
        int contentHeight = totalRows * (cardHeight() + ROW_GAP);
        float maxScroll = Math.max(0, contentHeight - gridH);
        scroll = Math.max(0, Math.min(maxScroll, scroll));
        int start = gridY;
        for (int i = 0; i < modules.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int x = contentX + col * (cardW + CARD_GAP);
            int y = start + row * (CARD_HEIGHT + ROW_GAP) - Math.round(scroll);
            if (y + cardHeight() < gridY || y > gridY + gridH) continue;
            Feature feature = modules.get(i);
            boolean hovered = Ui.inside(mouseX, mouseY, x, y, cardW, cardHeight());
            boolean enabled = isEnabled(feature);
            Ui.rounded(graphics, x, y, cardW, CARD_HEIGHT, 9, hovered ? CARD_HOVER : CARD_BG);
            graphics.fill(x + 12, y + 12, x + 14, y + cardHeight() - 12,
                    enabled ? Ui.accent() : 0x334F6074);
            Ui.text(graphics, font, Ui.fit(font, feature.label, cardW - 72), x + 22, y + 17,
                    enabled ? Ui.TEXT : 0xFFD4DEEA);
            String state = enabled ? "ON" : "OFF";
            Ui.text(graphics, font, state, x + 22, y + 39, enabled ? Ui.accent() : Ui.FAINT);
            if (VloxConfig.flag(Features.MENU_DESCRIPTIONS) && !VloxConfig.flag(Features.MENU_COMPACT)) {
                String description = Ui.fit(font, feature.description, cardW - 36);
                Ui.text(graphics, font, description, x + 22, y + 56, Ui.MUTED);
            }
            drawSwitch(graphics, x + cardW - 48, y + 17, enabled);
        }
        drawScrollHint(graphics, contentHeight);
    }

    private void drawMods(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int totalRows = (installedMods.size() + columns - 1) / columns;
        int contentHeight = totalRows * (CARD_HEIGHT + ROW_GAP);
        float maxScroll = Math.max(0, contentHeight - gridH);
        scroll = Math.max(0, Math.min(maxScroll, scroll));
        for (int i = 0; i < installedMods.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int x = contentX + col * (cardW + CARD_GAP);
            int y = gridY + row * (CARD_HEIGHT + ROW_GAP) - Math.round(scroll);
            if (y + CARD_HEIGHT < gridY || y > gridY + gridH) continue;
            ModContainer mod = installedMods.get(i);
            boolean hovered = Ui.inside(mouseX, mouseY, x, y, cardW, CARD_HEIGHT);
            Ui.rounded(graphics, x, y, cardW, CARD_HEIGHT, 9, hovered ? CARD_HOVER : CARD_BG);
            graphics.fill(x + 12, y + 12, x + 14, y + CARD_HEIGHT - 12, 0xFF71B8FF);
            String name = mod.getMetadata().getName();
            String version = mod.getMetadata().getVersion().getFriendlyString();
            Ui.text(graphics, font, Ui.fit(font, name, cardW - 36), x + 22, y + 16, Ui.TEXT);
            Ui.text(graphics, font, Ui.fit(font, mod.getMetadata().getId() + "  ·  " + version, cardW - 36),
                    x + 22, y + 36, 0xFF82BFFF);
            Ui.text(graphics, font, Ui.fit(font, mod.getMetadata().getDescription(), cardW - 36),
                    x + 22, y + 56, Ui.MUTED);
        }
        drawScrollHint(graphics, contentHeight);
    }

    private void drawScrollHint(GuiGraphicsExtractor graphics, int contentHeight) {
        if (contentHeight <= gridH) return;
        int trackX = contentX + contentW - 3;
        int trackY = gridY + 2;
        int trackH = gridH - 4;
        graphics.fill(trackX, trackY, trackX + 2, trackY + trackH, 0x263C4C61);
        int thumbH = Math.max(22, (int) ((float) gridH / contentHeight * trackH));
        float maxScroll = contentHeight - gridH;
        int thumbY = trackY + (int) ((trackH - thumbH) * (scroll / maxScroll));
        Ui.rounded(graphics, trackX - 1, thumbY, 4, thumbH, 2, Ui.accent());
    }

    private static boolean isEnabled(Feature feature) {
        OptionInstance<?> option = Features.optionFor(feature);
        if (option != null && option.get() instanceof Boolean flag) return flag;
        return VloxConfig.flag(feature);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void toggle(Feature feature) {
        OptionInstance<?> option = Features.optionFor(feature);
        if (option != null && option.get() instanceof Boolean flag) {
            ((OptionInstance) option).set(!flag);
        } else {
            VloxConfig.setFlag(feature, !VloxConfig.flag(feature));
        }
    }

    private static void drawSwitch(GuiGraphicsExtractor graphics, int x, int y, boolean enabled) {
        Ui.rounded(graphics, x, y, 34, 17, 9, enabled ? Ui.alpha(Ui.accent(), 105) : 0xFF263243);
        int knobX = enabled ? x + 19 : x + 2;
        Ui.rounded(graphics, knobX, y + 2, 13, 13, 7, enabled ? Ui.accent() : 0xFF8A98A9);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double mx = event.x();
        double my = event.y();
        int tabY = panelY + 82;
        for (Tab item : Tab.values()) {
            if (Ui.inside(mx, my, panelX + 12, tabY, SIDEBAR - 24, 30)) {
                tab = item;
                if (VloxConfig.flag(Features.MENU_REMEMBER)) lastTab = item;
                refreshModules();
                return true;
            }
            tabY += 36;
        }
        int searchW = Math.min(250, contentW);
        if (Ui.inside(mx, my, contentX, panelY + 82, searchW, 22)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;
        if (Ui.inside(mx, my, advancedX, advancedY, advancedW, advancedH)) {
            Minecraft.getInstance().setScreenAndShow(new VloxScreen());
            return true;
        }
        if (tab != Tab.MODS && my >= gridY && my <= gridY + gridH) {
            int col = (int) ((mx - contentX) / (cardW + CARD_GAP));
            int row = (int) ((my - gridY + scroll) / (CARD_HEIGHT + ROW_GAP));
            if (col >= 0 && col < columns && row >= 0) {
                int index = row * columns + col;
                if (index < modules.size()) {
                    int cardX = contentX + col * (cardW + CARD_GAP);
                    int cardY = gridY + row * (CARD_HEIGHT + ROW_GAP) - Math.round(scroll);
                    if (Ui.inside(mx, my, cardX, cardY, cardW, CARD_HEIGHT)) {
                        toggle(modules.get(index));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (vertical == 0 || !Ui.inside(mouseX, mouseY, contentX, gridY, contentW, gridH)) {
            return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
        }
        int rows = tab == Tab.MODS
                ? (installedMods.size() + columns - 1) / columns
                : (modules.size() + columns - 1) / columns;
        float contentHeight = rows * (cardHeight() + ROW_GAP);
        float maxScroll = Math.max(0, contentHeight - gridH);
        scroll = Math.max(0, Math.min(maxScroll, scroll - (float) vertical * 42.0F));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (searchFocused && event.key() == GLFW.GLFW_KEY_BACKSPACE && !query.isEmpty()) {
            query = query.substring(0, query.length() - 1);
            refreshModules();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (searchFocused && event.isAllowedChatCharacter() && query.length() < 32) {
            query += event.codepointAsString();
            refreshModules();
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public void removed() {
        open = false;
        super.removed();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
