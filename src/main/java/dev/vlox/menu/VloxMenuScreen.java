package dev.vlox.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class VloxMenuScreen extends Screen {
    private static final int PANEL_WIDTH = 570;
    private static final int PANEL_HEIGHT = 390;
    private static final int CARD_WIDTH = 170;
    private static final int CARD_HEIGHT = 66;
    private static final int GAP = 10;

    public VloxMenuScreen() {
        super(Component.literal("VLOX MENU"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        // Dim the game without hiding it; the panel itself uses translucent fills.
        graphics.fill(0, 0, this.width, this.height, 0x55050912);

        // Main frosted-glass panel.
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xD91B2230);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 1, 0x887DAFFF);
        graphics.fill(left, top, left + 1, top + PANEL_HEIGHT, 0x557DAFFF);
        graphics.fill(left + PANEL_WIDTH - 1, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0x557DAFFF);
        graphics.fill(left, top + PANEL_HEIGHT - 1, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0x557DAFFF);

        graphics.drawString(this.font, "VLOX MENU", left + 22, top + 18, 0xFFF4F7FF, false);
        graphics.drawString(this.font, "26.2  /  FABRIC", left + 22, top + 34, 0xFF91A5C8, false);
        graphics.drawString(this.font, "RIGHT SHIFT  •  CLOSE", left + PANEL_WIDTH - 150, top + 23, 0xFF91A5C8, false);

        // Search is a visual header element in this first foundation pass.
        graphics.fill(left + 20, top + 58, left + PANEL_WIDTH - 20, top + 83, 0x88343E50);
        graphics.drawString(this.font, "⌕   MODULES", left + 30, top + 67, 0xFFB9C7DF, false);

        List<VloxModule> modules = VloxModules.ALL;
        int startX = left + 20;
        int startY = top + 98;

        // Exactly three cards per row.
        for (int index = 0; index < modules.size(); index++) {
            VloxModule module = modules.get(index);
            int column = index % 3;
            int row = index / 3;
            int x = startX + column * (CARD_WIDTH + GAP);
            int y = startY + row * (CARD_HEIGHT + GAP);
            boolean hovered = mouseX >= x && mouseX <= x + CARD_WIDTH
                && mouseY >= y && mouseY <= y + CARD_HEIGHT;

            int background = module.enabled() ? 0xCC344A69 : (hovered ? 0xAA354052 : 0x77303A4A);
            graphics.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, background);
            graphics.fill(x, y, x + 2, y + CARD_HEIGHT, module.enabled() ? 0xFF8DB5FF : 0x557D91B8);

            graphics.drawString(this.font, module.icon(), x + 10, y + 12, 0xFFD8E5FF, false);
            graphics.drawString(this.font, module.name(), x + 31, y + 12, 0xFFF3F6FC, false);
            graphics.drawString(this.font, module.category(), x + 31, y + 31, 0xFF9EADC5, false);

            int toggleColor = module.enabled() ? 0xFF91B9FF : 0xFF657187;
            graphics.fill(x + CARD_WIDTH - 28, y + 27, x + CARD_WIDTH - 12, y + 36, toggleColor);
            graphics.fill(module.enabled() ? x + CARD_WIDTH - 18 : x + CARD_WIDTH - 27,
                y + 28, module.enabled() ? x + CARD_WIDTH - 12 : x + CARD_WIDTH - 18, y + 35, 0xFFFFFFFF);
        }

        graphics.drawString(this.font, "Click a module to toggle it", left + 22,
            top + PANEL_HEIGHT - 22, 0xFF91A1BA, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = (this.width - PANEL_WIDTH) / 2 + 20;
            int top = (this.height - PANEL_HEIGHT) / 2 + 98;

            for (int index = 0; index < VloxModules.ALL.size(); index++) {
                int column = index % 3;
                int row = index / 3;
                int x = left + column * (CARD_WIDTH + GAP);
                int y = top + row * (CARD_HEIGHT + GAP);

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
