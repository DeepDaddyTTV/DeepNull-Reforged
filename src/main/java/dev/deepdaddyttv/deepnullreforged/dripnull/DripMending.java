package dev.deepdaddyttv.deepnullreforged.dripnull;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DripNullItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DripMending {
    private static final int CARRIED_REPAIR_PER_XP = 2;
    private static final int HELD_REPAIR_PER_XP = 4;
    private static final long FOIL_TICKS = 40L;

    private DripMending() {
    }

    public static boolean handlePickup(ServerPlayer player, ExperienceOrb orb) {
        if (player == null || orb == null) {
            return false;
        }
        int xp = Math.max(0, xpValue(orb));
        if (xp <= 0) {
            return false;
        }
        int remaining = xp;
        for (Candidate candidate : candidates(player)) {
            if (remaining <= 0) {
                break;
            }
            RepairResult result = repair(candidate.stack(), candidate.item().tier(), player.level().registryAccess(), remaining, candidate.held(), player.level().getGameTime());
            if (result.consumedXp() > 0) {
                remaining -= result.consumedXp();
                candidate.setter().set(result.stack());
            }
        }
        int consumed = xp - remaining;
        if (consumed <= 0) {
            return false;
        }
        if (remaining > 0) {
            player.giveExperiencePoints(remaining);
        }
        orb.discard();
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        return true;
    }

    public static ItemStack clearExpiredFoil(ItemStack stack, DeepNullTier tier, HolderLookup.Provider registries, long gameTime) {
        if (!(stack.getItem() instanceof DripNullItem)) {
            return stack;
        }
        DripNullData data = DripNullData.get(stack, tier, registries);
        if (data.mendingActiveUntil() > 0L && data.mendingActiveUntil() <= gameTime) {
            DripNullData.set(stack, data.withMendingActiveUntil(0L), tier, registries);
        }
        return stack;
    }

    private static RepairResult repair(ItemStack dripStack, DeepNullTier tier, HolderLookup.Provider registries, int availableXp, boolean held, long gameTime) {
        DripNullData data = DripNullData.get(dripStack, tier, registries);
        if (!data.upgrades().has(DripNullUpgradeType.MEND)) {
            return new RepairResult(dripStack, 0);
        }
        int xp = availableXp;
        int repairPerXp = held ? HELD_REPAIR_PER_XP : CARRIED_REPAIR_PER_XP;
        Map<String, ItemStack> vault = new LinkedHashMap<>();
        for (Map.Entry<String, ItemStack> entry : data.vaultItems().entrySet()) {
            RepairStackResult result = repairStack(entry.getValue(), xp, repairPerXp);
            xp -= result.consumedXp();
            vault.put(entry.getKey(), result.stack());
        }
        List<ItemStack> loose = new ArrayList<>();
        for (ItemStack stack : data.looseItems()) {
            RepairStackResult result = repairStack(stack, xp, repairPerXp);
            xp -= result.consumedXp();
            loose.add(result.stack());
        }
        int consumed = availableXp - xp;
        if (consumed <= 0) {
            if (data.mendingActiveUntil() > 0L && data.mendingActiveUntil() <= gameTime) {
                DripNullData.set(dripStack, data.withMendingActiveUntil(0L), tier, registries);
            }
            return new RepairResult(dripStack, 0);
        }
        DripNullData updated = data.withState(data.selectedProfile(), data.equippedProfile(), data.profiles(), vault, loose, data.nextRef())
                .withMendingActiveUntil(gameTime + FOIL_TICKS);
        DripNullData.set(dripStack, updated, tier, registries);
        return new RepairResult(dripStack, consumed);
    }

    private static RepairStackResult repairStack(ItemStack stack, int availableXp, int repairPerXp) {
        if (availableXp <= 0 || stack.isEmpty() || !stack.isDamageableItem() || !stack.isDamaged()) {
            return new RepairStackResult(stack.copy(), 0);
        }
        int damage = stack.getDamageValue();
        int repair = Math.min(damage, availableXp * repairPerXp);
        int consumedXp = (repair + repairPerXp - 1) / repairPerXp;
        ItemStack updated = stack.copy();
        updated.setDamageValue(Math.max(0, damage - repair));
        return new RepairStackResult(updated, consumedXp);
    }

    private static List<Candidate> candidates(ServerPlayer player) {
        List<Candidate> candidates = new ArrayList<>();
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof DripNullItem item) {
            candidates.add(new Candidate(main, item, true, updated -> player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, updated)));
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof DripNullItem item) {
            candidates.add(new Candidate(offhand, item, true, updated -> player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, updated)));
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack == main || stack == offhand || !(stack.getItem() instanceof DripNullItem item)) {
                continue;
            }
            int inventorySlot = slot;
            candidates.add(new Candidate(stack, item, false, updated -> player.getInventory().setItem(inventorySlot, updated)));
        }
        return candidates;
    }

    private static int xpValue(ExperienceOrb orb) {
        try {
            Field field = ExperienceOrb.class.getDeclaredField("value");
            field.setAccessible(true);
            return field.getInt(orb);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return 0;
        }
    }

    private record RepairResult(ItemStack stack, int consumedXp) {
    }

    private record RepairStackResult(ItemStack stack, int consumedXp) {
    }

    private record Candidate(ItemStack stack, DripNullItem item, boolean held, StackSetter setter) {
    }

    private interface StackSetter {
        void set(ItemStack stack);
    }
}
