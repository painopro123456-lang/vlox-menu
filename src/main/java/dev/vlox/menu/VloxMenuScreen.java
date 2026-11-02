package dev.vlox.menu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class VloxMenuScreen extends Screen {
    private static final int PANEL_WIDTH = 570;
    private static final int PANEL_HEIGHT = 390;
    private static final int CARD_WIDTH = 170;
    private static final int CARD_HEIGHT = 60;
    private static final int GAP = 8;
    private static final int MODULES_PER_PAGE = 12;
    private int page;
    private VloxModule selectedModule;
    private boolean waitingForKeybind;

    public VloxMenuScreen() {
        super(Component.literal("VLOX MENU"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        graphics.fill(0, 0, this.width, this.height, 0x180D1420);

        // Light translucent glass surface: the world remains visible behind it.
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0x88F7FAFF);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 2, 0xDFFFFFFF);
        graphics.fill(left, top, left + 1, top + PANEL_HEIGHT, 0x88FFFFFF);
        graphics.fill(left + PANEL_WIDTH - 1, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0x66FFFFFF);
        graphics.fill(left, top + PANEL_HEIGHT - 1, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0x66FFFFFF);
        graphics.fill(left + 2, top + 2, left + PANEL_WIDTH - 2, top + 3, 0x44FFFFFF);

        graphics.text(this.font, "VLOX MENU", left + 22, top + 18, 0xFF243247, false);
        graphics.text(this.font, "26.2  /  FABRIC", left + 22, top + 34, 0xFF65758D, false);
        graphics.text(this.font, "RIGHT SHIFT  •  CLOSE", left + PANEL_WIDTH - 150, top + 23, 0xFF91A5C8, false);

        graphics.fill(left + 20, top + 58, left + PANEL_WIDTH - 20, top + 83, 0x66FFFFFF);
        graphics.fill(left + 20, top + 58, left + PANEL_WIDTH - 20, top + 59, 0x88FFFFFF);
        graphics.text(this.font, "⌕   MODULES", left + 30, top + 67, 0xFF53647C, false);

        List<VloxModule> modules = VloxModules.ALL;
        int startX = left + 20;
        int startY = top + 98;
        int pageCount = (modules.size() + MODULES_PER_PAGE - 1) / MODULES_PER_PAGE;
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

            // Soft layered card: a faint shadow, bright glass face and fine edge highlight.
            graphics.fill(x + 1, y + 2, x + CARD_WIDTH + 1, y + CARD_HEIGHT + 2, 0x180E2745);
            int background = module.enabled() ? 0xDDEAF8FF : (hovered ? 0xDDFEFFFF : 0xBFFFFFFF);
            graphics.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, background);
            graphics.fill(x, y, x + CARD_WIDTH, y + 1, 0xCCFFFFFF);
            graphics.fill(x, y + CARD_HEIGHT - 1, x + CARD_WIDTH, y + CARD_HEIGHT, 0x55849BB8);
            graphics.fill(x, y, x + 3, y + CARD_HEIGHT, module.enabled() ? 0xFF548DD0 : 0x668DA4C0);

            graphics.text(this.font, module.icon(), x + 11, y + 11, 0xFF42658C, false);
            graphics.text(this.font, module.name(), x + 31, y + 10, 0xFF1D3048, false);
            graphics.text(this.font, module.category().toUpperCase(), x + 31, y + 30, 0xFF71839A, false);

            // Compact switch with a contrasting thumb; active state uses the VLOX blue accent.
            int toggleLeft = x + CARD_WIDTH - 34;
            int toggleTop = y + 12;
            graphics.fill(toggleLeft, toggleTop, toggleLeft + 23, toggleTop + 12,
                module.enabled() ? 0xFF548DD0 : 0xFFB7C3D0);
            graphics.fill(module.enabled() ? toggleLeft + 12 : toggleLeft + 2,
                toggleTop + 2, module.enabled() ? toggleLeft + 21 : toggleLeft + 11,
                toggleTop + 10, 0xFFFFFFFF);
            graphics.text(this.font, module.enabled() ? "ON" : "OFF",
                x + CARD_WIDTH - 34, y + 39, module.enabled() ? 0xFF477EBB : 0xFF8795A7, false);
        }

        if (selectedModule != null) {
            int mx = left + 120;
            int my = top + 105;
            graphics.fill(mx + 2, my + 3, mx + 332, my + 208, 0x300D2745);
            graphics.fill(mx, my, mx + 330, my + 205, 0xF2F9FCFF);
            graphics.fill(mx, my, mx + 330, my + 3, 0xFF6E9FD6);
            graphics.fill(mx + 1, my + 3, mx + 329, my + 4, 0xAAFFFFFF);
            graphics.text(this.font, selectedModule.name() + " SETTINGS", mx + 16, my + 14, 0xFF20344D, false);
            graphics.text(this.font, selectedModule.category().toUpperCase() + "  /  MODULE OPTIONS", mx + 16, my + 29, 0xFF7A8BA0, false);
            graphics.fill(mx + 14, my + 39, mx + 316, my + 40, 0x55899DB5);
            graphics.text(this.font, "Scale", mx + 16, my + 49, 0xFF40546D, false);
            graphics.text(this.font, String.format("%.2f", selectedModule.scale()) + "     [ - ]  [ + ]", mx + 145, my + 49, 0xFF315F91, false);
            graphics.text(this.font, "Opacity", mx + 16, my + 76, 0xFF40546D, false);
            graphics.text(this.font, String.format("%.2f", selectedModule.opacity()) + "     [ - ]  [ + ]", mx + 145, my + 76, 0xFF315F91, false);
            graphics.text(this.font, "Position", mx + 16, my + 103, 0xFF40546D, false);
            graphics.text(this.font, selectedModule.offsetX() + ", " + selectedModule.offsetY() + "   [LEFT] [UP] [DOWN] [RIGHT]", mx + 100, my + 103, 0xFF315F91, false);
            String keyLabel = waitingForKeybind ? "PRESS A KEY... (ESC CANCEL / BACKSPACE UNBIND)" : "Keybind: " + (selectedModule.keyCode() < 0 ? "Unbound" : selectedModule.keyCode()) + "   [SET KEY]";
            graphics.text(this.font, keyLabel, mx + 16, my + 132, waitingForKeybind ? 0xFFB56A28 : 0xFF405F80, false);
            graphics.fill(mx + 14, my + 153, mx + 316, my + 154, 0x55899DB5);
            graphics.text(this.font, "[ CLOSE ]", mx + 16, my + 174, 0xFF477DBD, false);
        }

        graphics.text(this.font, "LEFT CLICK: TOGGLE     RIGHT CLICK: SETTINGS", left + 22,
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
                if (mouseY >= my + 35 && mouseY < my + 61) {
                    if (mouseX < mx + 170) selectedModule.setScale(selectedModule.scale() - 0.1f);
                    else selectedModule.setScale(selectedModule.scale() + 0.1f);
                } else if (mouseY >= my + 62 && mouseY < my + 88) {
                    if (mouseX < mx + 170) selectedModule.setOpacity(selectedModule.opacity() - 0.1f);
                    else selectedModule.setOpacity(selectedModule.opacity() + 0.1f);
                } else if (mouseY >= my + 89 && mouseY < my + 115) {
                    int section = Math.min(3, Math.max(0, (int) ((mouseX - mx) / 82.5)));
                    int dx = section == 0 ? -5 : section == 3 ? 5 : 0;
                    int dy = section == 1 ? -5 : section == 2 ? 5 : 0;
                    selectedModule.setPosition(selectedModule.offsetX() + dx, selectedModule.offsetY() + dy);
                } else if (mouseY >= my + 116 && mouseY < my + 145) {
                    waitingForKeybind = true;
                } else {
                    selectedModule = null;
                    waitingForKeybind = false;
                }
                return true;
            }
            return true;
        }
        if (event.button() == 1) {
            int startX = panelLeft + 20;
            int startY = panelTop + 98;
            int first = page * MODULES_PER_PAGE;
            int end = Math.min(first + MODULES_PER_PAGE, VloxModules.ALL.size());
            for (int index = first; index < end; index++) {
                int visibleIndex = index - first;
                int x = startX + (visibleIndex % 3) * (CARD_WIDTH + GAP);
                int y = startY + (visibleIndex / 3) * (CARD_HEIGHT + GAP);
                if (mouseX >= x && mouseX <= x + CARD_WIDTH && mouseY >= y && mouseY <= y + CARD_HEIGHT) {
                    selectedModule = VloxModules.ALL.get(index);
                    return true;
                }
            }
        }
        if (event.button() == 0) {
            int pageCount = (VloxModules.ALL.size() + MODULES_PER_PAGE - 1) / MODULES_PER_PAGE;

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
            int startY = panelTop + 98;
            int first = page * MODULES_PER_PAGE;
            int end = Math.min(first + MODULES_PER_PAGE, VloxModules.ALL.size());

            for (int index = first; index < end; index++) {
                int visibleIndex = index - first;
                int column = visibleIndex % 3;
                int row = visibleIndex / 3;
                int x = startX + column * (CARD_WIDTH + GAP);
                int y = startY + row * (CARD_HEIGHT + GAP);

                if (mouseX >= x && mouseX <= x + CARD_WIDTH
                    && mouseY >= y && mouseY <= y + CARD_HEIGHT) {
                    VloxModules.ALL.get(index).toggle();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
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
