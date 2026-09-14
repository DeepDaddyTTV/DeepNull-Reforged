package dev.deepdaddyttv.deepnullreforged.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.deepdaddyttv.deepnullreforged.block.NullWorkbenchBlock;
import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class NullWorkbenchRenderer implements BlockEntityRenderer<NullWorkbenchBlockEntity, NullWorkbenchRenderer.RenderState> {
    private static final float DISPLAY_SCALE = 0.85F;
    private static final double TABLETOP_Y = 12.0D / 16.0D;
    private final ItemModelResolver itemModelResolver;

    public NullWorkbenchRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            NullWorkbenchBlockEntity workbench,
            RenderState state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(workbench, state, partialTick, cameraPosition, breakProgress);
        state.item.clear();
        ItemStack stack = workbench.getStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT);
        state.hasItem = !stack.isEmpty();
        if (!state.hasItem) {
            return;
        }
        Direction facing = workbench.getBlockState().getValue(NullWorkbenchBlock.FACING);
        Direction extensionDirection = facing.getCounterClockWise();
        state.rotation = facing.toYRot();
        state.offsetX = extensionDirection.getStepX();
        state.offsetZ = extensionDirection.getStepZ();
        itemModelResolver.updateForTopItem(
                state.item,
                stack,
                ItemDisplayContext.FIXED,
                workbench.getLevel(),
                null,
                workbench.getBlockPos().hashCode()
        );
        state.centerY = displayCenterY(state.item.getModelBoundingBox());
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.hasItem) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5D + state.offsetX, state.centerY, 0.5D + state.offsetZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.rotation));
        poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        // The display is on the neighboring half, not inside the owning block.
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(NullWorkbenchBlockEntity workbench) {
        return displayBounds(workbench.getBlockPos(), workbench.getBlockState().getValue(NullWorkbenchBlock.FACING));
    }

    static AABB displayBounds(BlockPos mainPos, Direction facing) {
        Direction extensionDirection = facing.getCounterClockWise();
        return AABB.encapsulatingFullBlocks(mainPos, mainPos.relative(extensionDirection).above());
    }

    static double displayCenterY(AABB modelBounds) {
        return TABLETOP_Y + 0.01D - modelBounds.minY * DISPLAY_SCALE;
    }

    public static final class RenderState extends BlockEntityRenderState {
        private final ItemStackRenderState item = new ItemStackRenderState();
        private boolean hasItem;
        private float rotation;
        private int offsetX;
        private int offsetZ;
        private double centerY;
    }
}
