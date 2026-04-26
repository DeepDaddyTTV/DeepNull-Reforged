package dev.deepdaddyttv.deepnullreforged.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

public final class DeepNullPlayerState {
    private static final String ROOT_KEY = "DeepNullPlayerState";
    private static final String GLOBAL_AUTO_PICKUP_ENABLED_KEY = "GlobalAutoPickupEnabled";

    private DeepNullPlayerState() {
    }

    public static boolean isGlobalAutoPickupEnabled(Player player) {
        CompoundTag root = rootTag(player);
        return !root.contains(GLOBAL_AUTO_PICKUP_ENABLED_KEY) || root.getBoolean(GLOBAL_AUTO_PICKUP_ENABLED_KEY);
    }

    public static void setGlobalAutoPickupEnabled(Player player, boolean enabled) {
        CompoundTag root = rootTag(player);
        root.putBoolean(GLOBAL_AUTO_PICKUP_ENABLED_KEY, enabled);
        player.getPersistentData().put(ROOT_KEY, root);
    }

    public static boolean toggleGlobalAutoPickup(Player player) {
        boolean next = !isGlobalAutoPickupEnabled(player);
        setGlobalAutoPickupEnabled(player, next);
        return next;
    }

    public static void copyForClone(Player original, Player clone) {
        if (original.getPersistentData().contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            clone.getPersistentData().put(ROOT_KEY, original.getPersistentData().getCompound(ROOT_KEY).copy());
        }
    }

    private static CompoundTag rootTag(Player player) {
        return player.getPersistentData().contains(ROOT_KEY, Tag.TAG_COMPOUND)
                ? player.getPersistentData().getCompound(ROOT_KEY).copy()
                : new CompoundTag();
    }
}
