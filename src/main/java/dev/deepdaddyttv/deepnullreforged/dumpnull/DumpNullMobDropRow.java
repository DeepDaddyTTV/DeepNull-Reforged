package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record DumpNullMobDropRow(ResourceLocation entityId, List<DumpNullDropCandidate> drops) {
    public DumpNullMobDropRow {
        drops = List.copyOf(drops == null ? List.of() : drops);
    }

    public static void writeList(RegistryFriendlyByteBuf buffer, List<DumpNullMobDropRow> rows) {
        buffer.writeVarInt(Math.min(rows.size(), 128));
        for (int i = 0; i < rows.size() && i < 128; i++) {
            DumpNullMobDropRow row = rows.get(i);
            buffer.writeResourceLocation(row.entityId);
            DumpNullDropCandidate.writeList(buffer, row.drops);
        }
    }

    public static List<DumpNullMobDropRow> readList(RegistryFriendlyByteBuf buffer) {
        int count = Math.min(buffer.readVarInt(), 128);
        List<DumpNullMobDropRow> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rows.add(new DumpNullMobDropRow(
                    buffer.readResourceLocation(),
                    DumpNullDropCandidate.readList(buffer)
            ));
        }
        return rows;
    }
}
