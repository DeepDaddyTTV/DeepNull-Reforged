package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.network.NullWorkbenchPayloads;
import net.minecraft.client.Minecraft;

public final class NullWorkbenchClientPayloadHandler {
    private NullWorkbenchClientPayloadHandler() {
    }

    public static void handleSeedPreset(NullWorkbenchPayloads.SeedPresetPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof NullWorkbenchScreen screen && screen.getMenu().containerId == payload.containerId()) {
            screen.acceptSeedPreset(payload);
        }
    }

    public static void handleSeedPresetList(NullWorkbenchPayloads.SeedPresetListPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof NullWorkbenchScreen screen && screen.getMenu().containerId == payload.containerId()) {
            screen.acceptSeedPresetList(payload);
        }
    }

    public static void handleSeedApplyResult(NullWorkbenchPayloads.SeedApplyResultPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof NullWorkbenchScreen screen && screen.getMenu().containerId == payload.containerId()) {
            screen.acceptSeedApplyResult(payload);
        }
    }
}
