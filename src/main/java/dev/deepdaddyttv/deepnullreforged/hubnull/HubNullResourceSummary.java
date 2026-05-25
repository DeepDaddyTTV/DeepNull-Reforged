package dev.deepdaddyttv.deepnullreforged.hubnull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record HubNullResourceSummary(ResourceLocation itemId, long count, List<StationAmount> stations) {
    public HubNullResourceSummary {
        stations = List.copyOf(stations == null ? List.of() : stations);
    }

    public String namespace() {
        return itemId.getNamespace();
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceLocation(itemId);
        buffer.writeVarLong(count);
        buffer.writeVarInt(stations.size());
        for (StationAmount station : stations) {
            station.ref().write(buffer);
            buffer.writeVarLong(station.count());
        }
    }

    public static HubNullResourceSummary read(RegistryFriendlyByteBuf buffer) {
        ResourceLocation itemId = buffer.readResourceLocation();
        long count = buffer.readVarLong();
        int size = Math.min(1024, buffer.readVarInt());
        List<StationAmount> stations = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stations.add(new StationAmount(HubNullStationRef.read(buffer), buffer.readVarLong()));
        }
        return new HubNullResourceSummary(itemId, count, stations);
    }

    public static void writeList(RegistryFriendlyByteBuf buffer, List<HubNullResourceSummary> summaries) {
        List<HubNullResourceSummary> safe = summaries == null ? List.of() : summaries;
        buffer.writeVarInt(safe.size());
        for (HubNullResourceSummary summary : safe) {
            summary.write(buffer);
        }
    }

    public static List<HubNullResourceSummary> readList(RegistryFriendlyByteBuf buffer) {
        int size = Math.min(4096, buffer.readVarInt());
        List<HubNullResourceSummary> summaries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            summaries.add(read(buffer));
        }
        return summaries;
    }

    public record StationAmount(HubNullStationRef ref, long count) {
    }
}
