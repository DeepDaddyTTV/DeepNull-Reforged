package dev.deepdaddyttv.deepnullreforged.nullseed;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record ItemSetPresetEntry(ResourceLocation itemId, String label, String source) {
    public ItemSetPresetEntry {
        label = label == null ? "" : label;
        source = source == null ? "" : source;
    }

    public static void writeList(RegistryFriendlyByteBuf buffer, List<ItemSetPresetEntry> entries) {
        List<ItemSetPresetEntry> safeEntries = entries == null ? List.of() : entries;
        buffer.writeVarInt(safeEntries.size());
        for (ItemSetPresetEntry entry : safeEntries) {
            buffer.writeResourceLocation(entry.itemId());
            buffer.writeUtf(entry.label());
            buffer.writeUtf(entry.source());
        }
    }

    public static List<ItemSetPresetEntry> readList(RegistryFriendlyByteBuf buffer) {
        int size = Math.max(0, Math.min(buffer.readVarInt(), 2048));
        List<ItemSetPresetEntry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new ItemSetPresetEntry(buffer.readResourceLocation(), buffer.readUtf(), buffer.readUtf()));
        }
        return entries;
    }
}
