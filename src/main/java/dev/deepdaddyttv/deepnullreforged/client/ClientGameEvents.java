package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.client.render.DeepNullHudState;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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

        if (ClientModEvents.OPEN_DEEP_NULL.consumeClick()) {
            if (isDeepNullScreen(minecraft.screen)) {
                player.closeContainer();
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

        if (ClientModEvents.NEXT_ITEM.consumeClick()) {
            held.inventory().cycleSelected(true);
            PacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), held.inventory().getSelectedSlot()));
        }

        if (ClientModEvents.PREVIOUS_ITEM.consumeClick()) {
            held.inventory().cycleSelected(false);
            PacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), held.inventory().getSelectedSlot()));
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null || !player.isShiftKeyDown() || event.getScrollDeltaY() == 0.0D) {
            return;
        }

        ClientDeepNullAccess.HeldDeepNull held = ClientDeepNullAccess.findHeldDeepNull(player);
        if (held == null) {
            return;
        }

        held.inventory().cycleSelected(event.getScrollDeltaY() < 0.0D);
        PacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), held.inventory().getSelectedSlot()));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isPickBlock()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
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
        ItemStack targetStack = state.getCloneItemStack(minecraft.hitResult, minecraft.level, pos, player);
        int slot = held.inventory().findMatchingSlot(targetStack);
        if (slot >= 0) {
            held.inventory().setSelectedSlot(slot);
            PacketDistributor.sendToServer(new DeepNullPayloads.SetSelectedSlotPayload(held.inventorySlot(), slot));
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        DeepNullHudRenderer.render(event.getGuiGraphics(), event.getPartialTick());
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

    private static boolean isDeepNullScreen(net.minecraft.client.gui.screens.Screen screen) {
        return screen instanceof DeepNullScreen
                || screen instanceof DeepNullFluidScreen
                || screen instanceof DeepNullUpgradeScreen
                || screen instanceof DeepNullFilterScreen;
    }

    private static Component transferLockMessage(boolean locked) {
        return Component.translatable(locked ? "dn.transfer_locked.desc" : "dn.transfer_unlocked.desc");
    }
}
