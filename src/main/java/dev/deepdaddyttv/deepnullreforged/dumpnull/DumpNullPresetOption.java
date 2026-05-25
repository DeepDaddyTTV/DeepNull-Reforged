package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record DumpNullPresetOption(String presetId, String labelKey, String targetType) {
    public static final String TARGET_NONE = "none";
    public static final String TARGET_BIOME = "biome";
    public static final String TARGET_DIMENSION = "dimension";

    public DumpNullPresetOption {
        presetId = presetId == null ? "" : presetId;
        labelKey = labelKey == null || labelKey.isBlank() ? presetId : labelKey;
        targetType = targetType == null || targetType.isBlank() ? TARGET_NONE : targetType;
    }

    public static void writeList(RegistryFriendlyByteBuf buffer, List<DumpNullPresetOption> options) {
        buffer.writeVarInt(options.size());
        for (DumpNullPresetOption option : options) {
            buffer.writeUtf(option.presetId);
            buffer.writeUtf(option.labelKey);
            buffer.writeUtf(option.targetType);
        }
    }

    public static List<DumpNullPresetOption> readList(RegistryFriendlyByteBuf buffer) {
        int count = Math.min(buffer.readVarInt(), 64);
        List<DumpNullPresetOption> options = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            options.add(new DumpNullPresetOption(buffer.readUtf(), buffer.readUtf(), buffer.readUtf()));
        }
        return options;
    }
}
