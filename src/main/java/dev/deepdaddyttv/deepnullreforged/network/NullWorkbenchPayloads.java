package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import dev.deepdaddyttv.deepnullreforged.integration.jei.NullWorkbenchTransferSupport;
import dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

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

    public record TransferRecipePayload(BlockPos blockPos, Identifier resultItemId, boolean maxTransfer) implements CustomPacketPayload {
        public static final Type<TransferRecipePayload> TYPE = payloadType("null_workbench_transfer_recipe");
        public static final StreamCodec<RegistryFriendlyByteBuf, TransferRecipePayload> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC,
                        TransferRecipePayload::blockPos,
                        Identifier.STREAM_CODEC,
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
}
