package dev.deepdaddyttv.deepnullreforged.compat.fluids.capability;

import dev.deepdaddyttv.deepnullreforged.compat.fluids.FluidStack;

public interface IFluidHandler {
    enum FluidAction {
        EXECUTE,
        SIMULATE;

        public boolean simulate() {
            return this == SIMULATE;
        }
    }

    int getTanks();

    FluidStack getFluidInTank(int tank);

    int getTankCapacity(int tank);

    boolean isFluidValid(int tank, FluidStack stack);

    int fill(FluidStack resource, FluidAction action);

    FluidStack drain(FluidStack resource, FluidAction action);

    FluidStack drain(int maxDrain, FluidAction action);
}
