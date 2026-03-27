package dev.deepdaddyttv.deepnullreforged.integration.ae2;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

public final class Ae2TransferCompat {
    private Ae2TransferCompat() {
    }

    public static InteractionResult tryShiftItemTransfer(UseOnContext context, DeepNullInventory inventory) {
        return InteractionResult.PASS;
    }

    public static InteractionResult tryShiftFluidTransfer(UseOnContext context, DeepNullInventory inventory) {
        return InteractionResult.PASS;
    }
}
