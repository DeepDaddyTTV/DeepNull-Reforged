package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.dennull.DenNullData;
import dev.deepdaddyttv.deepnullreforged.item.DenNullItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ClientDenNullAccess {
    private ClientDenNullAccess() {
    }

    public static @Nullable HeldDenNull findHeldDenNull(Player player) {
        int mainHandSlot = player.getInventory().selected;
        ItemStack mainHandStack = player.getInventory().getItem(mainHandSlot);
        if (mainHandStack.getItem() instanceof DenNullItem denNullItem) {
            return new HeldDenNull(mainHandSlot, mainHandStack, denNullItem, DenNullData.get(mainHandStack));
        }

        ItemStack offhandStack = player.getOffhandItem();
        if (offhandStack.getItem() instanceof DenNullItem denNullItem) {
            return new HeldDenNull(40, offhandStack, denNullItem, DenNullData.get(offhandStack));
        }

        return null;
    }

    public static int cycleSelected(HeldDenNull held, boolean forward) {
        held.item().cycleSelected(held.stack(), forward);
        return DenNullData.get(held.stack()).selectedIndex();
    }

    public record HeldDenNull(int inventorySlot, ItemStack stack, DenNullItem item, DenNullData data) {
    }
}
