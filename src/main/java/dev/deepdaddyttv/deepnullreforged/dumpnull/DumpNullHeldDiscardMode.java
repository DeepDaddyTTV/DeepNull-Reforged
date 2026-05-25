package dev.deepdaddyttv.deepnullreforged.dumpnull;

public enum DumpNullHeldDiscardMode {
    VOID,
    BLOCK_PICKUP,
    GENERATE_FE;

    public static DumpNullHeldDiscardMode byId(int id) {
        DumpNullHeldDiscardMode[] values = values();
        return id >= 0 && id < values.length ? values[id] : BLOCK_PICKUP;
    }

    public static DumpNullHeldDiscardMode byName(String name) {
        if (name != null) {
            for (DumpNullHeldDiscardMode mode : values()) {
                if (mode.name().equalsIgnoreCase(name)) {
                    return mode;
                }
            }
        }
        return BLOCK_PICKUP;
    }

    public DumpNullHeldDiscardMode next() {
        DumpNullHeldDiscardMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
