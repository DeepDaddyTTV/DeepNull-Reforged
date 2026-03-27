package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.block.DeepNullDockBlock;
import dev.deepdaddyttv.deepnullreforged.client.render.DeepNullHudState;
import dev.deepdaddyttv.deepnullreforged.inventory.StoneGeneratorVariant;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
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
import net.neoforged.neoforge.network.PacketDistributor;

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
        HudRenderCallback.EVENT.register(DeepNullHudRenderer::render);
        ClientPickBlockApplyCallback.EVENT.register(ClientGameEvents::onPickBlockApply);
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
            player.displayClientMessage(hudMessage(DeepNullConfig.toggleHudEnabled()), true);
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

        if (minecraft.screen != null) {
            return;
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
            held.inventory().cycleSelected(true);
            PacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), held.inventory().getSelectedSlot()));
        }

        if (ClientModEvents.PREVIOUS_ITEM.consumeClick()) {
            held.inventory().cycleSelected(false);
            PacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), held.inventory().getSelectedSlot()));
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

        held.inventory().cycleSelected(scrollDeltaY < 0.0D);
        PacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), held.inventory().getSelectedSlot()));
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

        int slot = held.inventory().findMatchingSlot(pickedStack);
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

        PacketDistributor.sendToServer(new DeepNullPayloads.CraftingReturnPayload(containerScreen.getMenu().containerId));
        ClientDeepNullJeiSession.clear();
    }

    private static boolean handleTransferLockHotkey(Minecraft minecraft, Player player) {
        if (minecraft.screen instanceof DeepNullScreen screen) {
            boolean next = screen.toggleTransferLock();
            player.displayClientMessage(transferLockMessage(next), true);
            return true;
        }
        if (minecraft.screen instanceof DeepNullFluidScreen screen) {
            boolean next = screen.toggleTransferLock();
            player.displayClientMessage(transferLockMessage(next), true);
            return true;
        }
        if (minecraft.screen != null) {
            return false;
        }

        ClientDeepNullAccess.HeldDeepNull held = ClientDeepNullAccess.findHeldDeepNull(player);
        if (held == null) {
            return false;
        }

        boolean next = !held.inventory().isTransferLocked();
        held.inventory().setTransferLocked(next);
        PacketDistributor.sendToServer(new DeepNullPayloads.HeldTransferLockPayload(held.inventorySlot(), next));
        player.displayClientMessage(transferLockMessage(next), true);
        return true;
    }

    private static void handleAutoPickupHotkey(Player player, ClientDeepNullAccess.HeldDeepNull held) {
        if (held.inventory().isFluidOnly()) {
            return;
        }
        boolean next = !held.inventory().isAutoPickupEnabled();
        held.inventory().setAutoPickupEnabled(next);
        PacketDistributor.sendToServer(new DeepNullPayloads.HeldAutoPickupPayload(held.inventorySlot(), next));
        player.displayClientMessage(Component.translatable(next ? "dn.auto_pickup_enabled.desc" : "dn.auto_pickup_disabled.desc"), true);
    }

    private static void handleAutoFeedingHotkey(Player player, ClientDeepNullAccess.HeldDeepNull held) {
        if (!held.inventory().hasAutoFeedingUpgrade()) {
            return;
        }
        boolean next = !held.inventory().isAutoFeedingEnabled();
        held.inventory().setAutoFeedingEnabled(next);
        PacketDistributor.sendToServer(new DeepNullPayloads.HeldAutoFeedingPayload(held.inventorySlot(), next));
        player.displayClientMessage(Component.translatable(next ? "dn.auto_feeding_enabled.desc" : "dn.auto_feeding_disabled.desc"), true);
    }

    private static void handleAutoSmeltingHotkey(Player player, ClientDeepNullAccess.HeldDeepNull held) {
        if (!held.inventory().hasAutoSmeltingUpgrade()) {
            return;
        }
        boolean next = !held.inventory().isAutoSmeltingEnabled();
        held.inventory().setAutoSmeltingEnabled(next);
        PacketDistributor.sendToServer(new DeepNullPayloads.HeldAutoSmeltingPayload(held.inventorySlot(), next));
        player.displayClientMessage(Component.translatable(next ? "dn.auto_smelting_enabled.desc" : "dn.auto_smelting_disabled.desc"), true);
    }

    private static void handleStoneGeneratorHotkey(Player player, ClientDeepNullAccess.HeldDeepNull held) {
        if (!held.inventory().isFluidOnly() || !held.inventory().hasStoneGeneratorUpgrade()) {
            return;
        }
        StoneGeneratorVariant next = held.inventory().getStoneGeneratorVariant().cycle(true);
        held.inventory().setStoneGeneratorVariant(next);
        PacketDistributor.sendToServer(new DeepNullPayloads.HeldStoneVariantPayload(held.inventorySlot(), next.ordinal()));
        player.displayClientMessage(Component.translatable("dn.stone_type.desc").append(": ").append(next.displayName()), true);
    }

    private static boolean isDeepNullScreen(net.minecraft.client.gui.screens.Screen screen) {
        return screen instanceof DeepNullScreen
                || screen instanceof DeepNullFluidScreen
                || screen instanceof DeepNullUpgradeScreen
                || screen instanceof DeepNullFilterScreen;
    }

    private static Component transferLockMessage(boolean locked) {
        return Component.translatable(locked ? "dn.transfer_locked.desc" : "dn.transfer_unlocked.desc");
    }

    private static Component hudMessage(boolean enabled) {
        return Component.translatable(enabled ? "dn.hud_enabled.desc" : "dn.hud_disabled.desc");
    }
}
