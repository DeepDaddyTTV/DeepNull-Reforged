package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;

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
        DeepNullInventory deepNullInventory = new DeepNullInventory(tier, stack, player.level().registryAccess(), null);
        DeepNullMenu.ViewMode normalizedView = normalizeView(viewMode, deepNullInventory);
        player.openMenu(new ExtendedMenuProvider<DeepNullMenu.OpenData>() {
            @Override
            public DeepNullMenu.OpenData getScreenOpeningData(ServerPlayer serverPlayer) {
                return DeepNullMenu.OpenData.forItem(
                        tier,
                        normalizedView,
                        inventorySlot,
                        upgradeMask(deepNullInventory),
                        deepNullInventory.getEnergyStored(),
                        deepNullInventory.isChargingEnabled(),
                        player.blockPosition()
                );
            }

            @Override
            public Component getDisplayName() {
                return stack.getHoverName();
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player livingPlayer) {
                return DeepNullMenu.forItem(containerId, playerInventory, inventorySlot, tier, normalizedView);
            }
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
        DeepNullInventory inventory = dock.createInventory();
        DeepNullMenu.ViewMode normalizedView = normalizeView(viewMode, inventory);
        player.openMenu(new ExtendedMenuProvider<DeepNullMenu.OpenData>() {
            @Override
            public DeepNullMenu.OpenData getScreenOpeningData(ServerPlayer serverPlayer) {
                return DeepNullMenu.OpenData.forDock(
                        tier,
                        normalizedView,
                        dock.getBlockPos(),
                        inventory == null ? 0 : upgradeMask(inventory),
                        inventory == null ? 0 : inventory.getEnergyStored(),
                        inventory != null && inventory.isChargingEnabled()
                );
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("block." + DeepNullReforged.MODID + ".deepnull_dock");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player livingPlayer) {
                return DeepNullMenu.forDock(containerId, playerInventory, dock, normalizedView);
            }
        });
    }

    private static DeepNullMenu.ViewMode normalizeView(DeepNullMenu.ViewMode viewMode, DeepNullInventory inventory) {
        if (inventory != null && inventory.isFluidOnly() && viewMode == DeepNullMenu.ViewMode.MAIN) {
            return DeepNullMenu.ViewMode.FLUID;
        }
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
