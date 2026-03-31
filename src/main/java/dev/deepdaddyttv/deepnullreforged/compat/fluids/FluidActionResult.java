package dev.deepdaddyttv.deepnullreforged.compat.fluids;

import net.minecraft.world.item.ItemStack;

public final class FluidActionResult {
    private static final FluidActionResult FAILURE = new FluidActionResult(ItemStack.EMPTY, false);

    private final ItemStack result;
    private final boolean success;

    public FluidActionResult(ItemStack result, boolean success) {
        this.result = result;
        this.success = success;
    }

    public static FluidActionResult success(ItemStack result) {
        return new FluidActionResult(result, true);
    }

    public static FluidActionResult fail() {
        return FAILURE;
    }

    public boolean isSuccess() {
        return success;
    }

    public ItemStack getResult() {
        return result;
    }
}
