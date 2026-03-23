package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.Locale;

public class SynchronizerItem extends Item {
    private static final String ROOT_TAG = "Synchronizer";
    private static final String CONFIGURATION_TAG = "Configuration";
    private static final String FLUID_ONLY_TAG = "FluidOnly";
    private static final String SOURCE_TIER_TAG = "SourceTier";

    public SynchronizerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CompoundTag root = getRootTag(stack);
        if (!root.contains(CONFIGURATION_TAG, Tag.TAG_COMPOUND)) {
            tooltipComponents.add(Component.translatable("item.deepnullreforged.synchronizer.empty").withStyle(ChatFormatting.GRAY));
            return;
        }

        boolean fluidOnly = root.getBoolean(FLUID_ONLY_TAG);
        DeepNullTier tier = parseTier(root.getString(SOURCE_TIER_TAG));
        Component kind = fluidOnly
                ? Component.translatable("item.deepnullreforged.dampnull")
                : Component.translatable("item.deepnullreforged.deepnull");
        if (tier == null) {
            tooltipComponents.add(Component.translatable("item.deepnullreforged.synchronizer.stored_generic", kind).withStyle(ChatFormatting.AQUA));
        } else {
            tooltipComponents.add(Component.translatable("item.deepnullreforged.synchronizer.stored_tiered", kind, formatTierName(tier)).withStyle(ChatFormatting.AQUA));
        }
    }

    public static boolean hasConfiguration(ItemStack stack) {
        return getRootTag(stack).contains(CONFIGURATION_TAG, Tag.TAG_COMPOUND);
    }

    public static @org.jetbrains.annotations.Nullable CompoundTag getConfiguration(ItemStack stack) {
        CompoundTag root = getRootTag(stack);
        return root.contains(CONFIGURATION_TAG, Tag.TAG_COMPOUND) ? root.getCompound(CONFIGURATION_TAG).copy() : null;
    }

    public static boolean matchesNullType(ItemStack stack, boolean fluidOnly) {
        CompoundTag root = getRootTag(stack);
        return root.contains(CONFIGURATION_TAG, Tag.TAG_COMPOUND) && root.getBoolean(FLUID_ONLY_TAG) == fluidOnly;
    }

    public static void storeConfiguration(ItemStack stack, CompoundTag configuration, DeepNullTier tier, boolean fluidOnly) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.contains(ROOT_TAG, Tag.TAG_COMPOUND) ? tag.getCompound(ROOT_TAG).copy() : new CompoundTag();
            root.put(CONFIGURATION_TAG, configuration.copy());
            root.putBoolean(FLUID_ONLY_TAG, fluidOnly);
            root.putString(SOURCE_TIER_TAG, tier.name());
            tag.put(ROOT_TAG, root);
        });
    }

    public static void clearConfiguration(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(ROOT_TAG));
    }

    private static CompoundTag getRootTag(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(ROOT_TAG, Tag.TAG_COMPOUND) ? tag.getCompound(ROOT_TAG) : new CompoundTag();
    }

    private static DeepNullTier parseTier(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return DeepNullTier.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static Component formatTierName(DeepNullTier tier) {
        String lower = tier.name().toLowerCase(Locale.ROOT);
        return Component.literal(Character.toUpperCase(lower.charAt(0)) + lower.substring(1));
    }
}
