package dev.deepdaddyttv.deepnullreforged.fabric.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.fabric.DeepNullCiSmoke;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

public final class DeepNullCiSmokeClient {
    private static final String CLIENT_ENV = "DEEPNULL_CI_SMOKE_CLIENT";
    private static boolean stopRequested;
    private static String lastScreenName = "<none>";

    private DeepNullCiSmokeClient() {
    }

    public static void initialize() {
        stopRequested = false;
        lastScreenName = "<none>";
    }

    public static void onScreenSet(Minecraft minecraft, Screen screen) {
        if (!DeepNullCiSmoke.enabled(CLIENT_ENV) || stopRequested) {
            return;
        }

        String screenName = screen == null ? "<none>" : screen.getClass().getName();
        if (!screenName.equals(lastScreenName)) {
            lastScreenName = screenName;
            DeepNullReforged.LOGGER.info("DeepNull Fabric CI smoke: current screen {}", screenName);
        }

        if (isSmokeSuccessScreen(screen)) {
            stopRequested = true;
            DeepNullReforged.LOGGER.info("DeepNull Fabric CI smoke: client reached menu screen {}, shutting down", screenName);
            minecraft.stop();
        }
    }

    private static boolean isSmokeSuccessScreen(Screen screen) {
        return screen instanceof TitleScreen
                || screen instanceof AccessibilityOnboardingScreen
                || screen instanceof NoticeWithLinkScreen;
    }
}
