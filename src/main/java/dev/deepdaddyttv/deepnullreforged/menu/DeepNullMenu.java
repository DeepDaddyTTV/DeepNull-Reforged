package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public class DeepNullMenu extends AbstractContainerMenu {
    private static final int SLOT_SPACING = 21;
    private static final int TOP_PADDING = 19;
    private static final int LEFT_PADDING = 9;

    public enum SourceType {
        ITEM,
        DOCK
    }

    private final SourceType sourceType;
    private final DeepNullTier tier;
    private final DeepNullInventory dankInventory;
    private final int inventorySlot;
    private final @Nullable BlockPos dockPos;

    public static DeepNullMenu forItem(int containerId, Inventory playerInventory, int inventorySlot, DeepNullTier tier) {
        ItemStack stack = playerInventory.getItem(inventorySlot);
        DeepNullInventory inventory = new DeepNullInventory(tier, stack, playerInventory.player.level().registryAccess(), null);
        return new DeepNullMenu(containerId, playerInventory, SourceType.ITEM, tier, inventory, inventorySlot, null);
    }

    public static DeepNullMenu forDock(int containerId, Inventory playerInventory, DeepNullDockBlockEntity dock) {
        DeepNullInventory inventory = dock.createInventory();
        return new DeepNullMenu(
                containerId,
                playerInventory,
                SourceType.DOCK,
                dock.getTier(),
                inventory == null ? DeepNullInventory.client(dock.getTier()) : inventory,
                -1,
                dock.getBlockPos()
        );
    }

    public DeepNullMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(
                containerId,
                playerInventory,
                SourceType.values()[buffer.readVarInt()],
                DeepNullTier.byId(buffer.readVarInt()),
                null,
                buffer.readVarInt(),
                buffer.readBlockPos()
        );
    }

    private DeepNullMenu(
            int containerId,
            Inventory playerInventory,
            SourceType sourceType,
            DeepNullTier tier,
            @Nullable DeepNullInventory inventory,
            int inventorySlot,
            @Nullable BlockPos dockPos
    ) {
        super(ModMenus.DEEP_NULL_MENU.get(), containerId);
        this.sourceType = sourceType;
        this.tier = tier;
        this.inventorySlot = inventorySlot;
        this.dockPos = dockPos;
        this.dankInventory = inventory == null
                ? resolveClientInventory(playerInventory, sourceType, tier, inventorySlot, dockPos)
                : inventory;

        addDeepNullSlots();
        addPlayerInventorySlots(playerInventory);

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return DeepNullMenu.this.dankInventory.getSelectedSlot();
            }

            @Override
            public void set(int value) {
                DeepNullMenu.this.dankInventory.setSelectedSlot(value);
            }
        });
    }

    public DeepNullTier getTier() {
        return tier;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public DeepNullInventory getDankInventory() {
        return dankInventory;
    }

    public int getInventorySlot() {
        return inventorySlot;
    }

    public @Nullable BlockPos getDockPos() {
        return dockPos;
    }

    public int getStorageSlotCount() {
        return tier.slotCount();
    }

    public boolean selectStorageSlot(int slot) {
        if (!isStorageSlot(slot)) {
            return false;
        }
        dankInventory.setSelectedSlot(slot);
        return true;
    }

    public boolean cycleExtractionMode(int slot, boolean forward) {
        if (!isStorageSlot(slot) || dankInventory.getStackInSlot(slot).isEmpty()) {
            return false;
        }
        dankInventory.cycleExtractionMode(slot, forward);
        return true;
    }

    public boolean cyclePlacementMode(int slot, boolean forward) {
        if (!isStorageSlot(slot) || dankInventory.getStackInSlot(slot).isEmpty()) {
            return false;
        }
        dankInventory.cyclePlacementMode(slot, forward);
        return true;
    }

    public boolean toggleTagMatching(int slot) {
        if (!isStorageSlot(slot) || dankInventory.getStackInSlot(slot).isEmpty() || !dankInventory.supportsTagMatching(slot)) {
            return false;
        }
        dankInventory.toggleTagMatching(slot);
        return true;
    }

    public boolean moveStorageSlot(int fromSlot, int toSlot) {
        if (!isStorageSlot(fromSlot) || !isStorageSlot(toSlot)) {
            return false;
        }
        return dankInventory.moveSlot(fromSlot, toSlot);
    }

    public boolean setLocked(boolean locked) {
        if (!dankInventory.supportsLocking()) {
            return false;
        }
        dankInventory.setLocked(locked);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!player.isAlive()) {
            return false;
        }

        if (sourceType == SourceType.ITEM) {
            if (inventorySlot < 0 || inventorySlot >= player.getInventory().getContainerSize()) {
                return false;
            }
            return player.getInventory().getItem(inventorySlot).getItem() instanceof DeepNullItem;
        }

        if (dockPos == null) {
            return false;
        }

        if (!(player.level().getBlockEntity(dockPos) instanceof DeepNullDockBlockEntity dock)) {
            return false;
        }

        return player.distanceToSqr(dockPos.getCenter()) <= 64.0D && dock.hasStoredDeepNull();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        ItemStack original = stackInSlot.copy();
        int dankSlots = getStorageSlotCount();

        if (index < dankSlots) {
            ItemStack movable = dankInventory.getExtractableStackInSlot(index);
            if (movable.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack remaining = movable.copy();
            if (!moveItemStackTo(remaining, dankSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            int moved = movable.getCount() - remaining.getCount();
            if (moved <= 0) {
                return ItemStack.EMPTY;
            }
            dankInventory.extractItem(index, moved, false);
            slot.setChanged();
            return original;
        }

        ItemStack remaining = dankInventory.insertIntoFirstAvailableSlot(stackInSlot, false);
        if (remaining.getCount() == stackInSlot.getCount()) {
            return ItemStack.EMPTY;
        }

        if (remaining.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.set(remaining);
        }
        slot.setChanged();
        return original;
    }

    private void addDeepNullSlots() {
        for (int row = 0; row < tier.rows(); row++) {
            for (int column = 0; column < 9; column++) {
                int slotIndex = row * 9 + column;
                addSlot(sourceType == SourceType.DOCK
                        ? new DockSlot(dankInventory, slotIndex, LEFT_PADDING + column * SLOT_SPACING, TOP_PADDING + row * SLOT_SPACING)
                        : new SlotItemHandler(dankInventory, slotIndex, LEFT_PADDING + column * SLOT_SPACING, TOP_PADDING + row * SLOT_SPACING));
            }
        }
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        int startY = 28 + tier.rows() * SLOT_SPACING;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = column + row * 9 + 9;
                addSlot(createPlayerSlot(playerInventory, index, LEFT_PADDING + column * SLOT_SPACING, startY + row * SLOT_SPACING));
            }
        }

        int hotbarY = 95 + tier.rows() * SLOT_SPACING;
        for (int column = 0; column < 9; column++) {
            addSlot(createPlayerSlot(playerInventory, column, LEFT_PADDING + column * SLOT_SPACING, hotbarY));
        }
    }

    private Slot createPlayerSlot(Inventory inventory, int slotIndex, int x, int y) {
        if (sourceType == SourceType.ITEM && slotIndex == inventorySlot) {
            return new LockedPlayerSlot(inventory, slotIndex, x, y);
        }
        return new Slot(inventory, slotIndex, x, y);
    }

    private boolean isStorageSlot(int slot) {
        return slot >= 0 && slot < getStorageSlotCount();
    }

    private static DeepNullInventory resolveClientInventory(
            Inventory playerInventory,
            SourceType sourceType,
            DeepNullTier tier,
            int inventorySlot,
            @Nullable BlockPos dockPos
    ) {
        if (sourceType == SourceType.ITEM) {
            if (inventorySlot >= 0 && inventorySlot < playerInventory.getContainerSize()) {
                ItemStack stack = playerInventory.getItem(inventorySlot);
                if (stack.getItem() instanceof DeepNullItem deepNullItem) {
                    return new DeepNullInventory(deepNullItem.tier(), stack, playerInventory.player.level().registryAccess(), null);
                }
            }
            return DeepNullInventory.client(tier);
        }

        if (dockPos != null && playerInventory.player.level().getBlockEntity(dockPos) instanceof DeepNullDockBlockEntity dock) {
            DeepNullInventory inventory = dock.createInventory();
            if (inventory != null) {
                return inventory;
            }
        }
        return DeepNullInventory.client(tier);
    }

    private static final class LockedPlayerSlot extends Slot {
        private LockedPlayerSlot(Inventory container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    private static final class DockSlot extends SlotItemHandler {
        private DockSlot(DeepNullInventory itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPickup(Player player) {
            return !((DeepNullInventory) getItemHandler()).extractItemIgnoreExtractionMode(index, 1, true).isEmpty();
        }

        @Override
        public ItemStack remove(int amount) {
            return ((DeepNullInventory) getItemHandler()).extractItemIgnoreExtractionMode(index, amount, false);
        }
    }
}
