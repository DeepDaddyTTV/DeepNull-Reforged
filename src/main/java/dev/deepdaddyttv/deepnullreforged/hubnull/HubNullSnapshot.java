package dev.deepdaddyttv.deepnullreforged.hubnull;

import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullData;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatus;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatusSummary;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullRule;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullData;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullEntry;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullSpawnerEntry;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DenNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DockableNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DumpNullItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record HubNullSnapshot(
        List<HubNullStationSnapshot> stations,
        List<HubNullResourceSummary> resources,
        List<HubNullDampResourceSummary> dampResources,
        List<HubNullDumpRuleSummary> dumpRules,
        List<HubNullResourceSummary> denResources
) {
    public HubNullSnapshot(List<HubNullStationSnapshot> stations, List<HubNullResourceSummary> resources) {
        this(stations, resources, List.of(), List.of(), List.of());
    }

    public HubNullSnapshot {
        stations = List.copyOf(stations == null ? List.of() : stations);
        resources = List.copyOf(resources == null ? List.of() : resources);
        dampResources = List.copyOf(dampResources == null ? List.of() : dampResources);
        dumpRules = List.copyOf(dumpRules == null ? List.of() : dumpRules);
        denResources = List.copyOf(denResources == null ? List.of() : denResources);
    }

    public static HubNullSnapshot build(MinecraftServer server, HubNullData data) {
        if (server == null || data == null || data.stations().isEmpty()) {
            return new HubNullSnapshot(List.of(), List.of(), List.of(), List.of(), List.of());
        }

        List<HubNullStationSnapshot> stationSnapshots = new ArrayList<>();
        Map<ResourceLocation, ResourceBucket> resources = new LinkedHashMap<>();
        Map<String, DampResourceBucket> dampResources = new LinkedHashMap<>();
        Map<String, DumpRuleBucket> dumpRules = new LinkedHashMap<>();
        Map<ResourceLocation, ResourceBucket> denResources = new LinkedHashMap<>();
        for (HubNullStationRef ref : data.stations()) {
            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, ref.dimension()));
            if (level == null || !level.hasChunkAt(ref.pos())) {
                stationSnapshots.add(new HubNullStationSnapshot(ref, HubNullStationStatus.UNLOADED, "", 0));
                continue;
            }
            if (!(level.getBlockEntity(ref.pos()) instanceof DeepNullDockBlockEntity dock)) {
                stationSnapshots.add(new HubNullStationSnapshot(ref, HubNullStationStatus.MISSING, "", 0));
                continue;
            }
            ItemStack stored = dock.getStoredDeepNull();
            if (stored.isEmpty()) {
                stationSnapshots.add(new HubNullStationSnapshot(ref, HubNullStationStatus.EMPTY, "", 0));
                continue;
            }
            if (stored.getItem() instanceof DenNullItem denNullItem) {
                DenNullData denData = DenNullData.get(stored);
                addDenResources(ref, denData, denResources);
                stationSnapshots.add(onlineDenStation(ref, denNullItem, stored, denData.totalCount()));
                continue;
            }
            if (!(stored.getItem() instanceof DeepNullItem deepNullItem)) {
                stationSnapshots.add(new HubNullStationSnapshot(ref, HubNullStationStatus.UNSUPPORTED, "", 0));
                continue;
            }

            if (stored.getItem() instanceof DumpNullItem) {
                DumpNullData dumpData = DumpNullData.get(stored);
                addDumpRules(ref, dumpData, dumpRules);
                stationSnapshots.add(onlineStation(ref, deepNullItem, stored, dumpData.itemStatusSummaries().size(), HubNullStationNullType.DUMP));
                continue;
            }

            DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), stored, level.registryAccess(), null);
            if (stored.getItem() instanceof DampNullItem || inventory.isFluidOnly()) {
                int occupied = addDampResources(ref, inventory, dampResources);
                stationSnapshots.add(onlineStation(ref, deepNullItem, stored, occupied, HubNullStationNullType.DAMP));
                continue;
            }

            int occupied = 0;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                occupied++;
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (itemId == null) {
                    continue;
                }
                resources.computeIfAbsent(itemId, ResourceBucket::new).add(ref, stack.getCount());
            }
            stationSnapshots.add(onlineStation(ref, deepNullItem, stored, occupied, HubNullStationNullType.DEEP));
        }

        List<HubNullResourceSummary> summaries = resources.values().stream()
                .map(ResourceBucket::toSummary)
                .sorted(Comparator
                        .comparingLong(HubNullResourceSummary::count).reversed()
                        .thenComparing(summary -> summary.itemId().toString()))
                .toList();
        List<HubNullDampResourceSummary> dampSummaries = dampResources.values().stream()
                .map(DampResourceBucket::toSummary)
                .sorted(Comparator
                        .comparingLong(HubNullDampResourceSummary::amount).reversed()
                        .thenComparing(summary -> summary.resourceId().toString()))
                .toList();
        List<HubNullDumpRuleSummary> dumpSummaries = dumpRules.values().stream()
                .map(DumpRuleBucket::toSummary)
                .sorted(Comparator
                        .comparing((HubNullDumpRuleSummary summary) -> summary.status().ordinal())
                        .thenComparing(summary -> summary.itemId().toString()))
                .toList();
        List<HubNullResourceSummary> denSummaries = denResources.values().stream()
                .map(ResourceBucket::toSummary)
                .sorted(Comparator
                        .comparingLong(HubNullResourceSummary::count).reversed()
                        .thenComparing(summary -> summary.itemId().toString()))
                .toList();
        return new HubNullSnapshot(stationSnapshots, summaries, dampSummaries, dumpSummaries, denSummaries);
    }

    public static boolean canRemoteOpen(MinecraftServer server, HubNullData data, HubNullStationRef ref) {
        return resolveOpenableDock(server, data, ref) != null;
    }

    public static DeepNullDockBlockEntity resolveOnlineDock(MinecraftServer server, HubNullData data, HubNullStationRef ref) {
        DeepNullDockBlockEntity dock = resolveRegisteredDock(server, data, ref);
        if (dock == null) {
            return null;
        }
        ItemStack stored = dock.getStoredDeepNull();
        if (!(stored.getItem() instanceof DeepNullItem) || stored.getItem() instanceof DumpNullItem) {
            return null;
        }
        return dock;
    }

    public static DeepNullDockBlockEntity resolveOnlineDumpDock(MinecraftServer server, HubNullData data, HubNullStationRef ref) {
        DeepNullDockBlockEntity dock = resolveRegisteredDock(server, data, ref);
        if (dock == null || !(dock.getStoredDeepNull().getItem() instanceof DumpNullItem)) {
            return null;
        }
        return dock;
    }

    public static DeepNullDockBlockEntity resolveOpenableDock(MinecraftServer server, HubNullData data, HubNullStationRef ref) {
        DeepNullDockBlockEntity dock = resolveRegisteredDock(server, data, ref);
        if (dock == null || !(dock.getStoredDeepNull().getItem() instanceof DockableNullItem)) {
            return null;
        }
        return dock;
    }

    private static DeepNullDockBlockEntity resolveRegisteredDock(MinecraftServer server, HubNullData data, HubNullStationRef ref) {
        if (server == null || data == null || ref == null || !data.contains(ref)) {
            return null;
        }
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, ref.dimension()));
        if (level == null || !level.hasChunkAt(ref.pos()) || !(level.getBlockEntity(ref.pos()) instanceof DeepNullDockBlockEntity dock)) {
            return null;
        }
        return dock;
    }

    public long stableHash() {
        long hash = 1125899906842597L;
        for (HubNullStationSnapshot station : stations) {
            hash = 31L * hash + station.ref().hashCode();
            hash = 31L * hash + station.status().ordinal();
            hash = 31L * hash + station.occupiedSlots();
            hash = 31L * hash + station.tierName().hashCode();
            hash = 31L * hash + station.nullType().ordinal();
            hash = 31L * hash + station.displayName().hashCode();
            hash = 31L * hash + station.accentColor();
            hash = 31L * hash + (station.previewStack() == null ? 0 : ItemStack.hashItemAndComponents(station.previewStack()));
        }
        for (HubNullResourceSummary resource : resources) {
            hash = 31L * hash + resource.itemId().hashCode();
            hash = 31L * hash + Long.hashCode(resource.count());
        }
        for (HubNullDampResourceSummary resource : dampResources) {
            hash = 31L * hash + resource.resourceId().hashCode();
            hash = 31L * hash + Long.hashCode(resource.amount());
        }
        for (HubNullDumpRuleSummary rule : dumpRules) {
            hash = 31L * hash + rule.itemId().hashCode();
            hash = 31L * hash + rule.status().ordinal();
            hash = 31L * hash + rule.stations().size();
        }
        for (HubNullResourceSummary resource : denResources) {
            hash = 31L * hash + resource.itemId().hashCode();
            hash = 31L * hash + Long.hashCode(resource.count());
        }
        return hash;
    }

    public int totalStations() {
        return stations.size();
    }

    public int countStatus(HubNullStationStatus status) {
        int count = 0;
        for (HubNullStationSnapshot station : stations) {
            if (station.status() == status) {
                count++;
            }
        }
        return count;
    }

    private static final class ResourceBucket {
        private final ResourceLocation itemId;
        private long total;
        private final Map<HubNullStationRef, Long> stations = new LinkedHashMap<>();

        private ResourceBucket(ResourceLocation itemId) {
            this.itemId = itemId;
        }

        private void add(HubNullStationRef station, long amount) {
            if (amount <= 0L) {
                return;
            }
            total += amount;
            stations.merge(station, amount, Long::sum);
        }

        private HubNullResourceSummary toSummary() {
            List<HubNullResourceSummary.StationAmount> stationAmounts = stations.entrySet().stream()
                    .map(entry -> new HubNullResourceSummary.StationAmount(entry.getKey(), entry.getValue()))
                    .sorted(Comparator.comparingLong(HubNullResourceSummary.StationAmount::count).reversed())
                    .toList();
            return new HubNullResourceSummary(itemId, total, stationAmounts);
        }
    }

    private static void addDenResources(HubNullStationRef ref, DenNullData data, Map<ResourceLocation, ResourceBucket> denResources) {
        for (DenNullEntry entry : data.entries()) {
            denResources.computeIfAbsent(entry.entityType(), ResourceBucket::new).add(ref, entry.count());
        }
        for (DenNullSpawnerEntry entry : data.spawners()) {
            denResources.computeIfAbsent(entry.entityType(), ResourceBucket::new).add(ref, entry.count());
        }
    }

    private static HubNullStationSnapshot onlineStation(HubNullStationRef ref, DeepNullItem item, ItemStack stack, int occupied, HubNullStationNullType nullType) {
        boolean fluidOnly = stack.getItem() instanceof DampNullItem || nullType == HubNullStationNullType.DAMP;
        int accentColor = DeepNullInventory.readStyleRenderData(stack, item.tier(), fluidOnly).glassColor() | 0xFF000000;
        return HubNullStationSnapshot.of(
                ref,
                HubNullStationStatus.ONLINE,
                item.tier(),
                occupied,
                nullType,
                stack.getHoverName().getString(),
                accentColor,
                stack.copyWithCount(1)
        );
    }

    private static HubNullStationSnapshot onlineDenStation(HubNullStationRef ref, DenNullItem item, ItemStack stack, int occupied) {
        return HubNullStationSnapshot.of(
                ref,
                HubNullStationStatus.ONLINE,
                item.tier(),
                occupied,
                HubNullStationNullType.DEN,
                stack.getHoverName().getString(),
                0xFFA36AFF,
                stack.copyWithCount(1)
        );
    }

    private static int addDampResources(HubNullStationRef ref, DeepNullInventory inventory, Map<String, DampResourceBucket> resources) {
        int occupied = 0;
        long capacity = inventory.getFluidCapacity();
        for (int slot = 0; slot < inventory.getFluidSlotCount(); slot++) {
            FluidStack fluid = inventory.getFluidInSlot(slot);
            if (!fluid.isEmpty()) {
                occupied++;
                ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
                if (fluidId != null) {
                    String key = HubNullDampResourceKind.FLUID.name() + ":" + fluidId;
                    resources.computeIfAbsent(key, ignored -> DampResourceBucket.fluid(fluidId, fluid.getHoverName().getString()))
                            .add(ref, fluid.getAmount(), capacity);
                }
                continue;
            }
            StoredChemical chemical = inventory.getChemicalInSlot(slot);
            if (!chemical.isEmpty()) {
                occupied++;
                ResourceLocation chemicalId = chemical.chemicalLocation();
                if (chemicalId != null) {
                    String key = HubNullDampResourceKind.CHEMICAL.name() + ":" + chemicalId;
                    resources.computeIfAbsent(key, ignored -> DampResourceBucket.chemical(chemicalId, chemical.getHoverName().getString(), chemical.tint(), chemical.iconLocation()))
                            .add(ref, chemical.amount(), capacity);
                }
            }
        }
        return occupied;
    }

    private static void addDumpRules(HubNullStationRef ref, DumpNullData data, Map<String, DumpRuleBucket> rules) {
        Map<ResourceLocation, List<String>> summariesByItem = new LinkedHashMap<>();
        for (DumpNullRule rule : data.rules()) {
            summariesByItem.computeIfAbsent(rule.itemId(), ignored -> new ArrayList<>()).add(rule.summary());
        }
        for (DumpNullItemStatusSummary summary : data.itemStatusSummaries()) {
            if (summary.status() == DumpNullItemStatus.NEUTRAL) {
                continue;
            }
            String key = summary.status().name() + ":" + summary.itemId();
            rules.computeIfAbsent(key, ignored -> new DumpRuleBucket(summary.itemId(), summary.status()))
                    .add(ref, summary.advanced(), summary.presetGenerated(), summariesByItem.getOrDefault(summary.itemId(), List.of()));
        }
    }

    private static final class DampResourceBucket {
        private final HubNullDampResourceKind kind;
        private final ResourceLocation resourceId;
        private final String displayName;
        private final int tint;
        private final ResourceLocation iconId;
        private long amount;
        private long capacity;
        private final Map<HubNullStationRef, StationFluidAmount> stations = new LinkedHashMap<>();

        private DampResourceBucket(HubNullDampResourceKind kind, ResourceLocation resourceId, String displayName, int tint, ResourceLocation iconId) {
            this.kind = kind;
            this.resourceId = resourceId;
            this.displayName = displayName;
            this.tint = tint;
            this.iconId = iconId;
        }

        private static DampResourceBucket fluid(ResourceLocation resourceId, String displayName) {
            return new DampResourceBucket(HubNullDampResourceKind.FLUID, resourceId, displayName, 0xFF3AA7FF, ResourceLocation.withDefaultNamespace("empty"));
        }

        private static DampResourceBucket chemical(ResourceLocation resourceId, String displayName, int tint, ResourceLocation iconId) {
            return new DampResourceBucket(HubNullDampResourceKind.CHEMICAL, resourceId, displayName, tint, iconId == null ? ResourceLocation.withDefaultNamespace("empty") : iconId);
        }

        private void add(HubNullStationRef station, long amount, long capacity) {
            if (amount <= 0L) {
                return;
            }
            this.amount += amount;
            this.capacity += Math.max(0L, capacity);
            stations.computeIfAbsent(station, ignored -> new StationFluidAmount()).add(amount, capacity);
        }

        private HubNullDampResourceSummary toSummary() {
            List<HubNullDampResourceSummary.StationAmount> stationAmounts = stations.entrySet().stream()
                    .map(entry -> new HubNullDampResourceSummary.StationAmount(entry.getKey(), entry.getValue().amount, entry.getValue().capacity))
                    .sorted(Comparator.comparingLong(HubNullDampResourceSummary.StationAmount::amount).reversed())
                    .toList();
            return new HubNullDampResourceSummary(kind, resourceId, displayName, amount, capacity, tint, iconId, stationAmounts);
        }
    }

    private static final class StationFluidAmount {
        private long amount;
        private long capacity;

        private void add(long amount, long capacity) {
            this.amount += amount;
            this.capacity += Math.max(0L, capacity);
        }
    }

    private static final class DumpRuleBucket {
        private final ResourceLocation itemId;
        private final DumpNullItemStatus status;
        private boolean advanced;
        private boolean presetGenerated;
        private final List<HubNullDumpRuleSummary.StationRules> stations = new ArrayList<>();

        private DumpRuleBucket(ResourceLocation itemId, DumpNullItemStatus status) {
            this.itemId = itemId;
            this.status = status;
        }

        private void add(HubNullStationRef station, boolean advanced, boolean presetGenerated, List<String> ruleSummaries) {
            this.advanced |= advanced;
            this.presetGenerated |= presetGenerated;
            stations.add(new HubNullDumpRuleSummary.StationRules(station, advanced, presetGenerated, ruleSummaries));
        }

        private HubNullDumpRuleSummary toSummary() {
            return new HubNullDumpRuleSummary(itemId, status, advanced, presetGenerated, stations.stream()
                    .sorted(Comparator.comparing(rule -> rule.ref().name()))
                    .toList());
        }
    }
}
