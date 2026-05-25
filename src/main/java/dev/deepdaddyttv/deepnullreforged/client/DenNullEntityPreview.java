package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.dennull.DenNullEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public final class DenNullEntityPreview {
    private DenNullEntityPreview() {
    }

    public static Entity get(Minecraft minecraft, DenNullEntry entry, Map<String, Entity> cache) {
        if (minecraft == null || minecraft.level == null || entry == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(entry.entityType())) {
            return null;
        }
        return cache.computeIfAbsent(previewKey(entry), ignored -> {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entry.entityType());
            Entity entity = type.create(minecraft.level);
            if (entity == null) {
                return null;
            }
            CompoundTag tag = entry.entityTag().copy();
            if (!tag.isEmpty()) {
                try {
                    entity.load(tag);
                } catch (RuntimeException ignoredException) {
                    return entity;
                }
            }
            return entity;
        });
    }

    public static String previewKey(DenNullEntry entry) {
        return entry.entityType() + "|" + entry.identityKey();
    }
}
