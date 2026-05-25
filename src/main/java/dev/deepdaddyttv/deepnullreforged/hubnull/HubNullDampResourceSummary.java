package dev.deepdaddyttv.deepnullreforged.hubnull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record HubNullDampResourceSummary(
        HubNullDampResourceKind kind,
        ResourceLocation resourceId,
        String displayName,
        long amount,
        long capacity,
        int tint,
        ResourceLocation iconId,
        List<StationAmount> stations
) {
    public HubNullDampResourceSummary {
        kind = kind == null ? HubNullDampResourceKind.FLUID : kind;
        displayName = displayName == null ? "" : displayName;
        iconId = iconId == null ? ResourceLocation.withDefaultNamespace("empty") : iconId;
        stations = List.copyOf(stations == null ? List.of() : stations);
    }

    public String namespace() {
        return resourceId.getNamespace();
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(kind.ordinal());
        buffer.writeResourceLocation(resourceId);
        buffer.writeUtf(displayName);
        buffer.writeVarLong(amount);
        buffer.writeVarLong(capacity);
        buffer.writeInt(tint);
        buffer.writeResourceLocation(iconId);
        buffer.writeVarInt(stations.size());
        for (StationAmount station : stations) {
            station.ref().write(buffer);
            buffer.writeVarLong(station.amount());
            buffer.writeVarLong(station.capacity());
        }
    }

    public static HubNullDampResourceSummary read(RegistryFriendlyByteBuf buffer) {
        HubNullDampResourceKind kind = HubNullDampResourceKind.byId(buffer.readVarInt());
        ResourceLocation resourceId = buffer.readResourceLocation();
        String displayName = buffer.readUtf();
        long amount = buffer.readVarLong();
        long capacity = buffer.readVarLong();
        int tint = buffer.readInt();
        ResourceLocation iconId = buffer.readResourceLocation();
        int size = Math.min(1024, buffer.readVarInt());
        List<StationAmount> stations = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stations.add(new StationAmount(HubNullStationRef.read(buffer), buffer.readVarLong(), buffer.readVarLong()));
        }
        return new HubNullDampResourceSummary(kind, resourceId, displayName, amount, capacity, tint, iconId, stations);
    }

    public static void writeList(RegistryFriendlyByteBuf buffer, List<HubNullDampResourceSummary> summaries) {
        List<HubNullDampResourceSummary> safe = summaries == null ? List.of() : summaries;
        buffer.writeVarInt(safe.size());
        for (HubNullDampResourceSummary summary : safe) {
            summary.write(buffer);
        }
    }

    public static List<HubNullDampResourceSummary> readList(RegistryFriendlyByteBuf buffer) {
        int size = Math.min(4096, buffer.readVarInt());
        List<HubNullDampResourceSummary> summaries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            summaries.add(read(buffer));
        }
        return summaries;
    }

    public record StationAmount(HubNullStationRef ref, long amount, long capacity) {
    }
}
