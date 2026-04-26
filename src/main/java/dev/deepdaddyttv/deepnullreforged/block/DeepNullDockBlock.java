package dev.deepdaddyttv.deepnullreforged.block;

import com.mojang.serialization.MapCodec;
import dev.deepdaddyttv.deepnullreforged.capability.DeepNullFluidHandler;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenuOpener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

public class DeepNullDockBlock extends BaseEntityBlock {
    public static final MapCodec<DeepNullDockBlock> CODEC = simpleCodec(DeepNullDockBlock::new);
    private static final VoxelShape EMPTY_SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);
    private static final VoxelShape FILLED_SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    private static final VoxelShape FULL_SUPPORT_SHAPE = Shapes.block();

    public DeepNullDockBlock() {
        this(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5.0F, 6.0F).noOcclusion());
    }

    private DeepNullDockBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DeepNullDockBlockEntity dock && dock.hasStoredDeepNull()) {
            return FILLED_SHAPE;
        }
        return EMPTY_SHAPE;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return FULL_SUPPORT_SHAPE;
    }

    @Override
    protected float getDestroyProgress(BlockState state, net.minecraft.world.entity.player.Player player, BlockGetter level, BlockPos pos) {
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness == -1.0F) {
            return 0.0F;
        }
        boolean pickaxeLikeTool = player.getMainHandItem().canPerformAction(ItemAbilities.PICKAXE_DIG);
        float destroySpeed;
        try {
            destroySpeed = player.getDestroySpeed(state);
        } catch (NoSuchElementException ignored) {
            destroySpeed = player.getMainHandItem().getDestroySpeed(state);
        }
        if (pickaxeLikeTool && destroySpeed > 1.0F) {
            return destroySpeed / hardness / 7.5F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof DeepNullDockBlockEntity dock)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!dock.hasStoredDeepNull() && stack.getItem() instanceof DeepNullItem) {
            if (level.isClientSide) {
                dock.setStoredDeepNullClient(stack);
            } else {
                dock.setStoredDeepNull(stack.copy());
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (dock.hasStoredDeepNull()) {
            ItemStack updatedContainer = transferFluidContainerWithDockedDampNull(dock, stack, level.isClientSide);
            if (updatedContainer != null) {
                if (!level.isClientSide) {
                    player.setItemInHand(hand, updatedContainer);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        if (dock.hasStoredDeepNull() && player instanceof ServerPlayer serverPlayer && !player.isShiftKeyDown()) {
            DeepNullMenuOpener.openDock(serverPlayer, dock);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof DeepNullDockBlockEntity dock) || !dock.hasStoredDeepNull()) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                dock.setStoredDeepNullClient(ItemStack.EMPTY);
            } else {
                ItemStack stored = dock.removeStoredDeepNull();
                if (!player.addItem(stored)) {
                    player.drop(stored, false);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            DeepNullMenuOpener.openDock(serverPlayer, dock);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof DeepNullDockBlockEntity dock && dock.hasStoredDeepNull()) {
            popResource(level, pos, dock.removeStoredDeepNull());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeepNullDockBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities.DEEP_NULL_DOCK.get(), DeepNullDockBlockEntity::serverTick);
    }

    public static @Nullable ItemStack transferFluidContainerWithDockedDampNull(DeepNullDockBlockEntity dock, ItemStack heldStack, boolean simulate) {
        DeepNullInventory inventory = dock.createInventory();
        if (inventory == null || !inventory.supportsFluidStorage() || heldStack.isEmpty()) {
            return null;
        }

        ItemStack working = heldStack.copyWithCount(1);
        IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(working).orElse(null);
        FluidStack contained = itemHandler == null
                ? FluidStack.EMPTY
                : FluidUtil.getFluidContained(working).orElseGet(() -> firstFluidIn(itemHandler));
        boolean rawBucket = false;
        if (contained.isEmpty() && working.getItem() instanceof BucketItem bucketItem && bucketItem.content != Fluids.EMPTY) {
            contained = new FluidStack(bucketItem.content, FluidType.BUCKET_VOLUME);
            rawBucket = true;
        }

        if (!contained.isEmpty()) {
            int targetSlot = inventory.findFluidInsertSlot(contained);
            if (targetSlot < 0) {
                return null;
            }

            if (rawBucket) {
                if (inventory.fillFluid(targetSlot, contained, simulate) != contained.getAmount()) {
                    return null;
                }
            } else {
                DeepNullFluidHandler targetHandler = new DeepNullFluidHandler(inventory, ItemStack.EMPTY, targetSlot);
                FluidStack transferred = FluidUtil.tryFluidTransfer(targetHandler, itemHandler, contained.getAmount(), !simulate);
                if (transferred.isEmpty()) {
                    return null;
                }
            }

            if (!simulate && inventory.getSelectedSlot() != targetSlot) {
                inventory.setSelectedSlot(targetSlot);
            }
            if (rawBucket) {
                return heldStack.getCount() == 1 ? new ItemStack(Items.BUCKET) : heldStack;
            }
            return heldStack.getCount() == 1 ? itemHandler.getContainer() : heldStack;
        }

        if (itemHandler == null) {
            return null;
        }

        int sourceSlot = resolveDockedFluidSourceSlot(inventory);
        if (sourceSlot < 0) {
            return null;
        }

        DeepNullFluidHandler sourceHandler = new DeepNullFluidHandler(inventory, ItemStack.EMPTY, sourceSlot);
        FluidStack selectedFluid = inventory.getFluidInSlot(sourceSlot);
        FluidStack transferred = FluidUtil.tryFluidTransfer(itemHandler, sourceHandler, Math.min(selectedFluid.getAmount(), FluidType.BUCKET_VOLUME), !simulate);
        if (transferred.isEmpty()) {
            return null;
        }

        if (!simulate && inventory.getSelectedSlot() != sourceSlot) {
            inventory.setSelectedSlot(sourceSlot);
        }
        return heldStack.getCount() == 1 ? itemHandler.getContainer() : heldStack;
    }

    private static int resolveDockedFluidSourceSlot(DeepNullInventory inventory) {
        int selectedSlot = inventory.getSelectedSlot();
        if (selectedSlot >= 0 && selectedSlot < inventory.getFluidSlotCount() && !inventory.getFluidInSlot(selectedSlot).isEmpty()) {
            return selectedSlot;
        }
        for (int slot = 0; slot < inventory.getFluidSlotCount(); slot++) {
            if (!inventory.getFluidInSlot(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private static FluidStack firstFluidIn(IFluidHandlerItem itemHandler) {
        for (int tank = 0; tank < itemHandler.getTanks(); tank++) {
            FluidStack fluidInTank = itemHandler.getFluidInTank(tank);
            if (!fluidInTank.isEmpty()) {
                return fluidInTank;
            }
        }
        return FluidStack.EMPTY;
    }
}
