package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullData;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationRef;
import dev.deepdaddyttv.deepnullreforged.item.DenNullItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class DenNullMenuOpener {
    private DenNullMenuOpener() {
    }

    public static void openHeldItem(ServerPlayer player, Inventory inventory, int inventorySlot) {
        openHeldItem(player, inventory, inventorySlot, DenNullMenu.ViewMode.GRID);
    }

    public static void openHeldItem(ServerPlayer player, Inventory inventory, int inventorySlot, DenNullMenu.ViewMode viewMode) {
        if (inventorySlot < 0 || inventorySlot >= inventory.getContainerSize()) {
            return;
        }
        ItemStack stack = inventory.getItem(inventorySlot);
        if (!(stack.getItem() instanceof DenNullItem denNullItem)) {
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DenNullMenu.forItem(containerId, playerInventory, inventorySlot, denNullItem.tier(), viewMode),
                stack.getHoverName()
        ), buffer -> {
            buffer.writeVarInt(DenNullMenu.SourceType.ITEM.ordinal());
            buffer.writeVarInt(viewMode.ordinal());
            buffer.writeVarInt(denNullItem.tier().ordinalId());
            DenNullData.write(buffer, DenNullData.get(stack));
            buffer.writeVarInt(inventorySlot);
            buffer.writeBlockPos(player.blockPosition());
            buffer.writeResourceLocation(ResourceLocation.withDefaultNamespace("overworld"));
            buffer.writeVarInt(-1);
        });
    }

    public static void openDock(ServerPlayer player, DeepNullDockBlockEntity dock) {
        openDock(player, dock, DenNullMenu.ViewMode.GRID);
    }

    public static void openDock(ServerPlayer player, DeepNullDockBlockEntity dock, DenNullMenu.ViewMode viewMode) {
        ItemStack stack = dock.getStoredDeepNull();
        if (!(stack.getItem() instanceof DenNullItem denNullItem)) {
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DenNullMenu.forDock(containerId, playerInventory, dock, viewMode),
                Component.translatable("block." + DeepNullReforged.MODID + ".deepnull_dock")
        ), buffer -> {
            buffer.writeVarInt(DenNullMenu.SourceType.DOCK.ordinal());
            buffer.writeVarInt(viewMode.ordinal());
            buffer.writeVarInt(denNullItem.tier().ordinalId());
            DenNullData.write(buffer, DenNullData.get(stack));
            buffer.writeVarInt(-1);
            buffer.writeBlockPos(dock.getBlockPos());
            buffer.writeResourceLocation(ResourceLocation.withDefaultNamespace("overworld"));
            buffer.writeVarInt(-1);
        });
    }

    public static void openRemoteDock(ServerPlayer player, int hubInventorySlot, HubNullStationRef ref, DeepNullDockBlockEntity dock) {
        openRemoteDock(player, hubInventorySlot, ref, dock, DenNullMenu.ViewMode.GRID);
    }

    public static void openRemoteDock(ServerPlayer player, int hubInventorySlot, HubNullStationRef ref, DeepNullDockBlockEntity dock, DenNullMenu.ViewMode viewMode) {
        ItemStack stack = dock.getStoredDeepNull();
        if (!(stack.getItem() instanceof DenNullItem denNullItem)) {
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DenNullMenu.forRemoteDock(containerId, playerInventory, dock, ref.dimension(), hubInventorySlot, viewMode),
                stack.getHoverName()
        ), buffer -> {
            buffer.writeVarInt(DenNullMenu.SourceType.REMOTE_DOCK.ordinal());
            buffer.writeVarInt(viewMode.ordinal());
            buffer.writeVarInt(denNullItem.tier().ordinalId());
            DenNullData.write(buffer, DenNullData.get(stack));
            buffer.writeVarInt(-1);
            buffer.writeBlockPos(dock.getBlockPos());
            buffer.writeResourceLocation(ref.dimension());
            buffer.writeVarInt(hubInventorySlot);
        });
    }
}
