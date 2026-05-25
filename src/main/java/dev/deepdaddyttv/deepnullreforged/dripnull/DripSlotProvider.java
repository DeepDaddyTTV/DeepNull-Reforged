package dev.deepdaddyttv.deepnullreforged.dripnull;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface DripSlotProvider {
    String providerId();

    List<DripSlotRef> slots(ServerPlayer player);

    boolean hasSlot(ServerPlayer player, DripSlotRef ref);

    ItemStack get(ServerPlayer player, DripSlotRef ref);

    void set(ServerPlayer player, DripSlotRef ref, ItemStack stack);
}
