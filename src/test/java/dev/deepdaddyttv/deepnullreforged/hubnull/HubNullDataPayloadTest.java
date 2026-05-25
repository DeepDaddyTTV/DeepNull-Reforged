package dev.deepdaddyttv.deepnullreforged.hubnull;

import dev.deepdaddyttv.deepnullreforged.client.HubNullScreen;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatus;
import dev.deepdaddyttv.deepnullreforged.network.HubNullPayloads;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HubNullDataPayloadTest {
    @Test
    void stationDataRoundTripsDedupesAndRemoves() {
        HubNullStationRef original = new HubNullStationRef(ResourceLocation.withDefaultNamespace("overworld"), new BlockPos(1, 2, 3), "Old");
        HubNullStationRef refreshed = new HubNullStationRef(ResourceLocation.withDefaultNamespace("overworld"), new BlockPos(1, 2, 3), "New");
        HubNullStationRef other = new HubNullStationRef(ResourceLocation.withDefaultNamespace("the_nether"), new BlockPos(4, 5, 6), "Other");

        HubNullData loaded = HubNullData.load(HubNullData.EMPTY.withStation(original).withStation(refreshed).withStation(other).save());

        assertEquals(2, loaded.stations().size());
        assertEquals("New", loaded.stations().get(0).name());
        assertTrue(loaded.contains(refreshed));

        assertEquals(List.of(other), loaded.withoutStation(refreshed).stations());

        HubNullStationRef third = new HubNullStationRef(ResourceLocation.withDefaultNamespace("the_end"), new BlockPos(7, 7, 7), "Third");
        HubNullData reordered = new HubNullData(List.of(refreshed, other, third)).reordered(refreshed, 2);
        assertEquals(List.of(other, third, refreshed), reordered.stations());
        assertEquals(List.of(refreshed, other, third), reordered.reordered(refreshed, -20).stations());
        assertEquals(reordered.stations(), reordered.reordered(new HubNullStationRef(ResourceLocation.withDefaultNamespace("the_end"), new BlockPos(9, 9, 9), "Missing"), 0).stations());
    }

    @Test
    void hubNullPayloadsRoundTripStationAndStateData() {
        HubNullStationRef station = new HubNullStationRef(ResourceLocation.withDefaultNamespace("overworld"), new BlockPos(7, 8, 9), "Station");
        HubNullStationSnapshot snapshot = new HubNullStationSnapshot(station, HubNullStationStatus.ONLINE, "IRON", 2);
        HubNullResourceSummary resource = new HubNullResourceSummary(
                ResourceLocation.withDefaultNamespace("iron_ingot"),
                64L,
                List.of(new HubNullResourceSummary.StationAmount(station, 64L))
        );
        HubNullDampResourceSummary dampResource = new HubNullDampResourceSummary(
                HubNullDampResourceKind.FLUID,
                ResourceLocation.withDefaultNamespace("water"),
                "Water",
                1000L,
                4000L,
                0xFF3AA7FF,
                ResourceLocation.withDefaultNamespace("empty"),
                List.of(new HubNullDampResourceSummary.StationAmount(station, 1000L, 4000L))
        );
        HubNullDumpRuleSummary dumpRule = new HubNullDumpRuleSummary(
                ResourceLocation.withDefaultNamespace("rotten_flesh"),
                DumpNullItemStatus.DISCARDED,
                true,
                false,
                List.of(new HubNullDumpRuleSummary.StationRules(station, true, false, List.of("VOID minecraft:rotten_flesh durability>=80%")))
        );
        HubNullResourceSummary denResource = new HubNullResourceSummary(
                ResourceLocation.withDefaultNamespace("cow"),
                3L,
                List.of(new HubNullResourceSummary.StationAmount(station, 3L))
        );
        HubNullPayloads.RefreshPayload refresh = new HubNullPayloads.RefreshPayload(3);
        HubNullPayloads.RemoveStationPayload remove = new HubNullPayloads.RemoveStationPayload(3, station);
        HubNullPayloads.OpenStationPayload open = new HubNullPayloads.OpenStationPayload(3, station);
        HubNullPayloads.ReorderStationPayload reorder = new HubNullPayloads.ReorderStationPayload(3, station, 0);
        HubNullPayloads.StatePayload state = new HubNullPayloads.StatePayload(3, List.of(snapshot), List.of(resource), List.of(dampResource), List.of(dumpRule), List.of(denResource));
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        HubNullPayloads.RefreshPayload.STREAM_CODEC.encode(buffer, refresh);
        HubNullPayloads.RemoveStationPayload.STREAM_CODEC.encode(buffer, remove);
        HubNullPayloads.OpenStationPayload.STREAM_CODEC.encode(buffer, open);
        HubNullPayloads.ReorderStationPayload.STREAM_CODEC.encode(buffer, reorder);
        HubNullPayloads.StatePayload.STREAM_CODEC.encode(buffer, state);

        assertEquals(refresh, HubNullPayloads.RefreshPayload.STREAM_CODEC.decode(buffer));
        assertEquals(remove, HubNullPayloads.RemoveStationPayload.STREAM_CODEC.decode(buffer));
        assertEquals(open, HubNullPayloads.OpenStationPayload.STREAM_CODEC.decode(buffer));
        assertEquals(reorder, HubNullPayloads.ReorderStationPayload.STREAM_CODEC.decode(buffer));
        assertEquals(state, HubNullPayloads.StatePayload.STREAM_CODEC.decode(buffer));
    }

    @Test
    void stationCoordinatesAndColorsAreStable() {
        HubNullStationRef station = new HubNullStationRef(ResourceLocation.withDefaultNamespace("overworld"), new BlockPos(7, 8, 9), "Station");
        HubNullStationRef same = new HubNullStationRef(ResourceLocation.withDefaultNamespace("overworld"), new BlockPos(7, 8, 9), "Renamed");
        HubNullStationSnapshot styled = new HubNullStationSnapshot(station, HubNullStationStatus.ONLINE, "IRON", 2, HubNullStationNullType.DAMP, "Iron Damp Null", 0x123456);
        HubNullStationSnapshot den = new HubNullStationSnapshot(station, HubNullStationStatus.ONLINE, "EMERALD", 2, HubNullStationNullType.DEN, "Emerald Den Null", 0x654321);

        assertEquals("7, 8, 9", HubNullScreen.coordinateText(station));
        assertEquals(HubNullScreen.stationColor(station), HubNullScreen.stationColor(same));
        assertTrue((HubNullScreen.stationColor(station) & 0xFF000000) == 0xFF000000);
        assertEquals(0xFF123456, HubNullScreen.stationColor(styled));
        assertEquals("Damp III", HubNullScreen.stationCompactLabel(styled));
        assertEquals("Den VI", HubNullScreen.stationCompactLabel(den));
    }

    @Test
    void denResourceNamesUseReadableCapitalizedFallbacks() {
        assertEquals("Custom mob", HubNullScreen.denResourceName(ResourceLocation.fromNamespaceAndPath("example", "custom_mob")));
    }
}
