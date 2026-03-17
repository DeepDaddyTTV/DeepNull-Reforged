package dev.deepdaddyttv.deepnullreforged;

import com.mojang.logging.LogUtils;
import dev.deepdaddyttv.deepnullreforged.event.CommonEvents;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import dev.deepdaddyttv.deepnullreforged.registry.ModCapabilities;
import dev.deepdaddyttv.deepnullreforged.registry.ModCreativeTabs;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import dev.deepdaddyttv.deepnullreforged.registry.ModRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(DeepNullReforged.MODID)
public final class DeepNullReforged {
    public static final String MODID = "deepnullreforged";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static String MOD_VERSION = "0.0.0";

    public DeepNullReforged(IEventBus modEventBus, ModContainer modContainer) {
        MOD_VERSION = modContainer.getModInfo().getVersion().toString();
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(DeepNullPayloads::register);
        modEventBus.addListener(ModCapabilities::register);
        modEventBus.addListener(DeepNullConfig::onLoad);
        modEventBus.addListener(DeepNullConfig::onReload);
        modContainer.registerConfig(ModConfig.Type.SERVER, DeepNullConfig.SPEC);

        NeoForge.EVENT_BUS.register(new CommonEvents());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
