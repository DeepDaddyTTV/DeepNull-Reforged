package dev.deepdaddyttv.deepnullreforged.capability;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TransferCapabilityAdapters {
    private TransferCapabilityAdapters() {
    }

    public static ResourceHandler<ItemResource> item(IItemHandlerModifiable handler) {
        return new ItemHandlerResourceBridge(handler);
    }

    public static <S> ResourceHandler<FluidResource> fluid(
            DeepNullInventory inventory,
            Supplier<S> snapshotSource,
            Consumer<S> snapshotRestore
    ) {
        return new FluidHandlerResourceBridge<>(inventory, snapshotSource, snapshotRestore);
    }

    public static <S> EnergyHandler energy(
            DeepNullInventory inventory,
            Supplier<S> snapshotSource,
            Consumer<S> snapshotRestore
    ) {
        return new EnergyStorageResourceBridge<>(inventory, snapshotSource, snapshotRestore);
    }

    public static void restoreItemStack(ItemStack target, ItemStack snapshot) {
        if (target.isEmpty() || snapshot.isEmpty() || target.getItem() != snapshot.getItem()) {
            return;
        }
        target.applyComponents(snapshot.getComponentsPatch());
        target.setCount(snapshot.getCount());
    }

    private static final class ItemHandlerResourceBridge extends SnapshotJournal<NonNullList<ItemStack>> implements ResourceHandler<ItemResource> {
        private final IItemHandlerModifiable handler;

        private ItemHandlerResourceBridge(IItemHandlerModifiable handler) {
            this.handler = handler;
        }

        @Override
        public int size() {
            return handler.getSlots();
        }

        @Override
        public ItemResource getResource(int slot) {
            if (!isValidSlot(slot)) {
                return ItemResource.EMPTY;
            }
            ItemStack stack = handler.getStackInSlot(slot);
            return stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack.copyWithCount(1));
        }

        @Override
        public long getAmountAsLong(int slot) {
            return isValidSlot(slot) ? handler.getStackInSlot(slot).getCount() : 0L;
        }

        @Override
        public long getCapacityAsLong(int slot, ItemResource resource) {
            if (!isValidSlot(slot) || resource.isEmpty()) {
                return 0L;
            }
            return Math.min(handler.getSlotLimit(slot), resource.getMaxStackSize());
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return isValidSlot(slot) && !resource.isEmpty() && handler.isItemValid(slot, resource.toStack(1));
        }

        @Override
        public int insert(int slot, ItemResource resource, int maxAmount, TransactionContext transaction) {
            if (!isValid(slot, resource) || maxAmount <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            ItemStack remainder = handler.insertItem(slot, resource.toStack(maxAmount), false);
            return maxAmount - remainder.getCount();
        }

        @Override
        public int extract(int slot, ItemResource resource, int maxAmount, TransactionContext transaction) {
            if (!isValidSlot(slot) || maxAmount <= 0) {
                return 0;
            }

            ItemStack existing = handler.getStackInSlot(slot);
            if (existing.isEmpty() || (!resource.isEmpty() && !resource.matches(existing))) {
                return 0;
            }

            updateSnapshots(transaction);
            return handler.extractItem(slot, maxAmount, false).getCount();
        }

        @Override
        protected NonNullList<ItemStack> createSnapshot() {
            NonNullList<ItemStack> snapshot = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                snapshot.set(slot, handler.getStackInSlot(slot).copy());
            }
            return snapshot;
        }

        @Override
        protected void revertToSnapshot(NonNullList<ItemStack> snapshot) {
            for (int slot = 0; slot < snapshot.size(); slot++) {
                handler.setStackInSlot(slot, snapshot.get(slot).copy());
            }
        }

        private boolean isValidSlot(int slot) {
            return slot >= 0 && slot < handler.getSlots();
        }
    }

    private static final class FluidHandlerResourceBridge<S> extends SnapshotJournal<S> implements ResourceHandler<FluidResource> {
        private final DeepNullInventory inventory;
        private final Supplier<S> snapshotSource;
        private final Consumer<S> snapshotRestore;

        private FluidHandlerResourceBridge(DeepNullInventory inventory, Supplier<S> snapshotSource, Consumer<S> snapshotRestore) {
            this.inventory = inventory;
            this.snapshotSource = snapshotSource;
            this.snapshotRestore = snapshotRestore;
        }

        @Override
        public int size() {
            return inventory.supportsFluidStorage() ? inventory.getFluidSlotCount() : 0;
        }

        @Override
        public FluidResource getResource(int slot) {
            if (!isValidSlot(slot)) {
                return FluidResource.EMPTY;
            }
            FluidStack stack = inventory.getFluidInSlot(slot);
            return stack.isEmpty() ? FluidResource.EMPTY : FluidResource.of(stack);
        }

        @Override
        public long getAmountAsLong(int slot) {
            return isValidSlot(slot) ? inventory.getFluidInSlot(slot).getAmount() : 0L;
        }

        @Override
        public long getCapacityAsLong(int slot, FluidResource resource) {
            return isValidSlot(slot) && !resource.isEmpty() ? inventory.getFluidCapacity() : 0L;
        }

        @Override
        public boolean isValid(int slot, FluidResource resource) {
            return isValidSlot(slot)
                    && !resource.isEmpty()
                    && inventory.fillFluid(slot, resource.toStack(1), true) > 0;
        }

        @Override
        public int insert(int slot, FluidResource resource, int maxAmount, TransactionContext transaction) {
            if (!isValid(slot, resource) || maxAmount <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            return inventory.fillFluid(slot, resource.toStack(maxAmount), false);
        }

        @Override
        public int extract(int slot, FluidResource resource, int maxAmount, TransactionContext transaction) {
            if (!isValidSlot(slot) || maxAmount <= 0) {
                return 0;
            }

            FluidStack existing = inventory.getFluidInSlot(slot);
            if (existing.isEmpty() || (!resource.isEmpty() && !resource.matches(existing))) {
                return 0;
            }

            updateSnapshots(transaction);
            return inventory.drainFluid(slot, maxAmount, false).getAmount();
        }

        @Override
        protected S createSnapshot() {
            return snapshotSource.get();
        }

        @Override
        protected void revertToSnapshot(S snapshot) {
            snapshotRestore.accept(snapshot);
        }

        private boolean isValidSlot(int slot) {
            return inventory.supportsFluidStorage() && slot >= 0 && slot < inventory.getFluidSlotCount();
        }
    }

    private static final class EnergyStorageResourceBridge<S> extends SnapshotJournal<S> implements EnergyHandler {
        private final DeepNullInventory inventory;
        private final Supplier<S> snapshotSource;
        private final Consumer<S> snapshotRestore;

        private EnergyStorageResourceBridge(DeepNullInventory inventory, Supplier<S> snapshotSource, Consumer<S> snapshotRestore) {
            this.inventory = inventory;
            this.snapshotSource = snapshotSource;
            this.snapshotRestore = snapshotRestore;
        }

        @Override
        public long getAmountAsLong() {
            return inventory.getEnergyStored();
        }

        @Override
        public long getCapacityAsLong() {
            return inventory.getEnergyCapacity();
        }

        @Override
        public int insert(int maxAmount, TransactionContext transaction) {
            if (!inventory.hasEnergyUpgrade() || maxAmount <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            return inventory.receiveEnergy(maxAmount, false);
        }

        @Override
        public int extract(int maxAmount, TransactionContext transaction) {
            if (!inventory.hasEnergyUpgrade() || maxAmount <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            return inventory.extractEnergy(maxAmount, false);
        }

        @Override
        protected S createSnapshot() {
            return snapshotSource.get();
        }

        @Override
        protected void revertToSnapshot(S snapshot) {
            snapshotRestore.accept(snapshot);
        }
    }
}
