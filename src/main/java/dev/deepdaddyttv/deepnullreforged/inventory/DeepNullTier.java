package dev.deepdaddyttv.deepnullreforged.inventory;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Rarity;

public enum DeepNullTier {
    REDSTONE(0, 128, Rarity.COMMON),
    LAPIS(1, 512, Rarity.UNCOMMON),
    IRON(2, 1152, Rarity.UNCOMMON),
    GOLD(3, 2048, Rarity.RARE),
    DIAMOND(4, 3200, Rarity.RARE),
    EMERALD(5, Integer.MAX_VALUE, Rarity.EPIC),
    CREATIVE(6, Integer.MAX_VALUE, Rarity.EPIC);

    private final int ordinalId;
    private final int perSlotCapacity;
    private final Rarity rarity;

    DeepNullTier(int ordinalId, int perSlotCapacity, Rarity rarity) {
        this.ordinalId = ordinalId;
        this.perSlotCapacity = perSlotCapacity;
        this.rarity = rarity;
    }

    public int ordinalId() {
        return ordinalId;
    }

    public int slotCount() {
        return rows() * 9;
    }

    public int rows() {
        return this == CREATIVE ? 6 : ordinalId + 1;
    }

    public int perSlotCapacity() {
        return DeepNullConfig.getItemCapacity(this);
    }

    public boolean creative() {
        return this == CREATIVE;
    }

    public boolean supportsFilterUpgrade() {
        return this == IRON || this == GOLD || this == DIAMOND || this == EMERALD || this == CREATIVE;
    }

    public boolean supportsFluidUpgrade() {
        return true;
    }

    public boolean supportsEnergyUpgrade() {
        return this == DIAMOND || this == EMERALD || this == CREATIVE;
    }

    public boolean supportsDeepEnergyUpgrade() {
        return this == EMERALD;
    }

    public boolean supportsAutoFeedingUpgrade() {
        return true;
    }

    public boolean supportsAutoSmeltingUpgrade() {
        return true;
    }

    public boolean supportsBasicCompressionUpgrade() {
        return true;
    }

    public boolean supportsAdvancedCompressionUpgrade() {
        return true;
    }

    public boolean supportsStoneworksUpgrade() {
        return true;
    }

    public boolean supportsStoneGeneratorUpgrade() {
        return true;
    }

    public boolean supportsObsidianGeneratorUpgrade() {
        return true;
    }

    public boolean supportsSpongeUpgrade() {
        return true;
    }

    public boolean supportsGasUpgrade() {
        return true;
    }

    public int fluidCapacity() {
        return DeepNullConfig.getFluidCapacity(this);
    }

    public int energyCapacity() {
        return DeepNullConfig.getEnergyCapacity(this);
    }

    public int deepEnergyCapacity() {
        return DeepNullConfig.getDeepEnergyCapacity(this);
    }

    public int energyTransfer() {
        return DeepNullConfig.getEnergyTransfer(this);
    }

    public int deepEnergyTransfer() {
        return DeepNullConfig.getDeepEnergyTransfer(this);
    }

    public int dampNullTankCount() {
        return DeepNullConfig.getDampNullTankCount(this);
    }

    public int stoneGenerationRate() {
        return DeepNullConfig.getStoneGenerationRate(this);
    }

    public int spongeAbsorbLimit() {
        return DeepNullConfig.getSpongeAbsorbLimit(this);
    }

    public int spongeRangeWidth() {
        return DeepNullConfig.getSpongeRangeWidth(this);
    }

    public int spongeRangeHeight() {
        return DeepNullConfig.getSpongeRangeHeight(this);
    }

    public Rarity rarity() {
        return rarity;
    }

    public String deepNullId() {
        return "deep_null_" + ordinalId;
    }

    public String panelId() {
        return "deep_null_panel_" + ordinalId;
    }

    public String dampNullId() {
        return "damp_null_" + ordinalId;
    }

    public Identifier guiTexture() {
        int textureIndex = rows() - 1 + (creative() ? 1 : 0);
        return DeepNullReforged.id("textures/gui/deepnullscreen" + textureIndex + ".png");
    }

    public String displayTranslationKey() {
        return "item." + DeepNullReforged.MODID + "." + deepNullId();
    }

    public String panelTranslationKey() {
        return "item." + DeepNullReforged.MODID + "." + panelId();
    }

    public String dampNullTranslationKey() {
        return "item." + DeepNullReforged.MODID + "." + dampNullId();
    }

    public DeepNullTier next() {
        return switch (this) {
            case REDSTONE -> LAPIS;
            case LAPIS -> IRON;
            case IRON -> GOLD;
            case GOLD -> DIAMOND;
            case DIAMOND -> EMERALD;
            default -> this;
        };
    }

    public static DeepNullTier byId(int id) {
        for (DeepNullTier tier : values()) {
            if (tier.ordinalId == id) {
                return tier;
            }
        }
        return REDSTONE;
    }
}
