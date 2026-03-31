package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.testutil.MinecraftBootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClientInteractionLogicTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftBootstrap.ensureBootstrapped();
    }

    @Test
    void pickBlockReturnsMissingSlotForFluidOnlyInventories() {
        DeepNullInventory inventory = mock(DeepNullInventory.class);
        ItemStack target = new ItemStack(Items.COBBLESTONE);
        when(inventory.isFluidOnly()).thenReturn(true);

        assertEquals(-1, ClientInteractionLogic.pickBlockSlot(inventory, target));
    }

    @Test
    void pickBlockDelegatesToMatchingSlotLookup() {
        DeepNullInventory inventory = mock(DeepNullInventory.class);
        ItemStack target = new ItemStack(Items.COBBLESTONE);
        when(inventory.findMatchingSlot(target)).thenReturn(7);

        assertEquals(7, ClientInteractionLogic.pickBlockSlot(inventory, target));
        verify(inventory).findMatchingSlot(target);
    }

    @Test
    void cycleSelectedReturnsUpdatedSelection() {
        DeepNullInventory inventory = mock(DeepNullInventory.class);
        when(inventory.getSelectedSlot()).thenReturn(4);

        assertEquals(4, ClientInteractionLogic.cycleSelected(inventory, true));
        verify(inventory).cycleSelected(true);
    }
}
