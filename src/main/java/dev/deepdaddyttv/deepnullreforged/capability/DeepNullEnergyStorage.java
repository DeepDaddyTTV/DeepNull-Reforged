package dev.deepdaddyttv.deepnullreforged.capability;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class DeepNullEnergyStorage implements IEnergyStorage {
    private final Supplier<@Nullable DeepNullInventory> inventorySupplier;

    public DeepNullEnergyStorage(DeepNullInventory inventory) {
        this(() -> inventory);
    }

    public DeepNullEnergyStorage(Supplier<@Nullable DeepNullInventory> inventorySupplier) {
        this.inventorySupplier = inventorySupplier;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        DeepNullInventory inventory = currentInventory();
        if (inventory == null) {
            return 0;
        }
        return inventory.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        DeepNullInventory inventory = currentInventory();
        if (inventory == null) {
            return 0;
        }
        return inventory.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        DeepNullInventory inventory = currentInventory();
        return inventory == null ? 0 : inventory.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        DeepNullInventory inventory = currentInventory();
        return inventory == null ? 0 : inventory.getEnergyCapacity();
    }

    @Override
    public boolean canExtract() {
        DeepNullInventory inventory = currentInventory();
        if (inventory == null) {
            return false;
        }
        return inventory.hasEnergyUpgrade() && inventory.getEnergyTransferRate() > 0;
    }

    @Override
    public boolean canReceive() {
        DeepNullInventory inventory = currentInventory();
        if (inventory == null) {
            return false;
        }
        return inventory.hasEnergyUpgrade() && inventory.getEnergyTransferRate() > 0;
    }

    private @Nullable DeepNullInventory currentInventory() {
        DeepNullInventory inventory = inventorySupplier.get();
        return inventory != null && inventory.hasEnergyUpgrade() ? inventory : null;
    }
}
