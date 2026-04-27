package dev.deepdaddyttv.deepnullreforged.block.entity;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModCapabilities;
import dev.deepdaddyttv.deepnullreforged.util.NbtCodecs;
import net.minecraft.core.Direction;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import dev.deepdaddyttv.deepnullreforged.compat.items.IItemHandler;
import dev.deepdaddyttv.deepnullreforged.compat.items.IItemHandlerModifiable;
import dev.deepdaddyttv.deepnullreforged.compat.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class DeepNullDockBlockEntity extends BlockEntity {
    private static final String STORED_DANK_TAG = "StoredDeepNull";
    private static final String GENERATOR_BUFFER_TAG = "GeneratorBuffer";
    private static final int EMPTY_DOCK_SLOT = 0;
    private static final int GENERATOR_BUFFER_SLOT = 0;

    private ItemStack storedDeepNull = ItemStack.EMPTY;
    private ItemStack generatorBuffer = ItemStack.EMPTY;
    private final IItemHandler automationHandler = new DockAutomationHandler(this);

    public DeepNullDockBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DEEP_NULL_DOCK.get(), pos, blockState);
    }

    public boolean hasStoredDeepNull() {
        return !storedDeepNull.isEmpty() && storedDeepNull.getItem() instanceof DeepNullItem;
    }

    public ItemStack getStoredDeepNull() {
        return storedDeepNull;
    }

    public void setStoredDeepNull(ItemStack stack) {
        storedDeepNull = stack.copyWithCount(1);
        generatorBuffer = ItemStack.EMPTY;
        setChangedAndSync(true);
    }

    public void setStoredDeepNullClient(ItemStack stack) {
        storedDeepNull = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        Level level = getLevel();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    public boolean canAcceptDeepNull(ItemStack stack) {
        return !hasStoredDeepNull() && !stack.isEmpty() && stack.getItem() instanceof DeepNullItem;
    }

    public ItemStack removeStoredDeepNull() {
        ItemStack result = storedDeepNull.copy();
        storedDeepNull = ItemStack.EMPTY;
        generatorBuffer = ItemStack.EMPTY;
        setChangedAndSync(true);
        return result;
    }

    public DeepNullTier getTier() {
        if (storedDeepNull.getItem() instanceof DeepNullItem deepNullItem) {
            return deepNullItem.tier();
        }
        return DeepNullTier.REDSTONE;
    }

    public @Nullable DeepNullInventory createInventory() {
        if (!hasStoredDeepNull() || level == null) {
            return null;
        }
        return new DeepNullInventory(getTier(), storedDeepNull, level.registryAccess(), () -> setChangedAndSync(false));
    }

    public IItemHandler getAutomationHandler(@Nullable Direction side) {
        return automationHandler;
    }

    public boolean exposesGeneratorBuffer() {
        DeepNullInventory inventory = createInventory();
        return inventory != null && inventory.isFluidOnly() && hasGeneratorUpgrade(inventory);
    }

    public ItemStack getGeneratorBuffer() {
        return generatorBuffer;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DeepNullDockBlockEntity dock) {
        if (!dock.hasStoredDeepNull()) {
            return;
        }

        DeepNullInventory inventory = dock.createInventory();
        if (inventory == null) {
            return;
        }

        if (!inventory.isFluidOnly()) {
            if (!dock.generatorBuffer.isEmpty()) {
                dock.generatorBuffer = ItemStack.EMPTY;
                dock.setChangedAndSync(false);
            }
            if (inventory.hasStoneworksUpgrade() && level.getGameTime() % 20L == 0L) {
                inventory.runStoneworksCycle(false);
            }
            return;
        }

        if (!hasGeneratorUpgrade(inventory)) {
            if (!dock.generatorBuffer.isEmpty()) {
                dock.generatorBuffer = ItemStack.EMPTY;
                dock.setChangedAndSync(false);
            }
            return;
        }

        dock.pushGeneratorBuffer(level, pos);
        if (inventory.hasStoneGeneratorUpgrade()) {
            if (!inventory.hasStoneGenerationRequirements() || level.getGameTime() % 20L != 0L) {
                return;
            }
            dock.generateStone(inventory);
            dock.pushGeneratorBuffer(level, pos);
            return;
        }

        if (!inventory.hasObsidianGenerationRequirements() || level.getGameTime() % 100L != 0L) {
            return;
        }

        dock.generateObsidian(inventory);
        dock.pushGeneratorBuffer(level, pos);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (hasStoredDeepNull()) {
            output.store(STORED_DANK_TAG, ItemStack.OPTIONAL_CODEC, storedDeepNull);
        }
        if (!generatorBuffer.isEmpty()) {
            output.store(GENERATOR_BUFFER_TAG, ItemStack.OPTIONAL_CODEC, generatorBuffer);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedDeepNull = input.read(STORED_DANK_TAG, ItemStack.OPTIONAL_CODEC)
                .orElse(ItemStack.EMPTY);
        generatorBuffer = input.read(GENERATOR_BUFFER_TAG, ItemStack.OPTIONAL_CODEC)
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && hasStoredDeepNull()) {
            Block.popResource(level, pos, storedDeepNull.copy());
        }
    }

    private void setChangedAndSync(boolean invalidateCapabilities) {
        setChanged();
        Level level = getLevel();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    public void markStoredDeepNullChanged() {
        setChangedAndSync(false);
    }

    private void generateStone(DeepNullInventory inventory) {
        if (!inventory.hasStoneGeneratorUpgrade()) {
            return;
        }

        ItemStack generated = inventory.getStoneGeneratorOutput(inventory.getStoneGenerationRate());
        if (generated.isEmpty()) {
            return;
        }

        ItemStack remainder = insertIntoGeneratorBuffer(generated, true);
        if (remainder.getCount() == generated.getCount()) {
            return;
        }

        insertIntoGeneratorBuffer(generated, false);
    }

    private void generateObsidian(DeepNullInventory inventory) {
        if (!inventory.hasObsidianGeneratorUpgrade()) {
            return;
        }

        ItemStack generated = inventory.getObsidianGeneratorOutput();
        if (generated.isEmpty()) {
            return;
        }

        ItemStack remainder = insertIntoGeneratorBuffer(generated, true);
        if (remainder.getCount() == generated.getCount()) {
            return;
        }
        if (!inventory.consumeObsidianGeneratorInputs()) {
            return;
        }

        insertIntoGeneratorBuffer(generated, false);
    }

    private ItemStack insertIntoGeneratorBuffer(ItemStack stack, boolean simulate) {
        if (!exposesGeneratorBuffer() || stack.isEmpty()) {
            return stack;
        }

        int bufferSize = DeepNullConfig.getDockGeneratorBufferSize();
        if (generatorBuffer.isEmpty()) {
            int inserted = Math.min(bufferSize, stack.getCount());
            if (!simulate) {
                generatorBuffer = stack.copyWithCount(inserted);
                setChangedAndSync(false);
            }
            return remainder(stack, inserted);
        }

        if (!ItemStack.isSameItemSameComponents(generatorBuffer, stack)) {
            return stack;
        }

        int space = bufferSize - generatorBuffer.getCount();
        if (space <= 0) {
            return stack;
        }

        int inserted = Math.min(space, stack.getCount());
        if (!simulate) {
            generatorBuffer.grow(inserted);
            setChangedAndSync(false);
        }
        return remainder(stack, inserted);
    }

    private void pushGeneratorBuffer(Level level, BlockPos pos) {
        if (!exposesGeneratorBuffer() || generatorBuffer.isEmpty()) {
            return;
        }

        ItemStack remaining = generatorBuffer.copy();
        for (Direction direction : Direction.values()) {
            IItemHandler target = ModCapabilities.getBlockItemHandler(level, pos.relative(direction), direction.getOpposite());
            if (target == null) {
                target = ModCapabilities.getBlockItemHandler(level, pos.relative(direction), null);
            }
            if (target == null) {
                continue;
            }
            remaining = ItemHandlerHelper.insertItem(target, remaining, false);
            if (remaining.isEmpty()) {
                break;
            }
        }

        if (remaining.getCount() != generatorBuffer.getCount()) {
            generatorBuffer = remaining;
            setChangedAndSync(false);
        }
    }

    private static ItemStack remainder(ItemStack stack, int extracted) {
        if (stack.isEmpty() || extracted <= 0) {
            return stack;
        }
        if (extracted >= stack.getCount()) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(stack.getCount() - extracted);
    }

    private static boolean hasGeneratorUpgrade(DeepNullInventory inventory) {
        return inventory.hasStoneGeneratorUpgrade() || inventory.hasObsidianGeneratorUpgrade();
    }

    private static final class DockAutomationHandler implements IItemHandlerModifiable {
        private final DeepNullDockBlockEntity dock;

        private DockAutomationHandler(DeepNullDockBlockEntity dock) {
            this.dock = dock;
        }

        @Override
        public int getSlots() {
            DeepNullInventory inventory = dock.createInventory();
            if (inventory == null) {
                return 1;
            }
            if (dock.exposesGeneratorBuffer()) {
                return 1;
            }
            return inventory.isFluidOnly() ? 0 : inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            DeepNullInventory inventory = dock.createInventory();
            if (inventory == null) {
                return slot == EMPTY_DOCK_SLOT ? ItemStack.EMPTY : ItemStack.EMPTY;
            }
            if (dock.exposesGeneratorBuffer()) {
                return slot == GENERATOR_BUFFER_SLOT ? dock.generatorBuffer : ItemStack.EMPTY;
            }
            if (inventory.isFluidOnly()) {
                return ItemStack.EMPTY;
            }
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            DeepNullInventory inventory = dock.createInventory();
            if (inventory == null) {
                if (slot != EMPTY_DOCK_SLOT || !dock.canAcceptDeepNull(stack)) {
                    return stack;
                }
                if (!simulate) {
                    dock.setStoredDeepNull(stack);
                }
                return remainder(stack, 1);
            }
            if (dock.exposesGeneratorBuffer()) {
                return stack;
            }
            if (inventory.isFluidOnly()) {
                return stack;
            }
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            DeepNullInventory inventory = dock.createInventory();
            if (inventory == null) {
                return ItemStack.EMPTY;
            }
            if (dock.exposesGeneratorBuffer()) {
                if (slot != GENERATOR_BUFFER_SLOT || dock.generatorBuffer.isEmpty() || amount <= 0) {
                    return ItemStack.EMPTY;
                }
                int extracted = Math.min(amount, dock.generatorBuffer.getCount());
                ItemStack result = dock.generatorBuffer.copyWithCount(extracted);
                if (!simulate) {
                    dock.generatorBuffer.shrink(extracted);
                    if (dock.generatorBuffer.isEmpty()) {
                        dock.generatorBuffer = ItemStack.EMPTY;
                    }
                    dock.setChangedAndSync(false);
                }
                return result;
            }
            return inventory.isFluidOnly() ? ItemStack.EMPTY : inventory.extractItemForDockAutomation(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            DeepNullInventory inventory = dock.createInventory();
            if (inventory == null) {
                return slot == EMPTY_DOCK_SLOT ? 1 : 0;
            }
            if (dock.exposesGeneratorBuffer()) {
                return slot == GENERATOR_BUFFER_SLOT ? DeepNullConfig.getDockGeneratorBufferSize() : 0;
            }
            return inventory.isFluidOnly() ? 0 : inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            DeepNullInventory inventory = dock.createInventory();
            if (inventory == null) {
                return slot == EMPTY_DOCK_SLOT && dock.canAcceptDeepNull(stack);
            }
            if (dock.exposesGeneratorBuffer() || inventory.isFluidOnly()) {
                return false;
            }
            return inventory.isItemValid(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            DeepNullInventory inventory = dock.createInventory();
            if (inventory == null) {
                if (slot == EMPTY_DOCK_SLOT && dock.canAcceptDeepNull(stack)) {
                    dock.setStoredDeepNull(stack);
                }
                return;
            }
            if (dock.exposesGeneratorBuffer() || inventory.isFluidOnly()) {
                return;
            }
            inventory.setStackInSlot(slot, stack);
        }
    }
}
