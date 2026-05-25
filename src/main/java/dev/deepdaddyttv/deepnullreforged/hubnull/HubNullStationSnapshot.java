package dev.deepdaddyttv.deepnullreforged.hubnull;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record HubNullStationSnapshot(
        HubNullStationRef ref,
        HubNullStationStatus status,
        String tierName,
        int occupiedSlots,
        HubNullStationNullType nullType,
        String displayName,
        int accentColor,
        ItemStack previewStack
) {
    public HubNullStationSnapshot {
        status = status == null ? HubNullStationStatus.MISSING : status;
        tierName = tierName == null ? "" : tierName;
        nullType = nullType == null ? HubNullStationNullType.NONE : nullType;
        displayName = displayName == null || displayName.isBlank() ? ref.name() : displayName;
        accentColor = accentColor == 0 ? 0 : accentColor | 0xFF000000;
        previewStack = previewStack == null || previewStack.isEmpty() ? null : previewStack.copyWithCount(1);
    }

    public HubNullStationSnapshot(HubNullStationRef ref, HubNullStationStatus status, String tierName, int occupiedSlots) {
        this(ref, status, tierName, occupiedSlots, HubNullStationNullType.NONE);
    }

    public HubNullStationSnapshot(HubNullStationRef ref, HubNullStationStatus status, String tierName, int occupiedSlots, HubNullStationNullType nullType) {
        this(ref, status, tierName, occupiedSlots, nullType, ref.name(), 0, null);
    }

    public HubNullStationSnapshot(HubNullStationRef ref, HubNullStationStatus status, String tierName, int occupiedSlots, HubNullStationNullType nullType, String displayName, int accentColor) {
        this(ref, status, tierName, occupiedSlots, nullType, displayName, accentColor, null);
    }

    public static HubNullStationSnapshot of(HubNullStationRef ref, HubNullStationStatus status, DeepNullTier tier, int occupiedSlots) {
        return of(ref, status, tier, occupiedSlots, HubNullStationNullType.DEEP);
    }

    public static HubNullStationSnapshot of(HubNullStationRef ref, HubNullStationStatus status, DeepNullTier tier, int occupiedSlots, HubNullStationNullType nullType) {
        return of(ref, status, tier, occupiedSlots, nullType, ref.name(), 0);
    }

    public static HubNullStationSnapshot of(
            HubNullStationRef ref,
            HubNullStationStatus status,
            DeepNullTier tier,
            int occupiedSlots,
            HubNullStationNullType nullType,
            String displayName,
            int accentColor
    ) {
        return of(ref, status, tier, occupiedSlots, nullType, displayName, accentColor, null);
    }

    public static HubNullStationSnapshot of(
            HubNullStationRef ref,
            HubNullStationStatus status,
            DeepNullTier tier,
            int occupiedSlots,
            HubNullStationNullType nullType,
            String displayName,
            int accentColor,
            ItemStack previewStack
    ) {
        return new HubNullStationSnapshot(ref, status, tier == null ? "" : tier.name(), occupiedSlots, nullType, displayName, accentColor, previewStack);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        ref.write(buffer);
        buffer.writeVarInt(status.ordinal());
        buffer.writeUtf(tierName);
        buffer.writeVarInt(occupiedSlots);
        buffer.writeVarInt(nullType.ordinal());
        buffer.writeUtf(displayName);
        buffer.writeInt(accentColor);
        buffer.writeBoolean(previewStack != null && !previewStack.isEmpty());
        if (previewStack != null && !previewStack.isEmpty()) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, previewStack);
        }
    }

    public static HubNullStationSnapshot read(RegistryFriendlyByteBuf buffer) {
        HubNullStationRef ref = HubNullStationRef.read(buffer);
        HubNullStationStatus[] values = HubNullStationStatus.values();
        int statusId = Math.max(0, Math.min(values.length - 1, buffer.readVarInt()));
        return new HubNullStationSnapshot(
                ref,
                values[statusId],
                buffer.readUtf(),
                buffer.readVarInt(),
                HubNullStationNullType.byId(buffer.readVarInt()),
                buffer.readUtf(),
                buffer.readInt(),
                buffer.readBoolean() ? ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer) : null
        );
    }

    public static void writeList(RegistryFriendlyByteBuf buffer, List<HubNullStationSnapshot> snapshots) {
        List<HubNullStationSnapshot> safe = snapshots == null ? List.of() : snapshots;
        buffer.writeVarInt(safe.size());
        for (HubNullStationSnapshot snapshot : safe) {
            snapshot.write(buffer);
        }
    }

    public static List<HubNullStationSnapshot> readList(RegistryFriendlyByteBuf buffer) {
        int size = Math.min(1024, buffer.readVarInt());
        List<HubNullStationSnapshot> snapshots = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            snapshots.add(read(buffer));
        }
        return snapshots;
    }
}
