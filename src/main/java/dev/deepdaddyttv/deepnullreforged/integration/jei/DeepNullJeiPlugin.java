package dev.deepdaddyttv.deepnullreforged.integration.jei;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.client.DeepNullFluidScreen;
import dev.deepdaddyttv.deepnullreforged.client.DeepNullScreen;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JeiPlugin
public final class DeepNullJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID = DeepNullReforged.id("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RecipeTypes.CRAFTING, createDeepNullUpgradeDisplayRecipes(registration));
        registration.addRecipes(RecipeTypes.CRAFTING, createDampNullUpgradeDisplayRecipes(registration));

        List<ItemStack> deepNulls = List.of(
                new ItemStack(ModItems.REDSTONE_DEEP_NULL.get()),
                new ItemStack(ModItems.LAPIS_DEEP_NULL.get()),
                new ItemStack(ModItems.IRON_DEEP_NULL.get()),
                new ItemStack(ModItems.GOLD_DEEP_NULL.get()),
                new ItemStack(ModItems.DIAMOND_DEEP_NULL.get()),
                new ItemStack(ModItems.EMERALD_DEEP_NULL.get())
        );
        registration.addItemStackInfo(deepNulls, Component.translatable("jei.deepnullreforged.desc"));
        registration.addItemStackInfo(new ItemStack(ModItems.REDSTONE_DEEP_NULL.get()), Component.translatable("jei.deepnullreforged.desc0"));
        registration.addItemStackInfo(new ItemStack(ModItems.LAPIS_DEEP_NULL.get()), Component.translatable("jei.deepnullreforged.desc1"));
        registration.addItemStackInfo(new ItemStack(ModItems.IRON_DEEP_NULL.get()), Component.translatable("jei.deepnullreforged.desc2"));
        registration.addItemStackInfo(new ItemStack(ModItems.GOLD_DEEP_NULL.get()), Component.translatable("jei.deepnullreforged.desc3"));
        registration.addItemStackInfo(new ItemStack(ModItems.DIAMOND_DEEP_NULL.get()), Component.translatable("jei.deepnullreforged.desc4"));
        registration.addItemStackInfo(new ItemStack(ModItems.EMERALD_DEEP_NULL.get()), Component.translatable("jei.deepnullreforged.desc5"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.DEEP_NULL_DOCK.get()), Component.translatable("jei.deepnull_dock.desc"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.REDSTONE_DEEP_NULL.get()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.LAPIS_DEEP_NULL.get()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.IRON_DEEP_NULL.get()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.GOLD_DEEP_NULL.get()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.DIAMOND_DEEP_NULL.get()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.EMERALD_DEEP_NULL.get()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.REDSTONE_DAMP_NULL.get()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.LAPIS_DAMP_NULL.get()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.IRON_DAMP_NULL.get()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.GOLD_DAMP_NULL.get()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.DIAMOND_DAMP_NULL.get()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.EMERALD_DAMP_NULL.get()), RecipeTypes.CRAFTING);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(DeepNullScreen.class, new mezz.jei.api.gui.handlers.IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(DeepNullScreen screen) {
                Rect2i area = screen.getInfoPanelArea();
                return area == null ? Collections.emptyList() : List.of(area);
            }
        });
        registration.addGuiContainerHandler(DeepNullFluidScreen.class, new mezz.jei.api.gui.handlers.IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(DeepNullFluidScreen screen) {
                Rect2i area = screen.getInfoPanelArea();
                return area == null ? Collections.emptyList() : List.of(area);
            }
        });
    }

    private static List<RecipeHolder<CraftingRecipe>> createDeepNullUpgradeDisplayRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
        for (DeepNullTier tier : List.of(DeepNullTier.REDSTONE, DeepNullTier.LAPIS, DeepNullTier.IRON, DeepNullTier.GOLD, DeepNullTier.DIAMOND)) {
            DeepNullTier nextTier = tier.next();
            CraftingRecipe recipe = registration.getVanillaRecipeFactory()
                    .createShapedRecipeBuilder(CraftingBookCategory.MISC, List.of(deepNullStack(nextTier)))
                    .pattern(" a ")
                    .pattern("aca")
                    .pattern(" a ")
                    .define('a', Ingredient.of(panelStack(nextTier)))
                    .define('c', Ingredient.of(deepNullStack(tier)))
                    .build();
            recipes.add(new RecipeHolder<>(DeepNullReforged.id("jei/deepnull_upgrade/" + tier.deepNullId()), recipe));
        }
        return recipes;
    }

    private static List<RecipeHolder<CraftingRecipe>> createDampNullUpgradeDisplayRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();

        CraftingRecipe tierOneRecipe = registration.getVanillaRecipeFactory()
                .createShapedRecipeBuilder(CraftingBookCategory.MISC, List.of(dampNullStack(DeepNullTier.REDSTONE)))
                .pattern("bab")
                .pattern("aca")
                .pattern("bab")
                .define('a', Ingredient.of(panelStack(DeepNullTier.REDSTONE)))
                .define('b', Ingredient.of(new ItemStack(Items.BUCKET)))
                .define('c', Ingredient.of(new ItemStack(ModItems.FLUID_UPGRADE.get())))
                .build();
        recipes.add(new RecipeHolder<>(DeepNullReforged.id("jei/dampnull_upgrade/" + DeepNullTier.REDSTONE.dampNullId()), tierOneRecipe));

        for (DeepNullTier tier : List.of(DeepNullTier.REDSTONE, DeepNullTier.LAPIS, DeepNullTier.IRON, DeepNullTier.GOLD, DeepNullTier.DIAMOND)) {
            DeepNullTier nextTier = tier.next();
            CraftingRecipe recipe = registration.getVanillaRecipeFactory()
                    .createShapedRecipeBuilder(CraftingBookCategory.MISC, List.of(dampNullStack(nextTier)))
                    .pattern("bab")
                    .pattern("aca")
                    .pattern("bab")
                    .define('a', Ingredient.of(panelStack(nextTier)))
                    .define('b', Ingredient.of(new ItemStack(Items.BUCKET)))
                    .define('c', Ingredient.of(dampNullStack(tier)))
                    .build();
            recipes.add(new RecipeHolder<>(DeepNullReforged.id("jei/dampnull_upgrade/" + tier.dampNullId()), recipe));
        }
        return recipes;
    }

    private static ItemStack deepNullStack(DeepNullTier tier) {
        return switch (tier) {
            case REDSTONE -> new ItemStack(ModItems.REDSTONE_DEEP_NULL.get());
            case LAPIS -> new ItemStack(ModItems.LAPIS_DEEP_NULL.get());
            case IRON -> new ItemStack(ModItems.IRON_DEEP_NULL.get());
            case GOLD -> new ItemStack(ModItems.GOLD_DEEP_NULL.get());
            case DIAMOND -> new ItemStack(ModItems.DIAMOND_DEEP_NULL.get());
            case EMERALD -> new ItemStack(ModItems.EMERALD_DEEP_NULL.get());
            case CREATIVE -> new ItemStack(ModItems.CREATIVE_DEEP_NULL.get());
        };
    }

    private static ItemStack dampNullStack(DeepNullTier tier) {
        return switch (tier) {
            case REDSTONE -> new ItemStack(ModItems.REDSTONE_DAMP_NULL.get());
            case LAPIS -> new ItemStack(ModItems.LAPIS_DAMP_NULL.get());
            case IRON -> new ItemStack(ModItems.IRON_DAMP_NULL.get());
            case GOLD -> new ItemStack(ModItems.GOLD_DAMP_NULL.get());
            case DIAMOND -> new ItemStack(ModItems.DIAMOND_DAMP_NULL.get());
            case EMERALD -> new ItemStack(ModItems.EMERALD_DAMP_NULL.get());
            case CREATIVE -> new ItemStack(ModItems.CREATIVE_DAMP_NULL.get());
        };
    }

    private static ItemStack panelStack(DeepNullTier tier) {
        return switch (tier) {
            case REDSTONE -> new ItemStack(ModItems.REDSTONE_PANEL.get());
            case LAPIS -> new ItemStack(ModItems.LAPIS_PANEL.get());
            case IRON -> new ItemStack(ModItems.IRON_PANEL.get());
            case GOLD -> new ItemStack(ModItems.GOLD_PANEL.get());
            case DIAMOND -> new ItemStack(ModItems.DIAMOND_PANEL.get());
            case EMERALD, CREATIVE -> new ItemStack(ModItems.EMERALD_PANEL.get());
        };
    }
}
