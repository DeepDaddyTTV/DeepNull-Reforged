package dev.deepdaddyttv.deepnullreforged.dennull;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record DenNullData(int selectedIndex, List<DenNullEntry> entries, List<DenNullSpawnerEntry> spawners, List<DenNullTemplate> templates, DenNullUpgradeData upgrades) {
    public static final DenNullData EMPTY = new DenNullData(0, List.of(), List.of(), List.of(), DenNullUpgradeData.EMPTY);
    private static final String ROOT_TAG = "DenNull";
    private static final String SELECTED_TAG = "Selected";
    private static final String ENTRIES_TAG = "Entries";
    private static final String SPAWNERS_TAG = "Spawners";
    private static final String TEMPLATES_TAG = "Templates";
    private static final String UPGRADES_TAG = "Upgrades";

    public DenNullData(int selectedIndex, List<DenNullEntry> entries) {
        this(selectedIndex, entries, List.of(), List.of(), DenNullUpgradeData.EMPTY);
    }

    public DenNullData(int selectedIndex, List<DenNullEntry> entries, List<DenNullTemplate> templates) {
        this(selectedIndex, entries, List.of(), templates, DenNullUpgradeData.EMPTY);
    }

    public DenNullData(int selectedIndex, List<DenNullEntry> entries, List<DenNullTemplate> templates, DenNullUpgradeData upgrades) {
        this(selectedIndex, entries, List.of(), templates, upgrades);
    }

    public DenNullData {
        entries = normalizeEntries(entries);
        spawners = normalizeSpawners(spawners);
        templates = List.copyOf(templates == null
                ? List.of()
                : templates.stream()
                .filter(template -> template != null)
                .sorted(Comparator.comparingInt(DenNullTemplate::targetIndex))
                .distinct()
                .toList());
        upgrades = upgrades == null ? DenNullUpgradeData.EMPTY : upgrades;
        selectedIndex = clampSelected(selectedIndex, activeSize(entries, spawners));
    }

    public static DenNullData get(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return EMPTY;
        }
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return EMPTY;
        }
        return load(tag.getCompound(ROOT_TAG));
    }

    public static void set(ItemStack stack, DenNullData data) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(ROOT_TAG, (data == null ? EMPTY : data).save()));
    }

    public static DenNullData load(CompoundTag root) {
        List<DenNullEntry> entries = new ArrayList<>();
        if (root.contains(ENTRIES_TAG, Tag.TAG_LIST)) {
            ListTag list = root.getList(ENTRIES_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                entries.add(DenNullEntry.load(list.getCompound(i)));
            }
        }
        List<DenNullSpawnerEntry> spawners = new ArrayList<>();
        if (root.contains(SPAWNERS_TAG, Tag.TAG_LIST)) {
            ListTag list = root.getList(SPAWNERS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                spawners.add(DenNullSpawnerEntry.load(list.getCompound(i)));
            }
        }
        List<DenNullTemplate> templates = new ArrayList<>();
        if (root.contains(TEMPLATES_TAG, Tag.TAG_LIST)) {
            ListTag list = root.getList(TEMPLATES_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                templates.add(DenNullTemplate.load(list.getCompound(i)));
            }
        }
        DenNullUpgradeData upgrades = root.contains(UPGRADES_TAG, Tag.TAG_COMPOUND)
                ? DenNullUpgradeData.load(root.getCompound(UPGRADES_TAG))
                : DenNullUpgradeData.EMPTY;
        return new DenNullData(root.getInt(SELECTED_TAG), entries, spawners, templates, upgrades);
    }

    public CompoundTag save() {
        CompoundTag root = new CompoundTag();
        root.putInt(SELECTED_TAG, selectedIndex);
        ListTag list = new ListTag();
        for (DenNullEntry entry : entries) {
            list.add(entry.save());
        }
        root.put(ENTRIES_TAG, list);
        ListTag spawnerList = new ListTag();
        for (DenNullSpawnerEntry entry : spawners) {
            spawnerList.add(entry.save());
        }
        root.put(SPAWNERS_TAG, spawnerList);
        ListTag templateList = new ListTag();
        for (DenNullTemplate template : templates) {
            templateList.add(template.save());
        }
        root.put(TEMPLATES_TAG, templateList);
        root.put(UPGRADES_TAG, upgrades.save());
        return root;
    }

    public static void write(RegistryFriendlyByteBuf buffer, DenNullData data) {
        DenNullData value = data == null ? EMPTY : data;
        buffer.writeVarInt(value.selectedIndex);
        buffer.writeVarInt(value.entries.size());
        for (DenNullEntry entry : value.entries) {
            DenNullEntry.write(buffer, entry);
        }
        buffer.writeVarInt(value.spawners.size());
        for (DenNullSpawnerEntry entry : value.spawners) {
            DenNullSpawnerEntry.write(buffer, entry);
        }
        buffer.writeVarInt(value.templates.size());
        for (DenNullTemplate template : value.templates) {
            DenNullTemplate.write(buffer, template);
        }
        DenNullUpgradeData.write(buffer, value.upgrades);
    }

    public static DenNullData read(RegistryFriendlyByteBuf buffer) {
        int selected = buffer.readVarInt();
        int count = Math.max(0, Math.min(512, buffer.readVarInt()));
        List<DenNullEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(DenNullEntry.read(buffer));
        }
        int spawnerCount = Math.max(0, Math.min(512, buffer.readVarInt()));
        List<DenNullSpawnerEntry> spawners = new ArrayList<>(spawnerCount);
        for (int i = 0; i < spawnerCount; i++) {
            spawners.add(DenNullSpawnerEntry.read(buffer));
        }
        int templateCount = Math.max(0, Math.min(512, buffer.readVarInt()));
        List<DenNullTemplate> templates = new ArrayList<>(templateCount);
        for (int i = 0; i < templateCount; i++) {
            templates.add(DenNullTemplate.read(buffer));
        }
        return new DenNullData(selected, entries, spawners, templates, DenNullUpgradeData.read(buffer));
    }

    public DenNullData withSelectedIndex(int selectedIndex) {
        return new DenNullData(selectedIndex, entries, spawners, templates, upgrades);
    }

    public DenNullData cycleSelected(boolean forward) {
        int size = activeSize(entries, spawners);
        if (size <= 0) {
            return this;
        }
        int next = selectedIndex + (forward ? 1 : -1);
        if (next < 0) {
            next = size - 1;
        } else if (next >= size) {
            next = 0;
        }
        return withSelectedIndex(next);
    }

    public DenNullData withTemplates(List<DenNullTemplate> templates) {
        return new DenNullData(selectedIndex, entries, spawners, templates, upgrades);
    }

    public DenNullData withUpgrades(DenNullUpgradeData upgrades) {
        return new DenNullData(selectedIndex, entries, spawners, templates, upgrades);
    }

    public DenNullData withEntries(List<DenNullEntry> entries) {
        return new DenNullData(selectedIndex, entries, spawners, templates, upgrades);
    }

    public DenNullData withSpawnerEntries(List<DenNullSpawnerEntry> spawners) {
        return new DenNullData(selectedIndex, entries, spawners, templates, upgrades);
    }

    public DenNullTemplate templateAt(int targetIndex) {
        for (DenNullTemplate template : templates) {
            if (template.targetIndex() == targetIndex) {
                return template;
            }
        }
        return null;
    }

    public DenNullEntry selectedEntry() {
        return selectedIndex >= 0 && selectedIndex < entries.size() ? entries.get(selectedIndex) : null;
    }

    public DenNullSpawnerEntry selectedSpawnerEntry() {
        return selectedIndex >= 0 && selectedIndex < spawners.size() ? spawners.get(selectedIndex) : null;
    }

    public int totalCount() {
        int total = 0;
        for (DenNullEntry entry : entries) {
            total += entry.count();
        }
        for (DenNullSpawnerEntry entry : spawners) {
            total += entry.count();
        }
        return total;
    }

    public AddResult addCapture(DenNullEntry capture, DeepNullTier tier) {
        if (!spawners.isEmpty()) {
            return new AddResult(false, this, AddFailure.MIXED_MODE);
        }
        if (capture == null || capture.count() <= 0) {
            return new AddResult(false, this, AddFailure.UNSUPPORTED);
        }
        int maxPerEntry = tier.creative() ? Integer.MAX_VALUE : tier.perSlotCapacity();
        List<DenNullEntry> updated = new ArrayList<>(entries);
        for (int i = 0; i < updated.size(); i++) {
            DenNullEntry existing = updated.get(i);
            if (!existing.identityKey().equals(capture.identityKey())) {
                continue;
            }
            if (existing.count() >= maxPerEntry) {
                return new AddResult(false, this, AddFailure.FULL_STACK);
            }
            updated.set(i, existing.withCount(Math.min(maxPerEntry, existing.count() + capture.count())));
            return new AddResult(true, new DenNullData(i, updated, spawners, templates, upgrades), AddFailure.NONE);
        }

        if (!tier.creative() && updated.size() >= tier.slotCount()) {
            return new AddResult(false, this, AddFailure.FULL);
        }
        updated.add(capture.withCount(Math.min(maxPerEntry, capture.count())));
        return new AddResult(true, new DenNullData(updated.size() - 1, updated, spawners, templates, upgrades), AddFailure.NONE);
    }

    public AddResult addSpawner(DenNullSpawnerEntry spawner, DeepNullTier tier) {
        if (!entries.isEmpty()) {
            return new AddResult(false, this, AddFailure.MIXED_MODE);
        }
        if (spawner == null || spawner.count() <= 0) {
            return new AddResult(false, this, AddFailure.UNSUPPORTED);
        }
        int maxPerEntry = tier.creative() ? Integer.MAX_VALUE : tier.perSlotCapacity();
        List<DenNullSpawnerEntry> updated = new ArrayList<>(spawners);
        for (int i = 0; i < updated.size(); i++) {
            DenNullSpawnerEntry existing = updated.get(i);
            if (!existing.identityKey().equals(spawner.identityKey())) {
                continue;
            }
            if (existing.count() >= maxPerEntry) {
                return new AddResult(false, this, AddFailure.FULL_STACK);
            }
            updated.set(i, existing.withCount(Math.min(maxPerEntry, existing.count() + spawner.count())));
            return new AddResult(true, new DenNullData(i, entries, updated, templates, upgrades), AddFailure.NONE);
        }

        if (!tier.creative() && updated.size() >= tier.slotCount()) {
            return new AddResult(false, this, AddFailure.FULL);
        }
        updated.add(spawner.withCount(Math.min(maxPerEntry, spawner.count())));
        return new AddResult(true, new DenNullData(updated.size() - 1, entries, updated, templates, upgrades), AddFailure.NONE);
    }

    public ReleaseResult releaseSelected() {
        return releaseAtIndex(selectedIndex);
    }

    public ReleaseResult releaseAtIndex(int entryIndex) {
        DenNullEntry selected = entryIndex >= 0 && entryIndex < entries.size() ? entries.get(entryIndex) : null;
        if (selected == null) {
            return new ReleaseResult(false, this, null);
        }
        List<DenNullEntry> updated = new ArrayList<>(entries);
        if (selected.count() <= 1) {
            updated.remove(entryIndex);
        } else {
            updated.set(entryIndex, selected.withCount(selected.count() - 1));
        }
        int nextSelected = selectedIndex;
        if (entryIndex < selectedIndex) {
            nextSelected = selectedIndex - 1;
        }
        return new ReleaseResult(true, new DenNullData(nextSelected, updated, spawners, templates, upgrades), selected.withCount(1));
    }

    private static int clampSelected(int selected, int size) {
        if (size <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(selected, size - 1));
    }

    private static List<DenNullEntry> normalizeEntries(List<DenNullEntry> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        Map<String, DenNullEntry> merged = new LinkedHashMap<>();
        for (DenNullEntry rawEntry : source) {
            if (rawEntry == null || rawEntry.count() <= 0) {
                continue;
            }
            DenNullEntry entry = DenNullCaptureNormalizer.normalizeEntry(rawEntry);
            if (entry == null || entry.count() <= 0) {
                continue;
            }
            DenNullEntry existing = merged.get(entry.identityKey());
            if (existing == null) {
                merged.put(entry.identityKey(), entry);
            } else {
                merged.put(entry.identityKey(), existing.withCount(existing.count() + entry.count()));
            }
        }
        return List.copyOf(merged.values());
    }

    private static List<DenNullSpawnerEntry> normalizeSpawners(List<DenNullSpawnerEntry> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        Map<String, DenNullSpawnerEntry> merged = new LinkedHashMap<>();
        for (DenNullSpawnerEntry entry : source) {
            if (entry == null || entry.count() <= 0) {
                continue;
            }
            DenNullSpawnerEntry existing = merged.get(entry.identityKey());
            if (existing == null) {
                merged.put(entry.identityKey(), entry);
            } else {
                merged.put(entry.identityKey(), existing.withCount(existing.count() + entry.count()));
            }
        }
        return List.copyOf(merged.values());
    }

    private static int activeSize(List<DenNullEntry> entries, List<DenNullSpawnerEntry> spawners) {
        return spawners != null && !spawners.isEmpty() ? spawners.size() : entries.size();
    }

    public enum AddFailure {
        NONE,
        FULL,
        FULL_STACK,
        MIXED_MODE,
        UNSUPPORTED
    }

    public record AddResult(boolean success, DenNullData data, AddFailure failure) {
    }

    public record ReleaseResult(boolean success, DenNullData data, DenNullEntry entry) {
    }
}
