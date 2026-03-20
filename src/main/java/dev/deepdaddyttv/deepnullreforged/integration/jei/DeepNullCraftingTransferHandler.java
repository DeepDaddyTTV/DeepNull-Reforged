package dev.deepdaddyttv.deepnullreforged.integration.jei;

import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public final class DeepNullCraftingTransferHandler implements IRecipeTransferHandler<AbstractContainerMenu, RecipeHolder<CraftingRecipe>> {
    private final IRecipeTransferHandlerHelper transferHelper;

    public DeepNullCraftingTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Override
    public Class<? extends AbstractContainerMenu> getContainerClass() {
        return AbstractContainerMenu.class;
    }

    @Override
    public Optional<MenuType<AbstractContainerMenu>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public IRecipeTransferError transferRecipe(
            AbstractContainerMenu container,
            RecipeHolder<CraftingRecipe> recipe,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer
    ) {
        if (DeepNullCraftingTransferSupport.resolveContext(container) == null) {
            return null;
        }

        if (Minecraft.getInstance().level == null || DeepNullCraftingTransferSupport.planTransfer(container, player, recipe, maxTransfer) == null) {
            return transferHelper.createUserErrorWithTooltip(Component.translatable("jei.deepnullreforged.transfer.missing"));
        }

        if (doTransfer) {
            PacketDistributor.sendToServer(new DeepNullPayloads.CraftingTransferPayload(recipe.id(), maxTransfer));
        }
        return null;
    }
}
