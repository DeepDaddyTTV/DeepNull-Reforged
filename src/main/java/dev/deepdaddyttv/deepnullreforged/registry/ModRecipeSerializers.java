package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.recipe.DampNullUpgradeRecipe;
import dev.deepdaddyttv.deepnullreforged.recipe.DeepNullUpgradeRecipe;
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

    private ModRecipeSerializers() {
    }
}
