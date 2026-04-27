package dev.deepdaddyttv.deepnullreforged.inventory;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepNullFilterModeConnectionsTest {
    @Test
    void matches_common_connection_identifiers() {
        assertTrue(DeepNullFilterMode.matchesConnectionsIdentifier(Identifier.fromNamespaceAndPath("mekanism", "ultimate_universal_cable")));
        assertTrue(DeepNullFilterMode.matchesConnectionsIdentifier(Identifier.fromNamespaceAndPath("pipez", "item_pipe")));
        assertTrue(DeepNullFilterMode.matchesConnectionsIdentifier(Identifier.fromNamespaceAndPath("ae2", "storage_bus")));
        assertTrue(DeepNullFilterMode.matchesConnectionsIdentifier(Identifier.fromNamespaceAndPath("refinedstorage", "external_storage")));
        assertTrue(DeepNullFilterMode.matchesConnectionsIdentifier(Identifier.fromNamespaceAndPath("fluxnetworks", "flux_point")));
    }

    @Test
    void avoids_false_positives_from_plain_substrings() {
        assertFalse(DeepNullFilterMode.matchesConnectionsIdentifier(Identifier.fromNamespaceAndPath("minecraft", "tripwire_hook")));
        assertFalse(DeepNullFilterMode.matchesConnectionsIdentifier(Identifier.fromNamespaceAndPath("minecraft", "cobblestone")));
    }
}
