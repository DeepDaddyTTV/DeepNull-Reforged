package dev.deepdaddyttv.deepnullreforged.inventory;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullUpgradeItem;
import dev.deepdaddyttv.deepnullreforged.item.EnderUpgradeItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DeepNullInventory extends ItemStackHandler {
    private static final String ROOT_TAG = "DeepNull";
    private static final String ITEMS_TAG = "Inventory";
    private static final String STACK_TAG = "Stack";
    private static final String SLOT_TAG = "Slot";
    private static final String COUNT_TAG = "Count";
    private static final String SELECTED_TAG = "Selected";
    private static final String EXTRACTION_TAG = "ExtractionModes";
    private static final String CUSTOM_EXTRACTION_TAG = "CustomExtractionModes";
    private static final String PLACEMENT_TAG = "PlacementModes";
    private static final String TAG_MATCHING_TAG = "TagMatching";
    private static final String LOCKED_TAG = "Locked";
    private static final String UPGRADES_TAG = "Upgrades";
    private static final String FILTER_ITEMS_TAG = "FilterItems";
    private static final String FILTER_MODE_TAG = "FilterMode";
    private static final String AUTO_SMELT_FILTER_ITEMS_TAG = "AutoSmeltFilterItems";
    private static final String AUTO_SMELT_FILTER_MODE_TAG = "AutoSmeltFilterMode";
    private static final String CONTENT_MODE_TAG = "ContentMode";
    private static final String FLUIDS_TAG = "Fluids";
    private static final String CHEMICALS_TAG = "Chemicals";
    private static final String ENDER_MIRROR_ITEMS_TAG = "EnderMirrorItems";
    private static final String ENDER_MIRROR_FLUIDS_TAG = "EnderMirrorFluids";
    private static final String ENDER_MIRROR_CHEMICALS_TAG = "EnderMirrorChemicals";
    private static final String ENERGY_TAG = "Energy";
    private static final String CHARGING_TAG = "Charging";
    private static final String TRANSFER_LOCKED_TAG = "TransferLocked";
    private static final String AUTO_PICKUP_TAG = "AutoPickup";
    private static final String AUTO_FEEDING_TAG = "AutoFeeding";
    private static final String AUTO_SMELTING_TAG = "AutoSmelting";
    private static final String STONE_GENERATOR_VARIANT_TAG = "StoneGeneratorVariant";
    private static final String STONEWORKS_AMOUNT_TAG = "StoneworksAmount";
    private static final String STONEWORKS_MONITOR_TAG = "StoneworksMonitor";
    private static final String STONEWORKS_CURSOR_TAG = "StoneworksCursor";
    private static final String FRAME_COLOR_TAG = "FrameColor";
    private static final String GLASS_COLOR_TAG = "GlassColor";
    private static final String STYLE_VARIANT_TAG = "StyleVariant";
    private static final int FILTER_SLOT_COUNT = 27;
    private static final int CREATIVE_DISPLAY_ENERGY = Integer.MAX_VALUE / 2;
    private static final int CREATIVE_DISPLAY_FLUID = Integer.MAX_VALUE / 2;
    private static final int DEFAULT_STONEWORKS_AMOUNT = 1;
    private static final int MAX_STONEWORKS_AMOUNT = 9_999;
    private static final int DEFAULT_STYLE_COLOR = 0xFFFFFF;
    private static final int[] DEFAULT_DEEPNULL_GLASS_COLORS = {
            0xC62121,
            0x3A63CF,
            0xEBEBEB,
            0xEAE31C,
            0x29AFCE,
            0x0FDD74,
            0x9718E4
    };
    private static final int[] DEFAULT_DAMPNULL_GLASS_COLORS = {
            0xC82A2A,
            0x4269D1,
            0xEBEBEB,
            0xEBE428,
            0x34B3D0,
            0x1BDE7B,
            0x9C24E5
    };

    private final DeepNullTier tier;
    private final ItemStack backingStack;
    private final Supplier<HolderLookup.Provider> registriesSupplier;
    private final @Nullable Runnable changeListener;
    private final boolean fluidOnly;
    private final ItemExtractionMode[] extractionModes;
    private final int[] customExtractionAmounts;
    private final ItemPlacementMode[] placementModes;
    private final boolean[] tagMatchingModes;
    private final UpgradeItemHandler upgradeHandler;
    private final NonNullList<ItemStack> filterStacks;
    private final NonNullList<ItemStack> autoSmeltFilterStacks;
    private final NonNullList<FluidStack> fluidStacks;
    private final NonNullList<StoredChemical> chemicalStacks;

    private int selectedSlot = -1;
    private boolean locked;
    private DeepNullFilterMode filterMode = DeepNullFilterMode.WHITELIST;
    private DeepNullFilterMode autoSmeltFilterMode = DeepNullFilterMode.BLACKLIST;
    private DeepNullContentMode contentMode = DeepNullContentMode.ITEMS;
    private StoneGeneratorVariant stoneGeneratorVariant = StoneGeneratorVariant.COBBLESTONE;
    private int storedEnergy;
    private boolean chargingEnabled;
    private boolean transferLocked;
    private boolean autoPickupEnabled = DeepNullConfig.defaultAutoPickupEnabled();
    private boolean autoFeedingEnabled = DeepNullConfig.defaultAutoFeedingEnabled();
    private boolean autoSmeltingEnabled = DeepNullConfig.defaultAutoSmeltingEnabled();
    private int stoneworksTargetStacks = DeepNullConfig.defaultStoneworksAmount();
    private final boolean[] stoneworksMonitoring = new boolean[StoneworksMaterial.values().length];
    private int stoneworksCursor;
    private int frameColor = DEFAULT_STYLE_COLOR;
    private int glassColor = DEFAULT_STYLE_COLOR;
    private StyleGlassVariant styleVariant = StyleGlassVariant.DEFAULT;
    private boolean pendingLinkCleanup;

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
        this.fluidOnly = backingStack.getItem() instanceof DampNullItem;
        this.frameColor = defaultFrameColor();
        this.glassColor = defaultGlassColor();
        this.styleVariant = StyleGlassVariant.DEFAULT;
        this.extractionModes = new ItemExtractionMode[getSlots()];
        this.customExtractionAmounts = new int[getSlots()];
        this.placementModes = new ItemPlacementMode[getSlots()];
        this.tagMatchingModes = new boolean[getSlots()];
        this.upgradeHandler = new UpgradeItemHandler();
        this.filterStacks = NonNullList.withSize(FILTER_SLOT_COUNT, ItemStack.EMPTY);
        this.autoSmeltFilterStacks = NonNullList.withSize(FILTER_SLOT_COUNT, ItemStack.EMPTY);
        this.fluidStacks = NonNullList.withSize(initialFluidSlotCount(), FluidStack.EMPTY);
        this.chemicalStacks = NonNullList.withSize(initialFluidSlotCount(), StoredChemical.EMPTY);
        Arrays.fill(this.extractionModes, ItemExtractionMode.KEEP_1);
        Arrays.fill(this.placementModes, ItemPlacementMode.KEEP_1);
        Arrays.fill(this.stoneworksMonitoring, true);
        load();
        if (pendingLinkCleanup) {
            pendingLinkCleanup = false;
            save();
        }
    }

    public static DeepNullInventory client(DeepNullTier tier) {
        return new DeepNullInventory(tier, ItemStack.EMPTY, () -> null, null);
    }

    public static StyleRenderData readStyleRenderData(ItemStack stack, DeepNullTier tier, boolean fluidOnly) {
        CompoundTag root = getRootTagView(stack);
        boolean hasFrameOverride = root != null && hasNumeric(root, FRAME_COLOR_TAG);
        boolean hasGlassOverride = root != null && hasNumeric(root, GLASS_COLOR_TAG);
        return new StyleRenderData(
                hasFrameOverride || hasGlassOverride,
                root == null ? StyleGlassVariant.DEFAULT : StyleGlassVariant.byId(root.getStringOr(STYLE_VARIANT_TAG, StyleGlassVariant.DEFAULT.id())),
                hasFrameOverride ? sanitizeStyleColor(root.getIntOr(FRAME_COLOR_TAG, defaultFrameColor(tier, fluidOnly))) : defaultFrameColor(tier, fluidOnly),
                hasGlassOverride ? sanitizeStyleColor(root.getIntOr(GLASS_COLOR_TAG, defaultGlassColor(tier, fluidOnly))) : defaultGlassColor(tier, fluidOnly)
        );
    }

    public static boolean hasCustomStyle(ItemStack stack) {
        return hasColorOverrides(stack) || getStyleVariant(stack) != StyleGlassVariant.DEFAULT;
    }

    public static boolean hasColorOverrides(ItemStack stack) {
        CompoundTag root = getRootTagView(stack);
        if (root == null) {
            return false;
        }
        return hasNumeric(root, FRAME_COLOR_TAG) || hasNumeric(root, GLASS_COLOR_TAG);
    }

    public static StyleGlassVariant getStyleVariant(ItemStack stack) {
        CompoundTag root = getRootTagView(stack);
        if (root == null) {
            return StyleGlassVariant.DEFAULT;
        }
        return StyleGlassVariant.byId(root.getStringOr(STYLE_VARIANT_TAG, StyleGlassVariant.DEFAULT.id()));
    }

    public DeepNullTier tier() {
        return tier;
    }

    public ItemStack backingStack() {
        return backingStack;
    }

    public boolean isFluidOnly() {
        return fluidOnly;
    }

    public boolean supportsLocking() {
        return tier.creative();
    }

    public IItemHandlerModifiable getUpgradeHandler() {
        return upgradeHandler;
    }

    public boolean supportsUpgrade(DeepNullUpgradeType type) {
        return type.isSupportedBy(tier, fluidOnly);
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

    public boolean supportsAutoSmeltFiltering() {
        return hasAutoSmeltingUpgrade();
    }

    public DeepNullFilterMode getAutoSmeltFilterMode() {
        return normalizeAutoSmeltFilterMode(autoSmeltFilterMode);
    }

    public void setAutoSmeltFilterMode(DeepNullFilterMode mode) {
        if (!supportsAutoSmeltFiltering()) {
            return;
        }
        autoSmeltFilterMode = normalizeAutoSmeltFilterMode(mode);
        save();
    }

    public ItemStack getAutoSmeltFilterStack(int slot) {
        validateFilterSlot(slot);
        return autoSmeltFilterStacks.get(slot);
    }

    public void setAutoSmeltFilterStack(int slot, ItemStack stack) {
        validateFilterSlot(slot);
        if (!supportsAutoSmeltFiltering()) {
            return;
        }
        if (stack.isEmpty() || stack.getItem() instanceof DeepNullItem) {
            autoSmeltFilterStacks.set(slot, ItemStack.EMPTY);
        } else {
            autoSmeltFilterStacks.set(slot, stack.copyWithCount(1));
        }
        save();
    }

    public DeepNullContentMode getContentMode() {
        return fluidOnly ? DeepNullContentMode.FLUIDS : contentMode;
    }

    public boolean isFluidMode() {
        return getContentMode() == DeepNullContentMode.FLUIDS && supportsFluidStorage();
    }

    public void setContentMode(DeepNullContentMode mode) {
        if (fluidOnly) {
            contentMode = DeepNullContentMode.FLUIDS;
            if (selectedSlot < 0 && getFluidSlotCount() > 0) {
                selectedSlot = 0;
            }
            save();
            return;
        }
        contentMode = DeepNullContentMode.ITEMS;
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
        return fluidOnly || hasUpgrade(DeepNullUpgradeType.FLUID);
    }

    public boolean supportsFluidStorage() {
        return fluidOnly;
    }

    public boolean supportsChemicalStorage() {
        return fluidOnly && hasGasUpgrade() && DeepNullConfig.isChemicalStorageEnabled() && ModList.get().isLoaded("mekanism");
    }

    public int getFluidCapacity() {
        return supportsFluidStorage() ? tier.fluidCapacity() : 0;
    }

    public int getFluidSlotCount() {
        return fluidStacks.size();
    }

    public FluidStack getFluidInSlot(int slot) {
        validateSlotIndex(slot);
        return displayedFluid(fluidStacks.get(slot));
    }

    public StoredChemical getChemicalInSlot(int slot) {
        validateSlotIndex(slot);
        return displayedChemical(chemicalStacks.get(slot));
    }

    public boolean hasChemicalInSlot(int slot) {
        validateSlotIndex(slot);
        return !chemicalStacks.get(slot).isEmpty();
    }

    public boolean hasFluidInSlot(int slot) {
        validateSlotIndex(slot);
        return !fluidStacks.get(slot).isEmpty();
    }

    public FluidStack getSelectedFluid() {
        if (selectedSlot < 0 || selectedSlot >= fluidStacks.size()) {
            return FluidStack.EMPTY;
        }
        return displayedFluid(fluidStacks.get(selectedSlot));
    }

    public StoredChemical getSelectedChemical() {
        if (selectedSlot < 0 || selectedSlot >= chemicalStacks.size()) {
            return StoredChemical.EMPTY;
        }
        return displayedChemical(chemicalStacks.get(selectedSlot));
    }

    public boolean hasAnyFluid() {
        for (FluidStack fluidStack : fluidStacks) {
            if (!fluidStack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyChemical() {
        if (!supportsChemicalStorage()) {
            return false;
        }
        for (StoredChemical chemicalStack : chemicalStacks) {
            if (!chemicalStack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void reloadFromBacking() {
        load();
    }

    public int findMatchingFluidSlot(FluidStack stack) {
        if (!supportsFluidStorage() || stack.isEmpty()) {
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
        if (!supportsFluidStorage()) {
            return -1;
        }
        for (int slot = 0; slot < fluidStacks.size(); slot++) {
            if (isTankEmpty(slot)) {
                return slot;
            }
        }
        return -1;
    }

    public int findMatchingChemicalSlot(StoredChemical stack) {
        if (!supportsChemicalStorage() || stack.isEmpty()) {
            return -1;
        }

        for (int slot = 0; slot < chemicalStacks.size(); slot++) {
            StoredChemical existing = chemicalStacks.get(slot);
            if (!existing.isEmpty() && existing.isSameChemical(stack)) {
                return slot;
            }
        }
        return -1;
    }

    public int findFirstEmptyChemicalSlot() {
        return findFirstEmptyFluidSlot();
    }

    public int findChemicalInsertSlot(StoredChemical stack) {
        int matchingSlot = findMatchingChemicalSlot(stack);
        if (matchingSlot >= 0) {
            return matchingSlot;
        }
        if (selectedSlot >= 0 && selectedSlot < chemicalStacks.size() && isTankEmpty(selectedSlot)) {
            return selectedSlot;
        }
        return findFirstEmptyChemicalSlot();
    }

    public int findFluidPickupSlot(FluidStack stack) {
        int matchingSlot = findMatchingFluidSlot(stack);
        if (matchingSlot >= 0) {
            return matchingSlot;
        }
        if (selectedSlot >= 0 && selectedSlot < fluidStacks.size() && isTankEmpty(selectedSlot)) {
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
        if (!supportsFluidStorage() || resource.isEmpty()) {
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
            if (!isTankEmpty(slot)) {
                continue;
            }
            int slotFilled = fillFluid(slot, resource.copyWithAmount(remaining), simulate);
            remaining -= slotFilled;
            filled += slotFilled;
        }

        return filled;
    }

    public int fillExistingFluidSlotsOnly(FluidStack resource, boolean simulate) {
        if (!supportsFluidStorage() || resource.isEmpty()) {
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

        return filled;
    }

    public int fillFluid(int slot, FluidStack resource, boolean simulate) {
        validateSlotIndex(slot);
        if (!supportsFluidStorage() || resource.isEmpty()) {
            return 0;
        }

        FluidStack existing = fluidStacks.get(slot);
        if (!chemicalStacks.get(slot).isEmpty()) {
            return 0;
        }
        if (!existing.isEmpty() && !FluidStack.isSameFluidSameComponents(existing, resource)) {
            return 0;
        }

        if (tier.creative()) {
            if (!simulate && existing.isEmpty()) {
                fluidStacks.set(slot, resource.copyWithAmount(FluidType.BUCKET_VOLUME));
                save();
            }
            return resource.getAmount();
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
        if (!supportsFluidStorage() || resource.isEmpty()) {
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
        if (!supportsFluidStorage() || amount <= 0) {
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
        if (!supportsFluidStorage() || amount <= 0) {
            return FluidStack.EMPTY;
        }

        FluidStack existing = fluidStacks.get(slot);
        if (existing.isEmpty()) {
            return FluidStack.EMPTY;
        }

        if (tier.creative()) {
            return existing.copyWithAmount(amount);
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

    public boolean clearFluidSlot(int slot) {
        validateSlotIndex(slot);
        if (!supportsFluidStorage() || (fluidStacks.get(slot).isEmpty() && chemicalStacks.get(slot).isEmpty())) {
            return false;
        }
        fluidStacks.set(slot, FluidStack.EMPTY);
        chemicalStacks.set(slot, StoredChemical.EMPTY);
        save();
        return true;
    }

    public boolean setChemicalInSlot(int slot, StoredChemical chemical) {
        validateSlotIndex(slot);
        if (!supportsChemicalStorage()) {
            return false;
        }
        if (!chemical.isEmpty() && !fluidStacks.get(slot).isEmpty()) {
            return false;
        }
        if (chemical.isEmpty()) {
            chemicalStacks.set(slot, StoredChemical.EMPTY);
        } else {
            long amount = tier.creative() ? 1L : Math.min(chemical.amount(), getFluidCapacity());
            chemicalStacks.set(slot, amount <= 0L ? StoredChemical.EMPTY : chemical.copyWithAmount(amount));
        }
        if (!chemical.isEmpty()) {
            fluidStacks.set(slot, FluidStack.EMPTY);
        }
        save();
        return true;
    }

    public int fillChemical(StoredChemical resource, boolean simulate) {
        if (!supportsChemicalStorage() || resource.isEmpty()) {
            return 0;
        }
        int slot = findChemicalInsertSlot(resource);
        return slot < 0 ? 0 : fillChemical(slot, resource, simulate);
    }

    public int fillExistingChemicalSlotsOnly(StoredChemical resource, boolean simulate) {
        if (!supportsChemicalStorage() || resource.isEmpty()) {
            return 0;
        }
        long remaining = resource.amount();
        int filled = 0;

        for (int slot = 0; slot < getFluidSlotCount() && remaining > 0; slot++) {
            StoredChemical existing = chemicalStacks.get(slot);
            if (existing.isEmpty() || !existing.isSameChemical(resource)) {
                continue;
            }
            int slotFilled = fillChemical(slot, resource.copyWithAmount(remaining), simulate);
            remaining -= slotFilled;
            filled += slotFilled;
        }
        return filled;
    }

    public int fillChemical(int slot, StoredChemical resource, boolean simulate) {
        validateSlotIndex(slot);
        if (!supportsChemicalStorage() || resource.isEmpty()) {
            return 0;
        }

        if (!fluidStacks.get(slot).isEmpty()) {
            return 0;
        }

        StoredChemical existing = chemicalStacks.get(slot);
        if (!existing.isEmpty() && !existing.isSameChemical(resource)) {
            return 0;
        }

        if (tier.creative()) {
            if (!simulate && existing.isEmpty()) {
                chemicalStacks.set(slot, resource.copyWithAmount(1));
                save();
            }
            return (int) Math.min(Integer.MAX_VALUE, resource.amount());
        }

        long capacity = getFluidCapacity();
        if (capacity <= 0L) {
            return 0;
        }

        long storedAmount = existing.isEmpty() ? 0L : existing.amount();
        int filled = (int) Math.min(Integer.MAX_VALUE, Math.min(capacity - storedAmount, resource.amount()));
        if (filled <= 0) {
            return 0;
        }

        if (!simulate) {
            if (existing.isEmpty()) {
                chemicalStacks.set(slot, resource.copyWithAmount(filled));
            } else {
                chemicalStacks.set(slot, existing.copyWithAmount(existing.amount() + filled));
            }
            save();
        }
        return filled;
    }

    public StoredChemical drainChemical(int slot, long amount, boolean simulate) {
        validateSlotIndex(slot);
        if (!supportsChemicalStorage() || amount <= 0L) {
            return StoredChemical.EMPTY;
        }

        StoredChemical existing = chemicalStacks.get(slot);
        if (existing.isEmpty()) {
            return StoredChemical.EMPTY;
        }

        if (tier.creative()) {
            return existing.copyWithAmount(amount);
        }

        long drained = Math.min(amount, existing.amount());
        if (drained <= 0L) {
            return StoredChemical.EMPTY;
        }

        StoredChemical result = existing.copyWithAmount(drained);
        if (!simulate) {
            long remaining = existing.amount() - drained;
            chemicalStacks.set(slot, remaining <= 0L ? StoredChemical.EMPTY : existing.copyWithAmount(remaining));
            save();
        }
        return result;
    }

    public StoredChemical drainChemical(StoredChemical resource, boolean simulate) {
        if (!supportsChemicalStorage() || resource.isEmpty()) {
            return StoredChemical.EMPTY;
        }

        long remaining = resource.amount();
        StoredChemical drained = StoredChemical.EMPTY;
        for (int slot = 0; slot < getFluidSlotCount() && remaining > 0; slot++) {
            StoredChemical existing = chemicalStacks.get(slot);
            if (existing.isEmpty() || !existing.isSameChemical(resource)) {
                continue;
            }
            StoredChemical slotDrained = drainChemical(slot, remaining, simulate);
            if (slotDrained.isEmpty()) {
                continue;
            }
            remaining -= slotDrained.amount();
            drained = drained.isEmpty()
                    ? slotDrained.copy()
                    : drained.copyWithAmount(drained.amount() + slotDrained.amount());
        }
        return drained;
    }

    public StoredChemical drainChemical(long amount, boolean simulate) {
        if (!supportsChemicalStorage() || amount <= 0L) {
            return StoredChemical.EMPTY;
        }

        if (selectedSlot >= 0 && selectedSlot < getFluidSlotCount()) {
            StoredChemical selected = drainChemical(selectedSlot, amount, simulate);
            if (!selected.isEmpty()) {
                return selected;
            }
        }

        for (int slot = 0; slot < getFluidSlotCount(); slot++) {
            StoredChemical drained = drainChemical(slot, amount, simulate);
            if (!drained.isEmpty()) {
                return drained;
            }
        }
        return StoredChemical.EMPTY;
    }

    public boolean hasEnergyUpgrade() {
        return hasUpgrade(DeepNullUpgradeType.ENERGY) || hasUpgrade(DeepNullUpgradeType.DEEP_ENERGY);
    }

    public boolean hasAutoFeedingUpgrade() {
        return hasUpgrade(DeepNullUpgradeType.AUTO_FEEDING);
    }

    public boolean isAutoFeedingEnabled() {
        return hasAutoFeedingUpgrade() && autoFeedingEnabled && DeepNullConfig.isAutoFeedingEnabled();
    }

    public void setAutoFeedingEnabled(boolean autoFeedingEnabled) {
        if (this.autoFeedingEnabled == autoFeedingEnabled) {
            return;
        }
        this.autoFeedingEnabled = autoFeedingEnabled;
        save();
    }

    public boolean hasAutoSmeltingUpgrade() {
        return hasUpgrade(DeepNullUpgradeType.AUTO_SMELTING);
    }

    public boolean isAutoSmeltingEnabled() {
        return hasAutoSmeltingUpgrade() && autoSmeltingEnabled && DeepNullConfig.isAutoSmeltingEnabled();
    }

    public void setAutoSmeltingEnabled(boolean autoSmeltingEnabled) {
        if (this.autoSmeltingEnabled == autoSmeltingEnabled) {
            return;
        }
        this.autoSmeltingEnabled = autoSmeltingEnabled;
        save();
    }

    public boolean hasBasicCompressionUpgrade() {
        return hasUpgrade(DeepNullUpgradeType.BASIC_COMPRESSION);
    }

    public boolean hasAdvancedCompressionUpgrade() {
        return hasUpgrade(DeepNullUpgradeType.ADVANCED_COMPRESSION);
    }

    public boolean hasStoneworksUpgrade() {
        return hasUpgrade(DeepNullUpgradeType.STONEWORKS);
    }

    public boolean isAutoPickupEnabled() {
        return !fluidOnly && autoPickupEnabled && DeepNullConfig.isAutoPickupEnabled();
    }

    public void setAutoPickupEnabled(boolean autoPickupEnabled) {
        if (this.autoPickupEnabled == autoPickupEnabled) {
            return;
        }
        this.autoPickupEnabled = autoPickupEnabled;
        save();
    }

    public boolean hasStoneGeneratorUpgrade() {
        return hasUpgrade(DeepNullUpgradeType.STONE_GENERATOR);
    }

    public boolean hasObsidianGeneratorUpgrade() {
        return hasUpgrade(DeepNullUpgradeType.OBSIDIAN_GENERATOR);
    }

    public boolean hasSpongeUpgrade() {
        return hasUpgrade(DeepNullUpgradeType.SPONGE);
    }

    public boolean hasGasUpgrade() {
        return hasUpgrade(DeepNullUpgradeType.GAS);
    }

    public boolean hasDeepEnergyUpgrade() {
        return hasUpgrade(DeepNullUpgradeType.DEEP_ENERGY);
    }

    public int getFrameColor() {
        return frameColor;
    }

    public int getGlassColor() {
        return glassColor;
    }

    public StyleGlassVariant getStyleVariant() {
        return styleVariant;
    }

    public void setFrameColor(int frameColor) {
        int next = sanitizeStyleColor(frameColor);
        if (this.frameColor == next) {
            return;
        }
        this.frameColor = next;
        save();
    }

    public void setGlassColor(int glassColor) {
        int next = sanitizeStyleColor(glassColor);
        if (this.glassColor == next) {
            return;
        }
        this.glassColor = next;
        save();
    }

    public void setStyleColors(int frameColor, int glassColor) {
        int nextFrame = sanitizeStyleColor(frameColor);
        int nextGlass = sanitizeStyleColor(glassColor);
        if (this.frameColor == nextFrame && this.glassColor == nextGlass) {
            return;
        }
        this.frameColor = nextFrame;
        this.glassColor = nextGlass;
        save();
    }

    public void setStyleVariant(StyleGlassVariant styleVariant) {
        StyleGlassVariant next = styleVariant == null || !styleVariant.supports(fluidOnly)
                ? StyleGlassVariant.DEFAULT
                : styleVariant;
        if (this.styleVariant == next) {
            return;
        }
        this.styleVariant = next;
        save();
    }

    public void setStyle(int frameColor, int glassColor, StyleGlassVariant styleVariant) {
        int nextFrame = sanitizeStyleColor(frameColor);
        int nextGlass = sanitizeStyleColor(glassColor);
        StyleGlassVariant nextVariant = styleVariant == null || !styleVariant.supports(fluidOnly)
                ? StyleGlassVariant.DEFAULT
                : styleVariant;
        if (this.frameColor == nextFrame && this.glassColor == nextGlass && this.styleVariant == nextVariant) {
            return;
        }
        this.frameColor = nextFrame;
        this.glassColor = nextGlass;
        this.styleVariant = nextVariant;
        save();
    }

    public void resetStyleColors() {
        setStyle(defaultFrameColor(), defaultGlassColor(), StyleGlassVariant.DEFAULT);
    }

    public CompoundTag exportConfiguration() {
        HolderLookup.Provider registries = registriesSupplier.get();
        CompoundTag tag = new CompoundTag();
        if (registries == null) {
            return tag;
        }
        sanitizeState();
        writeLocalStateToRoot(tag, registries);
        return tag;
    }

    public boolean importConfiguration(CompoundTag configuration) {
        HolderLookup.Provider registries = registriesSupplier.get();
        if (configuration == null || registries == null) {
            return false;
        }

        clearFilterStacks();
        clearAutoSmeltFilterStacks();
        readItemList(registries, configuration.getListOrEmpty(FILTER_ITEMS_TAG), filterStacks);
        readItemList(registries, configuration.getListOrEmpty(AUTO_SMELT_FILTER_ITEMS_TAG), autoSmeltFilterStacks);
        selectedSlot = configuration.getIntOr(SELECTED_TAG, -1);
        readEnumModes(configuration.getIntArray(EXTRACTION_TAG).orElseGet(() -> new int[0]), extractionModes, ItemExtractionMode.values(), ItemExtractionMode.KEEP_1);
        readIntModes(configuration.getIntArray(CUSTOM_EXTRACTION_TAG).orElseGet(() -> new int[0]), customExtractionAmounts);
        readEnumModes(configuration.getIntArray(PLACEMENT_TAG).orElseGet(() -> new int[0]), placementModes, ItemPlacementMode.values(), ItemPlacementMode.KEEP_1);
        readBooleanModes(configuration.getByteArray(TAG_MATCHING_TAG).orElseGet(() -> new byte[0]), tagMatchingModes);
        locked = supportsLocking() && configuration.getBooleanOr(LOCKED_TAG, false);
        filterMode = DeepNullFilterMode.byId(configuration.getIntOr(FILTER_MODE_TAG, DeepNullFilterMode.WHITELIST.ordinal()));
        autoSmeltFilterMode = normalizeAutoSmeltFilterMode(DeepNullFilterMode.byId(configuration.getIntOr(AUTO_SMELT_FILTER_MODE_TAG, DeepNullFilterMode.WHITELIST.ordinal())));
        contentMode = fluidOnly
                ? DeepNullContentMode.FLUIDS
                : DeepNullContentMode.byId(configuration.getIntOr(CONTENT_MODE_TAG, DeepNullContentMode.ITEMS.ordinal()));
        chargingEnabled = configuration.getBooleanOr(CHARGING_TAG, false);
        transferLocked = configuration.getBooleanOr(TRANSFER_LOCKED_TAG, false);
        autoPickupEnabled = configuration.contains(AUTO_PICKUP_TAG)
                ? configuration.getBooleanOr(AUTO_PICKUP_TAG, DeepNullConfig.defaultAutoPickupEnabled())
                : DeepNullConfig.defaultAutoPickupEnabled();
        autoFeedingEnabled = configuration.contains(AUTO_FEEDING_TAG)
                ? configuration.getBooleanOr(AUTO_FEEDING_TAG, DeepNullConfig.defaultAutoFeedingEnabled())
                : DeepNullConfig.defaultAutoFeedingEnabled();
        autoSmeltingEnabled = configuration.contains(AUTO_SMELTING_TAG)
                ? configuration.getBooleanOr(AUTO_SMELTING_TAG, DeepNullConfig.defaultAutoSmeltingEnabled())
                : DeepNullConfig.defaultAutoSmeltingEnabled();
        stoneGeneratorVariant = StoneGeneratorVariant.byId(configuration.getIntOr(STONE_GENERATOR_VARIANT_TAG, StoneGeneratorVariant.COBBLESTONE.ordinal()));
        stoneworksTargetStacks = hasNumeric(configuration, STONEWORKS_AMOUNT_TAG)
                ? configuration.getIntOr(STONEWORKS_AMOUNT_TAG, DeepNullConfig.defaultStoneworksAmount())
                : DeepNullConfig.defaultStoneworksAmount();
        if (hasByteArray(configuration, STONEWORKS_MONITOR_TAG)) {
            readBooleanModes(configuration.getByteArray(STONEWORKS_MONITOR_TAG).orElseGet(() -> new byte[0]), stoneworksMonitoring);
        } else {
            Arrays.fill(stoneworksMonitoring, true);
        }
        stoneworksCursor = configuration.getIntOr(STONEWORKS_CURSOR_TAG, 0);
        frameColor = hasNumeric(configuration, FRAME_COLOR_TAG)
                ? configuration.getIntOr(FRAME_COLOR_TAG, defaultFrameColor())
                : defaultFrameColor();
        glassColor = hasNumeric(configuration, GLASS_COLOR_TAG)
                ? configuration.getIntOr(GLASS_COLOR_TAG, defaultGlassColor())
                : defaultGlassColor();
        styleVariant = StyleGlassVariant.byId(configuration.getStringOr(STYLE_VARIANT_TAG, StyleGlassVariant.DEFAULT.id()));
        if (!styleVariant.supports(fluidOnly)) {
            styleVariant = StyleGlassVariant.DEFAULT;
        }
        sanitizeState();
        save();
        return true;
    }

    public int getStoneworksTargetStacks() {
        return stoneworksTargetStacks;
    }

    public void setStoneworksTargetStacks(int stoneworksTargetStacks) {
        int clamped = Math.max(0, Math.min(MAX_STONEWORKS_AMOUNT, stoneworksTargetStacks));
        if (this.stoneworksTargetStacks == clamped) {
            return;
        }
        this.stoneworksTargetStacks = clamped;
        save();
    }

    public boolean isStoneworksMonitoring(StoneworksMaterial material) {
        return stoneworksMonitoring[material.ordinal()];
    }

    public void setStoneworksMonitoring(StoneworksMaterial material, boolean monitoring) {
        if (stoneworksMonitoring[material.ordinal()] == monitoring) {
            return;
        }
        stoneworksMonitoring[material.ordinal()] = monitoring;
        save();
    }

    public void toggleStoneworksMonitoring(StoneworksMaterial material) {
        setStoneworksMonitoring(material, !isStoneworksMonitoring(material));
    }

    public List<StoneworksMaterial> getVisibleStoneworksMaterials() {
        List<StoneworksMaterial> visible = new ArrayList<>();
        for (StoneworksMaterial material : StoneworksMaterial.values()) {
            if (material != StoneworksMaterial.DUST || !resolveDustOutput().isEmpty()) {
                visible.add(material);
            }
        }
        return visible;
    }

    public ItemStack getStoneworksDisplayStack(StoneworksMaterial material) {
        return switch (material) {
            case DIRT -> new ItemStack(Items.DIRT);
            case GRAVEL -> new ItemStack(Items.GRAVEL);
            case SAND -> new ItemStack(Items.SAND);
            case DUST -> resolveDustOutput();
            case CLAY -> new ItemStack(Items.CLAY);
            case GLASS -> resolveGlassOutput();
        };
    }

    public boolean runStoneworksCycle(boolean hasWaterSupport) {
        if (fluidOnly || !hasStoneworksUpgrade() || !DeepNullConfig.isStoneworksEnabled()) {
            return false;
        }
        if (countExtractableLike(new ItemStack(Items.COBBLESTONE)) <= 0) {
            return false;
        }

        StoneworksMaterial[] order = StoneworksMaterial.roundRobinOrder();
        boolean changed = false;
        int nextCursor = stoneworksCursor;
        for (int operations = 0; operations < 64; operations++) {
            boolean progressed = false;
            for (int offset = 0; offset < order.length; offset++) {
                int index = (nextCursor + offset) % order.length;
                StoneworksMaterial material = order[index];
                if (!canRunStoneworksFor(material, hasWaterSupport)) {
                    continue;
                }

                if (runStoneworksFor(material, hasWaterSupport, 1) > 0) {
                    nextCursor = (index + 1) % order.length;
                    changed = true;
                    progressed = true;
                    break;
                }
            }
            if (!progressed) {
                break;
            }
        }
        if (changed) {
            stoneworksCursor = nextCursor;
            save();
        }
        return changed;
    }

    public StoneGeneratorVariant getStoneGeneratorVariant() {
        return stoneGeneratorVariant;
    }

    public void setStoneGeneratorVariant(StoneGeneratorVariant stoneGeneratorVariant) {
        StoneGeneratorVariant next = stoneGeneratorVariant == null ? StoneGeneratorVariant.COBBLESTONE : stoneGeneratorVariant;
        if (this.stoneGeneratorVariant == next) {
            return;
        }
        this.stoneGeneratorVariant = next;
        save();
    }

    public int getStoneGenerationRate() {
        return hasStoneGeneratorUpgrade() && DeepNullConfig.isStoneGeneratorEnabled() ? tier.stoneGenerationRate() : 0;
    }

    public ItemStack getStoneGeneratorOutput() {
        return stoneGeneratorVariant.stack();
    }

    public ItemStack getStoneGeneratorOutput(int count) {
        return stoneGeneratorVariant.stack(count);
    }

    public ItemStack getObsidianGeneratorOutput() {
        return new ItemStack(Items.OBSIDIAN);
    }

    public boolean hasStoneGenerationRequirements() {
        return hasStoneGeneratorUpgrade()
                && DeepNullConfig.isStoneGeneratorEnabled()
                && supportsFluidStorage()
                && containsFluidAmountAtLeast(Fluids.WATER, FluidType.BUCKET_VOLUME)
                && containsFluidAmountAtLeast(Fluids.LAVA, FluidType.BUCKET_VOLUME);
    }

    public boolean hasObsidianGenerationRequirements() {
        return hasObsidianGeneratorUpgrade()
                && DeepNullConfig.isObsidianGeneratorEnabled()
                && supportsFluidStorage()
                && containsFluidAmountAtLeast(Fluids.WATER, FluidType.BUCKET_VOLUME)
                && containsFluidAmountAtLeast(Fluids.LAVA, FluidType.BUCKET_VOLUME);
    }

    public boolean consumeObsidianGeneratorInputs() {
        if (!hasObsidianGenerationRequirements()) {
            return false;
        }
        FluidStack drainedWater = drainFluid(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), false);
        FluidStack drainedLava = drainFluid(new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME), false);
        return drainedWater.getAmount() >= FluidType.BUCKET_VOLUME && drainedLava.getAmount() >= FluidType.BUCKET_VOLUME;
    }

    public boolean allowsAutomationOutput(ItemStack stack) {
        if (fluidOnly || stack.isEmpty() || (supportsLocking() && locked)) {
            return false;
        }
        return containsMatchingStack(stack) || filterExplicitlyAllows(stack);
    }

    public boolean allowsGeneratedOutput(ItemStack stack) {
        if (fluidOnly || stack.isEmpty() || (supportsLocking() && locked)) {
            return false;
        }
        return containsGeneratorSeedStack(stack) || filterExplicitlyAllows(stack);
    }

    public ItemStack insertAutomationOutput(ItemStack stack, boolean simulate) {
        if (fluidOnly || stack.isEmpty() || (supportsLocking() && locked)) {
            return stack;
        }
        if (containsMatchingStack(stack)) {
            return insertIntoExistingSlotsOnly(stack, simulate);
        }
        if (!filterExplicitlyAllows(stack)) {
            return stack;
        }
        return insertPreparedIntoFirstAvailableSlot(stack, simulate);
    }

    public int getEnergyStored() {
        if (!hasEnergyUpgrade()) {
            return 0;
        }
        return tier.creative() ? CREATIVE_DISPLAY_ENERGY : storedEnergy;
    }

    public int getEnergyCapacity() {
        if (!hasEnergyUpgrade()) {
            return 0;
        }
        return hasDeepEnergyUpgrade() ? tier.deepEnergyCapacity() : tier.energyCapacity();
    }

    public int getEnergyTransferRate() {
        if (!hasEnergyUpgrade()) {
            return 0;
        }
        return hasDeepEnergyUpgrade() ? tier.deepEnergyTransfer() : tier.energyTransfer();
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

    public boolean isTransferLocked() {
        return transferLocked;
    }

    public void setTransferLocked(boolean transferLocked) {
        if (this.transferLocked == transferLocked) {
            return;
        }
        this.transferLocked = transferLocked;
        save();
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!hasEnergyUpgrade() || maxReceive <= 0) {
            return 0;
        }
        if (tier.creative()) {
            return maxReceive;
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
        if (tier.creative()) {
            return maxExtract;
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
        int upperBound = fluidOnly ? getFluidSlotCount() : getSlots();
        if (selectedSlot < -1 || selectedSlot >= upperBound) {
            this.selectedSlot = -1;
        } else {
            this.selectedSlot = selectedSlot;
        }
        if (fluidOnly) {
            if (this.selectedSlot < 0 && getFluidSlotCount() > 0) {
                this.selectedSlot = 0;
            } else if (this.selectedSlot >= getFluidSlotCount()) {
                this.selectedSlot = getFluidSlotCount() > 0 ? 0 : -1;
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

    public int getExtractionMinimum(int slot) {
        validateSlotIndex(slot);
        return extractionModes[slot] == ItemExtractionMode.CUSTOM
                ? Math.max(0, customExtractionAmounts[slot])
                : Math.max(0, extractionModes[slot].keptAmount());
    }

    public Component getExtractionTooltip(int slot) {
        validateSlotIndex(slot);
        return extractionModes[slot].tooltip(getExtractionMinimum(slot));
    }

    public void setExtractionMode(int slot, ItemExtractionMode mode) {
        validateSlotIndex(slot);
        extractionModes[slot] = mode;
        if (mode != ItemExtractionMode.CUSTOM) {
            customExtractionAmounts[slot] = 0;
        }
        save();
    }

    public void setCustomExtractionMinimum(int slot, int amount) {
        validateSlotIndex(slot);
        int clamped = Math.max(0, Math.min(getSlotLimit(slot), amount));
        ItemExtractionMode nextMode = switch (clamped) {
            case 0 -> ItemExtractionMode.KEEP_NONE;
            case 1 -> ItemExtractionMode.KEEP_1;
            case 16 -> ItemExtractionMode.KEEP_16;
            case 64 -> ItemExtractionMode.KEEP_64;
            default -> ItemExtractionMode.CUSTOM;
        };
        int nextCustomAmount = nextMode == ItemExtractionMode.CUSTOM ? clamped : 0;
        if (extractionModes[slot] == nextMode && customExtractionAmounts[slot] == nextCustomAmount) {
            return;
        }
        extractionModes[slot] = nextMode;
        customExtractionAmounts[slot] = nextCustomAmount;
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

        int customExtractionAmount = customExtractionAmounts[fromSlot];
        customExtractionAmounts[fromSlot] = customExtractionAmounts[toSlot];
        customExtractionAmounts[toSlot] = customExtractionAmount;

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
        int keptAmount = getExtractionMinimum(slot);
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

    public boolean containsExtractableMatchingStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        for (int slot = 0; slot < getSlots(); slot++) {
            if (getExtractableAmount(slot) <= 0) {
                continue;
            }

            ItemStack stored = getStackInSlot(slot);
            if (ItemStack.isSameItemSameComponents(stored, stack) || ItemStack.isSameItem(stored, stack)) {
                return true;
            }
        }

        return false;
    }

    public boolean containsGeneratorSeedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        for (int slot = 0; slot < getSlots(); slot++) {
            if (getExtractionMode(slot) == ItemExtractionMode.KEEP_ALL) {
                continue;
            }

            ItemStack stored = getStackInSlot(slot);
            if (ItemStack.isSameItemSameComponents(stored, stack) || ItemStack.isSameItem(stored, stack)) {
                return true;
            }
        }

        return false;
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
        ItemStack transformedRemainder = tryInsertAutoTransformed(stack, simulate, false);
        if (transformedRemainder != null) {
            return transformedRemainder;
        }
        return insertIntoExistingSlotsOnly(stack, simulate);
    }

    public ItemStack insertIntoFirstAvailableSlot(ItemStack stack, boolean simulate) {
        ItemStack transformedRemainder = tryInsertAutoTransformed(stack, simulate, false);
        if (transformedRemainder != null) {
            return transformedRemainder;
        }
        return insertPreparedIntoFirstAvailableSlot(stack, simulate);
    }

    public ItemStack insertReturnedCraftingStack(ItemStack stack, boolean simulate) {
        if (fluidOnly || stack.isEmpty() || stack.getItem() instanceof DeepNullItem) {
            return stack;
        }

        ItemStack remaining = insertReturnedIntoMatchingSlots(stack, simulate, true);
        remaining = insertReturnedIntoMatchingSlots(remaining, simulate, false);
        for (int slot = 0; slot < getSlots() && !remaining.isEmpty(); slot++) {
            if (getStackInSlot(slot).isEmpty()) {
                remaining = insertReturnedItem(slot, remaining, simulate);
            }
        }
        return remaining;
    }

    public ItemStack insertPickedUpIntoMatchingSlots(ItemStack stack, boolean simulate) {
        ItemStack transformedRemainder = tryInsertAutoTransformed(stack, simulate, true);
        if (transformedRemainder != null) {
            return transformedRemainder;
        }
        return insertIntoExistingSlotsOnly(stack, simulate);
    }

    public ItemStack insertIntoExistingSlotsOnly(ItemStack stack, boolean simulate) {
        ItemStack remaining = insertIntoMatchingSlotsPrepared(stack, simulate, true);
        return insertIntoMatchingSlotsPrepared(remaining, simulate, false);
    }

    public ItemStack insertPickedUpIntoFirstAvailableSlot(ItemStack stack, boolean simulate) {
        ItemStack transformedRemainder = tryInsertAutoTransformed(stack, simulate, true);
        if (transformedRemainder != null) {
            return transformedRemainder;
        }
        return insertPreparedIntoFirstAvailableSlot(stack, simulate);
    }

    private ItemStack insertPreparedIntoFirstAvailableSlot(ItemStack stack, boolean simulate) {
        if (!passesFilter(stack)) {
            return stack;
        }
        ItemStack remaining = insertIntoMatchingSlotsPrepared(stack, simulate, true);
        remaining = insertIntoMatchingSlotsPrepared(remaining, simulate, false);
        for (int slot = 0; slot < getSlots() && !remaining.isEmpty(); slot++) {
            if (getStackInSlot(slot).isEmpty()) {
                remaining = insertItem(slot, remaining, simulate);
            }
        }
        return remaining;
    }

    private ItemStack insertReturnedIntoMatchingSlots(ItemStack stack, boolean simulate, boolean exactMatchOnly) {
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
            remaining = insertReturnedItem(slot, remaining, simulate);
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
        if (fluidOnly) {
            return false;
        }
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

    private ItemStack insertReturnedItem(int slot, ItemStack stack, boolean simulate) {
        validateSlotIndex(slot);
        if (stack.isEmpty() || stack.getCount() <= 0 || stack.getItem() instanceof DeepNullItem) {
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

    public ItemStack consumeStoredItem(int slot, int amount) {
        return extractItemInternal(slot, amount, false, true);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (getStackInSlot(slot).isEmpty()) {
            extractionModes[slot] = ItemExtractionMode.KEEP_1;
            customExtractionAmounts[slot] = 0;
        }
        if (!fluidOnly && contentMode == DeepNullContentMode.ITEMS) {
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

    private ItemStack insertIntoMatchingSlotsPrepared(ItemStack stack, boolean simulate, boolean exactMatchOnly) {
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

    private @Nullable ItemStack tryInsertAutoSmelted(ItemStack stack, boolean simulate) {
        SmeltConversion conversion = resolveAutoSmelt(stack);
        if (conversion == null || !passesFilter(conversion.outputSample())) {
            return null;
        }

        DeepNullInventory simulationInventory = new DeepNullInventory(tier, backingStack.copy(), registriesSupplier, null);
        ItemStack convertedStack = conversion.outputForInputs(stack.getCount());
        ItemStack convertedRemainder = simulationInventory.insertPreparedIntoFirstAvailableSlot(convertedStack, false);
        int insertedOutput = convertedStack.getCount() - convertedRemainder.getCount();
        int consumedInputs = insertedOutput / conversion.outputPerInput();
        if (consumedInputs <= 0) {
            return stack;
        }

        if (!simulate) {
            insertPreparedIntoFirstAvailableSlot(conversion.outputForInputs(consumedInputs), false);
        }
        return remainder(stack, consumedInputs);
    }

    private @Nullable ItemStack tryInsertAutoCompressed(ItemStack stack, boolean simulate) {
        CompressionConversion conversion = resolveCompression(stack);
        if (conversion == null || !passesFilter(conversion.outputSample())) {
            return null;
        }

        DeepNullInventory simulationInventory = new DeepNullInventory(tier, backingStack.copy(), registriesSupplier, null);
        ItemStack convertedStack = conversion.outputForInputs(stack.getCount());
        ItemStack convertedRemainder = simulationInventory.insertPreparedIntoFirstAvailableSlot(convertedStack, false);
        int insertedOutput = convertedStack.getCount() - convertedRemainder.getCount();
        int consumedInputs = conversion.inputCountForProduced(insertedOutput);
        if (consumedInputs <= 0) {
            return stack;
        }

        if (!simulate) {
            insertPreparedIntoFirstAvailableSlot(conversion.outputForInputs(consumedInputs), false);
        }
        return remainder(stack, consumedInputs);
    }

    private @Nullable ItemStack tryInsertAutoTransformed(ItemStack stack, boolean simulate, boolean allowAutoSmelt) {
        ItemStack current = stack;
        boolean transformed = false;

        if (allowAutoSmelt) {
            ItemStack smeltedRemainder = tryInsertAutoSmelted(current, simulate);
            if (smeltedRemainder != null) {
                current = smeltedRemainder;
                transformed = true;
                if (current.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        ItemStack compressedRemainder = tryInsertAutoCompressed(current, simulate);
        if (compressedRemainder != null) {
            return compressedRemainder;
        }

        return transformed ? current : null;
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
        if (!hasCompound(tag, ROOT_TAG)) {
            return;
        }
        CompoundTag root = tag.getCompoundOrEmpty(ROOT_TAG);
        readPrimaryStorageFromRoot(registries, root);
        if (hasList(root, UPGRADES_TAG)) {
            readItemList(registries, root.getListOrEmpty(UPGRADES_TAG), upgradeHandler.rawStacks());
            upgradeHandler.ensureSlotCount();
        }
        readItemList(registries, root.getListOrEmpty(FILTER_ITEMS_TAG), filterStacks);
        readItemList(registries, root.getListOrEmpty(AUTO_SMELT_FILTER_ITEMS_TAG), autoSmeltFilterStacks);
        selectedSlot = root.getIntOr(SELECTED_TAG, -1);
        readEnumModes(root.getIntArray(EXTRACTION_TAG).orElseGet(() -> new int[0]), extractionModes, ItemExtractionMode.values(), ItemExtractionMode.KEEP_1);
        readIntModes(root.getIntArray(CUSTOM_EXTRACTION_TAG).orElseGet(() -> new int[0]), customExtractionAmounts);
        readEnumModes(root.getIntArray(PLACEMENT_TAG).orElseGet(() -> new int[0]), placementModes, ItemPlacementMode.values(), ItemPlacementMode.KEEP_1);
        readBooleanModes(root.getByteArray(TAG_MATCHING_TAG).orElseGet(() -> new byte[0]), tagMatchingModes);
        locked = supportsLocking() && root.getBooleanOr(LOCKED_TAG, false);
        filterMode = DeepNullFilterMode.byId(root.getIntOr(FILTER_MODE_TAG, DeepNullFilterMode.WHITELIST.ordinal()));
        autoSmeltFilterMode = normalizeAutoSmeltFilterMode(DeepNullFilterMode.byId(root.getIntOr(AUTO_SMELT_FILTER_MODE_TAG, DeepNullFilterMode.WHITELIST.ordinal())));
        contentMode = fluidOnly
                ? DeepNullContentMode.FLUIDS
                : DeepNullContentMode.byId(root.getIntOr(CONTENT_MODE_TAG, DeepNullContentMode.ITEMS.ordinal()));
        storedEnergy = Math.max(0, root.getIntOr(ENERGY_TAG, 0));
        chargingEnabled = root.getBooleanOr(CHARGING_TAG, false);
        transferLocked = root.getBooleanOr(TRANSFER_LOCKED_TAG, false);
        autoPickupEnabled = root.contains(AUTO_PICKUP_TAG)
                ? root.getBooleanOr(AUTO_PICKUP_TAG, DeepNullConfig.defaultAutoPickupEnabled())
                : DeepNullConfig.defaultAutoPickupEnabled();
        autoFeedingEnabled = root.contains(AUTO_FEEDING_TAG)
                ? root.getBooleanOr(AUTO_FEEDING_TAG, DeepNullConfig.defaultAutoFeedingEnabled())
                : DeepNullConfig.defaultAutoFeedingEnabled();
        autoSmeltingEnabled = root.contains(AUTO_SMELTING_TAG)
                ? root.getBooleanOr(AUTO_SMELTING_TAG, DeepNullConfig.defaultAutoSmeltingEnabled())
                : DeepNullConfig.defaultAutoSmeltingEnabled();
        stoneGeneratorVariant = StoneGeneratorVariant.byId(root.getIntOr(STONE_GENERATOR_VARIANT_TAG, StoneGeneratorVariant.COBBLESTONE.ordinal()));
        stoneworksTargetStacks = hasNumeric(root, STONEWORKS_AMOUNT_TAG)
                ? root.getIntOr(STONEWORKS_AMOUNT_TAG, DeepNullConfig.defaultStoneworksAmount())
                : DeepNullConfig.defaultStoneworksAmount();
        if (hasByteArray(root, STONEWORKS_MONITOR_TAG)) {
            readBooleanModes(root.getByteArray(STONEWORKS_MONITOR_TAG).orElseGet(() -> new byte[0]), stoneworksMonitoring);
        } else {
            Arrays.fill(stoneworksMonitoring, true);
        }
        stoneworksCursor = root.getIntOr(STONEWORKS_CURSOR_TAG, 0);
        frameColor = hasNumeric(root, FRAME_COLOR_TAG)
                ? root.getIntOr(FRAME_COLOR_TAG, defaultFrameColor())
                : defaultFrameColor();
        glassColor = hasNumeric(root, GLASS_COLOR_TAG)
                ? root.getIntOr(GLASS_COLOR_TAG, defaultGlassColor())
                : defaultGlassColor();
        styleVariant = StyleGlassVariant.byId(root.getStringOr(STYLE_VARIANT_TAG, StyleGlassVariant.DEFAULT.id()));
        if (!styleVariant.supports(fluidOnly)) {
            styleVariant = StyleGlassVariant.DEFAULT;
        }

        LinkedDockSource linkedSource = resolveLinkedDockSource(true);
        if (linkedSource != null) {
            overlayLinkedStorage(registries, linkedSource.dock().getStoredDeepNull());
            cacheLinkedStorageLocallyIfChanged(registries);
        } else if (EnderUpgradeItem.isLinked(getEnderUpgradeStack())) {
            overlayMirrorStorageFromRoot(registries, root);
        }

        if (selectedSlot < -1 || selectedSlot >= getSlots()) {
            selectedSlot = -1;
        }
        if (fluidOnly) {
            if (selectedSlot < 0 && getFluidSlotCount() > 0) {
                selectedSlot = 0;
            }
        } else if (selectedSlot >= 0 && getStackInSlot(selectedSlot).isEmpty()) {
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

        CompoundTag tag = backingStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag root = hasCompound(tag, ROOT_TAG) ? tag.getCompoundOrEmpty(ROOT_TAG).copy() : new CompoundTag();
        LinkedDockSource linkedSource = resolveLinkedDockSource(false);

        writeLocalStateToRoot(root, registries);
        if (linkedSource != null) {
            writeMirrorStorageToRoot(root, registries);
            CustomData.update(DataComponents.CUSTOM_DATA, backingStack, customTag -> customTag.put(ROOT_TAG, root));
            writeLinkedStorage(registries, linkedSource);
        } else {
            writePrimaryStorageToRoot(root, registries);
            clearMirrorStorage(root);
            CustomData.update(DataComponents.CUSTOM_DATA, backingStack, customTag -> customTag.put(ROOT_TAG, root));
        }

        if (changeListener != null) {
            changeListener.run();
        }
    }

    private void readPrimaryStorageFromRoot(HolderLookup.Provider registries, CompoundTag root) {
        clearStorageContents();
        if (!fluidOnly) {
            if (hasList(root, ITEMS_TAG)) {
                readStoredItemList(registries, root.getListOrEmpty(ITEMS_TAG), stacks);
            } else if (hasCompound(root, ITEMS_TAG)) {
                readStoredItemList(registries, root.getCompoundOrEmpty(ITEMS_TAG).getListOrEmpty("Items"), stacks);
            }
        }
        if (supportsFluidStorage()) {
            if (hasList(root, FLUIDS_TAG)) {
                readFluidList(registries, root.getListOrEmpty(FLUIDS_TAG), fluidStacks);
            } else if (hasCompound(root, "Fluid")) {
                FluidStack migrated = readFluidValue(root, "Fluid");
                if (!migrated.isEmpty() && !fluidStacks.isEmpty()) {
                    fluidStacks.set(0, migrated);
                }
            }
            if (hasList(root, CHEMICALS_TAG)) {
                readChemicalList(root.getListOrEmpty(CHEMICALS_TAG), chemicalStacks);
            }
        }
    }

    private void overlayLinkedStorage(HolderLookup.Provider registries, ItemStack sourceStack) {
        CompoundTag sourceTag = sourceStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!hasCompound(sourceTag, ROOT_TAG)) {
            clearStorageContents();
            return;
        }
        readStorageFromRoot(registries, sourceTag.getCompoundOrEmpty(ROOT_TAG), ITEMS_TAG, FLUIDS_TAG, CHEMICALS_TAG);
    }

    private void overlayMirrorStorageFromRoot(HolderLookup.Provider registries, CompoundTag root) {
        readStorageFromRoot(registries, root, ENDER_MIRROR_ITEMS_TAG, ENDER_MIRROR_FLUIDS_TAG, ENDER_MIRROR_CHEMICALS_TAG);
    }

    private void readStorageFromRoot(
            HolderLookup.Provider registries,
            CompoundTag root,
            String itemsKey,
            String fluidsKey,
            String chemicalsKey
    ) {
        clearStorageContents();
        if (!fluidOnly && hasList(root, itemsKey)) {
            readStoredItemList(registries, root.getListOrEmpty(itemsKey), stacks);
        }
        if (supportsFluidStorage()) {
            if (hasList(root, fluidsKey)) {
                readFluidList(registries, root.getListOrEmpty(fluidsKey), fluidStacks);
            }
            if (hasList(root, chemicalsKey)) {
                readChemicalList(root.getListOrEmpty(chemicalsKey), chemicalStacks);
            }
        }
    }

    private void writeLocalStateToRoot(CompoundTag root, HolderLookup.Provider registries) {
        root.put(UPGRADES_TAG, writeItemList(registries, upgradeHandler.rawStacks()));
        root.put(FILTER_ITEMS_TAG, writeItemList(registries, filterStacks));
        root.put(AUTO_SMELT_FILTER_ITEMS_TAG, writeItemList(registries, autoSmeltFilterStacks));
        root.putInt(SELECTED_TAG, selectedSlot);
        root.putIntArray(EXTRACTION_TAG, Arrays.stream(extractionModes).mapToInt(Enum::ordinal).toArray());
        root.putIntArray(CUSTOM_EXTRACTION_TAG, Arrays.copyOf(customExtractionAmounts, customExtractionAmounts.length));
        root.putIntArray(PLACEMENT_TAG, Arrays.stream(placementModes).mapToInt(Enum::ordinal).toArray());
        root.putByteArray(TAG_MATCHING_TAG, booleanModesAsBytes(tagMatchingModes));

        if (supportsLocking() && locked) {
            root.putBoolean(LOCKED_TAG, true);
        } else {
            root.remove(LOCKED_TAG);
        }

        if (supportsFiltering()) {
            root.putInt(FILTER_MODE_TAG, filterMode.ordinal());
        } else {
            root.remove(FILTER_MODE_TAG);
        }

        if (supportsAutoSmeltFiltering()) {
            root.putInt(AUTO_SMELT_FILTER_MODE_TAG, normalizeAutoSmeltFilterMode(autoSmeltFilterMode).ordinal());
        } else {
            root.remove(AUTO_SMELT_FILTER_MODE_TAG);
        }

        if (!fluidOnly && contentMode != DeepNullContentMode.ITEMS) {
            root.putInt(CONTENT_MODE_TAG, contentMode.ordinal());
        } else {
            root.remove(CONTENT_MODE_TAG);
        }

        if (hasEnergyUpgrade() && storedEnergy > 0) {
            root.putInt(ENERGY_TAG, storedEnergy);
        } else {
            root.remove(ENERGY_TAG);
        }

        if (isChargingEnabled()) {
            root.putBoolean(CHARGING_TAG, true);
        } else {
            root.remove(CHARGING_TAG);
        }

        if (transferLocked) {
            root.putBoolean(TRANSFER_LOCKED_TAG, true);
        } else {
            root.remove(TRANSFER_LOCKED_TAG);
        }

        root.putBoolean(AUTO_PICKUP_TAG, autoPickupEnabled);
        root.putBoolean(AUTO_FEEDING_TAG, autoFeedingEnabled);
        root.putBoolean(AUTO_SMELTING_TAG, autoSmeltingEnabled);

        if (stoneGeneratorVariant != StoneGeneratorVariant.COBBLESTONE) {
            root.putInt(STONE_GENERATOR_VARIANT_TAG, stoneGeneratorVariant.ordinal());
        } else {
            root.remove(STONE_GENERATOR_VARIANT_TAG);
        }

        if (stoneworksTargetStacks != DeepNullConfig.defaultStoneworksAmount()) {
            root.putInt(STONEWORKS_AMOUNT_TAG, stoneworksTargetStacks);
        } else {
            root.remove(STONEWORKS_AMOUNT_TAG);
        }
        root.putByteArray(STONEWORKS_MONITOR_TAG, booleanModesAsBytes(stoneworksMonitoring));
        if (stoneworksCursor != 0) {
            root.putInt(STONEWORKS_CURSOR_TAG, stoneworksCursor);
        } else {
            root.remove(STONEWORKS_CURSOR_TAG);
        }

        if (frameColor != defaultFrameColor()) {
            root.putInt(FRAME_COLOR_TAG, frameColor);
        } else {
            root.remove(FRAME_COLOR_TAG);
        }

        if (glassColor != defaultGlassColor()) {
            root.putInt(GLASS_COLOR_TAG, glassColor);
        } else {
            root.remove(GLASS_COLOR_TAG);
        }

        if (styleVariant != StyleGlassVariant.DEFAULT) {
            root.putString(STYLE_VARIANT_TAG, styleVariant.id());
        } else {
            root.remove(STYLE_VARIANT_TAG);
        }
    }

    private void writePrimaryStorageToRoot(CompoundTag root, HolderLookup.Provider registries) {
        if (!fluidOnly) {
            root.put(ITEMS_TAG, writeStoredItemList(registries, stacks));
        }
        if (supportsFluidStorage()) {
            putOrRemoveList(root, FLUIDS_TAG, writeFluidList(registries, fluidStacks));
            putOrRemoveList(root, CHEMICALS_TAG, writeChemicalList(chemicalStacks));
        } else {
            root.remove(FLUIDS_TAG);
            root.remove(CHEMICALS_TAG);
        }
    }

    private void writeMirrorStorageToRoot(CompoundTag root, HolderLookup.Provider registries) {
        if (!fluidOnly) {
            putOrRemoveList(root, ENDER_MIRROR_ITEMS_TAG, writeStoredItemList(registries, stacks));
        }
        if (supportsFluidStorage()) {
            putOrRemoveList(root, ENDER_MIRROR_FLUIDS_TAG, writeFluidList(registries, fluidStacks));
            putOrRemoveList(root, ENDER_MIRROR_CHEMICALS_TAG, writeChemicalList(chemicalStacks));
        } else {
            root.remove(ENDER_MIRROR_FLUIDS_TAG);
            root.remove(ENDER_MIRROR_CHEMICALS_TAG);
        }
    }

    private void clearMirrorStorage(CompoundTag root) {
        root.remove(ENDER_MIRROR_ITEMS_TAG);
        root.remove(ENDER_MIRROR_FLUIDS_TAG);
        root.remove(ENDER_MIRROR_CHEMICALS_TAG);
    }

    private void writeLinkedStorage(HolderLookup.Provider registries, LinkedDockSource linkedSource) {
        ItemStack sourceStack = linkedSource.dock().getStoredDeepNull();
        if (sourceStack.isEmpty()) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, sourceStack, tag -> {
            CompoundTag root = hasCompound(tag, ROOT_TAG) ? tag.getCompoundOrEmpty(ROOT_TAG).copy() : new CompoundTag();
            if (!fluidOnly) {
                root.put(ITEMS_TAG, writeStoredItemList(registries, stacks));
            }
            if (supportsFluidStorage()) {
                putOrRemoveList(root, FLUIDS_TAG, writeFluidList(registries, fluidStacks));
                putOrRemoveList(root, CHEMICALS_TAG, writeChemicalList(chemicalStacks));
            }
            tag.put(ROOT_TAG, root);
        });
        linkedSource.dock().markStoredDeepNullChanged();
    }

    private void cacheLinkedStorageLocallyIfChanged(HolderLookup.Provider registries) {
        CompoundTag currentTag = backingStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag currentRoot = hasCompound(currentTag, ROOT_TAG) ? currentTag.getCompoundOrEmpty(ROOT_TAG).copy() : new CompoundTag();
        CompoundTag updatedRoot = currentRoot.copy();
        writeMirrorStorageToRoot(updatedRoot, registries);
        if (updatedRoot.equals(currentRoot)) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, backingStack, tag -> tag.put(ROOT_TAG, updatedRoot));
        if (changeListener != null) {
            changeListener.run();
        }
    }

    private void clearStorageContents() {
        for (int slot = 0; slot < stacks.size(); slot++) {
            stacks.set(slot, ItemStack.EMPTY);
        }
        for (int slot = 0; slot < fluidStacks.size(); slot++) {
            fluidStacks.set(slot, FluidStack.EMPTY);
        }
        for (int slot = 0; slot < chemicalStacks.size(); slot++) {
            chemicalStacks.set(slot, StoredChemical.EMPTY);
        }
    }

    private void putOrRemoveList(CompoundTag root, String key, net.minecraft.nbt.ListTag value) {
        if (value.isEmpty()) {
            root.remove(key);
            return;
        }
        root.put(key, value);
    }

    private ItemStack getEnderUpgradeStack() {
        if (DeepNullUpgradeType.ENDER.slot() < 0 || DeepNullUpgradeType.ENDER.slot() >= upgradeHandler.getSlots()) {
            return ItemStack.EMPTY;
        }
        return upgradeHandler.getStackInSlot(DeepNullUpgradeType.ENDER.slot());
    }

    private @Nullable LinkedDockSource resolveLinkedDockSource(boolean clearInvalidLink) {
        ItemStack enderUpgrade = getEnderUpgradeStack();
        EnderUpgradeItem.LinkData link = EnderUpgradeItem.getLink(enderUpgrade);
        if (link == null) {
            return null;
        }

        if (!EnderUpgradeItem.matchesNull(enderUpgrade, tier, fluidOnly)) {
            return null;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }

        Level level = server.getLevel(link.dimension());
        if (level == null || !(level.getBlockEntity(link.pos()) instanceof DeepNullDockBlockEntity dock) || !dock.hasStoredDeepNull()) {
            invalidateEnderLink(enderUpgrade, clearInvalidLink);
            return null;
        }
        if (!(dock.getStoredDeepNull().getItem() instanceof DeepNullItem linkedItem)) {
            invalidateEnderLink(enderUpgrade, clearInvalidLink);
            return null;
        }

        boolean linkedFluidOnly = linkedItem instanceof DampNullItem;
        if (linkedFluidOnly != fluidOnly || linkedItem.tier() != tier || linkedFluidOnly != link.fluidOnly() || linkedItem.tier() != link.tier()) {
            invalidateEnderLink(enderUpgrade, clearInvalidLink);
            return null;
        }

        return new LinkedDockSource(dock);
    }

    private void invalidateEnderLink(ItemStack enderUpgrade, boolean clearInvalidLink) {
        if (!clearInvalidLink) {
            return;
        }
        EnderUpgradeItem.clearLink(enderUpgrade);
        pendingLinkCleanup = true;
    }

    private void sanitizeState() {
        migrateLegacyUpgradeSlots();
        for (int slot = 0; slot < getSlots(); slot++) {
            if (getStackInSlot(slot).isEmpty()) {
                extractionModes[slot] = ItemExtractionMode.KEEP_1;
                customExtractionAmounts[slot] = 0;
            } else if (extractionModes[slot] == ItemExtractionMode.CUSTOM) {
                if (customExtractionAmounts[slot] <= 0) {
                    extractionModes[slot] = ItemExtractionMode.KEEP_NONE;
                    customExtractionAmounts[slot] = 0;
                } else {
                    customExtractionAmounts[slot] = Math.min(customExtractionAmounts[slot], getSlotLimit(slot));
                }
            } else {
                customExtractionAmounts[slot] = 0;
            }
            if (!supportsTagMatching(slot)) {
                tagMatchingModes[slot] = false;
            }
        }
        for (int slot = 0; slot < upgradeHandler.getSlots(); slot++) {
            ItemStack upgradeStack = upgradeHandler.getStackInSlot(slot);
            DeepNullUpgradeType upgradeType = upgradeType(upgradeStack);
            if (upgradeType == null || upgradeType.slot() != slot || !supportsUpgrade(upgradeType)) {
                upgradeHandler.clearSlotSilently(slot);
            }
        }
        if (!supportsFiltering()) {
            clearFilterStacks();
            filterMode = DeepNullFilterMode.WHITELIST;
        }
        if (!supportsAutoSmeltFiltering()) {
            clearAutoSmeltFilterStacks();
            autoSmeltFilterMode = DeepNullFilterMode.BLACKLIST;
        } else {
            autoSmeltFilterMode = normalizeAutoSmeltFilterMode(autoSmeltFilterMode);
        }
        if (!supportsFluidStorage()) {
            clearFluidStacks();
            clearChemicalStacks();
        } else {
            for (int slot = 0; slot < fluidStacks.size(); slot++) {
                FluidStack fluidStack = fluidStacks.get(slot);
                if (!fluidStack.isEmpty() && fluidStack.getAmount() > getFluidCapacity()) {
                    fluidStack.setAmount(getFluidCapacity());
                }
                StoredChemical chemicalStack = chemicalStacks.get(slot);
                if (!chemicalStack.isEmpty()) {
                    long clamped = Math.min(chemicalStack.amount(), getFluidCapacity());
                    chemicalStacks.set(slot, clamped <= 0L ? StoredChemical.EMPTY : chemicalStack.copyWithAmount(clamped));
                }
                if (!fluidStacks.get(slot).isEmpty() && !chemicalStacks.get(slot).isEmpty()) {
                    chemicalStacks.set(slot, StoredChemical.EMPTY);
                }
            }
        }
        stoneworksTargetStacks = Math.max(0, Math.min(MAX_STONEWORKS_AMOUNT, stoneworksTargetStacks));
        stoneworksCursor = Math.floorMod(stoneworksCursor, StoneworksMaterial.roundRobinOrder().length);
        frameColor = sanitizeStyleColor(frameColor);
        glassColor = sanitizeStyleColor(glassColor);
        if (!styleVariant.supports(fluidOnly)) {
            styleVariant = StyleGlassVariant.DEFAULT;
        }
        autoPickupEnabled = autoPickupEnabled && DeepNullConfig.isAutoPickupEnabled();
        autoFeedingEnabled = autoFeedingEnabled && hasAutoFeedingUpgrade() && DeepNullConfig.isAutoFeedingEnabled();
        autoSmeltingEnabled = autoSmeltingEnabled && hasAutoSmeltingUpgrade() && DeepNullConfig.isAutoSmeltingEnabled();
        contentMode = fluidOnly ? DeepNullContentMode.FLUIDS : DeepNullContentMode.ITEMS;
        if (!hasEnergyUpgrade()) {
            storedEnergy = 0;
            chargingEnabled = false;
        } else {
            storedEnergy = Math.max(0, Math.min(storedEnergy, getEnergyCapacity()));
        }
        if (fluidOnly) {
            if (selectedSlot < 0 && getFluidSlotCount() > 0) {
                selectedSlot = 0;
            } else if (selectedSlot >= getFluidSlotCount()) {
                selectedSlot = getFluidSlotCount() > 0 ? 0 : -1;
            }
        } else if (selectedSlot >= 0 && getStackInSlot(selectedSlot).isEmpty()) {
            selectedSlot = findFirstOccupiedSlot();
        }
    }

    private boolean passesFilter(ItemStack stack) {
        if (!supportsFiltering() || stack.isEmpty()) {
            return true;
        }

        if (filterMode.usesGhostSlots()) {
            return passesGhostSlotFilter(filterStacks, filterMode, stack);
        }

        return filterMode.matchesPreset(stack);
    }

    private boolean passesAutoSmeltFilter(ItemStack stack) {
        if (!supportsAutoSmeltFiltering() || stack.isEmpty()) {
            return true;
        }
        return passesGhostSlotFilter(autoSmeltFilterStacks, normalizeAutoSmeltFilterMode(autoSmeltFilterMode), stack);
    }

    private boolean passesGhostSlotFilter(NonNullList<ItemStack> configuredStacks, DeepNullFilterMode mode, ItemStack stack) {
        boolean hasEntries = false;
        boolean matched = false;
        for (ItemStack filterStack : configuredStacks) {
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
        return mode == DeepNullFilterMode.WHITELIST ? matched : !matched;
    }

    private boolean matchesFilterStack(ItemStack filterStack, ItemStack incomingStack) {
        if (filterStack.isEmpty() || incomingStack.isEmpty()) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(filterStack, incomingStack) || ItemStack.isSameItem(filterStack, incomingStack);
    }

    private static int sanitizeStyleColor(int color) {
        return color & 0xFFFFFF;
    }

    private static @Nullable CompoundTag getRootTagView(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return null;
        }
        CompoundTag tag = customData.copyTag();
        return hasCompound(tag, ROOT_TAG) ? tag.getCompoundOrEmpty(ROOT_TAG) : null;
    }

    private static boolean hasCompound(CompoundTag tag, String key) {
        return tag.get(key) instanceof CompoundTag;
    }

    private static boolean hasList(CompoundTag tag, String key) {
        return tag.get(key) instanceof ListTag;
    }

    private static boolean hasByteArray(CompoundTag tag, String key) {
        return tag.getByteArray(key).isPresent();
    }

    private static boolean hasNumeric(CompoundTag tag, String key) {
        return tag.get(key) instanceof NumericTag;
    }

    private static ItemStack readItemValue(CompoundTag tag, String key) {
        return tag.read(key, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    private static void storeItemValue(CompoundTag tag, String key, ItemStack stack) {
        tag.store(key, ItemStack.OPTIONAL_CODEC, stack);
    }

    private static FluidStack readFluidValue(CompoundTag tag, String key) {
        return tag.read(key, FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY);
    }

    private static void storeFluidValue(CompoundTag tag, String key, FluidStack stack) {
        tag.store(key, FluidStack.OPTIONAL_CODEC, stack);
    }

    private static int defaultFrameColor(DeepNullTier tier, boolean fluidOnly) {
        return DEFAULT_STYLE_COLOR;
    }

    private static int defaultGlassColor(DeepNullTier tier, boolean fluidOnly) {
        int tierId = Math.max(0, Math.min(tier.ordinalId(), DEFAULT_DEEPNULL_GLASS_COLORS.length - 1));
        return fluidOnly ? DEFAULT_DAMPNULL_GLASS_COLORS[tierId] : DEFAULT_DEEPNULL_GLASS_COLORS[tierId];
    }

    private int defaultFrameColor() {
        return defaultFrameColor(tier, fluidOnly);
    }

    private int defaultGlassColor() {
        return defaultGlassColor(tier, fluidOnly);
    }

    private boolean filterExplicitlyAllows(ItemStack stack) {
        if (!supportsFiltering() || stack.isEmpty()) {
            return false;
        }
        if (filterMode.usesGhostSlots()) {
            return hasGhostFilterEntries(filterStacks) && passesGhostSlotFilter(filterStacks, filterMode, stack);
        }
        return filterMode.matchesPreset(stack);
    }

    private boolean hasGhostFilterEntries(NonNullList<ItemStack> configuredStacks) {
        for (ItemStack configuredStack : configuredStacks) {
            if (!configuredStack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean stackHasUpgrade(DeepNullUpgradeType type, ItemStack stack) {
        return stack.getItem() instanceof DeepNullUpgradeItem upgradeItem && upgradeItem.type() == type;
    }

    private static DeepNullUpgradeType upgradeType(ItemStack stack) {
        return stack.getItem() instanceof DeepNullUpgradeItem upgradeItem ? upgradeItem.type() : null;
    }

    private void migrateLegacyUpgradeSlots() {
        migrateLegacyUpgradeSlot(3, DeepNullUpgradeType.DEEP_ENERGY);
        migrateLegacyUpgradeSlot(9, DeepNullUpgradeType.OBSIDIAN_GENERATOR);
    }

    private void migrateLegacyUpgradeSlot(int legacySlot, DeepNullUpgradeType type) {
        if (legacySlot < 0 || legacySlot >= upgradeHandler.getSlots()) {
            return;
        }
        ItemStack legacyStack = upgradeHandler.getStackInSlot(legacySlot);
        if (!stackHasUpgrade(type, legacyStack)) {
            return;
        }

        int currentSlot = type.slot();
        ItemStack currentStack = upgradeHandler.getStackInSlot(currentSlot);
        if (currentStack.isEmpty()) {
            upgradeHandler.setStackInSlot(currentSlot, legacyStack);
        }
        upgradeHandler.clearSlotSilently(legacySlot);
    }

    public boolean containsFluidAmountAtLeast(Fluid fluid, int amount) {
        if (!supportsFluidStorage() || fluid == Fluids.EMPTY || amount <= 0) {
            return false;
        }
        long stored = 0L;
        for (FluidStack fluidStack : fluidStacks) {
            if (!fluidStack.isEmpty() && fluidStack.getFluid().isSame(fluid)) {
                stored += fluidStack.getAmount();
                if (stored >= amount) {
                    return true;
                }
            }
        }
        return false;
    }

    private @Nullable SmeltConversion resolveAutoSmelt(ItemStack stack) {
        if (!isAutoSmeltingEnabled() || stack.isEmpty()) {
            return null;
        }
        if (!passesAutoSmeltFilter(stack)) {
            return null;
        }
        if (isBlockedAutoSmeltInput(stack)) {
            return null;
        }

        ItemStack smeltResult = smeltResult(stack);
        if (smeltResult.isEmpty() || !containsSmeltSeed(stack, smeltResult)) {
            return null;
        }
        return new SmeltConversion(smeltResult.copyWithCount(1), smeltResult.getCount());
    }

    private @Nullable CompressionConversion resolveCompression(ItemStack stack) {
        if (stack.isEmpty() || !DeepNullConfig.isCompressionEnabled()) {
            return null;
        }

        if (hasAdvancedCompressionUpgrade()) {
            CompressionConversion advanced = compressionRecipeFor(stack, 3);
            if (advanced != null) {
                return advanced;
            }
        }
        if (hasBasicCompressionUpgrade()) {
            return compressionRecipeFor(stack, 2);
        }
        return null;
    }

    private @Nullable CompressionConversion compressionRecipeFor(ItemStack stack, int gridSize) {
        if (stack.getCount() < gridSize * gridSize) {
            return null;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }

        CraftingInput input = CraftingInput.of(gridSize, gridSize, repeatedCraftingInputs(stack, gridSize * gridSize));
        RecipeHolder<CraftingRecipe> recipe = server.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, server.overworld())
                .orElse(null);
        if (recipe == null) {
            return null;
        }
        if (!recipe.value().getRemainingItems(input).stream().allMatch(ItemStack::isEmpty)) {
            return null;
        }

        HolderLookup.Provider registries = registriesSupplier.get();
        HolderLookup.Provider resultRegistries = registries == null ? server.registryAccess() : registries;
        ItemStack result = recipe.value().assemble(input);
        if (result.isEmpty() || ItemStack.isSameItemSameComponents(result, stack) || !containsCompressionSeed(result)) {
            return null;
        }
        return new CompressionConversion(result.copyWithCount(result.getCount()), gridSize * gridSize);
    }

    private boolean isBlockedAutoSmeltInput(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }

        Identifier itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemPath = itemKey.getPath().toLowerCase(Locale.ROOT);
        if (itemPath.contains("_ore") || itemPath.startsWith("ore_") || itemPath.endsWith("_ore")) {
            return true;
        }

        return Stream.concat(
                        stack.typeHolder().tags().map(TagKey::location),
                        blockItem.getBlock().builtInRegistryHolder().tags().map(TagKey::location)
                )
                .map(Identifier::getPath)
                .map(path -> path.toLowerCase(Locale.ROOT))
                .anyMatch(path -> path.equals("ores")
                        || path.startsWith("ores/")
                        || path.contains("/ores/")
                        || path.equals("ore")
                        || path.startsWith("ore/"));
    }

    private boolean containsSmeltSeed(ItemStack input, ItemStack output) {
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stored = getStackInSlot(slot);
            if (stored.isEmpty()) {
                continue;
            }
            if (ItemStack.isSameItemSameComponents(stored, input) || ItemStack.isSameItemSameComponents(stored, output)) {
                return true;
            }
            if (ItemStack.isSameItem(stored, input) || ItemStack.isSameItem(stored, output)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsCompressionSeed(ItemStack output) {
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stored = getStackInSlot(slot);
            if (stored.isEmpty()) {
                continue;
            }
            if (ItemStack.isSameItemSameComponents(stored, output) || ItemStack.isSameItem(stored, output)) {
                return true;
            }
        }
        return false;
    }

    private boolean canRunStoneworksFor(StoneworksMaterial material, boolean hasWaterSupport) {
        ItemStack output = getStoneworksDisplayStack(material);
        if (!isStoneworksOutputAvailable(material, output, hasWaterSupport) || !canInsertStoneworksOutput(output)) {
            return false;
        }

        if (!isStoneworksMonitoring(material)) {
            return true;
        }

        return countStoredLike(output) < requiredStoneworksItems(material, hasWaterSupport);
    }

    private int runStoneworksFor(StoneworksMaterial material, boolean hasWaterSupport, int maxOperations) {
        int completed = 0;
        for (int operation = 0; operation < maxOperations; operation++) {
            if (!canRunStoneworksFor(material, hasWaterSupport)) {
                break;
            }
            DeepNullInventory simulationInventory = new DeepNullInventory(tier, backingStack.copy(), registriesSupplier, null);
            if (!simulationInventory.tryRunStoneworksChain(material, hasWaterSupport, true)) {
                break;
            }
            if (!tryRunStoneworksChain(material, hasWaterSupport, false)) {
                break;
            }
            completed++;
        }
        return completed;
    }

    private boolean tryRunStoneworksChain(StoneworksMaterial target, boolean hasWaterSupport, boolean simulate) {
        ItemStack finalOutput = getStoneworksDisplayStack(target);
        if (!isStoneworksOutputAvailable(target, finalOutput, hasWaterSupport) || !canInsertStoneworksOutput(finalOutput)) {
            return false;
        }
        if (!insertStoneworksOutput(finalOutput.copyWithCount(1), true).isEmpty()) {
            return false;
        }

        ItemStack transientOutput = ItemStack.EMPTY;
        for (StoneworksMaterial stage : stoneworksStages(target)) {
            ItemStack input = stoneworksInput(stage);
            ItemStack output = getStoneworksDisplayStack(stage);
            if (!isStoneworksOutputAvailable(stage, output, hasWaterSupport)) {
                return false;
            }

            if (matchesStoneworksInput(transientOutput, input)) {
                transientOutput = ItemStack.EMPTY;
            } else if (!consumeMatchingItem(input, 1, simulate)) {
                return false;
            }

            transientOutput = output.copyWithCount(1);
        }
        return !transientOutput.isEmpty() && insertStoneworksOutput(transientOutput, simulate).isEmpty();
    }

    private StoneworksMaterial[] stoneworksStages(StoneworksMaterial target) {
        return switch (target) {
            case DIRT -> new StoneworksMaterial[]{StoneworksMaterial.DIRT};
            case GRAVEL -> new StoneworksMaterial[]{StoneworksMaterial.DIRT, StoneworksMaterial.GRAVEL};
            case SAND -> new StoneworksMaterial[]{StoneworksMaterial.DIRT, StoneworksMaterial.GRAVEL, StoneworksMaterial.SAND};
            case DUST -> new StoneworksMaterial[]{StoneworksMaterial.DIRT, StoneworksMaterial.GRAVEL, StoneworksMaterial.SAND, StoneworksMaterial.DUST};
            case CLAY -> new StoneworksMaterial[]{StoneworksMaterial.DIRT, StoneworksMaterial.CLAY};
            case GLASS -> new StoneworksMaterial[]{StoneworksMaterial.DIRT, StoneworksMaterial.GRAVEL, StoneworksMaterial.SAND, StoneworksMaterial.GLASS};
        };
    }

    private ItemStack stoneworksInput(StoneworksMaterial stage) {
        return switch (stage) {
            case DIRT -> new ItemStack(Items.COBBLESTONE);
            case GRAVEL, CLAY -> new ItemStack(Items.DIRT);
            case SAND -> new ItemStack(Items.GRAVEL);
            case DUST, GLASS -> new ItemStack(Items.SAND);
        };
    }

    private boolean matchesStoneworksInput(ItemStack transientOutput, ItemStack input) {
        if (transientOutput.isEmpty() || input.isEmpty()) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(transientOutput, input) || ItemStack.isSameItem(transientOutput, input);
    }

    private boolean canInsertStoneworksOutput(ItemStack stack) {
        return !insertStoneworksOutput(stack.copyWithCount(1), true).isEmpty() ? false : true;
    }

    private ItemStack insertStoneworksOutput(ItemStack stack, boolean simulate) {
        if (fluidOnly || stack.isEmpty() || (supportsLocking() && locked)) {
            return stack;
        }
        return insertPreparedIntoFirstAvailableSlot(stack, simulate);
    }

    private boolean isStoneworksOutputAvailable(StoneworksMaterial material, ItemStack output, boolean hasWaterSupport) {
        if (output.isEmpty()) {
            return false;
        }
        if (material == StoneworksMaterial.CLAY && !hasWaterSupport) {
            return false;
        }
        if (material == StoneworksMaterial.GLASS && !hasAutoSmeltingUpgrade()) {
            return false;
        }
        return material != StoneworksMaterial.DUST || !output.isEmpty();
    }

    private int requiredStoneworksItems(StoneworksMaterial material, boolean hasWaterSupport) {
        ItemStack output = getStoneworksDisplayStack(material);
        if (!isStoneworksOutputAvailable(material, output, hasWaterSupport)) {
            return 0;
        }

        int required = isStoneworksMonitoring(material) ? stoneworksTargetStacks : 0;
        return switch (material) {
            case DIRT -> required
                    + requiredStoneworksOperations(StoneworksMaterial.CLAY, hasWaterSupport)
                    + requiredStoneworksOperations(StoneworksMaterial.GRAVEL, hasWaterSupport);
            case GRAVEL -> required
                    + requiredStoneworksOperations(StoneworksMaterial.SAND, hasWaterSupport);
            case SAND -> required
                    + requiredStoneworksOperations(StoneworksMaterial.DUST, hasWaterSupport)
                    + requiredStoneworksOperations(StoneworksMaterial.GLASS, hasWaterSupport);
            case DUST, CLAY, GLASS -> required;
        };
    }

    private int requiredStoneworksOperations(StoneworksMaterial material, boolean hasWaterSupport) {
        if (!isStoneworksMonitoring(material)) {
            return 0;
        }
        return requiredStoneworksItems(material, hasWaterSupport);
    }

    private int countStoredLike(ItemStack sample) {
        if (sample.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stored = getStackInSlot(slot);
            if (stored.isEmpty()) {
                continue;
            }
            if (ItemStack.isSameItemSameComponents(stored, sample) || ItemStack.isSameItem(stored, sample)) {
                total += stored.getCount();
            }
        }
        return total;
    }

    private int countExtractableLike(ItemStack sample) {
        if (sample.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stored = getStackInSlot(slot);
            if (stored.isEmpty()) {
                continue;
            }
            if (ItemStack.isSameItemSameComponents(stored, sample) || ItemStack.isSameItem(stored, sample)) {
                total += getExtractableAmount(slot);
            }
        }
        return total;
    }

    private boolean consumeMatchingItem(ItemStack sample, int amount, boolean simulate) {
        int remaining = amount;
        for (int slot = 0; slot < getSlots() && remaining > 0; slot++) {
            ItemStack stored = getStackInSlot(slot);
            if (stored.isEmpty()) {
                continue;
            }
            if (!ItemStack.isSameItemSameComponents(stored, sample) && !ItemStack.isSameItem(stored, sample)) {
                continue;
            }
            ItemStack extracted = extractItem(slot, remaining, simulate);
            remaining -= extracted.getCount();
        }
        return remaining <= 0;
    }

    private ItemStack resolveGlassOutput() {
        ItemStack smeltResult = smeltResult(new ItemStack(Items.SAND));
        if (!smeltResult.isEmpty()) {
            return smeltResult;
        }
        return new ItemStack(Items.GLASS);
    }

    private ItemStack resolveDustOutput() {
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stored = getStackInSlot(slot);
            if (isDustCandidate(stored)) {
                return stored.copyWithCount(1);
            }
        }
        for (ItemStack filterStack : filterStacks) {
            if (isDustCandidate(filterStack)) {
                return filterStack.copyWithCount(1);
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean isDustCandidate(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = itemId.getPath().toLowerCase(Locale.ROOT);
        if (path.equals("dust") || path.endsWith("_dust") || path.startsWith("dust_")) {
            return true;
        }

        return stack.typeHolder().tags()
                .map(TagKey::location)
                .map(Identifier::getPath)
                .map(tagPath -> tagPath.toLowerCase(Locale.ROOT))
                .anyMatch(tagPath -> tagPath.equals("dusts")
                        || tagPath.startsWith("dusts/")
                        || tagPath.contains("/dusts/")
                        || tagPath.equals("dust")
                        || tagPath.startsWith("dust/"));
    }

    private ItemStack smeltResult(ItemStack stack) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return ItemStack.EMPTY;
        }

        HolderLookup.Provider registries = registriesSupplier.get();
        HolderLookup.Provider resultRegistries = registries == null ? server.registryAccess() : registries;
        RecipeHolder<SmeltingRecipe> recipe = server.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack.copyWithCount(1)), server.overworld())
                .orElse(null);
        if (recipe == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = recipe.value().assemble(new SingleRecipeInput(stack.copyWithCount(1)));
        return result.isEmpty() ? ItemStack.EMPTY : result.copy();
    }

    private void clearFilterStacks() {
        for (int slot = 0; slot < filterStacks.size(); slot++) {
            filterStacks.set(slot, ItemStack.EMPTY);
        }
    }

    private void clearAutoSmeltFilterStacks() {
        for (int slot = 0; slot < autoSmeltFilterStacks.size(); slot++) {
            autoSmeltFilterStacks.set(slot, ItemStack.EMPTY);
        }
    }

    private DeepNullFilterMode normalizeAutoSmeltFilterMode(DeepNullFilterMode mode) {
        return mode == DeepNullFilterMode.WHITELIST ? DeepNullFilterMode.WHITELIST : DeepNullFilterMode.BLACKLIST;
    }

    private void clearFluidStacks() {
        for (int slot = 0; slot < fluidStacks.size(); slot++) {
            fluidStacks.set(slot, FluidStack.EMPTY);
        }
    }

    private void clearChemicalStacks() {
        for (int slot = 0; slot < chemicalStacks.size(); slot++) {
            chemicalStacks.set(slot, StoredChemical.EMPTY);
        }
    }

    private FluidStack displayedFluid(FluidStack storedFluid) {
        if (storedFluid.isEmpty()) {
            return FluidStack.EMPTY;
        }
        if (!tier.creative()) {
            return storedFluid.copy();
        }
        return storedFluid.copyWithAmount(CREATIVE_DISPLAY_FLUID);
    }

    private StoredChemical displayedChemical(StoredChemical storedChemical) {
        if (storedChemical.isEmpty() || !supportsChemicalStorage()) {
            return StoredChemical.EMPTY;
        }
        if (!tier.creative()) {
            return storedChemical.copy();
        }
        return storedChemical.copyWithAmount(CREATIVE_DISPLAY_FLUID);
    }

    private boolean isTankEmpty(int slot) {
        return fluidStacks.get(slot).isEmpty() && chemicalStacks.get(slot).isEmpty();
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
        if (!supportsFluidStorage() || getFluidSlotCount() == 0) {
            return -1;
        }
        List<Integer> selectable = new ArrayList<>();
        for (int slot = 0; slot < fluidStacks.size(); slot++) {
            if (!fluidStacks.get(slot).isEmpty() || !chemicalStacks.get(slot).isEmpty()) {
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

    private static void readIntModes(int[] storedModes, int[] targetModes) {
        Arrays.fill(targetModes, 0);
        for (int i = 0; i < Math.min(storedModes.length, targetModes.length); i++) {
            targetModes[i] = storedModes[i];
        }
    }

    private static void readItemList(HolderLookup.Provider registries, Tag storedList, NonNullList<ItemStack> targetStacks) {
        for (int i = 0; i < targetStacks.size(); i++) {
            targetStacks.set(i, ItemStack.EMPTY);
        }
        if (!(storedList instanceof ListTag listTag)) {
            return;
        }
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag entry = listTag.getCompoundOrEmpty(i);
            int slot = entry.getIntOr(SLOT_TAG, -1);
            if (slot < 0 || slot >= targetStacks.size()) {
                continue;
            }
            try {
                targetStacks.set(slot, readItemValue(entry, STACK_TAG));
            } catch (RuntimeException exception) {
                DeepNullReforged.LOGGER.warn("Skipping unreadable DeepNull item entry in slot {}", slot, exception);
            }
        }
    }

    private static ListTag writeItemList(HolderLookup.Provider registries, NonNullList<ItemStack> sourceStacks) {
        ListTag list = new ListTag();
        for (int slot = 0; slot < sourceStacks.size(); slot++) {
            ItemStack stack = sourceStacks.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putInt(SLOT_TAG, slot);
            storeItemValue(entry, STACK_TAG, stack);
            list.add(entry);
        }
        return list;
    }

    private static void readStoredItemList(HolderLookup.Provider registries, Tag storedList, NonNullList<ItemStack> targetStacks) {
        for (int i = 0; i < targetStacks.size(); i++) {
            targetStacks.set(i, ItemStack.EMPTY);
        }
        if (!(storedList instanceof ListTag listTag)) {
            return;
        }
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag entry = listTag.getCompoundOrEmpty(i);
            int slot = entry.getIntOr(SLOT_TAG, -1);
            if (slot < 0 || slot >= targetStacks.size()) {
                continue;
            }

            ItemStack stack;
            try {
                stack = readItemValue(entry, STACK_TAG);
            } catch (RuntimeException exception) {
                DeepNullReforged.LOGGER.warn("Skipping unreadable DeepNull stored item entry in slot {}", slot, exception);
                continue;
            }
            if (stack.isEmpty()) {
                continue;
            }

            int storedCount = hasNumeric(entry, COUNT_TAG) ? entry.getIntOr(COUNT_TAG, stack.getCount()) : stack.getCount();
            if (storedCount <= 0) {
                continue;
            }

            stack.setCount(storedCount);
            targetStacks.set(slot, stack);
        }
    }

    private static ListTag writeStoredItemList(HolderLookup.Provider registries, NonNullList<ItemStack> sourceStacks) {
        ListTag list = new ListTag();
        for (int slot = 0; slot < sourceStacks.size(); slot++) {
            ItemStack stack = sourceStacks.get(slot);
            if (stack.isEmpty()) {
                continue;
            }

            CompoundTag entry = new CompoundTag();
            ItemStack storedStack = stack.copyWithCount(1);
            entry.putInt(SLOT_TAG, slot);
            entry.putInt(COUNT_TAG, stack.getCount());
            storeItemValue(entry, STACK_TAG, storedStack);
            list.add(entry);
        }
        return list;
    }

    private static void readFluidList(HolderLookup.Provider registries, Tag storedList, NonNullList<FluidStack> targetStacks) {
        for (int i = 0; i < targetStacks.size(); i++) {
            targetStacks.set(i, FluidStack.EMPTY);
        }
        if (!(storedList instanceof ListTag listTag)) {
            return;
        }
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag entry = listTag.getCompoundOrEmpty(i);
            int slot = entry.getIntOr(SLOT_TAG, -1);
            if (slot < 0 || slot >= targetStacks.size()) {
                continue;
            }
            try {
                targetStacks.set(slot, readFluidValue(entry, STACK_TAG));
            } catch (RuntimeException exception) {
                DeepNullReforged.LOGGER.warn("Skipping unreadable DeepNull fluid entry in slot {}", slot, exception);
            }
        }
    }

    private static ListTag writeFluidList(HolderLookup.Provider registries, NonNullList<FluidStack> sourceStacks) {
        ListTag list = new ListTag();
        for (int slot = 0; slot < sourceStacks.size(); slot++) {
            FluidStack stack = sourceStacks.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putInt(SLOT_TAG, slot);
            storeFluidValue(entry, STACK_TAG, stack);
            list.add(entry);
        }
        return list;
    }

    private static void readChemicalList(Tag storedList, NonNullList<StoredChemical> targetStacks) {
        for (int i = 0; i < targetStacks.size(); i++) {
            targetStacks.set(i, StoredChemical.EMPTY);
        }
        if (!(storedList instanceof ListTag listTag)) {
            return;
        }
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag entry = listTag.getCompoundOrEmpty(i);
            int slot = entry.getIntOr(SLOT_TAG, -1);
            if (slot < 0 || slot >= targetStacks.size()) {
                continue;
            }
            try {
                targetStacks.set(slot, StoredChemical.load(entry.getCompoundOrEmpty(STACK_TAG)));
            } catch (RuntimeException exception) {
                DeepNullReforged.LOGGER.warn("Skipping unreadable DeepNull chemical entry in slot {}", slot, exception);
            }
        }
    }

    private static ListTag writeChemicalList(NonNullList<StoredChemical> sourceStacks) {
        ListTag list = new ListTag();
        for (int slot = 0; slot < sourceStacks.size(); slot++) {
            StoredChemical stack = sourceStacks.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putInt(SLOT_TAG, slot);
            entry.put(STACK_TAG, stack.save());
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

    private static List<ItemStack> repeatedCraftingInputs(ItemStack stack, int count) {
        List<ItemStack> inputs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            inputs.add(stack.copyWithCount(1));
        }
        return inputs;
    }

    private final class UpgradeItemHandler extends ItemStackHandler {
        private UpgradeItemHandler() {
            super(DeepNullUpgradeType.values().length);
        }

        private void ensureSlotCount() {
            int expectedSize = DeepNullUpgradeType.values().length;
            if (stacks.size() == expectedSize) {
                return;
            }

            NonNullList<ItemStack> resized = NonNullList.withSize(expectedSize, ItemStack.EMPTY);
            for (int slot = 0; slot < Math.min(stacks.size(), expectedSize); slot++) {
                resized.set(slot, stacks.get(slot));
            }
            stacks = resized;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            DeepNullUpgradeType type = upgradeType(stack);
            if (type == null || type.slot() != slot || !supportsUpgrade(type)) {
                return false;
            }
            if (type == DeepNullUpgradeType.ENDER) {
                return EnderUpgradeItem.matchesNull(stack, tier, fluidOnly);
            }
            return true;
        }

        private void clearSlotSilently(int slot) {
            ensureSlotCount();
            stacks.set(slot, ItemStack.EMPTY);
        }

        private NonNullList<ItemStack> rawStacks() {
            ensureSlotCount();
            return stacks;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            save();
        }
    }

    private int initialFluidSlotCount() {
        return fluidOnly ? tier.dampNullTankCount() : 0;
    }

    private record CompressionConversion(ItemStack outputSample, int inputPerCraft) {
        private int outputPerCraft() {
            return outputSample.getCount();
        }

        private ItemStack outputForInputs(int inputCount) {
            int crafts = inputCount / inputPerCraft;
            return crafts <= 0 ? ItemStack.EMPTY : outputSample.copyWithCount(crafts * outputPerCraft());
        }

        private int inputCountForProduced(int outputCount) {
            if (outputCount <= 0) {
                return 0;
            }
            return (outputCount / outputPerCraft()) * inputPerCraft;
        }
    }

    private record SmeltConversion(ItemStack outputSample, int outputPerInput) {
        private ItemStack outputForInputs(int inputCount) {
            long total = (long) inputCount * outputPerInput;
            return outputSample.copyWithCount((int) Math.min(Integer.MAX_VALUE, total));
        }
    }

    public record StyleRenderData(boolean hasColorOverrides, StyleGlassVariant styleVariant, int frameColor, int glassColor) {
        public boolean hasCustomStyle() {
            return hasColorOverrides || styleVariant != StyleGlassVariant.DEFAULT;
        }
    }

    private record LinkedDockSource(DeepNullDockBlockEntity dock) {
    }
}
