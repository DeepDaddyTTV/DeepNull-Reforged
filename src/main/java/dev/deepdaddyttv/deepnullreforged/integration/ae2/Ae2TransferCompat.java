package dev.deepdaddyttv.deepnullreforged.integration.ae2;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.Nullable;

public final class Ae2TransferCompat {
    private Ae2TransferCompat() {
    }

    public static @Nullable InteractionResult tryShiftItemTransfer(UseOnContext context, DeepNullInventory inventory) {
        return null;
    }

    public static @Nullable InteractionResult tryShiftFluidTransfer(UseOnContext context, DeepNullInventory inventory) {
        return null;
    }
}
