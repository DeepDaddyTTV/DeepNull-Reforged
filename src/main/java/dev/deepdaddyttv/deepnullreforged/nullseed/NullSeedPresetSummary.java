package dev.deepdaddyttv.deepnullreforged.nullseed;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record NullSeedPresetSummary(
        String presetId,
        String displayName,
        NullSeedPresetSource source,
        String namespace,
        int entryCount
) {
    private static final int MAX_SUMMARIES = 512;

    public NullSeedPresetSummary {
        presetId = presetId == null ? "" : presetId;
        displayName = displayName == null || displayName.isBlank() ? presetId : displayName;
        source = source == null ? NullSeedPresetSource.BUILT_IN : source;
        namespace = namespace == null ? "" : namespace;
        entryCount = Math.max(0, entryCount);
    }

    public static void write(RegistryFriendlyByteBuf buffer, NullSeedPresetSummary summary) {
        NullSeedPresetSummary value = summary == null
                ? new NullSeedPresetSummary("", "", NullSeedPresetSource.BUILT_IN, "", 0)
                : summary;
        buffer.writeUtf(value.presetId());
        buffer.writeUtf(value.displayName());
        buffer.writeVarInt(value.source().ordinal());
        buffer.writeUtf(value.namespace());
        buffer.writeVarInt(value.entryCount());
    }

    public static NullSeedPresetSummary read(RegistryFriendlyByteBuf buffer) {
        return new NullSeedPresetSummary(
                buffer.readUtf(),
                buffer.readUtf(),
                NullSeedPresetSource.byId(buffer.readVarInt()),
                buffer.readUtf(),
                buffer.readVarInt()
        );
    }

    public static void writeList(RegistryFriendlyByteBuf buffer, List<NullSeedPresetSummary> summaries) {
        List<NullSeedPresetSummary> safe = summaries == null ? List.of() : summaries;
        buffer.writeVarInt(Math.min(safe.size(), MAX_SUMMARIES));
        for (int i = 0; i < safe.size() && i < MAX_SUMMARIES; i++) {
            write(buffer, safe.get(i));
        }
    }

    public static List<NullSeedPresetSummary> readList(RegistryFriendlyByteBuf buffer) {
        int size = Math.max(0, Math.min(buffer.readVarInt(), MAX_SUMMARIES));
        List<NullSeedPresetSummary> summaries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            summaries.add(read(buffer));
        }
        return summaries;
    }
}
