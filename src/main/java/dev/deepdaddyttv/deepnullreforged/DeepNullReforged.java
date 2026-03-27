package dev.deepdaddyttv.deepnullreforged;

import com.mojang.logging.LogUtils;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import dev.deepdaddyttv.deepnullreforged.registry.ModCapabilities;
import dev.deepdaddyttv.deepnullreforged.registry.ModCreativeTabs;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import dev.deepdaddyttv.deepnullreforged.registry.ModRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public final class DeepNullReforged {
    public static final String MODID = "deepnullreforged";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static String MOD_VERSION = "0.0.0";

    private static boolean initialized;

    private DeepNullReforged() {
    }

    public static void initialize(String modVersion) {
        if (initialized) {
            return;
        }

        initialized = true;
        MOD_VERSION = modVersion;

        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModMenus.register();
        ModRecipeSerializers.register();
        ModCreativeTabs.register();
        ModCapabilities.register();
        DeepNullConfig.initializeDefaults();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
