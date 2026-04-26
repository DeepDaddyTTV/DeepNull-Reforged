package dev.deepdaddyttv.deepnullreforged;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class DeepNullReforgedCompatTest {
    @Test
    void legacyFluidCapacityDefaultsMigrateToDoubledValues() {
        assertArrayEquals(
                new int[]{16_000, 32_000, 64_000, 128_000, 256_000, 512_000, Integer.MAX_VALUE},
                DeepNullConfig.normalizeFluidCapacityByTier(List.of(8_000, 16_000, 32_000, 64_000, 128_000, 256_000, Integer.MAX_VALUE))
        );
        assertArrayEquals(
                new int[]{12_000, 24_000, 48_000, 96_000, 192_000, 384_000, Integer.MAX_VALUE},
                DeepNullConfig.normalizeFluidCapacityByTier(List.of(12_000, 24_000, 48_000, 96_000, 192_000, 384_000, Integer.MAX_VALUE))
        );
    }

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
