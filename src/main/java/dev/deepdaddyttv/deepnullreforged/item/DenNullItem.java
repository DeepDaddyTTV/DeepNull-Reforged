package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.dennull.DenNullCaptureNormalizer;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullAutomation;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullData;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullEntry;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullSpawnerEntry;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullTagTemplate;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeData;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.menu.DenNullMenuOpener;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DenNullItem extends Item implements DockableNullItem {
    private final DeepNullTier tier;

    public DenNullItem(DeepNullTier tier, Properties properties) {
        super(properties.stacksTo(1).rarity(tier.rarity()));
        this.tier = tier;
    }

    @Override
    public DeepNullTier tier() {
        return tier;
    }

    @Override
    public NullKind nullKind(ItemStack stack) {
        return NullKind.DEN;
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
                DenNullMenuOpener.openHeldItem(serverPlayer, player.getInventory(), DeepNullItem.getInventorySlot(player, hand));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (!level.isClientSide && level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            releaseSelected(serverPlayer, serverLevel, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            return tryCaptureHeld(serverPlayer, hand, target) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(Blocks.SPAWNER)) {
            return super.useOn(context);
        }
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (!(player instanceof ServerPlayer serverPlayer) || !(level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        DenNullData data = DenNullData.get(stack);
        if (!data.upgrades().has(DenNullUpgradeType.SPAWNER)) {
            player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.spawner_upgrade_required"), true);
            return InteractionResult.FAIL;
        }
        DenNullData.AddResult result = data.addSpawner(DenNullSpawnerEntry.fromBlockEntity(spawner), tier);
        if (!result.success()) {
            Component message = switch (result.failure()) {
                case MIXED_MODE -> Component.translatable("item.deepnullreforged.den_null.spawner_mixed_mode");
                case FULL_STACK -> Component.translatable("item.deepnullreforged.den_null.capture_full_stack");
                default -> Component.translatable("item.deepnullreforged.den_null.capture_full");
            };
            player.displayClientMessage(message, true);
            return InteractionResult.FAIL;
        }
        DenNullData.set(stack, result.data());
        level.removeBlock(pos, false);
        syncInventory(serverPlayer);
        player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.spawner_captured", result.data().selectedSpawnerEntry().displayName()), true);
        return InteractionResult.SUCCESS;
    }

    public boolean tryCaptureHeld(ServerPlayer player, InteractionHand hand, Entity target) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (!(heldStack.getItem() instanceof DenNullItem heldDenNullItem)) {
            return false;
        }
        boolean captured = heldDenNullItem.tryCapture(player, heldStack, target);
        if (captured) {
            player.setItemInHand(hand, heldStack);
            syncInventory(player);
        }
        return captured;
    }

    public boolean tryCapture(ServerPlayer player, ItemStack stack, Entity target) {
        if (target == null || target instanceof Player) {
            player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.capture_unsupported"), true);
            return false;
        }
        if (target.isPassenger() || !target.getPassengers().isEmpty()) {
            player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.capture_passengers"), true);
            return false;
        }

        Optional<DenNullEntry> captured = DenNullCaptureNormalizer.capture(target);
        if (captured.isEmpty()) {
            player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.capture_unsupported"), true);
            return false;
        }

        DenNullData data = DenNullData.get(stack);
        if (data.upgrades().has(DenNullUpgradeType.SPAWNER)) {
            player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.spawner_mixed_mode"), true);
            return false;
        }
        DenNullData.AddResult result = data.addCapture(captured.get(), tier);
        if (!result.success()) {
            Component message = switch (result.failure()) {
                case MIXED_MODE -> Component.translatable("item.deepnullreforged.den_null.spawner_mixed_mode");
                case FULL_STACK -> Component.translatable("item.deepnullreforged.den_null.capture_full_stack");
                default -> Component.translatable("item.deepnullreforged.den_null.capture_full");
            };
            player.displayClientMessage(message, true);
            return false;
        }
        if (!DenNullAutomation.consumeBaitForManualCapture(player)) {
            player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.no_bait"), true);
            return false;
        }

        DenNullData.set(stack, result.data());
        syncInventory(player);
        target.discard();
        player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.captured", captured.get().displayName()), true);
        return true;
    }

    public void cycleSelected(ItemStack stack, boolean forward) {
        DenNullData data = DenNullData.get(stack);
        if (data.entries().isEmpty()) {
            return;
        }
        DenNullData.set(stack, data.cycleSelected(forward));
    }

    public boolean releaseEntryAtFeet(ServerPlayer player, ServerLevel level, ItemStack stack, int entryIndex) {
        return releaseEntry(player, level, stack, entryIndex, playerFeetPosition(player));
    }

    private void releaseSelected(ServerPlayer player, ServerLevel level, ItemStack stack) {
        DenNullData data = DenNullData.get(stack);
        if (!data.spawners().isEmpty()) {
            player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.spawner_requires_dock"), true);
            return;
        }
        DenNullEntry selected = data.selectedEntry();
        if (selected == null) {
            player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.empty"), true);
            return;
        }
        releaseEntry(player, level, stack, data.selectedIndex(), releasePosition(player, level));
    }

    private boolean releaseEntry(ServerPlayer player, ServerLevel level, ItemStack stack, int entryIndex, Vec3 spawnPos) {
        DenNullData data = DenNullData.get(stack);
        if (!data.spawners().isEmpty()) {
            player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.spawner_requires_dock"), true);
            return false;
        }
        if (entryIndex < 0 || entryIndex >= data.entries().size()) {
            player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.empty"), true);
            return false;
        }
        DenNullEntry selected = data.entries().get(entryIndex);
        if (!spawnEntry(player, level, stack, selected, data, spawnPos)) {
            player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.release_failed"), true);
            return false;
        }
        DenNullData latest = DenNullData.get(stack);
        if (!latest.upgrades().has(DenNullUpgradeType.CLONE)) {
            DenNullData.ReleaseResult released = latest.releaseAtIndex(entryIndex);
            DenNullData.set(stack, released.data());
        }
        player.displayClientMessage(Component.translatable("item.deepnullreforged.den_null.released", selected.displayName()), true);
        syncInventory(player);
        return true;
    }

    private boolean spawnEntry(ServerPlayer player, ServerLevel level, ItemStack stack, DenNullEntry entry, DenNullData data, Vec3 spawnPos) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entry.entityType()).orElse(null);
        if (type == null) {
            return false;
        }
        Entity entity = type.create(level);
        if (entity == null) {
            return false;
        }

        CompoundTag tag = entry.entityTag().copy();
        prepareReleaseTag(tag, data);
        entity.load(tag);
        entity.setUUID(UUID.randomUUID());
        entity.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), 0.0F);
        entity.setDeltaMovement(Vec3.ZERO);
        if (entity instanceof LivingEntity living) {
            living.setHealth(Math.max(1.0F, living.getMaxHealth()));
        }
        applyReleaseUpgrades(player, stack, entry, entity, data);
        return level.addFreshEntity(entity);
    }

    private void prepareReleaseTag(CompoundTag tag, DenNullData data) {
        DenNullUpgradeData upgrades = data.upgrades();
        if (upgrades.has(DenNullUpgradeType.DYE) && upgrades.dyeColorId() >= 0) {
            tag.putByte("Color", (byte) upgrades.dyeColorId());
            tag.putByte("CollarColor", (byte) upgrades.dyeColorId());
        }
    }

    private void applyReleaseUpgrades(ServerPlayer player, ItemStack stack, DenNullEntry entry, Entity entity, DenNullData data) {
        DenNullUpgradeData upgrades = data.upgrades();
        if (upgrades.has(DenNullUpgradeType.DYE) && upgrades.dyeColorId() >= 0) {
            DyeColor color = DyeColor.byId(upgrades.dyeColorId());
            if (entity instanceof Sheep sheep) {
                sheep.setColor(color);
            }
        }

        if (upgrades.has(DenNullUpgradeType.BABY) && upgrades.babyEnabled()) {
            if (entity instanceof AgeableMob ageableMob) {
                ageableMob.setAge(-24000);
            } else if (entity instanceof LivingEntity living) {
                AttributeInstance scale = living.getAttribute(Attributes.SCALE);
                if (scale != null) {
                    scale.setBaseValue(Math.min(scale.getBaseValue(), 0.5D));
                }
            }
        }

        if (upgrades.has(DenNullUpgradeType.TAG) && DenNullAutomation.consumeNameTagForRelease(player, stack)) {
            DenNullData fresh = DenNullData.get(stack);
            DenNullUpgradeData freshUpgrades = fresh.upgrades();
            String name = DenNullTagTemplate.render(freshUpgrades.tagTemplate(), player.getGameProfile().getName(), entry, freshUpgrades.tagCounter());
            entity.setCustomName(Component.literal(name));
            if (entity instanceof Mob mob) {
                mob.setPersistenceRequired();
            }
            DenNullData.set(stack, fresh.withUpgrades(freshUpgrades.withTagCounter(freshUpgrades.tagCounter() + 1)));
        }
    }

    private Vec3 releasePosition(ServerPlayer player, ServerLevel level) {
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hitResult.getBlockPos();
            Direction direction = hitResult.getDirection();
            return Vec3.atBottomCenterOf(pos.relative(direction));
        }
        return player.getEyePosition().add(player.getLookAngle().scale(2.5D));
    }

    private Vec3 playerFeetPosition(ServerPlayer player) {
        return new Vec3(player.getX(), player.getY(), player.getZ());
    }

    private static void syncInventory(ServerPlayer player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            DenNullAutomation.tickHeld(player, slotId, stack, this);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        DenNullData data = DenNullData.get(stack);
        tooltipComponents.add(Component.translatable("item.deepnullreforged.den_null.tooltip").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.deepnullreforged.den_null.entries", data.entries().size(), tier.slotCount()).withStyle(ChatFormatting.DARK_GRAY));
        if (!data.spawners().isEmpty()) {
            tooltipComponents.add(Component.translatable("item.deepnullreforged.den_null.spawner_entries", data.spawners().size(), tier.slotCount()).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltipComponents.add(Component.translatable("item.deepnullreforged.den_null.captures", data.totalCount()).withStyle(ChatFormatting.DARK_GRAY));
        DenNullEntry selected = data.selectedEntry();
        if (selected != null) {
            tooltipComponents.add(Component.translatable("item.deepnullreforged.den_null.selected", selected.displayName()).withStyle(ChatFormatting.GRAY));
        }
    }
}
