package dev.deepdaddyttv.deepnullreforged.fabric.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.fabric.DeepNullCiSmoke;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

public final class DeepNullCiSmokeClient {
    private static final String CLIENT_ENV = "DEEPNULL_CI_SMOKE_CLIENT";
    private static int titleScreenTicks;
    private static boolean stopRequested;

    private DeepNullCiSmokeClient() {
    }

    public static void initialize() {
        if (!DeepNullCiSmoke.enabled(CLIENT_ENV)) {
            return;
        }

        ClientTickEvents.END_CLIENT_TICK.register(DeepNullCiSmokeClient::onClientTick);
    }

    private static void onClientTick(Minecraft minecraft) {
        if (stopRequested) {
            return;
        }

        if (minecraft.screen instanceof TitleScreen) {
            titleScreenTicks++;
            if (titleScreenTicks >= 5) {
                stopRequested = true;
                DeepNullReforged.LOGGER.info("DeepNull Fabric CI smoke: client reached title screen, shutting down");
                minecraft.stop();
            }
            return;
        }

        titleScreenTicks = 0;
    }
}
