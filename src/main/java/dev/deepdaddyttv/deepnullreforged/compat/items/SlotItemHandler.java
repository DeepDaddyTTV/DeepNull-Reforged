package dev.deepdaddyttv.deepnullreforged.compat.items;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotItemHandler extends Slot {
    protected final IItemHandler itemHandler;
    protected final int index;

    public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(new ItemHandlerContainer(itemHandler), index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return itemHandler.isItemValid(index, stack);
    }

    @Override
    public int getMaxStackSize() {
        return itemHandler.getSlotLimit(index);
    }

    @Override
    public ItemStack getItem() {
        return itemHandler.getStackInSlot(index);
    }

    @Override
    public void set(ItemStack stack) {
        if (itemHandler instanceof IItemHandlerModifiable modifiable) {
            modifiable.setStackInSlot(index, stack);
        }
        setChanged();
    }

    @Override
    public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
        set(newStack);
    }

    @Override
    public ItemStack remove(int amount) {
        return itemHandler.extractItem(index, amount, false);
    }

    @Override
    public boolean mayPickup(Player player) {
        return !itemHandler.extractItem(index, 1, true).isEmpty();
    }

    private static final class ItemHandlerContainer implements Container {
        private final IItemHandler itemHandler;

        private ItemHandlerContainer(IItemHandler itemHandler) {
            this.itemHandler = itemHandler;
        }

        @Override
        public int getContainerSize() {
            return itemHandler.getSlots();
        }

        @Override
        public boolean isEmpty() {
            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                if (!itemHandler.getStackInSlot(slot).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return itemHandler.getStackInSlot(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return itemHandler.extractItem(slot, amount, false);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return itemHandler.extractItem(slot, itemHandler.getStackInSlot(slot).getCount(), false);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (itemHandler instanceof IItemHandlerModifiable modifiable) {
                modifiable.setStackInSlot(slot, stack);
            }
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            if (itemHandler instanceof IItemHandlerModifiable modifiable) {
                for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                    modifiable.setStackInSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }
}
