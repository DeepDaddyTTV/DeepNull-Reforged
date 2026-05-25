package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.network.DenNullPayloads;
import net.minecraft.client.Minecraft;

public final class DenNullClientPayloadHandler {
    private DenNullClientPayloadHandler() {
    }

    public static void handleState(DenNullPayloads.StatePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.screen instanceof DenNullScreen screen) {
                screen.acceptState(payload.containerId(), payload.data());
            }
        });
    }
}
