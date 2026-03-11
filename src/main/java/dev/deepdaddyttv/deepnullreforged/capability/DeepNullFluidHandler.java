package dev.deepdaddyttv.deepnullreforged.capability;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public final class DeepNullFluidHandler implements IFluidHandlerItem {
    private final DeepNullInventory inventory;
    private final ItemStack container;
    private final int fixedTank;

    public DeepNullFluidHandler(DeepNullInventory inventory, ItemStack container) {
        this(inventory, container, -1);
    }

    public DeepNullFluidHandler(DeepNullInventory inventory, ItemStack container, int fixedTank) {
        this.inventory = inventory;
        this.container = container;
        this.fixedTank = fixedTank;
    }

    @Override
    public int getTanks() {
        if (fixedTank >= 0) {
            return inventory.hasFluidUpgrade() ? 1 : 0;
        }
        return inventory.hasFluidUpgrade() ? inventory.getFluidSlotCount() : 0;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (fixedTank >= 0) {
            return tank == 0 ? inventory.getFluidInSlot(fixedTank) : FluidStack.EMPTY;
        }
        return tank >= 0 && tank < inventory.getFluidSlotCount() ? inventory.getFluidInSlot(tank) : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        if (fixedTank >= 0) {
            return tank == 0 ? inventory.getFluidCapacity() : 0;
        }
        return tank >= 0 && tank < inventory.getFluidSlotCount() ? inventory.getFluidCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (fixedTank >= 0) {
            return tank == 0 && inventory.hasFluidUpgrade() && !stack.isEmpty();
        }
        return tank >= 0 && tank < inventory.getFluidSlotCount() && inventory.hasFluidUpgrade() && !stack.isEmpty();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (fixedTank >= 0) {
            return inventory.fillFluid(fixedTank, resource, action.simulate());
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
        return inventory.drainFluid(resource, action.simulate());
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (fixedTank >= 0) {
            return inventory.drainFluid(fixedTank, maxDrain, action.simulate());
        }
        return inventory.drainFluid(maxDrain, action.simulate());
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }
}
