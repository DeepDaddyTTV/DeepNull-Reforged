package dev.deepdaddyttv.deepnullreforged.compat.fluids;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class FluidStack {
    private static final String FLUID_TAG = "FluidName";
    private static final String AMOUNT_TAG = "Amount";

    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

    private final Fluid fluid;
    private int amount;

    public FluidStack(Fluid fluid, int amount) {
        this.fluid = fluid == null ? Fluids.EMPTY : fluid;
        this.amount = Math.max(0, amount);
    }

    public Fluid getFluid() {
        return fluid;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isEmpty() {
        return fluid == Fluids.EMPTY || amount <= 0;
    }

    public FluidStack copy() {
        return isEmpty() ? EMPTY : new FluidStack(fluid, amount);
    }

    public FluidStack copyWithAmount(int newAmount) {
        return newAmount <= 0 || isEmpty() ? EMPTY : new FluidStack(fluid, newAmount);
    }

    public void setAmount(int newAmount) {
        this.amount = Math.max(0, newAmount);
    }

    public void grow(int amount) {
        setAmount(this.amount + amount);
    }

    public void shrink(int amount) {
        setAmount(this.amount - amount);
    }

    public Component getHoverName() {
        if (isEmpty()) {
            return Component.empty();
        }

        Item bucket = fluid.getBucket();
        if (bucket != null && bucket != ItemStack.EMPTY.getItem()) {
            ItemStack stack = new ItemStack(bucket);
            if (!stack.isEmpty()) {
                return stack.getHoverName();
            }
        }

        Identifier id = BuiltInRegistries.FLUID.getKey(fluid);
        return id == null ? Component.literal("unknown") : Component.literal(id.toString());
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (isEmpty()) {
            return tag;
        }
        tag.putString(FLUID_TAG, BuiltInRegistries.FLUID.getKey(fluid).toString());
        tag.putInt(AMOUNT_TAG, amount);
        return tag;
    }

    public CompoundTag saveOptional(HolderLookup.Provider registries) {
        return save();
    }

    public static FluidStack parseOptional(HolderLookup.Provider registries, CompoundTag tag) {
        if (tag == null) {
            return EMPTY;
        }

        String fluidName = tag.getStringOr(FLUID_TAG, "");
        int amount = tag.getIntOr(AMOUNT_TAG, 0);
        if (fluidName.isBlank() || amount <= 0) {
            return EMPTY;
        }

        Identifier id = Identifier.tryParse(fluidName);
        if (id == null) {
            return EMPTY;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getOptional(id).orElse(Fluids.EMPTY);
        return fluid == Fluids.EMPTY ? EMPTY : new FluidStack(fluid, amount);
    }

    public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
        if (first == null || second == null) {
            return false;
        }
        if (first.isEmpty() || second.isEmpty()) {
            return first.isEmpty() && second.isEmpty();
        }
        return first.fluid == second.fluid;
    }
}
