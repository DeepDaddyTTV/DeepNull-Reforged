package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record DumpNullRule(
        DumpNullRuleAction action,
        ResourceLocation itemId,
        int minDurabilityPercent,
        List<ResourceLocation> requiredEnchantments,
        List<ResourceLocation> forbiddenEnchantments,
        List<CompoundTag> customConditions
) {
    private static final String ACTION_TAG = "Action";
    private static final String ITEM_TAG = "Item";
    private static final String MIN_DURABILITY_TAG = "MinDurabilityPercent";
    private static final String REQUIRED_ENCHANTMENTS_TAG = "RequiredEnchantments";
    private static final String FORBIDDEN_ENCHANTMENTS_TAG = "ForbiddenEnchantments";
    private static final String CUSTOM_CONDITIONS_TAG = "CustomConditions";
    private static final String PRESET_GENERATED_TAG = "PresetGenerated";
    private static final String PRESET_ID_TAG = "PresetId";

    public DumpNullRule {
        action = action == null ? DumpNullRuleAction.PASS : action;
        minDurabilityPercent = Math.max(0, Math.min(100, minDurabilityPercent));
        requiredEnchantments = List.copyOf(requiredEnchantments == null ? List.of() : requiredEnchantments);
        forbiddenEnchantments = List.copyOf(forbiddenEnchantments == null ? List.of() : forbiddenEnchantments);
        customConditions = copyConditions(customConditions);
    }

    public static DumpNullRule basic(DumpNullRuleAction action, ResourceLocation itemId, int minDurabilityPercent,
                                     List<ResourceLocation> requiredEnchantments, List<ResourceLocation> forbiddenEnchantments) {
        return new DumpNullRule(action, itemId, minDurabilityPercent, requiredEnchantments, forbiddenEnchantments, List.of());
    }

    public static DumpNullRule presetGenerated(DumpNullRuleAction action, ResourceLocation itemId, String presetId) {
        CompoundTag condition = new CompoundTag();
        condition.putBoolean(PRESET_GENERATED_TAG, true);
        condition.putString(PRESET_ID_TAG, presetId == null ? "" : presetId);
        return new DumpNullRule(action, itemId, 0, List.of(), List.of(), List.of(condition));
    }

    public boolean isPresetGenerated() {
        return customConditions.stream().anyMatch(condition -> condition.getBoolean(PRESET_GENERATED_TAG));
    }

    public boolean isAdvanced() {
        return minDurabilityPercent > 0 || !requiredEnchantments.isEmpty() || !forbiddenEnchantments.isEmpty();
    }

    public boolean isSimpleStatusRuleFor(ResourceLocation itemId) {
        return this.itemId.equals(itemId) && !isAdvanced();
    }

    public static DumpNullRule load(CompoundTag tag) {
        ResourceLocation itemId = ResourceLocation.tryParse(tag.getString(ITEM_TAG));
        if (itemId == null) {
            itemId = ResourceLocation.withDefaultNamespace("air");
        }
        return new DumpNullRule(
                DumpNullRuleAction.byId(tag.getInt(ACTION_TAG)),
                itemId,
                tag.getInt(MIN_DURABILITY_TAG),
                readLocationList(tag, REQUIRED_ENCHANTMENTS_TAG),
                readLocationList(tag, FORBIDDEN_ENCHANTMENTS_TAG),
                readCompoundList(tag, CUSTOM_CONDITIONS_TAG)
        );
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(ACTION_TAG, action.ordinal());
        tag.putString(ITEM_TAG, itemId.toString());
        tag.putInt(MIN_DURABILITY_TAG, minDurabilityPercent);
        tag.put(REQUIRED_ENCHANTMENTS_TAG, writeLocationList(requiredEnchantments));
        tag.put(FORBIDDEN_ENCHANTMENTS_TAG, writeLocationList(forbiddenEnchantments));
        if (!customConditions.isEmpty()) {
            ListTag customList = new ListTag();
            for (CompoundTag customCondition : customConditions) {
                customList.add(customCondition.copy());
            }
            tag.put(CUSTOM_CONDITIONS_TAG, customList);
        }
        return tag;
    }

    public String summary() {
        StringBuilder builder = new StringBuilder(action.name()).append(' ').append(itemId);
        if (minDurabilityPercent > 0) {
            builder.append(" durability>=").append(minDurabilityPercent).append('%');
        }
        if (!requiredEnchantments.isEmpty()) {
            builder.append(" required=").append(requiredEnchantments);
        }
        if (!forbiddenEnchantments.isEmpty()) {
            builder.append(" forbidden=").append(forbiddenEnchantments);
        }
        if (!customConditions.isEmpty()) {
            builder.append(" custom=").append(customConditions.size());
        }
        return builder.toString();
    }

    private static List<ResourceLocation> readLocationList(CompoundTag tag, String key) {
        List<ResourceLocation> result = new ArrayList<>();
        if (!tag.contains(key, Tag.TAG_LIST)) {
            return result;
        }
        ListTag list = tag.getList(key, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

    private static ListTag writeLocationList(List<ResourceLocation> values) {
        ListTag list = new ListTag();
        for (ResourceLocation value : values) {
            list.add(StringTag.valueOf(value.toString()));
        }
        return list;
    }

    private static List<CompoundTag> readCompoundList(CompoundTag tag, String key) {
        List<CompoundTag> result = new ArrayList<>();
        if (!tag.contains(key, Tag.TAG_LIST)) {
            return result;
        }
        ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            result.add(list.getCompound(i).copy());
        }
        return result;
    }

    private static List<CompoundTag> copyConditions(List<CompoundTag> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return List.of();
        }
        List<CompoundTag> result = new ArrayList<>();
        for (CompoundTag condition : conditions) {
            result.add(condition.copy());
        }
        return List.copyOf(result);
    }
}
