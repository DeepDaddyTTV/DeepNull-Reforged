package dev.deepdaddyttv.deepnullreforged.event;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

public final class CommonEvents {
    @SubscribeEvent
    public void onItemPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        ItemEntity itemEntity = event.getItemEntity();
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) {
            return;
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
            return;
        }

        if (remaining.isEmpty() || voidOverflow) {
            itemEntity.discard();
            event.setCanPickup(TriState.FALSE);
        } else {
            itemEntity.setItem(remaining);
        }
    }
}
