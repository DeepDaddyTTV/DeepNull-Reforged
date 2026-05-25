package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullData;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullSnapshot;
import dev.deepdaddyttv.deepnullreforged.item.HubNullItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class HubNullMenuOpener {
    private HubNullMenuOpener() {
    }

    public static void openHeldItem(ServerPlayer player, Inventory inventory, int inventorySlot) {
        if (inventorySlot < 0 || inventorySlot >= inventory.getContainerSize()) {
            return;
        }
        ItemStack stack = inventory.getItem(inventorySlot);
        if (!(stack.getItem() instanceof HubNullItem)) {
            return;
        }
        HubNullSnapshot snapshot = HubNullSnapshot.build(player.server, HubNullData.get(stack));
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> HubNullMenu.forItem(containerId, playerInventory, inventorySlot, snapshot),
                Component.translatable("item." + DeepNullReforged.MODID + ".hub_null")
        ), buffer -> HubNullMenu.writeState(buffer, inventorySlot, snapshot));
    }

    public static void openRemoteDock(ServerPlayer player, int hubInventorySlot, dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationRef ref, DeepNullDockBlockEntity dock) {
        DeepNullMenuOpener.openRemoteDock(player, hubInventorySlot, ref, dock);
    }
}
