package dev.deepdaddyttv.deepnullreforged.inventory;

public enum TransferDirectionMode {
    OMNIDIRECTIONAL("dn.transfer_direction_omnidirectional.desc"),
    INSERT("dn.transfer_direction_insert.desc"),
    EXTRACT("dn.transfer_direction_extract.desc");

    private final String translationKey;

    TransferDirectionMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }

    public boolean allowsInsert() {
        return this != EXTRACT;
    }

    public boolean allowsExtract() {
        return this != INSERT;
    }

    public TransferDirectionMode cycle() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static TransferDirectionMode byId(int id) {
        TransferDirectionMode[] values = values();
        if (id < 0 || id >= values.length) {
            return OMNIDIRECTIONAL;
        }
        return values[id];
    }
}
