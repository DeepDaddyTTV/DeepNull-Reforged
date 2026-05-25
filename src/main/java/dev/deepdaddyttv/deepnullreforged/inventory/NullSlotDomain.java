package dev.deepdaddyttv.deepnullreforged.inventory;

public enum NullSlotDomain {
    ITEM_STORAGE,
    FLUID_STORAGE;

    public static NullSlotDomain byId(int id) {
        NullSlotDomain[] values = values();
        if (id < 0 || id >= values.length) {
            return ITEM_STORAGE;
        }
        return values[id];
    }
}
