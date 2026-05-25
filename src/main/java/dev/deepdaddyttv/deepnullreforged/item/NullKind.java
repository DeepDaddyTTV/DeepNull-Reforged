package dev.deepdaddyttv.deepnullreforged.item;

import net.minecraft.world.item.ItemStack;

public enum NullKind {
    DEEP,
    DAMP,
    DUMP,
    DRIP,
    HUB,
    DEN,
    HEX,
    UNKNOWN;

    public static NullKind of(ItemStack stack) {
        if (stack.isEmpty()) {
            return UNKNOWN;
        }
        if (stack.getItem() instanceof DockableNullItem dockable) {
            return dockable.nullKind(stack);
        }
        if (stack.getItem() instanceof HubNullItem) {
            return HUB;
        }
        return UNKNOWN;
    }
}
