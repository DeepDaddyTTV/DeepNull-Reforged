package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record DumpNullPresetTarget(ResourceLocation id, String label) {
    public DumpNullPresetTarget {
        label = label == null || label.isBlank() ? id.toString() : label;
    }

    public static void writeList(RegistryFriendlyByteBuf buffer, List<DumpNullPresetTarget> targets) {
        buffer.writeVarInt(targets.size());
        for (DumpNullPresetTarget target : targets) {
            buffer.writeResourceLocation(target.id);
            buffer.writeUtf(target.label);
        }
    }

    public static List<DumpNullPresetTarget> readList(RegistryFriendlyByteBuf buffer) {
        int count = Math.min(buffer.readVarInt(), 4096);
        List<DumpNullPresetTarget> targets = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            targets.add(new DumpNullPresetTarget(buffer.readResourceLocation(), buffer.readUtf()));
        }
        return targets;
    }
}
