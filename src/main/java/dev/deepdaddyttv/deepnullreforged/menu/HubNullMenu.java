package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullData;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullDampResourceSummary;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullDumpRuleSummary;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullResourceSummary;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullSnapshot;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationSnapshot;
import dev.deepdaddyttv.deepnullreforged.item.HubNullItem;
import dev.deepdaddyttv.deepnullreforged.network.HubNullPayloads;
import dev.deepdaddyttv.deepnullreforged.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class HubNullMenu extends AbstractContainerMenu {
    private final Inventory playerInventory;
    private final int inventorySlot;
    private List<HubNullStationSnapshot> stationSnapshots;
    private List<HubNullResourceSummary> resourceSummaries;
    private List<HubNullDampResourceSummary> dampResourceSummaries;
    private List<HubNullDumpRuleSummary> dumpRuleSummaries;
    private List<HubNullResourceSummary> denResourceSummaries;
    private long lastSnapshotHash = Long.MIN_VALUE;
    private long lastRefreshGameTime = Long.MIN_VALUE;

    public static HubNullMenu forItem(int containerId, Inventory playerInventory, int inventorySlot, HubNullSnapshot snapshot) {
        return new HubNullMenu(containerId, playerInventory, inventorySlot, snapshot.stations(), snapshot.resources(), snapshot.dampResources(), snapshot.dumpRules(), snapshot.denResources());
    }

    public HubNullMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(
                containerId,
                playerInventory,
                buffer.readVarInt(),
                HubNullStationSnapshot.readList(buffer),
                HubNullResourceSummary.readList(buffer),
                HubNullDampResourceSummary.readList(buffer),
                HubNullDumpRuleSummary.readList(buffer),
                HubNullResourceSummary.readList(buffer)
        );
    }

    private HubNullMenu(
            int containerId,
            Inventory playerInventory,
            int inventorySlot,
            List<HubNullStationSnapshot> stationSnapshots,
            List<HubNullResourceSummary> resourceSummaries,
            List<HubNullDampResourceSummary> dampResourceSummaries,
            List<HubNullDumpRuleSummary> dumpRuleSummaries,
            List<HubNullResourceSummary> denResourceSummaries
    ) {
        super(ModMenus.HUB_NULL_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.inventorySlot = inventorySlot;
        this.stationSnapshots = List.copyOf(stationSnapshots == null ? List.of() : stationSnapshots);
        this.resourceSummaries = List.copyOf(resourceSummaries == null ? List.of() : resourceSummaries);
        this.dampResourceSummaries = List.copyOf(dampResourceSummaries == null ? List.of() : dampResourceSummaries);
        this.dumpRuleSummaries = List.copyOf(dumpRuleSummaries == null ? List.of() : dumpRuleSummaries);
        this.denResourceSummaries = List.copyOf(denResourceSummaries == null ? List.of() : denResourceSummaries);
    }

    public static void writeState(RegistryFriendlyByteBuf buffer, int inventorySlot, HubNullSnapshot snapshot) {
        buffer.writeVarInt(inventorySlot);
        HubNullStationSnapshot.writeList(buffer, snapshot.stations());
        HubNullResourceSummary.writeList(buffer, snapshot.resources());
        HubNullDampResourceSummary.writeList(buffer, snapshot.dampResources());
        HubNullDumpRuleSummary.writeList(buffer, snapshot.dumpRules());
        HubNullResourceSummary.writeList(buffer, snapshot.denResources());
    }

    public int getInventorySlot() {
        return inventorySlot;
    }

    public List<HubNullStationSnapshot> getStationSnapshots() {
        return stationSnapshots;
    }

    public List<HubNullResourceSummary> getResourceSummaries() {
        return resourceSummaries;
    }

    public List<HubNullDampResourceSummary> getDampResourceSummaries() {
        return dampResourceSummaries;
    }

    public List<HubNullDumpRuleSummary> getDumpRuleSummaries() {
        return dumpRuleSummaries;
    }

    public List<HubNullResourceSummary> getDenResourceSummaries() {
        return denResourceSummaries;
    }

    public void updateState(
            List<HubNullStationSnapshot> stations,
            List<HubNullResourceSummary> resources,
            List<HubNullDampResourceSummary> dampResources,
            List<HubNullDumpRuleSummary> dumpRules,
            List<HubNullResourceSummary> denResources
    ) {
        this.stationSnapshots = List.copyOf(stations == null ? List.of() : stations);
        this.resourceSummaries = List.copyOf(resources == null ? List.of() : resources);
        this.dampResourceSummaries = List.copyOf(dampResources == null ? List.of() : dampResources);
        this.dumpRuleSummaries = List.copyOf(dumpRules == null ? List.of() : dumpRules);
        this.denResourceSummaries = List.copyOf(denResources == null ? List.of() : denResources);
    }

    public ItemStack resolveStack(Player player) {
        if (inventorySlot < 0 || inventorySlot >= player.getInventory().getContainerSize()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = player.getInventory().getItem(inventorySlot);
        return stack.getItem() instanceof HubNullItem ? stack : ItemStack.EMPTY;
    }

    public HubNullData resolveData(Player player) {
        ItemStack stack = resolveStack(player);
        return stack.isEmpty() ? HubNullData.EMPTY : HubNullData.get(stack);
    }

    public void refreshState(ServerPlayer player, boolean force) {
        long gameTime = player.level().getGameTime();
        if (!force && lastRefreshGameTime != Long.MIN_VALUE && gameTime - lastRefreshGameTime < 20L) {
            return;
        }
        lastRefreshGameTime = gameTime;
        HubNullSnapshot snapshot = HubNullSnapshot.build(player.server, resolveData(player));
        long hash = snapshot.stableHash();
        if (force || hash != lastSnapshotHash) {
            lastSnapshotHash = hash;
            updateState(snapshot.stations(), snapshot.resources(), snapshot.dampResources(), snapshot.dumpRules(), snapshot.denResources());
            HubNullPayloads.sendState(player, this);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return !resolveStack(player).isEmpty();
    }
}
