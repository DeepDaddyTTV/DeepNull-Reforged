package dev.deepdaddyttv.deepnullreforged.compat.items;

import dev.deepdaddyttv.deepnullreforged.util.NbtCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ItemStackHandler implements IItemHandlerModifiable {
    protected NonNullList<ItemStack> stacks;

    public ItemStackHandler() {
        this(1);
    }

    public ItemStackHandler(int size) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public void setSize(int size) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        onLoad();
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return stacks.get(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        ItemStack copy = stack.copy();
        int limit = getSlotLimit(slot);
        if (!copy.isEmpty() && copy.getCount() > limit) {
            copy.setCount(limit);
        }
        stacks.set(slot, copy);
        onContentsChanged(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        validateSlotIndex(slot);
        if (!isItemValid(slot, stack)) {
            return stack;
        }

        ItemStack existing = stacks.get(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(existing, stack)) {
                return stack;
            }
            limit -= existing.getCount();
        }

        if (limit <= 0) {
            return stack;
        }

        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        validateSlotIndex(slot);
        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int extracted = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= extracted) {
            if (!simulate) {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
            }
            return existing.copy();
        }

        ItemStack result = existing.copyWithCount(extracted);
        if (!simulate) {
            existing.shrink(extracted);
            onContentsChanged(slot);
        }
        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ')');
        }
    }

    protected void onContentsChanged(int slot) {
    }

    protected void onLoad() {
    }

    public void serialize(ValueOutput output) {
        output.putInt("Size", stacks.size());
        ValueOutput.ValueOutputList items = output.childrenList("Items");
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (stack.isEmpty()) {
                continue;
            }

            ValueOutput entry = items.addChild();
            entry.putInt("Slot", slot);
            entry.store("Stack", ItemStack.OPTIONAL_CODEC, stack);
        }
    }

    public void deserialize(ValueInput input) {
        int size = input.getIntOr("Size", stacks.size());
        if (size != stacks.size()) {
            setSize(size);
        }

        for (int slot = 0; slot < stacks.size(); slot++) {
            stacks.set(slot, ItemStack.EMPTY);
        }

        for (ValueInput entry : input.childrenListOrEmpty("Items")) {
            int slot = entry.getIntOr("Slot", -1);
            if (slot < 0 || slot >= stacks.size()) {
                continue;
            }
            stacks.set(slot, entry.read("Stack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        }

        onLoad();
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag stackTag = new CompoundTag();
            stackTag.putInt("Slot", slot);
            CompoundTag savedTag = NbtCodecs.encodeItemStack(provider, stack);
            if (!savedTag.isEmpty()) {
                stackTag.merge(savedTag);
            }
            items.add(stackTag);
        }
        tag.put("Items", items);
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        ListTag items = nbt.getListOrEmpty("Items");
        for (int slot = 0; slot < stacks.size(); slot++) {
            stacks.set(slot, ItemStack.EMPTY);
        }
        for (int i = 0; i < items.size(); i++) {
            CompoundTag stackTag = items.getCompoundOrEmpty(i);
            int slot = stackTag.getIntOr("Slot", -1);
            if (slot >= 0 && slot < stacks.size()) {
                CompoundTag itemTag = stackTag.copy();
                itemTag.remove("Slot");
                stacks.set(slot, NbtCodecs.decodeItemStack(provider, itemTag));
            }
        }
        onLoad();
    }
}
