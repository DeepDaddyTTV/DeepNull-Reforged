package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record DumpNullMobOption(ResourceLocation entityId, String descriptionId, String category, boolean selected) {
    public DumpNullMobOption {
        descriptionId = descriptionId == null || descriptionId.isBlank() ? entityId.toString() : descriptionId;
        category = category == null || category.isBlank() ? "UNKNOWN" : category;
    }

    public static void writeList(RegistryFriendlyByteBuf buffer, List<DumpNullMobOption> options) {
        buffer.writeVarInt(options.size());
        for (DumpNullMobOption option : options) {
            buffer.writeResourceLocation(option.entityId);
            buffer.writeUtf(option.descriptionId);
            buffer.writeUtf(option.category);
            buffer.writeBoolean(option.selected);
        }
    }

    public static List<DumpNullMobOption> readList(RegistryFriendlyByteBuf buffer) {
        int count = Math.min(buffer.readVarInt(), 4096);
        List<DumpNullMobOption> options = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            options.add(new DumpNullMobOption(
                    buffer.readResourceLocation(),
                    buffer.readUtf(),
                    buffer.readUtf(),
                    buffer.readBoolean()
            ));
        }
        return options;
    }
}
