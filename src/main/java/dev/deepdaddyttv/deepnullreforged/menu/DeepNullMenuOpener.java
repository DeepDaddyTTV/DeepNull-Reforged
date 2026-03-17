package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class DeepNullMenuOpener {
    private DeepNullMenuOpener() {
    }

    public static void openHeldItem(ServerPlayer player, Inventory inventory, int inventorySlot) {
        if (inventorySlot < 0 || inventorySlot >= inventory.getContainerSize()) {
            return;
        }

        ItemStack stack = inventory.getItem(inventorySlot);
        if (!(stack.getItem() instanceof dev.deepdaddyttv.deepnullreforged.item.DeepNullItem deepNullItem)) {
            return;
        }

        DeepNullInventory deepNullInventory = new DeepNullInventory(deepNullItem.tier(), stack, player.level().registryAccess(), null);
        openHeldItem(player, inventory, inventorySlot, preferredView(deepNullInventory));
    }

    public static void openHeldItem(ServerPlayer player, Inventory inventory, int inventorySlot, DeepNullMenu.ViewMode viewMode) {
        if (inventorySlot < 0 || inventorySlot >= inventory.getContainerSize()) {
            return;
        }

        ItemStack stack = inventory.getItem(inventorySlot);
        if (!(stack.getItem() instanceof dev.deepdaddyttv.deepnullreforged.item.DeepNullItem deepNullItem)) {
            return;
        }

        DeepNullTier tier = deepNullItem.tier();
        DeepNullMenu.ViewMode normalizedView = normalizeView(viewMode);
        DeepNullInventory deepNullInventory = new DeepNullInventory(tier, stack, player.level().registryAccess(), null);
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DeepNullMenu.forItem(containerId, playerInventory, inventorySlot, tier, normalizedView),
                stack.getHoverName()
        ), buffer -> {
            buffer.writeVarInt(DeepNullMenu.SourceType.ITEM.ordinal());
            buffer.writeVarInt(normalizedView.ordinal());
            buffer.writeVarInt(tier.ordinalId());
            buffer.writeVarInt(inventorySlot);
            buffer.writeBlockPos(player.blockPosition());
            buffer.writeVarInt(upgradeMask(deepNullInventory));
            buffer.writeVarInt(deepNullInventory.getEnergyStored());
            buffer.writeBoolean(deepNullInventory.isChargingEnabled());
        });
    }

    public static void openDock(ServerPlayer player, DeepNullDockBlockEntity dock) {
        DeepNullInventory inventory = dock.createInventory();
        openDock(player, dock, inventory == null ? DeepNullMenu.ViewMode.MAIN : preferredView(inventory));
    }

    private static DeepNullMenu.ViewMode preferredView(DeepNullInventory inventory) {
        return inventory.isFluidOnly() ? DeepNullMenu.ViewMode.FLUID : DeepNullMenu.ViewMode.MAIN;
    }

    public static void openDock(ServerPlayer player, DeepNullDockBlockEntity dock, DeepNullMenu.ViewMode viewMode) {
        DeepNullTier tier = dock.getTier();
        DeepNullMenu.ViewMode normalizedView = normalizeView(viewMode);
        DeepNullInventory inventory = dock.createInventory();
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DeepNullMenu.forDock(containerId, playerInventory, dock, normalizedView),
                Component.translatable("block." + DeepNullReforged.MODID + ".deepnull_dock")
        ), buffer -> {
            buffer.writeVarInt(DeepNullMenu.SourceType.DOCK.ordinal());
            buffer.writeVarInt(normalizedView.ordinal());
            buffer.writeVarInt(tier.ordinalId());
            buffer.writeVarInt(-1);
            buffer.writeBlockPos(dock.getBlockPos());
            buffer.writeVarInt(inventory == null ? 0 : upgradeMask(inventory));
            buffer.writeVarInt(inventory == null ? 0 : inventory.getEnergyStored());
            buffer.writeBoolean(inventory != null && inventory.isChargingEnabled());
        });
    }

    private static DeepNullMenu.ViewMode normalizeView(DeepNullMenu.ViewMode viewMode) {
        return viewMode;
    }

    private static int upgradeMask(DeepNullInventory inventory) {
        int mask = 0;
        for (var type : dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType.values()) {
            if (inventory.hasUpgrade(type)) {
                mask |= 1 << type.slot();
            }
        }
        return mask;
    }
}
