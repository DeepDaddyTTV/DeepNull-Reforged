package dev.deepdaddyttv.deepnullreforged.capability;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;

public class DampNullFluidHandler implements IFluidHandlerItem {
    private static final String TANKS = "DampNullTanks";
    private final ItemStack owner;
    private final DeepNullTier tier;
    private final FluidStack[] fluids;
    private CompoundNBT lastSaved;

    public DampNullFluidHandler(ItemStack owner, DeepNullTier tier) {
        this.owner = owner;
        this.tier = tier;
        this.fluids = new FluidStack[tier.tanks()];
        for (int i = 0; i < fluids.length; i++) fluids[i] = FluidStack.EMPTY;
        load();
    }

    @Nonnull
    @Override
    public ItemStack getContainer() { return owner; }

    @Override
    public int getTanks() { return fluids.length; }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        refresh();
        return validTank(tank) ? fluids[tank].copy() : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) { return tier.fluidCapacity(); }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return validTank(tank) && !stack.isEmpty(); }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        refresh();
        if (resource.isEmpty()) return 0;
        if (tier.creative()) {
            int tank = findMatching(resource);
            if (tank < 0) tank = findEmpty();
            if (tank < 0) return 0;
            if (action.execute() && fluids[tank].isEmpty()) {
                fluids[tank] = resource.copy();
                fluids[tank].setAmount(Math.min(1000, resource.getAmount()));
                save();
            }
            return resource.getAmount();
        }
        int remaining = resource.getAmount();
        // Top up matching tanks before using empty ones, including overflow.
        for (int pass = 0; pass < 2 && remaining > 0; pass++) {
            for (int tank = 0; tank < fluids.length && remaining > 0; tank++) {
                boolean empty = fluids[tank].isEmpty();
                if (pass == 0 ? empty || !fluids[tank].isFluidEqual(resource) : !empty) continue;
                int accepted = Math.min(remaining, Math.max(0, tier.fluidCapacity() - fluids[tank].getAmount()));
                if (accepted > 0 && action.execute()) {
                    if (empty) {
                        fluids[tank] = resource.copy();
                        fluids[tank].setAmount(accepted);
                    } else fluids[tank].grow(accepted);
                }
                remaining -= accepted;
            }
        }
        int accepted = resource.getAmount() - remaining;
        if (accepted > 0 && action.execute()) save();
        return accepted;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        refresh();
        if (resource.isEmpty()) return FluidStack.EMPTY;
        int remaining = resource.getAmount();
        for (int tank = 0; tank < fluids.length && remaining > 0; tank++) {
            if (!fluids[tank].isFluidEqual(resource)) continue;
            int drained = tier.creative() ? remaining : Math.min(remaining, fluids[tank].getAmount());
            remaining -= drained;
            if (action.execute() && !tier.creative()) {
                fluids[tank].shrink(drained);
                if (fluids[tank].isEmpty()) fluids[tank] = FluidStack.EMPTY;
            }
        }
        if (remaining == resource.getAmount()) return FluidStack.EMPTY;
        FluidStack result = resource.copy();
        result.setAmount(resource.getAmount() - remaining);
        if (action.execute() && !tier.creative()) save();
        return result;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        refresh();
        if (maxDrain <= 0) return FluidStack.EMPTY;
        for (FluidStack fluid : fluids) {
            if (fluid.isEmpty()) continue;
            FluidStack requested = fluid.copy();
            requested.setAmount(maxDrain);
            return drain(requested, action);
        }
        return FluidStack.EMPTY;
    }

    private int findMatching(FluidStack stack) {
        for (int i = 0; i < fluids.length; i++) if (fluids[i].isFluidEqual(stack)) return i;
        return -1;
    }

    private int findEmpty() {
        for (int i = 0; i < fluids.length; i++) if (fluids[i].isEmpty()) return i;
        return -1;
    }

    private boolean validTank(int tank) { return tank >= 0 && tank < fluids.length; }

    private void refresh() {
        if (owner.getTagElement(TANKS) != lastSaved) load();
    }

    private void load() {
        CompoundNBT root = owner.getTagElement(TANKS);
        lastSaved = root;
        java.util.Arrays.fill(fluids, FluidStack.EMPTY);
        if (root == null) return;
        ListNBT list = root.getList("Fluids", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entry = list.getCompound(i);
            int tank = entry.getInt("Tank");
            if (validTank(tank)) fluids[tank] = FluidStack.loadFluidStackFromNBT(entry);
        }
    }

    private void save() {
        ListNBT list = new ListNBT();
        for (int tank = 0; tank < fluids.length; tank++) {
            if (fluids[tank].isEmpty()) continue;
            CompoundNBT entry = new CompoundNBT();
            entry.putInt("Tank", tank);
            fluids[tank].writeToNBT(entry);
            list.add(entry);
        }
        CompoundNBT root = new CompoundNBT();
        root.put("Fluids", list);
        owner.getOrCreateTag().put(TANKS, root);
        lastSaved = root;
    }
}
