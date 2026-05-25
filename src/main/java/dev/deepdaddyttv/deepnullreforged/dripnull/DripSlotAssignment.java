package dev.deepdaddyttv.deepnullreforged.dripnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record DripSlotAssignment(DripSlotRef slot, String ref) {
    private static final String SLOT_TAG = "Slot";
    private static final String REF_TAG = "Ref";

    public DripSlotAssignment {
        slot = slot == null ? DripSlotRef.inventory(0) : slot;
        ref = ref == null ? "" : ref;
    }

    public boolean valid() {
        return !ref.isBlank();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put(SLOT_TAG, slot.save());
        tag.putString(REF_TAG, ref);
        return tag;
    }

    public static DripSlotAssignment load(CompoundTag tag) {
        DripSlotRef slot = tag.contains(SLOT_TAG, Tag.TAG_COMPOUND)
                ? DripSlotRef.load(tag.getCompound(SLOT_TAG))
                : DripSlotRef.inventory(0);
        return new DripSlotAssignment(slot, tag.getString(REF_TAG));
    }

    public static void write(RegistryFriendlyByteBuf buffer, DripSlotAssignment assignment) {
        DripSlotAssignment value = assignment == null ? new DripSlotAssignment(DripSlotRef.inventory(0), "") : assignment;
        DripSlotRef.write(buffer, value.slot);
        buffer.writeUtf(value.ref, 64);
    }

    public static DripSlotAssignment read(RegistryFriendlyByteBuf buffer) {
        return new DripSlotAssignment(DripSlotRef.read(buffer), buffer.readUtf(64));
    }
}
