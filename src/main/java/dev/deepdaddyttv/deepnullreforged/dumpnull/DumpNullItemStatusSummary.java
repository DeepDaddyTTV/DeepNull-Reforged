package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record DumpNullItemStatusSummary(
        ResourceLocation itemId,
        DumpNullItemStatus status,
        boolean advanced,
        boolean presetGenerated
) {
    public DumpNullItemStatusSummary {
        status = status == null ? DumpNullItemStatus.NEUTRAL : status;
    }

    public static void writeList(RegistryFriendlyByteBuf buffer, List<DumpNullItemStatusSummary> summaries) {
        buffer.writeVarInt(Math.min(summaries.size(), 4096));
        for (int i = 0; i < summaries.size() && i < 4096; i++) {
            DumpNullItemStatusSummary summary = summaries.get(i);
            buffer.writeResourceLocation(summary.itemId);
            buffer.writeVarInt(summary.status.ordinal());
            buffer.writeBoolean(summary.advanced);
            buffer.writeBoolean(summary.presetGenerated);
        }
    }

    public static List<DumpNullItemStatusSummary> readList(RegistryFriendlyByteBuf buffer) {
        int count = Math.min(buffer.readVarInt(), 4096);
        List<DumpNullItemStatusSummary> summaries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            summaries.add(new DumpNullItemStatusSummary(
                    buffer.readResourceLocation(),
                    DumpNullItemStatus.byId(buffer.readVarInt()),
                    buffer.readBoolean(),
                    buffer.readBoolean()
            ));
        }
        return summaries;
    }
}
