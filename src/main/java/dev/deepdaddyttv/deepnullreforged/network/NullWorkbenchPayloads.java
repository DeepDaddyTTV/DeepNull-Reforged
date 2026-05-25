package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.integration.jei.NullWorkbenchTransferSupport;
import dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedEntry;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedKind;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPlan;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPreset;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPresetCatalog;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPresetSavedData;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPresetSummary;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public final class NullWorkbenchPayloads {
    private NullWorkbenchPayloads() {
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String path) {
        return new CustomPacketPayload.Type<>(DeepNullReforged.id(path));
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(ApplyStylePayload.TYPE, ApplyStylePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleApplyStyle(payload, player);
                    }
                }));
        registrar.playToServer(TransferRecipePayload.TYPE, TransferRecipePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleTransferRecipe(payload, player);
                    }
                }));
        registrar.playToServer(RequestSeedPresetPayload.TYPE, RequestSeedPresetPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleRequestSeedPreset(payload, player);
                    }
                }));
        registrar.playToServer(RequestSeedPresetListPayload.TYPE, RequestSeedPresetListPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleRequestSeedPresetList(payload, player);
                    }
                }));
        registrar.playToServer(SaveSeedPresetPayload.TYPE, SaveSeedPresetPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleSaveSeedPreset(payload, player);
                    }
                }));
        registrar.playToServer(DeleteSeedPresetPayload.TYPE, DeleteSeedPresetPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleDeleteSeedPreset(payload, player);
                    }
                }));
        registrar.playToServer(ApplySeedConfigPayload.TYPE, ApplySeedConfigPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleApplySeedConfig(payload, player);
                    }
                }));
        registrar.playToServer(ClearSeedReservationsPayload.TYPE, ClearSeedReservationsPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleClearSeedReservations(payload, player);
                    }
                }));
        registrar.playToClient(SeedPresetPayload.TYPE, SeedPresetPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientSeedPreset(payload)));
        registrar.playToClient(SeedPresetListPayload.TYPE, SeedPresetListPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientSeedPresetList(payload)));
        registrar.playToClient(SeedApplyResultPayload.TYPE, SeedApplyResultPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientSeedApplyResult(payload)));
    }

    private static void handleApplyStyle(ApplyStylePayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof NullWorkbenchMenu menu) || !menu.getBlockPos().equals(payload.blockPos())) {
            return;
        }
        if (payload.reset()) {
            menu.resetStyleColors();
        } else {
            menu.applyStyleColors(payload.frameColor(), payload.glassColor());
        }
        menu.broadcastChanges();
    }

    private static void handleTransferRecipe(TransferRecipePayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof NullWorkbenchMenu menu) || !menu.getBlockPos().equals(payload.blockPos())) {
            return;
        }
        var recipe = NullWorkbenchTransferSupport.findRecipe(payload.resultItemId());
        if (recipe == null) {
            return;
        }
        if (NullWorkbenchTransferSupport.executeTransfer(menu, player, recipe)) {
            menu.broadcastChanges();
        }
    }

    private static void handleRequestSeedPreset(RequestSeedPresetPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof NullWorkbenchMenu menu) || !menu.getBlockPos().equals(payload.blockPos())) {
            return;
        }
        NullSeedKind kind = menu.seedKind();
        if (!menu.canSeedNull() || kind == null || !(player.level() instanceof ServerLevel serverLevel)) {
            PacketDistributor.sendToPlayer(player, new SeedPresetPayload(menu.containerId, NullSeedPreset.empty(payload.presetId())));
            return;
        }
        NullSeedPreset preset = NullSeedPresetCatalog.find(serverLevel, kind, payload.presetId()).orElse(NullSeedPreset.empty(payload.presetId()));
        PacketDistributor.sendToPlayer(player, new SeedPresetPayload(menu.containerId, preset));
    }

    private static void handleRequestSeedPresetList(RequestSeedPresetListPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof NullWorkbenchMenu menu) || !menu.getBlockPos().equals(payload.blockPos())) {
            return;
        }
        sendSeedPresetList(player, menu);
    }

    private static void handleSaveSeedPreset(SaveSeedPresetPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof NullWorkbenchMenu menu) || !menu.getBlockPos().equals(payload.blockPos()) || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        NullSeedKind kind = menu.seedKind();
        if (kind == null || payload.entries().isEmpty()) {
            return;
        }
        String presetId = payload.presetId().isBlank()
                ? "user:" + player.getUUID() + ":" + System.currentTimeMillis()
                : payload.presetId();
        NullSeedPreset preset = new NullSeedPreset(
                presetId,
                payload.displayName(),
                dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPresetSource.USER,
                player.getScoreboardName(),
                List.of(kind),
                payload.entries()
        ).asUserPreset();
        NullSeedPresetSavedData.get(serverLevel).savePreset(preset);
        sendSeedPresetList(player, menu);
        PacketDistributor.sendToPlayer(player, new SeedPresetPayload(menu.containerId, preset));
    }

    private static void handleDeleteSeedPreset(DeleteSeedPresetPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof NullWorkbenchMenu menu) || !menu.getBlockPos().equals(payload.blockPos()) || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        NullSeedPresetSavedData.get(serverLevel).deletePreset(payload.presetId());
        sendSeedPresetList(player, menu);
    }

    private static void sendSeedPresetList(ServerPlayer player, NullWorkbenchMenu menu) {
        NullSeedKind kind = menu.seedKind();
        if (kind == null || !(player.level() instanceof ServerLevel serverLevel)) {
            PacketDistributor.sendToPlayer(player, new SeedPresetListPayload(menu.containerId, List.of()));
            return;
        }
        List<NullSeedPresetSummary> summaries = NullSeedPresetCatalog.all(serverLevel, kind).stream()
                .map(NullSeedPreset::summary)
                .toList();
        PacketDistributor.sendToPlayer(player, new SeedPresetListPayload(menu.containerId, summaries));
    }

    private static void handleApplySeedConfig(ApplySeedConfigPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof NullWorkbenchMenu menu) || !menu.getBlockPos().equals(payload.blockPos()) || menu.getWorkbench() == null) {
            return;
        }
        NullSeedPlan result = menu.getWorkbench().applySeedEntries(payload.entries(), payload.replaceReservations());
        menu.broadcastChanges();
        PacketDistributor.sendToPlayer(player, SeedApplyResultPayload.from(menu.containerId, result));
    }

    private static void handleClearSeedReservations(ClearSeedReservationsPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof NullWorkbenchMenu menu) || !menu.getBlockPos().equals(payload.blockPos()) || menu.getWorkbench() == null) {
            return;
        }
        menu.getWorkbench().clearSeedReservations();
        menu.broadcastChanges();
        PacketDistributor.sendToPlayer(player, new SeedApplyResultPayload(menu.containerId, 0, 0, 0, 0));
    }

    private static void handleClientSeedPreset(SeedPresetPayload payload) {
        try {
            Class<?> handlerClass = Class.forName("dev.deepdaddyttv.deepnullreforged.client.NullWorkbenchClientPayloadHandler");
            handlerClass.getMethod("handleSeedPreset", SeedPresetPayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | LinkageError exception) {
            DeepNullReforged.LOGGER.debug("Skipping Null Workbench seed preset payload handler", exception);
        }
    }

    private static void handleClientSeedPresetList(SeedPresetListPayload payload) {
        try {
            Class<?> handlerClass = Class.forName("dev.deepdaddyttv.deepnullreforged.client.NullWorkbenchClientPayloadHandler");
            handlerClass.getMethod("handleSeedPresetList", SeedPresetListPayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | LinkageError exception) {
            DeepNullReforged.LOGGER.debug("Skipping Null Workbench seed preset list payload handler", exception);
        }
    }

    private static void handleClientSeedApplyResult(SeedApplyResultPayload payload) {
        try {
            Class<?> handlerClass = Class.forName("dev.deepdaddyttv.deepnullreforged.client.NullWorkbenchClientPayloadHandler");
            handlerClass.getMethod("handleSeedApplyResult", SeedApplyResultPayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | LinkageError exception) {
            DeepNullReforged.LOGGER.debug("Skipping Null Workbench seed result payload handler", exception);
        }
    }

    private static List<ResourceLocation> parseLocations(List<String> rawIds) {
        List<ResourceLocation> result = new ArrayList<>();
        for (String rawId : rawIds == null ? List.<String>of() : rawIds) {
            ResourceLocation id = ResourceLocation.tryParse(rawId.trim());
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

    private static void writeStringList(RegistryFriendlyByteBuf buffer, List<String> values) {
        List<String> safeValues = values == null ? List.of() : values;
        buffer.writeVarInt(Math.min(safeValues.size(), 1024));
        for (int i = 0; i < safeValues.size() && i < 1024; i++) {
            buffer.writeUtf(safeValues.get(i));
        }
    }

    private static List<String> readStringList(RegistryFriendlyByteBuf buffer) {
        int size = Math.max(0, Math.min(buffer.readVarInt(), 1024));
        List<String> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(buffer.readUtf());
        }
        return values;
    }

    public record ApplyStylePayload(BlockPos blockPos, int frameColor, int glassColor, boolean reset) implements CustomPacketPayload {
        public static final Type<ApplyStylePayload> TYPE = payloadType("null_workbench_apply_style");
        public static final StreamCodec<RegistryFriendlyByteBuf, ApplyStylePayload> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC,
                        ApplyStylePayload::blockPos,
                        ByteBufCodecs.VAR_INT,
                        ApplyStylePayload::frameColor,
                        ByteBufCodecs.VAR_INT,
                        ApplyStylePayload::glassColor,
                        ByteBufCodecs.BOOL,
                        ApplyStylePayload::reset,
                        ApplyStylePayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TransferRecipePayload(BlockPos blockPos, ResourceLocation resultItemId, boolean maxTransfer) implements CustomPacketPayload {
        public static final Type<TransferRecipePayload> TYPE = payloadType("null_workbench_transfer_recipe");
        public static final StreamCodec<RegistryFriendlyByteBuf, TransferRecipePayload> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC,
                        TransferRecipePayload::blockPos,
                        ResourceLocation.STREAM_CODEC,
                        TransferRecipePayload::resultItemId,
                        ByteBufCodecs.BOOL,
                        TransferRecipePayload::maxTransfer,
                        TransferRecipePayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestSeedPresetPayload(BlockPos blockPos, String presetId) implements CustomPacketPayload {
        public static final Type<RequestSeedPresetPayload> TYPE = payloadType("null_workbench_request_seed_preset");
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestSeedPresetPayload> STREAM_CODEC = StreamCodec.ofMember(RequestSeedPresetPayload::encode, RequestSeedPresetPayload::decode);

        private static RequestSeedPresetPayload decode(RegistryFriendlyByteBuf buffer) {
            return new RequestSeedPresetPayload(buffer.readBlockPos(), buffer.readUtf());
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeBlockPos(blockPos);
            buffer.writeUtf(presetId);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestSeedPresetListPayload(BlockPos blockPos) implements CustomPacketPayload {
        public static final Type<RequestSeedPresetListPayload> TYPE = payloadType("null_workbench_request_seed_preset_list");
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestSeedPresetListPayload> STREAM_CODEC =
                StreamCodec.composite(BlockPos.STREAM_CODEC, RequestSeedPresetListPayload::blockPos, RequestSeedPresetListPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SaveSeedPresetPayload(BlockPos blockPos, String presetId, String displayName, List<NullSeedEntry> entries) implements CustomPacketPayload {
        public static final Type<SaveSeedPresetPayload> TYPE = payloadType("null_workbench_save_seed_preset");
        public static final StreamCodec<RegistryFriendlyByteBuf, SaveSeedPresetPayload> STREAM_CODEC = StreamCodec.ofMember(SaveSeedPresetPayload::encode, SaveSeedPresetPayload::decode);

        public SaveSeedPresetPayload {
            presetId = presetId == null ? "" : presetId;
            displayName = displayName == null || displayName.isBlank() ? "Custom Preset" : displayName;
            entries = List.copyOf(entries == null ? List.of() : entries);
        }

        private static SaveSeedPresetPayload decode(RegistryFriendlyByteBuf buffer) {
            return new SaveSeedPresetPayload(buffer.readBlockPos(), buffer.readUtf(), buffer.readUtf(), NullSeedEntry.readList(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeBlockPos(blockPos);
            buffer.writeUtf(presetId);
            buffer.writeUtf(displayName);
            NullSeedEntry.writeList(buffer, entries);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DeleteSeedPresetPayload(BlockPos blockPos, String presetId) implements CustomPacketPayload {
        public static final Type<DeleteSeedPresetPayload> TYPE = payloadType("null_workbench_delete_seed_preset");
        public static final StreamCodec<RegistryFriendlyByteBuf, DeleteSeedPresetPayload> STREAM_CODEC = StreamCodec.ofMember(DeleteSeedPresetPayload::encode, DeleteSeedPresetPayload::decode);

        public DeleteSeedPresetPayload {
            presetId = presetId == null ? "" : presetId;
        }

        private static DeleteSeedPresetPayload decode(RegistryFriendlyByteBuf buffer) {
            return new DeleteSeedPresetPayload(buffer.readBlockPos(), buffer.readUtf());
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeBlockPos(blockPos);
            buffer.writeUtf(presetId);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SeedPresetListPayload(int containerId, List<NullSeedPresetSummary> presets) implements CustomPacketPayload {
        public static final Type<SeedPresetListPayload> TYPE = payloadType("null_workbench_seed_preset_list");
        public static final StreamCodec<RegistryFriendlyByteBuf, SeedPresetListPayload> STREAM_CODEC = StreamCodec.ofMember(SeedPresetListPayload::encode, SeedPresetListPayload::decode);

        public SeedPresetListPayload {
            presets = List.copyOf(presets == null ? List.of() : presets);
        }

        private static SeedPresetListPayload decode(RegistryFriendlyByteBuf buffer) {
            return new SeedPresetListPayload(buffer.readVarInt(), NullSeedPresetSummary.readList(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            NullSeedPresetSummary.writeList(buffer, presets);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SeedPresetPayload(int containerId, NullSeedPreset preset) implements CustomPacketPayload {
        public static final Type<SeedPresetPayload> TYPE = payloadType("null_workbench_seed_preset");
        public static final StreamCodec<RegistryFriendlyByteBuf, SeedPresetPayload> STREAM_CODEC = StreamCodec.ofMember(SeedPresetPayload::encode, SeedPresetPayload::decode);

        public SeedPresetPayload {
            preset = preset == null ? NullSeedPreset.empty("") : preset;
        }

        private static SeedPresetPayload decode(RegistryFriendlyByteBuf buffer) {
            return new SeedPresetPayload(buffer.readVarInt(), NullSeedPreset.read(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            NullSeedPreset.write(buffer, preset);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ApplySeedConfigPayload(BlockPos blockPos, List<NullSeedEntry> entries, boolean replaceReservations) implements CustomPacketPayload {
        public static final Type<ApplySeedConfigPayload> TYPE = payloadType("null_workbench_apply_seed_config");
        public static final StreamCodec<RegistryFriendlyByteBuf, ApplySeedConfigPayload> STREAM_CODEC = StreamCodec.ofMember(ApplySeedConfigPayload::encode, ApplySeedConfigPayload::decode);

        public ApplySeedConfigPayload {
            entries = List.copyOf(entries == null ? List.of() : entries);
        }

        private static ApplySeedConfigPayload decode(RegistryFriendlyByteBuf buffer) {
            return new ApplySeedConfigPayload(buffer.readBlockPos(), NullSeedEntry.readList(buffer), buffer.readBoolean());
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeBlockPos(blockPos);
            NullSeedEntry.writeList(buffer, entries);
            buffer.writeBoolean(replaceReservations);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SeedApplyResultPayload(int containerId, int applied, int selected, int overflow, int blocked) implements CustomPacketPayload {
        public static final Type<SeedApplyResultPayload> TYPE = payloadType("null_workbench_seed_apply_result");
        public static final StreamCodec<RegistryFriendlyByteBuf, SeedApplyResultPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        SeedApplyResultPayload::containerId,
                        ByteBufCodecs.VAR_INT,
                        SeedApplyResultPayload::applied,
                        ByteBufCodecs.VAR_INT,
                        SeedApplyResultPayload::selected,
                        ByteBufCodecs.VAR_INT,
                        SeedApplyResultPayload::overflow,
                        ByteBufCodecs.VAR_INT,
                        SeedApplyResultPayload::blocked,
                        SeedApplyResultPayload::new
                );

        private static SeedApplyResultPayload from(int containerId, NullSeedPlan plan) {
            return new SeedApplyResultPayload(containerId, plan.appliedCount(), plan.selectedCount(), plan.overflowCount(), plan.blockedCount());
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClearSeedReservationsPayload(BlockPos blockPos) implements CustomPacketPayload {
        public static final Type<ClearSeedReservationsPayload> TYPE = payloadType("null_workbench_clear_seed_reservations");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClearSeedReservationsPayload> STREAM_CODEC =
                StreamCodec.composite(BlockPos.STREAM_CODEC, ClearSeedReservationsPayload::blockPos, ClearSeedReservationsPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
