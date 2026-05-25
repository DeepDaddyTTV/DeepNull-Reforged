package dev.deepdaddyttv.deepnullreforged.dumpnull;

import dev.deepdaddyttv.deepnullreforged.item.DumpNullItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class DumpNullHeldPickup {
    private DumpNullHeldPickup() {
    }

    public static Result tryHandle(Player player, ItemEntity itemEntity) {
        ActiveDumpNull active = firstActiveDumpNull(player);
        if (active == null) {
            return Result.PASS;
        }
        ItemStack incoming = itemEntity.getItem();
        if (incoming.isEmpty()) {
            return Result.PASS;
        }
        if (DumpNullRuleMatcher.actionFor(active.data(), incoming) != DumpNullRuleAction.VOID) {
            return Result.PASS;
        }
        return switch (active.data().heldDiscardMode()) {
            case VOID -> {
                itemEntity.discard();
                yield Result.CONSUMED;
            }
            case BLOCK_PICKUP -> Result.BLOCKED;
            case GENERATE_FE -> generateEnergy(active, itemEntity);
        };
    }

    private static Result generateEnergy(ActiveDumpNull active, ItemEntity itemEntity) {
        DumpNullData data = active.data();
        if (!data.hasUpgrade(DumpNullUpgradeType.POWER)) {
            return Result.BLOCKED;
        }
        int reward = DumpNullEnergyRewardResolver.rewardFor(itemEntity.getItem());
        DumpNullData.EnergyMutation mutation = data.generateEnergy(reward, false);
        if (mutation.amount() <= 0) {
            return Result.BLOCKED;
        }
        DumpNullData.set(active.stack(), mutation.data());
        itemEntity.discard();
        return Result.CONSUMED;
    }

    private static ActiveDumpNull firstActiveDumpNull(Player player) {
        ActiveDumpNull mainHand = active(player.getMainHandItem());
        if (mainHand != null) {
            return mainHand;
        }
        ActiveDumpNull offhand = active(player.getOffhandItem());
        if (offhand != null) {
            return offhand;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ActiveDumpNull inventory = active(player.getInventory().getItem(slot));
            if (inventory != null) {
                return inventory;
            }
        }
        return null;
    }

    private static ActiveDumpNull active(ItemStack stack) {
        if (!(stack.getItem() instanceof DumpNullItem)) {
            return null;
        }
        DumpNullData data = DumpNullData.get(stack);
        return data.active() ? new ActiveDumpNull(stack, data) : null;
    }

    public enum Result {
        PASS,
        BLOCKED,
        CONSUMED
    }

    private record ActiveDumpNull(ItemStack stack, DumpNullData data) {
    }
}
