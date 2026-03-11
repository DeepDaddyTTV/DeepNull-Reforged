package dev.deepdaddyttv.deepnullreforged.inventory;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.minecraft.resources.ResourceLocation;
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
        return perSlotCapacity;
    }

    public boolean creative() {
        return this == CREATIVE;
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

    public ResourceLocation guiTexture() {
        int textureIndex = rows() - 1 + (creative() ? 1 : 0);
        return DeepNullReforged.id("textures/gui/deepnullscreen" + textureIndex + ".png");
    }

    public String displayTranslationKey() {
        return "item." + DeepNullReforged.MODID + "." + deepNullId();
    }

    public String panelTranslationKey() {
        return "item." + DeepNullReforged.MODID + "." + panelId();
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
