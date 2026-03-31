package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.block.NullWorkbenchBlock;
import dev.deepdaddyttv.deepnullreforged.block.NullWorkbenchPart;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import dev.deepdaddyttv.deepnullreforged.capability.DeepNullEnergyStorage;
import dev.deepdaddyttv.deepnullreforged.capability.DeepNullFluidHandler;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public final class ModCapabilities {
    private ModCapabilities() {
    }

    public static void register() {
    }

    public static @Nullable IItemHandler getItemHandler(ItemStack stack) {
        DeepNullInventory inventory = createInventory(stack);
        return inventory == null || inventory.isFluidOnly() ? null : new VisibleItemHandler(inventory);
    }

    public static @Nullable IFluidHandlerItem getFluidHandler(ItemStack stack) {
        DeepNullInventory inventory = createInventory(stack);
        if (inventory == null || !inventory.supportsFluidStorage()) {
            return null;
        }
        return new DeepNullFluidHandler(inventory, stack, true);
    }

    public static @Nullable IEnergyStorage getEnergyStorage(ItemStack stack) {
        DeepNullInventory inventory = createInventory(stack);
        if (inventory != null && inventory.hasEnergyUpgrade()) {
            return new DeepNullEnergyStorage(inventory);
        }

        EnergyStorage fabricStorage = ContainerItemContext.withConstant(stack).find(EnergyStorage.ITEM);
        return fabricStorage == null ? null : new FabricEnergyStorage(fabricStorage);
    }

    public static @Nullable IItemHandler getBlockItemHandler(Level level, BlockPos pos, @Nullable Direction side) {
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        IItemHandler localHandler = createBlockItemHandler(level, pos, state, blockEntity, side);
        if (localHandler != null) {
            return localHandler;
        }

        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, side);
        if (storage instanceof SlottedStorage<ItemVariant> slottedStorage) {
            return new FabricSlottedItemHandler(slottedStorage);
        }

        return null;
    }

    public static @Nullable IFluidHandler getBlockFluidHandler(Level level, BlockPos pos, @Nullable Direction side) {
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        IFluidHandler localHandler = createBlockFluidHandler(level, pos, state, blockEntity, side);
        if (localHandler != null) {
            return localHandler;
        }

        Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, side);
        return storage == null ? null : new FabricFluidHandler(storage);
    }

    public static @Nullable IEnergyStorage getBlockEnergyStorage(Level level, BlockPos pos, @Nullable Direction side) {
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        IEnergyStorage localStorage = createBlockEnergyStorage(level, pos, state, blockEntity, side);
        if (localStorage != null) {
            return localStorage;
        }

        EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, side);
        return storage == null ? null : new FabricEnergyStorage(storage);
    }

    private static @Nullable DeepNullInventory createInventory(ItemStack stack) {
        if (!(stack.getItem() instanceof DeepNullItem deepNullItem)) {
            return null;
        }

        return new DeepNullInventory(
                deepNullItem.tier(),
                stack,
                ModCapabilities::currentRegistries,
                null
        );
    }

    private static @Nullable IItemHandler createDockEntityHandler(DeepNullDockBlockEntity dock, @Nullable Direction side) {
        return dock.getAutomationHandler(side);
    }

    private static @Nullable IFluidHandler createDockEntityFluidHandler(DeepNullDockBlockEntity dock, @Nullable Direction side) {
        DeepNullInventory inventory = dock.createInventory();
        if (inventory == null || !inventory.supportsFluidStorage()) {
            return null;
        }
        return new DeepNullFluidHandler(inventory, dock.getStoredDeepNull());
    }

    private static @Nullable IEnergyStorage createDockEntityEnergyStorage(DeepNullDockBlockEntity dock, @Nullable Direction side) {
        DeepNullInventory inventory = dock.createInventory();
        if (inventory == null || !inventory.hasEnergyUpgrade()) {
            return null;
        }
        return new DeepNullEnergyStorage(inventory);
    }

    private static @Nullable IItemHandler createBlockItemHandler(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity instanceof DeepNullDockBlockEntity dock) {
            return createDockEntityHandler(dock, side);
        }
        if (blockEntity instanceof NullWorkbenchBlockEntity workbench) {
            return workbench.getAutomationHandler();
        }

        BlockPos mainPos = resolveNullWorkbenchMainPos(pos, state);
        if (mainPos != null && level.getBlockEntity(mainPos) instanceof NullWorkbenchBlockEntity workbench) {
            return workbench.getAutomationHandler();
        }

        return null;
    }

    private static @Nullable IFluidHandler createBlockFluidHandler(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity instanceof DeepNullDockBlockEntity dock) {
            return createDockEntityFluidHandler(dock, side);
        }
        if (level.getBlockEntity(pos) instanceof DeepNullDockBlockEntity dock) {
            return createDockEntityFluidHandler(dock, side);
        }
        return null;
    }

    private static @Nullable IEnergyStorage createBlockEnergyStorage(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity instanceof DeepNullDockBlockEntity dock) {
            return createDockEntityEnergyStorage(dock, side);
        }
        if (level.getBlockEntity(pos) instanceof DeepNullDockBlockEntity dock) {
            return createDockEntityEnergyStorage(dock, side);
        }
        return null;
    }

    private static @Nullable BlockPos resolveNullWorkbenchMainPos(BlockPos pos, BlockState state) {
        if (!state.is(ModBlocks.NULL_WORKBENCH.get())) {
            return null;
        }

        return state.getValue(NullWorkbenchBlock.PART) == NullWorkbenchPart.MAIN
                ? pos
                : pos.relative(state.getValue(NullWorkbenchBlock.FACING).getClockWise().getOpposite());
    }

    private static @Nullable HolderLookup.Provider currentRegistries() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess();
        }

        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            Object minecraft = minecraftClass.getMethod("getInstance").invoke(null);
            Object level = minecraftClass.getField("level").get(minecraft);
            if (level == null) {
                return null;
            }
            return (HolderLookup.Provider) level.getClass().getMethod("registryAccess").invoke(level);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static int fromFabricAmount(long amount) {
        return (int) Math.min(Integer.MAX_VALUE, amount * FluidType.BUCKET_VOLUME / FluidConstants.BUCKET);
    }

    private static long toFabricAmount(int amount) {
        return Math.max(0L, (long) amount * FluidConstants.BUCKET / FluidType.BUCKET_VOLUME);
    }

    private static int toInt(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, value));
    }

    private static @Nullable StorageView<FluidVariant> firstFluidView(Storage<FluidVariant> storage) {
        for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
            if (!view.isResourceBlank() && view.getAmount() > 0) {
                return view;
            }
        }
        return null;
    }

    private static final class VisibleItemHandler implements IItemHandlerModifiable {
        private final DeepNullInventory inventory;

        private VisibleItemHandler(DeepNullInventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return stack.copyWithCount(Math.min(stack.getCount(), stack.getMaxStackSize()));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return inventory.isItemValid(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            inventory.setStackInSlot(slot, stack);
        }
    }

    private static final class FabricSlottedItemHandler implements IItemHandler {
        private final SlottedStorage<ItemVariant> storage;

        private FabricSlottedItemHandler(SlottedStorage<ItemVariant> storage) {
            this.storage = storage;
        }

        @Override
        public int getSlots() {
            return storage.getSlotCount();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            SingleSlotStorage<ItemVariant> singleSlot = storage.getSlot(slot);
            return singleSlot.isResourceBlank()
                    ? ItemStack.EMPTY
                    : singleSlot.getResource().toStack(toInt(singleSlot.getAmount()));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            try (Transaction transaction = Transaction.openOuter()) {
                long inserted = storage.getSlot(slot).insert(ItemVariant.of(stack), stack.getCount(), transaction);
                if (!simulate) {
                    transaction.commit();
                }

                return inserted >= stack.getCount()
                        ? ItemStack.EMPTY
                        : stack.copyWithCount(toInt(stack.getCount() - inserted));
            }
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0) {
                return ItemStack.EMPTY;
            }

            SingleSlotStorage<ItemVariant> singleSlot = storage.getSlot(slot);
            if (singleSlot.isResourceBlank()) {
                return ItemStack.EMPTY;
            }

            ItemVariant resource = singleSlot.getResource();
            try (Transaction transaction = Transaction.openOuter()) {
                long extracted = singleSlot.extract(resource, amount, transaction);
                if (!simulate) {
                    transaction.commit();
                }

                return extracted <= 0 ? ItemStack.EMPTY : resource.toStack(toInt(extracted));
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return toInt(storage.getSlot(slot).getCapacity());
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return !stack.isEmpty() && storage.getSlot(slot).supportsInsertion();
        }
    }

    private static final class FabricFluidHandler implements IFluidHandler {
        private final Storage<FluidVariant> storage;

        private FabricFluidHandler(Storage<FluidVariant> storage) {
            this.storage = storage;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            if (tank != 0) {
                return FluidStack.EMPTY;
            }

            StorageView<FluidVariant> view = firstFluidView(storage);
            return view == null
                    ? FluidStack.EMPTY
                    : new FluidStack(view.getResource().getFluid(), fromFabricAmount(view.getAmount()));
        }

        @Override
        public int getTankCapacity(int tank) {
            if (tank != 0) {
                return 0;
            }

            long capacity = 0L;
            for (StorageView<FluidVariant> view : storage) {
                capacity += view.getCapacity();
            }

            return fromFabricAmount(capacity);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && !stack.isEmpty() && storage.supportsInsertion();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return 0;
            }

            try (Transaction transaction = Transaction.openOuter()) {
                long inserted = storage.insert(FluidVariant.of(resource.getFluid()), toFabricAmount(resource.getAmount()), transaction);
                if (!action.simulate()) {
                    transaction.commit();
                }

                return fromFabricAmount(inserted);
            }
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }

            try (Transaction transaction = Transaction.openOuter()) {
                long extracted = storage.extract(FluidVariant.of(resource.getFluid()), toFabricAmount(resource.getAmount()), transaction);
                if (!action.simulate()) {
                    transaction.commit();
                }

                return extracted <= 0
                        ? FluidStack.EMPTY
                        : resource.copyWithAmount(fromFabricAmount(extracted));
            }
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0) {
                return FluidStack.EMPTY;
            }

            StorageView<FluidVariant> view = firstFluidView(storage);
            if (view == null) {
                return FluidStack.EMPTY;
            }

            FluidVariant resource = view.getResource();
            try (Transaction transaction = Transaction.openOuter()) {
                long extracted = storage.extract(resource, toFabricAmount(maxDrain), transaction);
                if (!action.simulate()) {
                    transaction.commit();
                }

                return extracted <= 0
                        ? FluidStack.EMPTY
                        : new FluidStack(resource.getFluid(), fromFabricAmount(extracted));
            }
        }
    }

    private static final class FabricEnergyStorage implements IEnergyStorage {
        private final EnergyStorage storage;

        private FabricEnergyStorage(EnergyStorage storage) {
            this.storage = storage;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (maxReceive <= 0) {
                return 0;
            }

            try (Transaction transaction = Transaction.openOuter()) {
                long inserted = storage.insert(maxReceive, transaction);
                if (!simulate) {
                    transaction.commit();
                }
                return toInt(inserted);
            }
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (maxExtract <= 0) {
                return 0;
            }

            try (Transaction transaction = Transaction.openOuter()) {
                long extracted = storage.extract(maxExtract, transaction);
                if (!simulate) {
                    transaction.commit();
                }
                return toInt(extracted);
            }
        }

        @Override
        public int getEnergyStored() {
            return toInt(storage.getAmount());
        }

        @Override
        public int getMaxEnergyStored() {
            return toInt(storage.getCapacity());
        }

        @Override
        public boolean canExtract() {
            return storage.supportsExtraction();
        }

        @Override
        public boolean canReceive() {
            return storage.supportsInsertion();
        }
    }
}
