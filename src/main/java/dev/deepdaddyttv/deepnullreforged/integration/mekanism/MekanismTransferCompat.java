package dev.deepdaddyttv.deepnullreforged.integration.mekanism;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.Nullable;

public final class MekanismTransferCompat {
    private MekanismTransferCompat() {
    }

    public static @Nullable InteractionResult tryShiftChemicalTransfer(UseOnContext context, DeepNullInventory inventory) {
        return null;
    }
}
