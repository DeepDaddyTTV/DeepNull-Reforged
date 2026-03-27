package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.recipe.DampNullUpgradeRecipe;
import dev.deepdaddyttv.deepnullreforged.recipe.DeepNullUpgradeRecipe;
import dev.deepdaddyttv.deepnullreforged.recipe.SynchronizerClearRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, DeepNullReforged.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DeepNullUpgradeRecipe>> DEEP_NULL_UPGRADE = RECIPE_SERIALIZERS.register(
            "deepnull_upgrade",
            () -> new SimpleCraftingRecipeSerializer<>(DeepNullUpgradeRecipe::new)
    );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DampNullUpgradeRecipe>> DAMP_NULL_UPGRADE = RECIPE_SERIALIZERS.register(
            "dampnull_upgrade",
            () -> new SimpleCraftingRecipeSerializer<>(DampNullUpgradeRecipe::new)
    );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SynchronizerClearRecipe>> SYNCHRONIZER_CLEAR = RECIPE_SERIALIZERS.register(
            "synchronizer_clear",
            () -> new SimpleCraftingRecipeSerializer<>(SynchronizerClearRecipe::new)
    );

    private ModRecipeSerializers() {
    }

    public static void register() {
        RECIPE_SERIALIZERS.register();
    }
}
