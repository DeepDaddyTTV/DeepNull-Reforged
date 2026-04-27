package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.capability.DeepNullFluidHandler;
import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.integration.ae2.Ae2TransferCompat;
import dev.deepdaddyttv.deepnullreforged.integration.mekanism.MekanismTransferCompat;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenuOpener;
import dev.deepdaddyttv.deepnullreforged.registry.ModCapabilities;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;

public class DeepNullItem extends Item {
    private static final String DEEPNULL_TAG = "DeepNull";
    private static final String PROXY_USE_SLOT_TAG = "ProxyUseSlot";
    private static final String PROXY_USE_ANIM_TAG = "ProxyUseAnim";
    private static final String PROXY_USE_DURATION_TAG = "ProxyUseDuration";
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
            BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                return InteractionResultHolder.pass(stack);
            }
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                DeepNullMenuOpener.openHeldItem(serverPlayer, player.getInventory(), getInventorySlot(player, hand));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        DeepNullInventory inventory = new DeepNullInventory(tier, stack, level.registryAccess(), null);
        if (inventory.isFluidOnly()) {
            BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                InteractionResult spongeResult = tryUseSpongeAbsorption(level, player, stack, inventory, hitResult.getBlockPos());
                if (spongeResult != null) {
                    return new InteractionResultHolder<>(spongeResult, stack);
                }
                InteractionResult pickupResult = tryPickUpSourceFluid(level, player, stack, inventory, hitResult.getBlockPos(), hitResult.getDirection());
                if (pickupResult != null) {
                    return new InteractionResultHolder<>(pickupResult, stack);
                }
            }
            return InteractionResultHolder.pass(stack);
        }
        InteractionResultHolder<ItemStack> bucketResult = tryUseStoredBucket(level, player, hand, stack, inventory);
        if (bucketResult != null) {
            return bucketResult;
        }
        InteractionResultHolder<ItemStack> consumableResult = tryUseStoredConsumable(level, player, hand, stack, inventory);
        if (consumableResult != null) {
            return consumableResult;
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        int proxySlot = getProxyUseSlot(stack);
        if (proxySlot < 0) {
            return super.finishUsingItem(stack, level, livingEntity);
        }

        clearProxyUseState(stack);
        DeepNullInventory inventory = new DeepNullInventory(tier, stack, level.registryAccess(), null);
        if (proxySlot >= inventory.getSlots()) {
            return stack;
        }

        ItemStack selectedStack = inventory.getStackInSlot(proxySlot);
        if (!supportsStoredConsumeUse(selectedStack, livingEntity)) {
            return stack;
        }

        ItemStack resultStack = selectedStack.copyWithCount(1).finishUsingItem(level, livingEntity);
        if (!level.isClientSide && livingEntity instanceof Player player) {
            applyStoredUseResult(player, inventory, proxySlot, selectedStack, resultStack);
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        clearProxyUseState(stack);
        super.releaseUsing(stack, level, livingEntity, timeCharged);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        UseAnim proxyAnim = getProxyUseAnimation(stack);
        return proxyAnim == null ? super.getUseAnimation(stack) : proxyAnim;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        int proxyDuration = getProxyUseDuration(stack);
        return proxyDuration > 0 ? proxyDuration : super.getUseDuration(stack, entity);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        HolderLookup.Provider registries = context.getLevel().registryAccess();
        DeepNullInventory inventory = new DeepNullInventory(tier, context.getItemInHand(), registries, null);

        if (player.isShiftKeyDown() && !inventory.isTransferLocked()) {
            InteractionResult transferResult = tryShiftTransfer(context, inventory);
            if (transferResult != InteractionResult.PASS) {
                return transferResult;
            }
        }

        if (inventory.isFluidOnly()) {
            InteractionResult fluidResult = tryUseStoredFluid(context, inventory);
            return fluidResult == null ? InteractionResult.PASS : fluidResult;
        }
        InteractionResult bucketResult = tryUseStoredBucket(context, inventory);
        if (bucketResult != null) {
            return bucketResult;
        }
        InteractionResultHolder<ItemStack> consumableResult = tryUseStoredConsumable(
                context.getLevel(),
                player,
                context.getHand(),
                context.getItemInHand(),
                inventory
        );
        if (consumableResult != null) {
            return consumableResult.getResult();
        }
        InteractionResult proxiedUseOnResult = tryUseStoredStatefulItem(context, inventory);
        if (proxiedUseOnResult != null) {
            return proxiedUseOnResult;
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
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        DeepNullInventory inventory = new DeepNullInventory(tier, stack, player.level().registryAccess(), null);
        if (inventory.isFluidOnly()) {
            return InteractionResult.PASS;
        }

        ItemStack selectedStack = inventory.getSelectedStack();
        if (!shouldProxyStoredInteraction(selectedStack)) {
            return InteractionResult.PASS;
        }

        InteractionResultHolder<ItemStack> result = proxyStoredItemUse(
                player.level(),
                player,
                usedHand,
                stack,
                inventory,
                proxyStack -> new InteractionResultHolder<>(
                        proxyStack.interactLivingEntity(player, interactionTarget, usedHand),
                        player.getItemInHand(usedHand).copy()
                )
        );
        return result == null ? InteractionResult.PASS : result.getResult();
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
        if (DeepNullConfig.isGuideMeHintEnabled() && ModList.get().isLoaded("guideme")) {
            tooltipComponents.add(Component.translatable("dn.guideme_hint.desc").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide || !(entity instanceof Player player) || level.getGameTime() % 5L != Math.floorMod(slotId, 5)) {
            return;
        }

        DeepNullInventory inventory = new DeepNullInventory(tier, stack, level.registryAccess(), null);
        if (inventory.isAutoFeedingEnabled()) {
            autoFeedPlayer(player, inventory);
        }
        if (!inventory.isFluidOnly() && inventory.hasStoneworksUpgrade() && level.getGameTime() % 20L == Math.floorMod(slotId, 20)) {
            inventory.runStoneworksCycle(hasWaterDampNull(player));
        }
        if (inventory.isFluidOnly()) {
            if (inventory.hasStoneGeneratorUpgrade() && level.getGameTime() % 20L == Math.floorMod(slotId, 20)) {
                runDampNullGenerator(player, inventory, inventory.getStoneGeneratorOutput(inventory.getStoneGenerationRate()), false);
            }
            if (inventory.hasObsidianGeneratorUpgrade() && level.getGameTime() % 100L == Math.floorMod(slotId, 100)) {
                runDampNullGenerator(player, inventory, inventory.getObsidianGeneratorOutput(), true);
            }
        }
        if (inventory.isChargingEnabled() && inventory.getEnergyStored() > 0) {
            int remainingTransfer = inventory.getEnergyTransferRate();
            remainingTransfer = chargeInventorySection(player.getInventory().items, stack, inventory, remainingTransfer);
            remainingTransfer = chargeInventorySection(player.getInventory().offhand, stack, inventory, remainingTransfer);
            chargeInventorySection(player.getInventory().armor, stack, inventory, remainingTransfer);
        }
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

    private static boolean hasWaterDampNull(Player player) {
        for (ItemStack candidate : player.getInventory().items) {
            if (candidate.isEmpty() || !(candidate.getItem() instanceof DampNullItem dampNullItem)) {
                continue;
            }
            DeepNullInventory dampInventory = new DeepNullInventory(dampNullItem.tier(), candidate, player.level().registryAccess(), null);
            if (dampInventory.containsFluidAmountAtLeast(Fluids.WATER, FluidType.BUCKET_VOLUME)) {
                return true;
            }
        }
        for (ItemStack candidate : player.getInventory().offhand) {
            if (candidate.isEmpty() || !(candidate.getItem() instanceof DampNullItem dampNullItem)) {
                continue;
            }
            DeepNullInventory dampInventory = new DeepNullInventory(dampNullItem.tier(), candidate, player.level().registryAccess(), null);
            if (dampInventory.containsFluidAmountAtLeast(Fluids.WATER, FluidType.BUCKET_VOLUME)) {
                return true;
            }
        }
        return false;
    }

    private static InteractionResultHolder<ItemStack> tryUseStoredConsumable(
            Level level,
            Player player,
            InteractionHand hand,
            ItemStack deepNullStack,
            DeepNullInventory inventory
    ) {
        int selectedSlot = inventory.getSelectedSlot();
        ItemStack selectedStack = inventory.getSelectedStack();
        if (selectedSlot < 0 || !supportsStoredConsumeUse(selectedStack, player)) {
            clearProxyUseState(deepNullStack);
            return null;
        }

        FoodProperties foodProperties = selectedStack.get(DataComponents.FOOD);
        if (foodProperties != null && !player.canEat(foodProperties.canAlwaysEat())) {
            clearProxyUseState(deepNullStack);
            return InteractionResultHolder.fail(deepNullStack);
        }

        setProxyUseState(deepNullStack, selectedSlot, selectedStack.getUseAnimation(), selectedStack.getUseDuration(player));
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(deepNullStack);
    }

    private static InteractionResult tryUseStoredStatefulItem(UseOnContext context, DeepNullInventory inventory) {
        Player player = context.getPlayer();
        if (player == null || !shouldProxyStoredInteraction(inventory.getSelectedStack())) {
            return null;
        }

        InteractionResultHolder<ItemStack> result = proxyStoredItemUse(
                context.getLevel(),
                player,
                context.getHand(),
                context.getItemInHand(),
                inventory,
                proxyStack -> {
                    BlockHitResult hitResult = new BlockHitResult(
                            context.getClickLocation(),
                            context.getClickedFace(),
                            context.getClickedPos(),
                            context.isInside()
                    );
                    InteractionResult useOnResult = proxyStack.useOn(new UseOnContext(context.getLevel(), player, context.getHand(), proxyStack, hitResult));
                    return new InteractionResultHolder<>(useOnResult, player.getItemInHand(context.getHand()).copy());
                }
        );
        return result == null || result.getResult() == InteractionResult.PASS ? null : result.getResult();
    }

    private static boolean shouldProxyStoredInteraction(ItemStack selectedStack) {
        return !selectedStack.isEmpty()
                && !(selectedStack.getItem() instanceof BlockItem)
                && !(selectedStack.getItem() instanceof BucketItem)
                && !supportsStoredConsumeUse(selectedStack, null);
    }

    private static boolean supportsStoredConsumeUse(ItemStack selectedStack, LivingEntity entity) {
        if (selectedStack.isEmpty()) {
            return false;
        }
        UseAnim useAnim = selectedStack.getUseAnimation();
        if (useAnim != UseAnim.EAT && useAnim != UseAnim.DRINK) {
            return false;
        }
        return entity == null || selectedStack.getUseDuration(entity) > 0;
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
        if (ItemStack.isSameItemSameComponents(originalSingle, resultStack)
                && resultStack.getCount() == originalSingle.getCount()) {
            return;
        }

        if (originalSelected.getCount() <= 1) {
            inventory.setStackInSlot(selectedSlot, resultStack);
            return;
        }

        inventory.consumeStoredItem(selectedSlot, 1);
        if (!resultStack.isEmpty()) {
            ItemStack remainder = inventory.insertIntoFirstAvailableSlot(resultStack, false);
            if (!remainder.isEmpty()) {
                player.getInventory().placeItemBackInInventory(remainder);
            }
        }
    }

    private static void setProxyUseState(ItemStack deepNullStack, int selectedSlot, UseAnim useAnim, int useDuration) {
        CompoundTag root = deepNullStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag deepNullTag = root.contains(DEEPNULL_TAG, Tag.TAG_COMPOUND) ? root.getCompound(DEEPNULL_TAG).copy() : new CompoundTag();
        deepNullTag.putInt(PROXY_USE_SLOT_TAG, selectedSlot);
        deepNullTag.putInt(PROXY_USE_ANIM_TAG, useAnim.ordinal());
        deepNullTag.putInt(PROXY_USE_DURATION_TAG, useDuration);
        root.put(DEEPNULL_TAG, deepNullTag);
        deepNullStack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    private static void clearProxyUseState(ItemStack deepNullStack) {
        CompoundTag root = deepNullStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains(DEEPNULL_TAG, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag deepNullTag = root.getCompound(DEEPNULL_TAG).copy();
        if (!deepNullTag.contains(PROXY_USE_SLOT_TAG) && !deepNullTag.contains(PROXY_USE_ANIM_TAG) && !deepNullTag.contains(PROXY_USE_DURATION_TAG)) {
            return;
        }
        deepNullTag.remove(PROXY_USE_SLOT_TAG);
        deepNullTag.remove(PROXY_USE_ANIM_TAG);
        deepNullTag.remove(PROXY_USE_DURATION_TAG);
        root.put(DEEPNULL_TAG, deepNullTag);
        deepNullStack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    private static int getProxyUseSlot(ItemStack deepNullStack) {
        CompoundTag root = deepNullStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains(DEEPNULL_TAG, Tag.TAG_COMPOUND)) {
            return -1;
        }
        CompoundTag deepNullTag = root.getCompound(DEEPNULL_TAG);
        return deepNullTag.contains(PROXY_USE_SLOT_TAG, Tag.TAG_INT) ? deepNullTag.getInt(PROXY_USE_SLOT_TAG) : -1;
    }

    private static int getProxyUseDuration(ItemStack deepNullStack) {
        CompoundTag root = deepNullStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains(DEEPNULL_TAG, Tag.TAG_COMPOUND)) {
            return -1;
        }
        CompoundTag deepNullTag = root.getCompound(DEEPNULL_TAG);
        return deepNullTag.contains(PROXY_USE_DURATION_TAG, Tag.TAG_INT) ? deepNullTag.getInt(PROXY_USE_DURATION_TAG) : -1;
    }

    private static UseAnim getProxyUseAnimation(ItemStack deepNullStack) {
        CompoundTag root = deepNullStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains(DEEPNULL_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag deepNullTag = root.getCompound(DEEPNULL_TAG);
        if (!deepNullTag.contains(PROXY_USE_ANIM_TAG, Tag.TAG_INT)) {
            return null;
        }
        int ordinal = deepNullTag.getInt(PROXY_USE_ANIM_TAG);
        UseAnim[] values = UseAnim.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
    }

    private static int chargeInventorySection(List<ItemStack> stacks, ItemStack deepNullStack, DeepNullInventory inventory, int remainingTransfer) {
        for (ItemStack candidate : stacks) {
            if (remainingTransfer <= 0 || inventory.getEnergyStored() <= 0) {
                break;
            }
        if (candidate.isEmpty() || candidate == deepNullStack) {
            continue;
        }

            IEnergyStorage energyStorage = ModCapabilities.getEnergyStorage(candidate);
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

    private static void autoFeedPlayer(Player player, DeepNullInventory inventory) {
        if (!player.getFoodData().needsFood()) {
            return;
        }

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack candidate = inventory.getStackInSlot(slot);
            FoodProperties foodProperties = candidate.get(DataComponents.FOOD);
            if (foodProperties == null || !player.canEat(foodProperties.canAlwaysEat())) {
                continue;
            }

            ItemStack resultStack = candidate.copyWithCount(1).finishUsingItem(player.level(), player);
            applyStoredUseResult(player, inventory, slot, candidate, resultStack);
            return;
        }
    }

    private static void runDampNullGenerator(Player player, DeepNullInventory inventory, ItemStack generated, boolean consumesFluids) {
        if (generated.isEmpty()) {
            return;
        }
        if (consumesFluids ? !inventory.hasObsidianGenerationRequirements() : !inventory.hasStoneGenerationRequirements()) {
            return;
        }

        DeepNullInventory matchingTarget = findGeneratorTarget(player, generated, true);
        if (matchingTarget == null) {
            matchingTarget = findGeneratorTarget(player, generated, false);
        }
        if (matchingTarget == null) {
            return;
        }

        if (consumesFluids && !inventory.consumeObsidianGeneratorInputs()) {
            return;
        }
        matchingTarget.insertAutomationOutput(generated, false);
    }

    private static DeepNullInventory findGeneratorTarget(Player player, ItemStack generated, boolean preferExistingMatch) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidateStack = player.getInventory().getItem(slot);
            if (!(candidateStack.getItem() instanceof DeepNullItem deepNullItem) || candidateStack.getItem() instanceof DampNullItem) {
                continue;
            }

            DeepNullInventory targetInventory = new DeepNullInventory(deepNullItem.tier(), candidateStack, player.level().registryAccess(), null);
            boolean matchesPreference = preferExistingMatch
                    ? targetInventory.containsGeneratorSeedStack(generated)
                    : !targetInventory.containsGeneratorSeedStack(generated) && targetInventory.allowsGeneratedOutput(generated);
            if (!matchesPreference) {
                continue;
            }

            if (targetInventory.insertAutomationOutput(generated.copy(), true).getCount() < generated.getCount()) {
                return targetInventory;
            }
        }
        return null;
    }

    private static InteractionResult tryShiftTransfer(UseOnContext context, DeepNullInventory inventory) {
        return inventory.isFluidOnly()
                ? tryShiftFluidTransfer(context, inventory)
                : tryShiftItemTransfer(context, inventory);
    }

    private static InteractionResult tryShiftItemTransfer(UseOnContext context, DeepNullInventory inventory) {
        if (ModList.get().isLoaded("ae2")) {
            InteractionResult ae2Result = Ae2TransferCompat.tryShiftItemTransfer(context, inventory);
            if (ae2Result != null) {
                return ae2Result;
            }
        }

        IItemHandler target = getBlockItemHandler(context);
        if (target == null) {
            return InteractionResult.PASS;
        }

        if (context.getLevel().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        boolean moved = false;
        if (inventory.getTransferDirectionMode().allowsInsert()) {
            moved = inventory.transferItemsToTarget(target);
        }
        if (!moved && inventory.getTransferDirectionMode().allowsExtract()) {
            moved = inventory.transferItemsFromTargetMatching(target);
        }
        return moved ? InteractionResult.sidedSuccess(false) : InteractionResult.FAIL;
    }

    private static InteractionResult tryShiftFluidTransfer(UseOnContext context, DeepNullInventory inventory) {
        if (ModList.get().isLoaded("mekanism")) {
            InteractionResult chemicalResult = MekanismTransferCompat.tryShiftChemicalTransfer(context, inventory);
            if (chemicalResult == InteractionResult.sidedSuccess(context.getLevel().isClientSide)) {
                return chemicalResult;
            }
            if (chemicalResult == InteractionResult.FAIL && (inventory.hasAnyChemical() || !inventory.hasAnyFluid())) {
                return chemicalResult;
            }
        }

        if (ModList.get().isLoaded("ae2")) {
            InteractionResult ae2Result = Ae2TransferCompat.tryShiftFluidTransfer(context, inventory);
            if (ae2Result != null) {
                return ae2Result;
            }
        }

        IFluidHandler target = getBlockFluidHandler(context);
        if (target == null) {
            return InteractionResult.PASS;
        }

        if (context.getLevel().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        boolean moved = false;
        if (inventory.getTransferDirectionMode().allowsInsert()) {
            moved = inventory.transferFluidsToTarget(target);
        }
        if (!moved && inventory.getTransferDirectionMode().allowsExtract()) {
            moved = inventory.transferFluidsFromTargetMatching(target);
        }
        return moved ? InteractionResult.sidedSuccess(false) : InteractionResult.FAIL;
    }

    private static IItemHandler getBlockItemHandler(UseOnContext context) {
        IItemHandler target = ModCapabilities.getBlockItemHandler(context.getLevel(), context.getClickedPos(), context.getClickedFace());
        if (target == null) {
            target = ModCapabilities.getBlockItemHandler(context.getLevel(), context.getClickedPos(), null);
        }
        return target;
    }

    private static IFluidHandler getBlockFluidHandler(UseOnContext context) {
        IFluidHandler target = ModCapabilities.getBlockFluidHandler(context.getLevel(), context.getClickedPos(), context.getClickedFace());
        if (target == null) {
            target = ModCapabilities.getBlockFluidHandler(context.getLevel(), context.getClickedPos(), null);
        }
        if (target == null) {
            target = FluidUtil.getFluidHandler(context.getLevel(), context.getClickedPos(), context.getClickedFace()).orElse(null);
        }
        if (target == null) {
            target = FluidUtil.getFluidHandler(context.getLevel(), context.getClickedPos(), null).orElse(null);
        }
        return target;
    }

    private static InteractionResult tryUseStoredFluid(UseOnContext context, DeepNullInventory inventory) {
        if (!inventory.supportsFluidStorage()) {
            return null;
        }

        Level level = context.getLevel();
        Player player = context.getPlayer();
        InteractionResult spongeResult = tryUseSpongeAbsorption(level, player, context.getItemInHand(), inventory, context.getClickedPos());
        if (spongeResult != null) {
            return spongeResult;
        }
        BlockHitResult sourceHit = player == null ? null : getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (sourceHit != null && sourceHit.getType() == HitResult.Type.BLOCK) {
            BlockPos sourcePos = sourceHit.getBlockPos();
            FluidState sourceFluid = level.getFluidState(sourcePos);
            if (!sourceFluid.isEmpty() && sourceFluid.isSource()) {
                InteractionResult pickupResult = tryPickUpSourceFluid(level, player, context.getItemInHand(), inventory, sourcePos, sourceHit.getDirection());
                return pickupResult == null ? InteractionResult.FAIL : pickupResult;
            }
        }

        BlockPos clickedPos = context.getClickedPos();
        Direction side = context.getClickedFace();
        FluidState clickedFluid = level.getFluidState(clickedPos);
        if (!clickedFluid.isEmpty() && clickedFluid.isSource()) {
            InteractionResult pickupResult = tryPickUpSourceFluid(level, player, context.getItemInHand(), inventory, clickedPos, side);
            return pickupResult == null ? InteractionResult.FAIL : pickupResult;
        }

        int selectedSlot = inventory.getSelectedSlot();
        if (selectedSlot < 0 || selectedSlot >= inventory.getFluidSlotCount()) {
            return null;
        }
        FluidStack selectedFluid = inventory.getFluidInSlot(selectedSlot);
        if (selectedFluid.isEmpty()) {
            return null;
        }

        DeepNullFluidHandler sourceHandler = new DeepNullFluidHandler(inventory, context.getItemInHand(), selectedSlot);
        FluidStack placeAmount = selectedFluid.copyWithAmount(Math.min(selectedFluid.getAmount(), FluidType.BUCKET_VOLUME));
        if (FluidUtil.tryPlaceFluid(player, level, context.getHand(), clickedPos, sourceHandler, placeAmount)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        BlockPos adjacentPos = clickedPos.relative(side);
        if (!adjacentPos.equals(clickedPos) && FluidUtil.tryPlaceFluid(player, level, context.getHand(), adjacentPos, sourceHandler, placeAmount)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return null;
    }

    private static InteractionResult tryPickUpSourceFluid(
            Level level,
            Player player,
            ItemStack deepNullStack,
            DeepNullInventory inventory,
            BlockPos clickedPos,
            Direction side
    ) {
        SourceAbsorbPlan plan = previewSourceAbsorption(level, player, inventory, clickedPos, side);
        if (plan == null) {
            return null;
        }

        if (level.isClientSide) {
            inventory.setSelectedSlot(plan.targetSlot());
            return InteractionResult.sidedSuccess(true);
        }

        return executeSourceAbsorption(level, player, deepNullStack, inventory, clickedPos, side, plan)
                ? InteractionResult.sidedSuccess(false)
                : InteractionResult.FAIL;
    }

    private static InteractionResult tryUseSpongeAbsorption(
            Level level,
            Player player,
            ItemStack deepNullStack,
            DeepNullInventory inventory,
            BlockPos anchorPos
    ) {
        if (player == null || anchorPos == null || !inventory.isSpongeEnabled()) {
            return null;
        }

        List<BlockPos> sourceBlocks = findVisibleSourceBlocks(level, player, inventory.tier(), anchorPos);
        if (sourceBlocks.isEmpty()) {
            return null;
        }

        int absorbed = 0;
        DeepNullInventory simulatedInventory = level.isClientSide
                ? new DeepNullInventory(inventory.tier(), deepNullStack.copy(), level.registryAccess(), null)
                : inventory;

        for (BlockPos sourcePos : sourceBlocks) {
            SourceAbsorbPlan plan = previewSourceAbsorption(level, player, simulatedInventory, sourcePos, Direction.UP);
            if (plan == null) {
                continue;
            }

            boolean success = level.isClientSide
                    ? reserveSourceAbsorption(simulatedInventory, plan)
                    : executeSourceAbsorption(level, player, deepNullStack, inventory, sourcePos, Direction.UP, plan);
            if (!success) {
                continue;
            }

            absorbed++;
        }

        if (absorbed <= 0) {
            return null;
        }

        if (level.isClientSide && simulatedInventory.getSelectedSlot() >= 0) {
            inventory.setSelectedSlot(simulatedInventory.getSelectedSlot());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static List<BlockPos> findVisibleSourceBlocks(Level level, Player player, DeepNullTier tier, BlockPos anchorPos) {
        int horizontalSize = Math.max(1, tier.spongeRangeWidth());
        int verticalSize = Math.max(1, tier.spongeRangeHeight());
        int minX = anchorPos.getX() - (horizontalSize / 2);
        int minY = anchorPos.getY() - (verticalSize / 2);
        int minZ = anchorPos.getZ() - (horizontalSize / 2);
        int maxX = minX + horizontalSize - 1;
        int maxY = minY + verticalSize - 1;
        int maxZ = minZ + horizontalSize - 1;
        Vec3 eyePosition = player.getEyePosition();
        List<BlockPos> visibleSources = new ArrayList<>();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    FluidState fluidState = level.getFluidState(candidate);
                    if (fluidState.isEmpty() || !fluidState.isSource()) {
                        continue;
                    }
                    if (!hasLineOfSightToSource(level, player, eyePosition, candidate)) {
                        continue;
                    }
                    visibleSources.add(candidate);
                }
            }
        }

        visibleSources.sort(Comparator.comparingDouble(pos -> squaredDistanceToCenter(eyePosition, pos)));
        return visibleSources;
    }

    private static boolean hasLineOfSightToSource(Level level, Player player, Vec3 eyePosition, BlockPos sourcePos) {
        BlockHitResult result = level.clip(new ClipContext(eyePosition, Vec3.atCenterOf(sourcePos), Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return result.getType() == HitResult.Type.MISS
                || result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(sourcePos);
    }

    private static double squaredDistanceToCenter(Vec3 origin, BlockPos pos) {
        double dx = origin.x - (pos.getX() + 0.5D);
        double dy = origin.y - (pos.getY() + 0.5D);
        double dz = origin.z - (pos.getZ() + 0.5D);
        return dx * dx + dy * dy + dz * dz;
    }

    private static @org.jetbrains.annotations.Nullable SourceAbsorbPlan previewSourceAbsorption(
            Level level,
            Player player,
            DeepNullInventory inventory,
            BlockPos clickedPos,
            Direction side
    ) {
        IFluidHandler targetHandler = getSourceFluidHandler(level, player, clickedPos, side);
        if (targetHandler == null) {
            return null;
        }

        FluidStack available = targetHandler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
        if (available.isEmpty()) {
            available = firstFluid(targetHandler);
        }
        if (available.isEmpty()) {
            return null;
        }

        int transferAmount = Math.min(FluidType.BUCKET_VOLUME, available.getAmount());
        int targetSlot = inventory.findFluidInsertSlot(available);
        if (targetSlot < 0) {
            return null;
        }

        DeepNullFluidHandler internalHandler = new DeepNullFluidHandler(inventory, ItemStack.EMPTY, targetSlot);
        FluidStack transferStack = available.copyWithAmount(transferAmount);
        int accepted = internalHandler.fill(transferStack, IFluidHandler.FluidAction.SIMULATE);
        if (accepted >= transferAmount) {
            return new SourceAbsorbPlan(targetSlot, transferStack, false);
        }

        if (DeepNullConfig.voidFullFluidsOnSponge()) {
            FluidStack existing = inventory.getFluidInSlot(targetSlot);
            if (!existing.isEmpty()
                    && FluidStack.isSameFluidSameComponents(existing, transferStack)
                    && existing.getAmount() >= inventory.getFluidCapacity()) {
                return new SourceAbsorbPlan(targetSlot, transferStack, true);
            }
        }

        return null;
    }

    private static boolean reserveSourceAbsorption(DeepNullInventory inventory, SourceAbsorbPlan plan) {
        if (plan.voidExcess()) {
            inventory.setSelectedSlot(plan.targetSlot());
            return true;
        }
        int inserted = inventory.fillFluid(plan.targetSlot(), plan.fluid(), false);
        if (inserted < plan.fluid().getAmount()) {
            return false;
        }
        inventory.setSelectedSlot(plan.targetSlot());
        return true;
    }

    private static boolean executeSourceAbsorption(
            Level level,
            Player player,
            ItemStack deepNullStack,
            DeepNullInventory inventory,
            BlockPos clickedPos,
            Direction side,
            SourceAbsorbPlan plan
    ) {
        IFluidHandler targetHandler = getSourceFluidHandler(level, player, clickedPos, side);
        if (targetHandler == null) {
            return false;
        }

        FluidActionResult bucketPickup = FluidUtil.tryPickUpFluid(new ItemStack(Items.BUCKET), player, level, clickedPos, side);
        FluidStack pickedUp = bucketPickup.isSuccess()
                ? FluidUtil.getFluidContained(bucketPickup.getResult()).orElse(plan.fluid())
                : FluidStack.EMPTY;
        if (pickedUp.isEmpty() && !bucketPickup.isSuccess()) {
            pickedUp = targetHandler.drain(plan.fluid().getAmount(), IFluidHandler.FluidAction.EXECUTE);
        }
        if (pickedUp.isEmpty()) {
            return false;
        }

        if (plan.voidExcess()) {
            inventory.setSelectedSlot(plan.targetSlot());
            return true;
        }

        DeepNullFluidHandler internalHandler = new DeepNullFluidHandler(inventory, deepNullStack, plan.targetSlot());
        int inserted = internalHandler.fill(
                pickedUp.copyWithAmount(Math.min(plan.fluid().getAmount(), pickedUp.getAmount())),
                IFluidHandler.FluidAction.EXECUTE
        );
        if (inserted <= 0) {
            return false;
        }

        inventory.setSelectedSlot(plan.targetSlot());
        return true;
    }

    private static IFluidHandler getSourceFluidHandler(Level level, Player player, BlockPos clickedPos, Direction side) {
        IFluidHandler targetHandler = FluidUtil.getFluidHandler(level, clickedPos, side).orElse(null);
        if (targetHandler == null && level.getBlockState(clickedPos).getBlock() instanceof net.minecraft.world.level.block.BucketPickup bucketPickup) {
            targetHandler = new net.neoforged.neoforge.fluids.capability.wrappers.BucketPickupHandlerWrapper(player, bucketPickup, level, clickedPos);
        }
        return targetHandler;
    }

    private static FluidStack firstFluid(IFluidHandler handler) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack fluidInTank = handler.getFluidInTank(tank);
            if (!fluidInTank.isEmpty()) {
                return fluidInTank;
            }
        }
        return FluidStack.EMPTY;
    }

    private record SourceAbsorbPlan(int targetSlot, FluidStack fluid, boolean voidExcess) {
    }

}
