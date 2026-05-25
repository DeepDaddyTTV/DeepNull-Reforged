package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullData;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.menu.DumpNullMenuOpener;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Locale;

public class DumpNullItem extends DeepNullItem {
    public DumpNullItem(Properties properties) {
        super(DeepNullTier.REDSTONE, properties);
    }

    @Override
    public NullKind nullKind(ItemStack stack) {
        return NullKind.DUMP;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                DumpNullData updated = DumpNullData.get(stack).toggleActive();
                DumpNullData.set(stack, updated);
                player.displayClientMessage(Component.translatable(updated.active()
                        ? "item.deepnullreforged.dump_null.active_enabled"
                        : "item.deepnullreforged.dump_null.active_disabled"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            DumpNullMenuOpener.openHeldItem(serverPlayer, player.getInventory(), getInventorySlot(player, hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return DumpNullData.get(stack).active() || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        DumpNullData data = DumpNullData.get(stack);
        tooltipComponents.add(Component.translatable("item.deepnullreforged.dump_null.tooltip").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable(data.active()
                ? "item.deepnullreforged.dump_null.active_tooltip"
                : "item.deepnullreforged.dump_null.inactive_tooltip").withStyle(data.active() ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable("item.deepnullreforged.dump_null.rules", data.rules().size()).withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable("item.deepnullreforged.dump_null.mobs", data.selectedMobs().size()).withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable("item.deepnullreforged.dump_null.held_mode",
                Component.translatable("item.deepnullreforged.dump_null.held_mode." + data.heldDiscardMode().name().toLowerCase(Locale.ROOT))).withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable("item.deepnullreforged.dump_null.dock_mode",
                Component.translatable("item.deepnullreforged.dump_null.dock_mode." + data.dockDiscardMode().name().toLowerCase(Locale.ROOT))).withStyle(ChatFormatting.DARK_GRAY));
        if (data.hasUpgrade(dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullUpgradeType.POWER)) {
            tooltipComponents.add(Component.translatable("item.deepnullreforged.dump_null.energy", data.storedEnergy(), data.energyCapacity()).withStyle(ChatFormatting.GOLD));
        }
    }
}
