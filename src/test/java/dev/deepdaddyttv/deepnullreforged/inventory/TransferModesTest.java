package dev.deepdaddyttv.deepnullreforged.inventory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferModesTest {
    @Test
    void outputModeCycleAndFallbackStayStable() {
        assertEquals(TransferOutputMode.MATCHING, TransferOutputMode.ALL.cycle());
        assertEquals(TransferOutputMode.LOCKED, TransferOutputMode.MATCHING.cycle());
        assertEquals(TransferOutputMode.ALL, TransferOutputMode.LOCKED.cycle());
        assertEquals(TransferOutputMode.ALL, TransferOutputMode.byId(-1));
        assertEquals(TransferOutputMode.ALL, TransferOutputMode.byId(99));
        assertTrue(TransferOutputMode.MATCHING.matchingOnly());
        assertTrue(TransferOutputMode.LOCKED.isLocked());
        assertFalse(TransferOutputMode.ALL.isLocked());
    }

    @Test
    void directionModeCycleAndCapabilitiesStayStable() {
        assertEquals(TransferDirectionMode.INSERT, TransferDirectionMode.OMNIDIRECTIONAL.cycle());
        assertEquals(TransferDirectionMode.EXTRACT, TransferDirectionMode.INSERT.cycle());
        assertEquals(TransferDirectionMode.OMNIDIRECTIONAL, TransferDirectionMode.EXTRACT.cycle());
        assertEquals(TransferDirectionMode.OMNIDIRECTIONAL, TransferDirectionMode.byId(-1));
        assertEquals(TransferDirectionMode.OMNIDIRECTIONAL, TransferDirectionMode.byId(99));
        assertTrue(TransferDirectionMode.OMNIDIRECTIONAL.allowsInsert());
        assertTrue(TransferDirectionMode.OMNIDIRECTIONAL.allowsExtract());
        assertTrue(TransferDirectionMode.INSERT.allowsInsert());
        assertFalse(TransferDirectionMode.INSERT.allowsExtract());
        assertFalse(TransferDirectionMode.EXTRACT.allowsInsert());
        assertTrue(TransferDirectionMode.EXTRACT.allowsExtract());
    }
}
