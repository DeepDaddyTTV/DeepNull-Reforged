package dev.deepdaddyttv.deepnullreforged.entity;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModEntityTypes;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class DampNullBalloonProjectile extends ThrowableItemProjectile {
    private static final String INVENTORY_SLOT_TAG = "InventorySlot";
    private static final String TANK_SLOT_TAG = "TankSlot";

    private int inventorySlot = -1;
    private int tankSlot = -1;

    public DampNullBalloonProjectile(EntityType<? extends DampNullBalloonProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public DampNullBalloonProjectile(Level level, LivingEntity owner, int inventorySlot, int tankSlot) {
        super(ModEntityTypes.DAMPNULL_BALLOON_PROJECTILE.get(), owner, level);
        this.inventorySlot = inventorySlot;
        this.tankSlot = tankSlot;
        setItem(new ItemStack(ModItems.BALLOON_UPGRADE.get()));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BALLOON_UPGRADE.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) {
            return;
        }
        if (result instanceof BlockHitResult blockHitResult && getOwner() instanceof ServerPlayer player) {
            applyImpact(player, blockHitResult);
        }
        level().broadcastEntityEvent(this, (byte) 3);
        discard();
    }

    public boolean applyImpact(ServerPlayer player, BlockHitResult hitResult) {
        if (inventorySlot < 0 || inventorySlot >= player.getInventory().getContainerSize()) {
            return false;
        }
        ItemStack dampNullStack = player.getInventory().getItem(inventorySlot);
        if (!(dampNullStack.getItem() instanceof DampNullItem dampNullItem)) {
            return false;
        }

        DeepNullInventory inventory = new DeepNullInventory(dampNullItem.tier(), dampNullStack, player.level().registryAccess(), null);
        if (!inventory.hasBalloonUpgrade() || !inventory.acceptsNormalFluids() || tankSlot < 0 || tankSlot >= inventory.getFluidSlotCount()) {
            return false;
        }
        FluidStack selectedFluid = inventory.getFluidInSlot(tankSlot);
        if (selectedFluid.isEmpty() || selectedFluid.getAmount() < FluidType.BUCKET_VOLUME) {
            return false;
        }

        BlockPos targetPos = resolveTargetPos(player.level(), hitResult, selectedFluid.getFluid());
        if (targetPos == null) {
            return false;
        }
        return applyFluidImpact(player.level(), inventory, tankSlot, selectedFluid.copyWithAmount(FluidType.BUCKET_VOLUME), targetPos);
    }

    public static boolean applyFluidImpact(Level level, DeepNullInventory inventory, int tankSlot, FluidStack selectedFluid, BlockPos targetPos) {
        if (level.isClientSide || selectedFluid.isEmpty() || selectedFluid.getAmount() < FluidType.BUCKET_VOLUME) {
            return false;
        }

        FluidState existingFluid = level.getFluidState(targetPos);
        if (!existingFluid.isEmpty()) {
            if (!existingFluid.isSource()) {
                return false;
            }
            if (existingFluid.getType().isSame(selectedFluid.getFluid())) {
                return true;
            }
            FluidStack replaced = new FluidStack(existingFluid.getType(), FluidType.BUCKET_VOLUME);
            placeFluid(level, targetPos, selectedFluid.getFluid());
            inventory.drainFluid(tankSlot, FluidType.BUCKET_VOLUME, false);
            inventory.fillExistingFluidSlotsOnly(replaced, false);
            return true;
        }

        if (!canPlaceFluid(level, targetPos)) {
            return false;
        }
        placeFluid(level, targetPos, selectedFluid.getFluid());
        inventory.drainFluid(tankSlot, FluidType.BUCKET_VOLUME, false);
        return true;
    }

    private static BlockPos resolveTargetPos(Level level, BlockHitResult hitResult, Fluid selectedFluid) {
        BlockPos hitPos = hitResult.getBlockPos();
        if (canAffectPos(level, hitPos, selectedFluid)) {
            return hitPos;
        }
        BlockPos adjacentPos = hitPos.relative(hitResult.getDirection());
        return canAffectPos(level, adjacentPos, selectedFluid) ? adjacentPos : null;
    }

    private static boolean canAffectPos(Level level, BlockPos pos, Fluid selectedFluid) {
        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            return fluidState.isSource();
        }
        return canPlaceFluid(level, pos);
    }

    private static boolean canPlaceFluid(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || !state.blocksMotion();
    }

    private static void placeFluid(Level level, BlockPos pos, Fluid fluid) {
        level.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), 3);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(INVENTORY_SLOT_TAG, inventorySlot);
        tag.putInt(TANK_SLOT_TAG, tankSlot);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        inventorySlot = tag.getInt(INVENTORY_SLOT_TAG);
        tankSlot = tag.getInt(TANK_SLOT_TAG);
    }
}
