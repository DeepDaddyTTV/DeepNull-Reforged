package dev.deepdaddyttv.deepnullreforged.inventory;

import net.minecraft.item.Rarity;

public enum DeepNullTier {
    REDSTONE(0, 1, 128, 16000, 9, Rarity.COMMON),
    LAPIS(1, 2, 512, 32000, 9, Rarity.UNCOMMON),
    IRON(2, 3, 1152, 64000, 9, Rarity.UNCOMMON),
    GOLD(3, 4, 2048, 128000, 18, Rarity.RARE),
    DIAMOND(4, 5, 3200, 256000, 18, Rarity.RARE),
    EMERALD(5, 6, Integer.MAX_VALUE, 512000, 18, Rarity.EPIC),
    CREATIVE(6, 6, Integer.MAX_VALUE, Integer.MAX_VALUE, 18, Rarity.EPIC);

    private final int id;
    private final int rows;
    private final int itemCapacity;
    private final int fluidCapacity;
    private final int tanks;
    private final Rarity rarity;

    DeepNullTier(int id, int rows, int itemCapacity, int fluidCapacity, int tanks, Rarity rarity) {
        this.id = id;
        this.rows = rows;
        this.itemCapacity = itemCapacity;
        this.fluidCapacity = fluidCapacity;
        this.tanks = tanks;
        this.rarity = rarity;
    }

    public int id() { return id; }
    public int rows() { return rows; }
    public int slots() { return rows * 9; }
    public int itemCapacity() { return itemCapacity; }
    public int fluidCapacity() { return fluidCapacity; }
    public int tanks() { return tanks; }
    public int stoneRate() {
        int[] rates = {1, 2, 3, 4, 5, 10, 10};
        return rates[id];
    }
    public int spongeLimit() {
        int[] limits = {8, 10, 12, 16, 32, 32, 32};
        return limits[id];
    }
    public Rarity rarity() { return rarity; }
    public boolean creative() { return this == CREATIVE; }
    public String deepNullId() { return "deep_null_" + id; }
    public String dampNullId() { return "damp_null_" + id; }
    public String panelId() { return "deep_null_panel_" + id; }

    public DeepNullTier next() {
        if (this == REDSTONE) return LAPIS;
        if (this == LAPIS) return IRON;
        if (this == IRON) return GOLD;
        if (this == GOLD) return DIAMOND;
        if (this == DIAMOND) return EMERALD;
        return this;
    }

    public static DeepNullTier byId(int id) {
        for (DeepNullTier tier : values()) {
            if (tier.id == id) return tier;
        }
        return REDSTONE;
    }
}
