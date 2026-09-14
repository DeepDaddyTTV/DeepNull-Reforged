package dev.deepdaddyttv.deepnullreforged.client.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.StyleGlassVariant;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DeepNullItemModel implements ItemModel {
    private static final List<Identifier> MODEL_DEPENDENCIES = createModelDependencies();

    private final ItemModel[] deepNullBases;
    private final ItemModel[] dampNullBases;
    private final Map<StyleGlassVariant, ItemModel> deepNullStyles;
    private final Map<StyleGlassVariant, ItemModel> dampNullStyles;

    private DeepNullItemModel(
            ItemModel[] deepNullBases,
            ItemModel[] dampNullBases,
            Map<StyleGlassVariant, ItemModel> deepNullStyles,
            Map<StyleGlassVariant, ItemModel> dampNullStyles
    ) {
        this.deepNullBases = deepNullBases;
        this.dampNullBases = dampNullBases;
        this.deepNullStyles = deepNullStyles;
        this.dampNullStyles = dampNullStyles;
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
        if (!(item.getItem() instanceof DeepNullItem deepNullItem)) {
            return;
        }

        boolean fluidOnly = deepNullItem instanceof DampNullItem;
        DeepNullInventory.StyleRenderData style = DeepNullInventory.readStyleRenderData(item, deepNullItem.tier(), fluidOnly);
        ItemModel base = selectBase(deepNullItem.tier(), fluidOnly, style);
        DeepNullSelectedItemModel selected = DeepNullSelectedItemModel.instance();

        if (displayContext == ItemDisplayContext.GUI) {
            base.update(output, item, resolver, displayContext, level, owner, seed);
        } else if (displayContext.firstPerson()) {
            base.update(output, item, resolver, displayContext, level, owner, seed);
            selected.update(output, item, resolver, displayContext, level, owner, seed);
            base.update(output, item, resolver, displayContext, level, owner, seed);
        } else {
            selected.update(output, item, resolver, displayContext, level, owner, seed);
            base.update(output, item, resolver, displayContext, level, owner, seed);
        }
    }

    private ItemModel selectBase(
            DeepNullTier tier,
            boolean fluidOnly,
            DeepNullInventory.StyleRenderData style
    ) {
        if (!style.hasCustomStyle()) {
            return (fluidOnly ? dampNullBases : deepNullBases)[tier.ordinalId()];
        }
        Map<StyleGlassVariant, ItemModel> styles = fluidOnly ? dampNullStyles : deepNullStyles;
        return styles.getOrDefault(style.styleVariant(), styles.get(StyleGlassVariant.DEFAULT));
    }

    private static List<Identifier> createModelDependencies() {
        List<Identifier> models = new ArrayList<>();
        for (DeepNullTier tier : DeepNullTier.values()) {
            models.add(model("deep_null_base_" + tier.ordinalId()));
            models.add(model("damp_null_base_" + tier.ordinalId()));
        }
        models.add(model("deep_null_styled_display"));
        models.add(model("deep_null_creeper_styled_display"));
        models.add(model("deep_null_pickaxe_styled_display"));
        models.add(model("damp_null_styled_display"));
        models.add(model("damp_null_fish_styled_display"));
        models.add(model("damp_null_fishing_rod_styled_display"));
        return List.copyOf(models);
    }

    private static Identifier model(String name) {
        return DeepNullReforged.id("item/" + name);
    }

    private record StyleTintSource(int tintIndex) implements ItemTintSource {
        private static final MapCodec<StyleTintSource> MAP_CODEC = Codec.intRange(0, 1)
                .fieldOf("tint_index")
                .xmap(StyleTintSource::new, StyleTintSource::tintIndex);

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            if (!(stack.getItem() instanceof DeepNullItem deepNullItem)) {
                return 0xFFFFFFFF;
            }
            DeepNullInventory.StyleRenderData style = DeepNullInventory.readStyleRenderData(
                    stack,
                    deepNullItem.tier(),
                    deepNullItem instanceof DampNullItem
            );
            int rgb = tintIndex == 0 ? style.frameColor() : style.glassColor();
            return 0xFF000000 | (rgb & 0xFFFFFF);
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return MAP_CODEC;
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
            ItemModel[] deepNullBases = new ItemModel[DeepNullTier.values().length];
            ItemModel[] dampNullBases = new ItemModel[DeepNullTier.values().length];
            for (DeepNullTier tier : DeepNullTier.values()) {
                deepNullBases[tier.ordinalId()] = bakeModel(context, transformation, model("deep_null_base_" + tier.ordinalId()), List.of());
                dampNullBases[tier.ordinalId()] = bakeModel(context, transformation, model("damp_null_base_" + tier.ordinalId()), List.of());
            }

            List<ItemTintSource> tints = List.of(new StyleTintSource(0), new StyleTintSource(1));
            Map<StyleGlassVariant, ItemModel> deepNullStyles = new EnumMap<>(StyleGlassVariant.class);
            deepNullStyles.put(StyleGlassVariant.DEFAULT, bakeModel(context, transformation, model("deep_null_styled_display"), tints));
            deepNullStyles.put(StyleGlassVariant.CREEPER, bakeModel(context, transformation, model("deep_null_creeper_styled_display"), tints));
            deepNullStyles.put(StyleGlassVariant.PICKAXE, bakeModel(context, transformation, model("deep_null_pickaxe_styled_display"), tints));

            Map<StyleGlassVariant, ItemModel> dampNullStyles = new EnumMap<>(StyleGlassVariant.class);
            dampNullStyles.put(StyleGlassVariant.DEFAULT, bakeModel(context, transformation, model("damp_null_styled_display"), tints));
            dampNullStyles.put(StyleGlassVariant.FISH, bakeModel(context, transformation, model("damp_null_fish_styled_display"), tints));
            dampNullStyles.put(StyleGlassVariant.FISHING_ROD, bakeModel(context, transformation, model("damp_null_fishing_rod_styled_display"), tints));

            return new DeepNullItemModel(deepNullBases, dampNullBases, deepNullStyles, dampNullStyles);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            MODEL_DEPENDENCIES.forEach(resolver::markDependency);
        }

        private static ItemModel bakeModel(
                ItemModel.BakingContext context,
                Matrix4fc transformation,
                Identifier identifier,
                List<ItemTintSource> tints
        ) {
            return new CuboidItemModelWrapper.Unbaked(identifier, Optional.empty(), tints).bake(context, transformation);
        }
    }
}
