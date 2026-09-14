package dev.deepdaddyttv.deepnullreforged.inventory;

import net.minecraft.network.chat.Component;

public enum ItemExtractionMode {
    KEEP_ALL(1, Integer.MAX_VALUE, "dn.not_extract.desc"),
    KEEP_1(2, 1, "dn.extract_all_but.desc", 1),
    KEEP_16(3, 16, "dn.extract_all_but.desc", 16),
    KEEP_64(4, 64, "dn.extract_all_but.desc", 64),
    KEEP_NONE(5, 0, "dn.extract_all.desc"),
    CUSTOM(6, -1, "dn.extract_all_but.desc");

    private static final ItemExtractionMode[] CYCLE_VALUES = {KEEP_ALL, KEEP_1, KEEP_16, KEEP_64, KEEP_NONE};

    private final int protocolId;
    private final int keptAmount;
    private final String translationKey;
    private final Integer numericSuffix;

    ItemExtractionMode(int protocolId, int keptAmount, String translationKey) {
        this(protocolId, keptAmount, translationKey, null);
    }

    ItemExtractionMode(int protocolId, int keptAmount, String translationKey, Integer numericSuffix) {
        this.protocolId = protocolId;
        this.keptAmount = keptAmount;
        this.translationKey = translationKey;
        this.numericSuffix = numericSuffix;
    }

    public int keptAmount() {
        return keptAmount;
    }

    public int protocolId() {
        return protocolId;
    }

    public static ItemExtractionMode byProtocolId(int id) {
        for (ItemExtractionMode mode : values()) {
            if (mode.protocolId == id) {
                return mode;
            }
        }
        return null;
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
        int index = cycleIndex(this, forward);
        int nextIndex = forward ? (index + 1) % CYCLE_VALUES.length : Math.floorMod(index - 1, CYCLE_VALUES.length);
        return CYCLE_VALUES[nextIndex];
    }

    public static ItemExtractionMode[] cycleValues() {
        return CYCLE_VALUES.clone();
    }

    public static int cycleCount() {
        return CYCLE_VALUES.length;
    }

    public static int cycleIndex(ItemExtractionMode mode, boolean forwardFromCustom) {
        if (mode == CUSTOM) {
            return forwardFromCustom ? CYCLE_VALUES.length - 1 : 3;
        }
        for (int index = 0; index < CYCLE_VALUES.length; index++) {
            if (CYCLE_VALUES[index] == mode) {
                return index;
            }
        }
        return 0;
    }
}
