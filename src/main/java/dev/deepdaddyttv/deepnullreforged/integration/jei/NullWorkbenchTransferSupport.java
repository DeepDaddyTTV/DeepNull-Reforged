package dev.deepdaddyttv.deepnullreforged.integration.jei;

import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu;
import dev.deepdaddyttv.deepnullreforged.recipe.NullWorkbenchRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class NullWorkbenchTransferSupport {
    private NullWorkbenchTransferSupport() {
    }

    public static NullWorkbenchRecipes.CraftRecipe findRecipe(Identifier resultItemId) {
        for (NullWorkbenchRecipes.CraftRecipe recipe : NullWorkbenchRecipes.all()) {
            Identifier id = BuiltInRegistries.ITEM.getKey(recipe.result().getItem());
            if (Objects.equals(id, resultItemId)) {
                return recipe;
            }
        }
        return null;
    }

    public static boolean canTransfer(NullWorkbenchMenu menu, Player player, NullWorkbenchRecipes.CraftRecipe recipe) {
        if (!menu.hasWorkbench()) {
            return false;
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < recipe.ingredients().size(); i++) {
            ItemStack slotStack = menu.slots.get(NullWorkbenchBlockEntity.INPUT_SLOT_START + i).getItem();
            NullWorkbenchRecipes.IngredientCount ingredient = recipe.ingredients().get(i);
            if (!slotStack.isEmpty() && (!slotStack.is(ingredient.stack().getItem()) || slotStack.getCount() > ingredient.stack().getCount())) {
                return false;
            }
            int existing = slotStack.is(ingredient.stack().getItem()) ? slotStack.getCount() : 0;
            int missing = ingredient.stack().getCount() - existing;
            if (missing > 0 && countInInventory(inventory, ingredient.stack()) < missing) {
                return false;
            }
        }

        for (int i = recipe.ingredients().size(); i < NullWorkbenchBlockEntity.INPUT_SLOT_COUNT; i++) {
            if (!menu.slots.get(NullWorkbenchBlockEntity.INPUT_SLOT_START + i).getItem().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean executeTransfer(NullWorkbenchMenu menu, Player player, NullWorkbenchRecipes.CraftRecipe recipe) {
        if (!canTransfer(menu, player, recipe) || menu.getWorkbench() == null) {
            return false;
        }

        Inventory inventory = player.getInventory();
        for (int i = 0; i < recipe.ingredients().size(); i++) {
            int slotIndex = NullWorkbenchBlockEntity.INPUT_SLOT_START + i;
            ItemStack existing = menu.getWorkbench().getItemHandler().getStackInSlot(slotIndex);
            NullWorkbenchRecipes.IngredientCount ingredient = recipe.ingredients().get(i);
            int existingCount = existing.is(ingredient.stack().getItem()) ? existing.getCount() : 0;
            int missing = ingredient.stack().getCount() - existingCount;
            if (missing <= 0) {
                continue;
            }
            int extracted = extractFromInventory(inventory, ingredient.stack(), missing);
            if (extracted != missing) {
                return false;
            }
            ItemStack updated = existing.isEmpty() ? ingredient.stack().copy() : existing.copy();
            updated.setCount(existingCount + extracted);
            menu.getWorkbench().getItemHandler().setStackInSlot(slotIndex, updated);
        }
        menu.getWorkbench().setChangedAndSync();
        return true;
    }

    private static int countInInventory(Inventory inventory, ItemStack template) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(template.getItem())) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static int extractFromInventory(Inventory inventory, ItemStack template, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(template.getItem())) {
                continue;
            }
            int toTake = Math.min(remaining, stack.getCount());
            if (toTake <= 0) {
                continue;
            }
            stack.shrink(toTake);
            remaining -= toTake;
            if (remaining <= 0) {
                break;
            }
        }
        inventory.setChanged();
        return amount - remaining;
    }
}
