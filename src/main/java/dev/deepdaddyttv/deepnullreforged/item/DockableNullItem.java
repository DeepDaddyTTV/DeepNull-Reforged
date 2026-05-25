package dev.deepdaddyttv.deepnullreforged.item;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import net.minecraft.world.item.ItemStack;

public interface DockableNullItem {
    DeepNullTier tier();

    NullKind nullKind(ItemStack stack);
}
