package dev.deepdaddyttv.deepnullreforged.dripnull;

public enum DripNullUpgradeType {
    MEND(0, "drip_mend_upgrade");

    private final int slot;
    private final String itemId;

    DripNullUpgradeType(int slot, String itemId) {
        this.slot = slot;
        this.itemId = itemId;
    }

    public int slot() {
        return slot;
    }

    public String itemId() {
        return itemId;
    }

    public static DripNullUpgradeType byItemId(String itemId) {
        for (DripNullUpgradeType type : values()) {
            if (type.itemId.equals(itemId)) {
                return type;
            }
        }
        return null;
    }
}
