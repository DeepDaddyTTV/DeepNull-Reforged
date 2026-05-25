package dev.deepdaddyttv.deepnullreforged.dumpnull;

public enum DumpNullSideMode {
    DISABLED(false, false),
    INPUT(true, false),
    OUTPUT(false, true),
    BOTH(true, true);

    private final boolean input;
    private final boolean output;

    DumpNullSideMode(boolean input, boolean output) {
        this.input = input;
        this.output = output;
    }

    public boolean input() {
        return input;
    }

    public boolean output() {
        return output;
    }

    public static DumpNullSideMode byId(int id) {
        DumpNullSideMode[] values = values();
        return id >= 0 && id < values.length ? values[id] : BOTH;
    }

    public static DumpNullSideMode byName(String name) {
        if (name != null) {
            for (DumpNullSideMode mode : values()) {
                if (mode.name().equalsIgnoreCase(name)) {
                    return mode;
                }
            }
        }
        return BOTH;
    }

    public DumpNullSideMode next() {
        DumpNullSideMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
