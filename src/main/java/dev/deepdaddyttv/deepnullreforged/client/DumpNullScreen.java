package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullBufferEntry;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullCatalog;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullData;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullDockDiscardMode;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullDropCandidate;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullDropSearch;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullHeldDiscardMode;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemCatalogEntry;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemCatalogView;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatus;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatusSummary;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullMobFilter;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullMobDropRow;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullMobOption;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullPresetOption;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullPresetTarget;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullRule;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullSideMode;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullStatusCopyFormatter;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.menu.DumpNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DumpNullPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DumpNullScreen extends AbstractContainerScreen<DumpNullMenu> {
    private static final int PADDING = 8;
    private static final int CATALOG_Y = 20;
    private static final int TOOLBAR_HEIGHT = 18;
    private static final int CATALOG_CONTROLS_HEIGHT = 78;
    private static final int RIBBON_WIDTH = 132;
    private static final int RIBBON_MIN_WIDTH = 120;
    private static final int RIBBON_GAP = 6;
    private static final int RIBBON_DETAIL_HEIGHT = 78;
    private static final int ICON_BUTTON_SIZE = 14;
    private static final int COPY_ICON_SIZE = 10;
    private static final int ERASER_ICON_SIZE = 10;
    private static final int GRID_TILE_WIDTH = 40;
    private static final int GRID_TILE_HEIGHT = 32;
    private static final int GRID_HEADER_HEIGHT = 11;
    private static final int MOB_HEADER_HEIGHT = 14;
    private static final int CATEGORY_HEADER_HEIGHT = 13;
    private static final int MOB_CARD_HEIGHT = 104;
    private static final int MOB_CARD_TARGET_WIDTH = 96;
    private static final int MOB_CARD_GAP = 5;
    private static final int DROP_ICON_SIZE = 18;
    private static final int DROP_ICON_GAP = 3;
    private static final int ITEM_TILE_SIZE = 24;
    private static final int ITEM_TILE_GAP = 4;
    private static final int TILE_REMOVE_SIZE = 8;
    private static final int PRESET_RULE_ICON_SIZE = 18;
    private static final int PRESET_PREVIEW_CARD_HEIGHT = 54;
    private static final int SCROLLBAR_WIDTH = 5;
    private static final int SCROLLBAR_MIN_HANDLE = 10;
    private static final int DROP_SEARCH_MIN_LENGTH = 2;
    private static final int DROP_SEARCH_DEBOUNCE_TICKS = 8;
    private static final Duration TOOLTIP_DELAY = Duration.ofMillis(250);
    private static final ResourceLocation COPY_ICON = DeepNullReforged.id("textures/gui/widgets/dumpnull_copy.png");
    private static final ResourceLocation ERASER_ICON = DeepNullReforged.id("textures/gui/widgets/eraser_button.png");

    private static SavedState savedState;

    private final Map<ResourceLocation, List<DumpNullDropCandidate>> dropRows = new LinkedHashMap<>();
    private final Set<ResourceLocation> pendingDropRequests = new LinkedHashSet<>();
    private final Map<ResourceLocation, Entity> entityPreviewCache = new LinkedHashMap<>();
    private final List<DumpNullItemCatalogEntry> itemCatalogEntries = new ArrayList<>();
    private final Map<ResourceLocation, Integer> cardDropScrolls = new LinkedHashMap<>();
    private final Set<ResourceLocation> dropSearchMobMatches = new LinkedHashSet<>();
    private final Set<String> collapsedNamespaces = new LinkedHashSet<>();
    private final Set<String> collapsedCategories = new LinkedHashSet<>();
    private final Set<String> pendingTargetIds = new LinkedHashSet<>();
    private final Map<DumpNullMobFilter, Button> filterButtons = new EnumMap<>(DumpNullMobFilter.class);

    private EditBox mobSearchBox;
    private EditBox targetSearchBox;
    private EditBox minDurabilityBox;
    private EditBox requiredEnchantmentsBox;
    private EditBox forbiddenEnchantmentsBox;
    private Button confirmPresetButton;
    private Button cancelPresetButton;
    private Button saveFilterButton;
    private Button clearFilterButton;
    private Button copyAcceptedButton;
    private Button copyDiscardedButton;
    private Button clearAcceptedButton;
    private Button clearDiscardedButton;
    private Button writeAcceptedPresetButton;
    private Button mobsModeButton;
    private Button itemsModeButton;
    private Button automationModeButton;
    private Button upgradesModeButton;
    private Button heldDiscardModeButton;
    private Button dockDiscardModeButton;
    private Button installPowerUpgradeButton;
    private Button presetPreviousButton;
    private Button presetsButton;
    private Button presetNextButton;
    private Button previewPresetButton;
    private Button itemViewPreviousButton;
    private Button itemViewButton;
    private Button itemViewNextButton;
    private Button expandAllButton;
    private Button collapseAllButton;

    private CatalogMode catalogMode = CatalogMode.MOBS;
    private DumpNullItemCatalogView itemCatalogView = DumpNullItemCatalogView.ORES;
    private String loadedItemCatalogViewId = "";
    private String pendingItemCatalogViewId = "";
    private DumpNullMobFilter activeFilter;
    private boolean presetsExpanded;
    private boolean itemViewsExpanded;
    private int selectedPresetIndex;
    private String pendingPresetId = "";
    private String pendingTargetType = DumpNullPresetOption.TARGET_NONE;
    private List<String> presetPreviewTargetIds = List.of();
    private List<ResourceLocation> presetPreviewMobIds = List.of();
    private List<DumpNullItemStatusSummary> presetPreviewAcceptedSummaries = List.of();
    private List<DumpNullItemStatusSummary> presetPreviewDiscardedSummaries = List.of();
    private boolean presetPreviewResolved;
    private boolean writeAcceptedPresetRules;
    private boolean presetAcceptedCollapsed;
    private boolean presetDiscardedCollapsed;
    private int presetPreviewScroll;
    private ActiveScrollDrag activeScrollDrag;
    private ActiveMiddleScroll activeMiddleScroll;
    private ResourceLocation selectedItemId;
    private ResourceLocation focusedMobId;
    private int mobScroll;
    private int itemScroll;
    private int acceptedGridScroll;
    private int discardedGridScroll;
    private int dropSearchCooldownTicks;
    private String pendingDropSearchQuery = "";
    private String activeDropSearchQuery = "";
    private String lastDropSearchRequestQuery = "";
    private ItemStack hoveredItem = ItemStack.EMPTY;
    private List<Component> hoveredTooltipLines = List.of();
    private Direction selectedAutomationFace = Direction.NORTH;
    private String frameTooltipKey;
    private Component frameTooltip;
    private String activeTooltipKey = "";
    private long tooltipStartNanos;

    public DumpNullScreen(DumpNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 420;
        this.imageHeight = 236;
        this.inventoryLabelY = 10_000;
    }

    @Override
    protected void init() {
        this.imageWidth = Math.max(340, Math.min(560, this.width - 16));
        this.imageHeight = Math.max(220, Math.min(360, this.height - 16));
        if (this.imageWidth > this.width - 8) {
            this.imageWidth = Math.max(300, this.width - 8);
        }
        if (this.imageHeight > this.height - 8) {
            this.imageHeight = Math.max(200, this.height - 8);
        }
        super.init();
        filterButtons.clear();

        if (menu.getViewMode() == DumpNullMenu.ViewMode.UPGRADES) {
            mobsModeButton = addRenderableWidget(Button.builder(Component.literal("Back"), button ->
                            PacketDistributor.sendToServer(new DumpNullPayloads.OpenViewPayload(DumpNullMenu.ViewMode.CATALOG.ordinal())))
                    .bounds(leftPos + PADDING, topPos + CATALOG_Y + 2, 48, TOOLBAR_HEIGHT)
                    .build());
            upgradesModeButton = addRenderableWidget(Button.builder(Component.translatable("dn.dumpnull.upgrades"), button -> {
                    })
                    .bounds(leftPos + PADDING + 52, topPos + CATALOG_Y + 2, 64, TOOLBAR_HEIGHT)
                    .build());
            upgradesModeButton.active = false;
            return;
        }

        mobsModeButton = addRenderableWidget(Button.builder(Component.literal("Mobs"), button -> switchCatalogMode(CatalogMode.MOBS))
                .bounds(leftPos + PADDING, topPos + CATALOG_Y + 2, 34, TOOLBAR_HEIGHT)
                .build());
        mobsModeButton.setTooltip(Tooltip.create(Component.literal("Show the live mob catalogue.")));
        mobsModeButton.setTooltipDelay(TOOLTIP_DELAY);
        itemsModeButton = addRenderableWidget(Button.builder(Component.literal("Items"), button -> switchCatalogMode(CatalogMode.ITEMS))
                .bounds(leftPos + PADDING + 38, topPos + CATALOG_Y + 2, 36, TOOLBAR_HEIGHT)
                .build());
        itemsModeButton.setTooltip(Tooltip.create(Component.literal("Show temporary Catalog Views for choosing DumpNull item rules.")));
        itemsModeButton.setTooltipDelay(TOOLTIP_DELAY);
        automationModeButton = addRenderableWidget(Button.builder(Component.literal("Auto"), button -> switchCatalogMode(CatalogMode.AUTOMATION))
                .bounds(leftPos + PADDING + 78, topPos + CATALOG_Y + 2, 34, TOOLBAR_HEIGHT)
                .build());
        automationModeButton.setTooltip(Tooltip.create(Component.literal("Configure held pickup filtering, dock routing, buffers, and DumpNull power.")));
        automationModeButton.setTooltipDelay(TOOLTIP_DELAY);
        upgradesModeButton = addRenderableWidget(Button.builder(Component.translatable("dn.dumpnull.upgrades"), button ->
                        PacketDistributor.sendToServer(new DumpNullPayloads.OpenViewPayload(DumpNullMenu.ViewMode.UPGRADES.ordinal())))
                .bounds(leftPos + PADDING + 116, topPos + CATALOG_Y + 2, 64, TOOLBAR_HEIGHT)
                .build());
        upgradesModeButton.setTooltip(Tooltip.create(Component.translatable("dn.dumpnull.upgrades.tooltip")));
        upgradesModeButton.setTooltipDelay(TOOLTIP_DELAY);
        presetPreviousButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> cycleSetupPreset(-1))
                .bounds(leftPos + presetCarouselX(), topPos + presetCarouselY(), ICON_BUTTON_SIZE, TOOLBAR_HEIGHT)
                .build());
        presetPreviousButton.setTooltip(Tooltip.create(Component.literal("Cycle to the previous Setup Preset.")));
        presetPreviousButton.setTooltipDelay(TOOLTIP_DELAY);
        presetsButton = addRenderableWidget(Button.builder(currentPresetLabel(), button -> presetsExpanded = !presetsExpanded)
                .bounds(leftPos + presetCarouselX() + ICON_BUTTON_SIZE + 2, topPos + presetCarouselY(), presetCenterWidth(), TOOLBAR_HEIGHT)
                .build());
        presetsButton.setTooltip(Tooltip.create(Component.literal("Open the Setup Preset dropdown. Setup Presets only write after Preview and Apply.")));
        presetsButton.setTooltipDelay(TOOLTIP_DELAY);
        presetNextButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> cycleSetupPreset(1))
                .bounds(leftPos + presetCarouselX() + ICON_BUTTON_SIZE + 4 + presetCenterWidth(), topPos + presetCarouselY(), ICON_BUTTON_SIZE, TOOLBAR_HEIGHT)
                .build());
        presetNextButton.setTooltip(Tooltip.create(Component.literal("Cycle to the next Setup Preset.")));
        presetNextButton.setTooltipDelay(TOOLTIP_DELAY);
        previewPresetButton = addRenderableWidget(Button.builder(Component.literal("Preview"), button -> previewSelectedSetupPreset())
                .bounds(leftPos + presetPreviewButtonX(), topPos + presetCarouselY(), presetPreviewButtonWidth(), TOOLBAR_HEIGHT)
                .build());
        previewPresetButton.setTooltip(Tooltip.create(Component.literal("Preview the selected Setup Preset before applying it.")));
        previewPresetButton.setTooltipDelay(TOOLTIP_DELAY);
        itemViewPreviousButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> cycleItemCatalogView(-1))
                .bounds(leftPos + itemViewCarouselX(), topPos + itemViewCarouselY(), ICON_BUTTON_SIZE, TOOLBAR_HEIGHT)
                .build());
        itemViewPreviousButton.setTooltip(Tooltip.create(Component.literal("Cycle to the previous Catalog View.")));
        itemViewPreviousButton.setTooltipDelay(TOOLTIP_DELAY);
        itemViewButton = addRenderableWidget(Button.builder(currentItemViewLabel(), button -> itemViewsExpanded = !itemViewsExpanded)
                .bounds(leftPos + itemViewCarouselX() + ICON_BUTTON_SIZE + 2, topPos + itemViewCarouselY(), itemViewCenterWidth(), TOOLBAR_HEIGHT)
                .build());
        itemViewButton.setTooltip(Tooltip.create(Component.literal("Open the Catalog View dropdown. Catalog Views are temporary filters and are not saved.")));
        itemViewButton.setTooltipDelay(TOOLTIP_DELAY);
        itemViewNextButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> cycleItemCatalogView(1))
                .bounds(leftPos + itemViewCarouselX() + ICON_BUTTON_SIZE + 4 + itemViewCenterWidth(), topPos + itemViewCarouselY(), ICON_BUTTON_SIZE, TOOLBAR_HEIGHT)
                .build());
        itemViewNextButton.setTooltip(Tooltip.create(Component.literal("Cycle to the next Catalog View.")));
        itemViewNextButton.setTooltipDelay(TOOLTIP_DELAY);
        collapseAllButton = addRenderableWidget(Button.builder(Component.literal("-"), button -> collapseAllCatalog())
                .bounds(leftPos + collapseAllButtonX(), topPos + CATALOG_Y + 2, ICON_BUTTON_SIZE, TOOLBAR_HEIGHT)
                .build());
        collapseAllButton.setTooltip(Tooltip.create(Component.literal("Collapse all mob catalog sections.")));
        collapseAllButton.setTooltipDelay(TOOLTIP_DELAY);
        expandAllButton = addRenderableWidget(Button.builder(Component.literal("+"), button -> expandAllCatalog())
                .bounds(leftPos + expandAllButtonX(), topPos + CATALOG_Y + 2, ICON_BUTTON_SIZE, TOOLBAR_HEIGHT)
                .build());
        expandAllButton.setTooltip(Tooltip.create(Component.literal("Expand all mob catalog sections.")));
        expandAllButton.setTooltipDelay(TOOLTIP_DELAY);

        confirmPresetButton = addRenderableWidget(Button.builder(Component.literal("Apply"), button -> confirmPreset())
                .bounds(leftPos + imageWidth - 104, topPos + CATALOG_Y, 58, TOOLBAR_HEIGHT)
                .build());
        confirmPresetButton.setTooltip(Tooltip.create(Component.literal("Apply the staged preset to this DumpNull.")));
        confirmPresetButton.setTooltipDelay(TOOLTIP_DELAY);
        cancelPresetButton = addRenderableWidget(Button.builder(Component.literal("X"), button -> clearPendingPreset())
                .bounds(leftPos + imageWidth - 44, topPos + CATALOG_Y, 38, TOOLBAR_HEIGHT)
                .build());
        cancelPresetButton.setTooltip(Tooltip.create(Component.literal("Close the preset preview without applying it.")));
        cancelPresetButton.setTooltipDelay(TOOLTIP_DELAY);
        writeAcceptedPresetButton = addRenderableWidget(Button.builder(Component.literal("[ ] Write accepted"), button -> {
                    writeAcceptedPresetRules = !writeAcceptedPresetRules;
                    updateWidgetVisibility();
                })
                .bounds(leftPos + overlayX() + 10, topPos + overlayY() + 34, 112, 16)
                .build());
        writeAcceptedPresetButton.setTooltip(Tooltip.create(Component.literal("When enabled, applying the preset saves accepted drop rules instead of previewing them only.")));
        writeAcceptedPresetButton.setTooltipDelay(TOOLTIP_DELAY);
        copyAcceptedButton = addRenderableWidget(Button.builder(Component.empty(), button -> copyStatus(DumpNullItemStatus.ACCEPTED))
                .bounds(leftPos + acceptedGridX() + gridWidth() - ICON_BUTTON_SIZE, topPos + acceptedGridHeaderY(), ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
                .build());
        copyAcceptedButton.setTooltip(Tooltip.create(Component.literal("Copy accepted items, one per line.")));
        copyAcceptedButton.setTooltipDelay(TOOLTIP_DELAY);
        clearAcceptedButton = addRenderableWidget(Button.builder(Component.empty(), button -> clearStatus(DumpNullItemStatus.ACCEPTED))
                .bounds(leftPos + acceptedGridX() + gridWidth() - ICON_BUTTON_SIZE * 2 - 3, topPos + acceptedGridHeaderY(), ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
                .build());
        clearAcceptedButton.setTooltip(Tooltip.create(Component.literal("Clear all accepted items from this DumpNull.")));
        clearAcceptedButton.setTooltipDelay(TOOLTIP_DELAY);
        copyDiscardedButton = addRenderableWidget(Button.builder(Component.empty(), button -> copyStatus(DumpNullItemStatus.DISCARDED))
                .bounds(leftPos + discardedGridX() + gridWidth() - ICON_BUTTON_SIZE, topPos + discardedGridHeaderY(), ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
                .build());
        copyDiscardedButton.setTooltip(Tooltip.create(Component.literal("Copy discarded items, one per line.")));
        copyDiscardedButton.setTooltipDelay(TOOLTIP_DELAY);
        clearDiscardedButton = addRenderableWidget(Button.builder(Component.empty(), button -> clearStatus(DumpNullItemStatus.DISCARDED))
                .bounds(leftPos + discardedGridX() + gridWidth() - ICON_BUTTON_SIZE * 2 - 3, topPos + discardedGridHeaderY(), ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
                .build());
        clearDiscardedButton.setTooltip(Tooltip.create(Component.literal("Clear all discarded items from this DumpNull.")));
        clearDiscardedButton.setTooltipDelay(TOOLTIP_DELAY);
        heldDiscardModeButton = addRenderableWidget(Button.builder(Component.empty(), button -> cycleHeldDiscardMode())
                .bounds(leftPos + automationControlX(), topPos + automationModesY(), automationModeButtonWidth(), 16)
                .build());
        heldDiscardModeButton.setTooltip(Tooltip.create(Component.literal("Cycle what carried DumpNulls do with discarded pickup items.")));
        heldDiscardModeButton.setTooltipDelay(TOOLTIP_DELAY);
        dockDiscardModeButton = addRenderableWidget(Button.builder(Component.empty(), button -> cycleDockDiscardMode())
                .bounds(leftPos + automationControlX() + automationModeButtonWidth() + 4, topPos + automationModesY(), automationModeButtonWidth(), 16)
                .build());
        dockDiscardModeButton.setTooltip(Tooltip.create(Component.literal("Cycle what docked DumpNull automation does with discarded items.")));
        dockDiscardModeButton.setTooltipDelay(TOOLTIP_DELAY);
        installPowerUpgradeButton = addRenderableWidget(Button.builder(Component.empty(), button ->
                        PacketDistributor.sendToServer(DumpNullPayloads.InstallPowerUpgradePayload.INSTANCE))
                .bounds(leftPos + automationControlX(), topPos + automationInstallY(), automationInstallButtonWidth(), 16)
                .build());
        installPowerUpgradeButton.setTooltip(Tooltip.create(Component.literal("Consumes one DumpNull Power Upgrade from your inventory and enables FE storage.")));
        installPowerUpgradeButton.setTooltipDelay(TOOLTIP_DELAY);

        mobSearchBox = addBox(leftPos + mobSearchX(), topPos + CATALOG_Y + 2, mobSearchWidth(), "");
        mobSearchBox.setResponder(ignored -> onMobSearchChanged());
        int chipX = leftPos + PADDING;
        int chipY = topPos + CATALOG_Y + 24;
        for (DumpNullMobFilter filter : DumpNullMobFilter.values()) {
            Button button = addRenderableWidget(Button.builder(filterLabel(filter), ignored -> toggleFilter(filter))
                    .bounds(chipX, chipY, filterWidth(filter), 16)
                    .build());
            button.setTooltip(Tooltip.create(filterTooltip(filter)));
            button.setTooltipDelay(TOOLTIP_DELAY);
            filterButtons.put(filter, button);
            chipX += filterWidth(filter) + 3;
        }
        targetSearchBox = addBox(leftPos + overlayX() + 10, topPos + overlayY() + 24, overlayWidth() - 20, "");
        targetSearchBox.setResponder(ignored -> presetPreviewScroll = clamp(presetPreviewScroll, 0, presetPreviewMaxScroll()));
        int enchantBoxWidth = Math.max(42, (detailWidth() - 14) / 2);
        minDurabilityBox = addBox(leftPos + detailX() + 28, topPos + detailY() + 20, 24, "0");
        requiredEnchantmentsBox = addBox(leftPos + detailX() + 5, topPos + detailY() + 47, enchantBoxWidth, "");
        forbiddenEnchantmentsBox = addBox(leftPos + detailX() + 9 + enchantBoxWidth, topPos + detailY() + 47, enchantBoxWidth, "");

        saveFilterButton = addRenderableWidget(Button.builder(Component.literal("OK"), button -> saveSelectedFilter())
                .bounds(leftPos + detailX() + detailWidth() - 60, topPos + detailY() + 20, 28, 18)
                .build());
        saveFilterButton.setTooltip(Tooltip.create(Component.literal("Save durability and enchantment filters for the selected item.")));
        saveFilterButton.setTooltipDelay(TOOLTIP_DELAY);
        clearFilterButton = addRenderableWidget(Button.builder(Component.empty(), button -> clearSelectedFilter())
                .bounds(leftPos + detailX() + detailWidth() - ICON_BUTTON_SIZE, topPos + detailY() + 22, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
                .build());
        clearFilterButton.setTooltip(Tooltip.create(Component.literal("Remove all DumpNull rules for the selected item.")));
        clearFilterButton.setTooltipDelay(TOOLTIP_DELAY);

        restoreSavedState();
        hydrateDetailFields();
        updateWidgetVisibility();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF014171C);
        guiGraphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xE91E232A);
        if (menu.getViewMode() == DumpNullMenu.ViewMode.UPGRADES) {
            guiGraphics.fill(leftPos + PADDING, topPos + CATALOG_Y, leftPos + imageWidth - PADDING, topPos + imageHeight - PADDING, 0xEA22272F);
            renderUpgradeSlotFrames(guiGraphics);
            return;
        }
        guiGraphics.fill(leftPos + PADDING, topPos + CATALOG_Y, leftPos + catalogRight(), topPos + imageHeight - PADDING, 0xEA22272F);
        guiGraphics.fill(leftPos + ribbonX(), topPos + ribbonY(), leftPos + ribbonX() + ribbonWidth(), topPos + ribbonY() + ribbonHeight(), 0xEA242A32);
    }

    private void renderUpgradeSlotFrames(GuiGraphics guiGraphics) {
        for (Slot slot : menu.slots) {
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            guiGraphics.fill(x, y, x + 18, y + 18, 0xFF0B0F15);
            guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF151A22);
            guiGraphics.renderOutline(x, y, 18, 18, slot instanceof DumpNullMenu.DumpUpgradeSlot ? 0xFF53647A : 0xFF465366);
            if (slot instanceof DumpNullMenu.DumpUpgradeSlot upgradeSlot && !slot.hasItem()) {
                guiGraphics.drawCenteredString(font, upgradeSlot.getType().name().substring(0, 1), x + 9, y + 5, 0xFF65758A);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (menu.getViewMode() == DumpNullMenu.ViewMode.UPGRADES) {
            hoveredItem = ItemStack.EMPTY;
            hoveredTooltipLines = List.of();
            beginManualTooltipFrame();
            guiGraphics.drawString(font, title, PADDING, 6, 0xFFE8EDF5, false);
            guiGraphics.drawString(font, Component.translatable("dn.dumpnull.upgrades_hint"), PADDING, 46, 0xFFB8C4D6, false);
            guiGraphics.drawString(font, playerInventoryTitle, 14, 72, 0xFFB8C4D6, false);
            return;
        }
        updateWidgetVisibility();
        hoveredItem = ItemStack.EMPTY;
        hoveredTooltipLines = List.of();
        beginManualTooltipFrame();
        int localMouseX = mouseX - leftPos;
        int localMouseY = mouseY - topPos;
        guiGraphics.drawString(font, title, PADDING, 6, 0xFFE8EDF5, false);
        renderPendingPresetLabel(guiGraphics);
        if (catalogMode == CatalogMode.AUTOMATION) {
            renderAutomationView(guiGraphics, localMouseX, localMouseY);
        } else {
            renderStatusGrid(guiGraphics, DumpNullItemStatus.ACCEPTED, acceptedGridX(), acceptedGridY(), gridWidth(), statusGridHeight(), acceptedGridScroll, localMouseX, localMouseY);
            renderStatusGrid(guiGraphics, DumpNullItemStatus.DISCARDED, discardedGridX(), discardedGridY(), gridWidth(), statusGridHeight(), discardedGridScroll, localMouseX, localMouseY);
            renderDetailPanel(guiGraphics, localMouseX, localMouseY);
        }
        if (catalogMode == CatalogMode.ITEMS) {
            renderItemCatalog(guiGraphics, localMouseX, localMouseY);
        } else if (catalogMode == CatalogMode.MOBS) {
            renderMobCatalog(guiGraphics, localMouseX, localMouseY);
        }
        renderHeaderIconButton(guiGraphics, copyAcceptedButton, copyAcceptedButton.active, COPY_ICON, COPY_ICON_SIZE, "copy-accepted", Component.literal("Copy accepted items, one per line."));
        renderHeaderIconButton(guiGraphics, clearAcceptedButton, clearAcceptedButton.active, ERASER_ICON, ERASER_ICON_SIZE, "clear-accepted", Component.literal("Clear all accepted items."));
        renderHeaderIconButton(guiGraphics, copyDiscardedButton, copyDiscardedButton.active, COPY_ICON, COPY_ICON_SIZE, "copy-discarded", Component.literal("Copy discarded items, one per line."));
        renderHeaderIconButton(guiGraphics, clearDiscardedButton, clearDiscardedButton.active, ERASER_ICON, ERASER_ICON_SIZE, "clear-discarded", Component.literal("Clear all discarded items."));
        renderHeaderIconButton(guiGraphics, clearFilterButton, clearFilterButton.active, ERASER_ICON, ERASER_ICON_SIZE, "clear-selected-filter", Component.literal("Remove all DumpNull rules for the selected item."));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (!pendingPresetId.isBlank()) {
            guiGraphics.flush();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(leftPos, topPos, 320.0F);
            renderPresetOverlay(guiGraphics, mouseX - leftPos, mouseY - topPos);
            guiGraphics.pose().popPose();
            guiGraphics.flush();
            if (targetSearchBox.visible) {
                targetSearchBox.render(guiGraphics, mouseX, mouseY, partialTick);
            }
            writeAcceptedPresetButton.render(guiGraphics, mouseX, mouseY, partialTick);
            confirmPresetButton.render(guiGraphics, mouseX, mouseY, partialTick);
            cancelPresetButton.render(guiGraphics, mouseX, mouseY, partialTick);
        } else if (presetsExpanded) {
            guiGraphics.flush();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(leftPos, topPos, 320.0F);
            renderPresetDropdown(guiGraphics, mouseX - leftPos, mouseY - topPos);
            guiGraphics.pose().popPose();
            guiGraphics.flush();
            if (inside(mouseX - leftPos, mouseY - topPos, presetDropdownX(), presetDropdownY(), presetDropdownWidth(), presetDropdownHeight())) {
                hoveredItem = ItemStack.EMPTY;
            }
        } else if (itemViewsExpanded) {
            guiGraphics.flush();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(leftPos, topPos, 320.0F);
            renderItemViewDropdown(guiGraphics, mouseX - leftPos, mouseY - topPos);
            guiGraphics.pose().popPose();
            guiGraphics.flush();
        }
        guiGraphics.flush();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 500.0F);
        if (!hoveredTooltipLines.isEmpty()) {
            guiGraphics.renderTooltip(font, hoveredTooltipLines, java.util.Optional.empty(), mouseX, mouseY);
        } else if (!hoveredItem.isEmpty()) {
            guiGraphics.renderTooltip(font, hoveredItem, mouseX, mouseY);
        } else if (renderManualTooltip(guiGraphics, mouseX, mouseY)) {
            guiGraphics.pose().popPose();
            return;
        } else {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
        guiGraphics.pose().popPose();
    }

    private void beginManualTooltipFrame() {
        frameTooltipKey = null;
        frameTooltip = null;
    }

    private void offerManualTooltip(String key, Component tooltip) {
        if (frameTooltip == null) {
            frameTooltipKey = key;
            frameTooltip = tooltip;
        }
    }

    private boolean renderManualTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (frameTooltip == null) {
            activeTooltipKey = "";
            return false;
        }
        if (!frameTooltipKey.equals(activeTooltipKey)) {
            activeTooltipKey = frameTooltipKey;
            tooltipStartNanos = System.nanoTime();
            return false;
        }
        if (System.nanoTime() - tooltipStartNanos < TOOLTIP_DELAY.toNanos()) {
            return false;
        }
        guiGraphics.renderTooltip(font, frameTooltip, mouseX, mouseY);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.getViewMode() == DumpNullMenu.ViewMode.UPGRADES) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int localX = (int) mouseX - leftPos;
        int localY = (int) mouseY - topPos;
        ScrollRegion scrollRegion = scrollRegionAt(localX, localY);
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && scrollRegion != null) {
            activeMiddleScroll = new ActiveMiddleScroll(scrollRegion, localY, scrollFor(scrollRegion));
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && scrollRegion != null && scrollbarHit(scrollRegion, localX, localY)) {
            startScrollbarDrag(scrollRegion, localY);
            return true;
        }
        if (!pendingPresetId.isBlank()) {
            if (button == 0 && presetNeedsTargets()) {
                DumpNullPresetTarget target = targetAt(localX, localY);
                if (target != null) {
                    toggleTarget(target);
                    return true;
                }
            }
            PresetSection section = presetSectionAt(localX, localY);
            if (button == 0 && section != null) {
                togglePresetSection(section);
                return true;
            }
            if (inside(localX, localY, overlayX(), overlayY(), overlayWidth(), overlayHeight())) {
                boolean overlayWidget = confirmPresetButton.isMouseOver(mouseX, mouseY)
                        || cancelPresetButton.isMouseOver(mouseX, mouseY)
                        || writeAcceptedPresetButton.isMouseOver(mouseX, mouseY)
                        || presetNeedsTargets() && targetSearchBox.isMouseOver(mouseX, mouseY);
                return overlayWidget ? super.mouseClicked(mouseX, mouseY, button) || true : true;
            }
            return true;
        }

        if (presetsExpanded) {
            DumpNullPresetOption preset = presetOptionAt(localX, localY);
            if (button == 0 && preset != null) {
                presetsExpanded = false;
                selectedPresetIndex = Math.max(0, menu.getPresetOptions().indexOf(preset));
                updateWidgetVisibility();
                return true;
            }
            if (!inside(localX, localY, presetDropdownX(), presetCarouselY(), presetDropdownWidth(), presetDropdownTotalHeight())
                    && (presetsButton == null || !presetsButton.isMouseOver(mouseX, mouseY))) {
                presetsExpanded = false;
            }
        }
        if (itemViewsExpanded) {
            DumpNullItemCatalogView view = itemViewAt(localX, localY);
            if (button == 0 && view != null) {
                itemViewsExpanded = false;
                setItemCatalogView(view);
                return true;
            }
            if (!inside(localX, localY, itemViewDropdownX(), itemViewCarouselY(), itemViewDropdownWidth(), itemViewDropdownTotalHeight())
                    && (itemViewButton == null || !itemViewButton.isMouseOver(mouseX, mouseY))) {
                itemViewsExpanded = false;
            }
        }
        if (button == 0 && catalogControlWidgetAt(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (catalogMode == CatalogMode.AUTOMATION) {
            if (button == 0 && handleAutomationClick(localX, localY)) {
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (handleStatusGridClick(localX, localY, button)) {
            return true;
        }

        if (catalogMode == CatalogMode.ITEMS) {
            ItemCatalogTile itemTile = itemCatalogTileAt(localX, localY);
            if ((button == 0 || button == 1) && itemTile != null) {
                selectItem(itemTile.entry().itemId());
                DumpNullItemStatus current = statusOf(itemTile.entry().itemId());
                DumpNullItemStatus next = button == 1 ? current.catalogRightClickTarget() : current.next();
                setItemStatus(itemTile.entry().itemId(), next);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        HeaderClick headerClick = headerAt(localX, localY);
        if (button == 0 && headerClick != null) {
            if (headerClick.type() == CatalogRowType.MOD_HEADER) {
                toggleNamespace(headerClick.namespace());
            } else if (headerClick.type() == CatalogRowType.CATEGORY_HEADER) {
                toggleCategory(headerClick.namespace(), headerClick.category());
            }
            return true;
        }

        CardControl cardControl = cardControlAt(localX, localY);
        if (button == 0 && cardControl != null) {
            focusMob(cardControl.entityId(), false);
            copyMobDrops(cardControl.entityId());
            return true;
        }

        DropClick dropClick = dropAt(localX, localY);
        if ((button == 0 || button == 1) && dropClick != null) {
            focusMob(dropClick.entityId(), false);
            if (dropClick.allDrops()) {
                if (button == 1) {
                    setAllDropsFromRightClick(dropClick.entityId());
                } else {
                    cycleAllDrops(dropClick.entityId());
                }
                return true;
            }
            selectItem(dropClick.candidate().itemId());
            DumpNullItemStatus current = statusOf(dropClick.candidate().itemId());
            DumpNullItemStatus next = button == 1 ? current.catalogRightClickTarget() : current.next();
            setItemStatus(dropClick.candidate().itemId(), next);
            return true;
        }

        DumpNullMobOption option = mobAt(localX, localY);
        if (button == 0 && option != null) {
            focusMob(option.entityId(), false);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int localY = (int) mouseY - topPos;
        if (activeScrollDrag != null) {
            setScroll(activeScrollDrag.region(), scrollForHandleTop(activeScrollDrag.region(), localY - activeScrollDrag.handleOffsetY()));
            return true;
        }
        if (activeMiddleScroll != null) {
            setScroll(activeMiddleScroll.region(), activeMiddleScroll.startScroll() + (localY - activeMiddleScroll.startMouseY()));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (activeScrollDrag != null || activeMiddleScroll != null) {
            activeScrollDrag = null;
            activeMiddleScroll = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.getViewMode() == DumpNullMenu.ViewMode.UPGRADES) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int localX = (int) mouseX - leftPos;
        int localY = (int) mouseY - topPos;
        ScrollRegion region = scrollRegionAt(localX, localY);
        if (region != null) {
            setScroll(region, scrollFor(region) - scrollStep(region, scrollY));
            return true;
        }
        if (catalogMode == CatalogMode.AUTOMATION) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (!pendingPresetId.isBlank()) {
            if (inside(localX, localY, overlayX(), overlayY(), overlayWidth(), overlayHeight())) {
                presetPreviewScroll = clamp(presetPreviewScroll - (int) Math.round(scrollY * 18), 0, presetPreviewMaxScroll());
            }
            return true;
        }
        if (inside(localX, localY, acceptedGridX(), acceptedGridY(), gridWidth(), statusGridHeight())) {
            acceptedGridScroll = clamp(acceptedGridScroll - (int) Math.round(scrollY * GRID_TILE_HEIGHT), 0, gridMaxScroll(DumpNullItemStatus.ACCEPTED));
            return true;
        }
        if (inside(localX, localY, discardedGridX(), discardedGridY(), gridWidth(), statusGridHeight())) {
            discardedGridScroll = clamp(discardedGridScroll - (int) Math.round(scrollY * GRID_TILE_HEIGHT), 0, gridMaxScroll(DumpNullItemStatus.DISCARDED));
            return true;
        }
        CardDropArea dropArea = cardDropAreaAt(localX, localY);
        if (dropArea != null && dropArea.maxScroll() > 0) {
            int next = clamp(cardDropScrolls.getOrDefault(dropArea.entityId(), 0) - (int) Math.signum(scrollY) * dropArea.columns(), 0, dropArea.maxScroll());
            cardDropScrolls.put(dropArea.entityId(), next);
            return true;
        }
        if (inside(localX, localY, PADDING, CATALOG_Y + CATALOG_CONTROLS_HEIGHT, catalogWidth(), catalogHeight() - CATALOG_CONTROLS_HEIGHT)) {
            if (catalogMode == CatalogMode.ITEMS) {
                itemScroll = clamp(itemScroll - (int) Math.round(scrollY * 24.0D), 0, itemMaxScroll());
            } else {
                mobScroll = clamp(mobScroll - (int) Math.round(scrollY * 24.0D), 0, mobMaxScroll());
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!pendingPresetId.isBlank() && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            clearPendingPreset();
            return true;
        }
        boolean searchFocused = mobSearchBox != null && mobSearchBox.isFocused();
        if (catalogMode == CatalogMode.ITEMS) {
            if (searchFocused && hasShiftDown() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
                mobSearchBox.setValue("");
                mobSearchBox.setFocused(true);
                itemScroll = 0;
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (catalogMode == CatalogMode.AUTOMATION) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (searchFocused && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            if (hasShiftDown()) {
                mobSearchBox.setValue("");
                mobSearchBox.setFocused(true);
                ensureFocusedMobValid();
                ensureFocusedMobVisible();
            } else {
                activateFocusedMobAddAll();
            }
            return true;
        }
        if (searchFocused && (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN)) {
            navigateFocusedMob(keyCode);
            return true;
        }
        if (!searchFocused && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN)) {
            navigateFocusedMob(keyCode);
            return true;
        }
        if (!searchFocused && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            activateFocusedMobAddAll();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        rememberState();
        for (Entity entity : entityPreviewCache.values()) {
            entity.discard();
        }
        entityPreviewCache.clear();
        super.removed();
    }

    public void acceptMobDrops(int containerId, List<DumpNullMobDropRow> rows) {
        if (containerId != menu.containerId) {
            return;
        }
        for (DumpNullMobDropRow row : rows) {
            dropRows.put(row.entityId(), row.drops());
            pendingDropRequests.remove(row.entityId());
        }
        ensureFocusedMobValid();
    }

    public void acceptMobDropSearch(DumpNullPayloads.MobDropSearchPayload payload) {
        if (payload.containerId() != menu.containerId) {
            return;
        }
        String currentQuery = currentDropSearchQuery();
        String payloadQuery = DumpNullDropSearch.normalizeQuery(payload.query());
        if (!payloadQuery.equals(currentQuery)) {
            return;
        }
        activeDropSearchQuery = payloadQuery;
        dropSearchMobMatches.clear();
        for (String mobId : payload.matchingMobIds()) {
            ResourceLocation parsed = ResourceLocation.tryParse(mobId);
            if (parsed != null) {
                dropSearchMobMatches.add(parsed);
            }
        }
        for (DumpNullMobDropRow row : payload.rows()) {
            dropRows.put(row.entityId(), row.drops());
            pendingDropRequests.remove(row.entityId());
        }
        mobScroll = clamp(mobScroll, 0, mobMaxScroll());
        ensureFocusedMobValid();
    }

    public void acceptState(int containerId, DumpNullData data, List<DumpNullMobOption> mobOptions, List<DumpNullItemStatusSummary> itemStatuses) {
        if (containerId != menu.containerId) {
            return;
        }
        menu.updateState(data, mobOptions, itemStatuses);
        hydrateDetailFields();
        acceptedGridScroll = clamp(acceptedGridScroll, 0, gridMaxScroll(DumpNullItemStatus.ACCEPTED));
        discardedGridScroll = clamp(discardedGridScroll, 0, gridMaxScroll(DumpNullItemStatus.DISCARDED));
        mobScroll = clamp(mobScroll, 0, mobMaxScroll());
        itemScroll = clamp(itemScroll, 0, itemMaxScroll());
        ensureFocusedMobValid();
        updateWidgetVisibility();
    }

    public void acceptPresetPreview(DumpNullPayloads.PresetPreviewPayload payload) {
        if (payload.containerId() != menu.containerId || !payload.presetId().equals(pendingPresetId)) {
            return;
        }
        if (!new LinkedHashSet<>(payload.targetIds()).equals(new LinkedHashSet<>(pendingTargetIds))) {
            return;
        }
        presetPreviewTargetIds = payload.targetIds();
        presetPreviewMobIds = parseResourceLocations(payload.selectedMobs());
        presetPreviewAcceptedSummaries = payload.acceptedSummaries();
        presetPreviewDiscardedSummaries = payload.discardedSummaries();
        presetPreviewResolved = true;
        presetPreviewScroll = clamp(presetPreviewScroll, 0, presetPreviewMaxScroll());
        updateWidgetVisibility();
    }

    public void acceptItemCatalog(DumpNullPayloads.ItemCatalogPayload payload) {
        if (payload.containerId() != menu.containerId || !payload.viewId().equals(itemCatalogView.id())) {
            return;
        }
        itemCatalogEntries.clear();
        itemCatalogEntries.addAll(payload.entries());
        loadedItemCatalogViewId = payload.viewId();
        pendingItemCatalogViewId = "";
        itemScroll = clamp(itemScroll, 0, itemMaxScroll());
    }

    public Rect2i getAcceptedGridArea() {
        return new Rect2i(leftPos + acceptedGridX(), topPos + acceptedGridY(), gridWidth(), statusGridHeight());
    }

    public Rect2i getDiscardedGridArea() {
        return new Rect2i(leftPos + discardedGridX(), topPos + discardedGridY(), gridWidth(), statusGridHeight());
    }

    public void acceptGhostItem(ItemStack stack, DumpNullItemStatus status) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
            return;
        }
        selectItem(itemId);
        setItemStatus(itemId, status);
    }

    private EditBox addBox(int x, int y, int width, String value) {
        EditBox box = new EditBox(font, x, y, Math.max(24, width), 18, Component.literal(""));
        box.setMaxLength(256);
        box.setValue(value);
        addRenderableWidget(box);
        return box;
    }

    private void onMobSearchChanged() {
        if (catalogMode == CatalogMode.ITEMS) {
            itemScroll = 0;
            return;
        }
        String query = currentDropSearchQuery();
        if (query.length() < DROP_SEARCH_MIN_LENGTH) {
            dropSearchMobMatches.clear();
            pendingDropSearchQuery = "";
            activeDropSearchQuery = "";
            lastDropSearchRequestQuery = "";
            dropSearchCooldownTicks = 0;
        } else if (!query.equals(activeDropSearchQuery)) {
            dropSearchMobMatches.clear();
            pendingDropSearchQuery = query;
            lastDropSearchRequestQuery = "";
            dropSearchCooldownTicks = DROP_SEARCH_DEBOUNCE_TICKS;
        }
        mobScroll = clamp(mobScroll, 0, mobMaxScroll());
        ensureFocusedMobValid();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (catalogMode == CatalogMode.MOBS) {
            tickDropSearch();
        }
    }

    private void tickDropSearch() {
        String query = currentDropSearchQuery();
        if (query.length() < DROP_SEARCH_MIN_LENGTH) {
            return;
        }
        if (!query.equals(pendingDropSearchQuery) && !query.equals(activeDropSearchQuery)) {
            pendingDropSearchQuery = query;
            dropSearchCooldownTicks = DROP_SEARCH_DEBOUNCE_TICKS;
        }
        if (dropSearchCooldownTicks > 0 && --dropSearchCooldownTicks > 0) {
            return;
        }
        if (!query.equals(lastDropSearchRequestQuery) && !query.equals(activeDropSearchQuery)) {
            lastDropSearchRequestQuery = query;
            PacketDistributor.sendToServer(new DumpNullPayloads.SearchMobDropsPayload(menu.containerId, mobSearchBox.getValue()));
        }
    }

    private void switchCatalogMode(CatalogMode mode) {
        if (catalogMode == mode) {
            return;
        }
        catalogMode = mode;
        presetsExpanded = false;
        itemViewsExpanded = false;
        if (mode == CatalogMode.ITEMS) {
            itemScroll = 0;
            requestItemCatalogIfNeeded();
        } else if (mode == CatalogMode.MOBS) {
            mobScroll = clamp(mobScroll, 0, mobMaxScroll());
            ensureFocusedMobValid();
        }
        updateWidgetVisibility();
    }

    private void cycleSetupPreset(int delta) {
        List<DumpNullPresetOption> options = menu.getPresetOptions();
        if (options.isEmpty()) {
            return;
        }
        selectedPresetIndex = Math.floorMod(selectedPresetIndex + delta, options.size());
        presetsExpanded = false;
        updateWidgetVisibility();
    }

    private void previewSelectedSetupPreset() {
        DumpNullPresetOption option = selectedPresetOption();
        if (option != null) {
            stagePreset(option);
        }
    }

    private DumpNullPresetOption selectedPresetOption() {
        List<DumpNullPresetOption> options = menu.getPresetOptions();
        if (options.isEmpty()) {
            return null;
        }
        selectedPresetIndex = clamp(selectedPresetIndex, 0, options.size() - 1);
        return options.get(selectedPresetIndex);
    }

    private Component currentPresetLabel() {
        DumpNullPresetOption option = selectedPresetOption();
        return option == null ? Component.literal("Setup") : shortPresetLabel(option);
    }

    private void cycleItemCatalogView(int delta) {
        List<DumpNullItemCatalogView> views = DumpNullItemCatalogView.all();
        int index = views.indexOf(itemCatalogView);
        int next = Math.floorMod((index < 0 ? 0 : index) + delta, views.size());
        setItemCatalogView(views.get(next));
    }

    private void setItemCatalogView(DumpNullItemCatalogView view) {
        itemCatalogView = view == null ? DumpNullItemCatalogView.ORES : view;
        itemViewsExpanded = false;
        itemCatalogEntries.clear();
        loadedItemCatalogViewId = "";
        pendingItemCatalogViewId = "";
        itemScroll = 0;
        requestItemCatalogIfNeeded();
        updateWidgetVisibility();
    }

    private Component currentItemViewLabel() {
        return Component.literal(itemCatalogView.label());
    }

    private void requestItemCatalogIfNeeded() {
        if (catalogMode != CatalogMode.ITEMS || loadedItemCatalogViewId.equals(itemCatalogView.id()) || pendingItemCatalogViewId.equals(itemCatalogView.id())) {
            return;
        }
        pendingItemCatalogViewId = itemCatalogView.id();
        PacketDistributor.sendToServer(new DumpNullPayloads.RequestItemCatalogPayload(menu.containerId, itemCatalogView.id()));
    }

    private void renderPendingPresetLabel(GuiGraphics guiGraphics) {
        if (pendingPresetId.isBlank()) {
            return;
        }
        String label = "Apply " + pendingPresetId + (presetNeedsTargets() ? " (" + pendingTargetIds.size() + " targets)" : "");
        guiGraphics.drawString(font, trimToWidth(label, imageWidth - PADDING * 2), PADDING + font.width(title.getString()) + 8, 6, 0xFFFFD88A, false);
    }

    private void renderStatusGrid(GuiGraphics guiGraphics, DumpNullItemStatus status, int x, int y, int width, int height, int scroll, int mouseX, int mouseY) {
        String title = status == DumpNullItemStatus.ACCEPTED ? "Accepted" : "Discarded";
        int titleColor = status == DumpNullItemStatus.ACCEPTED ? 0xFFA9F5B6 : 0xFFFFA3A3;
        guiGraphics.drawString(font, title, x, y - GRID_HEADER_HEIGHT, titleColor, false);
        guiGraphics.fill(x, y, x + width, y + height, 0x852F3742);
        guiGraphics.enableScissor(leftPos + x, topPos + y, leftPos + x + width, topPos + y + height);
        int tileWidth = statusTileWidth(width);
        for (StatusTile tile : statusTiles(status, x, y, width, scroll)) {
            if (tile.y() + GRID_TILE_HEIGHT < y || tile.y() > y + height) {
                continue;
            }
            DumpNullItemStatusSummary summary = tile.summary();
            int border = status == DumpNullItemStatus.ACCEPTED ? 0xFF54D878 : 0xFFFF6868;
            boolean selectedForEditing = summary.itemId().equals(selectedItemId);
            boolean hoveringTile = inside(mouseX, mouseY, tile.x(), tile.y(), tileWidth - 2, GRID_TILE_HEIGHT - 2);
            boolean hoveringRemove = hoveringTile && tileRemoveAt(mouseX, mouseY, tile);
            guiGraphics.fill(tile.x(), tile.y(), tile.x() + tileWidth - 2, tile.y() + GRID_TILE_HEIGHT - 2, 0xC81A1F26);
            guiGraphics.renderOutline(tile.x(), tile.y(), tileWidth - 2, GRID_TILE_HEIGHT - 2, border);
            if (selectedForEditing) {
                guiGraphics.renderOutline(tile.x() - 1, tile.y() - 1, tileWidth, GRID_TILE_HEIGHT, 0xFFFFD766);
                guiGraphics.fill(tile.x(), tile.y(), tile.x() + tileWidth - 2, tile.y() + 2, 0x88FFD766);
            }
            ItemStack stack = itemStack(summary.itemId());
            guiGraphics.renderItem(stack, tile.x() + Math.max(1, (tileWidth - 18) / 2), tile.y() + Math.max(1, (GRID_TILE_HEIGHT - 18) / 2));
            if (hoveringTile && !hoveringRemove) {
                hoveredItem = stack;
            }
            if (hoveringTile) {
                int removeX = tile.x() + tileWidth - TILE_REMOVE_SIZE - 3;
                int removeY = tile.y() + 2;
                guiGraphics.fill(removeX, removeY, removeX + TILE_REMOVE_SIZE, removeY + TILE_REMOVE_SIZE, hoveringRemove ? 0xFFE15C5C : 0xCC252B33);
                guiGraphics.drawCenteredString(font, "x", removeX + TILE_REMOVE_SIZE / 2, removeY, 0xFFFFFFFF);
                if (hoveringRemove) {
                    offerManualTooltip("tile-remove:" + summary.itemId(), Component.literal("Remove this item from DumpNull rules."));
                } else {
                    offerManualTooltip("tile-toggle:" + summary.itemId(), Component.literal("Left click edits filters. Right click moves it to the other list."));
                }
            }
            if (summary.advanced()) {
                guiGraphics.fill(tile.x() + tileWidth - 10, tile.y() + 2, tile.x() + tileWidth - 4, tile.y() + 8, 0xFFFFD766);
            }
            if (summary.presetGenerated()) {
                guiGraphics.fill(tile.x() + 2, tile.y() + 2, tile.x() + 8, tile.y() + 8, 0xFF73A8FF);
            }
        }
        guiGraphics.disableScissor();
        renderScrollbar(guiGraphics, ScrollRegion.status(status), x + width - SCROLLBAR_WIDTH - 1, y + 1, height - 2, scroll, gridMaxScroll(status));
        if (statusSummaries(status).isEmpty()) {
            guiGraphics.drawCenteredString(font, "No items", x + width / 2, y + 20, 0xFF8B96A3);
        }
    }

    private void renderDetailPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = detailX();
        int y = detailY();
        int width = detailWidth();
        String titleText = selectedItemId == null ? "Filters" : "Filters: " + itemStack(selectedItemId).getHoverName().getString();
        guiGraphics.drawString(font, trimToWidth(titleText, width), x, y, 0xFFE8EDF5, false);
        guiGraphics.fill(x, y + 12, x + width, y + detailHeight(), 0x852F3742);
        if (selectedItemId == null) {
            guiGraphics.drawString(font, "Click an item", x + 6, y + 24, 0xFF95A0AD, false);
            guiGraphics.drawString(font, "to edit filters", x + 6, y + 34, 0xFF95A0AD, false);
            return;
        }

        int minX = x + 6;
        int reqX = x + 6;
        int banX = x + 10 + Math.max(42, (width - 14) / 2);
        guiGraphics.drawString(font, "Min", minX, y + 25, 0xFF95A0AD, false);
        guiGraphics.drawString(font, "Req", reqX, y + 42, 0xFF95A0AD, false);
        guiGraphics.drawString(font, "Ban", banX, y + 42, 0xFF95A0AD, false);
        offerLabelTooltip(mouseX, mouseY, minX, y + 25, "Min", "filter-min", Component.literal("Minimum durability remaining percent required. 0 disables durability filtering."));
        offerLabelTooltip(mouseX, mouseY, reqX, y + 42, "Req", "filter-req", Component.literal("Required enchantment ids. Separate multiple ids with commas."));
        offerLabelTooltip(mouseX, mouseY, banX, y + 42, "Ban", "filter-ban", Component.literal("Forbidden enchantment ids. Separate multiple ids with commas."));
    }

    private void offerLabelTooltip(int mouseX, int mouseY, int x, int y, String label, String key, Component tooltip) {
        if (inside(mouseX, mouseY, x, y, font.width(label), font.lineHeight)) {
            offerManualTooltip(key, tooltip);
        }
    }

    private boolean catalogControlWidgetAt(double mouseX, double mouseY) {
        if (mobSearchBox != null && mobSearchBox.visible && mobSearchBox.isMouseOver(mouseX, mouseY)) {
            return true;
        }
        for (Button button : List.of(
                mobsModeButton, itemsModeButton, automationModeButton,
                presetPreviousButton, presetsButton, presetNextButton, previewPresetButton,
                itemViewPreviousButton, itemViewButton, itemViewNextButton,
                heldDiscardModeButton, dockDiscardModeButton, installPowerUpgradeButton
        )) {
            if (button != null && button.visible && button.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        if (expandAllButton != null && expandAllButton.visible && expandAllButton.isMouseOver(mouseX, mouseY)) {
            return true;
        }
        if (collapseAllButton != null && collapseAllButton.visible && collapseAllButton.isMouseOver(mouseX, mouseY)) {
            return true;
        }
        for (Button button : filterButtons.values()) {
            if (button.visible && button.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private void renderHeaderIconButton(GuiGraphics guiGraphics, Button button, boolean active, ResourceLocation icon, int iconSize, String tooltipKey, Component tooltip) {
        if (button == null || !button.visible) {
            return;
        }
        int localX = button.getX() - leftPos;
        int localY = button.getY() - topPos;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 40.0F);
        renderTextureIconButton(guiGraphics, icon, iconSize, localX, localY, active, Integer.MIN_VALUE, Integer.MIN_VALUE, tooltipKey, tooltip);
        guiGraphics.pose().popPose();
    }

    private void renderTextureIconButton(GuiGraphics guiGraphics, ResourceLocation icon, int iconSize, int x, int y, boolean active, int mouseX, int mouseY, String tooltipKey, Component tooltip) {
        boolean hovering = inside(mouseX, mouseY, x, y, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE);
        int fill = active ? (hovering ? 0xF02B3340 : 0xD7212832) : 0x99212730;
        guiGraphics.fill(x, y, x + ICON_BUTTON_SIZE, y + ICON_BUTTON_SIZE, fill);
        guiGraphics.renderOutline(x, y, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE, active ? 0xFF73A8FF : 0xFF48505B);
        if (active) {
            int inset = Math.max(0, (ICON_BUTTON_SIZE - iconSize) / 2);
            guiGraphics.blit(icon, x + inset, y + inset, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
        }
        if (hovering) {
            offerManualTooltip(tooltipKey, tooltip);
        }
    }

    private void renderPresetDropdown(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!presetsExpanded || pendingPresetId != null && !pendingPresetId.isBlank()) {
            return;
        }
        int x = presetDropdownX();
        int y = presetDropdownY();
        int width = presetDropdownWidth();
        int height = presetDropdownHeight();
        guiGraphics.fill(x, y, x + width, y + height, 0xFA252B33);
        guiGraphics.renderOutline(x, y, width, height, 0xFF657080);
        for (int i = 0; i < menu.getPresetOptions().size(); i++) {
            DumpNullPresetOption option = menu.getPresetOptions().get(i);
            int rowY = y + 2 + i * presetDropdownRowHeight();
            boolean hovering = inside(mouseX, mouseY, x + 2, rowY, width - 4, presetDropdownRowHeight());
            if (hovering) {
                guiGraphics.fill(x + 2, rowY, x + width - 2, rowY + presetDropdownRowHeight(), 0xF02B3340);
                offerManualTooltip("preset-option:" + option.presetId(), setupPresetDescription(option));
            }
            guiGraphics.drawString(font, trimToWidth(shortPresetLabel(option).getString(), width - 8), x + 5, rowY + 4, 0xFFE8EDF5, false);
        }
    }

    private void renderItemViewDropdown(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!itemViewsExpanded || catalogMode != CatalogMode.ITEMS) {
            return;
        }
        int x = itemViewDropdownX();
        int y = itemViewDropdownY();
        int width = itemViewDropdownWidth();
        int height = itemViewDropdownHeight();
        guiGraphics.fill(x, y, x + width, y + height, 0xFA20262E);
        guiGraphics.renderOutline(x, y, width, height, 0xFF657080);
        List<DumpNullItemCatalogView> views = DumpNullItemCatalogView.all();
        for (int i = 0; i < views.size(); i++) {
            DumpNullItemCatalogView view = views.get(i);
            int rowY = y + 2 + i * itemViewDropdownRowHeight();
            boolean hovering = inside(mouseX, mouseY, x + 2, rowY, width - 4, itemViewDropdownRowHeight());
            boolean selected = view == itemCatalogView;
            if (hovering || selected) {
                guiGraphics.fill(x + 2, rowY, x + width - 2, rowY + itemViewDropdownRowHeight(), hovering ? 0xF02B3340 : 0xCC27313C);
                if (hovering) {
                    offerManualTooltip("item-view:" + view.id(), Component.literal(view.description()));
                }
            }
            guiGraphics.drawString(font, trimToWidth(view.label(), width - 8), x + 5, rowY + 4, 0xFFE8EDF5, false);
        }
    }

    private void renderAutomationView(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = automationControlX();
        int y = automationListY();
        guiGraphics.drawString(font, Component.translatable("dn.dumpnull.face_routing"), x, y, 0xFFE8EDF5, false);
        renderFaceMap(guiGraphics, mouseX, mouseY);
        renderFaceDetailPanel(guiGraphics, mouseX, mouseY);

        renderAutomationPower(guiGraphics, mouseX, mouseY);
        renderBufferPreview(guiGraphics, "Input Buffer", menu.getData().inputBuffer(), ribbonX() + 6, ribbonY() + 62, mouseX, mouseY);
        renderBufferPreview(guiGraphics, "Output Buffer", menu.getData().outputBuffer(), ribbonX() + 6, ribbonY() + 112, mouseX, mouseY);
        renderBufferPreview(guiGraphics, "Discard Buffer", menu.getData().discardBuffer(), ribbonX() + 6, ribbonY() + 162, mouseX, mouseY);
    }

    private void renderFaceMap(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (Direction direction : Direction.values()) {
            Rect2i rect = faceRect(direction);
            DumpNullSideMode mode = menu.getData().sideMode(direction);
            boolean selected = direction == selectedAutomationFace;
            boolean hover = inside(mouseX, mouseY, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            int fill = hover ? 0xF03C4A5A : 0xD7252D37;
            guiGraphics.fill(rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), fill);
            guiGraphics.renderOutline(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), selected ? 0xFFFFFFFF : modeColor(mode));
            if (menu.getData().isDiscardExportSide(direction)) {
                guiGraphics.fill(rect.getX() + rect.getWidth() - 7, rect.getY() + 2, rect.getX() + rect.getWidth() - 2, rect.getY() + 7, 0xFFFFB66B);
            }
            guiGraphics.drawCenteredString(font, faceShortLabel(direction), rect.getX() + rect.getWidth() / 2, rect.getY() + 5, 0xFFE8EDF5);
            guiGraphics.drawCenteredString(font, sideModeLabel(mode), rect.getX() + rect.getWidth() / 2, rect.getY() + 17, 0xFFB9C1CB);
            if (hover) {
                offerManualTooltip("face:" + direction.getName(), Component.translatable("dn.dumpnull.face.tooltip", directionLabel(direction), sideModeLabel(mode)));
            }
        }
    }

    private void renderFaceDetailPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = automationControlX() + 116;
        int y = automationListY() + 16;
        int width = Math.max(112, catalogWidth() - 120);
        DumpNullSideMode mode = menu.getData().sideMode(selectedAutomationFace);
        guiGraphics.fill(x, y, x + width, y + 88, 0xC81A1F26);
        guiGraphics.renderOutline(x, y, width, 88, 0xFF3C4654);
        guiGraphics.drawString(font, Component.translatable("dn.dumpnull.face.selected", directionLabel(selectedAutomationFace)), x + 6, y + 6, 0xFFE8EDF5, false);
        guiGraphics.drawString(font, Component.translatable("dn.dumpnull.face.mode"), x + 6, y + 22, 0xFF95A0AD, false);
        int buttonY = y + 34;
        int buttonX = x + 6;
        for (DumpNullSideMode candidate : DumpNullSideMode.values()) {
            int buttonWidth = 30;
            boolean hover = inside(mouseX, mouseY, buttonX, buttonY, buttonWidth, 14);
            guiGraphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + 14, hover ? 0xF03C4A5A : 0xD7252D37);
            guiGraphics.renderOutline(buttonX, buttonY, buttonWidth, 14, candidate == mode ? 0xFFFFFFFF : modeColor(candidate));
            guiGraphics.drawCenteredString(font, sideModeLabel(candidate), buttonX + buttonWidth / 2, buttonY + 3, 0xFFE8EDF5);
            buttonX += buttonWidth + 4;
        }
        int discardY = y + 58;
        boolean discard = menu.getData().isDiscardExportSide(selectedAutomationFace);
        boolean hover = inside(mouseX, mouseY, x + 6, discardY, width - 12, 16);
        guiGraphics.fill(x + 6, discardY, x + width - 6, discardY + 16, hover ? 0xF03C4A5A : 0xD7252D37);
        guiGraphics.renderOutline(x + 6, discardY, width - 12, 16, discard ? 0xFFFFB66B : 0xFF566170);
        guiGraphics.drawCenteredString(font, Component.translatable(discard ? "dn.dumpnull.face.discard_on" : "dn.dumpnull.face.discard_off"),
                x + width / 2, discardY + 4, discard ? 0xFFFFD19A : 0xFFB9C1CB);
        if (hover) {
            offerManualTooltip("face-discard:" + selectedAutomationFace.getName(), Component.translatable("dn.dumpnull.face.discard_tooltip"));
        }
    }

    private void renderAutomationPower(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        DumpNullData data = menu.getData();
        int x = ribbonX() + 6;
        int y = ribbonY() + 8;
        int width = gridWidth() - 4;
        boolean installed = data.hasUpgrade(DumpNullUpgradeType.POWER);
        guiGraphics.drawString(font, "Power", x, y, 0xFFE8EDF5, false);
        guiGraphics.fill(x, y + 14, x + width, y + 24, 0xCC11151B);
        guiGraphics.renderOutline(x, y + 14, width, 10, installed ? 0xFF73A8FF : 0xFF48505B);
        if (installed && data.energyCapacity() > 0) {
            int filled = Math.max(0, Math.min(width - 2, Math.round((width - 2) * (data.storedEnergy() / (float) data.energyCapacity()))));
            guiGraphics.fill(x + 1, y + 15, x + 1 + filled, y + 23, 0xFF65C8FF);
        }
        String text = installed ? data.storedEnergy() + " / " + data.energyCapacity() + " FE" : "Upgrade not installed";
        guiGraphics.drawString(font, trimToWidth(text, width), x, y + 28, installed ? 0xFFBFE7FF : 0xFF95A0AD, false);
        if (inside(mouseX, mouseY, x, y + 14, width, 10)) {
            offerManualTooltip("automation-power", Component.literal("DumpNull FE is generated from discarded items or exposed through the dock when the power upgrade is installed."));
        }
    }

    private void renderBufferPreview(GuiGraphics guiGraphics, String title, List<DumpNullBufferEntry> entries, int x, int y, int mouseX, int mouseY) {
        int width = gridWidth() - 4;
        guiGraphics.drawString(font, title, x, y, 0xFFE8EDF5, false);
        guiGraphics.fill(x, y + 12, x + width, y + 45, 0x852F3742);
        if (entries.isEmpty()) {
            guiGraphics.drawString(font, "Empty", x + 6, y + 24, 0xFF95A0AD, false);
            return;
        }
        int iconX = x + 4;
        int shown = Math.min(4, entries.size());
        for (int i = 0; i < shown; i++) {
            DumpNullBufferEntry entry = entries.get(i);
            ItemStack stack = entry.stack();
            int itemX = iconX + i * 24;
            guiGraphics.renderItem(stack, itemX, y + 20);
            guiGraphics.drawString(font, shortCount(entry.count()), itemX + 10, y + 34, 0xFFFFFFFF, true);
            if (inside(mouseX, mouseY, itemX, y + 20, 18, 18)) {
                hoveredItem = stack;
            }
        }
        if (entries.size() > shown) {
            guiGraphics.drawString(font, "+" + (entries.size() - shown), x + width - 22, y + 28, 0xFFB9C1CB, false);
        }
    }

    private void renderMobCatalog(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int catalogHeight = catalogHeight();
        int listY = CATALOG_Y + CATALOG_CONTROLS_HEIGHT;
        int listHeight = catalogHeight - CATALOG_CONTROLS_HEIGHT - 4;
        guiGraphics.enableScissor(leftPos + PADDING, topPos + listY, leftPos + catalogRight(), topPos + listY + listHeight);
        List<CatalogRow> entries = catalogRows();
        ensureFocusedMobValid();
        int y = listY - mobScroll;
        List<String> needsDropRows = new ArrayList<>();
        for (CatalogRow entry : entries) {
            int entryHeight = entry.height();
            if (y + entryHeight >= listY && y <= listY + listHeight) {
                if (entry.type() == CatalogRowType.MOD_HEADER) {
                    renderNamespaceHeader(guiGraphics, entry, y);
                } else if (entry.type() == CatalogRowType.CATEGORY_HEADER) {
                    renderCategoryHeader(guiGraphics, entry, y);
                } else {
                    int cardWidth = catalogCardWidth();
                    for (int i = 0; i < entry.options().size(); i++) {
                        DumpNullMobOption option = entry.options().get(i);
                        int cardX = PADDING + i * (cardWidth + MOB_CARD_GAP);
                        renderMobCard(guiGraphics, option, cardX, y, cardWidth, mouseX, mouseY);
                        ResourceLocation entityId = option.entityId();
                        if (!dropRows.containsKey(entityId) && pendingDropRequests.add(entityId)) {
                            needsDropRows.add(entityId.toString());
                        }
                    }
                }
            }
            y += entryHeight;
        }
        guiGraphics.disableScissor();
        renderScrollbar(guiGraphics, ScrollRegion.catalog(), catalogRight() - SCROLLBAR_WIDTH - 1, listY + 1, listHeight - 2, mobScroll, mobMaxScroll());
        if (entries.isEmpty()) {
            guiGraphics.drawCenteredString(font, "No mobs match", PADDING + catalogWidth() / 2, listY + 28, 0xFF8B96A3);
        }
        if (!needsDropRows.isEmpty()) {
            PacketDistributor.sendToServer(new DumpNullPayloads.RequestMobDropsPayload(menu.containerId, needsDropRows));
        }
    }

    private void renderItemCatalog(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        requestItemCatalogIfNeeded();
        int listY = catalogListY();
        int listHeight = catalogListHeight();
        List<DumpNullItemCatalogEntry> entries = filteredItemCatalogEntries();
        int columns = itemCatalogColumns();
        int contentHeight = itemCatalogContentHeight(entries.size());
        itemScroll = clamp(itemScroll, 0, itemMaxScroll());

        guiGraphics.drawString(font, trimToWidth("Catalog View: " + itemCatalogView.label(), catalogWidth() - 16), PADDING, CATALOG_Y + 50, 0xFF95A0AD, false);
        guiGraphics.enableScissor(leftPos + PADDING, topPos + listY, leftPos + catalogRight(), topPos + listY + listHeight);
        int baseY = listY - itemScroll;
        for (int i = 0; i < entries.size(); i++) {
            DumpNullItemCatalogEntry entry = entries.get(i);
            int column = i % columns;
            int row = i / columns;
            int x = PADDING + column * (ITEM_TILE_SIZE + ITEM_TILE_GAP);
            int y = baseY + row * (ITEM_TILE_SIZE + ITEM_TILE_GAP);
            if (y + ITEM_TILE_SIZE < listY || y > listY + listHeight) {
                continue;
            }
            renderItemCatalogTile(guiGraphics, entry, x, y, mouseX, mouseY);
        }
        guiGraphics.disableScissor();
        renderScrollbar(guiGraphics, ScrollRegion.catalog(), catalogRight() - SCROLLBAR_WIDTH - 1, listY + 1, listHeight - 2, itemScroll, Math.max(0, contentHeight - listHeight));
        if (entries.isEmpty()) {
            String message = loadedItemCatalogViewId.equals(itemCatalogView.id()) ? "No items match" : "Loading items";
            guiGraphics.drawCenteredString(font, message, PADDING + catalogWidth() / 2, listY + 28, 0xFF8B96A3);
        }
    }

    private void renderItemCatalogTile(GuiGraphics guiGraphics, DumpNullItemCatalogEntry entry, int x, int y, int mouseX, int mouseY) {
        DumpNullItemStatus status = statusOf(entry.itemId());
        boolean selectedForEditing = entry.itemId().equals(selectedItemId);
        boolean hovering = inside(mouseX, mouseY, x, y, ITEM_TILE_SIZE, ITEM_TILE_SIZE);
        guiGraphics.fill(x, y, x + ITEM_TILE_SIZE, y + ITEM_TILE_SIZE, 0xD012171D);
        guiGraphics.renderOutline(x, y, ITEM_TILE_SIZE, ITEM_TILE_SIZE, statusColor(status));
        if (selectedForEditing) {
            guiGraphics.renderOutline(x - 1, y - 1, ITEM_TILE_SIZE + 2, ITEM_TILE_SIZE + 2, 0xFFFFD766);
        }
        guiGraphics.renderItem(itemStack(entry.itemId()), x + 4, y + 4);
        if (hovering) {
            hoveredTooltipLines = itemCatalogTooltip(entry, status);
        }
    }

    private List<Component> itemCatalogTooltip(DumpNullItemCatalogEntry entry, DumpNullItemStatus status) {
        ItemStack stack = itemStack(entry.itemId());
        return List.of(
                stack.getHoverName(),
                Component.literal(entry.itemId().toString()),
                Component.literal("Status: " + statusLabel(status)),
                Component.literal("Source: " + entry.source()),
                Component.literal("Left-click cycles. Right-click toggles discard/accept.")
        );
    }

    private void renderNamespaceHeader(GuiGraphics guiGraphics, CatalogRow row, int y) {
        String namespace = row.namespace();
        guiGraphics.fill(PADDING, y, catalogRight(), y + MOB_HEADER_HEIGHT - 2, 0xC8303741);
        String marker = collapsedNamespaces.contains(namespace) ? "+" : "-";
        guiGraphics.drawString(font, marker, PADDING + 5, y + 3, 0xFFE8EDF5, false);
        String label = ("minecraft".equals(namespace) ? "Minecraft" : namespace) + " (" + row.count() + ")";
        guiGraphics.drawString(font, trimToWidth(label, catalogWidth() - 24), PADDING + 18, y + 3, 0xFFFFD88A, false);
    }

    private void renderCategoryHeader(GuiGraphics guiGraphics, CatalogRow row, int y) {
        guiGraphics.fill(PADDING + 6, y, catalogRight(), y + CATEGORY_HEADER_HEIGHT - 2, 0xB42A3039);
        String key = categoryKey(row.namespace(), row.category());
        String marker = collapsedCategories.contains(key) ? "+" : "-";
        guiGraphics.drawString(font, marker, PADDING + 12, y + 2, 0xFFE8EDF5, false);
        String label = categoryLabel(row.category()) + " (" + row.count() + ")";
        guiGraphics.drawString(font, trimToWidth(label, catalogWidth() - 34), PADDING + 25, y + 2, 0xFFB8C2CF, false);
    }

    private void renderMobCard(GuiGraphics guiGraphics, DumpNullMobOption option, int x, int y, int width, int mouseX, int mouseY) {
        boolean focused = option.entityId().equals(focusedMobId);
        boolean cardHovered = inside(mouseX, mouseY, x, y, width, MOB_CARD_HEIGHT);
        boolean detailsMode = hasShiftDown();
        int borderColor = focused ? 0xFFFFD766 : option.selected() ? 0xFF73D889 : 0xFF48505B;
        int previewHeight = cardPreviewHeight();
        guiGraphics.fill(x, y, x + width, y + MOB_CARD_HEIGHT, 0xC81A1F26);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + previewHeight, 0xB010151B);
        guiGraphics.renderOutline(x, y, width, MOB_CARD_HEIGHT, borderColor);
        if (focused) {
            guiGraphics.renderOutline(x + 1, y + 1, width - 2, MOB_CARD_HEIGHT - 2, 0x88FFD766);
        }
        if (detailsMode) {
            guiGraphics.fill(x + 4, y + 4, x + 4 + Math.min(width - 24, font.width(option.category()) + 4), y + 14, 0x88242A32);
            guiGraphics.drawString(font, trimToWidth(option.category().toLowerCase(Locale.ROOT), width - 28), x + 6, y + 5, 0x998B96A3, false);
        }
        int previewY = detailsMode ? y + 15 : y + 6;
        int previewDrawHeight = previewHeight - (detailsMode ? 18 : 9);
        renderEntityPreview(guiGraphics, option, x + 8, previewY, width - 16, previewDrawHeight);
        guiGraphics.drawCenteredString(font, trimToWidth(mobName(option), width - 8), x + width / 2, y + previewHeight - 10, 0xFFE8EDF5);

        int dropsX = x + 6;
        int dropsY = y + previewHeight + 4;
        int dropsWidth = width - 12;
        int dropsHeight = MOB_CARD_HEIGHT - previewHeight - 8;
        boolean dropsLoaded = dropRows.containsKey(option.entityId());
        List<DumpNullDropCandidate> candidates = uniqueDrops(dropRows.get(option.entityId()));
        if (!dropsLoaded) {
            guiGraphics.drawCenteredString(font, "Loading", x + width / 2, dropsY + 10, 0xFF95A0AD);
            return;
        }
        if (candidates.isEmpty()) {
            guiGraphics.drawCenteredString(font, "No drops", x + width / 2, dropsY + 10, 0xFF95A0AD);
            return;
        }
        int columns = Math.max(1, dropsWidth / (DROP_ICON_SIZE + DROP_ICON_GAP));
        int rows = Math.max(1, dropsHeight / (DROP_ICON_SIZE + DROP_ICON_GAP));
        int maxIcons = columns * rows;
        int totalTiles = dropTileCount(candidates);
        int scroll = cardDropScroll(option.entityId(), totalTiles, maxIcons);
        for (int visibleIndex = 0; visibleIndex < maxIcons && visibleIndex + scroll < totalTiles; visibleIndex++) {
            int tileIndex = visibleIndex + scroll;
            int row = visibleIndex / columns;
            int rowCount = Math.min(columns, totalTiles - scroll - row * columns);
            int rowWidth = rowCount * DROP_ICON_SIZE + Math.max(0, rowCount - 1) * DROP_ICON_GAP;
            int rowX = dropsX + Math.max(0, (dropsWidth - rowWidth) / 2);
            int iconX = rowX + (visibleIndex % columns) * (DROP_ICON_SIZE + DROP_ICON_GAP);
            int iconY = dropsY + (visibleIndex / columns) * (DROP_ICON_SIZE + DROP_ICON_GAP);
            boolean allTile = isAllDropTile(candidates, tileIndex);
            DumpNullItemStatus status = allTile ? aggregateStatus(candidates) : statusOf(candidates.get(candidateIndex(candidates, tileIndex)).itemId());
            int border = statusColor(status);
            guiGraphics.fill(iconX, iconY, iconX + DROP_ICON_SIZE, iconY + DROP_ICON_SIZE, 0xD00F1318);
            if (status != DumpNullItemStatus.NEUTRAL) {
                guiGraphics.renderOutline(iconX, iconY, DROP_ICON_SIZE, DROP_ICON_SIZE, border);
            }
            if (allTile) {
                guiGraphics.drawCenteredString(font, "All", iconX + DROP_ICON_SIZE / 2, iconY + 5, 0xFFE8EDF5);
            } else {
                ItemStack stack = itemStack(candidates.get(candidateIndex(candidates, tileIndex)).itemId());
                guiGraphics.renderItem(stack, iconX + 1, iconY + 1);
            }
            if (inside(mouseX, mouseY, iconX, iconY, DROP_ICON_SIZE, DROP_ICON_SIZE)) {
                if (allTile) {
                    offerManualTooltip("drop-all:" + option.entityId(), Component.literal("Left-click cycles all loaded drops. Right-click discards neutral/accepted drops or accepts discarded drops."));
                } else {
                    hoveredItem = itemStack(candidates.get(candidateIndex(candidates, tileIndex)).itemId());
                }
            }
        }
        if (totalTiles > maxIcons) {
            renderScrollbar(guiGraphics, ScrollRegion.cardDrops(option.entityId()), x + width - 6, dropsY, dropsHeight, scroll, totalTiles - maxIcons);
        }
        if (cardHovered && dropRows.containsKey(option.entityId())) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 35.0F);
            renderTextureIconButton(
                    guiGraphics,
                    COPY_ICON,
                    COPY_ICON_SIZE,
                    x + width - ICON_BUTTON_SIZE - 4,
                    y + 4,
                    true,
                    mouseX,
                    mouseY,
                    "card-copy:" + option.entityId(),
                    Component.literal("Copy this mob's loaded drops, one per line.")
            );
            guiGraphics.pose().popPose();
        }
    }

    private void renderEntityPreview(GuiGraphics guiGraphics, DumpNullMobOption option, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0x9010151B);
        Entity entity = previewEntity(option.entityId());
        if (entity instanceof LivingEntity livingEntity) {
            float entityHeight = Math.max(0.6F, livingEntity.getBbHeight());
            int scale = Math.max(10, Math.min(24, Math.round(22.0F / entityHeight)));
            InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, x + 2, y + 2, x + width - 2, y + height - 2, scale, 0.15F, 0.0F, 0.0F, livingEntity);
        } else {
            guiGraphics.drawCenteredString(font, "?", x + width / 2, y + 14, 0xFFE8EDF5);
        }
    }

    private void renderPresetOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (pendingPresetId.isBlank()) {
            return;
        }
        int x = overlayX();
        int y = overlayY();
        int width = overlayWidth();
        int height = overlayHeight();
        guiGraphics.fill(0, 0, imageWidth, imageHeight, 0xDD000000);
        guiGraphics.fill(x, y, x + width, y + height, 0xFC252B33);
        guiGraphics.renderOutline(x, y, width, height, 0xFF657080);
        guiGraphics.drawString(font, trimToWidth("Apply " + pendingPresetId + " preset?", width - 88), x + 10, y + 8, 0xFFE8EDF5, false);
        if (presetPreviewResolved) {
            String summary = "Selected mobs: " + presetPreviewMobIds.size()
                    + "  Accepted: " + presetPreviewAcceptedSummaries.size()
                    + "  Discarded: " + presetPreviewDiscardedSummaries.size();
            guiGraphics.drawString(font, trimToWidth(summary, width - 20), x + 10, y + 20, 0xFF95A0AD, false);
        }
        if (presetNeedsTargets()) {
            renderPresetTargets(guiGraphics, x, y, width);
        }
        renderPresetPreviewContent(guiGraphics, x, y, width, height, mouseX, mouseY);
    }

    private void renderPresetTargets(GuiGraphics guiGraphics, int x, int y, int width) {
        int rowY = presetTargetListY();
        int rows = 3;
        List<DumpNullPresetTarget> targets = filteredTargets();
        for (int i = 0; i < targets.size() && i < rows; i++) {
            DumpNullPresetTarget target = targets.get(i);
            boolean selected = pendingTargetIds.contains(target.id().toString());
            int color = selected ? 0xFFA9F5B6 : 0xFFE8EDF5;
            guiGraphics.drawString(font, (selected ? "[x] " : "[ ] ") + trimToWidth(target.label(), width - 28), x + 10, rowY + i * 12, color, false);
        }
        if (targets.isEmpty()) {
            guiGraphics.drawString(font, "No targets match", x + 10, rowY, 0xFF95A0AD, false);
        }
    }

    private void renderPresetPreviewContent(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        int previewY = presetPreviewY();
        int previewHeight = presetPreviewHeight();
        if (!presetPreviewResolved) {
            String message = presetNeedsTargets() && pendingTargetIds.isEmpty() ? "Select a target to preview." : "Loading preset preview...";
            guiGraphics.drawString(font, message, x + 10, previewY + 8, 0xFF95A0AD, false);
            return;
        }
        if (presetPreviewMobIds.isEmpty()) {
            guiGraphics.drawString(font, "This preset did not resolve to any mobs.", x + 10, previewY + 8, 0xFFFFA3A3, false);
            return;
        }

        int contentX = x + 10;
        int contentWidth = width - 20;
        guiGraphics.enableScissor(leftPos + contentX, topPos + previewY, leftPos + contentX + contentWidth, topPos + previewY + previewHeight);
        int contentY = previewY - presetPreviewScroll;
        List<String> needsDropRows = new ArrayList<>();
        contentY = renderPresetSummarySection(
                guiGraphics,
                PresetSection.ACCEPTED,
                presetPreviewAcceptedSummaries,
                contentX,
                contentY,
                contentWidth,
                mouseX,
                mouseY
        );
        contentY = renderPresetSummarySection(
                guiGraphics,
                PresetSection.DISCARDED,
                presetPreviewDiscardedSummaries,
                contentX,
                contentY,
                contentWidth,
                mouseX,
                mouseY
        );
        guiGraphics.drawString(font, "Selected mobs (" + presetPreviewMobIds.size() + ")", contentX, contentY, 0xFFA9F5B6, false);
        if (inside(mouseX, mouseY, contentX, contentY, contentWidth, 12)) {
            offerManualTooltip("preset-selected-mobs", Component.literal("Mobs this preset will select on the DumpNull."));
        }
        contentY += 12;
        for (ResourceLocation mobId : presetPreviewMobIds) {
            if (contentY + PRESET_PREVIEW_CARD_HEIGHT >= previewY && contentY <= previewY + previewHeight) {
                renderPresetMobPreviewCard(guiGraphics, mobId, contentX, contentY, contentWidth, mouseX, mouseY);
                if (!dropRows.containsKey(mobId) && pendingDropRequests.add(mobId)) {
                    needsDropRows.add(mobId.toString());
                }
            }
            contentY += PRESET_PREVIEW_CARD_HEIGHT + 4;
        }
        guiGraphics.disableScissor();
        renderScrollbar(guiGraphics, ScrollRegion.presetPreview(), contentX + contentWidth - SCROLLBAR_WIDTH - 1, previewY + 1, previewHeight - 2, presetPreviewScroll, presetPreviewMaxScroll());
        if (!needsDropRows.isEmpty()) {
            PacketDistributor.sendToServer(new DumpNullPayloads.RequestMobDropsPayload(menu.containerId, needsDropRows));
        }
    }

    private int renderPresetSummarySection(
            GuiGraphics guiGraphics,
            PresetSection section,
            List<DumpNullItemStatusSummary> summaries,
            int x,
            int y,
            int width,
            int mouseX,
            int mouseY
    ) {
        boolean collapsed = isPresetSectionCollapsed(section);
        int color = section == PresetSection.ACCEPTED ? 0xFFA9F5B6 : 0xFFFFA3A3;
        String marker = collapsed ? "+" : "-";
        String label = marker + " " + section.label() + " (" + summaries.size() + ")";
        guiGraphics.fill(x, y, x + width, y + 12, 0xB42A3039);
        guiGraphics.drawString(font, trimToWidth(label, width - 4), x + 4, y + 2, color, false);
        if (inside(mouseX, mouseY, x, y, width, 12)) {
            offerManualTooltip("preset-section:" + section.name(), Component.literal("Click to expand or collapse this preset item preview."));
        }
        y += 14;
        if (!collapsed) {
            if (summaries.isEmpty()) {
                guiGraphics.drawString(font, "No items", x + 6, y + 4, 0xFF95A0AD, false);
                y += 16;
            } else {
                renderPresetRuleGrid(guiGraphics, summaries, section.status(), x, y, width, mouseX, mouseY);
                y += presetRuleGridHeight(summaries, width) + 8;
            }
        }
        return y;
    }

    private void renderPresetRuleGrid(GuiGraphics guiGraphics, List<DumpNullItemStatusSummary> summaries, DumpNullItemStatus status, int x, int y, int width, int mouseX, int mouseY) {
        int columns = Math.max(1, width / (PRESET_RULE_ICON_SIZE + 3));
        int border = statusColor(status);
        for (int i = 0; i < summaries.size(); i++) {
            DumpNullItemStatusSummary summary = summaries.get(i);
            int iconX = x + (i % columns) * (PRESET_RULE_ICON_SIZE + 3);
            int iconY = y + (i / columns) * (PRESET_RULE_ICON_SIZE + 3);
            guiGraphics.fill(iconX, iconY, iconX + PRESET_RULE_ICON_SIZE, iconY + PRESET_RULE_ICON_SIZE, 0xD00F1318);
            guiGraphics.renderOutline(iconX, iconY, PRESET_RULE_ICON_SIZE, PRESET_RULE_ICON_SIZE, border);
            ItemStack stack = itemStack(summary.itemId());
            guiGraphics.renderItem(stack, iconX + 1, iconY + 1);
            if (inside(mouseX, mouseY, iconX, iconY, PRESET_RULE_ICON_SIZE, PRESET_RULE_ICON_SIZE)) {
                hoveredItem = stack;
            }
        }
    }

    private void renderPresetMobPreviewCard(GuiGraphics guiGraphics, ResourceLocation mobId, int x, int y, int width, int mouseX, int mouseY) {
        DumpNullMobOption option = menu.getMobOptions().stream()
                .filter(candidate -> candidate.entityId().equals(mobId))
                .findFirst()
                .orElse(new DumpNullMobOption(mobId, mobId.toString(), "UNKNOWN", false));
        guiGraphics.fill(x, y, x + width, y + PRESET_PREVIEW_CARD_HEIGHT, 0xC81A1F26);
        guiGraphics.renderOutline(x, y, width, PRESET_PREVIEW_CARD_HEIGHT, 0xFF48505B);
        renderEntityPreview(guiGraphics, option, x + 4, y + 4, 44, PRESET_PREVIEW_CARD_HEIGHT - 8);
        guiGraphics.drawString(font, trimToWidth(mobName(option), width - 58), x + 54, y + 5, 0xFFE8EDF5, false);
        List<DumpNullDropCandidate> candidates = uniqueDrops(dropRows.get(mobId));
        if (!dropRows.containsKey(mobId)) {
            guiGraphics.drawString(font, "Loading drops", x + 54, y + 22, 0xFF95A0AD, false);
            return;
        }
        if (candidates.isEmpty()) {
            guiGraphics.drawString(font, "No drops discovered", x + 54, y + 22, 0xFF95A0AD, false);
            return;
        }
        int dropX = x + 54;
        int dropY = y + 22;
        int columns = Math.max(1, (width - 58) / (DROP_ICON_SIZE + DROP_ICON_GAP));
        int maxIcons = Math.min(candidates.size(), columns);
        for (int i = 0; i < maxIcons; i++) {
            DumpNullDropCandidate candidate = candidates.get(i);
            int iconX = dropX + i * (DROP_ICON_SIZE + DROP_ICON_GAP);
            ItemStack stack = itemStack(candidate.itemId());
            guiGraphics.fill(iconX, dropY, iconX + DROP_ICON_SIZE, dropY + DROP_ICON_SIZE, 0xD00F1318);
            guiGraphics.renderItem(stack, iconX + 1, dropY + 1);
            if (inside(mouseX, mouseY, iconX, dropY, DROP_ICON_SIZE, DROP_ICON_SIZE)) {
                hoveredItem = stack;
            }
        }
    }

    private List<DumpNullDropCandidate> uniqueDrops(List<DumpNullDropCandidate> rawCandidates) {
        if (rawCandidates == null || rawCandidates.isEmpty()) {
            return List.of();
        }
        Map<ResourceLocation, DumpNullDropCandidate> unique = new LinkedHashMap<>();
        for (DumpNullDropCandidate candidate : rawCandidates) {
            unique.putIfAbsent(candidate.itemId(), candidate);
        }
        return unique.values().stream()
                .sorted(Comparator.comparing(candidate -> candidate.itemId().toString()))
                .toList();
    }

    public static boolean shouldShowAllDropTile(int uniqueDropCount) {
        return uniqueDropCount >= 2;
    }

    private int dropTileCount(List<DumpNullDropCandidate> candidates) {
        return candidates.size() + (shouldShowAllDropTile(candidates.size()) ? 1 : 0);
    }

    private boolean isAllDropTile(List<DumpNullDropCandidate> candidates, int tileIndex) {
        return shouldShowAllDropTile(candidates.size()) && tileIndex == 0;
    }

    private int candidateIndex(List<DumpNullDropCandidate> candidates, int tileIndex) {
        return shouldShowAllDropTile(candidates.size()) ? tileIndex - 1 : tileIndex;
    }

    private int cardDropScroll(ResourceLocation entityId, int totalTiles, int maxVisibleTiles) {
        int maxScroll = Math.max(0, totalTiles - maxVisibleTiles);
        int scroll = clamp(cardDropScrolls.getOrDefault(entityId, 0), 0, maxScroll);
        if (scroll == 0) {
            cardDropScrolls.remove(entityId);
        } else {
            cardDropScrolls.put(entityId, scroll);
        }
        return scroll;
    }

    private CardDropArea cardDropAreaAt(int localX, int localY) {
        if (!insideCatalogList(localX, localY)) {
            return null;
        }
        int listY = catalogListY();
        int y = listY - mobScroll;
        int cardWidth = catalogCardWidth();
        for (CatalogRow row : catalogRows()) {
            if (row.type() == CatalogRowType.CARDS) {
                for (int cardIndex = 0; cardIndex < row.options().size(); cardIndex++) {
                    DumpNullMobOption option = row.options().get(cardIndex);
                    int cardX = PADDING + cardIndex * (cardWidth + MOB_CARD_GAP);
                    int dropsX = cardX + 6;
                    int dropsY = y + cardPreviewHeight() + 4;
                    int dropsWidth = cardWidth - 12;
                    int dropsHeight = MOB_CARD_HEIGHT - cardPreviewHeight() - 8;
                    if (inside(localX, localY, dropsX, dropsY, dropsWidth + SCROLLBAR_WIDTH, dropsHeight)) {
                        List<DumpNullDropCandidate> candidates = uniqueDrops(dropRows.get(option.entityId()));
                        int columns = Math.max(1, dropsWidth / (DROP_ICON_SIZE + DROP_ICON_GAP));
                        int rows = Math.max(1, dropsHeight / (DROP_ICON_SIZE + DROP_ICON_GAP));
                        int maxVisible = columns * rows;
                        int total = dropTileCount(candidates);
                        return new CardDropArea(option.entityId(), columns, Math.max(0, total - maxVisible));
                    }
                }
            }
            y += row.height();
        }
        return null;
    }

    private List<CatalogRow> catalogRows() {
        List<DumpNullMobOption> options = filteredMobOptions();
        Map<String, Map<String, List<DumpNullMobOption>>> grouped = groupedCatalogOptions(options);
        Map<String, Long> namespaceCounts = namespaceCounts(options);
        Map<String, Long> categoryCounts = categoryCounts(options);
        List<CatalogRow> entries = new ArrayList<>();
        int columns = catalogColumns();
        for (String namespace : catalogNamespaceOrder(options)) {
            entries.add(CatalogRow.modHeader(namespace, namespaceCounts.getOrDefault(namespace, 0L).intValue()));
            if (collapsedNamespaces.contains(namespace)) {
                continue;
            }
            Map<String, List<DumpNullMobOption>> categories = grouped.getOrDefault(namespace, Map.of());
            for (String category : catalogCategoryOrder(options, namespace)) {
                entries.add(CatalogRow.categoryHeader(namespace, category, categoryCounts.getOrDefault(categoryKey(namespace, category), 0L).intValue()));
                if (collapsedCategories.contains(categoryKey(namespace, category))) {
                    continue;
                }
                List<DumpNullMobOption> categoryOptions = categories.getOrDefault(category, List.of());
                for (int i = 0; i < categoryOptions.size(); i += columns) {
                    entries.add(CatalogRow.cards(namespace, category, categoryOptions.subList(i, Math.min(i + columns, categoryOptions.size()))));
                }
            }
        }
        return entries;
    }

    private static Map<String, Map<String, List<DumpNullMobOption>>> groupedCatalogOptions(List<DumpNullMobOption> options) {
        Map<String, Map<String, List<DumpNullMobOption>>> grouped = new LinkedHashMap<>();
        for (DumpNullMobOption option : options.stream().sorted(DumpNullCatalog.mobOptionComparator()).toList()) {
            grouped
                    .computeIfAbsent(option.entityId().getNamespace(), ignored -> new LinkedHashMap<>())
                    .computeIfAbsent(option.category(), ignored -> new ArrayList<>())
                    .add(option);
        }
        return grouped;
    }

    private static Map<String, Long> namespaceCounts(List<DumpNullMobOption> options) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (DumpNullMobOption option : options) {
            counts.merge(option.entityId().getNamespace(), 1L, Long::sum);
        }
        return counts;
    }

    private static Map<String, Long> categoryCounts(List<DumpNullMobOption> options) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (DumpNullMobOption option : options) {
            counts.merge(categoryKey(option.entityId().getNamespace(), option.category()), 1L, Long::sum);
        }
        return counts;
    }

    public static List<String> catalogNamespaceOrder(List<DumpNullMobOption> options) {
        return options.stream()
                .map(option -> option.entityId().getNamespace())
                .distinct()
                .sorted(DumpNullScreen::compareNamespaces)
                .toList();
    }

    public static List<String> catalogCategoryOrder(List<DumpNullMobOption> options, String namespace) {
        Map<String, Long> counts = options.stream()
                .filter(option -> option.entityId().getNamespace().equals(namespace))
                .collect(LinkedHashMap::new, (map, option) -> map.merge(option.category(), 1L, Long::sum), LinkedHashMap::putAll);
        return counts.keySet().stream()
                .sorted((left, right) -> {
                    int countCompare = Long.compare(counts.getOrDefault(right, 0L), counts.getOrDefault(left, 0L));
                    return countCompare != 0 ? countCompare : left.compareTo(right);
                })
                .toList();
    }

    private List<DumpNullMobOption> filteredMobOptions() {
        String query = mobSearchBox == null ? "" : mobSearchBox.getValue().trim().toLowerCase(Locale.ROOT);
        return menu.getMobOptions().stream()
                .filter(option -> query.isEmpty() || matchesMobSearch(option, query))
                .filter(this::matchesActiveFilters)
                .sorted(DumpNullCatalog.mobOptionComparator())
                .toList();
    }

    private boolean matchesMobSearch(DumpNullMobOption option, String query) {
        return option.entityId().toString().toLowerCase(Locale.ROOT).contains(query)
                || option.entityId().getNamespace().toLowerCase(Locale.ROOT).contains(query)
                || mobName(option).toLowerCase(Locale.ROOT).contains(query)
                || option.category().toLowerCase(Locale.ROOT).contains(query)
                || loadedDropSearchMatches(option.entityId(), query)
                || (query.length() >= DROP_SEARCH_MIN_LENGTH
                && query.equals(activeDropSearchQuery)
                && dropSearchMobMatches.contains(option.entityId()));
    }

    private boolean loadedDropSearchMatches(ResourceLocation entityId, String query) {
        List<DumpNullDropCandidate> candidates = dropRows.get(entityId);
        if (candidates == null || candidates.isEmpty()) {
            return false;
        }
        for (DumpNullDropCandidate candidate : candidates) {
            ItemStack stack = itemStack(candidate.itemId());
            String displayName = stack.isEmpty() ? "" : stack.getHoverName().getString();
            if (DumpNullDropSearch.matches(candidate.itemId(), query, displayName)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesActiveFilters(DumpNullMobOption option) {
        boolean dropDataKnown = dropRows.containsKey(option.entityId());
        boolean hasDrops = dropDataKnown && !uniqueDrops(dropRows.get(option.entityId())).isEmpty();
        return activeFilter == null || activeFilter.matches(option, dropDataKnown, hasDrops);
    }

    private List<DumpNullPresetTarget> filteredTargets() {
        String query = targetSearchBox == null ? "" : targetSearchBox.getValue().trim().toLowerCase(Locale.ROOT);
        List<DumpNullPresetTarget> targets = DumpNullPresetOption.TARGET_BIOME.equals(pendingTargetType)
                ? menu.getBiomeTargets()
                : menu.getDimensionTargets();
        return targets.stream()
                .filter(target -> query.isEmpty()
                        || target.id().toString().toLowerCase(Locale.ROOT).contains(query)
                        || target.label().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    private List<DumpNullItemStatusSummary> statusSummaries(DumpNullItemStatus status) {
        return menu.getItemStatuses().stream()
                .filter(summary -> summary.status() == status)
                .toList();
    }

    private List<StatusTile> statusTiles(DumpNullItemStatus status, int x, int y, int width, int scroll) {
        List<DumpNullItemStatusSummary> summaries = statusSummaries(status);
        int columns = statusGridColumnsForWidth(width);
        int tileWidth = statusTileWidth(width);
        List<StatusTile> tiles = new ArrayList<>();
        for (int i = 0; i < summaries.size(); i++) {
            int column = i % columns;
            int row = i / columns;
            tiles.add(new StatusTile(
                    summaries.get(i),
                    x + column * tileWidth,
                    tileWidth,
                    y + row * GRID_TILE_HEIGHT - scroll
            ));
        }
        return tiles;
    }

    private StatusTile tileAt(int localX, int localY, DumpNullItemStatus status) {
        int x = status == DumpNullItemStatus.ACCEPTED ? acceptedGridX() : discardedGridX();
        int y = status == DumpNullItemStatus.ACCEPTED ? acceptedGridY() : discardedGridY();
        int width = gridWidth();
        int height = statusGridHeight();
        if (!inside(localX, localY, x, y, width, height)) {
            return null;
        }
        int scroll = status == DumpNullItemStatus.ACCEPTED ? acceptedGridScroll : discardedGridScroll;
        for (StatusTile tile : statusTiles(status, x, y, width, scroll)) {
            if (inside(localX, localY, tile.x(), tile.y(), tile.width() - 2, GRID_TILE_HEIGHT - 2)) {
                return tile;
            }
        }
        return null;
    }

    private boolean handleStatusGridClick(int localX, int localY, int button) {
        StatusTile acceptedTile = tileAt(localX, localY, DumpNullItemStatus.ACCEPTED);
        if (acceptedTile != null) {
            if (button == 0 && tileRemoveAt(localX, localY, acceptedTile)) {
                setItemStatus(acceptedTile.itemId(), DumpNullItemStatus.NEUTRAL);
                return true;
            }
            if (button == 1) {
                setItemStatus(acceptedTile.itemId(), DumpNullItemStatus.DISCARDED);
                return true;
            }
            if (button == 0) {
                selectItem(acceptedTile.itemId());
                return true;
            }
        }
        StatusTile discardedTile = tileAt(localX, localY, DumpNullItemStatus.DISCARDED);
        if (discardedTile != null) {
            if (button == 0 && tileRemoveAt(localX, localY, discardedTile)) {
                setItemStatus(discardedTile.itemId(), DumpNullItemStatus.NEUTRAL);
                return true;
            }
            if (button == 1) {
                setItemStatus(discardedTile.itemId(), DumpNullItemStatus.ACCEPTED);
                return true;
            }
            if (button == 0) {
                selectItem(discardedTile.itemId());
                return true;
            }
        }
        return false;
    }

    private boolean tileRemoveAt(int localX, int localY, StatusTile tile) {
        return inside(
                localX,
                localY,
                tile.x() + tile.width() - TILE_REMOVE_SIZE - 3,
                tile.y() + 2,
                TILE_REMOVE_SIZE,
                TILE_REMOVE_SIZE
        );
    }

    private CardControl cardControlAt(int localX, int localY) {
        if (!insideCatalogList(localX, localY)) {
            return null;
        }
        int listY = catalogListY();
        int y = listY - mobScroll;
        int cardWidth = catalogCardWidth();
        for (CatalogRow row : catalogRows()) {
            if (row.type() == CatalogRowType.CARDS) {
                for (int cardIndex = 0; cardIndex < row.options().size(); cardIndex++) {
                    DumpNullMobOption option = row.options().get(cardIndex);
                    int cardX = PADDING + cardIndex * (cardWidth + MOB_CARD_GAP);
                    if (dropRows.containsKey(option.entityId()) && inside(localX, localY, cardX + cardWidth - ICON_BUTTON_SIZE - 4, y + 4, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)) {
                        return new CardControl(option.entityId(), CardControlType.COPY);
                    }
                }
            }
            y += row.height();
        }
        return null;
    }

    private DropClick dropAt(int localX, int localY) {
        if (!insideCatalogList(localX, localY)) {
            return null;
        }
        int listY = catalogListY();
        int y = listY - mobScroll;
        int cardWidth = catalogCardWidth();
        for (CatalogRow row : catalogRows()) {
            if (row.type() == CatalogRowType.CARDS && inside(localX, localY, PADDING, y, catalogWidth(), MOB_CARD_HEIGHT)) {
                for (int cardIndex = 0; cardIndex < row.options().size(); cardIndex++) {
                    DumpNullMobOption option = row.options().get(cardIndex);
                    int cardX = PADDING + cardIndex * (cardWidth + MOB_CARD_GAP);
                    if (!inside(localX, localY, cardX, y, cardWidth, MOB_CARD_HEIGHT)) {
                        continue;
                    }
                    int previewHeight = cardPreviewHeight();
                    int dropsX = cardX + 6;
                    int dropsY = y + previewHeight + 4;
                    int dropsWidth = cardWidth - 12;
                    int dropsHeight = MOB_CARD_HEIGHT - previewHeight - 8;
                    int columns = Math.max(1, dropsWidth / (DROP_ICON_SIZE + DROP_ICON_GAP));
                    int rows = Math.max(1, dropsHeight / (DROP_ICON_SIZE + DROP_ICON_GAP));
                    int maxIcons = columns * rows;
                    List<DumpNullDropCandidate> candidates = uniqueDrops(dropRows.get(option.entityId()));
                    int totalTiles = dropTileCount(candidates);
                    int scroll = cardDropScroll(option.entityId(), totalTiles, maxIcons);
                    for (int visibleIndex = 0; visibleIndex < maxIcons && visibleIndex + scroll < totalTiles; visibleIndex++) {
                        int tileIndex = visibleIndex + scroll;
                        int rowIndex = visibleIndex / columns;
                        int rowCount = Math.min(columns, totalTiles - scroll - rowIndex * columns);
                        int rowWidth = rowCount * DROP_ICON_SIZE + Math.max(0, rowCount - 1) * DROP_ICON_GAP;
                        int rowX = dropsX + Math.max(0, (dropsWidth - rowWidth) / 2);
                        int iconX = rowX + (visibleIndex % columns) * (DROP_ICON_SIZE + DROP_ICON_GAP);
                        int iconY = dropsY + (visibleIndex / columns) * (DROP_ICON_SIZE + DROP_ICON_GAP);
                        if (inside(localX, localY, iconX, iconY, DROP_ICON_SIZE, DROP_ICON_SIZE)) {
                            if (isAllDropTile(candidates, tileIndex)) {
                                return new DropClick(option.entityId(), null, true);
                            }
                            return new DropClick(option.entityId(), candidates.get(candidateIndex(candidates, tileIndex)), false);
                        }
                    }
                }
            }
            y += row.height();
        }
        return null;
    }

    private DumpNullMobOption mobAt(int localX, int localY) {
        if (!insideCatalogList(localX, localY)) {
            return null;
        }
        int listY = catalogListY();
        int y = listY - mobScroll;
        int cardWidth = catalogCardWidth();
        for (CatalogRow row : catalogRows()) {
            if (row.type() == CatalogRowType.CARDS) {
                for (int cardIndex = 0; cardIndex < row.options().size(); cardIndex++) {
                    int cardX = PADDING + cardIndex * (cardWidth + MOB_CARD_GAP);
                    if (inside(localX, localY, cardX, y, cardWidth, MOB_CARD_HEIGHT)) {
                        return row.options().get(cardIndex);
                    }
                }
            }
            y += row.height();
        }
        return null;
    }

    private HeaderClick headerAt(int localX, int localY) {
        if (!insideCatalogList(localX, localY)) {
            return null;
        }
        int y = catalogListY() - mobScroll;
        for (CatalogRow row : catalogRows()) {
            if ((row.type() == CatalogRowType.MOD_HEADER || row.type() == CatalogRowType.CATEGORY_HEADER)
                    && inside(localX, localY, PADDING, y, catalogWidth(), row.height())) {
                return new HeaderClick(row.type(), row.namespace(), row.category());
            }
            y += row.height();
        }
        return null;
    }

    private DumpNullPresetTarget targetAt(int localX, int localY) {
        if (!presetNeedsTargets()) {
            return null;
        }
        int x = overlayX();
        int y = presetTargetListY();
        int width = overlayWidth();
        int height = 36;
        if (!inside(localX, localY, x + 10, y, width - 20, height)) {
            return null;
        }
        int index = (localY - y) / 12;
        List<DumpNullPresetTarget> targets = filteredTargets();
        return index >= 0 && index < targets.size() && index < 3 ? targets.get(index) : null;
    }

    private PresetSection presetSectionAt(int localX, int localY) {
        if (!presetPreviewResolved || presetPreviewMobIds.isEmpty()) {
            return null;
        }
        int x = overlayX() + 10;
        int width = overlayWidth() - 20;
        if (!inside(localX, localY, x, presetPreviewY(), width, presetPreviewHeight())) {
            return null;
        }
        int y = presetPreviewY() - presetPreviewScroll;
        if (inside(localX, localY, x, y, width, 12)) {
            return PresetSection.ACCEPTED;
        }
        y += presetSectionHeight(PresetSection.ACCEPTED, presetPreviewAcceptedSummaries, width);
        if (inside(localX, localY, x, y, width, 12)) {
            return PresetSection.DISCARDED;
        }
        return null;
    }

    private void togglePresetSection(PresetSection section) {
        if (section == PresetSection.ACCEPTED) {
            presetAcceptedCollapsed = !presetAcceptedCollapsed;
        } else {
            presetDiscardedCollapsed = !presetDiscardedCollapsed;
        }
        presetPreviewScroll = clamp(presetPreviewScroll, 0, presetPreviewMaxScroll());
    }

    private DumpNullPresetOption presetOptionAt(int localX, int localY) {
        if (!presetsExpanded) {
            return null;
        }
        int x = presetDropdownX();
        int y = presetDropdownY() + 2;
        int width = presetDropdownWidth();
        if (!inside(localX, localY, x + 2, y, width - 4, menu.getPresetOptions().size() * presetDropdownRowHeight())) {
            return null;
        }
        int index = (localY - y) / presetDropdownRowHeight();
        return index >= 0 && index < menu.getPresetOptions().size() ? menu.getPresetOptions().get(index) : null;
    }

    private DumpNullItemCatalogView itemViewAt(int localX, int localY) {
        if (!itemViewsExpanded) {
            return null;
        }
        int x = itemViewDropdownX();
        int y = itemViewDropdownY() + 2;
        int width = itemViewDropdownWidth();
        List<DumpNullItemCatalogView> views = DumpNullItemCatalogView.all();
        if (!inside(localX, localY, x + 2, y, width - 4, views.size() * itemViewDropdownRowHeight())) {
            return null;
        }
        int index = (localY - y) / itemViewDropdownRowHeight();
        return index >= 0 && index < views.size() ? views.get(index) : null;
    }

    private void stagePreset(DumpNullPresetOption preset) {
        presetsExpanded = false;
        pendingPresetId = preset.presetId();
        pendingTargetType = preset.targetType();
        pendingTargetIds.clear();
        writeAcceptedPresetRules = false;
        presetAcceptedCollapsed = false;
        presetDiscardedCollapsed = false;
        clearPresetPreview();
        requestPresetPreview();
        updateWidgetVisibility();
    }

    private void confirmPreset() {
        if (pendingPresetId.isBlank() || presetNeedsTargets() && pendingTargetIds.isEmpty() || !presetPreviewResolved || presetPreviewMobIds.isEmpty()) {
            return;
        }
        rememberState();
        PacketDistributor.sendToServer(new DumpNullPayloads.ApplyPresetPayload(pendingPresetId, pendingTargetIds.stream().toList(), writeAcceptedPresetRules));
        clearPendingPreset();
    }

    private void clearPendingPreset() {
        pendingPresetId = "";
        pendingTargetType = DumpNullPresetOption.TARGET_NONE;
        pendingTargetIds.clear();
        presetsExpanded = false;
        writeAcceptedPresetRules = false;
        presetAcceptedCollapsed = false;
        presetDiscardedCollapsed = false;
        clearPresetPreview();
        updateWidgetVisibility();
    }

    private void toggleTarget(DumpNullPresetTarget target) {
        String id = target.id().toString();
        if (pendingTargetIds.contains(id)) {
            pendingTargetIds.remove(id);
        } else {
            pendingTargetIds.add(id);
        }
        clearPresetPreview();
        requestPresetPreview();
        updateWidgetVisibility();
    }

    private void clearPresetPreview() {
        presetPreviewTargetIds = List.of();
        presetPreviewMobIds = List.of();
        presetPreviewAcceptedSummaries = List.of();
        presetPreviewDiscardedSummaries = List.of();
        presetPreviewResolved = false;
        presetPreviewScroll = 0;
    }

    private void requestPresetPreview() {
        if (pendingPresetId.isBlank() || presetNeedsTargets() && pendingTargetIds.isEmpty()) {
            return;
        }
        PacketDistributor.sendToServer(new DumpNullPayloads.RequestPresetPreviewPayload(
                menu.containerId,
                pendingPresetId,
                pendingTargetIds.stream().toList()
        ));
    }

    private void selectItem(ResourceLocation itemId) {
        selectedItemId = itemId;
        hydrateDetailFields();
        updateWidgetVisibility();
    }

    private void saveSelectedFilter() {
        if (selectedItemId == null) {
            return;
        }
        DumpNullItemStatus status = statusOf(selectedItemId);
        if (status == DumpNullItemStatus.NEUTRAL) {
            status = DumpNullItemStatus.ACCEPTED;
        }
        rememberState();
        PacketDistributor.sendToServer(new DumpNullPayloads.SaveItemFilterPayload(
                selectedItemId.toString(),
                status.ordinal(),
                parseInt(minDurabilityBox.getValue(), 0),
                parseIdList(requiredEnchantmentsBox.getValue()),
                parseIdList(forbiddenEnchantmentsBox.getValue())
        ));
    }

    private void clearSelectedFilter() {
        if (selectedItemId != null) {
            rememberState();
            PacketDistributor.sendToServer(new DumpNullPayloads.ClearItemFilterPayload(selectedItemId.toString()));
        }
    }

    private void setItemStatus(ResourceLocation itemId, DumpNullItemStatus status) {
        rememberState();
        PacketDistributor.sendToServer(new DumpNullPayloads.SetItemStatusPayload(itemId.toString(), status.ordinal()));
    }

    private void setItemStatuses(List<ResourceLocation> itemIds, DumpNullItemStatus status) {
        List<String> ids = itemIds.stream()
                .distinct()
                .map(ResourceLocation::toString)
                .toList();
        if (ids.isEmpty()) {
            return;
        }
        rememberState();
        PacketDistributor.sendToServer(new DumpNullPayloads.SetItemStatusesPayload(ids, status.ordinal()));
    }

    private void cycleHeldDiscardMode() {
        rememberState();
        DumpNullHeldDiscardMode next = menu.getData().heldDiscardMode().next();
        PacketDistributor.sendToServer(new DumpNullPayloads.SetHeldDiscardModePayload(next.ordinal()));
    }

    private void cycleDockDiscardMode() {
        rememberState();
        DumpNullDockDiscardMode next = menu.getData().dockDiscardMode().next();
        PacketDistributor.sendToServer(new DumpNullPayloads.SetDockDiscardModePayload(next.ordinal()));
    }

    private boolean handleAutomationClick(int localX, int localY) {
        for (Direction direction : Direction.values()) {
            Rect2i rect = faceRect(direction);
            if (inside(localX, localY, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight())) {
                selectedAutomationFace = direction;
                return true;
            }
        }
        DumpNullSideMode mode = faceModeAt(localX, localY);
        if (mode != null) {
            rememberState();
            PacketDistributor.sendToServer(new DumpNullPayloads.SetSideModePayload(selectedAutomationFace.get3DDataValue(), mode.ordinal()));
            return true;
        }
        if (faceDiscardToggleAt(localX, localY)) {
            boolean enabled = !menu.getData().isDiscardExportSide(selectedAutomationFace);
            rememberState();
            PacketDistributor.sendToServer(new DumpNullPayloads.SetDiscardExportSidePayload(selectedAutomationFace.get3DDataValue(), enabled));
            return true;
        }
        return false;
    }

    private void copyStatus(DumpNullItemStatus status) {
        if (minecraft == null) {
            return;
        }
        List<DumpNullStatusCopyFormatter.Entry> entries = statusSummaries(status).stream()
                .map(summary -> {
                    ItemStack stack = itemStack(summary.itemId());
                    return new DumpNullStatusCopyFormatter.Entry(stack.getHoverName().getString(), summary.itemId());
                })
                .toList();
        minecraft.keyboardHandler.setClipboard(DumpNullStatusCopyFormatter.format(entries));
    }

    private void clearStatus(DumpNullItemStatus status) {
        List<ResourceLocation> itemIds = statusSummaries(status).stream()
                .map(DumpNullItemStatusSummary::itemId)
                .distinct()
                .toList();
        if (itemIds.isEmpty()) {
            return;
        }
        if (selectedItemId != null && itemIds.contains(selectedItemId)) {
            selectedItemId = null;
            hydrateDetailFields();
        }
        rememberState();
        PacketDistributor.sendToServer(new DumpNullPayloads.ClearItemFiltersPayload(itemIds.stream().map(ResourceLocation::toString).toList()));
    }

    private void copyMobDrops(ResourceLocation entityId) {
        if (minecraft == null) {
            return;
        }
        List<DumpNullStatusCopyFormatter.Entry> entries = uniqueDrops(dropRows.get(entityId)).stream()
                .map(candidate -> {
                    ItemStack stack = itemStack(candidate.itemId());
                    return new DumpNullStatusCopyFormatter.Entry(stack.getHoverName().getString(), candidate.itemId());
                })
                .toList();
        if (!entries.isEmpty()) {
            minecraft.keyboardHandler.setClipboard(DumpNullStatusCopyFormatter.format(entries));
        }
    }

    private void cycleAllDrops(ResourceLocation entityId) {
        List<DumpNullDropCandidate> candidates = uniqueDrops(dropRows.get(entityId));
        if (!shouldShowAllDropTile(candidates.size())) {
            return;
        }
        DumpNullItemStatus next = aggregateStatus(candidates).next();
        setItemStatuses(candidates.stream().map(DumpNullDropCandidate::itemId).toList(), next);
    }

    private void setAllDropsFromRightClick(ResourceLocation entityId) {
        List<DumpNullDropCandidate> candidates = uniqueDrops(dropRows.get(entityId));
        if (!shouldShowAllDropTile(candidates.size())) {
            return;
        }
        DumpNullItemStatus next = aggregateStatus(candidates).catalogRightClickTarget();
        setItemStatuses(candidates.stream().map(DumpNullDropCandidate::itemId).toList(), next);
    }

    private DumpNullItemStatus aggregateStatus(List<DumpNullDropCandidate> candidates) {
        if (candidates.isEmpty()) {
            return DumpNullItemStatus.NEUTRAL;
        }
        boolean allAccepted = true;
        boolean allDiscarded = true;
        for (DumpNullDropCandidate candidate : candidates) {
            DumpNullItemStatus status = statusOf(candidate.itemId());
            allAccepted &= status == DumpNullItemStatus.ACCEPTED;
            allDiscarded &= status == DumpNullItemStatus.DISCARDED;
        }
        if (allAccepted) {
            return DumpNullItemStatus.ACCEPTED;
        }
        if (allDiscarded) {
            return DumpNullItemStatus.DISCARDED;
        }
        return DumpNullItemStatus.NEUTRAL;
    }

    private void toggleFilter(DumpNullMobFilter filter) {
        activeFilter = DumpNullMobFilter.toggleSingle(activeFilter, filter);
        mobScroll = 0;
        ensureFocusedMobValid();
        updateWidgetVisibility();
    }

    private void toggleNamespace(String namespace) {
        if (collapsedNamespaces.contains(namespace)) {
            collapsedNamespaces.remove(namespace);
        } else {
            collapsedNamespaces.add(namespace);
        }
        mobScroll = clamp(mobScroll, 0, mobMaxScroll());
        ensureFocusedMobValid();
    }

    private void toggleCategory(String namespace, String category) {
        String key = categoryKey(namespace, category);
        if (collapsedCategories.contains(key)) {
            collapsedCategories.remove(key);
        } else {
            collapsedCategories.add(key);
        }
        mobScroll = clamp(mobScroll, 0, mobMaxScroll());
        ensureFocusedMobValid();
    }

    private void expandAllCatalog() {
        collapsedNamespaces.clear();
        collapsedCategories.clear();
        mobScroll = clamp(mobScroll, 0, mobMaxScroll());
        ensureFocusedMobValid();
    }

    private void collapseAllCatalog() {
        List<DumpNullMobOption> options = filteredMobOptions();
        collapsedNamespaces.clear();
        collapsedNamespaces.addAll(catalogNamespaceOrder(options));
        collapsedCategories.clear();
        for (DumpNullMobOption option : options) {
            collapsedCategories.add(categoryKey(option.entityId().getNamespace(), option.category()));
        }
        mobScroll = clamp(mobScroll, 0, mobMaxScroll());
        ensureFocusedMobValid();
    }

    private Component filterLabel(DumpNullMobFilter filter) {
        return Component.literal((activeFilter == filter ? "*" : "") + switch (filter) {
            case HOSTILE -> "Hostile";
            case PEACEFUL -> "Peace";
            case BOSS -> "Boss";
            case SELECTED -> "Sel";
            case HAS_DROPS -> "Drops";
        });
    }

    private Component filterTooltip(DumpNullMobFilter filter) {
        return Component.literal(switch (filter) {
            case HOSTILE -> "Show hostile mobs from the live registry.";
            case PEACEFUL -> "Show non-hostile, non-misc mobs from the live registry.";
            case BOSS -> "Show known boss-like mobs.";
            case SELECTED -> "Show mobs currently selected on this DumpNull.";
            case HAS_DROPS -> "Show mobs with loaded drops; unloaded visible cards stay long enough to load.";
        });
    }

    private int filterWidth(DumpNullMobFilter filter) {
        return switch (filter) {
            case HOSTILE -> 52;
            case PEACEFUL -> 44;
            case BOSS -> 38;
            case SELECTED -> 32;
            case HAS_DROPS -> 44;
        };
    }

    private void hydrateDetailFields() {
        if (minDurabilityBox == null) {
            return;
        }
        DumpNullRule rule = selectedItemId == null ? null : menu.getData().rules().stream()
                .filter(candidate -> candidate.itemId().equals(selectedItemId) && candidate.isAdvanced())
                .findFirst()
                .orElse(null);
        minDurabilityBox.setValue(rule == null ? "0" : Integer.toString(rule.minDurabilityPercent()));
        requiredEnchantmentsBox.setValue(rule == null ? "" : joinIds(rule.requiredEnchantments()));
        forbiddenEnchantmentsBox.setValue(rule == null ? "" : joinIds(rule.forbiddenEnchantments()));
    }

    private void updateWidgetVisibility() {
        boolean pendingPreset = !pendingPresetId.isBlank();
        boolean targetOverlay = presetNeedsTargets();
        mobsModeButton.visible = !pendingPreset;
        mobsModeButton.active = !pendingPreset;
        mobsModeButton.setMessage(Component.literal((catalogMode == CatalogMode.MOBS ? "*" : "") + "Mobs"));
        mobsModeButton.setRectangle(34, TOOLBAR_HEIGHT, leftPos + PADDING, topPos + CATALOG_Y + 2);
        itemsModeButton.visible = !pendingPreset;
        itemsModeButton.active = !pendingPreset;
        itemsModeButton.setMessage(Component.literal((catalogMode == CatalogMode.ITEMS ? "*" : "") + "Items"));
        itemsModeButton.setRectangle(36, TOOLBAR_HEIGHT, leftPos + PADDING + 38, topPos + CATALOG_Y + 2);
        automationModeButton.visible = !pendingPreset;
        automationModeButton.active = !pendingPreset;
        automationModeButton.setMessage(Component.literal((catalogMode == CatalogMode.AUTOMATION ? "*" : "") + "Auto"));
        automationModeButton.setRectangle(34, TOOLBAR_HEIGHT, leftPos + PADDING + 78, topPos + CATALOG_Y + 2);
        upgradesModeButton.visible = !pendingPreset;
        upgradesModeButton.active = !pendingPreset;
        upgradesModeButton.setRectangle(64, TOOLBAR_HEIGHT, leftPos + PADDING + 116, topPos + CATALOG_Y + 2);
        boolean mobCatalog = catalogMode == CatalogMode.MOBS && !pendingPreset;
        boolean itemCatalog = catalogMode == CatalogMode.ITEMS && !pendingPreset;
        boolean automationCatalog = catalogMode == CatalogMode.AUTOMATION && !pendingPreset;
        presetsButton.visible = mobCatalog;
        presetsButton.active = mobCatalog && !menu.getPresetOptions().isEmpty();
        presetsButton.setMessage(currentPresetLabel());
        presetsButton.setRectangle(presetCenterWidth(), TOOLBAR_HEIGHT, leftPos + presetCarouselX() + ICON_BUTTON_SIZE + 2, topPos + presetCarouselY());
        presetPreviousButton.visible = mobCatalog;
        presetPreviousButton.active = mobCatalog && menu.getPresetOptions().size() > 1;
        presetPreviousButton.setRectangle(ICON_BUTTON_SIZE, TOOLBAR_HEIGHT, leftPos + presetCarouselX(), topPos + presetCarouselY());
        presetNextButton.visible = mobCatalog;
        presetNextButton.active = mobCatalog && menu.getPresetOptions().size() > 1;
        presetNextButton.setRectangle(ICON_BUTTON_SIZE, TOOLBAR_HEIGHT, leftPos + presetCarouselX() + ICON_BUTTON_SIZE + 4 + presetCenterWidth(), topPos + presetCarouselY());
        previewPresetButton.visible = mobCatalog;
        previewPresetButton.active = mobCatalog && selectedPresetOption() != null;
        previewPresetButton.setRectangle(presetPreviewButtonWidth(), TOOLBAR_HEIGHT, leftPos + presetPreviewButtonX(), topPos + presetCarouselY());
        itemViewPreviousButton.visible = itemCatalog;
        itemViewPreviousButton.active = itemCatalog;
        itemViewPreviousButton.setRectangle(ICON_BUTTON_SIZE, TOOLBAR_HEIGHT, leftPos + itemViewCarouselX(), topPos + itemViewCarouselY());
        itemViewButton.visible = itemCatalog;
        itemViewButton.active = itemCatalog;
        itemViewButton.setMessage(currentItemViewLabel());
        itemViewButton.setRectangle(itemViewCenterWidth(), TOOLBAR_HEIGHT, leftPos + itemViewCarouselX() + ICON_BUTTON_SIZE + 2, topPos + itemViewCarouselY());
        itemViewNextButton.visible = itemCatalog;
        itemViewNextButton.active = itemCatalog;
        itemViewNextButton.setRectangle(ICON_BUTTON_SIZE, TOOLBAR_HEIGHT, leftPos + itemViewCarouselX() + ICON_BUTTON_SIZE + 4 + itemViewCenterWidth(), topPos + itemViewCarouselY());
        collapseAllButton.visible = mobCatalog;
        collapseAllButton.active = mobCatalog;
        collapseAllButton.setRectangle(ICON_BUTTON_SIZE, TOOLBAR_HEIGHT, leftPos + collapseAllButtonX(), topPos + CATALOG_Y + 2);
        expandAllButton.visible = mobCatalog;
        expandAllButton.active = mobCatalog;
        expandAllButton.setRectangle(ICON_BUTTON_SIZE, TOOLBAR_HEIGHT, leftPos + expandAllButtonX(), topPos + CATALOG_Y + 2);
        confirmPresetButton.visible = pendingPreset;
        confirmPresetButton.active = pendingPreset && (!targetOverlay || !pendingTargetIds.isEmpty()) && presetPreviewResolved && !presetPreviewMobIds.isEmpty();
        cancelPresetButton.visible = pendingPreset;
        cancelPresetButton.active = pendingPreset;
        writeAcceptedPresetButton.visible = pendingPreset && presetPreviewResolved && !presetPreviewAcceptedSummaries.isEmpty();
        writeAcceptedPresetButton.active = writeAcceptedPresetButton.visible;
        writeAcceptedPresetButton.setMessage(Component.literal((writeAcceptedPresetRules ? "[x] " : "[ ] ") + "Write accepted"));

        if (pendingPreset) {
            confirmPresetButton.setRectangle(44, 16, leftPos + overlayX() + overlayWidth() - 66, topPos + overlayY() + 6);
            cancelPresetButton.setRectangle(16, 16, leftPos + overlayX() + overlayWidth() - 18, topPos + overlayY() + 6);
            writeAcceptedPresetButton.setRectangle(112, 16, leftPos + overlayX() + 10, topPos + overlayY() + 34);
        } else {
            confirmPresetButton.setRectangle(58, TOOLBAR_HEIGHT, leftPos + imageWidth - 104, topPos + CATALOG_Y);
            cancelPresetButton.setRectangle(38, TOOLBAR_HEIGHT, leftPos + imageWidth - 44, topPos + CATALOG_Y);
            writeAcceptedPresetButton.setRectangle(112, 16, leftPos + imageWidth - 160, topPos + CATALOG_Y);
        }

        targetSearchBox.visible = targetOverlay;
        targetSearchBox.active = targetOverlay;
        targetSearchBox.setRectangle(overlayWidth() - 20, 18, leftPos + overlayX() + 10, topPos + presetTargetSearchY());

        DumpNullData data = menu.getData();
        boolean powerInstalled = data.hasUpgrade(DumpNullUpgradeType.POWER);
        heldDiscardModeButton.visible = automationCatalog;
        heldDiscardModeButton.active = automationCatalog;
        heldDiscardModeButton.setMessage(Component.literal("Held: " + heldModeLabel(data.heldDiscardMode())));
        heldDiscardModeButton.setRectangle(automationModeButtonWidth(), 16, leftPos + automationControlX(), topPos + automationModesY());
        dockDiscardModeButton.visible = automationCatalog;
        dockDiscardModeButton.active = automationCatalog;
        dockDiscardModeButton.setMessage(Component.literal("Dock: " + dockModeLabel(data.dockDiscardMode())));
        dockDiscardModeButton.setRectangle(automationModeButtonWidth(), 16, leftPos + automationControlX() + automationModeButtonWidth() + 4, topPos + automationModesY());
        installPowerUpgradeButton.visible = automationCatalog;
        installPowerUpgradeButton.active = automationCatalog && !powerInstalled;
        installPowerUpgradeButton.setMessage(Component.literal(powerInstalled ? "Power Upgrade Installed" : "Install Power Upgrade"));
        installPowerUpgradeButton.setRectangle(automationInstallButtonWidth(), 16, leftPos + automationControlX(), topPos + automationInstallY());

        boolean selectedItem = selectedItemId != null && !automationCatalog;
        minDurabilityBox.visible = selectedItem;
        minDurabilityBox.active = selectedItem;
        requiredEnchantmentsBox.visible = selectedItem;
        requiredEnchantmentsBox.active = selectedItem;
        forbiddenEnchantmentsBox.visible = selectedItem;
        forbiddenEnchantmentsBox.active = selectedItem;
        saveFilterButton.visible = selectedItem;
        saveFilterButton.active = selectedItem;
        clearFilterButton.visible = selectedItem;
        clearFilterButton.active = selectedItem;

        int enchantBoxWidth = Math.max(42, (detailWidth() - 14) / 2);
        minDurabilityBox.setRectangle(24, 18, leftPos + detailX() + 28, topPos + detailY() + 20);
        requiredEnchantmentsBox.setRectangle(enchantBoxWidth, 18, leftPos + detailX() + 5, topPos + detailY() + 47);
        forbiddenEnchantmentsBox.setRectangle(enchantBoxWidth, 18, leftPos + detailX() + 9 + enchantBoxWidth, topPos + detailY() + 47);
        saveFilterButton.setRectangle(28, 18, leftPos + detailX() + detailWidth() - 47, topPos + detailY() + 20);
        clearFilterButton.setRectangle(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE, leftPos + detailX() + detailWidth() - ICON_BUTTON_SIZE - 2, topPos + detailY() + 22);

        copyAcceptedButton.visible = !pendingPreset && !automationCatalog;
        copyAcceptedButton.setRectangle(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE, leftPos + acceptedGridX() + gridWidth() - ICON_BUTTON_SIZE, topPos + acceptedGridHeaderY());
        copyAcceptedButton.active = !pendingPreset && !automationCatalog && !statusSummaries(DumpNullItemStatus.ACCEPTED).isEmpty();
        clearAcceptedButton.visible = !pendingPreset && !automationCatalog;
        clearAcceptedButton.setRectangle(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE, leftPos + acceptedGridX() + gridWidth() - ICON_BUTTON_SIZE * 2 - 3, topPos + acceptedGridHeaderY());
        clearAcceptedButton.active = !pendingPreset && !automationCatalog && !statusSummaries(DumpNullItemStatus.ACCEPTED).isEmpty();
        copyDiscardedButton.visible = !pendingPreset && !automationCatalog;
        copyDiscardedButton.setRectangle(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE, leftPos + discardedGridX() + gridWidth() - ICON_BUTTON_SIZE, topPos + discardedGridHeaderY());
        copyDiscardedButton.active = !pendingPreset && !automationCatalog && !statusSummaries(DumpNullItemStatus.DISCARDED).isEmpty();
        clearDiscardedButton.visible = !pendingPreset && !automationCatalog;
        clearDiscardedButton.setRectangle(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE, leftPos + discardedGridX() + gridWidth() - ICON_BUTTON_SIZE * 2 - 3, topPos + discardedGridHeaderY());
        clearDiscardedButton.active = !pendingPreset && !automationCatalog && !statusSummaries(DumpNullItemStatus.DISCARDED).isEmpty();

        int chipX = leftPos + PADDING;
        int chipY = topPos + CATALOG_Y + 24;
        for (DumpNullMobFilter filter : DumpNullMobFilter.values()) {
            Button button = filterButtons.get(filter);
            if (button != null) {
                button.visible = mobCatalog;
                button.active = mobCatalog;
                button.setMessage(filterLabel(filter));
                button.setRectangle(filterWidth(filter), 16, chipX, chipY);
                chipX += filterWidth(filter) + 3;
            }
        }
        mobSearchBox.active = mobCatalog || itemCatalog;
        mobSearchBox.visible = mobCatalog || itemCatalog;
        mobSearchBox.setRectangle(mobSearchWidth(), 18, leftPos + mobSearchX(), topPos + CATALOG_Y + 2);
    }

    private void focusMob(ResourceLocation entityId, boolean scrollIntoView) {
        focusedMobId = entityId;
        if (scrollIntoView) {
            ensureFocusedMobVisible();
        }
    }

    private void ensureFocusedMobValid() {
        List<DumpNullMobOption> options = visibleMobOptions();
        if (options.isEmpty()) {
            focusedMobId = null;
            return;
        }
        if (focusedMobId == null || options.stream().noneMatch(option -> option.entityId().equals(focusedMobId))) {
            focusedMobId = options.getFirst().entityId();
        }
    }

    private void navigateFocusedMob(int keyCode) {
        List<DumpNullMobOption> options = visibleMobOptions();
        if (options.isEmpty()) {
            focusedMobId = null;
            return;
        }
        int index = 0;
        if (focusedMobId != null) {
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).entityId().equals(focusedMobId)) {
                    index = i;
                    break;
                }
            }
        }
        int columns = catalogColumns();
        int next = switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> Math.max(0, index - 1);
            case GLFW.GLFW_KEY_RIGHT -> Math.min(options.size() - 1, index + 1);
            case GLFW.GLFW_KEY_UP -> Math.max(0, index - columns);
            case GLFW.GLFW_KEY_DOWN -> Math.min(options.size() - 1, index + columns);
            default -> index;
        };
        focusedMobId = options.get(next).entityId();
        ensureFocusedMobVisible();
    }

    private void ensureFocusedMobVisible() {
        if (focusedMobId == null) {
            return;
        }
        int listY = CATALOG_Y + CATALOG_CONTROLS_HEIGHT;
        int listHeight = catalogHeight() - CATALOG_CONTROLS_HEIGHT - 4;
        int y = listY;
        for (CatalogRow row : catalogRows()) {
            if (row.type() == CatalogRowType.CARDS) {
                for (DumpNullMobOption option : row.options()) {
                    if (option.entityId().equals(focusedMobId)) {
                        if (y - mobScroll < listY) {
                            mobScroll = y - listY;
                        } else if (y + MOB_CARD_HEIGHT - mobScroll > listY + listHeight) {
                            mobScroll = y + MOB_CARD_HEIGHT - listY - listHeight;
                        }
                        mobScroll = clamp(mobScroll, 0, mobMaxScroll());
                        return;
                    }
                }
            }
            y += row.height();
        }
    }

    private List<DumpNullMobOption> visibleMobOptions() {
        List<DumpNullMobOption> options = new ArrayList<>();
        for (CatalogRow row : catalogRows()) {
            if (row.type() == CatalogRowType.CARDS) {
                options.addAll(row.options());
            }
        }
        return options;
    }

    private void activateFocusedMobAddAll() {
        ensureFocusedMobValid();
        if (focusedMobId != null) {
            cycleAllDrops(focusedMobId);
        }
    }

    private void rememberState() {
        if (mobSearchBox == null) {
            return;
        }
        Map<ResourceLocation, List<DumpNullDropCandidate>> loadedRows = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, List<DumpNullDropCandidate>> entry : dropRows.entrySet()) {
            loadedRows.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        Map<ResourceLocation, Integer> dropScrolls = new LinkedHashMap<>(cardDropScrolls);
        savedState = new SavedState(
                stateKey(),
                mobSearchBox.getValue(),
                catalogMode,
                itemCatalogView,
                activeFilter,
                mobScroll,
                itemScroll,
                acceptedGridScroll,
                discardedGridScroll,
                selectedItemId,
                focusedMobId,
                mobSearchBox.isFocused(),
                loadedRows,
                dropScrolls,
                Set.copyOf(collapsedNamespaces),
                Set.copyOf(collapsedCategories)
        );
    }

    private void restoreSavedState() {
        if (savedState == null || !savedState.key().equals(stateKey())) {
            return;
        }
        activeFilter = savedState.activeFilter();
        catalogMode = savedState.catalogMode();
        itemCatalogView = savedState.itemCatalogView();
        mobScroll = savedState.mobScroll();
        itemScroll = savedState.itemScroll();
        acceptedGridScroll = savedState.acceptedGridScroll();
        discardedGridScroll = savedState.discardedGridScroll();
        selectedItemId = savedState.selectedItemId();
        focusedMobId = savedState.focusedMobId();
        dropRows.clear();
        for (Map.Entry<ResourceLocation, List<DumpNullDropCandidate>> entry : savedState.dropRows().entrySet()) {
            dropRows.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        pendingDropRequests.removeAll(dropRows.keySet());
        cardDropScrolls.clear();
        cardDropScrolls.putAll(savedState.cardDropScrolls());
        collapsedNamespaces.clear();
        collapsedNamespaces.addAll(savedState.collapsedNamespaces());
        collapsedCategories.clear();
        collapsedCategories.addAll(savedState.collapsedCategories());
        mobSearchBox.setValue(savedState.searchText());
        mobSearchBox.setFocused(savedState.searchFocused());
        mobScroll = clamp(mobScroll, 0, mobMaxScroll());
        itemScroll = clamp(itemScroll, 0, itemMaxScroll());
        acceptedGridScroll = clamp(acceptedGridScroll, 0, gridMaxScroll(DumpNullItemStatus.ACCEPTED));
        discardedGridScroll = clamp(discardedGridScroll, 0, gridMaxScroll(DumpNullItemStatus.DISCARDED));
        ensureFocusedMobValid();
    }

    private String stateKey() {
        return menu.getSourceType() + ":" + menu.getInventorySlot() + ":" + menu.getDockPos();
    }

    private String currentDropSearchQuery() {
        return DumpNullDropSearch.normalizeQuery(mobSearchBox == null ? "" : mobSearchBox.getValue());
    }

    private List<DumpNullItemCatalogEntry> filteredItemCatalogEntries() {
        String query = currentDropSearchQuery();
        return itemCatalogEntries.stream()
                .filter(entry -> query.isBlank()
                        || entry.itemId().toString().toLowerCase(Locale.ROOT).contains(query)
                        || entry.itemId().getNamespace().toLowerCase(Locale.ROOT).contains(query)
                        || entry.label().toLowerCase(Locale.ROOT).contains(query)
                        || entry.source().toLowerCase(Locale.ROOT).contains(query)
                        || itemCatalogView.label().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    private int itemCatalogColumns() {
        return Math.max(1, (catalogWidth() + ITEM_TILE_GAP) / (ITEM_TILE_SIZE + ITEM_TILE_GAP));
    }

    private int itemCatalogContentHeight(int entries) {
        int rows = (int) Math.ceil(entries / (double) itemCatalogColumns());
        return rows * (ITEM_TILE_SIZE + ITEM_TILE_GAP);
    }

    private int itemMaxScroll() {
        return Math.max(0, itemCatalogContentHeight(filteredItemCatalogEntries().size()) - catalogListHeight());
    }

    private ItemCatalogTile itemCatalogTileAt(int localX, int localY) {
        if (catalogMode != CatalogMode.ITEMS || !insideCatalogList(localX, localY)) {
            return null;
        }
        List<DumpNullItemCatalogEntry> entries = filteredItemCatalogEntries();
        int columns = itemCatalogColumns();
        int relativeY = localY - catalogListY() + itemScroll;
        int row = relativeY / (ITEM_TILE_SIZE + ITEM_TILE_GAP);
        int column = (localX - PADDING) / (ITEM_TILE_SIZE + ITEM_TILE_GAP);
        int xOffset = (localX - PADDING) % (ITEM_TILE_SIZE + ITEM_TILE_GAP);
        int yOffset = relativeY % (ITEM_TILE_SIZE + ITEM_TILE_GAP);
        int index = row * columns + column;
        if (column < 0 || column >= columns || xOffset >= ITEM_TILE_SIZE || yOffset >= ITEM_TILE_SIZE || index < 0 || index >= entries.size()) {
            return null;
        }
        return new ItemCatalogTile(entries.get(index));
    }

    private DumpNullItemStatus statusOf(ResourceLocation itemId) {
        for (DumpNullItemStatusSummary summary : menu.getItemStatuses()) {
            if (summary.itemId().equals(itemId)) {
                return summary.status();
            }
        }
        return DumpNullItemStatus.NEUTRAL;
    }

    private Entity previewEntity(ResourceLocation entityId) {
        if (minecraft == null || minecraft.level == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
            return null;
        }
        return entityPreviewCache.computeIfAbsent(entityId, id -> {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
            return type.create(minecraft.level);
        });
    }

    private ItemStack itemStack(ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        return new ItemStack(item);
    }

    private void renderScrollbar(GuiGraphics guiGraphics, ScrollRegion region, int x, int y, int height, int scroll, int maxScroll) {
        if (maxScroll <= 0 || height <= SCROLLBAR_MIN_HANDLE) {
            return;
        }
        int handleHeight = scrollbarHandleHeight(height, maxScroll);
        int handleY = scrollbarHandleY(y, height, scroll, maxScroll);
        boolean active = activeScrollDrag != null && activeScrollDrag.region().equals(region)
                || activeMiddleScroll != null && activeMiddleScroll.region().equals(region);
        guiGraphics.fill(x, y, x + SCROLLBAR_WIDTH, y + height, 0x66242A32);
        guiGraphics.fill(x, handleY, x + SCROLLBAR_WIDTH, handleY + handleHeight, active ? 0xFFE8EDF5 : 0xCC8B96A3);
    }

    private ScrollRegion scrollRegionAt(int localX, int localY) {
        if (!pendingPresetId.isBlank()) {
            ScrollRegion region = ScrollRegion.presetPreview();
            return regionContains(region, localX, localY) && maxScrollFor(region) > 0 ? region : null;
        }
        if (catalogMode == CatalogMode.AUTOMATION) {
            return null;
        }
        ScrollRegion accepted = ScrollRegion.status(DumpNullItemStatus.ACCEPTED);
        if (regionContains(accepted, localX, localY) && maxScrollFor(accepted) > 0) {
            return accepted;
        }
        ScrollRegion discarded = ScrollRegion.status(DumpNullItemStatus.DISCARDED);
        if (regionContains(discarded, localX, localY) && maxScrollFor(discarded) > 0) {
            return discarded;
        }
        ScrollRegion card = cardDropScrollRegionAt(localX, localY);
        if (card != null && maxScrollFor(card) > 0) {
            return card;
        }
        ScrollRegion catalog = ScrollRegion.catalog();
        return regionContains(catalog, localX, localY) && maxScrollFor(catalog) > 0 ? catalog : null;
    }

    private boolean regionContains(ScrollRegion region, int localX, int localY) {
        ScrollbarBounds bounds = scrollbarBounds(region);
        return bounds != null && inside(localX, localY, bounds.contentX(), bounds.contentY(), bounds.contentWidth(), bounds.contentHeight());
    }

    private boolean scrollbarHit(ScrollRegion region, int localX, int localY) {
        ScrollbarBounds bounds = scrollbarBounds(region);
        return bounds != null && inside(localX, localY, bounds.barX(), bounds.barY(), SCROLLBAR_WIDTH, bounds.barHeight());
    }

    private void startScrollbarDrag(ScrollRegion region, int localY) {
        ScrollbarBounds bounds = scrollbarBounds(region);
        int maxScroll = maxScrollFor(region);
        if (bounds == null || maxScroll <= 0) {
            return;
        }
        int scroll = scrollFor(region);
        int handleHeight = scrollbarHandleHeight(bounds.barHeight(), maxScroll);
        int handleY = scrollbarHandleY(bounds.barY(), bounds.barHeight(), scroll, maxScroll);
        int offset;
        if (localY >= handleY && localY < handleY + handleHeight) {
            offset = localY - handleY;
        } else {
            offset = handleHeight / 2;
            setScroll(region, scrollbarScrollForTrackClick(localY, bounds.barY(), bounds.barHeight(), maxScroll));
        }
        activeScrollDrag = new ActiveScrollDrag(region, offset);
    }

    private int scrollForHandleTop(ScrollRegion region, int handleTopY) {
        ScrollbarBounds bounds = scrollbarBounds(region);
        return bounds == null ? 0 : scrollbarScrollForHandleTop(handleTopY - bounds.barY(), bounds.barHeight(), maxScrollFor(region));
    }

    private int scrollFor(ScrollRegion region) {
        return switch (region.type()) {
            case CATALOG -> catalogMode == CatalogMode.ITEMS ? itemScroll : mobScroll;
            case ACCEPTED_GRID -> acceptedGridScroll;
            case DISCARDED_GRID -> discardedGridScroll;
            case PRESET_PREVIEW -> presetPreviewScroll;
            case CARD_DROPS -> cardDropScrolls.getOrDefault(region.entityId(), 0);
        };
    }

    private void setScroll(ScrollRegion region, int value) {
        int clamped = clamp(value, 0, maxScrollFor(region));
        switch (region.type()) {
            case CATALOG -> {
                if (catalogMode == CatalogMode.ITEMS) {
                    itemScroll = clamped;
                } else {
                    mobScroll = clamped;
                }
            }
            case ACCEPTED_GRID -> acceptedGridScroll = clamped;
            case DISCARDED_GRID -> discardedGridScroll = clamped;
            case PRESET_PREVIEW -> presetPreviewScroll = clamped;
            case CARD_DROPS -> {
                if (clamped == 0) {
                    cardDropScrolls.remove(region.entityId());
                } else {
                    cardDropScrolls.put(region.entityId(), clamped);
                }
            }
        }
    }

    private int maxScrollFor(ScrollRegion region) {
        return switch (region.type()) {
            case CATALOG -> catalogMode == CatalogMode.ITEMS ? itemMaxScroll() : mobMaxScroll();
            case ACCEPTED_GRID -> gridMaxScroll(DumpNullItemStatus.ACCEPTED);
            case DISCARDED_GRID -> gridMaxScroll(DumpNullItemStatus.DISCARDED);
            case PRESET_PREVIEW -> presetPreviewMaxScroll();
            case CARD_DROPS -> {
                CardDropArea area = cardDropArea(region.entityId());
                yield area == null ? 0 : area.maxScroll();
            }
        };
    }

    private int scrollStep(ScrollRegion region, double scrollY) {
        int direction = (int) Math.signum(scrollY);
        if (direction == 0) {
            return 0;
        }
        return switch (region.type()) {
            case CATALOG -> (int) Math.round(scrollY * 24.0D);
            case ACCEPTED_GRID, DISCARDED_GRID -> (int) Math.round(scrollY * GRID_TILE_HEIGHT);
            case PRESET_PREVIEW -> (int) Math.round(scrollY * 18.0D);
            case CARD_DROPS -> direction * Math.max(1, cardDropColumns(region.entityId()));
        };
    }

    private ScrollbarBounds scrollbarBounds(ScrollRegion region) {
        return switch (region.type()) {
            case CATALOG -> new ScrollbarBounds(PADDING, catalogListY(), catalogWidth(), catalogListHeight(), catalogRight() - SCROLLBAR_WIDTH - 1, catalogListY() + 1, catalogListHeight() - 2);
            case ACCEPTED_GRID -> new ScrollbarBounds(acceptedGridX(), acceptedGridY(), gridWidth(), statusGridHeight(), acceptedGridX() + gridWidth() - SCROLLBAR_WIDTH - 1, acceptedGridY() + 1, statusGridHeight() - 2);
            case DISCARDED_GRID -> new ScrollbarBounds(discardedGridX(), discardedGridY(), gridWidth(), statusGridHeight(), discardedGridX() + gridWidth() - SCROLLBAR_WIDTH - 1, discardedGridY() + 1, statusGridHeight() - 2);
            case PRESET_PREVIEW -> new ScrollbarBounds(overlayX() + 10, presetPreviewY(), overlayWidth() - 20, presetPreviewHeight(), overlayX() + overlayWidth() - 10 - SCROLLBAR_WIDTH - 1, presetPreviewY() + 1, presetPreviewHeight() - 2);
            case CARD_DROPS -> cardDropScrollbarBounds(region.entityId());
        };
    }

    private ScrollRegion cardDropScrollRegionAt(int localX, int localY) {
        if (catalogMode != CatalogMode.MOBS) {
            return null;
        }
        CardDropArea area = cardDropAreaAt(localX, localY);
        return area == null ? null : ScrollRegion.cardDrops(area.entityId());
    }

    private CardDropArea cardDropArea(ResourceLocation entityId) {
        int y = catalogListY() - mobScroll;
        int cardWidth = catalogCardWidth();
        for (CatalogRow row : catalogRows()) {
            if (row.type() == CatalogRowType.CARDS) {
                for (int cardIndex = 0; cardIndex < row.options().size(); cardIndex++) {
                    DumpNullMobOption option = row.options().get(cardIndex);
                    if (!option.entityId().equals(entityId)) {
                        continue;
                    }
                    int cardX = PADDING + cardIndex * (cardWidth + MOB_CARD_GAP);
                    int dropsWidth = cardWidth - 12;
                    int dropsHeight = MOB_CARD_HEIGHT - cardPreviewHeight() - 8;
                    List<DumpNullDropCandidate> candidates = uniqueDrops(dropRows.get(option.entityId()));
                    int columns = Math.max(1, dropsWidth / (DROP_ICON_SIZE + DROP_ICON_GAP));
                    int rows = Math.max(1, dropsHeight / (DROP_ICON_SIZE + DROP_ICON_GAP));
                    int maxVisible = columns * rows;
                    int total = dropTileCount(candidates);
                    return new CardDropArea(option.entityId(), columns, Math.max(0, total - maxVisible));
                }
            }
            y += row.height();
        }
        return null;
    }

    private ScrollbarBounds cardDropScrollbarBounds(ResourceLocation entityId) {
        int y = catalogListY() - mobScroll;
        int cardWidth = catalogCardWidth();
        for (CatalogRow row : catalogRows()) {
            if (row.type() == CatalogRowType.CARDS) {
                for (int cardIndex = 0; cardIndex < row.options().size(); cardIndex++) {
                    DumpNullMobOption option = row.options().get(cardIndex);
                    if (!option.entityId().equals(entityId)) {
                        continue;
                    }
                    int cardX = PADDING + cardIndex * (cardWidth + MOB_CARD_GAP);
                    int dropsY = y + cardPreviewHeight() + 4;
                    int dropsHeight = MOB_CARD_HEIGHT - cardPreviewHeight() - 8;
                    return new ScrollbarBounds(cardX + 6, dropsY, cardWidth - 12, dropsHeight, cardX + cardWidth - 6, dropsY, dropsHeight);
                }
            }
            y += row.height();
        }
        return null;
    }

    private int cardDropColumns(ResourceLocation entityId) {
        CardDropArea area = cardDropArea(entityId);
        return area == null ? 1 : area.columns();
    }

    public static int scrollbarHandleHeight(int trackHeight, int maxScroll) {
        if (maxScroll <= 0 || trackHeight <= 0) {
            return trackHeight;
        }
        int contentHeight = trackHeight + maxScroll;
        return clamp(trackHeight * trackHeight / Math.max(1, contentHeight), Math.min(SCROLLBAR_MIN_HANDLE, trackHeight), trackHeight);
    }

    public static int scrollbarHandleY(int trackY, int trackHeight, int scroll, int maxScroll) {
        if (maxScroll <= 0) {
            return trackY;
        }
        int handleHeight = scrollbarHandleHeight(trackHeight, maxScroll);
        int travel = Math.max(1, trackHeight - handleHeight);
        return trackY + clamp(scroll, 0, maxScroll) * travel / maxScroll;
    }

    public static int scrollbarScrollForTrackClick(int clickY, int trackY, int trackHeight, int maxScroll) {
        int handleHeight = scrollbarHandleHeight(trackHeight, maxScroll);
        return scrollbarScrollForHandleTop(clickY - trackY - handleHeight / 2, trackHeight, maxScroll);
    }

    public static int scrollbarScrollForHandleTop(int handleTop, int trackHeight, int maxScroll) {
        if (maxScroll <= 0) {
            return 0;
        }
        int handleHeight = scrollbarHandleHeight(trackHeight, maxScroll);
        int travel = Math.max(1, trackHeight - handleHeight);
        return clamp(Math.round(handleTop * maxScroll / (float) travel), 0, maxScroll);
    }

    private Component shortPresetLabel(DumpNullPresetOption option) {
        return Component.literal(switch (option.presetId()) {
            case "vanilla" -> "Vanilla";
            case "peaceful" -> "Peace";
            case "hostile" -> "Hostile";
            case "biome" -> "Biome";
            case "dimension" -> "Dim";
            case "boss" -> "Boss";
            default -> option.presetId();
        });
    }

    private Component setupPresetDescription(DumpNullPresetOption option) {
        return Component.literal(switch (option.presetId()) {
            case "vanilla" -> "Setup Preset: selects vanilla mobs and starter discard rules after preview confirmation.";
            case "peaceful" -> "Setup Preset: selects non-hostile mobs from the live registry after preview confirmation.";
            case "hostile" -> "Setup Preset: selects monster mobs from the live registry after preview confirmation.";
            case "biome" -> "Setup Preset: selects mobs from chosen biome spawn tables after target selection and preview.";
            case "dimension" -> "Setup Preset: selects mobs from chosen loaded dimensions after target selection and preview.";
            case "boss" -> "Setup Preset: selects known boss-like mobs after preview confirmation.";
            default -> "Setup Preset: preview before applying saved DumpNull data.";
        });
    }

    private String heldModeLabel(DumpNullHeldDiscardMode mode) {
        return switch (mode) {
            case VOID -> "Void";
            case BLOCK_PICKUP -> "Block";
            case GENERATE_FE -> "Make FE";
        };
    }

    private String dockModeLabel(DumpNullDockDiscardMode mode) {
        return switch (mode) {
            case VOID -> "Void";
            case EXPORT_DISCARD_LANE -> "Export";
            case GENERATE_FE -> "Make FE";
        };
    }

    private String sideModeLabel(DumpNullSideMode mode) {
        return switch (mode) {
            case DISABLED -> "Off";
            case INPUT -> "In";
            case OUTPUT -> "Out";
            case BOTH -> "Both";
        };
    }

    private int modeColor(DumpNullSideMode mode) {
        return switch (mode) {
            case DISABLED -> 0xFF59616C;
            case INPUT -> 0xFF71C58E;
            case OUTPUT -> 0xFF73A8FF;
            case BOTH -> 0xFFFFD766;
        };
    }

    private String directionLabel(Direction direction) {
        String name = direction.getName();
        return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }

    private Component faceShortLabel(Direction direction) {
        return Component.literal(switch (direction) {
            case DOWN -> "D";
            case UP -> "U";
            case NORTH -> "N";
            case SOUTH -> "S";
            case WEST -> "W";
            case EAST -> "E";
        });
    }

    private String shortCount(int count) {
        if (count >= 1_000_000) {
            return count / 1_000_000 + "m";
        }
        if (count >= 1_000) {
            return count / 1_000 + "k";
        }
        return Integer.toString(count);
    }

    private String mobName(DumpNullMobOption option) {
        String translated = Component.translatable(option.descriptionId()).getString();
        return translated == null || translated.isBlank() || translated.equals(option.descriptionId()) ? shortId(option.entityId()) : translated;
    }

    private String trimToWidth(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String suffix = "...";
        String trimmed = text;
        while (trimmed.length() > suffix.length() && font.width(trimmed + suffix) > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + suffix;
    }

    private int gridMaxScroll(DumpNullItemStatus status) {
        int columns = statusGridColumnsForWidth(gridWidth());
        int rows = (int) Math.ceil(statusSummaries(status).size() / (double) columns);
        int contentHeight = rows * GRID_TILE_HEIGHT;
        return Math.max(0, contentHeight - statusGridHeight());
    }

    public static int statusGridColumnsForWidth(int width) {
        return Math.max(1, Math.min(4, width / 30));
    }

    public static int automationSideRows() {
        return Direction.values().length;
    }

    private static int statusTileWidth(int width) {
        return Math.max(24, width / statusGridColumnsForWidth(width));
    }

    private int mobMaxScroll() {
        int totalHeight = catalogRows().stream().mapToInt(CatalogRow::height).sum();
        return Math.max(0, totalHeight - (catalogHeight() - CATALOG_CONTROLS_HEIGHT - 4));
    }

    private int catalogListY() {
        return CATALOG_Y + CATALOG_CONTROLS_HEIGHT;
    }

    private int catalogListHeight() {
        return catalogHeight() - CATALOG_CONTROLS_HEIGHT - 4;
    }

    private boolean insideCatalogList(int localX, int localY) {
        return inside(localX, localY, PADDING, catalogListY(), catalogWidth(), catalogListHeight());
    }

    private int catalogColumns() {
        int availableWidth = catalogWidth();
        return Math.max(1, (availableWidth + MOB_CARD_GAP) / (MOB_CARD_TARGET_WIDTH + MOB_CARD_GAP));
    }

    private int catalogCardWidth() {
        int columns = catalogColumns();
        int availableWidth = catalogWidth() - MOB_CARD_GAP * (columns - 1);
        return Math.max(86, availableWidth / columns);
    }

    private int automationControlX() {
        return PADDING;
    }

    private int automationModesY() {
        return CATALOG_Y + 24;
    }

    private int automationInstallY() {
        return CATALOG_Y + 44;
    }

    private int automationListY() {
        return CATALOG_Y + 66;
    }

    private int automationModeButtonWidth() {
        return Math.max(70, Math.min(100, (catalogWidth() - 4) / 2));
    }

    private int automationInstallButtonWidth() {
        return Math.min(catalogWidth(), automationModeButtonWidth() * 2 + 4);
    }

    private int automationSideRowHeight() {
        return 18;
    }

    private Rect2i faceRect(Direction direction) {
        int x = automationControlX() + 12;
        int y = automationListY() + 18;
        int size = 34;
        int gap = 4;
        return switch (direction) {
            case UP -> new Rect2i(x + size + gap, y, size, size);
            case NORTH -> new Rect2i(x + size + gap, y + size + gap, size, size);
            case WEST -> new Rect2i(x, y + (size + gap) * 2, size, size);
            case SOUTH -> new Rect2i(x + size + gap, y + (size + gap) * 2, size, size);
            case EAST -> new Rect2i(x + (size + gap) * 2, y + (size + gap) * 2, size, size);
            case DOWN -> new Rect2i(x + size + gap, y + (size + gap) * 3, size, size);
        };
    }

    private DumpNullSideMode faceModeAt(int localX, int localY) {
        int x = automationControlX() + 116;
        int y = automationListY() + 16;
        int buttonX = x + 6;
        int buttonY = y + 34;
        for (DumpNullSideMode mode : DumpNullSideMode.values()) {
            if (inside(localX, localY, buttonX, buttonY, 30, 14)) {
                return mode;
            }
            buttonX += 34;
        }
        return null;
    }

    private boolean faceDiscardToggleAt(int localX, int localY) {
        int x = automationControlX() + 116;
        int y = automationListY() + 16;
        int width = Math.max(112, catalogWidth() - 120);
        return inside(localX, localY, x + 6, y + 58, width - 12, 16);
    }

    private int automationSideRowY(int index) {
        return automationListY() + 13 + index * automationSideRowHeight();
    }

    private int automationSideModeX() {
        return PADDING + 42;
    }

    private int automationSideModeWidth() {
        return Math.max(48, Math.min(68, (catalogWidth() - 48) / 2));
    }

    private int automationDiscardToggleX() {
        return automationSideModeX() + automationSideModeWidth() + 4;
    }

    private int automationDiscardToggleWidth() {
        return Math.max(44, catalogRight() - automationDiscardToggleX() - 4);
    }

    private int catalogHeight() {
        return imageHeight - CATALOG_Y - PADDING;
    }

    private int catalogWidth() {
        return Math.max(150, ribbonX() - PADDING * 2);
    }

    private int catalogRight() {
        return PADDING + catalogWidth();
    }

    private int mobSearchX() {
        return PADDING + 116;
    }

    private int mobSearchWidth() {
        return Math.max(48, collapseAllButtonX() - mobSearchX() - 4);
    }

    private int collapseAllButtonX() {
        return catalogRight() - ICON_BUTTON_SIZE * 2 - 3;
    }

    private int expandAllButtonX() {
        return catalogRight() - ICON_BUTTON_SIZE;
    }

    private int presetCarouselX() {
        return PADDING;
    }

    private int presetCarouselY() {
        return CATALOG_Y + 44;
    }

    private int presetCenterWidth() {
        return Math.max(64, Math.min(96, catalogWidth() - ICON_BUTTON_SIZE * 2 - presetPreviewButtonWidth() - 12));
    }

    private int presetPreviewButtonWidth() {
        return 48;
    }

    private int presetPreviewButtonX() {
        return presetCarouselX() + ICON_BUTTON_SIZE * 2 + presetCenterWidth() + 6;
    }

    private int itemViewCarouselX() {
        return PADDING;
    }

    private int itemViewCarouselY() {
        return CATALOG_Y + 24;
    }

    private int itemViewCenterWidth() {
        return Math.max(80, Math.min(128, catalogWidth() - ICON_BUTTON_SIZE * 2 - 8));
    }

    private int ribbonWidth() {
        return Math.max(RIBBON_MIN_WIDTH, Math.min(RIBBON_WIDTH, imageWidth / 3));
    }

    private int ribbonX() {
        return imageWidth - PADDING - ribbonWidth();
    }

    private int ribbonY() {
        return CATALOG_Y;
    }

    private int ribbonHeight() {
        return imageHeight - ribbonY() - PADDING;
    }

    private int acceptedGridX() {
        return ribbonX() + 4;
    }

    private int acceptedGridHeaderY() {
        return ribbonY() + 4;
    }

    private int acceptedGridY() {
        return acceptedGridHeaderY() + GRID_HEADER_HEIGHT + 2;
    }

    private int discardedGridX() {
        return ribbonX() + 4;
    }

    private int discardedGridHeaderY() {
        return acceptedGridY() + statusGridHeight() + RIBBON_GAP + 2;
    }

    private int discardedGridY() {
        return discardedGridHeaderY() + GRID_HEADER_HEIGHT + 2;
    }

    private int gridWidth() {
        return ribbonWidth() - 8;
    }

    private int statusGridHeight() {
        int available = detailY() - acceptedGridY() - RIBBON_GAP * 2 - GRID_HEADER_HEIGHT - 4;
        return Math.max(42, available / 2);
    }

    private int detailX() {
        return ribbonX() + 4;
    }

    private int detailY() {
        return imageHeight - PADDING - detailHeight();
    }

    private int detailWidth() {
        return gridWidth();
    }

    private int detailHeight() {
        return Math.min(RIBBON_DETAIL_HEIGHT, Math.max(64, ribbonHeight() / 3));
    }

    private int cardPreviewHeight() {
        return MOB_CARD_HEIGHT / 2;
    }

    private int presetDropdownX() {
        return presetCarouselX() + ICON_BUTTON_SIZE + 2;
    }

    private int presetDropdownY() {
        return presetCarouselY() + TOOLBAR_HEIGHT + 2;
    }

    private int presetDropdownWidth() {
        return Math.max(98, presetCenterWidth() + 30);
    }

    private int presetDropdownRowHeight() {
        return 16;
    }

    private int presetDropdownHeight() {
        return menu.getPresetOptions().size() * presetDropdownRowHeight() + 4;
    }

    private int presetDropdownTotalHeight() {
        return TOOLBAR_HEIGHT + 4 + presetDropdownHeight();
    }

    private int itemViewDropdownX() {
        return itemViewCarouselX() + ICON_BUTTON_SIZE + 2;
    }

    private int itemViewDropdownY() {
        return itemViewCarouselY() + TOOLBAR_HEIGHT + 2;
    }

    private int itemViewDropdownWidth() {
        return Math.max(118, itemViewCenterWidth() + 30);
    }

    private int itemViewDropdownRowHeight() {
        return 16;
    }

    private int itemViewDropdownHeight() {
        return DumpNullItemCatalogView.all().size() * itemViewDropdownRowHeight() + 4;
    }

    private int itemViewDropdownTotalHeight() {
        return TOOLBAR_HEIGHT + 4 + itemViewDropdownHeight();
    }

    private int overlayWidth() {
        return Math.min(380, imageWidth - 32);
    }

    private int overlayHeight() {
        return Math.min(260, imageHeight - 28);
    }

    private int overlayX() {
        return (imageWidth - overlayWidth()) / 2;
    }

    private int overlayY() {
        return (imageHeight - overlayHeight()) / 2;
    }

    private boolean presetNeedsTargets() {
        return !pendingPresetId.isBlank() && !DumpNullPresetOption.TARGET_NONE.equals(pendingTargetType);
    }

    private int presetTargetSearchY() {
        return overlayY() + 52;
    }

    private int presetTargetListY() {
        return presetTargetSearchY() + 22;
    }

    private int presetPreviewY() {
        return overlayY() + (presetNeedsTargets() ? 116 : 54);
    }

    private int presetPreviewHeight() {
        return Math.max(36, overlayY() + overlayHeight() - 10 - presetPreviewY());
    }

    private int presetPreviewMaxScroll() {
        return Math.max(0, presetPreviewContentHeight(overlayWidth() - 20) - presetPreviewHeight());
    }

    private int presetPreviewContentHeight(int width) {
        if (!presetPreviewResolved || presetPreviewMobIds.isEmpty()) {
            return 0;
        }
        int height = 0;
        height += presetSectionHeight(PresetSection.ACCEPTED, presetPreviewAcceptedSummaries, width);
        height += presetSectionHeight(PresetSection.DISCARDED, presetPreviewDiscardedSummaries, width);
        height += 12;
        height += presetPreviewMobIds.size() * (PRESET_PREVIEW_CARD_HEIGHT + 4);
        return height;
    }

    private int presetSectionHeight(PresetSection section, List<DumpNullItemStatusSummary> summaries, int width) {
        int height = 14;
        if (isPresetSectionCollapsed(section)) {
            return height;
        }
        return height + (summaries.isEmpty() ? 16 : presetRuleGridHeight(summaries, width) + 8);
    }

    private boolean isPresetSectionCollapsed(PresetSection section) {
        return section == PresetSection.ACCEPTED ? presetAcceptedCollapsed : presetDiscardedCollapsed;
    }

    private int presetRuleGridHeight(List<DumpNullItemStatusSummary> summaries, int width) {
        int columns = Math.max(1, width / (PRESET_RULE_ICON_SIZE + 3));
        int rows = (int) Math.ceil(summaries.size() / (double) columns);
        return rows * (PRESET_RULE_ICON_SIZE + 3);
    }

    private static boolean inside(int x, int y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int statusColor(DumpNullItemStatus status) {
        return switch (status) {
            case ACCEPTED -> 0xFF54D878;
            case DISCARDED -> 0xFFFF6868;
            case NEUTRAL -> 0xFF48505B;
        };
    }

    private static String statusLabel(DumpNullItemStatus status) {
        return switch (status) {
            case ACCEPTED -> "Accepted";
            case DISCARDED -> "Discarded";
            case NEUTRAL -> "Neutral";
        };
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static List<String> parseIdList(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private static List<ResourceLocation> parseResourceLocations(List<String> rawIds) {
        if (rawIds == null || rawIds.isEmpty()) {
            return List.of();
        }
        List<ResourceLocation> result = new ArrayList<>();
        for (String rawId : rawIds) {
            ResourceLocation id = ResourceLocation.tryParse(rawId);
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

    private static String joinIds(List<ResourceLocation> ids) {
        return String.join(", ", ids.stream().map(ResourceLocation::toString).toList());
    }

    private static String shortId(ResourceLocation id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static int compareNamespaces(String left, String right) {
        int leftRank = "minecraft".equals(left) ? 0 : 1;
        int rightRank = "minecraft".equals(right) ? 0 : 1;
        if (leftRank != rightRank) {
            return Integer.compare(leftRank, rightRank);
        }
        return left.compareTo(right);
    }

    private static String categoryKey(String namespace, String category) {
        return namespace + "\u0000" + category;
    }

    private static String categoryLabel(String category) {
        String[] words = category.toLowerCase(Locale.ROOT).split("_");
        List<String> formatted = new ArrayList<>();
        for (String word : words) {
            if (!word.isBlank()) {
                formatted.add(word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1));
            }
        }
        return formatted.isEmpty() ? category : String.join(" ", formatted);
    }

    private enum CatalogRowType {
        MOD_HEADER,
        CATEGORY_HEADER,
        CARDS
    }

    private enum CatalogMode {
        MOBS,
        ITEMS,
        AUTOMATION
    }

    private enum PresetSection {
        ACCEPTED(DumpNullItemStatus.ACCEPTED, "Accepted"),
        DISCARDED(DumpNullItemStatus.DISCARDED, "Discarded");

        private final DumpNullItemStatus status;
        private final String label;

        PresetSection(DumpNullItemStatus status, String label) {
            this.status = status;
            this.label = label;
        }

        private DumpNullItemStatus status() {
            return status;
        }

        private String label() {
            return label;
        }
    }

    private enum ScrollRegionType {
        CATALOG,
        ACCEPTED_GRID,
        DISCARDED_GRID,
        PRESET_PREVIEW,
        CARD_DROPS
    }

    private record ScrollRegion(ScrollRegionType type, ResourceLocation entityId) {
        private static ScrollRegion catalog() {
            return new ScrollRegion(ScrollRegionType.CATALOG, null);
        }

        private static ScrollRegion status(DumpNullItemStatus status) {
            return new ScrollRegion(status == DumpNullItemStatus.ACCEPTED ? ScrollRegionType.ACCEPTED_GRID : ScrollRegionType.DISCARDED_GRID, null);
        }

        private static ScrollRegion presetPreview() {
            return new ScrollRegion(ScrollRegionType.PRESET_PREVIEW, null);
        }

        private static ScrollRegion cardDrops(ResourceLocation entityId) {
            return new ScrollRegion(ScrollRegionType.CARD_DROPS, entityId);
        }
    }

    private record ScrollbarBounds(int contentX, int contentY, int contentWidth, int contentHeight, int barX, int barY, int barHeight) {
    }

    private record ActiveScrollDrag(ScrollRegion region, int handleOffsetY) {
    }

    private record ActiveMiddleScroll(ScrollRegion region, int startMouseY, int startScroll) {
    }

    private record CatalogRow(String namespace, String category, List<DumpNullMobOption> options, CatalogRowType type, int height, int count) {
        private static CatalogRow modHeader(String namespace, int count) {
            return new CatalogRow(namespace, "", List.of(), CatalogRowType.MOD_HEADER, MOB_HEADER_HEIGHT, count);
        }

        private static CatalogRow categoryHeader(String namespace, String category, int count) {
            return new CatalogRow(namespace, category, List.of(), CatalogRowType.CATEGORY_HEADER, CATEGORY_HEADER_HEIGHT, count);
        }

        private static CatalogRow cards(String namespace, String category, List<DumpNullMobOption> options) {
            return new CatalogRow(namespace, category, List.copyOf(options), CatalogRowType.CARDS, MOB_CARD_HEIGHT + MOB_CARD_GAP, options.size());
        }
    }

    private record StatusTile(DumpNullItemStatusSummary summary, int x, int width, int y) {
        private ResourceLocation itemId() {
            return summary.itemId();
        }
    }

    private record ItemCatalogTile(DumpNullItemCatalogEntry entry) {
    }

    private record DropClick(ResourceLocation entityId, DumpNullDropCandidate candidate, boolean allDrops) {
    }

    private enum CardControlType {
        COPY
    }

    private record CardControl(ResourceLocation entityId, CardControlType type) {
    }

    private record CardDropArea(ResourceLocation entityId, int columns, int maxScroll) {
    }

    private record HeaderClick(CatalogRowType type, String namespace, String category) {
    }

    private record SavedState(
            String key,
            String searchText,
            CatalogMode catalogMode,
            DumpNullItemCatalogView itemCatalogView,
            DumpNullMobFilter activeFilter,
            int mobScroll,
            int itemScroll,
            int acceptedGridScroll,
            int discardedGridScroll,
            ResourceLocation selectedItemId,
            ResourceLocation focusedMobId,
            boolean searchFocused,
            Map<ResourceLocation, List<DumpNullDropCandidate>> dropRows,
            Map<ResourceLocation, Integer> cardDropScrolls,
            Set<String> collapsedNamespaces,
            Set<String> collapsedCategories
    ) {
    }
}
