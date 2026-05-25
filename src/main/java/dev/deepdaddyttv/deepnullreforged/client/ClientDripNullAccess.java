package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.dripnull.DripNullData;
import dev.deepdaddyttv.deepnullreforged.item.DripNullItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ClientDripNullAccess {
    private ClientDripNullAccess() {
    }

    public static @Nullable HeldDripNull findHeldDripNull(Player player) {
        int mainHandSlot = player.getInventory().selected;
        ItemStack mainHandStack = player.getInventory().getItem(mainHandSlot);
        if (mainHandStack.getItem() instanceof DripNullItem dripNullItem) {
            return new HeldDripNull(mainHandSlot, mainHandStack, dripNullItem, DripNullData.get(mainHandStack, dripNullItem.tier(), null));
        }

        ItemStack offhandStack = player.getOffhandItem();
        if (offhandStack.getItem() instanceof DripNullItem dripNullItem) {
            return new HeldDripNull(40, offhandStack, dripNullItem, DripNullData.get(offhandStack, dripNullItem.tier(), null));
        }

        return null;
    }

    public record HeldDripNull(int inventorySlot, ItemStack stack, DripNullItem item, DripNullData data) {
    }
}
