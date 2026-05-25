package dev.deepdaddyttv.deepnullreforged.hubnull;

public enum HubNullDampResourceKind {
    FLUID,
    CHEMICAL;

    public static HubNullDampResourceKind byId(int id) {
        HubNullDampResourceKind[] values = values();
        if (id < 0 || id >= values.length) {
            return FLUID;
        }
        return values[id];
    }
}
