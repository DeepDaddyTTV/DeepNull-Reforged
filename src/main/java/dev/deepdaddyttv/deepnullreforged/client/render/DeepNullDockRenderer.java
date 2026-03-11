package dev.deepdaddyttv.deepnullreforged.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class DeepNullDockRenderer implements BlockEntityRenderer<DeepNullDockBlockEntity> {
    public DeepNullDockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DeepNullDockBlockEntity dock, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack storedDeepNull = dock.getStoredDeepNull();
        if (storedDeepNull.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.4D, 0.5D);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        minecraft.getItemRenderer().renderStatic(storedDeepNull, ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, buffer, dock.getLevel(), 0);
        poseStack.popPose();
    }
}
