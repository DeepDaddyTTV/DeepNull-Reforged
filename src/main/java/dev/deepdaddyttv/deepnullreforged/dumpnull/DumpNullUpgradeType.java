package dev.deepdaddyttv.deepnullreforged.dumpnull;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.minecraft.resources.ResourceLocation;

public enum DumpNullUpgradeType {
    POWER(0, "dump_power_upgrade");

    private final int slot;
    private final String itemId;

    DumpNullUpgradeType(int slot, String itemId) {
        this.slot = slot;
        this.itemId = itemId;
    }

    public int slot() {
        return slot;
    }

    public String itemId() {
        return itemId;
    }

    public ResourceLocation id() {
        return DeepNullReforged.id(itemId);
    }

    public static DumpNullUpgradeType byId(int id) {
        DumpNullUpgradeType[] values = values();
        return id >= 0 && id < values.length ? values[id] : POWER;
    }

    public static DumpNullUpgradeType byItemId(String itemId) {
        if (itemId != null) {
            for (DumpNullUpgradeType type : values()) {
                if (type.itemId.equals(itemId)) {
                    return type;
                }
            }
        }
        return null;
    }
}
