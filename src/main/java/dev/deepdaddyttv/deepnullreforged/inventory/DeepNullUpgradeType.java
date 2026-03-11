package dev.deepdaddyttv.deepnullreforged.inventory;

public enum DeepNullUpgradeType {
    FILTER(0, "filter_upgrade"),
    FLUID(1, "fluid_upgrade"),
    ENERGY(2, "energy_upgrade");

    private final int slot;
    private final String itemId;

    DeepNullUpgradeType(int slot, String itemId) {
        this.slot = slot;
        this.itemId = itemId;
    }

    public int slot() {
        return slot;
    }

    public String itemId() {
        return itemId;
    }

    public boolean isSupportedBy(DeepNullTier tier) {
        return switch (this) {
            case FILTER -> tier.supportsFilterUpgrade();
            case FLUID -> tier.supportsFluidUpgrade();
            case ENERGY -> tier.supportsEnergyUpgrade();
        };
    }

    public static DeepNullUpgradeType bySlot(int slot) {
        for (DeepNullUpgradeType type : values()) {
            if (type.slot == slot) {
                return type;
            }
        }
        return FILTER;
    }
}
