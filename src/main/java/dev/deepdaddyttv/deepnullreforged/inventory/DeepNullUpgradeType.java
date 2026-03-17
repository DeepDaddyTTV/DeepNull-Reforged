package dev.deepdaddyttv.deepnullreforged.inventory;

public enum DeepNullUpgradeType {
    FILTER(0, "filter_upgrade"),
    FLUID(1, "fluid_upgrade"),
    ENERGY(2, "energy_upgrade"),
    DEEP_ENERGY(3, "deep_energy_upgrade"),
    AUTO_FEEDING(4, "auto_feeding_upgrade"),
    AUTO_SMELTING(5, "auto_smelting_upgrade"),
    BASIC_COMPRESSION(6, "basic_compression_upgrade"),
    ADVANCED_COMPRESSION(7, "advanced_compression_upgrade");

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
            case DEEP_ENERGY -> tier.supportsDeepEnergyUpgrade();
            case AUTO_FEEDING -> tier.supportsAutoFeedingUpgrade();
            case AUTO_SMELTING -> tier.supportsAutoSmeltingUpgrade();
            case BASIC_COMPRESSION -> tier.supportsBasicCompressionUpgrade();
            case ADVANCED_COMPRESSION -> tier.supportsAdvancedCompressionUpgrade();
        };
    }

    public static DeepNullUpgradeType bySlot(int slot) {
        for (DeepNullUpgradeType type : values()) {
            if (type.slot == slot) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown DeepNull upgrade slot: " + slot);
    }
}
