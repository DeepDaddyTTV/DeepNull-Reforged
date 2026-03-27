package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullFilterMode;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.StoneGeneratorVariant;
import dev.deepdaddyttv.deepnullreforged.inventory.StoneworksMaterial;
import dev.deepdaddyttv.deepnullreforged.integration.jei.DeepNullCraftingTransferSupport;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenuOpener;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
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
        registrar.playToServer(MenuStoneVariantPayload.TYPE, MenuStoneVariantPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleMenuStoneVariant(payload, player);
                    }
                }));
        registrar.playToServer(MenuStoneworksAmountPayload.TYPE, MenuStoneworksAmountPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleMenuStoneworksAmount(payload, player);
                    }
                }));
        registrar.playToServer(MenuCustomExtractionPayload.TYPE, MenuCustomExtractionPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleMenuCustomExtraction(payload, player);
                    }
                }));
        registrar.playToServer(MenuStoneworksTogglePayload.TYPE, MenuStoneworksTogglePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleMenuStoneworksToggle(payload, player);
                    }
                }));
        registrar.playToServer(HeldTransferLockPayload.TYPE, HeldTransferLockPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleHeldTransferLock(payload, player);
                    }
                }));
        registrar.playToServer(HeldAutoPickupPayload.TYPE, HeldAutoPickupPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleHeldAutoPickup(payload, player);
                    }
                }));
        registrar.playToServer(HeldAutoFeedingPayload.TYPE, HeldAutoFeedingPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleHeldAutoFeeding(payload, player);
                    }
                }));
        registrar.playToServer(HeldAutoSmeltingPayload.TYPE, HeldAutoSmeltingPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleHeldAutoSmelting(payload, player);
                    }
                }));
        registrar.playToServer(HeldStoneVariantPayload.TYPE, HeldStoneVariantPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleHeldStoneVariant(payload, player);
                    }
                }));
        registrar.playToServer(CraftingTransferPayload.TYPE, CraftingTransferPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleCraftingTransfer(payload, player);
                    }
                }));
        registrar.playToServer(CraftingReturnPayload.TYPE, CraftingReturnPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleCraftingReturn(payload, player);
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

    private static void handleMenuStoneVariant(MenuStoneVariantPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof DeepNullMenu menu)) {
            return;
        }

        if (menu.setStoneGeneratorVariant(StoneGeneratorVariant.byId(payload.variantId()))) {
            menu.broadcastChanges();
        }
    }

    private static void handleMenuStoneworksAmount(MenuStoneworksAmountPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof DeepNullMenu menu)) {
            return;
        }

        if (menu.setStoneworksTargetStacks(payload.amount())) {
            menu.broadcastChanges();
        }
    }

    private static void handleMenuCustomExtraction(MenuCustomExtractionPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof DeepNullMenu menu)) {
            return;
        }

        if (menu.setCustomExtractionMinimum(payload.slot(), payload.amount())) {
            menu.broadcastChanges();
        }
    }

    private static void handleMenuStoneworksToggle(MenuStoneworksTogglePayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof DeepNullMenu menu)) {
            return;
        }

        if (menu.toggleStoneworksMonitoring(StoneworksMaterial.byId(payload.materialId()))) {
            menu.broadcastChanges();
        }
    }

    private static void handleHeldTransferLock(HeldTransferLockPayload payload, ServerPlayer player) {
        withHeldInventory(player, payload.inventorySlot(), inventory -> inventory.setTransferLocked(payload.transferLocked()));
    }

    private static void handleHeldAutoPickup(HeldAutoPickupPayload payload, ServerPlayer player) {
        withHeldInventory(player, payload.inventorySlot(), inventory -> inventory.setAutoPickupEnabled(payload.enabled()));
    }

    private static void handleHeldAutoFeeding(HeldAutoFeedingPayload payload, ServerPlayer player) {
        withHeldInventory(player, payload.inventorySlot(), inventory -> inventory.setAutoFeedingEnabled(payload.enabled()));
    }

    private static void handleHeldAutoSmelting(HeldAutoSmeltingPayload payload, ServerPlayer player) {
        withHeldInventory(player, payload.inventorySlot(), inventory -> inventory.setAutoSmeltingEnabled(payload.enabled()));
    }

    private static void handleHeldStoneVariant(HeldStoneVariantPayload payload, ServerPlayer player) {
        withHeldInventory(player, payload.inventorySlot(), inventory -> inventory.setStoneGeneratorVariant(StoneGeneratorVariant.byId(payload.variantId())));
    }

    private static void withHeldInventory(ServerPlayer player, int inventorySlot, java.util.function.Consumer<DeepNullInventory> action) {
        Inventory inventory = player.getInventory();
        if (inventorySlot < 0 || inventorySlot >= inventory.getContainerSize()) {
            return;
        }

        ItemStack stack = inventory.getItem(inventorySlot);
        if (!(stack.getItem() instanceof DeepNullItem deepNullItem)) {
            return;
        }

        DeepNullInventory deepNullInventory = new DeepNullInventory(deepNullItem.tier(), stack, player.level().registryAccess(), null);
        action.accept(deepNullInventory);
    }

    private static void handleCraftingTransfer(CraftingTransferPayload payload, ServerPlayer player) {
        ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, payload.recipeId());
        RecipeHolder<?> recipeHolder = player.level() instanceof ServerLevel serverLevel
                ? serverLevel.recipeAccess().byKey(recipeKey).orElse(null)
                : null;
        if (!(recipeHolder instanceof RecipeHolder<?> rawHolder) || !(rawHolder.value() instanceof CraftingRecipe)) {
            return;
        }

        @SuppressWarnings("unchecked")
        RecipeHolder<CraftingRecipe> craftingRecipeHolder = (RecipeHolder<CraftingRecipe>) rawHolder;
        DeepNullCraftingTransferSupport.executeTransfer(player.containerMenu, player, craftingRecipeHolder, payload.maxTransfer());
    }

    private static void handleCraftingReturn(CraftingReturnPayload payload, ServerPlayer player) {
        if (player.containerMenu.containerId != payload.containerId()) {
            return;
        }
        DeepNullCraftingTransferSupport.returnCurrentCraftingContents(player.containerMenu, player);
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

    public record MenuStoneVariantPayload(int variantId) implements CustomPacketPayload {
        public static final Type<MenuStoneVariantPayload> TYPE = payloadType("menu_stone_variant");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuStoneVariantPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, MenuStoneVariantPayload::variantId, MenuStoneVariantPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MenuStoneworksAmountPayload(int amount) implements CustomPacketPayload {
        public static final Type<MenuStoneworksAmountPayload> TYPE = payloadType("menu_stoneworks_amount");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuStoneworksAmountPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, MenuStoneworksAmountPayload::amount, MenuStoneworksAmountPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MenuCustomExtractionPayload(int slot, int amount) implements CustomPacketPayload {
        public static final Type<MenuCustomExtractionPayload> TYPE = payloadType("menu_custom_extraction");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuCustomExtractionPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        MenuCustomExtractionPayload::slot,
                        ByteBufCodecs.VAR_INT,
                        MenuCustomExtractionPayload::amount,
                        MenuCustomExtractionPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MenuStoneworksTogglePayload(int materialId) implements CustomPacketPayload {
        public static final Type<MenuStoneworksTogglePayload> TYPE = payloadType("menu_stoneworks_toggle");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuStoneworksTogglePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, MenuStoneworksTogglePayload::materialId, MenuStoneworksTogglePayload::new);

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

    public record HeldAutoPickupPayload(int inventorySlot, boolean enabled) implements CustomPacketPayload {
        public static final Type<HeldAutoPickupPayload> TYPE = payloadType("held_auto_pickup");
        public static final StreamCodec<RegistryFriendlyByteBuf, HeldAutoPickupPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        HeldAutoPickupPayload::inventorySlot,
                        ByteBufCodecs.BOOL,
                        HeldAutoPickupPayload::enabled,
                        HeldAutoPickupPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record HeldAutoFeedingPayload(int inventorySlot, boolean enabled) implements CustomPacketPayload {
        public static final Type<HeldAutoFeedingPayload> TYPE = payloadType("held_auto_feeding");
        public static final StreamCodec<RegistryFriendlyByteBuf, HeldAutoFeedingPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        HeldAutoFeedingPayload::inventorySlot,
                        ByteBufCodecs.BOOL,
                        HeldAutoFeedingPayload::enabled,
                        HeldAutoFeedingPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record HeldAutoSmeltingPayload(int inventorySlot, boolean enabled) implements CustomPacketPayload {
        public static final Type<HeldAutoSmeltingPayload> TYPE = payloadType("held_auto_smelting");
        public static final StreamCodec<RegistryFriendlyByteBuf, HeldAutoSmeltingPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        HeldAutoSmeltingPayload::inventorySlot,
                        ByteBufCodecs.BOOL,
                        HeldAutoSmeltingPayload::enabled,
                        HeldAutoSmeltingPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record HeldStoneVariantPayload(int inventorySlot, int variantId) implements CustomPacketPayload {
        public static final Type<HeldStoneVariantPayload> TYPE = payloadType("held_stone_variant");
        public static final StreamCodec<RegistryFriendlyByteBuf, HeldStoneVariantPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        HeldStoneVariantPayload::inventorySlot,
                        ByteBufCodecs.VAR_INT,
                        HeldStoneVariantPayload::variantId,
                        HeldStoneVariantPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record CraftingTransferPayload(Identifier recipeId, boolean maxTransfer) implements CustomPacketPayload {
        public static final Type<CraftingTransferPayload> TYPE = payloadType("crafting_transfer");
        public static final StreamCodec<RegistryFriendlyByteBuf, CraftingTransferPayload> STREAM_CODEC =
                StreamCodec.composite(
                        Identifier.STREAM_CODEC,
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

    public record CraftingReturnPayload(int containerId) implements CustomPacketPayload {
        public static final Type<CraftingReturnPayload> TYPE = payloadType("crafting_return");
        public static final StreamCodec<RegistryFriendlyByteBuf, CraftingReturnPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, CraftingReturnPayload::containerId, CraftingReturnPayload::new);

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
