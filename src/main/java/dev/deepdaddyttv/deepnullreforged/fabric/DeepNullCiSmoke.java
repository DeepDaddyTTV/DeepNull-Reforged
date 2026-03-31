package dev.deepdaddyttv.deepnullreforged.fabric;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class DeepNullCiSmoke {
    private static final String SERVER_ENV = "DEEPNULL_CI_SMOKE_SERVER";

    private DeepNullCiSmoke() {
    }

    public static void initializeServer() {
        if (!enabled(SERVER_ENV)) {
            return;
        }

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            DeepNullReforged.LOGGER.info("DeepNull Fabric CI smoke: dedicated server reached started state");
            server.execute(() -> {
                DeepNullReforged.LOGGER.info("DeepNull Fabric CI smoke: stopping dedicated server after successful startup");
                server.halt(false);
            });
        });
    }

    public static boolean enabled(String envName) {
        String value = System.getenv(envName);
        return value != null
                && !value.isBlank()
                && !"0".equals(value)
                && !"false".equalsIgnoreCase(value)
                && !"no".equalsIgnoreCase(value);
    }
}
