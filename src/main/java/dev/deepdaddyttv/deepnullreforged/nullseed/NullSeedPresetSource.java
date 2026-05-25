package dev.deepdaddyttv.deepnullreforged.nullseed;

public enum NullSeedPresetSource {
    BUILT_IN("Built-in"),
    MOD("Mod"),
    USER("User");

    private final String label;

    NullSeedPresetSource(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static NullSeedPresetSource byId(int id) {
        NullSeedPresetSource[] values = values();
        return id >= 0 && id < values.length ? values[id] : BUILT_IN;
    }
}
