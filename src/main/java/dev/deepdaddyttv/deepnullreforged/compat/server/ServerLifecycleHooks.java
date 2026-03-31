package dev.deepdaddyttv.deepnullreforged.compat.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public final class ServerLifecycleHooks {
    private static volatile MinecraftServer currentServer;
    private static volatile boolean initialized;

    private ServerLifecycleHooks() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (currentServer == server) {
                currentServer = null;
            }
        });
    }

    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }
}
