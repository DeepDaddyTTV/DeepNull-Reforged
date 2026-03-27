package dev.deepdaddyttv.deepnullreforged.integration.jei;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class DeepNullCraftingTransferSupport {
    private DeepNullCraftingTransferSupport() {
    }

    public static boolean executeTransfer(AbstractContainerMenu menu, Player player, RecipeHolder<CraftingRecipe> recipeHolder, boolean maxTransfer) {
        return false;
    }

    public static boolean returnCurrentCraftingContents(AbstractContainerMenu menu, Player player) {
        return false;
    }
}
