package dev.deepdaddyttv.deepnullreforged.inventory;

import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class DeepNullItemHandler extends ItemStackHandler {
    public static final String INVENTORY_TAG = "DeepNullInventory";
    private final ItemStack owner;
    private final DeepNullTier tier;
    private boolean loading;

    public DeepNullItemHandler(ItemStack owner, DeepNullTier tier) {
        super(tier.slots());
        this.owner = owner;
        this.tier = tier;
        CompoundNBT saved = owner.getTagElement(INVENTORY_TAG);
        if (saved != null) deserializeNBT(saved);
    }

    public DeepNullTier getTier() {
        return tier;
    }

    public ItemStack getOwner() {
        return owner;
    }

    @Override
    public int getSlotLimit(int slot) {
        return tier.itemCapacity();
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return tier.itemCapacity();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return !(stack.getItem() instanceof DeepNullItem) && !(stack.getItem() instanceof DampNullItem);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack incoming, boolean simulate) {
        validateSlotIndex(slot);
        if (incoming.isEmpty() || !isItemValid(slot, incoming)) return incoming;

        ItemStack existing = stacks.get(slot);
        if (!existing.isEmpty() && !canStack(existing, incoming)) return incoming;

        if (tier.creative()) {
            if (!simulate && existing.isEmpty()) {
                ItemStack stored = incoming.copy();
                stored.setCount(Math.max(1, Math.min(incoming.getCount(), incoming.getMaxStackSize())));
                stacks.set(slot, stored);
                onContentsChanged(slot);
            }
            return ItemStack.EMPTY;
        }

        long room = (long) getSlotLimit(slot) - (existing.isEmpty() ? 0L : existing.getCount());
        if (room <= 0L) return incoming;
        int accepted = (int) Math.min(room, (long) incoming.getCount());

        if (!simulate) {
            if (existing.isEmpty()) {
                ItemStack stored = incoming.copy();
                stored.setCount(accepted);
                stacks.set(slot, stored);
            } else {
                existing.grow(accepted);
            }
            onContentsChanged(slot);
        }

        if (accepted == incoming.getCount()) return ItemStack.EMPTY;
        ItemStack remainder = incoming.copy();
        remainder.shrink(accepted);
        return remainder;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateSlotIndex(slot);
        if (amount <= 0) return ItemStack.EMPTY;
        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        int extracted = Math.min(amount, existing.getCount());
        if (tier.creative()) extracted = Math.min(amount, existing.getMaxStackSize());
        ItemStack result = ItemHandlerHelper.copyStackWithSize(existing, extracted);
        if (!simulate && !tier.creative()) {
            existing.shrink(extracted);
            if (existing.isEmpty()) stacks.set(slot, ItemStack.EMPTY);
            onContentsChanged(slot);
        }
        return result;
    }

    public ItemStack insertMatching(ItemStack incoming, boolean allowEmptySlot) {
        ItemStack remainder = incoming;
        for (int i = 0; i < getSlots() && !remainder.isEmpty(); i++) {
            ItemStack existing = getStackInSlot(i);
            if (!existing.isEmpty() && canStack(existing, remainder)) {
                remainder = insertItem(i, remainder, false);
            }
        }
        if (allowEmptySlot) {
            for (int i = 0; i < getSlots() && !remainder.isEmpty(); i++) {
                if (getStackInSlot(i).isEmpty()) remainder = insertItem(i, remainder, false);
            }
        }
        return remainder;
    }

    public boolean containsMatching(ItemStack stack) {
        for (int i = 0; i < getSlots(); i++) {
            if (canStack(getStackInSlot(i), stack)) return true;
        }
        return false;
    }

    public boolean canAcceptMatching(ItemStack incoming) {
        ItemStack remainder = incoming;
        for (int i = 0; i < getSlots() && !remainder.isEmpty(); i++) {
            ItemStack existing = getStackInSlot(i);
            if (!existing.isEmpty() && canStack(existing, remainder)) {
                remainder = insertItem(i, remainder, true);
            }
        }
        return remainder.isEmpty();
    }

    private static boolean canStack(ItemStack first, ItemStack second) {
        return !first.isEmpty() && !second.isEmpty() && ItemStack.isSame(first, second) && ItemStack.tagMatches(first, second);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (!loading) saveOwner();
    }

    public void saveOwner() {
        owner.getOrCreateTag().put(INVENTORY_TAG, serializeNBT());
    }

    @Override
    public CompoundNBT serializeNBT() {
        ListNBT list = new ListNBT();
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (stack.isEmpty()) continue;
            CompoundNBT entry = new CompoundNBT();
            entry.putInt("Slot", slot);
            stack.save(entry);
            entry.putInt("ExtendedCount", stack.getCount());
            list.add(entry);
        }
        CompoundNBT root = new CompoundNBT();
        root.put("Items", list);
        return root;
    }

    @Override
    public void deserializeNBT(CompoundNBT root) {
        loading = true;
        stacks = NonNullList.withSize(tier.slots(), ItemStack.EMPTY);
        ListNBT list = root.getList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entry = list.getCompound(i);
            int slot = entry.getInt("Slot");
            if (slot < 0 || slot >= stacks.size()) continue;
            ItemStack stack = ItemStack.of(entry);
            if (entry.contains("ExtendedCount", Constants.NBT.TAG_INT)) {
                stack.setCount(Math.min(entry.getInt("ExtendedCount"), tier.itemCapacity()));
            }
            stacks.set(slot, stack);
        }
        loading = false;
        onLoad();
    }
}
