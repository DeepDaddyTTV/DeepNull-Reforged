package dev.deepdaddyttv.deepnullreforged.dennull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class DenNullCaptureNormalizer {
    private static final String RANDOM_SPAWN_BONUS = "minecraft:random_spawn_bonus";
    private static final List<String> VOLATILE_KEYS = List.of(
            "UUID",
            "Pos",
            "Motion",
            "Rotation",
            "Health",
            "HurtTime",
            "DeathTime",
            "HurtByTimestamp",
            "FallDistance",
            "Fire",
            "Air",
            "OnGround",
            "PortalCooldown",
            "AbsorptionAmount",
            "TicksFrozen",
            "FallFlying",
            "LeftHanded"
    );

    private DenNullCaptureNormalizer() {
    }

    public static Optional<DenNullEntry> capture(Entity entity) {
        if (entity == null || entity instanceof Player || entity.isPassenger() || !entity.getPassengers().isEmpty()) {
            return Optional.empty();
        }

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (entityId == null) {
            return Optional.empty();
        }

        CompoundTag representative = entity.saveWithoutId(new CompoundTag());
        if (representative.isEmpty()) {
            return Optional.empty();
        }
        stripRuntimeState(representative);

        String key = identityKey(entityId, representative);
        String displayName = entity.getDisplayName().getString();
        String summary = summary(normalizedIdentity(representative));
        return Optional.of(new DenNullEntry(entityId, 1, key, representative, displayName, summary));
    }

    public static DenNullEntry normalizeEntry(DenNullEntry entry) {
        if (entry == null) {
            return null;
        }
        CompoundTag representative = entry.entityTag().copy();
        stripRuntimeState(representative);
        CompoundTag identity = normalizedIdentity(representative);
        return new DenNullEntry(
                entry.entityType(),
                entry.count(),
                identityKey(entry.entityType(), representative),
                representative,
                entry.displayName(),
                summary(identity)
        );
    }

    public static String identityKey(ResourceLocation entityId, CompoundTag representative) {
        CompoundTag identity = normalizedIdentity(representative);
        return entityId + "|" + stableTagString(identity);
    }

    public static CompoundTag normalizedIdentity(CompoundTag representative) {
        CompoundTag identity = representative == null ? new CompoundTag() : representative.copy();
        stripRuntimeState(identity);
        normalizeIdentity(identity);
        return identity;
    }

    public static void stripRuntimeState(CompoundTag tag) {
        for (String key : VOLATILE_KEYS) {
            tag.remove(key);
        }
        if (tag.contains("Brain", Tag.TAG_COMPOUND)) {
            CompoundTag brain = tag.getCompound("Brain");
            brain.remove("memories");
            if (brain.isEmpty()) {
                tag.remove("Brain");
            }
        }
    }

    private static void normalizeIdentity(CompoundTag tag) {
        if (tag.contains("Age", Tag.TAG_INT)) {
            int age = tag.getInt("Age");
            tag.putInt("Age", age < 0 ? -1 : 0);
        }
        if (tag.contains("LoveCause")) {
            tag.remove("LoveCause");
        }
        if (tag.contains("InLove")) {
            tag.remove("InLove");
        }
        normalizeAttributes(tag, "attributes");
        normalizeAttributes(tag, "Attributes");
    }

    private static void normalizeAttributes(CompoundTag tag, String attributesKey) {
        if (!tag.contains(attributesKey, Tag.TAG_LIST)) {
            return;
        }
        ListTag attributes = tag.getList(attributesKey, Tag.TAG_COMPOUND);
        ListTag normalizedAttributes = new ListTag();
        for (int i = 0; i < attributes.size(); i++) {
            CompoundTag attribute = attributes.getCompound(i).copy();
            normalizeModifiers(attribute, "modifiers");
            normalizeModifiers(attribute, "Modifiers");
            if (!attribute.isEmpty()) {
                normalizedAttributes.add(attribute);
            }
        }
        if (normalizedAttributes.isEmpty()) {
            tag.remove(attributesKey);
        } else {
            tag.put(attributesKey, normalizedAttributes);
        }
    }

    private static void normalizeModifiers(CompoundTag attribute, String modifiersKey) {
        if (!attribute.contains(modifiersKey, Tag.TAG_LIST)) {
            return;
        }
        ListTag modifiers = attribute.getList(modifiersKey, Tag.TAG_COMPOUND);
        ListTag normalizedModifiers = new ListTag();
        for (int i = 0; i < modifiers.size(); i++) {
            CompoundTag modifier = modifiers.getCompound(i);
            String id = modifier.getString("id");
            String name = modifier.getString("Name");
            if (RANDOM_SPAWN_BONUS.equals(id) || RANDOM_SPAWN_BONUS.equals(name)) {
                continue;
            }
            normalizedModifiers.add(modifier.copy());
        }
        if (normalizedModifiers.isEmpty()) {
            attribute.remove(modifiersKey);
        } else {
            attribute.put(modifiersKey, normalizedModifiers);
        }
    }

    public static String summary(CompoundTag tag) {
        List<String> parts = new ArrayList<>();
        if (tag.contains("CustomName")) {
            parts.add("custom name");
        }
        if (tag.contains("Variant") || tag.contains("variant") || tag.contains("Type")) {
            parts.add("variant/type");
        }
        if (tag.contains("VillagerData")) {
            parts.add("villager data");
        }
        if (tag.contains("Offers")) {
            parts.add("trades");
        }
        if (hasNonEmptyEquipment(tag)) {
            parts.add("equipment");
        }
        if (tag.contains("Owner") || tag.contains("OwnerUUID") || tag.contains("Trusted")) {
            parts.add("owner");
        }
        if (tag.contains("Color") || tag.contains("CollarColor")) {
            parts.add("color");
        }
        return parts.isEmpty() ? "standard data" : String.join(", ", parts);
    }

    public static List<String> detailLines(DenNullEntry entry) {
        if (entry == null) {
            return List.of();
        }
        CompoundTag identity = normalizedIdentity(entry.entityTag());
        List<String> lines = new ArrayList<>();
        addValue(lines, "Type", firstString(identity, "variant", "Variant", "Type"));
        if (identity.contains("Age", Tag.TAG_INT)) {
            lines.add(identity.getInt("Age") < 0 ? "Age: baby" : "Age: adult");
        }
        addValue(lines, "Name", identity.getString("CustomName"));
        addValue(lines, "Owner", firstString(identity, "Owner", "OwnerUUID"));
        if (identity.contains("Trusted", Tag.TAG_LIST)) {
            lines.add("Trusted players: " + identity.getList("Trusted", Tag.TAG_INT_ARRAY).size());
        }
        if (identity.contains("Tame")) {
            lines.add("Tame: " + identity.getBoolean("Tame"));
        }
        addValue(lines, "Color", firstPresentValue(identity, "Color", "CollarColor"));
        if (hasNonEmptyEquipment(identity)) {
            lines.add("Equipment: preserved");
        }
        if (identity.contains("VillagerData", Tag.TAG_COMPOUND)) {
            lines.add("Villager data: preserved");
        }
        if (identity.contains("Offers", Tag.TAG_COMPOUND)) {
            lines.add("Trades: preserved");
        }
        Set<String> preservedKeys = new LinkedHashSet<>(identity.getAllKeys());
        preservedKeys.removeAll(Set.of(
                "CustomName",
                "variant",
                "Variant",
                "Type",
                "Age",
                "Owner",
                "OwnerUUID",
                "Trusted",
                "Tame",
                "Color",
                "CollarColor",
                "ArmorItems",
                "HandItems",
                "VillagerData",
                "Offers"
        ));
        if (!preservedKeys.isEmpty()) {
            lines.add("Identity data: " + String.join(", ", preservedKeys.stream().limit(6).toList())
                    + (preservedKeys.size() > 6 ? ", ..." : ""));
        }
        return lines;
    }

    public static String stableTagString(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            List<String> keys = new ArrayList<>(compoundTag.getAllKeys());
            keys.sort(String::compareTo);
            StringBuilder builder = new StringBuilder("{");
            for (String key : keys) {
                builder.append(key).append(':').append(stableTagString(compoundTag.get(key))).append(';');
            }
            return builder.append('}').toString();
        }
        if (tag instanceof ListTag listTag) {
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < listTag.size(); i++) {
                builder.append(stableTagString(listTag.get(i))).append(';');
            }
            return builder.append(']').toString();
        }
        return tag == null ? "" : tag.getAsString();
    }

    private static boolean hasNonEmptyEquipment(CompoundTag tag) {
        return hasNonEmptyList(tag, "ArmorItems") || hasNonEmptyList(tag, "HandItems");
    }

    private static boolean hasNonEmptyList(CompoundTag tag, String key) {
        if (!tag.contains(key, Tag.TAG_LIST)) {
            return false;
        }
        ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            if (!list.getCompound(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static void addValue(List<String> lines, String label, String value) {
        if (value != null && !value.isBlank()) {
            lines.add(label + ": " + value);
        }
    }

    private static String firstString(CompoundTag tag, String... keys) {
        for (String key : keys) {
            if (tag.contains(key)) {
                String value = tag.getString(key);
                if (!value.isBlank()) {
                    return value;
                }
            }
        }
        return "";
    }

    private static String firstPresentValue(CompoundTag tag, String... keys) {
        for (String key : keys) {
            if (tag.contains(key)) {
                return tag.get(key).getAsString();
            }
        }
        return "";
    }
}
