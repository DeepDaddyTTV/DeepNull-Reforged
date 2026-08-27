package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullNetwork;
import dev.deepdaddyttv.deepnullreforged.network.SelectSlotPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DeepNullReforged.MODID, value = Dist.CLIENT)
public final class ClientInputEvents {
    private ClientInputEvents() {
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || !Screen.hasShiftDown()) return;
        ItemStack stack = minecraft.player.getMainHandItem();
        if (!(stack.getItem() instanceof DeepNullItem)) return;
        DeepNullItem item = (DeepNullItem) stack.getItem();
        int current = stack.getOrCreateTag().getInt(DeepNullItem.SELECTED_SLOT_TAG);
        int direction = event.getScrollDelta() > 0 ? -1 : 1;
        int selected = (current + direction + item.getTier().slots()) % item.getTier().slots();
        stack.getOrCreateTag().putInt(DeepNullItem.SELECTED_SLOT_TAG, selected);
        DeepNullNetwork.CHANNEL.sendToServer(new SelectSlotPacket(selected, Hand.MAIN_HAND));
        event.setCanceled(true);
    }
}
