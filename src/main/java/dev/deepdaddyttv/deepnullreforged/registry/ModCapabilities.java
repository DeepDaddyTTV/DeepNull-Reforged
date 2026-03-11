package dev.deepdaddyttv.deepnullreforged.registry;

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
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.DEEP_NULL_DOCK.get(),
                ModCapabilities::createDockEntityHandler
        );
        event.registerBlock(
                Capabilities.ItemHandler.BLOCK,
                ModCapabilities::createDockHandler,
                ModBlocks.DEEP_NULL_DOCK.get()
        );
    }

    private static @Nullable IItemHandler createItemHandler(ItemStack stack) {
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

    private static @Nullable IItemHandler createDockHandler(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity instanceof DeepNullDockBlockEntity dock) {
            return dock.getAutomationHandler(side);
        }
        if (level.getBlockEntity(pos) instanceof DeepNullDockBlockEntity dock) {
            return dock.getAutomationHandler(side);
        }
        return null;
    }

    private static @Nullable HolderLookup.Provider currentRegistries() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.registryAccess();
    }
}
