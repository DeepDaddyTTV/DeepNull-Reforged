package dev.deepdaddyttv.deepnullreforged.inventory;

import net.minecraft.network.chat.Component;

public enum ItemExtractionMode {
    KEEP_ALL(Integer.MAX_VALUE, "dn.not_extract.desc"),
    KEEP_1(1, "dn.extract_all_but.desc", 1),
    KEEP_16(16, "dn.extract_all_but.desc", 16),
    KEEP_64(64, "dn.extract_all_but.desc", 64),
    KEEP_NONE(0, "dn.extract_all.desc");

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
        Component base = Component.translatable(translationKey);
        if (numericSuffix == null) {
            return KEEP_ALL == this ? Component.translatable("dn.do.desc").append(" ").append(base) : base;
        }
        return base.copy().append(" ").append(Integer.toString(numericSuffix));
    }

    public ItemExtractionMode cycle(boolean forward) {
        ItemExtractionMode[] values = values();
        int nextIndex = forward ? (ordinal() + 1) % values.length : Math.floorMod(ordinal() - 1, values.length);
        return values[nextIndex];
    }
}
