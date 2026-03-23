package dev.deepdaddyttv.deepnullreforged.block;

import net.minecraft.util.StringRepresentable;

public enum NullWorkbenchPart implements StringRepresentable {
    MAIN("main"),
    EXTENSION("extension");

    private final String serializedName;

    NullWorkbenchPart(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
