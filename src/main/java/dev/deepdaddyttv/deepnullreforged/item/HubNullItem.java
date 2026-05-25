package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.block.DeepNullDockBlock;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullData;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationRef;
import dev.deepdaddyttv.deepnullreforged.menu.HubNullMenuOpener;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class HubNullItem extends Item {
    public HubNullItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        return NullInventorySlotOpener.openFromInventorySlot(stack, other, slot, action, player, access);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockState(pos).getBlock() instanceof DeepNullDockBlock) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
                HubNullStationRef ref = stationRef(serverLevel, pos);
                HubNullData data = HubNullData.get(context.getItemInHand());
                if (player.isShiftKeyDown() && data.contains(ref)) {
                    HubNullData.set(context.getItemInHand(), data.withoutStation(ref));
                    serverPlayer.displayClientMessage(stationFeedback("item.deepnullreforged.hub_null.unregistered", ChatFormatting.RED, ref.name()), true);
                } else {
                    HubNullData.set(context.getItemInHand(), data.withStation(ref));
                    serverPlayer.displayClientMessage(stationFeedback("item.deepnullreforged.hub_null.registered", ChatFormatting.GREEN, ref.name()), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (player.isShiftKeyDown()) {
            open(level, player, context.getHand());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hitResult.getType() == HitResult.Type.BLOCK && !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        open(level, player, hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        HubNullData data = HubNullData.get(stack);
        tooltipComponents.add(Component.translatable("item.deepnullreforged.hub_null.tooltip").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.deepnullreforged.hub_null.stations", data.stations().size()).withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void open(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            HubNullMenuOpener.openHeldItem(serverPlayer, player.getInventory(), DeepNullItem.getInventorySlot(player, hand));
        }
    }

    private static Component stationFeedback(String statusKey, ChatFormatting statusColor, String stationName) {
        return Component.translatable(statusKey)
                .withStyle(statusColor)
                .append(Component.literal(" "))
                .append(Component.literal(stationName));
    }

    private static HubNullStationRef stationRef(ServerLevel level, BlockPos pos) {
        ResourceLocation dimension = level.dimension().location();
        String name = Component.translatable("block.deepnullreforged.deepnull_dock").getString()
                + " @ " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
        if (level.getBlockEntity(pos) instanceof DeepNullDockBlockEntity dock && dock.hasStoredDeepNull()) {
            name = dock.getStoredDeepNull().getHoverName().getString();
        }
        return new HubNullStationRef(dimension, pos, name);
    }
}
