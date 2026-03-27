package net.neoforged.neoforge.items;

import net.minecraft.world.item.ItemStack;

public final class ItemHandlerHelper {
    private ItemHandlerHelper() {
    }

    public static ItemStack insertItem(IItemHandler itemHandler, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < itemHandler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = itemHandler.insertItem(slot, remaining, simulate);
        }
        return remaining;
    }
}
