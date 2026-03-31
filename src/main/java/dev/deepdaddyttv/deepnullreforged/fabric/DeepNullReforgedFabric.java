package dev.deepdaddyttv.deepnullreforged.fabric;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.integration.craftingtweaks.CraftingTweaksCompat;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import dev.deepdaddyttv.deepnullreforged.network.NullWorkbenchPayloads;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import dev.deepdaddyttv.deepnullreforged.compat.network.event.RegisterPayloadHandlersEvent;
import dev.deepdaddyttv.deepnullreforged.compat.server.ServerLifecycleHooks;

public final class DeepNullReforgedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerLifecycleHooks.initialize();
        String version = FabricLoader.getInstance()
                .getModContainer(DeepNullReforged.MODID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("0.0.0");
        DeepNullConfig.registerFabricConfigs(DeepNullReforged.MODID);
        DeepNullReforged.initialize(version);
        DeepNullCiSmoke.initializeServer();
        RegisterPayloadHandlersEvent payloadHandlersEvent = new RegisterPayloadHandlersEvent();
        DeepNullPayloads.register(payloadHandlersEvent);
        NullWorkbenchPayloads.register(payloadHandlersEvent);
        if (classPresent("net.blay09.mods.craftingtweaks.api.CraftingTweaksAPI")) {
            CraftingTweaksCompat.initialize();
        }
    }

    private static boolean classPresent(String className) {
        try {
            Class.forName(className, false, DeepNullReforgedFabric.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}
