package dev.deepdaddyttv.deepnullreforged.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import dev.deepdaddyttv.deepnullreforged.client.ClientFluidRendering;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullContentMode;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import dev.deepdaddyttv.deepnullreforged.integration.mekanism.MekanismClientCompat;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullPanelItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import dev.deepdaddyttv.deepnullreforged.compat.fluids.FluidStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class DeepNullSelectedItemModel implements ItemModel {
    private static final DeepNullSelectedItemModel INSTANCE = new DeepNullSelectedItemModel();
    private static final Field ACTIVE_LAYER_COUNT_FIELD = field(ItemStackRenderState.class, "activeLayerCount");
    private static final Field LAYERS_FIELD = field(ItemStackRenderState.class, "layers");
    private static final Field SPECIAL_RENDERER_FIELD = field(ItemStackRenderState.LayerRenderState.class, "specialRenderer");
    private static final ContainedCubeSpecialRenderer CONTAINED_CUBE_RENDERER = new ContainedCubeSpecialRenderer();
    private static final Vector3fc[] CONTAINED_CUBE_EXTENTS = new Vector3fc[]{
            new Vector3f(-0.28F, -0.24F, -0.28F),
            new Vector3f(-0.28F, -0.24F, 0.28F),
            new Vector3f(-0.28F, 0.32F, -0.28F),
            new Vector3f(-0.28F, 0.32F, 0.28F),
            new Vector3f(0.28F, -0.24F, -0.28F),
            new Vector3f(0.28F, -0.24F, 0.28F),
            new Vector3f(0.28F, 0.32F, -0.28F),
            new Vector3f(0.28F, 0.32F, 0.28F)
    };
    private static final Supplier<Vector3fc[]> CONTAINED_CUBE_EXTENTS_SUPPLIER = () -> CONTAINED_CUBE_EXTENTS;

    private DeepNullSelectedItemModel() {
    }

    @Override
    public void update(
            ItemStackRenderState output,
            ItemStack item,
            ItemModelResolver resolver,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed
    ) {
        output.appendModelIdentityElement(this);
        if (displayContext == ItemDisplayContext.GUI || !(item.getItem() instanceof DeepNullItem deepNullItem)) {
            return;
        }

        DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), item, level == null ? null : level.registryAccess(), null);
        if (inventory.getContentMode() == DeepNullContentMode.FLUIDS) {
            if (appendFluidContent(output, inventory, displayContext)) {
                output.setAnimated();
            }
            return;
        }

        ItemStack selectedStack = inventory.getSelectedStack();
        if (selectedStack.isEmpty() || selectedStack.getItem() instanceof DeepNullItem) {
            return;
        }

        output.setAnimated();
        int startLayer = activeLayerCount(output);
        resolver.appendItemLayers(output, selectedStack.copyWithCount(1), ItemDisplayContext.NONE, level, owner, seed);
        applyItemTransform(output, startLayer, selectedStack, displayContext);
    }

    private static boolean appendFluidContent(ItemStackRenderState output, DeepNullInventory inventory, ItemDisplayContext displayContext) {
        FluidStack storedFluid = inventory.getSelectedFluid();
        if (!storedFluid.isEmpty()) {
            ContainedCubeRenderData renderData = fluidRenderData(storedFluid);
            if (renderData != null) {
                appendContainedCubeLayer(output, renderData, buildFluidTransform(displayContext));
                return true;
            }
        }

        StoredChemical storedChemical = inventory.getSelectedChemical();
        if (!storedChemical.isEmpty()) {
            ContainedCubeRenderData renderData = chemicalRenderData(storedChemical);
            if (renderData != null) {
                appendContainedCubeLayer(output, renderData, buildFluidTransform(displayContext));
                return true;
            }
        }

        return false;
    }

    private static void appendContainedCubeLayer(ItemStackRenderState output, ContainedCubeRenderData renderData, Matrix4fc transform) {
        ItemStackRenderState.LayerRenderState layer = output.newLayer();
        layer.setExtents(CONTAINED_CUBE_EXTENTS_SUPPLIER);
        layer.setLocalTransform(transform);
        layer.setupSpecialModel(CONTAINED_CUBE_RENDERER, renderData);
        output.appendModelIdentityElement(renderData);
    }

    private static void applyItemTransform(
            ItemStackRenderState output,
            int startLayer,
            ItemStack selectedStack,
            ItemDisplayContext displayContext
    ) {
        ItemStackRenderState.LayerRenderState[] layers = layers(output);
        int endLayer = activeLayerCount(output);
        if (layers == null || endLayer <= startLayer) {
            return;
        }

        boolean customRendered = hasSpecialRenderer(layers, startLayer, endLayer);
        Matrix4fc transform = buildItemTransform(selectedStack, displayContext, customRendered);
        for (int i = startLayer; i < endLayer; i++) {
            layers[i].setLocalTransform(transform);
        }
    }

    private static Matrix4fc buildItemTransform(ItemStack selectedStack, ItemDisplayContext displayContext, boolean customRendered) {
        PoseStack poseStack = new PoseStack();
        boolean blockLike = isFullBlockItem(selectedStack);
        poseStack.translate(0.5F, 0.5F, 0.5F);
        applyDeepNullDisplayTransform(poseStack, displayContext);
        applyContainedItemTransform(poseStack, selectedStack, displayContext, blockLike, customRendered);
        applyContainedItemSpin(poseStack, selectedStack, blockLike, customRendered);
        return new Matrix4f(poseStack.last().pose());
    }

    private static Matrix4fc buildFluidTransform(ItemDisplayContext displayContext) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        applyDeepNullDisplayTransform(poseStack, displayContext);
        applyContainedFluidTransform(poseStack, displayContext);
        applyContainedFluidSpin(poseStack);
        return new Matrix4f(poseStack.last().pose());
    }

    private static void applyDeepNullDisplayTransform(PoseStack poseStack, ItemDisplayContext displayContext) {
        switch (displayContext) {
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> applyItemTransform(poseStack, 75.0F, 45.0F, 0.0F, 0.0F, 2.5F / 16.0F, 0.0F, 0.375F);
            case FIRST_PERSON_RIGHT_HAND -> applyItemTransform(poseStack, 0.0F, 45.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4F);
            case FIRST_PERSON_LEFT_HAND -> applyItemTransform(poseStack, 0.0F, 225.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4F);
            case GROUND -> applyItemTransform(poseStack, 0.0F, 0.0F, 0.0F, 0.0F, 3.0F / 16.0F, 0.0F, 0.25F);
            case FIXED, ON_SHELF -> applyItemTransform(poseStack, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F);
            default -> poseStack.translate(-0.5F, -0.5F, -0.5F);
        }
    }

    private static void applyItemTransform(
            PoseStack poseStack,
            float rotationX,
            float rotationY,
            float rotationZ,
            float translateX,
            float translateY,
            float translateZ,
            float scale
    ) {
        poseStack.translate(translateX, translateY, translateZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(rotationX));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationZ));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
    }

    private static void applyContainedItemTransform(
            PoseStack poseStack,
            ItemStack selectedStack,
            ItemDisplayContext displayContext,
            boolean blockLike,
            boolean customRendered
    ) {
        boolean firstPerson = displayContext.firstPerson();

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

    private static void applyContainedItemSpin(PoseStack poseStack, ItemStack selectedStack, boolean blockLike, boolean customRendered) {
        float rotation = (Util.getMillis() % 24_000L) * 0.015F;

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
        float scale = firstPerson ? 0.55F : 0.5F;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.9D, firstPerson ? 1.18D : 0.9D, 0.9D);
    }

    private static void applyContainedFluidSpin(PoseStack poseStack) {
        float rotation = (Util.getMillis() % 24_000L) * 0.015F;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
    }

    private static boolean hasSpecialRenderer(ItemStackRenderState.LayerRenderState[] layers, int startLayer, int endLayer) {
        try {
            for (int i = startLayer; i < endLayer; i++) {
                if (SPECIAL_RENDERER_FIELD.get(layers[i]) != null) {
                    return true;
                }
            }
            return false;
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to inspect item render layers", exception);
        }
    }

    private static @Nullable ContainedCubeRenderData fluidRenderData(FluidStack storedFluid) {
        TextureAtlasSprite sprite = ClientFluidRendering.getStillSprite(storedFluid);
        if (sprite == null) {
            return null;
        }

        return ContainedCubeRenderData.from(sprite, ensureOpaque(ClientFluidRendering.getTint(storedFluid)));
    }

    private static @Nullable ContainedCubeRenderData chemicalRenderData(StoredChemical storedChemical) {
        TextureAtlasSprite sprite = MekanismClientCompat.getChemicalSprite(storedChemical);
        if (sprite == null) {
            return null;
        }
        return ContainedCubeRenderData.from(sprite, ensureOpaque(storedChemical.tint()));
    }

    private static int ensureOpaque(int tint) {
        return (tint >>> 24) == 0 ? tint | 0xFF000000 : tint;
    }

    private static boolean isFullBlockItem(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        Block block = blockItem.getBlock();
        return !(block instanceof TorchBlock);
    }

    private static int activeLayerCount(ItemStackRenderState output) {
        try {
            return ACTIVE_LAYER_COUNT_FIELD.getInt(output);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to inspect item render state", exception);
        }
    }

    private static ItemStackRenderState.LayerRenderState[] layers(ItemStackRenderState output) {
        try {
            return (ItemStackRenderState.LayerRenderState[]) LAYERS_FIELD.get(output);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to inspect item render state layers", exception);
        }
    }

    private static Field field(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to access field " + owner.getName() + "#" + name, exception);
        }
    }

    private record ContainedCubeRenderData(Identifier atlasLocation, float u0, float v0, float u1, float v1, int tint) {
        private static ContainedCubeRenderData from(TextureAtlasSprite sprite, int tint) {
            return new ContainedCubeRenderData(sprite.atlasLocation(), sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), tint);
        }
    }

    private static final class ContainedCubeSpecialRenderer implements SpecialModelRenderer<ContainedCubeRenderData> {
        @Override
        public void submit(
                @Nullable ContainedCubeRenderData argument,
                PoseStack poseStack,
                SubmitNodeCollector submitNodeCollector,
                int lightCoords,
                int overlayCoords,
                boolean hasFoil,
                int outlineColor
        ) {
            if (argument == null) {
                return;
            }

            submitNodeCollector.submitCustomGeometry(
                    poseStack,
                    RenderTypes.entityTranslucent(argument.atlasLocation()),
                    (pose, buffer) -> renderContainedCube(pose, buffer, argument, lightCoords, overlayCoords)
            );
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            for (Vector3fc extent : CONTAINED_CUBE_EXTENTS) {
                output.accept(extent);
            }
        }

        @Override
        public @Nullable ContainedCubeRenderData extractArgument(ItemStack stack) {
            return null;
        }

        private static void renderContainedCube(
                PoseStack.Pose pose,
                VertexConsumer buffer,
                ContainedCubeRenderData data,
                int lightCoords,
                int overlayCoords
        ) {
            float minX = -0.28F;
            float maxX = 0.28F;
            float minY = -0.24F;
            float maxY = 0.32F;
            float minZ = -0.28F;
            float maxZ = 0.28F;

            addQuad(buffer, pose,
                    minX, minY, maxZ,
                    maxX, minY, maxZ,
                    maxX, maxY, maxZ,
                    minX, maxY, maxZ,
                    data, lightCoords, overlayCoords, 0.0F, 0.0F, 1.0F);
            addQuad(buffer, pose,
                    maxX, minY, minZ,
                    minX, minY, minZ,
                    minX, maxY, minZ,
                    maxX, maxY, minZ,
                    data, lightCoords, overlayCoords, 0.0F, 0.0F, -1.0F);
            addQuad(buffer, pose,
                    minX, minY, minZ,
                    minX, minY, maxZ,
                    minX, maxY, maxZ,
                    minX, maxY, minZ,
                    data, lightCoords, overlayCoords, -1.0F, 0.0F, 0.0F);
            addQuad(buffer, pose,
                    maxX, minY, maxZ,
                    maxX, minY, minZ,
                    maxX, maxY, minZ,
                    maxX, maxY, maxZ,
                    data, lightCoords, overlayCoords, 1.0F, 0.0F, 0.0F);
            addQuad(buffer, pose,
                    minX, maxY, maxZ,
                    maxX, maxY, maxZ,
                    maxX, maxY, minZ,
                    minX, maxY, minZ,
                    data, lightCoords, overlayCoords, 0.0F, 1.0F, 0.0F);
            addQuad(buffer, pose,
                    minX, minY, minZ,
                    maxX, minY, minZ,
                    maxX, minY, maxZ,
                    minX, minY, maxZ,
                    data, lightCoords, overlayCoords, 0.0F, -1.0F, 0.0F);
        }

        private static void addQuad(
                VertexConsumer buffer,
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
                ContainedCubeRenderData data,
                int lightCoords,
                int overlayCoords,
                float normalX,
                float normalY,
                float normalZ
        ) {
            addVertex(buffer, pose, x1, y1, z1, data.u0(), data.v1(), data.tint(), lightCoords, overlayCoords, normalX, normalY, normalZ);
            addVertex(buffer, pose, x2, y2, z2, data.u1(), data.v1(), data.tint(), lightCoords, overlayCoords, normalX, normalY, normalZ);
            addVertex(buffer, pose, x3, y3, z3, data.u1(), data.v0(), data.tint(), lightCoords, overlayCoords, normalX, normalY, normalZ);
            addVertex(buffer, pose, x4, y4, z4, data.u0(), data.v0(), data.tint(), lightCoords, overlayCoords, normalX, normalY, normalZ);
        }

        private static void addVertex(
                VertexConsumer buffer,
                PoseStack.Pose pose,
                float x,
                float y,
                float z,
                float u,
                float v,
                int tint,
                int lightCoords,
                int overlayCoords,
                float normalX,
                float normalY,
                float normalZ
        ) {
            buffer.addVertex(pose, x, y, z)
                    .setColor(tint)
                    .setUv(u, v)
                    .setOverlay(overlayCoords)
                    .setLight(lightCoords)
                    .setNormal(pose, normalX, normalY, normalZ);
        }
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return INSTANCE;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
        }
    }
}
