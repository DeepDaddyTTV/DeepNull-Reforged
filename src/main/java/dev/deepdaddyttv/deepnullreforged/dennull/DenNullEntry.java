package dev.deepdaddyttv.deepnullreforged.dennull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record DenNullEntry(
        ResourceLocation entityType,
        int count,
        String identityKey,
        CompoundTag entityTag,
        String displayName,
        String summary
) {
    private static final String ENTITY_TYPE_TAG = "EntityType";
    private static final String COUNT_TAG = "Count";
    private static final String IDENTITY_TAG = "Identity";
    private static final String ENTITY_TAG = "Entity";
    private static final String DISPLAY_TAG = "Display";
    private static final String SUMMARY_TAG = "Summary";

    public DenNullEntry {
        count = Math.max(0, count);
        identityKey = identityKey == null ? "" : identityKey;
        entityTag = entityTag == null ? new CompoundTag() : entityTag.copy();
        displayName = displayName == null || displayName.isBlank() ? entityType.toString() : displayName;
        summary = summary == null ? "" : summary;
    }

    public DenNullEntry withCount(int count) {
        return new DenNullEntry(entityType, count, identityKey, entityTag, displayName, summary);
    }

    public DenNullEntry withAdditionalCount(int amount) {
        return withCount(count + amount);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ENTITY_TYPE_TAG, entityType.toString());
        tag.putInt(COUNT_TAG, count);
        tag.putString(IDENTITY_TAG, identityKey);
        tag.put(ENTITY_TAG, entityTag.copy());
        tag.putString(DISPLAY_TAG, displayName);
        tag.putString(SUMMARY_TAG, summary);
        return tag;
    }

    public static DenNullEntry load(CompoundTag tag) {
        ResourceLocation entityType = ResourceLocation.tryParse(tag.getString(ENTITY_TYPE_TAG));
        if (entityType == null) {
            entityType = ResourceLocation.withDefaultNamespace("pig");
        }
        CompoundTag entityTag = tag.contains(ENTITY_TAG, Tag.TAG_COMPOUND) ? tag.getCompound(ENTITY_TAG).copy() : new CompoundTag();
        return new DenNullEntry(
                entityType,
                tag.getInt(COUNT_TAG),
                tag.getString(IDENTITY_TAG),
                entityTag,
                tag.getString(DISPLAY_TAG),
                tag.getString(SUMMARY_TAG)
        );
    }

    public static void write(RegistryFriendlyByteBuf buffer, DenNullEntry entry) {
        buffer.writeResourceLocation(entry.entityType);
        buffer.writeVarInt(entry.count);
        buffer.writeUtf(entry.identityKey);
        buffer.writeNbt(entry.entityTag);
        buffer.writeUtf(entry.displayName);
        buffer.writeUtf(entry.summary);
    }

    public static DenNullEntry read(RegistryFriendlyByteBuf buffer) {
        ResourceLocation entityType = buffer.readResourceLocation();
        int count = buffer.readVarInt();
        String identityKey = buffer.readUtf();
        CompoundTag entityTag = buffer.readNbt();
        String displayName = buffer.readUtf();
        String summary = buffer.readUtf();
        return new DenNullEntry(entityType, count, identityKey, entityTag, displayName, summary);
    }
}
