package dev.deepdaddyttv.deepnullreforged.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class LegacyCapabilityBridge {
    private static final @Nullable Constructor<?> ITEM_ADAPTER = findConstructor(
            "net.neoforged.neoforge.items.ItemResourceHandlerAdapter",
            ResourceHandler.class
    );
    private static final @Nullable Constructor<?> FLUID_ADAPTER = findConstructor(
            "net.neoforged.neoforge.fluids.capability.FluidResourceHandlerAdapter",
            ResourceHandler.class
    );
    private static final @Nullable Constructor<?> ENERGY_ADAPTER = findConstructor(
            "net.neoforged.neoforge.energy.EnergyHandlerAdapter",
            EnergyHandler.class
    );

    private LegacyCapabilityBridge() {
    }

    public static @Nullable IItemHandler getItemHandler(Level level, BlockPos pos, @Nullable Direction side) {
        return adaptItemHandler(level.getCapability(Capabilities.Item.BLOCK, pos, side));
    }

    public static @Nullable IFluidHandler getFluidHandler(Level level, BlockPos pos, @Nullable Direction side) {
        return adaptFluidHandler(level.getCapability(Capabilities.Fluid.BLOCK, pos, side));
    }

    public static @Nullable IEnergyStorage getEnergyStorage(ItemStack stack) {
        return adaptEnergyStorage(stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(stack)));
    }

    @SuppressWarnings("unchecked")
    public static @Nullable IItemHandler adaptItemHandler(@Nullable ResourceHandler<ItemResource> handler) {
        return (IItemHandler) newAdapter(ITEM_ADAPTER, handler);
    }

    @SuppressWarnings("unchecked")
    public static @Nullable IFluidHandler adaptFluidHandler(@Nullable ResourceHandler<FluidResource> handler) {
        return (IFluidHandler) newAdapter(FLUID_ADAPTER, handler);
    }

    public static @Nullable IEnergyStorage adaptEnergyStorage(@Nullable EnergyHandler handler) {
        return (IEnergyStorage) newAdapter(ENERGY_ADAPTER, handler);
    }

    private static @Nullable Constructor<?> findConstructor(String className, Class<?> parameterType) {
        try {
            Constructor<?> constructor = Class.forName(className).getDeclaredConstructor(parameterType);
            constructor.setAccessible(true);
            return constructor;
        } catch (ClassNotFoundException | NoSuchMethodException exception) {
            return null;
        }
    }

    private static @Nullable Object newAdapter(@Nullable Constructor<?> constructor, @Nullable Object handler) {
        if (constructor == null || handler == null) {
            return null;
        }
        try {
            return constructor.newInstance(handler);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            return null;
        }
    }
}
