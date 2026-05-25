package dev.deepdaddyttv.deepnullreforged.dripnull;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class VanillaDripSlotProvider implements DripSlotProvider {
    public static final VanillaDripSlotProvider INSTANCE = new VanillaDripSlotProvider();

    private VanillaDripSlotProvider() {
    }

    @Override
    public String providerId() {
        return "minecraft";
    }

    @Override
    public List<DripSlotRef> slots(ServerPlayer player) {
        List<DripSlotRef> refs = new ArrayList<>(41);
        for (int slot = 0; slot < 36; slot++) {
            refs.add(DripSlotRef.inventory(slot));
        }
        for (int slot = 0; slot < 4; slot++) {
            refs.add(DripSlotRef.armor(slot));
        }
        refs.add(DripSlotRef.offhand());
        return refs;
    }

    @Override
    public boolean hasSlot(ServerPlayer player, DripSlotRef ref) {
        if (ref == null) {
            return false;
        }
        return switch (ref.provider()) {
            case DripSlotRef.INVENTORY_PROVIDER -> ref.slot() >= 0 && ref.slot() < 36;
            case DripSlotRef.ARMOR_PROVIDER -> ref.slot() >= 0 && ref.slot() < player.getInventory().armor.size();
            case DripSlotRef.OFFHAND_PROVIDER -> ref.slot() == 0 && ref.slot() < player.getInventory().offhand.size();
            default -> false;
        };
    }

    @Override
    public ItemStack get(ServerPlayer player, DripSlotRef ref) {
        if (!hasSlot(player, ref)) {
            return ItemStack.EMPTY;
        }
        return switch (ref.provider()) {
            case DripSlotRef.INVENTORY_PROVIDER -> player.getInventory().getItem(ref.slot());
            case DripSlotRef.ARMOR_PROVIDER -> player.getInventory().armor.get(ref.slot());
            case DripSlotRef.OFFHAND_PROVIDER -> player.getInventory().offhand.get(0);
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public void set(ServerPlayer player, DripSlotRef ref, ItemStack stack) {
        if (!hasSlot(player, ref)) {
            return;
        }
        ItemStack value = stack == null ? ItemStack.EMPTY : stack;
        switch (ref.provider()) {
            case DripSlotRef.INVENTORY_PROVIDER -> player.getInventory().setItem(ref.slot(), value);
            case DripSlotRef.ARMOR_PROVIDER -> player.getInventory().armor.set(ref.slot(), value);
            case DripSlotRef.OFFHAND_PROVIDER -> player.getInventory().offhand.set(0, value);
            default -> {
            }
        }
        player.getInventory().setChanged();
    }
}
