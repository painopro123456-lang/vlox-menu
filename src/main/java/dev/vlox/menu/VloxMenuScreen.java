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

        graphics.fill(0, 0, this.width, this.height, 0x55050912);

        // Translucent smoked-glass panel with a subtle blue edge.
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xD91B2230);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 1, 0x887DAFFF);
        graphics.fill(left, top, left + 1, top + PANEL_HEIGHT, 0x557DAFFF);
        graphics.fill(left + PANEL_WIDTH - 1, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0x557DAFFF);
        graphics.fill(left, top + PANEL_HEIGHT - 1, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0x557DAFFF);

        graphics.text(this.font, "VLOX MENU", left + 22, top + 18, 0xFFF4F7FF, false);
        graphics.text(this.font, "26.2  /  FABRIC", left + 22, top + 34, 0xFF91A5C8, false);
        graphics.text(this.font, "RIGHT SHIFT  •  CLOSE", left + PANEL_WIDTH - 150, top + 23, 0xFF91A5C8, false);

        graphics.fill(left + 20, top + 58, left + PANEL_WIDTH - 20, top + 83, 0x88343E50);
        graphics.text(this.font, "⌕   MODULES", left + 30, top + 67, 0xFFB9C7DF, false);

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

            int background = module.enabled() ? 0xCC344A69 : (hovered ? 0xAA354052 : 0x77303A4A);
            graphics.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, background);
            graphics.fill(x, y, x + 2, y + CARD_HEIGHT, module.enabled() ? 0xFF8DB5FF : 0x557D91B8);

            graphics.text(this.font, module.icon(), x + 10, y + 10, 0xFFD8E5FF, false);
            graphics.text(this.font, module.name(), x + 31, y + 10, 0xFFF3F6FC, false);
            graphics.text(this.font, module.category(), x + 31, y + 29, 0xFF9EADC5, false);

            int toggleColor = module.enabled() ? 0xFF91B9FF : 0xFF657187;
            graphics.fill(x + CARD_WIDTH - 28, y + 25, x + CARD_WIDTH - 12, y + 34, toggleColor);
            graphics.fill(module.enabled() ? x + CARD_WIDTH - 18 : x + CARD_WIDTH - 27,
                y + 26, module.enabled() ? x + CARD_WIDTH - 12 : x + CARD_WIDTH - 18, y + 33, 0xFFFFFFFF);
        }

        if (selectedModule != null) {
            int mx = left + 120;
            int my = top + 105;
            graphics.fill(mx, my, mx + 330, my + 205, 0xF01B2434);
            graphics.fill(mx, my, mx + 330, my + 2, 0xFF8DB5FF);
            graphics.text(this.font, selectedModule.name() + " SETTINGS", mx + 16, my + 14, 0xFFF4F7FF, false);
            graphics.text(this.font, "Scale: " + String.format("%.2f", selectedModule.scale()) + "   [-]  [+]", mx + 16, my + 43, 0xFFD8E5FF, false);
            graphics.text(this.font, "Opacity: " + String.format("%.2f", selectedModule.opacity()) + "   [-]  [+]", mx + 16, my + 70, 0xFFD8E5FF, false);
            graphics.text(this.font, "Position: " + selectedModule.offsetX() + ", " + selectedModule.offsetY() + "   [LEFT] [UP] [DOWN] [RIGHT]", mx + 16, my + 97, 0xFFD8E5FF, false);
            String keyLabel = waitingForKeybind ? "PRESS A KEY... (ESC CANCEL / BACKSPACE UNBIND)" : "Key code: " + (selectedModule.keyCode() < 0 ? "Unbound" : selectedModule.keyCode()) + "   [SET KEY]";
            graphics.text(this.font, keyLabel, mx + 16, my + 124, 0xFFD8E5FF, false);
            graphics.text(this.font, "[CLOSE]", mx + 16, my + 169, 0xFF91B9FF, false);
        }

        graphics.text(this.font, "LEFT CLICK: TOGGLE     RIGHT CLICK: SETTINGS", left + 22,
            top + PANEL_HEIGHT - 22, 0xFF91A1BA, false);

        if (pageCount > 1) {
            String pageLabel = "PAGE " + (page + 1) + " / " + pageCount;
            graphics.text(this.font, "< PREV", left + PANEL_WIDTH - 150,
                top + PANEL_HEIGHT - 22, page > 0 ? 0xFFD8E5FF : 0xFF68758A, false);
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
