package dev.deepdaddyttv.deepnullreforged.inventory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NullProtocolIdsTest {
    @Test
    void storageDomainAndActionIdsAreExplicitAndStable() {
        assertEquals(1, NullSlotDomain.ITEM_STORAGE.id());
        assertEquals(2, NullSlotDomain.FLUID_STORAGE.id());
        assertEquals(NullSlotDomain.FLUID_STORAGE, NullSlotDomain.byId(2));
        assertNull(NullSlotDomain.byId(0));

        for (NullStorageAction action : NullStorageAction.values()) {
            assertEquals(action, NullStorageAction.byId(action.id()));
        }
        assertNull(NullStorageAction.byId(0));
    }

    @Test
    void extractionModeIdsAreExplicitAndKeepAllIsNotAComputedCustomValue() {
        assertEquals(1, ItemExtractionMode.KEEP_ALL.protocolId());
        assertEquals(Integer.MAX_VALUE, ItemExtractionMode.KEEP_ALL.keptAmount());
        for (ItemExtractionMode mode : ItemExtractionMode.values()) {
            assertEquals(mode, ItemExtractionMode.byProtocolId(mode.protocolId()));
        }
        assertNull(ItemExtractionMode.byProtocolId(0));
    }
}
