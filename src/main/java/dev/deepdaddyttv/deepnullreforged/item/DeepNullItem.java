package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenuOpener;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.List;
import java.util.function.Function;

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

        DeepNullInventory inventory = new DeepNullInventory(tier, stack, level.registryAccess(), null);
        InteractionResultHolder<ItemStack> bucketResult = tryUseStoredBucket(level, player, hand, stack, inventory);
        if (bucketResult != null) {
            return bucketResult;
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
        InteractionResult bucketResult = tryUseStoredBucket(context, inventory);
        if (bucketResult != null) {
            return bucketResult;
        }

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
        DeepNullInventory inventory = new DeepNullInventory(tier, stack, context.registries(), null);
        tooltipComponents.add(Component.translatable("dn.number_of_slots.desc")
                .append(Component.literal(": " + tier.slotCount()).withStyle(ChatFormatting.GRAY)));
        String capacity = tier.creative() ? Component.translatable("dn.infinite.desc").getString() : Integer.toString(tier.perSlotCapacity());
        tooltipComponents.add(Component.literal(capacity + " ").append(Component.translatable("dn.items_per_slot.desc")).withStyle(ChatFormatting.GRAY));
        for (DeepNullUpgradeType type : DeepNullUpgradeType.values()) {
            if (inventory.hasUpgrade(type)) {
                tooltipComponents.add(Component.translatable("upgrade." + type.itemId() + ".installed").withStyle(ChatFormatting.AQUA));
            }
        }
        if (inventory.supportsFiltering()) {
            tooltipComponents.add(Component.translatable("dn.filter_mode_label.desc")
                    .append(": ")
                    .append(inventory.getFilterMode().displayName())
                    .withStyle(ChatFormatting.GRAY));
        }
        if (inventory.hasEnergyUpgrade()) {
            tooltipComponents.add(Component.translatable("dn.energy.desc")
                    .append(": ")
                    .append(Component.literal(inventory.getEnergyStored() + " / " + inventory.getEnergyCapacity() + " FE"))
                    .withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("dn.charging.desc")
                    .append(": ")
                    .append(Component.translatable(inventory.isChargingEnabled() ? "dn.enabled.desc" : "dn.disabled.desc"))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide || !(entity instanceof Player player) || level.getGameTime() % 5L != Math.floorMod(slotId, 5)) {
            return;
        }

        DeepNullInventory inventory = new DeepNullInventory(tier, stack, level.registryAccess(), null);
        if (!inventory.isChargingEnabled() || inventory.getEnergyStored() <= 0) {
            return;
        }

        int remainingTransfer = inventory.getEnergyTransferRate();
        remainingTransfer = chargeInventorySection(player.getInventory().items, stack, inventory, remainingTransfer);
        remainingTransfer = chargeInventorySection(player.getInventory().offhand, stack, inventory, remainingTransfer);
        chargeInventorySection(player.getInventory().armor, stack, inventory, remainingTransfer);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("DeepNull", Tag.TAG_COMPOUND)) {
            return false;
        }
        return tag.getCompound("DeepNull").getBoolean("Charging");
    }

    public static int getInventorySlot(Player player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
    }

    private InteractionResult tryUseStoredBucket(UseOnContext context, DeepNullInventory inventory) {
        Player player = context.getPlayer();
        if (player == null) {
            return null;
        }

        ItemStack selectedStack = inventory.getSelectedStack();
        if (!canUseStoredBucket(inventory, selectedStack)) {
            return null;
        }

        InteractionResultHolder<ItemStack> result = proxyStoredItemUse(
                context.getLevel(),
                player,
                context.getHand(),
                context.getItemInHand(),
                inventory,
                proxyStack -> {
                    BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
                    InteractionResult useOnResult = proxyStack.useOn(new UseOnContext(context.getLevel(), player, context.getHand(), proxyStack, hitResult));
                    if (useOnResult != InteractionResult.PASS) {
                        return new InteractionResultHolder<>(useOnResult, player.getItemInHand(context.getHand()).copy());
                    }
                    return proxyStack.use(context.getLevel(), player, context.getHand());
                }
        );
        return result == null ? null : result.getResult();
    }

    private InteractionResultHolder<ItemStack> tryUseStoredBucket(
            Level level,
            Player player,
            InteractionHand hand,
            ItemStack deepNullStack,
            DeepNullInventory inventory
    ) {
        if (!canUseStoredBucket(inventory, inventory.getSelectedStack())) {
            return null;
        }
        return proxyStoredItemUse(level, player, hand, deepNullStack, inventory, proxyStack -> proxyStack.use(level, player, hand));
    }

    private static boolean canUseStoredBucket(DeepNullInventory inventory, ItemStack selectedStack) {
        if (!inventory.hasFluidUpgrade() || selectedStack.isEmpty()) {
            return false;
        }
        if (selectedStack.getItem() instanceof BucketItem || selectedStack.is(Items.BUCKET)) {
            return true;
        }
        return FluidUtil.getFluidHandler(selectedStack.copyWithCount(1)).isPresent();
    }

    private static InteractionResultHolder<ItemStack> proxyStoredItemUse(
            Level level,
            Player player,
            InteractionHand hand,
            ItemStack deepNullStack,
            DeepNullInventory inventory,
            Function<ItemStack, InteractionResultHolder<ItemStack>> action
    ) {
        int selectedSlot = inventory.getSelectedSlot();
        ItemStack selectedStack = inventory.getSelectedStack();
        if (selectedSlot < 0 || selectedStack.isEmpty()) {
            return null;
        }

        ItemStack originalHandStack = player.getItemInHand(hand);
        ItemStack proxyStack = selectedStack.copyWithCount(1);
        player.setItemInHand(hand, proxyStack);

        InteractionResultHolder<ItemStack> result;
        ItemStack resultStack;
        try {
            result = action.apply(proxyStack);
            resultStack = player.getItemInHand(hand).copy();
            if (resultStack.isEmpty() && result != null && !result.getObject().isEmpty()) {
                resultStack = result.getObject().copy();
            }
        } finally {
            player.setItemInHand(hand, originalHandStack);
        }

        if (result == null) {
            return InteractionResultHolder.pass(deepNullStack);
        }

        if (!level.isClientSide && result.getResult() != InteractionResult.PASS) {
            applyStoredUseResult(player, inventory, selectedSlot, selectedStack, resultStack);
        }

        return new InteractionResultHolder<>(result.getResult(), deepNullStack);
    }

    private static void applyStoredUseResult(
            Player player,
            DeepNullInventory inventory,
            int selectedSlot,
            ItemStack originalSelected,
            ItemStack resultStack
    ) {
        ItemStack originalSingle = originalSelected.copyWithCount(1);
        if (ItemStack.isSameItemSameComponents(originalSingle, resultStack)) {
            return;
        }

        if (originalSelected.getCount() <= 1) {
            inventory.setStackInSlot(selectedSlot, resultStack);
            return;
        }

        inventory.extractItem(selectedSlot, 1, false);
        if (!resultStack.isEmpty()) {
            ItemStack remainder = inventory.insertIntoFirstAvailableSlot(resultStack, false);
            if (!remainder.isEmpty()) {
                player.getInventory().placeItemBackInInventory(remainder);
            }
        }
    }

    private static int chargeInventorySection(List<ItemStack> stacks, ItemStack deepNullStack, DeepNullInventory inventory, int remainingTransfer) {
        for (ItemStack candidate : stacks) {
            if (remainingTransfer <= 0 || inventory.getEnergyStored() <= 0) {
                break;
            }
            if (candidate.isEmpty() || candidate == deepNullStack) {
                continue;
            }

            IEnergyStorage energyStorage = candidate.getCapability(Capabilities.EnergyStorage.ITEM);
            if (energyStorage == null || !energyStorage.canReceive()) {
                continue;
            }

            int available = Math.min(remainingTransfer, inventory.getEnergyStored());
            int accepted = energyStorage.receiveEnergy(available, true);
            if (accepted <= 0) {
                continue;
            }

            int extracted = inventory.extractEnergy(Math.min(accepted, available), false);
            if (extracted <= 0) {
                break;
            }

            int received = energyStorage.receiveEnergy(extracted, false);
            if (received < extracted) {
                inventory.receiveEnergy(extracted - received, false);
            }
            remainingTransfer -= received;
        }
        return remainingTransfer;
    }

}
