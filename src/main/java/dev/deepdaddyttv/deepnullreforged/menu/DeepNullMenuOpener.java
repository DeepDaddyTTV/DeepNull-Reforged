package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
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

        DeepNullTier tier = deepNullItem.tier();
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DeepNullMenu.forItem(containerId, playerInventory, inventorySlot, tier),
                Component.translatable(tier.displayTranslationKey())
        ), buffer -> {
            buffer.writeVarInt(DeepNullMenu.SourceType.ITEM.ordinal());
            buffer.writeVarInt(tier.ordinalId());
            buffer.writeVarInt(inventorySlot);
            buffer.writeBlockPos(player.blockPosition());
        });
    }

    public static void openDock(ServerPlayer player, DeepNullDockBlockEntity dock) {
        DeepNullTier tier = dock.getTier();
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DeepNullMenu.forDock(containerId, playerInventory, dock),
                Component.translatable("block." + DeepNullReforged.MODID + ".deepnull_dock")
        ), buffer -> {
            buffer.writeVarInt(DeepNullMenu.SourceType.DOCK.ordinal());
            buffer.writeVarInt(tier.ordinalId());
            buffer.writeVarInt(-1);
            buffer.writeBlockPos(dock.getBlockPos());
        });
    }
}
