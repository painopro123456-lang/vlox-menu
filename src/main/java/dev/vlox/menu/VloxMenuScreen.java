package dev.vlox.menu;

import net.minecraft.client.gui.GuiGraphics;
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

    public VloxMenuScreen() {
        super(Component.literal("VLOX MENU"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        graphics.fill(0, 0, this.width, this.height, 0x55050912);

        // Translucent smoked-glass panel with a subtle blue edge.
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xD91B2230);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 1, 0x887DAFFF);
        graphics.fill(left, top, left + 1, top + PANEL_HEIGHT, 0x557DAFFF);
        graphics.fill(left + PANEL_WIDTH - 1, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0x557DAFFF);
        graphics.fill(left, top + PANEL_HEIGHT - 1, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0x557DAFFF);

        graphics.drawString(this.font, "VLOX MENU", left + 22, top + 18, 0xFFF4F7FF, false);
        graphics.drawString(this.font, "26.2  /  FABRIC", left + 22, top + 34, 0xFF91A5C8, false);
        graphics.drawString(this.font, "RIGHT SHIFT  •  CLOSE", left + PANEL_WIDTH - 150, top + 23, 0xFF91A5C8, false);

        graphics.fill(left + 20, top + 58, left + PANEL_WIDTH - 20, top + 83, 0x88343E50);
        graphics.drawString(this.font, "⌕   MODULES", left + 30, top + 67, 0xFFB9C7DF, false);

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

            graphics.drawString(this.font, module.icon(), x + 10, y + 10, 0xFFD8E5FF, false);
            graphics.drawString(this.font, module.name(), x + 31, y + 10, 0xFFF3F6FC, false);
            graphics.drawString(this.font, module.category(), x + 31, y + 29, 0xFF9EADC5, false);

            int toggleColor = module.enabled() ? 0xFF91B9FF : 0xFF657187;
            graphics.fill(x + CARD_WIDTH - 28, y + 25, x + CARD_WIDTH - 12, y + 34, toggleColor);
            graphics.fill(module.enabled() ? x + CARD_WIDTH - 18 : x + CARD_WIDTH - 27,
                y + 26, module.enabled() ? x + CARD_WIDTH - 12 : x + CARD_WIDTH - 18, y + 33, 0xFFFFFFFF);
        }

        graphics.drawString(this.font, "Click a module to toggle it", left + 22,
            top + PANEL_HEIGHT - 22, 0xFF91A1BA, false);

        if (pageCount > 1) {
            String pageLabel = "PAGE " + (page + 1) + " / " + pageCount;
            graphics.drawString(this.font, "< PREV", left + PANEL_WIDTH - 150,
                top + PANEL_HEIGHT - 22, page > 0 ? 0xFFD8E5FF : 0xFF68758A, false);
            graphics.drawString(this.font, pageLabel, left + PANEL_WIDTH - 91,
                top + PANEL_HEIGHT - 22, 0xFF91A1BA, false);
            graphics.drawString(this.font, "NEXT >", left + PANEL_WIDTH - 48,
                top + PANEL_HEIGHT - 22, page < pageCount - 1 ? 0xFFD8E5FF : 0xFF68758A, false);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int panelLeft = (this.width - PANEL_WIDTH) / 2;
            int panelTop = (this.height - PANEL_HEIGHT) / 2;
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
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
