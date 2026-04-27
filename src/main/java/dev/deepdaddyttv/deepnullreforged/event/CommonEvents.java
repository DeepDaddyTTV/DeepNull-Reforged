package dev.deepdaddyttv.deepnullreforged.event;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.integration.jei.DeepNullCraftingTransferSupport;
import dev.deepdaddyttv.deepnullreforged.integration.jei.ServerDeepNullJeiSession;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.player.DeepNullPlayerState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.TriState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

public final class CommonEvents {
    private static final int THROWN_ITEM_PICKUP_DELAY_TICKS = 20 * 10;

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        ItemEntity itemEntity = event.getEntity();
        itemEntity.setThrower(event.getPlayer());
        itemEntity.setPickUpDelay(THROWN_ITEM_PICKUP_DELAY_TICKS);
    }

    @SubscribeEvent
    public void onItemPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        if (!DeepNullPlayerState.isGlobalAutoPickupEnabled(player)) {
            return;
        }
        ItemEntity itemEntity = event.getItemEntity();
        if (itemEntity.hasPickUpDelay()) {
            return;
        }
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

    @SubscribeEvent
    public void onContainerClose(PlayerContainerEvent.Close event) {
        Player player = event.getEntity();
        if (!ServerDeepNullJeiSession.shouldReturn(player, event.getContainer())) {
            return;
        }

        if (DeepNullCraftingTransferSupport.returnCurrentCraftingContents(event.getContainer(), player)) {
            ServerDeepNullJeiSession.clear(player);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        DeepNullPlayerState.copyForClone(event.getOriginal(), event.getEntity());
    }
}
