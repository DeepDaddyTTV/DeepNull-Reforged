package dev.deepdaddyttv.deepnullreforged.hubnull;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

public record HubNullData(List<HubNullStationRef> stations) {
    public static final HubNullData EMPTY = new HubNullData(List.of());
    private static final String ROOT_TAG = "HubNull";
    private static final String STATIONS_TAG = "Stations";

    public HubNullData {
        List<HubNullStationRef> deduped = new ArrayList<>();
        for (HubNullStationRef station : stations == null ? List.<HubNullStationRef>of() : stations) {
            if (station != null && deduped.stream().noneMatch(existing -> existing.sameStation(station))) {
                deduped.add(station);
            }
        }
        stations = List.copyOf(deduped);
    }

    public static HubNullData get(ItemStack stack) {
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

    public static void set(ItemStack stack, HubNullData data) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(ROOT_TAG, (data == null ? EMPTY : data).save()));
    }

    public static HubNullData load(CompoundTag root) {
        List<HubNullStationRef> stations = new ArrayList<>();
        if (root.contains(STATIONS_TAG, Tag.TAG_LIST)) {
            ListTag list = root.getList(STATIONS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                stations.add(HubNullStationRef.load(list.getCompound(i)));
            }
        }
        return new HubNullData(stations);
    }

    public CompoundTag save() {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        for (HubNullStationRef station : stations) {
            list.add(station.save());
        }
        root.put(STATIONS_TAG, list);
        return root;
    }

    public boolean contains(HubNullStationRef station) {
        return stations.stream().anyMatch(existing -> existing.sameStation(station));
    }

    public HubNullData withStation(HubNullStationRef station) {
        if (station == null) {
            return this;
        }
        List<HubNullStationRef> updated = new ArrayList<>();
        boolean replaced = false;
        for (HubNullStationRef existing : stations) {
            if (existing.sameStation(station)) {
                updated.add(station);
                replaced = true;
            } else {
                updated.add(existing);
            }
        }
        if (!replaced) {
            updated.add(station);
        }
        return new HubNullData(updated);
    }

    public HubNullData withoutStation(HubNullStationRef station) {
        if (station == null) {
            return this;
        }
        return new HubNullData(stations.stream().filter(existing -> !existing.sameStation(station)).toList());
    }

    public HubNullData reordered(HubNullStationRef station, int targetIndex) {
        if (station == null || stations.isEmpty()) {
            return this;
        }
        List<HubNullStationRef> updated = new ArrayList<>(stations.size());
        HubNullStationRef moved = null;
        for (HubNullStationRef existing : stations) {
            if (existing.sameStation(station)) {
                moved = existing;
            } else {
                updated.add(existing);
            }
        }
        if (moved == null) {
            return this;
        }
        int clampedTarget = Math.max(0, Math.min(targetIndex, updated.size()));
        updated.add(clampedTarget, moved);
        return new HubNullData(updated);
    }
}
