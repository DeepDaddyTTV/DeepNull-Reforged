package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullFilterMode;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.capability.DeepNullFluidHandler;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
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

    public enum ViewMode {
        MAIN,
        UPGRADES,
        FILTER,
        FLUID
    }

    private final SourceType sourceType;
    private final ViewMode viewMode;
    private final DeepNullTier tier;
    private final DeepNullInventory dankInventory;
    private final int inventorySlot;
    private final @Nullable BlockPos dockPos;
    private final int storageSlotCount;
    private final int upgradeSlotStartIndex;
    private final int upgradeSlotCount;
    private final int playerInventorySlotStartIndex;
    private final SimpleContainer fluidSlotContainer;

    public static DeepNullMenu forItem(int containerId, Inventory playerInventory, int inventorySlot, DeepNullTier tier) {
        return forItem(containerId, playerInventory, inventorySlot, tier, ViewMode.MAIN);
    }

    public static DeepNullMenu forItem(int containerId, Inventory playerInventory, int inventorySlot, DeepNullTier tier, ViewMode viewMode) {
        ItemStack stack = playerInventory.getItem(inventorySlot);
        DeepNullInventory inventory = new DeepNullInventory(tier, stack, playerInventory.player.level().registryAccess(), null);
        return new DeepNullMenu(containerId, playerInventory, SourceType.ITEM, viewMode, tier, inventory, inventorySlot, null);
    }

    public static DeepNullMenu forDock(int containerId, Inventory playerInventory, DeepNullDockBlockEntity dock) {
        return forDock(containerId, playerInventory, dock, ViewMode.MAIN);
    }

    public static DeepNullMenu forDock(int containerId, Inventory playerInventory, DeepNullDockBlockEntity dock, ViewMode viewMode) {
        DeepNullInventory inventory = dock.createInventory();
        return new DeepNullMenu(
                containerId,
                playerInventory,
                SourceType.DOCK,
                viewMode,
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
                ViewMode.values()[buffer.readVarInt()],
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
            ViewMode viewMode,
            DeepNullTier tier,
            @Nullable DeepNullInventory inventory,
            int inventorySlot,
            @Nullable BlockPos dockPos
    ) {
        super(ModMenus.DEEP_NULL_MENU.get(), containerId);
        this.sourceType = sourceType;
        this.viewMode = viewMode;
        this.tier = tier;
        this.inventorySlot = inventorySlot;
        this.dockPos = dockPos;
        this.dankInventory = inventory == null
                ? resolveClientInventory(playerInventory, sourceType, tier, inventorySlot, dockPos)
                : inventory;
        this.fluidSlotContainer = new SimpleContainer(tier.slotCount());

        this.storageSlotCount = addDeepNullSlots();
        this.upgradeSlotStartIndex = slots.size();
        this.upgradeSlotCount = addUpgradeSlots();
        this.playerInventorySlotStartIndex = slots.size();
        addPlayerInventorySlots(playerInventory, viewModeRows());

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

    public ViewMode getViewMode() {
        return viewMode;
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
        return storageSlotCount;
    }

    public int getUpgradeSlotStartIndex() {
        return upgradeSlotStartIndex;
    }

    public int getUpgradeSlotCount() {
        return upgradeSlotCount;
    }

    public int getPlayerInventorySlotStartIndex() {
        return playerInventorySlotStartIndex;
    }

    public int getPlayerSlotCount() {
        return 36;
    }

    public boolean hasUpgrade(DeepNullUpgradeType type) {
        return dankInventory.hasUpgrade(type);
    }

    public boolean supportsUpgrade(DeepNullUpgradeType type) {
        return dankInventory.supportsUpgrade(type);
    }

    public DeepNullFilterMode getFilterMode() {
        return dankInventory.getFilterMode();
    }

    public boolean setFilterMode(DeepNullFilterMode mode) {
        if (!dankInventory.supportsFiltering()) {
            return false;
        }
        dankInventory.setFilterMode(mode);
        return true;
    }

    public ItemStack getFilterStack(int slot) {
        return dankInventory.getFilterStack(slot);
    }

    public boolean setFilterStack(int slot, ItemStack stack) {
        if (!dankInventory.supportsFiltering()) {
            return false;
        }
        dankInventory.setFilterStack(slot, stack);
        return true;
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

    public boolean setChargingEnabled(boolean chargingEnabled) {
        if (!dankInventory.hasEnergyUpgrade()) {
            return false;
        }
        dankInventory.setChargingEnabled(chargingEnabled);
        return true;
    }

    public int addGhostFilterStack(ItemStack stack) {
        if (!dankInventory.supportsFiltering() || stack.isEmpty()) {
            return -1;
        }
        int filterSlot = firstEmptyFilterSlot();
        if (filterSlot < 0) {
            return -1;
        }
        dankInventory.setFilterStack(filterSlot, stack.copyWithCount(1));
        return filterSlot;
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
        int storageSlots = getStorageSlotCount();
        int upgradeStart = getUpgradeSlotStartIndex();
        int upgradeEnd = upgradeStart + getUpgradeSlotCount();
        int playerStart = getPlayerInventorySlotStartIndex();
        int playerEnd = playerStart + getPlayerSlotCount();

        if (index < storageSlots) {
            ItemStack movable = dankInventory.getExtractableStackInSlot(index);
            if (movable.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack remaining = movable.copy();
            if (!moveItemStackTo(remaining, playerStart, playerEnd, true)) {
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

        if (index >= upgradeStart && index < upgradeEnd) {
            if (!moveItemStackTo(stackInSlot, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
            slot.setChanged();
            return original;
        }

        if (index < playerStart || index >= playerEnd) {
            return ItemStack.EMPTY;
        }

        if (viewMode == ViewMode.FILTER) {
            if (index >= playerStart && index < playerEnd) {
                int filterSlot = addGhostFilterStack(stackInSlot);
                if (filterSlot < 0) {
                    return ItemStack.EMPTY;
                }
                broadcastChanges();
                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }

        if (moveItemStackTo(stackInSlot, upgradeStart, upgradeEnd, false)) {
            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            return original;
        }

        if (isFluidStorageView()) {
            ItemStack updated = tryStoreFluidFromContainer(stackInSlot, -1, true);
            if (ItemStack.matches(updated, stackInSlot)) {
                return ItemStack.EMPTY;
            }
            slot.set(updated);
            slot.setChanged();
            broadcastChanges();
            return original;
        }

        if (viewMode != ViewMode.MAIN && !isFluidStorageView()) {
            return ItemStack.EMPTY;
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

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (isFluidStorageView() && clickType == ClickType.PICKUP && slotId >= 0 && slotId < storageSlotCount) {
            ItemStack carried = getCarried();
            if (!carried.isEmpty()) {
                ItemStack updated = tryStoreFluidFromContainer(carried, slotId, false);
                if (!ItemStack.matches(updated, carried)) {
                    setCarried(updated);
                    broadcastChanges();
                    return;
                }
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    private int addDeepNullSlots() {
        if (!isStorageView()) {
            return 0;
        }
        for (int row = 0; row < tier.rows(); row++) {
            for (int column = 0; column < 9; column++) {
                int slotIndex = row * 9 + column;
                if (isFluidStorageView()) {
                    addSlot(new FluidStorageSlot(fluidSlotContainer, slotIndex, LEFT_PADDING + column * SLOT_SPACING, TOP_PADDING + row * SLOT_SPACING));
                } else {
                    addSlot(sourceType == SourceType.DOCK
                            ? new DockStorageSlot(dankInventory, slotIndex, LEFT_PADDING + column * SLOT_SPACING, TOP_PADDING + row * SLOT_SPACING)
                            : new StorageSlot(dankInventory, slotIndex, LEFT_PADDING + column * SLOT_SPACING, TOP_PADDING + row * SLOT_SPACING));
                }
            }
        }
        return tier.slotCount();
    }

    private int addUpgradeSlots() {
        if (viewMode != ViewMode.UPGRADES) {
            return 0;
        }
        int[] columns = {0, 1, 2};
        for (DeepNullUpgradeType type : DeepNullUpgradeType.values()) {
            addSlot(new UpgradeSlot(dankInventory, type.slot(), LEFT_PADDING + columns[type.slot()] * SLOT_SPACING, TOP_PADDING));
        }
        return DeepNullUpgradeType.values().length;
    }

    private void addPlayerInventorySlots(Inventory playerInventory, int rows) {
        int startY = 28 + rows * SLOT_SPACING;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = column + row * 9 + 9;
                addSlot(createPlayerSlot(playerInventory, index, LEFT_PADDING + column * SLOT_SPACING, startY + row * SLOT_SPACING));
            }
        }

        int hotbarY = 95 + rows * SLOT_SPACING;
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

    private boolean isStorageView() {
        return viewMode == ViewMode.MAIN || viewMode == ViewMode.FLUID;
    }

    private boolean isFluidStorageView() {
        return isStorageView() && dankInventory.isFluidMode();
    }

    private int firstEmptyFilterSlot() {
        for (int slot = 0; slot < dankInventory.getFilterSlotCount(); slot++) {
            if (dankInventory.getFilterStack(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private ItemStack tryStoreFluidFromContainer(ItemStack stack, int preferredSlot, boolean allowFirstEmptyFallback) {
        if (!dankInventory.hasFluidUpgrade() || stack.isEmpty()) {
            return stack;
        }

        ItemStack working = stack.copyWithCount(1);
        IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(working).orElse(null);
        FluidStack contained = itemHandler == null
                ? FluidStack.EMPTY
                : FluidUtil.getFluidContained(working).orElseGet(() -> firstFluidIn(itemHandler));
        boolean rawBucket = false;
        if (contained.isEmpty() && working.getItem() instanceof BucketItem bucketItem && bucketItem.content != Fluids.EMPTY) {
            contained = new FluidStack(bucketItem.content, FluidType.BUCKET_VOLUME);
            rawBucket = true;
        }
        if (contained.isEmpty()) {
            return stack;
        }

        int targetSlot = resolveFluidTargetSlot(contained, preferredSlot, allowFirstEmptyFallback);
        if (targetSlot < 0) {
            return stack;
        }

        if (rawBucket) {
            if (dankInventory.fillFluid(targetSlot, contained, false) != contained.getAmount()) {
                return stack;
            }
        } else {
            DeepNullFluidHandler targetHandler = new DeepNullFluidHandler(dankInventory, ItemStack.EMPTY, targetSlot);
            FluidStack transferred = FluidUtil.tryFluidTransfer(targetHandler, itemHandler, contained.getAmount(), true);
            if (transferred.isEmpty()) {
                return stack;
            }
        }

        if (dankInventory.getSelectedSlot() != targetSlot) {
            dankInventory.setSelectedSlot(targetSlot);
        }
        if (rawBucket) {
            return stack.getCount() == 1 ? new ItemStack(Items.BUCKET) : stack;
        }
        if (stack.getCount() == 1) {
            return itemHandler.getContainer();
        }
        return stack;
    }

    private int resolveFluidTargetSlot(FluidStack contained, int preferredSlot, boolean allowFirstEmptyFallback) {
        if (preferredSlot >= 0 && preferredSlot < dankInventory.getFluidSlotCount()) {
            FluidStack existing = dankInventory.getFluidInSlot(preferredSlot);
            if (existing.isEmpty() || FluidStack.isSameFluidSameComponents(existing, contained)) {
                return preferredSlot;
            }
        }

        int matchingSlot = dankInventory.findMatchingFluidSlot(contained);
        if (matchingSlot >= 0) {
            return matchingSlot;
        }

        if (!allowFirstEmptyFallback) {
            return -1;
        }

        return dankInventory.findFirstEmptyFluidSlot();
    }

    private static FluidStack firstFluidIn(IFluidHandlerItem itemHandler) {
        for (int tank = 0; tank < itemHandler.getTanks(); tank++) {
            FluidStack fluidInTank = itemHandler.getFluidInTank(tank);
            if (!fluidInTank.isEmpty()) {
                return fluidInTank;
            }
        }
        return FluidStack.EMPTY;
    }

    private int viewModeRows() {
        return switch (viewMode) {
            case MAIN -> tier.rows();
            case UPGRADES -> 1;
            case FILTER -> 3;
            case FLUID -> tier.rows();
        };
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

    public static class StorageSlot extends SlotItemHandler {
        private StorageSlot(DeepNullInventory itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }
    }

    public static final class FluidStorageSlot extends Slot {
        private FluidStorageSlot(SimpleContainer container, int slot, int xPosition, int yPosition) {
            super(container, slot, xPosition, yPosition);
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

    private static final class DockStorageSlot extends StorageSlot {
        private DockStorageSlot(DeepNullInventory itemHandler, int index, int xPosition, int yPosition) {
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

    public static final class UpgradeSlot extends SlotItemHandler {
        private UpgradeSlot(DeepNullInventory inventory, int index, int xPosition, int yPosition) {
            super(inventory.getUpgradeHandler(), index, xPosition, yPosition);
        }
    }
}
