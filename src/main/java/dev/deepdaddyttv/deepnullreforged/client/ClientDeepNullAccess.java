package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ClientDeepNullAccess {
    private ClientDeepNullAccess() {
    }

    public static int findFirstDeepNullSlot(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).getItem() instanceof DeepNullItem) {
                return slot;
            }
        }
        return -1;
    }

    public static @Nullable HeldDeepNull findHeldDeepNull(Player player) {
        int mainHandSlot = player.getInventory().selected;
        ItemStack mainHandStack = player.getInventory().getItem(mainHandSlot);
        if (mainHandStack.getItem() instanceof DeepNullItem deepNullItem) {
            return new HeldDeepNull(
                    mainHandSlot,
                    mainHandStack,
                    new DeepNullInventory(deepNullItem.tier(), mainHandStack, player.level().registryAccess(), null)
            );
        }

        ItemStack offhandStack = player.getOffhandItem();
        if (offhandStack.getItem() instanceof DeepNullItem deepNullItem) {
            return new HeldDeepNull(
                    40,
                    offhandStack,
                    new DeepNullInventory(deepNullItem.tier(), offhandStack, player.level().registryAccess(), null)
            );
        }

        return null;
    }

    public record HeldDeepNull(int inventorySlot, ItemStack stack, DeepNullInventory inventory) {
    }
}
