package dev.deepdaddyttv.deepnullreforged.dripnull;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DripNullDataPayloadTest {
    @Test
    void profileCountDefaultsAndClampByTier() {
        assertEquals(1, DripNullData.empty(DeepNullTier.REDSTONE).profiles().size());
        assertEquals(4, DripNullData.empty(DeepNullTier.GOLD).profiles().size());
        assertEquals(12, DripNullData.empty(DeepNullTier.CREATIVE).profiles().size());

        DripNullData oversized = new DripNullData(99, 99, List.of(
                DripProfile.defaultProfile(0),
                DripProfile.defaultProfile(1),
                DripProfile.defaultProfile(2),
                DripProfile.defaultProfile(3)
        ), Map.of(), List.of(), DripUpgradeData.EMPTY, 0L, 0L, 1).withTierProfileCount(DeepNullTier.REDSTONE);

        assertEquals(1, oversized.profiles().size());
        assertEquals(0, oversized.selectedProfile());
        assertEquals(-1, oversized.equippedProfile());
    }

    @Test
    void dripNullDataRoundTripsPayloadProfilesAndUpgradeState() {
        DripProfile profile = DripProfile.defaultProfile(0).withSlots(List.of(
                new DripSlotAssignment(DripSlotRef.inventory(0), "ref1"),
                new DripSlotAssignment(DripSlotRef.armor(0), "ref2")
        ));
        DripNullData data = new DripNullData(
                0,
                -1,
                List.of(profile, DripProfile.defaultProfile(1)),
                Map.of(),
                List.of(),
                DripUpgradeData.EMPTY.withInstalled(DripNullUpgradeType.MEND),
                42L,
                0L,
                3
        );

        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        DripNullData.write(buffer, data);
        DripNullData loaded = DripNullData.read(buffer);

        assertEquals(2, loaded.profiles().size());
        assertTrue(loaded.upgrades().has(DripNullUpgradeType.MEND));
        assertEquals(Set.of("ref1", "ref2"), loaded.assignedRefs());
    }
}
