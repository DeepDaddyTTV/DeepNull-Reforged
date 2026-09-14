package dev.deepdaddyttv.deepnullreforged.inventory;

import org.jetbrains.annotations.Nullable;

public enum NullSlotDomain {
    ITEM_STORAGE(1),
    FLUID_STORAGE(2);

    private final int id;

    NullSlotDomain(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static @Nullable NullSlotDomain byId(int id) {
        for (NullSlotDomain domain : values()) {
            if (domain.id == id) {
                return domain;
            }
        }
        return null;
    }
}
