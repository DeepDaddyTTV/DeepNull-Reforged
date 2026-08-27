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
    public FluidStack getFluidInTank(int tank) { return validTank(tank) ? fluids[tank] : FluidStack.EMPTY; }

    @Override
    public int getTankCapacity(int tank) { return tier.fluidCapacity(); }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return validTank(tank) && !stack.isEmpty(); }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        int tank = findMatching(resource);
        if (tank < 0) tank = findEmpty();
        if (tank < 0) return 0;
        if (tier.creative()) {
            if (action.execute() && fluids[tank].isEmpty()) {
                fluids[tank] = resource.copy();
                fluids[tank].setAmount(Math.min(1000, resource.getAmount()));
                save();
            }
            return resource.getAmount();
        }
        int accepted = Math.min(resource.getAmount(), tier.fluidCapacity() - fluids[tank].getAmount());
        if (accepted > 0 && action.execute()) {
            if (fluids[tank].isEmpty()) {
                fluids[tank] = resource.copy();
                fluids[tank].setAmount(accepted);
            } else {
                fluids[tank].grow(accepted);
            }
            save();
        }
        return accepted;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        int tank = findMatching(resource);
        if (tank < 0) return FluidStack.EMPTY;
        return drainTank(tank, resource.getAmount(), action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        for (int tank = 0; tank < fluids.length; tank++) {
            if (!fluids[tank].isEmpty()) return drainTank(tank, maxDrain, action);
        }
        return FluidStack.EMPTY;
    }

    private FluidStack drainTank(int tank, int maxDrain, FluidAction action) {
        if (maxDrain <= 0 || fluids[tank].isEmpty()) return FluidStack.EMPTY;
        int amount = tier.creative() ? maxDrain : Math.min(maxDrain, fluids[tank].getAmount());
        FluidStack result = fluids[tank].copy();
        result.setAmount(amount);
        if (action.execute() && !tier.creative()) {
            fluids[tank].shrink(amount);
            if (fluids[tank].isEmpty()) fluids[tank] = FluidStack.EMPTY;
            save();
        }
        return result;
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

    private void load() {
        CompoundNBT root = owner.getTagElement(TANKS);
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
    }
}
