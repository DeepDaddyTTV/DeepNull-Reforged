package dev.deepdaddyttv.deepnullreforged.block;

import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class DeepNullDockBlock extends ContainerBlock {
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 4, 16);

    public DeepNullDockBlock(AbstractBlock.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader world) { return new DeepNullDockBlockEntity(); }

    @Override
    public BlockRenderType getRenderShape(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return SHAPE; }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity tile = world.getBlockEntity(pos);
        if (!(tile instanceof DeepNullDockBlockEntity)) return ActionResultType.PASS;
        DeepNullDockBlockEntity dock = (DeepNullDockBlockEntity) tile;
        ItemStack held = player.getItemInHand(hand);
        if ((held.getItem() instanceof DeepNullItem || held.getItem() instanceof DampNullItem) && dock.getNull().isEmpty()) {
            if (!world.isClientSide) dock.insertNull(held);
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        if (held.isEmpty() && !dock.getNull().isEmpty()) {
            if (!world.isClientSide) {
                ItemStack removed = dock.removeNull();
                if (!player.addItem(removed)) player.drop(removed, false);
            }
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onRemove(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean moving) {
        if (oldState.getBlock() != newState.getBlock()) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof DeepNullDockBlockEntity) {
                ItemStack stored = ((DeepNullDockBlockEntity) tile).removeNull();
                if (!stored.isEmpty()) popResource(world, pos, stored);
            }
        }
        super.onRemove(oldState, world, pos, newState, moving);
    }
}
