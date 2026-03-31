package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.recipe.DampNullUpgradeRecipe;
import dev.deepdaddyttv.deepnullreforged.recipe.DeepNullUpgradeRecipe;
import dev.deepdaddyttv.deepnullreforged.recipe.SynchronizerClearRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import dev.deepdaddyttv.deepnullreforged.compat.registries.DeferredHolder;
import dev.deepdaddyttv.deepnullreforged.compat.registries.DeferredRegister;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, DeepNullReforged.MODID);

    private static <T extends Recipe<?>> RecipeSerializer<T> unitSerializer(T recipe) {
        return new RecipeSerializer<>(MapCodec.unit(recipe), StreamCodec.unit(recipe));
    }

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DeepNullUpgradeRecipe>> DEEP_NULL_UPGRADE = RECIPE_SERIALIZERS.register(
            "deepnull_upgrade",
            () -> unitSerializer(DeepNullUpgradeRecipe.INSTANCE)
    );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DampNullUpgradeRecipe>> DAMP_NULL_UPGRADE = RECIPE_SERIALIZERS.register(
            "dampnull_upgrade",
            () -> unitSerializer(DampNullUpgradeRecipe.INSTANCE)
    );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SynchronizerClearRecipe>> SYNCHRONIZER_CLEAR = RECIPE_SERIALIZERS.register(
            "synchronizer_clear",
            () -> unitSerializer(SynchronizerClearRecipe.INSTANCE)
    );

    private ModRecipeSerializers() {
    }

    public static void register() {
        RECIPE_SERIALIZERS.register();
    }
}
