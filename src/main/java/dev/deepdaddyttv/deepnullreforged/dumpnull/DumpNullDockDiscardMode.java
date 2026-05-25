package dev.deepdaddyttv.deepnullreforged.dumpnull;

public enum DumpNullDockDiscardMode {
    VOID,
    EXPORT_DISCARD_LANE,
    GENERATE_FE;

    public static DumpNullDockDiscardMode byId(int id) {
        DumpNullDockDiscardMode[] values = values();
        return id >= 0 && id < values.length ? values[id] : VOID;
    }

    public static DumpNullDockDiscardMode byName(String name) {
        if (name != null) {
            for (DumpNullDockDiscardMode mode : values()) {
                if (mode.name().equalsIgnoreCase(name)) {
                    return mode;
                }
            }
        }
        return VOID;
    }

    public DumpNullDockDiscardMode next() {
        DumpNullDockDiscardMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
