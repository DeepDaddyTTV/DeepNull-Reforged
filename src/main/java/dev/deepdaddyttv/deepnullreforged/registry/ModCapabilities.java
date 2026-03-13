package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.capability.DeepNullEnergyStorage;
import dev.deepdaddyttv.deepnullreforged.capability.DeepNullFluidHandler;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
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
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public final class ModCapabilities {
    private ModCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
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
        return inventory == null || inventory.isFluidOnly() ? null : inventory;
    }

    private static @Nullable IFluidHandlerItem createFluidHandler(ItemStack stack) {
        DeepNullInventory inventory = createInventory(stack);
        if (inventory == null || !inventory.supportsFluidStorage()) {
            return null;
        }
        return new DeepNullFluidHandler(inventory, stack);
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
        DeepNullInventory inventory = dock.createInventory();
        return inventory == null || inventory.isFluidOnly() ? null : dock.getAutomationHandler(side);
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

    private static @Nullable HolderLookup.Provider currentRegistries() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.registryAccess();
    }
}
