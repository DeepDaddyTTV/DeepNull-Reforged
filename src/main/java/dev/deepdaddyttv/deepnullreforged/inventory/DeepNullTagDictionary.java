package dev.deepdaddyttv.deepnullreforged.inventory;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import net.minecraft.resources.Identifier;
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

        Set<Identifier> storedTags = getDictionaryTags(storedStack);
        if (storedTags.isEmpty()) {
            return false;
        }

        Set<Identifier> incomingTags = getDictionaryTags(incomingStack);
        if (incomingTags.isEmpty()) {
            return false;
        }

        for (Identifier storedTag : storedTags) {
            if (incomingTags.contains(storedTag)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSupported(ItemStack stack) {
        return !getDictionaryTags(stack).isEmpty();
    }

    public static Set<Identifier> getDictionaryTags(ItemStack stack) {
        if (!DeepNullConfig.isTagMatchingEnabled()) {
            return Set.of();
        }
        if (stack.isEmpty()) {
            return Set.of();
        }
        return stack.typeHolder().tags()
                .map(TagKey::location)
                .filter(DeepNullTagDictionary::isDictionaryTag)
                .filter(DeepNullConfig::isDictionaryTagAllowed)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean isDictionaryTag(Identifier tagId) {
        return DICTIONARY_NAMESPACES.contains(tagId.getNamespace()) && tagId.getPath().contains("/");
    }
}
