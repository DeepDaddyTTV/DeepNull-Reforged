package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SelectSlotPacket {
    private final int slot;
    private final Hand hand;

    public SelectSlotPacket(int slot, Hand hand) {
        this.slot = slot;
        this.hand = hand;
    }

    public static void encode(SelectSlotPacket message, PacketBuffer buffer) {
        buffer.writeVarInt(message.slot);
        buffer.writeBoolean(message.hand == Hand.MAIN_HAND);
    }

    public static SelectSlotPacket decode(PacketBuffer buffer) {
        return new SelectSlotPacket(buffer.readVarInt(), buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }

    public static void handle(final SelectSlotPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(new Runnable() {
            @Override
            public void run() {
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;
                ItemStack held = player.getItemInHand(message.hand);
                if (!(held.getItem() instanceof DeepNullItem)) return;
                DeepNullItem item = (DeepNullItem) held.getItem();
                int selected = Math.max(0, Math.min(item.getTier().slots() - 1, message.slot));
                held.getOrCreateTag().putInt(DeepNullItem.SELECTED_SLOT_TAG, selected);
            }
        });
        context.setPacketHandled(true);
    }
}
