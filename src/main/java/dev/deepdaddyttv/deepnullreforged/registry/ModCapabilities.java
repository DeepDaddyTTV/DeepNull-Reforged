package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.capability.DeepNullEnergyStorage;
import dev.deepdaddyttv.deepnullreforged.capability.DeepNullFluidHandler;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.block.NullWorkbenchBlock;
import dev.deepdaddyttv.deepnullreforged.block.NullWorkbenchPart;
import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import dev.deepdaddyttv.deepnullreforged.integration.mekanism.MekanismCompat;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

public final class ModCapabilities {
    private ModCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        if (ModList.get().isLoaded("mekanism")) {
            MekanismCompat.registerCapabilities(event);
        }
        event.registerItem(
                Capabilities.ItemHandler.ITEM,
                (stack, context) -> createItemHandler(stack),
                ModItems.REDSTONE_DEEP_NULL.get(),
                ModItems.LAPIS_DEEP_NULL.get(),
                ModItems.IRON_DEEP_NULL.get(),
                ModItems.GOLD_DEEP_NULL.get(),
                ModItems.DIAMOND_DEEP_NULL.get(),
                ModItems.EMERALD_DEEP_NULL.get(),
                ModItems.CREATIVE_DEEP_NULL.get()
        );
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> createFluidHandler(stack),
                ModItems.REDSTONE_DAMP_NULL.get(),
                ModItems.LAPIS_DAMP_NULL.get(),
                ModItems.IRON_DAMP_NULL.get(),
                ModItems.GOLD_DAMP_NULL.get(),
                ModItems.DIAMOND_DAMP_NULL.get(),
                ModItems.EMERALD_DAMP_NULL.get(),
                ModItems.CREATIVE_DAMP_NULL.get(),
                ModItems.REDSTONE_DEEP_NULL.get(),
                ModItems.LAPIS_DEEP_NULL.get(),
                ModItems.IRON_DEEP_NULL.get(),
                ModItems.GOLD_DEEP_NULL.get(),
                ModItems.DIAMOND_DEEP_NULL.get(),
                ModItems.EMERALD_DEEP_NULL.get(),
                ModItems.CREATIVE_DEEP_NULL.get()
        );
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, context) -> createEnergyStorage(stack),
                ModItems.REDSTONE_DEEP_NULL.get(),
                ModItems.LAPIS_DEEP_NULL.get(),
                ModItems.IRON_DEEP_NULL.get(),
                ModItems.GOLD_DEEP_NULL.get(),
                ModItems.DIAMOND_DEEP_NULL.get(),
                ModItems.EMERALD_DEEP_NULL.get(),
                ModItems.CREATIVE_DEEP_NULL.get()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.DEEP_NULL_DOCK.get(),
                ModCapabilities::createDockEntityHandler
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.NULL_WORKBENCH.get(),
                (workbench, side) -> workbench.getAutomationHandler()
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.DEEP_NULL_DOCK.get(),
                ModCapabilities::createDockEntityFluidHandler
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.DEEP_NULL_DOCK.get(),
                ModCapabilities::createDockEntityEnergyStorage
        );
        event.registerBlock(
                Capabilities.ItemHandler.BLOCK,
                ModCapabilities::createDockHandler,
                ModBlocks.DEEP_NULL_DOCK.get()
        );
        event.registerBlock(
                Capabilities.ItemHandler.BLOCK,
                ModCapabilities::createNullWorkbenchHandler,
                ModBlocks.NULL_WORKBENCH.get()
        );
        event.registerBlock(
                Capabilities.FluidHandler.BLOCK,
                ModCapabilities::createDockFluidHandler,
                ModBlocks.DEEP_NULL_DOCK.get()
        );
        event.registerBlock(
                Capabilities.EnergyStorage.BLOCK,
                ModCapabilities::createDockEnergyStorage,
                ModBlocks.DEEP_NULL_DOCK.get()
        );
    }

    private static @Nullable IItemHandler createItemHandler(ItemStack stack) {
        DeepNullInventory inventory = createInventory(stack);
        return inventory == null || inventory.isFluidOnly() ? null : new VisibleItemHandler(inventory);
    }

    private static @Nullable IFluidHandlerItem createFluidHandler(ItemStack stack) {
        DeepNullInventory inventory = createInventory(stack);
        if (inventory == null || !inventory.supportsFluidStorage()) {
            return null;
        }
        return new DeepNullFluidHandler(inventory, stack, true);
    }

    private static @Nullable IEnergyStorage createEnergyStorage(ItemStack stack) {
        DeepNullInventory inventory = createInventory(stack);
        if (inventory == null || !inventory.hasEnergyUpgrade()) {
            return null;
        }
        return new DeepNullEnergyStorage(inventory);
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

    private static @Nullable IItemHandler createDockHandler(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity instanceof DeepNullDockBlockEntity dock) {
            return createDockEntityHandler(dock, side);
        }
        if (level.getBlockEntity(pos) instanceof DeepNullDockBlockEntity dock) {
            return createDockEntityHandler(dock, side);
        }
        return null;
    }

    private static @Nullable IFluidHandler createDockFluidHandler(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity instanceof DeepNullDockBlockEntity dock) {
            return createDockEntityFluidHandler(dock, side);
        }
        if (level.getBlockEntity(pos) instanceof DeepNullDockBlockEntity dock) {
            return createDockEntityFluidHandler(dock, side);
        }
        return null;
    }

    private static @Nullable IEnergyStorage createDockEnergyStorage(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity instanceof DeepNullDockBlockEntity dock) {
            return createDockEntityEnergyStorage(dock, side);
        }
        if (level.getBlockEntity(pos) instanceof DeepNullDockBlockEntity dock) {
            return createDockEntityEnergyStorage(dock, side);
        }
        return null;
    }

    private static @Nullable IItemHandler createNullWorkbenchHandler(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity instanceof NullWorkbenchBlockEntity workbench) {
            return workbench.getAutomationHandler();
        }
        BlockPos mainPos = resolveNullWorkbenchMainPos(pos, state);
        if (mainPos == null) {
            return null;
        }
        return level.getBlockEntity(mainPos) instanceof NullWorkbenchBlockEntity workbench
                ? workbench.getAutomationHandler()
                : null;
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
}
