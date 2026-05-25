package dev.deepdaddyttv.deepnullreforged.dripnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.EnumSet;
import java.util.Set;

public record DripUpgradeData(Set<DripNullUpgradeType> installed) {
    public static final DripUpgradeData EMPTY = new DripUpgradeData(Set.of());
    private static final String INSTALLED_TAG = "Installed";
    private static final String ID_TAG = "Id";

    public DripUpgradeData {
        installed = Set.copyOf(installed == null ? Set.of() : installed);
    }

    public boolean has(DripNullUpgradeType type) {
        return installed.contains(type);
    }

    public DripUpgradeData withInstalled(DripNullUpgradeType type) {
        EnumSet<DripNullUpgradeType> next = installed.isEmpty()
                ? EnumSet.noneOf(DripNullUpgradeType.class)
                : EnumSet.copyOf(installed);
        next.add(type);
        return new DripUpgradeData(next);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (DripNullUpgradeType type : installed) {
            CompoundTag entry = new CompoundTag();
            entry.putString(ID_TAG, type.itemId());
            list.add(entry);
        }
        tag.put(INSTALLED_TAG, list);
        return tag;
    }

    public static DripUpgradeData load(CompoundTag tag) {
        EnumSet<DripNullUpgradeType> installed = EnumSet.noneOf(DripNullUpgradeType.class);
        ListTag list = tag.getList(INSTALLED_TAG, net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            DripNullUpgradeType type = DripNullUpgradeType.byItemId(list.getCompound(i).getString(ID_TAG));
            if (type != null) {
                installed.add(type);
            }
        }
        return new DripUpgradeData(installed);
    }

    public static void write(RegistryFriendlyByteBuf buffer, DripUpgradeData data) {
        DripUpgradeData value = data == null ? EMPTY : data;
        buffer.writeVarInt(value.installed.size());
        for (DripNullUpgradeType type : value.installed) {
            buffer.writeVarInt(type.ordinal());
        }
    }

    public static DripUpgradeData read(RegistryFriendlyByteBuf buffer) {
        int count = Math.max(0, Math.min(DripNullUpgradeType.values().length, buffer.readVarInt()));
        EnumSet<DripNullUpgradeType> installed = EnumSet.noneOf(DripNullUpgradeType.class);
        for (int i = 0; i < count; i++) {
            int id = buffer.readVarInt();
            if (id >= 0 && id < DripNullUpgradeType.values().length) {
                installed.add(DripNullUpgradeType.values()[id]);
            }
        }
        return new DripUpgradeData(installed);
    }
}
