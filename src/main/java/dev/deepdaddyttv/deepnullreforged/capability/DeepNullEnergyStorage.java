package dev.deepdaddyttv.deepnullreforged.capability;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class DeepNullEnergyStorage implements IEnergyStorage {
    private final DeepNullInventory inventory;

    public DeepNullEnergyStorage(DeepNullInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return inventory.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return inventory.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return inventory.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return inventory.getEnergyCapacity();
    }

    @Override
    public boolean canExtract() {
        return inventory.hasEnergyUpgrade() && inventory.getEnergyTransferRate() > 0;
    }

    @Override
    public boolean canReceive() {
        return inventory.hasEnergyUpgrade() && inventory.getEnergyTransferRate() > 0;
    }
}
