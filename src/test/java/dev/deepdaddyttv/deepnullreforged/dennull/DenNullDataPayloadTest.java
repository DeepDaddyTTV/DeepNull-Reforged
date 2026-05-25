package dev.deepdaddyttv.deepnullreforged.dennull;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.NullSlotDomain;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import dev.deepdaddyttv.deepnullreforged.network.DenNullPayloads;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DenNullDataPayloadTest {
    @Test
    void denNullDataRoundTripsEntriesAndSelectedIndex() {
        CompoundTag cowTag = new CompoundTag();
        cowTag.putString("CustomName", "{\"text\":\"Moo\"}");
        DenNullEntry cow = new DenNullEntry(ResourceLocation.withDefaultNamespace("cow"), 2, "cow|named", cowTag, "Moo", "custom name");
        DenNullData loaded = DenNullData.load(new DenNullData(4, List.of(cow)).save());

        assertEquals(0, loaded.selectedIndex());
        assertEquals(1, loaded.entries().size());
        assertEquals("Moo", loaded.entries().get(0).displayName());
        assertEquals("custom name", loaded.entries().get(0).summary());
    }

    @Test
    void denIdentityNormalizationIgnoresRandomSpawnBonusAndRestacks() {
        ResourceLocation foxId = ResourceLocation.withDefaultNamespace("fox");
        DenNullEntry first = new DenNullEntry(foxId, 1, "fox|one", foxTagWithRandomSpawnBonus(-0.019D), "Fox", "standard data");
        DenNullEntry second = new DenNullEntry(foxId, 2, "fox|two", foxTagWithRandomSpawnBonus(0.095D), "Fox", "standard data");

        DenNullData data = DenNullData.load(new DenNullData(0, List.of(first, second)).save());

        assertEquals(1, data.entries().size());
        assertEquals(3, data.entries().get(0).count());
        assertTrue(DenNullCaptureNormalizer.detailLines(data.entries().get(0)).stream().anyMatch(line -> line.equals("Type: red")));
    }

    @Test
    void denNullCaptureStacksByNormalizedKeyAndReportsCapacity() {
        DenNullEntry first = new DenNullEntry(ResourceLocation.withDefaultNamespace("pig"), 1, "pig|adult", new CompoundTag(), "Pig", "standard data");
        DenNullEntry same = new DenNullEntry(ResourceLocation.withDefaultNamespace("pig"), 1, "pig|adult", new CompoundTag(), "Pig", "standard data");
        DenNullData.AddResult stacked = DenNullData.EMPTY.addCapture(first, DeepNullTier.REDSTONE).data().addCapture(same, DeepNullTier.REDSTONE);

        assertTrue(stacked.success());
        assertEquals(1, stacked.data().entries().size());
        assertEquals(2, stacked.data().entries().get(0).count());
    }

    private static CompoundTag foxTagWithRandomSpawnBonus(double amount) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", "red");
        tag.putInt("Age", 0);
        ListTag attributes = new ListTag();
        CompoundTag followRange = new CompoundTag();
        followRange.putString("id", "minecraft:generic.follow_range");
        followRange.putDouble("base", 32.0D);
        ListTag modifiers = new ListTag();
        CompoundTag modifier = new CompoundTag();
        modifier.putString("id", "minecraft:random_spawn_bonus");
        modifier.putDouble("amount", amount);
        modifier.putString("operation", "add_multiplied_base");
        modifiers.add(modifier);
        followRange.put("modifiers", modifiers);
        attributes.add(followRange);
        tag.put("attributes", attributes);
        return tag;
    }

    @Test
    void denNullSelectionCyclesAcrossStoredEntries() {
        DenNullEntry cow = new DenNullEntry(ResourceLocation.withDefaultNamespace("cow"), 1, "cow", new CompoundTag(), "Cow", "standard data");
        DenNullEntry pig = new DenNullEntry(ResourceLocation.withDefaultNamespace("pig"), 1, "pig", new CompoundTag(), "Pig", "standard data");
        DenNullEntry fox = new DenNullEntry(ResourceLocation.withDefaultNamespace("fox"), 1, "fox", new CompoundTag(), "Fox", "standard data");
        DenNullData data = new DenNullData(0, List.of(cow, pig, fox));

        assertEquals(1, data.cycleSelected(true).selectedIndex());
        assertEquals(2, data.cycleSelected(false).selectedIndex());
        assertEquals(0, new DenNullData(2, List.of(cow, pig, fox)).cycleSelected(true).selectedIndex());
        assertEquals(2, new DenNullData(0, List.of(cow, pig, fox)).cycleSelected(false).selectedIndex());
        assertEquals(0, DenNullData.EMPTY.cycleSelected(true).selectedIndex());
    }

    @Test
    void denNullReleaseAtIndexConsumesHoveredEntryAndPreservesSelection() {
        DenNullEntry cow = new DenNullEntry(ResourceLocation.withDefaultNamespace("cow"), 2, "cow", new CompoundTag(), "Cow", "standard data");
        DenNullEntry pig = new DenNullEntry(ResourceLocation.withDefaultNamespace("pig"), 1, "pig", new CompoundTag(), "Pig", "standard data");
        DenNullEntry fox = new DenNullEntry(ResourceLocation.withDefaultNamespace("fox"), 1, "fox", new CompoundTag(), "Fox", "standard data");
        DenNullData data = new DenNullData(2, List.of(cow, pig, fox));

        DenNullData.ReleaseResult releasedBeforeSelection = data.releaseAtIndex(0);
        assertTrue(releasedBeforeSelection.success());
        assertEquals(ResourceLocation.withDefaultNamespace("cow"), releasedBeforeSelection.entry().entityType());
        assertEquals(1, releasedBeforeSelection.data().entries().get(0).count());
        assertEquals(1, releasedBeforeSelection.data().selectedIndex());

        DenNullData.ReleaseResult releasedSelected = data.releaseAtIndex(2);
        assertTrue(releasedSelected.success());
        assertEquals(ResourceLocation.withDefaultNamespace("fox"), releasedSelected.entry().entityType());
        assertEquals(2, releasedSelected.data().entries().size());
        assertEquals(1, releasedSelected.data().selectedIndex());
    }

    @Test
    void oldDenNullSaveDefaultsUpgradeState() {
        CompoundTag oldSave = new DenNullData(0, List.of()).save();
        oldSave.remove("Upgrades");

        DenNullData loaded = DenNullData.load(oldSave);

        assertTrue(loaded.upgrades().installed().isEmpty());
        assertEquals(-1, loaded.upgrades().dyeColorId());
        assertFalse(loaded.upgrades().babyEnabled());
        assertFalse(loaded.upgrades().captureEnabled());
        assertEquals(DenNullUpgradeData.DEFAULT_TAG_TEMPLATE, loaded.upgrades().tagTemplate());
        assertEquals(DenNullCaptureFilterMode.OFF, loaded.upgrades().captureFilterMode());
    }

    @Test
    void denNullUpgradeStateRoundTripsThroughNbtAndPayload() {
        DenNullUpgradeData upgrades = new DenNullUpgradeData(
                EnumSet.of(DenNullUpgradeType.BREEDING, DenNullUpgradeType.DYE, DenNullUpgradeType.CAPTURE),
                List.of(new DenNullBufferEntry(ResourceLocation.withDefaultNamespace("wheat"), 24)),
                99,
                3,
                14,
                true,
                "{player}:{type}:{counter}",
                42,
                true,
                DenNullCaptureFilterMode.WHITELIST,
                List.of(ResourceLocation.withDefaultNamespace("cow"), ResourceLocation.withDefaultNamespace("sheep")),
                List.of(new DenNullBufferEntry(ResourceLocation.withDefaultNamespace("leather"), 5)),
                List.of(3, 5),
                10L,
                20L,
                30L,
                40L,
                50L
        );
        DenNullData data = new DenNullData(0, List.of(), List.of(), upgrades);
        DenNullData loaded = DenNullData.load(data.save());
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DenNullData.write(buffer, loaded);
        DenNullData decoded = DenNullData.read(buffer);

        assertTrue(decoded.upgrades().has(DenNullUpgradeType.BREEDING));
        assertTrue(decoded.upgrades().has(DenNullUpgradeType.DYE));
        assertTrue(decoded.upgrades().has(DenNullUpgradeType.CAPTURE));
        assertEquals(32, decoded.upgrades().milkBuckets(), "Milk buffer should clamp to capacity");
        assertEquals(3, decoded.upgrades().nameTags());
        assertEquals(14, decoded.upgrades().dyeColorId());
        assertTrue(decoded.upgrades().babyEnabled());
        assertTrue(decoded.upgrades().captureEnabled());
        assertEquals(DenNullCaptureFilterMode.WHITELIST, decoded.upgrades().captureFilterMode());
        assertEquals(2, decoded.upgrades().captureFilter().size());
        assertEquals(1, decoded.upgrades().lootItems().size());
        assertEquals(5, decoded.upgrades().farmThreshold(1));
        assertEquals(40L, decoded.upgrades().lastCaptureTick());
        assertEquals(50L, decoded.upgrades().lastFarmTick());
    }

    @Test
    void denSpawnerEntriesRoundTripAndRejectMixedNormalState() {
        CompoundTag spawnerTag = new CompoundTag();
        spawnerTag.putString("id", "minecraft:mob_spawner");
        spawnerTag.putInt("x", 10);
        spawnerTag.putInt("y", 64);
        spawnerTag.putInt("z", -3);
        spawnerTag.putShort("SpawnCount", (short) 2);
        spawnerTag.putShort("SpawnRange", (short) 5);
        CompoundTag spawnData = new CompoundTag();
        CompoundTag entity = new CompoundTag();
        entity.putString("id", "minecraft:zombie");
        spawnData.put("entity", entity);
        spawnerTag.put("SpawnData", spawnData);

        DenNullSpawnerEntry spawner = new DenNullSpawnerEntry(
                ResourceLocation.withDefaultNamespace("zombie"),
                1,
                "zombie|spawner",
                spawnerTag,
                "Zombie Spawner",
                ""
        );
        DenNullData spawnerData = new DenNullData(0, List.of(), List.of(spawner), List.of(),
                DenNullUpgradeData.EMPTY.withInstalled(DenNullUpgradeType.SPAWNER));
        DenNullData loaded = DenNullData.load(spawnerData.save());
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DenNullData.write(buffer, loaded);
        DenNullData decoded = DenNullData.read(buffer);

        assertEquals(1, decoded.spawners().size());
        assertEquals(ResourceLocation.withDefaultNamespace("zombie"), decoded.spawners().get(0).entityType());
        assertFalse(decoded.spawners().get(0).spawnerTag().contains("x"));
        assertFalse(decoded.spawners().get(0).spawnerTag().contains("id"));
        assertTrue(decoded.upgrades().has(DenNullUpgradeType.SPAWNER));

        DenNullEntry cow = new DenNullEntry(ResourceLocation.withDefaultNamespace("cow"), 1, "cow", new CompoundTag(), "Cow", "standard data");
        assertEquals(DenNullData.AddFailure.MIXED_MODE, decoded.addCapture(cow, DeepNullTier.REDSTONE).failure());
        assertEquals(DenNullData.AddFailure.MIXED_MODE, new DenNullData(0, List.of(cow)).addSpawner(spawner, DeepNullTier.REDSTONE).failure());
    }

    @Test
    void denFarmThresholdsClampAndDefaultMissingSlots() {
        DenNullUpgradeData upgrades = DenNullUpgradeData.EMPTY.withFarmThreshold(2, -5);
        DenNullUpgradeData loaded = DenNullUpgradeData.load(upgrades.save());

        assertEquals(DenNullUpgradeData.DEFAULT_FARM_THRESHOLD, loaded.farmThreshold(0));
        assertEquals(DenNullUpgradeData.DEFAULT_FARM_THRESHOLD, loaded.farmThreshold(1));
        assertEquals(0, loaded.farmThreshold(2));
        assertEquals(DenNullUpgradeData.DEFAULT_FARM_THRESHOLD, loaded.farmThreshold(999));
    }

    @Test
    void denNullUpgradeSlotsRemainStable() {
        assertEquals(0, DenNullUpgradeType.BREEDING.slot());
        assertEquals(1, DenNullUpgradeType.CLONE.slot());
        assertEquals(2, DenNullUpgradeType.DYE.slot());
        assertEquals(3, DenNullUpgradeType.MILK.slot());
        assertEquals(4, DenNullUpgradeType.SHEAR.slot());
        assertEquals(5, DenNullUpgradeType.BABY.slot());
        assertEquals(6, DenNullUpgradeType.TAG.slot());
        assertEquals(7, DenNullUpgradeType.CAPTURE.slot());
        assertEquals(8, DenNullUpgradeType.SPAWNER.slot());
        assertEquals(9, DenNullUpgradeType.FARM.slot());
        assertEquals(DenNullUpgradeType.CLONE, DenNullUpgradeType.byItemId("den_clone_upgrade"));
    }

    @Test
    void captureFilterModesMatchEntityIds() {
        ResourceLocation cow = ResourceLocation.withDefaultNamespace("cow");
        ResourceLocation sheep = ResourceLocation.withDefaultNamespace("sheep");

        DenNullUpgradeData whitelist = DenNullUpgradeData.EMPTY.withCaptureFilter(DenNullCaptureFilterMode.WHITELIST, List.of(cow));
        DenNullUpgradeData blacklist = DenNullUpgradeData.EMPTY.withCaptureFilter(DenNullCaptureFilterMode.BLACKLIST, List.of(cow));

        assertTrue(DenNullUpgradeData.EMPTY.captureAllows(cow));
        assertTrue(whitelist.captureAllows(cow));
        assertFalse(whitelist.captureAllows(sheep));
        assertFalse(blacklist.captureAllows(cow));
        assertTrue(blacklist.captureAllows(sheep));
    }

    @Test
    void tagTemplateRendersPlayerTypeCounterAndId() {
        DenNullEntry cow = new DenNullEntry(ResourceLocation.withDefaultNamespace("cow"), 1, "cow|adult", new CompoundTag(), "Cow", "standard data");

        assertEquals("Alex's Cow #007",
                DenNullTagTemplate.render("{player}'s {type} #{counter}", "Alex", cow, 7));
        assertEquals("minecraft:cow:Cow:003",
                DenNullTagTemplate.render("{id}:{entity}:{counter}", "Alex", cow, 3));
    }

    @Test
    void denAndSlotSwapPayloadsRoundTrip() {
        DenNullPayloads.SetSelectedPayload selected = new DenNullPayloads.SetSelectedPayload(7, 2);
        DenNullPayloads.ReleaseEntryPayload release = new DenNullPayloads.ReleaseEntryPayload(7, 1);
        DenNullPayloads.CycleHeldPayload cycle = new DenNullPayloads.CycleHeldPayload(3, true);
        DenNullPayloads.StatePayload state = new DenNullPayloads.StatePayload(7, new DenNullData(0, List.of(
                new DenNullEntry(ResourceLocation.withDefaultNamespace("fox"), 4, "fox|standard", new CompoundTag(), "Fox", "standard data")
        )));
        DenNullPayloads.OpenViewPayload view = new DenNullPayloads.OpenViewPayload(2);
        DenNullPayloads.SetDyePayload dye = new DenNullPayloads.SetDyePayload(11);
        DenNullPayloads.ToggleBabyPayload baby = new DenNullPayloads.ToggleBabyPayload(true);
        DenNullPayloads.ToggleCapturePayload capture = new DenNullPayloads.ToggleCapturePayload(true);
        DenNullPayloads.SetCaptureFilterModePayload mode = new DenNullPayloads.SetCaptureFilterModePayload(DenNullCaptureFilterMode.BLACKLIST.ordinal());
        DenNullPayloads.SetTagTemplatePayload template = new DenNullPayloads.SetTagTemplatePayload("{player}-{counter}");
        DenNullPayloads.AddCaptureFilterPayload filter = new DenNullPayloads.AddCaptureFilterPayload("minecraft:cow");
        DeepNullPayloads.MenuSlotSwapPayload swap = new DeepNullPayloads.MenuSlotSwapPayload(1, 5, NullSlotDomain.FLUID_STORAGE.ordinal());
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DenNullPayloads.SetSelectedPayload.STREAM_CODEC.encode(buffer, selected);
        DenNullPayloads.ReleaseEntryPayload.STREAM_CODEC.encode(buffer, release);
        DenNullPayloads.CycleHeldPayload.STREAM_CODEC.encode(buffer, cycle);
        DenNullPayloads.StatePayload.STREAM_CODEC.encode(buffer, state);
        DenNullPayloads.OpenViewPayload.STREAM_CODEC.encode(buffer, view);
        DenNullPayloads.SetDyePayload.STREAM_CODEC.encode(buffer, dye);
        DenNullPayloads.ToggleBabyPayload.STREAM_CODEC.encode(buffer, baby);
        DenNullPayloads.ToggleCapturePayload.STREAM_CODEC.encode(buffer, capture);
        DenNullPayloads.SetCaptureFilterModePayload.STREAM_CODEC.encode(buffer, mode);
        DenNullPayloads.SetTagTemplatePayload.STREAM_CODEC.encode(buffer, template);
        DenNullPayloads.AddCaptureFilterPayload.STREAM_CODEC.encode(buffer, filter);
        DeepNullPayloads.MenuSlotSwapPayload.STREAM_CODEC.encode(buffer, swap);

        assertEquals(selected, DenNullPayloads.SetSelectedPayload.STREAM_CODEC.decode(buffer));
        assertEquals(release, DenNullPayloads.ReleaseEntryPayload.STREAM_CODEC.decode(buffer));
        assertEquals(cycle, DenNullPayloads.CycleHeldPayload.STREAM_CODEC.decode(buffer));
        DenNullPayloads.StatePayload decodedState = DenNullPayloads.StatePayload.STREAM_CODEC.decode(buffer);
        assertEquals(state.containerId(), decodedState.containerId());
        assertEquals(4, decodedState.data().entries().get(0).count());
        assertEquals(view, DenNullPayloads.OpenViewPayload.STREAM_CODEC.decode(buffer));
        assertEquals(dye, DenNullPayloads.SetDyePayload.STREAM_CODEC.decode(buffer));
        assertEquals(baby, DenNullPayloads.ToggleBabyPayload.STREAM_CODEC.decode(buffer));
        assertEquals(capture, DenNullPayloads.ToggleCapturePayload.STREAM_CODEC.decode(buffer));
        assertEquals(mode, DenNullPayloads.SetCaptureFilterModePayload.STREAM_CODEC.decode(buffer));
        assertEquals(template, DenNullPayloads.SetTagTemplatePayload.STREAM_CODEC.decode(buffer));
        assertEquals(filter, DenNullPayloads.AddCaptureFilterPayload.STREAM_CODEC.decode(buffer));
        assertEquals(swap, DeepNullPayloads.MenuSlotSwapPayload.STREAM_CODEC.decode(buffer));
    }
}
