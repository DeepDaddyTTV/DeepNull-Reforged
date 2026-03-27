package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.StyleGlassVariant;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.SynchronizerItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public class NullWorkbenchMenu extends AbstractContainerMenu {
    public static final int BUTTON_BACKUP = 0;
    public static final int BUTTON_RESTORE = 1;

    private final BlockPos blockPos;
    private final @Nullable NullWorkbenchBlockEntity workbench;
    private final boolean clientSide;
    private final int[] syncedData = new int[5];
    private final ContainerData data;
    private ToggleableSlot inputSlot0;
    private ToggleableSlot inputSlot1;
    private ToggleableSlot inputSlot2;
    private ToggleableSlot inputSlot3;
    private ToggleableSlot outputSlot;

    public NullWorkbenchMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        this(containerId, playerInventory, resolveWorkbench(playerInventory, blockPos), blockPos);
    }

    public NullWorkbenchMenu(int containerId, Inventory playerInventory, NullWorkbenchBlockEntity workbench) {
        this(containerId, playerInventory, workbench, workbench.getBlockPos());
    }

    private NullWorkbenchMenu(int containerId, Inventory playerInventory, @Nullable NullWorkbenchBlockEntity workbench, BlockPos blockPos) {
        super(ModMenus.NULL_WORKBENCH_MENU.get(), containerId);
        this.workbench = workbench;
        this.blockPos = blockPos;
        this.clientSide = playerInventory.player.level().isClientSide();
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                if (clientSide) {
                    return index >= 0 && index < syncedData.length ? syncedData[index] : 0;
                }
                if (NullWorkbenchMenu.this.workbench == null) {
                    return 0;
                }
                return switch (index) {
                    case 0 -> NullWorkbenchMenu.this.workbench.getCraftProgress();
                    case 1 -> NullWorkbenchMenu.this.workbench.getCraftDuration();
                    case 2 -> NullWorkbenchMenu.this.workbench.getSyncProgress();
                    case 3 -> NullWorkbenchMenu.this.workbench.getSyncDuration();
                    case 4 -> NullWorkbenchMenu.this.workbench.isSyncing() ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < syncedData.length) {
                    syncedData[index] = value;
                }
            }

            @Override
            public int getCount() {
                return 5;
            }
        };

        if (workbench != null) {
            inputSlot0 = new ToggleableSlot(workbench, NullWorkbenchBlockEntity.INPUT_SLOT_START, 55, 73);
            inputSlot1 = new ToggleableSlot(workbench, NullWorkbenchBlockEntity.INPUT_SLOT_START + 1, 76, 73);
            inputSlot2 = new ToggleableSlot(workbench, NullWorkbenchBlockEntity.INPUT_SLOT_START + 2, 97, 73);
            inputSlot3 = new ToggleableSlot(workbench, NullWorkbenchBlockEntity.INPUT_SLOT_START + 3, 118, 73);
            outputSlot = new OutputSlot(workbench, 181, 73);
            addSlot(inputSlot0);
            addSlot(inputSlot1);
            addSlot(inputSlot2);
            addSlot(inputSlot3);
            addSlot(outputSlot);
            addSlot(new SlotItemHandler(workbench.getItemHandler(), NullWorkbenchBlockEntity.NULL_SLOT, -1000, -1000));
            addSlot(new SlotItemHandler(workbench.getItemHandler(), NullWorkbenchBlockEntity.SYNCHRONIZER_SLOT, -1000, -1000));
            addSlot(new SlotItemHandler(workbench.getItemHandler(), NullWorkbenchBlockEntity.SYNC_NULL_OUTPUT_SLOT, -1000, -1000));
            addSlot(new SlotItemHandler(workbench.getItemHandler(), NullWorkbenchBlockEntity.SYNC_SYNCHRONIZER_OUTPUT_SLOT, -1000, -1000));
            addSlot(new SlotItemHandler(workbench.getItemHandler(), NullWorkbenchBlockEntity.STYLE_MODIFIER_SLOT, -1000, -1000));
        }

        addPlayerInventorySlots(playerInventory);
        addDataSlots(data);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public boolean hasWorkbench() {
        return workbench != null;
    }

    public ItemStack getNullStack() {
        return workbench == null ? ItemStack.EMPTY : workbench.getStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT);
    }

    public ItemStack getSynchronizerStack() {
        return workbench == null ? ItemStack.EMPTY : workbench.getStackInSlot(NullWorkbenchBlockEntity.SYNCHRONIZER_SLOT);
    }

    public ItemStack getOutputStack() {
        return workbench == null ? ItemStack.EMPTY : workbench.getStackInSlot(NullWorkbenchBlockEntity.OUTPUT_SLOT);
    }

    public ItemStack getSyncNullOutputStack() {
        return workbench == null ? ItemStack.EMPTY : workbench.getStackInSlot(NullWorkbenchBlockEntity.SYNC_NULL_OUTPUT_SLOT);
    }

    public ItemStack getSyncSynchronizerOutputStack() {
        return workbench == null ? ItemStack.EMPTY : workbench.getStackInSlot(NullWorkbenchBlockEntity.SYNC_SYNCHRONIZER_OUTPUT_SLOT);
    }

    public ItemStack getStyleModifierStack() {
        return workbench == null ? ItemStack.EMPTY : workbench.getStackInSlot(NullWorkbenchBlockEntity.STYLE_MODIFIER_SLOT);
    }

    public boolean canBackup() {
        return workbench != null && workbench.canBackup();
    }

    public boolean canRestore() {
        return workbench != null && workbench.canRestore();
    }

    public int getCraftProgress() {
        return data.get(0);
    }

    public int getCraftDuration() {
        return Math.max(1, data.get(1));
    }

    public int getSyncProgress() {
        return data.get(2);
    }

    public int getSyncDuration() {
        return Math.max(1, data.get(3));
    }

    public boolean isSyncing() {
        return data.get(4) != 0;
    }

    public boolean applyStyleColors(int frameColor, int glassColor) {
        return workbench != null && workbench.applyStyleColors(frameColor, glassColor);
    }

    public boolean resetStyleColors() {
        return workbench != null && workbench.resetStyleColors();
    }

    public @Nullable NullWorkbenchBlockEntity getWorkbench() {
        return workbench;
    }

    public void setCraftSlotsActive(boolean active) {
        if (inputSlot0 != null) {
            inputSlot0.setActive(active);
            inputSlot1.setActive(active);
            inputSlot2.setActive(active);
            inputSlot3.setActive(active);
        }
    }

    public void setOutputSlotActive(boolean active) {
        if (outputSlot != null) {
            outputSlot.setActive(active);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return workbench != null
                && player.level().getBlockEntity(blockPos) instanceof NullWorkbenchBlockEntity
                && player.distanceToSqr(blockPos.getCenter()) <= 64.0D;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (workbench == null) {
            return false;
        }
        return switch (id) {
            case BUTTON_BACKUP -> workbench.startBackup();
            case BUTTON_RESTORE -> workbench.startRestore();
            default -> false;
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = index >= 0 && index < slots.size() ? slots.get(index) : null;
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int machineSlots = hasWorkbench() ? 10 : 0;

        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (hasWorkbench()) {
                if (stack.getItem() instanceof DeepNullItem) {
                    if (!moveItemStackTo(stack, NullWorkbenchBlockEntity.NULL_SLOT, NullWorkbenchBlockEntity.NULL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (stack.is(ModItems.SYNCHRONIZER.get())) {
                    if (!moveItemStackTo(stack, NullWorkbenchBlockEntity.SYNCHRONIZER_SLOT, NullWorkbenchBlockEntity.SYNCHRONIZER_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (StyleGlassVariant.isSupportedModifier(stack)) {
                    if (!moveItemStackTo(stack, NullWorkbenchBlockEntity.STYLE_MODIFIER_SLOT, NullWorkbenchBlockEntity.STYLE_MODIFIER_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, NullWorkbenchBlockEntity.INPUT_SLOT_START, NullWorkbenchBlockEntity.INPUT_SLOT_START + NullWorkbenchBlockEntity.INPUT_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        int inventoryX = 34;
        int inventoryY = 154;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, inventoryX + column * 21, inventoryY + row * 21));
            }
        }
        int hotbarY = 221;
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, inventoryX + column * 21, hotbarY));
        }
    }

    private static @Nullable NullWorkbenchBlockEntity resolveWorkbench(Inventory inventory, BlockPos blockPos) {
        return inventory.player.level().getBlockEntity(blockPos) instanceof NullWorkbenchBlockEntity workbench ? workbench : null;
    }

    private static class ToggleableSlot extends SlotItemHandler {
        private boolean active = true;

        private ToggleableSlot(NullWorkbenchBlockEntity workbench, int slot, int x, int y) {
            super(workbench.getItemHandler(), slot, x, y);
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public boolean isHighlightable() {
            return active;
        }
    }

    private static final class OutputSlot extends ToggleableSlot {
        private OutputSlot(NullWorkbenchBlockEntity workbench, int x, int y) {
            super(workbench, NullWorkbenchBlockEntity.OUTPUT_SLOT, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
