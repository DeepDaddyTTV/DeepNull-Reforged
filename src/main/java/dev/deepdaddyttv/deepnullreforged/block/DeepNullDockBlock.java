package dev.deepdaddyttv.deepnullreforged.block;

import com.mojang.serialization.MapCodec;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenuOpener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

public class DeepNullDockBlock extends BaseEntityBlock {
    public static final MapCodec<DeepNullDockBlock> CODEC = simpleCodec(DeepNullDockBlock::new);
    private static final VoxelShape EMPTY_SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);
    private static final VoxelShape FILLED_SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    private static final VoxelShape FULL_SUPPORT_SHAPE = Shapes.block();

    public DeepNullDockBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(DeepNullReforged.MODID, "deepnull_dock")))
                .mapColor(MapColor.METAL)
                .strength(5.0F, 6.0F)
                .noOcclusion();
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
        float destroySpeed = player.getDestroySpeed(state);
        boolean pickaxeLikeTool = player.getMainHandItem().canPerformAction(ItemAbility.get("pickaxe_dig"));
        if (pickaxeLikeTool && destroySpeed > 1.0F) {
            return destroySpeed / hardness / 15.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof DeepNullDockBlockEntity dock)) {
            return InteractionResult.PASS;
        }

        if (!dock.hasStoredDeepNull() && stack.getItem() instanceof DeepNullItem) {
            if (level.isClientSide()) {
                dock.setStoredDeepNullClient(stack);
            } else {
                dock.setStoredDeepNull(stack.copy());
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }

        if (dock.hasStoredDeepNull() && player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                dock.setStoredDeepNullClient(ItemStack.EMPTY);
            } else {
                ItemStack stored = dock.removeStoredDeepNull();
                if (!player.addItem(stored)) {
                    player.drop(stored, false);
                }
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }

        if (dock.hasStoredDeepNull() && player instanceof ServerPlayer serverPlayer) {
            DeepNullMenuOpener.openDock(serverPlayer, dock);
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof DeepNullDockBlockEntity dock) || !dock.hasStoredDeepNull()) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                dock.setStoredDeepNullClient(ItemStack.EMPTY);
            } else {
                ItemStack stored = dock.removeStoredDeepNull();
                if (!player.addItem(stored)) {
                    player.drop(stored, false);
                }
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            DeepNullMenuOpener.openDock(serverPlayer, dock);
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeepNullDockBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities.DEEP_NULL_DOCK.get(), DeepNullDockBlockEntity::serverTick);
    }
}
