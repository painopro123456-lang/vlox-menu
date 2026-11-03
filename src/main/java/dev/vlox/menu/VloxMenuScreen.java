package dev.vlox.menu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class VloxMenuScreen extends Screen {
    private static final int PANEL_WIDTH = 570;
    private static final int PANEL_HEIGHT = 430;
    private static final int CARD_WIDTH = 170;
    private static final int CARD_HEIGHT = 70;
    private static final int GAP = 7;
    private static final int MODULES_PER_PAGE = 12;
    private int page;
    private VloxModule selectedModule;
    private boolean waitingForKeybind;
    private String searchQuery = "";
    private boolean searchFocused;

    private void drawUiText(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean shadow) {
        graphics.text(this.font, text, x, y, color, shadow);
    }

    public VloxMenuScreen() {
        super(Component.literal("VLOX MENU"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        graphics.fill(0, 0, this.width, this.height, 0x32101A2B);
        graphics.fill(left - 5, top - 5, left + PANEL_WIDTH + 5, top + PANEL_HEIGHT + 5, 0x180B1728);

        // Layered frosted-glass shell with a cool blue edge and soft inner highlight.
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xDDF5F8FD);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 2, 0xDFFFFFFF);
        graphics.fill(left, top, left + 1, top + PANEL_HEIGHT, 0x88FFFFFF);
        graphics.fill(left + PANEL_WIDTH - 1, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0x66FFFFFF);
        graphics.fill(left, top + PANEL_HEIGHT - 1, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0x66FFFFFF);
        graphics.fill(left + 2, top + 2, left + PANEL_WIDTH - 2, top + 3, 0x66FFFFFF);
        graphics.fill(left + 18, top + 13, left + 21, top + 43, 0xFF5D9AD8);
        graphics.fill(left + 26, top + 45, left + PANEL_WIDTH - 20, top + 46, 0x337B9BC0);

        graphics.text(this.font, "VLOX", left + 30, top + 13, 0xFF1C2F48, false);
        graphics.text(this.font, "MENU  /  26.2 FABRIC", left + 30, top + 29, 0xFF71839A, false);
        int enabledCount = (int) VloxModules.ALL.stream().filter(VloxModule::enabled).count();
        String activeLabel = enabledCount + " ACTIVE";
        int activeWidth = this.font.width(activeLabel);
        graphics.fill(left + PANEL_WIDTH - activeWidth - 65, top + 17, left + PANEL_WIDTH - 20, top + 38, 0xDDE7F0F8);
        graphics.fill(left + PANEL_WIDTH - activeWidth - 65, top + 17, left + PANEL_WIDTH - 63, top + 38, 0xFF69A1D8);
        graphics.text(this.font, activeLabel, left + PANEL_WIDTH - activeWidth - 55, top + 23, 0xFF426A91, false);
        graphics.text(this.font, "RIGHT SHIFT TO CLOSE", left + PANEL_WIDTH - 150, top + 43, 0xFF8799AE, false);

        graphics.fill(left + 20, top + 57, left + PANEL_WIDTH - 20, top + 84, 0xBFEAF0F7);
        graphics.fill(left + 20, top + 57, left + PANEL_WIDTH - 20, top + 58, 0xFFFFFFFF);
        graphics.fill(left + 20, top + 83, left + PANEL_WIDTH - 20, top + 84, searchFocused ? 0xFF5A96D5 : 0x557D9DBD);
        graphics.text(this.font, "⌕", left + 30, top + 66, 0xFF6284A7, false);
        String searchLabel = searchQuery.isEmpty() ? "Search modules or categories" : searchQuery;
        graphics.text(this.font, searchLabel, left + 47, top + 66, searchFocused ? 0xFF294F78 : 0xFF53647C, false);
        if (!searchQuery.isEmpty()) graphics.text(this.font, "×", left + PANEL_WIDTH - 37, top + 65, 0xFF6483A3, false);
        if (searchFocused) graphics.fill(left + 47 + this.font.width(searchLabel), top + 65, left + 48 + this.font.width(searchLabel), top + 77, 0xFF477DBD);

        List<VloxModule> modules = visibleModules();
        int startX = left + 20;
        int startY = top + 94;
        int pageCount = Math.max(1, (modules.size() + MODULES_PER_PAGE - 1) / MODULES_PER_PAGE);
        int first = page * MODULES_PER_PAGE;
        int end = Math.min(first + MODULES_PER_PAGE, modules.size());

        // Three cards per row; additional modules are shown on the next page.
        for (int index = first; index < end; index++) {
            VloxModule module = modules.get(index);
            int visibleIndex = index - first;
            int column = visibleIndex % 3;
            int row = visibleIndex / 3;
            int x = startX + column * (CARD_WIDTH + GAP);
            int y = startY + row * (CARD_HEIGHT + GAP);
            boolean hovered = mouseX >= x && mouseX <= x + CARD_WIDTH
                && mouseY >= y && mouseY <= y + CARD_HEIGHT;

            // Vertical icon-first tile with a centered title underneath.
            graphics.fill(x + 1, y + 3, x + CARD_WIDTH + 1, y + CARD_HEIGHT + 3, 0x220E2745);
            int background = module.enabled() ? 0xF0E5F0FC : (hovered ? 0xF0FFFFFF : 0xDDEFF4FA);
            graphics.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, background);
            graphics.fill(x, y, x + CARD_WIDTH, y + 1, 0xFFFFFFFF);
            graphics.fill(x, y + CARD_HEIGHT - 1, x + CARD_WIDTH, y + CARD_HEIGHT, hovered ? 0xAA78A5D2 : 0x557E9AB8);
            graphics.fill(x, y, x + 3, y + CARD_HEIGHT, module.enabled() ? 0xFF4F91D2 : 0x668DA4C0);
            if (hovered) graphics.fill(x + 3, y + 1, x + CARD_WIDTH - 1, y + 2, 0x6678A9D8);

            int iconCenter = x + CARD_WIDTH / 2;
            int iconTileLeft = iconCenter - 18;
            int iconTileTop = y + 5;
            graphics.fill(iconTileLeft - 1, iconTileTop + 1, iconTileLeft + 37, iconTileTop + 35, 0x1D547AA4);
            graphics.fill(iconTileLeft, iconTileTop, iconTileLeft + 36, iconTileTop + 34,
                module.enabled() ? 0xFFD4E8FC : 0xFFE5EDF6);
            graphics.fill(iconTileLeft, iconTileTop, iconTileLeft + 36, iconTileTop + 1, 0xFFFFFFFF);
            String icon = module.icon();
            int iconWidth = this.font.width(icon);
            graphics.pose().pushMatrix();
            graphics.pose().translate(iconCenter - iconWidth * 0.9f, iconTileTop + 7);
            graphics.pose().scale(1.8f, 1.8f);
            drawUiText(graphics, icon, 0, 0,
                module.enabled() ? 0xFF286DB5 : 0xFF506B89, false);
            graphics.pose().popMatrix();

            // Always-visible settings gear anchored to the icon tile's bottom-right corner.
            int gearX = iconTileLeft + 24;
            int gearY = iconTileTop + 22;
            graphics.fill(gearX - 2, gearY - 2, gearX + 13, gearY + 13, 0xFFEAF3FC);
            graphics.fill(gearX - 2, gearY - 2, gearX + 13, gearY - 1, 0xFFFFFFFF);
            drawUiText(graphics, "⚙", gearX, gearY, 0xFF426F9E, false);

            String moduleName = module.name();
            while (this.font.width(moduleName) > CARD_WIDTH - 16 && moduleName.length() > 4) moduleName = moduleName.substring(0, moduleName.length() - 2) + "…";
            int nameWidth = this.font.width(moduleName);
            drawUiText(graphics, moduleName, x + Math.max(5, (CARD_WIDTH - nameWidth) / 2), y + 43,
                0xFF1D3048, false);
            String category = module.category().toUpperCase();
            int categoryWidth = this.font.width(category);
            drawUiText(graphics, category, x + (CARD_WIDTH - categoryWidth) / 2, y + 56,
                0xFF71839A, false);

            int pillLeft = x + CARD_WIDTH - 31;
            int pillTop = y + 5;
            graphics.fill(pillLeft, pillTop, pillLeft + 25, pillTop + 10,
                module.enabled() ? 0xFF548DD0 : 0xFF9EACBD);
            drawUiText(graphics, module.enabled() ? "ON" : "OFF",
                pillLeft + 4, pillTop + 1, 0xFFFFFFFF, false);
        }

                if (selectedModule != null) {
            int mx = left + 120;
            int my = top + 105;
            graphics.fill(mx + 2, my + 3, mx + 332, my + 208, 0x300D2745);
            graphics.fill(mx, my, mx + 330, my + 205, 0xE0F9FCFF);
            graphics.fill(mx, my, mx + 330, my + 3, 0xFF6E9FD6);
            graphics.text(this.font, selectedModule.name() + " SETTINGS", mx + 16, my + 14, 0xFF20344D, false);
            graphics.text(this.font, "Scale", mx + 16, my + 49, 0xFF40546D, false);
            graphics.text(this.font, String.format("%.2f", selectedModule.scale()), mx + 165, my + 49, 0xFF315F91, false);
            graphics.fill(mx + 230, my + 42, mx + 267, my + 62, 0xFFE6EFF8);
            graphics.fill(mx + 273, my + 42, mx + 310, my + 62, 0xFFE6EFF8);
            graphics.text(this.font, "−", mx + 244, my + 48, 0xFF315F91, false);
            graphics.text(this.font, "+", mx + 287, my + 48, 0xFF315F91, false);
            graphics.text(this.font, "Opacity", mx + 16, my + 76, 0xFF40546D, false);
            graphics.text(this.font, String.format("%.2f", selectedModule.opacity()), mx + 165, my + 76, 0xFF315F91, false);
            graphics.fill(mx + 230, my + 69, mx + 267, my + 89, 0xFFE6EFF8);
            graphics.fill(mx + 273, my + 69, mx + 310, my + 89, 0xFFE6EFF8);
            graphics.text(this.font, "−", mx + 244, my + 75, 0xFF315F91, false);
            graphics.text(this.font, "+", mx + 287, my + 75, 0xFF315F91, false);
            graphics.text(this.font, "Position  " + selectedModule.offsetX() + ", " + selectedModule.offsetY(), mx + 16, my + 103, 0xFF40546D, false);
            String[] directions = {"←", "↑", "↓", "→"};
            int[] dxs = {-5, 0, 0, 5};
            int[] dys = {0, -5, 5, 0};
            for (int i = 0; i < 4; i++) {
                int bx = mx + 170 + i * 36;
                graphics.fill(bx, my + 92, bx + 31, my + 113, 0xFFE6EFF8);
                graphics.text(this.font, directions[i], bx + 10, my + 98, 0xFF315F91, false);
            }
            String keyLabel = waitingForKeybind ? "PRESS A KEY... (ESC CANCEL / BACKSPACE UNBIND)" : "KEYBIND  " + (selectedModule.keyCode() < 0 ? "UNBOUND" : selectedModule.keyCode()) + "   /   CLICK TO CHANGE";
            graphics.text(this.font, keyLabel, mx + 16, my + 137, waitingForKeybind ? 0xFFB56A28 : 0xFF405F80, false);
            graphics.fill(mx + 14, my + 160, mx + 316, my + 193, 0xFFE5EFF8);
            graphics.text(this.font, "CLOSE SETTINGS", mx + 16, my + 172, 0xFF477DBD, false);
        }

        graphics.text(this.font, "CLICK MODULE: TOGGLE     GEAR: SETTINGS", left + 22,
            top + PANEL_HEIGHT - 22, 0xFF718097, false);

        if (pageCount > 1) {
            String pageLabel = "PAGE " + (page + 1) + " / " + pageCount;
            graphics.text(this.font, "< PREV", left + PANEL_WIDTH - 150,
                top + PANEL_HEIGHT - 22, page > 0 ? 0xFFD8E5FF : 0xFFA0AAB7, false);
            graphics.text(this.font, pageLabel, left + PANEL_WIDTH - 91,
                top + PANEL_HEIGHT - 22, 0xFF91A1BA, false);
            graphics.text(this.font, "NEXT >", left + PANEL_WIDTH - 48,
                top + PANEL_HEIGHT - 22, page < pageCount - 1 ? 0xFFD8E5FF : 0xFF68758A, false);
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - PANEL_HEIGHT) / 2;
        if (selectedModule != null && event.button() == 0) {
            int mx = panelLeft + 120;
            int my = panelTop + 105;
            if (mouseX >= mx && mouseX <= mx + 330 && mouseY >= my && mouseY <= my + 205) {
                if (mouseY >= my + 39 && mouseY < my + 64) {
                    if (mouseX >= mx + 228 && mouseX < mx + 270) selectedModule.setScale(selectedModule.scale() - 0.1f);
                    else if (mouseX >= mx + 271 && mouseX < mx + 313) selectedModule.setScale(selectedModule.scale() + 0.1f);
                } else if (mouseY >= my + 66 && mouseY < my + 91) {
                    if (mouseX >= mx + 228 && mouseX < mx + 270) selectedModule.setOpacity(selectedModule.opacity() - 0.1f);
                    else if (mouseX >= mx + 271 && mouseX < mx + 313) selectedModule.setOpacity(selectedModule.opacity() + 0.1f);
                } else if (mouseY >= my + 91 && mouseY < my + 116) {
                    int direction = (int) ((mouseX - (mx + 170)) / 36);
                    if (direction >= 0 && direction < 4) {
                        int[] dx = {-5, 0, 0, 5};
                        int[] dy = {0, -5, 5, 0};
                        selectedModule.setPosition(selectedModule.offsetX() + dx[direction], selectedModule.offsetY() + dy[direction]);
                    }
                } else if (mouseY >= my + 120 && mouseY < my + 151) {
                    waitingForKeybind = true;
                } else if (mouseY >= my + 158 && mouseY < my + 198) {
                    selectedModule = null;
                    waitingForKeybind = false;
                }
                return true;
            }
            return true;
        }        if (event.button() == 0) {
            if (mouseX >= panelLeft + 20 && mouseX <= panelLeft + PANEL_WIDTH - 20 && mouseY >= panelTop + 57 && mouseY <= panelTop + 84) {
                if (mouseX >= panelLeft + PANEL_WIDTH - 43 && !searchQuery.isEmpty()) { searchQuery = ""; page = 0; }
                else searchFocused = true;
                return true;
            }
            searchFocused = false;
            List<VloxModule> modules = visibleModules();
            int pageCount = Math.max(1, (modules.size() + MODULES_PER_PAGE - 1) / MODULES_PER_PAGE);

            // Page controls in the bottom-right footer.
            if (pageCount > 1 && mouseY >= panelTop + PANEL_HEIGHT - 30) {
                if (mouseX >= panelLeft + PANEL_WIDTH - 153 && mouseX < panelLeft + PANEL_WIDTH - 92 && page > 0) {
                    page--;
                    return true;
                }
                if (mouseX >= panelLeft + PANEL_WIDTH - 49 && mouseX <= panelLeft + PANEL_WIDTH - 12
                    && page < pageCount - 1) {
                    page++;
                    return true;
                }
            }

            int startX = panelLeft + 20;
            int startY = panelTop + 94;
            int first = page * MODULES_PER_PAGE;
            int end = Math.min(first + MODULES_PER_PAGE, modules.size());

            for (int index = first; index < end; index++) {
                int visibleIndex = index - first;
                int column = visibleIndex % 3;
                int row = visibleIndex / 3;
                int x = startX + column * (CARD_WIDTH + GAP);
                int y = startY + row * (CARD_HEIGHT + GAP);

                if (mouseX >= x && mouseX <= x + CARD_WIDTH
                    && mouseY >= y && mouseY <= y + CARD_HEIGHT) {
                    int iconLeft = x + CARD_WIDTH / 2 - 18;
                    int iconTop = y + 5;
                    int gearLeft = iconLeft + 22;
                    int gearTop = iconTop + 20;
                    if (mouseX >= gearLeft - 3 && mouseX <= gearLeft + 14
                        && mouseY >= gearTop - 3 && mouseY <= gearTop + 14) {
                        selectedModule = modules.get(index);
                        waitingForKeybind = false;
                    } else {
                        modules.get(index).toggle();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private List<VloxModule> visibleModules() {
        if (searchQuery.isBlank()) return VloxModules.ALL;
        String query = searchQuery.toLowerCase(java.util.Locale.ROOT);
        return VloxModules.ALL.stream().filter(module -> module.name().toLowerCase(java.util.Locale.ROOT).contains(query)
            || module.category().toLowerCase(java.util.Locale.ROOT).contains(query)).toList();
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (searchFocused && selectedModule == null && !waitingForKeybind) {
            int codePoint = event.codepoint();
            if (!Character.isISOControl(codePoint) && Character.isValidCodePoint(codePoint)) {
                searchQuery += new String(Character.toChars(codePoint));
                page = 0;
                return true;
            }
        }
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (selectedModule == null && searchFocused) {
            if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.offsetByCodePoints(searchQuery.length(), -1));
                page = 0;
                return true;
            }
            if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) { searchFocused = false; return true; }
        }
        if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE && selectedModule != null) {
            selectedModule = null; waitingForKeybind = false; return true;
        }
        return super.keyPressed(event);
    }
    public boolean waitingForKeybind() {
        return waitingForKeybind && selectedModule != null;
    }

    public void captureKeybind(int keyCode) {
        if (selectedModule == null || !waitingForKeybind) return;
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            waitingForKeybind = false;
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE) {
            selectedModule.setKeyCode(-1);
            waitingForKeybind = false;
        } else if (keyCode >= 0 && keyCode != org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) {
            selectedModule.setKeyCode(keyCode);
            waitingForKeybind = false;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
