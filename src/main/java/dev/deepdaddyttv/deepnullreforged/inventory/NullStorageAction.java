package dev.deepdaddyttv.deepnullreforged.inventory;

import org.jetbrains.annotations.Nullable;

public enum NullStorageAction {
    SWAP(1),
    MERGE(2),
    CLEAR(3),
    SORT(4),
    SELECT(5),
    CYCLE_FORWARD(6),
    CYCLE_BACKWARD(7);

    private final int id;

    NullStorageAction(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static @Nullable NullStorageAction byId(int id) {
        for (NullStorageAction action : values()) {
            if (action.id == id) {
                return action;
            }
        }
        return null;
    }
}
