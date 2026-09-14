package dev.deepdaddyttv.deepnullreforged.network;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeepNullPayloadRoundTripTest {
    @Test
    void storageActionPacketsRoundTripEveryField() {
        DeepNullPayloads.StorageActionRequestPayload request = new DeepNullPayloads.StorageActionRequestPayload(19, 441L, 2, 1, 3, 8);
        DeepNullPayloads.StorageActionResultPayload result = new DeepNullPayloads.StorageActionResultPayload(19, 441L, 2, 1, 3, 8, true);

        assertEquals(request, roundTrip(DeepNullPayloads.StorageActionRequestPayload.STREAM_CODEC, request));
        assertEquals(result, roundTrip(DeepNullPayloads.StorageActionResultPayload.STREAM_CODEC, result));
    }

    @Test
    void extractionEditPacketsRoundTripEveryField() {
        DeepNullPayloads.ExtractionEditBeginPayload begin = new DeepNullPayloads.ExtractionEditBeginPayload(7, 22L, 4, true);
        DeepNullPayloads.ExtractionEditSetPayload set = new DeepNullPayloads.ExtractionEditSetPayload(7, 22L, 6, 37);
        DeepNullPayloads.ExtractionEditResultPayload result = new DeepNullPayloads.ExtractionEditResultPayload(7, 22L, 2, true);

        assertEquals(begin, roundTrip(DeepNullPayloads.ExtractionEditBeginPayload.STREAM_CODEC, begin));
        assertEquals(set, roundTrip(DeepNullPayloads.ExtractionEditSetPayload.STREAM_CODEC, set));
        assertEquals(result, roundTrip(DeepNullPayloads.ExtractionEditResultPayload.STREAM_CODEC, result));
    }

    private static <T> T roundTrip(net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, T> codec, T value) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            codec.encode(buffer, value);
            return codec.decode(buffer);
        } finally {
            buffer.release();
        }
    }
}
