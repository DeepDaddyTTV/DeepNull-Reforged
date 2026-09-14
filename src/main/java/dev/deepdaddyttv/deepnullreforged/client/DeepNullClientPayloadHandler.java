package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import net.minecraft.client.Minecraft;

public final class DeepNullClientPayloadHandler {
    private DeepNullClientPayloadHandler() {
    }

    public static void handleFluidContents(DeepNullPayloads.FluidContentsPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.player.containerMenu instanceof DeepNullMenu menu)) {
            return;
        }
        if (menu.containerId == payload.containerId()) {
            menu.acceptFluidContents(payload.fluids(), payload.chemicals());
        }
    }

    public static void handleStorageActionResult(DeepNullPayloads.StorageActionResultPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.containerMenu.containerId != payload.containerId()) {
            return;
        }
        if (minecraft.screen instanceof StorageActionResultListener listener) {
            listener.deepNullReforged$handleStorageActionResult(payload);
        }
    }

    public static void handleExtractionEditResult(DeepNullPayloads.ExtractionEditResultPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.containerMenu.containerId != payload.containerId()) {
            return;
        }
        if (minecraft.screen instanceof ExtractionEditResultListener listener) {
            listener.deepNullReforged$handleExtractionEditResult(payload);
        }
    }
}
