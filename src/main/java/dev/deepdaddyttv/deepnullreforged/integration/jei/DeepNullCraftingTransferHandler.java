package dev.deepdaddyttv.deepnullreforged.integration.jei;

import dev.deepdaddyttv.deepnullreforged.client.ClientDeepNullJeiSession;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.Optional;

public final class DeepNullCraftingTransferHandler implements IRecipeTransferHandler<AbstractContainerMenu, RecipeHolder<CraftingRecipe>> {
    private final IRecipeTransferHandlerHelper transferHelper;
    private final Class<? extends AbstractContainerMenu> containerClass;
    private final Optional<? extends MenuType<?>> menuType;

    public DeepNullCraftingTransferHandler(
            IRecipeTransferHandlerHelper transferHelper,
            Class<? extends AbstractContainerMenu> containerClass,
            Optional<? extends MenuType<?>> menuType
    ) {
        this.transferHelper = transferHelper;
        this.containerClass = containerClass;
        this.menuType = menuType;
    }

    @Override
    public Class<? extends AbstractContainerMenu> getContainerClass() {
        return containerClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<MenuType<AbstractContainerMenu>> getMenuType() {
        return (Optional<MenuType<AbstractContainerMenu>>) (Optional<?>) menuType;
    }

    @Override
    public IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
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
        DeepNullCraftingTransferSupport.CraftingContext context = DeepNullCraftingTransferSupport.resolveContext(container);
        if (context == null) {
            return null;
        }

        if (!doTransfer) {
            return DeepNullCraftingTransferSupport.planTransfer(container, player, recipe, maxTransfer) == null
                    ? transferHelper.createUserErrorWithTooltip(Component.translatable("jei.deepnullreforged.transfer.missing"))
                    : null;
        }

        ClientDeepNullJeiSession.markTransfer(container);
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.CraftingTransferPayload(recipe.id().identifier(), maxTransfer));
        return null;
    }
}
