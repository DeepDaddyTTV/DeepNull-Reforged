package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.client.render.DeepNullHudState;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.minecraft.client.Minecraft;
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

        DeepNullHudState.tick(player);

        if (ClientModEvents.OPEN_DEEP_NULL.consumeClick()) {
            int inventorySlot = ClientDeepNullAccess.findFirstDeepNullSlot(player.getInventory());
            if (inventorySlot >= 0) {
                PacketDistributor.sendToServer(new DeepNullPayloads.OpenItemMenuPayload(inventorySlot));
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
}
