package dev.deepdaddyttv.deepnullreforged.dumpnull;

import dev.deepdaddyttv.deepnullreforged.nullseed.ItemSetPresetOption;

import java.util.Arrays;
import java.util.List;

public enum DumpNullItemCatalogView {
    ORES("ores", "Ores", "Raw ores, ingots, nuggets, gems, dusts, and ore blocks.", ItemSetPresetOption.ORES),
    FARMING("farming", "Farming", "Seeds, crops, saplings, plants, and farmable food outputs.", ItemSetPresetOption.FARMING),
    BUILDING("building", "Building", "Common blocks for building palettes and stocked construction materials.", ItemSetPresetOption.BUILDING),
    QUARRY("quarry", "Quarry", "Bulk stone, terrain, and excavation outputs.", ItemSetPresetOption.QUARRY),
    MOB_DROPS("mob_drops", "Mob Drops", "Common mob farm outputs and modded drop-like items.", ItemSetPresetOption.MOB_DROPS),
    INVENTORY("inventory", "Inventory", "Items currently carried in the player inventory.", "");

    private final String id;
    private final String label;
    private final String description;
    private final String itemSetPresetId;

    DumpNullItemCatalogView(String id, String label, String description, String itemSetPresetId) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.itemSetPresetId = itemSetPresetId;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public String itemSetPresetId() {
        return itemSetPresetId;
    }

    public boolean inventoryBacked() {
        return this == INVENTORY;
    }

    public static List<DumpNullItemCatalogView> all() {
        return Arrays.asList(values());
    }

    public static DumpNullItemCatalogView byId(String id) {
        if (id != null) {
            for (DumpNullItemCatalogView view : values()) {
                if (view.id.equals(id)) {
                    return view;
                }
            }
        }
        return ORES;
    }
}
