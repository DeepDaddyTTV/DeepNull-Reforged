package dev.deepdaddyttv.deepnullreforged.inventory;

public enum TransferOutputMode {
    ALL("dn.transfer_all.desc"),
    MATCHING("dn.transfer_matching.desc"),
    LOCKED("dn.transfer_locked.desc");

    private final String translationKey;

    TransferOutputMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }

    public boolean isLocked() {
        return this == LOCKED;
    }

    public boolean matchingOnly() {
        return this == MATCHING;
    }

    public TransferOutputMode cycle() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static TransferOutputMode byId(int id) {
        TransferOutputMode[] values = values();
        if (id < 0 || id >= values.length) {
            return ALL;
        }
        return values[id];
    }
}
