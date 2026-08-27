package dev.deepdaddyttv.deepnullreforged.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class DeepNullUpgradeItem extends Item {
    private final String upgradeId;

    public DeepNullUpgradeItem(String upgradeId, Properties properties) {
        super(properties);
        this.upgradeId = upgradeId;
    }

    public String getUpgradeId() {
        return upgradeId;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("upgrade." + upgradeId + ".desc"));
    }
}
