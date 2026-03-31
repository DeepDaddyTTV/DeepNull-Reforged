package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.minecraft.world.item.ItemStack;

final class ClientInteractionLogic {
    private ClientInteractionLogic() {
    }

    static int pickBlockSlot(DeepNullInventory inventory, ItemStack targetStack) {
        if (inventory.isFluidOnly()) {
            return -1;
        }
        return inventory.findMatchingSlot(targetStack);
    }

    static int cycleSelected(DeepNullInventory inventory, boolean forward) {
        inventory.cycleSelected(forward);
        return inventory.getSelectedSlot();
    }
}
