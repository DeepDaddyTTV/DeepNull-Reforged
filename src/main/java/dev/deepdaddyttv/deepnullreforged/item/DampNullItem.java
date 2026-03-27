package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class DampNullItem extends DeepNullItem {
    public DampNullItem(DeepNullTier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        DeepNullInventory inventory = new DeepNullInventory(tier(), stack, context.registries(), null);
        builder.accept(Component.translatable("dn.number_of_tanks.desc")
                .append(Component.literal(": " + tier().dampNullTankCount()).withStyle(ChatFormatting.GRAY)));
        String capacity = tier().creative() ? Component.translatable("dn.infinite.desc").getString() : Integer.toString(tier().fluidCapacity() / 1000);
        builder.accept(Component.literal(capacity + " ")
                .append(Component.translatable("dn.buckets_per_tank.desc"))
                .withStyle(ChatFormatting.GRAY));
        if (inventory.hasAnyFluid()) {
            builder.accept(Component.translatable("dn.fluid.desc")
                    .append(": ")
                    .append(inventory.getSelectedFluid().getHoverName())
                    .withStyle(ChatFormatting.GRAY));
        } else if (inventory.hasAnyChemical()) {
            StoredChemical selectedChemical = inventory.getSelectedChemical();
            if (!selectedChemical.isEmpty()) {
                builder.accept(Component.translatable("dn.chemical.desc")
                        .append(": ")
                        .append(selectedChemical.getHoverName())
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        if (inventory.hasStoneGeneratorUpgrade()) {
            builder.accept(Component.translatable("item.deepnullreforged.stone_generator_upgrade")
                    .append(": ")
                    .append(inventory.getStoneGeneratorVariant().displayName())
                    .withStyle(ChatFormatting.GRAY));
        } else if (inventory.hasObsidianGeneratorUpgrade()) {
            builder.accept(Component.translatable("item.deepnullreforged.obsidian_generator_upgrade")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (inventory.hasSpongeUpgrade()) {
            builder.accept(Component.translatable("item.deepnullreforged.sponge_upgrade")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (inventory.hasGasUpgrade()) {
            builder.accept(Component.translatable("item.deepnullreforged.gas_upgrade")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
