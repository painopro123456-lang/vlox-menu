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

/**
 * Nova's in-game library. A complete editorial-style workspace: persistent rail,
 * oversized page title, compact filter bar, two-column module tiles and a focused
 * right-side information panel.
 */
public final class NovaScreen extends Screen {
    private static boolean open;
    public static boolean isOpen() { return open; }

    private static final int BG = 0xF2090D14;
    private static final int RAIL = 0xFF0D131D;
    private static final int SURFACE = 0xFF151E2A;
    private static final int SURFACE_HOVER = 0xFF202D3D;
    private static final int LINE = 0x243F536B;

    private enum Tab {
        ALL("Overview"), HUD("HUD"), VISUAL("Visuals"), PLAYER("Player"),
        CHAT("Chat"), UTILITY("Utility"), MODS("Installed mods");
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
    private int panelX, panelY, panelW, panelH, railW;
    private int mainX, mainW, listY, listH, cardW, columns;
    private int detailX, detailW;
    private Feature selected;

    public NovaScreen() { super(Component.literal("Nova")); }

    @Override
    protected void init() {
        open = true;
        panelW = Math.min(1120, width - 24);
        panelH = Math.min(720, height - 24);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        railW = 174;
        mainX = panelX + railW + 28;
        detailW = Math.min(230, Math.max(180, panelW / 4));
        detailX = panelX + panelW - detailW - 24;
        mainW = detailX - mainX - 22;
        columns = mainW >= 500 ? 2 : 1;
        cardW = (mainW - (columns - 1) * 12) / columns;
        listY = panelY + 218;
        listH = panelH - 254;
        installedMods.clear();
        installedMods.addAll(FabricLoader.getInstance().getAllMods());
        installedMods.sort(Comparator.comparing(m -> m.getMetadata().getName(), String.CASE_INSENSITIVE_ORDER));
        tab = VloxConfig.flag(Features.MENU_REMEMBER) ? lastTab : Tab.ALL;
        refreshModules();
    }

    private void refreshModules() {
        modules.clear();
        for (Feature feature : Features.ALL) {
            if (feature.type == Feature.Type.TOGGLE && (tab == Tab.ALL || matches(feature, tab)) && matchesQuery(feature)) {
                modules.add(feature);
            }
        }
        if (selected != null && !modules.contains(selected)) selected = null;
        if (selected == null && !modules.isEmpty()) selected = modules.get(0);
        scroll = 0;
    }

    private boolean matchesQuery(Feature feature) {
        if (query.isBlank()) return true;
        String q = query.toLowerCase(java.util.Locale.ROOT);
        return feature.label.toLowerCase(java.util.Locale.ROOT).contains(q)
            || feature.description.toLowerCase(java.util.Locale.ROOT).contains(q)
            || feature.id.toLowerCase(java.util.Locale.ROOT).contains(q);
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

    private int activeCount() {
        int count = 0;
        for (Feature f : Features.ALL) if (f.type == Feature.Type.TOGGLE && isEnabled(f)) count++;
        return count;
    }

    private int countFor(Tab t) {
        int count = 0;
        for (Feature f : Features.ALL) if (f.type == Feature.Type.TOGGLE && (t == Tab.ALL || matches(f, t))) count++;
        return count;
    }

    private static boolean isEnabled(Feature feature) {
        OptionInstance<?> option = Features.optionFor(feature);
        if (option != null && option.get() instanceof Boolean flag) return flag;
        return VloxConfig.flag(feature);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void toggle(Feature feature) {
        OptionInstance<?> option = Features.optionFor(feature);
        if (option != null && option.get() instanceof Boolean flag) ((OptionInstance) option).set(!flag);
        else VloxConfig.setFlag(feature, !VloxConfig.flag(feature));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0xD9000308);
        // App shell and rail
        Ui.panel(g, panelX, panelY, panelW, panelH, 12, BG);
        Ui.rounded(g, panelX, panelY, railW, panelH, 12, RAIL);
        g.fill(panelX + railW - 1, panelY + 18, panelX + railW, panelY + panelH - 18, LINE);
        drawRail(g, mx, my);

        // Header: intentionally spacious and typography-led, not a row of tabs.
        Ui.textScaled(g, font, "NOVA", mainX, panelY + 27, Ui.TEXT, 1.55F);
        Ui.text(g, font, "CLIENT CONTROL CENTER  /  FABRIC 26.2", mainX, panelY + 52, Ui.MUTED);
        String active = activeCount() + " ACTIVE";
        Ui.rounded(g, detailX + detailW - font.width(active) - 24, panelY + 28, font.width(active) + 20, 25, 6, Ui.alpha(Ui.accent(), 32));
        Ui.text(g, font, active, detailX + detailW - font.width(active) - 14, panelY + 37, Ui.accent());

        // Large feature strip gives the page a distinct identity.
        Ui.rounded(g, mainX, panelY + 78, mainW, 76, 9, 0xFF172536);
        g.fill(mainX, panelY + 91, mainX + 3, panelY + 141, Ui.accent());
        Ui.text(g, font, tab == Tab.MODS ? "YOUR MOD ENVIRONMENT" : "MAKE THE CLIENT YOURS", mainX + 18, panelY + 96, Ui.TEXT);
        Ui.text(g, font, tab == Tab.MODS ? "A quick look at everything loaded with this instance." : "A clean space for the tools you actually use.", mainX + 18, panelY + 117, Ui.MUTED);
        Ui.text(g, font, tab == Tab.MODS ? installedMods.size() + " LOADED" : modules.size() + " MODULES", mainX + mainW - font.width(tab == Tab.MODS ? installedMods.size() + " LOADED" : modules.size() + " MODULES") - 18, panelY + 116, Ui.accent());

        // Search / section heading
        String section = tab.label.toUpperCase(java.util.Locale.ROOT);
        Ui.text(g, font, section, mainX, panelY + 178, Ui.TEXT);
        Ui.text(g, font, tab == Tab.MODS ? "Installed in this Minecraft profile" : "Select a module to inspect it, or use its switch to toggle it.", mainX, panelY + 194, Ui.MUTED);
        int sx = mainX + mainW - 176;
        Ui.rounded(g, sx, panelY + 171, 176, 28, 6, searchFocused ? Ui.alpha(Ui.accent(), 32) : 0xFF111A25);
        Ui.text(g, font, query.isEmpty() ? "Search modules..." : Ui.fit(font, query, 130), sx + 10, panelY + 181, query.isEmpty() ? Ui.FAINT : Ui.TEXT);
        drawLibrary(g, mx, my);
        drawDetails(g);
        super.extractRenderState(g, mx, my, delta);
    }

    private void drawRail(GuiGraphicsExtractor g, int mx, int my) {
        int x = panelX + 20;
        Ui.rounded(g, x, panelY + 22, 38, 38, 10, Ui.alpha(Ui.accent(), 42));
        Ui.textScaled(g, font, "N", x + 12, panelY + 30, Ui.TEXT, 1.5F);
        Ui.textScaled(g, font, "NOVA", x + 48, panelY + 23, Ui.TEXT, 1.25F);
        Ui.text(g, font, "MINECRAFT CLIENT", x + 49, panelY + 45, Ui.MUTED);
        Ui.text(g, font, "WORKSPACE", x, panelY + 91, Ui.FAINT);
        int y = panelY + 108;
        for (Tab t : Tab.values()) {
            boolean active = tab == t;
            boolean hover = Ui.inside(mx, my, panelX + 12, y, railW - 24, 32);
            int bg = active ? Ui.alpha(Ui.accent(), 34) : hover ? 0xFF182433 : 0;
            if (bg != 0) Ui.rounded(g, panelX + 12, y, railW - 24, 32, 6, bg);
            if (active) g.fill(panelX + 12, y + 8, panelX + 14, y + 24, Ui.accent());
            Ui.text(g, font, t.label, x + 7, y + 12, active ? Ui.TEXT : hover ? 0xFFD6E0EB : Ui.MUTED);
            String count = t == Tab.MODS ? Integer.toString(installedMods.size()) : Integer.toString(countFor(t));
            Ui.text(g, font, count, panelX + railW - 31 - font.width(count), y + 12, active ? Ui.accent() : Ui.FAINT);
            y += 38;
        }
        int bottom = panelY + panelH - 58;
        Ui.rounded(g, panelX + 12, bottom, railW - 24, 38, 7, 0xFF182638);
        Ui.text(g, font, "PREFERENCES  ↗", panelX + 27, bottom + 15, Ui.TEXT);
        Ui.text(g, font, "RIGHT SHIFT  ·  CLOSE", panelX + 20, panelY + panelH - 14, Ui.FAINT);
    }

    private void drawLibrary(GuiGraphicsExtractor g, int mx, int my) {
        int rows = tab == Tab.MODS ? (installedMods.size() + columns - 1) / columns : (modules.size() + columns - 1) / columns;
        int rowH = 58;
        int total = rows * (rowH + 9);
        float max = Math.max(0, total - listH);
        scroll = Math.max(0, Math.min(max, scroll));
        int count = tab == Tab.MODS ? installedMods.size() : modules.size();
        for (int i = 0; i < count; i++) {
            int col = i % columns, row = i / columns;
            int x = mainX + col * (cardW + 12);
            int y = listY + row * (rowH + 9) - Math.round(scroll);
            if (y + rowH < listY || y > listY + listH) continue;
            boolean mod = tab == Tab.MODS;
            Feature f = mod ? null : modules.get(i);
            boolean enabled = !mod && isEnabled(f);
            boolean chosen = !mod && selected == f;
            boolean hover = Ui.inside(mx, my, x, y, cardW, rowH);
            int bg = chosen ? 0xFF24364A : hover ? SURFACE_HOVER : SURFACE;
            Ui.rounded(g, x, y, cardW, rowH, 7, bg);
            if (chosen) g.fill(x, y + 9, x + 3, y + rowH - 9, Ui.accent());
            String label = mod ? installedMods.get(i).getMetadata().getName() : f.label;
            String sub = mod ? installedMods.get(i).getMetadata().getId() + "  ·  " + installedMods.get(i).getMetadata().getVersion().getFriendlyString() : Ui.fit(font, f.description, cardW - 86);
            Ui.rounded(g, x + 12, y + 12, 32, 32, 7, mod ? 0xFF26384A : enabled ? Ui.alpha(Ui.accent(), 36) : 0xFF26303D);
            String glyph = label.isEmpty() ? "N" : label.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
            Ui.textScaled(g, font, glyph, x + 23, y + 21, enabled ? Ui.accent() : Ui.TEXT, 1.1F);
            Ui.text(g, font, Ui.fit(font, label, cardW - (mod ? 60 : 104)), x + 54, y + 16, Ui.TEXT);
            Ui.text(g, font, Ui.fit(font, sub, cardW - (mod ? 60 : 104)), x + 54, y + 35, Ui.MUTED);
            if (!mod) drawSwitch(g, x + cardW - 48, y + 21, enabled);
        }
        if (total > listH) {
            int tx = mainX + mainW - 3, th = listH;
            g.fill(tx, listY, tx + 2, listY + th, 0x263C4C61);
            int thumb = Math.max(22, (int)((float)listH / total * th));
            int ty = listY + (int)((th - thumb) * (scroll / max));
            Ui.rounded(g, tx - 1, ty, 4, thumb, 2, Ui.accent());
        }
    }

    private void drawDetails(GuiGraphicsExtractor g) {
        int y = panelY + 78;
        Ui.rounded(g, detailX, y, detailW, panelH - 108, 9, 0xFF101823);
        Ui.text(g, font, "MODULE DETAILS", detailX + 16, y + 18, Ui.FAINT);
        if (tab == Tab.MODS) {
            Ui.textScaled(g, font, "MODS", detailX + 16, y + 48, Ui.TEXT, 1.25F);
            Ui.text(g, font, installedMods.size() + " mods detected", detailX + 16, y + 72, Ui.MUTED);
            Ui.text(g, font, "This panel lists the", detailX + 16, y + 112, Ui.MUTED);
            Ui.text(g, font, "mods loaded by Fabric", detailX + 16, y + 125, Ui.MUTED);
            Ui.text(g, font, "for this game profile.", detailX + 16, y + 138, Ui.MUTED);
            return;
        }
        if (selected == null) {
            Ui.text(g, font, "Nothing selected", detailX + 16, y + 54, Ui.MUTED);
            return;
        }
        boolean enabled = isEnabled(selected);
        Ui.rounded(g, detailX + 16, y + 48, 42, 42, 9, Ui.alpha(Ui.accent(), 34));
        String glyph = selected.label.isEmpty() ? "N" : selected.label.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
        Ui.textScaled(g, font, glyph, detailX + 31, y + 59, Ui.accent(), 1.35F);
        Ui.text(g, font, Ui.fit(font, selected.label, detailW - 32), detailX + 16, y + 108, Ui.TEXT);
        Ui.text(g, font, enabled ? "STATUS  ·  ENABLED" : "STATUS  ·  DISABLED", detailX + 16, y + 130, enabled ? Ui.accent() : Ui.MUTED);
        g.fill(detailX + 16, y + 151, detailX + detailW - 16, y + 152, LINE);
        Ui.text(g, font, "ABOUT", detailX + 16, y + 170, Ui.FAINT);
        String[] words = selected.description.split(" ");
        String line = "";
        int yy = y + 190;
        for (String word : words) {
            String next = line.isEmpty() ? word : line + " " + word;
            if (font.width(next) > detailW - 32) {
                Ui.text(g, font, line, detailX + 16, yy, Ui.MUTED);
                yy += 13;
                line = word;
            } else line = next;
            if (yy > y + 250) break;
        }
        if (!line.isEmpty() && yy <= y + 250) Ui.text(g, font, line, detailX + 16, yy, Ui.MUTED);
        int by = y + panelH - 108 - 54;
        Ui.rounded(g, detailX + 16, by, detailW - 32, 34, 6, enabled ? 0xFF29394B : Ui.accent());
        String action = enabled ? "DISABLE MODULE" : "ENABLE MODULE";
        Ui.text(g, font, action, detailX + (detailW - font.width(action)) / 2, by + 13, enabled ? Ui.TEXT : 0xFF07111C);
    }

    private static void drawSwitch(GuiGraphicsExtractor g, int x, int y, boolean enabled) {
        Ui.rounded(g, x, y, 34, 17, 9, enabled ? Ui.alpha(Ui.accent(), 105) : 0xFF263243);
        Ui.rounded(g, enabled ? x + 19 : x + 2, y + 2, 13, 13, 7, enabled ? Ui.accent() : 0xFF8A98A9);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double mx = event.x(), my = event.y();
        int y = panelY + 108;
        for (Tab t : Tab.values()) {
            if (Ui.inside(mx, my, panelX + 12, y, railW - 24, 32)) {
                tab = t;
                if (VloxConfig.flag(Features.MENU_REMEMBER)) lastTab = t;
                refreshModules();
                return true;
            }
            y += 38;
        }
        int sx = mainX + mainW - 176;
        if (Ui.inside(mx, my, sx, panelY + 171, 176, 28)) { searchFocused = true; return true; }
        searchFocused = false;
        int settingsY = panelY + panelH - 58;
        if (Ui.inside(mx, my, panelX + 12, settingsY, railW - 24, 38)) {
            Minecraft.getInstance().setScreenAndShow(new VloxScreen());
            return true;
        }
        if (tab == Tab.MODS && Ui.inside(mx, my, mainX, listY, mainW, listH)) {
            int col = (int)((mx - mainX) / (cardW + 12));
            int row = (int)((my - listY + scroll) / 67);
            int idx = row * columns + col;
            if (col >= 0 && col < columns && idx >= 0 && idx < installedMods.size()) return true;
        }
        if (tab != Tab.MODS && Ui.inside(mx, my, mainX, listY, mainW, listH)) {
            int col = (int)((mx - mainX) / (cardW + 12));
            int row = (int)((my - listY + scroll) / 67);
            int idx = row * columns + col;
            if (col >= 0 && col < columns && idx >= 0 && idx < modules.size()) {
                Feature f = modules.get(idx);
                int cardX = mainX + col * (cardW + 12);
                int cardY = listY + row * 67 - Math.round(scroll);
                if (Ui.inside(mx, my, cardX, cardY, cardW, 58)) {
                    selected = f;
                    if (mx >= cardX + cardW - 56) toggle(f);
                    return true;
                }
            }
        }
        int by = panelY + 78 + panelH - 108 - 54;
        if (tab != Tab.MODS && selected != null && Ui.inside(mx, my, detailX + 16, by, detailW - 32, 34)) {
            toggle(selected);
            return true;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (vertical == 0 || !Ui.inside(mouseX, mouseY, mainX, listY, mainW, listH)) return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
        int count = tab == Tab.MODS ? installedMods.size() : modules.size();
        int rows = (count + columns - 1) / columns;
        float max = Math.max(0, rows * 67 - listH);
        scroll = Math.max(0, Math.min(max, scroll - (float)vertical * 42));
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
    public void removed() { open = false; super.removed(); }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
