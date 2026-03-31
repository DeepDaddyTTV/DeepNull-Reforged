package dev.deepdaddyttv.deepnullreforged;

import com.mojang.logging.LogUtils;
import dev.deepdaddyttv.deepnullreforged.event.CommonEvents;
import dev.deepdaddyttv.deepnullreforged.integration.craftingtweaks.CraftingTweaksCompat;
import dev.deepdaddyttv.deepnullreforged.gametest.CraftingTransferRegressionGameTests;
import dev.deepdaddyttv.deepnullreforged.gametest.DeepNullRegressionGameTests;
import dev.deepdaddyttv.deepnullreforged.gametest.NullWorkbenchRegressionGameTests;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import dev.deepdaddyttv.deepnullreforged.network.NullWorkbenchPayloads;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import dev.deepdaddyttv.deepnullreforged.registry.ModCapabilities;
import dev.deepdaddyttv.deepnullreforged.registry.ModCreativeTabs;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import dev.deepdaddyttv.deepnullreforged.registry.ModRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
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

        modEventBus.addListener(DeepNullPayloads::register);
        modEventBus.addListener(NullWorkbenchPayloads::register);
        modEventBus.addListener(ModCapabilities::register);
        modEventBus.addListener(DeepNullReforged::commonSetup);
        modEventBus.addListener(DeepNullReforged::registerGameTests);
        modEventBus.addListener(DeepNullConfig::onLoad);
        modEventBus.addListener(DeepNullConfig::onReload);
        modEventBus.addListener(DeepNullReforged::registerInventorySorterCompat);
        modContainer.registerConfig(ModConfig.Type.CLIENT, DeepNullConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, DeepNullConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, DeepNullConfig.SERVER_SPEC);

        NeoForge.EVENT_BUS.register(new CommonEvents());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
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

    private static void registerGameTests(RegisterGameTestsEvent event) {
        event.register(DeepNullRegressionGameTests.class);
        event.register(NullWorkbenchRegressionGameTests.class);
        event.register(CraftingTransferRegressionGameTests.class);
        if (classPresent("appeng.api.storage.MEStorage")) {
            registerOptionalGameTest(event, "dev.deepdaddyttv.deepnullreforged.gametest.Ae2RegressionGameTests");
        }
        if (classPresent("mekanism.api.chemical.ChemicalStack")) {
            registerOptionalGameTest(event, "dev.deepdaddyttv.deepnullreforged.gametest.MekanismRegressionGameTests");
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerOptionalGameTest(RegisterGameTestsEvent event, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            event.register((Class<?>) clazz);
        } catch (ClassNotFoundException | LinkageError exception) {
            LOGGER.warn("Skipping optional GameTest registration for {}", className, exception);
        }
    }

    private static boolean classPresent(String className) {
        try {
            Class.forName(className, false, DeepNullReforged.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }

    static List<String> inventorySorterSlotBlacklists() {
        return List.of(
                "dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu$StorageSlot",
                "dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu$DockStorageSlot",
                "dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu$FluidStorageSlot"
        );
    }

    static ResourceLocation inventorySorterContainerBlacklist() {
        return id("deep_null");
    }
}
