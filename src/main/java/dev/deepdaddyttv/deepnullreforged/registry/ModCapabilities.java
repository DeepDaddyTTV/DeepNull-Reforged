package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.integration.mekanism.MekanismCompat;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ModCapabilities {
    private ModCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        if (ModList.get().isLoaded("mekanism")) {
            MekanismCompat.registerCapabilities(event);
        }
    }
}
