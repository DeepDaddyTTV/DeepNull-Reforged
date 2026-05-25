package dev.deepdaddyttv.deepnullreforged.capability;

import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullData;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.item.DumpNullItem;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class DumpNullEnergyStorage implements IEnergyStorage {
    private final DeepNullDockBlockEntity dock;

    public DumpNullEnergyStorage(DeepNullDockBlockEntity dock) {
        this.dock = dock;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        DumpNullData data = currentData();
        if (data == null) {
            return 0;
        }
        DumpNullData.EnergyMutation mutation = data.receiveEnergy(maxReceive, simulate);
        if (!simulate && mutation.amount() > 0) {
            DumpNullData.set(dock.getStoredDeepNull(), mutation.data());
            dock.markStoredDeepNullChanged(false);
        }
        return mutation.amount();
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        DumpNullData data = currentData();
        if (data == null) {
            return 0;
        }
        DumpNullData.EnergyMutation mutation = data.extractEnergy(maxExtract, simulate);
        if (!simulate && mutation.amount() > 0) {
            DumpNullData.set(dock.getStoredDeepNull(), mutation.data());
            dock.markStoredDeepNullChanged(false);
        }
        return mutation.amount();
    }

    @Override
    public int getEnergyStored() {
        DumpNullData data = currentData();
        return data == null ? 0 : data.storedEnergy();
    }

    @Override
    public int getMaxEnergyStored() {
        DumpNullData data = currentData();
        return data == null ? 0 : data.energyCapacity();
    }

    @Override
    public boolean canExtract() {
        DumpNullData data = currentData();
        return data != null && data.energyTransferRate() > 0;
    }

    @Override
    public boolean canReceive() {
        DumpNullData data = currentData();
        return data != null && data.energyTransferRate() > 0;
    }

    private DumpNullData currentData() {
        if (!(dock.getStoredDeepNull().getItem() instanceof DumpNullItem)) {
            return null;
        }
        DumpNullData data = DumpNullData.get(dock.getStoredDeepNull());
        return data.hasUpgrade(DumpNullUpgradeType.POWER) ? data : null;
    }
}
