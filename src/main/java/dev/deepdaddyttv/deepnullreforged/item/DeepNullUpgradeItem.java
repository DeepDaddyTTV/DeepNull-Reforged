package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class DeepNullUpgradeItem extends Item {
    private final DeepNullUpgradeType type;

    public DeepNullUpgradeItem(DeepNullUpgradeType type, Properties properties) {
        super(properties.stacksTo(1));
        this.type = type;
    }

    public DeepNullUpgradeType type() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("upgrade." + type.itemId() + ".desc").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("upgrade." + type.itemId() + ".tiers", supportedTierLabel()).withStyle(ChatFormatting.DARK_GRAY));
    }

    private Component supportedTierLabel() {
        return switch (type) {
            case FILTER -> Component.translatable("upgrade.tiers.iron_plus");
            case FLUID -> Component.translatable("upgrade.tiers.every_tier");
            case ENERGY -> Component.translatable("upgrade.tiers.diamond_plus");
            case DEEP_ENERGY -> Component.translatable("upgrade.tiers.emerald_only");
            case AUTO_FEEDING, AUTO_SMELTING, BASIC_COMPRESSION, ADVANCED_COMPRESSION -> Component.translatable("upgrade.tiers.every_tier");
        };
    }
}
