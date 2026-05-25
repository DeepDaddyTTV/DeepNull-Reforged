package dev.deepdaddyttv.deepnullreforged.nullseed;

import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record NullSeedEntry(
        NullSeedKind kind,
        int targetIndex,
        ResourceLocation id,
        int amount,
        DumpNullItemStatus dumpStatus
) {
    private static final ResourceLocation AIR = ResourceLocation.withDefaultNamespace("air");
    private static final int MAX_ENTRIES = 2048;

    public NullSeedEntry {
        kind = kind == null ? NullSeedKind.ITEM : kind;
        id = id == null ? AIR : id;
        amount = Math.max(0, amount);
        dumpStatus = dumpStatus == null ? DumpNullItemStatus.NEUTRAL : dumpStatus;
    }

    public static NullSeedEntry item(ResourceLocation itemId, int slot) {
        return new NullSeedEntry(NullSeedKind.ITEM, slot, itemId, 1, DumpNullItemStatus.NEUTRAL);
    }

    public static NullSeedEntry fluid(ResourceLocation fluidId, int tank, int amount) {
        return new NullSeedEntry(NullSeedKind.FLUID, tank, fluidId, amount, DumpNullItemStatus.NEUTRAL);
    }

    public static NullSeedEntry chemical(ResourceLocation chemicalId, int tank, int amount) {
        return new NullSeedEntry(NullSeedKind.CHEMICAL, tank, chemicalId, amount, DumpNullItemStatus.NEUTRAL);
    }

    public static NullSeedEntry dumpRule(ResourceLocation itemId, DumpNullItemStatus status) {
        return new NullSeedEntry(NullSeedKind.DUMP_RULE, -1, itemId, 1, status);
    }

    public static NullSeedEntry entity(ResourceLocation entityId, int slot) {
        return new NullSeedEntry(NullSeedKind.ENTITY, slot, entityId, 1, DumpNullItemStatus.NEUTRAL);
    }

    public NullSeedEntry withTargetIndex(int targetIndex) {
        return new NullSeedEntry(kind, targetIndex, id, amount, dumpStatus);
    }

    public NullSeedEntry withAmount(int amount) {
        return new NullSeedEntry(kind, targetIndex, id, amount, dumpStatus);
    }

    public NullSeedEntry withDumpStatus(DumpNullItemStatus status) {
        return new NullSeedEntry(kind, targetIndex, id, amount, status);
    }

    public static List<NullSeedEntry> itemEntries(List<ResourceLocation> itemIds) {
        List<NullSeedEntry> entries = new ArrayList<>();
        int slot = 0;
        for (ResourceLocation itemId : itemIds == null ? List.<ResourceLocation>of() : itemIds) {
            if (itemId != null) {
                entries.add(item(itemId, slot++));
            }
        }
        return entries;
    }

    public static void writeList(RegistryFriendlyByteBuf buffer, List<NullSeedEntry> entries) {
        List<NullSeedEntry> safeEntries = entries == null ? List.of() : entries;
        buffer.writeVarInt(Math.min(safeEntries.size(), MAX_ENTRIES));
        for (int i = 0; i < safeEntries.size() && i < MAX_ENTRIES; i++) {
            NullSeedEntry entry = safeEntries.get(i);
            buffer.writeVarInt(entry.kind().ordinal());
            buffer.writeVarInt(entry.targetIndex());
            buffer.writeResourceLocation(entry.id());
            buffer.writeVarInt(entry.amount());
            buffer.writeVarInt(entry.dumpStatus().ordinal());
        }
    }

    public static List<NullSeedEntry> readList(RegistryFriendlyByteBuf buffer) {
        int size = Math.max(0, Math.min(buffer.readVarInt(), MAX_ENTRIES));
        List<NullSeedEntry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new NullSeedEntry(
                    NullSeedKind.byId(buffer.readVarInt()),
                    buffer.readVarInt(),
                    buffer.readResourceLocation(),
                    buffer.readVarInt(),
                    DumpNullItemStatus.byId(buffer.readVarInt())
            ));
        }
        return entries;
    }
}
