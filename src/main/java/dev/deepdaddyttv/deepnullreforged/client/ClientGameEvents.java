package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.DeepNullDockBlock;
import dev.deepdaddyttv.deepnullreforged.client.render.DeepNullHudState;
import dev.deepdaddyttv.deepnullreforged.inventory.StoneGeneratorVariant;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferDirectionMode;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferOutputMode;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockApplyCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import dev.deepdaddyttv.deepnullreforged.compat.network.PacketDistributor;

public final class ClientGameEvents {
    private static boolean initialized;

    private ClientGameEvents() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        ClientTickEvents.END_CLIENT_TICK.register(ClientGameEvents::onClientTick);
        HudElementRegistry.addLast(DeepNullReforged.id("deepnull_hud"), DeepNullHudRenderer::render);
        ClientPickBlockApplyCallback.EVENT.register(ClientGameEvents::onPickBlockApply);
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof DeepNullScreen deepNullScreen)) {
                return;
            }

            ScreenMouseEvents.allowMouseClick(screen).register((currentScreen, event) ->
                    !ClientModEvents.isTertiaryGuiButton(event.button()) || !deepNullScreen.handleBlockedMiddleClick(event.x(), event.y()));
            ScreenMouseEvents.allowMouseRelease(screen).register((currentScreen, event) ->
                    !ClientModEvents.isTertiaryGuiButton(event.button()) || !deepNullScreen.handleBlockedMiddleRelease(event.x(), event.y()));
        });
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->
                ScreenEvents.remove(screen).register(ClientGameEvents::onScreenClosing));
    }

    private static void onClientTick(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null) {
            DeepNullHudState.clear();
            return;
        }

        DeepNullUpdateChecker.tick(minecraft);
        DeepNullHudState.tick(player);

        if (ClientModEvents.TOGGLE_HUD.consumeClick()) {
            player.sendOverlayMessage(hudMessage(DeepNullConfig.toggleHudEnabled()));
        }

        if (ClientModEvents.OPEN_DEEP_NULL.consumeClick()) {
            if (isDeepNullScreen(minecraft.screen)) {
                ((LocalPlayer) player).closeContainer();
            } else if (minecraft.screen == null) {
                int inventorySlot = ClientDeepNullAccess.findHotbarDeepNullSlot(player.getInventory());
                if (inventorySlot >= 0) {
                    PacketDistributor.sendToServer(new DeepNullPayloads.OpenItemMenuPayload(inventorySlot));
                }
            }
        }

        if (ClientModEvents.TOGGLE_TRANSFER_LOCK.consumeClick()) {
            if (handleTransferLockHotkey(minecraft, player)) {
                return;
            }
        }

        if (ClientModEvents.TOGGLE_TRANSFER_DIRECTION.consumeClick()) {
            if (handleTransferDirectionHotkey(minecraft, player)) {
                return;
            }
        }

        if (ClientModEvents.TOGGLE_SPONGE.consumeClick()) {
            if (handleSpongeHotkey(minecraft, player)) {
                return;
            }
        }

        if (minecraft.screen instanceof AbstractContainerScreen<?> containerScreen) {
            ClientDeepNullJeiSession.captureCraftContents(containerScreen.getMenu());
        }

        if (minecraft.screen != null) {
            return;
        }

        if (ClientModEvents.TOGGLE_GLOBAL_AUTO_PICKUP.consumeClick()) {
            PacketDistributor.sendToServer(DeepNullPayloads.ToggleGlobalAutoPickupPayload.INSTANCE);
        }

        ClientDeepNullAccess.HeldDeepNull held = ClientDeepNullAccess.findHeldDeepNull(player);
        if (held == null) {
            return;
        }

        if (ClientModEvents.TOGGLE_AUTO_PICKUP.consumeClick()) {
            handleAutoPickupHotkey(player, held);
        }

        if (ClientModEvents.TOGGLE_AUTO_FEEDING.consumeClick()) {
            handleAutoFeedingHotkey(player, held);
        }

        if (ClientModEvents.TOGGLE_AUTO_SMELTING.consumeClick()) {
            handleAutoSmeltingHotkey(player, held);
        }

        if (ClientModEvents.CYCLE_STONE_GENERATOR.consumeClick()) {
            handleStoneGeneratorHotkey(player, held);
        }

        if (ClientModEvents.NEXT_ITEM.consumeClick()) {
            int selectedSlot = ClientInteractionLogic.cycleSelected(held.inventory(), true);
            PacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), selectedSlot));
        }

        if (ClientModEvents.PREVIOUS_ITEM.consumeClick()) {
            int selectedSlot = ClientInteractionLogic.cycleSelected(held.inventory(), false);
            PacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), selectedSlot));
        }
    }

    public static boolean handleShiftScrollSelection(Player player, double scrollDeltaY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (player == null
                || minecraft.screen != null
                || !DeepNullConfig.isShiftScrollSelectionEnabled()
                || !player.isShiftKeyDown()
                || scrollDeltaY == 0.0D) {
            return false;
        }

        ClientDeepNullAccess.HeldDeepNull held = ClientDeepNullAccess.findHeldDeepNull(player);
        if (held == null) {
            return false;
        }

        int selectedSlot = ClientInteractionLogic.cycleSelected(held.inventory(), scrollDeltaY < 0.0D);
        PacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), selectedSlot));
        return true;
    }

    private static ItemStack onPickBlockApply(Player player, HitResult hitResult, ItemStack pickedStack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (player == null || minecraft.level == null || hitResult == null || pickedStack.isEmpty()) {
            return pickedStack;
        }

        ClientDeepNullAccess.HeldDeepNull held = ClientDeepNullAccess.findHeldDeepNull(player);
        if (held == null || held.inventory().isFluidMode()) {
            return pickedStack;
        }

        int slot = ClientInteractionLogic.pickBlockSlot(held.inventory(), pickedStack);
        if (slot < 0) {
            return pickedStack;
        }

        held.inventory().setSelectedSlot(slot);
        PacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), slot));
        return ItemStack.EMPTY;
    }

    private static void onScreenClosing(net.minecraft.client.gui.screens.Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        if (!ClientDeepNullJeiSession.shouldReturn(containerScreen.getMenu())) {
            return;
        }

        PacketDistributor.sendToServer(new DeepNullPayloads.CraftingReturnPayload(
                containerScreen.getMenu().containerId,
                ClientDeepNullJeiSession.craftingReturnContents(containerScreen.getMenu())
        ));
        ClientDeepNullJeiSession.clear();
    }

    private static boolean handleTransferLockHotkey(Minecraft minecraft, Player player) {
        if (minecraft.screen instanceof DeepNullScreen screen) {
            TransferOutputMode next = screen.toggleTransferOutputMode();
            player.sendOverlayMessage(ClientUiText.transferOutputModeMessage(false, next));
            return true;
        }
        if (minecraft.screen instanceof DeepNullFluidScreen screen) {
            TransferOutputMode next = screen.toggleTransferOutputMode();
            player.sendOverlayMessage(ClientUiText.transferOutputModeMessage(true, next));
            return true;
        }
        if (minecraft.screen != null) {
            return false;
        }

        ClientDeepNullAccess.HeldDeepNull held = ClientDeepNullAccess.findHeldDeepNull(player);
        if (held == null) {
            return false;
        }

        TransferOutputMode next = held.inventory().cycleTransferOutputMode();
        PacketDistributor.sendToServer(new DeepNullPayloads.HeldTransferModePayload(held.inventorySlot(), next.ordinal()));
        player.sendOverlayMessage(ClientUiText.transferOutputModeMessage(held.inventory().isFluidOnly(), next));
        return true;
    }

    private static boolean handleTransferDirectionHotkey(Minecraft minecraft, Player player) {
        if (minecraft.screen instanceof DeepNullScreen screen) {
            TransferDirectionMode next = screen.toggleTransferDirectionMode();
            player.sendOverlayMessage(ClientUiText.transferDirectionModeMessage(false, next));
            return true;
        }
        if (minecraft.screen instanceof DeepNullFluidScreen screen) {
            TransferDirectionMode next = screen.toggleTransferDirectionMode();
            player.sendOverlayMessage(ClientUiText.transferDirectionModeMessage(true, next));
            return true;
        }
        if (minecraft.screen != null) {
            return false;
        }

        ClientDeepNullAccess.HeldDeepNull held = ClientDeepNullAccess.findHeldDeepNull(player);
        if (held == null) {
            return false;
        }

        TransferDirectionMode next = held.inventory().cycleTransferDirectionMode();
        PacketDistributor.sendToServer(new DeepNullPayloads.HeldTransferDirectionPayload(held.inventorySlot(), next.ordinal()));
        player.sendOverlayMessage(ClientUiText.transferDirectionModeMessage(held.inventory().isFluidOnly(), next));
        return true;
    }

    private static boolean handleSpongeHotkey(Minecraft minecraft, Player player) {
        if (minecraft.screen != null && !(minecraft.screen instanceof DeepNullFluidScreen)) {
            return false;
        }

        ClientDeepNullAccess.HeldDeepNull held = ClientDeepNullAccess.findHeldDeepNull(player);
        if (held == null || !held.inventory().isFluidOnly() || !held.inventory().hasSpongeUpgrade()) {
            return false;
        }

        boolean next = !held.inventory().isSpongeEnabled();
        held.inventory().setSpongeEnabled(next);
        PacketDistributor.sendToServer(new DeepNullPayloads.HeldSpongeTogglePayload(held.inventorySlot(), next));
        player.sendOverlayMessage(Component.translatable(next ? "dn.sponge_enabled.desc" : "dn.sponge_disabled.desc"));
        return true;
    }

    public static boolean handleInvertedDampNullUse() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (!DeepNullConfig.isDampNullInteractionInverted() || player == null || minecraft.screen != null) {
            return false;
        }

        ClientDeepNullAccess.HeldDeepNull held = ClientDeepNullAccess.findHeldDeepNull(player);
        if (held == null || !held.inventory().isFluidOnly()) {
            return false;
        }

        boolean targetingBlock = minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK;
        if (targetingBlock && minecraft.level != null && minecraft.hitResult instanceof BlockHitResult blockHitResult
                && minecraft.level.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof DeepNullDockBlock) {
            return false;
        }
        if (!player.isShiftKeyDown()) {
            PacketDistributor.sendToServer(new DeepNullPayloads.OpenItemMenuPayload(held.inventorySlot()));
            return true;
        }

        return !targetingBlock;
    }

    private static void handleAutoPickupHotkey(Player player, ClientDeepNullAccess.HeldDeepNull held) {
        if (held.inventory().isFluidOnly()) {
            return;
        }
        boolean next = !held.inventory().isAutoPickupEnabled();
        held.inventory().setAutoPickupEnabled(next);
        PacketDistributor.sendToServer(new DeepNullPayloads.HeldAutoPickupPayload(held.inventorySlot(), next));
        player.sendOverlayMessage(Component.translatable(next ? "dn.auto_pickup_enabled.desc" : "dn.auto_pickup_disabled.desc"));
    }

    private static void handleAutoFeedingHotkey(Player player, ClientDeepNullAccess.HeldDeepNull held) {
        if (!held.inventory().hasAutoFeedingUpgrade()) {
            return;
        }
        boolean next = !held.inventory().isAutoFeedingEnabled();
        held.inventory().setAutoFeedingEnabled(next);
        PacketDistributor.sendToServer(new DeepNullPayloads.HeldAutoFeedingPayload(held.inventorySlot(), next));
        player.sendOverlayMessage(Component.translatable(next ? "dn.auto_feeding_enabled.desc" : "dn.auto_feeding_disabled.desc"));
    }

    private static void handleAutoSmeltingHotkey(Player player, ClientDeepNullAccess.HeldDeepNull held) {
        if (!held.inventory().hasAutoSmeltingUpgrade()) {
            return;
        }
        boolean next = !held.inventory().isAutoSmeltingEnabled();
        held.inventory().setAutoSmeltingEnabled(next);
        PacketDistributor.sendToServer(new DeepNullPayloads.HeldAutoSmeltingPayload(held.inventorySlot(), next));
        player.sendOverlayMessage(Component.translatable(next ? "dn.auto_smelting_enabled.desc" : "dn.auto_smelting_disabled.desc"));
    }

    private static void handleStoneGeneratorHotkey(Player player, ClientDeepNullAccess.HeldDeepNull held) {
        if (!held.inventory().isFluidOnly() || !held.inventory().hasStoneGeneratorUpgrade()) {
            return;
        }
        StoneGeneratorVariant next = held.inventory().getStoneGeneratorVariant().cycle(true);
        held.inventory().setStoneGeneratorVariant(next);
        PacketDistributor.sendToServer(new DeepNullPayloads.HeldStoneVariantPayload(held.inventorySlot(), next.ordinal()));
        player.sendOverlayMessage(Component.translatable("dn.stone_type.desc").append(": ").append(next.displayName()));
    }

    private static boolean isDeepNullScreen(net.minecraft.client.gui.screens.Screen screen) {
        return screen instanceof DeepNullScreen
                || screen instanceof DeepNullFluidScreen
                || screen instanceof DeepNullUpgradeScreen
                || screen instanceof DeepNullFilterScreen;
    }

    private static Component hudMessage(boolean enabled) {
        return Component.translatable(enabled ? "dn.hud_enabled.desc" : "dn.hud_disabled.desc");
    }
}
