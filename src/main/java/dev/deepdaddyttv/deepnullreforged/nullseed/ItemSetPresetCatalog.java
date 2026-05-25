package dev.deepdaddyttv.deepnullreforged.nullseed;

import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DockableNullItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class ItemSetPresetCatalog {
    private static final Set<String> ORE_FALLBACKS = Set.of(
            "coal", "charcoal", "raw_iron", "raw_gold", "raw_copper", "iron_ingot", "gold_ingot", "copper_ingot",
            "iron_nugget", "gold_nugget", "diamond", "emerald", "lapis_lazuli", "redstone", "quartz", "amethyst_shard",
            "coal_ore", "deepslate_coal_ore", "iron_ore", "deepslate_iron_ore", "gold_ore", "deepslate_gold_ore",
            "copper_ore", "deepslate_copper_ore", "diamond_ore", "deepslate_diamond_ore", "emerald_ore",
            "deepslate_emerald_ore", "lapis_ore", "deepslate_lapis_ore", "redstone_ore", "deepslate_redstone_ore",
            "nether_quartz_ore", "nether_gold_ore", "raw_iron_block", "raw_gold_block", "raw_copper_block",
            "iron_block", "gold_block", "copper_block", "diamond_block", "emerald_block", "lapis_block", "redstone_block",
            "quartz_block", "amethyst_block"
    );
    private static final Set<String> FARMING_FALLBACKS = Set.of(
            "oak_sapling", "spruce_sapling", "birch_sapling", "jungle_sapling", "acacia_sapling", "dark_oak_sapling",
            "mangrove_propagule", "cherry_sapling", "wheat_seeds", "pumpkin_seeds", "melon_seeds", "beetroot_seeds",
            "torchflower_seeds", "pitcher_pod", "wheat", "carrot", "potato", "beetroot", "pumpkin", "melon_slice",
            "sugar_cane", "cactus", "bamboo", "kelp", "sweet_berries", "glow_berries", "apple", "bread",
            "brown_mushroom", "red_mushroom", "cocoa_beans", "nether_wart"
    );
    private static final Set<String> REDSTONE_FALLBACKS = Set.of(
            "redstone", "redstone_torch", "repeater", "comparator", "observer", "piston", "sticky_piston", "dispenser",
            "dropper", "hopper", "lever", "tripwire_hook", "daylight_detector", "target", "note_block", "tnt",
            "redstone_lamp", "redstone_block", "powered_rail", "detector_rail", "activator_rail", "rail",
            "sculk_sensor", "calibrated_sculk_sensor", "crafter"
    );
    private static final Set<String> MOB_DROP_FALLBACKS = Set.of(
            "rotten_flesh", "bone", "arrow", "string", "spider_eye", "gunpowder", "ender_pearl", "slime_ball",
            "magma_cream", "blaze_rod", "blaze_powder", "ghast_tear", "leather", "feather", "rabbit_hide",
            "rabbit_foot", "phantom_membrane", "prismarine_shard", "prismarine_crystals", "ink_sac", "glow_ink_sac",
            "shulker_shell", "nautilus_shell", "trident"
    );
    private static final Set<String> BUILDING_FALLBACKS = Set.of(
            "oak_log", "spruce_log", "birch_log", "jungle_log", "acacia_log", "dark_oak_log", "mangrove_log", "cherry_log",
            "oak_planks", "spruce_planks", "birch_planks", "jungle_planks", "acacia_planks", "dark_oak_planks", "mangrove_planks", "cherry_planks",
            "stone", "cobblestone", "smooth_stone", "stone_bricks", "bricks", "glass", "sand", "red_sand", "gravel",
            "dirt", "coarse_dirt", "mud", "clay", "terracotta", "white_concrete", "gray_concrete", "black_concrete"
    );
    private static final Set<String> QUARRY_FALLBACKS = Set.of(
            "cobblestone", "stone", "deepslate", "cobbled_deepslate", "granite", "diorite", "andesite", "tuff",
            "calcite", "basalt", "blackstone", "netherrack", "end_stone", "gravel", "sand", "red_sand", "clay",
            "flint", "dripstone_block", "pointed_dripstone"
    );

    private ItemSetPresetCatalog() {
    }

    public static List<ItemSetPresetEntry> build(String presetId) {
        if (!ItemSetPresetOption.isKnown(presetId)) {
            return List.of();
        }
        Map<ResourceLocation, ItemSetPresetEntry> entries = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (item == Items.AIR || item instanceof DeepNullItem || item instanceof DockableNullItem || id == null) {
                continue;
            }
            String source = sourceFor(presetId, item, id);
            if (!source.isEmpty()) {
                entries.put(id, new ItemSetPresetEntry(id, label(id), source));
            }
        }
        fallbackSet(presetId).forEach(path -> {
            ResourceLocation id = ResourceLocation.withDefaultNamespace(path);
            if (BuiltInRegistries.ITEM.containsKey(id)) {
                entries.putIfAbsent(id, new ItemSetPresetEntry(id, label(id), "vanilla"));
            }
        });
        return entries.values().stream()
                .sorted(Comparator.comparing(ItemSetPresetEntry::label).thenComparing(entry -> entry.itemId().toString()))
                .toList();
    }

    public static List<ItemSetPresetEntry> entriesForIds(List<ResourceLocation> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return List.of();
        }
        List<ItemSetPresetEntry> entries = new ArrayList<>();
        for (ResourceLocation itemId : itemIds) {
            if (itemId != null && BuiltInRegistries.ITEM.containsKey(itemId)) {
                entries.add(new ItemSetPresetEntry(itemId, label(itemId), "custom"));
            }
        }
        return entries;
    }

    private static String sourceFor(String presetId, Item item, ResourceLocation id) {
        List<String> tagPaths = tagLocations(item).map(ResourceLocation::getPath).map(ItemSetPresetCatalog::lower).toList();
        String path = lower(id.getPath());
        return switch (presetId) {
            case ItemSetPresetOption.ORES -> matchesOre(path, tagPaths) ? "tag/path" : "";
            case ItemSetPresetOption.FARMING -> matchesFarming(path, tagPaths) ? "tag/path" : "";
            case ItemSetPresetOption.REDSTONE -> matchesRedstone(path, tagPaths) ? "tag/path" : "";
            case ItemSetPresetOption.MOB_DROPS -> matchesMobDrop(path, tagPaths) ? "tag/path" : "";
            case ItemSetPresetOption.BUILDING -> matchesBuilding(path, tagPaths) ? "tag/path" : "";
            case ItemSetPresetOption.QUARRY -> matchesQuarry(path, tagPaths) ? "tag/path" : "";
            default -> "";
        };
    }

    private static boolean matchesOre(String path, List<String> tagPaths) {
        return hasAnyTagToken(tagPaths, "ores", "raw_materials", "ingots", "nuggets", "gems", "dusts")
                || hasAnyPathToken(path, "ore", "raw_", "_ingot", "_nugget", "_gem", "_dust");
    }

    private static boolean matchesFarming(String path, List<String> tagPaths) {
        return hasAnyTagToken(tagPaths, "saplings", "seeds", "crops", "foods", "mushrooms", "flowers", "leaves")
                || hasAnyPathToken(path, "sapling", "seed", "crop", "wheat", "carrot", "potato", "beetroot", "pumpkin", "melon", "berry", "mushroom", "cactus", "bamboo", "sugar_cane", "kelp", "wart");
    }

    private static boolean matchesRedstone(String path, List<String> tagPaths) {
        return hasAnyTagToken(tagPaths, "redstone", "rails", "buttons", "pressure_plates")
                || hasAnyPathToken(path, "redstone", "repeater", "comparator", "observer", "piston", "dispenser", "dropper", "hopper", "rail", "lever", "tripwire", "detector", "sensor", "lamp", "target", "crafter");
    }

    private static boolean matchesMobDrop(String path, List<String> tagPaths) {
        return hasAnyTagToken(tagPaths, "mob_drops", "bones", "gunpowders", "ender_pearls", "slimeballs", "leathers", "feathers", "strings")
                || hasAnyPathToken(path, "bone", "arrow", "string", "eye", "gunpowder", "flesh", "pearl", "slime", "blaze", "ghast", "leather", "feather", "membrane", "shulker", "ink_sac", "prismarine", "trident");
    }

    private static boolean matchesBuilding(String path, List<String> tagPaths) {
        return hasAnyTagToken(tagPaths, "logs", "planks", "stone", "bricks", "glass", "sand", "terracotta", "concrete")
                || hasAnyPathToken(path, "log", "wood", "planks", "stone", "brick", "glass", "sand", "dirt", "mud", "clay", "terracotta", "concrete");
    }

    private static boolean matchesQuarry(String path, List<String> tagPaths) {
        return hasAnyTagToken(tagPaths, "stone", "cobblestone", "deepslate", "gravel", "sand", "ores")
                || hasAnyPathToken(path, "stone", "cobble", "deepslate", "granite", "diorite", "andesite", "tuff", "calcite", "basalt", "blackstone", "netherrack", "end_stone", "gravel", "sand", "clay", "dripstone");
    }

    private static Set<String> fallbackSet(String presetId) {
        return switch (presetId) {
            case ItemSetPresetOption.ORES -> ORE_FALLBACKS;
            case ItemSetPresetOption.FARMING -> FARMING_FALLBACKS;
            case ItemSetPresetOption.REDSTONE -> REDSTONE_FALLBACKS;
            case ItemSetPresetOption.MOB_DROPS -> MOB_DROP_FALLBACKS;
            case ItemSetPresetOption.BUILDING -> BUILDING_FALLBACKS;
            case ItemSetPresetOption.QUARRY -> QUARRY_FALLBACKS;
            default -> Set.of();
        };
    }

    private static Stream<ResourceLocation> tagLocations(Item item) {
        Stream<ResourceLocation> itemTags = item.builtInRegistryHolder().tags().map(TagKey::location);
        if (!(item instanceof BlockItem blockItem)) {
            return itemTags;
        }
        Block block = blockItem.getBlock();
        return Stream.concat(itemTags, block.builtInRegistryHolder().tags().map(TagKey::location)).distinct();
    }

    private static boolean hasAnyTagToken(List<String> values, String... tokens) {
        for (String value : values) {
            if (hasAnyPathToken(value, tokens)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAnyPathToken(String value, String... tokens) {
        for (String token : tokens) {
            if (value.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static String label(ResourceLocation id) {
        Item item = BuiltInRegistries.ITEM.get(id);
        return new ItemStack(item).getHoverName().getString();
    }

    private static String lower(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}
