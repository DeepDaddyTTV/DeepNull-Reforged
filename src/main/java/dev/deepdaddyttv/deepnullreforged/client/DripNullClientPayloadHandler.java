package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.menu.DripNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DripNullPayloads;
import net.minecraft.client.Minecraft;

public final class DripNullClientPayloadHandler {
    private DripNullClientPayloadHandler() {
    }

    public static void handleState(DripNullPayloads.StatePayload payload) {
        if (Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.containerMenu instanceof DripNullMenu menu
                && menu.containerId == payload.containerId()) {
            menu.updateData(payload.data());
        }
    }
}
