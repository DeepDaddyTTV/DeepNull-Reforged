package dev.deepdaddyttv.deepnullreforged.integration.ae2;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class Ae2TransferCompat {
    private Ae2TransferCompat() {
    }

    public static @Nullable InteractionResult tryShiftItemTransfer(UseOnContext context, DeepNullInventory inventory) {
        MEStorage storage = getStorage(context);
        if (storage == null) {
            return null;
        }

        if (context.getLevel().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        IActionSource actionSource = context.getPlayer() != null
                ? IActionSource.ofPlayer(context.getPlayer())
                : IActionSource.empty();
        boolean moved = false;
        if (inventory.getTransferDirectionMode().allowsInsert()) {
            moved = moveItemsToStorage(inventory, storage, actionSource);
        }
        if (!moved && inventory.getTransferDirectionMode().allowsExtract()) {
            moved = moveItemsFromStorage(inventory, storage, actionSource);
        }
        return moved ? InteractionResult.sidedSuccess(false) : InteractionResult.FAIL;
    }

    public static @Nullable InteractionResult tryShiftFluidTransfer(UseOnContext context, DeepNullInventory inventory) {
        MEStorage storage = getStorage(context);
        if (storage == null) {
            return null;
        }

        if (context.getLevel().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        IActionSource actionSource = context.getPlayer() != null
                ? IActionSource.ofPlayer(context.getPlayer())
                : IActionSource.empty();
        boolean moved = false;
        if (inventory.getTransferDirectionMode().allowsInsert()) {
            moved = moveFluidsToStorage(inventory, storage, actionSource);
        }
        if (!moved && inventory.getTransferDirectionMode().allowsExtract()) {
            moved = moveFluidsFromStorage(inventory, storage, actionSource);
        }
        return moved ? InteractionResult.sidedSuccess(false) : InteractionResult.FAIL;
    }

    public static @Nullable MEStorage getStorage(UseOnContext context) {
        MEStorage storage = context.getLevel().getCapability(AECapabilities.ME_STORAGE, context.getClickedPos(), context.getClickedFace());
        if (storage == null) {
            storage = context.getLevel().getCapability(AECapabilities.ME_STORAGE, context.getClickedPos(), null);
        }
        return storage;
    }

    public static boolean moveItemsToStorage(DeepNullInventory inventory, MEStorage storage, IActionSource actionSource) {
        if (inventory.getTransferOutputMode().isLocked()) {
            return false;
        }
        boolean movedAny = false;
        boolean progressed;
        do {
            progressed = false;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack extractable = inventory.getExtractableStackInSlot(slot);
                if (extractable.isEmpty()) {
                    continue;
                }

                AEItemKey key = AEItemKey.of(extractable);
                if (key == null) {
                    continue;
                }

                long inserted = storage.insert(key, extractable.getCount(), Actionable.MODULATE, actionSource);
                if (inserted <= 0) {
                    continue;
                }

                inventory.extractItem(slot, (int) Math.min(inserted, Integer.MAX_VALUE), false);
                movedAny = true;
                progressed = true;
            }
        } while (progressed);
        return movedAny;
    }

    public static boolean moveItemsFromStorage(DeepNullInventory inventory, MEStorage storage, IActionSource actionSource) {
        return refillStoredItemTypes(inventory, storage, actionSource);
    }

    public static boolean refillStoredItemTypes(DeepNullInventory inventory, MEStorage storage, IActionSource actionSource) {
        boolean movedAny = false;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stored = inventory.getStackInSlot(slot);
            if (stored.isEmpty()) {
                continue;
            }

            int space = inventory.getSlotLimit(slot) - stored.getCount();
            if (space <= 0) {
                continue;
            }

            AEItemKey key = AEItemKey.of(stored);
            if (key == null) {
                continue;
            }

            long moved = storage.extract(key, space, Actionable.MODULATE, actionSource);
            if (moved <= 0) {
                continue;
            }

            inventory.insertItem(slot, key.toStack((int) Math.min(moved, Integer.MAX_VALUE)), false);
            movedAny = true;
        }
        return movedAny;
    }

    public static boolean moveFluidsToStorage(DeepNullInventory inventory, MEStorage storage, IActionSource actionSource) {
        if (inventory.getTransferOutputMode().isLocked()) {
            return false;
        }
        boolean movedAny = false;
        boolean progressed;
        do {
            progressed = false;
            for (int slot = 0; slot < inventory.getFluidSlotCount(); slot++) {
                FluidStack stored = inventory.getFluidInSlot(slot);
                if (stored.isEmpty()) {
                    continue;
                }

                AEFluidKey key = AEFluidKey.of(stored);
                if (key == null) {
                    continue;
                }
                if (inventory.getTransferOutputMode().matchingOnly()
                        && storage.extract(key, 1, Actionable.SIMULATE, actionSource) <= 0L) {
                    continue;
                }

                long inserted = storage.insert(key, stored.getAmount(), Actionable.MODULATE, actionSource);
                if (inserted <= 0) {
                    continue;
                }

                inventory.drainFluid(slot, (int) Math.min(inserted, Integer.MAX_VALUE), false);
                movedAny = true;
                progressed = true;
            }
        } while (progressed);
        return movedAny;
    }

    public static boolean moveFluidsFromStorage(DeepNullInventory inventory, MEStorage storage, IActionSource actionSource) {
        return refillStoredFluidTypes(inventory, storage, actionSource);
    }

    public static boolean refillStoredFluidTypes(DeepNullInventory inventory, MEStorage storage, IActionSource actionSource) {
        boolean movedAny = false;
        for (int slot = 0; slot < inventory.getFluidSlotCount(); slot++) {
            FluidStack stored = inventory.getFluidInSlot(slot);
            if (stored.isEmpty()) {
                continue;
            }

            int space = inventory.getFluidCapacity() - stored.getAmount();
            if (space <= 0) {
                continue;
            }

            AEFluidKey key = AEFluidKey.of(stored);
            if (key == null) {
                continue;
            }

            long moved = storage.extract(key, space, Actionable.MODULATE, actionSource);
            if (moved <= 0) {
                continue;
            }

            inventory.fillFluid(slot, key.toStack((int) Math.min(moved, Integer.MAX_VALUE)), false);
            movedAny = true;
        }
        return movedAny;
    }

}
