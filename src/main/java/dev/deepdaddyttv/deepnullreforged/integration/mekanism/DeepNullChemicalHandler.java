package dev.deepdaddyttv.deepnullreforged.integration.mekanism;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.world.item.ItemStack;

public final class DeepNullChemicalHandler implements IChemicalHandler {
    private final DeepNullInventory inventory;
    private final int fixedTank;

    public DeepNullChemicalHandler(DeepNullInventory inventory, ItemStack container) {
        this(inventory, container, -1);
    }

    public DeepNullChemicalHandler(DeepNullInventory inventory, ItemStack container, int fixedTank) {
        this.inventory = inventory;
        this.fixedTank = fixedTank;
    }

    @Override
    public int getChemicalTanks() {
        return fixedTank >= 0 ? 1 : inventory.getFluidSlotCount();
    }

    @Override
    public ChemicalStack getChemicalInTank(int tank) {
        int actualTank = actualTank(tank);
        if (actualTank < 0) {
            return ChemicalStack.EMPTY;
        }
        return MekanismCompat.toChemicalStack(inventory, inventory.getChemicalInSlot(actualTank));
    }

    @Override
    public void setChemicalInTank(int tank, ChemicalStack stack) {
        int actualTank = actualTank(tank);
        if (actualTank < 0) {
            return;
        }
        if (stack.isEmpty()) {
            inventory.clearFluidSlot(actualTank);
            return;
        }
        inventory.setChemicalInSlot(actualTank, MekanismCompat.fromChemicalStack(stack));
    }

    @Override
    public long getChemicalTankCapacity(int tank) {
        int actualTank = actualTank(tank);
        return actualTank < 0 ? 0L : inventory.getFluidCapacity();
    }

    @Override
    public boolean isValid(int tank, ChemicalStack stack) {
        int actualTank = actualTank(tank);
        if (actualTank < 0 || stack.isEmpty()) {
            return false;
        }
        return !inventory.hasFluidInSlot(actualTank);
    }

    @Override
    public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
        int actualTank = actualTank(tank);
        if (actualTank < 0 || stack.isEmpty()) {
            return stack;
        }

        int inserted = inventory.fillChemical(actualTank, MekanismCompat.fromChemicalStack(stack), action.simulate());
        if (inserted <= 0) {
            return stack;
        }
        return stack.copyWithAmount(stack.getAmount() - inserted);
    }

    @Override
    public ChemicalStack insertChemical(ChemicalStack stack, Action action) {
        if (stack.isEmpty()) {
            return stack;
        }

        int actualTank = fixedTank >= 0
                ? fixedTank
                : inventory.findChemicalInsertSlot(MekanismCompat.fromChemicalStack(stack));
        if (actualTank < 0) {
            return stack;
        }

        return insertChemical(fixedTank >= 0 ? 0 : actualTank, stack, action);
    }

    @Override
    public ChemicalStack extractChemical(int tank, long amount, Action action) {
        int actualTank = actualTank(tank);
        if (actualTank < 0 || amount <= 0L) {
            return ChemicalStack.EMPTY;
        }
        StoredChemical drained = inventory.drainChemical(actualTank, amount, action.simulate());
        return MekanismCompat.toChemicalStack(inventory, drained);
    }

    private int actualTank(int tank) {
        if (fixedTank >= 0) {
            return tank == 0 ? fixedTank : -1;
        }
        return tank >= 0 && tank < inventory.getFluidSlotCount() ? tank : -1;
    }
}
