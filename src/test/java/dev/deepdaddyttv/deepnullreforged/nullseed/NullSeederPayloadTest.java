package dev.deepdaddyttv.deepnullreforged.nullseed;

import dev.deepdaddyttv.deepnullreforged.network.NullWorkbenchPayloads;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NullSeederPayloadTest {
    @Test
    void seedPayloadsRoundTripPresetEntriesAndApplyData() {
        BlockPos blockPos = new BlockPos(1, 2, 3);
        List<NullSeedEntry> entries = List.of(
                NullSeedEntry.item(ResourceLocation.withDefaultNamespace("iron_ingot"), 0),
                NullSeedEntry.fluid(ResourceLocation.withDefaultNamespace("water"), 0, 1000),
                NullSeedEntry.chemical(ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen"), 1, 1000),
                NullSeedEntry.entity(ResourceLocation.withDefaultNamespace("cow"), 0)
        );
        NullWorkbenchPayloads.RequestSeedPresetPayload request = new NullWorkbenchPayloads.RequestSeedPresetPayload(blockPos, NullSeedPresetCatalog.BUILT_IN_ORES);
        NullWorkbenchPayloads.RequestSeedPresetListPayload requestList = new NullWorkbenchPayloads.RequestSeedPresetListPayload(blockPos);
        NullSeedPreset preset = new NullSeedPreset(
                "user:test",
                "Test Preset",
                NullSeedPresetSource.USER,
                "test",
                List.of(NullSeedKind.ITEM, NullSeedKind.FLUID, NullSeedKind.ENTITY),
                entries
        );
        NullWorkbenchPayloads.SeedPresetPayload response = new NullWorkbenchPayloads.SeedPresetPayload(4, preset);
        NullWorkbenchPayloads.SeedPresetListPayload list = new NullWorkbenchPayloads.SeedPresetListPayload(4, List.of(preset.summary()));
        NullWorkbenchPayloads.SaveSeedPresetPayload save = new NullWorkbenchPayloads.SaveSeedPresetPayload(blockPos, "user:test", "Test Preset", entries);
        NullWorkbenchPayloads.DeleteSeedPresetPayload delete = new NullWorkbenchPayloads.DeleteSeedPresetPayload(blockPos, "user:test");
        NullWorkbenchPayloads.ApplySeedConfigPayload apply = new NullWorkbenchPayloads.ApplySeedConfigPayload(
                blockPos,
                List.of(
                        NullSeedEntry.item(ResourceLocation.withDefaultNamespace("iron_ingot"), 0),
                        NullSeedEntry.item(ResourceLocation.withDefaultNamespace("redstone"), 1),
                        NullSeedEntry.fluid(ResourceLocation.withDefaultNamespace("water"), 0, 1000),
                        NullSeedEntry.entity(ResourceLocation.withDefaultNamespace("pig"), 2)
                ),
                true
        );
        NullWorkbenchPayloads.SeedApplyResultPayload result = new NullWorkbenchPayloads.SeedApplyResultPayload(4, 2, 2, 0, 0);
        NullWorkbenchPayloads.ClearSeedReservationsPayload clear = new NullWorkbenchPayloads.ClearSeedReservationsPayload(blockPos);
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        NullWorkbenchPayloads.RequestSeedPresetPayload.STREAM_CODEC.encode(buffer, request);
        NullWorkbenchPayloads.RequestSeedPresetListPayload.STREAM_CODEC.encode(buffer, requestList);
        NullWorkbenchPayloads.SeedPresetPayload.STREAM_CODEC.encode(buffer, response);
        NullWorkbenchPayloads.SeedPresetListPayload.STREAM_CODEC.encode(buffer, list);
        NullWorkbenchPayloads.SaveSeedPresetPayload.STREAM_CODEC.encode(buffer, save);
        NullWorkbenchPayloads.DeleteSeedPresetPayload.STREAM_CODEC.encode(buffer, delete);
        NullWorkbenchPayloads.ApplySeedConfigPayload.STREAM_CODEC.encode(buffer, apply);
        NullWorkbenchPayloads.SeedApplyResultPayload.STREAM_CODEC.encode(buffer, result);
        NullWorkbenchPayloads.ClearSeedReservationsPayload.STREAM_CODEC.encode(buffer, clear);

        assertEquals(request, NullWorkbenchPayloads.RequestSeedPresetPayload.STREAM_CODEC.decode(buffer));
        assertEquals(requestList, NullWorkbenchPayloads.RequestSeedPresetListPayload.STREAM_CODEC.decode(buffer));
        assertEquals(response, NullWorkbenchPayloads.SeedPresetPayload.STREAM_CODEC.decode(buffer));
        assertEquals(list, NullWorkbenchPayloads.SeedPresetListPayload.STREAM_CODEC.decode(buffer));
        assertEquals(save, NullWorkbenchPayloads.SaveSeedPresetPayload.STREAM_CODEC.decode(buffer));
        assertEquals(delete, NullWorkbenchPayloads.DeleteSeedPresetPayload.STREAM_CODEC.decode(buffer));
        assertEquals(apply, NullWorkbenchPayloads.ApplySeedConfigPayload.STREAM_CODEC.decode(buffer));
        assertEquals(result, NullWorkbenchPayloads.SeedApplyResultPayload.STREAM_CODEC.decode(buffer));
        assertEquals(clear, NullWorkbenchPayloads.ClearSeedReservationsPayload.STREAM_CODEC.decode(buffer));
    }
}
