package dev.deepdaddyttv.deepnullreforged.dumpnull;

public enum DumpNullItemStatus {
    NEUTRAL,
    ACCEPTED,
    DISCARDED;

    public DumpNullItemStatus next() {
        return switch (this) {
            case NEUTRAL -> ACCEPTED;
            case ACCEPTED -> DISCARDED;
            case DISCARDED -> NEUTRAL;
        };
    }

    public DumpNullItemStatus catalogRightClickTarget() {
        return this == DISCARDED ? ACCEPTED : DISCARDED;
    }

    public static DumpNullItemStatus byId(int id) {
        DumpNullItemStatus[] values = values();
        if (id < 0 || id >= values.length) {
            return NEUTRAL;
        }
        return values[id];
    }

    public static DumpNullItemStatus fromAction(DumpNullRuleAction action) {
        return action == DumpNullRuleAction.VOID ? DISCARDED : ACCEPTED;
    }

    public DumpNullRuleAction toRuleAction() {
        return this == DISCARDED ? DumpNullRuleAction.VOID : DumpNullRuleAction.PASS;
    }
}
