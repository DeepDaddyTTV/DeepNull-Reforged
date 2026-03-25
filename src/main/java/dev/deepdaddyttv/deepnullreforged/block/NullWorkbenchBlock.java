package dev.deepdaddyttv.deepnullreforged.block;

import com.mojang.serialization.MapCodec;
import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class NullWorkbenchBlock extends BaseEntityBlock {
    public static final MapCodec<NullWorkbenchBlock> CODEC = simpleCodec(NullWorkbenchBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<NullWorkbenchPart> PART = EnumProperty.create("part", NullWorkbenchPart.class);
    private static final VoxelShape MAIN_NORTH_SHAPE = Shapes.or(
            box(0.0D, 11.0D, 1.0D, 16.0D, 12.0D, 13.0D),
            box(1.0D, 0.0D, 2.0D, 3.0D, 12.0D, 4.0D),
            box(1.0D, 0.0D, 10.0D, 3.0D, 12.0D, 12.0D),
            box(1.0D, 12.0D, 3.0D, 9.0D, 13.0D, 11.0D)
    );
    private static final VoxelShape EXTENSION_NORTH_SHAPE = Shapes.or(
            box(0.0D, 11.0D, 1.0D, 16.0D, 12.0D, 13.0D),
            box(13.0D, 0.0D, 2.0D, 15.0D, 12.0D, 4.0D),
            box(13.0D, 0.0D, 10.0D, 15.0D, 12.0D, 12.0D)
    );
    private static final Map<Direction, VoxelShape> MAIN_SHAPES = createShapes(MAIN_NORTH_SHAPE);
    private static final Map<Direction, VoxelShape> EXTENSION_SHAPES = createShapes(EXTENSION_NORTH_SHAPE);

    public NullWorkbenchBlock() {
        this(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(5.0F, 6.0F).noOcclusion());
    }

    private NullWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, NullWorkbenchPart.MAIN));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos extensionPos = context.getClickedPos().relative(extensionDirection(facing));
        Level level = context.getLevel();
        return level.getBlockState(extensionPos).canBeReplaced(context)
                ? defaultBlockState().setValue(FACING, facing).setValue(PART, NullWorkbenchPart.MAIN)
                : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) {
            return;
        }
        BlockPos extensionPos = pos.relative(extensionDirection(state.getValue(FACING)));
        level.setBlock(extensionPos, state.setValue(PART, NullWorkbenchPart.EXTENSION), 3);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING).getOpposite();
        return state.getValue(PART) == NullWorkbenchPart.MAIN
                ? MAIN_SHAPES.getOrDefault(facing, MAIN_NORTH_SHAPE)
                : EXTENSION_SHAPES.getOrDefault(facing, EXTENSION_NORTH_SHAPE);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness == -1.0F) {
            return 0.0F;
        }
        float destroySpeed = player.getDestroySpeed(state);
        boolean pickaxeLikeTool = player.getMainHandItem().canPerformAction(ItemAbilities.PICKAXE_DIG);
        if (pickaxeLikeTool && destroySpeed > 1.0F) {
            return destroySpeed / hardness / 15.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        BlockPos mainPos = resolveMainPos(state, pos);
        if (!(level.getBlockEntity(mainPos) instanceof NullWorkbenchBlockEntity workbench)) {
            return InteractionResult.PASS;
        }
        MenuProvider provider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("container.deepnullreforged.null_workbench");
            }

            @Override
            public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                return new NullWorkbenchMenu(containerId, inventory, workbench);
            }
        };
        serverPlayer.openMenu(provider, buf -> buf.writeBlockPos(mainPos));
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
            return;
        }

        BlockPos mainPos = resolveMainPos(state, pos);
        BlockPos extensionPos = mainPos.relative(extensionDirection(state.getValue(FACING)));
        boolean removingMain = pos.equals(mainPos);

        if (removingMain && level.getBlockEntity(mainPos) instanceof NullWorkbenchBlockEntity workbench) {
            for (int slot = 0; slot < workbench.getItemHandler().getSlots(); slot++) {
                popResource(level, mainPos, workbench.getItemHandler().getStackInSlot(slot));
            }
        }

        if (removingMain) {
            if (!extensionPos.equals(pos) && level.getBlockState(extensionPos).is(this)) {
                level.removeBlock(extensionPos, false);
            }
        } else if (!mainPos.equals(pos) && level.getBlockState(mainPos).is(this)) {
            level.removeBlock(mainPos, false);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == NullWorkbenchPart.MAIN ? new NullWorkbenchBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (state.getValue(PART) != NullWorkbenchPart.MAIN) {
            return null;
        }
        return createTickerHelper(blockEntityType, ModBlockEntities.NULL_WORKBENCH.get(), NullWorkbenchBlockEntity::serverTick);
    }

    private static Direction extensionDirection(Direction facing) {
        return facing.getCounterClockWise();
    }

    private static Map<Direction, VoxelShape> createShapes(VoxelShape northShape) {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.NORTH, northShape);
        shapes.put(Direction.EAST, rotateShape(northShape, 1));
        shapes.put(Direction.SOUTH, rotateShape(northShape, 2));
        shapes.put(Direction.WEST, rotateShape(northShape, 3));
        return shapes;
    }

    private static VoxelShape rotateShape(VoxelShape shape, int turns) {
        VoxelShape rotated = shape;
        for (int i = 0; i < turns; i++) {
            VoxelShape source = rotated;
            final VoxelShape[] buffer = new VoxelShape[]{Shapes.empty()};
            source.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[0] = Shapes.or(buffer[0], box(
                            16.0D - (maxZ * 16.0D),
                            minY * 16.0D,
                            minX * 16.0D,
                            16.0D - (minZ * 16.0D),
                            maxY * 16.0D,
                            maxX * 16.0D
                    )));
            rotated = buffer[0];
        }
        return rotated;
    }

    private static BlockPos resolveMainPos(BlockState state, BlockPos pos) {
        return state.getValue(PART) == NullWorkbenchPart.MAIN
                ? pos
                : pos.relative(extensionDirection(state.getValue(FACING)).getOpposite());
    }
}
