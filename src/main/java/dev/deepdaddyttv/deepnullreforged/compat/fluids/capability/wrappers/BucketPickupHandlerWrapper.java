package dev.deepdaddyttv.deepnullreforged.compat.fluids.capability.wrappers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import dev.deepdaddyttv.deepnullreforged.compat.fluids.FluidStack;
import dev.deepdaddyttv.deepnullreforged.compat.fluids.FluidType;
import dev.deepdaddyttv.deepnullreforged.compat.fluids.FluidUtil;
import dev.deepdaddyttv.deepnullreforged.compat.fluids.capability.IFluidHandler;

public final class BucketPickupHandlerWrapper implements IFluidHandler {
    private final Player player;
    private final BucketPickup bucketPickup;
    private final Level level;
    private final BlockPos pos;

    public BucketPickupHandlerWrapper(Player player, BucketPickup bucketPickup, Level level, BlockPos pos) {
        this.player = player;
        this.bucketPickup = bucketPickup;
        this.level = level;
        this.pos = pos;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank != 0) {
            return FluidStack.EMPTY;
        }

        var fluidState = level.getFluidState(pos);
        return fluidState.isEmpty()
                ? FluidStack.EMPTY
                : new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? FluidType.BUCKET_VOLUME : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack contained = getFluidInTank(0);
        if (contained.isEmpty() || !FluidStack.isSameFluidSameComponents(contained, resource)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack contained = getFluidInTank(0);
        if (contained.isEmpty() || maxDrain < FluidType.BUCKET_VOLUME) {
            return FluidStack.EMPTY;
        }

        if (action.simulate()) {
            return contained;
        }

        ItemStack pickedUp = bucketPickup.pickupBlock(player, level, pos, level.getBlockState(pos));
        return pickedUp.isEmpty()
                ? FluidStack.EMPTY
                : FluidUtil.getFluidContained(pickedUp).orElse(contained);
    }
}
