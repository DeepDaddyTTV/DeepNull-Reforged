package dev.deepdaddyttv.deepnullreforged.nullseed;

public enum NullSeedKind {
    ITEM,
    FLUID,
    CHEMICAL,
    DUMP_RULE,
    ENTITY;

    public static NullSeedKind byId(int id) {
        NullSeedKind[] values = values();
        return id >= 0 && id < values.length ? values[id] : ITEM;
    }
}
