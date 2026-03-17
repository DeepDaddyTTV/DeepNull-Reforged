package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullFilterMode;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.integration.jei.DeepNullCraftingTransferSupport;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenuOpener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
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
        registrar.playToServer(OpenMenuViewPayload.TYPE, OpenMenuViewPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleOpenMenuView(payload, player);
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
        registrar.playToServer(MenuChargingPayload.TYPE, MenuChargingPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleMenuCharging(payload, player);
                    }
                }));
        registrar.playToServer(MenuTransferLockPayload.TYPE, MenuTransferLockPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleMenuTransferLock(payload, player);
                    }
                }));
        registrar.playToServer(MenuFilterModePayload.TYPE, MenuFilterModePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleMenuFilterMode(payload, player);
                    }
                }));
        registrar.playToServer(MenuFilterSlotPayload.TYPE, MenuFilterSlotPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleMenuFilterSlot(payload, player);
                    }
                }));
        registrar.playToServer(HeldTransferLockPayload.TYPE, HeldTransferLockPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleHeldTransferLock(payload, player);
                    }
                }));
        registrar.playToServer(CraftingTransferPayload.TYPE, CraftingTransferPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleCraftingTransfer(payload, player);
                    }
                }));
    }

    private static void handleOpenItemMenu(OpenItemMenuPayload payload, ServerPlayer player) {
        DeepNullMenuOpener.openHeldItem(player, player.getInventory(), payload.inventorySlot());
    }

    private static void handleOpenMenuView(OpenMenuViewPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof DeepNullMenu menu)) {
            return;
        }

        DeepNullMenu.ViewMode viewMode = DeepNullMenu.ViewMode.values()[Math.max(0, Math.min(payload.viewId(), DeepNullMenu.ViewMode.values().length - 1))];
        if (menu.getSourceType() == DeepNullMenu.SourceType.ITEM) {
            syncContentMode(player.getInventory().getItem(menu.getInventorySlot()), player, viewMode);
            DeepNullMenuOpener.openHeldItem(player, player.getInventory(), menu.getInventorySlot(), viewMode);
            return;
        }

        if (menu.getDockPos() != null && player.level().getBlockEntity(menu.getDockPos()) instanceof dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity dock) {
            syncDockContentMode(dock, viewMode);
            DeepNullMenuOpener.openDock(player, dock, viewMode);
        }
    }

    private static void syncContentMode(ItemStack stack, ServerPlayer player, DeepNullMenu.ViewMode viewMode) {
        if (!(stack.getItem() instanceof DeepNullItem deepNullItem)) {
            return;
        }

        DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), stack, player.level().registryAccess(), null);
        if (inventory.isFluidOnly()) {
            inventory.setContentMode(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullContentMode.FLUIDS);
            return;
        }
        inventory.setContentMode(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullContentMode.ITEMS);
    }

    private static void syncDockContentMode(dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity dock, DeepNullMenu.ViewMode viewMode) {
        DeepNullInventory inventory = dock.createInventory();
        if (inventory == null) {
            return;
        }
        inventory.setContentMode(inventory.isFluidOnly()
                ? dev.deepdaddyttv.deepnullreforged.inventory.DeepNullContentMode.FLUIDS
                : dev.deepdaddyttv.deepnullreforged.inventory.DeepNullContentMode.ITEMS);
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
            case CLEAR_FLUID -> menu.clearFluidSlot(payload.slot());
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

    private static void handleMenuCharging(MenuChargingPayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof DeepNullMenu menu && menu.setChargingEnabled(payload.chargingEnabled())) {
            menu.broadcastChanges();
        }
    }

    private static void handleMenuTransferLock(MenuTransferLockPayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof DeepNullMenu menu && menu.setTransferLocked(payload.transferLocked())) {
            menu.broadcastChanges();
        }
    }

    private static void handleMenuReorder(MenuReorderPayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof DeepNullMenu menu && menu.moveStorageSlot(payload.fromSlot(), payload.toSlot())) {
            menu.broadcastChanges();
        }
    }

    private static void handleMenuFilterMode(MenuFilterModePayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof DeepNullMenu menu)) {
            return;
        }

        DeepNullFilterMode mode = DeepNullFilterMode.byId(payload.modeId());
        boolean changed = switch (menu.getViewMode()) {
            case AUTO_SMELT_FILTER -> menu.setAutoSmeltFilterMode(mode);
            case FILTER -> menu.setFilterMode(mode);
            default -> false;
        };
        if (changed) {
            menu.broadcastChanges();
        }
    }

    private static void handleMenuFilterSlot(MenuFilterSlotPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof DeepNullMenu menu)) {
            return;
        }

        boolean changed = switch (menu.getViewMode()) {
            case AUTO_SMELT_FILTER -> menu.setAutoSmeltFilterStack(payload.slot(), payload.stack());
            case FILTER -> menu.setFilterStack(payload.slot(), payload.stack());
            default -> false;
        };
        if (changed) {
            menu.broadcastChanges();
        }
    }

    private static void handleHeldTransferLock(HeldTransferLockPayload payload, ServerPlayer player) {
        Inventory inventory = player.getInventory();
        if (payload.inventorySlot() < 0 || payload.inventorySlot() >= inventory.getContainerSize()) {
            return;
        }

        ItemStack stack = inventory.getItem(payload.inventorySlot());
        if (!(stack.getItem() instanceof DeepNullItem deepNullItem)) {
            return;
        }

        DeepNullInventory deepNullInventory = new DeepNullInventory(deepNullItem.tier(), stack, player.level().registryAccess(), null);
        deepNullInventory.setTransferLocked(payload.transferLocked());
    }

    private static void handleCraftingTransfer(CraftingTransferPayload payload, ServerPlayer player) {
        RecipeHolder<?> recipeHolder = player.serverLevel().getRecipeManager().byKey(payload.recipeId()).orElse(null);
        if (!(recipeHolder instanceof RecipeHolder<?> rawHolder) || !(rawHolder.value() instanceof CraftingRecipe)) {
            return;
        }

        @SuppressWarnings("unchecked")
        RecipeHolder<CraftingRecipe> craftingRecipeHolder = (RecipeHolder<CraftingRecipe>) rawHolder;
        DeepNullCraftingTransferSupport.executeTransfer(player.containerMenu, player, craftingRecipeHolder, payload.maxTransfer());
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

    public record OpenMenuViewPayload(int viewId) implements CustomPacketPayload {
        public static final Type<OpenMenuViewPayload> TYPE = payloadType("open_menu_view");
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenMenuViewPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, OpenMenuViewPayload::viewId, OpenMenuViewPayload::new);

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

    public record MenuChargingPayload(boolean chargingEnabled) implements CustomPacketPayload {
        public static final Type<MenuChargingPayload> TYPE = payloadType("menu_charging");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuChargingPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, MenuChargingPayload::chargingEnabled, MenuChargingPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MenuTransferLockPayload(boolean transferLocked) implements CustomPacketPayload {
        public static final Type<MenuTransferLockPayload> TYPE = payloadType("menu_transfer_lock");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuTransferLockPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, MenuTransferLockPayload::transferLocked, MenuTransferLockPayload::new);

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

    public record MenuFilterModePayload(int modeId) implements CustomPacketPayload {
        public static final Type<MenuFilterModePayload> TYPE = payloadType("menu_filter_mode");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuFilterModePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, MenuFilterModePayload::modeId, MenuFilterModePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MenuFilterSlotPayload(int slot, ItemStack stack) implements CustomPacketPayload {
        public static final Type<MenuFilterSlotPayload> TYPE = payloadType("menu_filter_slot");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuFilterSlotPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        MenuFilterSlotPayload::slot,
                        ItemStack.OPTIONAL_STREAM_CODEC,
                        MenuFilterSlotPayload::stack,
                        MenuFilterSlotPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record HeldTransferLockPayload(int inventorySlot, boolean transferLocked) implements CustomPacketPayload {
        public static final Type<HeldTransferLockPayload> TYPE = payloadType("held_transfer_lock");
        public static final StreamCodec<RegistryFriendlyByteBuf, HeldTransferLockPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        HeldTransferLockPayload::inventorySlot,
                        ByteBufCodecs.BOOL,
                        HeldTransferLockPayload::transferLocked,
                        HeldTransferLockPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record CraftingTransferPayload(ResourceLocation recipeId, boolean maxTransfer) implements CustomPacketPayload {
        public static final Type<CraftingTransferPayload> TYPE = payloadType("crafting_transfer");
        public static final StreamCodec<RegistryFriendlyByteBuf, CraftingTransferPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC,
                        CraftingTransferPayload::recipeId,
                        ByteBufCodecs.BOOL,
                        CraftingTransferPayload::maxTransfer,
                        CraftingTransferPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum MenuSlotAction {
        SELECT,
        CLEAR_FLUID,
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
