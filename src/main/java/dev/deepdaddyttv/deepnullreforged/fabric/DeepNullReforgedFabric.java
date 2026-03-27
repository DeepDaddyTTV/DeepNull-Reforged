package dev.deepdaddyttv.deepnullreforged.fabric;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import dev.deepdaddyttv.deepnullreforged.network.NullWorkbenchPayloads;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class DeepNullReforgedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerLifecycleHooks.initialize();
        String version = FabricLoader.getInstance()
                .getModContainer(DeepNullReforged.MODID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("0.0.0");
        DeepNullReforged.initialize(version);
        RegisterPayloadHandlersEvent payloadHandlersEvent = new RegisterPayloadHandlersEvent();
        DeepNullPayloads.register(payloadHandlersEvent);
        NullWorkbenchPayloads.register(payloadHandlersEvent);
    }
}
