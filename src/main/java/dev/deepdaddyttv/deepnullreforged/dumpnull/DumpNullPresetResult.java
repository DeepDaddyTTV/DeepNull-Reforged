package dev.deepdaddyttv.deepnullreforged.dumpnull;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record DumpNullPresetResult(String presetId, List<ResourceLocation> selectedMobs, List<DumpNullRule> presetRules) {
    public DumpNullPresetResult {
        presetId = presetId == null ? "" : presetId;
        selectedMobs = List.copyOf(selectedMobs == null ? List.of() : selectedMobs);
        presetRules = List.copyOf(presetRules == null ? List.of() : presetRules);
    }
}
