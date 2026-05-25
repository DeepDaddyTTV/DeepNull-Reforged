package dev.deepdaddyttv.deepnullreforged.inventory;

import dev.deepdaddyttv.deepnullreforged.capability.DeepNullFluidHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

public final class DampNullFluidContainerTransfer {
    private DampNullFluidContainerTransfer() {
    }

    public static @Nullable ItemStack transferSingleContainer(
            DeepNullInventory inventory,
            ItemStack heldStack,
            int tankSlot,
            boolean allowFallbackSlot,
            boolean simulate
    ) {
        return transferSingleContainerInternal(inventory, heldStack, tankSlot, allowFallbackSlot, simulate);
    }

    public static boolean containsTransferableFluid(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        ItemStack working = stack.copyWithCount(1);
        IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(working).orElse(null);
        FluidStack contained = itemHandler == null
                ? FluidStack.EMPTY
                : FluidUtil.getFluidContained(working).orElseGet(() -> firstFluidIn(itemHandler));
        if (!contained.isEmpty()) {
            return true;
        }
        return working.getItem() instanceof BucketItem bucketItem && bucketItem.content != Fluids.EMPTY;
    }

    public static @Nullable CarriedTransfer transferCarriedContainer(
            Player player,
            DeepNullInventory inventory,
            ItemStack carriedStack,
            int tankSlot
    ) {
        return transferCarriedContainer(player, inventory, carriedStack, tankSlot, false);
    }

    public static @Nullable CarriedTransfer transferCarriedContainer(
            Player player,
            DeepNullInventory inventory,
            ItemStack carriedStack,
            int tankSlot,
            boolean allowFallbackSlot
    ) {
        if (carriedStack.isEmpty()) {
            return null;
        }

        if (carriedStack.getCount() == 1) {
            ItemStack actualResult = transferSingleContainerInternal(inventory, carriedStack, tankSlot, allowFallbackSlot, false);
            return actualResult == null ? null : new CarriedTransfer(actualResult);
        }

        ItemStack previewResult = previewSingleContainer(inventory, carriedStack, tankSlot, allowFallbackSlot);
        if (previewResult == null) {
            return null;
        }

        ItemStack remainingCarried = carriedStack.copy();
        remainingCarried.shrink(1);
        boolean previewMergesIntoCursor = canMergeInto(remainingCarried, previewResult);
        if (!previewMergesIntoCursor && !previewResult.isEmpty() && !canPlaceInInventory(player, previewResult)) {
            return null;
        }

        ItemStack actualResult = transferSingleContainerInternal(inventory, carriedStack, tankSlot, allowFallbackSlot, false);
        if (actualResult == null) {
            return null;
        }

        if (canMergeInto(remainingCarried, actualResult)) {
            remainingCarried.grow(actualResult.getCount());
        } else if (!actualResult.isEmpty()) {
            player.getInventory().placeItemBackInInventory(actualResult);
        }
        return new CarriedTransfer(remainingCarried);
    }

    private static @Nullable ItemStack previewSingleContainer(
            DeepNullInventory inventory,
            ItemStack heldStack,
            int tankSlot,
            boolean allowFallbackSlot
    ) {
        return transferSingleContainerInternal(inventory, heldStack, tankSlot, allowFallbackSlot, true);
    }

    private static @Nullable ItemStack transferSingleContainerInternal(
            DeepNullInventory inventory,
            ItemStack heldStack,
            int tankSlot,
            boolean allowFallbackSlot,
            boolean simulate
    ) {
        if (!inventory.supportsFluidStorage() || heldStack.isEmpty()) {
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
            return depositContainer(inventory, heldStack, itemHandler, contained, rawBucket, tankSlot, allowFallbackSlot, simulate);
        }

        if (working.is(Items.BUCKET)) {
            return withdrawToEmptyBucket(inventory, tankSlot, allowFallbackSlot, simulate);
        }

        if (itemHandler == null) {
            return null;
        }
        return withdrawToContainer(inventory, heldStack, itemHandler, tankSlot, allowFallbackSlot, simulate);
    }

    private static @Nullable ItemStack depositContainer(
            DeepNullInventory inventory,
            ItemStack heldStack,
            @Nullable IFluidHandlerItem itemHandler,
            FluidStack contained,
            boolean rawBucket,
            int tankSlot,
            boolean allowFallbackSlot,
            boolean simulate
    ) {
        int targetSlot = resolveDepositSlot(inventory, contained, tankSlot, allowFallbackSlot);
        if (targetSlot < 0) {
            return null;
        }

        if (rawBucket) {
            if (inventory.fillFluid(targetSlot, contained, simulate) != contained.getAmount()) {
                return null;
            }
        } else {
            if (itemHandler == null) {
                return null;
            }
            DeepNullFluidHandler targetHandler = new DeepNullFluidHandler(inventory, ItemStack.EMPTY, targetSlot);
            FluidStack transferred = FluidUtil.tryFluidTransfer(targetHandler, itemHandler, contained.getAmount(), !simulate);
            if (transferred.isEmpty()) {
                return null;
            }
        }

        if (!simulate && inventory.getSelectedSlot() != targetSlot) {
            inventory.setSelectedSlot(targetSlot);
        }
        return rawBucket ? new ItemStack(Items.BUCKET) : itemHandler.getContainer();
    }

    private static @Nullable ItemStack withdrawToEmptyBucket(
            DeepNullInventory inventory,
            int tankSlot,
            boolean allowFallbackSlot,
            boolean simulate
    ) {
        int sourceSlot = resolveSourceSlot(inventory, tankSlot, allowFallbackSlot);
        if (sourceSlot < 0) {
            return null;
        }

        FluidStack storedFluid = inventory.getFluidInSlot(sourceSlot);
        if (storedFluid.isEmpty() || storedFluid.getAmount() < FluidType.BUCKET_VOLUME) {
            return null;
        }

        ItemStack filledBucket = new ItemStack(storedFluid.getFluid().getBucket());
        if (filledBucket.isEmpty()) {
            return null;
        }

        FluidStack drained = inventory.drainFluid(sourceSlot, FluidType.BUCKET_VOLUME, simulate);
        if (drained.getAmount() != FluidType.BUCKET_VOLUME) {
            return null;
        }

        if (!simulate && inventory.getSelectedSlot() != sourceSlot) {
            inventory.setSelectedSlot(sourceSlot);
        }
        return filledBucket;
    }

    private static @Nullable ItemStack withdrawToContainer(
            DeepNullInventory inventory,
            ItemStack heldStack,
            IFluidHandlerItem itemHandler,
            int tankSlot,
            boolean allowFallbackSlot,
            boolean simulate
    ) {
        int sourceSlot = resolveSourceSlot(inventory, tankSlot, allowFallbackSlot);
        if (sourceSlot < 0) {
            return null;
        }

        FluidStack storedFluid = inventory.getFluidInSlot(sourceSlot);
        if (storedFluid.isEmpty()) {
            return null;
        }

        DeepNullFluidHandler sourceHandler = new DeepNullFluidHandler(inventory, ItemStack.EMPTY, sourceSlot);
        FluidStack transferred = FluidUtil.tryFluidTransfer(itemHandler, sourceHandler, storedFluid.getAmount(), !simulate);
        if (transferred.isEmpty()) {
            return null;
        }

        if (!simulate && inventory.getSelectedSlot() != sourceSlot) {
            inventory.setSelectedSlot(sourceSlot);
        }
        return itemHandler.getContainer();
    }

    private static int resolveDepositSlot(
            DeepNullInventory inventory,
            FluidStack contained,
            int tankSlot,
            boolean allowFallbackSlot
    ) {
        if (!inventory.acceptsNormalFluids()) {
            return -1;
        }
        if (tankSlot >= 0) {
            if (tankSlot >= inventory.getFluidSlotCount() || inventory.hasChemicalInSlot(tankSlot)) {
                return -1;
            }
            FluidStack existing = inventory.getFluidInSlot(tankSlot);
            return existing.isEmpty() || FluidStack.isSameFluidSameComponents(existing, contained) ? tankSlot : -1;
        }
        return allowFallbackSlot ? inventory.findFluidInsertSlot(contained) : -1;
    }

    private static int resolveSourceSlot(DeepNullInventory inventory, int tankSlot, boolean allowFallbackSlot) {
        if (tankSlot >= 0) {
            if (tankSlot >= inventory.getFluidSlotCount() || inventory.hasChemicalInSlot(tankSlot)) {
                return -1;
            }
            return inventory.getFluidInSlot(tankSlot).isEmpty() ? -1 : tankSlot;
        }

        if (!allowFallbackSlot) {
            return -1;
        }

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

    private static boolean canMergeInto(ItemStack target, ItemStack addition) {
        return !target.isEmpty()
                && !addition.isEmpty()
                && ItemStack.isSameItemSameComponents(target, addition)
                && target.getCount() + addition.getCount() <= target.getMaxStackSize();
    }

    private static boolean canPlaceInInventory(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        int remaining = stack.getCount();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack existing = player.getInventory().getItem(slot);
            if (existing.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameComponents(existing, stack)) {
                remaining -= Math.max(0, existing.getMaxStackSize() - existing.getCount());
                if (remaining <= 0) {
                    return true;
                }
            }
        }
        return false;
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

    public record CarriedTransfer(ItemStack carriedStack) {
    }
}
