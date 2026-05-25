package dev.deepdaddyttv.deepnullreforged.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

final class GhostSlotRenderer {
    static final float GHOST_ALPHA = 0.58F;
    static final float PLANNED_ALPHA = 0.64F;
    static final float BLOCKED_ALPHA = 0.50F;

    private static final int GHOST_WASH = 0x33101824;
    private static final int BLOCKED_WASH = 0x44301818;

    private GhostSlotRenderer() {
    }

    static void renderContainedItem(GuiGraphics graphics, ItemStack stack, int x, int y, int size) {
        if (stack.isEmpty()) {
            return;
        }
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 150.0F);
        float scale = size / 16.0F;
        poseStack.scale(scale, scale, 1.0F);
        graphics.renderItem(stack.copyWithCount(1), 0, 0);
        poseStack.popPose();
    }

    static void renderGhostItem(GuiGraphics graphics, ItemStack stack, int x, int y, int size) {
        renderGhostItem(graphics, stack, x, y, size, GHOST_ALPHA);
    }

    static void renderGhostItem(GuiGraphics graphics, ItemStack stack, int x, int y, int size, float alpha) {
        if (stack.isEmpty()) {
            return;
        }
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        renderContainedItem(graphics, stack, x, y, size);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.fill(x, y, x + size, y + size, alpha <= BLOCKED_ALPHA ? BLOCKED_WASH : GHOST_WASH);
    }
}
