package dev.deepdaddyttv.deepnullreforged.dripnull;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record DripNullData(
        int selectedProfile,
        int equippedProfile,
        List<DripProfile> profiles,
        Map<String, ItemStack> vaultItems,
        List<ItemStack> looseItems,
        DripUpgradeData upgrades,
        long mendingActiveUntil,
        long chargeCanceledUntil,
        int nextRef
) {
    public static final String ROOT_TAG = "DripNull";
    public static final int MAX_PROFILES = 12;
    public static final int MAX_LOOSE_ITEMS = 54;
    private static final String SELECTED_PROFILE_TAG = "SelectedProfile";
    private static final String EQUIPPED_PROFILE_TAG = "EquippedProfile";
    private static final String PROFILES_TAG = "Profiles";
    private static final String VAULT_ITEMS_TAG = "VaultItems";
    private static final String LOOSE_ITEMS_TAG = "LooseItems";
    private static final String UPGRADE_DATA_TAG = "UpgradeData";
    private static final String MENDING_ACTIVE_UNTIL_TAG = "MendingActiveUntil";
    private static final String CHARGE_CANCELED_UNTIL_TAG = "ChargeCanceledUntil";
    private static final String NEXT_REF_TAG = "NextRef";
    private static final String REF_TAG = "Ref";
    private static final String STACK_TAG = "Stack";

    public DripNullData {
        profiles = profiles == null ? List.of() : List.copyOf(profiles);
        vaultItems = copyVault(vaultItems);
        looseItems = copyStacks(looseItems, MAX_LOOSE_ITEMS);
        upgrades = upgrades == null ? DripUpgradeData.EMPTY : upgrades;
        int maxProfile = Math.max(0, profiles.size() - 1);
        selectedProfile = profiles.isEmpty() ? 0 : Math.max(0, Math.min(maxProfile, selectedProfile));
        equippedProfile = equippedProfile < 0 || equippedProfile > maxProfile ? -1 : equippedProfile;
        nextRef = Math.max(nextRef, inferNextRef(vaultItems, profiles));
    }

    public static DripNullData empty(DeepNullTier tier) {
        int profileCount = profileCount(tier);
        List<DripProfile> profiles = new ArrayList<>(profileCount);
        for (int i = 0; i < profileCount; i++) {
            profiles.add(DripProfile.defaultProfile(i));
        }
        return new DripNullData(0, -1, profiles, Map.of(), List.of(), DripUpgradeData.EMPTY, 0L, 0L, 1);
    }

    public static int profileCount(DeepNullTier tier) {
        return DeepNullConfig.getDripNullProfileCount(tier);
    }

    public static DripNullData get(ItemStack stack, DeepNullTier tier, HolderLookup.Provider registries) {
        if (stack.isEmpty()) {
            return empty(tier);
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return empty(tier);
        }
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return empty(tier);
        }
        return load(tag.getCompound(ROOT_TAG), tier, registries);
    }

    public static void set(ItemStack stack, DripNullData data, DeepNullTier tier, HolderLookup.Provider registries) {
        DripNullData value = (data == null ? empty(tier) : data).withTierProfileCount(tier);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(ROOT_TAG, value.save(registries)));
    }

    public static DripNullData load(CompoundTag root, DeepNullTier tier, HolderLookup.Provider registries) {
        List<DripProfile> profiles = new ArrayList<>();
        if (root.contains(PROFILES_TAG, Tag.TAG_LIST)) {
            ListTag profileList = root.getList(PROFILES_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < profileList.size(); i++) {
                profiles.add(DripProfile.load(profileList.getCompound(i), i));
            }
        }
        Map<String, ItemStack> vaultItems = new LinkedHashMap<>();
        if (registries != null && root.contains(VAULT_ITEMS_TAG, Tag.TAG_LIST)) {
            ListTag vaultList = root.getList(VAULT_ITEMS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < vaultList.size(); i++) {
                CompoundTag entry = vaultList.getCompound(i);
                ItemStack stack = ItemStack.parseOptional(registries, entry.getCompound(STACK_TAG));
                if (!stack.isEmpty()) {
                    vaultItems.put(entry.getString(REF_TAG), stack);
                }
            }
        }
        List<ItemStack> looseItems = new ArrayList<>();
        if (registries != null && root.contains(LOOSE_ITEMS_TAG, Tag.TAG_LIST)) {
            ListTag looseList = root.getList(LOOSE_ITEMS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < looseList.size(); i++) {
                ItemStack stack = ItemStack.parseOptional(registries, looseList.getCompound(i).getCompound(STACK_TAG));
                if (!stack.isEmpty()) {
                    looseItems.add(stack);
                }
            }
        }
        DripUpgradeData upgrades = root.contains(UPGRADE_DATA_TAG, Tag.TAG_COMPOUND)
                ? DripUpgradeData.load(root.getCompound(UPGRADE_DATA_TAG))
                : DripUpgradeData.EMPTY;
        return new DripNullData(
                root.getInt(SELECTED_PROFILE_TAG),
                root.contains(EQUIPPED_PROFILE_TAG) ? root.getInt(EQUIPPED_PROFILE_TAG) : -1,
                profiles,
                vaultItems,
                looseItems,
                upgrades,
                root.getLong(MENDING_ACTIVE_UNTIL_TAG),
                root.getLong(CHARGE_CANCELED_UNTIL_TAG),
                root.getInt(NEXT_REF_TAG)
        ).withTierProfileCount(tier);
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag root = new CompoundTag();
        root.putInt(SELECTED_PROFILE_TAG, selectedProfile);
        root.putInt(EQUIPPED_PROFILE_TAG, equippedProfile);
        ListTag profileList = new ListTag();
        for (DripProfile profile : profiles) {
            profileList.add(profile.save());
        }
        root.put(PROFILES_TAG, profileList);
        ListTag vaultList = new ListTag();
        if (registries != null) {
            for (Map.Entry<String, ItemStack> entry : vaultItems.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    continue;
                }
                CompoundTag tag = new CompoundTag();
                tag.putString(REF_TAG, entry.getKey());
                tag.put(STACK_TAG, entry.getValue().saveOptional(registries));
                vaultList.add(tag);
            }
        }
        root.put(VAULT_ITEMS_TAG, vaultList);
        ListTag looseList = new ListTag();
        if (registries != null) {
            for (ItemStack stack : looseItems) {
                if (stack.isEmpty()) {
                    continue;
                }
                CompoundTag tag = new CompoundTag();
                tag.put(STACK_TAG, stack.saveOptional(registries));
                looseList.add(tag);
            }
        }
        root.put(LOOSE_ITEMS_TAG, looseList);
        root.put(UPGRADE_DATA_TAG, upgrades.save());
        root.putLong(MENDING_ACTIVE_UNTIL_TAG, mendingActiveUntil);
        root.putLong(CHARGE_CANCELED_UNTIL_TAG, chargeCanceledUntil);
        root.putInt(NEXT_REF_TAG, nextRef);
        return root;
    }

    public static void write(RegistryFriendlyByteBuf buffer, DripNullData data) {
        DripNullData value = data == null ? empty(DeepNullTier.REDSTONE) : data;
        buffer.writeVarInt(value.selectedProfile);
        buffer.writeVarInt(value.equippedProfile);
        buffer.writeVarInt(value.profiles.size());
        for (DripProfile profile : value.profiles) {
            DripProfile.write(buffer, profile);
        }
        buffer.writeVarInt(value.vaultItems.size());
        for (Map.Entry<String, ItemStack> entry : value.vaultItems.entrySet()) {
            buffer.writeUtf(entry.getKey(), 64);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, entry.getValue());
        }
        buffer.writeVarInt(value.looseItems.size());
        for (ItemStack stack : value.looseItems) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
        }
        DripUpgradeData.write(buffer, value.upgrades);
        buffer.writeVarLong(value.mendingActiveUntil);
        buffer.writeVarLong(value.chargeCanceledUntil);
        buffer.writeVarInt(value.nextRef);
    }

    public static DripNullData read(RegistryFriendlyByteBuf buffer) {
        int selected = buffer.readVarInt();
        int equipped = buffer.readVarInt();
        int profileCount = Math.max(0, Math.min(MAX_PROFILES, buffer.readVarInt()));
        List<DripProfile> profiles = new ArrayList<>(profileCount);
        for (int i = 0; i < profileCount; i++) {
            profiles.add(DripProfile.read(buffer, i));
        }
        int vaultCount = Math.max(0, Math.min(256, buffer.readVarInt()));
        Map<String, ItemStack> vaultItems = new LinkedHashMap<>();
        for (int i = 0; i < vaultCount; i++) {
            String ref = buffer.readUtf(64);
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            if (!stack.isEmpty()) {
                vaultItems.put(ref, stack);
            }
        }
        int looseCount = Math.max(0, Math.min(MAX_LOOSE_ITEMS, buffer.readVarInt()));
        List<ItemStack> looseItems = new ArrayList<>(looseCount);
        for (int i = 0; i < looseCount; i++) {
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            if (!stack.isEmpty()) {
                looseItems.add(stack);
            }
        }
        return new DripNullData(
                selected,
                equipped,
                profiles,
                vaultItems,
                looseItems,
                DripUpgradeData.read(buffer),
                buffer.readVarLong(),
                buffer.readVarLong(),
                buffer.readVarInt()
        );
    }

    public DripProfile selected() {
        return profile(selectedProfile);
    }

    public DripProfile equipped() {
        return equippedProfile >= 0 ? profile(equippedProfile) : null;
    }

    public DripProfile profile(int index) {
        if (index < 0 || index >= profiles.size()) {
            return DripProfile.defaultProfile(index);
        }
        return profiles.get(index);
    }

    public DripNullData withSelectedProfile(int selectedProfile) {
        return new DripNullData(selectedProfile, equippedProfile, profiles, vaultItems, looseItems, upgrades, mendingActiveUntil, chargeCanceledUntil, nextRef);
    }

    public DripNullData withProfile(int index, DripProfile profile) {
        if (index < 0 || index >= profiles.size() || profile == null) {
            return this;
        }
        List<DripProfile> updated = new ArrayList<>(profiles);
        updated.set(index, profile);
        return new DripNullData(selectedProfile, equippedProfile, updated, vaultItems, looseItems, upgrades, mendingActiveUntil, chargeCanceledUntil, nextRef);
    }

    public DripNullData withState(int selectedProfile, int equippedProfile, List<DripProfile> profiles, Map<String, ItemStack> vaultItems, List<ItemStack> looseItems, int nextRef) {
        return new DripNullData(selectedProfile, equippedProfile, profiles, vaultItems, looseItems, upgrades, mendingActiveUntil, chargeCanceledUntil, nextRef);
    }

    public DripNullData withUpgrades(DripUpgradeData upgrades) {
        return new DripNullData(selectedProfile, equippedProfile, profiles, vaultItems, looseItems, upgrades, mendingActiveUntil, chargeCanceledUntil, nextRef);
    }

    public DripNullData withMendingActiveUntil(long gameTime) {
        return new DripNullData(selectedProfile, equippedProfile, profiles, vaultItems, looseItems, upgrades, gameTime, chargeCanceledUntil, nextRef);
    }

    public DripNullData withChargeCanceledUntil(long gameTime) {
        return new DripNullData(selectedProfile, equippedProfile, profiles, vaultItems, looseItems, upgrades, mendingActiveUntil, gameTime, nextRef);
    }

    public DripNullData withTierProfileCount(DeepNullTier tier) {
        int profileCount = profileCount(tier);
        List<DripProfile> updated = new ArrayList<>(profiles);
        while (updated.size() < profileCount) {
            updated.add(DripProfile.defaultProfile(updated.size()));
        }
        if (updated.size() > profileCount) {
            updated = new ArrayList<>(updated.subList(0, profileCount));
        }
        return new DripNullData(selectedProfile, equippedProfile, updated, vaultItems, looseItems, upgrades, mendingActiveUntil, chargeCanceledUntil, nextRef);
    }

    public String nextRefId() {
        return "ref" + Math.max(1, nextRef);
    }

    public int nextRefAfterAllocating(int count) {
        return Math.max(1, nextRef) + Math.max(0, count);
    }

    public Set<String> assignedRefs() {
        Set<String> refs = new LinkedHashSet<>();
        for (DripProfile profile : profiles) {
            for (DripSlotAssignment assignment : profile.slots()) {
                refs.add(assignment.ref());
            }
        }
        return refs;
    }

    private static Map<String, ItemStack> copyVault(Map<String, ItemStack> vaultItems) {
        if (vaultItems == null || vaultItems.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, ItemStack> copy = new LinkedHashMap<>();
        for (Map.Entry<String, ItemStack> entry : vaultItems.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null && !entry.getValue().isEmpty()) {
                copy.put(entry.getKey(), entry.getValue().copy());
            }
        }
        return java.util.Collections.unmodifiableMap(copy);
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks, int limit) {
        if (stacks == null || stacks.isEmpty()) {
            return List.of();
        }
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                copy.add(stack.copy());
                if (copy.size() >= limit) {
                    break;
                }
            }
        }
        return List.copyOf(copy);
    }

    private static int inferNextRef(Map<String, ItemStack> vaultItems, List<DripProfile> profiles) {
        int max = 1;
        if (vaultItems != null) {
            for (String ref : vaultItems.keySet()) {
                max = Math.max(max, refNumber(ref) + 1);
            }
        }
        if (profiles != null) {
            for (DripProfile profile : profiles) {
                if (profile == null) {
                    continue;
                }
                for (DripSlotAssignment assignment : profile.slots()) {
                    max = Math.max(max, refNumber(assignment.ref()) + 1);
                }
            }
        }
        return max;
    }

    private static int refNumber(String ref) {
        if (ref == null || !ref.startsWith("ref")) {
            return 0;
        }
        try {
            return Integer.parseInt(ref.substring(3));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
