package dev.deepdaddyttv.deepnullreforged.dennull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record DenNullUpgradeData(
        Set<DenNullUpgradeType> installed,
        List<DenNullBufferEntry> breedingItems,
        int milkBuckets,
        int nameTags,
        int dyeColorId,
        boolean babyEnabled,
        String tagTemplate,
        int tagCounter,
        boolean captureEnabled,
        DenNullCaptureFilterMode captureFilterMode,
        List<ResourceLocation> captureFilter,
        List<DenNullBufferEntry> lootItems,
        List<Integer> farmThresholds,
        long lastBreedingTick,
        long lastMilkTick,
        long lastShearTick,
        long lastCaptureTick,
        long lastFarmTick
) {
    public static final int BREEDING_BUFFER_SLOTS = 9;
    public static final int LOOT_BUFFER_SLOTS = 9;
    public static final int MILK_BUCKET_CAPACITY = 32;
    public static final int DEFAULT_FARM_THRESHOLD = 8;
    public static final String DEFAULT_TAG_TEMPLATE = "{player}'s {type} #{counter}";
    public static final DenNullUpgradeData EMPTY = new DenNullUpgradeData(
            Set.of(),
            List.of(),
            0,
            0,
            -1,
            false,
            DEFAULT_TAG_TEMPLATE,
            1,
            false,
            DenNullCaptureFilterMode.OFF,
            List.of(),
            List.of(),
            List.of(),
            0L,
            0L,
            0L,
            0L,
            0L
    );

    private static final String INSTALLED_TAG = "Installed";
    private static final String BREEDING_TAG = "BreedingItems";
    private static final String MILK_TAG = "MilkBuckets";
    private static final String NAME_TAGS_TAG = "NameTags";
    private static final String DYE_TAG = "DyeColor";
    private static final String BABY_TAG = "BabyEnabled";
    private static final String TAG_TEMPLATE_TAG = "TagTemplate";
    private static final String TAG_COUNTER_TAG = "TagCounter";
    private static final String CAPTURE_ENABLED_TAG = "CaptureEnabled";
    private static final String CAPTURE_FILTER_MODE_TAG = "CaptureFilterMode";
    private static final String CAPTURE_FILTER_TAG = "CaptureFilter";
    private static final String LOOT_TAG = "LootItems";
    private static final String FARM_THRESHOLDS_TAG = "FarmThresholds";
    private static final String LAST_BREEDING_TAG = "LastBreedingTick";
    private static final String LAST_MILK_TAG = "LastMilkTick";
    private static final String LAST_SHEAR_TAG = "LastShearTick";
    private static final String LAST_CAPTURE_TAG = "LastCaptureTick";
    private static final String LAST_FARM_TAG = "LastFarmTick";
    private static final String ID_TAG = "Id";
    private static final String SLOT_TAG = "Slot";
    private static final String VALUE_TAG = "Value";

    public DenNullUpgradeData {
        installed = Set.copyOf(installed == null ? Set.of() : installed);
        breedingItems = normalizeBuffer(breedingItems, BREEDING_BUFFER_SLOTS);
        lootItems = normalizeBuffer(lootItems, LOOT_BUFFER_SLOTS);
        farmThresholds = normalizeThresholds(farmThresholds);
        milkBuckets = Math.max(0, Math.min(MILK_BUCKET_CAPACITY, milkBuckets));
        nameTags = Math.max(0, nameTags);
        dyeColorId = dyeColorId < -1 ? -1 : Math.min(15, dyeColorId);
        tagTemplate = tagTemplate == null || tagTemplate.isBlank() ? DEFAULT_TAG_TEMPLATE : tagTemplate;
        tagCounter = Math.max(1, tagCounter);
        captureFilterMode = captureFilterMode == null ? DenNullCaptureFilterMode.OFF : captureFilterMode;
        captureFilter = List.copyOf(captureFilter == null
                ? List.of()
                : new LinkedHashSet<>(captureFilter).stream().filter(id -> id != null).toList());
    }

    public boolean has(DenNullUpgradeType type) {
        return installed.contains(type);
    }

    public DenNullUpgradeData withInstalled(DenNullUpgradeType type) {
        EnumSet<DenNullUpgradeType> next = installed.isEmpty()
                ? EnumSet.noneOf(DenNullUpgradeType.class)
                : EnumSet.copyOf(installed);
        next.add(type);
        return new DenNullUpgradeData(next, breedingItems, milkBuckets, nameTags, dyeColorId, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, captureFilterMode, captureFilter, lootItems, farmThresholds, lastBreedingTick,
                lastMilkTick, lastShearTick, lastCaptureTick, lastFarmTick);
    }

    public DenNullUpgradeData withBreedingItems(List<DenNullBufferEntry> breedingItems) {
        return new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags, dyeColorId, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, captureFilterMode, captureFilter, lootItems, farmThresholds, lastBreedingTick,
                lastMilkTick, lastShearTick, lastCaptureTick, lastFarmTick);
    }

    public DenNullUpgradeData withLootItems(List<DenNullBufferEntry> lootItems) {
        return new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags, dyeColorId, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, captureFilterMode, captureFilter, lootItems, farmThresholds, lastBreedingTick,
                lastMilkTick, lastShearTick, lastCaptureTick, lastFarmTick);
    }

    public DenNullUpgradeData withFarmThreshold(int entryIndex, int threshold) {
        if (entryIndex < 0) {
            return this;
        }
        List<Integer> next = new ArrayList<>(farmThresholds);
        while (next.size() <= entryIndex && next.size() < 512) {
            next.add(DEFAULT_FARM_THRESHOLD);
        }
        if (entryIndex < next.size()) {
            next.set(entryIndex, Math.max(0, threshold));
        }
        return new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags, dyeColorId, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, captureFilterMode, captureFilter, lootItems, next, lastBreedingTick,
                lastMilkTick, lastShearTick, lastCaptureTick, lastFarmTick);
    }

    public int farmThreshold(int entryIndex) {
        if (entryIndex < 0 || entryIndex >= farmThresholds.size()) {
            return DEFAULT_FARM_THRESHOLD;
        }
        return Math.max(0, farmThresholds.get(entryIndex));
    }

    public DenNullUpgradeData withMilkBuckets(int milkBuckets) {
        return new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags, dyeColorId, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, captureFilterMode, captureFilter, lootItems, farmThresholds, lastBreedingTick,
                lastMilkTick, lastShearTick, lastCaptureTick, lastFarmTick);
    }

    public DenNullUpgradeData withNameTags(int nameTags) {
        return new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags, dyeColorId, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, captureFilterMode, captureFilter, lootItems, farmThresholds, lastBreedingTick,
                lastMilkTick, lastShearTick, lastCaptureTick, lastFarmTick);
    }

    public DenNullUpgradeData withDyeColorId(int dyeColorId) {
        return new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags, dyeColorId, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, captureFilterMode, captureFilter, lootItems, farmThresholds, lastBreedingTick,
                lastMilkTick, lastShearTick, lastCaptureTick, lastFarmTick);
    }

    public DenNullUpgradeData withBabyEnabled(boolean babyEnabled) {
        return new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags, dyeColorId, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, captureFilterMode, captureFilter, lootItems, farmThresholds, lastBreedingTick,
                lastMilkTick, lastShearTick, lastCaptureTick, lastFarmTick);
    }

    public DenNullUpgradeData withTagTemplate(String tagTemplate) {
        return new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags, dyeColorId, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, captureFilterMode, captureFilter, lootItems, farmThresholds, lastBreedingTick,
                lastMilkTick, lastShearTick, lastCaptureTick, lastFarmTick);
    }

    public DenNullUpgradeData withTagCounter(int tagCounter) {
        return new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags, dyeColorId, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, captureFilterMode, captureFilter, lootItems, farmThresholds, lastBreedingTick,
                lastMilkTick, lastShearTick, lastCaptureTick, lastFarmTick);
    }

    public DenNullUpgradeData withCaptureEnabled(boolean captureEnabled) {
        return new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags, dyeColorId, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, captureFilterMode, captureFilter, lootItems, farmThresholds, lastBreedingTick,
                lastMilkTick, lastShearTick, lastCaptureTick, lastFarmTick);
    }

    public DenNullUpgradeData withCaptureFilter(DenNullCaptureFilterMode mode, List<ResourceLocation> filter) {
        return new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags, dyeColorId, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, mode, filter, lootItems, farmThresholds, lastBreedingTick, lastMilkTick,
                lastShearTick, lastCaptureTick, lastFarmTick);
    }

    public DenNullUpgradeData withAutomationTicks(long breeding, long milk, long shear, long capture) {
        return withAutomationTicks(breeding, milk, shear, capture, lastFarmTick);
    }

    public DenNullUpgradeData withAutomationTicks(long breeding, long milk, long shear, long capture, long farm) {
        return new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags, dyeColorId, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, captureFilterMode, captureFilter, lootItems, farmThresholds, breeding, milk,
                shear, capture, farm);
    }

    public boolean captureAllows(ResourceLocation entityId) {
        if (entityId == null || captureFilterMode == DenNullCaptureFilterMode.OFF) {
            return true;
        }
        boolean listed = captureFilter.contains(entityId);
        return captureFilterMode == DenNullCaptureFilterMode.WHITELIST ? listed : !listed;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag installedList = new ListTag();
        for (DenNullUpgradeType type : installed) {
            CompoundTag entry = new CompoundTag();
            entry.putString(ID_TAG, type.itemId());
            installedList.add(entry);
        }
        tag.put(INSTALLED_TAG, installedList);
        ListTag breedingList = new ListTag();
        for (DenNullBufferEntry entry : breedingItems) {
            breedingList.add(entry.save());
        }
        tag.put(BREEDING_TAG, breedingList);
        tag.putInt(MILK_TAG, milkBuckets);
        tag.putInt(NAME_TAGS_TAG, nameTags);
        tag.putInt(DYE_TAG, dyeColorId);
        tag.putBoolean(BABY_TAG, babyEnabled);
        tag.putString(TAG_TEMPLATE_TAG, tagTemplate);
        tag.putInt(TAG_COUNTER_TAG, tagCounter);
        tag.putBoolean(CAPTURE_ENABLED_TAG, captureEnabled);
        tag.putInt(CAPTURE_FILTER_MODE_TAG, captureFilterMode.ordinal());
        ListTag filterList = new ListTag();
        for (ResourceLocation id : captureFilter) {
            CompoundTag entry = new CompoundTag();
            entry.putString(ID_TAG, id.toString());
            filterList.add(entry);
        }
        tag.put(CAPTURE_FILTER_TAG, filterList);
        ListTag lootList = new ListTag();
        for (DenNullBufferEntry entry : lootItems) {
            lootList.add(entry.save());
        }
        tag.put(LOOT_TAG, lootList);
        ListTag thresholdList = new ListTag();
        for (int slot = 0; slot < farmThresholds.size(); slot++) {
            CompoundTag entry = new CompoundTag();
            entry.putInt(SLOT_TAG, slot);
            entry.putInt(VALUE_TAG, farmThresholds.get(slot));
            thresholdList.add(entry);
        }
        tag.put(FARM_THRESHOLDS_TAG, thresholdList);
        tag.putLong(LAST_BREEDING_TAG, lastBreedingTick);
        tag.putLong(LAST_MILK_TAG, lastMilkTick);
        tag.putLong(LAST_SHEAR_TAG, lastShearTick);
        tag.putLong(LAST_CAPTURE_TAG, lastCaptureTick);
        tag.putLong(LAST_FARM_TAG, lastFarmTick);
        return tag;
    }

    public static DenNullUpgradeData load(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return EMPTY;
        }
        EnumSet<DenNullUpgradeType> installed = EnumSet.noneOf(DenNullUpgradeType.class);
        if (tag.contains(INSTALLED_TAG, Tag.TAG_LIST)) {
            ListTag list = tag.getList(INSTALLED_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                DenNullUpgradeType type = DenNullUpgradeType.byItemId(list.getCompound(i).getString(ID_TAG));
                if (type != null) {
                    installed.add(type);
                }
            }
        }
        List<DenNullBufferEntry> breedingItems = new ArrayList<>();
        if (tag.contains(BREEDING_TAG, Tag.TAG_LIST)) {
            ListTag list = tag.getList(BREEDING_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                breedingItems.add(DenNullBufferEntry.load(list.getCompound(i)));
            }
        }
        List<ResourceLocation> filter = new ArrayList<>();
        if (tag.contains(CAPTURE_FILTER_TAG, Tag.TAG_LIST)) {
            ListTag list = tag.getList(CAPTURE_FILTER_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                ResourceLocation id = ResourceLocation.tryParse(list.getCompound(i).getString(ID_TAG));
                if (id != null) {
                    filter.add(id);
                }
            }
        }
        List<DenNullBufferEntry> lootItems = new ArrayList<>();
        if (tag.contains(LOOT_TAG, Tag.TAG_LIST)) {
            ListTag list = tag.getList(LOOT_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                lootItems.add(DenNullBufferEntry.load(list.getCompound(i)));
            }
        }
        List<Integer> farmThresholds = new ArrayList<>();
        if (tag.contains(FARM_THRESHOLDS_TAG, Tag.TAG_LIST)) {
            ListTag list = tag.getList(FARM_THRESHOLDS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                int slot = entry.getInt(SLOT_TAG);
                if (slot < 0 || slot >= 512) {
                    continue;
                }
                while (farmThresholds.size() <= slot) {
                    farmThresholds.add(DEFAULT_FARM_THRESHOLD);
                }
                farmThresholds.set(slot, Math.max(0, entry.getInt(VALUE_TAG)));
            }
        }
        return new DenNullUpgradeData(
                installed,
                breedingItems,
                tag.getInt(MILK_TAG),
                tag.getInt(NAME_TAGS_TAG),
                tag.contains(DYE_TAG, Tag.TAG_INT) ? tag.getInt(DYE_TAG) : -1,
                tag.getBoolean(BABY_TAG),
                tag.getString(TAG_TEMPLATE_TAG),
                tag.contains(TAG_COUNTER_TAG, Tag.TAG_INT) ? tag.getInt(TAG_COUNTER_TAG) : 1,
                tag.getBoolean(CAPTURE_ENABLED_TAG),
                DenNullCaptureFilterMode.byId(tag.getInt(CAPTURE_FILTER_MODE_TAG)),
                filter,
                lootItems,
                farmThresholds,
                tag.getLong(LAST_BREEDING_TAG),
                tag.getLong(LAST_MILK_TAG),
                tag.getLong(LAST_SHEAR_TAG),
                tag.getLong(LAST_CAPTURE_TAG),
                tag.getLong(LAST_FARM_TAG)
        );
    }

    public static void write(RegistryFriendlyByteBuf buffer, DenNullUpgradeData data) {
        DenNullUpgradeData value = data == null ? EMPTY : data;
        buffer.writeVarInt(value.installed.size());
        for (DenNullUpgradeType type : value.installed) {
            buffer.writeVarInt(type.ordinal());
        }
        buffer.writeVarInt(value.breedingItems.size());
        for (DenNullBufferEntry entry : value.breedingItems) {
            DenNullBufferEntry.write(buffer, entry);
        }
        buffer.writeVarInt(value.milkBuckets);
        buffer.writeVarInt(value.nameTags);
        buffer.writeVarInt(value.dyeColorId + 1);
        buffer.writeBoolean(value.babyEnabled);
        buffer.writeUtf(value.tagTemplate);
        buffer.writeVarInt(value.tagCounter);
        buffer.writeBoolean(value.captureEnabled);
        buffer.writeVarInt(value.captureFilterMode.ordinal());
        buffer.writeVarInt(value.captureFilter.size());
        for (ResourceLocation id : value.captureFilter) {
            buffer.writeResourceLocation(id);
        }
        buffer.writeVarInt(value.lootItems.size());
        for (DenNullBufferEntry entry : value.lootItems) {
            DenNullBufferEntry.write(buffer, entry);
        }
        buffer.writeVarInt(value.farmThresholds.size());
        for (Integer threshold : value.farmThresholds) {
            buffer.writeVarInt(Math.max(0, threshold == null ? DEFAULT_FARM_THRESHOLD : threshold));
        }
        buffer.writeLong(value.lastBreedingTick);
        buffer.writeLong(value.lastMilkTick);
        buffer.writeLong(value.lastShearTick);
        buffer.writeLong(value.lastCaptureTick);
        buffer.writeLong(value.lastFarmTick);
    }

    public static DenNullUpgradeData read(RegistryFriendlyByteBuf buffer) {
        int installedCount = Math.max(0, Math.min(DenNullUpgradeType.values().length, buffer.readVarInt()));
        EnumSet<DenNullUpgradeType> installed = EnumSet.noneOf(DenNullUpgradeType.class);
        for (int i = 0; i < installedCount; i++) {
            int id = buffer.readVarInt();
            if (id >= 0 && id < DenNullUpgradeType.values().length) {
                installed.add(DenNullUpgradeType.values()[id]);
            }
        }
        int breedingCount = Math.max(0, Math.min(BREEDING_BUFFER_SLOTS, buffer.readVarInt()));
        List<DenNullBufferEntry> breedingItems = new ArrayList<>(breedingCount);
        for (int i = 0; i < breedingCount; i++) {
            breedingItems.add(DenNullBufferEntry.read(buffer));
        }
        int milkBuckets = buffer.readVarInt();
        int nameTags = buffer.readVarInt();
        int dyeColor = buffer.readVarInt() - 1;
        boolean babyEnabled = buffer.readBoolean();
        String tagTemplate = buffer.readUtf();
        int tagCounter = buffer.readVarInt();
        boolean captureEnabled = buffer.readBoolean();
        DenNullCaptureFilterMode mode = DenNullCaptureFilterMode.byId(buffer.readVarInt());
        int filterCount = Math.max(0, Math.min(128, buffer.readVarInt()));
        List<ResourceLocation> filter = new ArrayList<>(filterCount);
        for (int i = 0; i < filterCount; i++) {
            filter.add(buffer.readResourceLocation());
        }
        int lootCount = Math.max(0, Math.min(LOOT_BUFFER_SLOTS, buffer.readVarInt()));
        List<DenNullBufferEntry> lootItems = new ArrayList<>(lootCount);
        for (int i = 0; i < lootCount; i++) {
            lootItems.add(DenNullBufferEntry.read(buffer));
        }
        int thresholdCount = Math.max(0, Math.min(512, buffer.readVarInt()));
        List<Integer> farmThresholds = new ArrayList<>(thresholdCount);
        for (int i = 0; i < thresholdCount; i++) {
            farmThresholds.add(Math.max(0, buffer.readVarInt()));
        }
        return new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags, dyeColor, babyEnabled, tagTemplate,
                tagCounter, captureEnabled, mode, filter, lootItems, farmThresholds, buffer.readLong(), buffer.readLong(),
                buffer.readLong(), buffer.readLong(), buffer.readLong());
    }

    private static List<DenNullBufferEntry> normalizeBuffer(List<DenNullBufferEntry> entries, int maxSlots) {
        List<DenNullBufferEntry> normalized = new ArrayList<>();
        if (entries == null) {
            return List.of();
        }
        LinkedHashSet<ResourceLocation> seen = new LinkedHashSet<>();
        for (DenNullBufferEntry entry : entries) {
            if (entry == null || entry.isEmpty() || !seen.add(entry.itemId())) {
                continue;
            }
            normalized.add(entry);
            if (normalized.size() >= maxSlots) {
                break;
            }
        }
        return List.copyOf(normalized);
    }

    private static List<Integer> normalizeThresholds(List<Integer> thresholds) {
        if (thresholds == null || thresholds.isEmpty()) {
            return List.of();
        }
        List<Integer> normalized = new ArrayList<>();
        for (Integer threshold : thresholds) {
            normalized.add(Math.max(0, threshold == null ? DEFAULT_FARM_THRESHOLD : threshold));
            if (normalized.size() >= 512) {
                break;
            }
        }
        return List.copyOf(normalized);
    }
}
