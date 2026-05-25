package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.dripnull.DripNullData;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripMending;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripSwapEngine;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.menu.DripNullMenuOpener;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

public class DripNullItem extends Item implements DockableNullItem {
    public static final int CHARGE_TICKS = 20;
    private final DeepNullTier tier;

    public DripNullItem(DeepNullTier tier, Properties properties) {
        super(properties.stacksTo(1).rarity(tier.rarity()));
        this.tier = tier;
    }

    @Override
    public DeepNullTier tier() {
        return tier;
    }

    @Override
    public NullKind nullKind(ItemStack stack) {
        return NullKind.DRIP;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        return NullInventorySlotOpener.openFromInventorySlot(stack, other, slot, action, player, access);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                DripNullMenuOpener.openHeldItem(serverPlayer, player.getInventory(), DeepNullItem.getInventorySlot(player, hand));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!(livingEntity instanceof ServerPlayer player)) {
            return;
        }
        DripNullData data = DripNullData.get(stack, tier, level.registryAccess());
        if (data.chargeCanceledUntil() >= level.getGameTime()) {
            DripNullData.set(stack, data.withChargeCanceledUntil(0L), tier, level.registryAccess());
            return;
        }
        if (timeCharged < CHARGE_TICKS) {
            player.displayClientMessage(Component.translatable("item.deepnullreforged.drip_null.charge_cancelled"), true);
            return;
        }
        DripSwapEngine.Result result = DripSwapEngine.run(player, stack, tier, DeepNullItem.getInventorySlot(player, player.getUsedItemHand()));
        player.displayClientMessage(result.message(), true);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return DripNullData.get(stack, tier, null).mendingActiveUntil() > 0L || super.isFoil(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide) {
            DripMending.clearExpiredFoil(stack, tier, level.registryAccess(), level.getGameTime());
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        DripNullData data = DripNullData.get(stack, tier, null);
        tooltipComponents.add(Component.translatable("item.deepnullreforged.drip_null.tooltip").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.deepnullreforged.drip_null.profile_count", data.selectedProfile() + 1, data.profiles().size()).withStyle(ChatFormatting.DARK_GRAY));
        if (!data.profiles().isEmpty()) {
            tooltipComponents.add(Component.translatable("item.deepnullreforged.drip_null.selected", data.selected().name()).withStyle(ChatFormatting.AQUA));
        }
        if (!data.looseItems().isEmpty()) {
            tooltipComponents.add(Component.translatable("item.deepnullreforged.drip_null.loose", data.looseItems().size()).withStyle(ChatFormatting.GOLD));
        }
    }
}
