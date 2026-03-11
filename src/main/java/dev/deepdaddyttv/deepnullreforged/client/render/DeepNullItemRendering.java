package dev.deepdaddyttv.deepnullreforged.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullContentMode;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullPanelItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3f;

public final class DeepNullItemRendering {
    private static final IClientItemExtensions EXTENSIONS = new IClientItemExtensions() {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return RendererHolder.get();
        }
    };

    private DeepNullItemRendering() {
    }

    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(
                EXTENSIONS,
                ModItems.REDSTONE_DEEP_NULL.get(),
                ModItems.LAPIS_DEEP_NULL.get(),
                ModItems.IRON_DEEP_NULL.get(),
                ModItems.GOLD_DEEP_NULL.get(),
                ModItems.DIAMOND_DEEP_NULL.get(),
                ModItems.EMERALD_DEEP_NULL.get(),
                ModItems.CREATIVE_DEEP_NULL.get()
        );
    }

    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (DeepNullTier tier : DeepNullTier.values()) {
            event.register(baseModelLocation(tier));
        }
    }

    private static RenderContents getRenderContents(ItemStack deepNullStack) {
        if (!(deepNullStack.getItem() instanceof DeepNullItem deepNullItem)) {
            return RenderContents.EMPTY;
        }

        Minecraft minecraft = Minecraft.getInstance();
        HolderLookup.Provider registries = minecraft.level == null ? null : minecraft.level.registryAccess();
        DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), deepNullStack, registries, null);
        return new RenderContents(inventory.getSelectedStack(), inventory.getSelectedFluid(), inventory.getContentMode());
    }

    private static final class RendererHolder {
        private static DeepNullItemRenderer INSTANCE;

        private static DeepNullItemRenderer get() {
            if (INSTANCE == null) {
                Minecraft minecraft = Minecraft.getInstance();
                BlockEntityRenderDispatcher dispatcher = minecraft.getBlockEntityRenderDispatcher();
                INSTANCE = new DeepNullItemRenderer(dispatcher);
            }
            return INSTANCE;
        }
    }

    private static final class DeepNullItemRenderer extends BlockEntityWithoutLevelRenderer {
        private DeepNullItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
            super(blockEntityRenderDispatcher, Minecraft.getInstance().getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
            Minecraft minecraft = Minecraft.getInstance();
            ItemRenderer itemRenderer = minecraft.getItemRenderer();
            BakedModel baseModel = getBaseModel(stack);
            boolean firstPerson = displayContext.firstPerson();
            RenderContents renderContents = getRenderContents(stack);

            if (firstPerson && baseModel != minecraft.getModelManager().getMissingModel()) {
                renderBaseModel(itemRenderer, baseModel, stack, poseStack, buffer, packedLight, packedOverlay);
            }

            if (renderContents.contentMode() == DeepNullContentMode.FLUIDS) {
                if (!renderContents.storedFluid().isEmpty() && displayContext != ItemDisplayContext.GUI) {
                    poseStack.pushPose();
                    applyContainedFluidTransform(poseStack, displayContext);
                    applyContainedFluidSpin(poseStack);
                    renderStoredFluid(renderContents.storedFluid(), poseStack, buffer, packedLight, packedOverlay);
                    poseStack.popPose();
                }
            } else if (!renderContents.selectedStack().isEmpty() && displayContext != ItemDisplayContext.GUI) {
                ItemStack selectedStack = renderContents.selectedStack();
                BakedModel selectedModel = itemRenderer.getModel(selectedStack, minecraft.level, minecraft.player, 0);
                poseStack.pushPose();
                boolean blockLike = isFullBlockItem(selectedStack);
                applyContainedItemTransform(poseStack, selectedStack, selectedModel, displayContext, blockLike);
                applyContainedItemSpin(poseStack, selectedStack, selectedModel, blockLike);
                renderSelectedItem(itemRenderer, selectedStack, selectedModel, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }

            if (baseModel != minecraft.getModelManager().getMissingModel()) {
                renderBaseModel(itemRenderer, baseModel, stack, poseStack, buffer, packedLight, packedOverlay);
            }
        }

        private static BakedModel getBaseModel(ItemStack stack) {
            if (!(stack.getItem() instanceof DeepNullItem deepNullItem)) {
                return Minecraft.getInstance().getModelManager().getMissingModel();
            }
            return Minecraft.getInstance().getModelManager().getModel(baseModelLocation(deepNullItem.tier()));
        }

        private static void renderBaseModel(
                ItemRenderer itemRenderer,
                BakedModel baseModel,
                ItemStack stack,
                PoseStack poseStack,
                MultiBufferSource buffer,
                int packedLight,
                int packedOverlay
        ) {
            for (BakedModel renderPass : baseModel.getRenderPasses(stack, true)) {
                for (RenderType renderType : renderPass.getRenderTypes(stack, true)) {
                    VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer, renderType, true, stack.hasFoil());
                    itemRenderer.renderModelLists(renderPass, stack, packedLight, packedOverlay, poseStack, vertexConsumer);
                }
            }
        }

        private static void renderSelectedItem(
                ItemRenderer itemRenderer,
                ItemStack selectedStack,
                BakedModel selectedModel,
                PoseStack poseStack,
                MultiBufferSource buffer,
                int packedLight,
                int packedOverlay
        ) {
            ItemStack renderStack = selectedStack.copyWithCount(1);
            if (selectedModel.isCustomRenderer()) {
                itemRenderer.render(
                        renderStack,
                        ItemDisplayContext.NONE,
                        false,
                        poseStack,
                        buffer,
                        packedLight,
                        packedOverlay,
                        selectedModel
                );
                return;
            }

            for (BakedModel renderPass : selectedModel.getRenderPasses(renderStack, true)) {
                for (RenderType renderType : renderPass.getRenderTypes(renderStack, true)) {
                    VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer, renderType, true, renderStack.hasFoil());
                    itemRenderer.renderModelLists(renderPass, renderStack, packedLight, packedOverlay, poseStack, vertexConsumer);
                }
            }
        }

        private static void applyContainedItemTransform(
                PoseStack poseStack,
                ItemStack selectedStack,
                BakedModel selectedModel,
                ItemDisplayContext displayContext,
                boolean blockLike
        ) {
            boolean firstPerson = displayContext.firstPerson();
            boolean customRendered = selectedModel.isCustomRenderer();

            if (blockLike) {
                poseStack.scale(0.4F, 0.4F, 0.4F);
                if (customRendered) {
                    if (firstPerson) {
                        poseStack.translate(1.25D, 2.0D, 1.25D);
                    } else {
                        poseStack.scale(1.1F, 1.1F, 1.1F);
                        poseStack.translate(1.25D, 1.4D, 1.25D);
                    }
                } else if (firstPerson) {
                    poseStack.translate(0.75D, 1.5D, 0.75D);
                } else {
                    poseStack.translate(0.75D, 0.9D, 0.75D);
                }
                return;
            }

            poseStack.scale(0.5F, 0.5F, 0.5F);
            if (customRendered) {
                if (firstPerson) {
                    poseStack.translate(0.75D, 2.0D, 1.0D);
                } else {
                    poseStack.scale(1.1F, 1.1F, 1.1F);
                    poseStack.translate(0.95D, 1.4D, 0.9D);
                }
            } else if (firstPerson) {
                poseStack.translate(0.5D, 1.5D, 0.5D);
            } else {
                poseStack.translate(0.5D, 0.9D, 0.5D);
            }
        }

        private static void applyContainedItemSpin(PoseStack poseStack, ItemStack selectedStack, BakedModel selectedModel, boolean blockLike) {
            float rotation = (Util.getMillis() % 24_000L) * 0.015F;
            boolean customRendered = selectedModel.isCustomRenderer();

            if (customRendered) {
                if (selectedStack.is(ModItems.DEEP_NULL_DOCK.get())) {
                    poseStack.translate(0.0D, 1.0D, 0.0D);
                } else if (!(selectedStack.getItem() instanceof DeepNullPanelItem) && !(selectedStack.getItem() instanceof BannerItem)) {
                    poseStack.translate(-0.1D, 0.0D, -0.1D);
                }
            }
            if (blockLike) {
                poseStack.translate(0.5D, 0.5D, 0.5D);
            }

            Axis rotationAxis = customRendered
                    ? Axis.of(new Vector3f(1.0F, Math.max(rotation, 1.0F), 1.0F))
                    : Axis.of(new Vector3f(1.0F, 1.0F, 1.0F));
            poseStack.mulPose(rotationAxis.rotationDegrees(rotation));

            if (blockLike) {
                poseStack.translate(-0.5D, -0.5D, -0.5D);
            }
        }

        private static void applyContainedFluidTransform(PoseStack poseStack, ItemDisplayContext displayContext) {
            boolean firstPerson = displayContext.firstPerson();
            poseStack.scale(firstPerson ? 0.55F : 0.5F, firstPerson ? 0.55F : 0.5F, firstPerson ? 0.55F : 0.5F);
            poseStack.translate(0.9D, firstPerson ? 1.18D : 0.9D, 0.9D);
        }

        private static void applyContainedFluidSpin(PoseStack poseStack) {
            float rotation = (Util.getMillis() % 24_000L) * 0.015F;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        }

        private static void renderStoredFluid(
                FluidStack storedFluid,
                PoseStack poseStack,
                MultiBufferSource buffer,
                int packedLight,
                int packedOverlay
        ) {
            IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(storedFluid.getFluid());
            ResourceLocation texture = clientFluid.getStillTexture(storedFluid);
            if (texture == null) {
                return;
            }

            TextureAtlasSprite sprite = FluidSpriteCache.getSprite(texture);
            int tint = clientFluid.getTintColor(storedFluid);
            if ((tint >>> 24) == 0) {
                tint |= 0xFF000000;
            }

            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(sprite.atlasLocation()));
            renderFluidCube(poseStack, vertexConsumer, sprite, tint, packedLight, packedOverlay);
        }

        private static void renderFluidCube(
                PoseStack poseStack,
                VertexConsumer vertexConsumer,
                TextureAtlasSprite sprite,
                int tint,
                int packedLight,
                int packedOverlay
        ) {
            PoseStack.Pose pose = poseStack.last();
            float minX = -0.28F;
            float maxX = 0.28F;
            float minY = -0.24F;
            float maxY = 0.32F;
            float minZ = -0.28F;
            float maxZ = 0.28F;

            addFluidQuad(vertexConsumer, pose,
                    minX, minY, maxZ,
                    maxX, minY, maxZ,
                    maxX, maxY, maxZ,
                    minX, maxY, maxZ,
                    sprite, tint, packedLight, packedOverlay, 0.0F, 0.0F, 1.0F);
            addFluidQuad(vertexConsumer, pose,
                    maxX, minY, minZ,
                    minX, minY, minZ,
                    minX, maxY, minZ,
                    maxX, maxY, minZ,
                    sprite, tint, packedLight, packedOverlay, 0.0F, 0.0F, -1.0F);
            addFluidQuad(vertexConsumer, pose,
                    minX, minY, minZ,
                    minX, minY, maxZ,
                    minX, maxY, maxZ,
                    minX, maxY, minZ,
                    sprite, tint, packedLight, packedOverlay, -1.0F, 0.0F, 0.0F);
            addFluidQuad(vertexConsumer, pose,
                    maxX, minY, maxZ,
                    maxX, minY, minZ,
                    maxX, maxY, minZ,
                    maxX, maxY, maxZ,
                    sprite, tint, packedLight, packedOverlay, 1.0F, 0.0F, 0.0F);
            addFluidQuad(vertexConsumer, pose,
                    minX, maxY, maxZ,
                    maxX, maxY, maxZ,
                    maxX, maxY, minZ,
                    minX, maxY, minZ,
                    sprite, tint, packedLight, packedOverlay, 0.0F, 1.0F, 0.0F);
            addFluidQuad(vertexConsumer, pose,
                    minX, minY, minZ,
                    maxX, minY, minZ,
                    maxX, minY, maxZ,
                    minX, minY, maxZ,
                    sprite, tint, packedLight, packedOverlay, 0.0F, -1.0F, 0.0F);
        }

        private static void addFluidQuad(
                VertexConsumer vertexConsumer,
                PoseStack.Pose pose,
                float x1,
                float y1,
                float z1,
                float x2,
                float y2,
                float z2,
                float x3,
                float y3,
                float z3,
                float x4,
                float y4,
                float z4,
                TextureAtlasSprite sprite,
                int tint,
                int packedLight,
                int packedOverlay,
                float normalX,
                float normalY,
                float normalZ
        ) {
            addFluidVertex(vertexConsumer, pose, x1, y1, z1, sprite.getU0(), sprite.getV1(), tint, packedLight, packedOverlay, normalX, normalY, normalZ);
            addFluidVertex(vertexConsumer, pose, x2, y2, z2, sprite.getU1(), sprite.getV1(), tint, packedLight, packedOverlay, normalX, normalY, normalZ);
            addFluidVertex(vertexConsumer, pose, x3, y3, z3, sprite.getU1(), sprite.getV0(), tint, packedLight, packedOverlay, normalX, normalY, normalZ);
            addFluidVertex(vertexConsumer, pose, x4, y4, z4, sprite.getU0(), sprite.getV0(), tint, packedLight, packedOverlay, normalX, normalY, normalZ);
        }

        private static void addFluidVertex(
                VertexConsumer vertexConsumer,
                PoseStack.Pose pose,
                float x,
                float y,
                float z,
                float u,
                float v,
                int tint,
                int packedLight,
                int packedOverlay,
                float normalX,
                float normalY,
                float normalZ
        ) {
            vertexConsumer.addVertex(pose.pose(), x, y, z)
                    .setColor(tint)
                    .setUv(u, v)
                    .setOverlay(packedOverlay)
                    .setLight(packedLight)
                    .setNormal(pose, normalX, normalY, normalZ);
        }

        private static boolean isFullBlockItem(ItemStack stack) {
            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                return false;
            }
            Block block = blockItem.getBlock();
            return !(block instanceof TorchBlock);
        }
    }

    private static ModelResourceLocation baseModelLocation(DeepNullTier tier) {
        ResourceLocation id = DeepNullReforged.id("item/deep_null_base_" + tier.ordinalId());
        return ModelResourceLocation.standalone(id);
    }

    private record RenderContents(ItemStack selectedStack, FluidStack storedFluid, DeepNullContentMode contentMode) {
        private static final RenderContents EMPTY = new RenderContents(ItemStack.EMPTY, FluidStack.EMPTY, DeepNullContentMode.ITEMS);
    }
}
