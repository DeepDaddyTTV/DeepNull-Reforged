package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EnderUpgradeItem extends DeepNullUpgradeItem {
    private static final String LINK_DIMENSION_TAG = "LinkDimension";
    private static final String LINK_X_TAG = "LinkX";
    private static final String LINK_Y_TAG = "LinkY";
    private static final String LINK_Z_TAG = "LinkZ";
    private static final String LINK_FLUID_ONLY_TAG = "LinkFluidOnly";
    private static final String LINK_TIER_TAG = "LinkTier";

    public EnderUpgradeItem(Properties properties) {
        super(DeepNullUpgradeType.ENDER, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof DeepNullDockBlockEntity dock) || !dock.hasStoredDeepNull()) {
            return InteractionResult.PASS;
        }
        if (!(dock.getStoredDeepNull().getItem() instanceof DeepNullItem storedNullItem)) {
            return InteractionResult.PASS;
        }

        if (!context.getLevel().isClientSide()) {
            setLink(
                    context.getItemInHand(),
                    context.getLevel().dimension(),
                    dock.getBlockPos(),
                    storedNullItem instanceof DampNullItem,
                    storedNullItem.tier()
            );
            context.getPlayer().sendSystemMessage(
                    Component.translatable(
                            "upgrade.ender_upgrade.linked_message",
                            linkedTargetName(storedNullItem instanceof DampNullItem, storedNullItem.tier()),
                            dock.getBlockPos().getX(),
                            dock.getBlockPos().getY(),
                            dock.getBlockPos().getZ()
                    ).withStyle(ChatFormatting.AQUA)
            );
        }
        return context.getLevel().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);
        LinkData link = getLink(stack);
        if (link == null) {
            builder.accept(Component.translatable("upgrade.ender_upgrade.unlinked").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        builder.accept(Component.translatable(
                "upgrade.ender_upgrade.linked",
                linkedTargetName(link.fluidOnly(), link.tier()),
                link.pos().getX(),
                link.pos().getY(),
                link.pos().getZ()
        ).withStyle(ChatFormatting.DARK_AQUA));
    }

    @Override
    protected MutableComponent upgradeKindLabel(ItemStack stack) {
        LinkData link = getLink(stack);
        if (link != null) {
            return link.fluidOnly()
                    ? Component.translatable("upgrade.kind.dampnull")
                    : Component.translatable("upgrade.kind.deepnull");
        }
        return super.upgradeKindLabel(stack);
    }

    public static boolean isLinked(ItemStack stack) {
        return getLink(stack) != null;
    }

    public static boolean matchesNull(ItemStack stack, DeepNullTier tier, boolean fluidOnly) {
        LinkData link = getLink(stack);
        return link != null && link.tier() == tier && link.fluidOnly() == fluidOnly;
    }

    public static @Nullable LinkData getLink(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        Identifier dimensionId = Identifier.tryParse(tag.getStringOr(LINK_DIMENSION_TAG, ""));
        if (dimensionId == null || !tag.contains(LINK_X_TAG) || !tag.contains(LINK_Y_TAG) || !tag.contains(LINK_Z_TAG)) {
            return null;
        }
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
        return new LinkData(
                dimension,
                new BlockPos(tag.getIntOr(LINK_X_TAG, 0), tag.getIntOr(LINK_Y_TAG, 0), tag.getIntOr(LINK_Z_TAG, 0)),
                tag.getBooleanOr(LINK_FLUID_ONLY_TAG, false),
                DeepNullTier.byId(tag.getIntOr(LINK_TIER_TAG, 0))
        );
    }

    public static void setLink(ItemStack stack, ResourceKey<Level> dimension, BlockPos pos, boolean fluidOnly, DeepNullTier tier) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(LINK_DIMENSION_TAG, dimension.identifier().toString());
        tag.putInt(LINK_X_TAG, pos.getX());
        tag.putInt(LINK_Y_TAG, pos.getY());
        tag.putInt(LINK_Z_TAG, pos.getZ());
        tag.putBoolean(LINK_FLUID_ONLY_TAG, fluidOnly);
        tag.putInt(LINK_TIER_TAG, tier.ordinalId());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void clearLink(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove(LINK_DIMENSION_TAG);
        tag.remove(LINK_X_TAG);
        tag.remove(LINK_Y_TAG);
        tag.remove(LINK_Z_TAG);
        tag.remove(LINK_FLUID_ONLY_TAG);
        tag.remove(LINK_TIER_TAG);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static Component linkedTargetName(boolean fluidOnly, DeepNullTier tier) {
        return fluidOnly
                ? Component.translatable(tier.dampNullTranslationKey())
                : Component.translatable(tier.displayTranslationKey());
    }

    public record LinkData(ResourceKey<Level> dimension, BlockPos pos, boolean fluidOnly, DeepNullTier tier) {
    }
}
