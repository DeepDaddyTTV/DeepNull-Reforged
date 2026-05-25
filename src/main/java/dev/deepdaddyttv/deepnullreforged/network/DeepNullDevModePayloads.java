package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.lang.reflect.InvocationTargetException;

public final class DeepNullDevModePayloads {
    private DeepNullDevModePayloads() {
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String path) {
        return new CustomPacketPayload.Type<>(DeepNullReforged.id(path));
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(StatePayload.TYPE, StatePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientState(payload)));
    }

    public static void sendState(ServerPlayer player, boolean enabled) {
        PacketDistributor.sendToPlayer(player, new StatePayload(enabled));
    }

    private static void handleClientState(StatePayload payload) {
        try {
            Class<?> handler = Class.forName("dev.deepdaddyttv.deepnullreforged.client.DeepNullDevModeClientState");
            handler.getMethod("handleState", StatePayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    public record StatePayload(boolean enabled) implements CustomPacketPayload {
        public static final Type<StatePayload> TYPE = payloadType("dev_mode_state");
        public static final StreamCodec<RegistryFriendlyByteBuf, StatePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, StatePayload::enabled, StatePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
