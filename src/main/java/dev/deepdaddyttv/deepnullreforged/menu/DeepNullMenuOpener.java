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
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DeepNullMenu.forItem(containerId, playerInventory, inventorySlot, tier, normalizedView),
                Component.translatable(tier.displayTranslationKey())
        ), buffer -> {
            buffer.writeVarInt(DeepNullMenu.SourceType.ITEM.ordinal());
            buffer.writeVarInt(normalizedView.ordinal());
            buffer.writeVarInt(tier.ordinalId());
            buffer.writeVarInt(inventorySlot);
            buffer.writeBlockPos(player.blockPosition());
        });
    }

    public static void openDock(ServerPlayer player, DeepNullDockBlockEntity dock) {
        DeepNullInventory inventory = dock.createInventory();
        openDock(player, dock, inventory == null ? DeepNullMenu.ViewMode.MAIN : preferredView(inventory));
    }

    private static DeepNullMenu.ViewMode preferredView(DeepNullInventory inventory) {
        return DeepNullMenu.ViewMode.MAIN;
    }

    public static void openDock(ServerPlayer player, DeepNullDockBlockEntity dock, DeepNullMenu.ViewMode viewMode) {
        DeepNullTier tier = dock.getTier();
        DeepNullMenu.ViewMode normalizedView = normalizeView(viewMode);
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DeepNullMenu.forDock(containerId, playerInventory, dock, normalizedView),
                Component.translatable("block." + DeepNullReforged.MODID + ".deepnull_dock")
        ), buffer -> {
            buffer.writeVarInt(DeepNullMenu.SourceType.DOCK.ordinal());
            buffer.writeVarInt(normalizedView.ordinal());
            buffer.writeVarInt(tier.ordinalId());
            buffer.writeVarInt(-1);
            buffer.writeBlockPos(dock.getBlockPos());
        });
    }

    private static DeepNullMenu.ViewMode normalizeView(DeepNullMenu.ViewMode viewMode) {
        return viewMode == DeepNullMenu.ViewMode.FLUID ? DeepNullMenu.ViewMode.MAIN : viewMode;
    }
}
