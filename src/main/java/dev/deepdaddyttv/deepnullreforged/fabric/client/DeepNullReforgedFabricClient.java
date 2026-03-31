package dev.deepdaddyttv.deepnullreforged.fabric.client;

import dev.deepdaddyttv.deepnullreforged.client.ClientGameEvents;
import dev.deepdaddyttv.deepnullreforged.client.ClientModEvents;
import net.fabricmc.api.ClientModInitializer;

public final class DeepNullReforgedFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientModEvents.initialize();
        ClientGameEvents.initialize();
        DeepNullCiSmokeClient.initialize();
    }
}
