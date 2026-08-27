package dev.deepdaddyttv.deepnullreforged.capability;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullData;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

public class DeepNullEnergyStorage implements IEnergyStorage {
    private static final String ENERGY = "DeepNullEnergy";
    private final ItemStack owner;
    private final DeepNullTier tier;

    public DeepNullEnergyStorage(ItemStack owner, DeepNullTier tier) {
        this.owner = owner;
        this.tier = tier;
    }

    private int capacity() {
        if (tier.creative()) return Integer.MAX_VALUE;
        if (tier == DeepNullTier.EMERALD && DeepNullData.hasUpgrade(owner, "deep_energy_upgrade")) return 25000000;
        if (tier == DeepNullTier.EMERALD) return 1000000;
        if (tier == DeepNullTier.DIAMOND) return 100000;
        return 0;
    }

    private int transfer() {
        if (tier.creative()) return Integer.MAX_VALUE;
        if (tier == DeepNullTier.EMERALD && DeepNullData.hasUpgrade(owner, "deep_energy_upgrade")) return 100000;
        if (tier == DeepNullTier.EMERALD) return 20000;
        if (tier == DeepNullTier.DIAMOND) return 2000;
        return 0;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!DeepNullData.hasUpgrade(owner, "energy_upgrade")) return 0;
        if (tier.creative()) return maxReceive;
        int received = Math.min(transfer(), Math.min(maxReceive, capacity() - getEnergyStored()));
        if (!simulate && received > 0) owner.getOrCreateTag().putInt(ENERGY, getEnergyStored() + received);
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!DeepNullData.hasUpgrade(owner, "energy_upgrade")) return 0;
        if (tier.creative()) return maxExtract;
        int extracted = Math.min(transfer(), Math.min(maxExtract, getEnergyStored()));
        if (!simulate && extracted > 0) owner.getOrCreateTag().putInt(ENERGY, getEnergyStored() - extracted);
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        if (tier.creative() && DeepNullData.hasUpgrade(owner, "energy_upgrade")) return Integer.MAX_VALUE;
        return Math.min(owner.getOrCreateTag().getInt(ENERGY), capacity());
    }

    @Override
    public int getMaxEnergyStored() {
        return DeepNullData.hasUpgrade(owner, "energy_upgrade") ? capacity() : 0;
    }

    @Override
    public boolean canExtract() { return true; }

    @Override
    public boolean canReceive() { return true; }
}
