package dev.deepdaddyttv.deepnullreforged;

import com.mojang.logging.LogUtils;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import dev.deepdaddyttv.deepnullreforged.registry.ModCapabilities;
import dev.deepdaddyttv.deepnullreforged.registry.ModCreativeTabs;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import dev.deepdaddyttv.deepnullreforged.registry.ModRecipeSerializers;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.util.List;

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
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    static List<String> inventorySorterSlotBlacklists() {
        return List.of(
                "dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu$StorageSlot",
                "dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu$DockStorageSlot",
                "dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu$FluidStorageSlot"
        );
    }

    static Identifier inventorySorterContainerBlacklist() {
        return id("deep_null");
    }
}
