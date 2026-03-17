package dev.deepdaddyttv.deepnullreforged;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class DeepNullConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DISABLE_TAG_MATCHING;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> TAG_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> TAG_WHITELIST;
    public static final ModConfigSpec SPEC;

    private static volatile boolean tagMatchingDisabled;
    private static volatile Set<ResourceLocation> tagBlacklist = Set.of();
    private static volatile Set<ResourceLocation> tagWhitelist = Set.of();

    static {
        BUILDER.push("tagMatching");

        DISABLE_TAG_MATCHING = BUILDER
                .comment("Disable DeepNull tag-matching mode entirely.")
                .define("disableTagMatching", false);

        TAG_BLACKLIST = BUILDER
                .comment("Dictionary-style item tags that will not be allowed for tag matching unless explicitly whitelisted. Example: c:storage_blocks/coal")
                .defineListAllowEmpty("tagBlacklist", List.of(), () -> "", DeepNullConfig::validateTagName);

        TAG_WHITELIST = BUILDER
                .comment("If non-empty, only these dictionary-style item tags will be allowed for tag matching. Example: c:ingots/copper")
                .defineListAllowEmpty("tagWhitelist", List.of(), () -> "", DeepNullConfig::validateTagName);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private DeepNullConfig() {
    }

    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            bake();
        }
    }

    public static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            bake();
        }
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

    private static void bake() {
        tagMatchingDisabled = DISABLE_TAG_MATCHING.getAsBoolean();
        tagBlacklist = normalizeTags(TAG_BLACKLIST.get());
        tagWhitelist = normalizeTags(TAG_WHITELIST.get());
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

    private static boolean validateTagName(Object value) {
        return value instanceof String tagName && ResourceLocation.tryParse(tagName) != null;
    }
}
