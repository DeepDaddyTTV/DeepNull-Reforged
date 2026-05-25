package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class DumpNullPresetCatalog {
    public static final String VANILLA = "vanilla";
    public static final String PEACEFUL = "peaceful";
    public static final String HOSTILE = "hostile";
    public static final String BIOME = "biome";
    public static final String DIMENSION = "dimension";
    public static final String BOSS = "boss";

    private static final List<ResourceLocation> BOSS_MOBS = List.of(
            ResourceLocation.withDefaultNamespace("ender_dragon"),
            ResourceLocation.withDefaultNamespace("wither"),
            ResourceLocation.withDefaultNamespace("warden"),
            ResourceLocation.withDefaultNamespace("elder_guardian")
    );

    private DumpNullPresetCatalog() {
    }

    public static List<DumpNullPresetOption> presetOptions() {
        return List.of(
                new DumpNullPresetOption(VANILLA, "dn.dumpnull.preset.vanilla", DumpNullPresetOption.TARGET_NONE),
                new DumpNullPresetOption(PEACEFUL, "dn.dumpnull.preset.peaceful", DumpNullPresetOption.TARGET_NONE),
                new DumpNullPresetOption(HOSTILE, "dn.dumpnull.preset.hostile", DumpNullPresetOption.TARGET_NONE),
                new DumpNullPresetOption(BIOME, "dn.dumpnull.preset.biome", DumpNullPresetOption.TARGET_BIOME),
                new DumpNullPresetOption(DIMENSION, "dn.dumpnull.preset.dimension", DumpNullPresetOption.TARGET_DIMENSION),
                new DumpNullPresetOption(BOSS, "dn.dumpnull.preset.boss", DumpNullPresetOption.TARGET_NONE)
        );
    }

    public static List<DumpNullPresetTarget> biomeTargets(ServerLevel level) {
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        return biomes.entrySet().stream()
                .map(entry -> new DumpNullPresetTarget(entry.getKey().location(), prettify(entry.getKey().location())))
                .sorted(Comparator.comparing(target -> target.id().toString()))
                .toList();
    }

    public static List<DumpNullPresetTarget> dimensionTargets(ServerLevel level) {
        return level.getServer().levelKeys().stream()
                .map(ResourceKey::location)
                .map(id -> new DumpNullPresetTarget(id, prettify(id)))
                .sorted(Comparator.comparing(target -> target.id().toString()))
                .toList();
    }

    public static Optional<DumpNullPresetResult> build(ServerLevel level, String presetId, Collection<ResourceLocation> targetIds) {
        Set<ResourceLocation> mobs = switch (presetId) {
            case VANILLA -> vanillaMobs();
            case PEACEFUL -> mobsByCategory(false);
            case HOSTILE -> mobsByCategory(true);
            case BIOME -> mobsFromBiomes(level, targetIds);
            case DIMENSION -> mobsFromDimensions(level, targetIds);
            case BOSS -> bossMobs();
            default -> Set.of();
        };
        if (mobs.isEmpty() || !isKnownPreset(presetId)) {
            return Optional.empty();
        }
        return Optional.of(new DumpNullPresetResult(presetId, sortedMobs(mobs), gearRules(presetId)));
    }

    public static boolean isKnownPreset(String presetId) {
        return VANILLA.equals(presetId)
                || PEACEFUL.equals(presetId)
                || HOSTILE.equals(presetId)
                || BIOME.equals(presetId)
                || DIMENSION.equals(presetId)
                || BOSS.equals(presetId);
    }

    public static DumpNullPresetResult withAcceptedRules(DumpNullPresetResult preset, Collection<ResourceLocation> acceptedItemIds) {
        if (preset == null || acceptedItemIds == null || acceptedItemIds.isEmpty()) {
            return preset;
        }
        Set<ResourceLocation> discarded = new LinkedHashSet<>();
        for (DumpNullRule rule : preset.presetRules()) {
            if (rule.action() == DumpNullRuleAction.VOID) {
                discarded.add(rule.itemId());
            }
        }

        List<DumpNullRule> rules = new ArrayList<>(preset.presetRules());
        acceptedItemIds.stream()
                .filter(id -> id != null && !discarded.contains(id))
                .distinct()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .map(id -> DumpNullRule.presetGenerated(DumpNullRuleAction.PASS, id, preset.presetId()))
                .forEach(rules::add);
        return new DumpNullPresetResult(preset.presetId(), preset.selectedMobs(), rules);
    }

    public static boolean isGearJunk(Item item) {
        return item instanceof ArmorItem
                || item instanceof AnimalArmorItem
                || item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof ProjectileWeaponItem
                || item instanceof ShieldItem
                || item instanceof SwordItem
                || item instanceof DiggerItem
                || item instanceof TieredItem
                || item instanceof TridentItem
                || item instanceof MaceItem;
    }

    private static Set<ResourceLocation> vanillaMobs() {
        Set<ResourceLocation> mobs = new LinkedHashSet<>();
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            if ("minecraft".equals(id.getNamespace()) && entityType.getCategory() != MobCategory.MISC) {
                mobs.add(id);
            }
        }
        return mobs;
    }

    private static Set<ResourceLocation> mobsByCategory(boolean hostile) {
        Set<ResourceLocation> mobs = new LinkedHashSet<>();
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            MobCategory category = entityType.getCategory();
            if (category == MobCategory.MISC) {
                continue;
            }
            if (hostile == (category == MobCategory.MONSTER)) {
                mobs.add(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
            }
        }
        return mobs;
    }

    private static Set<ResourceLocation> bossMobs() {
        Set<ResourceLocation> mobs = new LinkedHashSet<>();
        for (ResourceLocation bossMob : BOSS_MOBS) {
            if (BuiltInRegistries.ENTITY_TYPE.containsKey(bossMob)) {
                mobs.add(bossMob);
            }
        }
        return mobs;
    }

    private static Set<ResourceLocation> mobsFromBiomes(ServerLevel level, Collection<ResourceLocation> targetIds) {
        Set<ResourceLocation> mobs = new LinkedHashSet<>();
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        for (ResourceLocation targetId : targetIds) {
            ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, targetId);
            biomes.getOptional(biomeKey).ifPresent(biome -> collectBiomeMobs(biome, mobs));
        }
        return mobs;
    }

    private static Set<ResourceLocation> mobsFromDimensions(ServerLevel level, Collection<ResourceLocation> targetIds) {
        Set<ResourceLocation> mobs = new LinkedHashSet<>();
        for (ResourceLocation targetId : targetIds) {
            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, targetId);
            ServerLevel targetLevel = level.getServer().getLevel(dimensionKey);
            if (targetLevel == null) {
                continue;
            }
            for (Holder<Biome> biome : targetLevel.getChunkSource().getGenerator().getBiomeSource().possibleBiomes()) {
                collectBiomeMobs(biome.value(), mobs);
            }
        }
        return mobs;
    }

    private static void collectBiomeMobs(Biome biome, Set<ResourceLocation> mobs) {
        for (MobCategory category : MobCategory.values()) {
            if (category == MobCategory.MISC) {
                continue;
            }
            for (MobSpawnSettings.SpawnerData spawner : biome.getMobSettings().getMobs(category).unwrap()) {
                ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(spawner.type);
                if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
                    mobs.add(entityId);
                }
            }
        }
    }

    private static List<DumpNullRule> gearRules(String presetId) {
        List<DumpNullRule> rules = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (isGearJunk(item)) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                rules.add(DumpNullRule.presetGenerated(DumpNullRuleAction.VOID, itemId, presetId));
            }
        }
        rules.sort(Comparator.comparing(rule -> rule.itemId().toString()));
        return rules;
    }

    private static List<ResourceLocation> sortedMobs(Set<ResourceLocation> mobs) {
        return mobs.stream()
                .filter(DumpNullCatalog::isRegisteredEntity)
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();
    }

    private static String prettify(ResourceLocation id) {
        String path = id.getPath().replace('_', ' ').replace('/', ' ');
        return id.getNamespace() + ": " + path.toLowerCase(Locale.ROOT);
    }
}
