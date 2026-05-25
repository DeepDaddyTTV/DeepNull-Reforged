package dev.deepdaddyttv.deepnullreforged.inventory;

public enum DeepNullUpgradeType {
    FILTER(0, "filter_upgrade"),
    FLUID(1, "fluid_upgrade"),
    ENERGY(2, "energy_upgrade"),
    DEEP_ENERGY(2, "deep_energy_upgrade"),
    AUTO_FEEDING(4, "auto_feeding_upgrade"),
    AUTO_SMELTING(5, "auto_smelting_upgrade"),
    BASIC_COMPRESSION(6, "basic_compression_upgrade"),
    ADVANCED_COMPRESSION(7, "advanced_compression_upgrade"),
    STONE_GENERATOR(8, "stone_generator_upgrade"),
    OBSIDIAN_GENERATOR(8, "obsidian_generator_upgrade"),
    SPONGE(10, "sponge_upgrade"),
    GAS(11, "gas_upgrade"),
    STONEWORKS(12, "stoneworks_upgrade"),
    ENDER(13, "ender_upgrade"),
    FARM(14, "farm_upgrade"),
    DIVNULL(15, "divnull_upgrade"),
    BALLOON(16, "balloon_upgrade");

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

    public boolean isUpgradeScreenRepresentative() {
        return switch (this) {
            case DEEP_ENERGY, OBSIDIAN_GENERATOR -> false;
            default -> true;
        };
    }

    public boolean usesSharedSlot() {
        return switch (this) {
            case ENERGY, DEEP_ENERGY, STONE_GENERATOR, OBSIDIAN_GENERATOR -> true;
            default -> false;
        };
    }

    public boolean isSupportedBy(DeepNullTier tier, boolean fluidOnly) {
        if (fluidOnly) {
            return switch (this) {
                case STONE_GENERATOR -> tier.supportsStoneGeneratorUpgrade();
                case OBSIDIAN_GENERATOR -> tier.supportsObsidianGeneratorUpgrade();
                case SPONGE -> tier.supportsSpongeUpgrade();
                case GAS -> tier.supportsGasUpgrade();
                case ENDER -> true;
                case BALLOON -> true;
                default -> false;
            };
        }

        return switch (this) {
            case FILTER -> tier.supportsFilterUpgrade();
            case FLUID -> tier.supportsFluidUpgrade();
            case ENERGY -> tier.supportsEnergyUpgrade();
            case DEEP_ENERGY -> tier.supportsDeepEnergyUpgrade();
            case AUTO_FEEDING -> tier.supportsAutoFeedingUpgrade();
            case AUTO_SMELTING -> tier.supportsAutoSmeltingUpgrade();
            case BASIC_COMPRESSION -> tier.supportsBasicCompressionUpgrade();
            case ADVANCED_COMPRESSION -> tier.supportsAdvancedCompressionUpgrade();
            case STONEWORKS -> tier.supportsStoneworksUpgrade();
            case ENDER -> true;
            case FARM -> true;
            case DIVNULL -> true;
            case STONE_GENERATOR -> false;
            case OBSIDIAN_GENERATOR -> false;
            case SPONGE -> false;
            case GAS -> false;
            case BALLOON -> false;
        };
    }

    public static DeepNullUpgradeType bySlot(int slot) {
        for (DeepNullUpgradeType type : values()) {
            if (type.slot == slot) {
                return type;
            }
        }
        if (slot == 3) {
            return DEEP_ENERGY;
        }
        if (slot == 9) {
            return OBSIDIAN_GENERATOR;
        }
        throw new IllegalArgumentException("Unknown DeepNull upgrade slot: " + slot);
    }
}
