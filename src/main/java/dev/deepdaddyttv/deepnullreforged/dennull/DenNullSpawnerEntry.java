package dev.deepdaddyttv.deepnullreforged.dennull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public record DenNullSpawnerEntry(
        ResourceLocation entityType,
        int count,
        String identityKey,
        CompoundTag spawnerTag,
        String displayName,
        String summary
) {
    private static final String ENTITY_TYPE_TAG = "EntityType";
    private static final String COUNT_TAG = "Count";
    private static final String IDENTITY_TAG = "Identity";
    private static final String SPAWNER_TAG = "Spawner";
    private static final String DISPLAY_TAG = "Display";
    private static final String SUMMARY_TAG = "Summary";

    public DenNullSpawnerEntry {
        entityType = entityType == null ? ResourceLocation.withDefaultNamespace("pig") : entityType;
        count = Math.max(0, count);
        identityKey = identityKey == null ? "" : identityKey;
        spawnerTag = spawnerTag == null ? new CompoundTag() : sanitizeSpawnerTag(spawnerTag);
        displayName = displayName == null || displayName.isBlank() ? defaultDisplayName(entityType) : displayName;
        summary = summary == null || summary.isBlank() ? "spawner data" : summary;
    }

    public static DenNullSpawnerEntry fromBlockEntity(SpawnerBlockEntity blockEntity) {
        CompoundTag tag = blockEntity.saveWithoutMetadata(blockEntity.getLevel().registryAccess());
        ResourceLocation entityType = entityType(tag);
        String key = entityType + "|" + DenNullCaptureNormalizer.stableTagString(sanitizeSpawnerTag(tag));
        return new DenNullSpawnerEntry(entityType, 1, key, tag, defaultDisplayName(entityType), summary(tag));
    }

    public DenNullSpawnerEntry withCount(int count) {
        return new DenNullSpawnerEntry(entityType, count, identityKey, spawnerTag, displayName, summary);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ENTITY_TYPE_TAG, entityType.toString());
        tag.putInt(COUNT_TAG, count);
        tag.putString(IDENTITY_TAG, identityKey);
        tag.put(SPAWNER_TAG, spawnerTag.copy());
        tag.putString(DISPLAY_TAG, displayName);
        tag.putString(SUMMARY_TAG, summary);
        return tag;
    }

    public static DenNullSpawnerEntry load(CompoundTag tag) {
        CompoundTag spawnerTag = tag.contains(SPAWNER_TAG, Tag.TAG_COMPOUND) ? tag.getCompound(SPAWNER_TAG).copy() : new CompoundTag();
        ResourceLocation entityType = ResourceLocation.tryParse(tag.getString(ENTITY_TYPE_TAG));
        if (entityType == null) {
            entityType = entityType(spawnerTag);
        }
        return new DenNullSpawnerEntry(
                entityType,
                tag.getInt(COUNT_TAG),
                tag.getString(IDENTITY_TAG),
                spawnerTag,
                tag.getString(DISPLAY_TAG),
                tag.getString(SUMMARY_TAG)
        );
    }

    public static void write(RegistryFriendlyByteBuf buffer, DenNullSpawnerEntry entry) {
        buffer.writeResourceLocation(entry.entityType);
        buffer.writeVarInt(entry.count);
        buffer.writeUtf(entry.identityKey);
        buffer.writeNbt(entry.spawnerTag);
        buffer.writeUtf(entry.displayName);
        buffer.writeUtf(entry.summary);
    }

    public static DenNullSpawnerEntry read(RegistryFriendlyByteBuf buffer) {
        return new DenNullSpawnerEntry(
                buffer.readResourceLocation(),
                buffer.readVarInt(),
                buffer.readUtf(),
                buffer.readNbt(),
                buffer.readUtf(),
                buffer.readUtf()
        );
    }

    public static ResourceLocation entityType(CompoundTag spawnerTag) {
        CompoundTag entity = entityToSpawn(spawnerTag);
        ResourceLocation parsed = ResourceLocation.tryParse(entity.getString("id"));
        return parsed == null ? ResourceLocation.withDefaultNamespace("pig") : parsed;
    }

    public static CompoundTag entityToSpawn(CompoundTag spawnerTag) {
        if (spawnerTag.contains("SpawnData", Tag.TAG_COMPOUND)) {
            CompoundTag spawnData = spawnerTag.getCompound("SpawnData");
            if (spawnData.contains("entity", Tag.TAG_COMPOUND)) {
                return spawnData.getCompound("entity").copy();
            }
            if (spawnData.contains("id", Tag.TAG_STRING)) {
                return spawnData.copy();
            }
        }
        if (spawnerTag.contains("SpawnPotentials", Tag.TAG_LIST)) {
            ListTag potentials = spawnerTag.getList("SpawnPotentials", Tag.TAG_COMPOUND);
            for (int i = 0; i < potentials.size(); i++) {
                CompoundTag potential = potentials.getCompound(i);
                if (potential.contains("data", Tag.TAG_COMPOUND)) {
                    CompoundTag data = potential.getCompound("data");
                    if (data.contains("entity", Tag.TAG_COMPOUND)) {
                        return data.getCompound("entity").copy();
                    }
                }
            }
        }
        CompoundTag fallback = new CompoundTag();
        fallback.putString("id", "minecraft:pig");
        return fallback;
    }

    private static CompoundTag sanitizeSpawnerTag(CompoundTag source) {
        CompoundTag tag = source.copy();
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
        tag.remove("id");
        return tag;
    }

    private static String defaultDisplayName(ResourceLocation entityType) {
        String path = entityType == null ? "mob" : entityType.getPath().replace('_', ' ');
        return Character.toUpperCase(path.charAt(0)) + path.substring(1) + " Spawner";
    }

    private static String summary(CompoundTag spawnerTag) {
        int spawnCount = spawnerTag.contains("SpawnCount", Tag.TAG_SHORT) ? spawnerTag.getShort("SpawnCount") : 4;
        int spawnRange = spawnerTag.contains("SpawnRange", Tag.TAG_SHORT) ? spawnerTag.getShort("SpawnRange") : 4;
        return "spawns " + entityType(spawnerTag) + ", count " + spawnCount + ", range " + spawnRange;
    }
}
