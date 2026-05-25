package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.network.DumpNullPayloads;
import net.minecraft.client.Minecraft;

public final class DumpNullClientPayloadHandler {
    private DumpNullClientPayloadHandler() {
    }

    public static void handleMobDrops(DumpNullPayloads.MobDropsPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.screen instanceof DumpNullScreen screen) {
                screen.acceptMobDrops(payload.containerId(), payload.rows());
            }
        });
    }

    public static void handleMobDropSearch(DumpNullPayloads.MobDropSearchPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.screen instanceof DumpNullScreen screen) {
                screen.acceptMobDropSearch(payload);
            }
        });
    }

    public static void handleState(DumpNullPayloads.DumpNullStatePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.screen instanceof DumpNullScreen screen) {
                screen.acceptState(payload.containerId(), payload.data(), payload.mobOptions(), payload.itemStatuses());
            }
        });
    }

    public static void handlePresetPreview(DumpNullPayloads.PresetPreviewPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.screen instanceof DumpNullScreen screen) {
                screen.acceptPresetPreview(payload);
            }
        });
    }

    public static void handleItemCatalog(DumpNullPayloads.ItemCatalogPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.screen instanceof DumpNullScreen screen) {
                screen.acceptItemCatalog(payload);
            }
        });
    }
}
