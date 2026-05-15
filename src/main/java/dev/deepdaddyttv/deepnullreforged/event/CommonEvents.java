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
        Player player = event.getPlayer();
        ItemEntity itemEntity = event.getEntity();
        if (!DeepNullPlayerState.isGlobalAutoPickupEnabled(player)) {
            return;
        }
        AutoPickupResult pickupResult = applyDeepNullAutoPickup(player, itemEntity.getItem(), true);
        if (!pickupResult.handled()) {
            return;
        }
        itemEntity.setThrower(player);
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

        AutoPickupResult pickupResult = applyDeepNullAutoPickup(player, stack, false);
        if (!pickupResult.handled()) {
            return;
        }

        if (pickupResult.remaining().isEmpty() || pickupResult.voidOverflow()) {
            itemEntity.discard();
            event.setCanPickup(TriState.FALSE);
        } else {
            itemEntity.setItem(pickupResult.remaining());
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

    private static AutoPickupResult applyDeepNullAutoPickup(Player player, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return new AutoPickupResult(ItemStack.EMPTY, false, false);
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
                    ? inventory.insertPickedUpIntoFirstAvailableSlot(remaining, simulate)
                    : inventory.insertPickedUpIntoMatchingSlots(remaining, simulate);
            insertedAny |= remaining.getCount() != before;
            if (DeepNullConfig.voidFullItemsOnPickup() && !remaining.isEmpty() && inventory.shouldVoidOverflowingPickup(remaining)) {
                voidOverflow = true;
            }
        }

        return new AutoPickupResult(remaining, insertedAny, voidOverflow);
    }

    private record AutoPickupResult(ItemStack remaining, boolean insertedAny, boolean voidOverflow) {
        private boolean handled() {
            return insertedAny || voidOverflow;
        }
    }
}
