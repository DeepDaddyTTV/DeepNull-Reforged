package dev.deepdaddyttv.deepnullreforged.inventory;

public enum StoneworksMaterial {
    DIRT,
    GRAVEL,
    SAND,
    DUST,
    CLAY,
    GLASS;

    private static final StoneworksMaterial[] ROUND_ROBIN_ORDER = {
            GLASS,
            DUST,
            CLAY,
            SAND,
            GRAVEL,
            DIRT
    };

    public static StoneworksMaterial byId(int id) {
        StoneworksMaterial[] values = values();
        if (id < 0 || id >= values.length) {
            return DIRT;
        }
        return values[id];
    }

    public static StoneworksMaterial[] roundRobinOrder() {
        return ROUND_ROBIN_ORDER;
    }
}
