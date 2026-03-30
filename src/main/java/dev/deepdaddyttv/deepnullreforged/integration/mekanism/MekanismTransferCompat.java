package dev.deepdaddyttv.deepnullreforged.integration.mekanism;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.Nullable;

public final class MekanismTransferCompat {
    private MekanismTransferCompat() {
    }

    public static @Nullable InteractionResult tryShiftChemicalTransfer(UseOnContext context, DeepNullInventory inventory) {
        if (!inventory.supportsChemicalStorage()) {
            return null;
        }
        IChemicalHandler target = MekanismCompat.getBlockChemicalHandler(context);
        if (target == null) {
            return null;
        }

        if (context.getLevel().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        boolean moved = false;
        if (inventory.getTransferDirectionMode().allowsInsert()) {
            moved = moveChemicalsToTarget(inventory, target);
        }
        if (!moved && inventory.getTransferDirectionMode().allowsExtract()) {
            moved = moveChemicalsFromTarget(inventory, target);
        }
        return moved ? InteractionResult.sidedSuccess(false) : InteractionResult.FAIL;
    }

    public static boolean moveChemicalsToTarget(DeepNullInventory inventory, IChemicalHandler target) {
        if (inventory.getTransferOutputMode().isLocked()) {
            return false;
        }
        boolean movedAny = false;
        boolean progressed;
        do {
            progressed = false;
            for (int slot = 0; slot < inventory.getFluidSlotCount(); slot++) {
                StoredChemical stored = inventory.getChemicalInSlot(slot);
                if (stored.isEmpty()) {
                    continue;
                }

                ChemicalStack chemical = MekanismCompat.toChemicalStack(inventory, stored);
                if (chemical.isEmpty()) {
                    continue;
                }
                if (inventory.getTransferOutputMode().matchingOnly() && !targetContainsMatchingChemical(target, chemical)) {
                    continue;
                }

                ChemicalStack remainder = target.insertChemical(chemical.copy(), Action.SIMULATE);
                long inserted = chemical.getAmount() - remainder.getAmount();
                if (inserted <= 0L) {
                    continue;
                }

                StoredChemical drained = inventory.drainChemical(slot, inserted, false);
                if (drained.isEmpty()) {
                    continue;
                }

                ChemicalStack acceptedRemainder = target.insertChemical(MekanismCompat.toChemicalStack(inventory, drained), Action.EXECUTE);
                long accepted = drained.amount() - acceptedRemainder.getAmount();
                if (accepted <= 0L) {
                    inventory.fillChemical(slot, drained, false);
                    continue;
                }

                if (accepted < drained.amount()) {
                    inventory.fillChemical(slot, drained.copyWithAmount(drained.amount() - accepted), false);
                }
                movedAny = true;
                progressed = true;
            }
        } while (progressed);
        return movedAny;
    }

    private static boolean targetContainsMatchingChemical(IChemicalHandler target, ChemicalStack candidate) {
        for (int tank = 0; tank < target.getChemicalTanks(); tank++) {
            ChemicalStack targetStack = target.getChemicalInTank(tank);
            if (!targetStack.isEmpty() && ChemicalStack.isSameChemical(targetStack, candidate)) {
                return true;
            }
        }
        return false;
    }

    public static boolean moveChemicalsFromTarget(DeepNullInventory inventory, IChemicalHandler target) {
        boolean movedAny = false;
        boolean progressed;
        do {
            progressed = false;
            for (int tank = 0; tank < target.getChemicalTanks(); tank++) {
                ChemicalStack available = target.getChemicalInTank(tank);
                if (available.isEmpty()) {
                    continue;
                }

                StoredChemical stored = MekanismCompat.fromChemicalStack(available);
                int targetSlot = inventory.findMatchingChemicalSlot(stored);
                if (targetSlot < 0) {
                    continue;
                }
                int accepted = inventory.fillChemical(targetSlot, stored, true);
                if (accepted <= 0) {
                    continue;
                }

                ChemicalStack drained = target.extractChemical(tank, accepted, Action.EXECUTE);
                if (drained.isEmpty()) {
                    continue;
                }

                int inserted = inventory.fillChemical(targetSlot, MekanismCompat.fromChemicalStack(drained), false);
                if (inserted <= 0) {
                    target.insertChemical(drained, Action.EXECUTE);
                    continue;
                }

                if (inserted < drained.getAmount()) {
                    target.insertChemical(drained.copyWithAmount(drained.getAmount() - inserted), Action.EXECUTE);
                }
                if (inventory.getSelectedChemical().isEmpty()) {
                    inventory.setSelectedSlot(targetSlot);
                }
                movedAny = true;
                progressed = true;
            }
        } while (progressed);
        return movedAny;
    }
}
