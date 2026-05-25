package dev.deepdaddyttv.deepnullreforged.nullseed;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NullSeedPresetSavedData extends SavedData {
    private static final String DATA_NAME = DeepNullReforged.MODID + "_null_seed_presets";
    private static final String PRESETS_TAG = "Presets";

    private final Map<String, NullSeedPreset> presets = new LinkedHashMap<>();

    public static NullSeedPresetSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        NullSeedPresetSavedData::new,
                        NullSeedPresetSavedData::load,
                        DataFixTypes.LEVEL
                ),
                DATA_NAME
        );
    }

    private static NullSeedPresetSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        NullSeedPresetSavedData data = new NullSeedPresetSavedData();
        if (tag.contains(PRESETS_TAG, Tag.TAG_LIST)) {
            ListTag list = tag.getList(PRESETS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                NullSeedPreset preset = NullSeedPreset.load(list.getCompound(i));
                if (!preset.presetId().isBlank()) {
                    data.presets.put(preset.presetId(), preset);
                }
            }
        }
        return data;
    }

    public List<NullSeedPreset> presetsFor(NullSeedKind targetKind) {
        return presets.values().stream()
                .filter(preset -> preset.supports(targetKind))
                .sorted(Comparator.comparing(NullSeedPreset::displayName).thenComparing(NullSeedPreset::presetId))
                .toList();
    }

    public NullSeedPreset find(String presetId) {
        return presets.get(presetId);
    }

    public void savePreset(NullSeedPreset preset) {
        if (preset == null || preset.presetId().isBlank() || preset.entries().isEmpty()) {
            return;
        }
        presets.put(preset.presetId(), preset.asUserPreset());
        setDirty();
    }

    public boolean deletePreset(String presetId) {
        if (presets.remove(presetId) != null) {
            setDirty();
            return true;
        }
        return false;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        presets.values().stream()
                .sorted(Comparator.comparing(NullSeedPreset::displayName).thenComparing(NullSeedPreset::presetId))
                .forEach(preset -> list.add(preset.save()));
        tag.put(PRESETS_TAG, list);
        return tag;
    }
}
