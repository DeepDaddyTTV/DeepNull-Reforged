package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class DumpNullDropSearch {
    private DumpNullDropSearch() {
    }

    public static String normalizeQuery(@Nullable String value) {
        return normalize(value, false);
    }

    public static boolean matches(ResourceLocation itemId, @Nullable String query) {
        return matches(itemId, query, null);
    }

    public static boolean matches(ResourceLocation itemId, @Nullable String query, @Nullable String displayName) {
        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery.isBlank() || itemId == null) {
            return false;
        }
        return normalize(itemId.toString(), false).contains(normalizedQuery)
                || normalize(itemId.getNamespace(), false).contains(normalizedQuery)
                || normalize(itemId.getPath(), false).contains(normalizedQuery)
                || normalize(itemId.getPath(), true).contains(normalizedQuery)
                || (!isBlank(displayName) && normalize(displayName, true).contains(normalizedQuery));
    }

    private static boolean isBlank(@Nullable String value) {
        return value == null || value.isBlank();
    }

    private static String normalize(@Nullable String value, boolean separatorsAsSpaces) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (separatorsAsSpaces) {
            normalized = normalized.replace('_', ' ').replace('-', ' ');
        }
        while (normalized.contains("  ")) {
            normalized = normalized.replace("  ", " ");
        }
        return normalized;
    }
}
