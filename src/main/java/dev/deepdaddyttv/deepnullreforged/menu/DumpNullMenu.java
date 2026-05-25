package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullCatalog;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullData;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullDropCandidate;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullDropSearch;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatusSummary;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullMobDropRow;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullMobOption;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullPresetCatalog;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullPresetOption;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullPresetTarget;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullData;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullSnapshot;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationRef;
import dev.deepdaddyttv.deepnullreforged.item.DockableNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DumpNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DumpNullUpgradeItem;
import dev.deepdaddyttv.deepnullreforged.item.HubNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DumpNullMenu extends AbstractContainerMenu {
    private static final int MAX_DROP_REQUEST_SIZE = 12;
    private static final int PLAYER_INV_COLUMNS = 9;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int UPGRADE_SLOT_COUNT = DumpNullUpgradeType.values().length;

    public enum SourceType {
        ITEM,
        DOCK,
        REMOTE_DOCK
    }

    public enum ViewMode {
        CATALOG,
        UPGRADES
    }

    private final SourceType sourceType;
    private final ViewMode viewMode;
    private final int inventorySlot;
    private final BlockPos dockPos;
    private final ResourceLocation remoteDimension;
    private final int hubInventorySlot;
    private final ItemStack snapshot;
    private DumpNullData data;
    private List<DumpNullMobOption> mobOptions;
    private final List<DumpNullPresetOption> presetOptions;
    private final List<DumpNullPresetTarget> biomeTargets;
    private final List<DumpNullPresetTarget> dimensionTargets;
    private List<DumpNullItemStatusSummary> itemStatuses;
    private final Map<ResourceLocation, List<DumpNullDropCandidate>> dropCache = new LinkedHashMap<>();
    private final SimpleContainer upgradeContainer = new SimpleContainer(UPGRADE_SLOT_COUNT);
    private final int upgradeSlotStartIndex;
    private final int upgradeSlotCount;
    private final int playerInventorySlotStartIndex;

    public static DumpNullMenu forItem(int containerId, Inventory playerInventory, int inventorySlot) {
        return forItem(containerId, playerInventory, inventorySlot, ViewMode.CATALOG);
    }

    public static DumpNullMenu forItem(int containerId, Inventory playerInventory, int inventorySlot, ViewMode viewMode) {
        ItemStack stack = playerInventory.getItem(inventorySlot);
        DumpNullData data = DumpNullData.get(stack);
        List<DumpNullMobOption> mobOptions = DumpNullCatalog.mobOptions(data);
        List<DumpNullPresetOption> presetOptions = presetOptions(playerInventory.player);
        List<DumpNullPresetTarget> biomeTargets = biomeTargets(playerInventory.player);
        List<DumpNullPresetTarget> dimensionTargets = dimensionTargets(playerInventory.player);
        return new DumpNullMenu(containerId, playerInventory, SourceType.ITEM, viewMode, inventorySlot, BlockPos.ZERO, ResourceLocation.withDefaultNamespace("overworld"), -1, stack.copy(), mobOptions, presetOptions, biomeTargets, dimensionTargets, data.itemStatusSummaries());
    }

    public static DumpNullMenu forDock(int containerId, Inventory playerInventory, DeepNullDockBlockEntity dock) {
        return forDock(containerId, playerInventory, dock, ViewMode.CATALOG);
    }

    public static DumpNullMenu forDock(int containerId, Inventory playerInventory, DeepNullDockBlockEntity dock, ViewMode viewMode) {
        ItemStack stack = dock.getStoredDeepNull();
        DumpNullData data = DumpNullData.get(stack);
        List<DumpNullMobOption> mobOptions = DumpNullCatalog.mobOptions(data);
        List<DumpNullPresetOption> presetOptions = presetOptions(playerInventory.player);
        List<DumpNullPresetTarget> biomeTargets = biomeTargets(playerInventory.player);
        List<DumpNullPresetTarget> dimensionTargets = dimensionTargets(playerInventory.player);
        return new DumpNullMenu(containerId, playerInventory, SourceType.DOCK, viewMode, -1, dock.getBlockPos(), ResourceLocation.withDefaultNamespace("overworld"), -1, stack.copy(), mobOptions, presetOptions, biomeTargets, dimensionTargets, data.itemStatusSummaries());
    }

    public static DumpNullMenu forRemoteDock(int containerId, Inventory playerInventory, DeepNullDockBlockEntity dock, ResourceLocation dimension, int hubInventorySlot) {
        return forRemoteDock(containerId, playerInventory, dock, dimension, hubInventorySlot, ViewMode.CATALOG);
    }

    public static DumpNullMenu forRemoteDock(int containerId, Inventory playerInventory, DeepNullDockBlockEntity dock, ResourceLocation dimension, int hubInventorySlot, ViewMode viewMode) {
        ItemStack stack = dock.getStoredDeepNull();
        DumpNullData data = DumpNullData.get(stack);
        List<DumpNullMobOption> mobOptions = DumpNullCatalog.mobOptions(data);
        List<DumpNullPresetOption> presetOptions = presetOptions(playerInventory.player);
        List<DumpNullPresetTarget> biomeTargets = biomeTargets(playerInventory.player);
        List<DumpNullPresetTarget> dimensionTargets = dimensionTargets(playerInventory.player);
        return new DumpNullMenu(containerId, playerInventory, SourceType.REMOTE_DOCK, viewMode, -1, dock.getBlockPos(), dimension, hubInventorySlot, stack.copy(), mobOptions, presetOptions, biomeTargets, dimensionTargets, data.itemStatusSummaries());
    }

    public DumpNullMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(
                containerId,
                playerInventory,
                SourceType.values()[buffer.readVarInt()],
                ViewMode.values()[buffer.readVarInt()],
                buffer.readVarInt(),
                buffer.readBlockPos(),
                buffer.readResourceLocation(),
                buffer.readVarInt(),
                ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),
                DumpNullMobOption.readList(buffer),
                DumpNullPresetOption.readList(buffer),
                DumpNullPresetTarget.readList(buffer),
                DumpNullPresetTarget.readList(buffer),
                DumpNullItemStatusSummary.readList(buffer)
        );
    }

    private DumpNullMenu(
            int containerId,
            Inventory playerInventory,
            SourceType sourceType,
            ViewMode viewMode,
            int inventorySlot,
            BlockPos dockPos,
            ResourceLocation remoteDimension,
            int hubInventorySlot,
            ItemStack snapshot,
            List<DumpNullMobOption> mobOptions,
            List<DumpNullPresetOption> presetOptions,
            List<DumpNullPresetTarget> biomeTargets,
            List<DumpNullPresetTarget> dimensionTargets,
            List<DumpNullItemStatusSummary> itemStatuses
    ) {
        super(ModMenus.DUMP_NULL_MENU.get(), containerId);
        this.sourceType = sourceType;
        this.viewMode = viewMode == null ? ViewMode.CATALOG : viewMode;
        this.inventorySlot = inventorySlot;
        this.dockPos = dockPos;
        this.remoteDimension = remoteDimension == null ? ResourceLocation.withDefaultNamespace("overworld") : remoteDimension;
        this.hubInventorySlot = hubInventorySlot;
        this.snapshot = snapshot;
        this.data = DumpNullData.get(snapshot);
        this.mobOptions = List.copyOf(mobOptions);
        this.presetOptions = List.copyOf(presetOptions);
        this.biomeTargets = List.copyOf(biomeTargets);
        this.dimensionTargets = List.copyOf(dimensionTargets);
        this.itemStatuses = List.copyOf(itemStatuses);
        refreshUpgradeContainer();
        this.upgradeSlotStartIndex = slots.size();
        if (this.viewMode == ViewMode.UPGRADES) {
            addUpgradeSlots();
            this.upgradeSlotCount = UPGRADE_SLOT_COUNT;
        } else {
            this.upgradeSlotCount = 0;
        }
        this.playerInventorySlotStartIndex = slots.size();
        if (this.viewMode == ViewMode.UPGRADES) {
            addPlayerInventorySlots(playerInventory, 82);
        }
    }

    public static void writeState(
            RegistryFriendlyByteBuf buffer,
            SourceType sourceType,
            ViewMode viewMode,
            int inventorySlot,
            BlockPos dockPos,
            ResourceLocation remoteDimension,
            int hubInventorySlot,
            ItemStack stack,
            List<DumpNullMobOption> mobOptions,
            List<DumpNullPresetOption> presetOptions,
            List<DumpNullPresetTarget> biomeTargets,
            List<DumpNullPresetTarget> dimensionTargets,
            List<DumpNullItemStatusSummary> itemStatuses
    ) {
        buffer.writeVarInt(sourceType.ordinal());
        buffer.writeVarInt((viewMode == null ? ViewMode.CATALOG : viewMode).ordinal());
        buffer.writeVarInt(inventorySlot);
        buffer.writeBlockPos(dockPos);
        buffer.writeResourceLocation(remoteDimension == null ? ResourceLocation.withDefaultNamespace("overworld") : remoteDimension);
        buffer.writeVarInt(hubInventorySlot);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
        DumpNullMobOption.writeList(buffer, mobOptions);
        DumpNullPresetOption.writeList(buffer, presetOptions);
        DumpNullPresetTarget.writeList(buffer, biomeTargets);
        DumpNullPresetTarget.writeList(buffer, dimensionTargets);
        DumpNullItemStatusSummary.writeList(buffer, itemStatuses);
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public ViewMode getViewMode() {
        return viewMode;
    }

    public int getInventorySlot() {
        return inventorySlot;
    }

    public BlockPos getDockPos() {
        return dockPos;
    }

    public ResourceLocation getRemoteDimension() {
        return remoteDimension;
    }

    public int getHubInventorySlot() {
        return hubInventorySlot;
    }

    public ItemStack getSnapshot() {
        return snapshot;
    }

    public DumpNullData getData() {
        return data;
    }

    public List<DumpNullMobOption> getMobOptions() {
        return mobOptions;
    }

    public List<DumpNullPresetOption> getPresetOptions() {
        return presetOptions;
    }

    public List<DumpNullPresetTarget> getBiomeTargets() {
        return biomeTargets;
    }

    public List<DumpNullPresetTarget> getDimensionTargets() {
        return dimensionTargets;
    }

    public List<DumpNullItemStatusSummary> getItemStatuses() {
        return itemStatuses;
    }

    public void updateState(DumpNullData data, List<DumpNullMobOption> mobOptions, List<DumpNullItemStatusSummary> itemStatuses) {
        this.data = data == null ? DumpNullData.EMPTY : data;
        this.mobOptions = List.copyOf(mobOptions == null ? List.of() : mobOptions);
        this.itemStatuses = List.copyOf(itemStatuses == null ? List.of() : itemStatuses);
        refreshUpgradeContainer();
    }

    public List<DumpNullMobDropRow> loadDropRows(ServerLevel level, Collection<ResourceLocation> requestedMobIds) {
        List<ResourceLocation> validIds = new ArrayList<>();
        for (ResourceLocation mobId : requestedMobIds) {
            if (validIds.size() >= MAX_DROP_REQUEST_SIZE) {
                break;
            }
            if (mobId != null && DumpNullCatalog.isVisibleMobEntity(mobId) && !validIds.contains(mobId)) {
                validIds.add(mobId);
            }
        }
        for (ResourceLocation mobId : validIds) {
            if (!dropCache.containsKey(mobId)) {
                dropCache.put(mobId, DumpNullDropCandidate.discover(level, List.of(mobId)));
            }
        }
        return validIds.stream()
                .map(mobId -> new DumpNullMobDropRow(mobId, dropCache.getOrDefault(mobId, List.of())))
                .toList();
    }

    public List<DumpNullMobDropRow> searchDropRows(ServerLevel level, String query) {
        String normalizedQuery = DumpNullDropSearch.normalizeQuery(query);
        if (normalizedQuery.length() < 2) {
            return List.of();
        }
        List<DumpNullMobDropRow> matches = new ArrayList<>();
        for (DumpNullMobOption option : mobOptions) {
            ResourceLocation mobId = option.entityId();
            if (!DumpNullCatalog.isVisibleMobEntity(mobId)) {
                continue;
            }
            List<DumpNullDropCandidate> candidates = dropCache.computeIfAbsent(mobId, id -> DumpNullDropCandidate.discover(level, List.of(id)));
            if (candidates.stream().anyMatch(candidate -> DumpNullDropSearch.matches(candidate.itemId(), normalizedQuery))) {
                matches.add(new DumpNullMobDropRow(mobId, candidates));
            }
        }
        return matches;
    }

    public @Nullable ItemStack resolveMutableStack(ServerPlayer player) {
        if (sourceType == SourceType.ITEM) {
            if (inventorySlot < 0 || inventorySlot >= player.getInventory().getContainerSize()) {
                return null;
            }
            ItemStack stack = player.getInventory().getItem(inventorySlot);
            return stack.getItem() instanceof DumpNullItem ? stack : null;
        }
        DeepNullDockBlockEntity dock = resolveDock(player);
        if (dock == null || !(dock.getStoredDeepNull().getItem() instanceof DumpNullItem)) {
            return null;
        }
        return dock.getStoredDeepNull();
    }

    public @Nullable DeepNullDockBlockEntity resolveDock(ServerPlayer player) {
        if (sourceType == SourceType.REMOTE_DOCK) {
            return resolveRemoteDock(player);
        }
        if (sourceType != SourceType.DOCK) {
            return null;
        }
        BlockEntity blockEntity = player.level().getBlockEntity(dockPos);
        return blockEntity instanceof DeepNullDockBlockEntity dock ? dock : null;
    }

    private @Nullable DeepNullDockBlockEntity resolveRemoteDock(ServerPlayer player) {
        if (hubInventorySlot < 0 || hubInventorySlot >= player.getInventory().getContainerSize()) {
            return null;
        }
        ItemStack hubStack = player.getInventory().getItem(hubInventorySlot);
        if (!(hubStack.getItem() instanceof HubNullItem)) {
            return null;
        }
        if (player.server == null) {
            return null;
        }
        HubNullStationRef ref = new HubNullStationRef(remoteDimension, dockPos, "");
        return HubNullSnapshot.resolveOnlineDumpDock(player.server, HubNullData.get(hubStack), ref);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (viewMode != ViewMode.UPGRADES || index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stackInSlot = slot.getItem();
        ItemStack original = stackInSlot.copy();
        int playerEnd = playerInventorySlotStartIndex + getPlayerSlotCount();

        if (index < playerInventorySlotStartIndex) {
            if (!moveItemStackTo(stackInSlot, playerInventorySlotStartIndex, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
            finishQuickMove(player, slot, stackInSlot, original);
            saveUpgradesToStack(player);
            return original;
        }

        if (tryInstallUpgrade(stackInSlot)) {
            stackInSlot.shrink(1);
            finishQuickMove(player, slot, stackInSlot, original);
            saveUpgradesToStack(player);
            return original;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.CLONE || clickType == ClickType.PICKUP_ALL) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
        if (viewMode == ViewMode.UPGRADES) {
            saveUpgradesToStack(player);
        }
    }

    @Override
    public void removed(Player player) {
        if (viewMode == ViewMode.UPGRADES) {
            saveUpgradesToStack(player);
        }
        super.removed(player);
    }

    @Override
    public boolean stillValid(Player player) {
        if (sourceType == SourceType.ITEM) {
            return inventorySlot >= 0
                    && inventorySlot < player.getInventory().getContainerSize()
                    && player.getInventory().getItem(inventorySlot).getItem() instanceof DumpNullItem;
        }
        if (sourceType == SourceType.REMOTE_DOCK) {
            return player instanceof ServerPlayer serverPlayer && resolveRemoteDock(serverPlayer) != null;
        }
        if (player.level().getBlockEntity(dockPos) instanceof DeepNullDockBlockEntity dock) {
            return dock.getStoredDeepNull().getItem() instanceof DumpNullItem
                    && player.distanceToSqr(dockPos.getX() + 0.5D, dockPos.getY() + 0.5D, dockPos.getZ() + 0.5D) <= 64.0D;
        }
        return false;
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
        return PLAYER_INV_COLUMNS * (PLAYER_INV_ROWS + 1);
    }

    private int addUpgradeSlots() {
        for (DumpNullUpgradeType type : DumpNullUpgradeType.values()) {
            addSlot(new DumpUpgradeSlot(upgradeContainer, type, 14 + type.slot() * 21, 35));
        }
        return UPGRADE_SLOT_COUNT;
    }

    private void addPlayerInventorySlots(Inventory playerInventory, int startY) {
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int column = 0; column < PLAYER_INV_COLUMNS; column++) {
                int index = column + row * PLAYER_INV_COLUMNS + 9;
                addSlot(createPlayerSlot(playerInventory, index, 14 + column * 21, startY + row * 21));
            }
        }
        int hotbarY = startY + 67;
        for (int column = 0; column < PLAYER_INV_COLUMNS; column++) {
            addSlot(createPlayerSlot(playerInventory, column, 14 + column * 21, hotbarY));
        }
    }

    private Slot createPlayerSlot(Inventory inventory, int slotIndex, int x, int y) {
        if ((sourceType == SourceType.ITEM && slotIndex == inventorySlot)
                || (sourceType == SourceType.REMOTE_DOCK && slotIndex == hubInventorySlot)) {
            return new LockedPlayerSlot(inventory, slotIndex, x, y);
        }
        return new Slot(inventory, slotIndex, x, y);
    }

    private void refreshUpgradeContainer() {
        upgradeContainer.clearContent();
        for (DumpNullUpgradeType type : data.upgrades()) {
            upgradeContainer.setItem(type.slot(), new ItemStack(itemFor(type)));
        }
    }

    private boolean tryInstallUpgrade(ItemStack stack) {
        if (!(stack.getItem() instanceof DumpNullUpgradeItem upgradeItem)) {
            return false;
        }
        int slot = upgradeItem.type().slot();
        if (!upgradeContainer.getItem(slot).isEmpty()) {
            return false;
        }
        upgradeContainer.setItem(slot, new ItemStack(stack.getItem()));
        return true;
    }

    private void saveUpgradesToStack(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack stack = resolveMutableStack(serverPlayer);
        if (stack == null || stack.isEmpty()) {
            return;
        }
        DumpNullData current = DumpNullData.get(stack);
        DumpNullData updated = current;
        for (DumpNullUpgradeType type : DumpNullUpgradeType.values()) {
            if (!upgradeContainer.getItem(type.slot()).isEmpty()) {
                updated = updated.withUpgrade(type);
            }
        }
        if (!updated.equals(current)) {
            DumpNullData.set(stack, updated);
            this.data = updated;
            if (sourceType == SourceType.ITEM) {
                serverPlayer.getInventory().setChanged();
            } else {
                DeepNullDockBlockEntity dock = resolveDock(serverPlayer);
                if (dock != null) {
                    dock.markStoredDeepNullChanged(true);
                }
            }
        }
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

    private static net.minecraft.world.item.Item itemFor(DumpNullUpgradeType type) {
        return switch (type) {
            case POWER -> ModItems.UPGRADE_CORE.get();
        };
    }

    private final class LockedPlayerSlot extends Slot {
        private LockedPlayerSlot(Inventory inventory, int slot, int x, int y) {
            super(inventory, slot, x, y);
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

    public static final class DumpUpgradeSlot extends Slot {
        private final DumpNullUpgradeType type;

        private DumpUpgradeSlot(SimpleContainer container, DumpNullUpgradeType type, int x, int y) {
            super(container, type.slot(), x, y);
            this.type = type;
        }

        public DumpNullUpgradeType getType() {
            return type;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof DumpNullUpgradeItem upgradeItem && upgradeItem.type() == type && !hasItem();
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static List<DumpNullPresetOption> presetOptions(Player player) {
        return player.level() instanceof ServerLevel ? DumpNullPresetCatalog.presetOptions() : List.of();
    }

    private static List<DumpNullPresetTarget> biomeTargets(Player player) {
        return player.level() instanceof ServerLevel serverLevel ? DumpNullPresetCatalog.biomeTargets(serverLevel) : List.of();
    }

    private static List<DumpNullPresetTarget> dimensionTargets(Player player) {
        return player.level() instanceof ServerLevel serverLevel ? DumpNullPresetCatalog.dimensionTargets(serverLevel) : List.of();
    }
}
