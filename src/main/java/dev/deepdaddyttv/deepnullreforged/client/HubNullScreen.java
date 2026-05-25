package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatus;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullDampResourceKind;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullDampResourceSummary;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullDumpRuleSummary;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullResourceSummary;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullResourceTab;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationNullType;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationRef;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationSnapshot;
import dev.deepdaddyttv.deepnullreforged.hubnull.HubNullStationStatus;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.menu.HubNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.HubNullPayloads;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HubNullScreen extends AbstractContainerScreen<HubNullMenu> {
    private static final int WIDTH = 382;
    private static final int HEIGHT = 236;
    private static final int PADDING = 8;
    private static final int STATION_WIDTH = 150;
    private static final int ROW_HEIGHT = 24;
    private static final int TILE_SIZE = 26;
    private static final int TILE_GAP = 4;
    private static final int PANEL_TOP = 96;
    private static final int ICON_BUTTON_SIZE = 14;
    private static final int COPY_ICON_SIZE = 10;
    private static final ResourceLocation COPY_ICON = DeepNullReforged.id("textures/gui/widgets/dumpnull_copy.png");
    private static final int[] STATION_COLORS = {
            0xFF5AD7FF,
            0xFFFFB45A,
            0xFFB58CFF,
            0xFF6CE38E,
            0xFFFF6F91,
            0xFFFFE066,
            0xFF58E5D1,
            0xFFFF8CDA
    };

    private EditBox searchBox;
    private Button deepTabButton;
    private Button dampTabButton;
    private Button dumpTabButton;
    private Button denTabButton;
    private Button sortButton;
    private Button separateButton;
    private Button searchContextPreviousButton;
    private Button searchContextButton;
    private Button searchContextNextButton;
    private Button sortPreviousButton;
    private Button sortNextButton;
    private Button separatePreviousButton;
    private Button separateNextButton;
    private Button collapseButton;
    private Button expandButton;
    private int stationScroll;
    private int resourceScroll;
    private int refreshTicks;
    private HubNullResourceTab activeTab = HubNullResourceTab.DEEP;
    private SearchContext searchContext = SearchContext.BOTH;
    private boolean searchContextDropdownOpen;
    private final Map<HubNullResourceTab, Set<String>> collapsedKeys = new EnumMap<>(HubNullResourceTab.class);
    private final Map<HubNullResourceTab, Boolean> ascendingByTab = new EnumMap<>(HubNullResourceTab.class);
    private final Map<HubNullResourceTab, Boolean> separateByTab = new EnumMap<>(HubNullResourceTab.class);
    private final Map<ResourceLocation, Entity> denEntityPreviewCache = new LinkedHashMap<>();
    private ResourceHighlightKey selectedResourceHighlight;
    private HubNullStationRef draggedStation;
    private double stationDragOffsetY;
    private double stationDragMouseY;

    public HubNullScreen(HubNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelY = HEIGHT + 200;
        this.titleLabelX = PADDING;
        this.titleLabelY = PADDING;
        for (HubNullResourceTab tab : HubNullResourceTab.values()) {
            collapsedKeys.put(tab, new HashSet<>());
        }
        separateByTab.put(HubNullResourceTab.DUMP, true);
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos + PADDING;
        searchBox = new EditBox(font, x, topPos + 32, 118, 16, Component.translatable("menu.deepnullreforged.hub_null.search"));
        searchBox.setHint(Component.translatable("menu.deepnullreforged.hub_null.search"));
        searchBox.setResponder(ignored -> {
            stationScroll = 0;
            resourceScroll = 0;
            if (!canReorderStations()) {
                cancelStationDrag();
            }
            clampScrolls();
        });
        addRenderableWidget(searchBox);

        searchContextPreviousButton = Button.builder(Component.literal("<"), button -> cycleSearchContext(-1))
                .bounds(leftPos + 132, topPos + 32, 14, 16)
                .tooltip(delayed("menu.deepnullreforged.hub_null.search_context.previous.tooltip"))
                .build();
        searchContextPreviousButton.setTooltipDelay(Duration.ofMillis(250));
        addRenderableWidget(searchContextPreviousButton);

        searchContextButton = Button.builder(Component.empty(), button -> {
                    searchContextDropdownOpen = !searchContextDropdownOpen;
                    updateControlLabels();
                })
                .bounds(leftPos + 148, topPos + 32, 54, 16)
                .tooltip(searchContextTooltip())
                .build();
        searchContextButton.setTooltipDelay(Duration.ofMillis(250));
        addRenderableWidget(searchContextButton);

        searchContextNextButton = Button.builder(Component.literal(">"), button -> cycleSearchContext(1))
                .bounds(leftPos + 204, topPos + 32, 14, 16)
                .tooltip(delayed("menu.deepnullreforged.hub_null.search_context.next.tooltip"))
                .build();
        searchContextNextButton.setTooltipDelay(Duration.ofMillis(250));
        addRenderableWidget(searchContextNextButton);

        Button refreshButton = Button.builder(Component.translatable("menu.deepnullreforged.hub_null.refresh"), button -> refresh())
                .bounds(leftPos + PADDING, topPos + 52, 54, 16)
                .tooltip(delayed("menu.deepnullreforged.hub_null.refresh.tooltip"))
                .build();
        refreshButton.setTooltipDelay(Duration.ofMillis(250));
        addRenderableWidget(refreshButton);

        deepTabButton = tabButton(HubNullResourceTab.DEEP, leftPos + 68);
        dampTabButton = tabButton(HubNullResourceTab.DAMP, leftPos + 107);
        dumpTabButton = tabButton(HubNullResourceTab.DUMP, leftPos + 146);
        denTabButton = tabButton(HubNullResourceTab.DEN, leftPos + 185);
        addRenderableWidget(deepTabButton);
        addRenderableWidget(dampTabButton);
        addRenderableWidget(dumpTabButton);
        addRenderableWidget(denTabButton);

        sortPreviousButton = Button.builder(Component.literal("<"), button -> cycleSort())
                .bounds(leftPos + 58, topPos + 72, 14, 16)
                .tooltip(delayed("menu.deepnullreforged.hub_null.sort.tooltip"))
                .build();
        sortPreviousButton.setTooltipDelay(Duration.ofMillis(250));
        addRenderableWidget(sortPreviousButton);

        sortButton = Button.builder(Component.empty(), button -> cycleSort())
                .bounds(leftPos + 74, topPos + 72, 52, 16)
                .tooltip(delayed("menu.deepnullreforged.hub_null.sort.tooltip"))
                .build();
        sortButton.setTooltipDelay(Duration.ofMillis(250));
        addRenderableWidget(sortButton);

        sortNextButton = Button.builder(Component.literal(">"), button -> cycleSort())
                .bounds(leftPos + 128, topPos + 72, 14, 16)
                .tooltip(delayed("menu.deepnullreforged.hub_null.sort.tooltip"))
                .build();
        sortNextButton.setTooltipDelay(Duration.ofMillis(250));
        addRenderableWidget(sortNextButton);

        separatePreviousButton = Button.builder(Component.literal("<"), button -> cycleSeparate())
                .bounds(leftPos + 180, topPos + 72, 14, 16)
                .tooltip(delayed("menu.deepnullreforged.hub_null.group.tooltip"))
                .build();
        separatePreviousButton.setTooltipDelay(Duration.ofMillis(250));
        addRenderableWidget(separatePreviousButton);

        separateButton = Button.builder(Component.empty(), button -> cycleSeparate())
                .bounds(leftPos + 196, topPos + 72, 52, 16)
                .tooltip(delayed("menu.deepnullreforged.hub_null.group.tooltip"))
                .build();
        separateButton.setTooltipDelay(Duration.ofMillis(250));
        addRenderableWidget(separateButton);

        separateNextButton = Button.builder(Component.literal(">"), button -> cycleSeparate())
                .bounds(leftPos + 250, topPos + 72, 14, 16)
                .tooltip(delayed("menu.deepnullreforged.hub_null.group.tooltip"))
                .build();
        separateNextButton.setTooltipDelay(Duration.ofMillis(250));
        addRenderableWidget(separateNextButton);

        collapseButton = Button.builder(Component.literal("-"), button -> collapseAll())
                .bounds(leftPos + WIDTH - 48, topPos + 72, 16, 16)
                .tooltip(delayed("menu.deepnullreforged.hub_null.collapse_all.tooltip"))
                .build();
        collapseButton.setTooltipDelay(Duration.ofMillis(250));
        addRenderableWidget(collapseButton);
        expandButton = Button.builder(Component.literal("+"), button -> expandAll())
                .bounds(leftPos + WIDTH - 30, topPos + 72, 16, 16)
                .tooltip(delayed("menu.deepnullreforged.hub_null.expand_all.tooltip"))
                .build();
        expandButton.setTooltipDelay(Duration.ofMillis(250));
        addRenderableWidget(expandButton);
        updateControlLabels();
    }

    private Button tabButton(HubNullResourceTab tab, int x) {
        Button button = Button.builder(tabLabel(tab), clicked -> {
            activeTab = tab;
            resourceScroll = 0;
            updateControlLabels();
        }).bounds(x, topPos + 52, 36, 16).tooltip(Tooltip.create(tabLabel(tab))).build();
        button.setTooltipDelay(Duration.ofMillis(250));
        return button;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (++refreshTicks >= 20) {
            refreshTicks = 0;
            refresh();
        }
    }

    public void handleStateUpdated() {
        if (draggingStation() && stationByRef(draggedStation) == null) {
            cancelStationDrag();
        }
        clampScrolls();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderSearchContextDropdown(guiGraphics, mouseX, mouseY);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xEE15171B);
        guiGraphics.fill(leftPos + 1, topPos + 1, leftPos + WIDTH - 1, topPos + HEIGHT - 1, 0xEE22262D);
        guiGraphics.drawString(font, title, leftPos + PADDING, topPos + PADDING, 0xF2F2F2, false);

        renderSummary(guiGraphics);
        renderFilterLabels(guiGraphics);
        renderStations(guiGraphics, mouseX, mouseY);
        renderResources(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (handleSearchContextDropdownClick(mouseX, mouseY, button)) {
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && draggingStation()) {
            cancelStationDrag();
            return true;
        }
        if (handleStationClick(mouseX, mouseY, button) || handleResourceClick(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleSearchContextDropdownClick(double mouseX, double mouseY, int button) {
        if (!searchContextDropdownOpen || searchContextButton == null) {
            return false;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        int dropdownX = searchContextButton.getX();
        int dropdownY = searchContextButton.getY() + 18;
        int dropdownWidth = 54;
        int rowHeight = 16;
        SearchContext[] contexts = SearchContext.values();
        if (in(mouseX, mouseY, dropdownX, dropdownY, dropdownWidth, contexts.length * rowHeight)) {
            int index = ((int) mouseY - dropdownY) / rowHeight;
            setSearchContext(contexts[Math.max(0, Math.min(contexts.length - 1, index))]);
            return true;
        }
        if (in(mouseX, mouseY, searchContextButton.getX(), searchContextButton.getY(), 54, 16)) {
            return false;
        }
        searchContextDropdownOpen = false;
        updateControlLabels();
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && draggingStation()) {
            stationDragMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && draggingStation()) {
            finishStationDrag(mouseY);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int stationX = leftPos + PADDING;
        int stationY = topPos + PANEL_TOP;
        int resourceX = leftPos + PADDING + STATION_WIDTH + 8;
        int listBottom = topPos + HEIGHT - PADDING;
        if (in(mouseX, mouseY, stationX, stationY, STATION_WIDTH, listBottom - stationY)) {
            stationScroll = Math.max(0, stationScroll - (int) Math.signum(scrollY));
            return true;
        }
        if (in(mouseX, mouseY, resourceX, stationY, WIDTH - STATION_WIDTH - 24, listBottom - stationY)) {
            resourceScroll = Math.max(0, resourceScroll - (int) Math.signum(scrollY));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && draggingStation()) {
            cancelStationDrag();
            return true;
        }
        if (searchBox != null && searchBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
            if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                clampScrolls();
                return true;
            }
            if (isPrintableSearchKey(keyCode)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox != null && searchBox.isFocused() && searchBox.charTyped(codePoint, modifiers)) {
            clampScrolls();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void renderSummary(GuiGraphics guiGraphics) {
        int online = count(HubNullStationStatus.ONLINE);
        int empty = count(HubNullStationStatus.EMPTY);
        int unsupported = count(HubNullStationStatus.UNSUPPORTED);
        int unavailable = count(HubNullStationStatus.UNLOADED) + count(HubNullStationStatus.MISSING);
        Component summary = Component.translatable(
                "menu.deepnullreforged.hub_null.summary",
                menu.getStationSnapshots().size(),
                online,
                empty,
                unsupported,
                unavailable
        );
        guiGraphics.drawString(font, trim(summary.getString(), 216), leftPos + PADDING, topPos + 20, 0xB8C7D9, false);
    }

    private void renderFilterLabels(GuiGraphics guiGraphics) {
        guiGraphics.drawString(font, Component.translatable("menu.deepnullreforged.hub_null.filters"), leftPos + PADDING, topPos + 62, 0xDCE7F5, false);
        guiGraphics.drawString(font, Component.translatable("menu.deepnullreforged.hub_null.filter.order"), leftPos + PADDING, topPos + 76, 0xAEBBCC, false);
        guiGraphics.drawString(font, Component.translatable("menu.deepnullreforged.hub_null.filter.group"), leftPos + 146, topPos + 76, 0xAEBBCC, false);
        guiGraphics.drawString(font, Component.translatable("menu.deepnullreforged.hub_null.filter.sections"), leftPos + WIDTH - 100, topPos + 76, 0xAEBBCC, false);
    }

    private void renderSearchContextDropdown(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!searchContextDropdownOpen || searchContextButton == null) {
            return;
        }
        int x = searchContextButton.getX();
        int y = searchContextButton.getY() + 18;
        int width = 54;
        int rowHeight = 16;
        SearchContext[] contexts = SearchContext.values();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 240.0F);
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + contexts.length * rowHeight + 1, 0xF00A0D12);
        guiGraphics.renderOutline(x - 1, y - 1, width + 2, contexts.length * rowHeight + 2, 0xFF6A7485);
        for (int index = 0; index < contexts.length; index++) {
            SearchContext context = contexts[index];
            int rowY = y + index * rowHeight;
            boolean hovered = in(mouseX, mouseY, x, rowY, width, rowHeight);
            int rowColor = context == searchContext ? 0xFF344052 : hovered ? 0xFF28303A : 0xFF151A22;
            guiGraphics.fill(x, rowY, x + width, rowY + rowHeight, rowColor);
            guiGraphics.drawCenteredString(font, context.label(), x + width / 2, rowY + 4, 0xFFEAF2FF);
            if (hovered) {
                guiGraphics.renderTooltip(font, Component.translatable(context.tooltipKey()), mouseX, mouseY);
            }
        }
        guiGraphics.pose().popPose();
    }

    private void renderStations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = leftPos + PADDING;
        int y = topPos + PANEL_TOP;
        int h = HEIGHT - PANEL_TOP - PADDING;
        guiGraphics.fill(x, y, x + STATION_WIDTH, y + h, 0xAA111318);
        guiGraphics.drawString(font, Component.translatable("menu.deepnullreforged.hub_null.stations"), x + 5, y + 4, 0xE8E8E8, false);
        List<HubNullStationSnapshot> visible = filteredStations();
        List<HubNullStationSnapshot> rows = draggingStation()
                ? visible.stream().filter(station -> !sameStation(station, draggedStation)).toList()
                : visible;
        int rowY = y + 18;
        int first = Math.min(stationScroll, Math.max(0, rows.size()));
        int maxRows = Math.max(0, (h - 20) / ROW_HEIGHT);
        int targetIndex = draggingStation() ? stationTargetIndex(rows, mouseY) : -1;
        for (int i = first; i < rows.size() && i < first + maxRows; i++) {
            HubNullStationSnapshot station = rows.get(i);
            int drawY = rowY + (i - first) * ROW_HEIGHT;
            if (draggingStation() && targetIndex == i) {
                renderStationInsertMarker(guiGraphics, x, drawY - 2);
            }
            renderStationRow(guiGraphics, station, x, drawY, mouseX, mouseY, false);
        }
        if (draggingStation()) {
            int endVisibleIndex = Math.min(rows.size(), first + maxRows);
            if (targetIndex >= endVisibleIndex && targetIndex <= rows.size()) {
                int markerY = rowY + (endVisibleIndex - first) * ROW_HEIGHT - 2;
                renderStationInsertMarker(guiGraphics, x, markerY);
            }
            renderDraggedStation(guiGraphics, x);
        }
        renderScrollbar(guiGraphics, x + STATION_WIDTH - 5, y + 18, h - 22, rows.size(), maxRows, stationScroll);
    }

    private void renderStationRow(GuiGraphics guiGraphics, HubNullStationSnapshot station, int x, int drawY, int mouseX, int mouseY, boolean dragging) {
        boolean hovered = in(mouseX, mouseY, x + 3, drawY, STATION_WIDTH - 6, ROW_HEIGHT - 2);
        boolean handleExpanded = dragging || hovered;
        boolean highlighted = stationHighlighted(station);
        int rowColor = highlighted ? 0xDD344052 : 0xAA1D222A;
        int color = stationColor(station);
        guiGraphics.fill(x + 3, drawY, x + STATION_WIDTH - 3, drawY + ROW_HEIGHT - 2, rowColor);
        if (highlighted) {
            guiGraphics.renderOutline(x + 3, drawY, STATION_WIDTH - 6, ROW_HEIGHT - 2, 0xFFEAF2FF);
        }
        int handleWidth = handleExpanded ? 18 : 2;
        guiGraphics.fill(x + 3, drawY, x + 3 + handleWidth, drawY + ROW_HEIGHT - 2, color);
        if (handleExpanded) {
            drawBurgerHandle(guiGraphics, x + 8, drawY + 6, canReorderStations() ? 0xFFFFFFFF : 0x88FFFFFF);
        }

        int contentX = x + (handleExpanded ? 24 : 8);
        int textX = contentX;
        ItemStack previewStack = station.previewStack();
        if (previewStack != null && !previewStack.isEmpty()) {
            guiGraphics.renderItem(previewStack, contentX, drawY + 3);
            textX = contentX + 19;
        }
        int labelWidth = station.status() == HubNullStationStatus.ONLINE ? x + STATION_WIDTH - 55 - textX : x + STATION_WIDTH - 20 - textX;
        guiGraphics.drawString(font, trim(stationCompactLabel(station), Math.max(34, labelWidth)), textX, drawY + 3, 0xF2F2F2, false);
        guiGraphics.drawString(font, trim(stationStatusLine(station), Math.max(48, x + STATION_WIDTH - 18 - textX)), textX, drawY + 13, statusColor(station.status()), false);
        if (station.status() == HubNullStationStatus.ONLINE) {
            guiGraphics.drawString(font, "Open", x + STATION_WIDTH - 50, drawY + 3, 0x8EEB98, false);
        }
        guiGraphics.drawString(font, "X", x + STATION_WIDTH - 14, drawY + 3, 0xF07C7C, false);
        if (hovered && !dragging) {
            if (in(mouseX, mouseY, x + 3, drawY, 18, ROW_HEIGHT - 2) && !canReorderStations()) {
                guiGraphics.renderTooltip(font, Component.translatable("menu.deepnullreforged.hub_null.reorder_search_blocked"), mouseX, mouseY);
                return;
            }
            renderStationTooltip(guiGraphics, station, mouseX, mouseY);
        }
    }

    private void drawBurgerHandle(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.fill(x, y, x + 8, y + 1, color);
        guiGraphics.fill(x, y + 4, x + 8, y + 5, color);
        guiGraphics.fill(x, y + 8, x + 8, y + 9, color);
    }

    private void renderStationTooltip(GuiGraphics guiGraphics, HubNullStationSnapshot station, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal(stationDisplayName(station)));
        tooltip.add(Component.literal(station.ref().dimension().toString()).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal(coordinateText(station.ref())).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(statusText(station)).withStyle(ChatFormatting.GRAY));
        if (station.nullType() != HubNullStationNullType.NONE) {
            tooltip.add(Component.literal("Type: " + nullTypeLabel(station.nullType())).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (!station.tierName().isBlank()) {
            tooltip.add(Component.literal("Tier: " + station.tierName().toLowerCase()).withStyle(ChatFormatting.DARK_GRAY));
        }
        guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }

    private void renderStationInsertMarker(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x + 4, y, x + STATION_WIDTH - 4, y + 2, 0xFFEAF2FF);
    }

    private void renderDraggedStation(GuiGraphics guiGraphics, int x) {
        HubNullStationSnapshot station = stationByRef(draggedStation);
        if (station == null) {
            return;
        }
        int drawY = (int) Math.round(stationDragMouseY - stationDragOffsetY);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 220.0F);
        renderStationRow(guiGraphics, station, x, drawY, -1, -1, true);
        guiGraphics.pose().popPose();
    }

    private void renderResources(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = leftPos + PADDING + STATION_WIDTH + 8;
        int y = topPos + PANEL_TOP;
        int w = WIDTH - STATION_WIDTH - 24;
        int h = HEIGHT - PANEL_TOP - PADDING;
        guiGraphics.fill(x, y, x + w, y + h, 0xAA111318);
        guiGraphics.drawString(font, tabLabel(activeTab), x + 5, y + 4, 0xE8E8E8, false);

        List<ResourceLine> lines = resourceLines(w);
        int lineY = y + 18;
        int first = Math.min(resourceScroll, Math.max(0, lines.size()));
        int visibleHeight = h - 22;
        int consumed = 0;
        List<Component> hoveredResourceTooltip = null;
        boolean copyTooltip = false;
        guiGraphics.flush();
        guiGraphics.enableScissor(x, y + 18, x + w - 6, y + h - 2);
        try {
            for (int i = first; i < lines.size() && consumed < visibleHeight; i++) {
                ResourceLine line = lines.get(i);
                int drawY = lineY + consumed;
                if (line.type != LineType.ITEMS) {
                    int bg = line.type == LineType.CATEGORY ? 0xAA252A33 : 0xAA20252D;
                    int indent = line.type == LineType.CATEGORY ? 0 : 8;
                    boolean collapsed = collapsedKeys().contains(line.key);
                    guiGraphics.fill(x + 3 + indent, drawY, x + w - 3, drawY + 15, bg);
                    renderSectionToggle(guiGraphics, x + 7 + indent, drawY + 1, collapsed, in(mouseX, mouseY, x + 7 + indent, drawY + 1, 12, 12));
                    guiGraphics.drawString(font, trim(line.title + " (" + compact(line.total) + ")", w - 76 - indent), x + 24 + indent, drawY + 4, 0xDCE7F5, false);
                    int copyX = x + w - ICON_BUTTON_SIZE - 7;
                    renderCopyIconButton(guiGraphics, copyX, drawY + 1, in(mouseX, mouseY, copyX, drawY + 1, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE));
                    if (in(mouseX, mouseY, copyX, drawY + 1, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)) {
                        copyTooltip = true;
                    }
                    consumed += 17;
                } else {
                    for (int col = 0; col < line.items.size(); col++) {
                        ResourceEntry entry = line.items.get(col);
                        int tileX = x + 8 + col * (TILE_SIZE + TILE_GAP);
                        if (renderResourceTile(guiGraphics, entry, tileX, drawY, mouseX, mouseY)
                                && in(mouseX, mouseY, x, y + 18, w, h - 20)) {
                            hoveredResourceTooltip = entry.tooltip();
                        }
                    }
                    consumed += TILE_SIZE + 12;
                }
            }
            guiGraphics.flush();
        } finally {
            guiGraphics.disableScissor();
        }
        renderScrollbar(guiGraphics, x + w - 5, y + 18, h - 22, lines.size(), Math.max(1, visibleHeight / 17), resourceScroll);
        if (copyTooltip) {
            guiGraphics.renderTooltip(font, Component.translatable("menu.deepnullreforged.hub_null.copy.tooltip"), mouseX, mouseY);
        } else if (hoveredResourceTooltip != null) {
            guiGraphics.renderComponentTooltip(font, hoveredResourceTooltip, mouseX, mouseY);
        }
    }

    private void renderSectionToggle(GuiGraphics guiGraphics, int x, int y, boolean collapsed, boolean hovered) {
        guiGraphics.fill(x, y, x + 12, y + 12, hovered ? 0xF02B3340 : 0xD7212832);
        guiGraphics.renderOutline(x, y, 12, 12, 0xFF73A8FF);
        guiGraphics.drawCenteredString(font, collapsed ? "+" : "-", x + 6, y + 2, 0xFFEAF2FF);
    }

    private void renderCopyIconButton(GuiGraphics guiGraphics, int x, int y, boolean hovered) {
        guiGraphics.fill(x, y, x + ICON_BUTTON_SIZE, y + ICON_BUTTON_SIZE, hovered ? 0xF02B3340 : 0xD7212832);
        guiGraphics.renderOutline(x, y, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE, 0xFF73A8FF);
        int inset = Math.max(0, (ICON_BUTTON_SIZE - COPY_ICON_SIZE) / 2);
        guiGraphics.blit(COPY_ICON, x + inset, y + inset, 0.0F, 0.0F, COPY_ICON_SIZE, COPY_ICON_SIZE, COPY_ICON_SIZE, COPY_ICON_SIZE);
    }

    private boolean renderResourceTile(GuiGraphics guiGraphics, ResourceEntry entry, int x, int y, int mouseX, int mouseY) {
        guiGraphics.fill(x, y, x + TILE_SIZE, y + TILE_SIZE, 0xCC20252E);
        renderStationBorder(guiGraphics, entry, x, y);
        if (entry.tab == HubNullResourceTab.DAMP) {
            renderDampResourceTile(guiGraphics, entry, x, y);
        } else if (entry.tab == HubNullResourceTab.DEN) {
            if (!renderDenResourceTile(guiGraphics, entry, x, y)) {
                guiGraphics.renderItem(new ItemStack(Items.SPAWNER), x + 5, y + 4);
            }
        } else {
            Item item = BuiltInRegistries.ITEM.get(entry.id);
            ItemStack stack = item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, x + 5, y + 3);
            }
        }
        if (entry.tab != HubNullResourceTab.DUMP) {
            guiGraphics.drawString(font, compact(entry.amount), x + 2, y + TILE_SIZE + 1, 0xD8E6F2, false);
        }
        return in(mouseX, mouseY, x, y, TILE_SIZE, entry.tab == HubNullResourceTab.DUMP ? TILE_SIZE : TILE_SIZE + 10);
    }

    private boolean renderDenResourceTile(GuiGraphics guiGraphics, ResourceEntry entry, int x, int y) {
        Entity entity = denEntityPreview(entry.id);
        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }
        float entityHeight = Math.max(0.6F, livingEntity.getBbHeight());
        float entityWidth = Math.max(0.6F, livingEntity.getBbWidth());
        int scale = Math.max(12, Math.min(24, Math.round(17.0F / Math.max(entityHeight, entityWidth * 1.15F))));
        guiGraphics.enableScissor(x + 1, y + 1, x + TILE_SIZE - 1, y + TILE_SIZE - 1);
        InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, x + 1, y + 1, x + TILE_SIZE - 1, y + TILE_SIZE - 1, scale, 0.15F, 0.0F, 0.0F, livingEntity);
        guiGraphics.disableScissor();
        return true;
    }

    private Entity denEntityPreview(ResourceLocation id) {
        if (minecraft == null || minecraft.level == null) {
            return null;
        }
        return denEntityPreviewCache.computeIfAbsent(id, key -> BuiltInRegistries.ENTITY_TYPE.getOptional(key)
                .map(type -> type.create(minecraft.level))
                .orElse(null));
    }

    private void renderDampResourceTile(GuiGraphics guiGraphics, ResourceEntry entry, int x, int y) {
        if (entry.dampKind == HubNullDampResourceKind.FLUID && renderFluidResourceTile(guiGraphics, entry, x, y)) {
            return;
        }
        renderTintedDampFallback(guiGraphics, entry, x, y);
    }

    private boolean renderFluidResourceTile(GuiGraphics guiGraphics, ResourceEntry entry, int x, int y) {
        Fluid fluid = BuiltInRegistries.FLUID.get(entry.id);
        if (fluid == Fluids.EMPTY) {
            return false;
        }
        FluidStack fluidStack = new FluidStack(fluid, 1);
        IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation texture = clientFluid.getStillTexture(fluidStack);
        if (texture == null) {
            return false;
        }
        TextureAtlasSprite sprite = FluidSpriteCache.getSprite(texture);
        int tint = clientFluid.getTintColor(fluidStack);
        float alpha = ((tint >> 24) & 0xFF) / 255.0F;
        float red = ((tint >> 16) & 0xFF) / 255.0F;
        float green = ((tint >> 8) & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;
        int drawX = x + 5;
        int drawY = y + 4;
        int drawWidth = TILE_SIZE - 10;
        int drawHeight = TILE_SIZE - 9;
        for (int offsetY = 0; offsetY < drawHeight; offsetY += 16) {
            int sliceHeight = Math.min(16, drawHeight - offsetY);
            guiGraphics.blit(drawX, drawY + offsetY, 0, drawWidth, sliceHeight, sprite, red, green, blue, alpha <= 0.0F ? 1.0F : alpha);
        }
        return true;
    }

    private void renderTintedDampFallback(GuiGraphics guiGraphics, ResourceEntry entry, int x, int y) {
        int tint = entry.tint == 0 ? 0xFF3AA7FF : entry.tint;
        guiGraphics.fill(x + 5, y + 4, x + TILE_SIZE - 5, y + TILE_SIZE - 5, tint);
        guiGraphics.fill(x + 8, y + 7, x + TILE_SIZE - 8, y + TILE_SIZE - 8, 0x5522262D);
    }

    private void renderStationBorder(GuiGraphics guiGraphics, ResourceEntry entry, int x, int y) {
        if (entry.stations.isEmpty()) {
            return;
        }
        int primaryColor = entry.tab == HubNullResourceTab.DUMP ? dumpStatusColor(entry.status) : entry.stations.get(0).color;
        guiGraphics.fill(x, y, x + TILE_SIZE, y + 1, primaryColor);
        guiGraphics.fill(x, y + TILE_SIZE - 1, x + TILE_SIZE, y + TILE_SIZE, primaryColor);
        guiGraphics.fill(x, y, x + 1, y + TILE_SIZE, primaryColor);
        guiGraphics.fill(x + TILE_SIZE - 1, y, x + TILE_SIZE, y + TILE_SIZE, primaryColor);
        if (entry.stations.size() <= 1) {
            return;
        }
        int accentWidth = Math.max(3, (TILE_SIZE - 4) / Math.min(5, entry.stations.size()));
        for (int i = 0; i < entry.stations.size() && i < 5; i++) {
            int accentX = x + 2 + i * accentWidth;
            guiGraphics.fill(accentX, y + TILE_SIZE - 3, Math.min(x + TILE_SIZE - 2, accentX + accentWidth - 1), y + TILE_SIZE - 1, entry.stations.get(i).color);
        }
    }

    private static int dumpStatusColor(DumpNullItemStatus status) {
        return status == DumpNullItemStatus.ACCEPTED ? 0xFF6CE38E : 0xFFFF5A5A;
    }

    private boolean handleStationClick(double mouseX, double mouseY, int button) {
        int x = leftPos + PADDING;
        int y = topPos + PANEL_TOP;
        int h = HEIGHT - PANEL_TOP - PADDING;
        if (!in(mouseX, mouseY, x, y + 18, STATION_WIDTH, h - 20)) {
            return false;
        }
        List<HubNullStationSnapshot> visible = filteredStations();
        int row = ((int) mouseY - (y + 18)) / ROW_HEIGHT;
        int index = stationScroll + row;
        if (index < 0 || index >= visible.size()) {
            return false;
        }
        HubNullStationSnapshot station = visible.get(index);
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (in(mouseX, mouseY, x + 3, y + 18 + row * ROW_HEIGHT, 18, ROW_HEIGHT - 2)) {
            if (!canReorderStations()) {
                return true;
            }
            draggedStation = station.ref();
            stationDragMouseY = mouseY;
            stationDragOffsetY = mouseY - (y + 18 + row * ROW_HEIGHT);
            return true;
        }
        if (mouseX >= x + STATION_WIDTH - 18) {
            PacketDistributor.sendToServer(new HubNullPayloads.RemoveStationPayload(menu.containerId, station.ref()));
            return true;
        }
        if (station.status() == HubNullStationStatus.ONLINE && mouseX >= x + STATION_WIDTH - 54) {
            PacketDistributor.sendToServer(new HubNullPayloads.OpenStationPayload(menu.containerId, station.ref()));
            return true;
        }
        return true;
    }

    private boolean handleResourceClick(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            ResourceEntry entry = resourceEntryAt(mouseX, mouseY);
            if (entry != null) {
                toggleResourceHighlight(entry);
                return true;
            }
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        int x = leftPos + PADDING + STATION_WIDTH + 8;
        int y = topPos + PANEL_TOP;
        int w = WIDTH - STATION_WIDTH - 24;
        int h = HEIGHT - PANEL_TOP - PADDING;
        if (!in(mouseX, mouseY, x, y + 18, w, h - 20)) {
            return false;
        }
        List<ResourceLine> lines = resourceLines(w);
        int cursorY = y + 18;
        int first = Math.min(resourceScroll, Math.max(0, lines.size()));
        int consumed = 0;
        for (int i = first; i < lines.size() && consumed < h - 22; i++) {
            ResourceLine line = lines.get(i);
            int lineHeight = line.type == LineType.ITEMS ? TILE_SIZE + 12 : 17;
            if (mouseY >= cursorY + consumed && mouseY < cursorY + consumed + lineHeight) {
                if (line.type == LineType.ITEMS) {
                    return false;
                }
                int copyX = x + w - ICON_BUTTON_SIZE - 7;
                if (in(mouseX, mouseY, copyX, cursorY + consumed + 1, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)) {
                    copyToClipboard(line.copyText);
                    return true;
                }
                if (!collapsedKeys().add(line.key)) {
                    collapsedKeys().remove(line.key);
                }
                return true;
            }
            consumed += lineHeight;
        }
        return false;
    }

    private ResourceEntry resourceEntryAt(double mouseX, double mouseY) {
        int x = leftPos + PADDING + STATION_WIDTH + 8;
        int y = topPos + PANEL_TOP;
        int w = WIDTH - STATION_WIDTH - 24;
        int h = HEIGHT - PANEL_TOP - PADDING;
        if (!in(mouseX, mouseY, x, y + 18, w, h - 20)) {
            return null;
        }
        List<ResourceLine> lines = resourceLines(w);
        int first = Math.min(resourceScroll, Math.max(0, lines.size()));
        int consumed = 0;
        for (int i = first; i < lines.size() && consumed < h - 22; i++) {
            ResourceLine line = lines.get(i);
            int drawY = y + 18 + consumed;
            if (line.type == LineType.ITEMS) {
                for (int col = 0; col < line.items.size(); col++) {
                    int tileX = x + 8 + col * (TILE_SIZE + TILE_GAP);
                    ResourceEntry entry = line.items.get(col);
                    int tileHeight = entry.tab == HubNullResourceTab.DUMP ? TILE_SIZE : TILE_SIZE + 10;
                    if (in(mouseX, mouseY, tileX, drawY, TILE_SIZE, tileHeight)) {
                        return entry;
                    }
                }
                consumed += TILE_SIZE + 12;
            } else {
                consumed += 17;
            }
        }
        return null;
    }

    private List<HubNullStationSnapshot> filteredStations() {
        String needle = searchTextForStations();
        return menu.getStationSnapshots().stream()
                .filter(station -> needle.isBlank()
                        || station.ref().name().toLowerCase().contains(needle)
                        || station.displayName().toLowerCase().contains(needle)
                        || station.ref().dimension().toString().toLowerCase().contains(needle)
                        || station.status().name().toLowerCase().contains(needle)
                        || station.nullType().name().toLowerCase().contains(needle)
                        || nullTypeLabel(station.nullType()).toLowerCase().contains(needle)
                        || stationCompactLabel(station).toLowerCase().contains(needle)
                        || coordinateText(station.ref()).contains(needle)
                        || station.ref().pos().toShortString().contains(needle))
                .toList();
    }

    private List<ResourceLine> resourceLines(int width) {
        List<ResourceEntry> entries = filteredResourceEntries();
        int columns = Math.max(1, (width - 16) / (TILE_SIZE + TILE_GAP));
        if (isSeparate()) {
            return stationGroupedLines(entries, columns);
        }
        return combinedLines(entries, columns);
    }

    private List<ResourceLine> combinedLines(List<ResourceEntry> entries, int columns) {
        if (activeTab == HubNullResourceTab.DUMP) {
            Map<DumpNullItemStatus, List<ResourceEntry>> byStatus = byDumpStatus(entries);
            List<ResourceLine> lines = new ArrayList<>();
            for (Map.Entry<DumpNullItemStatus, List<ResourceEntry>> statusEntry : byStatus.entrySet()) {
                if (statusEntry.getValue().isEmpty()) {
                    continue;
                }
                String categoryKey = key("category", activeTab.name(), dumpStatusKey(statusEntry.getKey()));
                lines.add(ResourceLine.header(LineType.CATEGORY, categoryKey, dumpStatusLabel(statusEntry.getKey()), total(statusEntry.getValue()), copyText(statusEntry.getValue())));
                if (collapsedKeys().contains(categoryKey)) {
                    continue;
                }
                appendDumpNamespaceSubgroups(lines, categoryKey, statusEntry.getKey(), statusEntry.getValue(), columns, false);
            }
            return lines;
        }
        Map<String, List<ResourceEntry>> byNamespace = byNamespace(entries);
        return namespaceCategoryLines(byNamespace, columns);
    }

    private List<ResourceLine> stationGroupedLines(List<ResourceEntry> entries, int columns) {
        Map<HubNullStationRef, List<ResourceEntry>> byStation = new LinkedHashMap<>();
        for (ResourceEntry entry : entries) {
            HubNullStationRef ref = entry.stations.isEmpty() ? new HubNullStationRef(ResourceLocation.withDefaultNamespace("overworld"), net.minecraft.core.BlockPos.ZERO, "Unknown") : entry.stations.get(0).ref;
            byStation.computeIfAbsent(ref, ignored -> new ArrayList<>()).add(entry);
        }
        List<Map.Entry<HubNullStationRef, List<ResourceEntry>>> stationEntries = new ArrayList<>(byStation.entrySet());
        stationEntries.sort((left, right) -> compareTotals(total(left.getValue()), total(right.getValue()), stationDisplayName(left.getKey()), stationDisplayName(right.getKey())));
        List<ResourceLine> lines = new ArrayList<>();
        for (Map.Entry<HubNullStationRef, List<ResourceEntry>> stationEntry : stationEntries) {
            HubNullStationRef ref = stationEntry.getKey();
            String categoryKey = key("station", activeTab.name(), ref.dimension() + "@" + ref.pos().toShortString());
            String title = stationDisplayName(ref) + " @ " + coordinateText(ref);
            lines.add(ResourceLine.header(LineType.CATEGORY, categoryKey, title, total(stationEntry.getValue()), copyText(stationEntry.getValue())));
            if (collapsedKeys().contains(categoryKey)) {
                continue;
            }
            if (activeTab == HubNullResourceTab.DUMP) {
                appendDumpStatusSubgroups(lines, categoryKey, stationEntry.getValue(), columns);
            } else {
                appendNamespaceSubgroups(lines, categoryKey, stationEntry.getValue(), columns);
            }
        }
        return lines;
    }

    private List<ResourceLine> namespaceCategoryLines(Map<String, List<ResourceEntry>> byNamespace, int columns) {
        List<Map.Entry<String, List<ResourceEntry>>> groups = new ArrayList<>(byNamespace.entrySet());
        groups.sort((left, right) -> compareTotals(total(left.getValue()), total(right.getValue()), left.getKey(), right.getKey()));
        List<ResourceLine> lines = new ArrayList<>();
        for (Map.Entry<String, List<ResourceEntry>> group : groups) {
            String categoryKey = key("namespace", activeTab.name(), group.getKey());
            lines.add(ResourceLine.header(LineType.CATEGORY, categoryKey, group.getKey(), total(group.getValue()), copyText(group.getValue())));
            if (!collapsedKeys().contains(categoryKey)) {
                appendItems(lines, categoryKey, sortEntries(group.getValue()), columns);
            }
        }
        return lines;
    }

    private void appendNamespaceSubgroups(List<ResourceLine> lines, String parentKey, List<ResourceEntry> entries, int columns) {
        List<Map.Entry<String, List<ResourceEntry>>> groups = new ArrayList<>(byNamespace(entries).entrySet());
        groups.sort((left, right) -> compareTotals(total(left.getValue()), total(right.getValue()), left.getKey(), right.getKey()));
        for (Map.Entry<String, List<ResourceEntry>> group : groups) {
            String subKey = key("sub", parentKey, group.getKey());
            lines.add(ResourceLine.header(LineType.SUBCATEGORY, subKey, group.getKey(), total(group.getValue()), copyText(group.getValue())));
            if (!collapsedKeys().contains(subKey)) {
                appendItems(lines, subKey, sortEntries(group.getValue()), columns);
            }
        }
    }

    private void appendDumpStatusSubgroups(List<ResourceLine> lines, String parentKey, List<ResourceEntry> entries, int columns) {
        for (Map.Entry<DumpNullItemStatus, List<ResourceEntry>> statusEntry : byDumpStatus(entries).entrySet()) {
            if (statusEntry.getValue().isEmpty()) {
                continue;
            }
            appendDumpNamespaceSubgroups(lines, parentKey, statusEntry.getKey(), statusEntry.getValue(), columns, true);
        }
    }

    private void appendDumpNamespaceSubgroups(List<ResourceLine> lines, String parentKey, DumpNullItemStatus status, List<ResourceEntry> entries, int columns, boolean includeStatusInTitle) {
        List<Map.Entry<String, List<ResourceEntry>>> groups = new ArrayList<>(byNamespace(entries).entrySet());
        groups.sort((left, right) -> compareTotals(total(left.getValue()), total(right.getValue()), left.getKey(), right.getKey()));
        for (Map.Entry<String, List<ResourceEntry>> group : groups) {
            String subKey = key("sub", parentKey, dumpStatusKey(status), group.getKey());
            String title = includeStatusInTitle ? dumpStatusLabel(status) + " / " + group.getKey() : group.getKey();
            lines.add(ResourceLine.header(LineType.SUBCATEGORY, subKey, title, total(group.getValue()), copyText(group.getValue())));
            if (!collapsedKeys().contains(subKey)) {
                appendItems(lines, subKey, sortEntries(group.getValue()), columns);
            }
        }
    }

    private Map<DumpNullItemStatus, List<ResourceEntry>> byDumpStatus(List<ResourceEntry> entries) {
        Map<DumpNullItemStatus, List<ResourceEntry>> byStatus = new LinkedHashMap<>();
        byStatus.put(DumpNullItemStatus.ACCEPTED, new ArrayList<>());
        byStatus.put(DumpNullItemStatus.DISCARDED, new ArrayList<>());
        for (ResourceEntry entry : entries) {
            byStatus.computeIfAbsent(normalizedDumpStatus(entry.status), ignored -> new ArrayList<>()).add(entry);
        }
        return byStatus;
    }

    private static DumpNullItemStatus normalizedDumpStatus(DumpNullItemStatus status) {
        return status == DumpNullItemStatus.ACCEPTED ? DumpNullItemStatus.ACCEPTED : DumpNullItemStatus.DISCARDED;
    }

    private static String dumpStatusKey(DumpNullItemStatus status) {
        return normalizedDumpStatus(status).name().toLowerCase();
    }

    private static String dumpStatusLabel(DumpNullItemStatus status) {
        return normalizedDumpStatus(status) == DumpNullItemStatus.ACCEPTED ? "Accepted" : "Discarded";
    }

    private void appendItems(List<ResourceLine> lines, String parentKey, List<ResourceEntry> entries, int columns) {
        for (int start = 0; start < entries.size(); start += columns) {
            lines.add(ResourceLine.items(parentKey, entries.subList(start, Math.min(entries.size(), start + columns))));
        }
    }

    private List<ResourceEntry> filteredResourceEntries() {
        String needle = searchTextForResources();
        List<ResourceEntry> entries = switch (activeTab) {
            case DEEP -> deepEntries();
            case DAMP -> dampEntries();
            case DUMP -> dumpEntries();
            case DEN -> denEntries();
        };
        if (needle.isBlank()) {
            return entries;
        }
        return entries.stream()
                .filter(entry -> entry.matches(needle))
                .toList();
    }

    private List<ResourceEntry> deepEntries() {
        List<ResourceEntry> entries = new ArrayList<>();
        for (HubNullResourceSummary summary : menu.getResourceSummaries()) {
            Item item = BuiltInRegistries.ITEM.get(summary.itemId());
            String name = item == Items.AIR ? summary.itemId().toString() : new ItemStack(item).getHoverName().getString();
            if (isSeparate()) {
                for (HubNullResourceSummary.StationAmount station : summary.stations()) {
                    entries.add(ResourceEntry.deep(summary.itemId(), name, station.count(), List.of(countDetail(station.ref(), station.count()))));
                }
            } else {
                entries.add(ResourceEntry.deep(summary.itemId(), name, summary.count(), summary.stations().stream()
                        .map(station -> countDetail(station.ref(), station.count()))
                        .toList()));
            }
        }
        return entries;
    }

    private List<ResourceEntry> dampEntries() {
        List<ResourceEntry> entries = new ArrayList<>();
        for (HubNullDampResourceSummary summary : menu.getDampResourceSummaries()) {
            if (isSeparate()) {
                for (HubNullDampResourceSummary.StationAmount station : summary.stations()) {
                    entries.add(ResourceEntry.damp(summary, station.amount(), station.capacity(), List.of(fluidDetail(station.ref(), station.amount(), station.capacity()))));
                }
            } else {
                entries.add(ResourceEntry.damp(summary, summary.amount(), summary.capacity(), summary.stations().stream()
                        .map(station -> fluidDetail(station.ref(), station.amount(), station.capacity()))
                        .toList()));
            }
        }
        return entries;
    }

    private List<ResourceEntry> dumpEntries() {
        List<ResourceEntry> entries = new ArrayList<>();
        for (HubNullDumpRuleSummary summary : menu.getDumpRuleSummaries()) {
            Item item = BuiltInRegistries.ITEM.get(summary.itemId());
            String name = item == Items.AIR ? summary.itemId().toString() : new ItemStack(item).getHoverName().getString();
            if (isSeparate()) {
                for (HubNullDumpRuleSummary.StationRules station : summary.stations()) {
                    entries.add(ResourceEntry.dump(summary.itemId(), name, summary.status(), station.advanced(), station.presetGenerated(), List.of(ruleDetail(station.ref(), station.ruleSummaries(), station.advanced(), station.presetGenerated()))));
                }
            } else {
                entries.add(ResourceEntry.dump(summary.itemId(), name, summary.status(), summary.advanced(), summary.presetGenerated(), summary.stations().stream()
                        .map(station -> ruleDetail(station.ref(), station.ruleSummaries(), station.advanced(), station.presetGenerated()))
                        .toList()));
            }
        }
        return entries;
    }

    private List<ResourceEntry> denEntries() {
        List<ResourceEntry> entries = new ArrayList<>();
        for (HubNullResourceSummary summary : menu.getDenResourceSummaries()) {
            String name = denResourceName(summary.itemId());
            if (isSeparate()) {
                for (HubNullResourceSummary.StationAmount station : summary.stations()) {
                    entries.add(ResourceEntry.den(summary.itemId(), name, station.count(), List.of(countDetail(station.ref(), station.count()))));
                }
            } else {
                entries.add(ResourceEntry.den(summary.itemId(), name, summary.count(), summary.stations().stream()
                        .map(station -> countDetail(station.ref(), station.count()))
                        .toList()));
            }
        }
        return entries;
    }

    private Map<String, List<ResourceEntry>> byNamespace(List<ResourceEntry> entries) {
        Map<String, List<ResourceEntry>> byNamespace = new LinkedHashMap<>();
        for (ResourceEntry entry : entries) {
            byNamespace.computeIfAbsent(entry.id.getNamespace(), ignored -> new ArrayList<>()).add(entry);
        }
        return byNamespace;
    }

    private List<ResourceEntry> sortEntries(List<ResourceEntry> entries) {
        List<ResourceEntry> sorted = new ArrayList<>(entries);
        sorted.sort((left, right) -> compareTotals(left.amount, right.amount, left.displayName, right.displayName));
        return sorted;
    }

    private long total(List<ResourceEntry> entries) {
        return entries.stream().mapToLong(entry -> entry.amount).sum();
    }

    private int compareTotals(long leftTotal, long rightTotal, String leftName, String rightName) {
        int byAmount = isAscending() ? Long.compare(leftTotal, rightTotal) : Long.compare(rightTotal, leftTotal);
        return byAmount != 0 ? byAmount : leftName.compareToIgnoreCase(rightName);
    }

    private void refresh() {
        PacketDistributor.sendToServer(new HubNullPayloads.RefreshPayload(menu.containerId));
    }

    private void collapseAll() {
        for (ResourceLine line : resourceLines(WIDTH - STATION_WIDTH - 24)) {
            if (line.type != LineType.ITEMS) {
                collapsedKeys().add(line.key);
            }
        }
    }

    private void expandAll() {
        collapsedKeys().clear();
    }

    private void clampScrolls() {
        stationScroll = Math.min(stationScroll, Math.max(0, filteredStations().size() - 1));
        resourceScroll = Math.min(resourceScroll, Math.max(0, resourceLines(WIDTH - STATION_WIDTH - 24).size() - 1));
    }

    private Set<String> collapsedKeys() {
        return collapsedKeys.computeIfAbsent(activeTab, ignored -> new HashSet<>());
    }

    private boolean isAscending() {
        return ascendingByTab.getOrDefault(activeTab, false);
    }

    private boolean isSeparate() {
        return separateByTab.getOrDefault(activeTab, activeTab == HubNullResourceTab.DUMP);
    }

    private boolean canReorderStations() {
        return searchTextForStations().isBlank();
    }

    private boolean draggingStation() {
        return draggedStation != null;
    }

    private void cancelStationDrag() {
        draggedStation = null;
        stationDragOffsetY = 0.0D;
        stationDragMouseY = 0.0D;
    }

    private void finishStationDrag(double mouseY) {
        List<HubNullStationSnapshot> rows = menu.getStationSnapshots().stream()
                .filter(station -> !sameStation(station, draggedStation))
                .toList();
        int targetIndex = stationTargetIndex(rows, mouseY);
        HubNullStationRef station = draggedStation;
        cancelStationDrag();
        if (station != null) {
            PacketDistributor.sendToServer(new HubNullPayloads.ReorderStationPayload(menu.containerId, station, targetIndex));
        }
    }

    private int stationTargetIndex(List<HubNullStationSnapshot> rows, double mouseY) {
        int listY = topPos + PANEL_TOP + 18;
        int row = (int) Math.floor((mouseY - listY + (ROW_HEIGHT / 2.0D)) / ROW_HEIGHT);
        return Math.max(0, Math.min(stationScroll + row, rows.size()));
    }

    private HubNullStationSnapshot stationByRef(HubNullStationRef ref) {
        if (ref == null) {
            return null;
        }
        for (HubNullStationSnapshot station : menu.getStationSnapshots()) {
            if (sameStation(station, ref)) {
                return station;
            }
        }
        return null;
    }

    private static boolean sameStation(HubNullStationSnapshot station, HubNullStationRef ref) {
        return station != null && ref != null && station.ref().sameStation(ref);
    }

    private boolean stationHighlighted(HubNullStationSnapshot station) {
        if (selectedResourceHighlight == null || station == null) {
            return false;
        }
        return selectedResourceHighlight.stations.stream().anyMatch(ref -> station.ref().sameStation(ref));
    }

    private void toggleResourceHighlight(ResourceEntry entry) {
        if (selectedResourceHighlight != null && selectedResourceHighlight.matches(entry)) {
            selectedResourceHighlight = null;
            return;
        }
        selectedResourceHighlight = ResourceHighlightKey.from(entry);
    }

    private static boolean isPrintableSearchKey(int keyCode) {
        return (keyCode >= GLFW.GLFW_KEY_SPACE && keyCode <= GLFW.GLFW_KEY_GRAVE_ACCENT)
                || (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_EQUAL);
    }

    private void updateControlLabels() {
        if (searchContextButton != null) {
            searchContextButton.setMessage(Component.literal(searchContext.label()));
            searchContextButton.setTooltip(searchContextTooltip());
        }
        if (sortButton != null) {
            sortButton.setMessage(Component.literal(isAscending() ? "Least" : "Most"));
        }
        if (separateButton != null) {
            separateButton.setMessage(Component.literal(isSeparate() ? "Split" : "Comb."));
        }
        if (deepTabButton != null) {
            deepTabButton.setMessage(Component.literal((activeTab == HubNullResourceTab.DEEP ? "*" : "") + "Deep"));
        }
        if (dampTabButton != null) {
            dampTabButton.setMessage(Component.literal((activeTab == HubNullResourceTab.DAMP ? "*" : "") + "Damp"));
        }
        if (dumpTabButton != null) {
            dumpTabButton.setMessage(Component.literal((activeTab == HubNullResourceTab.DUMP ? "*" : "") + "Dump"));
        }
        if (denTabButton != null) {
            denTabButton.setMessage(Component.literal((activeTab == HubNullResourceTab.DEN ? "*" : "") + "Den"));
        }
    }

    private void cycleSearchContext(int delta) {
        setSearchContext(searchContext.cycle(delta));
    }

    private void setSearchContext(SearchContext next) {
        if (next == null) {
            return;
        }
        searchContext = next;
        searchContextDropdownOpen = false;
        stationScroll = 0;
        resourceScroll = 0;
        if (!canReorderStations()) {
            cancelStationDrag();
        }
        updateControlLabels();
        clampScrolls();
    }

    private void cycleSort() {
        ascendingByTab.put(activeTab, !isAscending());
        updateControlLabels();
        resourceScroll = 0;
        clampScrolls();
    }

    private void cycleSeparate() {
        separateByTab.put(activeTab, !isSeparate());
        updateControlLabels();
        resourceScroll = 0;
        clampScrolls();
    }

    public static String coordinateText(HubNullStationRef ref) {
        return ref.pos().getX() + ", " + ref.pos().getY() + ", " + ref.pos().getZ();
    }

    public static int stationColor(HubNullStationRef ref) {
        int hash = 31 * ref.dimension().hashCode() + ref.pos().hashCode();
        return STATION_COLORS[Math.floorMod(hash, STATION_COLORS.length)];
    }

    public static int stationColor(HubNullStationSnapshot station) {
        return station.accentColor() == 0 ? stationColor(station.ref()) : station.accentColor();
    }

    private int stationColorFor(HubNullStationRef ref) {
        for (HubNullStationSnapshot station : menu.getStationSnapshots()) {
            if (station.ref().equals(ref)) {
                return stationColor(station);
            }
        }
        return stationColor(ref);
    }

    private String stationDisplayName(HubNullStationRef ref) {
        for (HubNullStationSnapshot station : menu.getStationSnapshots()) {
            if (station.ref().equals(ref)) {
                return stationDisplayName(station);
            }
        }
        return ref.name();
    }

    private static String stationDisplayName(HubNullStationSnapshot station) {
        return station.displayName() == null || station.displayName().isBlank() ? station.ref().name() : station.displayName();
    }

    private static String stationStatusLine(HubNullStationSnapshot station) {
        return statusText(station) + " \u00B7 " + coordinateText(station.ref());
    }

    public static String stationCompactLabel(HubNullStationSnapshot station) {
        if (station.nullType() == HubNullStationNullType.NONE) {
            return stationDisplayName(station);
        }
        if (station.nullType() == HubNullStationNullType.DUMP) {
            return "Dump";
        }
        String prefix = switch (station.nullType()) {
            case DAMP -> "Damp";
            case DEN -> "Den";
            case HEX -> "Hex";
            default -> "Deep";
        };
        DeepNullTier tier = parseTier(station.tierName());
        if (tier == null) {
            return prefix;
        }
        return prefix + " " + tierLabel(tier);
    }

    private static String nullTypeLabel(HubNullStationNullType type) {
        return switch (type) {
            case DEEP -> "DeepNull";
            case DAMP -> "DampNull";
            case DUMP -> "DumpNull";
            case DEN -> "DenNull";
            case HEX -> "HexNull";
            case NONE -> "No Null";
        };
    }

    private static DeepNullTier parseTier(String tierName) {
        if (tierName == null || tierName.isBlank()) {
            return null;
        }
        try {
            return DeepNullTier.valueOf(tierName);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String tierLabel(DeepNullTier tier) {
        return switch (tier) {
            case REDSTONE -> "I";
            case LAPIS -> "II";
            case IRON -> "III";
            case GOLD -> "IV";
            case DIAMOND -> "V";
            case EMERALD -> "VI";
            case CREATIVE -> "Creative";
        };
    }

    private StationDetail countDetail(HubNullStationRef ref, long amount) {
        return StationDetail.count(ref, stationDisplayName(ref), stationColorFor(ref), amount);
    }

    private StationDetail fluidDetail(HubNullStationRef ref, long amount, long capacity) {
        return StationDetail.fluid(ref, stationDisplayName(ref), stationColorFor(ref), amount, capacity);
    }

    private StationDetail ruleDetail(HubNullStationRef ref, List<String> details, boolean advanced, boolean presetGenerated) {
        return StationDetail.rules(ref, stationDisplayName(ref), stationColorFor(ref), details, advanced, presetGenerated);
    }

    private int count(HubNullStationStatus status) {
        int count = 0;
        for (HubNullStationSnapshot station : menu.getStationSnapshots()) {
            if (station.status() == status) {
                count++;
            }
        }
        return count;
    }

    private static int statusColor(HubNullStationStatus status) {
        return switch (status) {
            case ONLINE -> 0xFF8EEB98;
            case EMPTY -> 0xFFE4C85F;
            case UNSUPPORTED -> 0xFFFF9A68;
            case UNLOADED, MISSING -> 0xFF9BAABE;
        };
    }

    private static String statusText(HubNullStationSnapshot station) {
        String lower = station.status().name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static Component tabLabel(HubNullResourceTab tab) {
        return Component.translatable("menu.deepnullreforged.hub_null.tab." + tab.name().toLowerCase());
    }

    public static String denResourceName(ResourceLocation id) {
        return capitalizeFirst(id.getPath().replace('_', ' '));
    }

    private static String capitalizeFirst(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static Tooltip delayed(String key) {
        return Tooltip.create(Component.translatable(key));
    }

    private Tooltip searchContextTooltip() {
        return Tooltip.create(Component.translatable(searchContext.tooltipKey()));
    }

    private static boolean in(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private String searchText() {
        return searchBox == null ? "" : searchBox.getValue().trim().toLowerCase();
    }

    private String searchTextForStations() {
        return searchContext.searchesDocks() ? searchText() : "";
    }

    private String searchTextForResources() {
        return searchContext.searchesNulls() ? searchText() : "";
    }

    private String trim(String value, int maxWidth) {
        if (font.width(value) <= maxWidth) {
            return value;
        }
        String trimmed = value;
        while (!trimmed.isEmpty() && font.width(trimmed + "...") > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + "...";
    }

    private static String compact(long value) {
        if (value >= 1_000_000_000L) {
            return (value / 1_000_000_000L) + "B";
        }
        if (value >= 1_000_000L) {
            return (value / 1_000_000L) + "M";
        }
        if (value >= 1_000L) {
            return (value / 1_000L) + "K";
        }
        return Long.toString(value);
    }

    private static String amountText(ResourceEntry entry) {
        if (entry.tab == HubNullResourceTab.DAMP) {
            return compact(entry.amount) + " mB";
        }
        if (entry.tab == HubNullResourceTab.DUMP) {
            return compact(entry.amount) + " station" + (entry.amount == 1 ? "" : "s");
        }
        return compact(entry.amount);
    }

    private static String copyText(List<ResourceEntry> entries) {
        List<String> lines = new ArrayList<>();
        for (ResourceEntry entry : entries) {
            lines.add(entry.copyLine());
        }
        return String.join(System.lineSeparator(), lines);
    }

    private static void copyToClipboard(String value) {
        Minecraft.getInstance().keyboardHandler.setClipboard(value == null ? "" : value);
    }

    private static String key(String... parts) {
        return String.join("|", parts);
    }

    private static void renderScrollbar(GuiGraphics guiGraphics, int x, int y, int height, int contentRows, int visibleRows, int scroll) {
        if (contentRows <= visibleRows) {
            return;
        }
        guiGraphics.fill(x, y, x + 3, y + height, 0x882E333B);
        int handleHeight = Math.max(12, height * visibleRows / contentRows);
        int maxScroll = Math.max(1, contentRows - visibleRows);
        int handleY = y + (height - handleHeight) * Math.min(scroll, maxScroll) / maxScroll;
        guiGraphics.fill(x, handleY, x + 3, handleY + handleHeight, 0xCCB8C7D9);
    }

    private enum SearchContext {
        BOTH("Both", "menu.deepnullreforged.hub_null.search_context.both.tooltip", true, true),
        DOCKS("Docks", "menu.deepnullreforged.hub_null.search_context.docks.tooltip", true, false),
        NULLS("Nulls", "menu.deepnullreforged.hub_null.search_context.nulls.tooltip", false, true);

        private final String label;
        private final String tooltipKey;
        private final boolean searchesDocks;
        private final boolean searchesNulls;

        SearchContext(String label, String tooltipKey, boolean searchesDocks, boolean searchesNulls) {
            this.label = label;
            this.tooltipKey = tooltipKey;
            this.searchesDocks = searchesDocks;
            this.searchesNulls = searchesNulls;
        }

        private String label() {
            return label;
        }

        private String tooltipKey() {
            return tooltipKey;
        }

        private boolean searchesDocks() {
            return searchesDocks;
        }

        private boolean searchesNulls() {
            return searchesNulls;
        }

        private SearchContext cycle(int delta) {
            SearchContext[] values = values();
            return values[Math.floorMod(ordinal() + delta, values.length)];
        }
    }

    private enum LineType {
        CATEGORY,
        SUBCATEGORY,
        ITEMS
    }

    private record ResourceLine(LineType type, String key, String title, long total, List<ResourceEntry> items, String copyText) {
        private static ResourceLine header(LineType type, String key, String title, long total, String copyText) {
            return new ResourceLine(type, key, title, total, List.of(), copyText);
        }

        private static ResourceLine items(String parentKey, List<ResourceEntry> items) {
            return new ResourceLine(LineType.ITEMS, parentKey, "", 0L, List.copyOf(items), "");
        }
    }

    private record StationDetail(HubNullStationRef ref, String displayName, int color, long amount, long capacity, List<String> details, boolean advanced, boolean presetGenerated) {
        private static StationDetail count(HubNullStationRef ref, String displayName, int color, long amount) {
            return new StationDetail(ref, displayName, color, amount, 0L, List.of(), false, false);
        }

        private static StationDetail fluid(HubNullStationRef ref, String displayName, int color, long amount, long capacity) {
            return new StationDetail(ref, displayName, color, amount, capacity, List.of(), false, false);
        }

        private static StationDetail rules(HubNullStationRef ref, String displayName, int color, List<String> details, boolean advanced, boolean presetGenerated) {
            return new StationDetail(ref, displayName, color, 1L, 0L, details, advanced, presetGenerated);
        }

        private StationDetail {
            displayName = displayName == null || displayName.isBlank() ? ref.name() : displayName;
            color = color == 0 ? stationColor(ref) : color | 0xFF000000;
            details = List.copyOf(details == null ? List.of() : details);
        }
    }

    private record ResourceHighlightKey(HubNullResourceTab tab, ResourceLocation id, HubNullDampResourceKind dampKind, DumpNullItemStatus status, List<HubNullStationRef> stations) {
        private static ResourceHighlightKey from(ResourceEntry entry) {
            return new ResourceHighlightKey(
                    entry.tab,
                    entry.id,
                    entry.dampKind,
                    entry.status,
                    entry.stations.stream().map(StationDetail::ref).toList()
            );
        }

        private boolean matches(ResourceEntry entry) {
            return tab == entry.tab
                    && id.equals(entry.id)
                    && dampKind == entry.dampKind
                    && status == entry.status;
        }
    }

    private record ResourceEntry(
            HubNullResourceTab tab,
            ResourceLocation id,
            String displayName,
            long amount,
            long capacity,
            int tint,
            HubNullDampResourceKind dampKind,
            DumpNullItemStatus status,
            boolean advanced,
            boolean presetGenerated,
            List<StationDetail> stations
    ) {
        private static ResourceEntry deep(ResourceLocation id, String displayName, long amount, List<StationDetail> stations) {
            return new ResourceEntry(HubNullResourceTab.DEEP, id, displayName, amount, 0L, 0, null, DumpNullItemStatus.NEUTRAL, false, false, stations);
        }

        private static ResourceEntry damp(HubNullDampResourceSummary summary, long amount, long capacity, List<StationDetail> stations) {
            return new ResourceEntry(HubNullResourceTab.DAMP, summary.resourceId(), summary.displayName(), amount, capacity, summary.tint(), summary.kind(), DumpNullItemStatus.NEUTRAL, false, false, stations);
        }

        private static ResourceEntry dump(ResourceLocation id, String displayName, DumpNullItemStatus status, boolean advanced, boolean presetGenerated, List<StationDetail> stations) {
            return new ResourceEntry(HubNullResourceTab.DUMP, id, displayName, stations.size(), 0L, 0, null, status, advanced, presetGenerated, stations);
        }

        private static ResourceEntry den(ResourceLocation id, String displayName, long amount, List<StationDetail> stations) {
            return new ResourceEntry(HubNullResourceTab.DEN, id, displayName, amount, 0L, 0, null, DumpNullItemStatus.NEUTRAL, false, false, stations);
        }

        private ResourceEntry {
            displayName = displayName == null || displayName.isBlank() ? id.toString() : displayName;
            stations = List.copyOf(stations == null ? List.of() : stations);
        }

        private boolean matches(String needle) {
            if (displayName.toLowerCase().contains(needle) || id.toString().toLowerCase().contains(needle) || id.getNamespace().toLowerCase().contains(needle)) {
                return true;
            }
            if (tab == HubNullResourceTab.DUMP && status.name().toLowerCase().contains(needle)) {
                return true;
            }
            for (StationDetail station : stations) {
                if (station.ref.name().toLowerCase().contains(needle) || station.displayName.toLowerCase().contains(needle) || coordinateText(station.ref).contains(needle)) {
                    return true;
                }
                for (String detail : station.details) {
                    if (detail.toLowerCase().contains(needle)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private List<Component> tooltip() {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal(displayName));
            tooltip.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
            if (tab == HubNullResourceTab.DAMP) {
                tooltip.add(Component.literal((dampKind == HubNullDampResourceKind.CHEMICAL ? "Chemical" : "Fluid") + ": " + amountText(this)).withStyle(ChatFormatting.GRAY));
                if (capacity > 0L) {
                    tooltip.add(Component.literal("Capacity: " + compact(capacity) + " mB").withStyle(ChatFormatting.DARK_GRAY));
                }
            } else if (tab == HubNullResourceTab.DUMP) {
                tooltip.add(Component.literal(status == DumpNullItemStatus.ACCEPTED ? "Accepted" : "Discarded").withStyle(ChatFormatting.GRAY));
                if (advanced) {
                    tooltip.add(Component.literal("Advanced filter").withStyle(ChatFormatting.AQUA));
                }
                if (presetGenerated) {
                    tooltip.add(Component.literal("Preset-generated").withStyle(ChatFormatting.DARK_AQUA));
                }
            } else {
                tooltip.add(Component.translatable("menu.deepnullreforged.hub_null.resource_total", amount));
            }
            for (StationDetail station : stations) {
                tooltip.add(Component.literal(amountTextForStation(station) + " - " + station.displayName + " @ " + coordinateText(station.ref)).withStyle(ChatFormatting.GRAY));
                for (String detail : station.details) {
                    tooltip.add(Component.literal("  " + detail).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            return tooltip;
        }

        private String amountTextForStation(StationDetail station) {
            if (tab == HubNullResourceTab.DAMP) {
                return compact(station.amount) + " mB";
            }
            if (tab == HubNullResourceTab.DUMP) {
                return status == DumpNullItemStatus.ACCEPTED ? "Accepted" : "Discarded";
            }
            return compact(station.amount);
        }

        private String copyLine() {
            String stationText = stations.size() == 1 ? " - " + stations.get(0).displayName : "";
            if (tab == HubNullResourceTab.DUMP) {
                String details = stations.stream()
                        .flatMap(station -> station.details.stream())
                        .findFirst()
                        .orElse(advanced ? "advanced filter" : "simple");
                return (status == DumpNullItemStatus.ACCEPTED ? "Accepted" : "Discarded") + " - " + displayName + " - " + id + stationText + " - " + details;
            }
            String kind = tab == HubNullResourceTab.DAMP && dampKind == HubNullDampResourceKind.CHEMICAL ? "Chemical - " : "";
            return kind + displayName + " - " + id + " - " + amountText(this) + stationText;
        }
    }
}
