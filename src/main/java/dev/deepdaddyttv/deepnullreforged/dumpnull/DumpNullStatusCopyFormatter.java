package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class DumpNullStatusCopyFormatter {
    private DumpNullStatusCopyFormatter() {
    }

    public record Entry(String displayName, ResourceLocation itemId) {
        public Entry {
            displayName = normalize(displayName);
        }
    }

    public static String format(List<Entry> entries) {
        return String.join(System.lineSeparator(), entries.stream()
                .map(entry -> entry.displayName() + " - " + entry.itemId())
                .toList());
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown Item";
        }
        return value.replace('\r', ' ').replace('\n', ' ').trim();
    }
}
