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
        return fluid(() -> inventory, snapshotSource, snapshotRestore, false);
    }

    public static <S> ResourceHandler<FluidResource> fluid(
            DeepNullInventory inventory,
            Supplier<S> snapshotSource,
            Consumer<S> snapshotRestore,
            boolean singleSelectedTankView
    ) {
        return fluid(() -> inventory, snapshotSource, snapshotRestore, singleSelectedTankView);
    }

    public static <S> ResourceHandler<FluidResource> fluid(
            Supplier<@Nullable DeepNullInventory> inventorySupplier,
            Supplier<S> snapshotSource,
            Consumer<S> snapshotRestore
    ) {
        return fluid(inventorySupplier, snapshotSource, snapshotRestore, false);
    }

    public static <S> ResourceHandler<FluidResource> fluid(
            Supplier<@Nullable DeepNullInventory> inventorySupplier,
            Supplier<S> snapshotSource,
            Consumer<S> snapshotRestore,
            boolean singleSelectedTankView
    ) {
        return new FluidHandlerResourceBridge<>(inventorySupplier, snapshotSource, snapshotRestore, singleSelectedTankView);
    }

    public static <S> EnergyHandler energy(
            DeepNullInventory inventory,
            Supplier<S> snapshotSource,
            Consumer<S> snapshotRestore
    ) {
        return energy(() -> inventory, snapshotSource, snapshotRestore);
    }

    public static <S> EnergyHandler energy(
            Supplier<@Nullable DeepNullInventory> inventorySupplier,
            Supplier<S> snapshotSource,
            Consumer<S> snapshotRestore
    ) {
        return new EnergyStorageResourceBridge<>(inventorySupplier, snapshotSource, snapshotRestore);
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
            return handler.getSlotLimit(slot);
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
        private final Supplier<@Nullable DeepNullInventory> inventorySupplier;
        private final Supplier<S> snapshotSource;
        private final Consumer<S> snapshotRestore;
        private final boolean singleSelectedTankView;

        private FluidHandlerResourceBridge(
                Supplier<@Nullable DeepNullInventory> inventorySupplier,
                Supplier<S> snapshotSource,
                Consumer<S> snapshotRestore,
                boolean singleSelectedTankView
        ) {
            this.inventorySupplier = inventorySupplier;
            this.snapshotSource = snapshotSource;
            this.snapshotRestore = snapshotRestore;
            this.singleSelectedTankView = singleSelectedTankView;
        }

        @Override
        public int size() {
            DeepNullInventory inventory = currentInventory();
            if (inventory == null) {
                return 0;
            }
            return singleSelectedTankView ? 1 : inventory.getFluidSlotCount();
        }

        @Override
        public FluidResource getResource(int slot) {
            DeepNullInventory inventory = currentInventory();
            if (inventory == null) {
                return FluidResource.EMPTY;
            }
            int resolvedSlot = resolveSlot(inventory, slot);
            if (resolvedSlot < 0) {
                return FluidResource.EMPTY;
            }
            FluidStack stack = inventory.getFluidInSlot(resolvedSlot);
            return stack.isEmpty() ? FluidResource.EMPTY : FluidResource.of(stack);
        }

        @Override
        public long getAmountAsLong(int slot) {
            DeepNullInventory inventory = currentInventory();
            if (inventory == null) {
                return 0L;
            }
            int resolvedSlot = resolveSlot(inventory, slot);
            return resolvedSlot >= 0 ? inventory.getFluidInSlot(resolvedSlot).getAmount() : 0L;
        }

        @Override
        public long getCapacityAsLong(int slot, FluidResource resource) {
            DeepNullInventory inventory = currentInventory();
            if (inventory == null || resource.isEmpty()) {
                return 0L;
            }
            return resolveSlot(inventory, slot) >= 0 ? inventory.getFluidCapacity() : 0L;
        }

        @Override
        public boolean isValid(int slot, FluidResource resource) {
            DeepNullInventory inventory = currentInventory();
            if (inventory == null) {
                return false;
            }
            int resolvedSlot = resolveSlot(inventory, slot);
            return resolvedSlot >= 0
                    && !resource.isEmpty()
                    && inventory.fillFluid(resolvedSlot, resource.toStack(1), true) > 0;
        }

        @Override
        public int insert(int slot, FluidResource resource, int maxAmount, TransactionContext transaction) {
            DeepNullInventory inventory = currentInventory();
            if (inventory == null || maxAmount <= 0 || resource.isEmpty()) {
                return 0;
            }
            int resolvedSlot = resolveSlot(inventory, slot);
            if (resolvedSlot < 0 || inventory.fillFluid(resolvedSlot, resource.toStack(1), true) <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            return inventory.fillFluid(resolvedSlot, resource.toStack(maxAmount), false);
        }

        @Override
        public int extract(int slot, FluidResource resource, int maxAmount, TransactionContext transaction) {
            DeepNullInventory inventory = currentInventory();
            if (inventory == null || maxAmount <= 0) {
                return 0;
            }
            int resolvedSlot = resolveSlot(inventory, slot);
            if (resolvedSlot < 0) {
                return 0;
            }

            FluidStack existing = inventory.getFluidInSlot(resolvedSlot);
            if (existing.isEmpty() || (!resource.isEmpty() && !resource.matches(existing))) {
                return 0;
            }

            updateSnapshots(transaction);
            return inventory.drainFluid(resolvedSlot, maxAmount, false).getAmount();
        }

        @Override
        protected S createSnapshot() {
            return snapshotSource.get();
        }

        @Override
        protected void revertToSnapshot(S snapshot) {
            snapshotRestore.accept(snapshot);
        }

        private @Nullable DeepNullInventory currentInventory() {
            DeepNullInventory inventory = inventorySupplier.get();
            if (inventory == null || !inventory.supportsFluidStorage()) {
                return null;
            }
            return inventory;
        }

        private int resolveSlot(DeepNullInventory inventory, int slot) {
            if (!singleSelectedTankView) {
                return slot >= 0 && slot < inventory.getFluidSlotCount() ? slot : -1;
            }
            if (slot != 0) {
                return -1;
            }
            int selectedSlot = inventory.getSelectedSlot();
            if (selectedSlot >= 0 && selectedSlot < inventory.getFluidSlotCount()) {
                return selectedSlot;
            }
            return inventory.getFluidSlotCount() > 0 ? 0 : -1;
        }
    }

    private static final class EnergyStorageResourceBridge<S> extends SnapshotJournal<S> implements EnergyHandler {
        private final Supplier<@Nullable DeepNullInventory> inventorySupplier;
        private final Supplier<S> snapshotSource;
        private final Consumer<S> snapshotRestore;

        private EnergyStorageResourceBridge(Supplier<@Nullable DeepNullInventory> inventorySupplier, Supplier<S> snapshotSource, Consumer<S> snapshotRestore) {
            this.inventorySupplier = inventorySupplier;
            this.snapshotSource = snapshotSource;
            this.snapshotRestore = snapshotRestore;
        }

        @Override
        public long getAmountAsLong() {
            DeepNullInventory inventory = currentInventory();
            return inventory == null ? 0L : inventory.getEnergyStored();
        }

        @Override
        public long getCapacityAsLong() {
            DeepNullInventory inventory = currentInventory();
            return inventory == null ? 0L : inventory.getEnergyCapacity();
        }

        @Override
        public int insert(int maxAmount, TransactionContext transaction) {
            DeepNullInventory inventory = currentInventory();
            if (inventory == null || maxAmount <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            return inventory.receiveEnergy(maxAmount, false);
        }

        @Override
        public int extract(int maxAmount, TransactionContext transaction) {
            DeepNullInventory inventory = currentInventory();
            if (inventory == null || maxAmount <= 0) {
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

        private @Nullable DeepNullInventory currentInventory() {
            DeepNullInventory inventory = inventorySupplier.get();
            if (inventory == null || !inventory.hasEnergyUpgrade()) {
                return null;
            }
            return inventory;
        }
    }
}
