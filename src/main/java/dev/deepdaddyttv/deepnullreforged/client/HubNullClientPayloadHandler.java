package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.menu.HubNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.HubNullPayloads;
import net.minecraft.client.Minecraft;

public final class HubNullClientPayloadHandler {
    private HubNullClientPayloadHandler() {
    }

    public static void handleState(HubNullPayloads.StatePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.player.containerMenu instanceof HubNullMenu menu) || menu.containerId != payload.containerId()) {
            return;
        }
        menu.updateState(payload.stations(), payload.resources(), payload.dampResources(), payload.dumpRules(), payload.denResources());
        if (minecraft.screen instanceof HubNullScreen screen) {
            screen.handleStateUpdated();
        }
    }
}
