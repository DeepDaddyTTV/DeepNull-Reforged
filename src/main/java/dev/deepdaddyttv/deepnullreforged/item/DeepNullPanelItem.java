package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DeepNullPanelItem extends Item {
    private final DeepNullTier tier;

    public DeepNullPanelItem(DeepNullTier tier, Properties properties) {
        super(properties.rarity(tier.rarity()));
        this.tier = tier;
    }

    public DeepNullTier tier() {
        return tier;
    }

}
