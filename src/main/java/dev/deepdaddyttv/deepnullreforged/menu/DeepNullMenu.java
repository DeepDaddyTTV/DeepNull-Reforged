package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullFilterMode;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.inventory.DampNullFluidContainerTransfer;
import dev.deepdaddyttv.deepnullreforged.inventory.StoneworksMaterial;
import dev.deepdaddyttv.deepnullreforged.inventory.StoneGeneratorVariant;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import dev.deepdaddyttv.deepnullreforged.inventory.NullSlotDomain;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferDirectionMode;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferOutputMode;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullData;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullSnapshot;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationRef;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DumpNullItem;
import dev.deepdaddyttv.deepnullreforged.item.HubNullItem;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class DeepNullMenu extends AbstractContainerMenu {
    private static final int SLOT_SPACING = 21;
    private static final int TOP_PADDING = 19;
    private static final int LEFT_PADDING = 9;

    public enum SourceType {
        ITEM,
        DOCK,
        REMOTE_DOCK
    }

    public enum ViewMode {
        MAIN,
        UPGRADES,
        FILTER,
        AUTO_SMELT_FILTER,
        FLUID,
        FARM
    }

    private final SourceType sourceType;
    private final ViewMode viewMode;
    private final DeepNullTier tier;
    private final DeepNullInventory dankInventory;
    private final int inventorySlot;
    private final @Nullable BlockPos dockPos;
    private final @Nullable ResourceLocation remoteDimension;
    private final int hubInventorySlot;
    private final boolean clientFluidOnly;
    private final int storageSlotCount;
    private final int upgradeSlotStartIndex;
    private final int upgradeSlotCount;
    private final int playerInventorySlotStartIndex;
    private final List<DeepNullUpgradeType> visibleUpgradeTypes;
    private final SimpleContainer fluidSlotContainer;
    private int syncedUpgradeMask;
    private int syncedEnergyStored;
    private boolean syncedChargingEnabled;
    private final @Nullable ServerPlayer serverPlayer;
    private List<FluidStack> lastSyncedFluids = List.of();
    private List<StoredChemical> lastSyncedChemicals = List.of();
    private boolean lastSyncedFarmEnabled;
    private ItemStack lastSyncedFarmSubstrate = ItemStack.EMPTY;

    public static DeepNullMenu forItem(int containerId, Inventory playerInventory, int inventorySlot, DeepNullTier tier) {
        return forItem(containerId, playerInventory, inventorySlot, tier, ViewMode.MAIN);
    }

    public static DeepNullMenu forItem(int containerId, Inventory playerInventory, int inventorySlot, DeepNullTier tier, ViewMode viewMode) {
        ItemStack stack = playerInventory.getItem(inventorySlot);
        DeepNullInventory inventory = new DeepNullInventory(tier, stack, playerInventory.player.level().registryAccess(), null);
        return new DeepNullMenu(
                containerId,
                playerInventory,
                SourceType.ITEM,
                viewMode,
                tier,
                inventory,
                inventorySlot,
                null,
                upgradeMask(inventory),
                inventory.getEnergyStored(),
                inventory.isChargingEnabled(),
                null,
                -1,
                inventory.isFluidOnly()
        );
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
                inventory == null ? DeepNullInventory.client(dock.getTier(), viewMode == ViewMode.FLUID) : inventory,
                -1,
                dock.getBlockPos(),
                inventory == null ? 0 : upgradeMask(inventory),
                inventory == null ? 0 : inventory.getEnergyStored(),
                inventory != null && inventory.isChargingEnabled(),
                null,
                -1,
                inventory == null ? viewMode == ViewMode.FLUID : inventory.isFluidOnly()
        );
    }

    public static DeepNullMenu forRemoteDock(int containerId, Inventory playerInventory, DeepNullDockBlockEntity dock, ResourceLocation dimension, int hubInventorySlot, ViewMode viewMode) {
        DeepNullInventory inventory = dock.createInventory();
        return new DeepNullMenu(
                containerId,
                playerInventory,
                SourceType.REMOTE_DOCK,
                viewMode,
                dock.getTier(),
                inventory == null ? DeepNullInventory.client(dock.getTier(), viewMode == ViewMode.FLUID) : inventory,
                -1,
                dock.getBlockPos(),
                inventory == null ? 0 : upgradeMask(inventory),
                inventory == null ? 0 : inventory.getEnergyStored(),
                inventory != null && inventory.isChargingEnabled(),
                dimension,
                hubInventorySlot,
                inventory == null ? viewMode == ViewMode.FLUID : inventory.isFluidOnly()
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
                buffer.readBlockPos(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readResourceLocation(),
                buffer.readVarInt(),
                buffer.readBoolean()
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
            @Nullable BlockPos dockPos,
            int syncedUpgradeMask,
            int syncedEnergyStored,
            boolean syncedChargingEnabled,
            @Nullable ResourceLocation remoteDimension,
            int hubInventorySlot,
            boolean clientFluidOnly
    ) {
        super(ModMenus.DEEP_NULL_MENU.get(), containerId);
        this.sourceType = sourceType;
        this.viewMode = viewMode;
        this.tier = tier;
        this.inventorySlot = inventorySlot;
        this.dockPos = dockPos;
        this.remoteDimension = remoteDimension;
        this.hubInventorySlot = hubInventorySlot;
        this.clientFluidOnly = clientFluidOnly;
        this.dankInventory = inventory == null
                ? resolveClientInventory(playerInventory, sourceType, tier, inventorySlot, dockPos, clientFluidOnly)
                : inventory;
        this.visibleUpgradeTypes = resolveVisibleUpgradeTypes();
        this.fluidSlotContainer = new SimpleContainer(tier.slotCount());
        this.syncedUpgradeMask = syncedUpgradeMask;
        this.syncedEnergyStored = syncedEnergyStored;
        this.syncedChargingEnabled = syncedChargingEnabled;
        this.serverPlayer = playerInventory.player instanceof ServerPlayer serverPlayer ? serverPlayer : null;

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
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return upgradeMask(DeepNullMenu.this.dankInventory);
            }

            @Override
            public void set(int value) {
                DeepNullMenu.this.syncedUpgradeMask = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return DeepNullMenu.this.dankInventory.getEnergyStored();
            }

            @Override
            public void set(int value) {
                DeepNullMenu.this.syncedEnergyStored = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return DeepNullMenu.this.dankInventory.isChargingEnabled() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                DeepNullMenu.this.syncedChargingEnabled = value != 0;
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

    public @Nullable ResourceLocation getRemoteDimension() {
        return remoteDimension;
    }

    public int getHubInventorySlot() {
        return hubInventorySlot;
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

    public DeepNullUpgradeType getUpgradeTypeAt(int visibleIndex) {
        return visibleUpgradeTypes.get(visibleIndex);
    }

    public int getPlayerInventorySlotStartIndex() {
        return playerInventorySlotStartIndex;
    }

    public int getPlayerSlotCount() {
        return 36;
    }

    public boolean hasUpgrade(DeepNullUpgradeType type) {
        if (dankInventory.hasUpgrade(type)) {
            return true;
        }
        return !type.usesSharedSlot() && (syncedUpgradeMask & (1 << type.slot())) != 0;
    }

    public boolean hasEnergyUpgrade() {
        return dankInventory.hasEnergyUpgrade() || (syncedUpgradeMask & (1 << DeepNullUpgradeType.ENERGY.slot())) != 0;
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

    public DeepNullFilterMode getAutoSmeltFilterMode() {
        return dankInventory.getAutoSmeltFilterMode();
    }

    public boolean setAutoSmeltFilterMode(DeepNullFilterMode mode) {
        if (!dankInventory.supportsAutoSmeltFiltering()) {
            return false;
        }
        dankInventory.setAutoSmeltFilterMode(mode);
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

    public ItemStack getAutoSmeltFilterStack(int slot) {
        return dankInventory.getAutoSmeltFilterStack(slot);
    }

    public boolean setAutoSmeltFilterStack(int slot, ItemStack stack) {
        if (!dankInventory.supportsAutoSmeltFiltering()) {
            return false;
        }
        dankInventory.setAutoSmeltFilterStack(slot, stack);
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

    public boolean setCustomExtractionMinimum(int slot, int amount) {
        return setCustomExtractionMinimum(slot, amount, false);
    }

    public boolean setCustomExtractionMinimum(int slot, int amount, boolean applyAll) {
        if (applyAll) {
            return dankInventory.setCustomExtractionMinimumAllOccupied(amount);
        }
        if (!isStorageSlot(slot) || dankInventory.getStackInSlot(slot).isEmpty()) {
            return false;
        }
        var currentMode = dankInventory.getExtractionMode(slot);
        int currentMinimum = dankInventory.getExtractionMinimum(slot);
        dankInventory.setCustomExtractionMinimum(slot, amount);
        return currentMode != dankInventory.getExtractionMode(slot)
                || currentMinimum != dankInventory.getExtractionMinimum(slot);
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

    public boolean moveStorageSlot(NullSlotDomain domain, int fromSlot, int toSlot) {
        if (!isStorageSlot(fromSlot) || !isStorageSlot(toSlot)) {
            return false;
        }
        return switch (domain) {
            case ITEM_STORAGE -> !isFluidStorageView() && dankInventory.moveSlot(fromSlot, toSlot);
            case FLUID_STORAGE -> isFluidStorageView() && dankInventory.moveTankSlot(fromSlot, toSlot);
        };
    }

    public boolean mergeTankSlot(int fromSlot, int toSlot) {
        if (!isStorageSlot(fromSlot) || !isStorageSlot(toSlot) || !isFluidStorageView()) {
            return false;
        }
        return dankInventory.mergeTankSlot(fromSlot, toSlot);
    }

    public boolean setDivNullLayer(int slot, ResourceLocation layerId) {
        if (!isStorageSlot(slot) || isFluidStorageView()) {
            return false;
        }
        return dankInventory.setDivNullLayer(slot, layerId);
    }

    public boolean clearFluidSlot(int slot) {
        if (slot < 0 || slot >= dankInventory.getFluidSlotCount()) {
            return false;
        }
        return dankInventory.clearFluidSlot(slot);
    }

    public boolean setFarmEnabled(boolean enabled) {
        if (!canConfigureFarm()) {
            return false;
        }
        boolean before = dankInventory.isFarmEnabled();
        dankInventory.setFarmEnabled(enabled);
        return before != dankInventory.isFarmEnabled();
    }

    public boolean setFarmSubstrate(ItemStack substrate) {
        if (!canConfigureFarm()) {
            return false;
        }
        ItemStack before = dankInventory.getFarmSubstrate();
        dankInventory.setFarmSubstrate(substrate);
        return !ItemStack.isSameItemSameComponents(before, dankInventory.getFarmSubstrate());
    }

    public boolean clearStorageSlotToPlayer(int slot, ServerPlayer player) {
        if (!canClearItemStorageSlot(slot)) {
            return false;
        }

        ItemStack stored = dankInventory.getStackInSlot(slot).copy();
        if (stored.isEmpty()) {
            return false;
        }

        dankInventory.setStackInSlot(slot, ItemStack.EMPTY);
        int remainingCount = stored.getCount();
        while (remainingCount > 0) {
            int chunkSize = Math.min(stored.getMaxStackSize(), remainingCount);
            ItemStack chunk = stored.copyWithCount(chunkSize);
            player.getInventory().add(chunk);
            if (!chunk.isEmpty()) {
                player.drop(chunk, false);
            }
            remainingCount -= chunkSize;
        }
        player.getInventory().setChanged();
        broadcastChanges();
        return true;
    }

    public boolean setLocked(boolean locked) {
        if (!dankInventory.supportsLocking()) {
            return false;
        }
        dankInventory.setLocked(locked);
        return true;
    }

    public boolean setChargingEnabled(boolean chargingEnabled) {
        if (!hasEnergyUpgrade()) {
            return false;
        }
        dankInventory.setChargingEnabled(chargingEnabled);
        return true;
    }

    public boolean setTransferLocked(boolean transferLocked) {
        return setTransferOutputMode(transferLocked ? TransferOutputMode.LOCKED : TransferOutputMode.ALL);
    }

    public boolean setTransferOutputMode(TransferOutputMode transferOutputMode) {
        if (dankInventory.getTransferOutputMode() == transferOutputMode) {
            return false;
        }
        dankInventory.setTransferOutputMode(transferOutputMode);
        return true;
    }

    public boolean setTransferDirectionMode(TransferDirectionMode transferDirectionMode) {
        if (dankInventory.getTransferDirectionMode() == transferDirectionMode) {
            return false;
        }
        dankInventory.setTransferDirectionMode(transferDirectionMode);
        return true;
    }

    public StoneGeneratorVariant getStoneGeneratorVariant() {
        return dankInventory.getStoneGeneratorVariant();
    }

    public boolean setStoneGeneratorVariant(StoneGeneratorVariant variant) {
        if (!dankInventory.hasStoneGeneratorUpgrade()) {
            return false;
        }
        dankInventory.setStoneGeneratorVariant(variant);
        return true;
    }

    public int getStoneworksTargetStacks() {
        return dankInventory.getStoneworksTargetStacks();
    }

    public boolean setStoneworksTargetStacks(int amount) {
        if (!dankInventory.hasStoneworksUpgrade()) {
            return false;
        }
        if (dankInventory.getStoneworksTargetStacks() == amount) {
            return false;
        }
        dankInventory.setStoneworksTargetStacks(amount);
        return true;
    }

    public boolean isStoneworksMonitoring(StoneworksMaterial material) {
        return dankInventory.isStoneworksMonitoring(material);
    }

    public boolean toggleStoneworksMonitoring(StoneworksMaterial material) {
        if (!dankInventory.hasStoneworksUpgrade()) {
            return false;
        }
        dankInventory.toggleStoneworksMonitoring(material);
        return true;
    }

    public List<StoneworksMaterial> getVisibleStoneworksMaterials() {
        return dankInventory.getVisibleStoneworksMaterials();
    }

    public ItemStack getStoneworksDisplayStack(StoneworksMaterial material) {
        return dankInventory.getStoneworksDisplayStack(material);
    }

    public int getDisplayedEnergyStored() {
        return hasEnergyUpgrade() ? syncedEnergyStored : 0;
    }

    public int getDisplayedEnergyCapacity() {
        return hasEnergyUpgrade() ? dankInventory.getEnergyCapacity() : 0;
    }

    public boolean isChargingEnabledDisplayed() {
        return hasEnergyUpgrade() && syncedChargingEnabled;
    }

    @Override
    public void broadcastChanges() {
        dankInventory.reloadFromBacking();
        super.broadcastChanges();
        syncFluidContentsToClient(false);
        syncFarmConfigToClient(false);
    }

    public void syncFluidContentsToClient(boolean force) {
        if (serverPlayer == null || !isFluidStorageView()) {
            return;
        }
        List<FluidStack> fluids = dankInventory.copyFluidStacks();
        List<StoredChemical> chemicals = dankInventory.copyChemicalStacks();
        if (!force && sameFluidContents(lastSyncedFluids, fluids) && sameChemicalContents(lastSyncedChemicals, chemicals)) {
            return;
        }
        lastSyncedFluids = fluids;
        lastSyncedChemicals = chemicals;
        DeepNullPayloads.sendFluidContents(serverPlayer, containerId, fluids, chemicals);
    }

    public void acceptFluidContents(List<FluidStack> fluids, List<StoredChemical> chemicals) {
        dankInventory.replaceFluidContents(fluids, chemicals);
    }

    public void syncFarmConfigToClient(boolean force) {
        if (serverPlayer == null || viewMode != ViewMode.FARM || !dankInventory.hasFarmUpgrade()) {
            return;
        }
        boolean enabled = dankInventory.isFarmEnabled();
        ItemStack substrate = dankInventory.getFarmSubstrate();
        if (!force
                && lastSyncedFarmEnabled == enabled
                && ItemStack.isSameItemSameComponents(lastSyncedFarmSubstrate, substrate)) {
            return;
        }
        lastSyncedFarmEnabled = enabled;
        lastSyncedFarmSubstrate = substrate.copy();
        DeepNullPayloads.sendFarmConfig(serverPlayer, containerId, enabled, substrate);
    }

    public void acceptFarmConfig(boolean enabled, ItemStack substrate) {
        dankInventory.setFarmEnabled(enabled);
        dankInventory.setFarmSubstrate(substrate);
    }

    private static boolean sameFluidContents(List<FluidStack> left, List<FluidStack> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int index = 0; index < left.size(); index++) {
            FluidStack leftStack = left.get(index);
            FluidStack rightStack = right.get(index);
            if (leftStack.isEmpty() || rightStack.isEmpty()) {
                if (leftStack.isEmpty() != rightStack.isEmpty()) {
                    return false;
                }
                continue;
            }
            if (leftStack.getAmount() != rightStack.getAmount()
                    || !FluidStack.isSameFluidSameComponents(leftStack, rightStack)) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameChemicalContents(List<StoredChemical> left, List<StoredChemical> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int index = 0; index < left.size(); index++) {
            StoredChemical leftStack = left.get(index);
            StoredChemical rightStack = right.get(index);
            if (leftStack.isEmpty() || rightStack.isEmpty()) {
                if (leftStack.isEmpty() != rightStack.isEmpty()) {
                    return false;
                }
                continue;
            }
            if (!leftStack.chemicalId().equals(rightStack.chemicalId())
                    || leftStack.amount() != rightStack.amount()
                    || !leftStack.iconPath().equals(rightStack.iconPath())
                    || leftStack.tint() != rightStack.tint()
                    || !leftStack.translationKey().equals(rightStack.translationKey())
                    || leftStack.gaseous() != rightStack.gaseous()) {
                return false;
            }
        }
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

    public int addGhostAutoSmeltFilterStack(ItemStack stack) {
        if (!dankInventory.supportsAutoSmeltFiltering() || stack.isEmpty()) {
            return -1;
        }
        int filterSlot = firstEmptyAutoSmeltFilterSlot();
        if (filterSlot < 0) {
            return -1;
        }
        dankInventory.setAutoSmeltFilterStack(filterSlot, stack.copyWithCount(1));
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

        if (sourceType == SourceType.REMOTE_DOCK) {
            return stillValidRemote(player);
        }

        if (dockPos == null) {
            return false;
        }

        if (!(player.level().getBlockEntity(dockPos) instanceof DeepNullDockBlockEntity dock)) {
            return false;
        }

        return player.distanceToSqr(dockPos.getCenter()) <= 64.0D && dock.hasStoredDeepNull();
    }

    private boolean stillValidRemote(Player player) {
        if (dockPos == null || remoteDimension == null || hubInventorySlot < 0 || hubInventorySlot >= player.getInventory().getContainerSize()) {
            return false;
        }
        ItemStack hubStack = player.getInventory().getItem(hubInventorySlot);
        if (!(hubStack.getItem() instanceof HubNullItem)) {
            return false;
        }
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return true;
        }
        HubNullData data = HubNullData.get(hubStack);
        HubNullStationRef ref = new HubNullStationRef(remoteDimension, dockPos, "");
        return HubNullSnapshot.resolveOnlineDock(server, data, ref) != null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

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
            ItemStack movable = slot instanceof DockStorageSlot
                    ? dankInventory.extractItemIgnoreExtractionMode(index, stackInSlot.getMaxStackSize(), true)
                    : dankInventory.getExtractableStackInSlot(index);
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
            if (slot instanceof DockStorageSlot) {
                dankInventory.extractItemIgnoreExtractionMode(index, moved, false);
            } else {
                dankInventory.extractItem(index, moved, false);
            }
            slot.setChanged();
            slot.onTake(player, movable.copyWithCount(moved));
            broadcastChanges();
            return ItemStack.EMPTY;
        }

        if (index >= upgradeStart && index < upgradeEnd) {
            if (!moveItemStackTo(stackInSlot, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
            finishQuickMove(player, slot, stackInSlot, original);
            return original;
        }

        if (index < playerStart || index >= playerEnd) {
            return ItemStack.EMPTY;
        }

        if (viewMode == ViewMode.FILTER || viewMode == ViewMode.AUTO_SMELT_FILTER) {
            if (index >= playerStart && index < playerEnd) {
                int filterSlot = viewMode == ViewMode.AUTO_SMELT_FILTER
                        ? addGhostAutoSmeltFilterStack(stackInSlot)
                        : addGhostFilterStack(stackInSlot);
                if (filterSlot < 0) {
                    return ItemStack.EMPTY;
                }
                broadcastChanges();
                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }

        if (moveItemStackTo(stackInSlot, upgradeStart, upgradeEnd, false)) {
            finishQuickMove(player, slot, stackInSlot, original);
            return original;
        }

        if (isFluidStorageView()) {
            ItemStack updated = tryStoreFluidFromContainer(stackInSlot, -1, true);
            if (ItemStack.matches(updated, stackInSlot)) {
                return ItemStack.EMPTY;
            }
            slot.set(updated);
            slot.setChanged();
            slot.onTake(player, updated);
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

        slot.set(remaining);
        finishQuickMove(player, slot, remaining, original);
        return original;
    }

    private void finishQuickMove(Player player, Slot slot, ItemStack currentStack, ItemStack originalStack) {
        if (currentStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (currentStack.getCount() != originalStack.getCount()) {
            slot.onTake(player, currentStack);
        }
        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.CLONE || clickType == ClickType.PICKUP_ALL) {
            return;
        }
        if (isFluidStorageView() && clickType == ClickType.PICKUP && slotId >= 0 && slotId < storageSlotCount) {
            ItemStack carried = getCarried();
            if (!carried.isEmpty()) {
                DampNullFluidContainerTransfer.CarriedTransfer transfer = DampNullFluidContainerTransfer.transferCarriedContainer(player, dankInventory, carried, slotId);
                if (transfer != null) {
                    setCarried(transfer.carriedStack());
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
        int leftPadding = storageLeftPadding();
        int topPadding = storageTopPadding();
        int slotCount = isFluidStorageView() ? dankInventory.getFluidSlotCount() : tier.slotCount();
        for (int slotIndex = 0; slotIndex < slotCount; slotIndex++) {
            int row = slotIndex / 9;
            int column = slotIndex % 9;
            if (isFluidStorageView()) {
                addSlot(new FluidStorageSlot(fluidSlotContainer, slotIndex, leftPadding + column * SLOT_SPACING, topPadding + row * SLOT_SPACING));
            } else {
                addSlot(sourceType != SourceType.ITEM
                        ? new DockStorageSlot(dankInventory, slotIndex, leftPadding + column * SLOT_SPACING, topPadding + row * SLOT_SPACING)
                        : new StorageSlot(dankInventory, slotIndex, leftPadding + column * SLOT_SPACING, topPadding + row * SLOT_SPACING));
            }
        }
        return slotCount;
    }

    private int addUpgradeSlots() {
        if (viewMode != ViewMode.UPGRADES) {
            return 0;
        }
        int leftPadding = storageLeftPadding();
        for (int visibleIndex = 0; visibleIndex < visibleUpgradeTypes.size(); visibleIndex++) {
            DeepNullUpgradeType type = visibleUpgradeTypes.get(visibleIndex);
            addSlot(new UpgradeSlot(dankInventory, type, leftPadding + visibleIndex * SLOT_SPACING, TOP_PADDING));
        }
        return visibleUpgradeTypes.size();
    }

    private void addPlayerInventorySlots(Inventory playerInventory, int rows) {
        int leftPadding = playerInventoryLeftPadding();
        int startY = playerInventoryStartY(rows);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = column + row * 9 + 9;
                addSlot(createPlayerSlot(playerInventory, index, leftPadding + column * SLOT_SPACING, startY + row * SLOT_SPACING));
            }
        }

        int hotbarY = hotbarY(rows);
        for (int column = 0; column < 9; column++) {
            addSlot(createPlayerSlot(playerInventory, column, leftPadding + column * SLOT_SPACING, hotbarY));
        }
    }

    private Slot createPlayerSlot(Inventory inventory, int slotIndex, int x, int y) {
        if ((sourceType == SourceType.ITEM && slotIndex == inventorySlot)
                || (sourceType == SourceType.REMOTE_DOCK && slotIndex == hubInventorySlot)) {
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
        return viewMode == ViewMode.FLUID && dankInventory.supportsFluidStorage();
    }

    private boolean canConfigureFarm() {
        return viewMode == ViewMode.FARM && !dankInventory.isFluidOnly() && dankInventory.hasFarmUpgrade();
    }

    private boolean canClearItemStorageSlot(int slot) {
        return viewMode == ViewMode.MAIN
                && !dankInventory.isFluidOnly()
                && isStorageSlot(slot)
                && !dankInventory.getStackInSlot(slot).isEmpty();
    }

    private boolean usesIntegratedEnergyLayout() {
        return hasEnergyUpgrade()
                && tier.supportsEnergyUpgrade()
                && (viewMode == ViewMode.MAIN
                || viewMode == ViewMode.UPGRADES
                || viewMode == ViewMode.FILTER
                || viewMode == ViewMode.AUTO_SMELT_FILTER);
    }

    private static int upgradeMask(DeepNullInventory inventory) {
        int mask = 0;
        for (DeepNullUpgradeType type : DeepNullUpgradeType.values()) {
            if (inventory.hasUpgrade(type)) {
                mask |= 1 << type.slot();
            }
        }
        return mask;
    }

    private List<DeepNullUpgradeType> resolveVisibleUpgradeTypes() {
        if (viewMode != ViewMode.UPGRADES) {
            return List.of();
        }
        return Arrays.stream(DeepNullUpgradeType.values())
                .filter(DeepNullUpgradeType::isUpgradeScreenRepresentative)
                .filter(dankInventory::supportsUpgrade)
                .filter(type -> type != DeepNullUpgradeType.GAS || ModList.get().isLoaded("mekanism"))
                .toList();
    }

    private int storageLeftPadding() {
        return usesIntegratedEnergyLayout() ? 59 : LEFT_PADDING;
    }

    private int storageTopPadding() {
        return TOP_PADDING;
    }

    private int playerInventoryLeftPadding() {
        return usesIntegratedEnergyLayout() ? 59 : LEFT_PADDING;
    }

    private int playerInventoryStartY(int rows) {
        return 28 + rows * SLOT_SPACING;
    }

    private int hotbarY(int rows) {
        return 95 + rows * SLOT_SPACING;
    }

    private int firstEmptyFilterSlot() {
        for (int slot = 0; slot < dankInventory.getFilterSlotCount(); slot++) {
            if (dankInventory.getFilterStack(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private int firstEmptyAutoSmeltFilterSlot() {
        for (int slot = 0; slot < dankInventory.getFilterSlotCount(); slot++) {
            if (dankInventory.getAutoSmeltFilterStack(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private ItemStack tryStoreFluidFromContainer(ItemStack stack, int preferredSlot, boolean allowFirstEmptyFallback) {
        if (!dankInventory.supportsFluidStorage()
                || stack.isEmpty()
                || stack.getCount() != 1
                || !DampNullFluidContainerTransfer.containsTransferableFluid(stack)) {
            return stack;
        }

        ItemStack updated = DampNullFluidContainerTransfer.transferSingleContainer(
                dankInventory,
                stack,
                preferredSlot,
                allowFirstEmptyFallback,
                false
        );
        return updated == null ? stack : updated;
    }

    private int viewModeRows() {
        return switch (viewMode) {
            case MAIN -> tier.rows();
            case UPGRADES -> 1;
            case FILTER, AUTO_SMELT_FILTER -> 3;
            case FLUID -> tier.rows();
            case FARM -> 3;
        };
    }

    private static DeepNullInventory resolveClientInventory(
            Inventory playerInventory,
            SourceType sourceType,
            DeepNullTier tier,
            int inventorySlot,
            @Nullable BlockPos dockPos,
            boolean clientFluidOnly
    ) {
        if (sourceType == SourceType.ITEM) {
            if (inventorySlot >= 0 && inventorySlot < playerInventory.getContainerSize()) {
                ItemStack stack = playerInventory.getItem(inventorySlot);
                if (stack.getItem() instanceof DeepNullItem deepNullItem) {
                    return new DeepNullInventory(deepNullItem.tier(), stack, playerInventory.player.level().registryAccess(), null);
                }
            }
            return DeepNullInventory.client(tier, clientFluidOnly);
        }

        if (sourceType == SourceType.DOCK && dockPos != null && playerInventory.player.level().getBlockEntity(dockPos) instanceof DeepNullDockBlockEntity dock) {
            DeepNullInventory inventory = dock.createInventory();
            if (inventory != null) {
                return inventory;
            }
        }
        return DeepNullInventory.client(tier, clientFluidOnly);
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
        private final DeepNullUpgradeType upgradeType;

        private UpgradeSlot(DeepNullInventory inventory, DeepNullUpgradeType upgradeType, int xPosition, int yPosition) {
            super(inventory.getUpgradeHandler(), upgradeType.slot(), xPosition, yPosition);
            this.upgradeType = upgradeType;
        }

        public DeepNullUpgradeType getUpgradeType() {
            return upgradeType;
        }
    }
}
