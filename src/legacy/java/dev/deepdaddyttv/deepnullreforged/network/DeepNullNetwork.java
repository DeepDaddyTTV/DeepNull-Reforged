package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;

public final class DeepNullNetwork {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            DeepNullReforged.id("main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private DeepNullNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SelectSlotPacket.class, SelectSlotPacket::encode, SelectSlotPacket::decode,
                SelectSlotPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id, ExtendedSlotPacket.class, ExtendedSlotPacket::encode, ExtendedSlotPacket::decode,
                ExtendedSlotPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
