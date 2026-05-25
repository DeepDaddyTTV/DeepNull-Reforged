package dev.deepdaddyttv.deepnullreforged.dennull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record DenNullTemplate(int targetIndex, ResourceLocation entityType) {
    private static final String TARGET_TAG = "Target";
    private static final String ENTITY_TYPE_TAG = "EntityType";

    public DenNullTemplate {
        targetIndex = Math.max(0, targetIndex);
        entityType = entityType == null ? ResourceLocation.withDefaultNamespace("pig") : entityType;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TARGET_TAG, targetIndex);
        tag.putString(ENTITY_TYPE_TAG, entityType.toString());
        return tag;
    }

    public static DenNullTemplate load(CompoundTag tag) {
        ResourceLocation entityType = ResourceLocation.tryParse(tag.getString(ENTITY_TYPE_TAG));
        return new DenNullTemplate(tag.getInt(TARGET_TAG), entityType);
    }

    public static void write(RegistryFriendlyByteBuf buffer, DenNullTemplate template) {
        buffer.writeVarInt(template.targetIndex());
        buffer.writeResourceLocation(template.entityType());
    }

    public static DenNullTemplate read(RegistryFriendlyByteBuf buffer) {
        return new DenNullTemplate(buffer.readVarInt(), buffer.readResourceLocation());
    }
}
