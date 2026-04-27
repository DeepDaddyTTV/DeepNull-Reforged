package dev.deepdaddyttv.deepnullreforged.inventory;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepNullFilterModeConnectionsTest {
    @Test
    void matches_common_connection_identifiers() {
        assertTrue(DeepNullFilterMode.matchesConnectionsIdentifier(ResourceLocation.fromNamespaceAndPath("mekanism", "ultimate_universal_cable")));
        assertTrue(DeepNullFilterMode.matchesConnectionsIdentifier(ResourceLocation.fromNamespaceAndPath("pipez", "item_pipe")));
        assertTrue(DeepNullFilterMode.matchesConnectionsIdentifier(ResourceLocation.fromNamespaceAndPath("ae2", "storage_bus")));
        assertTrue(DeepNullFilterMode.matchesConnectionsIdentifier(ResourceLocation.fromNamespaceAndPath("refinedstorage", "external_storage")));
        assertTrue(DeepNullFilterMode.matchesConnectionsIdentifier(ResourceLocation.fromNamespaceAndPath("fluxnetworks", "flux_point")));
    }

    @Test
    void avoids_false_positives_from_plain_substrings() {
        assertFalse(DeepNullFilterMode.matchesConnectionsIdentifier(ResourceLocation.fromNamespaceAndPath("minecraft", "tripwire_hook")));
        assertFalse(DeepNullFilterMode.matchesConnectionsIdentifier(ResourceLocation.fromNamespaceAndPath("minecraft", "cobblestone")));
    }
}
