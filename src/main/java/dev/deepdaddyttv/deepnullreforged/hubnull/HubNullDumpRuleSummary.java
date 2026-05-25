package dev.deepdaddyttv.deepnullreforged.hubnull;

import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record HubNullDumpRuleSummary(
        ResourceLocation itemId,
        DumpNullItemStatus status,
        boolean advanced,
        boolean presetGenerated,
        List<StationRules> stations
) {
    public HubNullDumpRuleSummary {
        status = status == null ? DumpNullItemStatus.NEUTRAL : status;
        stations = List.copyOf(stations == null ? List.of() : stations);
    }

    public String namespace() {
        return itemId.getNamespace();
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceLocation(itemId);
        buffer.writeVarInt(status.ordinal());
        buffer.writeBoolean(advanced);
        buffer.writeBoolean(presetGenerated);
        buffer.writeVarInt(stations.size());
        for (StationRules station : stations) {
            station.ref().write(buffer);
            buffer.writeBoolean(station.advanced());
            buffer.writeBoolean(station.presetGenerated());
            buffer.writeVarInt(station.ruleSummaries().size());
            for (String summary : station.ruleSummaries()) {
                buffer.writeUtf(summary);
            }
        }
    }

    public static HubNullDumpRuleSummary read(RegistryFriendlyByteBuf buffer) {
        ResourceLocation itemId = buffer.readResourceLocation();
        DumpNullItemStatus status = DumpNullItemStatus.byId(buffer.readVarInt());
        boolean advanced = buffer.readBoolean();
        boolean presetGenerated = buffer.readBoolean();
        int size = Math.min(1024, buffer.readVarInt());
        List<StationRules> stations = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            HubNullStationRef ref = HubNullStationRef.read(buffer);
            boolean stationAdvanced = buffer.readBoolean();
            boolean stationPresetGenerated = buffer.readBoolean();
            int summaryCount = Math.min(128, buffer.readVarInt());
            List<String> summaries = new ArrayList<>(summaryCount);
            for (int summaryIndex = 0; summaryIndex < summaryCount; summaryIndex++) {
                summaries.add(buffer.readUtf());
            }
            stations.add(new StationRules(ref, stationAdvanced, stationPresetGenerated, summaries));
        }
        return new HubNullDumpRuleSummary(itemId, status, advanced, presetGenerated, stations);
    }

    public static void writeList(RegistryFriendlyByteBuf buffer, List<HubNullDumpRuleSummary> summaries) {
        List<HubNullDumpRuleSummary> safe = summaries == null ? List.of() : summaries;
        buffer.writeVarInt(safe.size());
        for (HubNullDumpRuleSummary summary : safe) {
            summary.write(buffer);
        }
    }

    public static List<HubNullDumpRuleSummary> readList(RegistryFriendlyByteBuf buffer) {
        int size = Math.min(4096, buffer.readVarInt());
        List<HubNullDumpRuleSummary> summaries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            summaries.add(read(buffer));
        }
        return summaries;
    }

    public record StationRules(HubNullStationRef ref, boolean advanced, boolean presetGenerated, List<String> ruleSummaries) {
        public StationRules {
            ruleSummaries = List.copyOf(ruleSummaries == null ? List.of() : ruleSummaries);
        }
    }
}
