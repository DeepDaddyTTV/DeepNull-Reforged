package dev.deepdaddyttv.deepnullreforged.nullseed;

import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatus;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DockableNullItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class NullSeedPresetCatalog {
    public static final String BUILT_IN_ORES = "builtin:ores";
    public static final String BUILT_IN_FARMING = "builtin:farming";
    public static final String BUILT_IN_REDSTONE = "builtin:redstone";
    public static final String BUILT_IN_MOB_DROPS = "builtin:mob_drops";
    public static final String BUILT_IN_FLUIDS = "builtin:fluids";
    public static final String BUILT_IN_CHEMICALS = "builtin:chemicals";
    public static final String BUILT_IN_ENTITIES = "builtin:entities";
    public static final String BUILT_IN_ENCHANTMENTS = "builtin:enchantments";

    private NullSeedPresetCatalog() {
    }

    public static List<NullSeedPreset> all(ServerLevel level, NullSeedKind targetKind) {
        List<NullSeedPreset> presets = new ArrayList<>();
        presets.addAll(builtIns(level));
        presets.addAll(generatedModPresets(targetKind));
        if (level != null) {
            presets.addAll(NullSeedPresetSavedData.get(level).presetsFor(targetKind));
        }
        return presets.stream()
                .filter(preset -> preset.supports(targetKind) && !preset.entries().isEmpty())
                .sorted(Comparator
                        .comparingInt((NullSeedPreset preset) -> preset.source().ordinal())
                        .thenComparing(NullSeedPreset::namespace)
                        .thenComparing(NullSeedPreset::displayName)
                        .thenComparing(NullSeedPreset::presetId))
                .toList();
    }

    public static Optional<NullSeedPreset> find(ServerLevel level, NullSeedKind targetKind, String presetId) {
        if (presetId == null || presetId.isBlank()) {
            return Optional.empty();
        }
        return all(level, targetKind).stream()
                .filter(preset -> preset.presetId().equals(presetId))
                .findFirst();
    }

    private static List<NullSeedPreset> builtIns(ServerLevel level) {
        List<NullSeedPreset> presets = new ArrayList<>();
        presets.add(itemPreset(BUILT_IN_ORES, "Ores", ItemSetPresetOption.ORES, DumpNullItemStatus.ACCEPTED));
        presets.add(itemPreset(BUILT_IN_FARMING, "Farming", ItemSetPresetOption.FARMING, DumpNullItemStatus.ACCEPTED));
        presets.add(itemPreset(BUILT_IN_REDSTONE, "Redstone", ItemSetPresetOption.REDSTONE, DumpNullItemStatus.ACCEPTED));
        presets.add(itemPreset(BUILT_IN_MOB_DROPS, "Mob Drops", ItemSetPresetOption.MOB_DROPS, DumpNullItemStatus.DISCARDED));
        presets.add(fluidPreset(BUILT_IN_FLUIDS, "Fluids", "minecraft", allFluidEntries("minecraft")));
        presets.add(entityPreset(BUILT_IN_ENTITIES, "Entities", "minecraft", allEntityEntries("minecraft")));
        presets.add(enchantmentFallbackPreset(level));
        if (ModList.get().isLoaded("mekanism")) {
            chemicalPreset().ifPresent(presets::add);
        }
        return presets;
    }

    private static NullSeedPreset itemPreset(String id, String name, String itemPresetId, DumpNullItemStatus dumpStatus) {
        List<ItemSetPresetEntry> itemEntries = ItemSetPresetCatalog.build(itemPresetId);
        List<NullSeedEntry> entries = new ArrayList<>();
        for (int i = 0; i < itemEntries.size(); i++) {
            ResourceLocation itemId = itemEntries.get(i).itemId();
            entries.add(NullSeedEntry.item(itemId, i));
        }
        for (ItemSetPresetEntry entry : itemEntries) {
            entries.add(NullSeedEntry.dumpRule(entry.itemId(), dumpStatus));
        }
        return new NullSeedPreset(
                id,
                name,
                NullSeedPresetSource.BUILT_IN,
                "minecraft",
                List.of(NullSeedKind.ITEM, NullSeedKind.DUMP_RULE),
                entries
        );
    }

    private static NullSeedPreset fluidPreset(String id, String name, String namespace, List<NullSeedEntry> entries) {
        return new NullSeedPreset(id, name, NullSeedPresetSource.BUILT_IN, namespace, List.of(NullSeedKind.FLUID), entries);
    }

    private static NullSeedPreset entityPreset(String id, String name, String namespace, List<NullSeedEntry> entries) {
        return new NullSeedPreset(id, name, NullSeedPresetSource.BUILT_IN, namespace, List.of(NullSeedKind.ENTITY), entries);
    }

    private static Optional<NullSeedPreset> chemicalPreset() {
        List<NullSeedEntry> chemicals = NullSeedMekanismBridge.chemicalEntries();
        if (chemicals.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new NullSeedPreset(
                BUILT_IN_CHEMICALS,
                "Chemicals / Gases",
                NullSeedPresetSource.BUILT_IN,
                "mekanism",
                List.of(NullSeedKind.FLUID),
                chemicals
        ));
    }

    private static NullSeedPreset enchantmentFallbackPreset(ServerLevel level) {
        List<NullSeedEntry> entries = new ArrayList<>();
        if (BuiltInRegistries.ITEM.containsKey(ResourceLocation.withDefaultNamespace("enchanted_book"))) {
            entries.add(NullSeedEntry.dumpRule(ResourceLocation.withDefaultNamespace("enchanted_book"), DumpNullItemStatus.ACCEPTED));
        }
        return new NullSeedPreset(
                BUILT_IN_ENCHANTMENTS,
                "Enchantments",
                NullSeedPresetSource.BUILT_IN,
                "minecraft",
                List.of(NullSeedKind.DUMP_RULE),
                entries
        );
    }

    private static List<NullSeedPreset> generatedModPresets(NullSeedKind targetKind) {
        Map<String, List<NullSeedEntry>> grouped = new LinkedHashMap<>();
        if (targetKind == NullSeedKind.ITEM || targetKind == NullSeedKind.DUMP_RULE) {
            for (Item item : BuiltInRegistries.ITEM) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                if (id == null || item == Items.AIR || item instanceof DeepNullItem || item instanceof DockableNullItem) {
                    continue;
                }
                if ("deepnullreforged".equals(id.getNamespace())) {
                    continue;
                }
                grouped.computeIfAbsent(id.getNamespace(), ignored -> new ArrayList<>()).add(
                        targetKind == NullSeedKind.DUMP_RULE
                                ? NullSeedEntry.dumpRule(id, DumpNullItemStatus.ACCEPTED)
                                : NullSeedEntry.item(id, grouped.getOrDefault(id.getNamespace(), List.of()).size())
                );
            }
        } else if (targetKind == NullSeedKind.FLUID) {
            for (Fluid fluid : BuiltInRegistries.FLUID) {
                ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
                if (!isUsableFluid(id, fluid)) {
                    continue;
                }
                grouped.computeIfAbsent(id.getNamespace(), ignored -> new ArrayList<>()).add(
                        NullSeedEntry.fluid(id, grouped.getOrDefault(id.getNamespace(), List.of()).size(), 1000)
                );
            }
            if (ModList.get().isLoaded("mekanism")) {
                for (NullSeedEntry chemical : NullSeedMekanismBridge.chemicalEntries()) {
                    String namespace = chemical.id().getNamespace();
                    grouped.computeIfAbsent(namespace, ignored -> new ArrayList<>()).add(chemical.withTargetIndex(grouped.getOrDefault(namespace, List.of()).size()));
                }
            }
        } else if (targetKind == NullSeedKind.ENTITY) {
            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
                if (id == null || entityType.getCategory() == MobCategory.MISC) {
                    continue;
                }
                grouped.computeIfAbsent(id.getNamespace(), ignored -> new ArrayList<>()).add(
                        NullSeedEntry.entity(id, grouped.getOrDefault(id.getNamespace(), List.of()).size())
                );
            }
        }

        List<NullSeedPreset> presets = new ArrayList<>();
        for (Map.Entry<String, List<NullSeedEntry>> entry : grouped.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            String namespace = entry.getKey();
            presets.add(new NullSeedPreset(
                    "mod:" + targetKind.name().toLowerCase(Locale.ROOT) + ":" + namespace,
                    modDisplayName(namespace),
                    NullSeedPresetSource.MOD,
                    namespace,
                    List.of(targetKind),
                    entry.getValue()
            ));
        }
        return presets;
    }

    private static List<NullSeedEntry> allFluidEntries(String preferredNamespace) {
        List<NullSeedEntry> entries = new ArrayList<>();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
            if (!isUsableFluid(id, fluid) || !preferredNamespace.equals(id.getNamespace())) {
                continue;
            }
            entries.add(NullSeedEntry.fluid(id, entries.size(), "lava".equals(id.getPath()) ? 1000 : 1000));
        }
        entries.sort(Comparator.comparing(entry -> entry.id().toString()));
        for (int i = 0; i < entries.size(); i++) {
            entries.set(i, entries.get(i).withTargetIndex(i));
        }
        return entries;
    }

    private static boolean isUsableFluid(ResourceLocation id, Fluid fluid) {
        return id != null
                && fluid != Fluids.EMPTY
                && BuiltInRegistries.FLUID.containsKey(id)
                && !id.getPath().startsWith("flowing_");
    }

    private static List<NullSeedEntry> allEntityEntries(String preferredNamespace) {
        List<NullSeedEntry> entries = new ArrayList<>();
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            if (id == null || entityType.getCategory() == MobCategory.MISC || !preferredNamespace.equals(id.getNamespace())) {
                continue;
            }
            entries.add(NullSeedEntry.entity(id, entries.size()));
        }
        entries.sort(Comparator.comparing(entry -> entry.id().toString()));
        for (int i = 0; i < entries.size(); i++) {
            entries.set(i, entries.get(i).withTargetIndex(i));
        }
        return entries;
    }

    private static String modDisplayName(String namespace) {
        return ModList.get().getModContainerById(namespace)
                .map(container -> container.getModInfo().getDisplayName())
                .filter(name -> name != null && !name.isBlank())
                .orElse(namespace);
    }
}
