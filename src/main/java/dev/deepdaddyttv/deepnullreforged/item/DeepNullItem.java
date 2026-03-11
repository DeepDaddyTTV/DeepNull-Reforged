package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenuOpener;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class DeepNullItem extends Item {
    private final DeepNullTier tier;

    public DeepNullItem(DeepNullTier tier, Properties properties) {
        super(properties.stacksTo(1).rarity(tier.rarity()));
        this.tier = tier;
    }

    public DeepNullTier tier() {
        return tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                DeepNullMenuOpener.openHeldItem(serverPlayer, player.getInventory(), getInventorySlot(player, hand));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (!context.getLevel().isClientSide && player instanceof ServerPlayer serverPlayer) {
                DeepNullMenuOpener.openHeldItem(serverPlayer, player.getInventory(), getInventorySlot(player, context.getHand()));
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        HolderLookup.Provider registries = context.getLevel().registryAccess();
        DeepNullInventory inventory = new DeepNullInventory(tier, context.getItemInHand(), registries, null);
        int selectedSlot = inventory.getSelectedSlot();
        ItemStack selectedStack = inventory.getSelectedStack();
        if (selectedSlot < 0 || selectedStack.isEmpty()) {
            return InteractionResult.PASS;
        }

        int availableForUse = player.getAbilities().instabuild
                ? selectedStack.getMaxStackSize()
                : Math.min(inventory.getExtractableAmount(selectedSlot), inventory.getPlaceableAmount(selectedSlot));
        if (availableForUse <= 0) {
            return InteractionResult.PASS;
        }

        ItemStack workingCopy = selectedStack.copyWithCount(1);
        BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
        UseOnContext selectedContext = new UseOnContext(context.getLevel(), player, context.getHand(), workingCopy, hitResult);
        int before = workingCopy.getCount();
        InteractionResult result = workingCopy.useOn(selectedContext);
        if (context.getLevel().isClientSide || player.getAbilities().instabuild || !result.consumesAction()) {
            return result;
        }
        int used = before - workingCopy.getCount();
        if (used > 0) {
            inventory.extractItem(selectedSlot, used, false);
        } else if (!ItemStack.isSameItemSameComponents(selectedStack, workingCopy)) {
            inventory.extractItem(selectedSlot, 1, false);
            if (!workingCopy.isEmpty()) {
                player.getInventory().placeItemBackInInventory(workingCopy);
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("dn.number_of_slots.desc")
                .append(Component.literal(": " + tier.slotCount()).withStyle(ChatFormatting.GRAY)));
        String capacity = tier.creative() ? Component.translatable("dn.infinite.desc").getString() : Integer.toString(tier.perSlotCapacity());
        tooltipComponents.add(Component.literal(capacity + " ").append(Component.translatable("dn.items_per_slot.desc")).withStyle(ChatFormatting.GRAY));
    }

    public static int getInventorySlot(Player player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
    }
}
