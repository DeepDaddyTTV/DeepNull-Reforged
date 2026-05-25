package dev.deepdaddyttv.deepnullreforged;

import dev.deepdaddyttv.deepnullreforged.client.theme.DeepNullUiTheme;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeData;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DeepNullConfig {
    private static final int TIER_COUNT = DeepNullTier.values().length;

    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec SERVER_SPEC;

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
    private static final int[] DEFAULT_DRIPNULL_PROFILE_SLOTS_BY_TIER = {
            1, 2, 3, 4, 5, 6, 12
    };
    private static final int[] DEFAULT_HEXNULL_LIBRARY_ENTRIES_BY_TIER = {
            8, 16, 24, 36, 54, 72, 256
    };
    private static final int[] DEFAULT_HEXNULL_STORED_LEVELS_BY_TIER = {
            16, 32, 64, 128, 256, 512, Integer.MAX_VALUE
    };
    private static final int[] DEFAULT_HEXNULL_XP_LEVEL_CAPACITY_BY_TIER = {
            8, 16, 32, 64, 128, 256, 1024
    };
    private static final List<String> DEFAULT_HEXNULL_XP_FLUID_VALUES = List.of(
            "mob_grinding_utils:fluid_xp=20",
            "industrialforegoing:essence=20",
            "sophisticatedcore:xp_still=20",
            "cyclic:xpjuice=20",
            "cofh_core:experience=20",
            "enderio:xp_juice=20"
    );
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

    private static final ModConfigSpec.BooleanValue CLIENT_SHOW_HUD;
    private static final ModConfigSpec.IntValue CLIENT_HUD_OFFSET_X;
    private static final ModConfigSpec.IntValue CLIENT_HUD_OFFSET_Y;
    private static final ModConfigSpec.DoubleValue CLIENT_HUD_BACKGROUND_OPACITY;
    private static final ModConfigSpec.IntValue CLIENT_HUD_DISPLAY_MS;
    private static final ModConfigSpec.BooleanValue CLIENT_ENABLE_SHIFT_SCROLL_SELECTION;
    private static final ModConfigSpec.BooleanValue CLIENT_ENABLE_UPDATE_CHECKER;
    private static final ModConfigSpec.BooleanValue CLIENT_SHOW_GUIDEME_HINT;
    private static final ModConfigSpec.BooleanValue CLIENT_INVERT_DAMPNULL_INTERACTION;
    private static final ModConfigSpec.BooleanValue CLIENT_ANIMATE_DOCKED_NULLS;
    private static final ModConfigSpec.BooleanValue CLIENT_SHOW_FULL_DEEPNULL_COUNTS;
    private static final ModConfigSpec.EnumValue<DeepNullUiTheme> CLIENT_UI_THEME;

    private static final ModConfigSpec.BooleanValue COMMON_DEFAULT_TRANSFER_LOCKED;
    private static final ModConfigSpec.BooleanValue COMMON_DEFAULT_AUTO_PICKUP_ENABLED;
    private static final ModConfigSpec.BooleanValue COMMON_DEFAULT_AUTO_FEEDING_ENABLED;
    private static final ModConfigSpec.BooleanValue COMMON_DEFAULT_AUTO_SMELTING_ENABLED;
    private static final ModConfigSpec.IntValue COMMON_DEFAULT_STONEWORKS_AMOUNT;
    private static final ModConfigSpec.BooleanValue COMMON_ENABLE_CRAFTINGTWEAKS_RETURN_INTEGRATION;

    private static final ModConfigSpec.BooleanValue SERVER_DISABLE_TAG_MATCHING;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> SERVER_TAG_BLACKLIST;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> SERVER_TAG_WHITELIST;
    private static final ModConfigSpec.BooleanValue SERVER_ENABLE_AUTO_PICKUP;
    private static final ModConfigSpec.BooleanValue SERVER_ENABLE_AUTO_FEEDING;
    private static final ModConfigSpec.BooleanValue SERVER_ENABLE_AUTO_SMELTING;
    private static final ModConfigSpec.BooleanValue SERVER_ENABLE_COMPRESSION;
    private static final ModConfigSpec.BooleanValue SERVER_ENABLE_STONEWORKS;
    private static final ModConfigSpec.BooleanValue SERVER_ENABLE_STONE_GENERATOR;
    private static final ModConfigSpec.BooleanValue SERVER_ENABLE_OBSIDIAN_GENERATOR;
    private static final ModConfigSpec.BooleanValue SERVER_ENABLE_SPONGE_UPGRADE;
    private static final ModConfigSpec.BooleanValue SERVER_VOID_FULL_ITEMS_ON_PICKUP;
    private static final ModConfigSpec.BooleanValue SERVER_VOID_FULL_FLUIDS_ON_SPONGE;
    private static final ModConfigSpec.BooleanValue SERVER_ENABLE_CHEMICAL_STORAGE;
    private static final ModConfigSpec.IntValue SERVER_DOCK_GENERATOR_BUFFER_SIZE;
    private static final ModConfigSpec.IntValue SERVER_DUMP_NULL_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue SERVER_DUMP_NULL_ENERGY_TRANSFER_RATE;
    private static final ModConfigSpec.IntValue SERVER_DUMP_NULL_COMMON_FE_REWARD;
    private static final ModConfigSpec.IntValue SERVER_DUMP_NULL_UNCOMMON_FE_REWARD;
    private static final ModConfigSpec.IntValue SERVER_DUMP_NULL_RARE_FE_REWARD;
    private static final ModConfigSpec.IntValue SERVER_DUMP_NULL_EPIC_FE_REWARD;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> SERVER_DUMP_NULL_ITEM_FE_REWARDS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> SERVER_DUMP_NULL_TAG_FE_REWARDS;
    private static final ModConfigSpec.IntValue SERVER_DEN_BREEDING_INTERVAL_TICKS;
    private static final ModConfigSpec.IntValue SERVER_DEN_BREEDING_MAX_BIRTHS_PER_CYCLE;
    private static final ModConfigSpec.IntValue SERVER_DEN_MILK_INTERVAL_TICKS;
    private static final ModConfigSpec.IntValue SERVER_DEN_MILK_MAX_BUCKETS_PER_CYCLE;
    private static final ModConfigSpec.IntValue SERVER_DEN_SHEAR_INTERVAL_TICKS;
    private static final ModConfigSpec.IntValue SERVER_DEN_SHEAR_MAX_SHEEP_PER_CYCLE;
    private static final ModConfigSpec.IntValue SERVER_DEN_CAPTURE_INTERVAL_TICKS;
    private static final ModConfigSpec.DoubleValue SERVER_DEN_CAPTURE_RADIUS;
    private static final ModConfigSpec.IntValue SERVER_DEN_CAPTURE_MAX_ENTITIES_PER_CYCLE;
    private static final ModConfigSpec.BooleanValue SERVER_DEN_TAG_UPGRADE_CONSUMES_NAME_TAGS;
    private static final ModConfigSpec.IntValue SERVER_DEN_FARM_INTERVAL_TICKS;
    private static final ModConfigSpec.IntValue SERVER_DEN_FARM_MAX_KILLS_PER_CYCLE;
    private static final ModConfigSpec.IntValue SERVER_DEN_FARM_LOOT_BUFFER_SLOTS;
    private static final ModConfigSpec.IntValue SERVER_DEN_FARM_DEFAULT_THRESHOLD;
    private static final ModConfigSpec.IntValue SERVER_DEEP_FARM_INTERVAL_TICKS;
    private static final ModConfigSpec.IntValue SERVER_DEEP_FARM_MAX_HARVESTS_PER_CYCLE;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> SERVER_HEXNULL_XP_FLUID_VALUES;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_ITEM_CAPACITY_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_FLUID_CAPACITY_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_ENERGY_CAPACITY_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_DEEP_ENERGY_CAPACITY_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_ENERGY_TRANSFER_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_DEEP_ENERGY_TRANSFER_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_DAMPNULL_TANK_COUNT_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_DRIPNULL_PROFILE_SLOTS_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_HEXNULL_LIBRARY_ENTRIES_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_HEXNULL_STORED_LEVELS_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_HEXNULL_XP_LEVEL_CAPACITY_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_STONE_GENERATION_RATE_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_SPONGE_ABSORB_LIMIT_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_SPONGE_RANGE_WIDTH_BY_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SERVER_SPONGE_RANGE_HEIGHT_BY_TIER;

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
    private static volatile DeepNullUiTheme clientUiTheme = DeepNullUiTheme.VANILLA;

    private static volatile boolean defaultTransferLocked;
    private static volatile boolean defaultAutoPickupEnabled = true;
    private static volatile boolean defaultAutoFeedingEnabled = true;
    private static volatile boolean defaultAutoSmeltingEnabled = true;
    private static volatile int defaultStoneworksAmount = 1;
    private static volatile boolean enableCraftingTweaksReturnIntegration = true;

    private static volatile boolean tagMatchingDisabled;
    private static volatile Set<ResourceLocation> tagBlacklist = Set.of();
    private static volatile Set<ResourceLocation> tagWhitelist = Set.of();
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
    private static volatile int dumpNullEnergyCapacity = 100_000;
    private static volatile int dumpNullEnergyTransferRate = 2_000;
    private static volatile int dumpNullCommonFeReward = 1;
    private static volatile int dumpNullUncommonFeReward = 4;
    private static volatile int dumpNullRareFeReward = 16;
    private static volatile int dumpNullEpicFeReward = 64;
    private static volatile Map<ResourceLocation, Integer> dumpNullItemFeRewards = Map.of();
    private static volatile Map<ResourceLocation, Integer> dumpNullTagFeRewards = Map.of();
    private static volatile int denBreedingIntervalTicks = 1200;
    private static volatile int denBreedingMaxBirthsPerCycle = 8;
    private static volatile int denMilkIntervalTicks = 200;
    private static volatile int denMilkMaxBucketsPerCycle = 8;
    private static volatile int denShearIntervalTicks = 600;
    private static volatile int denShearMaxSheepPerCycle = 16;
    private static volatile int denCaptureIntervalTicks = 20;
    private static volatile double denCaptureRadius = 5.0D;
    private static volatile int denCaptureMaxEntitiesPerCycle = 2;
    private static volatile boolean denTagUpgradeConsumesNameTags = true;
    private static volatile int denFarmIntervalTicks = 600;
    private static volatile int denFarmMaxKillsPerCycle = 16;
    private static volatile int denFarmLootBufferSlots = DenNullUpgradeData.LOOT_BUFFER_SLOTS;
    private static volatile int denFarmDefaultThreshold = DenNullUpgradeData.DEFAULT_FARM_THRESHOLD;
    private static volatile int deepFarmIntervalTicks = 1200;
    private static volatile int deepFarmMaxHarvestsPerCycle = 8;
    private static volatile Map<ResourceLocation, Integer> hexNullXpFluidValues = parseRewardOverrides(DEFAULT_HEXNULL_XP_FLUID_VALUES);
    private static volatile int[] itemCapacityByTier = DEFAULT_ITEM_CAPACITY_BY_TIER.clone();
    private static volatile int[] fluidCapacityByTier = DEFAULT_FLUID_CAPACITY_BY_TIER.clone();
    private static volatile int[] energyCapacityByTier = DEFAULT_ENERGY_CAPACITY_BY_TIER.clone();
    private static volatile int[] deepEnergyCapacityByTier = DEFAULT_DEEP_ENERGY_CAPACITY_BY_TIER.clone();
    private static volatile int[] energyTransferByTier = DEFAULT_ENERGY_TRANSFER_BY_TIER.clone();
    private static volatile int[] deepEnergyTransferByTier = DEFAULT_DEEP_ENERGY_TRANSFER_BY_TIER.clone();
    private static volatile int[] dampNullTankCountByTier = DEFAULT_DAMPNULL_TANK_COUNT_BY_TIER.clone();
    private static volatile int[] dripNullProfileSlotsByTier = DEFAULT_DRIPNULL_PROFILE_SLOTS_BY_TIER.clone();
    private static volatile int[] hexNullLibraryEntriesByTier = DEFAULT_HEXNULL_LIBRARY_ENTRIES_BY_TIER.clone();
    private static volatile int[] hexNullStoredLevelsByTier = DEFAULT_HEXNULL_STORED_LEVELS_BY_TIER.clone();
    private static volatile int[] hexNullXpLevelCapacityByTier = DEFAULT_HEXNULL_XP_LEVEL_CAPACITY_BY_TIER.clone();
    private static volatile int[] stoneGenerationRateByTier = DEFAULT_STONE_GENERATION_RATE_BY_TIER.clone();
    private static volatile int[] spongeAbsorbLimitByTier = DEFAULT_SPONGE_ABSORB_LIMIT_BY_TIER.clone();
    private static volatile int[] spongeRangeWidthByTier = DEFAULT_SPONGE_RANGE_WIDTH_BY_TIER.clone();
    private static volatile int[] spongeRangeHeightByTier = DEFAULT_SPONGE_RANGE_HEIGHT_BY_TIER.clone();

    static {
        CLIENT_BUILDER.push("hud");
        CLIENT_SHOW_HUD = CLIENT_BUILDER.comment("Show the Null HUD panel when selection changes.")
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
        CLIENT_ENABLE_SHIFT_SCROLL_SELECTION = CLIENT_BUILDER.comment("Allow Shift + Scroll Wheel to cycle the selected slot, tank, or Den entry.")
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
        CLIENT_UI_THEME = CLIENT_BUILDER.comment("Choose the active DeepNull UI theme: VANILLA, MINECRAFT_DARK, or LEGACY.")
                .defineEnum("uiTheme", DeepNullUiTheme.VANILLA);
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
                .defineListAllowEmpty("tagBlacklist", List.of(), () -> "", DeepNullConfig::validateTagName);
        SERVER_TAG_WHITELIST = SERVER_BUILDER
                .comment("If non-empty, only these dictionary-style item tags will be allowed for tag matching. Example: c:ingots/copper")
                .defineListAllowEmpty("tagWhitelist", List.of(), () -> "", DeepNullConfig::validateTagName);
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

        SERVER_BUILDER.push("dumpNull");
        SERVER_DUMP_NULL_ENERGY_CAPACITY = SERVER_BUILDER.comment("DumpNull Power Upgrade FE capacity.")
                .defineInRange("dumpNullEnergyCapacity", 100_000, 0, Integer.MAX_VALUE);
        SERVER_DUMP_NULL_ENERGY_TRANSFER_RATE = SERVER_BUILDER.comment("DumpNull Power Upgrade FE transfer rate through dock energy capability.")
                .defineInRange("dumpNullEnergyTransferRate", 2_000, 0, Integer.MAX_VALUE);
        SERVER_DUMP_NULL_COMMON_FE_REWARD = SERVER_BUILDER.comment("FE generated per discarded COMMON item when no item or tag override matches.")
                .defineInRange("dumpNullCommonFeReward", 1, 0, Integer.MAX_VALUE);
        SERVER_DUMP_NULL_UNCOMMON_FE_REWARD = SERVER_BUILDER.comment("FE generated per discarded UNCOMMON item when no item or tag override matches.")
                .defineInRange("dumpNullUncommonFeReward", 4, 0, Integer.MAX_VALUE);
        SERVER_DUMP_NULL_RARE_FE_REWARD = SERVER_BUILDER.comment("FE generated per discarded RARE item when no item or tag override matches.")
                .defineInRange("dumpNullRareFeReward", 16, 0, Integer.MAX_VALUE);
        SERVER_DUMP_NULL_EPIC_FE_REWARD = SERVER_BUILDER.comment("FE generated per discarded EPIC item when no item or tag override matches.")
                .defineInRange("dumpNullEpicFeReward", 64, 0, Integer.MAX_VALUE);
        SERVER_DUMP_NULL_ITEM_FE_REWARDS = SERVER_BUILDER.comment("Per-item DumpNull FE rewards. Format: namespace:path=amount")
                .defineListAllowEmpty("dumpNullItemFeRewards", List.of(), () -> "", DeepNullConfig::validateRewardOverride);
        SERVER_DUMP_NULL_TAG_FE_REWARDS = SERVER_BUILDER.comment("Per-tag DumpNull FE rewards. Format: namespace:path=amount, without the leading #")
                .defineListAllowEmpty("dumpNullTagFeRewards", List.of(), () -> "", DeepNullConfig::validateRewardOverride);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("denNull");
        SERVER_DEN_BREEDING_INTERVAL_TICKS = SERVER_BUILDER.comment("Ticks between DenNull Breeding Upgrade cycles.")
                .defineInRange("denBreedingIntervalTicks", 1200, 20, 72000);
        SERVER_DEN_BREEDING_MAX_BIRTHS_PER_CYCLE = SERVER_BUILDER.comment("Maximum internal births a DenNull can create in one Breeding cycle.")
                .defineInRange("denBreedingMaxBirthsPerCycle", 8, 1, 1024);
        SERVER_DEN_MILK_INTERVAL_TICKS = SERVER_BUILDER.comment("Ticks between DenNull Milk Upgrade cycles.")
                .defineInRange("denMilkIntervalTicks", 200, 20, 72000);
        SERVER_DEN_MILK_MAX_BUCKETS_PER_CYCLE = SERVER_BUILDER.comment("Maximum milk buckets a DenNull can create in one Milk cycle.")
                .defineInRange("denMilkMaxBucketsPerCycle", 8, 1, DenNullUpgradeData.MILK_BUCKET_CAPACITY);
        SERVER_DEN_SHEAR_INTERVAL_TICKS = SERVER_BUILDER.comment("Ticks between DenNull Shear Upgrade cycles.")
                .defineInRange("denShearIntervalTicks", 600, 20, 72000);
        SERVER_DEN_SHEAR_MAX_SHEEP_PER_CYCLE = SERVER_BUILDER.comment("Maximum stored sheep represented by one DenNull Shear cycle.")
                .defineInRange("denShearMaxSheepPerCycle", 16, 1, 1024);
        SERVER_DEN_CAPTURE_INTERVAL_TICKS = SERVER_BUILDER.comment("Ticks between carried DenNull Capture Upgrade scans.")
                .defineInRange("denCaptureIntervalTicks", 20, 5, 72000);
        SERVER_DEN_CAPTURE_RADIUS = SERVER_BUILDER.comment("Radius in blocks for carried DenNull Capture Upgrade scans.")
                .defineInRange("denCaptureRadius", 5.0D, 1.0D, 32.0D);
        SERVER_DEN_CAPTURE_MAX_ENTITIES_PER_CYCLE = SERVER_BUILDER.comment("Maximum entities a carried DenNull can auto-capture per scan.")
                .defineInRange("denCaptureMaxEntitiesPerCycle", 2, 1, 128);
        SERVER_DEN_TAG_UPGRADE_CONSUMES_NAME_TAGS = SERVER_BUILDER.comment("If true, DenNull Tag Upgrade consumes Name Tags for released entity names.")
                .define("denTagUpgradeConsumesNameTags", true);
        SERVER_DEN_FARM_INTERVAL_TICKS = SERVER_BUILDER.comment("Ticks between DenNull Farm Upgrade cycles.")
                .defineInRange("denFarmIntervalTicks", 600, 20, 72000);
        SERVER_DEN_FARM_MAX_KILLS_PER_CYCLE = SERVER_BUILDER.comment("Maximum virtual DenNull Farm kills in one cycle.")
                .defineInRange("denFarmMaxKillsPerCycle", 16, 1, 1024);
        SERVER_DEN_FARM_LOOT_BUFFER_SLOTS = SERVER_BUILDER.comment("Typed item slots available in the DenNull Farm loot buffer.")
                .defineInRange("denFarmLootBufferSlots", DenNullUpgradeData.LOOT_BUFFER_SLOTS, 1, DenNullUpgradeData.LOOT_BUFFER_SLOTS);
        SERVER_DEN_FARM_DEFAULT_THRESHOLD = SERVER_BUILDER.comment("Default per-entry DenNull population kept before Farm consumes surplus.")
                .defineInRange("denFarmDefaultThreshold", DenNullUpgradeData.DEFAULT_FARM_THRESHOLD, 0, Integer.MAX_VALUE);
        SERVER_DEEP_FARM_INTERVAL_TICKS = SERVER_BUILDER.comment("Ticks between DeepNull Farm Upgrade crop cycles.")
                .defineInRange("deepFarmIntervalTicks", 1200, 20, 72000);
        SERVER_DEEP_FARM_MAX_HARVESTS_PER_CYCLE = SERVER_BUILDER.comment("Maximum virtual crop harvests a DeepNull Farm can process in one cycle.")
                .defineInRange("deepFarmMaxHarvestsPerCycle", 8, 1, 1024);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("hexNull");
        SERVER_HEXNULL_XP_FLUID_VALUES = SERVER_BUILDER.comment("Fluid XP sources HexNull may drain from carried DampNulls. Format: namespace:path=mbPerLevel")
                .defineListAllowEmpty("hexNullXpFluidValues", DEFAULT_HEXNULL_XP_FLUID_VALUES, () -> "", DeepNullConfig::validateRewardOverride);
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
        SERVER_DRIPNULL_PROFILE_SLOTS_BY_TIER = integerListConfig(SERVER_BUILDER, "dripNullProfileSlotsByTier", DEFAULT_DRIPNULL_PROFILE_SLOTS_BY_TIER,
                "DripNull profile count by tier. Values above 12 are clamped by the profile menu.");
        SERVER_HEXNULL_LIBRARY_ENTRIES_BY_TIER = integerListConfig(SERVER_BUILDER, "hexNullLibraryEntriesByTier", DEFAULT_HEXNULL_LIBRARY_ENTRIES_BY_TIER,
                "HexNull unique enchantment library entries by tier.");
        SERVER_HEXNULL_STORED_LEVELS_BY_TIER = integerListConfig(SERVER_BUILDER, "hexNullStoredLevelsByTier", DEFAULT_HEXNULL_STORED_LEVELS_BY_TIER,
                "HexNull maximum stored level stock per enchantment by tier.");
        SERVER_HEXNULL_XP_LEVEL_CAPACITY_BY_TIER = integerListConfig(SERVER_BUILDER, "hexNullXpLevelCapacityByTier", DEFAULT_HEXNULL_XP_LEVEL_CAPACITY_BY_TIER,
                "HexNull internal anvil-level XP buffer capacity by tier.");
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

    public static void onLoad(ModConfigEvent.Loading event) {
        bakeFor(event.getConfig());
    }

    public static void onReload(ModConfigEvent.Reloading event) {
        bakeFor(event.getConfig());
    }

    public static boolean isHudEnabled() {
        return clientShowHud;
    }

    public static boolean toggleHudEnabled() {
        boolean next = !clientShowHud;
        CLIENT_SHOW_HUD.set(next);
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

    public static DeepNullUiTheme uiTheme() {
        return clientUiTheme;
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

    public static boolean isDictionaryTagAllowed(ResourceLocation tagId) {
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

    public static int getDockGeneratorBufferSize() {
        return dockGeneratorBufferSize;
    }

    public static int getDumpNullEnergyCapacity() {
        return dumpNullEnergyCapacity;
    }

    public static int getDumpNullEnergyTransferRate() {
        return dumpNullEnergyTransferRate;
    }

    public static int getDumpNullRarityFeReward(String rarityName) {
        return switch (rarityName == null ? "COMMON" : rarityName.toUpperCase(java.util.Locale.ROOT)) {
            case "UNCOMMON" -> dumpNullUncommonFeReward;
            case "RARE" -> dumpNullRareFeReward;
            case "EPIC" -> dumpNullEpicFeReward;
            default -> dumpNullCommonFeReward;
        };
    }

    public static int getDumpNullItemFeReward(ResourceLocation itemId) {
        return dumpNullItemFeRewards.getOrDefault(itemId, -1);
    }

    public static int getDumpNullTagFeReward(ResourceLocation tagId) {
        return dumpNullTagFeRewards.getOrDefault(tagId, -1);
    }

    public static int getDenBreedingIntervalTicks() {
        return denBreedingIntervalTicks;
    }

    public static int getDenBreedingMaxBirthsPerCycle() {
        return denBreedingMaxBirthsPerCycle;
    }

    public static int getDenMilkIntervalTicks() {
        return denMilkIntervalTicks;
    }

    public static int getDenMilkMaxBucketsPerCycle() {
        return denMilkMaxBucketsPerCycle;
    }

    public static int getDenShearIntervalTicks() {
        return denShearIntervalTicks;
    }

    public static int getDenShearMaxSheepPerCycle() {
        return denShearMaxSheepPerCycle;
    }

    public static int getDenCaptureIntervalTicks() {
        return denCaptureIntervalTicks;
    }

    public static double getDenCaptureRadius() {
        return denCaptureRadius;
    }

    public static int getDenCaptureMaxEntitiesPerCycle() {
        return denCaptureMaxEntitiesPerCycle;
    }

    public static boolean denTagUpgradeConsumesNameTags() {
        return denTagUpgradeConsumesNameTags;
    }

    public static int getDenFarmIntervalTicks() {
        return denFarmIntervalTicks;
    }

    public static int getDenFarmMaxKillsPerCycle() {
        return denFarmMaxKillsPerCycle;
    }

    public static int getDenFarmLootBufferSlots() {
        return denFarmLootBufferSlots;
    }

    public static int getDenFarmDefaultThreshold() {
        return denFarmDefaultThreshold;
    }

    public static int getDeepFarmIntervalTicks() {
        return deepFarmIntervalTicks;
    }

    public static int getDeepFarmMaxHarvestsPerCycle() {
        return deepFarmMaxHarvestsPerCycle;
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

    public static int getDripNullProfileCount(DeepNullTier tier) {
        DeepNullTier safeTier = tier == null ? DeepNullTier.REDSTONE : tier;
        int configured = tierValue(dripNullProfileSlotsByTier, safeTier, DEFAULT_DRIPNULL_PROFILE_SLOTS_BY_TIER[safeTier.ordinalId()]);
        return Math.max(1, Math.min(12, configured));
    }

    public static int getHexNullLibraryEntryLimit(DeepNullTier tier) {
        DeepNullTier safeTier = tier == null ? DeepNullTier.REDSTONE : tier;
        return Math.max(1, tierValue(hexNullLibraryEntriesByTier, safeTier, DEFAULT_HEXNULL_LIBRARY_ENTRIES_BY_TIER[safeTier.ordinalId()]));
    }

    public static int getHexNullStoredLevelLimit(DeepNullTier tier) {
        DeepNullTier safeTier = tier == null ? DeepNullTier.REDSTONE : tier;
        return Math.max(1, tierValue(hexNullStoredLevelsByTier, safeTier, DEFAULT_HEXNULL_STORED_LEVELS_BY_TIER[safeTier.ordinalId()]));
    }

    public static int getHexNullXpLevelCapacity(DeepNullTier tier) {
        DeepNullTier safeTier = tier == null ? DeepNullTier.REDSTONE : tier;
        return Math.max(0, tierValue(hexNullXpLevelCapacityByTier, safeTier, DEFAULT_HEXNULL_XP_LEVEL_CAPACITY_BY_TIER[safeTier.ordinalId()]));
    }

    public static int getHexNullXpFluidMbPerLevel(ResourceLocation fluidId) {
        if (fluidId == null) {
            return -1;
        }
        return hexNullXpFluidValues.getOrDefault(fluidId, -1);
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

    private static void bakeFor(ModConfig config) {
        Object spec = config.getSpec();
        if (spec == CLIENT_SPEC) {
            bakeClient();
        } else if (spec == COMMON_SPEC) {
            bakeCommon();
        } else if (spec == SERVER_SPEC) {
            bakeServer();
        }
    }

    private static void bakeClient() {
        clientShowHud = CLIENT_SHOW_HUD.getAsBoolean();
        clientHudOffsetX = CLIENT_HUD_OFFSET_X.getAsInt();
        clientHudOffsetY = CLIENT_HUD_OFFSET_Y.getAsInt();
        clientHudBackgroundOpacity = CLIENT_HUD_BACKGROUND_OPACITY.get().floatValue();
        clientHudDisplayMs = CLIENT_HUD_DISPLAY_MS.getAsInt();
        clientEnableShiftScrollSelection = CLIENT_ENABLE_SHIFT_SCROLL_SELECTION.getAsBoolean();
        clientEnableUpdateChecker = CLIENT_ENABLE_UPDATE_CHECKER.getAsBoolean();
        clientShowGuideMeHint = CLIENT_SHOW_GUIDEME_HINT.getAsBoolean();
        clientInvertDampNullInteraction = CLIENT_INVERT_DAMPNULL_INTERACTION.getAsBoolean();
        clientAnimateDockedNulls = CLIENT_ANIMATE_DOCKED_NULLS.getAsBoolean();
        clientShowFullDeepNullCounts = CLIENT_SHOW_FULL_DEEPNULL_COUNTS.getAsBoolean();
        clientUiTheme = CLIENT_UI_THEME.get();
    }

    private static void bakeCommon() {
        defaultTransferLocked = COMMON_DEFAULT_TRANSFER_LOCKED.getAsBoolean();
        defaultAutoPickupEnabled = COMMON_DEFAULT_AUTO_PICKUP_ENABLED.getAsBoolean();
        defaultAutoFeedingEnabled = COMMON_DEFAULT_AUTO_FEEDING_ENABLED.getAsBoolean();
        defaultAutoSmeltingEnabled = COMMON_DEFAULT_AUTO_SMELTING_ENABLED.getAsBoolean();
        defaultStoneworksAmount = COMMON_DEFAULT_STONEWORKS_AMOUNT.getAsInt();
        enableCraftingTweaksReturnIntegration = COMMON_ENABLE_CRAFTINGTWEAKS_RETURN_INTEGRATION.getAsBoolean();
    }

    private static void bakeServer() {
        tagMatchingDisabled = SERVER_DISABLE_TAG_MATCHING.getAsBoolean();
        tagBlacklist = normalizeTags(SERVER_TAG_BLACKLIST.get());
        tagWhitelist = normalizeTags(SERVER_TAG_WHITELIST.get());
        serverEnableAutoPickup = SERVER_ENABLE_AUTO_PICKUP.getAsBoolean();
        serverEnableAutoFeeding = SERVER_ENABLE_AUTO_FEEDING.getAsBoolean();
        serverEnableAutoSmelting = SERVER_ENABLE_AUTO_SMELTING.getAsBoolean();
        serverEnableCompression = SERVER_ENABLE_COMPRESSION.getAsBoolean();
        serverEnableStoneworks = SERVER_ENABLE_STONEWORKS.getAsBoolean();
        serverEnableStoneGenerator = SERVER_ENABLE_STONE_GENERATOR.getAsBoolean();
        serverEnableObsidianGenerator = SERVER_ENABLE_OBSIDIAN_GENERATOR.getAsBoolean();
        serverEnableSpongeUpgrade = SERVER_ENABLE_SPONGE_UPGRADE.getAsBoolean();
        serverVoidFullItemsOnPickup = SERVER_VOID_FULL_ITEMS_ON_PICKUP.getAsBoolean();
        serverVoidFullFluidsOnSponge = SERVER_VOID_FULL_FLUIDS_ON_SPONGE.getAsBoolean();
        serverEnableChemicalStorage = SERVER_ENABLE_CHEMICAL_STORAGE.getAsBoolean();
        dockGeneratorBufferSize = SERVER_DOCK_GENERATOR_BUFFER_SIZE.getAsInt();
        dumpNullEnergyCapacity = SERVER_DUMP_NULL_ENERGY_CAPACITY.getAsInt();
        dumpNullEnergyTransferRate = SERVER_DUMP_NULL_ENERGY_TRANSFER_RATE.getAsInt();
        dumpNullCommonFeReward = SERVER_DUMP_NULL_COMMON_FE_REWARD.getAsInt();
        dumpNullUncommonFeReward = SERVER_DUMP_NULL_UNCOMMON_FE_REWARD.getAsInt();
        dumpNullRareFeReward = SERVER_DUMP_NULL_RARE_FE_REWARD.getAsInt();
        dumpNullEpicFeReward = SERVER_DUMP_NULL_EPIC_FE_REWARD.getAsInt();
        dumpNullItemFeRewards = parseDumpNullEnergyOverrides(SERVER_DUMP_NULL_ITEM_FE_REWARDS.get());
        dumpNullTagFeRewards = parseDumpNullEnergyOverrides(SERVER_DUMP_NULL_TAG_FE_REWARDS.get());
        denBreedingIntervalTicks = SERVER_DEN_BREEDING_INTERVAL_TICKS.getAsInt();
        denBreedingMaxBirthsPerCycle = SERVER_DEN_BREEDING_MAX_BIRTHS_PER_CYCLE.getAsInt();
        denMilkIntervalTicks = SERVER_DEN_MILK_INTERVAL_TICKS.getAsInt();
        denMilkMaxBucketsPerCycle = SERVER_DEN_MILK_MAX_BUCKETS_PER_CYCLE.getAsInt();
        denShearIntervalTicks = SERVER_DEN_SHEAR_INTERVAL_TICKS.getAsInt();
        denShearMaxSheepPerCycle = SERVER_DEN_SHEAR_MAX_SHEEP_PER_CYCLE.getAsInt();
        denCaptureIntervalTicks = SERVER_DEN_CAPTURE_INTERVAL_TICKS.getAsInt();
        denCaptureRadius = SERVER_DEN_CAPTURE_RADIUS.getAsDouble();
        denCaptureMaxEntitiesPerCycle = SERVER_DEN_CAPTURE_MAX_ENTITIES_PER_CYCLE.getAsInt();
        denTagUpgradeConsumesNameTags = SERVER_DEN_TAG_UPGRADE_CONSUMES_NAME_TAGS.getAsBoolean();
        denFarmIntervalTicks = SERVER_DEN_FARM_INTERVAL_TICKS.getAsInt();
        denFarmMaxKillsPerCycle = SERVER_DEN_FARM_MAX_KILLS_PER_CYCLE.getAsInt();
        denFarmLootBufferSlots = SERVER_DEN_FARM_LOOT_BUFFER_SLOTS.getAsInt();
        denFarmDefaultThreshold = SERVER_DEN_FARM_DEFAULT_THRESHOLD.getAsInt();
        deepFarmIntervalTicks = SERVER_DEEP_FARM_INTERVAL_TICKS.getAsInt();
        deepFarmMaxHarvestsPerCycle = SERVER_DEEP_FARM_MAX_HARVESTS_PER_CYCLE.getAsInt();
        hexNullXpFluidValues = parseRewardOverrides(SERVER_HEXNULL_XP_FLUID_VALUES.get());
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
        dripNullProfileSlotsByTier = normalizeIntList(SERVER_DRIPNULL_PROFILE_SLOTS_BY_TIER.get(), DEFAULT_DRIPNULL_PROFILE_SLOTS_BY_TIER);
        hexNullLibraryEntriesByTier = normalizeIntList(SERVER_HEXNULL_LIBRARY_ENTRIES_BY_TIER.get(), DEFAULT_HEXNULL_LIBRARY_ENTRIES_BY_TIER);
        hexNullStoredLevelsByTier = normalizeIntList(SERVER_HEXNULL_STORED_LEVELS_BY_TIER.get(), DEFAULT_HEXNULL_STORED_LEVELS_BY_TIER);
        hexNullXpLevelCapacityByTier = normalizeIntList(SERVER_HEXNULL_XP_LEVEL_CAPACITY_BY_TIER.get(), DEFAULT_HEXNULL_XP_LEVEL_CAPACITY_BY_TIER);
        stoneGenerationRateByTier = normalizeIntList(SERVER_STONE_GENERATION_RATE_BY_TIER.get(), DEFAULT_STONE_GENERATION_RATE_BY_TIER);
        spongeAbsorbLimitByTier = normalizeIntList(SERVER_SPONGE_ABSORB_LIMIT_BY_TIER.get(), DEFAULT_SPONGE_ABSORB_LIMIT_BY_TIER);
        spongeRangeWidthByTier = normalizeIntList(SERVER_SPONGE_RANGE_WIDTH_BY_TIER.get(), DEFAULT_SPONGE_RANGE_WIDTH_BY_TIER);
        spongeRangeHeightByTier = normalizeIntList(SERVER_SPONGE_RANGE_HEIGHT_BY_TIER.get(), DEFAULT_SPONGE_RANGE_HEIGHT_BY_TIER);
    }

    private static int tierValue(int[] configuredValues, DeepNullTier tier, int fallback) {
        int index = Math.max(0, Math.min(configuredValues.length - 1, tier.ordinalId()));
        return configuredValues.length == 0 ? fallback : configuredValues[index];
    }

    private static ModConfigSpec.ConfigValue<List<? extends Integer>> integerListConfig(
            ModConfigSpec.Builder builder,
            String name,
            int[] defaults,
            String comment
    ) {
        return builder.comment(comment)
                .defineListAllowEmpty(name, intList(defaults), () -> 0, DeepNullConfig::validateIntegerValue);
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

    private static Set<ResourceLocation> normalizeTags(List<? extends String> configuredTags) {
        LinkedHashSet<ResourceLocation> normalized = new LinkedHashSet<>();
        for (String configuredTag : configuredTags) {
            ResourceLocation parsed = ResourceLocation.tryParse(configuredTag);
            if (parsed != null) {
                normalized.add(parsed);
            }
        }
        return Set.copyOf(normalized);
    }

    public static Map<ResourceLocation, Integer> parseDumpNullEnergyOverrides(List<? extends String> configuredRewards) {
        return parseRewardOverrides(configuredRewards);
    }

    public static Map<ResourceLocation, Integer> parseRewardOverrides(List<? extends String> configuredRewards) {
        LinkedHashMap<ResourceLocation, Integer> rewards = new LinkedHashMap<>();
        if (configuredRewards == null) {
            return Map.of();
        }
        for (String configuredReward : configuredRewards) {
            RewardOverride override = parseRewardOverride(configuredReward);
            if (override != null) {
                rewards.put(override.id(), override.amount());
            }
        }
        return Map.copyOf(rewards);
    }

    private static boolean validateTagName(Object value) {
        return value instanceof String tagName && ResourceLocation.tryParse(tagName) != null;
    }

    private static boolean validateRewardOverride(Object value) {
        return value instanceof String text && parseRewardOverride(text) != null;
    }

    private static boolean validateIntegerValue(Object value) {
        return value instanceof Number;
    }

    private static RewardOverride parseRewardOverride(String text) {
        if (text == null) {
            return null;
        }
        int separator = text.indexOf('=');
        if (separator <= 0 || separator >= text.length() - 1) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(text.substring(0, separator).trim());
        if (id == null) {
            return null;
        }
        try {
            int amount = Integer.parseInt(text.substring(separator + 1).trim());
            return new RewardOverride(id, Math.max(0, amount));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private record RewardOverride(ResourceLocation id, int amount) {
    }
}
