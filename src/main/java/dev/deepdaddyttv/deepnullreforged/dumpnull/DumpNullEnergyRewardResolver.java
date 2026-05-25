package dev.deepdaddyttv.deepnullreforged.dumpnull;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class DumpNullEnergyRewardResolver {
    private DumpNullEnergyRewardResolver() {
    }

    public static int rewardFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        int perItem = rewardPerItem(stack);
        if (perItem <= 0) {
            return 0;
        }
        long total = (long) perItem * stack.getCount();
        return total > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;
    }

    public static int rewardPerItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        int itemReward = DeepNullConfig.getDumpNullItemFeReward(itemId);
        if (itemReward >= 0) {
            return itemReward;
        }
        int tagReward = stack.getTags()
                .map(TagKey<Item>::location)
                .mapToInt(DeepNullConfig::getDumpNullTagFeReward)
                .filter(value -> value >= 0)
                .findFirst()
                .orElse(-1);
        if (tagReward >= 0) {
            return tagReward;
        }
        return DeepNullConfig.getDumpNullRarityFeReward(stack.getRarity().name());
    }
}
