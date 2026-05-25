package dev.deepdaddyttv.deepnullreforged.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.deepdaddyttv.deepnullreforged.block.entity.DripStandBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripNullData;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripSlotAssignment;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripSlotRef;
import dev.deepdaddyttv.deepnullreforged.item.DripNullItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class DripStandRenderer implements BlockEntityRenderer<DripStandBlockEntity> {
    public DripStandRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DripStandBlockEntity stand, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack stored = stand.getStoredDripNull();
        if (stored.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.12D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees((stand.getLevel() == null ? 0 : stand.getLevel().getGameTime() % 360) + partialTick));
        poseStack.scale(0.45F, 0.45F, 0.45F);
        minecraft.getItemRenderer().renderStatic(stored, ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, buffer, stand.getLevel(), 0);
        poseStack.popPose();

        if (!(stored.getItem() instanceof DripNullItem dripNullItem) || stand.getLevel() == null) {
            return;
        }
        DripNullData data = DripNullData.get(stored, dripNullItem.tier(), stand.getLevel().registryAccess());
        renderProfilePreview(minecraft, stand, data, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderProfilePreview(Minecraft minecraft, DripStandBlockEntity stand, DripNullData data, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int armorIndex = 0;
        boolean renderedHotbar = false;
        for (DripSlotAssignment assignment : data.selected().slots()) {
            ItemStack stack = data.vaultItems().getOrDefault(assignment.ref(), ItemStack.EMPTY);
            if (stack.isEmpty()) {
                continue;
            }
            if (DripSlotRef.ARMOR_PROVIDER.equals(assignment.slot().provider())) {
                renderSmallItem(minecraft, stand, stack, -0.45D + armorIndex * 0.3D, 0.72D, -0.18D, poseStack, buffer, packedLight, packedOverlay);
                armorIndex++;
            } else if (!renderedHotbar && DripSlotRef.INVENTORY_PROVIDER.equals(assignment.slot().provider()) && assignment.slot().slot() < 9) {
                renderSmallItem(minecraft, stand, stack, 0.0D, 0.38D, -0.42D, poseStack, buffer, packedLight, packedOverlay);
                renderedHotbar = true;
            }
        }
    }

    private static void renderSmallItem(Minecraft minecraft, DripStandBlockEntity stand, ItemStack stack, double x, double y, double z, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D + x, y, 0.5D + z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.32F, 0.32F, 0.32F);
        minecraft.getItemRenderer().renderStatic(stack.copyWithCount(1), ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, buffer, stand.getLevel(), 0);
        poseStack.popPose();
    }
}
