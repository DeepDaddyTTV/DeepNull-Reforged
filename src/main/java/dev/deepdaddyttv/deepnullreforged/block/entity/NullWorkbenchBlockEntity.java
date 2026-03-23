package dev.deepdaddyttv.deepnullreforged.block.entity;

import dev.deepdaddyttv.deepnullreforged.block.NullWorkbenchPart;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.SynchronizerItem;
import dev.deepdaddyttv.deepnullreforged.recipe.NullWorkbenchRecipes;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class NullWorkbenchBlockEntity extends BlockEntity {
    public static final int INPUT_SLOT_START = 0;
    public static final int INPUT_SLOT_COUNT = 4;
    public static final int OUTPUT_SLOT = 4;
    public static final int NULL_SLOT = 5;
    public static final int SYNCHRONIZER_SLOT = 6;
    private static final int CRAFT_DURATION = 72;
    private static final int SYNC_DURATION = 40;
    private static final String ITEMS_TAG = "Items";
    private static final String CRAFT_PROGRESS_TAG = "CraftProgress";
    private static final String CRAFT_DURATION_TAG = "CraftDuration";
    private static final String SYNC_PROGRESS_TAG = "SyncProgress";
    private static final String SYNC_ACTION_TAG = "SyncAction";

    private final ItemStackHandler items = new ItemStackHandler(7) {
        @Override
        protected void onContentsChanged(int slot) {
            craftProgress = 0;
            syncProgress = 0;
            syncAction = SyncAction.NONE;
            setChangedAndSync();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot >= INPUT_SLOT_START && slot < INPUT_SLOT_START + INPUT_SLOT_COUNT) {
                return true;
            }
            if (slot == OUTPUT_SLOT) {
                return false;
            }
            if (slot == NULL_SLOT) {
                return stack.getItem() instanceof DeepNullItem;
            }
            if (slot == SYNCHRONIZER_SLOT) {
                return stack.is(ModItems.SYNCHRONIZER.get());
            }
            return false;
        }
    };

    private int craftProgress;
    private int craftDuration = CRAFT_DURATION;
    private int syncProgress;
    private SyncAction syncAction = SyncAction.NONE;
    private final IItemHandler automationHandler = new AutomationItemHandler();

    public NullWorkbenchBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.NULL_WORKBENCH.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NullWorkbenchBlockEntity workbench) {
        if (state.getValue(dev.deepdaddyttv.deepnullreforged.block.NullWorkbenchBlock.PART) != NullWorkbenchPart.MAIN) {
            return;
        }
        workbench.tickCrafting();
        workbench.tickSync();
    }

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public IItemHandler getAutomationHandler() {
        return automationHandler;
    }

    public int getCraftProgress() {
        return craftProgress;
    }

    public int getCraftDuration() {
        return craftDuration;
    }

    public int getSyncProgress() {
        return syncProgress;
    }

    public int getSyncDuration() {
        return SYNC_DURATION;
    }

    public boolean isSyncing() {
        return syncAction != SyncAction.NONE;
    }

    public Component getSyncStatus() {
        return switch (syncAction) {
            case BACKUP -> Component.translatable("container.deepnullreforged.null_workbench.backup");
            case RESTORE -> Component.translatable("container.deepnullreforged.null_workbench.restore");
            case NONE -> Component.empty();
        };
    }

    public ItemStack getStackInSlot(int slot) {
        return items.getStackInSlot(slot);
    }

    public boolean startBackup() {
        if (isSyncing() || !canBackup()) {
            return false;
        }
        syncAction = SyncAction.BACKUP;
        syncProgress = 0;
        setChangedAndSync();
        return true;
    }

    public boolean startRestore() {
        if (isSyncing() || !canRestore()) {
            return false;
        }
        syncAction = SyncAction.RESTORE;
        syncProgress = 0;
        setChangedAndSync();
        return true;
    }

    public boolean canBackup() {
        return createNullInventory() != null && !items.getStackInSlot(SYNCHRONIZER_SLOT).isEmpty();
    }

    public boolean canRestore() {
        DeepNullInventory inventory = createNullInventory();
        ItemStack synchronizer = items.getStackInSlot(SYNCHRONIZER_SLOT);
        return inventory != null
                && !synchronizer.isEmpty()
                && SynchronizerItem.hasConfiguration(synchronizer)
                && SynchronizerItem.matchesNullType(synchronizer, inventory.isFluidOnly());
    }

    public boolean applyStyleColors(int frameColor, int glassColor) {
        ItemStack input = items.getStackInSlot(NULL_SLOT);
        if (!(input.getItem() instanceof DeepNullItem deepNullItem) || level == null || !items.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            return false;
        }
        ItemStack styled = input.copy();
        DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), styled, level.registryAccess(), null);
        inventory.setStyleColors(frameColor, glassColor);
        items.setStackInSlot(NULL_SLOT, ItemStack.EMPTY);
        items.setStackInSlot(OUTPUT_SLOT, styled);
        setChangedAndSync();
        return true;
    }

    public boolean resetStyleColors() {
        ItemStack input = items.getStackInSlot(NULL_SLOT);
        if (!(input.getItem() instanceof DeepNullItem deepNullItem) || level == null || !items.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            return false;
        }
        ItemStack styled = input.copy();
        DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), styled, level.registryAccess(), null);
        inventory.resetStyleColors();
        items.setStackInSlot(NULL_SLOT, ItemStack.EMPTY);
        items.setStackInSlot(OUTPUT_SLOT, styled);
        setChangedAndSync();
        return true;
    }

    public @Nullable DeepNullInventory createNullInventory() {
        ItemStack stack = items.getStackInSlot(NULL_SLOT);
        if (!(stack.getItem() instanceof DeepNullItem deepNullItem) || level == null) {
            return null;
        }
        return new DeepNullInventory(deepNullItem.tier(), stack, level.registryAccess(), () -> {
            items.setStackInSlot(NULL_SLOT, stack);
            setChangedAndSync();
        });
    }

    public void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(ITEMS_TAG, items.serializeNBT(registries));
        tag.putInt(CRAFT_PROGRESS_TAG, craftProgress);
        tag.putInt(CRAFT_DURATION_TAG, craftDuration);
        tag.putInt(SYNC_PROGRESS_TAG, syncProgress);
        tag.putInt(SYNC_ACTION_TAG, syncAction.ordinal());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(ITEMS_TAG, Tag.TAG_COMPOUND)) {
            items.deserializeNBT(registries, tag.getCompound(ITEMS_TAG));
        }
        craftProgress = tag.getInt(CRAFT_PROGRESS_TAG);
        craftDuration = tag.contains(CRAFT_DURATION_TAG) ? tag.getInt(CRAFT_DURATION_TAG) : CRAFT_DURATION;
        syncProgress = tag.getInt(SYNC_PROGRESS_TAG);
        syncAction = SyncAction.byId(tag.getInt(SYNC_ACTION_TAG));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void tickCrafting() {
        if (level == null || level.isClientSide) {
            return;
        }

        NullWorkbenchRecipes.CraftRecipe recipe = findCraftRecipe();
        if (recipe == null || !canOutput(recipe.result())) {
            if (craftProgress != 0) {
                craftProgress = 0;
                setChangedAndSync();
            }
            return;
        }

        craftDuration = CRAFT_DURATION;
        craftProgress++;
        if (craftProgress < craftDuration) {
            setChanged();
            return;
        }

        craftProgress = 0;
        consumeIngredients(recipe);
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            items.setStackInSlot(OUTPUT_SLOT, recipe.result().copy());
        } else {
            output.grow(recipe.result().getCount());
            items.setStackInSlot(OUTPUT_SLOT, output);
        }
        setChangedAndSync();
    }

    private void tickSync() {
        if (level == null || level.isClientSide || syncAction == SyncAction.NONE) {
            return;
        }

        if ((syncAction == SyncAction.BACKUP && !canBackup()) || (syncAction == SyncAction.RESTORE && !canRestore())) {
            syncAction = SyncAction.NONE;
            syncProgress = 0;
            setChangedAndSync();
            return;
        }

        syncProgress++;
        if (syncProgress < SYNC_DURATION) {
            setChanged();
            return;
        }

        syncProgress = 0;
        switch (syncAction) {
            case BACKUP -> performBackup();
            case RESTORE -> performRestore();
            case NONE -> {
            }
        }
        syncAction = SyncAction.NONE;
        setChangedAndSync();
    }

    private void performBackup() {
        DeepNullInventory inventory = createNullInventory();
        ItemStack synchronizer = items.getStackInSlot(SYNCHRONIZER_SLOT);
        if (inventory == null || synchronizer.isEmpty()) {
            return;
        }
        SynchronizerItem.storeConfiguration(synchronizer, inventory.exportConfiguration(), inventory.tier(), inventory.isFluidOnly());
        items.setStackInSlot(SYNCHRONIZER_SLOT, synchronizer);
    }

    private void performRestore() {
        DeepNullInventory inventory = createNullInventory();
        ItemStack synchronizer = items.getStackInSlot(SYNCHRONIZER_SLOT);
        if (inventory == null || synchronizer.isEmpty()) {
            return;
        }
        CompoundTag configuration = SynchronizerItem.getConfiguration(synchronizer);
        if (configuration == null) {
            return;
        }
        inventory.importConfiguration(configuration);
        items.setStackInSlot(NULL_SLOT, inventory.backingStack());
    }

    private boolean canOutput(ItemStack result) {
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void consumeIngredients(NullWorkbenchRecipes.CraftRecipe recipe) {
        boolean[] consumed = new boolean[INPUT_SLOT_COUNT];
        for (NullWorkbenchRecipes.IngredientCount ingredient : recipe.ingredients()) {
            for (int slot = INPUT_SLOT_START; slot < INPUT_SLOT_START + INPUT_SLOT_COUNT; slot++) {
                if (consumed[slot - INPUT_SLOT_START]) {
                    continue;
                }
                ItemStack stack = items.getStackInSlot(slot);
                if (ingredient.matches(stack)) {
                    stack.shrink(ingredient.stack().getCount());
                    items.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
                    consumed[slot - INPUT_SLOT_START] = true;
                    break;
                }
            }
        }
    }

    private @Nullable NullWorkbenchRecipes.CraftRecipe findCraftRecipe() {
        for (NullWorkbenchRecipes.CraftRecipe recipe : NullWorkbenchRecipes.all()) {
            if (matches(recipe)) {
                return recipe;
            }
        }
        return null;
    }

    private boolean matches(NullWorkbenchRecipes.CraftRecipe recipe) {
        boolean[] used = new boolean[INPUT_SLOT_COUNT];
        for (NullWorkbenchRecipes.IngredientCount ingredient : recipe.ingredients()) {
            boolean matched = false;
            for (int slot = INPUT_SLOT_START; slot < INPUT_SLOT_START + INPUT_SLOT_COUNT; slot++) {
                if (used[slot - INPUT_SLOT_START]) {
                    continue;
                }
                if (ingredient.matches(items.getStackInSlot(slot))) {
                    used[slot - INPUT_SLOT_START] = true;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        for (int slot = INPUT_SLOT_START; slot < INPUT_SLOT_START + INPUT_SLOT_COUNT; slot++) {
            if (!used[slot - INPUT_SLOT_START] && !items.getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public enum SyncAction {
        NONE,
        BACKUP,
        RESTORE;

        public static SyncAction byId(int id) {
            SyncAction[] values = values();
            return id >= 0 && id < values.length ? values[id] : NONE;
        }
    }

    private final class AutomationItemHandler implements IItemHandlerModifiable {
        @Override
        public int getSlots() {
            return INPUT_SLOT_COUNT + 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < getSlots() ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot < INPUT_SLOT_START || slot >= INPUT_SLOT_START + INPUT_SLOT_COUNT) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < INPUT_SLOT_START || slot > OUTPUT_SLOT) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot <= OUTPUT_SLOT ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= INPUT_SLOT_START
                    && slot < INPUT_SLOT_START + INPUT_SLOT_COUNT
                    && items.isItemValid(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (slot >= 0 && slot <= OUTPUT_SLOT) {
                items.setStackInSlot(slot, stack);
            }
        }
    }
}
