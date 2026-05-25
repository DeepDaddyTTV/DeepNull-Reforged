package dev.deepdaddyttv.deepnullreforged.devmode;

import dev.deepdaddyttv.deepnullreforged.client.DeepNullDevModeClientState;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullDevModePayloads;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepNullDevModePayloadsTest {
    @Test
    void statePayloadRoundTripsAndUpdatesClientState() {
        DeepNullDevModePayloads.StatePayload original = new DeepNullDevModePayloads.StatePayload(true);
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DeepNullDevModePayloads.StatePayload.STREAM_CODEC.encode(buffer, original);
        DeepNullDevModePayloads.StatePayload decoded = DeepNullDevModePayloads.StatePayload.STREAM_CODEC.decode(buffer);

        assertEquals(original, decoded);

        DeepNullDevModeClientState.handleState(new DeepNullDevModePayloads.StatePayload(false));
        assertFalse(DeepNullDevModeClientState.isEnabled());

        DeepNullDevModeClientState.handleState(decoded);
        assertTrue(DeepNullDevModeClientState.isEnabled());
    }
}
