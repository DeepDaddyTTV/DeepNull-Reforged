package dev.deepdaddyttv.deepnullreforged.network;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullCatalog;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullData;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullDockDiscardMode;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullDropCandidate;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullHeldDiscardMode;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemCatalog;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemCatalogEntry;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemCatalogView;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatus;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatusSummary;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullMobDropRow;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullMobOption;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullPresetCatalog;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullPresetResult;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullRule;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullRuleAction;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullSideMode;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.menu.DumpNullMenu;
import dev.deepdaddyttv.deepnullreforged.menu.DumpNullMenuOpener;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class DumpNullPayloads {
    private static final int MAX_STRING_LIST_SIZE = 128;

    private DumpNullPayloads() {
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String path) {
        return new CustomPacketPayload.Type<>(DeepNullReforged.id(path));
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(MobDropsPayload.TYPE, MobDropsPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientMobDrops(payload)));
        registrar.playToClient(MobDropSearchPayload.TYPE, MobDropSearchPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientMobDropSearch(payload)));
        registrar.playToClient(DumpNullStatePayload.TYPE, DumpNullStatePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientState(payload)));
        registrar.playToClient(PresetPreviewPayload.TYPE, PresetPreviewPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientPresetPreview(payload)));
        registrar.playToClient(ItemCatalogPayload.TYPE, ItemCatalogPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> handleClientItemCatalog(payload)));
        registrar.playToServer(SetMobPayload.TYPE, SetMobPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleSetMob(payload, player);
                    }
                }));
        registrar.playToServer(ClearMobsPayload.TYPE, ClearMobsPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        updateData(player, DumpNullData::clearMobs);
                    }
                }));
        registrar.playToServer(AddRulePayload.TYPE, AddRulePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleAddRule(payload, player);
                    }
                }));
        registrar.playToServer(RemoveRulePayload.TYPE, RemoveRulePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        updateData(player, data -> data.removeRule(payload.index()));
                    }
                }));
        registrar.playToServer(MoveRulePayload.TYPE, MoveRulePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        updateData(player, data -> data.moveRule(payload.index(), payload.delta()));
                    }
                }));
        registrar.playToServer(RefreshPayload.TYPE, RefreshPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DumpNullMenu menu) {
                        DumpNullMenuOpener.reopen(player, menu);
                    }
                }));
        registrar.playToServer(OpenViewPayload.TYPE, OpenViewPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DumpNullMenu menu) {
                        handleOpenView(payload, player, menu);
                    }
                }));
        registrar.playToServer(ApplyPresetPayload.TYPE, ApplyPresetPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleApplyPreset(payload, player);
                    }
                }));
        registrar.playToServer(RequestPresetPreviewPayload.TYPE, RequestPresetPreviewPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleRequestPresetPreview(payload, player);
                    }
                }));
        registrar.playToServer(RequestMobDropsPayload.TYPE, RequestMobDropsPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleRequestMobDrops(payload, player);
                    }
                }));
        registrar.playToServer(SearchMobDropsPayload.TYPE, SearchMobDropsPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleSearchMobDrops(payload, player);
                    }
                }));
        registrar.playToServer(RequestItemCatalogPayload.TYPE, RequestItemCatalogPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleRequestItemCatalog(payload, player);
                    }
                }));
        registrar.playToServer(SetItemStatusPayload.TYPE, SetItemStatusPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleSetItemStatus(payload, player);
                    }
                }));
        registrar.playToServer(SetItemStatusesPayload.TYPE, SetItemStatusesPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleSetItemStatuses(payload, player);
                    }
                }));
        registrar.playToServer(SaveItemFilterPayload.TYPE, SaveItemFilterPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleSaveItemFilter(payload, player);
                    }
                }));
        registrar.playToServer(ClearItemFilterPayload.TYPE, ClearItemFilterPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleClearItemFilter(payload, player);
                    }
                }));
        registrar.playToServer(ClearItemFiltersPayload.TYPE, ClearItemFiltersPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleClearItemFilters(payload, player);
                    }
                }));
        registrar.playToServer(SetHeldDiscardModePayload.TYPE, SetHeldDiscardModePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        updateData(player, data -> data.withHeldDiscardMode(DumpNullHeldDiscardMode.byId(payload.mode())));
                    }
                }));
        registrar.playToServer(SetDockDiscardModePayload.TYPE, SetDockDiscardModePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        updateData(player, data -> data.withDockDiscardMode(DumpNullDockDiscardMode.byId(payload.mode())));
                    }
                }));
        registrar.playToServer(SetSideModePayload.TYPE, SetSideModePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        Direction direction = Direction.from3DDataValue(payload.direction());
                        updateData(player, data -> data.withSideMode(direction, DumpNullSideMode.byId(payload.mode())));
                    }
                }));
        registrar.playToServer(SetDiscardExportSidePayload.TYPE, SetDiscardExportSidePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        Direction direction = Direction.from3DDataValue(payload.direction());
                        updateData(player, data -> data.withDiscardExportSide(direction, payload.enabled()));
                    }
                }));
        registrar.playToServer(InstallPowerUpgradePayload.TYPE, InstallPowerUpgradePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleInstallPowerUpgrade(player);
                    }
                }));
    }

    private static void handleSetMob(SetMobPayload payload, ServerPlayer player) {
        ResourceLocation mobId = ResourceLocation.tryParse(payload.mobId());
        if (mobId == null) {
            return;
        }
        if (payload.selected() && !DumpNullCatalog.isRegisteredEntity(mobId)) {
            return;
        }
        updateData(player, data -> data.withMob(mobId, payload.selected()));
    }

    private static void handleOpenView(OpenViewPayload payload, ServerPlayer player, DumpNullMenu menu) {
        if (payload.viewMode() < 0 || payload.viewMode() >= DumpNullMenu.ViewMode.values().length) {
            return;
        }
        DumpNullMenuOpener.reopen(player, menu, DumpNullMenu.ViewMode.values()[payload.viewMode()]);
    }

    private static void handleAddRule(AddRulePayload payload, ServerPlayer player) {
        ResourceLocation itemId = ResourceLocation.tryParse(payload.itemId());
        if (itemId == null) {
            return;
        }
        DumpNullRule rule = DumpNullRule.basic(
                DumpNullRuleAction.byId(payload.action()),
                itemId,
                payload.minDurabilityPercent(),
                parseLocations(payload.requiredEnchantments()),
                parseLocations(payload.forbiddenEnchantments())
        );
        updateData(player, data -> data.addRule(rule));
    }

    private static void handleApplyPreset(ApplyPresetPayload payload, ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        List<ResourceLocation> targets = parseLocations(payload.targetIds());
        DumpNullPresetCatalog.build(serverLevel, payload.presetId(), targets).ifPresent(result -> {
            DumpNullPresetResult finalResult = payload.writeAcceptedRules()
                    ? DumpNullPresetCatalog.withAcceptedRules(result, acceptedPreviewItemIds(serverLevel, result))
                    : result;
            updateData(player, data -> data.applyPreset(finalResult));
        });
    }

    private static void handleRequestPresetPreview(RequestPresetPreviewPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof DumpNullMenu menu) || payload.containerId() != menu.containerId) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        List<ResourceLocation> targets = parseLocations(payload.targetIds());
        DumpNullPresetResult result = DumpNullPresetCatalog.build(serverLevel, payload.presetId(), targets).orElse(null);
        List<String> selectedMobs = result == null
                ? List.of()
                : result.selectedMobs().stream().map(ResourceLocation::toString).toList();
        List<DumpNullItemStatusSummary> presetRuleSummaries = result == null
                ? List.of()
                : new DumpNullData(List.of(), result.presetRules()).itemStatusSummaries();
        List<DumpNullItemStatusSummary> acceptedSummaries = result == null
                ? List.of()
                : acceptedPreviewItemIds(serverLevel, result).stream()
                .map(itemId -> new DumpNullItemStatusSummary(itemId, DumpNullItemStatus.ACCEPTED, false, false))
                .toList();
        PacketDistributor.sendToPlayer(player, new PresetPreviewPayload(
                menu.containerId,
                payload.presetId(),
                targets.stream().map(ResourceLocation::toString).toList(),
                selectedMobs,
                acceptedSummaries,
                presetRuleSummaries
        ));
    }

    private static void handleRequestMobDrops(RequestMobDropsPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof DumpNullMenu menu) || payload.containerId() != menu.containerId) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        List<DumpNullMobDropRow> rows = menu.loadDropRows(serverLevel, parseLocations(payload.mobIds()));
        PacketDistributor.sendToPlayer(player, new MobDropsPayload(menu.containerId, rows));
    }

    private static void handleSearchMobDrops(SearchMobDropsPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof DumpNullMenu menu) || payload.containerId() != menu.containerId) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        String query = payload.query().trim();
        if (query.length() < 2) {
            PacketDistributor.sendToPlayer(player, new MobDropSearchPayload(menu.containerId, query, List.of(), List.of()));
            return;
        }
        List<DumpNullMobDropRow> rows = menu.searchDropRows(serverLevel, query);
        PacketDistributor.sendToPlayer(player, new MobDropSearchPayload(
                menu.containerId,
                query,
                rows.stream().map(row -> row.entityId().toString()).toList(),
                rows
        ));
    }

    private static void handleRequestItemCatalog(RequestItemCatalogPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof DumpNullMenu menu) || payload.containerId() != menu.containerId) {
            return;
        }
        DumpNullItemCatalogView view = DumpNullItemCatalogView.byId(payload.viewId());
        PacketDistributor.sendToPlayer(player, new ItemCatalogPayload(
                menu.containerId,
                view.id(),
                DumpNullItemCatalog.build(view, player.getInventory())
        ));
    }

    private static void handleSetItemStatus(SetItemStatusPayload payload, ServerPlayer player) {
        ResourceLocation itemId = ResourceLocation.tryParse(payload.itemId());
        if (!isRegisteredItem(itemId)) {
            return;
        }
        DumpNullItemStatus status = DumpNullItemStatus.byId(payload.status());
        updateData(player, data -> data.withItemStatus(itemId, status));
    }

    private static void handleSetItemStatuses(SetItemStatusesPayload payload, ServerPlayer player) {
        DumpNullItemStatus status = DumpNullItemStatus.byId(payload.status());
        List<ResourceLocation> itemIds = new ArrayList<>();
        for (ResourceLocation itemId : parseLocations(payload.itemIds())) {
            if (isRegisteredItem(itemId) && !itemIds.contains(itemId)) {
                itemIds.add(itemId);
            }
            if (itemIds.size() >= MAX_STRING_LIST_SIZE) {
                break;
            }
        }
        if (!itemIds.isEmpty()) {
            updateData(player, data -> data.withItemStatuses(itemIds, status));
        }
    }

    private static void handleSaveItemFilter(SaveItemFilterPayload payload, ServerPlayer player) {
        ResourceLocation itemId = ResourceLocation.tryParse(payload.itemId());
        if (!isRegisteredItem(itemId)) {
            return;
        }
        DumpNullItemStatus status = DumpNullItemStatus.byId(payload.status());
        updateData(player, data -> data.withItemFilter(
                itemId,
                status,
                payload.minDurabilityPercent(),
                parseLocations(payload.requiredEnchantments()),
                parseLocations(payload.forbiddenEnchantments())
        ));
    }

    private static void handleClearItemFilter(ClearItemFilterPayload payload, ServerPlayer player) {
        ResourceLocation itemId = ResourceLocation.tryParse(payload.itemId());
        if (!isRegisteredItem(itemId)) {
            return;
        }
        updateData(player, data -> data.clearItemFilter(itemId));
    }

    private static void handleClearItemFilters(ClearItemFiltersPayload payload, ServerPlayer player) {
        List<ResourceLocation> itemIds = new ArrayList<>();
        for (ResourceLocation itemId : parseLocations(payload.itemIds())) {
            if (isRegisteredItem(itemId) && !itemIds.contains(itemId)) {
                itemIds.add(itemId);
            }
            if (itemIds.size() >= MAX_STRING_LIST_SIZE) {
                break;
            }
        }
        if (!itemIds.isEmpty()) {
            updateData(player, data -> data.clearItemFilters(itemIds));
        }
    }

    private static void handleInstallPowerUpgrade(ServerPlayer player) {
        if (!(player.containerMenu instanceof DumpNullMenu menu)) {
            return;
        }
        ItemStack stack = menu.resolveMutableStack(player);
        if (stack == null || stack.isEmpty()) {
            return;
        }
        DumpNullData data = DumpNullData.get(stack);
        if (data.hasUpgrade(DumpNullUpgradeType.POWER)) {
            sendStateUpdate(player, menu, data);
            return;
        }
        int upgradeSlot = findPowerUpgrade(player);
        if (upgradeSlot < 0) {
            return;
        }
        ItemStack upgradeStack = player.getInventory().getItem(upgradeSlot);
        upgradeStack.shrink(1);
        player.getInventory().setChanged();
        DumpNullData updated = data.withUpgrade(DumpNullUpgradeType.POWER);
        DumpNullData.set(stack, updated);
        if (menu.getSourceType() == DumpNullMenu.SourceType.ITEM) {
            player.getInventory().setChanged();
        } else {
            DeepNullDockBlockEntity dock = menu.resolveDock(player);
            if (dock != null) {
                dock.markStoredDeepNullChanged(true);
            }
        }
        sendStateUpdate(player, menu, updated);
    }

    private static int findPowerUpgrade(ServerPlayer player) {
        return -1;
    }

    private static boolean isRegisteredItem(ResourceLocation itemId) {
        return itemId != null && BuiltInRegistries.ITEM.containsKey(itemId);
    }

    private static List<ResourceLocation> acceptedPreviewItemIds(ServerLevel level, DumpNullPresetResult result) {
        Set<ResourceLocation> discarded = new LinkedHashSet<>();
        for (DumpNullRule rule : result.presetRules()) {
            if (rule.action() == DumpNullRuleAction.VOID) {
                discarded.add(rule.itemId());
            }
        }
        Set<ResourceLocation> accepted = new LinkedHashSet<>();
        for (DumpNullDropCandidate candidate : DumpNullDropCandidate.discover(level, result.selectedMobs())) {
            ResourceLocation itemId = candidate.itemId();
            if (isRegisteredItem(itemId) && !discarded.contains(itemId)) {
                accepted.add(itemId);
            }
        }
        return accepted.stream()
                .sorted((left, right) -> left.toString().compareTo(right.toString()))
                .toList();
    }

    private static void handleClientMobDrops(MobDropsPayload payload) {
        try {
            Class<?> handlerClass = Class.forName("dev.deepdaddyttv.deepnullreforged.client.DumpNullClientPayloadHandler");
            handlerClass.getMethod("handleMobDrops", MobDropsPayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | LinkageError exception) {
            DeepNullReforged.LOGGER.debug("Skipping DumpNull client drop payload handler", exception);
        }
    }

    private static void handleClientMobDropSearch(MobDropSearchPayload payload) {
        try {
            Class<?> handlerClass = Class.forName("dev.deepdaddyttv.deepnullreforged.client.DumpNullClientPayloadHandler");
            handlerClass.getMethod("handleMobDropSearch", MobDropSearchPayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | LinkageError exception) {
            DeepNullReforged.LOGGER.debug("Skipping DumpNull client drop search payload handler", exception);
        }
    }

    private static void handleClientState(DumpNullStatePayload payload) {
        try {
            Class<?> handlerClass = Class.forName("dev.deepdaddyttv.deepnullreforged.client.DumpNullClientPayloadHandler");
            handlerClass.getMethod("handleState", DumpNullStatePayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | LinkageError exception) {
            DeepNullReforged.LOGGER.debug("Skipping DumpNull client state payload handler", exception);
        }
    }

    private static void handleClientPresetPreview(PresetPreviewPayload payload) {
        try {
            Class<?> handlerClass = Class.forName("dev.deepdaddyttv.deepnullreforged.client.DumpNullClientPayloadHandler");
            handlerClass.getMethod("handlePresetPreview", PresetPreviewPayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | LinkageError exception) {
            DeepNullReforged.LOGGER.debug("Skipping DumpNull client preset preview payload handler", exception);
        }
    }

    private static void handleClientItemCatalog(ItemCatalogPayload payload) {
        try {
            Class<?> handlerClass = Class.forName("dev.deepdaddyttv.deepnullreforged.client.DumpNullClientPayloadHandler");
            handlerClass.getMethod("handleItemCatalog", ItemCatalogPayload.class).invoke(null, payload);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | LinkageError exception) {
            DeepNullReforged.LOGGER.debug("Skipping DumpNull client item catalog payload handler", exception);
        }
    }

    private static void updateData(ServerPlayer player, java.util.function.Function<DumpNullData, DumpNullData> updater) {
        if (!(player.containerMenu instanceof DumpNullMenu menu)) {
            return;
        }
        ItemStack stack = menu.resolveMutableStack(player);
        if (stack == null || stack.isEmpty()) {
            return;
        }
        DumpNullData updatedData = updater.apply(DumpNullData.get(stack));
        if (updatedData == null) {
            updatedData = DumpNullData.EMPTY;
        }
        DumpNullData.set(stack, updatedData);
        if (menu.getSourceType() == DumpNullMenu.SourceType.ITEM) {
            player.getInventory().setChanged();
        } else {
            DeepNullDockBlockEntity dock = menu.resolveDock(player);
            if (dock != null) {
                dock.markStoredDeepNullChanged();
            }
        }
        sendStateUpdate(player, menu, updatedData);
    }

    private static void sendStateUpdate(ServerPlayer player, DumpNullMenu menu, DumpNullData data) {
        List<DumpNullMobOption> mobOptions = DumpNullCatalog.mobOptions(data);
        List<DumpNullItemStatusSummary> itemStatuses = data.itemStatusSummaries();
        menu.updateState(data, mobOptions, itemStatuses);
        PacketDistributor.sendToPlayer(player, new DumpNullStatePayload(menu.containerId, data, mobOptions, itemStatuses));
    }

    private static List<ResourceLocation> parseLocations(List<String> rawIds) {
        List<ResourceLocation> result = new ArrayList<>();
        for (String rawId : rawIds) {
            ResourceLocation id = ResourceLocation.tryParse(rawId.trim());
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

    private static void writeStringList(RegistryFriendlyByteBuf buffer, List<String> values) {
        buffer.writeVarInt(Math.min(values.size(), MAX_STRING_LIST_SIZE));
        for (int i = 0; i < values.size() && i < MAX_STRING_LIST_SIZE; i++) {
            buffer.writeUtf(values.get(i));
        }
    }

    private static List<String> readStringList(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String value = buffer.readUtf();
            if (values.size() < MAX_STRING_LIST_SIZE) {
                values.add(value);
            }
        }
        return values;
    }

    public record SetMobPayload(String mobId, boolean selected) implements CustomPacketPayload {
        public static final Type<SetMobPayload> TYPE = payloadType("dump_null_set_mob");
        public static final StreamCodec<RegistryFriendlyByteBuf, SetMobPayload> STREAM_CODEC = StreamCodec.ofMember(SetMobPayload::encode, SetMobPayload::decode);

        private static SetMobPayload decode(RegistryFriendlyByteBuf buffer) {
            return new SetMobPayload(buffer.readUtf(), buffer.readBoolean());
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeUtf(mobId);
            buffer.writeBoolean(selected);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum ClearMobsPayload implements CustomPacketPayload {
        INSTANCE;

        public static final Type<ClearMobsPayload> TYPE = payloadType("dump_null_clear_mobs");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClearMobsPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record AddRulePayload(String itemId, int action, int minDurabilityPercent, List<String> requiredEnchantments, List<String> forbiddenEnchantments) implements CustomPacketPayload {
        public static final Type<AddRulePayload> TYPE = payloadType("dump_null_add_rule");
        public static final StreamCodec<RegistryFriendlyByteBuf, AddRulePayload> STREAM_CODEC = StreamCodec.ofMember(AddRulePayload::encode, AddRulePayload::decode);

        public AddRulePayload {
            minDurabilityPercent = Math.max(0, Math.min(100, minDurabilityPercent));
            requiredEnchantments = List.copyOf(requiredEnchantments == null ? List.of() : requiredEnchantments);
            forbiddenEnchantments = List.copyOf(forbiddenEnchantments == null ? List.of() : forbiddenEnchantments);
        }

        private static AddRulePayload decode(RegistryFriendlyByteBuf buffer) {
            return new AddRulePayload(
                    buffer.readUtf(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    readStringList(buffer),
                    readStringList(buffer)
            );
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeUtf(itemId);
            buffer.writeVarInt(action);
            buffer.writeVarInt(minDurabilityPercent);
            writeStringList(buffer, requiredEnchantments);
            writeStringList(buffer, forbiddenEnchantments);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RemoveRulePayload(int index) implements CustomPacketPayload {
        public static final Type<RemoveRulePayload> TYPE = payloadType("dump_null_remove_rule");
        public static final StreamCodec<RegistryFriendlyByteBuf, RemoveRulePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, RemoveRulePayload::index, RemoveRulePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MoveRulePayload(int index, int delta) implements CustomPacketPayload {
        public static final Type<MoveRulePayload> TYPE = payloadType("dump_null_move_rule");
        public static final StreamCodec<RegistryFriendlyByteBuf, MoveRulePayload> STREAM_CODEC = StreamCodec.ofMember(MoveRulePayload::encode, MoveRulePayload::decode);

        private static MoveRulePayload decode(RegistryFriendlyByteBuf buffer) {
            return new MoveRulePayload(buffer.readVarInt(), buffer.readVarInt());
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(index);
            buffer.writeVarInt(delta);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum RefreshPayload implements CustomPacketPayload {
        INSTANCE;

        public static final Type<RefreshPayload> TYPE = payloadType("dump_null_refresh");
        public static final StreamCodec<RegistryFriendlyByteBuf, RefreshPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record OpenViewPayload(int viewMode) implements CustomPacketPayload {
        public static final Type<OpenViewPayload> TYPE = payloadType("dump_null_open_view");
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenViewPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, OpenViewPayload::viewMode, OpenViewPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ApplyPresetPayload(String presetId, List<String> targetIds, boolean writeAcceptedRules) implements CustomPacketPayload {
        public static final Type<ApplyPresetPayload> TYPE = payloadType("dump_null_apply_preset");
        public static final StreamCodec<RegistryFriendlyByteBuf, ApplyPresetPayload> STREAM_CODEC = StreamCodec.ofMember(ApplyPresetPayload::encode, ApplyPresetPayload::decode);

        public ApplyPresetPayload {
            presetId = presetId == null ? "" : presetId;
            targetIds = List.copyOf(targetIds == null ? List.of() : targetIds);
        }

        private static ApplyPresetPayload decode(RegistryFriendlyByteBuf buffer) {
            return new ApplyPresetPayload(buffer.readUtf(), readStringList(buffer), buffer.readBoolean());
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeUtf(presetId);
            writeStringList(buffer, targetIds);
            buffer.writeBoolean(writeAcceptedRules);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestPresetPreviewPayload(int containerId, String presetId, List<String> targetIds) implements CustomPacketPayload {
        public static final Type<RequestPresetPreviewPayload> TYPE = payloadType("dump_null_request_preset_preview");
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestPresetPreviewPayload> STREAM_CODEC = StreamCodec.ofMember(RequestPresetPreviewPayload::encode, RequestPresetPreviewPayload::decode);

        public RequestPresetPreviewPayload {
            presetId = presetId == null ? "" : presetId;
            targetIds = List.copyOf(targetIds == null ? List.of() : targetIds);
        }

        private static RequestPresetPreviewPayload decode(RegistryFriendlyByteBuf buffer) {
            return new RequestPresetPreviewPayload(buffer.readVarInt(), buffer.readUtf(), readStringList(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            buffer.writeUtf(presetId);
            writeStringList(buffer, targetIds);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestMobDropsPayload(int containerId, List<String> mobIds) implements CustomPacketPayload {
        public static final Type<RequestMobDropsPayload> TYPE = payloadType("dump_null_request_mob_drops");
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestMobDropsPayload> STREAM_CODEC = StreamCodec.ofMember(RequestMobDropsPayload::encode, RequestMobDropsPayload::decode);

        public RequestMobDropsPayload {
            mobIds = List.copyOf(mobIds == null ? List.of() : mobIds);
        }

        private static RequestMobDropsPayload decode(RegistryFriendlyByteBuf buffer) {
            return new RequestMobDropsPayload(buffer.readVarInt(), readStringList(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            writeStringList(buffer, mobIds);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MobDropsPayload(int containerId, List<DumpNullMobDropRow> rows) implements CustomPacketPayload {
        public static final Type<MobDropsPayload> TYPE = payloadType("dump_null_mob_drops");
        public static final StreamCodec<RegistryFriendlyByteBuf, MobDropsPayload> STREAM_CODEC = StreamCodec.ofMember(MobDropsPayload::encode, MobDropsPayload::decode);

        public MobDropsPayload {
            rows = List.copyOf(rows == null ? List.of() : rows);
        }

        private static MobDropsPayload decode(RegistryFriendlyByteBuf buffer) {
            return new MobDropsPayload(buffer.readVarInt(), DumpNullMobDropRow.readList(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            DumpNullMobDropRow.writeList(buffer, rows);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SearchMobDropsPayload(int containerId, String query) implements CustomPacketPayload {
        public static final Type<SearchMobDropsPayload> TYPE = payloadType("dump_null_search_mob_drops");
        public static final StreamCodec<RegistryFriendlyByteBuf, SearchMobDropsPayload> STREAM_CODEC = StreamCodec.ofMember(SearchMobDropsPayload::encode, SearchMobDropsPayload::decode);

        public SearchMobDropsPayload {
            query = query == null ? "" : query;
        }

        private static SearchMobDropsPayload decode(RegistryFriendlyByteBuf buffer) {
            return new SearchMobDropsPayload(buffer.readVarInt(), buffer.readUtf(256));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            buffer.writeUtf(query, 256);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MobDropSearchPayload(int containerId, String query, List<String> matchingMobIds, List<DumpNullMobDropRow> rows) implements CustomPacketPayload {
        public static final Type<MobDropSearchPayload> TYPE = payloadType("dump_null_mob_drop_search");
        public static final StreamCodec<RegistryFriendlyByteBuf, MobDropSearchPayload> STREAM_CODEC = StreamCodec.ofMember(MobDropSearchPayload::encode, MobDropSearchPayload::decode);

        public MobDropSearchPayload {
            query = query == null ? "" : query;
            matchingMobIds = List.copyOf(matchingMobIds == null ? List.of() : matchingMobIds);
            rows = List.copyOf(rows == null ? List.of() : rows);
        }

        private static MobDropSearchPayload decode(RegistryFriendlyByteBuf buffer) {
            return new MobDropSearchPayload(
                    buffer.readVarInt(),
                    buffer.readUtf(256),
                    readStringList(buffer),
                    DumpNullMobDropRow.readList(buffer)
            );
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            buffer.writeUtf(query, 256);
            writeStringList(buffer, matchingMobIds);
            DumpNullMobDropRow.writeList(buffer, rows);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestItemCatalogPayload(int containerId, String viewId) implements CustomPacketPayload {
        public static final Type<RequestItemCatalogPayload> TYPE = payloadType("dump_null_request_item_catalog");
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestItemCatalogPayload> STREAM_CODEC = StreamCodec.ofMember(RequestItemCatalogPayload::encode, RequestItemCatalogPayload::decode);

        public RequestItemCatalogPayload {
            viewId = viewId == null ? "" : viewId;
        }

        private static RequestItemCatalogPayload decode(RegistryFriendlyByteBuf buffer) {
            return new RequestItemCatalogPayload(buffer.readVarInt(), buffer.readUtf(64));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            buffer.writeUtf(viewId, 64);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ItemCatalogPayload(int containerId, String viewId, List<DumpNullItemCatalogEntry> entries) implements CustomPacketPayload {
        public static final Type<ItemCatalogPayload> TYPE = payloadType("dump_null_item_catalog");
        public static final StreamCodec<RegistryFriendlyByteBuf, ItemCatalogPayload> STREAM_CODEC = StreamCodec.ofMember(ItemCatalogPayload::encode, ItemCatalogPayload::decode);

        public ItemCatalogPayload {
            viewId = viewId == null ? "" : viewId;
            entries = List.copyOf(entries == null ? List.of() : entries);
        }

        private static ItemCatalogPayload decode(RegistryFriendlyByteBuf buffer) {
            return new ItemCatalogPayload(buffer.readVarInt(), buffer.readUtf(64), DumpNullItemCatalogEntry.readList(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            buffer.writeUtf(viewId, 64);
            DumpNullItemCatalogEntry.writeList(buffer, entries);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DumpNullStatePayload(int containerId, DumpNullData data, List<DumpNullMobOption> mobOptions, List<DumpNullItemStatusSummary> itemStatuses) implements CustomPacketPayload {
        public static final Type<DumpNullStatePayload> TYPE = payloadType("dump_null_state");
        public static final StreamCodec<RegistryFriendlyByteBuf, DumpNullStatePayload> STREAM_CODEC = StreamCodec.ofMember(DumpNullStatePayload::encode, DumpNullStatePayload::decode);

        public DumpNullStatePayload {
            data = data == null ? DumpNullData.EMPTY : data;
            mobOptions = List.copyOf(mobOptions == null ? List.of() : mobOptions);
            itemStatuses = List.copyOf(itemStatuses == null ? List.of() : itemStatuses);
        }

        private static DumpNullStatePayload decode(RegistryFriendlyByteBuf buffer) {
            return new DumpNullStatePayload(
                    buffer.readVarInt(),
                    DumpNullData.read(buffer),
                    DumpNullMobOption.readList(buffer),
                    DumpNullItemStatusSummary.readList(buffer)
            );
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            DumpNullData.write(buffer, data);
            DumpNullMobOption.writeList(buffer, mobOptions);
            DumpNullItemStatusSummary.writeList(buffer, itemStatuses);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PresetPreviewPayload(
            int containerId,
            String presetId,
            List<String> targetIds,
            List<String> selectedMobs,
            List<DumpNullItemStatusSummary> acceptedSummaries,
            List<DumpNullItemStatusSummary> discardedSummaries
    ) implements CustomPacketPayload {
        public static final Type<PresetPreviewPayload> TYPE = payloadType("dump_null_preset_preview");
        public static final StreamCodec<RegistryFriendlyByteBuf, PresetPreviewPayload> STREAM_CODEC = StreamCodec.ofMember(PresetPreviewPayload::encode, PresetPreviewPayload::decode);

        public PresetPreviewPayload {
            presetId = presetId == null ? "" : presetId;
            targetIds = List.copyOf(targetIds == null ? List.of() : targetIds);
            selectedMobs = List.copyOf(selectedMobs == null ? List.of() : selectedMobs);
            acceptedSummaries = List.copyOf(acceptedSummaries == null ? List.of() : acceptedSummaries);
            discardedSummaries = List.copyOf(discardedSummaries == null ? List.of() : discardedSummaries);
        }

        private static PresetPreviewPayload decode(RegistryFriendlyByteBuf buffer) {
            return new PresetPreviewPayload(
                    buffer.readVarInt(),
                    buffer.readUtf(),
                    readStringList(buffer),
                    readStringList(buffer),
                    DumpNullItemStatusSummary.readList(buffer),
                    DumpNullItemStatusSummary.readList(buffer)
            );
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(containerId);
            buffer.writeUtf(presetId);
            writeStringList(buffer, targetIds);
            writeStringList(buffer, selectedMobs);
            DumpNullItemStatusSummary.writeList(buffer, acceptedSummaries);
            DumpNullItemStatusSummary.writeList(buffer, discardedSummaries);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetItemStatusPayload(String itemId, int status) implements CustomPacketPayload {
        public static final Type<SetItemStatusPayload> TYPE = payloadType("dump_null_set_item_status");
        public static final StreamCodec<RegistryFriendlyByteBuf, SetItemStatusPayload> STREAM_CODEC = StreamCodec.ofMember(SetItemStatusPayload::encode, SetItemStatusPayload::decode);

        public SetItemStatusPayload {
            itemId = itemId == null ? "" : itemId;
        }

        private static SetItemStatusPayload decode(RegistryFriendlyByteBuf buffer) {
            return new SetItemStatusPayload(buffer.readUtf(), buffer.readVarInt());
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeUtf(itemId);
            buffer.writeVarInt(status);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetItemStatusesPayload(List<String> itemIds, int status) implements CustomPacketPayload {
        public static final Type<SetItemStatusesPayload> TYPE = payloadType("dump_null_set_item_statuses");
        public static final StreamCodec<RegistryFriendlyByteBuf, SetItemStatusesPayload> STREAM_CODEC = StreamCodec.ofMember(SetItemStatusesPayload::encode, SetItemStatusesPayload::decode);

        public SetItemStatusesPayload {
            itemIds = List.copyOf(itemIds == null ? List.of() : itemIds);
        }

        private static SetItemStatusesPayload decode(RegistryFriendlyByteBuf buffer) {
            return new SetItemStatusesPayload(readStringList(buffer), buffer.readVarInt());
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            writeStringList(buffer, itemIds);
            buffer.writeVarInt(status);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SaveItemFilterPayload(
            String itemId,
            int status,
            int minDurabilityPercent,
            List<String> requiredEnchantments,
            List<String> forbiddenEnchantments
    ) implements CustomPacketPayload {
        public static final Type<SaveItemFilterPayload> TYPE = payloadType("dump_null_save_item_filter");
        public static final StreamCodec<RegistryFriendlyByteBuf, SaveItemFilterPayload> STREAM_CODEC = StreamCodec.ofMember(SaveItemFilterPayload::encode, SaveItemFilterPayload::decode);

        public SaveItemFilterPayload {
            itemId = itemId == null ? "" : itemId;
            minDurabilityPercent = Math.max(0, Math.min(100, minDurabilityPercent));
            requiredEnchantments = List.copyOf(requiredEnchantments == null ? List.of() : requiredEnchantments);
            forbiddenEnchantments = List.copyOf(forbiddenEnchantments == null ? List.of() : forbiddenEnchantments);
        }

        private static SaveItemFilterPayload decode(RegistryFriendlyByteBuf buffer) {
            return new SaveItemFilterPayload(
                    buffer.readUtf(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    readStringList(buffer),
                    readStringList(buffer)
            );
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeUtf(itemId);
            buffer.writeVarInt(status);
            buffer.writeVarInt(minDurabilityPercent);
            writeStringList(buffer, requiredEnchantments);
            writeStringList(buffer, forbiddenEnchantments);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClearItemFilterPayload(String itemId) implements CustomPacketPayload {
        public static final Type<ClearItemFilterPayload> TYPE = payloadType("dump_null_clear_item_filter");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClearItemFilterPayload> STREAM_CODEC = StreamCodec.ofMember(ClearItemFilterPayload::encode, ClearItemFilterPayload::decode);

        public ClearItemFilterPayload {
            itemId = itemId == null ? "" : itemId;
        }

        private static ClearItemFilterPayload decode(RegistryFriendlyByteBuf buffer) {
            return new ClearItemFilterPayload(buffer.readUtf());
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeUtf(itemId);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClearItemFiltersPayload(List<String> itemIds) implements CustomPacketPayload {
        public static final Type<ClearItemFiltersPayload> TYPE = payloadType("dump_null_clear_item_filters");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClearItemFiltersPayload> STREAM_CODEC = StreamCodec.ofMember(ClearItemFiltersPayload::encode, ClearItemFiltersPayload::decode);

        public ClearItemFiltersPayload {
            itemIds = List.copyOf(itemIds == null ? List.of() : itemIds);
        }

        private static ClearItemFiltersPayload decode(RegistryFriendlyByteBuf buffer) {
            return new ClearItemFiltersPayload(readStringList(buffer));
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            writeStringList(buffer, itemIds);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetHeldDiscardModePayload(int mode) implements CustomPacketPayload {
        public static final Type<SetHeldDiscardModePayload> TYPE = payloadType("dump_null_set_held_discard_mode");
        public static final StreamCodec<RegistryFriendlyByteBuf, SetHeldDiscardModePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, SetHeldDiscardModePayload::mode, SetHeldDiscardModePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetDockDiscardModePayload(int mode) implements CustomPacketPayload {
        public static final Type<SetDockDiscardModePayload> TYPE = payloadType("dump_null_set_dock_discard_mode");
        public static final StreamCodec<RegistryFriendlyByteBuf, SetDockDiscardModePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, SetDockDiscardModePayload::mode, SetDockDiscardModePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetSideModePayload(int direction, int mode) implements CustomPacketPayload {
        public static final Type<SetSideModePayload> TYPE = payloadType("dump_null_set_side_mode");
        public static final StreamCodec<RegistryFriendlyByteBuf, SetSideModePayload> STREAM_CODEC = StreamCodec.ofMember(SetSideModePayload::encode, SetSideModePayload::decode);

        private static SetSideModePayload decode(RegistryFriendlyByteBuf buffer) {
            return new SetSideModePayload(buffer.readVarInt(), buffer.readVarInt());
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(direction);
            buffer.writeVarInt(mode);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetDiscardExportSidePayload(int direction, boolean enabled) implements CustomPacketPayload {
        public static final Type<SetDiscardExportSidePayload> TYPE = payloadType("dump_null_set_discard_export_side");
        public static final StreamCodec<RegistryFriendlyByteBuf, SetDiscardExportSidePayload> STREAM_CODEC = StreamCodec.ofMember(SetDiscardExportSidePayload::encode, SetDiscardExportSidePayload::decode);

        private static SetDiscardExportSidePayload decode(RegistryFriendlyByteBuf buffer) {
            return new SetDiscardExportSidePayload(buffer.readVarInt(), buffer.readBoolean());
        }

        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeVarInt(direction);
            buffer.writeBoolean(enabled);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum InstallPowerUpgradePayload implements CustomPacketPayload {
        INSTANCE;

        public static final Type<InstallPowerUpgradePayload> TYPE = payloadType("dump_null_install_power_upgrade");
        public static final StreamCodec<RegistryFriendlyByteBuf, InstallPowerUpgradePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
