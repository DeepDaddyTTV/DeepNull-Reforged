package dev.deepdaddyttv.deepnullreforged.nullseed;

import java.util.List;

public record ItemSetPresetOption(String presetId, String labelKey) {
    public static final String ORES = "ores";
    public static final String FARMING = "farming";
    public static final String REDSTONE = "redstone";
    public static final String MOB_DROPS = "mob_drops";
    public static final String BUILDING = "building";
    public static final String QUARRY = "quarry";

    public static List<ItemSetPresetOption> all() {
        return List.of(
                new ItemSetPresetOption(ORES, "container.deepnullreforged.null_workbench.seeder.preset.ores"),
                new ItemSetPresetOption(FARMING, "container.deepnullreforged.null_workbench.seeder.preset.farming"),
                new ItemSetPresetOption(REDSTONE, "container.deepnullreforged.null_workbench.seeder.preset.redstone"),
                new ItemSetPresetOption(MOB_DROPS, "container.deepnullreforged.null_workbench.seeder.preset.mob_drops")
        );
    }

    public static boolean isKnown(String presetId) {
        return ORES.equals(presetId)
                || FARMING.equals(presetId)
                || REDSTONE.equals(presetId)
                || MOB_DROPS.equals(presetId)
                || BUILDING.equals(presetId)
                || QUARRY.equals(presetId);
    }
}
