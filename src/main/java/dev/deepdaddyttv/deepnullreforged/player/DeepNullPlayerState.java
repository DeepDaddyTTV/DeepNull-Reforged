package dev.deepdaddyttv.deepnullreforged.player;

import com.mojang.serialization.Codec;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;

public final class DeepNullPlayerState {
    private static final AttachmentType<Boolean> GLOBAL_AUTO_PICKUP_ENABLED = AttachmentRegistry.create(
            DeepNullReforged.id("global_auto_pickup_enabled"),
            builder -> builder.persistent(Codec.BOOL).copyOnDeath().initializer(() -> true)
    );

    private DeepNullPlayerState() {
    }

    public static void initialize() {
    }

    public static boolean isGlobalAutoPickupEnabled(Player player) {
        return attachmentTarget(player).getAttachedOrCreate(GLOBAL_AUTO_PICKUP_ENABLED);
    }

    public static void setGlobalAutoPickupEnabled(Player player, boolean enabled) {
        attachmentTarget(player).setAttached(GLOBAL_AUTO_PICKUP_ENABLED, enabled);
    }

    public static boolean toggleGlobalAutoPickup(Player player) {
        boolean next = !isGlobalAutoPickupEnabled(player);
        setGlobalAutoPickupEnabled(player, next);
        return next;
    }

    public static void copyForClone(Player original, Player clone) {
        setGlobalAutoPickupEnabled(clone, isGlobalAutoPickupEnabled(original));
    }

    private static AttachmentTarget attachmentTarget(Player player) {
        return (AttachmentTarget) player;
    }
}
