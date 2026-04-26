package dev.deepdaddyttv.deepnullreforged.inventory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Locale;
import java.util.stream.Stream;

public enum DeepNullFilterMode {
    WHITELIST("whitelist"),
    BLACKLIST("blacklist"),
    MINER("miner"),
    HERBALIST("herbalist"),
    HUNTER("hunter"),
    NETWORK_ENGINEER("network_engineer"),
    CONNECTIONS("connections"),
    BUILDER("builder");

    private final String id;

    DeepNullFilterMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return Component.translatable("filter.mode." + id);
    }

    public boolean usesGhostSlots() {
        return this == WHITELIST || this == BLACKLIST;
    }

    public boolean matchesPreset(ItemStack stack) {
        if (stack.isEmpty() || usesGhostSlots()) {
            return false;
        }

        return switch (this) {
            case MINER -> isMinerItem(stack);
            case HERBALIST -> isHerbalistItem(stack);
            case HUNTER -> isHunterItem(stack);
            case NETWORK_ENGINEER -> isNetworkEngineerItem(stack);
            case CONNECTIONS -> isConnectionsItem(stack);
            case BUILDER -> stack.getItem() instanceof BlockItem;
            default -> false;
        };
    }

    public DeepNullFilterMode cycle(boolean forward) {
        DeepNullFilterMode[] values = values();
        int next = forward ? ordinal() + 1 : ordinal() - 1;
        if (next < 0) {
            next = values.length - 1;
        } else if (next >= values.length) {
            next = 0;
        }
        return values[next];
    }

    public static DeepNullFilterMode byId(int id) {
        DeepNullFilterMode[] values = values();
        if (id < 0 || id >= values.length) {
            return WHITELIST;
        }
        return values[id];
    }

    private static boolean isMinerItem(ItemStack stack) {
        return hasAnyTagToken(stack, "ore", "ores", "stone", "sand", "gravel", "dirt", "clay", "deepslate", "cobblestone")
                || pathContains(stack, "ore", "stone", "sand", "gravel", "dirt", "clay");
    }

    private static boolean isHerbalistItem(ItemStack stack) {
        return hasAnyTagToken(stack, "sapling", "saplings", "flower", "flowers", "crop", "crops", "seed", "seeds", "mushroom", "mushrooms", "leaf", "leaves", "plant", "plants")
                || pathContains(stack, "sapling", "flower", "crop", "seed", "mushroom", "leaf", "plant");
    }

    private static boolean isHunterItem(ItemStack stack) {
        return hasAnyTagToken(stack, "mob", "drop", "drops", "bone", "bones", "leather", "feather", "string", "gunpowder", "rotten_flesh", "ender_pearl", "slimeball", "blaze", "ghast", "membrane", "shulker", "spider_eye", "ink_sac", "prismarine")
                || pathContains(stack, "bone", "leather", "feather", "string", "gunpowder", "rotten_flesh", "ender_pearl", "slime_ball", "blaze", "ghast", "membrane", "shulker_shell", "spider_eye", "ink_sac", "prismarine");
    }

    private static boolean isNetworkEngineerItem(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String namespace = key.getNamespace();
        return "ae2".equals(namespace) || "refinedstorage".equals(namespace);
    }

    private static boolean isConnectionsItem(ItemStack stack) {
        return matchesConnectionsIdentifier(BuiltInRegistries.ITEM.getKey(stack.getItem()))
                || hasAnyTagWord(stack, "pipe", "connector", "wire", "cable", "conduit", "duct", "tube", "transporter", "conductor", "plug", "point", "tunnel", "bus");
    }

    static boolean matchesConnectionsIdentifier(ResourceLocation key) {
        String namespace = key.getNamespace();
        String path = key.getPath().toLowerCase(Locale.ROOT);

        if (hasAnyPathWord(path, "pipe", "connector", "wire", "cable", "conduit", "duct", "tube", "transporter", "conductor", "plug", "point")) {
            return true;
        }

        return switch (namespace) {
            case "ae2" -> hasAnyPathWord(path, "bus", "tunnel", "p2p", "anchor");
            case "refinedstorage", "refinedstorageaddons" -> hasAnyPathWord(path, "external", "importer", "exporter", "constructor", "destructor", "detector", "interface");
            case "mekanism" -> hasAnyPathWord(path, "transmitter");
            case "xnet" -> hasAnyPathWord(path, "router", "channel");
            case "laserio" -> hasAnyPathWord(path, "node");
            case "fluxnetworks" -> hasAnyPathWord(path, "controller");
            default -> false;
        };
    }

    private static boolean hasAnyTagToken(ItemStack stack, String... tokens) {
        return tagLocations(stack)
                .map(ResourceLocation::getPath)
                .map(path -> path.toLowerCase(Locale.ROOT))
                .anyMatch(path -> containsAny(path, tokens));
    }

    private static boolean hasAnyTagWord(ItemStack stack, String... words) {
        return tagLocations(stack)
                .map(ResourceLocation::getPath)
                .map(path -> path.toLowerCase(Locale.ROOT))
                .anyMatch(path -> hasAnyPathWord(path, words));
    }

    private static boolean pathContains(ItemStack stack, String... tokens) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return containsAny(key.getPath().toLowerCase(Locale.ROOT), tokens);
    }

    private static boolean containsAny(String value, String... tokens) {
        for (String token : tokens) {
            if (value.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAnyPathWord(String value, String... words) {
        String[] parts = value.toLowerCase(Locale.ROOT).split("[^a-z0-9]+");
        for (String part : parts) {
            for (String word : words) {
                if (part.equals(word)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Stream<ResourceLocation> tagLocations(ItemStack stack) {
        Stream<ResourceLocation> itemTags = stack.getTags().map(TagKey::location);
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return itemTags;
        }
        Block block = blockItem.getBlock();
        Stream<ResourceLocation> blockTags = block.builtInRegistryHolder().tags().map(TagKey::location);
        return Stream.concat(itemTags, blockTags).distinct();
    }
}
