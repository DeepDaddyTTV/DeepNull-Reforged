package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

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
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("upgrade." + type.itemId() + ".desc").withStyle(ChatFormatting.GRAY));
        builder.accept(upgradeKindLabel(stack).withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("upgrade." + type.itemId() + ".tiers", supportedTierLabel()).withStyle(ChatFormatting.DARK_GRAY));
    }

    protected MutableComponent upgradeKindLabel(ItemStack stack) {
        return switch (type) {
            case STONE_GENERATOR, OBSIDIAN_GENERATOR, SPONGE, GAS -> Component.translatable("upgrade.kind.dampnull");
            case ENDER -> Component.translatable("upgrade.kind.anynull");
            default -> Component.translatable("upgrade.kind.deepnull");
        };
    }

    protected Component supportedTierLabel() {
        return switch (type) {
            case FILTER -> Component.translatable("upgrade.tiers.iron_plus");
            case FLUID -> Component.translatable("upgrade.tiers.every_tier");
            case ENERGY -> Component.translatable("upgrade.tiers.diamond_plus");
            case DEEP_ENERGY -> Component.translatable("upgrade.tiers.emerald_only");
            case AUTO_FEEDING, AUTO_SMELTING, BASIC_COMPRESSION, ADVANCED_COMPRESSION, STONEWORKS, STONE_GENERATOR, OBSIDIAN_GENERATOR, SPONGE, GAS, ENDER -> Component.translatable("upgrade.tiers.every_tier");
        };
    }
}
