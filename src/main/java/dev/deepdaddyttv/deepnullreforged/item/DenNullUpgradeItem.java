package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class DenNullUpgradeItem extends Item {
    private final DenNullUpgradeType type;

    public DenNullUpgradeItem(DenNullUpgradeType type, Properties properties) {
        super(properties.stacksTo(16));
        this.type = type;
    }

    public DenNullUpgradeType type() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("upgrade." + type.itemId() + ".desc").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("upgrade.kind.dennull").withStyle(ChatFormatting.DARK_GRAY));
    }
}
