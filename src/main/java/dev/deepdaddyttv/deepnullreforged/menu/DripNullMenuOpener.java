package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripNullData;
import dev.deepdaddyttv.deepnullreforged.item.DripNullItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class DripNullMenuOpener {
    private DripNullMenuOpener() {
    }

    public static void openHeldItem(ServerPlayer player, Inventory inventory, int inventorySlot) {
        openHeldItem(player, inventory, inventorySlot, DripNullMenu.ViewMode.PROFILES);
    }

    public static void openHeldItem(ServerPlayer player, Inventory inventory, int inventorySlot, DripNullMenu.ViewMode viewMode) {
        if (inventorySlot < 0 || inventorySlot >= inventory.getContainerSize()) {
            return;
        }
        ItemStack stack = inventory.getItem(inventorySlot);
        if (!(stack.getItem() instanceof DripNullItem dripNullItem)) {
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DripNullMenu.forItem(containerId, playerInventory, inventorySlot, dripNullItem.tier(), viewMode),
                stack.getHoverName()
        ), buffer -> {
            buffer.writeVarInt(DripNullMenu.SourceType.ITEM.ordinal());
            buffer.writeVarInt(viewMode.ordinal());
            buffer.writeVarInt(dripNullItem.tier().ordinalId());
            DripNullData.write(buffer, DripNullData.get(stack, dripNullItem.tier(), player.level().registryAccess()));
            buffer.writeVarInt(inventorySlot);
            buffer.writeBlockPos(player.blockPosition());
        });
    }

    public static void openDock(ServerPlayer player, DeepNullDockBlockEntity dock) {
        openDock(player, dock, DripNullMenu.ViewMode.PROFILES);
    }

    public static void openDock(ServerPlayer player, DeepNullDockBlockEntity dock, DripNullMenu.ViewMode viewMode) {
        ItemStack stack = dock.getStoredDeepNull();
        if (!(stack.getItem() instanceof DripNullItem dripNullItem)) {
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DripNullMenu.forDock(containerId, playerInventory, dock, dripNullItem.tier(), viewMode),
                Component.translatable("block." + DeepNullReforged.MODID + ".deepnull_dock")
        ), buffer -> {
            buffer.writeVarInt(DripNullMenu.SourceType.DOCK.ordinal());
            buffer.writeVarInt(viewMode.ordinal());
            buffer.writeVarInt(dripNullItem.tier().ordinalId());
            DripNullData.write(buffer, DripNullData.get(stack, dripNullItem.tier(), player.level().registryAccess()));
            buffer.writeVarInt(-1);
            buffer.writeBlockPos(dock.getBlockPos());
        });
    }
}
