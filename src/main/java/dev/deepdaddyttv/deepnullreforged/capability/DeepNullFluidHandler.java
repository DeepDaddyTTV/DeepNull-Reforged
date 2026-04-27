package dev.deepdaddyttv.deepnullreforged.capability;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.minecraft.world.item.ItemStack;
import dev.deepdaddyttv.deepnullreforged.compat.fluids.FluidStack;
import dev.deepdaddyttv.deepnullreforged.compat.fluids.capability.IFluidHandler;
import dev.deepdaddyttv.deepnullreforged.compat.fluids.capability.IFluidHandlerItem;

public final class DeepNullFluidHandler implements IFluidHandlerItem {
    private final DeepNullInventory inventory;
    private final ItemStack container;
    private final int fixedTank;
    private final boolean singleSelectedTankView;

    public DeepNullFluidHandler(DeepNullInventory inventory, ItemStack container) {
        this(inventory, container, -1, false);
    }

    public DeepNullFluidHandler(DeepNullInventory inventory, ItemStack container, int fixedTank) {
        this(inventory, container, fixedTank, false);
    }

    public DeepNullFluidHandler(DeepNullInventory inventory, ItemStack container, boolean singleSelectedTankView) {
        this(inventory, container, -1, singleSelectedTankView);
    }

    public DeepNullFluidHandler(DeepNullInventory inventory, ItemStack container, int fixedTank, boolean singleSelectedTankView) {
        this.inventory = inventory;
        this.container = container;
        this.fixedTank = fixedTank;
        this.singleSelectedTankView = singleSelectedTankView;
    }

    @Override
    public int getTanks() {
        if (fixedTank >= 0) {
            return inventory.supportsFluidStorage() ? 1 : 0;
        }
        if (singleSelectedTankView) {
            return inventory.supportsFluidStorage() ? 1 : 0;
        }
        return inventory.supportsFluidStorage() ? inventory.getFluidSlotCount() : 0;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (fixedTank >= 0) {
            return tank == 0 ? inventory.getFluidInSlot(fixedTank) : FluidStack.EMPTY;
        }
        if (singleSelectedTankView) {
            int selectedTank = selectedTank();
            return tank == 0 && selectedTank >= 0 ? inventory.getFluidInSlot(selectedTank) : FluidStack.EMPTY;
        }
        return tank >= 0 && tank < inventory.getFluidSlotCount() ? inventory.getFluidInSlot(tank) : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        if (fixedTank >= 0) {
            return tank == 0 ? inventory.getFluidCapacity() : 0;
        }
        if (singleSelectedTankView) {
            return tank == 0 && inventory.supportsFluidStorage() ? inventory.getFluidCapacity() : 0;
        }
        return tank >= 0 && tank < inventory.getFluidSlotCount() ? inventory.getFluidCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (fixedTank >= 0) {
            return tank == 0 && inventory.acceptsNormalFluids() && !stack.isEmpty();
        }
        if (singleSelectedTankView) {
            return tank == 0 && inventory.acceptsNormalFluids() && !stack.isEmpty();
        }
        return tank >= 0 && tank < inventory.getFluidSlotCount() && inventory.acceptsNormalFluids() && !stack.isEmpty();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (fixedTank >= 0) {
            return inventory.fillFluid(fixedTank, resource, action.simulate());
        }
        if (singleSelectedTankView) {
            int selectedTank = selectedTank();
            return selectedTank >= 0 ? inventory.fillFluid(selectedTank, resource, action.simulate()) : 0;
        }
        return inventory.fillFluid(resource, action.simulate());
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (fixedTank >= 0) {
            FluidStack existing = inventory.getFluidInSlot(fixedTank);
            if (existing.isEmpty() || !FluidStack.isSameFluidSameComponents(existing, resource)) {
                return FluidStack.EMPTY;
            }
            return inventory.drainFluid(fixedTank, resource.getAmount(), action.simulate());
        }
        if (singleSelectedTankView) {
            int selectedTank = selectedTank();
            if (selectedTank < 0) {
                return FluidStack.EMPTY;
            }
            FluidStack existing = inventory.getFluidInSlot(selectedTank);
            if (existing.isEmpty() || !FluidStack.isSameFluidSameComponents(existing, resource)) {
                return FluidStack.EMPTY;
            }
            return inventory.drainFluid(selectedTank, resource.getAmount(), action.simulate());
        }
        return inventory.drainFluid(resource, action.simulate());
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (fixedTank >= 0) {
            return inventory.drainFluid(fixedTank, maxDrain, action.simulate());
        }
        if (singleSelectedTankView) {
            int selectedTank = selectedTank();
            return selectedTank >= 0 ? inventory.drainFluid(selectedTank, maxDrain, action.simulate()) : FluidStack.EMPTY;
        }
        return inventory.drainFluid(maxDrain, action.simulate());
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    private int selectedTank() {
        if (!inventory.supportsFluidStorage()) {
            return -1;
        }
        int selectedSlot = inventory.getSelectedSlot();
        if (selectedSlot >= 0 && selectedSlot < inventory.getFluidSlotCount()) {
            return selectedSlot;
        }
        return inventory.getFluidSlotCount() > 0 ? 0 : -1;
    }
}
