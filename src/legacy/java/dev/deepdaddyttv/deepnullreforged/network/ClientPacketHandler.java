package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    static void apply(ExtendedSlotPacket message) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null || !(player.containerMenu instanceof DeepNullMenu)) return;
        if (player.containerMenu.containerId != message.containerId) return;
        if (message.slot < 0 || message.slot >= player.containerMenu.slots.size()) return;
        player.containerMenu.getSlot(message.slot).set(message.stack);
    }
}
