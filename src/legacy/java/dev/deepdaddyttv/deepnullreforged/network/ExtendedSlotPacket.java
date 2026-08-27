package dev.deepdaddyttv.deepnullreforged.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ExtendedSlotPacket {
    final int containerId;
    final int slot;
    final ItemStack stack;

    public ExtendedSlotPacket(int containerId, int slot, ItemStack stack) {
        this.containerId = containerId;
        this.slot = slot;
        this.stack = stack.copy();
    }

    public static void encode(ExtendedSlotPacket message, PacketBuffer buffer) {
        buffer.writeVarInt(message.containerId);
        buffer.writeVarInt(message.slot);
        int count = message.stack.getCount();
        ItemStack networkStack = message.stack.copy();
        if (!networkStack.isEmpty()) networkStack.setCount(1);
        buffer.writeItemStack(networkStack, false);
        buffer.writeVarInt(count);
    }

    public static ExtendedSlotPacket decode(PacketBuffer buffer) {
        int containerId = buffer.readVarInt();
        int slot = buffer.readVarInt();
        ItemStack stack = buffer.readItem();
        int count = buffer.readVarInt();
        if (!stack.isEmpty()) stack.setCount(count);
        return new ExtendedSlotPacket(containerId, slot, stack);
    }

    public static void handle(final ExtendedSlotPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(new Runnable() {
            @Override
            public void run() {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.apply(message));
            }
        });
        context.setPacketHandled(true);
    }
}
