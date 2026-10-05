package com.tech_monkey.fishtraps.screen;

import com.tech_monkey.fishtraps.FishTraps;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class FishTrapScreen extends AbstractContainerScreen<FishTrapScreenHandler> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            FishTraps.MOD_ID, "textures/gui/fish_trap_gui.png");

    public FishTrapScreen(FishTrapScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos,
                0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(graphics, mouseX, mouseY, partialTick);

        boolean openWater = this.menu.isOpenWater();
        int circleX = this.leftPos + this.imageWidth - 18;
        int circleY = this.topPos + 8;
        int radius = 5;
        drawFilledCircle(graphics, circleX, circleY, radius, openWater);

        int dx = mouseX - circleX;
        int dy = mouseY - circleY;
        if (dx * dx + dy * dy <= (radius + 2) * (radius + 2)) {
            Component tooltip = Component.translatable(openWater
                    ? "gui.fishtraps.open_water.yes"
                    : "gui.fishtraps.open_water.no");
            graphics.setTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        }
    }

    private static void drawFilledCircle(GuiGraphicsExtractor graphics, int centerX, int centerY,
                                         int radius, boolean openWater) {
        int color = openWater ? 0xFF55FF55 : 0xFFDD3333;
        int border = openWater ? 0xFF338833 : 0xFF881111;
        for (int y = -radius; y <= radius; y++) {
            int width = (int) Math.sqrt(radius * radius - y * y);
            graphics.fill(centerX - width, centerY + y, centerX + width + 1, centerY + y + 1, color);
        }
        graphics.outline(centerX - radius, centerY - radius, radius * 2 + 1, radius * 2 + 1, border);
    }
}
