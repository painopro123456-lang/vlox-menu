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

/** Nova's redesigned in-game control center. */
public final class NovaScreen extends Screen {
    private static boolean open;
    public static boolean isOpen() { return open; }
    private static final int SIDEBAR = 168;
    private static final int CARD_HEIGHT = 92;
    private static final int CARD_GAP = 12;
    private static final int ROW_GAP = 12;
    private static final int PANEL_BG = 0xF20A111E;
    private static final int SIDEBAR_BG = 0xF7080E19;
    private static final int CARD_BG = 0xD9142030;
    private static final int CARD_HOVER = 0xF01D3047;

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
        panelW = Math.min(980, width - 32);
        panelH = Math.min(590, height - 32);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        contentX = panelX + SIDEBAR + 24;
        contentW = panelW - SIDEBAR - 48;
        gridY = panelY + 150;
        gridH = panelH - 208;
        columns = 3;
        cardW = Math.max(120, (contentW - CARD_GAP * (columns - 1)) / columns);
        advancedW = 124;
        advancedH = 28;
        advancedX = panelX + panelW - advancedW - 22;
        advancedY = panelY + panelH - 42;
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
        graphics.fill(0, 0, width, height, 0xC5070B13);
        Ui.panel(graphics, panelX, panelY, panelW, panelH, 16, PANEL_BG);
        Ui.rounded(graphics, panelX, panelY, SIDEBAR, panelH, 16, SIDEBAR_BG);
        graphics.fill(panelX + SIDEBAR - 1, panelY + 18, panelX + SIDEBAR, panelY + panelH - 18, 0x20FFFFFF);

        Ui.rounded(graphics, panelX + 18, panelY + 22, 36, 36, 10, Ui.alpha(Ui.accent(), 42));
        Ui.textScaled(graphics, font, "N", panelX + 29, panelY + 30, Ui.TEXT, 1.45F);
        Ui.textScaled(graphics, font, "NOVA", panelX + 64, panelY + 23, Ui.TEXT, 1.45F);
        Ui.text(graphics, font, "CLIENT  /  26.2", panelX + 65, panelY + 48, Ui.accent());
        graphics.fill(panelX + 18, panelY + 76, panelX + SIDEBAR - 18, panelY + 77, 0x22FFFFFF);

        Ui.text(graphics, font, "WORKSPACE", panelX + 20, panelY + 94, Ui.FAINT);
        int tabY = panelY + 110;
        for (Tab item : Tab.values()) {
            boolean active = item == tab;
            boolean hovered = Ui.inside(mouseX, mouseY, panelX + 12, tabY, SIDEBAR - 24, 32);
            if (active) {
                Ui.rounded(graphics, panelX + 12, tabY, SIDEBAR - 24, 32, 8, Ui.alpha(Ui.accent(), 36));
                graphics.fill(panelX + 12, tabY + 8, panelX + 15, tabY + 24, Ui.accent());
            } else if (hovered) {
                Ui.rounded(graphics, panelX + 12, tabY, SIDEBAR - 24, 32, 8, 0x16FFFFFF);
            }
            Ui.text(graphics, font, item.label, panelX + 27, tabY + 11,
                    active ? Ui.TEXT : hovered ? 0xFFDCE7F4 : Ui.MUTED);
            int count = item == Tab.MODS ? installedMods.size() : countFor(item);
            String countText = Integer.toString(count);
            Ui.text(graphics, font, countText, panelX + SIDEBAR - 28 - font.width(countText),
                    tabY + 10, active ? Ui.accent() : Ui.FAINT);
            tabY += 39;
        }

        String heading = tab == Tab.MODS ? "Installed mods" : (tab == Tab.ALL ? "Module library" : tab.label + " modules");
        if (tab != Tab.MODS && VloxConfig.flag(Features.MENU_SHOW_FPS)) {
            heading += "  ·  " + Minecraft.getInstance().getFps() + " FPS";
        }
        String subtitle = tab == Tab.MODS
                ? "Fabric components installed in this Minecraft instance"
                : "Your client tools, visuals and quality-of-life controls";
        Ui.text(graphics, font, "NOVA  /  CONTROL CENTER", contentX, panelY + 23, Ui.accent());
        Ui.textScaled(graphics, font, heading, contentX, panelY + 43, Ui.TEXT, 1.35F);
        Ui.text(graphics, font, subtitle, contentX, panelY + 69, Ui.MUTED);

        String count = tab == Tab.MODS ? installedMods.size() + " installed" : modules.size() + " available";
        String active = activeCount() + " ACTIVE";
        Ui.rounded(graphics, contentX + contentW - font.width(active) - 24, panelY + 32, font.width(active) + 18, 22, 7, Ui.alpha(Ui.accent(), 30));
        Ui.text(graphics, font, active, contentX + contentW - font.width(active) - 15, panelY + 40, Ui.accent());
        Ui.text(graphics, font, count, contentX + contentW - font.width(count), panelY + 65, Ui.MUTED);

        graphics.fill(contentX, panelY + 91, contentX + contentW, panelY + 92, 0x22FFFFFF);
        drawSearch(graphics, mouseX, mouseY);

        if (tab == Tab.MODS) {
            drawMods(graphics, mouseX, mouseY);
        } else {
            drawModules(graphics, mouseX, mouseY);
        }

        boolean advancedHover = Ui.inside(mouseX, mouseY, advancedX, advancedY, advancedW, advancedH);
        Ui.rounded(graphics, advancedX, advancedY, advancedW, advancedH, 7,
                advancedHover ? Ui.alpha(Ui.accent(), 54) : 0xFF1A2637);
        Ui.text(graphics, font, "SETTINGS",
                advancedX + (advancedW - font.width("SETTINGS")) / 2,
                advancedY + 10, advancedHover ? Ui.TEXT : Ui.MUTED);

        Ui.text(graphics, font, "RIGHT SHIFT  /  CLOSE",
                panelX + 20, panelY + panelH - 26, Ui.FAINT);
        String enabledLabel = activeCount() + " modules running";
        Ui.text(graphics, font, enabledLabel, contentX, panelY + panelH - 26, Ui.MUTED);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawSearch(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = contentX;
        int y = panelY + 104;
        int w = Math.min(300, contentW);
        boolean hover = Ui.inside(mouseX, mouseY, x, y, w, 22);
        int bg = searchFocused ? Ui.alpha(Ui.accent(), 28) : hover ? 0xFF1B293A : 0xFF121C2A;
        Ui.rounded(graphics, x, y, w, 26, 8, bg);
        if (searchFocused) graphics.fill(x + 1, y + 6, x + 3, y + 20, Ui.accent());
        String shown = query.isEmpty() ? "Search modules..." : query;
        Ui.text(graphics, font, Ui.fit(font, shown, w - 18), x + 12, y + 9,
                query.isEmpty() ? Ui.FAINT : Ui.TEXT);
        String hint = query.isEmpty() ? "Click to search" : modules.size() + " matches";
        Ui.text(graphics, font, hint, x + w + 12, y + 9, Ui.FAINT);
    }

    private int activeCount() {
        int count = 0;
        for (Feature feature : Features.ALL) if (feature.type == Feature.Type.TOGGLE && isEnabled(feature)) count++;
        return count;
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
            int y = start + row * (cardHeight() + ROW_GAP) - Math.round(scroll);
            if (y + cardHeight() < gridY || y > gridY + gridH) continue;
            Feature feature = modules.get(i);
            boolean hovered = Ui.inside(mouseX, mouseY, x, y, cardW, cardHeight());
            boolean enabled = isEnabled(feature);
            Ui.rounded(graphics, x, y + 2, cardW, cardHeight() - 2, 11, hovered ? CARD_HOVER : CARD_BG);
            if (enabled) graphics.fill(x + 12, y + 13, x + cardW - 12, y + 15, Ui.alpha(Ui.accent(), 190));
            Ui.rounded(graphics, x + 14, y + 25, 34, 34, 9, enabled ? Ui.alpha(Ui.accent(), 46) : 0xFF202C3B);
            String glyph = feature.label.isEmpty() ? "N" : feature.label.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
            Ui.textScaled(graphics, font, glyph, x + 25, y + 34, enabled ? Ui.accent() : 0xFF9BAABD, 1.15F);
            Ui.text(graphics, font, Ui.fit(font, feature.label, cardW - 112), x + 58, y + 23,
                    enabled ? Ui.TEXT : 0xFFD4DEEA);
            String state = enabled ? "ENABLED" : "DISABLED";
            Ui.text(graphics, font, state, x + 58, y + 42, enabled ? Ui.accent() : Ui.FAINT);
            if (VloxConfig.flag(Features.MENU_DESCRIPTIONS) && !VloxConfig.flag(Features.MENU_COMPACT)) {
                String description = Ui.fit(font, feature.description, cardW - 30);
                Ui.text(graphics, font, description, x + 14, y + 70, Ui.MUTED);
            }
            drawSwitch(graphics, x + cardW - 48, y + 27, enabled);
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
        int tabY = panelY + 110;
        for (Tab item : Tab.values()) {
            if (Ui.inside(mx, my, panelX + 12, tabY, SIDEBAR - 24, 32)) {
                tab = item;
                if (VloxConfig.flag(Features.MENU_REMEMBER)) lastTab = item;
                refreshModules();
                return true;
            }
            tabY += 36;
        }
        int searchW = Math.min(300, contentW);
        if (Ui.inside(mx, my, contentX, panelY + 104, searchW, 26)) {
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
            int row = (int) ((my - gridY + scroll) / (cardHeight() + ROW_GAP));
            if (col >= 0 && col < columns && row >= 0) {
                int index = row * columns + col;
                if (index < modules.size()) {
                    int cardX = contentX + col * (cardW + CARD_GAP);
                    int cardY = gridY + row * (cardHeight() + ROW_GAP) - Math.round(scroll);
                    if (Ui.inside(mx, my, cardX, cardY, cardW, cardHeight())) {
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
