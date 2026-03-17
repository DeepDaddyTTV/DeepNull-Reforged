package dev.deepdaddyttv.deepnullreforged.inventory;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class DeepNullTagDictionary {
    private static final Set<String> DICTIONARY_NAMESPACES = Set.of("c", "forge", "neoforge");

    private DeepNullTagDictionary() {
    }

    public static boolean canMatch(ItemStack storedStack, ItemStack incomingStack) {
        if (storedStack.isEmpty() || incomingStack.isEmpty()) {
            return false;
        }
        if (ItemStack.isSameItemSameComponents(storedStack, incomingStack)) {
            return true;
        }
        if (ItemStack.isSameItem(storedStack, incomingStack)) {
            return false;
        }

        Set<ResourceLocation> storedTags = getDictionaryTags(storedStack);
        if (storedTags.isEmpty()) {
            return false;
        }

        Set<ResourceLocation> incomingTags = getDictionaryTags(incomingStack);
        if (incomingTags.isEmpty()) {
            return false;
        }

        for (ResourceLocation storedTag : storedTags) {
            if (incomingTags.contains(storedTag)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSupported(ItemStack stack) {
        return !getDictionaryTags(stack).isEmpty();
    }

    public static Set<ResourceLocation> getDictionaryTags(ItemStack stack) {
        if (!DeepNullConfig.isTagMatchingEnabled()) {
            return Set.of();
        }
        if (stack.isEmpty()) {
            return Set.of();
        }
        return stack.getTags()
                .map(TagKey::location)
                .filter(DeepNullTagDictionary::isDictionaryTag)
                .filter(DeepNullConfig::isDictionaryTagAllowed)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean isDictionaryTag(ResourceLocation tagId) {
        return DICTIONARY_NAMESPACES.contains(tagId.getNamespace()) && tagId.getPath().contains("/");
    }
}
