package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullBufferEntry;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullCaptureFilterMode;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullData;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeData;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullData;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullSnapshot;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationRef;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DenNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DenNullUpgradeItem;
import dev.deepdaddyttv.deepnullreforged.item.DockableNullItem;
import dev.deepdaddyttv.deepnullreforged.item.HubNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class DenNullMenu extends AbstractContainerMenu {
    private static final int SLOT_SPACING = 21;
    private static final int PLAYER_INV_COLUMNS = 9;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int UPGRADE_SLOT_COUNT = DenNullUpgradeType.values().length;

    public enum SourceType {
        ITEM,
        DOCK,
        REMOTE_DOCK
    }

    public enum ViewMode {
        GRID,
        UPGRADES,
        SETTINGS
    }

    private final SourceType sourceType;
    private final ViewMode viewMode;
    private final DeepNullTier tier;
    private DenNullData data;
    private final int inventorySlot;
    private final @Nullable BlockPos dockPos;
    private final @Nullable ResourceLocation remoteDimension;
    private final int hubInventorySlot;
    private final int upgradeSlotStartIndex;
    private final int upgradeSlotCount;
    private final int breedingSlotStartIndex;
    private final int breedingSlotCount;
    private final int milkSlotIndex;
    private final int nameTagSlotIndex;
    private final int playerInventorySlotStartIndex;
    private final SimpleContainer upgradeContainer = new SimpleContainer(UPGRADE_SLOT_COUNT);
    private final SimpleContainer breedingContainer = new SimpleContainer(DenNullUpgradeData.BREEDING_BUFFER_SLOTS);
    private final SimpleContainer milkContainer = new SimpleContainer(1);
    private final SimpleContainer nameTagContainer = new SimpleContainer(1);

    public static DenNullMenu forItem(int containerId, Inventory playerInventory, int inventorySlot, DeepNullTier tier) {
        return forItem(containerId, playerInventory, inventorySlot, tier, ViewMode.GRID);
    }

    public static DenNullMenu forItem(int containerId, Inventory playerInventory, int inventorySlot, DeepNullTier tier, ViewMode viewMode) {
        ItemStack stack = playerInventory.getItem(inventorySlot);
        return new DenNullMenu(containerId, playerInventory, SourceType.ITEM, viewMode, tier, DenNullData.get(stack), inventorySlot, null, null, -1);
    }

    public static DenNullMenu forDock(int containerId, Inventory playerInventory, DeepNullDockBlockEntity dock) {
        return forDock(containerId, playerInventory, dock, ViewMode.GRID);
    }

    public static DenNullMenu forDock(int containerId, Inventory playerInventory, DeepNullDockBlockEntity dock, ViewMode viewMode) {
        ItemStack stack = dock.getStoredDeepNull();
        return new DenNullMenu(containerId, playerInventory, SourceType.DOCK, viewMode, dock.getTier(), DenNullData.get(stack), -1, dock.getBlockPos(), null, -1);
    }

    public static DenNullMenu forRemoteDock(int containerId, Inventory playerInventory, DeepNullDockBlockEntity dock, ResourceLocation dimension, int hubInventorySlot) {
        return forRemoteDock(containerId, playerInventory, dock, dimension, hubInventorySlot, ViewMode.GRID);
    }

    public static DenNullMenu forRemoteDock(int containerId, Inventory playerInventory, DeepNullDockBlockEntity dock, ResourceLocation dimension, int hubInventorySlot, ViewMode viewMode) {
        ItemStack stack = dock.getStoredDeepNull();
        return new DenNullMenu(containerId, playerInventory, SourceType.REMOTE_DOCK, viewMode, dock.getTier(), DenNullData.get(stack), -1, dock.getBlockPos(), dimension, hubInventorySlot);
    }

    public DenNullMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(
                containerId,
                playerInventory,
                SourceType.values()[buffer.readVarInt()],
                ViewMode.values()[buffer.readVarInt()],
                DeepNullTier.byId(buffer.readVarInt()),
                DenNullData.read(buffer),
                buffer.readVarInt(),
                buffer.readBlockPos(),
                buffer.readResourceLocation(),
                buffer.readVarInt()
        );
    }

    private DenNullMenu(
            int containerId,
            Inventory playerInventory,
            SourceType sourceType,
            ViewMode viewMode,
            DeepNullTier tier,
            DenNullData data,
            int inventorySlot,
            @Nullable BlockPos dockPos,
            @Nullable ResourceLocation remoteDimension,
            int hubInventorySlot
    ) {
        super(ModMenus.DEN_NULL_MENU.get(), containerId);
        this.sourceType = sourceType;
        this.viewMode = viewMode == null ? ViewMode.GRID : viewMode;
        this.tier = tier;
        this.data = data == null ? DenNullData.EMPTY : data;
        this.inventorySlot = inventorySlot;
        this.dockPos = dockPos;
        this.remoteDimension = remoteDimension;
        this.hubInventorySlot = hubInventorySlot;
        refreshContainersFromData();

        this.upgradeSlotStartIndex = slots.size();
        this.upgradeSlotCount = addUpgradeSlots();
        this.breedingSlotStartIndex = slots.size();
        this.breedingSlotCount = addBreedingSlots();
        this.milkSlotIndex = addMilkSlot();
        this.nameTagSlotIndex = addNameTagSlot();
        this.playerInventorySlotStartIndex = slots.size();
        if (viewMode == ViewMode.UPGRADES) {
            addPlayerInventorySlots(playerInventory, 74);
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

    public DenNullData getData() {
        return data;
    }

    public void updateData(DenNullData data) {
        this.data = data == null ? DenNullData.EMPTY : data;
        refreshContainersFromData();
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

    public void reopen(ServerPlayer player, ViewMode targetView) {
        if (sourceType == SourceType.ITEM) {
            DenNullMenuOpener.openHeldItem(player, player.getInventory(), inventorySlot, targetView);
            return;
        }
        DeepNullDockBlockEntity dock = resolveDock(player);
        if (dock == null) {
            return;
        }
        if (sourceType == SourceType.DOCK) {
            DenNullMenuOpener.openDock(player, dock, targetView);
            return;
        }
        if (remoteDimension != null) {
            DenNullMenuOpener.openRemoteDock(player, hubInventorySlot, new HubNullStationRef(remoteDimension, dock.getBlockPos(), ""), dock, targetView);
        }
    }

    public boolean setSelectedIndex(Player player, int selectedIndex) {
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DenNullItem)) {
            return false;
        }
        DenNullData current = DenNullData.get(stack).withSelectedIndex(selectedIndex);
        DenNullData.set(stack, current);
        updateData(current);
        markChanged(player, stack);
        return true;
    }

    public boolean releaseEntryAtFeet(ServerPlayer player, int entryIndex) {
        if (viewMode != ViewMode.GRID || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DenNullItem denNullItem)) {
            return false;
        }
        if (!denNullItem.releaseEntryAtFeet(player, level, stack, entryIndex)) {
            return false;
        }
        DenNullData current = DenNullData.get(stack);
        updateData(current);
        markChanged(player, stack);
        return true;
    }

    public boolean setDyeColor(Player player, int dyeColorId) {
        return mutateUpgrades(player, upgrades -> upgrades.withDyeColorId(dyeColorId));
    }

    public boolean setBabyEnabled(Player player, boolean enabled) {
        return mutateUpgrades(player, upgrades -> upgrades.withBabyEnabled(enabled));
    }

    public boolean setCaptureEnabled(Player player, boolean enabled) {
        return mutateUpgrades(player, upgrades -> upgrades.withCaptureEnabled(enabled));
    }

    public boolean setCaptureFilterMode(Player player, DenNullCaptureFilterMode mode) {
        return mutateUpgrades(player, upgrades -> upgrades.withCaptureFilter(mode, upgrades.captureFilter()));
    }

    public boolean setTagTemplate(Player player, String template) {
        return mutateUpgrades(player, upgrades -> upgrades.withTagTemplate(template));
    }

    public boolean addCaptureFilter(Player player, ResourceLocation entityId) {
        if (entityId == null || BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).isEmpty()) {
            return false;
        }
        return mutateUpgrades(player, upgrades -> {
            List<ResourceLocation> filter = new ArrayList<>(upgrades.captureFilter());
            if (!filter.contains(entityId)) {
                filter.add(entityId);
            }
            return upgrades.withCaptureFilter(upgrades.captureFilterMode(), filter);
        });
    }

    public boolean clearCaptureFilter(Player player) {
        return mutateUpgrades(player, upgrades -> upgrades.withCaptureFilter(upgrades.captureFilterMode(), List.of()));
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

    private boolean mutateUpgrades(Player player, UpgradeMutation mutation) {
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DenNullItem)) {
            return false;
        }
        DenNullData current = DenNullData.get(stack);
        DenNullData updated = current.withUpgrades(mutation.apply(current.upgrades()));
        DenNullData.set(stack, updated);
        updateData(updated);
        markChanged(player, stack);
        return true;
    }

    private @Nullable DeepNullDockBlockEntity resolveDock(Player player) {
        if (dockPos == null) {
            return null;
        }
        if (sourceType == SourceType.DOCK) {
            return player.level().getBlockEntity(dockPos) instanceof DeepNullDockBlockEntity dock ? dock : null;
        }
        if (remoteDimension == null || hubInventorySlot < 0 || hubInventorySlot >= player.getInventory().getContainerSize()) {
            return null;
        }
        ItemStack hubStack = player.getInventory().getItem(hubInventorySlot);
        if (!(hubStack.getItem() instanceof HubNullItem)) {
            return null;
        }
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return null;
        }
        return HubNullSnapshot.resolveOpenableDock(server, HubNullData.get(hubStack), new HubNullStationRef(remoteDimension, dockPos, ""));
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
        int playerEnd = playerInventorySlotStartIndex + getPlayerSlotCount();

        if (index < playerInventorySlotStartIndex) {
            if (!moveItemStackTo(stackInSlot, playerInventorySlotStartIndex, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
            finishQuickMove(player, slot, stackInSlot, original);
            saveContainersToStack(player);
            return original;
        }

        if (viewMode == ViewMode.UPGRADES && tryInstallUpgrade(stackInSlot)) {
            stackInSlot.shrink(1);
            finishQuickMove(player, slot, stackInSlot, original);
            saveContainersToStack(player);
            return original;
        }

        if (viewMode == ViewMode.SETTINGS && tryMoveIntoSettings(stackInSlot)) {
            finishQuickMove(player, slot, stackInSlot, original);
            saveContainersToStack(player);
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
        if (viewMode != ViewMode.GRID) {
            saveContainersToStack(player);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (!player.isAlive()) {
            return false;
        }
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DenNullItem)) {
            return false;
        }
        return sourceType != SourceType.DOCK || dockPos == null || player.distanceToSqr(dockPos.getCenter()) <= 64.0D;
    }

    private int addUpgradeSlots() {
        if (viewMode != ViewMode.UPGRADES) {
            return 0;
        }
        for (DenNullUpgradeType type : DenNullUpgradeType.values()) {
            addSlot(new DenUpgradeSlot(upgradeContainer, type, 14 + type.slot() * SLOT_SPACING, 35));
        }
        return UPGRADE_SLOT_COUNT;
    }

    private int addBreedingSlots() {
        if (viewMode != ViewMode.SETTINGS) {
            return 0;
        }
        for (int slot = 0; slot < DenNullUpgradeData.BREEDING_BUFFER_SLOTS; slot++) {
            addSlot(new BreedingBufferSlot(breedingContainer, slot, 14 + slot * SLOT_SPACING, 61, breedingLimit()));
        }
        return DenNullUpgradeData.BREEDING_BUFFER_SLOTS;
    }

    private int addMilkSlot() {
        if (viewMode != ViewMode.SETTINGS) {
            return -1;
        }
        int index = slots.size();
        addSlot(new MilkSlot(milkContainer, 0, 14, 33));
        return index;
    }

    private int addNameTagSlot() {
        if (viewMode != ViewMode.SETTINGS) {
            return -1;
        }
        int index = slots.size();
        addSlot(new NameTagSlot(nameTagContainer, 0, 38, 33));
        return index;
    }

    private void addPlayerInventorySlots(Inventory playerInventory, int startY) {
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int column = 0; column < PLAYER_INV_COLUMNS; column++) {
                int index = column + row * PLAYER_INV_COLUMNS + 9;
                addSlot(createPlayerSlot(playerInventory, index, 14 + column * SLOT_SPACING, startY + row * SLOT_SPACING));
            }
        }
        int hotbarY = startY + 67;
        for (int column = 0; column < PLAYER_INV_COLUMNS; column++) {
            addSlot(createPlayerSlot(playerInventory, column, 14 + column * SLOT_SPACING, hotbarY));
        }
    }

    private Slot createPlayerSlot(Inventory inventory, int slotIndex, int x, int y) {
        if ((sourceType == SourceType.ITEM && slotIndex == inventorySlot)
                || (sourceType == SourceType.REMOTE_DOCK && slotIndex == hubInventorySlot)) {
            return new LockedPlayerSlot(inventory, slotIndex, x, y);
        }
        return new Slot(inventory, slotIndex, x, y);
    }

    private boolean tryInstallUpgrade(ItemStack stack) {
        if (!(stack.getItem() instanceof DenNullUpgradeItem upgradeItem)) {
            return false;
        }
        int slot = upgradeItem.type().slot();
        if (!upgradeContainer.getItem(slot).isEmpty()) {
            return false;
        }
        upgradeContainer.setItem(slot, new ItemStack(stack.getItem()));
        return true;
    }

    private boolean tryMoveIntoSettings(ItemStack stack) {
        if (stack.is(Items.MILK_BUCKET)) {
            return mergeIntoSingleSlot(milkContainer, 0, stack, DenNullUpgradeData.MILK_BUCKET_CAPACITY);
        }
        if (stack.is(Items.NAME_TAG)) {
            return mergeIntoSingleSlot(nameTagContainer, 0, stack, 64);
        }
        return mergeIntoBreedingBuffer(stack);
    }

    private boolean mergeIntoSingleSlot(SimpleContainer container, int slot, ItemStack source, int limit) {
        ItemStack existing = container.getItem(slot);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, source)) {
            return false;
        }
        int current = existing.isEmpty() ? 0 : existing.getCount();
        int moved = Math.min(source.getCount(), limit - current);
        if (moved <= 0) {
            return false;
        }
        container.setItem(slot, source.copyWithCount(current + moved));
        source.shrink(moved);
        return true;
    }

    private boolean mergeIntoBreedingBuffer(ItemStack source) {
        if (source.isEmpty() || source.getItem() instanceof DockableNullItem || source.getItem() instanceof DenNullUpgradeItem) {
            return false;
        }
        int limit = breedingLimit();
        for (int slot = 0; slot < breedingContainer.getContainerSize(); slot++) {
            ItemStack existing = breedingContainer.getItem(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, source)) {
                int moved = Math.min(source.getCount(), limit - existing.getCount());
                if (moved <= 0) {
                    return false;
                }
                existing.grow(moved);
                source.shrink(moved);
                return true;
            }
        }
        for (int slot = 0; slot < breedingContainer.getContainerSize(); slot++) {
            if (breedingContainer.getItem(slot).isEmpty()) {
                int moved = Math.min(source.getCount(), limit);
                breedingContainer.setItem(slot, source.copyWithCount(moved));
                source.shrink(moved);
                return true;
            }
        }
        return false;
    }

    private void refreshContainersFromData() {
        upgradeContainer.clearContent();
        for (DenNullUpgradeType type : data.upgrades().installed()) {
            upgradeContainer.setItem(type.slot(), new ItemStack(itemFor(type)));
        }
        breedingContainer.clearContent();
        List<DenNullBufferEntry> breedingItems = data.upgrades().breedingItems();
        for (int i = 0; i < Math.min(breedingItems.size(), breedingContainer.getContainerSize()); i++) {
            breedingContainer.setItem(i, breedingItems.get(i).stack());
        }
        milkContainer.setItem(0, data.upgrades().milkBuckets() <= 0 ? ItemStack.EMPTY : new ItemStack(Items.MILK_BUCKET, data.upgrades().milkBuckets()));
        nameTagContainer.setItem(0, data.upgrades().nameTags() <= 0 ? ItemStack.EMPTY : new ItemStack(Items.NAME_TAG, data.upgrades().nameTags()));
    }

    private void saveContainersToStack(Player player) {
        ItemStack stack = resolveStack(player);
        if (!(stack.getItem() instanceof DenNullItem)) {
            return;
        }
        DenNullData current = DenNullData.get(stack);
        DenNullUpgradeData old = current.upgrades();
        EnumSet<DenNullUpgradeType> installed = old.installed().isEmpty()
                ? EnumSet.noneOf(DenNullUpgradeType.class)
                : EnumSet.copyOf(old.installed());
        if (viewMode == ViewMode.UPGRADES) {
            for (DenNullUpgradeType type : DenNullUpgradeType.values()) {
                if (!upgradeContainer.getItem(type.slot()).isEmpty()) {
                    installed.add(type);
                }
            }
        }
        List<DenNullBufferEntry> breedingItems = old.breedingItems();
        int milkBuckets = old.milkBuckets();
        int nameTags = old.nameTags();
        if (viewMode == ViewMode.SETTINGS) {
            breedingItems = new ArrayList<>();
            for (int slot = 0; slot < breedingContainer.getContainerSize(); slot++) {
                ItemStack bufferStack = breedingContainer.getItem(slot);
                if (!bufferStack.isEmpty()) {
                    breedingItems.add(DenNullBufferEntry.fromStack(bufferStack));
                }
            }
            milkBuckets = milkContainer.getItem(0).is(Items.MILK_BUCKET) ? milkContainer.getItem(0).getCount() : 0;
            nameTags = nameTagContainer.getItem(0).is(Items.NAME_TAG) ? nameTagContainer.getItem(0).getCount() : 0;
        }
        DenNullUpgradeData updatedUpgrades = new DenNullUpgradeData(installed, breedingItems, milkBuckets, nameTags,
                old.dyeColorId(), old.babyEnabled(), old.tagTemplate(), old.tagCounter(), old.captureEnabled(),
                old.captureFilterMode(), old.captureFilter(), old.lootItems(), old.farmThresholds(), old.lastBreedingTick(),
                old.lastMilkTick(), old.lastShearTick(), old.lastCaptureTick(), old.lastFarmTick());
        DenNullData updated = current.withUpgrades(updatedUpgrades);
        DenNullData.set(stack, updated);
        updateData(updated);
        markChanged(player, stack);
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

    private int getPlayerSlotCount() {
        return viewMode == ViewMode.UPGRADES ? 36 : 0;
    }

    private int breedingLimit() {
        if (tier.creative()) {
            return Integer.MAX_VALUE;
        }
        return Math.max(16, Math.min(512, tier.ordinalId() <= 0 ? 16 : 16 << tier.ordinalId()));
    }

    private static net.minecraft.world.item.Item itemFor(DenNullUpgradeType type) {
        return switch (type) {
            case BREEDING -> ModItems.DEN_BREEDING_UPGRADE.get();
            case CLONE -> ModItems.DEN_CLONE_UPGRADE.get();
            case DYE -> ModItems.DEN_DYE_UPGRADE.get();
            case MILK -> ModItems.DEN_MILK_UPGRADE.get();
            case SHEAR -> ModItems.DEN_SHEAR_UPGRADE.get();
            case BABY -> ModItems.DEN_BABY_UPGRADE.get();
            case TAG -> ModItems.DEN_TAG_UPGRADE.get();
            case CAPTURE -> ModItems.DEN_CAPTURE_UPGRADE.get();
            case SPAWNER -> ModItems.DEN_SPAWNER_UPGRADE.get();
            case FARM -> ModItems.DEN_FARM_UPGRADE.get();
        };
    }

    private void markChanged(Player player, ItemStack stack) {
        player.getInventory().setChanged();
        if (sourceType != SourceType.ITEM) {
            DeepNullDockBlockEntity dock = resolveDock(player);
            if (dock != null) {
                dock.markStoredDeepNullChanged();
            }
        }
        broadcastChanges();
    }

    private interface UpgradeMutation {
        DenNullUpgradeData apply(DenNullUpgradeData upgrades);
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

    public static final class DenUpgradeSlot extends Slot {
        private final DenNullUpgradeType type;

        private DenUpgradeSlot(SimpleContainer container, DenNullUpgradeType type, int x, int y) {
            super(container, type.slot(), x, y);
            this.type = type;
        }

        public DenNullUpgradeType getType() {
            return type;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof DenNullUpgradeItem upgradeItem && upgradeItem.type() == type && !hasItem();
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

    private static final class BreedingBufferSlot extends Slot {
        private final int limit;

        private BreedingBufferSlot(SimpleContainer container, int slot, int x, int y, int limit) {
            super(container, slot, x, y);
            this.limit = limit;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !stack.isEmpty() && !(stack.getItem() instanceof DockableNullItem) && !(stack.getItem() instanceof DenNullUpgradeItem);
        }

        @Override
        public int getMaxStackSize() {
            return limit;
        }
    }

    private static final class MilkSlot extends Slot {
        private MilkSlot(SimpleContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(Items.MILK_BUCKET);
        }

        @Override
        public int getMaxStackSize() {
            return DenNullUpgradeData.MILK_BUCKET_CAPACITY;
        }
    }

    private static final class NameTagSlot extends Slot {
        private NameTagSlot(SimpleContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(Items.NAME_TAG);
        }
    }
}
