package dev.deepdaddyttv.deepnullreforged.block.entity;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import net.minecraft.core.Direction;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

public class DeepNullDockBlockEntity extends BlockEntity {
    private static final String STORED_DANK_TAG = "StoredDeepNull";
    private static final int EMPTY_DOCK_SLOT = 0;

    private ItemStack storedDeepNull = ItemStack.EMPTY;
    private final IItemHandler automationHandler = new DockAutomationHandler(this);

    public DeepNullDockBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DEEP_NULL_DOCK.get(), pos, blockState);
    }

    public boolean hasStoredDeepNull() {
        return !storedDeepNull.isEmpty() && storedDeepNull.getItem() instanceof DeepNullItem;
    }

    public ItemStack getStoredDeepNull() {
        return storedDeepNull;
    }

    public void setStoredDeepNull(ItemStack stack) {
        storedDeepNull = stack.copyWithCount(1);
        setChangedAndSync(true);
    }

    public void setStoredDeepNullClient(ItemStack stack) {
        storedDeepNull = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        Level level = getLevel();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    public boolean canAcceptDeepNull(ItemStack stack) {
        return !hasStoredDeepNull() && !stack.isEmpty() && stack.getItem() instanceof DeepNullItem;
    }

    public ItemStack removeStoredDeepNull() {
        ItemStack result = storedDeepNull.copy();
        storedDeepNull = ItemStack.EMPTY;
        setChangedAndSync(true);
        return result;
    }

    public DeepNullTier getTier() {
        if (storedDeepNull.getItem() instanceof DeepNullItem deepNullItem) {
            return deepNullItem.tier();
        }
        return DeepNullTier.REDSTONE;
    }

    public @Nullable DeepNullInventory createInventory() {
        if (!hasStoredDeepNull() || level == null) {
            return null;
        }
        return new DeepNullInventory(getTier(), storedDeepNull, level.registryAccess(), () -> setChangedAndSync(false));
    }

    public IItemHandler getAutomationHandler(@Nullable Direction side) {
        return automationHandler;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (hasStoredDeepNull()) {
            tag.put(STORED_DANK_TAG, storedDeepNull.saveOptional(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(STORED_DANK_TAG, Tag.TAG_COMPOUND)) {
            storedDeepNull = ItemStack.parseOptional(registries, tag.getCompound(STORED_DANK_TAG));
        } else {
            storedDeepNull = ItemStack.EMPTY;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void setChangedAndSync(boolean invalidateCapabilities) {
        setChanged();
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
            if (invalidateCapabilities) {
                level.invalidateCapabilities(worldPosition);
            }
        }
    }

    private static final class DockAutomationHandler implements IItemHandlerModifiable {
        private final DeepNullDockBlockEntity dock;

        private DockAutomationHandler(DeepNullDockBlockEntity dock) {
            this.dock = dock;
        }

        @Override
        public int getSlots() {
            DeepNullInventory inventory = dock.createInventory();
            return inventory == null ? 1 : inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            DeepNullInventory inventory = dock.createInventory();
            if (inventory == null) {
                return slot == EMPTY_DOCK_SLOT ? ItemStack.EMPTY : ItemStack.EMPTY;
            }
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            DeepNullInventory inventory = dock.createInventory();
            if (inventory == null) {
                if (slot != EMPTY_DOCK_SLOT || !dock.canAcceptDeepNull(stack)) {
                    return stack;
                }
                if (!simulate) {
                    dock.setStoredDeepNull(stack);
                }
                return remainder(stack, 1);
            }
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            DeepNullInventory inventory = dock.createInventory();
            return inventory == null ? ItemStack.EMPTY : inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            DeepNullInventory inventory = dock.createInventory();
            return inventory == null ? (slot == EMPTY_DOCK_SLOT ? 1 : 0) : inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            DeepNullInventory inventory = dock.createInventory();
            if (inventory == null) {
                return slot == EMPTY_DOCK_SLOT && dock.canAcceptDeepNull(stack);
            }
            return inventory.isItemValid(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            DeepNullInventory inventory = dock.createInventory();
            if (inventory == null) {
                if (slot == EMPTY_DOCK_SLOT && dock.canAcceptDeepNull(stack)) {
                    dock.setStoredDeepNull(stack);
                }
                return;
            }
            inventory.setStackInSlot(slot, stack);
        }

        private static ItemStack remainder(ItemStack stack, int extracted) {
            if (stack.isEmpty() || extracted <= 0) {
                return stack;
            }
            if (extracted >= stack.getCount()) {
                return ItemStack.EMPTY;
            }
            return stack.copyWithCount(stack.getCount() - extracted);
        }
    }
}
