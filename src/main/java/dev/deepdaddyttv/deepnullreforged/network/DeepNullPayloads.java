package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullFilterMode;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.NullSlotDomain;
import dev.deepdaddyttv.deepnullreforged.inventory.NullStorageAction;
import dev.deepdaddyttv.deepnullreforged.inventory.ItemExtractionMode;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import dev.deepdaddyttv.deepnullreforged.inventory.StoneGeneratorVariant;
import dev.deepdaddyttv.deepnullreforged.inventory.StoneworksMaterial;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferDirectionMode;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferOutputMode;
import dev.deepdaddyttv.deepnullreforged.integration.jei.DeepNullCraftingTransferSupport;
import dev.deepdaddyttv.deepnullreforged.integration.jei.ServerDeepNullJeiSession;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenuOpener;
import dev.deepdaddyttv.deepnullreforged.player.DeepNullPlayerState;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.fluids.FluidStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public final class DeepNullPayloads {
    private static final int MAX_TANK_CONTENTS = 256;
    private DeepNullPayloads() {
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String path) {
        return new CustomPacketPayload.Type<>(DeepNullReforged.id(path));
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("2");
        registrar.playToClient(FluidContentsPayload.TYPE, FluidContentsPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientFluidContents(payload)));
        registrar.playToClient(StorageActionResultPayload.TYPE, StorageActionResultPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientStorageActionResult(payload)));
        registrar.playToClient(ExtractionEditResultPayload.TYPE, ExtractionEditResultPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientExtractionEditResult(payload)));
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
        registrar.playToServer(StorageActionRequestPayload.TYPE, StorageActionRequestPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleStorageActionRequest(payload, player);
                    }
                }));
        registrar.playToServer(ExtractionEditBeginPayload.TYPE, ExtractionEditBeginPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleExtractionEditBegin(payload, player);
                    }
                }));
        registrar.playToServer(ExtractionEditSetPayload.TYPE, ExtractionEditSetPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleExtractionEditSet(payload, player);
                    }
                }));
        registrar.playToServer(ExtractionEditUndoPayload.TYPE, ExtractionEditUndoPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleExtractionEditUndo(payload, player);
                    }
                }));
        registrar.playToServer(ExtractionEditInvalidatePayload.TYPE, ExtractionEditInvalidatePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleExtractionEditInvalidate(payload, player);
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
        registrar.playToServer(MenuTransferModePayload.TYPE, MenuTransferModePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleMenuTransferMode(payload, player);
                    }
                }));
        registrar.playToServer(MenuTransferDirectionPayload.TYPE, MenuTransferDirectionPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleMenuTransferDirection(payload, player);
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
        registrar.playToServer(HeldTransferModePayload.TYPE, HeldTransferModePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleHeldTransferMode(payload, player);
                    }
                }));
        registrar.playToServer(HeldTransferDirectionPayload.TYPE, HeldTransferDirectionPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleHeldTransferDirection(payload, player);
                    }
                }));
        registrar.playToServer(HeldSpongeTogglePayload.TYPE, HeldSpongeTogglePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleHeldSpongeToggle(payload, player);
                    }
                }));
        registrar.playToServer(HeldAutoPickupPayload.TYPE, HeldAutoPickupPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleHeldAutoPickup(payload, player);
                    }
                }));
        registrar.playToServer(ToggleGlobalAutoPickupPayload.TYPE, ToggleGlobalAutoPickupPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleToggleGlobalAutoPickup(player);
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

        MenuSlotAction action = MenuSlotAction.fromId(payload.actionId());
        if (action == null) {
            return;
        }
        boolean changed = switch (action) {
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

    private static void handleMenuTransferMode(MenuTransferModePayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof DeepNullMenu menu && menu.setTransferOutputMode(TransferOutputMode.byId(payload.modeId()))) {
            menu.broadcastChanges();
        }
    }

    private static void handleMenuTransferDirection(MenuTransferDirectionPayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof DeepNullMenu menu && menu.setTransferDirectionMode(TransferDirectionMode.byId(payload.modeId()))) {
            menu.broadcastChanges();
        }
    }

    private static void handleMenuReorder(MenuReorderPayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof DeepNullMenu menu && menu.moveStorageSlot(payload.fromSlot(), payload.toSlot())) {
            menu.broadcastChanges();
        }
    }

    private static void handleStorageActionRequest(StorageActionRequestPayload payload, ServerPlayer player) {
        boolean success = false;
        NullSlotDomain domain = NullSlotDomain.byId(payload.domainId());
        NullStorageAction action = NullStorageAction.byId(payload.actionId());
        if (player.containerMenu instanceof DeepNullMenu menu
                && menu.containerId == payload.containerId()
                && menu.hasCurrentSourceIdentity()
                && domain != null
                && action != null
                && menu.acceptStorageActionNonce(payload.nonce())) {
            success = switch (action) {
                case SWAP -> menu.moveStorageSlot(domain, payload.sourceSlot(), payload.targetSlot());
                case MERGE -> menu.mergeStorageSlot(domain, payload.sourceSlot(), payload.targetSlot());
                case CLEAR -> payload.sourceSlot() == payload.targetSlot()
                        && menu.clearStorageSlot(domain, payload.targetSlot());
                case SORT -> domain == NullSlotDomain.ITEM_STORAGE
                        && payload.targetSlot() >= 0
                        && payload.targetSlot() < menu.getStorageSlotCount()
                        && menu.getDankInventory().getStackInSlot(payload.targetSlot()).isEmpty()
                        && menu.compactItemStorage();
                case SELECT -> menu.selectStorageSlot(domain, payload.targetSlot());
                case CYCLE_FORWARD -> domain == NullSlotDomain.ITEM_STORAGE
                        && menu.cycleExtractionMode(payload.targetSlot(), true);
                case CYCLE_BACKWARD -> domain == NullSlotDomain.ITEM_STORAGE
                        && menu.cycleExtractionMode(payload.targetSlot(), false);
            };
            if (success) {
                menu.broadcastChanges();
            }
        }

        PacketDistributor.sendToPlayer(player, new StorageActionResultPayload(
                payload.containerId(),
                payload.nonce(),
                payload.actionId(),
                payload.domainId(),
                payload.sourceSlot(),
                payload.targetSlot(),
                success
        ));
    }

    private static void handleClientStorageActionResult(StorageActionResultPayload payload) {
        try {
            Class<?> handler = Class.forName("dev.deepdaddyttv.deepnullreforged.client.DeepNullClientPayloadHandler");
            handler.getMethod("handleStorageActionResult", StorageActionResultPayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    public static void sendFluidContents(ServerPlayer player, int containerId, List<FluidStack> fluids, List<StoredChemical> chemicals) {
        PacketDistributor.sendToPlayer(player, new FluidContentsPayload(containerId, fluids, chemicals));
    }

    private static void handleClientFluidContents(FluidContentsPayload payload) {
        try {
            Class<?> handler = Class.forName("dev.deepdaddyttv.deepnullreforged.client.DeepNullClientPayloadHandler");
            handler.getMethod("handleFluidContents", FluidContentsPayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    private static void handleExtractionEditBegin(ExtractionEditBeginPayload payload, ServerPlayer player) {
        boolean success = player.containerMenu instanceof DeepNullMenu menu
                && menu.containerId == payload.containerId()
                && menu.beginExtractionEdit(payload.editId(), payload.slot(), payload.applyAll());
        sendExtractionEditResult(player, payload.containerId(), payload.editId(), ExtractionEditOperation.BEGIN, success);
    }

    private static void handleExtractionEditSet(ExtractionEditSetPayload payload, ServerPlayer player) {
        boolean success = false;
        if (player.containerMenu instanceof DeepNullMenu menu && menu.containerId == payload.containerId()) {
            ItemExtractionMode mode = ItemExtractionMode.byProtocolId(payload.modeId());
            success = mode != null && menu.setExtractionEdit(payload.editId(), mode, payload.customAmount());
            if (success) {
                menu.broadcastChanges();
            }
        }
        sendExtractionEditResult(player, payload.containerId(), payload.editId(), ExtractionEditOperation.SET, success);
    }

    private static void handleExtractionEditUndo(ExtractionEditUndoPayload payload, ServerPlayer player) {
        boolean success = false;
        if (player.containerMenu instanceof DeepNullMenu menu && menu.containerId == payload.containerId()) {
            success = menu.undoExtractionEdit(payload.editId());
            if (success) {
                menu.broadcastChanges();
            }
        }
        sendExtractionEditResult(player, payload.containerId(), payload.editId(), ExtractionEditOperation.UNDO, success);
    }

    private static void handleExtractionEditInvalidate(ExtractionEditInvalidatePayload payload, ServerPlayer player) {
        boolean success = player.containerMenu instanceof DeepNullMenu menu
                && menu.containerId == payload.containerId()
                && menu.invalidateExtractionEdit(payload.editId());
        sendExtractionEditResult(player, payload.containerId(), payload.editId(), ExtractionEditOperation.INVALIDATE, success);
    }

    private static void sendExtractionEditResult(
            ServerPlayer player,
            int containerId,
            long editId,
            ExtractionEditOperation operation,
            boolean success
    ) {
        PacketDistributor.sendToPlayer(player, new ExtractionEditResultPayload(containerId, editId, operation.id(), success));
    }

    private static void handleClientExtractionEditResult(ExtractionEditResultPayload payload) {
        try {
            Class<?> handler = Class.forName("dev.deepdaddyttv.deepnullreforged.client.DeepNullClientPayloadHandler");
            handler.getMethod("handleExtractionEditResult", ExtractionEditResultPayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
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

        if (menu.setCustomExtractionMinimum(payload.slot(), payload.amount(), payload.applyAll())) {
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

    private static void handleHeldTransferMode(HeldTransferModePayload payload, ServerPlayer player) {
        withHeldInventory(player, payload.inventorySlot(), inventory -> inventory.setTransferOutputMode(TransferOutputMode.byId(payload.modeId())));
    }

    private static void handleHeldTransferDirection(HeldTransferDirectionPayload payload, ServerPlayer player) {
        withHeldInventory(player, payload.inventorySlot(), inventory -> inventory.setTransferDirectionMode(TransferDirectionMode.byId(payload.modeId())));
    }

    private static void handleHeldSpongeToggle(HeldSpongeTogglePayload payload, ServerPlayer player) {
        withHeldInventory(player, payload.inventorySlot(), inventory -> inventory.setSpongeEnabled(payload.enabled()));
    }

    private static void handleHeldAutoPickup(HeldAutoPickupPayload payload, ServerPlayer player) {
        withHeldInventory(player, payload.inventorySlot(), inventory -> inventory.setAutoPickupEnabled(payload.enabled()));
    }

    private static void handleToggleGlobalAutoPickup(ServerPlayer player) {
        boolean next = DeepNullPlayerState.toggleGlobalAutoPickup(player);
        player.sendSystemMessage(Component.translatable(next
                ? "dn.global_auto_pickup_enabled.desc"
                : "dn.global_auto_pickup_disabled.desc"));
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
        if (!ServerDeepNullJeiSession.shouldReturn(player, payload.containerId())) {
            return;
        }

        boolean returnedFromGrid = false;
        if (player.containerMenu.containerId == payload.containerId()) {
            returnedFromGrid = DeepNullCraftingTransferSupport.returnCurrentCraftingContents(player.containerMenu, player);
        }
        if (!returnedFromGrid && !payload.craftContents().isEmpty()) {
            DeepNullCraftingTransferSupport.returnCraftingSnapshotContents(player, payload.containerId(), payload.craftContents());
        }
        ServerDeepNullJeiSession.clear(player);
    }

    public record FluidContentsPayload(
            int containerId,
            List<FluidStack> fluids,
            List<StoredChemical> chemicals
    ) implements CustomPacketPayload {
        public static final Type<FluidContentsPayload> TYPE = payloadType("deep_null_fluid_contents");
        public static final StreamCodec<RegistryFriendlyByteBuf, FluidContentsPayload> STREAM_CODEC =
                StreamCodec.ofMember(FluidContentsPayload::encode, FluidContentsPayload::decode);

        private static FluidContentsPayload decode(RegistryFriendlyByteBuf buffer) {
            return new FluidContentsPayload(buffer.readVarInt(), readFluidStacks(buffer), readChemicalStacks(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            writeFluidStacks(buffer, fluids);
            writeChemicalStacks(buffer, chemicals);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private static List<FluidStack> readFluidStacks(RegistryFriendlyByteBuf buffer) {
        int encodedCount = Math.max(0, buffer.readVarInt());
        int acceptedCount = Math.min(encodedCount, MAX_TANK_CONTENTS);
        List<FluidStack> fluids = new ArrayList<>(acceptedCount);
        for (int index = 0; index < encodedCount; index++) {
            FluidStack fluidStack = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            if (index < acceptedCount) {
                fluids.add(fluidStack.isEmpty() ? FluidStack.EMPTY : fluidStack.copy());
            }
        }
        return List.copyOf(fluids);
    }

    private static void writeFluidStacks(RegistryFriendlyByteBuf buffer, List<FluidStack> fluids) {
        int encodedCount = Math.min(fluids == null ? 0 : fluids.size(), MAX_TANK_CONTENTS);
        buffer.writeVarInt(encodedCount);
        for (int index = 0; index < encodedCount; index++) {
            FluidStack fluidStack = fluids.get(index);
            FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluidStack == null ? FluidStack.EMPTY : fluidStack);
        }
    }

    private static List<StoredChemical> readChemicalStacks(RegistryFriendlyByteBuf buffer) {
        int encodedCount = Math.max(0, buffer.readVarInt());
        int acceptedCount = Math.min(encodedCount, MAX_TANK_CONTENTS);
        List<StoredChemical> chemicals = new ArrayList<>(acceptedCount);
        for (int index = 0; index < encodedCount; index++) {
            StoredChemical chemical = readChemical(buffer);
            if (index < acceptedCount) {
                chemicals.add(chemical.copy());
            }
        }
        return List.copyOf(chemicals);
    }

    private static void writeChemicalStacks(RegistryFriendlyByteBuf buffer, List<StoredChemical> chemicals) {
        int encodedCount = Math.min(chemicals == null ? 0 : chemicals.size(), MAX_TANK_CONTENTS);
        buffer.writeVarInt(encodedCount);
        for (int index = 0; index < encodedCount; index++) {
            writeChemical(buffer, chemicals.get(index));
        }
    }

    private static StoredChemical readChemical(RegistryFriendlyByteBuf buffer) {
        StoredChemical chemical = new StoredChemical(
                buffer.readUtf(),
                buffer.readVarLong(),
                buffer.readUtf(),
                buffer.readInt(),
                buffer.readUtf(),
                buffer.readBoolean()
        );
        return chemical.isEmpty() ? StoredChemical.EMPTY : chemical;
    }

    private static void writeChemical(RegistryFriendlyByteBuf buffer, StoredChemical chemical) {
        StoredChemical value = chemical == null ? StoredChemical.EMPTY : chemical;
        buffer.writeUtf(value.chemicalId());
        buffer.writeVarLong(Math.max(0L, value.amount()));
        buffer.writeUtf(value.iconPath());
        buffer.writeInt(value.tint());
        buffer.writeUtf(value.translationKey());
        buffer.writeBoolean(value.gaseous());
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

    public record MenuTransferModePayload(int modeId) implements CustomPacketPayload {
        public static final Type<MenuTransferModePayload> TYPE = payloadType("menu_transfer_mode");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuTransferModePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, MenuTransferModePayload::modeId, MenuTransferModePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MenuTransferDirectionPayload(int modeId) implements CustomPacketPayload {
        public static final Type<MenuTransferDirectionPayload> TYPE = payloadType("menu_transfer_direction");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuTransferDirectionPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, MenuTransferDirectionPayload::modeId, MenuTransferDirectionPayload::new);

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

    public record StorageActionRequestPayload(
            int containerId,
            long nonce,
            int actionId,
            int domainId,
            int sourceSlot,
            int targetSlot
    ) implements CustomPacketPayload {
        public static final Type<StorageActionRequestPayload> TYPE = payloadType("storage_action_request");
        public static final StreamCodec<RegistryFriendlyByteBuf, StorageActionRequestPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        StorageActionRequestPayload::containerId,
                        ByteBufCodecs.VAR_LONG,
                        StorageActionRequestPayload::nonce,
                        ByteBufCodecs.VAR_INT,
                        StorageActionRequestPayload::actionId,
                        ByteBufCodecs.VAR_INT,
                        StorageActionRequestPayload::domainId,
                        ByteBufCodecs.VAR_INT,
                        StorageActionRequestPayload::sourceSlot,
                        ByteBufCodecs.VAR_INT,
                        StorageActionRequestPayload::targetSlot,
                        StorageActionRequestPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record StorageActionResultPayload(
            int containerId,
            long nonce,
            int actionId,
            int domainId,
            int sourceSlot,
            int targetSlot,
            boolean success
    ) implements CustomPacketPayload {
        public static final Type<StorageActionResultPayload> TYPE = payloadType("storage_action_result");
        public static final StreamCodec<RegistryFriendlyByteBuf, StorageActionResultPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        StorageActionResultPayload::containerId,
                        ByteBufCodecs.VAR_LONG,
                        StorageActionResultPayload::nonce,
                        ByteBufCodecs.VAR_INT,
                        StorageActionResultPayload::actionId,
                        ByteBufCodecs.VAR_INT,
                        StorageActionResultPayload::domainId,
                        ByteBufCodecs.VAR_INT,
                        StorageActionResultPayload::sourceSlot,
                        ByteBufCodecs.VAR_INT,
                        StorageActionResultPayload::targetSlot,
                        ByteBufCodecs.BOOL,
                        StorageActionResultPayload::success,
                        StorageActionResultPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ExtractionEditBeginPayload(int containerId, long editId, int slot, boolean applyAll) implements CustomPacketPayload {
        public static final Type<ExtractionEditBeginPayload> TYPE = payloadType("extraction_edit_begin");
        public static final StreamCodec<RegistryFriendlyByteBuf, ExtractionEditBeginPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, ExtractionEditBeginPayload::containerId,
                        ByteBufCodecs.VAR_LONG, ExtractionEditBeginPayload::editId,
                        ByteBufCodecs.VAR_INT, ExtractionEditBeginPayload::slot,
                        ByteBufCodecs.BOOL, ExtractionEditBeginPayload::applyAll,
                        ExtractionEditBeginPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ExtractionEditSetPayload(int containerId, long editId, int modeId, int customAmount) implements CustomPacketPayload {
        public static final Type<ExtractionEditSetPayload> TYPE = payloadType("extraction_edit_set");
        public static final StreamCodec<RegistryFriendlyByteBuf, ExtractionEditSetPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, ExtractionEditSetPayload::containerId,
                        ByteBufCodecs.VAR_LONG, ExtractionEditSetPayload::editId,
                        ByteBufCodecs.VAR_INT, ExtractionEditSetPayload::modeId,
                        ByteBufCodecs.VAR_INT, ExtractionEditSetPayload::customAmount,
                        ExtractionEditSetPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ExtractionEditUndoPayload(int containerId, long editId) implements CustomPacketPayload {
        public static final Type<ExtractionEditUndoPayload> TYPE = payloadType("extraction_edit_undo");
        public static final StreamCodec<RegistryFriendlyByteBuf, ExtractionEditUndoPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, ExtractionEditUndoPayload::containerId,
                        ByteBufCodecs.VAR_LONG, ExtractionEditUndoPayload::editId,
                        ExtractionEditUndoPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ExtractionEditInvalidatePayload(int containerId, long editId) implements CustomPacketPayload {
        public static final Type<ExtractionEditInvalidatePayload> TYPE = payloadType("extraction_edit_invalidate");
        public static final StreamCodec<RegistryFriendlyByteBuf, ExtractionEditInvalidatePayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, ExtractionEditInvalidatePayload::containerId,
                        ByteBufCodecs.VAR_LONG, ExtractionEditInvalidatePayload::editId,
                        ExtractionEditInvalidatePayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ExtractionEditResultPayload(int containerId, long editId, int operationId, boolean success) implements CustomPacketPayload {
        public static final Type<ExtractionEditResultPayload> TYPE = payloadType("extraction_edit_result");
        public static final StreamCodec<RegistryFriendlyByteBuf, ExtractionEditResultPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, ExtractionEditResultPayload::containerId,
                        ByteBufCodecs.VAR_LONG, ExtractionEditResultPayload::editId,
                        ByteBufCodecs.VAR_INT, ExtractionEditResultPayload::operationId,
                        ByteBufCodecs.BOOL, ExtractionEditResultPayload::success,
                        ExtractionEditResultPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum ExtractionEditOperation {
        BEGIN(1),
        SET(2),
        UNDO(3),
        INVALIDATE(4);

        private final int id;

        ExtractionEditOperation(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public static ExtractionEditOperation byId(int id) {
            for (ExtractionEditOperation operation : values()) {
                if (operation.id == id) {
                    return operation;
                }
            }
            return null;
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

    public record MenuCustomExtractionPayload(int slot, int amount, boolean applyAll) implements CustomPacketPayload {
        public static final Type<MenuCustomExtractionPayload> TYPE = payloadType("menu_custom_extraction");
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuCustomExtractionPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        MenuCustomExtractionPayload::slot,
                        ByteBufCodecs.VAR_INT,
                        MenuCustomExtractionPayload::amount,
                        ByteBufCodecs.BOOL,
                        MenuCustomExtractionPayload::applyAll,
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

    public record HeldTransferModePayload(int inventorySlot, int modeId) implements CustomPacketPayload {
        public static final Type<HeldTransferModePayload> TYPE = payloadType("held_transfer_mode");
        public static final StreamCodec<RegistryFriendlyByteBuf, HeldTransferModePayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        HeldTransferModePayload::inventorySlot,
                        ByteBufCodecs.VAR_INT,
                        HeldTransferModePayload::modeId,
                        HeldTransferModePayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record HeldTransferDirectionPayload(int inventorySlot, int modeId) implements CustomPacketPayload {
        public static final Type<HeldTransferDirectionPayload> TYPE = payloadType("held_transfer_direction");
        public static final StreamCodec<RegistryFriendlyByteBuf, HeldTransferDirectionPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        HeldTransferDirectionPayload::inventorySlot,
                        ByteBufCodecs.VAR_INT,
                        HeldTransferDirectionPayload::modeId,
                        HeldTransferDirectionPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record HeldSpongeTogglePayload(int inventorySlot, boolean enabled) implements CustomPacketPayload {
        public static final Type<HeldSpongeTogglePayload> TYPE = payloadType("held_sponge_toggle");
        public static final StreamCodec<RegistryFriendlyByteBuf, HeldSpongeTogglePayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        HeldSpongeTogglePayload::inventorySlot,
                        ByteBufCodecs.BOOL,
                        HeldSpongeTogglePayload::enabled,
                        HeldSpongeTogglePayload::new
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

    public static final class ToggleGlobalAutoPickupPayload implements CustomPacketPayload {
        public static final ToggleGlobalAutoPickupPayload INSTANCE = new ToggleGlobalAutoPickupPayload();
        public static final Type<ToggleGlobalAutoPickupPayload> TYPE = payloadType("toggle_global_auto_pickup");
        public static final StreamCodec<RegistryFriendlyByteBuf, ToggleGlobalAutoPickupPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        private ToggleGlobalAutoPickupPayload() {
        }

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

    public record CraftingReturnPayload(int containerId, java.util.List<ItemStack> craftContents) implements CustomPacketPayload {
        public static final Type<CraftingReturnPayload> TYPE = payloadType("crafting_return");
        public static final StreamCodec<RegistryFriendlyByteBuf, CraftingReturnPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        CraftingReturnPayload::containerId,
                        ItemStack.OPTIONAL_LIST_STREAM_CODEC,
                        CraftingReturnPayload::craftContents,
                        CraftingReturnPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum MenuSlotAction {
        SELECT(1),
        CLEAR_FLUID(2),
        CYCLE_EXTRACTION_FORWARD(3),
        CYCLE_EXTRACTION_BACKWARD(4),
        CYCLE_PLACEMENT_FORWARD(5),
        CYCLE_PLACEMENT_BACKWARD(6),
        TOGGLE_TAG_MATCHING(7);

        private final int id;

        MenuSlotAction(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public static MenuSlotAction fromId(int actionId) {
            for (MenuSlotAction action : values()) {
                if (action.id == actionId) {
                    return action;
                }
            }
            return null;
        }
    }
}
