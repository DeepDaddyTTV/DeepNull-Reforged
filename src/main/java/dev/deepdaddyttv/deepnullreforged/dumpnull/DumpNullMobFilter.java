package dev.deepdaddyttv.deepnullreforged.dumpnull;

public enum DumpNullMobFilter {
    HOSTILE,
    PEACEFUL,
    BOSS,
    SELECTED,
    HAS_DROPS;

    public static DumpNullMobFilter toggleSingle(DumpNullMobFilter current, DumpNullMobFilter clicked) {
        return current == clicked ? null : clicked;
    }

    public boolean matches(DumpNullMobOption option, boolean dropDataKnown, boolean hasDrops) {
        return switch (this) {
            case HOSTILE -> "MONSTER".equals(option.category());
            case PEACEFUL -> !"MONSTER".equals(option.category()) && !"MISC".equals(option.category());
            case BOSS -> DumpNullCatalog.isVanillaBossLike(option.entityId());
            case SELECTED -> option.selected();
            case HAS_DROPS -> !dropDataKnown || hasDrops;
        };
    }
}
