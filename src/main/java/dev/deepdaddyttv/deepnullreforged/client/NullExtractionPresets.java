package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.inventory.ItemExtractionMode;

import java.util.List;

public final class NullExtractionPresets {
    private static final List<ItemExtractionMode> STOPS = List.of(
            ItemExtractionMode.KEEP_NONE,
            ItemExtractionMode.KEEP_1,
            ItemExtractionMode.KEEP_16,
            ItemExtractionMode.KEEP_64,
            ItemExtractionMode.KEEP_ALL
    );

    private NullExtractionPresets() {
    }

    public static int stopCount() {
        return STOPS.size();
    }

    public static ItemExtractionMode modeAt(int index) {
        return STOPS.get(Math.max(0, Math.min(STOPS.size() - 1, index)));
    }

    public static int indexOf(ItemExtractionMode mode, int customAmount) {
        if (mode == ItemExtractionMode.CUSTOM) {
            if (customAmount <= 0) {
                return 0;
            }
            if (customAmount <= 1) {
                return 1;
            }
            if (customAmount <= 16) {
                return 2;
            }
            if (customAmount <= 64) {
                return 3;
            }
            return 4;
        }
        int index = STOPS.indexOf(mode);
        return index >= 0 ? index : 0;
    }

    public static int amountFor(ItemExtractionMode mode, int capacity) {
        return switch (mode) {
            case KEEP_NONE -> 0;
            case KEEP_1 -> 1;
            case KEEP_16 -> 16;
            case KEEP_64 -> 64;
            case KEEP_ALL -> Math.max(0, capacity);
            case CUSTOM -> throw new IllegalArgumentException("CUSTOM requires an explicit amount");
        };
    }

    public static ItemExtractionMode modeForTypedAmount(int amount) {
        return switch (Math.max(0, amount)) {
            case 0 -> ItemExtractionMode.KEEP_NONE;
            case 1 -> ItemExtractionMode.KEEP_1;
            case 16 -> ItemExtractionMode.KEEP_16;
            case 64 -> ItemExtractionMode.KEEP_64;
            default -> ItemExtractionMode.CUSTOM;
        };
    }
}
