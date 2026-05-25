package dev.deepdaddyttv.deepnullreforged.dennull;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DenNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class DenNullAutomation {
    private static final ResourceLocation COW = ResourceLocation.withDefaultNamespace("cow");
    private static final ResourceLocation MOOSHROOM = ResourceLocation.withDefaultNamespace("mooshroom");
    private static final ResourceLocation SHEEP = ResourceLocation.withDefaultNamespace("sheep");

    private DenNullAutomation() {
    }

    public static void tickHeld(ServerPlayer player, int inventorySlot, ItemStack stack, DenNullItem item) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        DenNullData data = DenNullData.get(stack);
        DenNullData updated = runProduction(level, player, stack, item, data, null);
        if (updated != data) {
            DenNullData.set(stack, updated);
            syncPlayerInventory(player);
        }
    }

    public static void tickDock(ServerLevel level, BlockPos pos, DeepNullDockBlockEntity dock, ItemStack stack, DenNullItem item) {
        DenNullData data = DenNullData.get(stack);
        DenNullData updated = runProduction(level, null, stack, item, data, dock);
        if (updated != data) {
            DenNullData.set(stack, updated);
            dock.markStoredDeepNullChanged();
        }
    }

    public static void runPlayerCapture(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        Candidate candidate = captureCandidate(player);
        if (candidate == null) {
            return;
        }

        DenNullData data = DenNullData.get(candidate.stack);
        DenNullUpgradeData upgrades = data.upgrades();
        long gameTime = level.getGameTime();
        if (!upgrades.has(DenNullUpgradeType.CAPTURE)
                || !upgrades.captureEnabled()
                || gameTime - upgrades.lastCaptureTick() < DeepNullConfig.getDenCaptureIntervalTicks()) {
            return;
        }

        int captured = 0;
        double radius = DeepNullConfig.getDenCaptureRadius();
        AABB box = player.getBoundingBox().inflate(radius);
        List<Entity> targets = level.getEntities(player, box, entity -> isAutoCaptureCandidate(player, entity, upgrades))
                .stream()
                .sorted(Comparator.comparingDouble(player::distanceToSqr))
                .toList();

        DenNullData current = data;
        for (Entity target : targets) {
            if (captured >= DeepNullConfig.getDenCaptureMaxEntitiesPerCycle()) {
                break;
            }
            DenNullData next = tryAutoCapture(player, candidate.stack, candidate.item, target, current);
            if (next != current) {
                current = next;
                captured++;
            }
        }

        DenNullUpgradeData nextUpgrades = current.upgrades().withAutomationTicks(
                current.upgrades().lastBreedingTick(),
                current.upgrades().lastMilkTick(),
                current.upgrades().lastShearTick(),
                gameTime
        );
        current = current.withUpgrades(nextUpgrades);
        DenNullData.set(candidate.stack, current);
        syncPlayerInventory(player);
    }

    public static boolean consumeNameTagForRelease(ServerPlayer player, ItemStack denStack) {
        if (!DeepNullConfig.denTagUpgradeConsumesNameTags()) {
            return true;
        }
        DenNullData data = DenNullData.get(denStack);
        DenNullUpgradeData upgrades = data.upgrades();
        if (upgrades.nameTags() > 0) {
            DenNullData.set(denStack, data.withUpgrades(upgrades.withNameTags(upgrades.nameTags() - 1)));
            return true;
        }
        if (consumeFromPlayerInventory(player, new ItemStack(Items.NAME_TAG), 1)) {
            return true;
        }
        return consumeFromCarriedDeepNulls(player, new ItemStack(Items.NAME_TAG), 1);
    }

    public static boolean consumeBaitForManualCapture(ServerPlayer player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        if (consumeFromPlayerInventory(player, new ItemStack(ModItems.BAIT.get()), 1)) {
            return true;
        }
        return consumeFromCarriedDeepNulls(player, new ItemStack(ModItems.BAIT.get()), 1);
    }

    private static DenNullData runProduction(
            ServerLevel level,
            @Nullable ServerPlayer player,
            ItemStack stack,
            DenNullItem item,
            DenNullData data,
            @Nullable DeepNullDockBlockEntity dock
    ) {
        DenNullData current = data;
        DenNullUpgradeData upgrades = current.upgrades();
        long gameTime = level.getGameTime();

        long breedingTick = upgrades.lastBreedingTick();
        long milkTick = upgrades.lastMilkTick();
        long shearTick = upgrades.lastShearTick();
        long farmTick = upgrades.lastFarmTick();

        if (dock != null && upgrades.has(DenNullUpgradeType.SPAWNER) && !upgrades.has(DenNullUpgradeType.FARM) && !current.spawners().isEmpty()) {
            current = runVirtualSpawner(level, dock.getBlockPos(), current);
            upgrades = current.upgrades();
        }

        if (upgrades.has(DenNullUpgradeType.BREEDING)
                && gameTime - breedingTick >= DeepNullConfig.getDenBreedingIntervalTicks()) {
            current = runBreeding(level, player, stack, item, current);
            breedingTick = gameTime;
            upgrades = current.upgrades();
        }

        if (upgrades.has(DenNullUpgradeType.MILK)
                && gameTime - milkTick >= DeepNullConfig.getDenMilkIntervalTicks()) {
            current = runMilk(current);
            milkTick = gameTime;
            upgrades = current.upgrades();
        }

        if (upgrades.has(DenNullUpgradeType.SHEAR)
                && gameTime - shearTick >= DeepNullConfig.getDenShearIntervalTicks()) {
            runShear(player, dock, current);
            shearTick = gameTime;
            upgrades = current.upgrades();
        }

        if (upgrades.has(DenNullUpgradeType.FARM)
                && gameTime - farmTick >= DeepNullConfig.getDenFarmIntervalTicks()) {
            current = runFarm(level, player, dock, current);
            farmTick = gameTime;
            upgrades = current.upgrades();
        }

        DenNullUpgradeData nextUpgrades = upgrades.withAutomationTicks(breedingTick, milkTick, shearTick, upgrades.lastCaptureTick(), farmTick);
        return current.withUpgrades(nextUpgrades);
    }

    private static DenNullData runBreeding(ServerLevel level, @Nullable ServerPlayer player, ItemStack denStack, DenNullItem item, DenNullData data) {
        MutableBreedingSource source = new MutableBreedingSource(data.upgrades().breedingItems(), item.tier().creative()
                ? Integer.MAX_VALUE
                : Math.max(16, Math.min(512, item.tier().ordinalId() <= 0 ? 16 : 16 << item.tier().ordinalId())));
        int births = 0;
        DenNullData current = data;

        for (DenNullEntry entry : data.entries()) {
            if (births >= DeepNullConfig.getDenBreedingMaxBirthsPerCycle()) {
                break;
            }
            if (entry.count() < 2) {
                continue;
            }
            ItemStack food = findBreedingFood(level, entry, source, player);
            if (food.isEmpty()) {
                continue;
            }
            while (births < DeepNullConfig.getDenBreedingMaxBirthsPerCycle()
                    && entry.count() + births >= 2
                    && consumeBreedingItem(source, player, food, 2)) {
                DenNullData.AddResult result = current.addCapture(entry.withCount(1), item.tier());
                if (!result.success()) {
                    break;
                }
                current = result.data();
                births++;
            }
        }

        return current.withUpgrades(current.upgrades().withBreedingItems(source.entries()));
    }

    private static DenNullData runMilk(DenNullData data) {
        int available = 0;
        for (DenNullEntry entry : data.entries()) {
            if (entry.entityType().equals(COW) || entry.entityType().equals(MOOSHROOM)) {
                available += Math.max(0, entry.count());
            }
        }
        if (available <= 0) {
            return data;
        }
        DenNullUpgradeData upgrades = data.upgrades();
        int room = DenNullUpgradeData.MILK_BUCKET_CAPACITY - upgrades.milkBuckets();
        int generated = Math.min(room, Math.min(available, DeepNullConfig.getDenMilkMaxBucketsPerCycle()));
        return generated <= 0 ? data : data.withUpgrades(upgrades.withMilkBuckets(upgrades.milkBuckets() + generated));
    }

    private static void runShear(@Nullable ServerPlayer player, @Nullable DeepNullDockBlockEntity dock, DenNullData data) {
        int remaining = DeepNullConfig.getDenShearMaxSheepPerCycle();
        for (DenNullEntry entry : data.entries()) {
            if (remaining <= 0 || !entry.entityType().equals(SHEEP)) {
                continue;
            }
            int amount = Math.min(remaining, Math.max(0, entry.count()));
            ItemStack wool = new ItemStack(woolFor(sheepColor(data, entry)), amount);
            if (routeOutput(player, dock, wool)) {
                remaining -= amount;
            }
        }
    }

    private static DenNullData runFarm(ServerLevel level, @Nullable ServerPlayer player, @Nullable DeepNullDockBlockEntity dock, DenNullData data) {
        if (!data.spawners().isEmpty()) {
            return runSpawnerFarm(level, player, dock, data);
        }

        int remaining = DeepNullConfig.getDenFarmMaxKillsPerCycle();
        List<DenNullEntry> updatedEntries = new ArrayList<>(data.entries());
        DenNullUpgradeData upgrades = data.upgrades();

        for (int index = 0; index < updatedEntries.size() && remaining > 0; index++) {
            DenNullEntry entry = updatedEntries.get(index);
            int threshold = Math.max(0, upgrades.farmThreshold(index));
            int surplus = Math.max(0, entry.count() - threshold);
            int kills = Math.min(remaining, surplus);
            if (kills <= 0) {
                continue;
            }
            List<ItemStack> drops = rollLoot(level, player, null, entry.entityType(), entry.entityTag(), kills);
            LootInsertResult insert = insertLoot(upgrades, drops, dock);
            if (!insert.success()) {
                break;
            }
            upgrades = insert.upgrades();
            updatedEntries.set(index, entry.withCount(entry.count() - kills));
            remaining -= kills;
        }

        return data.withEntries(updatedEntries).withUpgrades(upgrades);
    }

    private static DenNullData runSpawnerFarm(ServerLevel level, @Nullable ServerPlayer player, @Nullable DeepNullDockBlockEntity dock, DenNullData data) {
        DenNullSpawnerEntry selected = data.selectedSpawnerEntry();
        if (selected == null) {
            return data;
        }
        CompoundTag spawnerTag = selected.spawnerTag().copy();
        int delay = spawnerTag.contains("Delay") ? spawnerTag.getShort("Delay") : 20;
        if (delay > 0) {
            spawnerTag.putShort("Delay", (short) Math.max(0, delay - DeepNullConfig.getDenFarmIntervalTicks()));
            return data.withSpawnerEntries(replaceSpawner(data.spawners(), data.selectedIndex(), selected, spawnerTag));
        }

        int spawnCount = spawnerTag.contains("SpawnCount") ? Math.max(1, spawnerTag.getShort("SpawnCount")) : 4;
        int kills = Math.min(spawnCount, DeepNullConfig.getDenFarmMaxKillsPerCycle());
        CompoundTag entityTag = DenNullSpawnerEntry.entityToSpawn(spawnerTag);
        ResourceLocation entityId = DenNullSpawnerEntry.entityType(spawnerTag);
        List<ItemStack> drops = rollLoot(level, player, null, entityId, entityTag, kills);
        LootInsertResult insert = insertLoot(data.upgrades(), drops, dock);
        if (!insert.success()) {
            return data;
        }

        int minDelay = spawnerTag.contains("MinSpawnDelay") ? spawnerTag.getShort("MinSpawnDelay") : 200;
        int maxDelay = spawnerTag.contains("MaxSpawnDelay") ? spawnerTag.getShort("MaxSpawnDelay") : 800;
        int nextDelay = maxDelay <= minDelay ? minDelay : minDelay + level.random.nextInt(maxDelay - minDelay);
        spawnerTag.putShort("Delay", (short) nextDelay);
        return data.withSpawnerEntries(replaceSpawner(data.spawners(), data.selectedIndex(), selected, spawnerTag))
                .withUpgrades(insert.upgrades());
    }

    private static DenNullData runVirtualSpawner(ServerLevel level, BlockPos pos, DenNullData data) {
        DenNullSpawnerEntry selected = data.selectedSpawnerEntry();
        if (selected == null) {
            return data;
        }
        CompoundTag spawnerTag = selected.spawnerTag().copy();
        int delay = spawnerTag.contains("Delay") ? spawnerTag.getShort("Delay") : 20;
        if (delay > 0) {
            spawnerTag.putShort("Delay", (short) (delay - 1));
            return data.withSpawnerEntries(replaceSpawner(data.spawners(), data.selectedIndex(), selected, spawnerTag));
        }

        ResourceLocation entityId = DenNullSpawnerEntry.entityType(spawnerTag);
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).orElse(null);
        if (type == null || (!type.getCategory().isFriendly() && level.getDifficulty() == Difficulty.PEACEFUL)) {
            return data;
        }

        int spawnCount = spawnerTag.contains("SpawnCount") ? Math.max(1, spawnerTag.getShort("SpawnCount")) : 4;
        int spawnRange = spawnerTag.contains("SpawnRange") ? Math.max(1, spawnerTag.getShort("SpawnRange")) : 4;
        int maxNearby = spawnerTag.contains("MaxNearbyEntities") ? Math.max(1, spawnerTag.getShort("MaxNearbyEntities")) : 6;
        int spawned = 0;
        for (int i = 0; i < spawnCount; i++) {
            double x = (double) pos.getX() + (level.random.nextDouble() - level.random.nextDouble()) * (double) spawnRange + 0.5D;
            double y = (double) (pos.getY() + level.random.nextInt(3) - 1);
            double z = (double) pos.getZ() + (level.random.nextDouble() - level.random.nextDouble()) * (double) spawnRange + 0.5D;
            if (!level.noCollision(type.getSpawnAABB(x, y, z))) {
                continue;
            }
            BlockPos spawnPos = BlockPos.containing(x, y, z);
            if (!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.SPAWNER, spawnPos, level.random)) {
                continue;
            }
            Entity entity = type.create(level);
            if (entity == null) {
                continue;
            }
            CompoundTag entityTag = DenNullSpawnerEntry.entityToSpawn(spawnerTag);
            if (!entityTag.isEmpty()) {
                entity.load(entityTag);
            }
            if (level.getEntities(entity, new AABB(pos).inflate(spawnRange), other -> other.getType() == type).size() >= maxNearby) {
                continue;
            }
            entity.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
            if (level.tryAddFreshEntityWithPassengers(entity)) {
                level.levelEvent(2004, pos, 0);
                spawned++;
            }
        }
        if (spawned > 0) {
            int minDelay = spawnerTag.contains("MinSpawnDelay") ? spawnerTag.getShort("MinSpawnDelay") : 200;
            int maxDelay = spawnerTag.contains("MaxSpawnDelay") ? spawnerTag.getShort("MaxSpawnDelay") : 800;
            int nextDelay = maxDelay <= minDelay ? minDelay : minDelay + level.random.nextInt(maxDelay - minDelay);
            spawnerTag.putShort("Delay", (short) nextDelay);
        }
        return data.withSpawnerEntries(replaceSpawner(data.spawners(), data.selectedIndex(), selected, spawnerTag));
    }

    private static List<DenNullSpawnerEntry> replaceSpawner(List<DenNullSpawnerEntry> entries, int index, DenNullSpawnerEntry previous, CompoundTag spawnerTag) {
        List<DenNullSpawnerEntry> updated = new ArrayList<>(entries);
        if (index >= 0 && index < updated.size()) {
            updated.set(index, new DenNullSpawnerEntry(previous.entityType(), previous.count(), previous.identityKey(), spawnerTag, previous.displayName(), previous.summary()));
        }
        return updated;
    }

    private static List<ItemStack> rollLoot(
            ServerLevel level,
            @Nullable ServerPlayer player,
            @Nullable BlockPos origin,
            ResourceLocation entityId,
            CompoundTag entityTag,
            int count
    ) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).orElse(null);
        if (type == null || count <= 0) {
            return List.of();
        }
        List<ItemStack> drops = new ArrayList<>();
        BlockPos lootOrigin = origin == null ? BlockPos.ZERO : origin;
        for (int i = 0; i < count; i++) {
            Entity entity = type.create(level);
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            CompoundTag tag = entityTag == null ? new CompoundTag() : entityTag.copy();
            if (!tag.isEmpty()) {
                living.load(tag);
            }
            Vec3 originPos = Vec3.atCenterOf(lootOrigin);
            living.moveTo(originPos.x, originPos.y, originPos.z, 0.0F, 0.0F);
            DamageSource damageSource = level.damageSources().genericKill();
            ResourceKey<LootTable> lootKey = living.getLootTable();
            LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(lootKey);
            LootParams.Builder builder = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, living)
                    .withParameter(LootContextParams.ORIGIN, living.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                    .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player)
                    .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player);
            if (player != null) {
                builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player).withLuck(player.getLuck());
            }
            drops.addAll(lootTable.getRandomItems(builder.create(LootContextParamSets.ENTITY), living.getLootTableSeed()));
        }
        return drops;
    }

    private static LootInsertResult insertLoot(DenNullUpgradeData upgrades, List<ItemStack> drops, @Nullable DeepNullDockBlockEntity dock) {
        if (drops.isEmpty()) {
            return new LootInsertResult(true, upgrades);
        }
        MutableLootBuffer buffer = new MutableLootBuffer(upgrades.lootItems(), DeepNullConfig.getDenFarmLootBufferSlots());
        List<ItemStack> dockRemainders = new ArrayList<>();
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            ItemStack remainder = buffer.insert(drop);
            if (!remainder.isEmpty()) {
                dockRemainders.add(remainder);
            }
        }
        if (dock == null && !dockRemainders.isEmpty()) {
            return new LootInsertResult(false, upgrades);
        }
        if (dock != null) {
            for (ItemStack remainder : dockRemainders) {
                if (!dock.pushDenNullOutput(remainder, true).isEmpty()) {
                    return new LootInsertResult(false, upgrades);
                }
            }
            for (ItemStack remainder : dockRemainders) {
                dock.pushDenNullOutput(remainder, false);
            }
        }
        return new LootInsertResult(true, upgrades.withLootItems(buffer.entries()));
    }

    private static boolean routeOutput(@Nullable ServerPlayer player, @Nullable DeepNullDockBlockEntity dock, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (dock != null) {
            ItemStack preview = dock.pushDenNullOutput(stack, true);
            if (!preview.isEmpty()) {
                return false;
            }
            dock.pushDenNullOutput(stack, false);
            return true;
        }
        if (player == null) {
            return false;
        }
        ItemStack remaining = routeIntoCarriedDeepNulls(player, stack, true);
        if (!canPlaceInInventory(player, remaining)) {
            return false;
        }
        remaining = routeIntoCarriedDeepNulls(player, stack, false);
        if (!remaining.isEmpty()) {
            player.getInventory().placeItemBackInInventory(remaining);
        }
        return true;
    }

    private static DenNullData tryAutoCapture(ServerPlayer player, ItemStack stack, DenNullItem item, Entity target, DenNullData data) {
        Optional<DenNullEntry> captured = DenNullCaptureNormalizer.capture(target);
        if (captured.isEmpty()) {
            return data;
        }
        DenNullData.AddResult result = data.addCapture(captured.get(), item.tier());
        if (!result.success() || !consumeBaitForManualCapture(player)) {
            return data;
        }
        target.discard();
        return result.data();
    }

    private static boolean isAutoCaptureCandidate(ServerPlayer player, Entity entity, DenNullUpgradeData upgrades) {
        if (!(entity instanceof LivingEntity) || entity == player || entity.isRemoved() || entity.isPassenger() || !entity.getPassengers().isEmpty()) {
            return false;
        }
        if (entity instanceof TamableAnimal tamable && tamable.isTame()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return id != null && upgrades.captureAllows(id);
    }

    private static Candidate captureCandidate(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof DenNullItem mainDen && hasActiveCapture(main)) {
            return new Candidate(main, mainDen);
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof DenNullItem offhandDen && hasActiveCapture(offhand)) {
            return new Candidate(offhand, offhandDen);
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof DenNullItem denNullItem && hasActiveCapture(stack)) {
                return new Candidate(stack, denNullItem);
            }
        }
        return null;
    }

    private static boolean hasActiveCapture(ItemStack stack) {
        DenNullUpgradeData upgrades = DenNullData.get(stack).upgrades();
        return upgrades.has(DenNullUpgradeType.CAPTURE) && upgrades.captureEnabled() && !upgrades.has(DenNullUpgradeType.SPAWNER);
    }

    private static ItemStack findBreedingFood(ServerLevel level, DenNullEntry entry, MutableBreedingSource source, @Nullable ServerPlayer player) {
        for (DenNullBufferEntry bufferEntry : source.entries()) {
            ItemStack sample = bufferEntry.stack().copyWithCount(1);
            if (isFoodFor(level, entry, sample)) {
                return sample;
            }
        }
        if (player != null) {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack sample = player.getInventory().getItem(slot);
                if (!sample.isEmpty() && isFoodFor(level, entry, sample)) {
                    return sample.copyWithCount(1);
                }
            }
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack carrier = player.getInventory().getItem(slot);
                if (!(carrier.getItem() instanceof DeepNullItem deepNullItem) || carrier.getItem() instanceof DampNullItem) {
                    continue;
                }
                DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), carrier, player.level().registryAccess(), null);
                for (int deepSlot = 0; deepSlot < inventory.getSlots(); deepSlot++) {
                    ItemStack sample = inventory.getStackInSlot(deepSlot);
                    if (!sample.isEmpty() && isFoodFor(level, entry, sample)) {
                        return sample.copyWithCount(1);
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean isFoodFor(ServerLevel level, DenNullEntry entry, ItemStack sample) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entry.entityType()).orElse(null);
        if (type == null) {
            return false;
        }
        Entity entity = type.create(level);
        return entity instanceof Animal animal && animal.isFood(sample);
    }

    private static boolean consumeBreedingItem(MutableBreedingSource source, @Nullable ServerPlayer player, ItemStack sample, int amount) {
        if (source.consume(sample, amount)) {
            return true;
        }
        if (player == null) {
            return false;
        }
        if (consumeFromPlayerInventory(player, sample, amount)) {
            return true;
        }
        return consumeFromCarriedDeepNulls(player, sample, amount);
    }

    private static boolean consumeFromPlayerInventory(ServerPlayer player, ItemStack sample, int amount) {
        int remaining = amount;
        List<ItemStack> consumedFrom = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, sample)) {
                continue;
            }
            int consumed = Math.min(remaining, stack.getCount());
            consumedFrom.add(stack);
            remaining -= consumed;
        }
        if (remaining > 0) {
            return false;
        }
        remaining = amount;
        for (ItemStack stack : consumedFrom) {
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
            if (remaining <= 0) {
                break;
            }
        }
        player.getInventory().setChanged();
        return true;
    }

    private static boolean consumeFromCarriedDeepNulls(ServerPlayer player, ItemStack sample, int amount) {
        int remaining = amount;
        List<DeepConsumption> planned = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack carrier = player.getInventory().getItem(slot);
            if (!(carrier.getItem() instanceof DeepNullItem deepNullItem) || carrier.getItem() instanceof DampNullItem) {
                continue;
            }
            DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), carrier, player.level().registryAccess(), null);
            int deepSlot = inventory.findMatchingSlot(sample);
            if (deepSlot < 0) {
                continue;
            }
            int available = inventory.extractItemIgnoreExtractionMode(deepSlot, remaining, true).getCount();
            if (available <= 0) {
                continue;
            }
            planned.add(new DeepConsumption(inventory, deepSlot, Math.min(available, remaining)));
            remaining -= available;
        }
        if (remaining > 0) {
            return false;
        }
        for (DeepConsumption consumption : planned) {
            consumption.inventory.extractItemIgnoreExtractionMode(consumption.slot, consumption.amount, false);
        }
        syncPlayerInventory(player);
        return true;
    }

    private static ItemStack routeIntoCarriedDeepNulls(ServerPlayer player, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < player.getInventory().getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack carrier = player.getInventory().getItem(slot);
            if (!(carrier.getItem() instanceof DeepNullItem deepNullItem) || carrier.getItem() instanceof DampNullItem) {
                continue;
            }
            DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), carrier, player.level().registryAccess(), null);
            remaining = inventory.insertIntoFirstAvailableSlot(remaining, simulate);
        }
        return remaining;
    }

    private static boolean canPlaceInInventory(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        int remaining = stack.getCount();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack existing = player.getInventory().getItem(slot);
            if (existing.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameComponents(existing, stack)) {
                remaining -= Math.max(0, existing.getMaxStackSize() - existing.getCount());
                if (remaining <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static DyeColor sheepColor(DenNullData data, DenNullEntry entry) {
        if (data.upgrades().dyeColorId() >= 0) {
            return DyeColor.byId(data.upgrades().dyeColorId());
        }
        CompoundTag tag = entry.entityTag();
        return tag.contains("Color") ? DyeColor.byId(tag.getInt("Color")) : DyeColor.WHITE;
    }

    private static Item woolFor(DyeColor color) {
        return switch (color) {
            case WHITE -> Items.WHITE_WOOL;
            case ORANGE -> Items.ORANGE_WOOL;
            case MAGENTA -> Items.MAGENTA_WOOL;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_WOOL;
            case YELLOW -> Items.YELLOW_WOOL;
            case LIME -> Items.LIME_WOOL;
            case PINK -> Items.PINK_WOOL;
            case GRAY -> Items.GRAY_WOOL;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_WOOL;
            case CYAN -> Items.CYAN_WOOL;
            case PURPLE -> Items.PURPLE_WOOL;
            case BLUE -> Items.BLUE_WOOL;
            case BROWN -> Items.BROWN_WOOL;
            case GREEN -> Items.GREEN_WOOL;
            case RED -> Items.RED_WOOL;
            case BLACK -> Items.BLACK_WOOL;
        };
    }

    private static void syncPlayerInventory(ServerPlayer player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
    }

    private record Candidate(ItemStack stack, DenNullItem item) {
    }

    private record DeepConsumption(DeepNullInventory inventory, int slot, int amount) {
    }

    private record LootInsertResult(boolean success, DenNullUpgradeData upgrades) {
    }

    private static final class MutableBreedingSource {
        private final List<DenNullBufferEntry> entries;
        private final int maxPerEntry;

        private MutableBreedingSource(List<DenNullBufferEntry> entries, int maxPerEntry) {
            this.entries = new ArrayList<>(entries);
            this.maxPerEntry = maxPerEntry;
        }

        private List<DenNullBufferEntry> entries() {
            return List.copyOf(entries);
        }

        private boolean consume(ItemStack sample, int amount) {
            int index = find(sample);
            if (index < 0 || entries.get(index).count() < amount) {
                return false;
            }
            DenNullBufferEntry entry = entries.get(index);
            int next = entry.count() - amount;
            if (next <= 0) {
                entries.remove(index);
            } else {
                entries.set(index, entry.withCount(next));
            }
            return true;
        }

        @SuppressWarnings("unused")
        private boolean add(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            int index = find(stack);
            if (index >= 0) {
                DenNullBufferEntry entry = entries.get(index);
                int next = Math.min(maxPerEntry, entry.count() + stack.getCount());
                entries.set(index, entry.withCount(next));
                return next > entry.count();
            }
            if (entries.size() >= DenNullUpgradeData.BREEDING_BUFFER_SLOTS) {
                return false;
            }
            entries.add(DenNullBufferEntry.fromStack(stack.copyWithCount(Math.min(maxPerEntry, stack.getCount()))));
            return true;
        }

        private int find(ItemStack sample) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(sample.getItem());
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).itemId().equals(id)) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static final class MutableLootBuffer {
        private final List<DenNullBufferEntry> entries;
        private final int maxSlots;

        private MutableLootBuffer(List<DenNullBufferEntry> entries, int maxSlots) {
            this.entries = new ArrayList<>(entries);
            this.maxSlots = Math.max(1, Math.min(DenNullUpgradeData.LOOT_BUFFER_SLOTS, maxSlots));
        }

        private List<DenNullBufferEntry> entries() {
            return List.copyOf(entries);
        }

        private ItemStack insert(ItemStack source) {
            ItemStack remaining = source.copy();
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(remaining.getItem());
            for (int i = 0; i < entries.size() && !remaining.isEmpty(); i++) {
                DenNullBufferEntry entry = entries.get(i);
                if (!entry.itemId().equals(id)) {
                    continue;
                }
                int room = Math.max(0, 64 - entry.count());
                int moved = Math.min(room, remaining.getCount());
                if (moved > 0) {
                    entries.set(i, entry.withCount(entry.count() + moved));
                    remaining.shrink(moved);
                }
            }
            while (!remaining.isEmpty() && entries.size() < maxSlots) {
                int moved = Math.min(64, remaining.getCount());
                entries.add(new DenNullBufferEntry(id, moved));
                remaining.shrink(moved);
            }
            return remaining;
        }
    }
}
