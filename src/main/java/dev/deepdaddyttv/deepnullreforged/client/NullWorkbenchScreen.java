package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullData;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullEntry;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullTemplate;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemStatus;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import dev.deepdaddyttv.deepnullreforged.inventory.StyleGlassVariant;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu;
import dev.deepdaddyttv.deepnullreforged.network.NullWorkbenchPayloads;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedEntry;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedKind;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPlan;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPresetSource;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPresetSummary;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NullWorkbenchScreen extends AbstractContainerScreen<NullWorkbenchMenu> {
    private static final ResourceLocation CRAFT_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_crafting_gui.png");
    private static final ResourceLocation SYNC_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_sync_gui.png");
    private static final ResourceLocation STYLE_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_style_gui.png");
    private static final ResourceLocation CRAFT_TAB_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_crafting_tab.png");
    private static final ResourceLocation CRAFT_TAB_ACTIVE_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_crafting_tab_active.png");
    private static final ResourceLocation SYNC_TAB_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_sync_tab.png");
    private static final ResourceLocation SYNC_TAB_ACTIVE_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_sync_tab_active.png");
    private static final ResourceLocation STYLE_TAB_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_style_tab.png");
    private static final ResourceLocation STYLE_TAB_ACTIVE_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_style_tab_active.png");
    private static final ResourceLocation CRAFT_PROGRESS_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_crafting_gui_bar_progress.png");
    private static final ResourceLocation SYNC_PROGRESS_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_sync_gui_progress.png");

    private static final int GUI_WIDTH = 252;
    private static final int GUI_HEIGHT = 246;
    private static final int TAB_U = 40;
    private static final int TAB_V = 53;
    private static final int TAB_WIDTH = 49;
    private static final int TAB_HEIGHT = 15;
    private static final int SLOT_SIZE = 32;
    private static final int LARGE_SLOT_HITBOX_X_OFFSET = 2;
    private static final int LARGE_SLOT_HITBOX_Y_OFFSET = 0;
    private static final int NULL_SLOT_X = 16;
    private static final int NULL_SLOT_Y = 56;
    private static final int SYNC_SLOT_X = 56;
    private static final int SYNC_SLOT_Y = 56;
    private static final int PREVIEW_LEFT_X = 159;
    private static final int PREVIEW_LEFT_Y = 56;
    private static final int PREVIEW_RIGHT_X = 199;
    private static final int PREVIEW_RIGHT_Y = 56;
    private static final int STYLE_NULL_SLOT_X = 36;
    private static final int STYLE_NULL_SLOT_Y = 25;
    private static final int STYLE_MODIFIER_SLOT_X = 76;
    private static final int STYLE_MODIFIER_SLOT_Y = 25;
    private static final int STYLE_OUTPUT_PREVIEW_X = 179;
    private static final int STYLE_OUTPUT_PREVIEW_Y = 25;
    private static final int CRAFT_INPUT_Y = 73;
    private static final int CRAFT_OUTPUT_X = 181;
    private static final int CRAFT_OUTPUT_Y = 73;
    private static final int CRAFT_PROGRESS_X = 61;
    private static final int CRAFT_PROGRESS_Y = 93;
    private static final int CRAFT_PROGRESS_WIDTH = 133;
    private static final int CRAFT_PROGRESS_HEIGHT = 26;
    private static final int SYNC_PROGRESS_X = 103;
    private static final int SYNC_PROGRESS_Y = 60;
    private static final int SYNC_PROGRESS_WIDTH = 46;
    private static final int SYNC_PROGRESS_HEIGHT = 23;
    private static final int STYLE_PICKER_X = 46;
    private static final int STYLE_PICKER_Y = 73;
    private static final int STYLE_PICKER_SIZE = 68;
    private static final int STYLE_HUE_X = 119;
    private static final int STYLE_HUE_Y = 73;
    private static final int STYLE_HUE_WIDTH = 10;
    private static final int STYLE_HUE_HEIGHT = 68;
    private static final int STYLE_FRAME_BOX_X = 141;
    private static final int STYLE_FRAME_BOX_Y = 76;
    private static final int STYLE_GLASS_BOX_X = 141;
    private static final int STYLE_GLASS_BOX_Y = 104;
    private static final int STYLE_BOX_WIDTH = 42;
    private static final int STYLE_BOX_HEIGHT = 12;
    private static final int STYLE_APPLY_BUTTON_X = 142;
    private static final int STYLE_APPLY_BUTTON_Y = 128;
    private static final int STYLE_APPLY_BUTTON_WIDTH = 26;
    private static final int STYLE_APPLY_BUTTON_HEIGHT = 15;
    private static final int STYLE_RESET_BUTTON_X = 177;
    private static final int STYLE_RESET_BUTTON_Y = 128;
    private static final int STYLE_RESET_BUTTON_WIDTH = 35;
    private static final int STYLE_RESET_BUTTON_HEIGHT = 15;
    private static final int STYLE_CLICK_PADDING = 2;
    private static final int SYNC_BACKUP_BUTTON_X = 34;
    private static final int SYNC_BACKUP_BUTTON_Y = 105;
    private static final int SYNC_BACKUP_BUTTON_WIDTH = 64;
    private static final int SYNC_BACKUP_BUTTON_HEIGHT = 20;
    private static final int SYNC_RESTORE_BUTTON_X = 108;
    private static final int SYNC_RESTORE_BUTTON_Y = 105;
    private static final int SYNC_RESTORE_BUTTON_WIDTH = 64;
    private static final int SYNC_RESTORE_BUTTON_HEIGHT = 20;
    private static final int SEED_SEARCH_X = 16;
    private static final int SEED_SEARCH_Y = 42;
    private static final int SEED_SEARCH_WIDTH = 218;
    private static final int SEED_SEARCH_HEIGHT = 14;
    private static final int SEED_PRESET_X = 16;
    private static final int SEED_PRESET_Y = 62;
    private static final int SEED_PRESET_WIDTH = 72;
    private static final int SEED_PRESET_HEIGHT = 70;
    private static final int SEED_PRESET_ROW_HEIGHT = 10;
    private static final int SEED_GRID_X = 96;
    private static final int SEED_GRID_Y = 84;
    private static final int SEED_GRID_WIDTH = 140;
    private static final int SEED_GRID_HEIGHT = 50;
    private static final int SEED_GRID_COLUMNS = 7;
    private static final int SEED_CELL_SIZE = 18;
    private static final int SEED_CANDIDATE_X = 184;
    private static final int SEED_CANDIDATE_Y = 137;
    private static final int SEED_CANDIDATE_WIDTH = 0;
    private static final int SEED_CANDIDATE_HEIGHT = 0;
    private static final Duration TOOLTIP_DELAY = Duration.ofMillis(250);
    private static final int TAB_START_X = 16;
    private static final int TAB_GAP = 2;
    private static final int TAB_Y_OFFSET = 13;
    private WorkbenchTab activeTab = WorkbenchTab.CRAFT;
    private StyleTarget selectedStyleTarget = StyleTarget.FRAME;
    private Button backupButton;
    private Button restoreButton;
    private Button seedApplyButton;
    private Button seedClearButton;
    private Button seedSaveButton;
    private Button seedDeleteButton;
    private EditBox frameColorBox;
    private EditBox glassColorBox;
    private EditBox seedSearchBox;
    private List<NullSeedPresetSummary> seedPresets = List.of();
    private List<NullSeedEntry> seedCandidates = List.of();
    private final List<NullSeedEntry> selectedSeedEntries = new ArrayList<>();
    private String seedPresetId = "";
    private NullSeedPresetSource seedPresetSource = NullSeedPresetSource.BUILT_IN;
    private String seedListKindKey = "";
    private boolean seedListLoaded;
    private Component seedStatus = Component.translatable("container.deepnullreforged.null_workbench.seed.pick_preset");
    private int seedPresetScroll;
    private int seedCandidateScroll;
    private int seedGridScroll;
    private int selectedSeedIndex = -1;
    private boolean draggingStylePicker;
    private boolean draggingHueStrip;

    public NullWorkbenchScreen(NullWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
        inventoryLabelX = 32;
        inventoryLabelY = 140;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 16;
        titleLabelY = 6;

        frameColorBox = new EditBox(font, leftPos + STYLE_FRAME_BOX_X + 1, topPos + STYLE_FRAME_BOX_Y + 1, STYLE_BOX_WIDTH, STYLE_BOX_HEIGHT, Component.translatable("container.deepnullreforged.null_workbench.frame_color"));
        frameColorBox.setMaxLength(7);
        frameColorBox.setFilter(value -> value.isEmpty() || value.matches("#?[0-9a-fA-F]{0,6}"));
        frameColorBox.setBordered(false);
        frameColorBox.setTextColor(0xFFFFFFFF);
        frameColorBox.setTextColorUneditable(0xFFFFFFFF);
        addRenderableWidget(frameColorBox);

        glassColorBox = new EditBox(font, leftPos + STYLE_GLASS_BOX_X + 1, topPos + STYLE_GLASS_BOX_Y + 1, STYLE_BOX_WIDTH, STYLE_BOX_HEIGHT, Component.translatable("container.deepnullreforged.null_workbench.glass_color"));
        glassColorBox.setMaxLength(7);
        glassColorBox.setFilter(value -> value.isEmpty() || value.matches("#?[0-9a-fA-F]{0,6}"));
        glassColorBox.setBordered(false);
        glassColorBox.setTextColor(0xFFFFFFFF);
        glassColorBox.setTextColorUneditable(0xFFFFFFFF);
        addRenderableWidget(glassColorBox);

        backupButton = addRenderableWidget(Button.builder(Component.translatable("container.deepnullreforged.null_workbench.backup"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, NullWorkbenchMenu.BUTTON_BACKUP);
            }
        }).bounds(leftPos + SYNC_BACKUP_BUTTON_X, topPos + SYNC_BACKUP_BUTTON_Y, SYNC_BACKUP_BUTTON_WIDTH, SYNC_BACKUP_BUTTON_HEIGHT).build());

        restoreButton = addRenderableWidget(Button.builder(Component.translatable("container.deepnullreforged.null_workbench.restore"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, NullWorkbenchMenu.BUTTON_RESTORE);
            }
        }).bounds(leftPos + SYNC_RESTORE_BUTTON_X, topPos + SYNC_RESTORE_BUTTON_Y, SYNC_RESTORE_BUTTON_WIDTH, SYNC_RESTORE_BUTTON_HEIGHT).build());

        seedSearchBox = new EditBox(font, leftPos + SEED_SEARCH_X, topPos + SEED_SEARCH_Y, SEED_SEARCH_WIDTH, SEED_SEARCH_HEIGHT, Component.translatable("container.deepnullreforged.null_workbench.seed.search"));
        seedSearchBox.setMaxLength(80);
        seedSearchBox.setBordered(true);
        seedSearchBox.setResponder(value -> {
            seedCandidateScroll = 0;
            seedPresetScroll = 0;
        });
        addRenderableWidget(seedSearchBox);

        seedApplyButton = Button.builder(Component.translatable("container.deepnullreforged.null_workbench.seed.apply"), button -> applySeedConfig())
                .bounds(leftPos + 193, topPos + 137, 43, 16)
                .build();
        seedApplyButton.setTooltip(Tooltip.create(Component.translatable("container.deepnullreforged.null_workbench.seed.apply.tooltip")));
        seedApplyButton.setTooltipDelay(TOOLTIP_DELAY);
        addRenderableWidget(seedApplyButton);

        seedClearButton = Button.builder(Component.translatable("container.deepnullreforged.null_workbench.seed.clear"), button -> clearSeedReservations())
                .bounds(leftPos + 146, topPos + 137, 43, 16)
                .build();
        seedClearButton.setTooltip(Tooltip.create(Component.translatable("container.deepnullreforged.null_workbench.seed.clear.tooltip")));
        seedClearButton.setTooltipDelay(TOOLTIP_DELAY);
        addRenderableWidget(seedClearButton);

        seedSaveButton = Button.builder(Component.translatable("container.deepnullreforged.null_workbench.seed.save_custom"), button -> saveCustomSeedPreset())
                .bounds(leftPos + 16, topPos + 137, 60, 16)
                .build();
        seedSaveButton.setTooltip(Tooltip.create(Component.translatable("container.deepnullreforged.null_workbench.seed.save_custom.tooltip")));
        seedSaveButton.setTooltipDelay(TOOLTIP_DELAY);
        addRenderableWidget(seedSaveButton);

        seedDeleteButton = Button.builder(Component.translatable("container.deepnullreforged.null_workbench.seed.delete_custom"), button -> deleteCustomSeedPreset())
                .bounds(leftPos + 80, topPos + 137, 62, 16)
                .build();
        seedDeleteButton.setTooltip(Tooltip.create(Component.translatable("container.deepnullreforged.null_workbench.seed.delete_custom.tooltip")));
        seedDeleteButton.setTooltipDelay(TOOLTIP_DELAY);
        addRenderableWidget(seedDeleteButton);

        refreshStyleFields();
        updateStyleControlPositions();
        updateWidgetVisibility();
        updateMachineSlotLayout();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        boolean styleFieldsWereVisible = frameColorBox.visible;
        updateStyleControlPositions();
        updateWidgetVisibility();
        updateMachineSlotLayout();
        if (activeTab == WorkbenchTab.SEED) {
            requestSeedPresetListIfNeeded();
        }
        if (activeTab == WorkbenchTab.STYLE && hasStyledNull() && !styleFieldsWereVisible && frameColorBox.visible) {
            refreshStyleFields();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ClientModEvents.isPrimaryGuiButton(button) && clickTab(mouseX, mouseY, tabX(0), tabY(), WorkbenchTab.CRAFT)) {
            return true;
        }
        if (ClientModEvents.isPrimaryGuiButton(button) && clickTab(mouseX, mouseY, tabX(1), tabY(), WorkbenchTab.SYNC)) {
            return true;
        }
        if (ClientModEvents.isPrimaryGuiButton(button) && clickTab(mouseX, mouseY, tabX(2), tabY(), WorkbenchTab.STYLE)) {
            return true;
        }
        if (ClientModEvents.isPrimaryGuiButton(button) && clickTab(mouseX, mouseY, tabX(3), tabY(), WorkbenchTab.SEED)) {
            return true;
        }

        if (activeTab == WorkbenchTab.SEED) {
            if (handleLargeSlotClick(mouseX, mouseY, button, leftPos + NULL_SLOT_X, topPos + 20, NullWorkbenchBlockEntity.NULL_SLOT)) {
                return true;
            }
            if (handleSeedClick(mouseX, mouseY, button)) {
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (activeTab != WorkbenchTab.CRAFT) {
            int nullSlotX = activeTab == WorkbenchTab.STYLE ? leftPos + STYLE_NULL_SLOT_X : leftPos + NULL_SLOT_X;
            int nullSlotY = activeTab == WorkbenchTab.STYLE ? topPos + STYLE_NULL_SLOT_Y : topPos + NULL_SLOT_Y;
            if (handleLargeSlotClick(mouseX, mouseY, button, nullSlotX, nullSlotY, NullWorkbenchBlockEntity.NULL_SLOT)) {
                return true;
            }
            if (activeTab == WorkbenchTab.SYNC && handleLargeSlotClick(mouseX, mouseY, button, leftPos + SYNC_SLOT_X, topPos + SYNC_SLOT_Y, NullWorkbenchBlockEntity.SYNCHRONIZER_SLOT)) {
                return true;
            }
            if (activeTab == WorkbenchTab.SYNC && handleLargeSlotClick(mouseX, mouseY, button, leftPos + PREVIEW_LEFT_X, topPos + PREVIEW_LEFT_Y, NullWorkbenchBlockEntity.SYNC_NULL_OUTPUT_SLOT)) {
                return true;
            }
            if (activeTab == WorkbenchTab.SYNC && handleLargeSlotClick(mouseX, mouseY, button, leftPos + PREVIEW_RIGHT_X, topPos + PREVIEW_RIGHT_Y, NullWorkbenchBlockEntity.SYNC_SYNCHRONIZER_OUTPUT_SLOT)) {
                return true;
            }
            if (activeTab == WorkbenchTab.STYLE && handleLargeSlotClick(mouseX, mouseY, button, leftPos + STYLE_MODIFIER_SLOT_X, topPos + STYLE_MODIFIER_SLOT_Y, NullWorkbenchBlockEntity.STYLE_MODIFIER_SLOT)) {
                return true;
            }
            if (activeTab == WorkbenchTab.STYLE && handleLargeSlotClick(mouseX, mouseY, button, leftPos + STYLE_OUTPUT_PREVIEW_X, topPos + STYLE_OUTPUT_PREVIEW_Y, NullWorkbenchBlockEntity.OUTPUT_SLOT)) {
                return true;
            }
            if (activeTab == WorkbenchTab.SYNC && ClientModEvents.isPrimaryGuiButton(button)) {
                if (insideAbsolute(mouseX, mouseY, leftPos + SYNC_BACKUP_BUTTON_X, topPos + SYNC_BACKUP_BUTTON_Y, SYNC_BACKUP_BUTTON_WIDTH, SYNC_BACKUP_BUTTON_HEIGHT)) {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, NullWorkbenchMenu.BUTTON_BACKUP);
                    }
                    return true;
                }
                if (insideAbsolute(mouseX, mouseY, leftPos + SYNC_RESTORE_BUTTON_X, topPos + SYNC_RESTORE_BUTTON_Y, SYNC_RESTORE_BUTTON_WIDTH, SYNC_RESTORE_BUTTON_HEIGHT)) {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, NullWorkbenchMenu.BUTTON_RESTORE);
                    }
                    return true;
                }
            }
            if (activeTab == WorkbenchTab.STYLE && handleStylePickerClick(mouseX, mouseY)) {
                return true;
            }
            if (activeTab == WorkbenchTab.STYLE && hasStyledNull() && ClientModEvents.isPrimaryGuiButton(button)) {
                if (insideAbsolute(mouseX, mouseY, styleApplyButtonX() - STYLE_CLICK_PADDING, styleApplyButtonY() - STYLE_CLICK_PADDING, STYLE_APPLY_BUTTON_WIDTH + (STYLE_CLICK_PADDING * 2), STYLE_APPLY_BUTTON_HEIGHT + (STYLE_CLICK_PADDING * 2))) {
                    applyStyle(false);
                    return true;
                }
                if (insideAbsolute(mouseX, mouseY, styleResetButtonX() - STYLE_CLICK_PADDING, styleResetButtonY() - STYLE_CLICK_PADDING, STYLE_RESET_BUTTON_WIDTH + (STYLE_CLICK_PADDING * 2), STYLE_RESET_BUTTON_HEIGHT + (STYLE_CLICK_PADDING * 2))) {
                    applyStyle(true);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        renderTabs(graphics);
        ResourceLocation background = switch (activeTab) {
            case CRAFT -> CRAFT_TEXTURE;
            case SYNC -> SYNC_TEXTURE;
            case STYLE -> STYLE_TEXTURE;
            case SEED -> SYNC_TEXTURE;
        };
        graphics.blit(background, leftPos, topPos, 0.0F, 0.0F, GUI_WIDTH, GUI_HEIGHT, 256, 256);

        if (activeTab == WorkbenchTab.CRAFT) {
            renderCraftProgress(graphics);
        } else if (activeTab == WorkbenchTab.SYNC) {
            renderSyncProgress(graphics);
            renderLargeSlotItem(graphics, menu.getNullStack(), leftPos + NULL_SLOT_X, topPos + NULL_SLOT_Y);
            renderLargeSlotItem(graphics, menu.getSynchronizerStack(), leftPos + SYNC_SLOT_X, topPos + SYNC_SLOT_Y);
            renderLargeSlotFrame(graphics, leftPos + PREVIEW_LEFT_X, topPos + PREVIEW_LEFT_Y);
            renderLargeSlotFrame(graphics, leftPos + PREVIEW_RIGHT_X, topPos + PREVIEW_RIGHT_Y);
            renderLargeSlotItem(graphics, menu.getSyncNullOutputStack(), leftPos + PREVIEW_LEFT_X, topPos + PREVIEW_LEFT_Y);
            renderLargeSlotItem(graphics, menu.getSyncSynchronizerOutputStack(), leftPos + PREVIEW_RIGHT_X, topPos + PREVIEW_RIGHT_Y);
            renderLargeSlotHover(graphics, mouseX, mouseY, leftPos + NULL_SLOT_X, topPos + NULL_SLOT_Y);
            renderLargeSlotHover(graphics, mouseX, mouseY, leftPos + SYNC_SLOT_X, topPos + SYNC_SLOT_Y);
            renderLargeSlotHover(graphics, mouseX, mouseY, leftPos + PREVIEW_LEFT_X, topPos + PREVIEW_LEFT_Y);
            renderLargeSlotHover(graphics, mouseX, mouseY, leftPos + PREVIEW_RIGHT_X, topPos + PREVIEW_RIGHT_Y);
        } else if (activeTab == WorkbenchTab.STYLE) {
            renderLargeSlotItem(graphics, menu.getNullStack(), leftPos + STYLE_NULL_SLOT_X, topPos + STYLE_NULL_SLOT_Y);
            renderLargeSlotItem(graphics, menu.getStyleModifierStack(), leftPos + STYLE_MODIFIER_SLOT_X, topPos + STYLE_MODIFIER_SLOT_Y);
            if (hasStyledNull()) {
                renderStyleGradient(graphics);
                renderStyleSelection(graphics);
            }
            ItemStack output = menu.getOutputStack();
            if (!output.isEmpty()) {
                renderLargeSlotItem(graphics, output, leftPos + STYLE_OUTPUT_PREVIEW_X, topPos + STYLE_OUTPUT_PREVIEW_Y);
            } else if (hasStyledNull()) {
                ItemStack preview = previewStack();
                renderLargeSlotItem(graphics, preview, leftPos + STYLE_OUTPUT_PREVIEW_X, topPos + STYLE_OUTPUT_PREVIEW_Y);
            }
            renderLargeSlotHover(graphics, mouseX, mouseY, leftPos + STYLE_NULL_SLOT_X, topPos + STYLE_NULL_SLOT_Y);
            renderLargeSlotHover(graphics, mouseX, mouseY, leftPos + STYLE_MODIFIER_SLOT_X, topPos + STYLE_MODIFIER_SLOT_Y);
            renderLargeSlotHover(graphics, mouseX, mouseY, leftPos + STYLE_OUTPUT_PREVIEW_X, topPos + STYLE_OUTPUT_PREVIEW_Y);
        } else if (activeTab == WorkbenchTab.SEED) {
            renderLargeSlotItem(graphics, menu.getNullStack(), leftPos + NULL_SLOT_X, topPos + 20);
            renderLargeSlotHover(graphics, mouseX, mouseY, leftPos + NULL_SLOT_X, topPos + 20);
            renderSeed(graphics, mouseX, mouseY);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateMachineSlotLayout();
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        if (slot.index == NullWorkbenchBlockEntity.NULL_SLOT
                || slot.index == NullWorkbenchBlockEntity.SYNCHRONIZER_SLOT
                || slot.index == NullWorkbenchBlockEntity.SYNC_NULL_OUTPUT_SLOT
                || slot.index == NullWorkbenchBlockEntity.SYNC_SYNCHRONIZER_OUTPUT_SLOT
                || slot.index == NullWorkbenchBlockEntity.STYLE_MODIFIER_SLOT) {
            return;
        }
        if (slot.index >= NullWorkbenchBlockEntity.INPUT_SLOT_START && slot.index < NullWorkbenchBlockEntity.OUTPUT_SLOT && activeTab != WorkbenchTab.CRAFT) {
            return;
        }
        if (slot.index == NullWorkbenchBlockEntity.OUTPUT_SLOT && activeTab != WorkbenchTab.CRAFT) {
            return;
        }
        super.renderSlot(graphics, slot);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFFF, false);
    }

    private void renderTabs(GuiGraphics graphics) {
        drawTab(graphics, CRAFT_TAB_TEXTURE, CRAFT_TAB_ACTIVE_TEXTURE, tabX(0), tabY(), activeTab == WorkbenchTab.CRAFT);
        drawTab(graphics, SYNC_TAB_TEXTURE, SYNC_TAB_ACTIVE_TEXTURE, tabX(1), tabY(), activeTab == WorkbenchTab.SYNC);
        drawTab(graphics, STYLE_TAB_TEXTURE, STYLE_TAB_ACTIVE_TEXTURE, tabX(2), tabY(), activeTab == WorkbenchTab.STYLE);
        drawTab(graphics, STYLE_TAB_TEXTURE, STYLE_TAB_ACTIVE_TEXTURE, tabX(3), tabY(), activeTab == WorkbenchTab.SEED);
        graphics.drawString(font, "S", tabX(3) + 20, tabY() + 4, menu.canSeedNull() ? 0xFFFFFFFF : 0xFF777777, false);
    }

    private void drawTab(GuiGraphics graphics, ResourceLocation inactiveTexture, ResourceLocation activeTexture, int x, int y, boolean active) {
        graphics.blit(active ? activeTexture : inactiveTexture, x, y, TAB_U, TAB_V, TAB_WIDTH, TAB_HEIGHT, 256, 256);
    }

    private void renderCraftProgress(GuiGraphics graphics) {
        int filled = (int) (CRAFT_PROGRESS_WIDTH * (menu.getCraftProgress() / (float) menu.getCraftDuration()));
        if (filled <= 0) {
            return;
        }
        graphics.blit(CRAFT_PROGRESS_TEXTURE, leftPos + CRAFT_PROGRESS_X, topPos + CRAFT_PROGRESS_Y, CRAFT_PROGRESS_X, CRAFT_PROGRESS_Y, filled, CRAFT_PROGRESS_HEIGHT, 256, 256);
    }

    private void renderSyncProgress(GuiGraphics graphics) {
        int filled = (int) (SYNC_PROGRESS_WIDTH * (menu.getSyncProgress() / (float) menu.getSyncDuration()));
        if (filled <= 0) {
            return;
        }
        graphics.blit(SYNC_PROGRESS_TEXTURE, leftPos + SYNC_PROGRESS_X, topPos + SYNC_PROGRESS_Y, SYNC_PROGRESS_X, SYNC_PROGRESS_Y, filled, SYNC_PROGRESS_HEIGHT, 256, 256);
    }

    private void renderLargeSlotItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        GhostSlotRenderer.renderContainedItem(graphics, stack, x + 1, y + 1, SLOT_SIZE - 2);
    }

    private void renderStyleGradient(GuiGraphics graphics) {
        float[] hsv = getActiveHsv();
        int hueColor = hsvToRgb(hsv[0], 1.0F, 1.0F) | 0xFF000000;
        int left = leftPos + STYLE_PICKER_X;
        int top = topPos + STYLE_PICKER_Y;
        int right = left + STYLE_PICKER_SIZE;
        int bottom = top + STYLE_PICKER_SIZE;

        for (int dx = 0; dx < STYLE_PICKER_SIZE; dx++) {
            float sat = dx / (float) (STYLE_PICKER_SIZE - 1);
            int baseColor = blendRgb(0xFFFFFF, hueColor & 0xFFFFFF, sat) | 0xFF000000;
            graphics.fill(left + dx, top, left + dx + 1, bottom, baseColor);
        }
        for (int dy = 0; dy < STYLE_PICKER_SIZE; dy++) {
            float dark = dy / (float) (STYLE_PICKER_SIZE - 1);
            int alpha = Math.round(dark * 255.0F) << 24;
            graphics.fill(left, top + dy, right, top + dy + 1, alpha);
        }
    }

    private void renderStyleSelection(GuiGraphics graphics) {
        float[] hsv = getActiveHsv();
        int pickerX = leftPos + STYLE_PICKER_X + Math.round(hsv[1] * (STYLE_PICKER_SIZE - 1));
        int pickerY = topPos + STYLE_PICKER_Y + Math.round((1.0F - hsv[2]) * (STYLE_PICKER_SIZE - 1));
        graphics.renderOutline(pickerX - 2, pickerY - 2, 5, 5, 0xFFFFFFFF);

        int hueY = topPos + STYLE_HUE_Y + Math.round((hsv[0] / 360.0F) * (STYLE_HUE_HEIGHT - 1));
        graphics.fill(leftPos + STYLE_HUE_X - 1, hueY, leftPos + STYLE_HUE_X + STYLE_HUE_WIDTH + 1, hueY + 2, 0xFFFFFFFF);
    }

    private void renderLargeSlotHover(GuiGraphics graphics, int mouseX, int mouseY, int x, int y) {
        int hoverX = x + LARGE_SLOT_HITBOX_X_OFFSET;
        int hoverY = y + LARGE_SLOT_HITBOX_Y_OFFSET;
        if (insideAbsolute(mouseX, mouseY, hoverX, hoverY, SLOT_SIZE, SLOT_SIZE)) {
            graphics.fill(hoverX + 1, hoverY + 1, hoverX + SLOT_SIZE - 1, hoverY + SLOT_SIZE - 1, 0x22FFFFFF);
            graphics.renderOutline(hoverX, hoverY, SLOT_SIZE, SLOT_SIZE, 0xFFFFFFFF);
        }
    }

    private void renderLargeSlotFrame(GuiGraphics graphics, int x, int y) {
        int frameX = x + LARGE_SLOT_HITBOX_X_OFFSET;
        int frameY = y + LARGE_SLOT_HITBOX_Y_OFFSET;
        graphics.renderOutline(frameX, frameY, SLOT_SIZE, SLOT_SIZE, 0xFF8C8C8C);
    }

    private boolean handleLargeSlotClick(double mouseX, double mouseY, int button, int x, int y, int slotIndex) {
        int hitboxX = x + LARGE_SLOT_HITBOX_X_OFFSET;
        int hitboxY = y + LARGE_SLOT_HITBOX_Y_OFFSET;
        if (mouseX < hitboxX || mouseX >= hitboxX + SLOT_SIZE || mouseY < hitboxY || mouseY >= hitboxY + SLOT_SIZE) {
            return false;
        }
        if (slotIndex < 0 || slotIndex >= menu.slots.size()) {
            return false;
        }
        Slot slot = menu.slots.get(slotIndex);
        ClickType clickType = hasShiftDown() && button == 0 ? ClickType.QUICK_MOVE : ClickType.PICKUP;
        slotClicked(slot, slot.index, button, clickType);
        return true;
    }

    public void acceptSeedPreset(NullWorkbenchPayloads.SeedPresetPayload payload) {
        seedPresetId = payload.preset().presetId();
        seedPresetSource = payload.preset().source();
        seedCandidates = compatibleEntries(payload.preset().entries());
        selectedSeedEntries.clear();
        for (NullSeedEntry candidate : seedCandidates) {
            placeSeedCandidate(candidate, false);
        }
        selectedSeedIndex = selectedSeedEntries.isEmpty() ? -1 : selectedSeedEntries.get(selectedSeedEntries.size() - 1).targetIndex();
        seedCandidateScroll = 0;
        seedGridScroll = 0;
        seedStatus = seedCandidates.isEmpty()
                ? Component.translatable("container.deepnullreforged.null_workbench.seed.empty_preset")
                : Component.translatable("container.deepnullreforged.null_workbench.seed.loaded_preset", seedCandidates.size());
        updateWidgetVisibility();
    }

    public void acceptSeedPresetList(NullWorkbenchPayloads.SeedPresetListPayload payload) {
        seedPresets = payload.presets();
        seedListLoaded = true;
        seedPresetScroll = 0;
        seedStatus = seedPresets.isEmpty()
                ? Component.translatable("container.deepnullreforged.null_workbench.seed.no_presets")
                : Component.translatable("container.deepnullreforged.null_workbench.seed.pick_preset");
        updateWidgetVisibility();
    }

    public void acceptSeedApplyResult(NullWorkbenchPayloads.SeedApplyResultPayload payload) {
        seedStatus = Component.translatable(
                "container.deepnullreforged.null_workbench.seed.apply_result",
                payload.applied(),
                payload.selected(),
                payload.overflow(),
                payload.blocked()
        );
        updateWidgetVisibility();
    }

    private void requestSeedPreset(String presetId) {
        if (!menu.canSeedNull()) {
            seedStatus = Component.translatable("container.deepnullreforged.null_workbench.seed.requires_deepnull");
            return;
        }
        PacketDistributor.sendToServer(new NullWorkbenchPayloads.RequestSeedPresetPayload(menu.getBlockPos(), presetId));
        seedStatus = Component.translatable("container.deepnullreforged.null_workbench.seed.loading");
    }

    private void requestSeedPresetListIfNeeded() {
        String key = menu.seedKind() == null ? "" : menu.seedKind().name() + ":" + menu.getNullStack().getItem().toString();
        if (!menu.canSeedNull()) {
            if (!seedListKindKey.isEmpty()) {
                seedPresets = List.of();
                seedCandidates = List.of();
                selectedSeedEntries.clear();
                seedPresetId = "";
                seedListKindKey = "";
                seedListLoaded = false;
            }
            return;
        }
        if (key.equals(seedListKindKey) && seedListLoaded) {
            return;
        }
        seedListKindKey = key;
        seedListLoaded = false;
        seedPresets = List.of();
        seedPresetScroll = 0;
        PacketDistributor.sendToServer(new NullWorkbenchPayloads.RequestSeedPresetListPayload(menu.getBlockPos()));
        seedStatus = Component.translatable("container.deepnullreforged.null_workbench.seed.loading");
    }

    private void saveCustomSeedPreset() {
        if (!menu.canSeedNull() || selectedSeedEntries.isEmpty()) {
            return;
        }
        String name = seedSearchBox == null || seedSearchBox.getValue().trim().isEmpty()
                ? Component.translatable("container.deepnullreforged.null_workbench.seed.custom_default").getString()
                : seedSearchBox.getValue().trim();
        String id = seedPresetSource == NullSeedPresetSource.USER ? seedPresetId : "";
        PacketDistributor.sendToServer(new NullWorkbenchPayloads.SaveSeedPresetPayload(menu.getBlockPos(), id, name, selectedSeedEntries));
        seedListLoaded = false;
        seedStatus = Component.translatable("container.deepnullreforged.null_workbench.seed.saving_custom");
    }

    private void deleteCustomSeedPreset() {
        if (!menu.canSeedNull() || seedPresetSource != NullSeedPresetSource.USER || seedPresetId.isBlank()) {
            return;
        }
        PacketDistributor.sendToServer(new NullWorkbenchPayloads.DeleteSeedPresetPayload(menu.getBlockPos(), seedPresetId));
        seedListLoaded = false;
        seedPresetId = "";
        seedPresetSource = NullSeedPresetSource.BUILT_IN;
        seedStatus = Component.translatable("container.deepnullreforged.null_workbench.seed.deleted_custom");
    }

    private void applySeedConfig() {
        if (!menu.canSeedNull() || selectedSeedEntries.isEmpty()) {
            return;
        }
        PacketDistributor.sendToServer(new NullWorkbenchPayloads.ApplySeedConfigPayload(
                menu.getBlockPos(),
                selectedSeedEntries,
                true
        ));
    }

    private void clearSeedReservations() {
        if (!menu.canSeedNull()) {
            return;
        }
        selectedSeedEntries.clear();
        selectedSeedIndex = -1;
        PacketDistributor.sendToServer(new NullWorkbenchPayloads.ClearSeedReservationsPayload(menu.getBlockPos()));
        seedStatus = Component.translatable("container.deepnullreforged.null_workbench.seed.cleared");
        updateWidgetVisibility();
    }

    private void moveSelectedSeed(int delta) {
        int entryIndex = selectedEntryIndexForTarget(selectedSeedIndex);
        if (entryIndex < 0) {
            return;
        }
        NullSeedEntry entry = selectedSeedEntries.get(entryIndex);
        int target = entry.targetIndex() + delta;
        if (target < 0 || target >= seedCapacity()) {
            return;
        }
        selectedSeedEntries.set(entryIndex, entry.withTargetIndex(target));
        selectedSeedIndex = target;
        updateWidgetVisibility();
    }

    private void renderSeed(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("container.deepnullreforged.null_workbench.seed"), leftPos + 54, topPos + 22, 0xFFFFFFFF, false);
        if (!menu.canSeedNull()) {
            graphics.drawString(font, Component.translatable("container.deepnullreforged.null_workbench.seed.requires_deepnull"), leftPos + 54, topPos + 34, 0xFFFF7777, false);
            return;
        }

        DeepNullInventory inventory = menu.createNullInventory();
        NullSeedPlan plan = menu.planSeedEntries(selectedSeedEntries);
        int occupied = occupiedSlots(inventory);
        int reserved = reservedTemplateCount(inventory);
        graphics.drawString(font, trim(Component.translatable("container.deepnullreforged.null_workbench.seed.summary", seedCapacity(), occupied, reserved, plan.appliedCount(), plan.selectedCount()), 172), leftPos + 54, topPos + 34, 0xFFCEDAF2, false);

        drawPanel(graphics, leftPos + SEED_PRESET_X - 2, topPos + SEED_PRESET_Y - 2, SEED_PRESET_WIDTH + 4, SEED_PRESET_HEIGHT + 4);
        drawPanel(graphics, leftPos + SEED_GRID_X - 2, topPos + SEED_GRID_Y - 2, SEED_GRID_WIDTH + 4, SEED_GRID_HEIGHT + 4);
        graphics.drawString(font, Component.translatable("container.deepnullreforged.null_workbench.seed.presets"), leftPos + SEED_PRESET_X, topPos + SEED_PRESET_Y - 13, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.translatable(gridLabelKey()), leftPos + SEED_GRID_X, topPos + SEED_GRID_Y - 13, 0xFFFFFFFF, false);

        renderSeedPresetList(graphics, mouseX, mouseY);
        renderSeedGrid(graphics, inventory, plan, mouseX, mouseY);
        graphics.drawString(font, trim(seedStatus, 210), leftPos + 16, topPos + 132, 0xFFCEDAF2, false);
    }

    private boolean handleSeedClick(double mouseX, double mouseY, int button) {
        if (!ClientModEvents.isPrimaryGuiButton(button) && !ClientModEvents.isSecondaryGuiButton(button)) {
            return false;
        }
        int presetIndex = seedPresetIndexAt(mouseX, mouseY);
        if (presetIndex >= 0) {
            List<NullSeedPresetSummary> presets = filteredSeedPresets();
            int index = presetIndex + seedPresetScroll;
            if (index >= 0 && index < presets.size()) {
                requestSeedPreset(presets.get(index).presetId());
                return true;
            }
        }
        if (addCarriedSeedEntry(mouseX, mouseY, button)) {
            return true;
        }
        int candidateIndex = seedCandidateIndexAt(mouseX, mouseY);
        if (candidateIndex >= 0) {
            List<NullSeedEntry> filtered = filteredSeedCandidates();
            int index = candidateIndex + seedCandidateScroll;
            if (index >= 0 && index < filtered.size()) {
                placeSeedCandidate(filtered.get(index), ClientModEvents.isSecondaryGuiButton(button));
                return true;
            }
        }
        int gridIndex = seedGridIndexAt(mouseX, mouseY);
        if (gridIndex >= 0) {
            if (menu.seedKind() == NullSeedKind.DUMP_RULE) {
                if (cycleDumpSeedEntry(gridIndex, ClientModEvents.isSecondaryGuiButton(button))) {
                    updateWidgetVisibility();
                    return true;
                }
            } else {
                int entryIndex = selectedEntryIndexForTarget(gridIndex);
                if (entryIndex >= 0 && ClientModEvents.isSecondaryGuiButton(button)) {
                    selectedSeedEntries.remove(entryIndex);
                    selectedSeedIndex = -1;
                } else {
                    selectedSeedIndex = gridIndex;
                }
                updateWidgetVisibility();
                return true;
            }
        }
        return false;
    }

    private void renderSeedPresetList(GuiGraphics graphics, int mouseX, int mouseY) {
        List<NullSeedPresetSummary> filtered = filteredSeedPresets();
        int visible = Math.max(1, SEED_PRESET_HEIGHT / SEED_PRESET_ROW_HEIGHT);
        seedPresetScroll = clamp(seedPresetScroll, 0, Math.max(0, filtered.size() - visible));
        for (int row = 0; row < visible && row + seedPresetScroll < filtered.size(); row++) {
            NullSeedPresetSummary preset = filtered.get(row + seedPresetScroll);
            int x = leftPos + SEED_PRESET_X;
            int y = topPos + SEED_PRESET_Y + row * SEED_PRESET_ROW_HEIGHT;
            boolean selected = preset.presetId().equals(seedPresetId);
            graphics.fill(x, y, x + SEED_PRESET_WIDTH, y + SEED_PRESET_ROW_HEIGHT - 1, selected ? 0x99436AA8 : 0x66000000);
            int sourceColor = switch (preset.source()) {
                case BUILT_IN -> 0xFF88CCFF;
                case MOD -> 0xFF8EEB98;
                case USER -> 0xFFFFC857;
            };
            graphics.fill(x, y, x + 2, y + SEED_PRESET_ROW_HEIGHT - 1, sourceColor);
            graphics.drawString(font, font.plainSubstrByWidth(preset.displayName(), SEED_PRESET_WIDTH - 6), x + 4, y + 1, 0xFFE8EDF5, false);
            if (insideAbsolute(mouseX, mouseY, x, y, SEED_PRESET_WIDTH, SEED_PRESET_ROW_HEIGHT)) {
                graphics.renderTooltip(font, List.of(
                        Component.literal(preset.displayName()),
                        Component.literal(preset.source().label() + (preset.namespace().isBlank() ? "" : " - " + preset.namespace())),
                        Component.translatable("container.deepnullreforged.null_workbench.seed.entry_count", preset.entryCount())
                ), java.util.Optional.empty(), mouseX, mouseY);
            }
        }
        renderTinyScrollHint(graphics, leftPos + SEED_PRESET_X + SEED_PRESET_WIDTH - 4, topPos + SEED_PRESET_Y, SEED_PRESET_HEIGHT, Math.max(1, filtered.size()), visible, seedPresetScroll);
    }

    private List<NullSeedEntry> filteredSeedCandidates() {
        return seedCandidates;
    }

    private List<NullSeedPresetSummary> filteredSeedPresets() {
        String query = seedSearchBox == null ? "" : seedSearchBox.getValue().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return seedPresets;
        }
        return seedPresets.stream()
                .filter(preset -> preset.presetId().toLowerCase(Locale.ROOT).contains(query)
                        || preset.displayName().toLowerCase(Locale.ROOT).contains(query)
                        || preset.namespace().toLowerCase(Locale.ROOT).contains(query)
                        || preset.source().label().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    private String statusLabel(NullSeedPlan plan, ResourceLocation itemId, int index) {
        if (index >= plan.entries().size() || !plan.entries().get(index).itemId().equals(itemId)) {
            return "";
        }
        return switch (plan.entries().get(index).status()) {
            case ALREADY_PRESENT -> Component.translatable("container.deepnullreforged.null_workbench.seed.status.present").getString();
            case WILL_RESERVE -> Component.translatable("container.deepnullreforged.null_workbench.seed.status.reserve").getString();
            case WILL_FILL -> Component.translatable("container.deepnullreforged.null_workbench.seed.status.fill").getString();
            case WILL_RULE -> Component.translatable("container.deepnullreforged.null_workbench.seed.status.rule").getString();
            case BLOCKED_OCCUPIED -> Component.translatable("container.deepnullreforged.null_workbench.seed.status.blocked").getString();
            case OVERFLOW -> Component.translatable("container.deepnullreforged.null_workbench.seed.status.overflow").getString();
            case INVALID -> Component.translatable("container.deepnullreforged.null_workbench.seed.status.invalid").getString();
        };
    }

    private void renderSeedGrid(GuiGraphics graphics, DeepNullInventory inventory, NullSeedPlan plan, int mouseX, int mouseY) {
        if (menu.seedKind() == NullSeedKind.DUMP_RULE) {
            renderDumpSeedGrid(graphics, mouseX, mouseY);
            return;
        }
        int capacity = seedCapacity();
        int visibleRows = Math.max(1, SEED_GRID_HEIGHT / SEED_CELL_SIZE);
        int totalRows = Math.max(1, (capacity + SEED_GRID_COLUMNS - 1) / SEED_GRID_COLUMNS);
        seedGridScroll = clamp(seedGridScroll, 0, Math.max(0, totalRows - visibleRows));
        for (int row = 0; row < visibleRows; row++) {
            for (int column = 0; column < SEED_GRID_COLUMNS; column++) {
                int index = (seedGridScroll + row) * SEED_GRID_COLUMNS + column;
                if (index >= capacity) {
                    continue;
                }
                int x = leftPos + SEED_GRID_X + column * SEED_CELL_SIZE;
                int y = topPos + SEED_GRID_Y + row * SEED_CELL_SIZE;
                NullSeedEntry planned = selectedEntryAtTarget(index);
                NullSeedPlan.Status status = planned == null ? null : plannedStatus(plan, planned, index);
                int outline = index == selectedSeedIndex ? 0xFFFFFFFF : statusColor(status);
                graphics.fill(x, y, x + 17, y + 17, 0x66000000);
                graphics.renderOutline(x, y, 17, 17, outline);
                if (menu.seedKind() == NullSeedKind.FLUID) {
                    renderTankSeedCell(graphics, inventory, planned, status, x, y, index);
                } else if (menu.seedKind() == NullSeedKind.ENTITY) {
                    renderEntitySeedCell(graphics, planned, status, x, y, index);
                } else {
                    renderItemSeedCell(graphics, inventory, planned, status, x, y, index);
                }
                if (insideAbsolute(mouseX, mouseY, x, y, 17, 17)) {
                    graphics.fill(x + 1, y + 1, x + 16, y + 16, 0x33FFFFFF);
                }
            }
        }
        renderTinyScrollHint(graphics, leftPos + SEED_GRID_X + SEED_GRID_WIDTH - 4, topPos + SEED_GRID_Y, SEED_GRID_HEIGHT, totalRows, visibleRows, seedGridScroll);
    }

    private void renderItemSeedCell(GuiGraphics graphics, DeepNullInventory inventory, NullSeedEntry planned, NullSeedPlan.Status status, int x, int y, int slot) {
        ItemStack stored = inventory == null ? ItemStack.EMPTY : inventory.getStackInSlot(slot);
        ItemStack reserved = inventory == null ? ItemStack.EMPTY : inventory.getReservedStack(slot);
        if (!stored.isEmpty()) {
            GhostSlotRenderer.renderContainedItem(graphics, stored, x + 2, y + 2, 14);
            return;
        }
        if (planned != null) {
            ItemStack plannedStack = itemStack(planned.id());
            if (!plannedStack.isEmpty()) {
                GhostSlotRenderer.renderGhostItem(graphics, plannedStack, x + 2, y + 2, 14,
                        status == NullSeedPlan.Status.BLOCKED_OCCUPIED ? GhostSlotRenderer.BLOCKED_ALPHA : GhostSlotRenderer.PLANNED_ALPHA);
            }
        } else if (!reserved.isEmpty()) {
            GhostSlotRenderer.renderGhostItem(graphics, reserved, x + 2, y + 2, 14);
        }
        if (status == NullSeedPlan.Status.BLOCKED_OCCUPIED) {
            graphics.fill(x + 1, y + 1, x + 16, y + 16, 0x55FF3333);
        }
    }

    private void renderTankSeedCell(GuiGraphics graphics, DeepNullInventory inventory, NullSeedEntry planned, NullSeedPlan.Status status, int x, int y, int tank) {
        FluidStack stored = inventory == null || tank >= inventory.getFluidSlotCount() ? FluidStack.EMPTY : inventory.getFluidInSlot(tank);
        StoredChemical storedChemical = inventory == null || tank >= inventory.getFluidSlotCount() ? StoredChemical.EMPTY : inventory.getChemicalInSlot(tank);
        if (!stored.isEmpty()) {
            renderFluidTexture(graphics, stored, x + 2, y + 2, 14, 14);
            return;
        }
        if (!storedChemical.isEmpty()) {
            renderChemicalSwatch(graphics, storedChemical, x + 2, y + 2, 14, 14);
            return;
        }
        if (planned != null) {
            float alpha = status == NullSeedPlan.Status.BLOCKED_OCCUPIED ? GhostSlotRenderer.BLOCKED_ALPHA : GhostSlotRenderer.PLANNED_ALPHA;
            if (planned.kind() == NullSeedKind.CHEMICAL) {
                renderChemicalSwatch(graphics, plannedChemical(planned), x + 2, y + 2, 14, 14, alpha);
            } else {
                Fluid fluid = BuiltInRegistries.FLUID.get(planned.id());
                if (fluid != Fluids.EMPTY) {
                    renderFluidTexture(graphics, new FluidStack(fluid, Math.max(1, planned.amount())), x + 2, y + 2, 14, 14, alpha);
                }
            }
        } else if (inventory != null) {
            FluidStack reservedFluid = inventory.getReservedFluidTemplate(tank);
            StoredChemical reservedChemical = inventory.getReservedChemicalTemplate(tank);
            if (!reservedFluid.isEmpty()) {
                renderFluidTexture(graphics, reservedFluid, x + 2, y + 2, 14, 14, GhostSlotRenderer.GHOST_ALPHA);
            } else if (!reservedChemical.isEmpty()) {
                renderChemicalSwatch(graphics, reservedChemical, x + 2, y + 2, 14, 14, GhostSlotRenderer.GHOST_ALPHA);
            }
        }
        if (status == NullSeedPlan.Status.BLOCKED_OCCUPIED) {
            graphics.fill(x + 1, y + 1, x + 16, y + 16, 0x55FF3333);
        }
    }

    private void renderEntitySeedCell(GuiGraphics graphics, NullSeedEntry planned, NullSeedPlan.Status status, int x, int y, int slot) {
        DenNullData data = DenNullData.get(menu.getNullStack());
        if (slot < data.entries().size()) {
            drawEntityTile(graphics, data.entries().get(slot).entityType(), x + 1, y + 1, 0xFFB8C7FF);
        } else if (planned != null) {
            drawEntityTile(graphics, planned.id(), x + 1, y + 1, status == NullSeedPlan.Status.BLOCKED_OCCUPIED ? 0xFFFF7777 : 0xFF88CCFF);
        } else {
            DenNullTemplate template = data.templateAt(slot);
            if (template != null) {
                drawEntityTile(graphics, template.entityType(), x + 1, y + 1, 0x9988CCFF);
            }
        }
        if (status == NullSeedPlan.Status.BLOCKED_OCCUPIED) {
            graphics.fill(x + 1, y + 1, x + 16, y + 16, 0x55FF3333);
        }
    }

    private void drawEntityTile(GuiGraphics graphics, ResourceLocation entityId, int x, int y, int color) {
        graphics.fill(x, y, x + 15, y + 15, 0xAA182032);
        graphics.renderOutline(x, y, 15, 15, color);
        String text = entityId == null || entityId.getPath().isBlank() ? "?" : entityId.getPath().substring(0, 1).toUpperCase(Locale.ROOT);
        graphics.drawString(font, text, x + 5, y + 4, color, false);
    }

    private void renderDumpSeedGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("container.deepnullreforged.null_workbench.seed.accepted"), leftPos + SEED_GRID_X, topPos + SEED_GRID_Y - 1, 0xFF8EEB98, false);
        graphics.drawString(font, Component.translatable("container.deepnullreforged.null_workbench.seed.discarded"), leftPos + SEED_GRID_X, topPos + SEED_GRID_Y + 24, 0xFFFF7777, false);
        renderDumpRuleRow(graphics, mouseX, mouseY, DumpNullItemStatus.ACCEPTED, topPos + SEED_GRID_Y + 9, 0xFF65D982);
        renderDumpRuleRow(graphics, mouseX, mouseY, DumpNullItemStatus.DISCARDED, topPos + SEED_GRID_Y + 34, 0xFFFF5555);
    }

    private void renderDumpRuleRow(GuiGraphics graphics, int mouseX, int mouseY, DumpNullItemStatus status, int y, int outline) {
        List<NullSeedEntry> entries = selectedSeedEntries.stream()
                .filter(entry -> entry.kind() == NullSeedKind.DUMP_RULE && entry.dumpStatus() == status)
                .limit(SEED_GRID_COLUMNS)
                .toList();
        for (int column = 0; column < SEED_GRID_COLUMNS; column++) {
            int x = leftPos + SEED_GRID_X + column * SEED_CELL_SIZE;
            graphics.fill(x, y, x + 17, y + 17, 0x66000000);
            graphics.renderOutline(x, y, 17, 17, outline);
            if (column < entries.size()) {
                ItemStack stack = itemStack(entries.get(column).id());
                if (!stack.isEmpty()) {
                    GhostSlotRenderer.renderGhostItem(graphics, stack, x + 2, y + 2, 14, GhostSlotRenderer.PLANNED_ALPHA);
                }
            }
            if (insideAbsolute(mouseX, mouseY, x, y, 17, 17)) {
                graphics.fill(x + 1, y + 1, x + 16, y + 16, 0x33FFFFFF);
            }
        }
    }

    private void renderSeedCandidates(GuiGraphics graphics, int mouseX, int mouseY) {
        List<NullSeedEntry> filtered = filteredSeedCandidates();
        int columns = Math.max(1, SEED_CANDIDATE_WIDTH / SEED_CELL_SIZE);
        int rows = Math.max(1, SEED_CANDIDATE_HEIGHT / SEED_CELL_SIZE);
        int visible = columns * rows;
        seedCandidateScroll = clamp(seedCandidateScroll, 0, Math.max(0, filtered.size() - visible));
        for (int index = 0; index < visible && index + seedCandidateScroll < filtered.size(); index++) {
            NullSeedEntry entry = filtered.get(index + seedCandidateScroll);
            int column = index % columns;
            int row = index / columns;
            int x = leftPos + SEED_CANDIDATE_X + column * SEED_CELL_SIZE;
            int y = topPos + SEED_CANDIDATE_Y + row * SEED_CELL_SIZE;
            graphics.fill(x, y, x + 17, y + 17, isSelected(entry) ? 0x663F6AA8 : 0x66000000);
            graphics.renderOutline(x, y, 17, 17, isSelected(entry) ? 0xFF88CCFF : 0xFF3A3A44);
            if (entry.kind() == NullSeedKind.FLUID) {
                Fluid fluid = BuiltInRegistries.FLUID.get(entry.id());
                if (fluid != Fluids.EMPTY) {
                    renderFluidTexture(graphics, new FluidStack(fluid, Math.max(1, entry.amount())), x + 2, y + 2, 14, 14);
                }
            } else {
                ItemStack stack = itemStack(entry.id());
                if (!stack.isEmpty()) {
                    GhostSlotRenderer.renderContainedItem(graphics, stack, x + 2, y + 2, 14);
                }
            }
            if (insideAbsolute(mouseX, mouseY, x, y, 17, 17)) {
                graphics.renderTooltip(font, List.of(Component.literal(seedCandidateLabel(entry)), Component.literal(entry.id().toString())), java.util.Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private List<NullSeedEntry> compatibleEntries(List<NullSeedEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<NullSeedEntry> compatible = new ArrayList<>();
        int sequentialTarget = 0;
        for (NullSeedEntry entry : entries) {
            if (!compatibleSeedEntry(entry)) {
                continue;
            }
            NullSeedEntry adjusted = entry.targetIndex() < 0 ? entry.withTargetIndex(sequentialTarget) : entry;
            if (adjusted.kind() == NullSeedKind.FLUID && adjusted.amount() <= 0) {
                adjusted = adjusted.withAmount(defaultSeedFluidAmount());
            } else if (adjusted.kind() == NullSeedKind.CHEMICAL && adjusted.amount() <= 0) {
                adjusted = adjusted.withAmount(defaultSeedFluidAmount());
            }
            compatible.add(adjusted);
            sequentialTarget++;
        }
        return List.copyOf(compatible);
    }

    private void placeSeedCandidate(NullSeedEntry candidate, boolean secondary) {
        NullSeedKind kind = menu.seedKind();
        if (candidate == null || kind == null) {
            return;
        }
        if (kind == NullSeedKind.DUMP_RULE) {
            DumpNullItemStatus status = secondary ? DumpNullItemStatus.DISCARDED : DumpNullItemStatus.ACCEPTED;
            selectedSeedEntries.removeIf(entry -> entry.kind() == NullSeedKind.DUMP_RULE && entry.id().equals(candidate.id()));
            selectedSeedEntries.add(NullSeedEntry.dumpRule(candidate.id(), status));
            selectedSeedIndex = selectedSeedEntries.size() - 1;
            updateWidgetVisibility();
            return;
        }
        int target = selectedSeedIndex >= 0 && selectedEntryIndexForTarget(selectedSeedIndex) < 0 ? selectedSeedIndex : firstAvailableSeedTarget();
        if (target < 0) {
            selectedSeedEntries.add(candidate.withTargetIndex(seedCapacity()));
        } else {
            selectedSeedEntries.removeIf(entry -> compatibleSeedEntry(entry) && entry.targetIndex() == target);
            if (kind == NullSeedKind.ITEM) {
                selectedSeedEntries.removeIf(entry -> entry.kind() == kind && entry.id().equals(candidate.id()));
            }
            selectedSeedEntries.add(candidate.withTargetIndex(target));
            selectedSeedIndex = target;
        }
        updateWidgetVisibility();
    }

    private boolean addCarriedSeedEntry(double mouseX, double mouseY, int button) {
        if (!ClientModEvents.isPrimaryGuiButton(button)) {
            return false;
        }
        int gridIndex = seedGridIndexAt(mouseX, mouseY);
        if (gridIndex < 0 || minecraft == null || minecraft.player == null) {
            return false;
        }
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            return false;
        }
        NullSeedEntry entry = seedEntryFromCarried(carried, gridIndex);
        if (entry == null) {
            return false;
        }
        selectedSeedEntries.removeIf(existing -> compatibleSeedEntry(existing) && existing.targetIndex() == gridIndex);
        selectedSeedEntries.add(entry);
        seedCandidates = appendCandidate(seedCandidates, entry);
        seedPresetId = "";
        seedPresetSource = NullSeedPresetSource.USER;
        selectedSeedIndex = gridIndex;
        seedStatus = Component.translatable("container.deepnullreforged.null_workbench.seed.added_carried");
        updateWidgetVisibility();
        return true;
    }

    private NullSeedEntry seedEntryFromCarried(ItemStack carried, int target) {
        NullSeedKind kind = menu.seedKind();
        if (kind == NullSeedKind.ITEM) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(carried.getItem());
            return itemId == null ? null : NullSeedEntry.item(itemId, target);
        }
        if (kind == NullSeedKind.DUMP_RULE) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(carried.getItem());
            return itemId == null ? null : NullSeedEntry.dumpRule(itemId, target < SEED_GRID_COLUMNS ? DumpNullItemStatus.ACCEPTED : DumpNullItemStatus.DISCARDED);
        }
        if (kind == NullSeedKind.FLUID) {
            FluidStack fluid = FluidUtil.getFluidHandler(carried.copyWithCount(1))
                    .map(handler -> handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE))
                    .orElse(FluidStack.EMPTY);
            if (fluid.isEmpty()) {
                return null;
            }
            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
            return fluidId == null ? null : NullSeedEntry.fluid(fluidId, target, Math.max(1000, fluid.getAmount()));
        }
        if (kind == NullSeedKind.ENTITY && carried.getItem() instanceof SpawnEggItem spawnEgg) {
            EntityType<?> type = spawnEgg.getType(carried);
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            return entityId == null ? null : NullSeedEntry.entity(entityId, target);
        }
        return null;
    }

    private List<NullSeedEntry> appendCandidate(List<NullSeedEntry> candidates, NullSeedEntry entry) {
        List<NullSeedEntry> result = new ArrayList<>(candidates == null ? List.of() : candidates);
        boolean exists = result.stream().anyMatch(candidate -> candidate.kind() == entry.kind() && candidate.id().equals(entry.id()));
        if (!exists) {
            result.add(entry.withTargetIndex(-1));
        }
        return List.copyOf(result);
    }

    private boolean cycleDumpSeedEntry(int gridIndex, boolean removeOnly) {
        DumpNullItemStatus status = gridIndex < SEED_GRID_COLUMNS ? DumpNullItemStatus.ACCEPTED : DumpNullItemStatus.DISCARDED;
        int index = gridIndex % SEED_GRID_COLUMNS;
        List<NullSeedEntry> entries = selectedSeedEntries.stream()
                .filter(entry -> entry.kind() == NullSeedKind.DUMP_RULE && entry.dumpStatus() == status)
                .limit(SEED_GRID_COLUMNS)
                .toList();
        if (index >= entries.size()) {
            return false;
        }
        NullSeedEntry entry = entries.get(index);
        selectedSeedEntries.remove(entry);
        if (!removeOnly && status == DumpNullItemStatus.ACCEPTED) {
            selectedSeedEntries.add(entry.withDumpStatus(DumpNullItemStatus.DISCARDED));
        }
        selectedSeedIndex = -1;
        return true;
    }

    private int seedCandidateIndexAt(double mouseX, double mouseY) {
        int x = leftPos + SEED_CANDIDATE_X;
        int y = topPos + SEED_CANDIDATE_Y;
        if (!insideAbsolute(mouseX, mouseY, x, y, SEED_CANDIDATE_WIDTH, SEED_CANDIDATE_HEIGHT)) {
            return -1;
        }
        int columns = Math.max(1, SEED_CANDIDATE_WIDTH / SEED_CELL_SIZE);
        int column = ((int) mouseX - x) / SEED_CELL_SIZE;
        int row = ((int) mouseY - y) / SEED_CELL_SIZE;
        return row * columns + column;
    }

    private int seedPresetIndexAt(double mouseX, double mouseY) {
        int x = leftPos + SEED_PRESET_X;
        int y = topPos + SEED_PRESET_Y;
        if (!insideAbsolute(mouseX, mouseY, x, y, SEED_PRESET_WIDTH, SEED_PRESET_HEIGHT)) {
            return -1;
        }
        return ((int) mouseY - y) / SEED_PRESET_ROW_HEIGHT;
    }

    private int seedGridIndexAt(double mouseX, double mouseY) {
        int x = leftPos + SEED_GRID_X;
        int y = topPos + SEED_GRID_Y;
        if (!insideAbsolute(mouseX, mouseY, x, y, SEED_GRID_WIDTH, SEED_GRID_HEIGHT)) {
            return -1;
        }
        if (menu.seedKind() == NullSeedKind.DUMP_RULE) {
            int localY = (int) mouseY - y;
            int row;
            if (localY >= 9 && localY < 26) {
                row = 0;
            } else if (localY >= 34 && localY < 51) {
                row = 1;
            } else {
                return -1;
            }
            int column = ((int) mouseX - x) / SEED_CELL_SIZE;
            return row * SEED_GRID_COLUMNS + Math.min(SEED_GRID_COLUMNS - 1, column);
        }
        int column = ((int) mouseX - x) / SEED_CELL_SIZE;
        int row = ((int) mouseY - y) / SEED_CELL_SIZE;
        int index = (seedGridScroll + row) * SEED_GRID_COLUMNS + column;
        return index < seedCapacity() ? index : -1;
    }

    private int selectedEntryIndexForTarget(int target) {
        NullSeedKind kind = menu.seedKind();
        for (int index = 0; index < selectedSeedEntries.size(); index++) {
            NullSeedEntry entry = selectedSeedEntries.get(index);
            if (compatibleSeedEntry(entry) && entry.targetIndex() == target) {
                return index;
            }
        }
        return -1;
    }

    private NullSeedEntry selectedEntryAtTarget(int target) {
        int index = selectedEntryIndexForTarget(target);
        return index < 0 ? null : selectedSeedEntries.get(index);
    }

    private int firstAvailableSeedTarget() {
        int capacity = seedCapacity();
        for (int target = 0; target < capacity; target++) {
            if (selectedEntryIndexForTarget(target) < 0) {
                return target;
            }
        }
        return -1;
    }

    private int seedCapacity() {
        return menu.seedTargetCapacity();
    }

    private int defaultSeedFluidAmount() {
        DeepNullInventory inventory = menu.createNullInventory();
        if (inventory == null || inventory.getFluidCapacity() <= 0) {
            return 1000;
        }
        return Math.max(1000, Math.min(64_000, inventory.getFluidCapacity() / 4));
    }

    private NullSeedPlan.Status plannedStatus(NullSeedPlan plan, NullSeedEntry seed, int target) {
        for (NullSeedPlan.Entry entry : plan.entries()) {
            if (entry.slot() == target && entry.itemId().equals(seed.id())) {
                return entry.status();
            }
        }
        return null;
    }

    private int statusColor(NullSeedPlan.Status status) {
        if (status == null) {
            return 0xFF3A3A44;
        }
        return switch (status) {
            case ALREADY_PRESENT -> 0xFF8EEB98;
            case WILL_RESERVE, WILL_FILL, WILL_RULE -> 0xFF88CCFF;
            case BLOCKED_OCCUPIED, INVALID -> 0xFFFF7777;
            case OVERFLOW -> 0xFFFFC857;
        };
    }

    private boolean isSelected(NullSeedEntry candidate) {
        return selectedSeedEntries.stream().anyMatch(entry -> entry.kind() == candidate.kind() && entry.id().equals(candidate.id()));
    }

    private boolean compatibleSeedEntry(NullSeedEntry entry) {
        NullSeedKind kind = menu.seedKind();
        return entry != null && (entry.kind() == kind || (kind == NullSeedKind.FLUID && (entry.kind() == NullSeedKind.FLUID || entry.kind() == NullSeedKind.CHEMICAL)));
    }

    private String seedCandidateLabel(NullSeedEntry entry) {
        if (entry.kind() == NullSeedKind.FLUID) {
            Fluid fluid = BuiltInRegistries.FLUID.get(entry.id());
            return fluid == Fluids.EMPTY ? entry.id().toString() : new FluidStack(fluid, Math.max(1, entry.amount())).getHoverName().getString();
        }
        if (entry.kind() == NullSeedKind.CHEMICAL) {
            return entry.id().getPath();
        }
        if (entry.kind() == NullSeedKind.ENTITY) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entry.id());
            return !BuiltInRegistries.ENTITY_TYPE.containsKey(entry.id()) ? entry.id().toString() : Component.translatable(type.getDescriptionId()).getString();
        }
        ItemStack stack = itemStack(entry.id());
        return stack.isEmpty() ? entry.id().toString() : stack.getHoverName().getString();
    }

    private String gridLabelKey() {
        NullSeedKind kind = menu.seedKind();
        if (kind == NullSeedKind.FLUID) {
            return "container.deepnullreforged.null_workbench.seed.tanks";
        }
        if (kind == NullSeedKind.DUMP_RULE) {
            return "container.deepnullreforged.null_workbench.seed.rules";
        }
        if (kind == NullSeedKind.ENTITY) {
            return "container.deepnullreforged.null_workbench.seed.entities";
        }
        return "container.deepnullreforged.null_workbench.seed.slots";
    }

    private void renderTinyScrollHint(GuiGraphics graphics, int x, int y, int height, int totalRows, int visibleRows, int firstRow) {
        if (totalRows <= visibleRows) {
            return;
        }
        graphics.fill(x, y, x + 2, y + height, 0x553C4655);
        int handleHeight = Math.max(8, (height * visibleRows) / totalRows);
        int track = Math.max(1, height - handleHeight);
        int handleY = y + Math.round(track * (firstRow / (float) Math.max(1, totalRows - visibleRows)));
        graphics.fill(x, handleY, x + 2, handleY + handleHeight, 0xFFC8D7E8);
    }

    private void renderFluidTexture(GuiGraphics graphics, FluidStack fluidStack, int x, int y, int width, int height) {
        renderFluidTexture(graphics, fluidStack, x, y, width, height, 1.0F);
    }

    private void renderFluidTexture(GuiGraphics graphics, FluidStack fluidStack, int x, int y, int width, int height, float alphaMultiplier) {
        IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation texture = clientFluid.getStillTexture(fluidStack);
        int tint = clientFluid.getTintColor(fluidStack);
        if (texture == null) {
            graphics.fill(x, y, x + width, y + height, withAlpha(tint == 0 ? 0xFF3AA7FF : tint, alphaMultiplier));
            return;
        }
        TextureAtlasSprite sprite = FluidSpriteCache.getSprite(texture);
        float baseAlpha = ((tint >> 24) & 0xFF) / 255.0F;
        float alpha = (baseAlpha <= 0.0F ? 1.0F : baseAlpha) * alphaMultiplier;
        float red = ((tint >> 16) & 0xFF) / 255.0F;
        float green = ((tint >> 8) & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;
        graphics.blit(x, y, 0, width, height, sprite, red, green, blue, alpha);
    }

    private void renderChemicalSwatch(GuiGraphics graphics, StoredChemical chemical, int x, int y, int width, int height) {
        renderChemicalSwatch(graphics, chemical, x, y, width, height, 1.0F);
    }

    private void renderChemicalSwatch(GuiGraphics graphics, StoredChemical chemical, int x, int y, int width, int height, float alphaMultiplier) {
        int tint = chemical == null || chemical.isEmpty() ? 0xFFB8D7FF : chemical.tint();
        if ((tint >>> 24) == 0) {
            tint |= 0xFF000000;
        }
        graphics.fill(x, y, x + width, y + height, withAlpha(tint, alphaMultiplier));
        graphics.renderOutline(x, y, width, height, 0xFFE8EDF5);
    }

    private int withAlpha(int color, float alphaMultiplier) {
        int baseAlpha = (color >>> 24) & 0xFF;
        int alpha = Math.max(0, Math.min(255, Math.round((baseAlpha == 0 ? 255 : baseAlpha) * alphaMultiplier)));
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private StoredChemical plannedChemical(NullSeedEntry entry) {
        return new StoredChemical(entry.id().toString(), Math.max(1, entry.amount()), "", 0xFFB8D7FF, "", true);
    }

    private int occupiedSlots(DeepNullInventory inventory) {
        if (menu.seedKind() == NullSeedKind.ENTITY) {
            return DenNullData.get(menu.getNullStack()).entries().size();
        }
        if (inventory == null) {
            return 0;
        }
        if (inventory.isFluidOnly()) {
            int occupied = 0;
            for (int tank = 0; tank < inventory.getFluidSlotCount(); tank++) {
                if (!inventory.getFluidInSlot(tank).isEmpty() || !inventory.getChemicalInSlot(tank).isEmpty()) {
                    occupied++;
                }
            }
            return occupied;
        }
        int occupied = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                occupied++;
            }
        }
        return occupied;
    }

    private int reservedTemplateCount(DeepNullInventory inventory) {
        if (menu.seedKind() == NullSeedKind.ENTITY) {
            return DenNullData.get(menu.getNullStack()).templates().size();
        }
        if (inventory == null) {
            return 0;
        }
        return inventory.isFluidOnly() ? inventory.getReservedTankTemplateCount() : inventory.getReservedSlotCount();
    }

    private ItemStack itemStack(ResourceLocation itemId) {
        if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(itemId);
        return new ItemStack(item);
    }

    private void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xAA0A0C12);
        graphics.renderOutline(x, y, width, height, 0xFF4E5A6D);
    }

    private Component trim(Component component, int maxWidth) {
        return Component.literal(font.plainSubstrByWidth(component.getString(), maxWidth));
    }

    private boolean handleStylePickerClick(double mouseX, double mouseY) {
        if (!hasStyledNull()) {
            return false;
        }
        int localX = (int) mouseX - leftPos;
        int localY = (int) mouseY - topPos;

        if (inside(localX, localY, STYLE_FRAME_BOX_X - STYLE_CLICK_PADDING, STYLE_FRAME_BOX_Y - STYLE_CLICK_PADDING, STYLE_BOX_WIDTH + (STYLE_CLICK_PADDING * 2), STYLE_BOX_HEIGHT + (STYLE_CLICK_PADDING * 2))) {
            selectedStyleTarget = StyleTarget.FRAME;
            frameColorBox.setFocused(true);
            glassColorBox.setFocused(false);
            return false;
        }
        if (inside(localX, localY, STYLE_GLASS_BOX_X - STYLE_CLICK_PADDING, STYLE_GLASS_BOX_Y - STYLE_CLICK_PADDING, STYLE_BOX_WIDTH + (STYLE_CLICK_PADDING * 2), STYLE_BOX_HEIGHT + (STYLE_CLICK_PADDING * 2))) {
            selectedStyleTarget = StyleTarget.GLASS;
            glassColorBox.setFocused(true);
            frameColorBox.setFocused(false);
            return false;
        }
        if (inside(localX, localY, STYLE_PICKER_X, STYLE_PICKER_Y, STYLE_PICKER_SIZE, STYLE_PICKER_SIZE)) {
            draggingStylePicker = true;
            updateColorFromPicker(localX, localY);
            return true;
        }
        if (inside(localX, localY, STYLE_HUE_X, STYLE_HUE_Y, STYLE_HUE_WIDTH, STYLE_HUE_HEIGHT)) {
            draggingHueStrip = true;
            updateColorFromHue(localY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (activeTab == WorkbenchTab.STYLE && hasStyledNull() && button == 0) {
            int localX = (int) mouseX - leftPos;
            int localY = (int) mouseY - topPos;
            if (draggingStylePicker) {
                updateColorFromPicker(localX, localY);
                return true;
            }
            if (draggingHueStrip) {
                updateColorFromHue(localY);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingStylePicker = false;
        draggingHueStrip = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (activeTab == WorkbenchTab.SEED) {
            if (insideAbsolute(mouseX, mouseY, leftPos + SEED_PRESET_X, topPos + SEED_PRESET_Y, SEED_PRESET_WIDTH, SEED_PRESET_HEIGHT)) {
                int visible = Math.max(1, SEED_PRESET_HEIGHT / SEED_PRESET_ROW_HEIGHT);
                seedPresetScroll = clamp(seedPresetScroll - (int) Math.signum(scrollY), 0, Math.max(0, filteredSeedPresets().size() - visible));
                return true;
            }
            if (insideAbsolute(mouseX, mouseY, leftPos + SEED_CANDIDATE_X, topPos + SEED_CANDIDATE_Y, SEED_CANDIDATE_WIDTH, SEED_CANDIDATE_HEIGHT)) {
                int columns = Math.max(1, SEED_CANDIDATE_WIDTH / SEED_CELL_SIZE);
                int rows = Math.max(1, SEED_CANDIDATE_HEIGHT / SEED_CELL_SIZE);
                int visible = columns * rows;
                seedCandidateScroll = clamp(seedCandidateScroll - (int) Math.signum(scrollY), 0, Math.max(0, filteredSeedCandidates().size() - visible));
                return true;
            }
            if (insideAbsolute(mouseX, mouseY, leftPos + SEED_GRID_X, topPos + SEED_GRID_Y, SEED_GRID_WIDTH, SEED_GRID_HEIGHT) && menu.seedKind() != NullSeedKind.DUMP_RULE) {
                int visibleRows = Math.max(1, SEED_GRID_HEIGHT / SEED_CELL_SIZE);
                int totalRows = Math.max(1, (seedCapacity() + SEED_GRID_COLUMNS - 1) / SEED_GRID_COLUMNS);
                seedGridScroll = clamp(seedGridScroll - (int) Math.signum(scrollY), 0, Math.max(0, totalRows - visibleRows));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean clickTab(double mouseX, double mouseY, int x, int y, WorkbenchTab target) {
        if (mouseX < x || mouseX >= x + TAB_WIDTH || mouseY < y || mouseY >= y + TAB_HEIGHT) {
            return false;
        }
        if (activeTab != target) {
            activeTab = target;
            if (target == WorkbenchTab.SEED && !menu.canSeedNull()) {
                seedStatus = Component.translatable("container.deepnullreforged.null_workbench.seed.requires_deepnull");
            } else if (target == WorkbenchTab.SEED) {
                requestSeedPresetListIfNeeded();
            }
            refreshStyleFields();
            updateWidgetVisibility();
            updateMachineSlotLayout();
        }
        return true;
    }

    private void updateWidgetVisibility() {
        boolean sync = activeTab == WorkbenchTab.SYNC;
        boolean style = activeTab == WorkbenchTab.STYLE && hasStyledNull();
        boolean seed = activeTab == WorkbenchTab.SEED;
        backupButton.visible = sync;
        backupButton.active = sync && menu.canBackup();
        restoreButton.visible = sync;
        restoreButton.active = sync && menu.canRestore();
        frameColorBox.visible = style;
        frameColorBox.setEditable(style);
        glassColorBox.visible = style;
        glassColorBox.setEditable(style);
        seedSearchBox.visible = seed;
        seedSearchBox.setEditable(seed);
        boolean canApplySeed = seed && menu.canSeedNull() && !selectedSeedEntries.isEmpty();
        seedApplyButton.visible = seed;
        seedApplyButton.active = canApplySeed;
        seedClearButton.visible = seed;
        seedClearButton.active = seed && menu.canSeedNull();
        seedSaveButton.visible = seed;
        seedSaveButton.active = canApplySeed;
        seedDeleteButton.visible = seed;
        seedDeleteButton.active = seed && menu.canSeedNull() && seedPresetSource == NullSeedPresetSource.USER && !seedPresetId.isBlank();
    }

    private void refreshStyleFields() {
        ItemStack stack = menu.getNullStack();
        if (!menu.canStyleNull() || !(stack.getItem() instanceof DeepNullItem deepNullItem) || minecraft == null || minecraft.level == null) {
            frameColorBox.setValue("#FFFFFF");
            glassColorBox.setValue("#FFFFFF");
            return;
        }
        DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), stack.copy(), minecraft.level.registryAccess(), null);
        frameColorBox.setValue(formatHex(inventory.getFrameColor()));
        glassColorBox.setValue(formatHex(inventory.getGlassColor()));
    }

    private void applyStyle(boolean reset) {
        if (!menu.hasWorkbench()) {
            return;
        }
        PacketDistributor.sendToServer(new NullWorkbenchPayloads.ApplyStylePayload(
                menu.getBlockPos(),
                parseHex(frameColorBox.getValue(), 0xFFFFFF),
                parseHex(glassColorBox.getValue(), 0xFFFFFF),
                reset
        ));
    }

    private ItemStack previewStack() {
        ItemStack stack = menu.getNullStack();
        if (!menu.canStyleNull() || !(stack.getItem() instanceof DeepNullItem deepNullItem) || minecraft == null || minecraft.level == null) {
            return stack;
        }
        ItemStack preview = stack.copy();
        DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), preview, minecraft.level.registryAccess(), null);
        StyleGlassVariant variant = menu.getStyleModifierStack().isEmpty()
                ? DeepNullInventory.getStyleVariant(preview)
                : StyleGlassVariant.fromModifier(preview, menu.getStyleModifierStack());
        inventory.setStyle(
                parseHex(frameColorBox.getValue(), inventory.getFrameColor()),
                parseHex(glassColorBox.getValue(), inventory.getGlassColor()),
                variant
        );
        return preview;
    }

    private float[] getActiveHsv() {
        int color = selectedStyleTarget == StyleTarget.FRAME
                ? parseHex(frameColorBox.getValue(), 0xFFFFFF)
                : parseHex(glassColorBox.getValue(), 0xFFFFFF);
        return rgbToHsv(color);
    }

    private void updateColorFromPicker(int localX, int localY) {
        float[] hsv = getActiveHsv();
        hsv[1] = clamp01((localX - STYLE_PICKER_X) / (float) (STYLE_PICKER_SIZE - 1));
        hsv[2] = clamp01(1.0F - ((localY - STYLE_PICKER_Y) / (float) (STYLE_PICKER_SIZE - 1)));
        setActiveHexValue(formatHex(hsvToRgb(hsv[0], hsv[1], hsv[2])));
    }

    private void updateColorFromHue(int localY) {
        float[] hsv = getActiveHsv();
        hsv[0] = clamp01((localY - STYLE_HUE_Y) / (float) (STYLE_HUE_HEIGHT - 1)) * 360.0F;
        setActiveHexValue(formatHex(hsvToRgb(hsv[0], hsv[1], hsv[2])));
    }

    private void setActiveHexValue(String value) {
        if (selectedStyleTarget == StyleTarget.FRAME) {
            frameColorBox.setValue(value);
        } else {
            glassColorBox.setValue(value);
        }
    }

    private static int parseHex(String value, int fallback) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (normalized.length() != 6) {
            return fallback;
        }
        try {
            return Integer.parseInt(normalized, 16) & 0xFFFFFF;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String formatHex(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }

    private static int blendRgb(int from, int to, float t) {
        t = clamp01(t);
        int fr = (from >> 16) & 0xFF;
        int fg = (from >> 8) & 0xFF;
        int fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF;
        int tg = (to >> 8) & 0xFF;
        int tb = to & 0xFF;
        int r = Math.round(fr + ((tr - fr) * t));
        int g = Math.round(fg + ((tg - fg) * t));
        int b = Math.round(fb + ((tb - fb) * t));
        return (r << 16) | (g << 8) | b;
    }

    private static boolean inside(int x, int y, int areaX, int areaY, int areaWidth, int areaHeight) {
        return x >= areaX && x < areaX + areaWidth && y >= areaY && y < areaY + areaHeight;
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float[] rgbToHsv(int color) {
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float hue;
        if (delta == 0.0F) {
            hue = 0.0F;
        } else if (max == r) {
            hue = 60.0F * (((g - b) / delta) % 6.0F);
        } else if (max == g) {
            hue = 60.0F * (((b - r) / delta) + 2.0F);
        } else {
            hue = 60.0F * (((r - g) / delta) + 4.0F);
        }
        if (hue < 0.0F) {
            hue += 360.0F;
        }
        float saturation = max == 0.0F ? 0.0F : delta / max;
        return new float[]{hue, saturation, max};
    }

    private static int hsvToRgb(float hue, float saturation, float value) {
        float c = value * saturation;
        float x = c * (1.0F - Math.abs(((hue / 60.0F) % 2.0F) - 1.0F));
        float m = value - c;

        float rPrime;
        float gPrime;
        float bPrime;
        if (hue < 60.0F) {
            rPrime = c;
            gPrime = x;
            bPrime = 0.0F;
        } else if (hue < 120.0F) {
            rPrime = x;
            gPrime = c;
            bPrime = 0.0F;
        } else if (hue < 180.0F) {
            rPrime = 0.0F;
            gPrime = c;
            bPrime = x;
        } else if (hue < 240.0F) {
            rPrime = 0.0F;
            gPrime = x;
            bPrime = c;
        } else if (hue < 300.0F) {
            rPrime = x;
            gPrime = 0.0F;
            bPrime = c;
        } else {
            rPrime = c;
            gPrime = 0.0F;
            bPrime = x;
        }

        int r = Math.round((rPrime + m) * 255.0F);
        int g = Math.round((gPrime + m) * 255.0F);
        int b = Math.round((bPrime + m) * 255.0F);
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    private void updateStyleControlPositions() {
        frameColorBox.setX(leftPos + STYLE_FRAME_BOX_X + 1);
        frameColorBox.setY(topPos + STYLE_FRAME_BOX_Y + 1);
        glassColorBox.setX(leftPos + STYLE_GLASS_BOX_X + 1);
        glassColorBox.setY(topPos + STYLE_GLASS_BOX_Y + 1);
    }

    private void updateMachineSlotLayout() {
        menu.setCraftSlotsActive(activeTab == WorkbenchTab.CRAFT);
        menu.setOutputSlotActive(activeTab == WorkbenchTab.CRAFT || activeTab == WorkbenchTab.STYLE);
    }

    private int tabX(int index) {
        return leftPos + TAB_START_X + (index * (TAB_WIDTH + TAB_GAP));
    }

    private int tabY() {
        return topPos - TAB_Y_OFFSET;
    }

    private int styleApplyButtonX() {
        return leftPos + STYLE_APPLY_BUTTON_X;
    }

    private int styleApplyButtonY() {
        return topPos + STYLE_APPLY_BUTTON_Y;
    }

    private int styleResetButtonX() {
        return leftPos + STYLE_RESET_BUTTON_X;
    }

    private int styleResetButtonY() {
        return topPos + STYLE_RESET_BUTTON_Y;
    }

    private boolean hasStyledNull() {
        return menu.canStyleNull();
    }

    private static boolean insideAbsolute(double mouseX, double mouseY, int areaX, int areaY, int areaWidth, int areaHeight) {
        return mouseX >= areaX && mouseX < areaX + areaWidth && mouseY >= areaY && mouseY < areaY + areaHeight;
    }

    private enum WorkbenchTab {
        CRAFT,
        SYNC,
        STYLE,
        SEED
    }

    private enum StyleTarget {
        FRAME,
        GLASS
    }
}
