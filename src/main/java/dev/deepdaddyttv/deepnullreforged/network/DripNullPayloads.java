package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripNullData;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripSwapEngine;
import dev.deepdaddyttv.deepnullreforged.item.DripNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.DripNullMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.lang.reflect.InvocationTargetException;

public final class DripNullPayloads {
    private DripNullPayloads() {
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String path) {
        return new CustomPacketPayload.Type<>(DeepNullReforged.id(path));
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(StatePayload.TYPE, StatePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientState(payload)));
        registrar.playToServer(OpenViewPayload.TYPE, OpenViewPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleOpenView(payload, player);
                    }
                }));
        registrar.playToServer(SetProfilePayload.TYPE, SetProfilePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DripNullMenu menu) {
                        menu.selectProfile(player, payload.profile());
                        sendStateUpdate(player, menu);
                    }
                }));
        registrar.playToServer(CycleHeldProfilePayload.TYPE, CycleHeldProfilePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleCycleHeldProfile(payload, player);
                    }
                }));
        registrar.playToServer(ToggleScopePayload.TYPE, ToggleScopePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DripNullMenu menu) {
                        menu.toggleScope(player, payload.scope());
                        sendStateUpdate(player, menu);
                    }
                }));
        registrar.playToServer(RunSwapPayload.TYPE, RunSwapPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DripNullMenu menu) {
                        var result = menu.runSwap(player);
                        player.displayClientMessage(result.message(), true);
                        sendStateUpdate(player, menu);
                    }
                }));
        registrar.playToServer(ClearProfilePayload.TYPE, ClearProfilePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DripNullMenu menu) {
                        var result = menu.clearSelectedProfile(player);
                        player.displayClientMessage(result.message(), true);
                        sendStateUpdate(player, menu);
                    }
                }));
        registrar.playToServer(RecoverPayload.TYPE, RecoverPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DripNullMenu menu) {
                        boolean recovered = menu.recover(player);
                        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(recovered
                                ? "item.deepnullreforged.drip_null.recovered"
                                : "item.deepnullreforged.drip_null.recovery_blocked"), true);
                        sendStateUpdate(player, menu);
                    }
                }));
        registrar.playToServer(InstallMendPayload.TYPE, InstallMendPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DripNullMenu menu) {
                        boolean installed = menu.installMendFromInventory(player);
                        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(installed
                                ? "item.deepnullreforged.drip_null.mend_installed"
                                : "item.deepnullreforged.drip_null.mend_missing"), true);
                        sendStateUpdate(player, menu);
                    }
                }));
        registrar.playToServer(CancelChargePayload.TYPE, CancelChargePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleCancelCharge(payload, player);
                    }
                }));
    }

    private static void handleOpenView(OpenViewPayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof DripNullMenu menu && payload.viewMode() >= 0 && payload.viewMode() < DripNullMenu.ViewMode.values().length) {
            menu.reopen(player, DripNullMenu.ViewMode.values()[payload.viewMode()]);
        }
    }

    private static void handleCycleHeldProfile(CycleHeldProfilePayload payload, ServerPlayer player) {
        if (payload.inventorySlot() < 0 || payload.inventorySlot() >= player.getInventory().getContainerSize()) {
            return;
        }
        ItemStack stack = player.getInventory().getItem(payload.inventorySlot());
        if (!(stack.getItem() instanceof DripNullItem dripNullItem)) {
            return;
        }
        DripSwapEngine.cycleProfile(player, stack, dripNullItem.tier(), payload.forward());
        player.getInventory().setChanged();
        if (player.containerMenu instanceof DripNullMenu menu
                && menu.getSourceType() == DripNullMenu.SourceType.ITEM
                && menu.getInventorySlot() == payload.inventorySlot()) {
            menu.updateData(DripNullData.get(stack, dripNullItem.tier(), player.level().registryAccess()));
            sendStateUpdate(player, menu);
        }
    }

    private static void handleCancelCharge(CancelChargePayload payload, ServerPlayer player) {
        if (payload.inventorySlot() < 0 || payload.inventorySlot() >= player.getInventory().getContainerSize()) {
            return;
        }
        ItemStack stack = player.getInventory().getItem(payload.inventorySlot());
        if (!(stack.getItem() instanceof DripNullItem dripNullItem)) {
            return;
        }
        DripNullData data = DripNullData.get(stack, dripNullItem.tier(), player.level().registryAccess());
        DripNullData.set(stack, data.withChargeCanceledUntil(player.level().getGameTime() + 5L), dripNullItem.tier(), player.level().registryAccess());
        player.getInventory().setChanged();
    }

    private static void sendStateUpdate(ServerPlayer player, DripNullMenu menu) {
        PacketDistributor.sendToPlayer(player, new StatePayload(menu.containerId, menu.getData()));
    }

    private static void handleClientState(StatePayload payload) {
        try {
            Class<?> handler = Class.forName("dev.deepdaddyttv.deepnullreforged.client.DripNullClientPayloadHandler");
            handler.getMethod("handleState", StatePayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    public record StatePayload(int containerId, DripNullData data) implements CustomPacketPayload {
        public static final Type<StatePayload> TYPE = payloadType("drip_null_state");
        public static final StreamCodec<RegistryFriendlyByteBuf, StatePayload> STREAM_CODEC = StreamCodec.ofMember(StatePayload::encode, StatePayload::decode);

        private static StatePayload decode(RegistryFriendlyByteBuf buffer) {
            return new StatePayload(buffer.readVarInt(), DripNullData.read(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            DripNullData.write(buffer, data);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record OpenViewPayload(int viewMode) implements CustomPacketPayload {
        public static final Type<OpenViewPayload> TYPE = payloadType("drip_null_open_view");
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenViewPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, OpenViewPayload::viewMode, OpenViewPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetProfilePayload(int profile) implements CustomPacketPayload {
        public static final Type<SetProfilePayload> TYPE = payloadType("drip_null_set_profile");
        public static final StreamCodec<RegistryFriendlyByteBuf, SetProfilePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, SetProfilePayload::profile, SetProfilePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record CycleHeldProfilePayload(int inventorySlot, boolean forward) implements CustomPacketPayload {
        public static final Type<CycleHeldProfilePayload> TYPE = payloadType("drip_null_cycle_held_profile");
        public static final StreamCodec<RegistryFriendlyByteBuf, CycleHeldProfilePayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        CycleHeldProfilePayload::inventorySlot,
                        ByteBufCodecs.BOOL,
                        CycleHeldProfilePayload::forward,
                        CycleHeldProfilePayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ToggleScopePayload(String scope) implements CustomPacketPayload {
        public static final Type<ToggleScopePayload> TYPE = payloadType("drip_null_toggle_scope");
        public static final StreamCodec<RegistryFriendlyByteBuf, ToggleScopePayload> STREAM_CODEC = StreamCodec.ofMember(ToggleScopePayload::encode, ToggleScopePayload::decode);

        private static ToggleScopePayload decode(RegistryFriendlyByteBuf buffer) {
            return new ToggleScopePayload(buffer.readUtf(32));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeUtf(scope, 32);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum RunSwapPayload implements CustomPacketPayload {
        INSTANCE;

        public static final Type<RunSwapPayload> TYPE = payloadType("drip_null_run_swap");
        public static final StreamCodec<RegistryFriendlyByteBuf, RunSwapPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum ClearProfilePayload implements CustomPacketPayload {
        INSTANCE;

        public static final Type<ClearProfilePayload> TYPE = payloadType("drip_null_clear_profile");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClearProfilePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum RecoverPayload implements CustomPacketPayload {
        INSTANCE;

        public static final Type<RecoverPayload> TYPE = payloadType("drip_null_recover");
        public static final StreamCodec<RegistryFriendlyByteBuf, RecoverPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum InstallMendPayload implements CustomPacketPayload {
        INSTANCE;

        public static final Type<InstallMendPayload> TYPE = payloadType("drip_null_install_mend");
        public static final StreamCodec<RegistryFriendlyByteBuf, InstallMendPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record CancelChargePayload(int inventorySlot) implements CustomPacketPayload {
        public static final Type<CancelChargePayload> TYPE = payloadType("drip_null_cancel_charge");
        public static final StreamCodec<RegistryFriendlyByteBuf, CancelChargePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, CancelChargePayload::inventorySlot, CancelChargePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
