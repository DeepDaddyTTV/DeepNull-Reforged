package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DumpNullCatalog {
    private DumpNullCatalog() {
    }

    public static List<DumpNullMobOption> mobOptions(DumpNullData data) {
        Set<ResourceLocation> selected = new HashSet<>(data.selectedMobs());
        return BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(entityType -> entityType.getCategory() != MobCategory.MISC)
                .map(entityType -> optionFor(entityType, selected))
                .filter(option -> option.entityId() != null)
                .sorted(mobOptionComparator())
                .toList();
    }

    public static List<ResourceLocation> validSelectedMobs(DumpNullData data) {
        return data.selectedMobs().stream()
                .filter(DumpNullCatalog::isRegisteredEntity)
                .toList();
    }

    public static boolean isRegisteredEntity(ResourceLocation entityId) {
        return BuiltInRegistries.ENTITY_TYPE.containsKey(entityId);
    }

    public static boolean isVisibleMobEntity(ResourceLocation entityId) {
        if (!isRegisteredEntity(entityId)) {
            return false;
        }
        return BuiltInRegistries.ENTITY_TYPE.get(entityId).getCategory() != MobCategory.MISC;
    }

    public static boolean isVanillaBossLike(ResourceLocation entityId) {
        return ResourceLocation.withDefaultNamespace("ender_dragon").equals(entityId)
                || ResourceLocation.withDefaultNamespace("wither").equals(entityId)
                || ResourceLocation.withDefaultNamespace("warden").equals(entityId)
                || ResourceLocation.withDefaultNamespace("elder_guardian").equals(entityId);
    }

    public static Comparator<DumpNullMobOption> mobOptionComparator() {
        return Comparator
                .comparingInt((DumpNullMobOption option) -> namespaceRank(option.entityId()))
                .thenComparing(option -> option.entityId().getNamespace())
                .thenComparingInt(option -> vanillaBossRank(option.entityId()))
                .thenComparing(option -> option.entityId().getPath());
    }

    private static int namespaceRank(ResourceLocation entityId) {
        return "minecraft".equals(entityId.getNamespace()) ? 0 : 1;
    }

    private static int vanillaBossRank(ResourceLocation entityId) {
        return "minecraft".equals(entityId.getNamespace()) && isVanillaBossLike(entityId) ? 1 : 0;
    }

    private static DumpNullMobOption optionFor(EntityType<?> entityType, Set<ResourceLocation> selected) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return new DumpNullMobOption(
                entityId,
                entityType.getDescriptionId(),
                entityType.getCategory().name(),
                selected.contains(entityId)
        );
    }
}
