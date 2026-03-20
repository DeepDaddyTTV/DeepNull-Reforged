package dev.deepdaddyttv.deepnullreforged.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class StoredChemical {
    private static final String ID_TAG = "Id";
    private static final String AMOUNT_TAG = "Amount";
    private static final String ICON_TAG = "Icon";
    private static final String TINT_TAG = "Tint";
    private static final String TRANSLATION_KEY_TAG = "TranslationKey";
    private static final String GASEOUS_TAG = "Gaseous";

    public static final StoredChemical EMPTY = new StoredChemical("", 0L, "", 0xFFFFFFFF, "", false);

    private final String chemicalId;
    private final long amount;
    private final String iconPath;
    private final int tint;
    private final String translationKey;
    private final boolean gaseous;

    public StoredChemical(String chemicalId, long amount, String iconPath, int tint, String translationKey, boolean gaseous) {
        this.chemicalId = chemicalId == null ? "" : chemicalId;
        this.amount = amount;
        this.iconPath = iconPath == null ? "" : iconPath;
        this.tint = tint;
        this.translationKey = translationKey == null ? "" : translationKey;
        this.gaseous = gaseous;
    }

    public String chemicalId() {
        return chemicalId;
    }

    public long amount() {
        return amount;
    }

    public String iconPath() {
        return iconPath;
    }

    public int tint() {
        return tint;
    }

    public String translationKey() {
        return translationKey;
    }

    public boolean gaseous() {
        return gaseous;
    }

    public boolean isEmpty() {
        return chemicalId.isBlank() || amount <= 0L;
    }

    public StoredChemical copy() {
        return isEmpty() ? EMPTY : new StoredChemical(chemicalId, amount, iconPath, tint, translationKey, gaseous);
    }

    public StoredChemical copyWithAmount(long amount) {
        return isEmpty() || amount <= 0L ? EMPTY : new StoredChemical(chemicalId, amount, iconPath, tint, translationKey, gaseous);
    }

    public boolean isSameChemical(StoredChemical other) {
        return other != null && !isEmpty() && !other.isEmpty() && chemicalId.equals(other.chemicalId);
    }

    public Component getHoverName() {
        if (!translationKey.isBlank()) {
            return Component.translatable(translationKey);
        }
        ResourceLocation id = chemicalLocation();
        return id == null ? Component.literal(chemicalId) : Component.literal(id.getPath());
    }

    public ResourceLocation chemicalLocation() {
        return chemicalId.isBlank() ? null : ResourceLocation.tryParse(chemicalId);
    }

    public ResourceLocation iconLocation() {
        return iconPath.isBlank() ? null : ResourceLocation.tryParse(iconPath);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ID_TAG, chemicalId);
        tag.putLong(AMOUNT_TAG, amount);
        if (!iconPath.isBlank()) {
            tag.putString(ICON_TAG, iconPath);
        }
        tag.putInt(TINT_TAG, tint);
        if (!translationKey.isBlank()) {
            tag.putString(TRANSLATION_KEY_TAG, translationKey);
        }
        if (gaseous) {
            tag.putBoolean(GASEOUS_TAG, true);
        }
        return tag;
    }

    public static StoredChemical load(CompoundTag tag) {
        StoredChemical chemical = new StoredChemical(
                tag.getString(ID_TAG),
                tag.getLong(AMOUNT_TAG),
                tag.getString(ICON_TAG),
                tag.contains(TINT_TAG) ? tag.getInt(TINT_TAG) : 0xFFFFFFFF,
                tag.getString(TRANSLATION_KEY_TAG),
                tag.getBoolean(GASEOUS_TAG)
        );
        return chemical.isEmpty() ? EMPTY : chemical;
    }
}
