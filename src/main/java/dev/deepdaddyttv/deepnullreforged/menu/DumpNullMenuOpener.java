package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullCatalog;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullData;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatusSummary;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullMobOption;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullPresetCatalog;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullPresetOption;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullPresetTarget;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationRef;
import dev.deepdaddyttv.deepnullreforged.item.DumpNullItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class DumpNullMenuOpener {
    private DumpNullMenuOpener() {
    }

    public static void openHeldItem(ServerPlayer player, Inventory inventory, int inventorySlot) {
        openHeldItem(player, inventory, inventorySlot, DumpNullMenu.ViewMode.CATALOG);
    }

    public static void openHeldItem(ServerPlayer player, Inventory inventory, int inventorySlot, DumpNullMenu.ViewMode viewMode) {
        if (inventorySlot < 0 || inventorySlot >= inventory.getContainerSize()) {
            return;
        }
        ItemStack stack = inventory.getItem(inventorySlot);
        if (!(stack.getItem() instanceof DumpNullItem)) {
            return;
        }
        DumpNullData data = DumpNullData.get(stack);
        List<DumpNullMobOption> mobOptions = DumpNullCatalog.mobOptions(data);
        List<DumpNullPresetOption> presetOptions = DumpNullPresetCatalog.presetOptions();
        List<DumpNullPresetTarget> biomeTargets = presetBiomeTargets(player);
        List<DumpNullPresetTarget> dimensionTargets = presetDimensionTargets(player);
        List<DumpNullItemStatusSummary> itemStatuses = data.itemStatusSummaries();
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DumpNullMenu.forItem(containerId, playerInventory, inventorySlot, viewMode),
                stack.getHoverName()
        ), buffer -> DumpNullMenu.writeState(buffer, DumpNullMenu.SourceType.ITEM, viewMode, inventorySlot, player.blockPosition(), ResourceLocation.withDefaultNamespace("overworld"), -1, stack.copy(), mobOptions, presetOptions, biomeTargets, dimensionTargets, itemStatuses));
    }

    public static void openDock(ServerPlayer player, DeepNullDockBlockEntity dock) {
        openDock(player, dock, DumpNullMenu.ViewMode.CATALOG);
    }

    public static void openDock(ServerPlayer player, DeepNullDockBlockEntity dock, DumpNullMenu.ViewMode viewMode) {
        ItemStack stack = dock.getStoredDeepNull();
        if (!(stack.getItem() instanceof DumpNullItem)) {
            return;
        }
        DumpNullData data = DumpNullData.get(stack);
        List<DumpNullMobOption> mobOptions = DumpNullCatalog.mobOptions(data);
        List<DumpNullPresetOption> presetOptions = DumpNullPresetCatalog.presetOptions();
        List<DumpNullPresetTarget> biomeTargets = presetBiomeTargets(player);
        List<DumpNullPresetTarget> dimensionTargets = presetDimensionTargets(player);
        List<DumpNullItemStatusSummary> itemStatuses = data.itemStatusSummaries();
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DumpNullMenu.forDock(containerId, playerInventory, dock, viewMode),
                Component.translatable("item." + DeepNullReforged.MODID + ".dump_null")
        ), buffer -> DumpNullMenu.writeState(buffer, DumpNullMenu.SourceType.DOCK, viewMode, -1, dock.getBlockPos(), ResourceLocation.withDefaultNamespace("overworld"), -1, stack.copy(), mobOptions, presetOptions, biomeTargets, dimensionTargets, itemStatuses));
    }

    public static void openRemoteDock(ServerPlayer player, int hubInventorySlot, HubNullStationRef ref, DeepNullDockBlockEntity dock) {
        openRemoteDock(player, hubInventorySlot, ref, dock, DumpNullMenu.ViewMode.CATALOG);
    }

    public static void openRemoteDock(ServerPlayer player, int hubInventorySlot, HubNullStationRef ref, DeepNullDockBlockEntity dock, DumpNullMenu.ViewMode viewMode) {
        ItemStack stack = dock.getStoredDeepNull();
        if (!(stack.getItem() instanceof DumpNullItem)) {
            return;
        }
        DumpNullData data = DumpNullData.get(stack);
        List<DumpNullMobOption> mobOptions = DumpNullCatalog.mobOptions(data);
        List<DumpNullPresetOption> presetOptions = DumpNullPresetCatalog.presetOptions();
        List<DumpNullPresetTarget> biomeTargets = presetBiomeTargets(player);
        List<DumpNullPresetTarget> dimensionTargets = presetDimensionTargets(player);
        List<DumpNullItemStatusSummary> itemStatuses = data.itemStatusSummaries();
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, livingPlayer) -> DumpNullMenu.forRemoteDock(containerId, playerInventory, dock, ref.dimension(), hubInventorySlot, viewMode),
                Component.translatable("item." + DeepNullReforged.MODID + ".dump_null")
        ), buffer -> DumpNullMenu.writeState(buffer, DumpNullMenu.SourceType.REMOTE_DOCK, viewMode, -1, dock.getBlockPos(), ref.dimension(), hubInventorySlot, stack.copy(), mobOptions, presetOptions, biomeTargets, dimensionTargets, itemStatuses));
    }

    public static void reopen(ServerPlayer player, DumpNullMenu menu) {
        reopen(player, menu, menu.getViewMode());
    }

    public static void reopen(ServerPlayer player, DumpNullMenu menu, DumpNullMenu.ViewMode viewMode) {
        if (menu.getSourceType() == DumpNullMenu.SourceType.ITEM) {
            openHeldItem(player, player.getInventory(), menu.getInventorySlot(), viewMode);
            return;
        }
        if (menu.getSourceType() == DumpNullMenu.SourceType.REMOTE_DOCK) {
            DeepNullDockBlockEntity dock = menu.resolveDock(player);
            if (dock != null) {
                openRemoteDock(player, menu.getHubInventorySlot(), new HubNullStationRef(menu.getRemoteDimension(), menu.getDockPos(), ""), dock, viewMode);
            }
            return;
        }
        DeepNullDockBlockEntity dock = menu.resolveDock(player);
        if (dock != null) {
            openDock(player, dock, viewMode);
        }
    }

    private static List<DumpNullPresetTarget> presetBiomeTargets(ServerPlayer player) {
        return player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel ? DumpNullPresetCatalog.biomeTargets(serverLevel) : List.of();
    }

    private static List<DumpNullPresetTarget> presetDimensionTargets(ServerPlayer player) {
        return player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel ? DumpNullPresetCatalog.dimensionTargets(serverLevel) : List.of();
    }
}
