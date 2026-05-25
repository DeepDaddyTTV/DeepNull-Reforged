package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripNullData;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripProfile;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripSwapEngine;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DripNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DripNullUpgradeItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DripNullMenu extends AbstractContainerMenu {
    private static final int PLAYER_INV_COLUMNS = 9;
    private static final int PLAYER_INV_ROWS = 3;

    public enum SourceType {
        ITEM,
        DOCK
    }

    public enum ViewMode {
        PROFILES,
        SETTINGS,
        RECOVERY
    }

    private final SourceType sourceType;
    private final ViewMode viewMode;
    private final DeepNullTier tier;
    private DripNullData data;
    private final int inventorySlot;
    private final @Nullable BlockPos dockPos;
    private final SimpleContainer recoveryContainer = new SimpleContainer(DripNullData.MAX_LOOSE_ITEMS);
    private final int recoverySlotStartIndex;
    private final int recoverySlotCount;
    private final int playerInventorySlotStartIndex;

    public static DripNullMenu forItem(int containerId, Inventory playerInventory, int inventorySlot, DeepNullTier tier, ViewMode viewMode) {
        ItemStack stack = playerInventory.getItem(inventorySlot);
        return new DripNullMenu(containerId, playerInventory, SourceType.ITEM, viewMode, tier,
                DripNullData.get(stack, tier, playerInventory.player.level().registryAccess()), inventorySlot, null);
    }

    public static DripNullMenu forDock(int containerId, Inventory playerInventory, DeepNullDockBlockEntity dock, DeepNullTier tier, ViewMode viewMode) {
        ItemStack stack = dock.getStoredDeepNull();
        return new DripNullMenu(containerId, playerInventory, SourceType.DOCK, viewMode, tier,
                DripNullData.get(stack, tier, playerInventory.player.level().registryAccess()), -1, dock.getBlockPos());
    }

    public DripNullMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(
                containerId,
                playerInventory,
                SourceType.values()[buffer.readVarInt()],
                ViewMode.values()[buffer.readVarInt()],
                DeepNullTier.byId(buffer.readVarInt()),
                DripNullData.read(buffer),
                buffer.readVarInt(),
                buffer.readBlockPos()
        );
    }

    private DripNullMenu(
            int containerId,
            Inventory playerInventory,
            SourceType sourceType,
            ViewMode viewMode,
            DeepNullTier tier,
            DripNullData data,
            int inventorySlot,
            @Nullable BlockPos dockPos
    ) {
        super(ModMenus.DRIP_NULL_MENU.get(), containerId);
        this.sourceType = sourceType;
        this.viewMode = viewMode == null ? ViewMode.PROFILES : viewMode;
        this.tier = tier;
        this.data = data == null ? DripNullData.empty(tier) : data.withTierProfileCount(tier);
        this.inventorySlot = inventorySlot;
        this.dockPos = dockPos;
        refreshRecoveryContainer();
        this.recoverySlotStartIndex = slots.size();
        if (this.viewMode == ViewMode.RECOVERY) {
            addRecoverySlots();
            this.recoverySlotCount = DripNullData.MAX_LOOSE_ITEMS;
        } else {
            this.recoverySlotCount = 0;
        }
        this.playerInventorySlotStartIndex = slots.size();
        if (this.viewMode == ViewMode.RECOVERY) {
            addPlayerInventorySlots(playerInventory, 158);
        }
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public ViewMode getViewMode() {
        return viewMode;
    }

    public DeepNullTier getTier() {
        return tier;
    }

    public DripNullData getData() {
        return data;
    }

    public int getInventorySlot() {
        return inventorySlot;
    }

    public void updateData(DripNullData data) {
        this.data = data == null ? DripNullData.empty(tier) : data.withTierProfileCount(tier);
        refreshRecoveryContainer();
    }

    public void reopen(ServerPlayer player, ViewMode targetView) {
        if (sourceType == SourceType.ITEM) {
            DripNullMenuOpener.openHeldItem(player, player.getInventory(), inventorySlot, targetView);
            return;
        }
        DeepNullDockBlockEntity dock = resolveDock(player);
        if (dock != null) {
            DripNullMenuOpener.openDock(player, dock, targetView);
        }
    }

    public ItemStack resolveStack(Player player) {
        if (sourceType == SourceType.ITEM) {
            if (inventorySlot < 0 || inventorySlot >= player.getInventory().getContainerSize()) {
                return ItemStack.EMPTY;
            }
            return player.getInventory().getItem(inventorySlot);
        }
        DeepNullDockBlockEntity dock = resolveDock(player);
        return dock == null ? ItemStack.EMPTY : dock.getStoredDeepNull();
    }

    public boolean selectProfile(ServerPlayer player, int profile) {
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DripNullItem dripNullItem)) {
            return false;
        }
        boolean changed = DripSwapEngine.selectProfile(player, stack, dripNullItem.tier(), profile);
        if (changed) {
            updateData(DripNullData.get(stack, dripNullItem.tier(), player.level().registryAccess()));
            markDockChanged(player);
        }
        return changed;
    }

    public boolean cycleProfile(ServerPlayer player, boolean forward) {
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DripNullItem dripNullItem)) {
            return false;
        }
        boolean changed = DripSwapEngine.cycleProfile(player, stack, dripNullItem.tier(), forward);
        if (changed) {
            updateData(DripNullData.get(stack, dripNullItem.tier(), player.level().registryAccess()));
            markDockChanged(player);
        }
        return changed;
    }

    public boolean toggleScope(ServerPlayer player, String scope) {
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DripNullItem dripNullItem)) {
            return false;
        }
        boolean changed = DripSwapEngine.toggleScope(player, stack, dripNullItem.tier(), data.selectedProfile(), scope);
        if (changed) {
            updateData(DripNullData.get(stack, dripNullItem.tier(), player.level().registryAccess()));
            markDockChanged(player);
        }
        return changed;
    }

    public DripSwapEngine.Result runSwap(ServerPlayer player) {
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DripNullItem dripNullItem)) {
            return DripSwapEngine.Result.fail(net.minecraft.network.chat.Component.translatable("item.deepnullreforged.drip_null.missing"));
        }
        DripSwapEngine.Result result = DripSwapEngine.run(player, stack, dripNullItem.tier(), inventorySlot);
        updateData(DripNullData.get(stack, dripNullItem.tier(), player.level().registryAccess()));
        markDockChanged(player);
        return result;
    }

    public DripSwapEngine.Result clearSelectedProfile(ServerPlayer player) {
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DripNullItem dripNullItem)) {
            return DripSwapEngine.Result.fail(net.minecraft.network.chat.Component.translatable("item.deepnullreforged.drip_null.missing"));
        }
        DripSwapEngine.Result result = DripSwapEngine.clearSelectedProfile(player, stack, dripNullItem.tier());
        updateData(DripNullData.get(stack, dripNullItem.tier(), player.level().registryAccess()));
        markDockChanged(player);
        return result;
    }

    public boolean recover(ServerPlayer player) {
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DripNullItem dripNullItem)) {
            return false;
        }
        boolean recovered = DripSwapEngine.recover(player, stack, dripNullItem.tier());
        if (recovered) {
            updateData(DripNullData.get(stack, dripNullItem.tier(), player.level().registryAccess()));
            markDockChanged(player);
        }
        return recovered;
    }

    public boolean installMendFromInventory(ServerPlayer player) {
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DripNullItem dripNullItem)) {
            return false;
        }
        int upgradeSlot = findMendUpgrade(player.getInventory());
        if (upgradeSlot < 0) {
            return false;
        }
        boolean installed = DripSwapEngine.installMend(player, stack, dripNullItem.tier());
        if (!installed) {
            return false;
        }
        player.getInventory().getItem(upgradeSlot).shrink(1);
        updateData(DripNullData.get(stack, dripNullItem.tier(), player.level().registryAccess()));
        markDockChanged(player);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (viewMode != ViewMode.RECOVERY || index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem() || index < recoverySlotStartIndex || index >= recoverySlotStartIndex + recoverySlotCount) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int playerEnd = playerInventorySlotStartIndex + getPlayerSlotCount();
        if (!moveItemStackTo(stack, playerInventorySlotStartIndex, playerEnd, true)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        saveRecoveryToStack(player);
        broadcastChanges();
        return original;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.CLONE || clickType == ClickType.PICKUP_ALL) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
        if (viewMode == ViewMode.RECOVERY) {
            saveRecoveryToStack(player);
        }
    }

    @Override
    public void removed(Player player) {
        if (viewMode == ViewMode.RECOVERY) {
            saveRecoveryToStack(player);
        }
        super.removed(player);
    }

    public int getRecoverySlotStartIndex() {
        return recoverySlotStartIndex;
    }

    public int getRecoverySlotCount() {
        return recoverySlotCount;
    }

    public int getPlayerInventorySlotStartIndex() {
        return playerInventorySlotStartIndex;
    }

    public int getPlayerSlotCount() {
        return PLAYER_INV_COLUMNS * (PLAYER_INV_ROWS + 1);
    }

    private void addRecoverySlots() {
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                int index = column + row * 9;
                addSlot(new RecoverySlot(recoveryContainer, index, 14 + column * 18, 34 + row * 18));
            }
        }
    }

    private void refreshRecoveryContainer() {
        recoveryContainer.clearContent();
        List<ItemStack> looseItems = data.looseItems();
        for (int i = 0; i < Math.min(looseItems.size(), recoveryContainer.getContainerSize()); i++) {
            recoveryContainer.setItem(i, looseItems.get(i).copy());
        }
    }

    private void saveRecoveryToStack(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DripNullItem dripNullItem)) {
            return;
        }
        List<ItemStack> loose = new ArrayList<>();
        for (int slot = 0; slot < recoveryContainer.getContainerSize(); slot++) {
            ItemStack stored = recoveryContainer.getItem(slot);
            if (!stored.isEmpty()) {
                loose.add(stored.copy());
            }
        }
        DripNullData current = DripNullData.get(stack, dripNullItem.tier(), player.level().registryAccess());
        DripNullData updated = current.withState(current.selectedProfile(), current.equippedProfile(), current.profiles(), current.vaultItems(), loose, current.nextRef());
        DripNullData.set(stack, updated, dripNullItem.tier(), player.level().registryAccess());
        updateData(updated);
        markDockChanged(serverPlayer);
        serverPlayer.getInventory().setChanged();
    }

    private final class RecoverySlot extends Slot {
        private RecoverySlot(SimpleContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            saveRecoveryToStack(player);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (!player.isAlive()) {
            return false;
        }
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DripNullItem)) {
            return false;
        }
        return sourceType != SourceType.DOCK || dockPos == null || player.distanceToSqr(dockPos.getCenter()) <= 64.0D;
    }

    public DripProfile selectedProfile() {
        return data.selected();
    }

    private void addPlayerInventorySlots(Inventory playerInventory, int startY) {
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int column = 0; column < PLAYER_INV_COLUMNS; column++) {
                int index = column + row * PLAYER_INV_COLUMNS + 9;
                addSlot(createPlayerSlot(playerInventory, index, 14 + column * 18, startY + row * 18));
            }
        }
        int hotbarY = startY + 58;
        for (int column = 0; column < PLAYER_INV_COLUMNS; column++) {
            addSlot(createPlayerSlot(playerInventory, column, 14 + column * 18, hotbarY));
        }
    }

    private Slot createPlayerSlot(Inventory inventory, int slot, int x, int y) {
        return sourceType == SourceType.ITEM && slot == inventorySlot
                ? new LockedSourceSlot(inventory, slot, x, y)
                : new Slot(inventory, slot, x, y);
    }

    private @Nullable DeepNullDockBlockEntity resolveDock(Player player) {
        if (dockPos == null) {
            return null;
        }
        return player.level().getBlockEntity(dockPos) instanceof DeepNullDockBlockEntity dock ? dock : null;
    }

    private void markDockChanged(Player player) {
        if (sourceType == SourceType.DOCK) {
            DeepNullDockBlockEntity dock = resolveDock(player);
            if (dock != null) {
                dock.markStoredDeepNullChanged();
            }
        }
    }

    private static int findMendUpgrade(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof DripNullUpgradeItem upgradeItem && upgradeItem.type() == DripNullUpgradeType.MEND) {
                return slot;
            }
        }
        return -1;
    }

    private final class LockedSourceSlot extends Slot {
        private LockedSourceSlot(Inventory inventory, int slot, int x, int y) {
            super(inventory, slot, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return !(sourceType == SourceType.ITEM && getContainerSlot() == inventorySlot);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !(sourceType == SourceType.ITEM && getContainerSlot() == inventorySlot);
        }
    }
}
