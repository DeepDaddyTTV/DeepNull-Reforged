package dev.deepdaddyttv.deepnullreforged.compat.fml;

import net.fabricmc.loader.api.FabricLoader;

public final class ModList {
    private static final ModList INSTANCE = new ModList();

    private ModList() {
    }

    public static ModList get() {
        return INSTANCE;
    }

    public boolean isLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
