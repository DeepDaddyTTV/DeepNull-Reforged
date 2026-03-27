package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.DeepNullDockBlock;
import dev.deepdaddyttv.deepnullreforged.client.render.DeepNullHudState;
import dev.deepdaddyttv.deepnullreforged.inventory.StoneGeneratorVariant;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = DeepNullReforged.MODID, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            DeepNullHudState.clear();
            return;
        }

        DeepNullUpdateChecker.tick(minecraft);
        DeepNullHudState.tick(player);

        if (ClientModEvents.TOGGLE_HUD.consumeClick()) {
            player.sendSystemMessage(hudMessage(DeepNullConfig.toggleHudEnabled()));
        }

        if (ClientModEvents.OPEN_DEEP_NULL.consumeClick()) {
            if (isDeepNullScreen(minecraft.screen)) {
                player.closeContainer();
            } else if (minecraft.screen == null) {
                int inventorySlot = ClientDeepNullAccess.findHotbarDeepNullSlot(player.getInventory());
                if (inventorySlot >= 0) {
                    ClientPacketDistributor.sendToServer(new DeepNullPayloads.OpenItemMenuPayload(inventorySlot));
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
            ClientPacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), held.inventory().getSelectedSlot()));
        }

        if (ClientModEvents.PREVIOUS_ITEM.consumeClick()) {
            held.inventory().cycleSelected(false);
            ClientPacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), held.inventory().getSelectedSlot()));
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null
                || minecraft.screen != null
                || !DeepNullConfig.isShiftScrollSelectionEnabled()
                || !player.isShiftKeyDown()
                || event.getScrollDeltaY() == 0.0D) {
            return;
        }

        ClientDeepNullAccess.HeldDeepNull held = ClientDeepNullAccess.findHeldDeepNull(player);
        if (held == null) {
            return;
        }

        held.inventory().cycleSelected(event.getScrollDeltaY() < 0.0D);
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), held.inventory().getSelectedSlot()));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (event.isUseItem() && handleInvertedDampNullUse(event, minecraft, player)) {
            return;
        }

        if (!event.isPickBlock()) {
            return;
        }

        if (player == null || minecraft.level == null || minecraft.hitResult == null || minecraft.hitResult.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) {
            return;
        }

        ClientDeepNullAccess.HeldDeepNull held = ClientDeepNullAccess.findHeldDeepNull(player);
        if (held == null) {
            return;
        }

        if (held.inventory().isFluidMode()) {
            return;
        }

        net.minecraft.world.phys.BlockHitResult hitResult = (net.minecraft.world.phys.BlockHitResult) minecraft.hitResult;
        net.minecraft.core.BlockPos pos = hitResult.getBlockPos();
        net.minecraft.world.level.block.state.BlockState state = minecraft.level.getBlockState(pos);
        ItemStack targetStack = state.getCloneItemStack(pos, minecraft.level, true, player);
        int slot = held.inventory().findMatchingSlot(targetStack);
        if (slot >= 0) {
            held.inventory().setSelectedSlot(slot);
            ClientPacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), slot));
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        DeepNullHudRenderer.render(event.getGuiGraphics(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onScreenClosing(ScreenEvent.Closing event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        if (!ClientDeepNullJeiSession.shouldReturn(containerScreen.getMenu())) {
            return;
        }

        ClientPacketDistributor.sendToServer(new DeepNullPayloads.CraftingReturnPayload(containerScreen.getMenu().containerId));
        ClientDeepNullJeiSession.clear();
    }

    @SubscribeEvent
    public static void onDeepNullMiddleClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != 2 || !(event.getScreen() instanceof DeepNullScreen screen)) {
            return;
        }
        if (screen.handleBlockedMiddleClick(event.getMouseX(), event.getMouseY())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDeepNullMiddleRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getButton() != 2 || !(event.getScreen() instanceof DeepNullScreen screen)) {
            return;
        }
        if (screen.handleBlockedMiddleRelease(event.getMouseX(), event.getMouseY())) {
            event.setCanceled(true);
        }
    }

    private static boolean handleTransferLockHotkey(Minecraft minecraft, Player player) {
        if (minecraft.screen instanceof DeepNullScreen screen) {
            boolean next = screen.toggleTransferLock();
            player.sendSystemMessage(transferLockMessage(next));
            return true;
        }
        if (minecraft.screen instanceof DeepNullFluidScreen screen) {
            boolean next = screen.toggleTransferLock();
            player.sendSystemMessage(transferLockMessage(next));
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
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.HeldTransferLockPayload(held.inventorySlot(), next));
        player.sendSystemMessage(transferLockMessage(next));
        return true;
    }

    private static void handleAutoPickupHotkey(Player player, ClientDeepNullAccess.HeldDeepNull held) {
        if (held.inventory().isFluidOnly()) {
            return;
        }
        boolean next = !held.inventory().isAutoPickupEnabled();
        held.inventory().setAutoPickupEnabled(next);
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.HeldAutoPickupPayload(held.inventorySlot(), next));
        player.sendSystemMessage(Component.translatable(next ? "dn.auto_pickup_enabled.desc" : "dn.auto_pickup_disabled.desc"));
    }

    private static void handleAutoFeedingHotkey(Player player, ClientDeepNullAccess.HeldDeepNull held) {
        if (!held.inventory().hasAutoFeedingUpgrade()) {
            return;
        }
        boolean next = !held.inventory().isAutoFeedingEnabled();
        held.inventory().setAutoFeedingEnabled(next);
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.HeldAutoFeedingPayload(held.inventorySlot(), next));
        player.sendSystemMessage(Component.translatable(next ? "dn.auto_feeding_enabled.desc" : "dn.auto_feeding_disabled.desc"));
    }

    private static void handleAutoSmeltingHotkey(Player player, ClientDeepNullAccess.HeldDeepNull held) {
        if (!held.inventory().hasAutoSmeltingUpgrade()) {
            return;
        }
        boolean next = !held.inventory().isAutoSmeltingEnabled();
        held.inventory().setAutoSmeltingEnabled(next);
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.HeldAutoSmeltingPayload(held.inventorySlot(), next));
        player.sendSystemMessage(Component.translatable(next ? "dn.auto_smelting_enabled.desc" : "dn.auto_smelting_disabled.desc"));
    }

    private static void handleStoneGeneratorHotkey(Player player, ClientDeepNullAccess.HeldDeepNull held) {
        if (!held.inventory().isFluidOnly() || !held.inventory().hasStoneGeneratorUpgrade()) {
            return;
        }
        StoneGeneratorVariant next = held.inventory().getStoneGeneratorVariant().cycle(true);
        held.inventory().setStoneGeneratorVariant(next);
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.HeldStoneVariantPayload(held.inventorySlot(), next.ordinal()));
        player.sendSystemMessage(Component.translatable("dn.stone_type.desc").append(": ").append(next.displayName()));
    }

    private static boolean handleInvertedDampNullUse(InputEvent.InteractionKeyMappingTriggered event, Minecraft minecraft, Player player) {
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
            event.setCanceled(true);
            event.setSwingHand(false);
            ClientPacketDistributor.sendToServer(new DeepNullPayloads.OpenItemMenuPayload(held.inventorySlot()));
            return true;
        }

        if (!targetingBlock) {
            event.setCanceled(true);
            event.setSwingHand(false);
            return true;
        }
        return false;
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
