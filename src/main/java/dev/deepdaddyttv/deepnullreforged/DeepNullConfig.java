package dev.deepdaddyttv.deepnullreforged;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import dev.deepdaddyttv.deepnullreforged.compat.fml.ModList;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DeepNullConfig {
    private static final int TIER_COUNT = DeepNullTier.values().length;

    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec SERVER_SPEC;

    private static final int[] DEFAULT_ITEM_CAPACITY_BY_TIER = {
            128, 512, 1152, 2048, 3200, Integer.MAX_VALUE, Integer.MAX_VALUE
    };
    private static final int[] DEFAULT_FLUID_CAPACITY_BY_TIER = {
            16_000, 32_000, 64_000, 128_000, 256_000, 512_000, Integer.MAX_VALUE
    };
    private static final int[] LEGACY_DEFAULT_FLUID_CAPACITY_BY_TIER = {
            8_000, 16_000, 32_000, 64_000, 128_000, 256_000, Integer.MAX_VALUE
    };
    private static final int[] DEFAULT_ENERGY_CAPACITY_BY_TIER = {
            0, 0, 0, 0, 100_000, 1_000_000, Integer.MAX_VALUE
    };
    private static final int[] DEFAULT_DEEP_ENERGY_CAPACITY_BY_TIER = {
            0, 0, 0, 0, 100_000, 25_000_000, Integer.MAX_VALUE
    };
    private static final int[] DEFAULT_ENERGY_TRANSFER_BY_TIER = {
            0, 0, 0, 0, 2_000, 20_000, Integer.MAX_VALUE
    };
    private static final int[] DEFAULT_DEEP_ENERGY_TRANSFER_BY_TIER = {
            0, 0, 0, 0, 2_000, 100_000, Integer.MAX_VALUE
    };
    private static final int[] DEFAULT_DAMPNULL_TANK_COUNT_BY_TIER = {
            9, 9, 9, 18, 18, 18, 18
    };
    private static final int[] DEFAULT_STONE_GENERATION_RATE_BY_TIER = {
            1, 2, 3, 4, 5, 10, 10
    };
    private static final int[] DEFAULT_SPONGE_ABSORB_LIMIT_BY_TIER = {
            8, 10, 12, 16, 32, 32, 32
    };
    private static final int[] DEFAULT_SPONGE_RANGE_WIDTH_BY_TIER = {
            6, 8, 8, 10, 12, 16, 16
    };
    private static final int[] DEFAULT_SPONGE_RANGE_HEIGHT_BY_TIER = {
            4, 6, 6, 8, 10, 12, 12
    };

    private static final ForgeConfigSpec.BooleanValue CLIENT_SHOW_HUD;
    private static final ForgeConfigSpec.IntValue CLIENT_HUD_OFFSET_X;
    private static final ForgeConfigSpec.IntValue CLIENT_HUD_OFFSET_Y;
    private static final ForgeConfigSpec.DoubleValue CLIENT_HUD_BACKGROUND_OPACITY;
    private static final ForgeConfigSpec.IntValue CLIENT_HUD_DISPLAY_MS;
    private static final ForgeConfigSpec.BooleanValue CLIENT_ENABLE_SHIFT_SCROLL_SELECTION;
    private static final ForgeConfigSpec.BooleanValue CLIENT_ENABLE_UPDATE_CHECKER;
    private static final ForgeConfigSpec.BooleanValue CLIENT_SHOW_GUIDEME_HINT;
    private static final ForgeConfigSpec.BooleanValue CLIENT_INVERT_DAMPNULL_INTERACTION;
    private static final ForgeConfigSpec.BooleanValue CLIENT_ANIMATE_DOCKED_NULLS;
    private static final ForgeConfigSpec.BooleanValue CLIENT_SHOW_FULL_DEEPNULL_COUNTS;

    private static final ForgeConfigSpec.BooleanValue COMMON_DEFAULT_TRANSFER_LOCKED;
    private static final ForgeConfigSpec.BooleanValue COMMON_DEFAULT_AUTO_PICKUP_ENABLED;
    private static final ForgeConfigSpec.BooleanValue COMMON_DEFAULT_AUTO_FEEDING_ENABLED;
    private static final ForgeConfigSpec.BooleanValue COMMON_DEFAULT_AUTO_SMELTING_ENABLED;
    private static final ForgeConfigSpec.IntValue COMMON_DEFAULT_STONEWORKS_AMOUNT;
    private static final ForgeConfigSpec.BooleanValue COMMON_ENABLE_CRAFTINGTWEAKS_RETURN_INTEGRATION;

    private static final ForgeConfigSpec.BooleanValue SERVER_DISABLE_TAG_MATCHING;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SERVER_TAG_BLACKLIST;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SERVER_TAG_WHITELIST;
    private static final ForgeConfigSpec.BooleanValue SERVER_ENABLE_AUTO_PICKUP;
    private static final ForgeConfigSpec.BooleanValue SERVER_ENABLE_AUTO_FEEDING;
    private static final ForgeConfigSpec.BooleanValue SERVER_ENABLE_AUTO_SMELTING;
    private static final ForgeConfigSpec.BooleanValue SERVER_ENABLE_COMPRESSION;
    private static final ForgeConfigSpec.BooleanValue SERVER_ENABLE_STONEWORKS;
    private static final ForgeConfigSpec.BooleanValue SERVER_ENABLE_STONE_GENERATOR;
    private static final ForgeConfigSpec.BooleanValue SERVER_ENABLE_OBSIDIAN_GENERATOR;
    private static final ForgeConfigSpec.BooleanValue SERVER_ENABLE_SPONGE_UPGRADE;
    private static final ForgeConfigSpec.BooleanValue SERVER_VOID_FULL_ITEMS_ON_PICKUP;
    private static final ForgeConfigSpec.BooleanValue SERVER_VOID_FULL_FLUIDS_ON_SPONGE;
    private static final ForgeConfigSpec.BooleanValue SERVER_ENABLE_CHEMICAL_STORAGE;
    private static final ForgeConfigSpec.IntValue SERVER_DOCK_GENERATOR_BUFFER_SIZE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> SERVER_ITEM_CAPACITY_BY_TIER;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> SERVER_FLUID_CAPACITY_BY_TIER;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> SERVER_ENERGY_CAPACITY_BY_TIER;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> SERVER_DEEP_ENERGY_CAPACITY_BY_TIER;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> SERVER_ENERGY_TRANSFER_BY_TIER;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> SERVER_DEEP_ENERGY_TRANSFER_BY_TIER;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> SERVER_DAMPNULL_TANK_COUNT_BY_TIER;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> SERVER_STONE_GENERATION_RATE_BY_TIER;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> SERVER_SPONGE_ABSORB_LIMIT_BY_TIER;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> SERVER_SPONGE_RANGE_WIDTH_BY_TIER;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> SERVER_SPONGE_RANGE_HEIGHT_BY_TIER;

    private static volatile @Nullable CommentedFileConfig clientConfigFile;
    private static volatile @Nullable CommentedFileConfig commonConfigFile;
    private static volatile @Nullable CommentedFileConfig serverConfigFile;

    private static volatile boolean clientShowHud = true;
    private static volatile int clientHudOffsetX;
    private static volatile int clientHudOffsetY;
    private static volatile float clientHudBackgroundOpacity = 0.82F;
    private static volatile int clientHudDisplayMs = 5_000;
    private static volatile boolean clientEnableShiftScrollSelection = true;
    private static volatile boolean clientEnableUpdateChecker = true;
    private static volatile boolean clientShowGuideMeHint = true;
    private static volatile boolean clientInvertDampNullInteraction;
    private static volatile boolean clientAnimateDockedNulls = true;
    private static volatile boolean clientShowFullDeepNullCounts;

    private static volatile boolean defaultTransferLocked;
    private static volatile boolean defaultAutoPickupEnabled = true;
    private static volatile boolean defaultAutoFeedingEnabled = true;
    private static volatile boolean defaultAutoSmeltingEnabled = true;
    private static volatile int defaultStoneworksAmount = 1;
    private static volatile boolean enableCraftingTweaksReturnIntegration = true;

    private static volatile boolean tagMatchingDisabled;
    private static volatile Set<Identifier> tagBlacklist = Set.of();
    private static volatile Set<Identifier> tagWhitelist = Set.of();
    private static volatile boolean serverEnableAutoPickup = true;
    private static volatile boolean serverEnableAutoFeeding = true;
    private static volatile boolean serverEnableAutoSmelting = true;
    private static volatile boolean serverEnableCompression = true;
    private static volatile boolean serverEnableStoneworks = true;
    private static volatile boolean serverEnableStoneGenerator = true;
    private static volatile boolean serverEnableObsidianGenerator = true;
    private static volatile boolean serverEnableSpongeUpgrade = true;
    private static volatile boolean serverVoidFullItemsOnPickup = true;
    private static volatile boolean serverVoidFullFluidsOnSponge = true;
    private static volatile boolean serverEnableChemicalStorage = true;
    private static volatile int dockGeneratorBufferSize = 64;
    private static volatile int[] itemCapacityByTier = DEFAULT_ITEM_CAPACITY_BY_TIER.clone();
    private static volatile int[] fluidCapacityByTier = DEFAULT_FLUID_CAPACITY_BY_TIER.clone();
    private static volatile int[] energyCapacityByTier = DEFAULT_ENERGY_CAPACITY_BY_TIER.clone();
    private static volatile int[] deepEnergyCapacityByTier = DEFAULT_DEEP_ENERGY_CAPACITY_BY_TIER.clone();
    private static volatile int[] energyTransferByTier = DEFAULT_ENERGY_TRANSFER_BY_TIER.clone();
    private static volatile int[] deepEnergyTransferByTier = DEFAULT_DEEP_ENERGY_TRANSFER_BY_TIER.clone();
    private static volatile int[] dampNullTankCountByTier = DEFAULT_DAMPNULL_TANK_COUNT_BY_TIER.clone();
    private static volatile int[] stoneGenerationRateByTier = DEFAULT_STONE_GENERATION_RATE_BY_TIER.clone();
    private static volatile int[] spongeAbsorbLimitByTier = DEFAULT_SPONGE_ABSORB_LIMIT_BY_TIER.clone();
    private static volatile int[] spongeRangeWidthByTier = DEFAULT_SPONGE_RANGE_WIDTH_BY_TIER.clone();
    private static volatile int[] spongeRangeHeightByTier = DEFAULT_SPONGE_RANGE_HEIGHT_BY_TIER.clone();

    static {
        CLIENT_BUILDER.push("hud");
        CLIENT_SHOW_HUD = CLIENT_BUILDER.comment("Show the DeepNull/DampNull HUD panel when selection changes.")
                .define("showHud", true);
        CLIENT_HUD_OFFSET_X = CLIENT_BUILDER.comment("Horizontal HUD offset in pixels.")
                .defineInRange("hudOffsetX", 0, -4000, 4000);
        CLIENT_HUD_OFFSET_Y = CLIENT_BUILDER.comment("Vertical HUD offset in pixels.")
                .defineInRange("hudOffsetY", 0, -4000, 4000);
        CLIENT_HUD_BACKGROUND_OPACITY = CLIENT_BUILDER.comment("HUD background opacity from 0.0 to 1.0.")
                .defineInRange("hudBackgroundOpacity", 0.82D, 0.0D, 1.0D);
        CLIENT_HUD_DISPLAY_MS = CLIENT_BUILDER.comment("How long the HUD stays visible after a selection change.")
                .defineInRange("hudDisplayMs", 5_000, 250, 60_000);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.push("controls");
        CLIENT_ENABLE_SHIFT_SCROLL_SELECTION = CLIENT_BUILDER.comment("Allow Shift + Scroll Wheel to cycle the selected slot/tank.")
                .define("enableShiftScrollSelection", true);
        CLIENT_INVERT_DAMPNULL_INTERACTION = CLIENT_BUILDER.comment("Invert DampNull use behavior so right click opens the GUI and Shift + Right Click performs fluid placement/interaction.")
                .define("invertDampNullInteraction", false);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.push("integration");
        CLIENT_ENABLE_UPDATE_CHECKER = CLIENT_BUILDER.comment("Enable the GitHub-backed update checker message that links to CurseForge.")
                .define("enableUpdateChecker", true);
        CLIENT_SHOW_GUIDEME_HINT = CLIENT_BUILDER.comment("Show a GuideME tooltip hint when GuideME is installed.")
                .define("showGuideMeHint", true);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.push("visuals");
        CLIENT_ANIMATE_DOCKED_NULLS = CLIENT_BUILDER.comment("Animate docked DeepNulls and DampNulls while rendering on the dock.")
                .define("animateDockedNulls", true);
        CLIENT_SHOW_FULL_DEEPNULL_COUNTS = CLIENT_BUILDER.comment("Show full item counts in DeepNull screens instead of compact 1.5K / 2M style numbers.")
                .define("showFullDeepNullCounts", false);
        CLIENT_BUILDER.pop();
        CLIENT_SPEC = CLIENT_BUILDER.build();

        COMMON_BUILDER.push("defaults");
        COMMON_DEFAULT_TRANSFER_LOCKED = COMMON_BUILDER.comment("Default transfer lock state for newly created nulls.")
                .define("defaultTransferLocked", false);
        COMMON_DEFAULT_AUTO_PICKUP_ENABLED = COMMON_BUILDER.comment("Default auto-pickup state for newly created DeepNulls.")
                .define("defaultAutoPickupEnabled", true);
        COMMON_DEFAULT_AUTO_FEEDING_ENABLED = COMMON_BUILDER.comment("Default auto-feeding state when the upgrade is installed.")
                .define("defaultAutoFeedingEnabled", true);
        COMMON_DEFAULT_AUTO_SMELTING_ENABLED = COMMON_BUILDER.comment("Default auto-smelting state when the upgrade is installed.")
                .define("defaultAutoSmeltingEnabled", true);
        COMMON_DEFAULT_STONEWORKS_AMOUNT = COMMON_BUILDER.comment("Default Stoneworks target item count for newly created DeepNulls.")
                .defineInRange("defaultStoneworksAmount", 1, 0, Integer.MAX_VALUE);
        COMMON_ENABLE_CRAFTINGTWEAKS_RETURN_INTEGRATION = COMMON_BUILDER.comment("Enable the Crafting Tweaks clear-grid integration that returns JEI-filled ingredients back into DeepNulls instead of normal player inventory.")
                .define("enableCraftingTweaksReturnIntegration", true);
        COMMON_BUILDER.pop();
        COMMON_SPEC = COMMON_BUILDER.build();

        SERVER_BUILDER.push("tagMatching");
        SERVER_DISABLE_TAG_MATCHING = SERVER_BUILDER
                .comment("Disable DeepNull tag-matching mode entirely.")
                .define("disableTagMatching", false);
        SERVER_TAG_BLACKLIST = SERVER_BUILDER
                .comment("Dictionary-style item tags that will not be allowed for tag matching unless explicitly whitelisted. Example: c:storage_blocks/coal")
                .defineListAllowEmpty("tagBlacklist", List.of(), DeepNullConfig::validateTagName);
        SERVER_TAG_WHITELIST = SERVER_BUILDER
                .comment("If non-empty, only these dictionary-style item tags will be allowed for tag matching. Example: c:ingots/copper")
                .defineListAllowEmpty("tagWhitelist", List.of(), DeepNullConfig::validateTagName);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("features");
        SERVER_ENABLE_AUTO_PICKUP = SERVER_BUILDER.comment("Enable automatic pickup into matching DeepNulls.")
                .define("enableAutoPickup", true);
        SERVER_ENABLE_AUTO_FEEDING = SERVER_BUILDER.comment("Enable Auto-Feeding Upgrade behavior.")
                .define("enableAutoFeeding", true);
        SERVER_ENABLE_AUTO_SMELTING = SERVER_BUILDER.comment("Enable Auto-Smelting Upgrade behavior.")
                .define("enableAutoSmelting", true);
        SERVER_ENABLE_COMPRESSION = SERVER_BUILDER.comment("Enable Basic and Advanced Compression Upgrade behavior.")
                .define("enableCompression", true);
        SERVER_ENABLE_STONEWORKS = SERVER_BUILDER.comment("Enable Stoneworks Upgrade behavior.")
                .define("enableStoneworks", true);
        SERVER_ENABLE_STONE_GENERATOR = SERVER_BUILDER.comment("Enable DampNull Stone Generator behavior.")
                .define("enableStoneGenerator", true);
        SERVER_ENABLE_OBSIDIAN_GENERATOR = SERVER_BUILDER.comment("Enable DampNull Obsidian Generator behavior.")
                .define("enableObsidianGenerator", true);
        SERVER_ENABLE_SPONGE_UPGRADE = SERVER_BUILDER.comment("Enable DampNull Sponge Upgrade behavior.")
                .define("enableSpongeUpgrade", true);
        SERVER_VOID_FULL_ITEMS_ON_PICKUP = SERVER_BUILDER.comment("When auto-pickup hits an item that is already stored in a matching DeepNull slot and that matching storage is full, still absorb and void the overflow instead of rejecting it.")
                .define("voidFullItemsOnPickup", true);
        SERVER_VOID_FULL_FLUIDS_ON_SPONGE = SERVER_BUILDER.comment("When the Sponge Upgrade hits a matching tank that is already full, still absorb and void those matching source blocks.")
                .define("voidFullFluidsOnSponge", true);
        SERVER_ENABLE_CHEMICAL_STORAGE = SERVER_BUILDER.comment("Enable Mekanism chemical storage and transfer when Mekanism is installed.")
                .define("enableChemicalStorage", true);
        SERVER_DOCK_GENERATOR_BUFFER_SIZE = SERVER_BUILDER.comment("Hidden dock generator buffer size used by DampNull generators.")
                .defineInRange("dockGeneratorBufferSize", 64, 1, 64);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("tiers");
        SERVER_ITEM_CAPACITY_BY_TIER = integerListConfig(SERVER_BUILDER, "itemCapacityByTier", DEFAULT_ITEM_CAPACITY_BY_TIER,
                "Per-slot item capacity by tier: Redstone, Lapis, Iron, Gold, Diamond, Emerald, Creative.");
        SERVER_FLUID_CAPACITY_BY_TIER = integerListConfig(SERVER_BUILDER, "fluidCapacityByTier", DEFAULT_FLUID_CAPACITY_BY_TIER,
                "Per-tank fluid capacity in mB by tier.");
        SERVER_ENERGY_CAPACITY_BY_TIER = integerListConfig(SERVER_BUILDER, "energyCapacityByTier", DEFAULT_ENERGY_CAPACITY_BY_TIER,
                "Base Energy Upgrade FE capacity by tier.");
        SERVER_DEEP_ENERGY_CAPACITY_BY_TIER = integerListConfig(SERVER_BUILDER, "deepEnergyCapacityByTier", DEFAULT_DEEP_ENERGY_CAPACITY_BY_TIER,
                "Deep Energy Upgrade FE capacity by tier.");
        SERVER_ENERGY_TRANSFER_BY_TIER = integerListConfig(SERVER_BUILDER, "energyTransferByTier", DEFAULT_ENERGY_TRANSFER_BY_TIER,
                "Base Energy Upgrade FE transfer rate by tier.");
        SERVER_DEEP_ENERGY_TRANSFER_BY_TIER = integerListConfig(SERVER_BUILDER, "deepEnergyTransferByTier", DEFAULT_DEEP_ENERGY_TRANSFER_BY_TIER,
                "Deep Energy Upgrade FE transfer rate by tier.");
        SERVER_DAMPNULL_TANK_COUNT_BY_TIER = integerListConfig(SERVER_BUILDER, "dampNullTankCountByTier", DEFAULT_DAMPNULL_TANK_COUNT_BY_TIER,
                "DampNull tank count by tier.");
        SERVER_STONE_GENERATION_RATE_BY_TIER = integerListConfig(SERVER_BUILDER, "stoneGenerationRateByTier", DEFAULT_STONE_GENERATION_RATE_BY_TIER,
                "Stone Generator output rate per second by tier.");
        SERVER_SPONGE_ABSORB_LIMIT_BY_TIER = integerListConfig(SERVER_BUILDER, "spongeAbsorbLimitByTier", DEFAULT_SPONGE_ABSORB_LIMIT_BY_TIER,
                "Legacy Sponge Upgrade absorb limit setting retained for compatibility. Sponge now absorbs all visible source blocks in range.");
        SERVER_SPONGE_RANGE_WIDTH_BY_TIER = integerListConfig(SERVER_BUILDER, "spongeRangeWidthByTier", DEFAULT_SPONGE_RANGE_WIDTH_BY_TIER,
                "Horizontal sponge scan size by tier.");
        SERVER_SPONGE_RANGE_HEIGHT_BY_TIER = integerListConfig(SERVER_BUILDER, "spongeRangeHeightByTier", DEFAULT_SPONGE_RANGE_HEIGHT_BY_TIER,
                "Vertical sponge scan size by tier.");
        SERVER_BUILDER.pop();
        SERVER_SPEC = SERVER_BUILDER.build();
    }

    private DeepNullConfig() {
    }

    public static void initializeDefaults() {
        bakeClient();
        bakeCommon();
        bakeServer();
    }

    public static void registerFabricConfigs(String modId) {
        clientConfigFile = loadConfig(modId, "client", CLIENT_SPEC);
        commonConfigFile = loadConfig(modId, "common", COMMON_SPEC);
        serverConfigFile = loadConfig(modId, "server", SERVER_SPEC);
        initializeDefaults();
    }

    public static boolean isHudEnabled() {
        return clientShowHud;
    }

    public static boolean toggleHudEnabled() {
        boolean next = !clientShowHud;
        CLIENT_SHOW_HUD.set(next);
        saveConfig(clientConfigFile);
        clientShowHud = next;
        return next;
    }

    public static int getHudOffsetX() {
        return clientHudOffsetX;
    }

    public static int getHudOffsetY() {
        return clientHudOffsetY;
    }

    public static float getHudBackgroundOpacity() {
        return clientHudBackgroundOpacity;
    }

    public static int getHudDisplayMs() {
        return clientHudDisplayMs;
    }

    public static boolean isShiftScrollSelectionEnabled() {
        return clientEnableShiftScrollSelection;
    }

    public static boolean isUpdateCheckerEnabled() {
        return clientEnableUpdateChecker;
    }

    public static boolean isGuideMeHintEnabled() {
        return clientShowGuideMeHint;
    }

    public static boolean isDampNullInteractionInverted() {
        return clientInvertDampNullInteraction;
    }

    public static boolean animateDockedNulls() {
        return clientAnimateDockedNulls;
    }

    public static boolean showFullDeepNullCounts() {
        return clientShowFullDeepNullCounts;
    }

    public static boolean defaultTransferLocked() {
        return defaultTransferLocked;
    }

    public static boolean defaultAutoPickupEnabled() {
        return defaultAutoPickupEnabled;
    }

    public static boolean defaultAutoFeedingEnabled() {
        return defaultAutoFeedingEnabled;
    }

    public static boolean defaultAutoSmeltingEnabled() {
        return defaultAutoSmeltingEnabled;
    }

    public static int defaultStoneworksAmount() {
        return defaultStoneworksAmount;
    }

    public static boolean enableCraftingTweaksReturnIntegration() {
        return enableCraftingTweaksReturnIntegration;
    }

    public static boolean isTagMatchingEnabled() {
        return !tagMatchingDisabled;
    }

    public static boolean isDictionaryTagAllowed(Identifier tagId) {
        if (!isTagMatchingEnabled()) {
            return false;
        }
        if (!tagWhitelist.isEmpty()) {
            return tagWhitelist.contains(tagId);
        }
        return !tagBlacklist.contains(tagId);
    }

    public static boolean isAutoPickupEnabled() {
        return serverEnableAutoPickup;
    }

    public static boolean isAutoFeedingEnabled() {
        return serverEnableAutoFeeding;
    }

    public static boolean isAutoSmeltingEnabled() {
        return serverEnableAutoSmelting;
    }

    public static boolean isCompressionEnabled() {
        return serverEnableCompression;
    }

    public static boolean isStoneworksEnabled() {
        return serverEnableStoneworks;
    }

    public static boolean isStoneGeneratorEnabled() {
        return serverEnableStoneGenerator;
    }

    public static boolean isObsidianGeneratorEnabled() {
        return serverEnableObsidianGenerator;
    }

    public static boolean isSpongeUpgradeEnabled() {
        return serverEnableSpongeUpgrade;
    }

    public static boolean voidFullItemsOnPickup() {
        return serverVoidFullItemsOnPickup;
    }

    public static boolean voidFullFluidsOnSponge() {
        return serverVoidFullFluidsOnSponge;
    }

    public static boolean isChemicalStorageEnabled() {
        return serverEnableChemicalStorage;
    }

    public static boolean isChemicalStorageAvailable() {
        return serverEnableChemicalStorage && ModList.get().isLoaded("mekanism");
    }

    public static int getDockGeneratorBufferSize() {
        return dockGeneratorBufferSize;
    }

    public static int getItemCapacity(DeepNullTier tier) {
        return tierValue(itemCapacityByTier, tier, DEFAULT_ITEM_CAPACITY_BY_TIER[tier.ordinalId()]);
    }

    public static int getFluidCapacity(DeepNullTier tier) {
        return tierValue(fluidCapacityByTier, tier, DEFAULT_FLUID_CAPACITY_BY_TIER[tier.ordinalId()]);
    }

    public static int getEnergyCapacity(DeepNullTier tier) {
        return tierValue(energyCapacityByTier, tier, DEFAULT_ENERGY_CAPACITY_BY_TIER[tier.ordinalId()]);
    }

    public static int getDeepEnergyCapacity(DeepNullTier tier) {
        return tierValue(deepEnergyCapacityByTier, tier, DEFAULT_DEEP_ENERGY_CAPACITY_BY_TIER[tier.ordinalId()]);
    }

    public static int getEnergyTransfer(DeepNullTier tier) {
        return tierValue(energyTransferByTier, tier, DEFAULT_ENERGY_TRANSFER_BY_TIER[tier.ordinalId()]);
    }

    public static int getDeepEnergyTransfer(DeepNullTier tier) {
        return tierValue(deepEnergyTransferByTier, tier, DEFAULT_DEEP_ENERGY_TRANSFER_BY_TIER[tier.ordinalId()]);
    }

    public static int getDampNullTankCount(DeepNullTier tier) {
        return tierValue(dampNullTankCountByTier, tier, DEFAULT_DAMPNULL_TANK_COUNT_BY_TIER[tier.ordinalId()]);
    }

    public static int getStoneGenerationRate(DeepNullTier tier) {
        return tierValue(stoneGenerationRateByTier, tier, DEFAULT_STONE_GENERATION_RATE_BY_TIER[tier.ordinalId()]);
    }

    public static int getSpongeAbsorbLimit(DeepNullTier tier) {
        return tierValue(spongeAbsorbLimitByTier, tier, DEFAULT_SPONGE_ABSORB_LIMIT_BY_TIER[tier.ordinalId()]);
    }

    public static int getSpongeRangeWidth(DeepNullTier tier) {
        return tierValue(spongeRangeWidthByTier, tier, DEFAULT_SPONGE_RANGE_WIDTH_BY_TIER[tier.ordinalId()]);
    }

    public static int getSpongeRangeHeight(DeepNullTier tier) {
        return tierValue(spongeRangeHeightByTier, tier, DEFAULT_SPONGE_RANGE_HEIGHT_BY_TIER[tier.ordinalId()]);
    }

    private static void bakeClient() {
        clientShowHud = CLIENT_SHOW_HUD.get();
        clientHudOffsetX = CLIENT_HUD_OFFSET_X.get();
        clientHudOffsetY = CLIENT_HUD_OFFSET_Y.get();
        clientHudBackgroundOpacity = CLIENT_HUD_BACKGROUND_OPACITY.get().floatValue();
        clientHudDisplayMs = CLIENT_HUD_DISPLAY_MS.get();
        clientEnableShiftScrollSelection = CLIENT_ENABLE_SHIFT_SCROLL_SELECTION.get();
        clientEnableUpdateChecker = CLIENT_ENABLE_UPDATE_CHECKER.get();
        clientShowGuideMeHint = CLIENT_SHOW_GUIDEME_HINT.get();
        clientInvertDampNullInteraction = CLIENT_INVERT_DAMPNULL_INTERACTION.get();
        clientAnimateDockedNulls = CLIENT_ANIMATE_DOCKED_NULLS.get();
        clientShowFullDeepNullCounts = CLIENT_SHOW_FULL_DEEPNULL_COUNTS.get();
    }

    private static void bakeCommon() {
        defaultTransferLocked = COMMON_DEFAULT_TRANSFER_LOCKED.get();
        defaultAutoPickupEnabled = COMMON_DEFAULT_AUTO_PICKUP_ENABLED.get();
        defaultAutoFeedingEnabled = COMMON_DEFAULT_AUTO_FEEDING_ENABLED.get();
        defaultAutoSmeltingEnabled = COMMON_DEFAULT_AUTO_SMELTING_ENABLED.get();
        defaultStoneworksAmount = COMMON_DEFAULT_STONEWORKS_AMOUNT.get();
        enableCraftingTweaksReturnIntegration = COMMON_ENABLE_CRAFTINGTWEAKS_RETURN_INTEGRATION.get();
    }

    private static void bakeServer() {
        tagMatchingDisabled = SERVER_DISABLE_TAG_MATCHING.get();
        tagBlacklist = normalizeTags(SERVER_TAG_BLACKLIST.get());
        tagWhitelist = normalizeTags(SERVER_TAG_WHITELIST.get());
        serverEnableAutoPickup = SERVER_ENABLE_AUTO_PICKUP.get();
        serverEnableAutoFeeding = SERVER_ENABLE_AUTO_FEEDING.get();
        serverEnableAutoSmelting = SERVER_ENABLE_AUTO_SMELTING.get();
        serverEnableCompression = SERVER_ENABLE_COMPRESSION.get();
        serverEnableStoneworks = SERVER_ENABLE_STONEWORKS.get();
        serverEnableStoneGenerator = SERVER_ENABLE_STONE_GENERATOR.get();
        serverEnableObsidianGenerator = SERVER_ENABLE_OBSIDIAN_GENERATOR.get();
        serverEnableSpongeUpgrade = SERVER_ENABLE_SPONGE_UPGRADE.get();
        serverVoidFullItemsOnPickup = SERVER_VOID_FULL_ITEMS_ON_PICKUP.get();
        serverVoidFullFluidsOnSponge = SERVER_VOID_FULL_FLUIDS_ON_SPONGE.get();
        serverEnableChemicalStorage = SERVER_ENABLE_CHEMICAL_STORAGE.get();
        dockGeneratorBufferSize = SERVER_DOCK_GENERATOR_BUFFER_SIZE.get();
        itemCapacityByTier = normalizeIntList(SERVER_ITEM_CAPACITY_BY_TIER.get(), DEFAULT_ITEM_CAPACITY_BY_TIER);
        fluidCapacityByTier = normalizeFluidCapacityByTier(SERVER_FLUID_CAPACITY_BY_TIER.get());
        if (Arrays.equals(fluidCapacityByTier, DEFAULT_FLUID_CAPACITY_BY_TIER)
                && !SERVER_FLUID_CAPACITY_BY_TIER.get().equals(intList(DEFAULT_FLUID_CAPACITY_BY_TIER))) {
            SERVER_FLUID_CAPACITY_BY_TIER.set(intList(DEFAULT_FLUID_CAPACITY_BY_TIER));
        }
        energyCapacityByTier = normalizeIntList(SERVER_ENERGY_CAPACITY_BY_TIER.get(), DEFAULT_ENERGY_CAPACITY_BY_TIER);
        deepEnergyCapacityByTier = normalizeIntList(SERVER_DEEP_ENERGY_CAPACITY_BY_TIER.get(), DEFAULT_DEEP_ENERGY_CAPACITY_BY_TIER);
        energyTransferByTier = normalizeIntList(SERVER_ENERGY_TRANSFER_BY_TIER.get(), DEFAULT_ENERGY_TRANSFER_BY_TIER);
        deepEnergyTransferByTier = normalizeIntList(SERVER_DEEP_ENERGY_TRANSFER_BY_TIER.get(), DEFAULT_DEEP_ENERGY_TRANSFER_BY_TIER);
        dampNullTankCountByTier = normalizeIntList(SERVER_DAMPNULL_TANK_COUNT_BY_TIER.get(), DEFAULT_DAMPNULL_TANK_COUNT_BY_TIER);
        stoneGenerationRateByTier = normalizeIntList(SERVER_STONE_GENERATION_RATE_BY_TIER.get(), DEFAULT_STONE_GENERATION_RATE_BY_TIER);
        spongeAbsorbLimitByTier = normalizeIntList(SERVER_SPONGE_ABSORB_LIMIT_BY_TIER.get(), DEFAULT_SPONGE_ABSORB_LIMIT_BY_TIER);
        spongeRangeWidthByTier = normalizeIntList(SERVER_SPONGE_RANGE_WIDTH_BY_TIER.get(), DEFAULT_SPONGE_RANGE_WIDTH_BY_TIER);
        spongeRangeHeightByTier = normalizeIntList(SERVER_SPONGE_RANGE_HEIGHT_BY_TIER.get(), DEFAULT_SPONGE_RANGE_HEIGHT_BY_TIER);
    }

    private static int tierValue(int[] configuredValues, DeepNullTier tier, int fallback) {
        int index = Math.max(0, Math.min(configuredValues.length - 1, tier.ordinalId()));
        return configuredValues.length == 0 ? fallback : configuredValues[index];
    }

    private static ForgeConfigSpec.ConfigValue<List<? extends Integer>> integerListConfig(
            ForgeConfigSpec.Builder builder,
            String name,
            int[] defaults,
            String comment
    ) {
        return builder.comment(comment)
                .defineListAllowEmpty(name, intList(defaults), DeepNullConfig::validateIntegerValue);
    }

    private static @Nullable CommentedFileConfig loadConfig(String modId, String suffix, ForgeConfigSpec spec) {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(modId + "-" + suffix + ".toml");
        try {
            Files.createDirectories(configPath.getParent());
            if (Files.notExists(configPath)) {
                Files.createFile(configPath);
            }

            CommentedFileConfig config = CommentedFileConfig.of(configPath, TomlFormat.instance());
            config.load();
            spec.correct(config);
            spec.acceptConfig(config);
            spec.afterReload();
            config.save();
            return config;
        } catch (IOException | RuntimeException exception) {
            DeepNullReforged.LOGGER.warn("Failed to load {} config from {}", suffix, configPath, exception);
            return null;
        }
    }

    private static void saveConfig(@Nullable CommentedFileConfig config) {
        if (config == null) {
            return;
        }
        try {
            config.save();
        } catch (RuntimeException exception) {
            DeepNullReforged.LOGGER.warn("Failed to save config {}", config.getNioPath(), exception);
        }
    }

    private static List<Integer> intList(int[] values) {
        return java.util.Arrays.stream(values).boxed().toList();
    }

    static int[] normalizeFluidCapacityByTier(List<? extends Integer> configuredValues) {
        int[] normalized = normalizeIntList(configuredValues, DEFAULT_FLUID_CAPACITY_BY_TIER);
        if (Arrays.equals(normalized, LEGACY_DEFAULT_FLUID_CAPACITY_BY_TIER)) {
            return DEFAULT_FLUID_CAPACITY_BY_TIER.clone();
        }
        return normalized;
    }

    private static int[] normalizeIntList(List<? extends Integer> configuredValues, int[] defaults) {
        int[] normalized = defaults.clone();
        if (configuredValues == null || configuredValues.isEmpty()) {
            return normalized;
        }
        int limit = Math.min(TIER_COUNT, configuredValues.size());
        for (int index = 0; index < limit; index++) {
            Integer value = configuredValues.get(index);
            if (value != null) {
                normalized[index] = Math.max(0, value);
            }
        }
        return normalized;
    }

    private static Set<Identifier> normalizeTags(List<? extends String> configuredTags) {
        LinkedHashSet<Identifier> normalized = new LinkedHashSet<>();
        for (String configuredTag : configuredTags) {
            Identifier parsed = Identifier.tryParse(configuredTag);
            if (parsed != null) {
                normalized.add(parsed);
            }
        }
        return Set.copyOf(normalized);
    }

    private static boolean validateTagName(Object value) {
        return value instanceof String tagName && Identifier.tryParse(tagName) != null;
    }

    private static boolean validateIntegerValue(Object value) {
        return value instanceof Number;
    }
}
