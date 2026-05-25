package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullCaptureFilterMode;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullData;
import dev.deepdaddyttv.deepnullreforged.item.DenNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.DenNullMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.lang.reflect.InvocationTargetException;

public final class DenNullPayloads {
    private DenNullPayloads() {
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String path) {
        return new CustomPacketPayload.Type<>(DeepNullReforged.id(path));
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(StatePayload.TYPE, StatePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientState(payload)));
        registrar.playToServer(SetSelectedPayload.TYPE, SetSelectedPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleSetSelected(payload, player);
                    }
                }));
        registrar.playToServer(ReleaseEntryPayload.TYPE, ReleaseEntryPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleReleaseEntry(payload, player);
                    }
                }));
        registrar.playToServer(CycleHeldPayload.TYPE, CycleHeldPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleCycleHeld(payload, player);
                    }
                }));
        registrar.playToServer(OpenViewPayload.TYPE, OpenViewPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleOpenView(payload, player);
                    }
                }));
        registrar.playToServer(SetDyePayload.TYPE, SetDyePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DenNullMenu menu) {
                        menu.setDyeColor(player, payload.dyeColorId());
                        sendStateUpdate(player, menu, menu.getData());
                    }
                }));
        registrar.playToServer(ToggleBabyPayload.TYPE, ToggleBabyPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DenNullMenu menu) {
                        menu.setBabyEnabled(player, payload.enabled());
                        sendStateUpdate(player, menu, menu.getData());
                    }
                }));
        registrar.playToServer(ToggleCapturePayload.TYPE, ToggleCapturePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DenNullMenu menu) {
                        menu.setCaptureEnabled(player, payload.enabled());
                        sendStateUpdate(player, menu, menu.getData());
                    }
                }));
        registrar.playToServer(SetCaptureFilterModePayload.TYPE, SetCaptureFilterModePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DenNullMenu menu) {
                        menu.setCaptureFilterMode(player, DenNullCaptureFilterMode.byId(payload.mode()));
                        sendStateUpdate(player, menu, menu.getData());
                    }
                }));
        registrar.playToServer(SetTagTemplatePayload.TYPE, SetTagTemplatePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DenNullMenu menu) {
                        menu.setTagTemplate(player, payload.template());
                        sendStateUpdate(player, menu, menu.getData());
                    }
                }));
        registrar.playToServer(AddCaptureFilterPayload.TYPE, AddCaptureFilterPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DenNullMenu menu) {
                        ResourceLocation id = ResourceLocation.tryParse(payload.entityId());
                        if (id != null) {
                            menu.addCaptureFilter(player, id);
                            sendStateUpdate(player, menu, menu.getData());
                        }
                    }
                }));
        registrar.playToServer(ClearCaptureFilterPayload.TYPE, ClearCaptureFilterPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DenNullMenu menu) {
                        menu.clearCaptureFilter(player);
                        sendStateUpdate(player, menu, menu.getData());
                    }
                }));
    }

    private static void handleSetSelected(SetSelectedPayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof DenNullMenu menu && menu.containerId == payload.containerId()) {
            if (menu.setSelectedIndex(player, payload.selectedIndex())) {
                sendStateUpdate(player, menu, menu.getData());
            }
        }
    }

    private static void handleReleaseEntry(ReleaseEntryPayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof DenNullMenu menu && menu.containerId == payload.containerId()) {
            if (menu.releaseEntryAtFeet(player, payload.entryIndex())) {
                sendStateUpdate(player, menu, menu.getData());
            }
        }
    }

    private static void handleCycleHeld(CycleHeldPayload payload, ServerPlayer player) {
        if (payload.inventorySlot() < 0 || payload.inventorySlot() >= player.getInventory().getContainerSize()) {
            return;
        }
        ItemStack stack = player.getInventory().getItem(payload.inventorySlot());
        if (stack.getItem() instanceof DenNullItem denNullItem) {
            denNullItem.cycleSelected(stack, payload.forward());
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            if (player.containerMenu != player.inventoryMenu) {
                player.containerMenu.broadcastChanges();
            }
            if (player.containerMenu instanceof DenNullMenu menu) {
                ItemStack menuStack = menu.resolveStack(player);
                if (menuStack == stack) {
                    DenNullData data = DenNullData.get(stack);
                    menu.updateData(data);
                    sendStateUpdate(player, menu, data);
                }
            }
        }
    }

    private static void handleOpenView(OpenViewPayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof DenNullMenu menu && payload.viewMode() >= 0 && payload.viewMode() < DenNullMenu.ViewMode.values().length) {
            menu.reopen(player, DenNullMenu.ViewMode.values()[payload.viewMode()]);
        }
    }

    private static void sendStateUpdate(ServerPlayer player, DenNullMenu menu, DenNullData data) {
        menu.updateData(data);
        PacketDistributor.sendToPlayer(player, new StatePayload(menu.containerId, data));
    }

    private static void handleClientState(StatePayload payload) {
        try {
            Class<?> handler = Class.forName("dev.deepdaddyttv.deepnullreforged.client.DenNullClientPayloadHandler");
            handler.getMethod("handleState", StatePayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    public record StatePayload(int containerId, DenNullData data) implements CustomPacketPayload {
        public static final Type<StatePayload> TYPE = payloadType("den_null_state");
        public static final StreamCodec<RegistryFriendlyByteBuf, StatePayload> STREAM_CODEC = StreamCodec.ofMember(StatePayload::encode, StatePayload::decode);

        public StatePayload {
            data = data == null ? DenNullData.EMPTY : data;
        }

        private static StatePayload decode(RegistryFriendlyByteBuf buffer) {
            return new StatePayload(buffer.readVarInt(), DenNullData.read(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            DenNullData.write(buffer, data);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetSelectedPayload(int containerId, int selectedIndex) implements CustomPacketPayload {
        public static final Type<SetSelectedPayload> TYPE = payloadType("den_null_set_selected");
        public static final StreamCodec<RegistryFriendlyByteBuf, SetSelectedPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        SetSelectedPayload::containerId,
                        ByteBufCodecs.VAR_INT,
                        SetSelectedPayload::selectedIndex,
                        SetSelectedPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ReleaseEntryPayload(int containerId, int entryIndex) implements CustomPacketPayload {
        public static final Type<ReleaseEntryPayload> TYPE = payloadType("den_null_release_entry");
        public static final StreamCodec<RegistryFriendlyByteBuf, ReleaseEntryPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ReleaseEntryPayload::containerId,
                        ByteBufCodecs.VAR_INT,
                        ReleaseEntryPayload::entryIndex,
                        ReleaseEntryPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record CycleHeldPayload(int inventorySlot, boolean forward) implements CustomPacketPayload {
        public static final Type<CycleHeldPayload> TYPE = payloadType("den_null_cycle_held");
        public static final StreamCodec<RegistryFriendlyByteBuf, CycleHeldPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        CycleHeldPayload::inventorySlot,
                        ByteBufCodecs.BOOL,
                        CycleHeldPayload::forward,
                        CycleHeldPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record OpenViewPayload(int viewMode) implements CustomPacketPayload {
        public static final Type<OpenViewPayload> TYPE = payloadType("den_null_open_view");
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenViewPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, OpenViewPayload::viewMode, OpenViewPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetDyePayload(int dyeColorId) implements CustomPacketPayload {
        public static final Type<SetDyePayload> TYPE = payloadType("den_null_set_dye");
        public static final StreamCodec<RegistryFriendlyByteBuf, SetDyePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, SetDyePayload::dyeColorId, SetDyePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ToggleBabyPayload(boolean enabled) implements CustomPacketPayload {
        public static final Type<ToggleBabyPayload> TYPE = payloadType("den_null_toggle_baby");
        public static final StreamCodec<RegistryFriendlyByteBuf, ToggleBabyPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, ToggleBabyPayload::enabled, ToggleBabyPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ToggleCapturePayload(boolean enabled) implements CustomPacketPayload {
        public static final Type<ToggleCapturePayload> TYPE = payloadType("den_null_toggle_capture");
        public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCapturePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, ToggleCapturePayload::enabled, ToggleCapturePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetCaptureFilterModePayload(int mode) implements CustomPacketPayload {
        public static final Type<SetCaptureFilterModePayload> TYPE = payloadType("den_null_capture_filter_mode");
        public static final StreamCodec<RegistryFriendlyByteBuf, SetCaptureFilterModePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, SetCaptureFilterModePayload::mode, SetCaptureFilterModePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetTagTemplatePayload(String template) implements CustomPacketPayload {
        public static final Type<SetTagTemplatePayload> TYPE = payloadType("den_null_tag_template");
        public static final StreamCodec<RegistryFriendlyByteBuf, SetTagTemplatePayload> STREAM_CODEC = StreamCodec.ofMember(SetTagTemplatePayload::encode, SetTagTemplatePayload::decode);

        private static SetTagTemplatePayload decode(RegistryFriendlyByteBuf buffer) {
            return new SetTagTemplatePayload(buffer.readUtf(128));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeUtf(template, 128);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record AddCaptureFilterPayload(String entityId) implements CustomPacketPayload {
        public static final Type<AddCaptureFilterPayload> TYPE = payloadType("den_null_add_capture_filter");
        public static final StreamCodec<RegistryFriendlyByteBuf, AddCaptureFilterPayload> STREAM_CODEC = StreamCodec.ofMember(AddCaptureFilterPayload::encode, AddCaptureFilterPayload::decode);

        private static AddCaptureFilterPayload decode(RegistryFriendlyByteBuf buffer) {
            return new AddCaptureFilterPayload(buffer.readUtf(128));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeUtf(entityId, 128);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum ClearCaptureFilterPayload implements CustomPacketPayload {
        INSTANCE;

        public static final Type<ClearCaptureFilterPayload> TYPE = payloadType("den_null_clear_capture_filter");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClearCaptureFilterPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
