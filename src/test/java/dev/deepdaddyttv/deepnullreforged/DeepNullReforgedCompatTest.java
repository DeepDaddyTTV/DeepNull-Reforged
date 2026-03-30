package dev.deepdaddyttv.deepnullreforged;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class DeepNullReforgedCompatTest {
    @Test
    void inventorySorterBlacklistsStayStable() {
        assertIterableEquals(
                List.of(
                        "dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu$StorageSlot",
                        "dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu$DockStorageSlot",
                        "dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu$FluidStorageSlot"
                ),
                DeepNullReforged.inventorySorterSlotBlacklists()
        );
        assertEquals("deepnullreforged:deep_null", DeepNullReforged.inventorySorterContainerBlacklist().toString());
    }
}
