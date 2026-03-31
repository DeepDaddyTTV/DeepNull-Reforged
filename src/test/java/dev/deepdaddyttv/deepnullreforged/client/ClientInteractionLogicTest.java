package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.testutil.MinecraftBootstrap;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientInteractionLogicTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftBootstrap.ensureBootstrapped();
    }

    @Test
    void pickBlockReturnsMissingSlotForFluidOnlyInventories() {
        ClientInteractionLogic.InventoryView inventory = new FakeInventory(true, -1, -1);

        assertEquals(-1, ClientInteractionLogic.pickBlockSlot(inventory, ItemStack.EMPTY));
    }

    @Test
    void pickBlockDelegatesToMatchingSlotLookup() {
        ClientInteractionLogic.InventoryView inventory = new FakeInventory(false, 7, -1);

        assertEquals(7, ClientInteractionLogic.pickBlockSlot(inventory, ItemStack.EMPTY));
    }

    @Test
    void cycleSelectedReturnsUpdatedSelection() {
        ClientInteractionLogic.InventoryView inventory = new FakeInventory(false, -1, 4, 7);

        assertEquals(7, ClientInteractionLogic.cycleSelected(inventory, true));
    }

    private static final class FakeInventory implements ClientInteractionLogic.InventoryView {
        private final boolean fluidOnly;
        private final int matchingSlot;
        private final int nextSelectedSlot;
        private int selectedSlot;

        private FakeInventory(boolean fluidOnly, int matchingSlot, int selectedSlot) {
            this(fluidOnly, matchingSlot, selectedSlot, selectedSlot);
        }

        private FakeInventory(boolean fluidOnly, int matchingSlot, int selectedSlot, int nextSelectedSlot) {
            this.fluidOnly = fluidOnly;
            this.matchingSlot = matchingSlot;
            this.selectedSlot = selectedSlot;
            this.nextSelectedSlot = nextSelectedSlot;
        }

        @Override
        public boolean isFluidOnly() {
            return fluidOnly;
        }

        @Override
        public int findMatchingSlot(ItemStack targetStack) {
            return matchingSlot;
        }

        @Override
        public void cycleSelected(boolean forward) {
            selectedSlot = nextSelectedSlot;
        }

        @Override
        public int getSelectedSlot() {
            return selectedSlot;
        }
    }
}
