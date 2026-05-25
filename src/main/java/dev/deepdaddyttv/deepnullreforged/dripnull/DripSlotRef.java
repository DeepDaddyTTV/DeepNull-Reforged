package dev.deepdaddyttv.deepnullreforged.dripnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record DripSlotRef(String provider, int slot) {
    public static final String INVENTORY_PROVIDER = "minecraft:inventory";
    public static final String ARMOR_PROVIDER = "minecraft:armor";
    public static final String OFFHAND_PROVIDER = "minecraft:offhand";
    public static final String CURIOS_PROVIDER = "curios:item_handler";
    private static final String PROVIDER_TAG = "Provider";
    private static final String SLOT_TAG = "Slot";

    public DripSlotRef {
        provider = provider == null || provider.isBlank() ? INVENTORY_PROVIDER : provider;
        slot = Math.max(0, slot);
    }

    public static DripSlotRef inventory(int slot) {
        return new DripSlotRef(INVENTORY_PROVIDER, slot);
    }

    public static DripSlotRef armor(int slot) {
        return new DripSlotRef(ARMOR_PROVIDER, slot);
    }

    public static DripSlotRef offhand() {
        return new DripSlotRef(OFFHAND_PROVIDER, 0);
    }

    public boolean vanilla() {
        return INVENTORY_PROVIDER.equals(provider) || ARMOR_PROVIDER.equals(provider) || OFFHAND_PROVIDER.equals(provider);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(PROVIDER_TAG, provider);
        tag.putInt(SLOT_TAG, slot);
        return tag;
    }

    public static DripSlotRef load(CompoundTag tag) {
        return new DripSlotRef(tag.getString(PROVIDER_TAG), tag.getInt(SLOT_TAG));
    }

    public static void write(RegistryFriendlyByteBuf buffer, DripSlotRef ref) {
        DripSlotRef value = ref == null ? inventory(0) : ref;
        buffer.writeUtf(value.provider, 128);
        buffer.writeVarInt(value.slot);
    }

    public static DripSlotRef read(RegistryFriendlyByteBuf buffer) {
        return new DripSlotRef(buffer.readUtf(128), buffer.readVarInt());
    }
}
