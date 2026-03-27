package net.neoforged.neoforge.fluids;

import dev.deepdaddyttv.deepnullreforged.registry.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.Optional;

public final class FluidUtil {
    private FluidUtil() {
    }

    public static Optional<IFluidHandlerItem> getFluidHandler(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        if (stack.getItem() instanceof BucketItem || stack.is(Items.BUCKET)) {
            return Optional.of(new BucketFluidHandler(stack));
        }
        return Optional.empty();
    }

    public static Optional<IFluidHandler> getFluidHandler(Level level, BlockPos pos, Direction side) {
        return Optional.ofNullable(ModCapabilities.getBlockFluidHandler(level, pos, side));
    }

    public static Optional<FluidStack> getFluidContained(ItemStack stack) {
        return getFluidHandler(stack)
                .map(handler -> handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE))
                .filter(fluidStack -> !fluidStack.isEmpty());
    }

    public static FluidStack tryFluidTransfer(IFluidHandler destination, IFluidHandler source, int maxAmount, boolean doTransfer) {
        IFluidHandler.FluidAction action = doTransfer ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE;
        FluidStack simulated = source.drain(maxAmount, IFluidHandler.FluidAction.SIMULATE);
        if (simulated.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int accepted = destination.fill(simulated.copy(), IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) {
            return FluidStack.EMPTY;
        }
        FluidStack requested = simulated.copyWithAmount(Math.min(simulated.getAmount(), accepted));
        if (!doTransfer) {
            return requested;
        }
        FluidStack drained = source.drain(requested, action);
        if (drained.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int filled = destination.fill(drained.copy(), action);
        return filled <= 0 ? FluidStack.EMPTY : drained.copyWithAmount(filled);
    }

    public static boolean tryPlaceFluid(Player player, Level level, InteractionHand hand, BlockPos pos, IFluidHandler sourceHandler, FluidStack resource) {
        if (resource.isEmpty() || resource.getAmount() < FluidType.BUCKET_VOLUME) {
            return false;
        }

        Fluid fluid = resource.getFluid();
        if (fluid == Fluids.EMPTY) {
            return false;
        }

        FluidStack drained = sourceHandler.drain(resource.copyWithAmount(FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty() || drained.getAmount() < FluidType.BUCKET_VOLUME) {
            return false;
        }

        Item bucketItem = fluid.getBucket();
        if (bucketItem instanceof BucketItem bucket) {
            ItemStack bucketStack = new ItemStack(bucketItem);
            if (bucket.emptyContents(player, level, pos, null)) {
                bucket.checkExtraContent(player, level, bucketStack, pos);
                sourceHandler.drain(drained.copyWithAmount(FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
                return true;
            }
            return false;
        }

        if (!(fluid instanceof FlowingFluid flowingFluid)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        boolean mayReplace = state.canBeReplaced(fluid);
        boolean canPlaceLiquid = state.getBlock() instanceof LiquidBlockContainer container
                && container.canPlaceLiquid(player, level, pos, state, fluid);
        if (!state.isAir() && !mayReplace && !canPlaceLiquid) {
            return false;
        }

        if (canPlaceLiquid) {
            ((LiquidBlockContainer) state.getBlock()).placeLiquid(level, pos, state, flowingFluid.getSource(false));
            sourceHandler.drain(drained.copyWithAmount(FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
            return true;
        }

        if (!level.isClientSide && mayReplace && !state.liquid()) {
            level.destroyBlock(pos, true);
        }

        if (!level.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), 11) && !state.getFluidState().isSource()) {
            return false;
        }

        sourceHandler.drain(drained.copyWithAmount(FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    public static FluidActionResult tryPickUpFluid(ItemStack container, Player player, Level level, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BucketPickup bucketPickup)) {
            return FluidActionResult.fail();
        }

        ItemStack pickedUp = bucketPickup.pickupBlock(player, level, pos, state);
        if (pickedUp.isEmpty()) {
            return FluidActionResult.fail();
        }

        return FluidActionResult.success(pickedUp);
    }

    private static final class BucketFluidHandler implements IFluidHandlerItem {
        private ItemStack container;

        private BucketFluidHandler(ItemStack container) {
            this.container = container;
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
            return bucketFluid(container);
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? FluidType.BUCKET_VOLUME : 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && !stack.isEmpty() && container.is(Items.BUCKET) && bucketItemFor(stack.getFluid()) != Items.AIR;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!container.is(Items.BUCKET) || resource.isEmpty() || resource.getAmount() < FluidType.BUCKET_VOLUME) {
                return 0;
            }

            Item filledBucket = bucketItemFor(resource.getFluid());
            if (filledBucket == Items.AIR) {
                return 0;
            }

            if (!action.simulate()) {
                container = new ItemStack(filledBucket);
            }
            return FluidType.BUCKET_VOLUME;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack contained = bucketFluid(container);
            if (contained.isEmpty() || !FluidStack.isSameFluidSameComponents(contained, resource)) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack contained = bucketFluid(container);
            if (contained.isEmpty() || maxDrain < FluidType.BUCKET_VOLUME) {
                return FluidStack.EMPTY;
            }
            if (!action.simulate()) {
                container = new ItemStack(Items.BUCKET);
            }
            return contained;
        }

        @Override
        public ItemStack getContainer() {
            return container;
        }

        private static FluidStack bucketFluid(ItemStack stack) {
            if (!(stack.getItem() instanceof BucketItem bucketItem)) {
                return FluidStack.EMPTY;
            }
            for (Fluid fluid : BuiltInRegistries.FLUID) {
                if (fluid != Fluids.EMPTY && fluid.getBucket() == bucketItem) {
                    return new FluidStack(fluid, FluidType.BUCKET_VOLUME);
                }
            }
            return FluidStack.EMPTY;
        }

        private static Item bucketItemFor(Fluid fluid) {
            Item bucket = fluid.getBucket();
            return bucket == null ? Items.AIR : bucket;
        }
    }
}
