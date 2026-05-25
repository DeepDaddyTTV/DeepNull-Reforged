package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullData;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullDampResourceSummary;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullDumpRuleSummary;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullResourceSummary;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullSnapshot;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationRef;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationSnapshot;
import dev.deepdaddyttv.deepnullreforged.item.DenNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DumpNullItem;
import dev.deepdaddyttv.deepnullreforged.item.HubNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.DenNullMenuOpener;
import dev.deepdaddyttv.deepnullreforged.menu.DumpNullMenuOpener;
import dev.deepdaddyttv.deepnullreforged.menu.HubNullMenu;
import dev.deepdaddyttv.deepnullreforged.menu.HubNullMenuOpener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public final class HubNullPayloads {
    private HubNullPayloads() {
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String path) {
        return new CustomPacketPayload.Type<>(DeepNullReforged.id(path));
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(RefreshPayload.TYPE, RefreshPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleRefresh(payload, player);
                    }
                }));
        registrar.playToServer(RemoveStationPayload.TYPE, RemoveStationPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleRemoveStation(payload, player);
                    }
                }));
        registrar.playToServer(OpenStationPayload.TYPE, OpenStationPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleOpenStation(payload, player);
                    }
                }));
        registrar.playToServer(ReorderStationPayload.TYPE, ReorderStationPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleReorderStation(payload, player);
                    }
                }));
        registrar.playToClient(StatePayload.TYPE, StatePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientState(payload)));
    }

    public static void sendState(ServerPlayer player, HubNullMenu menu) {
        PacketDistributor.sendToPlayer(player, new StatePayload(
                menu.containerId,
                menu.getStationSnapshots(),
                menu.getResourceSummaries(),
                menu.getDampResourceSummaries(),
                menu.getDumpRuleSummaries(),
                menu.getDenResourceSummaries()
        ));
    }

    private static void handleRefresh(RefreshPayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof HubNullMenu menu && menu.containerId == payload.containerId()) {
            menu.refreshState(player, false);
        }
    }

    private static void handleRemoveStation(RemoveStationPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof HubNullMenu menu) || menu.containerId != payload.containerId()) {
            return;
        }
        ItemStack stack = menu.resolveStack(player);
        if (!(stack.getItem() instanceof HubNullItem)) {
            return;
        }
        HubNullData.set(stack, HubNullData.get(stack).withoutStation(payload.station()));
        menu.refreshState(player, true);
    }

    private static void handleOpenStation(OpenStationPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof HubNullMenu menu) || menu.containerId != payload.containerId()) {
            return;
        }
        ItemStack stack = menu.resolveStack(player);
        if (!(stack.getItem() instanceof HubNullItem)) {
            return;
        }
        HubNullData data = HubNullData.get(stack);
        DeepNullDockBlockEntity dock = HubNullSnapshot.resolveOpenableDock(player.server, data, payload.station());
        if (dock == null) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("item.deepnullreforged.hub_null.remote_unavailable"), true);
            menu.refreshState(player, true);
            return;
        }
        if (dock.getStoredDeepNull().getItem() instanceof DenNullItem) {
            DenNullMenuOpener.openRemoteDock(player, menu.getInventorySlot(), payload.station(), dock);
        } else if (dock.getStoredDeepNull().getItem() instanceof DumpNullItem) {
            DumpNullMenuOpener.openRemoteDock(player, menu.getInventorySlot(), payload.station(), dock);
        } else {
            HubNullMenuOpener.openRemoteDock(player, menu.getInventorySlot(), payload.station(), dock);
        }
    }

    private static void handleReorderStation(ReorderStationPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof HubNullMenu menu) || menu.containerId != payload.containerId()) {
            return;
        }
        ItemStack stack = menu.resolveStack(player);
        if (!(stack.getItem() instanceof HubNullItem)) {
            return;
        }
        HubNullData data = HubNullData.get(stack);
        if (!data.contains(payload.station())) {
            return;
        }
        HubNullData.set(stack, data.reordered(payload.station(), payload.targetIndex()));
        menu.refreshState(player, true);
    }

    private static void handleClientState(StatePayload payload) {
        try {
            Class<?> handler = Class.forName("dev.deepdaddyttv.deepnullreforged.client.HubNullClientPayloadHandler");
            handler.getMethod("handleState", StatePayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    public record RefreshPayload(int containerId) implements CustomPacketPayload {
        public static final Type<RefreshPayload> TYPE = payloadType("hub_null_refresh");
        public static final StreamCodec<RegistryFriendlyByteBuf, RefreshPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, RefreshPayload::containerId, RefreshPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RemoveStationPayload(int containerId, HubNullStationRef station) implements CustomPacketPayload {
        public static final Type<RemoveStationPayload> TYPE = payloadType("hub_null_remove_station");
        public static final StreamCodec<RegistryFriendlyByteBuf, RemoveStationPayload> STREAM_CODEC = StreamCodec.ofMember(RemoveStationPayload::encode, RemoveStationPayload::decode);

        private static RemoveStationPayload decode(RegistryFriendlyByteBuf buffer) {
            return new RemoveStationPayload(buffer.readVarInt(), HubNullStationRef.read(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            station.write(buffer);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record OpenStationPayload(int containerId, HubNullStationRef station) implements CustomPacketPayload {
        public static final Type<OpenStationPayload> TYPE = payloadType("hub_null_open_station");
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenStationPayload> STREAM_CODEC = StreamCodec.ofMember(OpenStationPayload::encode, OpenStationPayload::decode);

        private static OpenStationPayload decode(RegistryFriendlyByteBuf buffer) {
            return new OpenStationPayload(buffer.readVarInt(), HubNullStationRef.read(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            station.write(buffer);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ReorderStationPayload(int containerId, HubNullStationRef station, int targetIndex) implements CustomPacketPayload {
        public static final Type<ReorderStationPayload> TYPE = payloadType("hub_null_reorder_station");
        public static final StreamCodec<RegistryFriendlyByteBuf, ReorderStationPayload> STREAM_CODEC = StreamCodec.ofMember(ReorderStationPayload::encode, ReorderStationPayload::decode);

        private static ReorderStationPayload decode(RegistryFriendlyByteBuf buffer) {
            return new ReorderStationPayload(buffer.readVarInt(), HubNullStationRef.read(buffer), buffer.readVarInt());
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            station.write(buffer);
            buffer.writeVarInt(targetIndex);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record StatePayload(
            int containerId,
            List<HubNullStationSnapshot> stations,
            List<HubNullResourceSummary> resources,
            List<HubNullDampResourceSummary> dampResources,
            List<HubNullDumpRuleSummary> dumpRules,
            List<HubNullResourceSummary> denResources
    ) implements CustomPacketPayload {
        public static final Type<StatePayload> TYPE = payloadType("hub_null_state");
        public static final StreamCodec<RegistryFriendlyByteBuf, StatePayload> STREAM_CODEC = StreamCodec.ofMember(StatePayload::encode, StatePayload::decode);

        private static StatePayload decode(RegistryFriendlyByteBuf buffer) {
            return new StatePayload(
                    buffer.readVarInt(),
                    HubNullStationSnapshot.readList(buffer),
                    HubNullResourceSummary.readList(buffer),
                    HubNullDampResourceSummary.readList(buffer),
                    HubNullDumpRuleSummary.readList(buffer),
                    HubNullResourceSummary.readList(buffer)
            );
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            HubNullStationSnapshot.writeList(buffer, stations);
            HubNullResourceSummary.writeList(buffer, resources);
            HubNullDampResourceSummary.writeList(buffer, dampResources);
            HubNullDumpRuleSummary.writeList(buffer, dumpRules);
            HubNullResourceSummary.writeList(buffer, denResources);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
