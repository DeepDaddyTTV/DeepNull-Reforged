package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenuOpener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class DeepNullPayloads {
    private DeepNullPayloads() {
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String path) {
        return new CustomPacketPayload.Type<>(DeepNullReforged.id(path));
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(OpenItemMenuPayload.TYPE, OpenItemMenuPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleOpenItemMenu(payload, player);
                    }
                }));
        registrar.playToServer(SetSelectedSlotPayload.TYPE, SetSelectedSlotPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleSetSelected(payload, player);
                    }
                }));
        registrar.playToServer(MenuSlotActionPayload.TYPE, MenuSlotActionPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleMenuSlotAction(payload, player);
                    }
                }));
        registrar.playToServer(MenuReorderPayload.TYPE, MenuReorderPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleMenuReorder(payload, player);
                    }
                }));
        registrar.playToServer(MenuLockPayload.TYPE, MenuLockPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleMenuLock(payload, player);
                    }
                }));
    }

    private static void handleOpenItemMenu(OpenItemMenuPayload payload, ServerPlayer player) {
        DeepNullMenuOpener.openHeldItem(player, player.getInventory(), payload.inventorySlot());
    }

    private static void handleSetSelected(SetSelectedSlotPayload payload, ServerPlayer player) {
        Inventory inventory = player.getInventory();
        if (payload.inventorySlot() < 0 || payload.inventorySlot() >= inventory.getContainerSize()) {
            return;
        }

        ItemStack stack = inventory.getItem(payload.inventorySlot());
        if (!(stack.getItem() instanceof DeepNullItem deepNullItem)) {
            return;
        }

        DeepNullInventory dankInventory = new DeepNullInventory(deepNullItem.tier(), stack, player.level().registryAccess(), null);
        dankInventory.setSelectedSlot(payload.selectedSlot());
    }

    private static void handleMenuSlotAction(MenuSlotActionPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof DeepNullMenu menu)) {
            return;
        }

        boolean changed = switch (MenuSlotAction.fromId(payload.actionId())) {
            case SELECT -> menu.selectStorageSlot(payload.slot());
            case CYCLE_EXTRACTION_FORWARD -> menu.cycleExtractionMode(payload.slot(), true);
            case CYCLE_EXTRACTION_BACKWARD -> menu.cycleExtractionMode(payload.slot(), false);
            case CYCLE_PLACEMENT_FORWARD -> menu.cyclePlacementMode(payload.slot(), true);
            case CYCLE_PLACEMENT_BACKWARD -> menu.cyclePlacementMode(payload.slot(), false);
            case TOGGLE_TAG_MATCHING -> menu.toggleTagMatching(payload.slot());
        };

        if (changed) {
            menu.broadcastChanges();
        }
    }

    private static void handleMenuLock(MenuLockPayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof DeepNullMenu menu && menu.setLocked(payload.locked())) {
            menu.broadcastChanges();
        }
    }

    private static void handleMenuReorder(MenuReorderPayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof DeepNullMenu menu && menu.moveStorageSlot(payload.fromSlot(), payload.toSlot())) {
            menu.broadcastChanges();
        }
    }

    public record OpenItemMenuPayload(int inventorySlot) implements CustomPacketPayload {
        public static final Type<OpenItemMenuPayload> TYPE = payloadType("open_item_menu");
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenItemMenuPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, OpenItemMenuPayload::inventorySlot, OpenItemMenuPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetSelectedSlotPayload(int inventorySlot, int selectedSlot) implements CustomPacketPayload {
        public static final Type<SetSelectedSlotPayload> TYPE = payloadType("set_selected_slot");
        public static final StreamCodec<RegistryFriendlyByteBuf, SetSelectedSlotPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        SetSelectedSlotPayload::inventorySlot,
                        ByteBufCodecs.VAR_INT,
                        SetSelectedSlotPayload::selectedSlot,
                        SetSelectedSlotPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MenuSlotActionPayload(int slot, int actionId) implements CustomPacketPayload {
        public static final Type<MenuSlotActionPayload> TYPE = payloadType("menu_slot_action");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuSlotActionPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        MenuSlotActionPayload::slot,
                        ByteBufCodecs.VAR_INT,
                        MenuSlotActionPayload::actionId,
                        MenuSlotActionPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MenuLockPayload(boolean locked) implements CustomPacketPayload {
        public static final Type<MenuLockPayload> TYPE = payloadType("menu_lock");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuLockPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, MenuLockPayload::locked, MenuLockPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MenuReorderPayload(int fromSlot, int toSlot) implements CustomPacketPayload {
        public static final Type<MenuReorderPayload> TYPE = payloadType("menu_reorder");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuReorderPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        MenuReorderPayload::fromSlot,
                        ByteBufCodecs.VAR_INT,
                        MenuReorderPayload::toSlot,
                        MenuReorderPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum MenuSlotAction {
        SELECT,
        CYCLE_EXTRACTION_FORWARD,
        CYCLE_EXTRACTION_BACKWARD,
        CYCLE_PLACEMENT_FORWARD,
        CYCLE_PLACEMENT_BACKWARD,
        TOGGLE_TAG_MATCHING;

        public static MenuSlotAction fromId(int actionId) {
            MenuSlotAction[] values = values();
            if (actionId < 0 || actionId >= values.length) {
                return SELECT;
            }
            return values[actionId];
        }
    }
}
