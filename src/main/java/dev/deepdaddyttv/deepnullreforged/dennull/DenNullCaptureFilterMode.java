package dev.deepdaddyttv.deepnullreforged.dennull;

public enum DenNullCaptureFilterMode {
    OFF,
    WHITELIST,
    BLACKLIST;

    public DenNullCaptureFilterMode cycle() {
        DenNullCaptureFilterMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static DenNullCaptureFilterMode byId(int id) {
        DenNullCaptureFilterMode[] values = values();
        if (id < 0 || id >= values.length) {
            return OFF;
        }
        return values[id];
    }
}
