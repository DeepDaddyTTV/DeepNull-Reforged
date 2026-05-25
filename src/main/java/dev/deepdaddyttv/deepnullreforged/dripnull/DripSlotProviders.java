package dev.deepdaddyttv.deepnullreforged.dripnull;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class DripSlotProviders {
    private DripSlotProviders() {
    }

    public static List<DripSlotRef> slotsForCapture(ServerPlayer player, DripProfile profile, int reservedInventorySlot) {
        return VanillaDripSlotProvider.INSTANCE.slots(player).stream()
                .filter(profile::accepts)
                .filter(ref -> !isReserved(ref, reservedInventorySlot))
                .toList();
    }

    public static boolean hasSlot(ServerPlayer player, DripSlotRef ref) {
        return ref != null && ref.vanilla() && VanillaDripSlotProvider.INSTANCE.hasSlot(player, ref);
    }

    public static ItemStack get(ServerPlayer player, DripSlotRef ref) {
        return hasSlot(player, ref) ? VanillaDripSlotProvider.INSTANCE.get(player, ref) : ItemStack.EMPTY;
    }

    public static void set(ServerPlayer player, DripSlotRef ref, ItemStack stack) {
        if (hasSlot(player, ref)) {
            VanillaDripSlotProvider.INSTANCE.set(player, ref, stack);
        }
    }

    private static boolean isReserved(DripSlotRef ref, int reservedInventorySlot) {
        return reservedInventorySlot >= 0
                && DripSlotRef.INVENTORY_PROVIDER.equals(ref.provider())
                && ref.slot() == reservedInventorySlot;
    }
}
