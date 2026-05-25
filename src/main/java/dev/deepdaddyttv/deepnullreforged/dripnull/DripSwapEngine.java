package dev.deepdaddyttv.deepnullreforged.dripnull;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DripNullItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DripSwapEngine {
    private DripSwapEngine() {
    }

    public static Result run(ServerPlayer player, ItemStack dripStack, DeepNullTier tier, int reservedInventorySlot) {
        HolderLookup.Provider registries = player.level().registryAccess();
        DripNullData data = DripNullData.get(dripStack, tier, registries);
        DripProfile selected = data.selected();
        Result result = selected.empty()
                ? capture(player, dripStack, tier, data, selected, reservedInventorySlot, registries)
                : data.equippedProfile() == data.selectedProfile()
                ? stowSelected(player, dripStack, tier, data, selected, registries)
                : swap(player, dripStack, tier, data, selected, registries);
        if (result.success()) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.7F, 1.1F);
            player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.45F, 1.35F);
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 20, 0.45D, 0.7D, 0.45D, 0.05D);
                serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 0.9D, player.getZ(), 12, 0.35D, 0.55D, 0.35D, 0.02D);
            }
        }
        return result;
    }

    public static boolean cycleProfile(ServerPlayer player, ItemStack dripStack, DeepNullTier tier, boolean forward) {
        HolderLookup.Provider registries = player.level().registryAccess();
        DripNullData data = DripNullData.get(dripStack, tier, registries);
        if (data.profiles().isEmpty()) {
            return false;
        }
        int next = Math.floorMod(data.selectedProfile() + (forward ? 1 : -1), data.profiles().size());
        DripNullData.set(dripStack, data.withSelectedProfile(next), tier, registries);
        sync(player);
        return true;
    }

    public static boolean recover(ServerPlayer player, ItemStack dripStack, DeepNullTier tier) {
        HolderLookup.Provider registries = player.level().registryAccess();
        DripNullData data = DripNullData.get(dripStack, tier, registries);
        Set<String> assignedRefs = data.assignedRefs();
        List<ItemStack> recoverable = new ArrayList<>(data.looseItems());
        List<String> recoveredVaultRefs = new ArrayList<>();
        for (Map.Entry<String, ItemStack> entry : data.vaultItems().entrySet()) {
            if (!assignedRefs.contains(entry.getKey())) {
                recoverable.add(entry.getValue());
                recoveredVaultRefs.add(entry.getKey());
            }
        }
        if (recoverable.isEmpty() || !canFitPlayerInventory(player, recoverable)) {
            return false;
        }
        for (ItemStack stack : recoverable) {
            player.getInventory().add(stack.copy());
        }
        Map<String, ItemStack> vault = new LinkedHashMap<>(data.vaultItems());
        for (String ref : recoveredVaultRefs) {
            vault.remove(ref);
        }
        DripNullData updated = data.withState(data.selectedProfile(), data.equippedProfile(), data.profiles(), vault, List.of(), data.nextRef());
        DripNullData.set(dripStack, updated, tier, registries);
        sync(player);
        return true;
    }

    public static boolean installMend(ServerPlayer player, ItemStack dripStack, DeepNullTier tier) {
        HolderLookup.Provider registries = player.level().registryAccess();
        DripNullData data = DripNullData.get(dripStack, tier, registries);
        if (data.upgrades().has(DripNullUpgradeType.MEND)) {
            return false;
        }
        DripNullData.set(dripStack, data.withUpgrades(data.upgrades().withInstalled(DripNullUpgradeType.MEND)), tier, registries);
        sync(player);
        return true;
    }

    public static boolean selectProfile(ServerPlayer player, ItemStack dripStack, DeepNullTier tier, int profile) {
        HolderLookup.Provider registries = player.level().registryAccess();
        DripNullData data = DripNullData.get(dripStack, tier, registries);
        if (profile < 0 || profile >= data.profiles().size()) {
            return false;
        }
        DripNullData.set(dripStack, data.withSelectedProfile(profile), tier, registries);
        sync(player);
        return true;
    }

    public static boolean toggleScope(ServerPlayer player, ItemStack dripStack, DeepNullTier tier, int profile, String scope) {
        HolderLookup.Provider registries = player.level().registryAccess();
        DripNullData data = DripNullData.get(dripStack, tier, registries);
        if (profile < 0 || profile >= data.profiles().size()) {
            return false;
        }
        DripProfile old = data.profile(profile);
        boolean next = switch (scope) {
            case "inventory" -> !old.includeInventory();
            case "hotbar" -> !old.includeHotbar();
            case "armor" -> !old.includeArmor();
            case "offhand" -> !old.includeOffhand();
            case "curios" -> !old.includeCurios();
            default -> old.includeInventory();
        };
        DripNullData.set(dripStack, data.withProfile(profile, old.withScope(scope, next)), tier, registries);
        sync(player);
        return true;
    }

    public static Result clearSelectedProfile(ServerPlayer player, ItemStack dripStack, DeepNullTier tier) {
        HolderLookup.Provider registries = player.level().registryAccess();
        DripNullData data = DripNullData.get(dripStack, tier, registries);
        if (data.profiles().isEmpty()) {
            return Result.fail(Component.translatable("item.deepnullreforged.drip_null.clear_empty").withStyle(ChatFormatting.GRAY));
        }
        int selectedIndex = data.selectedProfile();
        DripProfile selected = data.selected();
        if (selected.empty()) {
            return Result.fail(Component.translatable("item.deepnullreforged.drip_null.clear_empty").withStyle(ChatFormatting.GRAY));
        }

        Map<String, ItemStack> vault = new LinkedHashMap<>(data.vaultItems());
        if (data.equippedProfile() == selectedIndex) {
            Result validation = validateSlots(player, selected);
            if (!validation.success()) {
                return validation;
            }
            vault = vaultAfterStowing(player, selected, vault);
        }

        Set<String> sharedRefs = refsUsedOutside(data.profiles(), selectedIndex);
        List<ItemStack> loose = new ArrayList<>(data.looseItems());
        for (DripSlotAssignment assignment : selected.slots()) {
            if (sharedRefs.contains(assignment.ref())) {
                continue;
            }
            ItemStack stored = vault.remove(assignment.ref());
            if (stored != null && !stored.isEmpty()) {
                loose.add(stored.copy());
            }
        }
        if (loose.size() > DripNullData.MAX_LOOSE_ITEMS) {
            return Result.fail(Component.translatable("item.deepnullreforged.drip_null.swap_loose_full"));
        }

        if (data.equippedProfile() == selectedIndex) {
            for (DripSlotAssignment assignment : selected.slots()) {
                DripSlotProviders.set(player, assignment.slot(), ItemStack.EMPTY);
            }
        }

        DripProfile cleared = selected.withSlots(List.of());
        List<DripProfile> profiles = replaceProfile(data.profiles(), selectedIndex, cleared);
        DripNullData updated = data.withState(selectedIndex, data.equippedProfile() == selectedIndex ? -1 : data.equippedProfile(),
                profiles, vault, loose, data.nextRef());
        DripNullData.set(dripStack, updated, tier, registries);
        sync(player);
        return Result.success(Component.translatable("item.deepnullreforged.drip_null.cleared", selected.name()));
    }

    private static Result capture(ServerPlayer player, ItemStack dripStack, DeepNullTier tier, DripNullData data, DripProfile selected, int reservedInventorySlot, HolderLookup.Provider registries) {
        List<DripSlotAssignment> assignments = new ArrayList<>();
        Map<String, ItemStack> vault = new LinkedHashMap<>(data.vaultItems());
        List<DripSlotRef> slots = DripSlotProviders.slotsForCapture(player, selected, reservedInventorySlot);
        int nextRef = data.nextRef();
        for (DripSlotRef ref : slots) {
            ItemStack stack = DripSlotProviders.get(player, ref);
            if (stack.isEmpty() || stack.getItem() instanceof DripNullItem) {
                continue;
            }
            String id = "ref" + nextRef++;
            vault.put(id, stack.copy());
            assignments.add(new DripSlotAssignment(ref, id));
        }
        if (assignments.isEmpty()) {
            return Result.fail(Component.translatable("item.deepnullreforged.drip_null.capture_empty").withStyle(ChatFormatting.GRAY));
        }
        for (DripSlotAssignment assignment : assignments) {
            DripSlotProviders.set(player, assignment.slot(), ItemStack.EMPTY);
        }
        DripProfile captured = selected.withSlots(assignments);
        DripNullData updated = data.withProfile(data.selectedProfile(), captured)
                .withState(data.selectedProfile(), -1, replaceProfile(data.profiles(), data.selectedProfile(), captured), vault, data.looseItems(), nextRef);
        DripNullData.set(dripStack, updated, tier, registries);
        sync(player);
        return Result.success(Component.translatable("item.deepnullreforged.drip_null.captured", assignments.size()));
    }

    private static Result stowSelected(ServerPlayer player, ItemStack dripStack, DeepNullTier tier, DripNullData data, DripProfile selected, HolderLookup.Provider registries) {
        Result validation = validateSlots(player, selected);
        if (!validation.success()) {
            return validation;
        }
        Map<String, ItemStack> vault = vaultAfterStowing(player, selected, new LinkedHashMap<>(data.vaultItems()));
        List<DripSlotAssignment> keptAssignments = new ArrayList<>();
        for (DripSlotAssignment assignment : selected.slots()) {
            if (vault.containsKey(assignment.ref())) {
                keptAssignments.add(assignment);
            }
        }
        for (DripSlotAssignment assignment : selected.slots()) {
            DripSlotProviders.set(player, assignment.slot(), ItemStack.EMPTY);
        }
        DripProfile stowed = selected.withSlots(keptAssignments);
        List<DripProfile> profiles = replaceProfile(data.profiles(), data.selectedProfile(), stowed);
        DripNullData updated = data.withState(data.selectedProfile(), -1, profiles, vault, data.looseItems(), data.nextRef());
        DripNullData.set(dripStack, updated, tier, registries);
        sync(player);
        return Result.success(Component.translatable("item.deepnullreforged.drip_null.stowed", stowed.name()));
    }

    private static Result swap(ServerPlayer player, ItemStack dripStack, DeepNullTier tier, DripNullData data, DripProfile selected, HolderLookup.Provider registries) {
        Map<String, ItemStack> vault = new LinkedHashMap<>(data.vaultItems());
        List<ItemStack> loose = new ArrayList<>(data.looseItems());
        Map<DripSlotRef, ItemStack> simulated = new LinkedHashMap<>();
        DripProfile equipped = data.equipped();
        Set<String> targetRefs = new HashSet<>();

        for (DripSlotAssignment assignment : selected.slots()) {
            if (!targetRefs.add(assignment.ref())) {
                return Result.fail(Component.translatable("item.deepnullreforged.drip_null.swap_duplicate_ref"));
            }
            if (!DripSlotProviders.hasSlot(player, assignment.slot())) {
                return Result.fail(Component.translatable("item.deepnullreforged.drip_null.swap_missing_slot"));
            }
            simulated.put(assignment.slot(), DripSlotProviders.get(player, assignment.slot()).copy());
        }

        if (equipped != null) {
            for (DripSlotAssignment assignment : equipped.slots()) {
                if (!DripSlotProviders.hasSlot(player, assignment.slot())) {
                    return Result.fail(Component.translatable("item.deepnullreforged.drip_null.swap_missing_slot"));
                }
                simulated.putIfAbsent(assignment.slot(), DripSlotProviders.get(player, assignment.slot()).copy());
                ItemStack current = simulated.get(assignment.slot());
                if (!current.isEmpty()) {
                    vault.put(assignment.ref(), current.copy());
                } else {
                    vault.remove(assignment.ref());
                }
                simulated.put(assignment.slot(), ItemStack.EMPTY);
            }
        }

        for (DripSlotAssignment assignment : selected.slots()) {
            ItemStack target = vault.remove(assignment.ref());
            if (target == null || target.isEmpty()) {
                return Result.fail(Component.translatable("item.deepnullreforged.drip_null.swap_missing_item"));
            }
            ItemStack displaced = simulated.getOrDefault(assignment.slot(), ItemStack.EMPTY);
            if (!displaced.isEmpty()) {
                if (loose.size() >= DripNullData.MAX_LOOSE_ITEMS) {
                    return Result.fail(Component.translatable("item.deepnullreforged.drip_null.swap_loose_full"));
                }
                loose.add(displaced.copy());
            }
            simulated.put(assignment.slot(), target.copy());
        }

        for (Map.Entry<DripSlotRef, ItemStack> entry : simulated.entrySet()) {
            DripSlotProviders.set(player, entry.getKey(), entry.getValue().copy());
        }
        DripNullData updated = data.withState(data.selectedProfile(), data.selectedProfile(), data.profiles(), vault, loose, data.nextRef());
        DripNullData.set(dripStack, updated, tier, registries);
        sync(player);
        return Result.success(Component.translatable("item.deepnullreforged.drip_null.swapped", data.selected().name()));
    }

    private static Result validateSlots(ServerPlayer player, DripProfile profile) {
        for (DripSlotAssignment assignment : profile.slots()) {
            if (!DripSlotProviders.hasSlot(player, assignment.slot())) {
                return Result.fail(Component.translatable("item.deepnullreforged.drip_null.swap_missing_slot"));
            }
        }
        return Result.success(Component.empty());
    }

    private static Map<String, ItemStack> vaultAfterStowing(ServerPlayer player, DripProfile profile, Map<String, ItemStack> vault) {
        Map<String, ItemStack> updated = new LinkedHashMap<>(vault);
        for (DripSlotAssignment assignment : profile.slots()) {
            ItemStack current = DripSlotProviders.get(player, assignment.slot()).copy();
            if (!current.isEmpty() && !(current.getItem() instanceof DripNullItem)) {
                updated.put(assignment.ref(), current);
            } else {
                updated.remove(assignment.ref());
            }
        }
        return updated;
    }

    private static Set<String> refsUsedOutside(List<DripProfile> profiles, int excludedProfile) {
        Set<String> refs = new HashSet<>();
        for (int i = 0; i < profiles.size(); i++) {
            if (i == excludedProfile) {
                continue;
            }
            for (DripSlotAssignment assignment : profiles.get(i).slots()) {
                refs.add(assignment.ref());
            }
        }
        return refs;
    }

    private static List<DripProfile> replaceProfile(List<DripProfile> profiles, int index, DripProfile profile) {
        List<DripProfile> updated = new ArrayList<>(profiles);
        if (index >= 0 && index < updated.size()) {
            updated.set(index, profile);
        }
        return updated;
    }

    private static boolean canFitPlayerInventory(ServerPlayer player, List<ItemStack> stacks) {
        List<ItemStack> simulated = new ArrayList<>();
        for (int slot = 0; slot < 36; slot++) {
            simulated.add(player.getInventory().getItem(slot).copy());
        }
        for (ItemStack source : stacks) {
            ItemStack remaining = source.copy();
            for (ItemStack existing : simulated) {
                if (remaining.isEmpty()) {
                    break;
                }
                if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, remaining)) {
                    int moved = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                    if (moved > 0) {
                        existing.grow(moved);
                        remaining.shrink(moved);
                    }
                }
            }
            for (int i = 0; i < simulated.size() && !remaining.isEmpty(); i++) {
                if (simulated.get(i).isEmpty()) {
                    int moved = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                    simulated.set(i, remaining.copyWithCount(moved));
                    remaining.shrink(moved);
                }
            }
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void sync(ServerPlayer player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
    }

    public record Result(boolean success, Component message) {
        public static Result success(Component message) {
            return new Result(true, message);
        }

        public static Result fail(Component message) {
            return new Result(false, message);
        }
    }
}
