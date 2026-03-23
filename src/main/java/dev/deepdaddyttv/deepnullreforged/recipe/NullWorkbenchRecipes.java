package dev.deepdaddyttv.deepnullreforged.recipe;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class NullWorkbenchRecipes {
    private static final List<CraftRecipe> RECIPES = buildRecipes();

    private NullWorkbenchRecipes() {
    }

    public static List<CraftRecipe> all() {
        return RECIPES;
    }

    private static List<CraftRecipe> buildRecipes() {
        List<CraftRecipe> recipes = new ArrayList<>();
        for (DeepNullTier tier : DeepNullTier.values()) {
            if (tier.creative()) {
                continue;
            }

            Item tierItem = switch (tier) {
                case REDSTONE -> Items.REDSTONE;
                case LAPIS -> Items.LAPIS_LAZULI;
                case IRON -> Items.IRON_INGOT;
                case GOLD -> Items.GOLD_INGOT;
                case DIAMOND -> Items.DIAMOND;
                case EMERALD -> Items.EMERALD;
                case CREATIVE -> null;
            };
            Item dyeItem = switch (tier) {
                case REDSTONE -> Items.RED_DYE;
                case LAPIS -> Items.BLUE_DYE;
                case IRON -> Items.WHITE_DYE;
                case GOLD -> Items.YELLOW_DYE;
                case DIAMOND -> Items.CYAN_DYE;
                case EMERALD -> Items.GREEN_DYE;
                case CREATIVE -> null;
            };
            if (tierItem == null) {
                continue;
            }

            recipes.add(new CraftRecipe(List.of(
                    new IngredientCount(new ItemStack(tierItem, 16)),
                    new IngredientCount(new ItemStack(Items.COAL_BLOCK, 10)),
                    new IngredientCount(new ItemStack(Items.GLASS)),
                    new IngredientCount(new ItemStack(dyeItem))
            ), deepNullStack(tier)));

            recipes.add(new CraftRecipe(List.of(
                    new IngredientCount(deepNullStack(tier)),
                    new IngredientCount(new ItemStack(ModItems.FLUID_UPGRADE.get()))
            ), dampNullStack(tier)));
        }
        return List.copyOf(recipes);
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

    public record IngredientCount(ItemStack stack) {
        public boolean matches(ItemStack other) {
            return !other.isEmpty()
                    && other.is(stack.getItem())
                    && other.getCount() >= stack.getCount();
        }
    }

    public record CraftRecipe(List<IngredientCount> ingredients, ItemStack result) {
    }
}
