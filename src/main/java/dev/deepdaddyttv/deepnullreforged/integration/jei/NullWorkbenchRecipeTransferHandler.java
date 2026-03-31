package dev.deepdaddyttv.deepnullreforged.integration.jei;

import dev.deepdaddyttv.deepnullreforged.network.NullWorkbenchPayloads;
import dev.deepdaddyttv.deepnullreforged.recipe.NullWorkbenchRecipes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import dev.deepdaddyttv.deepnullreforged.compat.network.PacketDistributor;

import java.util.Optional;

public final class NullWorkbenchRecipeTransferHandler implements IRecipeTransferHandler<dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu, NullWorkbenchRecipes.CraftRecipe> {
    private final IRecipeTransferHandlerHelper transferHelper;

    public NullWorkbenchRecipeTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Override
    public Class<? extends dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu> getContainerClass() {
        return dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu.class;
    }

    @Override
    public Optional<MenuType<dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu>> getMenuType() {
        return Optional.of(dev.deepdaddyttv.deepnullreforged.registry.ModMenus.NULL_WORKBENCH_MENU.get());
    }

    @Override
    public IRecipeType<NullWorkbenchRecipes.CraftRecipe> getRecipeType() {
        return NullWorkbenchRecipeCategory.RECIPE_TYPE;
    }

    @Override
    public IRecipeTransferError transferRecipe(
            dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu container,
            NullWorkbenchRecipes.CraftRecipe recipe,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer
    ) {
        if (!NullWorkbenchTransferSupport.canTransfer(container, player, recipe)) {
            return transferHelper.createUserErrorWithTooltip(Component.translatable("jei.deepnullreforged.transfer.missing"));
        }

        if (doTransfer) {
            PacketDistributor.sendToServer(new NullWorkbenchPayloads.TransferRecipePayload(
                    container.getBlockPos(),
                    BuiltInRegistries.ITEM.getKey(recipe.result().getItem()),
                    maxTransfer
            ));
        }
        return null;
    }
}
