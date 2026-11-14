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
    private static final int SIDEBAR = 190;
    private static final int CARD_HEIGHT = 112;
    private static final int CARD_GAP = 14;
    private static final int ROW_GAP = 14;
    private static final int PANEL_BG = 0xF20A101B;
    private static final int SIDEBAR_BG = 0xFF0B1421;
    private static final int CARD_BG = 0xE5111D2B;
    private static final int CARD_HOVER = 0xF01B2B40;

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
        panelW = Math.min(1160, width - 24);
        panelH = Math.min(700, height - 24);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        contentX = panelX + SIDEBAR + 30;
        contentW = panelW - SIDEBAR - 60;
        gridY = panelY + 224;
        gridH = panelH - 284;
        columns = 2;
        cardW = Math.max(120, (contentW - CARD_GAP * (columns - 1)) / columns);
        advancedW = 136;
        advancedH = 32;
        advancedX = panelX + panelW - advancedW - 24;
        advancedY = panelY + panelH - 48;
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
        graphics.fill(0, 0, width, height, 0xD0060A12);
        Ui.panel(graphics, panelX, panelY, panelW, panelH, 18, PANEL_BG);
        Ui.rounded(graphics, panelX, panelY, SIDEBAR, panelH, 18, SIDEBAR_BG);
        graphics.fill(panelX + SIDEBAR - 1, panelY + 20, panelX + SIDEBAR, panelY + panelH - 20, 0x22FFFFFF);

        // Brand block and primary navigation
        Ui.rounded(graphics, panelX + 22, panelY + 24, 42, 42, 12, Ui.alpha(Ui.accent(), 42));
        Ui.textScaled(graphics, font, "N", panelX + 35, panelY + 34, Ui.TEXT, 1.7F);
        Ui.textScaled(graphics, font, "NOVA", panelX + 76, panelY + 25, Ui.TEXT, 1.65F);
        Ui.text(graphics, font, "CLIENT  /  26.2", panelX + 77, panelY + 51, Ui.accent());
        graphics.fill(panelX + 22, panelY + 84, panelX + SIDEBAR - 22, panelY + 85, 0x22FFFFFF);
        Ui.text(graphics, font, "LIBRARY", panelX + 24, panelY + 104, Ui.FAINT);

        int tabY = panelY + 124;
        for (Tab item : Tab.values()) {
            boolean active = item == tab;
            boolean hovered = Ui.inside(mouseX, mouseY, panelX + 14, tabY, SIDEBAR - 28, 36);
            if (active) {
                Ui.rounded(graphics, panelX + 14, tabY, SIDEBAR - 28, 36, 9, Ui.alpha(Ui.accent(), 34));
                graphics.fill(panelX + 14, tabY + 9, panelX + 17, tabY + 27, Ui.accent());
            } else if (hovered) {
                Ui.rounded(graphics, panelX + 14, tabY, SIDEBAR - 28, 36, 9, 0x16FFFFFF);
            }
            Ui.text(graphics, font, item.label, panelX + 31, tabY + 13,
                    active ? Ui.TEXT : hovered ? 0xFFDCE7F4 : Ui.MUTED);
            int count = item == Tab.MODS ? installedMods.size() : countFor(item);
            String countText = Integer.toString(count);
            Ui.text(graphics, font, countText, panelX + SIDEBAR - 34 - font.width(countText),
                    tabY + 13, active ? Ui.accent() : Ui.FAINT);
            tabY += 43;
        }

        Ui.rounded(graphics, panelX + 18, panelY + panelH - 64, SIDEBAR - 36, 38, 9, 0xFF172536);
        Ui.text(graphics, font, "RIGHT SHIFT  ·  CLOSE", panelX + 29, panelY + panelH - 49, Ui.MUTED);

        // Main workspace header
        String heading = tab == Tab.MODS ? "Installed mods" : (tab == Tab.ALL ? "Your modules" : tab.label + " modules");
        String subtitle = tab == Tab.MODS
                ? "Everything loaded by Fabric in this instance"
                : "A clean control center for your Minecraft experience";
        Ui.text(graphics, font, "NOVA  /  WORKSPACE", contentX, panelY + 28, Ui.accent());
        Ui.textScaled(graphics, font, heading, contentX, panelY + 52, Ui.TEXT, 1.7F);
        Ui.text(graphics, font, subtitle, contentX, panelY + 82, Ui.MUTED);

        String active = activeCount() + " ACTIVE";
        int badgeW = font.width(active) + 30;
        Ui.rounded(graphics, contentX + contentW - badgeW, panelY + 46, badgeW, 28, 9, Ui.alpha(Ui.accent(), 34));
        Ui.rounded(graphics, contentX + contentW - badgeW + 10, panelY + 57, 6, 6, 3, Ui.accent());
        Ui.text(graphics, font, active, contentX + contentW - badgeW + 22, panelY + 56, Ui.accent());

        // Search is a full-width toolbar, separate from the module cards.
        drawSearch(graphics, mouseX, mouseY);
        if (tab == Tab.MODS) drawMods(graphics, mouseX, mouseY);
        else drawModules(graphics, mouseX, mouseY);

        boolean advancedHover = Ui.inside(mouseX, mouseY, advancedX, advancedY, advancedW, advancedH);
        Ui.rounded(graphics, advancedX, advancedY, advancedW, advancedH, 9,
                advancedHover ? Ui.alpha(Ui.accent(), 58) : 0xFF1A2A3D);
        Ui.text(graphics, font, "MENU SETTINGS",
                advancedX + (advancedW - font.width("MENU SETTINGS")) / 2,
                advancedY + 12, advancedHover ? Ui.TEXT : Ui.MUTED);

        String count = tab == Tab.MODS ? installedMods.size() + " mods installed" : modules.size() + " modules in this view";
        Ui.text(graphics, font, count, contentX, panelY + panelH - 27, Ui.MUTED);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawSearch(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = contentX;
        int y = panelY + 158;
        int w = contentW;
        boolean hover = Ui.inside(mouseX, mouseY, x, y, w, 38);
        int bg = searchFocused ? Ui.alpha(Ui.accent(), 28) : hover ? 0xFF1B293A : 0xFF121C2A;
        Ui.rounded(graphics, x, y, w, 38, 11, bg);
        if (searchFocused) graphics.fill(x + 1, y + 9, x + 3, y + 29, Ui.accent());
        String shown = query.isEmpty() ? "Search by module name or feature..." : query;
        Ui.text(graphics, font, Ui.fit(font, shown, w - 190), x + 16, y + 15,
                query.isEmpty() ? Ui.FAINT : Ui.TEXT);
        String hint = query.isEmpty() ? " /  SEARCH" : modules.size() + " RESULTS";
        Ui.text(graphics, font, hint, x + w - font.width(hint) - 16, y + 15, Ui.FAINT);
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
            Ui.rounded(graphics, x, y, cardW, cardHeight(), 13, hovered ? CARD_HOVER : CARD_BG);
            Ui.rounded(graphics, x + 14, y + 17, 48, 48, 13,
                    enabled ? Ui.alpha(Ui.accent(), 48) : 0xFF1B2938);
            String glyph = feature.label.isEmpty() ? "N" : feature.label.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
            Ui.textScaled(graphics, font, glyph, x + 31, y + 31, enabled ? Ui.accent() : 0xFF9BAABD, 1.55F);
            Ui.text(graphics, font, Ui.fit(font, feature.label, cardW - 150), x + 76, y + 21,
                    enabled ? Ui.TEXT : 0xFFD4DEEA);
            Ui.text(graphics, font, Ui.fit(font, feature.category.label.toUpperCase(java.util.Locale.ROOT), cardW - 150),
                    x + 76, y + 40, enabled ? Ui.accent() : Ui.FAINT);
            if (VloxConfig.flag(Features.MENU_DESCRIPTIONS) && !VloxConfig.flag(Features.MENU_COMPACT)) {
                Ui.text(graphics, font, Ui.fit(font, feature.description, cardW - 30), x + 16, y + 78, Ui.MUTED);
            }
            graphics.fill(x + 16, y + cardHeight() - 17, x + cardW - 16, y + cardHeight() - 16, 0x18FFFFFF);
            Ui.text(graphics, font, enabled ? "RUNNING" : "READY", x + 17, y + cardHeight() - 11,
                    enabled ? Ui.accent() : Ui.FAINT);
            drawSwitch(graphics, x + cardW - 52, y + 25, enabled);
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
            tabY += 39;
        }
        int searchW = contentW;
        if (Ui.inside(mx, my, contentX, panelY + 158, searchW, 38)) {
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
