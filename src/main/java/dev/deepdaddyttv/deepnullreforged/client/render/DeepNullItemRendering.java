package dev.deepdaddyttv.deepnullreforged.client.render;

import com.mojang.serialization.MapCodec;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;

public final class DeepNullItemRendering {
    private static final Field ITEM_MODEL_ID_MAPPER_FIELD = field(ItemModels.class, "ID_MAPPER");
    private static final Field ITEM_TINT_ID_MAPPER_FIELD = field(ItemTintSources.class, "ID_MAPPER");
    private static boolean initialized;

    private DeepNullItemRendering() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        registerItemModel(DeepNullReforged.id("selected_item"), DeepNullSelectedItemModel.Unbaked.MAP_CODEC);
        registerItemTint(DeepNullReforged.id("deep_null_frame"), FrameTintSource.MAP_CODEC);
        registerItemTint(DeepNullReforged.id("deep_null_glass"), GlassTintSource.MAP_CODEC);
    }

    @SuppressWarnings("unchecked")
    private static void registerItemModel(Identifier id, MapCodec<? extends ItemModel.Unbaked> codec) {
        try {
            ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemModel.Unbaked>> mapper =
                    (ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemModel.Unbaked>>) ITEM_MODEL_ID_MAPPER_FIELD.get(null);
            mapper.put(id, codec);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to register item model codec " + id, exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerItemTint(Identifier id, MapCodec<? extends ItemTintSource> codec) {
        try {
            ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemTintSource>> mapper =
                    (ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemTintSource>>) ITEM_TINT_ID_MAPPER_FIELD.get(null);
            mapper.put(id, codec);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to register item tint codec " + id, exception);
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

    private static int resolveTint(ItemStack stack, boolean frame) {
        if (!(stack.getItem() instanceof DeepNullItem deepNullItem)) {
            return 0xFFFFFFFF;
        }

        DeepNullInventory.StyleRenderData style = DeepNullInventory.readStyleRenderData(
                stack,
                deepNullItem.tier(),
                stack.getItem() instanceof DampNullItem
        );
        if (!style.hasCustomStyle()) {
            return 0xFFFFFFFF;
        }

        int rgb = frame ? style.frameColor() : style.glassColor();
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    public record FrameTintSource() implements ItemTintSource {
        public static final MapCodec<FrameTintSource> MAP_CODEC = MapCodec.unit(new FrameTintSource());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            return resolveTint(stack, true);
        }

        @Override
        public MapCodec<FrameTintSource> type() {
            return MAP_CODEC;
        }
    }

    public record GlassTintSource() implements ItemTintSource {
        public static final MapCodec<GlassTintSource> MAP_CODEC = MapCodec.unit(new GlassTintSource());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            return resolveTint(stack, false);
        }

        @Override
        public MapCodec<GlassTintSource> type() {
            return MAP_CODEC;
        }
    }
}
