package dev.deepdaddyttv.deepnullreforged.event;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.player.DeepNullPlayerState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CommonEvents {
    private static final int THROWN_ITEM_PICKUP_DELAY_TICKS = 20 * 10;

    private CommonEvents() {
    }

    public static void register() {
    }

    public static void markPlayerTossedItem(Player player, ItemEntity itemEntity) {
        itemEntity.setThrower(player);
        itemEntity.setPickUpDelay(THROWN_ITEM_PICKUP_DELAY_TICKS);
    }

    public static boolean handleItemPickup(Player player, ItemEntity itemEntity) {
        if (!DeepNullPlayerState.isGlobalAutoPickupEnabled(player) || itemEntity.hasPickUpDelay()) {
            return false;
        }

        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) {
            return false;
        }

        ItemStack remaining = stack.copy();
        boolean insertedAny = false;
        boolean voidOverflow = false;

        for (int slot = 0; slot < player.getInventory().getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (!(candidate.getItem() instanceof DeepNullItem deepNullItem)) {
                continue;
            }

            DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), candidate, player.level().registryAccess(), null);
            if (!inventory.isAutoPickupEnabled()) {
                continue;
            }
            int before = remaining.getCount();
            remaining = inventory.supportsFiltering()
                    ? inventory.insertPickedUpIntoFirstAvailableSlot(remaining, false)
                    : inventory.insertPickedUpIntoMatchingSlots(remaining, false);
            insertedAny |= remaining.getCount() != before;
            if (DeepNullConfig.voidFullItemsOnPickup() && !remaining.isEmpty() && inventory.shouldVoidOverflowingPickup(remaining)) {
                voidOverflow = true;
            }
        }

        if (!insertedAny && !voidOverflow) {
            return false;
        }

        if (remaining.isEmpty() || voidOverflow) {
            itemEntity.discard();
            return true;
        }

        itemEntity.setItem(remaining);
        return false;
    }
}
