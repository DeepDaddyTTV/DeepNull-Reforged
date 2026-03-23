package dev.deepdaddyttv.deepnullreforged.inventory;

import net.minecraft.network.chat.Component;

public enum ItemExtractionMode {
    KEEP_ALL(Integer.MAX_VALUE, "dn.not_extract.desc"),
    KEEP_1(1, "dn.extract_all_but.desc", 1),
    KEEP_16(16, "dn.extract_all_but.desc", 16),
    KEEP_64(64, "dn.extract_all_but.desc", 64),
    KEEP_NONE(0, "dn.extract_all.desc"),
    CUSTOM(-1, "dn.extract_all_but.desc");

    private final int keptAmount;
    private final String translationKey;
    private final Integer numericSuffix;

    ItemExtractionMode(int keptAmount, String translationKey) {
        this(keptAmount, translationKey, null);
    }

    ItemExtractionMode(int keptAmount, String translationKey, Integer numericSuffix) {
        this.keptAmount = keptAmount;
        this.translationKey = translationKey;
        this.numericSuffix = numericSuffix;
    }

    public int keptAmount() {
        return keptAmount;
    }

    public Component tooltip() {
        return tooltip(keptAmount);
    }

    public Component tooltip(int amountOverride) {
        Component base = Component.translatable(translationKey);
        if (this == CUSTOM) {
            return base.copy().append(" ").append(Integer.toString(Math.max(0, amountOverride)));
        }
        if (numericSuffix != null) {
            return base.copy().append(" ").append(Integer.toString(numericSuffix));
        }
        return KEEP_ALL == this ? Component.translatable("dn.do.desc").append(" ").append(base) : base;
    }

    public ItemExtractionMode cycle(boolean forward) {
        ItemExtractionMode[] cycleValues = {KEEP_ALL, KEEP_1, KEEP_16, KEEP_64, KEEP_NONE};
        int index = 0;
        if (this == CUSTOM) {
            index = forward ? cycleValues.length - 1 : 3;
        } else {
            for (int i = 0; i < cycleValues.length; i++) {
                if (cycleValues[i] == this) {
                    index = i;
                    break;
                }
            }
        }
        int nextIndex = forward ? (index + 1) % cycleValues.length : Math.floorMod(index - 1, cycleValues.length);
        return cycleValues[nextIndex];
    }
}
