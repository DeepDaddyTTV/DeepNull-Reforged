package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public record DumpNullDropCandidate(ResourceLocation entityId, ResourceLocation itemId, String source, int observed, int samples) {
    private static final int LOOT_SAMPLES_PER_MOB = 128;
    private static final int EQUIPMENT_SAMPLES_PER_MOB = 192;
    private static final int CUSTOM_DEATH_LOOT_SAMPLES_PER_MOB = 1;

    public static List<DumpNullDropCandidate> discover(ServerLevel level, Collection<ResourceLocation> entityIds) {
        Map<String, Accumulator> candidates = new LinkedHashMap<>();
        for (ResourceLocation entityId : entityIds) {
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
                continue;
            }
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
            sampleLoot(level, entityId, entityType, candidates);
            sampleCustomDeathLoot(level, entityId, entityType, candidates);
            sampleEquipment(level, entityId, entityType, candidates);
        }
        return candidates.values().stream()
                .map(Accumulator::candidate)
                .sorted((left, right) -> {
                    int entityCompare = left.entityId().toString().compareTo(right.entityId().toString());
                    if (entityCompare != 0) {
                        return entityCompare;
                    }
                    int sourceCompare = left.source().compareTo(right.source());
                    if (sourceCompare != 0) {
                        return sourceCompare;
                    }
                    return left.itemId().toString().compareTo(right.itemId().toString());
                })
                .toList();
    }

    private static void sampleLoot(ServerLevel level, ResourceLocation entityId, EntityType<?> entityType, Map<String, Accumulator> candidates) {
        Entity entity = entityType.create(level);
        if (entity == null) {
            return;
        }
        entity.moveTo(0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(entityType.getDefaultLootTable());
        sampleLootContext(level, entityId, entity, lootTable, null, "loot", candidates);
        sampleLootContext(level, entityId, entity, lootTable, FakePlayerFactory.getMinecraft(level), "player loot", candidates);
        entity.discard();
    }

    private static void sampleCustomDeathLoot(ServerLevel level, ResourceLocation entityId, EntityType<?> entityType, Map<String, Accumulator> candidates) {
        Entity entity = entityType.create(level);
        if (!(entity instanceof LivingEntity livingEntity)) {
            if (entity != null) {
                entity.discard();
            }
            return;
        }
        Method method = customDeathLootMethod(livingEntity.getClass());
        if (method == null || method.getDeclaringClass() == LivingEntity.class) {
            livingEntity.discard();
            return;
        }
        livingEntity.moveTo(0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
        List<ItemEntity> drops = new ArrayList<>();
        Collection<ItemEntity> previousDrops = livingEntity.captureDrops(drops);
        try {
            ServerPlayer player = FakePlayerFactory.getMinecraft(level);
            DamageSource damageSource = player == null ? level.damageSources().generic() : level.damageSources().playerAttack(player);
            method.invoke(livingEntity, level, damageSource, true);
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException ignored) {
            // Some modded entities may reject synthetic custom-death sampling; loot-table discovery still applies.
        } finally {
            livingEntity.captureDrops(previousDrops);
            livingEntity.discard();
        }
        for (ItemEntity itemEntity : drops) {
            ItemStack stack = itemEntity.getItem();
            if (!stack.isEmpty()) {
                add(candidates, entityId, BuiltInRegistries.ITEM.getKey(stack.getItem()), "death loot", CUSTOM_DEATH_LOOT_SAMPLES_PER_MOB);
            }
            itemEntity.discard();
        }
    }

    private static Method customDeathLootMethod(Class<?> entityClass) {
        Class<?> current = entityClass;
        while (current != null && LivingEntity.class.isAssignableFrom(current)) {
            try {
                Method method = current.getDeclaredMethod("dropCustomDeathLoot", ServerLevel.class, DamageSource.class, boolean.class);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static void sampleLootContext(
            ServerLevel level,
            ResourceLocation entityId,
            Entity entity,
            LootTable lootTable,
            ServerPlayer player,
            String source,
            Map<String, Accumulator> candidates
    ) {
        for (int sample = 0; sample < LOOT_SAMPLES_PER_MOB; sample++) {
            LootParams.Builder builder = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, entity)
                    .withParameter(LootContextParams.ORIGIN, entity.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, player == null ? level.damageSources().generic() : level.damageSources().playerAttack(player));
            if (player != null) {
                builder.withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                        .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player)
                        .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
                        .withLuck(0.0F);
            }

            LootParams params = builder.create(LootContextParamSets.ENTITY);
            for (ItemStack stack : lootTable.getRandomItems(params, sample + 1L)) {
                if (!stack.isEmpty()) {
                    add(candidates, entityId, BuiltInRegistries.ITEM.getKey(stack.getItem()), source, LOOT_SAMPLES_PER_MOB);
                }
            }
        }
    }

    private static void sampleEquipment(ServerLevel level, ResourceLocation entityId, EntityType<?> entityType, Map<String, Accumulator> candidates) {
        for (int sample = 0; sample < EQUIPMENT_SAMPLES_PER_MOB; sample++) {
            Entity entity = entityType.create(level);
            if (!(entity instanceof Mob mob)) {
                if (entity != null) {
                    entity.discard();
                }
                return;
            }
            DifficultyInstance difficulty = level.getCurrentDifficultyAt(BlockPos.ZERO);
            mob.finalizeSpawn(level, difficulty, MobSpawnType.SPAWNER, null);
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = mob.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    add(candidates, entityId, BuiltInRegistries.ITEM.getKey(stack.getItem()), "equipment", EQUIPMENT_SAMPLES_PER_MOB);
                }
            }
            mob.discard();
        }
    }

    private static void add(Map<String, Accumulator> candidates, ResourceLocation entityId, ResourceLocation itemId, String source, int samples) {
        String key = entityId + "|" + itemId + "|" + source;
        candidates.computeIfAbsent(key, ignored -> new Accumulator(entityId, itemId, source, samples)).observed++;
    }

    public static void writeList(net.minecraft.network.RegistryFriendlyByteBuf buffer, List<DumpNullDropCandidate> candidates) {
        buffer.writeVarInt(candidates.size());
        for (DumpNullDropCandidate candidate : candidates) {
            buffer.writeResourceLocation(candidate.entityId);
            buffer.writeResourceLocation(candidate.itemId);
            buffer.writeUtf(candidate.source);
            buffer.writeVarInt(candidate.observed);
            buffer.writeVarInt(candidate.samples);
        }
    }

    public static List<DumpNullDropCandidate> readList(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        int count = Math.min(buffer.readVarInt(), 4096);
        List<DumpNullDropCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            candidates.add(new DumpNullDropCandidate(
                    buffer.readResourceLocation(),
                    buffer.readResourceLocation(),
                    buffer.readUtf(),
                    buffer.readVarInt(),
                    buffer.readVarInt()
            ));
        }
        return candidates;
    }

    public String estimateLabel() {
        if (samples <= 0) {
            return "observed";
        }
        int percent = Math.round(observed * 100.0F / samples);
        return "~" + percent + "% observed";
    }

    private static final class Accumulator {
        private final ResourceLocation entityId;
        private final ResourceLocation itemId;
        private final String source;
        private final int samples;
        private int observed;

        private Accumulator(ResourceLocation entityId, ResourceLocation itemId, String source, int samples) {
            this.entityId = entityId;
            this.itemId = itemId;
            this.source = source;
            this.samples = samples;
        }

        private DumpNullDropCandidate candidate() {
            return new DumpNullDropCandidate(entityId, itemId, source, observed, samples);
        }
    }
}
