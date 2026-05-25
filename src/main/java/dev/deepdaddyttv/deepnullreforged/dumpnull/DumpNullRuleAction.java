package dev.deepdaddyttv.deepnullreforged.dumpnull;

public enum DumpNullRuleAction {
    PASS,
    VOID;

    public static DumpNullRuleAction byId(int id) {
        DumpNullRuleAction[] values = values();
        if (id < 0 || id >= values.length) {
            return PASS;
        }
        return values[id];
    }
}
