package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
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

    public static int findHotbarDeepNullSlot(Inventory inventory) {
        int selected = inventory.getSelectedSlot();
        if (selected >= 0 && selected < Inventory.getSelectionSize() && inventory.getItem(selected).getItem() instanceof DeepNullItem) {
            return selected;
        }
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (inventory.getItem(slot).getItem() instanceof DeepNullItem) {
                return slot;
            }
        }
        return -1;
    }

    public static @Nullable HeldDeepNull findHeldDeepNull(Player player) {
        int mainHandSlot = player.getInventory().getSelectedSlot();
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

    /**
     * Cheap alternative to {@link #findHeldDeepNull} for callers that only need to know what's
     * currently selected (e.g. HUD change-detection) and don't need read/write access to the
     * rest of the storage. Safe to call every tick or every frame.
     */
    public static @Nullable HeldDeepNullPreview peekHeldDeepNull(Player player) {
        int mainHandSlot = player.getInventory().getSelectedSlot();
        ItemStack mainHandStack = player.getInventory().getItem(mainHandSlot);
        if (mainHandStack.getItem() instanceof DeepNullItem deepNullItem) {
            return new HeldDeepNullPreview(
                    mainHandSlot,
                    mainHandStack,
                    DeepNullInventory.peekSelectedForRender(mainHandStack, deepNullItem instanceof DampNullItem)
            );
        }

        ItemStack offhandStack = player.getOffhandItem();
        if (offhandStack.getItem() instanceof DeepNullItem deepNullItem) {
            return new HeldDeepNullPreview(
                    40,
                    offhandStack,
                    DeepNullInventory.peekSelectedForRender(offhandStack, deepNullItem instanceof DampNullItem)
            );
        }

        return null;
    }

    public record HeldDeepNullPreview(int inventorySlot, ItemStack stack, DeepNullInventory.SelectedRenderPreview preview) {
    }
}
