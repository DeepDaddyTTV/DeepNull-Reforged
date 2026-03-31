package dev.deepdaddyttv.deepnullreforged.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class DeepNullDockRenderer implements BlockEntityRenderer<DeepNullDockBlockEntity, DeepNullDockRenderer.RenderState> {
    private final ItemModelResolver itemModelResolver;

    public DeepNullDockRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            DeepNullDockBlockEntity dock,
            RenderState state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(dock, state, partialTick, cameraPosition, breakProgress);
        state.item.clear();
        ItemStack storedDeepNull = dock.getStoredDeepNull();
        state.hasItem = !storedDeepNull.isEmpty();
        if (!state.hasItem) {
            return;
        }

        state.rotation = DeepNullConfig.animateDockedNulls()
                ? -((System.currentTimeMillis() % 24_000L) * 0.015F)
                : -35.0F;
        itemModelResolver.updateForTopItem(state.item, storedDeepNull, ItemDisplayContext.NONE, dock.getLevel(), null, dock.getBlockPos().hashCode());
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.hasItem) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.48D, 0.5D);
        poseStack.mulPose(Axis.of(new Vector3f(1.0F, 1.0F, 1.0F)).rotationDegrees(state.rotation));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static final class RenderState extends BlockEntityRenderState {
        private final ItemStackRenderState item = new ItemStackRenderState();
        private boolean hasItem;
        private float rotation;
    }
}
