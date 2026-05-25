package dev.deepdaddyttv.deepnullreforged.dumpnull;

import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DockableNullItem;
import dev.deepdaddyttv.deepnullreforged.nullseed.ItemSetPresetCatalog;
import dev.deepdaddyttv.deepnullreforged.nullseed.ItemSetPresetEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DumpNullItemCatalog {
    private static final Map<DumpNullItemCatalogView, List<String>> VANILLA_PRIORITY = Map.of(
            DumpNullItemCatalogView.ORES, List.of(
                    "coal", "charcoal", "raw_iron", "raw_gold", "raw_copper", "iron_ingot", "gold_ingot", "copper_ingot",
                    "iron_nugget", "gold_nugget", "diamond", "emerald", "lapis_lazuli", "redstone", "quartz", "amethyst_shard",
                    "coal_ore", "deepslate_coal_ore", "iron_ore", "deepslate_iron_ore", "gold_ore", "deepslate_gold_ore",
                    "copper_ore", "deepslate_copper_ore", "diamond_ore", "deepslate_diamond_ore", "emerald_ore", "deepslate_emerald_ore",
                    "lapis_ore", "deepslate_lapis_ore", "redstone_ore", "deepslate_redstone_ore", "nether_quartz_ore", "nether_gold_ore"
            ),
            DumpNullItemCatalogView.FARMING, List.of(
                    "wheat_seeds", "pumpkin_seeds", "melon_seeds", "beetroot_seeds", "torchflower_seeds", "pitcher_pod",
                    "wheat", "carrot", "potato", "beetroot", "pumpkin", "melon_slice", "sugar_cane", "cactus", "bamboo",
                    "kelp", "oak_sapling", "spruce_sapling", "birch_sapling", "jungle_sapling", "acacia_sapling", "dark_oak_sapling",
                    "mangrove_propagule", "cherry_sapling", "sweet_berries", "glow_berries", "nether_wart", "apple", "bread"
            ),
            DumpNullItemCatalogView.BUILDING, List.of(
                    "oak_log", "spruce_log", "birch_log", "jungle_log", "acacia_log", "dark_oak_log", "mangrove_log", "cherry_log",
                    "oak_planks", "spruce_planks", "birch_planks", "jungle_planks", "acacia_planks", "dark_oak_planks", "mangrove_planks", "cherry_planks",
                    "stone", "cobblestone", "smooth_stone", "stone_bricks", "bricks", "glass", "sand", "red_sand", "gravel",
                    "dirt", "coarse_dirt", "mud", "clay", "terracotta", "white_concrete", "gray_concrete", "black_concrete"
            ),
            DumpNullItemCatalogView.QUARRY, List.of(
                    "cobblestone", "stone", "deepslate", "cobbled_deepslate", "granite", "diorite", "andesite", "tuff",
                    "calcite", "basalt", "blackstone", "netherrack", "end_stone", "gravel", "sand", "red_sand", "clay",
                    "flint", "dripstone_block", "pointed_dripstone"
            ),
            DumpNullItemCatalogView.MOB_DROPS, List.of(
                    "rotten_flesh", "bone", "arrow", "string", "spider_eye", "gunpowder", "ender_pearl", "slime_ball",
                    "magma_cream", "blaze_rod", "blaze_powder", "ghast_tear", "leather", "feather", "rabbit_hide",
                    "rabbit_foot", "phantom_membrane", "prismarine_shard", "prismarine_crystals", "ink_sac", "glow_ink_sac",
                    "shulker_shell", "nautilus_shell", "trident"
            )
    );

    private DumpNullItemCatalog() {
    }

    public static List<DumpNullItemCatalogEntry> build(DumpNullItemCatalogView view, Inventory inventory) {
        DumpNullItemCatalogView normalizedView = view == null ? DumpNullItemCatalogView.ORES : view;
        return normalizedView.inventoryBacked() ? inventoryEntries(inventory) : presetEntries(normalizedView);
    }

    public static int priorityIndex(DumpNullItemCatalogView view, ResourceLocation itemId) {
        if (itemId == null || !"minecraft".equals(itemId.getNamespace())) {
            return Integer.MAX_VALUE;
        }
        List<String> priority = VANILLA_PRIORITY.getOrDefault(view, List.of());
        int index = priority.indexOf(itemId.getPath());
        return index < 0 ? Integer.MAX_VALUE : index;
    }

    private static List<DumpNullItemCatalogEntry> presetEntries(DumpNullItemCatalogView view) {
        Map<ResourceLocation, DumpNullItemCatalogEntry> entries = new LinkedHashMap<>();
        for (ItemSetPresetEntry entry : ItemSetPresetCatalog.build(view.itemSetPresetId())) {
            if (isSelectable(entry.itemId())) {
                entries.put(entry.itemId(), new DumpNullItemCatalogEntry(entry.itemId(), entry.label(), entry.source()));
            }
        }
        return sorted(entries.values().stream().toList(), view);
    }

    private static List<DumpNullItemCatalogEntry> inventoryEntries(Inventory inventory) {
        if (inventory == null) {
            return List.of();
        }
        Map<ResourceLocation, DumpNullItemCatalogEntry> entries = new LinkedHashMap<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (isSelectable(itemId)) {
                entries.putIfAbsent(itemId, new DumpNullItemCatalogEntry(itemId, stack.getHoverName().getString(), "inventory"));
            }
        }
        return entries.values().stream()
                .sorted(Comparator.comparing(DumpNullItemCatalogEntry::label).thenComparing(entry -> entry.itemId().toString()))
                .toList();
    }

    private static List<DumpNullItemCatalogEntry> sorted(List<DumpNullItemCatalogEntry> entries, DumpNullItemCatalogView view) {
        return entries.stream()
                .sorted(Comparator
                        .comparingInt((DumpNullItemCatalogEntry entry) -> priorityIndex(view, entry.itemId()))
                        .thenComparing(entry -> entry.itemId().getNamespace().equals("minecraft") ? 0 : 1)
                        .thenComparing(DumpNullItemCatalogEntry::label)
                        .thenComparing(entry -> entry.itemId().toString()))
                .toList();
    }

    private static boolean isSelectable(ResourceLocation itemId) {
        if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
            return false;
        }
        Item item = BuiltInRegistries.ITEM.get(itemId);
        return item != Items.AIR && !(item instanceof DeepNullItem) && !(item instanceof DockableNullItem);
    }
}
