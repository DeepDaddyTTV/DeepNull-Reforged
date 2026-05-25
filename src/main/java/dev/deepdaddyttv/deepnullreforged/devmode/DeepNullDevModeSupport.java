package dev.deepdaddyttv.deepnullreforged.devmode;

import dev.deepdaddyttv.deepnullreforged.network.DeepNullDevModePayloads;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;

import java.lang.reflect.InvocationTargetException;

public final class DeepNullDevModeSupport {
    private DeepNullDevModeSupport() {
    }

    public static boolean isAvailable() {
        return !FMLLoader.isProduction();
    }

    public static boolean isEnabled(ServerPlayer player) {
        return isEnabled(player.serverLevel());
    }

    public static boolean isEnabled(net.minecraft.server.level.ServerLevel level) {
        return isAvailable() && DeepNullDevModeSavedData.get(level).isEnabled();
    }

    public static void syncToPlayer(ServerPlayer player) {
        DeepNullDevModePayloads.sendState(player, isEnabled(player));
    }

    public static void syncToAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncToPlayer(player);
        }
    }

    public static boolean shouldShowCreativePresets() {
        if (!isAvailable() || !FMLEnvironment.dist.isClient()) {
            return false;
        }
        try {
            Class<?> stateClass = Class.forName("dev.deepdaddyttv.deepnullreforged.client.DeepNullDevModeClientState");
            return (boolean) stateClass.getMethod("isEnabled").invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            return false;
        }
    }
}
