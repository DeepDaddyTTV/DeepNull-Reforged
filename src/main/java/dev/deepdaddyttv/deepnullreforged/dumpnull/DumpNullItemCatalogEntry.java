package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record DumpNullItemCatalogEntry(ResourceLocation itemId, String label, String source) {
    private static final int MAX_ENTRIES = 2048;

    public DumpNullItemCatalogEntry {
        label = label == null ? "" : label;
        source = source == null ? "" : source;
    }

    public static void writeList(RegistryFriendlyByteBuf buffer, List<DumpNullItemCatalogEntry> entries) {
        List<DumpNullItemCatalogEntry> safeEntries = entries == null ? List.of() : entries;
        buffer.writeVarInt(Math.min(safeEntries.size(), MAX_ENTRIES));
        for (int i = 0; i < safeEntries.size() && i < MAX_ENTRIES; i++) {
            DumpNullItemCatalogEntry entry = safeEntries.get(i);
            buffer.writeResourceLocation(entry.itemId());
            buffer.writeUtf(entry.label());
            buffer.writeUtf(entry.source());
        }
    }

    public static List<DumpNullItemCatalogEntry> readList(RegistryFriendlyByteBuf buffer) {
        int size = Math.max(0, Math.min(buffer.readVarInt(), MAX_ENTRIES));
        List<DumpNullItemCatalogEntry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new DumpNullItemCatalogEntry(buffer.readResourceLocation(), buffer.readUtf(), buffer.readUtf()));
        }
        return entries;
    }
}
