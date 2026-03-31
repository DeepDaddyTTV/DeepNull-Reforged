package dev.deepdaddyttv.deepnullreforged;

import com.mojang.logging.LogUtils;
import dev.deepdaddyttv.deepnullreforged.event.CommonEvents;
import dev.deepdaddyttv.deepnullreforged.gametest.ModGameTests;
import dev.deepdaddyttv.deepnullreforged.integration.craftingtweaks.CraftingTweaksCompat;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import dev.deepdaddyttv.deepnullreforged.network.NullWorkbenchPayloads;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import dev.deepdaddyttv.deepnullreforged.registry.ModCapabilities;
import dev.deepdaddyttv.deepnullreforged.registry.ModCreativeTabs;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import dev.deepdaddyttv.deepnullreforged.registry.ModRecipeSerializers;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import java.util.List;

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
        ModGameTests.register(modEventBus);

        modEventBus.addListener(DeepNullPayloads::register);
        modEventBus.addListener(NullWorkbenchPayloads::register);
        modEventBus.addListener(ModCapabilities::register);
        modEventBus.addListener(DeepNullReforged::commonSetup);
        modEventBus.addListener(DeepNullConfig::onLoad);
        modEventBus.addListener(DeepNullConfig::onReload);
        modEventBus.addListener(DeepNullReforged::registerInventorySorterCompat);
        modContainer.registerConfig(ModConfig.Type.CLIENT, DeepNullConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, DeepNullConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, DeepNullConfig.SERVER_SPEC);
        registerClientEvents(modEventBus);

        NeoForge.EVENT_BUS.register(new CommonEvents());
    }

    private static void registerClientEvents(IEventBus modEventBus) {
        try {
            Class<?> clientModEvents = Class.forName("dev.deepdaddyttv.deepnullreforged.client.ClientModEvents");
            clientModEvents.getMethod("register", IEventBus.class).invoke(null, modEventBus);
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        if (classPresent("net.blay09.mods.craftingtweaks.api.CraftingTweaksAPI")) {
            event.enqueueWork(CraftingTweaksCompat::initialize);
        }
    }

    private static void registerInventorySorterCompat(InterModEnqueueEvent event) {
        for (String slotClass : inventorySorterSlotBlacklists()) {
            InterModComms.sendTo("inventorysorter", "slotblacklist", () -> slotClass);
        }
        InterModComms.sendTo("inventorysorter", "containerblacklist", DeepNullReforged::inventorySorterContainerBlacklist);
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

    private static boolean classPresent(String className) {
        try {
            Class.forName(className, false, DeepNullReforged.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }
}
