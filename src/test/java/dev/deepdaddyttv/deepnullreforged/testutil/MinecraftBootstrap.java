package dev.deepdaddyttv.deepnullreforged.testutil;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public final class MinecraftBootstrap {
    private static boolean bootstrapped;

    private MinecraftBootstrap() {
    }

    public static synchronized void ensureBootstrapped() {
        if (bootstrapped) {
            return;
        }

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        bootstrapped = true;
    }
}
