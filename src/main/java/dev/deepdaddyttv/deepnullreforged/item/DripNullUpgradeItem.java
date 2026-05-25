package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.dripnull.DripNullUpgradeType;
import net.minecraft.world.item.Item;

public class DripNullUpgradeItem extends Item {
    private final DripNullUpgradeType type;

    public DripNullUpgradeItem(DripNullUpgradeType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public DripNullUpgradeType type() {
        return type;
    }
}
