package dev.deepdaddyttv.deepnullreforged.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.client.ClientFluidRendering;
import dev.deepdaddyttv.deepnullreforged.integration.mekanism.MekanismClientCompat;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullContentMode;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import dev.deepdaddyttv.deepnullreforged.inventory.StyleGlassVariant;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullPanelItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class DeepNullItemRendering {
    private static final ResourceLocation[] ADDITIONAL_MODELS = createAdditionalModels();
    private static final BuiltinItemRendererRegistry.DynamicItemRenderer RENDERER = new DeepNullItemRenderer();
    private static final Field ITEM_COLORS_FIELD = findItemColorsField();

    private static boolean initialized;
    private static boolean itemColorsLookupLogged;

    private DeepNullItemRendering() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        ModelLoadingPlugin.register(context -> context.addModels(ADDITIONAL_MODELS));

        registerBuiltinRenderer(ModItems.REDSTONE_DEEP_NULL.get());
        registerBuiltinRenderer(ModItems.LAPIS_DEEP_NULL.get());
        registerBuiltinRenderer(ModItems.IRON_DEEP_NULL.get());
        registerBuiltinRenderer(ModItems.GOLD_DEEP_NULL.get());
        registerBuiltinRenderer(ModItems.DIAMOND_DEEP_NULL.get());
        registerBuiltinRenderer(ModItems.EMERALD_DEEP_NULL.get());
        registerBuiltinRenderer(ModItems.CREATIVE_DEEP_NULL.get());
        registerBuiltinRenderer(ModItems.REDSTONE_DAMP_NULL.get());
        registerBuiltinRenderer(ModItems.LAPIS_DAMP_NULL.get());
        registerBuiltinRenderer(ModItems.IRON_DAMP_NULL.get());
        registerBuiltinRenderer(ModItems.GOLD_DAMP_NULL.get());
        registerBuiltinRenderer(ModItems.DIAMOND_DAMP_NULL.get());
        registerBuiltinRenderer(ModItems.EMERALD_DAMP_NULL.get());
        registerBuiltinRenderer(ModItems.CREATIVE_DAMP_NULL.get());
    }

    private static void registerBuiltinRenderer(Item item) {
        BuiltinItemRendererRegistry.INSTANCE.register(item, RENDERER);
    }

    private static ResourceLocation[] createAdditionalModels() {
        List<ResourceLocation> models = new ArrayList<>();
        for (DeepNullTier tier : DeepNullTier.values()) {
            models.add(baseModelLocation(tier));
            models.add(dampBaseModelLocation(tier));
            models.add(variantBaseModelLocation(tier, false, StyleGlassVariant.CREEPER));
            models.add(variantBaseModelLocation(tier, false, StyleGlassVariant.PICKAXE));
            models.add(variantBaseModelLocation(tier, true, StyleGlassVariant.FISH));
            models.add(variantBaseModelLocation(tier, true, StyleGlassVariant.FISHING_ROD));
        }
        models.add(styledBaseModelLocation());
        models.add(styledDampBaseModelLocation());
        models.add(styledVariantModelLocation(false, StyleGlassVariant.CREEPER));
        models.add(styledVariantModelLocation(false, StyleGlassVariant.PICKAXE));
        models.add(styledVariantModelLocation(true, StyleGlassVariant.FISH));
        models.add(styledVariantModelLocation(true, StyleGlassVariant.FISHING_ROD));
        return models.toArray(ResourceLocation[]::new);
    }

    private static RenderContents getRenderContents(ItemStack deepNullStack) {
        if (!(deepNullStack.getItem() instanceof DeepNullItem deepNullItem)) {
            return RenderContents.EMPTY;
        }

        Minecraft minecraft = Minecraft.getInstance();
        HolderLookup.Provider registries = minecraft.level == null ? null : minecraft.level.registryAccess();
        DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), deepNullStack, registries, null);
        return new RenderContents(inventory.getSelectedStack(), inventory.getSelectedFluid(), inventory.getSelectedChemical(), inventory.getContentMode());
    }

    private static ItemColors resolveItemColors(ItemRenderer itemRenderer) {
        if (ITEM_COLORS_FIELD == null) {
            logItemColorsLookupFailure();
            return null;
        }

        try {
            return (ItemColors) ITEM_COLORS_FIELD.get(itemRenderer);
        } catch (IllegalAccessException exception) {
            logItemColorsLookupFailure();
            return null;
        }
    }

    private static Field findItemColorsField() {
        try {
            for (Field field : ItemRenderer.class.getDeclaredFields()) {
                if (field.getType() == ItemColors.class) {
                    field.setAccessible(true);
                    return field;
                }
            }
        } catch (RuntimeException exception) {
            DeepNullReforged.LOGGER.warn("Failed to make ItemRenderer item color field accessible", exception);
        }
        return null;
    }

    private static void logItemColorsLookupFailure() {
        if (itemColorsLookupLogged) {
            return;
        }
        itemColorsLookupLogged = true;
        DeepNullReforged.LOGGER.warn("Falling back to untinted DeepNull item model rendering because ItemColors could not be resolved");
    }

    private static BakedModel loadAdditionalModel(ResourceLocation modelId) {
        Minecraft minecraft = Minecraft.getInstance();
        ModelManager modelManager = minecraft.getModelManager();
        BakedModel model = ((FabricBakedModelManager) modelManager).getModel(modelId);
        return model == null ? modelManager.getMissingModel() : model;
    }

    private static void renderModelLists(
            ItemRenderer itemRenderer,
            BakedModel model,
            ItemStack stack,
            int packedLight,
            int packedOverlay,
            PoseStack poseStack,
            VertexConsumer buffer
    ) {
        RandomSource randomSource = RandomSource.create();
        ItemColors itemColors = resolveItemColors(itemRenderer);

        for (Direction direction : Direction.values()) {
            randomSource.setSeed(42L);
            renderQuadList(itemColors, poseStack, buffer, model.getQuads(null, direction, randomSource), stack, packedLight, packedOverlay);
        }

        randomSource.setSeed(42L);
        renderQuadList(itemColors, poseStack, buffer, model.getQuads(null, null, randomSource), stack, packedLight, packedOverlay);
    }

    private static void renderQuadList(
            ItemColors itemColors,
            PoseStack poseStack,
            VertexConsumer buffer,
            List<BakedQuad> quads,
            ItemStack itemStack,
            int packedLight,
            int packedOverlay
    ) {
        boolean hasItemStack = !itemStack.isEmpty();
        PoseStack.Pose pose = poseStack.last();

        for (BakedQuad bakedQuad : quads) {
            int color = -1;
            if (hasItemStack && bakedQuad.isTinted() && itemColors != null) {
                color = itemColors.getColor(itemStack, bakedQuad.getTintIndex());
            }

            float alpha = (float) FastColor.ARGB32.alpha(color) / 255.0F;
            float red = (float) FastColor.ARGB32.red(color) / 255.0F;
            float green = (float) FastColor.ARGB32.green(color) / 255.0F;
            float blue = (float) FastColor.ARGB32.blue(color) / 255.0F;
            buffer.putBulkData(pose, bakedQuad, red, green, blue, alpha, packedLight, packedOverlay);
        }
    }

    private static final class DeepNullItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
        @Override
        public void render(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
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
                } else if (!renderContents.storedChemical().isEmpty() && displayContext != ItemDisplayContext.GUI) {
                    poseStack.pushPose();
                    applyContainedFluidTransform(poseStack, displayContext);
                    applyContainedFluidSpin(poseStack);
                    renderStoredChemical(renderContents.storedChemical(), poseStack, buffer, packedLight, packedOverlay);
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

            boolean fluidOnly = stack.getItem() instanceof DampNullItem;
            StyleGlassVariant styleVariant = DeepNullInventory.getStyleVariant(stack);
            if (styleVariant != StyleGlassVariant.DEFAULT) {
                return loadAdditionalModel(
                        DeepNullInventory.hasColorOverrides(stack)
                                ? styledVariantModelLocation(fluidOnly, styleVariant)
                                : variantBaseModelLocation(deepNullItem.tier(), fluidOnly, styleVariant)
                );
            }
            if (DeepNullInventory.hasColorOverrides(stack)) {
                return loadAdditionalModel(
                        fluidOnly
                                ? styledDampBaseModelLocation()
                                : styledBaseModelLocation()
                );
            }
            return loadAdditionalModel(
                    fluidOnly
                            ? dampBaseModelLocation(deepNullItem.tier())
                            : baseModelLocation(deepNullItem.tier())
            );
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
            VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(
                    buffer,
                    ItemBlockRenderTypes.getRenderType(stack, true),
                    true,
                    stack.hasFoil()
            );
            renderModelLists(itemRenderer, baseModel, stack, packedLight, packedOverlay, poseStack, vertexConsumer);
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

            VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(
                    buffer,
                    ItemBlockRenderTypes.getRenderType(renderStack, true),
                    true,
                    renderStack.hasFoil()
            );
            renderModelLists(itemRenderer, selectedModel, renderStack, packedLight, packedOverlay, poseStack, vertexConsumer);
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
            TextureAtlasSprite sprite = ClientFluidRendering.getStillSprite(storedFluid);
            if (sprite == null) {
                return;
            }

            int tint = ClientFluidRendering.getTint(storedFluid);
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(sprite.atlasLocation()));
            renderFluidCube(poseStack, vertexConsumer, sprite, tint, packedLight, packedOverlay);
        }

        private static void renderStoredChemical(
                StoredChemical storedChemical,
                PoseStack poseStack,
                MultiBufferSource buffer,
                int packedLight,
                int packedOverlay
        ) {
            TextureAtlasSprite sprite = MekanismClientCompat.getChemicalSprite(storedChemical);
            if (sprite == null) {
                return;
            }

            int tint = storedChemical.tint();
            if ((tint >>> 24) == 0) {
                tint |= 0xFF000000;
            }

            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(sprite.atlasLocation()));
            renderChemicalCube(poseStack, vertexConsumer, sprite, tint, packedLight, packedOverlay);
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

        private static void renderChemicalCube(
                PoseStack poseStack,
                VertexConsumer vertexConsumer,
                TextureAtlasSprite sprite,
                int tint,
                int packedLight,
                int packedOverlay
        ) {
            renderFluidCube(poseStack, vertexConsumer, sprite, tint, packedLight, packedOverlay);
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

    private static ResourceLocation baseModelLocation(DeepNullTier tier) {
        return DeepNullReforged.id("item/deep_null_base_" + tier.ordinalId());
    }

    private static ResourceLocation dampBaseModelLocation(DeepNullTier tier) {
        return DeepNullReforged.id("item/damp_null_base_" + tier.ordinalId());
    }

    private static ResourceLocation styledBaseModelLocation() {
        return DeepNullReforged.id("item/deep_null_styled");
    }

    private static ResourceLocation styledDampBaseModelLocation() {
        return DeepNullReforged.id("item/damp_null_styled");
    }

    private static ResourceLocation variantBaseModelLocation(DeepNullTier tier, boolean fluidOnly, StyleGlassVariant variant) {
        String path = switch (variant) {
            case CREEPER -> "item/deep_null_creeper_base_" + tier.ordinalId();
            case PICKAXE -> "item/deep_null_pickaxe_base_" + tier.ordinalId();
            case FISH -> "item/damp_null_fish_base_" + tier.ordinalId();
            case FISHING_ROD -> "item/damp_null_fishing_rod_base_" + tier.ordinalId();
            case DEFAULT -> fluidOnly
                    ? "item/damp_null_base_" + tier.ordinalId()
                    : "item/deep_null_base_" + tier.ordinalId();
        };
        return DeepNullReforged.id(path);
    }

    private static ResourceLocation styledVariantModelLocation(boolean fluidOnly, StyleGlassVariant variant) {
        String path = switch (variant) {
            case CREEPER -> "item/deep_null_creeper_styled";
            case PICKAXE -> "item/deep_null_pickaxe_styled";
            case FISH -> "item/damp_null_fish_styled";
            case FISHING_ROD -> "item/damp_null_fishing_rod_styled";
            case DEFAULT -> fluidOnly ? "item/damp_null_styled" : "item/deep_null_styled";
        };
        return DeepNullReforged.id(path);
    }

    private record RenderContents(ItemStack selectedStack, FluidStack storedFluid, StoredChemical storedChemical, DeepNullContentMode contentMode) {
        private static final RenderContents EMPTY = new RenderContents(ItemStack.EMPTY, FluidStack.EMPTY, StoredChemical.EMPTY, DeepNullContentMode.ITEMS);
    }
}
