package dev.deepdaddyttv.deepnullreforged.integration.jei;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.client.DeepNullFluidScreen;
import dev.deepdaddyttv.deepnullreforged.client.DeepNullScreen;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.recipe.NullWorkbenchRecipes;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JeiPlugin
public final class DeepNullJeiPlugin implements IModPlugin {
    private static final Identifier PLUGIN_UID = DeepNullReforged.id("jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new NullWorkbenchRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RecipeTypes.CRAFTING, createDeepNullUpgradeDisplayRecipes(registration));
        registration.addRecipes(RecipeTypes.CRAFTING, createDampNullUpgradeDisplayRecipes(registration));
        registration.addRecipes(NullWorkbenchRecipeCategory.RECIPE_TYPE, NullWorkbenchRecipes.all());

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
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.NULL_WORKBENCH.get()), NullWorkbenchRecipeCategory.RECIPE_TYPE);
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

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                new DeepNullCraftingTransferHandler(registration.getTransferHelper(), InventoryMenu.class, Optional.empty()),
                RecipeTypes.CRAFTING
        );
        registration.addRecipeTransferHandler(
                new DeepNullCraftingTransferHandler(registration.getTransferHelper(), CraftingMenu.class, Optional.of(MenuType.CRAFTING)),
                RecipeTypes.CRAFTING
        );
        registration.addRecipeTransferHandler(new NullWorkbenchRecipeTransferHandler(registration.getTransferHelper()), NullWorkbenchRecipeCategory.RECIPE_TYPE);
    }

    private static List<RecipeHolder<CraftingRecipe>> createDeepNullUpgradeDisplayRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
        for (DeepNullTier tier : List.of(DeepNullTier.REDSTONE, DeepNullTier.LAPIS, DeepNullTier.IRON, DeepNullTier.GOLD, DeepNullTier.DIAMOND)) {
            DeepNullTier nextTier = tier.next();
            CraftingRecipe recipe = registration.getVanillaRecipeFactory()
                    .createShapedRecipeBuilder(CraftingBookCategory.MISC, new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(deepNullStack(nextTier))))
                    .pattern(" a ")
                    .pattern("aca")
                    .pattern(" a ")
                    .define('a', Ingredient.of(panelItem(nextTier)))
                    .define('c', Ingredient.of(deepNullItem(tier)))
                    .build();
            recipes.add(new RecipeHolder<>(recipeKey("jei/deepnull_upgrade/" + tier.deepNullId()), recipe));
        }
        return recipes;
    }

    private static List<RecipeHolder<CraftingRecipe>> createDampNullUpgradeDisplayRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();

        CraftingRecipe tierOneRecipe = registration.getVanillaRecipeFactory()
                .createShapedRecipeBuilder(CraftingBookCategory.MISC, new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(dampNullStack(DeepNullTier.REDSTONE))))
                .pattern("bab")
                .pattern("aca")
                .pattern("bab")
                .define('a', Ingredient.of(panelItem(DeepNullTier.REDSTONE)))
                .define('b', Ingredient.of(Items.BUCKET))
                .define('c', Ingredient.of(ModItems.FLUID_UPGRADE.get()))
                .build();
        recipes.add(new RecipeHolder<>(recipeKey("jei/dampnull_upgrade/" + DeepNullTier.REDSTONE.dampNullId()), tierOneRecipe));

        for (DeepNullTier tier : List.of(DeepNullTier.REDSTONE, DeepNullTier.LAPIS, DeepNullTier.IRON, DeepNullTier.GOLD, DeepNullTier.DIAMOND)) {
            DeepNullTier nextTier = tier.next();
            CraftingRecipe recipe = registration.getVanillaRecipeFactory()
                    .createShapedRecipeBuilder(CraftingBookCategory.MISC, new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(dampNullStack(nextTier))))
                    .pattern("bab")
                    .pattern("aca")
                    .pattern("bab")
                    .define('a', Ingredient.of(panelItem(nextTier)))
                    .define('b', Ingredient.of(Items.BUCKET))
                    .define('c', Ingredient.of(dampNullItem(tier)))
                    .build();
            recipes.add(new RecipeHolder<>(recipeKey("jei/dampnull_upgrade/" + tier.dampNullId()), recipe));
        }
        return recipes;
    }

    private static ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> recipeKey(String path) {
        return ResourceKey.create(Registries.RECIPE, DeepNullReforged.id(path));
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

    private static net.minecraft.world.item.Item deepNullItem(DeepNullTier tier) {
        return deepNullStack(tier).getItem();
    }

    private static net.minecraft.world.item.Item dampNullItem(DeepNullTier tier) {
        return dampNullStack(tier).getItem();
    }

    private static net.minecraft.world.item.Item panelItem(DeepNullTier tier) {
        return panelStack(tier).getItem();
    }
}
