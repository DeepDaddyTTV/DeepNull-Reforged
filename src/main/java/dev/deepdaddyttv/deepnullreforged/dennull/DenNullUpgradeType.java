package dev.deepdaddyttv.deepnullreforged.dennull;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.minecraft.resources.ResourceLocation;

public enum DenNullUpgradeType {
    BREEDING(0, "den_breeding_upgrade"),
    CLONE(1, "den_clone_upgrade"),
    DYE(2, "den_dye_upgrade"),
    MILK(3, "den_milk_upgrade"),
    SHEAR(4, "den_shear_upgrade"),
    BABY(5, "den_baby_upgrade"),
    TAG(6, "den_tag_upgrade"),
    CAPTURE(7, "den_capture_upgrade"),
    SPAWNER(8, "den_spawner_upgrade"),
    FARM(9, "den_farm_upgrade");

    private final int slot;
    private final String itemId;

    DenNullUpgradeType(int slot, String itemId) {
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

    public static DenNullUpgradeType bySlot(int slot) {
        for (DenNullUpgradeType type : values()) {
            if (type.slot == slot) {
                return type;
            }
        }
        return BREEDING;
    }

    public static DenNullUpgradeType byItemId(String itemId) {
        for (DenNullUpgradeType type : values()) {
            if (type.itemId.equals(itemId)) {
                return type;
            }
        }
        return null;
    }
}
