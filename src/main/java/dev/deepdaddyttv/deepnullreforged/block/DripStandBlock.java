package dev.deepdaddyttv.deepnullreforged.block;

import com.mojang.serialization.MapCodec;
import dev.deepdaddyttv.deepnullreforged.block.entity.DripStandBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripSwapEngine;
import dev.deepdaddyttv.deepnullreforged.item.DripNullItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DripStandBlock extends BaseEntityBlock {
    public static final MapCodec<DripStandBlock> CODEC = simpleCodec(DripStandBlock::new);
    private static final VoxelShape SHAPE = box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);

    public DripStandBlock() {
        this(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.5F, 6.0F).noOcclusion());
    }

    private DripStandBlock(BlockBehaviour.Properties properties) {
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
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof DripStandBlockEntity stand)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!stand.hasStoredDripNull() && stack.getItem() instanceof DripNullItem) {
            if (level.isClientSide) {
                stand.setStoredDripNullClient(stack);
            } else {
                stand.setStoredDripNull(stack.copy());
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stand.hasStoredDripNull() && player.isShiftKeyDown()) {
            removeStored(level, stand, player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stand.hasStoredDripNull() && player instanceof ServerPlayer serverPlayer) {
            runStandSwap(serverPlayer, stand);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof DripStandBlockEntity stand) || !stand.hasStoredDripNull()) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            removeStored(level, stand, player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            runStandSwap(serverPlayer, stand);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof DripStandBlockEntity stand && stand.hasStoredDripNull()) {
            popResource(level, pos, stand.removeStoredDripNull());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DripStandBlockEntity(pos, state);
    }

    private static void runStandSwap(ServerPlayer player, DripStandBlockEntity stand) {
        ItemStack stack = stand.getStoredDripNull();
        if (!(stack.getItem() instanceof DripNullItem dripNullItem)) {
            return;
        }
        DripSwapEngine.Result result = DripSwapEngine.run(player, stack, dripNullItem.tier(), -1);
        stand.markStoredDripNullChanged();
        player.displayClientMessage(result.message(), true);
    }

    private static void removeStored(Level level, DripStandBlockEntity stand, Player player) {
        if (level.isClientSide) {
            stand.setStoredDripNullClient(ItemStack.EMPTY);
            return;
        }
        ItemStack stored = stand.removeStoredDripNull();
        if (!player.addItem(stored)) {
            player.drop(stored, false);
        }
    }
}
