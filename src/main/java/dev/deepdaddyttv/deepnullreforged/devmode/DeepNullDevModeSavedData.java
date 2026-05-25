package dev.deepdaddyttv.deepnullreforged.devmode;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public final class DeepNullDevModeSavedData extends SavedData {
    private static final String DATA_NAME = DeepNullReforged.MODID + "_dev_mode";
    private static final String ENABLED_TAG = "Enabled";

    private boolean enabled;

    public static DeepNullDevModeSavedData get(ServerLevel level) {
        ServerLevel storageLevel = level.getServer().overworld();
        return storageLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        DeepNullDevModeSavedData::new,
                        DeepNullDevModeSavedData::load,
                        DataFixTypes.LEVEL
                ),
                DATA_NAME
        );
    }

    private static DeepNullDevModeSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        DeepNullDevModeSavedData data = new DeepNullDevModeSavedData();
        data.enabled = tag.getBoolean(ENABLED_TAG);
        return data;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return false;
        }
        this.enabled = enabled;
        setDirty();
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean(ENABLED_TAG, enabled);
        return tag;
    }
}
