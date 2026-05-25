package dev.deepdaddyttv.deepnullreforged.dumpnull;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.HashSet;
import java.util.Set;

public final class DumpNullRuleMatcher {
    private DumpNullRuleMatcher() {
    }

    public static DumpNullRuleAction actionFor(DumpNullData data, ItemStack stack) {
        if (stack.isEmpty()) {
            return DumpNullRuleAction.PASS;
        }
        for (DumpNullRule rule : data.rules()) {
            if (matches(rule, stack)) {
                return rule.action();
            }
        }
        return DumpNullRuleAction.PASS;
    }

    public static boolean matches(DumpNullRule rule, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation stackItemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!rule.itemId().equals(stackItemId)) {
            return false;
        }
        if (rule.minDurabilityPercent() > 0 && durabilityRemainingPercent(stack) < rule.minDurabilityPercent()) {
            return false;
        }

        Set<ResourceLocation> stackEnchantments = enchantmentIds(stack);
        if (!stackEnchantments.containsAll(rule.requiredEnchantments())) {
            return false;
        }
        for (ResourceLocation forbidden : rule.forbiddenEnchantments()) {
            if (stackEnchantments.contains(forbidden)) {
                return false;
            }
        }
        return true;
    }

    public static int durabilityRemainingPercent(ItemStack stack) {
        if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
            return 100;
        }
        int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
        return Math.round(remaining * 100.0F / stack.getMaxDamage());
    }

    private static Set<ResourceLocation> enchantmentIds(ItemStack stack) {
        Set<ResourceLocation> result = new HashSet<>();
        ItemEnchantments enchantments = stack.getEnchantments();
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            if (entry.getIntValue() <= 0) {
                continue;
            }
            entry.getKey().unwrapKey().ifPresent(key -> result.add(key.location()));
        }
        return result;
    }
}
