package dev.deepdaddyttv.deepnullreforged.inventory;

import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Supplier;

public class DeepNullInventory extends ItemStackHandler {
    private static final String ROOT_TAG = "DeepNull";
    private static final String ITEMS_TAG = "Inventory";
    private static final String SELECTED_TAG = "Selected";
    private static final String EXTRACTION_TAG = "ExtractionModes";
    private static final String PLACEMENT_TAG = "PlacementModes";
    private static final String TAG_MATCHING_TAG = "TagMatching";
    private static final String LOCKED_TAG = "Locked";

    private final DeepNullTier tier;
    private final ItemStack backingStack;
    private final Supplier<HolderLookup.Provider> registriesSupplier;
    private final @Nullable Runnable changeListener;
    private final ItemExtractionMode[] extractionModes;
    private final ItemPlacementMode[] placementModes;
    private final boolean[] tagMatchingModes;

    private int selectedSlot = -1;
    private boolean locked;

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
        if (this.selectedSlot >= 0 && getStackInSlot(this.selectedSlot).isEmpty()) {
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
        int next = findNextOccupiedSlot(selectedSlot, forward);
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
        if (selectedSlot >= 0 && getStackInSlot(selectedSlot).isEmpty()) {
            selectedSlot = findNextOccupiedSlot(selectedSlot, true);
            if (selectedSlot >= 0 && getStackInSlot(selectedSlot).isEmpty()) {
                selectedSlot = -1;
            }
        } else if (selectedSlot < 0) {
            selectedSlot = findFirstOccupiedSlot();
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
        selectedSlot = root.getInt(SELECTED_TAG);
        readEnumModes(root.getIntArray(EXTRACTION_TAG), extractionModes, ItemExtractionMode.values(), ItemExtractionMode.KEEP_1);
        readEnumModes(root.getIntArray(PLACEMENT_TAG), placementModes, ItemPlacementMode.values(), ItemPlacementMode.KEEP_1);
        readBooleanModes(root.getByteArray(TAG_MATCHING_TAG), tagMatchingModes);
        locked = supportsLocking() && root.getBoolean(LOCKED_TAG);
        if (selectedSlot < -1 || selectedSlot >= getSlots() || (selectedSlot >= 0 && getStackInSlot(selectedSlot).isEmpty())) {
            selectedSlot = findFirstOccupiedSlot();
        }
        sanitizeModes();
    }

    private void save() {
        HolderLookup.Provider registries = registriesSupplier.get();
        if (backingStack.isEmpty() || registries == null) {
            return;
        }

        sanitizeModes();

        CompoundTag root = new CompoundTag();
        root.put(ITEMS_TAG, serializeNBT(registries));
        root.putInt(SELECTED_TAG, selectedSlot);
        root.putIntArray(EXTRACTION_TAG, Arrays.stream(extractionModes).mapToInt(Enum::ordinal).toArray());
        root.putIntArray(PLACEMENT_TAG, Arrays.stream(placementModes).mapToInt(Enum::ordinal).toArray());
        root.putByteArray(TAG_MATCHING_TAG, booleanModesAsBytes(tagMatchingModes));
        if (supportsLocking() && locked) {
            root.putBoolean(LOCKED_TAG, true);
        }

        CustomData.update(DataComponents.CUSTOM_DATA, backingStack, tag -> tag.put(ROOT_TAG, root));

        if (changeListener != null) {
            changeListener.run();
        }
    }

    private void sanitizeModes() {
        for (int slot = 0; slot < getSlots(); slot++) {
            if (!supportsTagMatching(slot)) {
                tagMatchingModes[slot] = false;
            }
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

    private static void readBooleanModes(byte[] storedModes, boolean[] targetModes) {
        Arrays.fill(targetModes, false);
        for (int i = 0; i < Math.min(storedModes.length, targetModes.length); i++) {
            targetModes[i] = storedModes[i] != 0;
        }
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
}
