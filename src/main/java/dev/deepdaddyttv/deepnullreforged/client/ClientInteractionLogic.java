package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.minecraft.world.item.ItemStack;

final class ClientInteractionLogic {
    private ClientInteractionLogic() {
    }

    interface InventoryView {
        boolean isFluidOnly();

        int findMatchingSlot(ItemStack targetStack);

        void cycleSelected(boolean forward);

        int getSelectedSlot();
    }

    static int pickBlockSlot(DeepNullInventory inventory, ItemStack targetStack) {
        return pickBlockSlot(view(inventory), targetStack);
    }

    static int pickBlockSlot(InventoryView inventory, ItemStack targetStack) {
        if (inventory.isFluidOnly()) {
            return -1;
        }
        return inventory.findMatchingSlot(targetStack);
    }

    static int cycleSelected(DeepNullInventory inventory, boolean forward) {
        return cycleSelected(view(inventory), forward);
    }

    static int cycleSelected(InventoryView inventory, boolean forward) {
        inventory.cycleSelected(forward);
        return inventory.getSelectedSlot();
    }

    private static InventoryView view(DeepNullInventory inventory) {
        return new InventoryView() {
            @Override
            public boolean isFluidOnly() {
                return inventory.isFluidOnly();
            }

            @Override
            public int findMatchingSlot(ItemStack targetStack) {
                return inventory.findMatchingSlot(targetStack);
            }

            @Override
            public void cycleSelected(boolean forward) {
                inventory.cycleSelected(forward);
            }

            @Override
            public int getSelectedSlot() {
                return inventory.getSelectedSlot();
            }
        };
    }
}
