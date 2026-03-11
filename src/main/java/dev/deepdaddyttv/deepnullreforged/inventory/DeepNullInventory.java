package dev.deepdaddyttv.deepnullreforged.inventory;

import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullUpgradeItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DeepNullInventory extends ItemStackHandler {
    private static final String ROOT_TAG = "DeepNull";
    private static final String ITEMS_TAG = "Inventory";
    private static final String SELECTED_TAG = "Selected";
    private static final String EXTRACTION_TAG = "ExtractionModes";
    private static final String PLACEMENT_TAG = "PlacementModes";
    private static final String TAG_MATCHING_TAG = "TagMatching";
    private static final String LOCKED_TAG = "Locked";
    private static final String UPGRADES_TAG = "Upgrades";
    private static final String FILTER_ITEMS_TAG = "FilterItems";
    private static final String FILTER_MODE_TAG = "FilterMode";
    private static final String CONTENT_MODE_TAG = "ContentMode";
    private static final String FLUIDS_TAG = "Fluids";
    private static final String ENERGY_TAG = "Energy";
    private static final String CHARGING_TAG = "Charging";
    private static final int FILTER_SLOT_COUNT = 27;

    private final DeepNullTier tier;
    private final ItemStack backingStack;
    private final Supplier<HolderLookup.Provider> registriesSupplier;
    private final @Nullable Runnable changeListener;
    private final ItemExtractionMode[] extractionModes;
    private final ItemPlacementMode[] placementModes;
    private final boolean[] tagMatchingModes;
    private final UpgradeItemHandler upgradeHandler;
    private final NonNullList<ItemStack> filterStacks;
    private final NonNullList<FluidStack> fluidStacks;

    private int selectedSlot = -1;
    private boolean locked;
    private DeepNullFilterMode filterMode = DeepNullFilterMode.WHITELIST;
    private DeepNullContentMode contentMode = DeepNullContentMode.ITEMS;
    private int storedEnergy;
    private boolean chargingEnabled;

    public DeepNullInventory(DeepNullTier tier, ItemStack backingStack, @Nullable HolderLookup.Provider registries, @Nullable Runnable changeListener) {
        this(tier, backingStack, () -> registries, changeListener);
    }

    public DeepNullInventory(
            DeepNullTier tier,
            ItemStack backingStack,
            Supplier<HolderLookup.Provider> registriesSupplier,
            @Nullable Runnable changeListener
    ) {
        super(tier.slotCount());
        this.tier = tier;
        this.backingStack = backingStack;
        this.registriesSupplier = registriesSupplier;
        this.changeListener = changeListener;
        this.extractionModes = new ItemExtractionMode[getSlots()];
        this.placementModes = new ItemPlacementMode[getSlots()];
        this.tagMatchingModes = new boolean[getSlots()];
        this.upgradeHandler = new UpgradeItemHandler();
        this.filterStacks = NonNullList.withSize(FILTER_SLOT_COUNT, ItemStack.EMPTY);
        this.fluidStacks = NonNullList.withSize(getSlots(), FluidStack.EMPTY);
        Arrays.fill(this.extractionModes, ItemExtractionMode.KEEP_1);
        Arrays.fill(this.placementModes, ItemPlacementMode.KEEP_1);
        load();
    }

    public static DeepNullInventory client(DeepNullTier tier) {
        return new DeepNullInventory(tier, ItemStack.EMPTY, () -> null, null);
    }

    public DeepNullTier tier() {
        return tier;
    }

    public boolean supportsLocking() {
        return tier.creative();
    }

    public IItemHandlerModifiable getUpgradeHandler() {
        return upgradeHandler;
    }

    public boolean supportsUpgrade(DeepNullUpgradeType type) {
        return type.isSupportedBy(tier);
    }

    public boolean hasUpgrade(DeepNullUpgradeType type) {
        return stackHasUpgrade(type, upgradeHandler.getStackInSlot(type.slot()));
    }

    public boolean supportsFiltering() {
        return hasUpgrade(DeepNullUpgradeType.FILTER);
    }

    public DeepNullFilterMode getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(DeepNullFilterMode mode) {
        if (!supportsFiltering()) {
            return;
        }
        filterMode = mode;
        save();
    }

    public void cycleFilterMode(boolean forward) {
        setFilterMode(filterMode.cycle(forward));
    }

    public DeepNullContentMode getContentMode() {
        return contentMode;
    }

    public boolean isFluidMode() {
        return contentMode == DeepNullContentMode.FLUIDS && hasFluidUpgrade();
    }

    public void setContentMode(DeepNullContentMode mode) {
        if (contentMode != DeepNullContentMode.ITEMS) {
            contentMode = DeepNullContentMode.ITEMS;
            if (selectedSlot >= 0 && getStackInSlot(selectedSlot).isEmpty()) {
                selectedSlot = findFirstOccupiedSlot();
            }
            save();
            return;
        }
        if (selectedSlot >= 0 && getStackInSlot(selectedSlot).isEmpty()) {
            selectedSlot = findFirstOccupiedSlot();
            save();
        }
    }

    public int getFilterSlotCount() {
        return FILTER_SLOT_COUNT;
    }

    public ItemStack getFilterStack(int slot) {
        validateFilterSlot(slot);
        return filterStacks.get(slot);
    }

    public void setFilterStack(int slot, ItemStack stack) {
        validateFilterSlot(slot);
        if (!supportsFiltering()) {
            return;
        }
        if (stack.isEmpty() || stack.getItem() instanceof DeepNullItem) {
            filterStacks.set(slot, ItemStack.EMPTY);
        } else {
            filterStacks.set(slot, stack.copyWithCount(1));
        }
        save();
    }

    public FluidStack getStoredFluid() {
        return getSelectedFluid();
    }

    public boolean hasFluidUpgrade() {
        return hasUpgrade(DeepNullUpgradeType.FLUID);
    }

    public int getFluidCapacity() {
        return hasFluidUpgrade() ? tier.fluidCapacity() : 0;
    }

    public int getFluidSlotCount() {
        return fluidStacks.size();
    }

    public FluidStack getFluidInSlot(int slot) {
        validateSlotIndex(slot);
        return fluidStacks.get(slot).copy();
    }

    public FluidStack getSelectedFluid() {
        if (selectedSlot < 0 || selectedSlot >= fluidStacks.size()) {
            return FluidStack.EMPTY;
        }
        return fluidStacks.get(selectedSlot).copy();
    }

    public boolean hasAnyFluid() {
        for (FluidStack fluidStack : fluidStacks) {
            if (!fluidStack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public int findMatchingFluidSlot(FluidStack stack) {
        if (!hasFluidUpgrade() || stack.isEmpty()) {
            return -1;
        }

        for (int slot = 0; slot < fluidStacks.size(); slot++) {
            FluidStack existing = fluidStacks.get(slot);
            if (!existing.isEmpty() && FluidStack.isSameFluidSameComponents(existing, stack)) {
                return slot;
            }
        }
        return -1;
    }

    public int findFirstEmptyFluidSlot() {
        if (!hasFluidUpgrade()) {
            return -1;
        }
        for (int slot = 0; slot < fluidStacks.size(); slot++) {
            if (fluidStacks.get(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    public int findFluidPickupSlot(FluidStack stack) {
        int matchingSlot = findMatchingFluidSlot(stack);
        if (matchingSlot >= 0) {
            return matchingSlot;
        }
        if (selectedSlot >= 0 && selectedSlot < fluidStacks.size() && fluidStacks.get(selectedSlot).isEmpty()) {
            return selectedSlot;
        }
        return -1;
    }

    public int findFluidInsertSlot(FluidStack stack) {
        int pickupSlot = findFluidPickupSlot(stack);
        if (pickupSlot >= 0) {
            return pickupSlot;
        }
        return findFirstEmptyFluidSlot();
    }

    public int fillFluid(FluidStack resource, boolean simulate) {
        if (!hasFluidUpgrade() || resource.isEmpty()) {
            return 0;
        }
        int remaining = resource.getAmount();
        int filled = 0;

        for (int slot = 0; slot < getFluidSlotCount() && remaining > 0; slot++) {
            FluidStack existing = fluidStacks.get(slot);
            if (existing.isEmpty() || !FluidStack.isSameFluidSameComponents(existing, resource)) {
                continue;
            }
            int slotFilled = fillFluid(slot, resource.copyWithAmount(remaining), simulate);
            remaining -= slotFilled;
            filled += slotFilled;
        }

        for (int slot = 0; slot < getFluidSlotCount() && remaining > 0; slot++) {
            if (!fluidStacks.get(slot).isEmpty()) {
                continue;
            }
            int slotFilled = fillFluid(slot, resource.copyWithAmount(remaining), simulate);
            remaining -= slotFilled;
            filled += slotFilled;
        }

        return filled;
    }

    public int fillFluid(int slot, FluidStack resource, boolean simulate) {
        validateSlotIndex(slot);
        if (!hasFluidUpgrade() || resource.isEmpty()) {
            return 0;
        }

        FluidStack existing = fluidStacks.get(slot);
        if (!existing.isEmpty() && !FluidStack.isSameFluidSameComponents(existing, resource)) {
            return 0;
        }

        int capacity = getFluidCapacity();
        if (capacity <= 0) {
            return 0;
        }

        int storedAmount = existing.isEmpty() ? 0 : existing.getAmount();
        int filled = Math.min(capacity - storedAmount, resource.getAmount());
        if (filled <= 0) {
            return 0;
        }

        if (!simulate) {
            if (existing.isEmpty()) {
                fluidStacks.set(slot, resource.copyWithAmount(filled));
            } else {
                existing.grow(filled);
            }
            save();
        }

        return filled;
    }

    public FluidStack drainFluid(FluidStack resource, boolean simulate) {
        if (!hasFluidUpgrade() || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }

        int remaining = resource.getAmount();
        FluidStack drained = FluidStack.EMPTY;
        for (int slot = 0; slot < getFluidSlotCount() && remaining > 0; slot++) {
            FluidStack existing = fluidStacks.get(slot);
            if (existing.isEmpty() || !FluidStack.isSameFluidSameComponents(existing, resource)) {
                continue;
            }
            FluidStack slotDrained = drainFluid(slot, remaining, simulate);
            if (slotDrained.isEmpty()) {
                continue;
            }
            remaining -= slotDrained.getAmount();
            if (drained.isEmpty()) {
                drained = slotDrained.copy();
            } else {
                drained.grow(slotDrained.getAmount());
            }
        }
        return drained;
    }

    public FluidStack drainFluid(int amount, boolean simulate) {
        if (!hasFluidUpgrade() || amount <= 0) {
            return FluidStack.EMPTY;
        }

        if (selectedSlot >= 0 && selectedSlot < getFluidSlotCount()) {
            FluidStack selected = drainFluid(selectedSlot, amount, simulate);
            if (!selected.isEmpty()) {
                return selected;
            }
        }

        for (int slot = 0; slot < getFluidSlotCount(); slot++) {
            FluidStack drained = drainFluid(slot, amount, simulate);
            if (!drained.isEmpty()) {
                return drained;
            }
        }
        return FluidStack.EMPTY;
    }

    public FluidStack drainFluid(int slot, int amount, boolean simulate) {
        validateSlotIndex(slot);
        if (!hasFluidUpgrade() || amount <= 0) {
            return FluidStack.EMPTY;
        }

        FluidStack existing = fluidStacks.get(slot);
        if (existing.isEmpty()) {
            return FluidStack.EMPTY;
        }

        int drained = Math.min(amount, existing.getAmount());
        if (drained <= 0) {
            return FluidStack.EMPTY;
        }

        FluidStack result = existing.copyWithAmount(drained);
        if (!simulate) {
            existing.shrink(drained);
            if (existing.isEmpty()) {
                fluidStacks.set(slot, FluidStack.EMPTY);
            }
            save();
        }
        return result;
    }

    public boolean hasEnergyUpgrade() {
        return hasUpgrade(DeepNullUpgradeType.ENERGY);
    }

    public int getEnergyStored() {
        return hasEnergyUpgrade() ? storedEnergy : 0;
    }

    public int getEnergyCapacity() {
        return hasEnergyUpgrade() ? tier.energyCapacity() : 0;
    }

    public int getEnergyTransferRate() {
        return hasEnergyUpgrade() ? tier.energyTransfer() : 0;
    }

    public boolean isChargingEnabled() {
        return hasEnergyUpgrade() && chargingEnabled;
    }

    public void setChargingEnabled(boolean chargingEnabled) {
        boolean next = hasEnergyUpgrade() && chargingEnabled;
        if (this.chargingEnabled == next) {
            return;
        }
        this.chargingEnabled = next;
        save();
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!hasEnergyUpgrade() || maxReceive <= 0) {
            return 0;
        }

        int received = Math.min(getEnergyCapacity() - storedEnergy, Math.min(getEnergyTransferRate(), maxReceive));
        if (received <= 0) {
            return 0;
        }

        if (!simulate) {
            storedEnergy += received;
            save();
        }

        return received;
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!hasEnergyUpgrade() || maxExtract <= 0) {
            return 0;
        }

        int extracted = Math.min(storedEnergy, Math.min(getEnergyTransferRate(), maxExtract));
        if (extracted <= 0) {
            return 0;
        }

        if (!simulate) {
            storedEnergy -= extracted;
            save();
        }

        return extracted;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        if (!supportsLocking()) {
            return;
        }
        this.locked = locked;
        save();
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(int selectedSlot) {
        if (selectedSlot < -1 || selectedSlot >= getSlots()) {
            this.selectedSlot = -1;
        } else {
            this.selectedSlot = selectedSlot;
        }
        if (contentMode == DeepNullContentMode.FLUIDS) {
            if (this.selectedSlot < 0 && hasFluidUpgrade()) {
                this.selectedSlot = 0;
            }
        } else if (this.selectedSlot >= 0 && getStackInSlot(this.selectedSlot).isEmpty()) {
            this.selectedSlot = findFirstOccupiedSlot();
        }
        save();
    }

    public ItemStack getSelectedStack() {
        if (selectedSlot < 0 || selectedSlot >= getSlots()) {
            return ItemStack.EMPTY;
        }
        return getStackInSlot(selectedSlot);
    }

    public void cycleSelected(boolean forward) {
        int next = contentMode == DeepNullContentMode.FLUIDS
                ? findNextFluidSlot(selectedSlot, forward)
                : findNextOccupiedSlot(selectedSlot, forward);
        if (next != selectedSlot) {
            setSelectedSlot(next);
        }
    }

    public ItemExtractionMode getExtractionMode(int slot) {
        validateSlotIndex(slot);
        return extractionModes[slot];
    }

    public void setExtractionMode(int slot, ItemExtractionMode mode) {
        validateSlotIndex(slot);
        extractionModes[slot] = mode;
        save();
    }

    public void cycleExtractionMode(int slot, boolean forward) {
        setExtractionMode(slot, getExtractionMode(slot).cycle(forward));
    }

    public ItemPlacementMode getPlacementMode(int slot) {
        validateSlotIndex(slot);
        return placementModes[slot];
    }

    public void setPlacementMode(int slot, ItemPlacementMode mode) {
        validateSlotIndex(slot);
        placementModes[slot] = mode;
        save();
    }

    public void cyclePlacementMode(int slot, boolean forward) {
        setPlacementMode(slot, getPlacementMode(slot).cycle(forward));
    }

    public boolean isTagMatchingEnabled(int slot) {
        validateSlotIndex(slot);
        return tagMatchingModes[slot];
    }

    public boolean supportsTagMatching(int slot) {
        validateSlotIndex(slot);
        return DeepNullTagDictionary.isSupported(getStackInSlot(slot));
    }

    public void toggleTagMatching(int slot) {
        validateSlotIndex(slot);
        if (!supportsTagMatching(slot)) {
            tagMatchingModes[slot] = false;
        } else {
            tagMatchingModes[slot] = !tagMatchingModes[slot];
        }
        save();
    }

    public boolean moveSlot(int fromSlot, int toSlot) {
        validateSlotIndex(fromSlot);
        validateSlotIndex(toSlot);
        if (fromSlot == toSlot) {
            return false;
        }

        ItemStack fromStack = stacks.get(fromSlot);
        ItemStack toStack = stacks.get(toSlot);
        if (fromStack.isEmpty() && toStack.isEmpty()) {
            return false;
        }

        stacks.set(fromSlot, toStack);
        stacks.set(toSlot, fromStack);

        ItemExtractionMode extractionMode = extractionModes[fromSlot];
        extractionModes[fromSlot] = extractionModes[toSlot];
        extractionModes[toSlot] = extractionMode;

        ItemPlacementMode placementMode = placementModes[fromSlot];
        placementModes[fromSlot] = placementModes[toSlot];
        placementModes[toSlot] = placementMode;

        boolean tagMatching = tagMatchingModes[fromSlot];
        tagMatchingModes[fromSlot] = tagMatchingModes[toSlot];
        tagMatchingModes[toSlot] = tagMatching;

        if (selectedSlot == fromSlot) {
            selectedSlot = toSlot;
        } else if (selectedSlot == toSlot) {
            selectedSlot = fromSlot;
        }

        save();
        return true;
    }

    public int getExtractableAmount(int slot) {
        validateSlotIndex(slot);
        ItemStack stack = getStackInSlot(slot);
        if (stack.isEmpty()) {
            return 0;
        }
        int keptAmount = getExtractionMode(slot).keptAmount();
        if (keptAmount == Integer.MAX_VALUE) {
            return 0;
        }
        return Math.max(0, stack.getCount() - keptAmount);
    }

    public int getPlaceableAmount(int slot) {
        validateSlotIndex(slot);
        ItemStack stack = getStackInSlot(slot);
        if (stack.isEmpty()) {
            return 0;
        }
        int keptAmount = getPlacementMode(slot).keptAmount();
        if (keptAmount == Integer.MAX_VALUE) {
            return 0;
        }
        return Math.max(0, stack.getCount() - keptAmount);
    }

    public ItemStack getExtractableStackInSlot(int slot) {
        validateSlotIndex(slot);
        ItemStack stack = getStackInSlot(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int extractableAmount = Math.min(stack.getMaxStackSize(), getExtractableAmount(slot));
        return extractableAmount <= 0 ? ItemStack.EMPTY : stack.copyWithCount(extractableAmount);
    }

    public ItemStack extractItemIgnoreExtractionMode(int slot, int amount, boolean simulate) {
        return extractItemInternal(slot, amount, simulate, true);
    }

    public int findMatchingSlot(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }

        for (int slot = 0; slot < getSlots(); slot++) {
            if (ItemStack.isSameItemSameComponents(getStackInSlot(slot), stack)) {
                return slot;
            }
        }

        for (int slot = 0; slot < getSlots(); slot++) {
            if (ItemStack.isSameItem(getStackInSlot(slot), stack)) {
                return slot;
            }
        }

        for (int slot = 0; slot < getSlots(); slot++) {
            if (matchesIncoming(slot, stack)) {
                return slot;
            }
        }
        return -1;
    }

    public boolean containsMatchingStack(ItemStack stack) {
        return findMatchingSlot(stack) >= 0;
    }

    public boolean matchesIncoming(int slot, ItemStack incomingStack) {
        validateSlotIndex(slot);
        ItemStack storedStack = getStackInSlot(slot);
        if (storedStack.isEmpty() || incomingStack.isEmpty()) {
            return false;
        }
        if (ItemStack.isSameItemSameComponents(storedStack, incomingStack)) {
            return true;
        }
        return isTagMatchingEnabled(slot) && DeepNullTagDictionary.canMatch(storedStack, incomingStack);
    }

    public ItemStack insertIntoMatchingSlots(ItemStack stack, boolean simulate) {
        ItemStack remaining = insertIntoMatchingSlots(stack, simulate, true);
        return insertIntoMatchingSlots(remaining, simulate, false);
    }

    public ItemStack insertIntoFirstAvailableSlot(ItemStack stack, boolean simulate) {
        if (!passesFilter(stack)) {
            return stack;
        }
        ItemStack remaining = insertIntoMatchingSlots(stack, simulate);
        for (int slot = 0; slot < getSlots() && !remaining.isEmpty(); slot++) {
            if (getStackInSlot(slot).isEmpty()) {
                remaining = insertItem(slot, remaining, simulate);
            }
        }
        return remaining;
    }

    @Override
    public int getSlotLimit(int slot) {
        return tier.perSlotCapacity();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        if (stack.isEmpty() || stack.getCount() <= 0 || stack.getItem() instanceof DeepNullItem) {
            return false;
        }
        if (!passesFilter(stack)) {
            return false;
        }
        if (supportsLocking() && locked) {
            return false;
        }
        ItemStack existing = getStackInSlot(slot);
        return existing.isEmpty() || matchesIncoming(slot, stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        validateSlotIndex(slot);
        if (!isItemValid(slot, stack)) {
            return stack;
        }

        ItemStack existing = getStackInSlot(slot);
        int limit = getSlotLimit(slot);
        if (existing.isEmpty()) {
            int inserted = Math.min(stack.getCount(), limit);
            if (!simulate) {
                setStackInSlot(slot, stack.copyWithCount(inserted));
            }
            return remainder(stack, inserted);
        }

        ItemStack normalizedInsert = normalizeForInsert(slot, stack);
        if (normalizedInsert.isEmpty()) {
            return stack;
        }

        int space = limit - existing.getCount();
        if (space <= 0) {
            return stack;
        }

        int inserted = Math.min(space, normalizedInsert.getCount());
        if (!simulate) {
            existing.grow(inserted);
            onContentsChanged(slot);
        }
        return remainder(stack, inserted);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return extractItemInternal(slot, amount, simulate, false);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (contentMode == DeepNullContentMode.ITEMS) {
            if (selectedSlot >= 0 && getStackInSlot(selectedSlot).isEmpty()) {
                selectedSlot = findNextOccupiedSlot(selectedSlot, true);
                if (selectedSlot >= 0 && getStackInSlot(selectedSlot).isEmpty()) {
                    selectedSlot = -1;
                }
            } else if (selectedSlot < 0) {
                selectedSlot = findFirstOccupiedSlot();
            }
        }
        if (!supportsTagMatching(slot)) {
            tagMatchingModes[slot] = false;
        }
        save();
    }

    private ItemStack insertIntoMatchingSlots(ItemStack stack, boolean simulate, boolean exactMatchOnly) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < getSlots() && !remaining.isEmpty(); slot++) {
            ItemStack existing = getStackInSlot(slot);
            if (existing.isEmpty()) {
                continue;
            }
            boolean exactMatch = ItemStack.isSameItemSameComponents(existing, remaining);
            if (exactMatchOnly != exactMatch) {
                continue;
            }
            if (!exactMatchOnly && !matchesIncoming(slot, remaining)) {
                continue;
            }
            if (exactMatchOnly || isTagMatchingEnabled(slot)) {
                remaining = insertItem(slot, remaining, simulate);
            }
        }
        return remaining;
    }

    private ItemStack extractItemInternal(int slot, int amount, boolean simulate, boolean ignoreExtractionMode) {
        if (slot < 0 || slot >= getSlots() || amount <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = getStackInSlot(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int maxAvailable = ignoreExtractionMode ? existing.getCount() : getExtractableAmount(slot);
        int extracted = Math.min(amount, existing.getMaxStackSize());
        extracted = Math.min(extracted, maxAvailable);
        if (extracted <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack result = existing.copyWithCount(extracted);
        if (!simulate && !(supportsLocking() && locked)) {
            existing.shrink(extracted);
            if (existing.isEmpty()) {
                setStackInSlot(slot, ItemStack.EMPTY);
            } else {
                onContentsChanged(slot);
            }
        }
        return result;
    }

    private ItemStack normalizeForInsert(int slot, ItemStack incomingStack) {
        ItemStack existing = getStackInSlot(slot);
        if (existing.isEmpty()) {
            return incomingStack;
        }
        if (ItemStack.isSameItemSameComponents(existing, incomingStack)) {
            return incomingStack;
        }
        if (isTagMatchingEnabled(slot) && DeepNullTagDictionary.canMatch(existing, incomingStack)) {
            return existing.copyWithCount(incomingStack.getCount());
        }
        return ItemStack.EMPTY;
    }

    private void load() {
        HolderLookup.Provider registries = registriesSupplier.get();
        if (backingStack.isEmpty() || registries == null) {
            return;
        }
        CompoundTag tag = backingStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag root = tag.getCompound(ROOT_TAG);
        if (root.contains(ITEMS_TAG, Tag.TAG_COMPOUND)) {
            deserializeNBT(registries, root.getCompound(ITEMS_TAG));
        }
        if (root.contains(UPGRADES_TAG, Tag.TAG_COMPOUND)) {
            upgradeHandler.deserializeNBT(registries, root.getCompound(UPGRADES_TAG));
        }
        readItemList(registries, root.getList(FILTER_ITEMS_TAG, Tag.TAG_COMPOUND), filterStacks);
        selectedSlot = root.getInt(SELECTED_TAG);
        readEnumModes(root.getIntArray(EXTRACTION_TAG), extractionModes, ItemExtractionMode.values(), ItemExtractionMode.KEEP_1);
        readEnumModes(root.getIntArray(PLACEMENT_TAG), placementModes, ItemPlacementMode.values(), ItemPlacementMode.KEEP_1);
        readBooleanModes(root.getByteArray(TAG_MATCHING_TAG), tagMatchingModes);
        locked = supportsLocking() && root.getBoolean(LOCKED_TAG);
        filterMode = DeepNullFilterMode.byId(root.getInt(FILTER_MODE_TAG));
        contentMode = DeepNullContentMode.ITEMS;
        if (root.contains(FLUIDS_TAG, Tag.TAG_LIST)) {
            readFluidList(registries, root.getList(FLUIDS_TAG, Tag.TAG_COMPOUND), fluidStacks);
        } else if (root.contains("Fluid", Tag.TAG_COMPOUND)) {
            FluidStack migrated = FluidStack.parseOptional(registries, root.getCompound("Fluid"));
            if (!migrated.isEmpty() && !fluidStacks.isEmpty()) {
                fluidStacks.set(0, migrated);
            }
        }
        storedEnergy = Math.max(0, root.getInt(ENERGY_TAG));
        chargingEnabled = root.getBoolean(CHARGING_TAG);
        if (selectedSlot < -1 || selectedSlot >= getSlots()) {
            selectedSlot = -1;
        }
        if (selectedSlot >= 0 && getStackInSlot(selectedSlot).isEmpty()) {
            selectedSlot = findFirstOccupiedSlot();
        }
        sanitizeState();
    }

    private void save() {
        HolderLookup.Provider registries = registriesSupplier.get();
        if (backingStack.isEmpty() || registries == null) {
            return;
        }

        sanitizeState();

        CompoundTag root = new CompoundTag();
        root.put(ITEMS_TAG, serializeNBT(registries));
        root.put(UPGRADES_TAG, upgradeHandler.serializeNBT(registries));
        root.put(FILTER_ITEMS_TAG, writeItemList(registries, filterStacks));
        root.putInt(SELECTED_TAG, selectedSlot);
        root.putIntArray(EXTRACTION_TAG, Arrays.stream(extractionModes).mapToInt(Enum::ordinal).toArray());
        root.putIntArray(PLACEMENT_TAG, Arrays.stream(placementModes).mapToInt(Enum::ordinal).toArray());
        root.putByteArray(TAG_MATCHING_TAG, booleanModesAsBytes(tagMatchingModes));
        if (supportsLocking() && locked) {
            root.putBoolean(LOCKED_TAG, true);
        }
        if (supportsFiltering()) {
            root.putInt(FILTER_MODE_TAG, filterMode.ordinal());
        }
        if (contentMode != DeepNullContentMode.ITEMS) {
            root.putInt(CONTENT_MODE_TAG, contentMode.ordinal());
        }
        if (hasFluidUpgrade()) {
            net.minecraft.nbt.ListTag fluids = writeFluidList(registries, fluidStacks);
            if (!fluids.isEmpty()) {
                root.put(FLUIDS_TAG, fluids);
            }
        }
        if (hasEnergyUpgrade() && storedEnergy > 0) {
            root.putInt(ENERGY_TAG, storedEnergy);
        }
        if (isChargingEnabled()) {
            root.putBoolean(CHARGING_TAG, true);
        }

        CustomData.update(DataComponents.CUSTOM_DATA, backingStack, tag -> tag.put(ROOT_TAG, root));

        if (changeListener != null) {
            changeListener.run();
        }
    }

    private void sanitizeState() {
        for (int slot = 0; slot < getSlots(); slot++) {
            if (!supportsTagMatching(slot)) {
                tagMatchingModes[slot] = false;
            }
        }
        for (DeepNullUpgradeType type : DeepNullUpgradeType.values()) {
            if (!supportsUpgrade(type) || !stackHasUpgrade(type, upgradeHandler.getStackInSlot(type.slot()))) {
                upgradeHandler.clearSlotSilently(type.slot());
            }
        }
        if (!supportsFiltering()) {
            clearFilterStacks();
            filterMode = DeepNullFilterMode.WHITELIST;
        }
        if (!hasFluidUpgrade()) {
            clearFluidStacks();
        } else {
            for (int slot = 0; slot < fluidStacks.size(); slot++) {
                FluidStack fluidStack = fluidStacks.get(slot);
                if (!fluidStack.isEmpty() && fluidStack.getAmount() > getFluidCapacity()) {
                    fluidStack.setAmount(getFluidCapacity());
                }
            }
        }
        contentMode = DeepNullContentMode.ITEMS;
        if (!hasEnergyUpgrade()) {
            storedEnergy = 0;
            chargingEnabled = false;
        } else {
            storedEnergy = Math.max(0, Math.min(storedEnergy, getEnergyCapacity()));
        }
        if (selectedSlot >= 0 && getStackInSlot(selectedSlot).isEmpty()) {
            selectedSlot = findFirstOccupiedSlot();
        }
    }

    private boolean passesFilter(ItemStack stack) {
        if (!supportsFiltering() || stack.isEmpty()) {
            return true;
        }

        if (filterMode.usesGhostSlots()) {
            boolean hasEntries = false;
            boolean matched = false;
            for (ItemStack filterStack : filterStacks) {
                if (filterStack.isEmpty()) {
                    continue;
                }
                hasEntries = true;
                if (matchesFilterStack(filterStack, stack)) {
                    matched = true;
                    break;
                }
            }
            if (!hasEntries) {
                return true;
            }
            return filterMode == DeepNullFilterMode.WHITELIST ? matched : !matched;
        }

        return filterMode.matchesPreset(stack);
    }

    private boolean matchesFilterStack(ItemStack filterStack, ItemStack incomingStack) {
        if (filterStack.isEmpty() || incomingStack.isEmpty()) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(filterStack, incomingStack) || ItemStack.isSameItem(filterStack, incomingStack);
    }

    private boolean stackHasUpgrade(DeepNullUpgradeType type, ItemStack stack) {
        return stack.getItem() instanceof DeepNullUpgradeItem upgradeItem && upgradeItem.type() == type;
    }

    private void clearFilterStacks() {
        for (int slot = 0; slot < filterStacks.size(); slot++) {
            filterStacks.set(slot, ItemStack.EMPTY);
        }
    }

    private void clearFluidStacks() {
        for (int slot = 0; slot < fluidStacks.size(); slot++) {
            fluidStacks.set(slot, FluidStack.EMPTY);
        }
    }

    private void validateFilterSlot(int slot) {
        if (slot < 0 || slot >= filterStacks.size()) {
            throw new RuntimeException("Filter slot " + slot + " not in valid range - [0," + filterStacks.size() + ")");
        }
    }

    private int findFirstOccupiedSlot() {
        for (int slot = 0; slot < getSlots(); slot++) {
            if (!getStackInSlot(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private int findNextOccupiedSlot(int current, boolean forward) {
        if (getSlots() == 0) {
            return -1;
        }

        if (current < 0) {
            return findFirstOccupiedSlot();
        }

        for (int offset = 1; offset <= getSlots(); offset++) {
            int slot = forward
                    ? (current + offset) % getSlots()
                    : Math.floorMod(current - offset, getSlots());
            if (!getStackInSlot(slot).isEmpty()) {
                return slot;
            }
        }

        return current;
    }

    private int findNextFluidSlot(int current, boolean forward) {
        if (!hasFluidUpgrade() || getSlots() == 0) {
            return -1;
        }
        List<Integer> selectable = new ArrayList<>();
        for (int slot = 0; slot < fluidStacks.size(); slot++) {
            if (!fluidStacks.get(slot).isEmpty()) {
                selectable.add(slot);
            }
        }

        int emptySlot = current >= 0 && current < fluidStacks.size() && fluidStacks.get(current).isEmpty()
                ? current
                : findFirstEmptyFluidSlot();
        if (emptySlot >= 0 && !selectable.contains(emptySlot)) {
            selectable.add(emptySlot);
        }

        if (selectable.isEmpty()) {
            return -1;
        }
        if (current < 0) {
            return selectable.get(0);
        }

        int index = selectable.indexOf(current);
        if (index < 0) {
            return selectable.get(0);
        }
        int nextIndex = forward
                ? (index + 1) % selectable.size()
                : Math.floorMod(index - 1, selectable.size());
        return selectable.get(nextIndex);
    }

    private static void readBooleanModes(byte[] storedModes, boolean[] targetModes) {
        Arrays.fill(targetModes, false);
        for (int i = 0; i < Math.min(storedModes.length, targetModes.length); i++) {
            targetModes[i] = storedModes[i] != 0;
        }
    }

    private static void readItemList(HolderLookup.Provider registries, Tag storedList, NonNullList<ItemStack> targetStacks) {
        for (int i = 0; i < targetStacks.size(); i++) {
            targetStacks.set(i, ItemStack.EMPTY);
        }
        if (!(storedList instanceof net.minecraft.nbt.ListTag listTag)) {
            return;
        }
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag entry = listTag.getCompound(i);
            int slot = entry.getInt("Slot");
            if (slot < 0 || slot >= targetStacks.size()) {
                continue;
            }
            targetStacks.set(slot, ItemStack.parseOptional(registries, entry.getCompound("Stack")));
        }
    }

    private static net.minecraft.nbt.ListTag writeItemList(HolderLookup.Provider registries, NonNullList<ItemStack> sourceStacks) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (int slot = 0; slot < sourceStacks.size(); slot++) {
            ItemStack stack = sourceStacks.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", slot);
            entry.put("Stack", stack.saveOptional(registries));
            list.add(entry);
        }
        return list;
    }

    private static void readFluidList(HolderLookup.Provider registries, Tag storedList, NonNullList<FluidStack> targetStacks) {
        for (int i = 0; i < targetStacks.size(); i++) {
            targetStacks.set(i, FluidStack.EMPTY);
        }
        if (!(storedList instanceof net.minecraft.nbt.ListTag listTag)) {
            return;
        }
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag entry = listTag.getCompound(i);
            int slot = entry.getInt("Slot");
            if (slot < 0 || slot >= targetStacks.size()) {
                continue;
            }
            targetStacks.set(slot, FluidStack.parseOptional(registries, entry.getCompound("Stack")));
        }
    }

    private static net.minecraft.nbt.ListTag writeFluidList(HolderLookup.Provider registries, NonNullList<FluidStack> sourceStacks) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (int slot = 0; slot < sourceStacks.size(); slot++) {
            FluidStack stack = sourceStacks.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", slot);
            entry.put("Stack", stack.saveOptional(registries));
            list.add(entry);
        }
        return list;
    }

    private static <E extends Enum<E>> void readEnumModes(int[] storedModes, E[] targetModes, E[] validValues, E defaultValue) {
        Arrays.fill(targetModes, defaultValue);
        for (int i = 0; i < Math.min(storedModes.length, targetModes.length); i++) {
            int ordinal = storedModes[i];
            if (ordinal >= 0 && ordinal < validValues.length) {
                targetModes[i] = validValues[ordinal];
            }
        }
    }

    private static byte[] booleanModesAsBytes(boolean[] modes) {
        byte[] serialized = new byte[modes.length];
        for (int i = 0; i < modes.length; i++) {
            serialized[i] = (byte) (modes[i] ? 1 : 0);
        }
        return serialized;
    }

    private static ItemStack remainder(ItemStack original, int inserted) {
        if (inserted >= original.getCount()) {
            return ItemStack.EMPTY;
        }
        return original.copyWithCount(original.getCount() - inserted);
    }

    private final class UpgradeItemHandler extends ItemStackHandler {
        private UpgradeItemHandler() {
            super(DeepNullUpgradeType.values().length);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            DeepNullUpgradeType type = DeepNullUpgradeType.bySlot(slot);
            return type.isSupportedBy(tier) && stackHasUpgrade(type, stack);
        }

        private void clearSlotSilently(int slot) {
            stacks.set(slot, ItemStack.EMPTY);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            save();
        }
    }
}
