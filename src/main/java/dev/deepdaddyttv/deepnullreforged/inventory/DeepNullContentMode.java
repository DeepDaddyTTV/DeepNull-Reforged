package dev.deepdaddyttv.deepnullreforged.inventory;

public enum DeepNullContentMode {
    ITEMS,
    FLUIDS;

    public static DeepNullContentMode byId(int id) {
        DeepNullContentMode[] values = values();
        if (id < 0 || id >= values.length) {
            return ITEMS;
        }
        return values[id];
    }
}
