package dev.deepdaddyttv.deepnullreforged.dripnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public record DripProfile(
        String name,
        boolean includeInventory,
        boolean includeHotbar,
        boolean includeArmor,
        boolean includeOffhand,
        boolean includeCurios,
        int selectedHotbar,
        List<DripSlotAssignment> slots
) {
    private static final String NAME_TAG = "Name";
    private static final String INCLUDE_INVENTORY_TAG = "IncludeInventory";
    private static final String INCLUDE_HOTBAR_TAG = "IncludeHotbar";
    private static final String INCLUDE_ARMOR_TAG = "IncludeArmor";
    private static final String INCLUDE_OFFHAND_TAG = "IncludeOffhand";
    private static final String INCLUDE_CURIOS_TAG = "IncludeCurios";
    private static final String SELECTED_HOTBAR_TAG = "SelectedHotbar";
    private static final String SLOTS_TAG = "Slots";

    public DripProfile {
        name = name == null || name.isBlank() ? "Profile" : name;
        selectedHotbar = Math.max(0, Math.min(8, selectedHotbar));
        slots = normalizeSlots(slots);
    }

    public static DripProfile defaultProfile(int index) {
        return new DripProfile("Profile " + (index + 1), true, true, true, true, false, 0, List.of());
    }

    public boolean empty() {
        return slots.isEmpty();
    }

    public boolean accepts(DripSlotRef ref) {
        if (ref == null) {
            return false;
        }
        return switch (ref.provider()) {
            case DripSlotRef.INVENTORY_PROVIDER -> ref.slot() < 9 ? includeHotbar : includeInventory;
            case DripSlotRef.ARMOR_PROVIDER -> includeArmor;
            case DripSlotRef.OFFHAND_PROVIDER -> includeOffhand;
            case DripSlotRef.CURIOS_PROVIDER -> includeCurios;
            default -> false;
        };
    }

    public DripProfile withSlots(List<DripSlotAssignment> slots) {
        return new DripProfile(name, includeInventory, includeHotbar, includeArmor, includeOffhand, includeCurios, selectedHotbar, slots);
    }

    public DripProfile withScope(String scope, boolean enabled) {
        return switch (scope == null ? "" : scope) {
            case "inventory" -> new DripProfile(name, enabled, includeHotbar, includeArmor, includeOffhand, includeCurios, selectedHotbar, slots);
            case "hotbar" -> new DripProfile(name, includeInventory, enabled, includeArmor, includeOffhand, includeCurios, selectedHotbar, slots);
            case "armor" -> new DripProfile(name, includeInventory, includeHotbar, enabled, includeOffhand, includeCurios, selectedHotbar, slots);
            case "offhand" -> new DripProfile(name, includeInventory, includeHotbar, includeArmor, enabled, includeCurios, selectedHotbar, slots);
            case "curios" -> new DripProfile(name, includeInventory, includeHotbar, includeArmor, includeOffhand, enabled, selectedHotbar, slots);
            default -> this;
        };
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(NAME_TAG, name);
        tag.putBoolean(INCLUDE_INVENTORY_TAG, includeInventory);
        tag.putBoolean(INCLUDE_HOTBAR_TAG, includeHotbar);
        tag.putBoolean(INCLUDE_ARMOR_TAG, includeArmor);
        tag.putBoolean(INCLUDE_OFFHAND_TAG, includeOffhand);
        tag.putBoolean(INCLUDE_CURIOS_TAG, includeCurios);
        tag.putInt(SELECTED_HOTBAR_TAG, selectedHotbar);
        ListTag slotList = new ListTag();
        for (DripSlotAssignment assignment : slots) {
            slotList.add(assignment.save());
        }
        tag.put(SLOTS_TAG, slotList);
        return tag;
    }

    public static DripProfile load(CompoundTag tag, int index) {
        List<DripSlotAssignment> slots = new ArrayList<>();
        if (tag.contains(SLOTS_TAG, Tag.TAG_LIST)) {
            ListTag slotList = tag.getList(SLOTS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < slotList.size(); i++) {
                slots.add(DripSlotAssignment.load(slotList.getCompound(i)));
            }
        }
        return new DripProfile(
                tag.getString(NAME_TAG).isBlank() ? "Profile " + (index + 1) : tag.getString(NAME_TAG),
                !tag.contains(INCLUDE_INVENTORY_TAG) || tag.getBoolean(INCLUDE_INVENTORY_TAG),
                !tag.contains(INCLUDE_HOTBAR_TAG) || tag.getBoolean(INCLUDE_HOTBAR_TAG),
                !tag.contains(INCLUDE_ARMOR_TAG) || tag.getBoolean(INCLUDE_ARMOR_TAG),
                !tag.contains(INCLUDE_OFFHAND_TAG) || tag.getBoolean(INCLUDE_OFFHAND_TAG),
                tag.getBoolean(INCLUDE_CURIOS_TAG),
                tag.getInt(SELECTED_HOTBAR_TAG),
                slots
        );
    }

    public static void write(RegistryFriendlyByteBuf buffer, DripProfile profile) {
        DripProfile value = profile == null ? defaultProfile(0) : profile;
        buffer.writeUtf(value.name, 64);
        buffer.writeBoolean(value.includeInventory);
        buffer.writeBoolean(value.includeHotbar);
        buffer.writeBoolean(value.includeArmor);
        buffer.writeBoolean(value.includeOffhand);
        buffer.writeBoolean(value.includeCurios);
        buffer.writeVarInt(value.selectedHotbar);
        buffer.writeVarInt(value.slots.size());
        for (DripSlotAssignment assignment : value.slots) {
            DripSlotAssignment.write(buffer, assignment);
        }
    }

    public static DripProfile read(RegistryFriendlyByteBuf buffer, int index) {
        String name = buffer.readUtf(64);
        boolean includeInventory = buffer.readBoolean();
        boolean includeHotbar = buffer.readBoolean();
        boolean includeArmor = buffer.readBoolean();
        boolean includeOffhand = buffer.readBoolean();
        boolean includeCurios = buffer.readBoolean();
        int selectedHotbar = buffer.readVarInt();
        int slotCount = Math.max(0, Math.min(128, buffer.readVarInt()));
        List<DripSlotAssignment> slots = new ArrayList<>(slotCount);
        for (int i = 0; i < slotCount; i++) {
            slots.add(DripSlotAssignment.read(buffer));
        }
        String fallbackName = name == null || name.isBlank() ? "Profile " + (index + 1) : name;
        return new DripProfile(fallbackName, includeInventory, includeHotbar, includeArmor, includeOffhand, includeCurios, selectedHotbar, slots);
    }

    private static List<DripSlotAssignment> normalizeSlots(List<DripSlotAssignment> slots) {
        if (slots == null || slots.isEmpty()) {
            return List.of();
        }
        LinkedHashMap<DripSlotRef, DripSlotAssignment> unique = new LinkedHashMap<>();
        for (DripSlotAssignment assignment : slots) {
            if (assignment != null && assignment.valid()) {
                unique.put(assignment.slot(), assignment);
            }
        }
        return List.copyOf(unique.values());
    }
}
