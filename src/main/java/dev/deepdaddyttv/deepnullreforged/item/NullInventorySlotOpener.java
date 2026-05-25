package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenuOpener;
import dev.deepdaddyttv.deepnullreforged.menu.DenNullMenuOpener;
import dev.deepdaddyttv.deepnullreforged.menu.DumpNullMenuOpener;
import dev.deepdaddyttv.deepnullreforged.menu.HubNullMenuOpener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class NullInventorySlotOpener {
    private NullInventorySlotOpener() {
    }

    public static boolean openFromInventorySlot(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !other.isEmpty()) {
            return false;
        }
        int inventorySlot = resolveOwnedInventorySlot(player.getInventory(), slot, stack);
        if (inventorySlot < 0) {
            return false;
        }
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            openStack(serverPlayer, player.getInventory(), inventorySlot);
        }
        return true;
    }

    public static int resolveOwnedInventorySlot(Inventory inventory, Slot slot, ItemStack stack) {
        if (inventory == null || slot == null || stack.isEmpty()) {
            return -1;
        }
        int inventorySlot = slot.getContainerSlot();
        if (inventorySlot < 0 || inventorySlot >= inventory.getContainerSize()) {
            return -1;
        }
        ItemStack ownedStack = inventory.getItem(inventorySlot);
        if (ownedStack.isEmpty() || !ItemStack.matches(ownedStack, stack)) {
            return -1;
        }
        return inventorySlot;
    }

    public static void openStack(ServerPlayer player, Inventory inventory, int inventorySlot) {
        if (inventorySlot < 0 || inventorySlot >= inventory.getContainerSize()) {
            return;
        }
        ItemStack stack = inventory.getItem(inventorySlot);
        if (stack.getItem() instanceof HubNullItem) {
            HubNullMenuOpener.openHeldItem(player, inventory, inventorySlot);
        } else if (stack.getItem() instanceof DripNullItem) {
            dev.deepdaddyttv.deepnullreforged.menu.DripNullMenuOpener.openHeldItem(player, inventory, inventorySlot);
        } else if (stack.getItem() instanceof DenNullItem) {
            DenNullMenuOpener.openHeldItem(player, inventory, inventorySlot);
        } else if (stack.getItem() instanceof DumpNullItem) {
            DumpNullMenuOpener.openHeldItem(player, inventory, inventorySlot);
        } else if (stack.getItem() instanceof DeepNullItem) {
            DeepNullMenuOpener.openHeldItem(player, inventory, inventorySlot);
        }
    }
}
