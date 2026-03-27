package net.neoforged.neoforge.network.event;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class RegisterPayloadHandlersEvent {
    public Registrar registrar(String version) {
        return new Registrar();
    }

    public static final class Registrar {
        public <T extends CustomPacketPayload> void playToServer(
                CustomPacketPayload.Type<T> type,
                StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec,
                PlayToServerHandler<T> handler
        ) {
            PayloadTypeRegistry.playC2S().register(type, streamCodec.cast());
            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                    handler.handle(payload, new Context(context)));
        }
    }

    @FunctionalInterface
    public interface PlayToServerHandler<T extends CustomPacketPayload> {
        void handle(T payload, Context context);
    }

    public static final class Context {
        private final ServerPlayNetworking.Context context;

        private Context(ServerPlayNetworking.Context context) {
            this.context = context;
        }

        public ServerPlayer player() {
            return context.player();
        }

        public void enqueueWork(Runnable task) {
            context.server().execute(task);
        }
    }
}
