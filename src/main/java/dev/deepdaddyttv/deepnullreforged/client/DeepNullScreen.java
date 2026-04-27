package dev.deepdaddyttv.deepnullreforged.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.inventory.StoneworksMaterial;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferDirectionMode;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferOutputMode;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeepNullScreen extends AbstractContainerScreen<DeepNullMenu> {
    private static final int BASE_IMAGE_WIDTH = 202;
    private static final int ENERGY_IMAGE_WIDTH = 252;
    private static final int INFO_PANEL_WIDTH = 146;
    private static final int INFO_PANEL_PADDING = 6;
    private static final int INFO_PANEL_LINE_HEIGHT = 10;
    private static final Identifier INFO_BUTTON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_info_button.png");
    private static final Identifier LOCK_BUTTON_OFF_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_lock_button_off.png");
    private static final Identifier LOCK_BUTTON_ON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_lock_button_on.png");
    private static final Identifier UPGRADE_BUTTON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_upgrade_button.png");
    private static final Identifier CHARGING_BUTTON_OFF_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_charging_button_off.png");
    private static final Identifier CHARGING_BUTTON_ON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_charging_button_on.png");
    private static final Identifier STONEWORKS_BUTTON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_stoneworks_button.png");
    private static final Identifier DIALOGUE_ACTIVE_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_dialogue_active.png");
    private static final Identifier DIALOGUE_INACTIVE_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_dialogue_inactive.png");
    private static final Identifier PLUS_BUTTON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_plus_button.png");
    private static final Identifier MINUS_BUTTON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_minus_button.png");
    private static final Identifier EXTRACT_DIALOG_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_extract_mode_dialogue_box.png");
    private static final Identifier INFO_TAB_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_info_tab.png");
    private static final int TAB_BUTTON_U = 98;
    private static final int INFO_BUTTON_V = 16;
    private static final int LOCK_BUTTON_V = 37;
    private static final int UPGRADE_BUTTON_V = 37;
    private static final int CHARGING_BUTTON_V = 58;
    private static final int STONEWORKS_BUTTON_V = 37;
    private static final int TAB_BUTTON_WIDTH = 13;
    private static final int TAB_BUTTON_HEIGHT = 19;
    private static final int INFO_TAB_U = 105;
    private static final int INFO_TAB_V = 6;
    private static final int INFO_TAB_WIDTH = 146;
    private static final int INFO_TAB_HEIGHT = 170;
    private static final int ENERGY_TOOLTIP_WIDTH = 50;
    private static final int INTEGRATED_LAYOUT_LABEL_X = 57;
    private static final int INFO_UPGRADE_ICON_SIZE = 16;
    private static final int INFO_UPGRADE_ICON_GAP = 2;
    private static final int STONEWORKS_GRID_COLUMNS = 3;
    private static final int STONEWORKS_GRID_SPACING = 22;
    private static final int STONEWORKS_GRID_START_Y = 86;
    private static final int DIALOGUE_TEXTURE_SIZE = 128;
    private static final int DIALOGUE_ACTIVE_U = 39;
    private static final int DIALOGUE_ACTIVE_V = 53;
    private static final int DIALOGUE_INACTIVE_U = 40;
    private static final int DIALOGUE_INACTIVE_V = 53;
    private static final int DIALOGUE_WIDTH = 49;
    private static final int DIALOGUE_HEIGHT = 19;
    private static final int DIALOGUE_TEXT_PADDING_X = 6;
    private static final int DIALOGUE_TEXT_PADDING_Y = 3;
    private static final int DIALOGUE_TEXTBOX_HEIGHT = 14;
    private static final int DIALOGUE_BUTTON_GAP = 2;
    private static final int STEP_BUTTON_TEXTURE_SIZE = 128;
    private static final int STEP_BUTTON_SIZE = 19;
    private static final int PLUS_BUTTON_U = 55;
    private static final int PLUS_BUTTON_V = 47;
    private static final int MINUS_BUTTON_U = 54;
    private static final int MINUS_BUTTON_V = 55;
    private static final int EXTRACT_DIALOG_TEXTURE_SIZE = 128;
    private static final int EXTRACT_DIALOG_U = 11;
    private static final int EXTRACT_DIALOG_V = 35;
    private static final int EXTRACT_DIALOG_WIDTH = 106;
    private static final int EXTRACT_DIALOG_HEIGHT = 42;
    private static final int EXTRACT_DIALOG_TITLE_X = 8;
    private static final int EXTRACT_DIALOG_TITLE_Y = 7;
    private static final int EXTRACT_DIALOG_CONTROLS_Y = 18;

    private final Identifier backgroundTexture;
    private final int baseImageWidth;
    private final boolean integratedEnergyGui;
    private int latchedReorderSlot = -1;
    private boolean shiftQuickMoveDragging;
    private int shiftQuickMoveButton = -1;
    private final Set<Integer> shiftQuickMovedSlots = new HashSet<>();
    private boolean infoPanelOpen;
    private boolean stoneworksPanelOpen;
    private EditBox stoneworksAmountBox;
    private EditBox customExtractionBox;
    private int customExtractionSlot = -1;
    private int customExtractionAnchorX;
    private int customExtractionAnchorY;
    private int customExtractionInitialValue;
    private boolean customExtractionApplyAll;

    public DeepNullScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, imageWidthFor(menu), imageHeightFor(menu));
        this.integratedEnergyGui = menu.hasEnergyUpgrade() && menu.getTier().supportsEnergyUpgrade();
        this.baseImageWidth = integratedEnergyGui ? ENERGY_IMAGE_WIDTH : BASE_IMAGE_WIDTH;
        this.backgroundTexture = integratedEnergyGui ? integratedEnergyTexture(menu) : menu.getTier().guiTexture();
        this.inventoryLabelX = integratedEnergyGui ? INTEGRATED_LAYOUT_LABEL_X : 7;
        this.inventoryLabelY = this.imageHeight - 103;
        this.titleLabelX = 7;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        latchedReorderSlot = -1;
        clearShiftQuickMoveState();

        stoneworksAmountBox = addRenderableWidget(new EditBox(
                font,
                0,
                0,
                DIALOGUE_WIDTH - (DIALOGUE_TEXT_PADDING_X * 2),
                DIALOGUE_TEXTBOX_HEIGHT,
                Component.translatable("dn.stoneworks_amount.desc")
        ));
        stoneworksAmountBox.setMaxLength(10);
        stoneworksAmountBox.setBordered(false);
        stoneworksAmountBox.setTextColor(0xFFFFFFFF);
        stoneworksAmountBox.setTextColorUneditable(0xFFFFFFFF);
        stoneworksAmountBox.setFilter(value -> value.chars().allMatch(Character::isDigit));
        stoneworksAmountBox.setResponder(this::onStoneworksAmountChanged);

        customExtractionBox = addRenderableWidget(new EditBox(
                font,
                0,
                0,
                DIALOGUE_WIDTH - (DIALOGUE_TEXT_PADDING_X * 2),
                DIALOGUE_TEXTBOX_HEIGHT,
                Component.translatable("dn.custom_extract_amount.desc")
        ));
        customExtractionBox.setMaxLength(10);
        customExtractionBox.setBordered(false);
        customExtractionBox.setTextColor(0xFFFFFFFF);
        customExtractionBox.setTextColorUneditable(0xFFFFFFFF);
        customExtractionBox.setFilter(value -> value.chars().allMatch(Character::isDigit));
        customExtractionBox.visible = false;
        customExtractionBox.active = false;

        updateStoneworksAmountBox();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, leftPos, topPos, 0.0F, 0.0F, baseImageWidth, imageHeight, 256, 256);
        if (integratedEnergyGui) {
            renderIntegratedEnergyFill(graphics);
        }

        highlightSelectedSlot(graphics);
        for (int slotIndex = 0; slotIndex < menu.getStorageSlotCount(); slotIndex++) {
            Slot slot = menu.slots.get(slotIndex);
            if (slot.hasItem()) {
                drawModeMarkers(graphics, slotIndex, leftPos + slot.x, topPos + slot.y);
            }
        }
        renderIconButtons(graphics);

        super.extractContents(graphics, mouseX, mouseY, partialTick);

        if (infoPanelOpen) {
            graphics.nextStratum();
            renderInfoPanel(graphics);
        } else if (stoneworksPanelOpen) {
            graphics.nextStratum();
            renderStoneworksPanel(graphics);
        }
        if (customExtractionBox != null && customExtractionBox.visible) {
            graphics.nextStratum();
            renderCustomExtractionEditor(graphics);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0xFFFFFFFF, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFFFFFFF, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (customExtractionBox != null && customExtractionBox.visible && isWithin(customExtractionPopupBounds(), mouseX, mouseY)) {
            return;
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (integratedEnergyGui) {
            renderIntegratedCreativeEnergyTooltip(graphics, mouseX, mouseY);
        }
        renderIconButtonTooltip(graphics, mouseX, mouseY);
        if (stoneworksPanelOpen) {
            renderStoneworksTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (ClientModEvents.isTertiaryGuiButton(event.button()) && handleBlockedMiddleClick(event.x(), event.y())) {
            return true;
        }
        if (customExtractionBox != null && customExtractionBox.visible) {
            if (handleCustomExtractionEditorClick(event)) {
                return true;
            }
            if (!isWithin(customExtractionEditorBounds(), event.x(), event.y())) {
                closeCustomExtractionEditor(true);
            }
        }

        Slot slot = findSlotAt(event.x(), event.y());
        if (handleIconButtonClick(event)) {
            return true;
        }
        if (stoneworksPanelOpen && handleStoneworksAmountClick(event)) {
            return true;
        }
        if (stoneworksPanelOpen && ClientModEvents.isPrimaryGuiButton(event.button())) {
            StoneworksMaterial material = stoneworksMaterialAt(event.x(), event.y());
            if (material != null) {
                menu.toggleStoneworksMonitoring(material);
                ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuStoneworksTogglePayload(material.ordinal()));
                return true;
            }
        }

        clearShiftQuickMoveState();
        boolean shouldBeginShiftQuickMoveDrag = canStartShiftQuickMoveDrag(slot, event.button());
        if (slot instanceof DeepNullMenu.StorageSlot && slot.hasItem()) {
            if (ScreenActions.handle(this, slot.index, event.button())) {
                return true;
            }
        }
        if (shouldBeginShiftQuickMoveDrag) {
            shiftQuickMoveDragging = true;
            shiftQuickMoveButton = event.button();
            shiftQuickMovedSlots.add(slot.index);
        }

        boolean handled = super.mouseClicked(event, doubleClick);
        if (!handled) {
            clearShiftQuickMoveState();
        }
        return handled;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (shiftQuickMoveDragging && event.button() == shiftQuickMoveButton && isShiftDown() && menu.getCarried().isEmpty()) {
            Slot hovered = findSlotAt(event.x(), event.y());
            if (canShiftQuickMoveSlot(hovered) && shiftQuickMovedSlots.add(hovered.index)) {
                slotClicked(hovered, hovered.index, event.button(), ContainerInput.QUICK_MOVE);
                return true;
            }
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        clearShiftQuickMoveState();
        if (ClientModEvents.isTertiaryGuiButton(event.button()) && handleBlockedMiddleRelease(event.x(), event.y())) {
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (customExtractionBox != null && customExtractionBox.isFocused()) {
            if (event.isConfirmation() || event.isCycleFocus()) {
                closeCustomExtractionEditor(true);
                return true;
            }
            if (event.isEscape()) {
                closeCustomExtractionEditor(false);
                return true;
            }
            return customExtractionBox.keyPressed(event) || super.keyPressed(event);
        }
        if (stoneworksAmountBox != null && stoneworksAmountBox.isFocused()) {
            if (stoneworksAmountBox.keyPressed(event)) {
                return true;
            }
            return super.keyPressed(event);
        }
        if (event.hasAltDown()) {
            int sourceSlot = getReorderSourceSlot();
            int targetSlot = getReorderTargetSlot(sourceSlot, event.key());
            if (sourceSlot >= 0 && targetSlot >= 0 && menu.moveStorageSlot(sourceSlot, targetSlot)) {
                latchedReorderSlot = targetSlot;
                ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuReorderPayload(sourceSlot, targetSlot));
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_LEFT_ALT || event.key() == GLFW.GLFW_KEY_RIGHT_ALT || !event.hasAltDown()) {
            latchedReorderSlot = -1;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (customExtractionBox != null && customExtractionBox.isFocused()) {
            return customExtractionBox.charTyped(event);
        }
        if (stoneworksAmountBox != null && stoneworksAmountBox.isFocused()) {
            return stoneworksAmountBox.charTyped(event);
        }
        return super.charTyped(event);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (!isAltDown()) {
            latchedReorderSlot = -1;
        }
        if (!isShiftDown()) {
            clearShiftQuickMoveState();
        }
        updateStoneworksAmountBox();
        updateCustomExtractionBox();
    }

    @Override
    protected void renderSlotContents(GuiGraphicsExtractor graphics, ItemStack itemStack, Slot slot, String countString) {
        if (isStorageItemSlot(slot) && !itemStack.isEmpty()) {
            if (customExtractionBox != null && customExtractionBox.visible) {
                Rect2i slotRect = new Rect2i(leftPos + slot.x, topPos + slot.y, 16, 16);
                if (intersects(customExtractionPopupBounds(), slotRect)) {
                    return;
                }
            }

            String overlay = compactSlotCountText(itemStack);
            super.renderSlotContents(graphics, itemStack.copyWithCount(1), slot, "");
            if (overlay != null && !overlay.isEmpty()) {
                renderStorageCountOverlay(graphics, slot, overlay);
            }
            return;
        }
        super.renderSlotContents(graphics, itemStack, slot, countString);
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> tooltip = new ArrayList<>(super.getTooltipFromContainerItem(stack));
        if (!isStorageItemSlot(hoveredSlot) || stack.isEmpty()) {
            return tooltip;
        }

        Component countLine = Component.literal("")
                .append(label(
                        "dn.count.desc",
                        menu.getDankInventory().supportsLocking() && menu.getDankInventory().isLocked()
                                ? Component.translatable("dn.infinite.desc")
                                : Component.literal(DeepNullCountFormatter.formatExact(stack.getCount()))
                ))
                .withStyle(ChatFormatting.GRAY);

        if (tooltip.isEmpty()) {
            tooltip.add(stack.getHoverName());
        }
        tooltip.add(Math.min(1, tooltip.size()), countLine);
        return tooltip;
    }

    private void highlightSelectedSlot(GuiGraphicsExtractor graphics) {
        int selectedSlot = menu.getDankInventory().getSelectedSlot();
        if (selectedSlot >= 0 && selectedSlot < menu.getStorageSlotCount()) {
            Slot slot = menu.slots.get(selectedSlot);
            graphics.outline(leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18, menu.getTier().ordinalId() == 0 ? 0xFFE7B623 : 0xFFD8DCE5);
        }
    }

    private boolean canStartShiftQuickMoveDrag(Slot slot, int button) {
        if ((button != 0 && button != 1) || !isShiftDown() || !menu.getCarried().isEmpty()) {
            return false;
        }
        return canShiftQuickMoveSlot(slot);
    }

    private boolean canShiftQuickMoveSlot(Slot slot) {
        if (slot == null || !slot.hasItem() || minecraft == null || minecraft.player == null) {
            return false;
        }
        int index = slot.index;
        return index >= menu.getPlayerInventorySlotStartIndex()
                && index < menu.getPlayerInventorySlotStartIndex() + menu.getPlayerSlotCount()
                && slot.mayPickup(minecraft.player);
    }

    private Slot findSlotAt(double mouseX, double mouseY) {
        for (Slot slot : menu.slots) {
            if (slot != null && slot.isActive() && isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    private void clearShiftQuickMoveState() {
        shiftQuickMoveDragging = false;
        shiftQuickMoveButton = -1;
        shiftQuickMovedSlots.clear();
    }

    private void renderIntegratedEnergyFill(GuiGraphicsExtractor graphics) {
        int capacity = menu.getDisplayedEnergyCapacity();
        int stored = menu.getDisplayedEnergyStored();
        if (capacity <= 0 || stored <= 0) {
            return;
        }

        int fillHeight = Math.max(1, Math.round(imageHeight * Math.min(1.0F, stored / (float) capacity)));
        int drawY = topPos + (imageHeight - fillHeight);
        int sourceY = imageHeight - fillHeight;
        graphics.blit(RenderPipelines.GUI_TEXTURED, integratedEnergyFillTexture(), leftPos, drawY, 0.0F, sourceY, baseImageWidth, fillHeight, 256, 256);
    }

    private void renderIntegratedCreativeEnergyTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!menu.hasEnergyUpgrade()) {
            return;
        }
        if (mouseX < leftPos || mouseX >= leftPos + ENERGY_TOOLTIP_WIDTH || mouseY < topPos || mouseY >= topPos + imageHeight) {
            return;
        }

        graphics.setComponentTooltipForNextFrame(
                font,
                List.of(
                        Component.translatable("dn.energy.desc"),
                        Component.literal(menu.getDisplayedEnergyStored() + " / " + menu.getDisplayedEnergyCapacity() + " FE"),
                        Component.translatable("dn.charging.desc").append(": ").append(Component.translatable(menu.isChargingEnabledDisplayed() ? "dn.enabled.desc" : "dn.disabled.desc"))
                ),
                mouseX,
                mouseY
        );
    }

    private void drawModeMarkers(GuiGraphicsExtractor graphics, int slotIndex, int x, int y) {
        graphics.fill(x, y, x + 16, y + 2, extractionColor(slotIndex));
        graphics.fill(x, y + 14, x + 16, y + 16, placementColor(slotIndex));
        if (menu.getDankInventory().isTagMatchingEnabled(slotIndex)) {
            graphics.fill(x + 12, y + 2, x + 16, y + 6, 0xFF39C6DD);
        }
    }

    private int extractionColor(int slotIndex) {
        return switch (menu.getDankInventory().getExtractionMode(slotIndex)) {
            case KEEP_ALL -> 0xFFB74040;
            case KEEP_1 -> 0xFFCC7A33;
            case KEEP_16 -> 0xFFD6AE3B;
            case KEEP_64 -> 0xFF6193C5;
            case KEEP_NONE -> 0xFF4FA96A;
            case CUSTOM -> 0xFFAF6FDB;
        };
    }

    private int placementColor(int slotIndex) {
        return switch (menu.getDankInventory().getPlacementMode(slotIndex)) {
            case KEEP_ALL -> 0xFF4C5B88;
            case KEEP_1 -> 0xFF4A8A73;
            case KEEP_16 -> 0xFF58A36C;
            case KEEP_64 -> 0xFF7AAE56;
            case KEEP_NONE -> 0xFF93B64E;
        };
    }

    private void renderInfoPanel(GuiGraphicsExtractor graphics) {
        Slot slot = getContextSlot();
        int panelX = infoPanelX();
        int panelY = infoPanelY();
        int textX = panelX + 14;
        int lineY = panelY + 12;
        int textWidth = INFO_TAB_WIDTH - 24;

        graphics.blit(RenderPipelines.GUI_TEXTURED, INFO_TAB_TEXTURE, panelX, panelY, INFO_TAB_U, INFO_TAB_V, INFO_TAB_WIDTH, INFO_TAB_HEIGHT, 256, 256);
        graphics.text(font, infoPanelTitle(), textX, lineY, 0xFFFFFFFF, false);
        lineY += 18;

        if (slot == null || !slot.hasItem() || !(slot instanceof SlotItemHandler)) {
            lineY = drawWrapped(graphics, Component.translatable("dn.hover_for_details.desc"), textX, lineY, textWidth, 0xFFC9D0DB);
            lineY += 4;
            lineY = drawWrapped(graphics, Component.translatable("dn.alt_click_set.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            lineY = drawWrapped(graphics, Component.translatable("dn.alt_arrow_move.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            lineY = drawWrapped(graphics, Component.translatable("dn.middle_click_custom_extract.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            lineY = drawWrapped(graphics, Component.translatable("dn.ctrl_click_change.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            lineY = drawWrapped(graphics, Component.translatable("dn.p_click_toggle.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            lineY += 4;
            drawWrapped(graphics, Component.translatable("dn.upgrades_hint.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            return;
        }

        int storageSlot = slot.index;
        ItemStack stack = slot.getItem();
        int itemX = textX;
        textX = panelX + 34;
        graphics.item(stack.copyWithCount(1), itemX, lineY);
        lineY = drawWrapped(graphics, Component.translatable("dn.slot.desc").append(" ").append(Integer.toString(storageSlot + 1)), textX, lineY, INFO_TAB_WIDTH - 40, 0xFFE8EDF5);
        lineY = drawWrapped(graphics, stack.getHoverName(), textX, lineY, INFO_TAB_WIDTH - 40, 0xFFFFFFFF);
        lineY += 4;
        lineY = drawWrapped(graphics, label("dn.count.desc", countText(stack)), textX - 20, lineY, textWidth, 0xFFE8EDF5);
        lineY = drawWrapped(graphics, label("dn.extract.desc", menu.getDankInventory().getExtractionTooltip(storageSlot)), textX - 20, lineY, textWidth, 0xFFE8EDF5);
        lineY = drawWrapped(graphics, label("dn.place.desc", menu.getDankInventory().getPlacementMode(storageSlot).tooltip()), textX - 20, lineY, textWidth, 0xFFE8EDF5);
        lineY = drawWrapped(graphics, label("dn.tag_matching.desc", tagText(storageSlot)), textX - 20, lineY, textWidth, 0xFFE8EDF5);
        renderUpgradeSummary(graphics, panelX, panelY);
    }

    private Slot getContextSlot() {
        Slot hovered = hoveredSlot;
        if (hovered != null && hovered.index >= 0 && hovered.index < menu.getStorageSlotCount()) {
            return hovered;
        }
        int selectedSlot = menu.getDankInventory().getSelectedSlot();
        if (selectedSlot >= 0 && selectedSlot < menu.getStorageSlotCount()) {
            return menu.slots.get(selectedSlot);
        }
        return null;
    }

    private Component infoPanelTitle() {
        String key = menu.getDankInventory().isFluidOnly()
                ? "item.deepnullreforged.damp_null_"
                : "item.deepnullreforged.deep_null_";
        return Component.translatable(key + menu.getTier().ordinalId());
    }

    private Component label(String key, Component value) {
        return Component.translatable(key).append(": ").append(value);
    }

    private Component countText(ItemStack stack) {
        if (menu.getDankInventory().supportsLocking() && menu.getDankInventory().isLocked()) {
            return Component.translatable("dn.infinite.desc");
        }
        return Component.literal(DeepNullConfig.showFullDeepNullCounts()
                ? DeepNullCountFormatter.formatExact(stack.getCount())
                : DeepNullCountFormatter.formatCompact(stack.getCount()));
    }

    private Component tagText(int slotIndex) {
        if (!DeepNullConfig.isTagMatchingEnabled()) {
            return Component.translatable("dn.disabled_by_config.desc");
        }
        if (!menu.getDankInventory().supportsTagMatching(slotIndex)) {
            return Component.translatable("dn.not_oredicted.desc");
        }
        return Component.translatable(menu.getDankInventory().isTagMatchingEnabled(slotIndex) ? "dn.enabled.desc" : "dn.disabled.desc");
    }

    private boolean isStorageItemSlot(Slot slot) {
        return slot instanceof SlotItemHandler && slot.index >= 0 && slot.index < menu.getStorageSlotCount();
    }

    private String compactSlotCountText(ItemStack stack) {
        if (menu.getDankInventory().supportsLocking() && menu.getDankInventory().isLocked()) {
            return "inf";
        }
        return DeepNullConfig.showFullDeepNullCounts()
                ? DeepNullCountFormatter.formatExact(stack.getCount())
                : DeepNullCountFormatter.formatCompact(stack.getCount());
    }

    private void renderStorageCountOverlay(GuiGraphicsExtractor graphics, Slot slot, String overlay) {
        if (customExtractionBox != null && customExtractionBox.visible) {
            Rect2i popup = customExtractionPopupBounds();
            Rect2i slotRect = new Rect2i(leftPos + slot.x, topPos + slot.y, 16, 16);
            if (intersects(popup, slotRect)) {
                return;
            }
        }
        float scale = overlay.length() <= 3 ? 0.75F : 0.6F;
        int textWidth = font.width(overlay);
        int drawX = slot.x + 16 - Math.round(textWidth * scale) - 1;
        int drawY = slot.y + 16 - Math.round(font.lineHeight * scale);
        graphics.pose().pushMatrix();
        graphics.pose().translate(drawX, drawY);
        graphics.pose().scale(scale, scale);
        graphics.text(font, overlay, 0, 0, 0xFFFFFFFF, true);
        graphics.pose().popMatrix();
    }

    private Component chargingLabel() {
        return Component.translatable(menu.isChargingEnabledDisplayed() ? "dn.charging_on.desc" : "dn.charging_off.desc");
    }

    private Component transferLockLabel() {
        return ClientUiText.transferOutputModeMessage(false, menu.getDankInventory().getTransferOutputMode());
    }

    public TransferOutputMode toggleTransferOutputMode() {
        TransferOutputMode next = menu.getDankInventory().cycleTransferOutputMode();
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuTransferModePayload(next.ordinal()));
        return next;
    }

    public TransferDirectionMode toggleTransferDirectionMode() {
        TransferDirectionMode next = menu.getDankInventory().cycleTransferDirectionMode();
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuTransferDirectionPayload(next.ordinal()));
        return next;
    }

    private int drawWrapped(GuiGraphicsExtractor graphics, Component component, int x, int y, int maxWidth, int color) {
        for (FormattedCharSequence line : font.split(component, maxWidth)) {
            graphics.text(font, line, x, y, color, false);
            y += INFO_PANEL_LINE_HEIGHT;
        }
        return y;
    }

    private void renderUpgradeSummary(GuiGraphicsExtractor graphics, int panelX, int panelY) {
        List<DeepNullUpgradeType> installed = new ArrayList<>();
        for (DeepNullUpgradeType type : DeepNullUpgradeType.values()) {
            if (menu.hasUpgrade(type)) {
                installed.add(type);
            }
        }
        if (installed.isEmpty()) {
            return;
        }

        int innerLeft = panelX + INFO_PANEL_PADDING;
        int innerWidth = INFO_TAB_WIDTH - (INFO_PANEL_PADDING * 2);
        int perRow = Math.max(1, (innerWidth + INFO_UPGRADE_ICON_GAP) / (INFO_UPGRADE_ICON_SIZE + INFO_UPGRADE_ICON_GAP));
        int rows = (installed.size() + perRow - 1) / perRow;
        int startY = panelY + INFO_TAB_HEIGHT - INFO_PANEL_PADDING - (rows * INFO_UPGRADE_ICON_SIZE) - ((rows - 1) * INFO_UPGRADE_ICON_GAP);

        for (int index = 0; index < installed.size(); index++) {
            int row = index / perRow;
            int column = index % perRow;
            int x = innerLeft + column * (INFO_UPGRADE_ICON_SIZE + INFO_UPGRADE_ICON_GAP);
            int y = startY + row * (INFO_UPGRADE_ICON_SIZE + INFO_UPGRADE_ICON_GAP);
            graphics.item(upgradeIcon(installed.get(index)), x, y);
        }
    }

    private ItemStack upgradeIcon(DeepNullUpgradeType type) {
        return switch (type) {
            case FILTER -> new ItemStack(ModItems.FILTER_UPGRADE.get());
            case FLUID -> new ItemStack(ModItems.FLUID_UPGRADE.get());
            case ENERGY -> new ItemStack(ModItems.ENERGY_UPGRADE.get());
            case DEEP_ENERGY -> new ItemStack(ModItems.DEEP_ENERGY_UPGRADE.get());
            case AUTO_FEEDING -> new ItemStack(ModItems.AUTO_FEEDING_UPGRADE.get());
            case AUTO_SMELTING -> new ItemStack(ModItems.AUTO_SMELTING_UPGRADE.get());
            case BASIC_COMPRESSION -> new ItemStack(ModItems.BASIC_COMPRESSION_UPGRADE.get());
            case ADVANCED_COMPRESSION -> new ItemStack(ModItems.ADVANCED_COMPRESSION_UPGRADE.get());
            case STONEWORKS -> new ItemStack(ModItems.STONEWORKS_UPGRADE.get());
            case STONE_GENERATOR -> new ItemStack(ModItems.STONE_GENERATOR_UPGRADE.get());
            case OBSIDIAN_GENERATOR -> new ItemStack(ModItems.OBSIDIAN_GENERATOR_UPGRADE.get());
            case SPONGE -> new ItemStack(ModItems.SPONGE_UPGRADE.get());
            case GAS -> new ItemStack(ModItems.GAS_UPGRADE.get());
            case ENDER -> new ItemStack(ModItems.ENDER_UPGRADE.get());
        };
    }

    private void renderIconButtons(GuiGraphicsExtractor graphics) {
        int x = leftPos + baseImageWidth - 1;
        renderIconButton(graphics, INFO_BUTTON_TEXTURE, x, topPos + 38, INFO_BUTTON_V);
        renderIconButton(graphics, menu.getDankInventory().getTransferOutputMode().isLocked() ? LOCK_BUTTON_ON_TEXTURE : LOCK_BUTTON_OFF_TEXTURE, x, topPos + 59, LOCK_BUTTON_V);
        renderIconButton(graphics, UPGRADE_BUTTON_TEXTURE, x, topPos + 80, UPGRADE_BUTTON_V);
        if (menu.hasEnergyUpgrade()) {
            renderIconButton(
                    graphics,
                    menu.isChargingEnabledDisplayed() ? CHARGING_BUTTON_ON_TEXTURE : CHARGING_BUTTON_OFF_TEXTURE,
                    x,
                    topPos + 101,
                    CHARGING_BUTTON_V
            );
        }
        if (menu.hasUpgrade(DeepNullUpgradeType.STONEWORKS)) {
            renderIconButton(graphics, STONEWORKS_BUTTON_TEXTURE, x, stoneworksButtonY(), STONEWORKS_BUTTON_V);
        }
    }

    private void renderIconButton(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int v) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, TAB_BUTTON_U, v, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT, 256, 256);
    }

    private void renderIconButtonTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = leftPos + baseImageWidth - 1;
        if (isWithin(mouseX, mouseY, x, topPos + 38, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            graphics.setTooltipForNextFrame(font, Component.translatable("dn.info.desc"), mouseX, mouseY);
        } else if (isWithin(mouseX, mouseY, x, topPos + 59, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            graphics.setTooltipForNextFrame(font, transferLockLabel(), mouseX, mouseY);
        } else if (isWithin(mouseX, mouseY, x, topPos + 80, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            graphics.setTooltipForNextFrame(font, Component.translatable("dn.upgrades_screen.desc"), mouseX, mouseY);
        } else if (menu.hasEnergyUpgrade() && isWithin(mouseX, mouseY, x, topPos + 101, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            graphics.setTooltipForNextFrame(font, chargingLabel(), mouseX, mouseY);
        } else if (menu.hasUpgrade(DeepNullUpgradeType.STONEWORKS) && isWithin(mouseX, mouseY, x, stoneworksButtonY(), TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            graphics.setTooltipForNextFrame(font, Component.translatable("upgrade.stoneworks_upgrade.installed"), mouseX, mouseY);
        }
    }

    private boolean handleIconButtonClick(MouseButtonEvent event) {
        if (event.button() != 0) {
            return false;
        }
        int x = leftPos + baseImageWidth - 1;
        if (isWithin(event.x(), event.y(), x, topPos + 38, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            infoPanelOpen = !infoPanelOpen;
            if (infoPanelOpen) {
                stoneworksPanelOpen = false;
            }
            return true;
        }
        if (isWithin(event.x(), event.y(), x, topPos + 59, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            toggleTransferOutputMode();
            return true;
        }
        if (isWithin(event.x(), event.y(), x, topPos + 80, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            ClientPacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.UPGRADES.ordinal()));
            return true;
        }
        if (menu.hasEnergyUpgrade() && isWithin(event.x(), event.y(), x, topPos + 101, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            boolean next = !menu.isChargingEnabledDisplayed();
            menu.getDankInventory().setChargingEnabled(next);
            ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuChargingPayload(next));
            return true;
        }
        if (menu.hasUpgrade(DeepNullUpgradeType.STONEWORKS) && isWithin(event.x(), event.y(), x, stoneworksButtonY(), TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            stoneworksPanelOpen = !stoneworksPanelOpen;
            if (stoneworksPanelOpen) {
                infoPanelOpen = false;
                syncStoneworksAmountBox();
            } else if (stoneworksAmountBox != null) {
                stoneworksAmountBox.setFocused(false);
            }
            updateStoneworksAmountBox();
            return true;
        }
        return false;
    }

    private int infoPanelX() {
        int rightSide = leftPos + baseImageWidth + TAB_BUTTON_WIDTH + 4;
        if (rightSide + INFO_TAB_WIDTH <= width - 4) {
            return rightSide;
        }
        return Math.max(4, leftPos - INFO_TAB_WIDTH - TAB_BUTTON_WIDTH - 4);
    }

    private int infoPanelY() {
        return topPos + 4;
    }

    private int stoneworksButtonY() {
        return topPos + (menu.hasEnergyUpgrade() ? 122 : 101);
    }

    public Rect2i getInfoPanelArea() {
        if (!infoPanelOpen && !stoneworksPanelOpen) {
            return null;
        }
        return new Rect2i(infoPanelX(), infoPanelY(), INFO_TAB_WIDTH, INFO_TAB_HEIGHT);
    }

    private void renderStoneworksPanel(GuiGraphicsExtractor graphics) {
        int panelX = infoPanelX();
        int panelY = infoPanelY();
        int textX = panelX + 14;

        graphics.blit(RenderPipelines.GUI_TEXTURED, INFO_TAB_TEXTURE, panelX, panelY, INFO_TAB_U, INFO_TAB_V, INFO_TAB_WIDTH, INFO_TAB_HEIGHT, 256, 256);
        graphics.text(font, Component.translatable("upgrade.stoneworks_upgrade.installed"), textX, panelY + 12, 0xFFFFFFFF, false);
        graphics.text(font, Component.translatable("dn.stoneworks_amount.desc"), textX, panelY + 30, 0xFFC9D0DB, false);
        renderStoneworksAmountEditor(graphics);
        graphics.text(font, Component.translatable("dn.stoneworks_outputs.desc"), textX, panelY + 64, 0xFFC9D0DB, false);

        List<StoneworksMaterial> materials = menu.getVisibleStoneworksMaterials();
        int startX = textX;
        int startY = panelY + STONEWORKS_GRID_START_Y;
        for (int index = 0; index < materials.size(); index++) {
            StoneworksMaterial material = materials.get(index);
            int x = startX + (index % STONEWORKS_GRID_COLUMNS) * STONEWORKS_GRID_SPACING;
            int y = startY + (index / STONEWORKS_GRID_COLUMNS) * STONEWORKS_GRID_SPACING;
            ItemStack displayStack = menu.getStoneworksDisplayStack(material);
            if (!displayStack.isEmpty()) {
                graphics.item(displayStack, x, y);
            }
            if (menu.isStoneworksMonitoring(material)) {
                graphics.outline(x - 1, y - 1, 18, 18, 0xFFE7F2FF);
            } else {
                graphics.fill(x, y, x + 16, y + 16, 0xAA000000);
            }
        }
    }

    private void renderStoneworksTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        StoneworksMaterial material = stoneworksMaterialAt(mouseX, mouseY);
        if (material == null) {
            return;
        }

        ItemStack displayStack = menu.getStoneworksDisplayStack(material);
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(displayStack.isEmpty() ? Component.literal(material.name()) : displayStack.getHoverName());
        tooltip.add(Component.translatable(menu.isStoneworksMonitoring(material) ? "dn.monitoring_on.desc" : "dn.monitoring_off.desc"));
        graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
    }

    private StoneworksMaterial stoneworksMaterialAt(double mouseX, double mouseY) {
        if (!stoneworksPanelOpen) {
            return null;
        }

        int panelX = infoPanelX();
        int panelY = infoPanelY();
        int startX = panelX + 14;
        int startY = panelY + STONEWORKS_GRID_START_Y;
        List<StoneworksMaterial> materials = menu.getVisibleStoneworksMaterials();
        for (int index = 0; index < materials.size(); index++) {
            int x = startX + (index % STONEWORKS_GRID_COLUMNS) * STONEWORKS_GRID_SPACING;
            int y = startY + (index / STONEWORKS_GRID_COLUMNS) * STONEWORKS_GRID_SPACING;
            if (isWithin(mouseX, mouseY, x, y, 16, 16)) {
                return materials.get(index);
            }
        }
        return null;
    }

    private void updateStoneworksAmountBox() {
        if (stoneworksAmountBox == null) {
            return;
        }
        stoneworksAmountBox.visible = stoneworksPanelOpen && menu.hasUpgrade(DeepNullUpgradeType.STONEWORKS);
        stoneworksAmountBox.active = stoneworksAmountBox.visible;
        Rect2i bounds = stoneworksDialogBounds();
        stoneworksAmountBox.setX(bounds.getX() + DIALOGUE_TEXT_PADDING_X);
        stoneworksAmountBox.setY(bounds.getY() + DIALOGUE_TEXT_PADDING_Y + 3);
        if (!stoneworksAmountBox.visible) {
            stoneworksAmountBox.setFocused(false);
            return;
        }
        if (!stoneworksAmountBox.isFocused() && stoneworksAmountBox.getValue().isEmpty()) {
            syncStoneworksAmountBox();
        }
    }

    private void syncStoneworksAmountBox() {
        if (stoneworksAmountBox == null) {
            return;
        }
        String value = Integer.toString(menu.getStoneworksTargetStacks());
        if (!value.equals(stoneworksAmountBox.getValue())) {
            stoneworksAmountBox.setValue(value);
        }
    }

    private void onStoneworksAmountChanged(String value) {
        if (stoneworksAmountBox == null || !stoneworksPanelOpen || value.isEmpty()) {
            return;
        }
        int parsed;
        try {
            parsed = Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return;
        }
        if (menu.setStoneworksTargetStacks(parsed)) {
            ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuStoneworksAmountPayload(parsed));
        }
    }

    private void openCustomExtractionEditor(int slot, int mouseX, int mouseY, boolean applyAll) {
        if (customExtractionBox == null || slot < 0 || slot >= menu.getStorageSlotCount()) {
            return;
        }
        ItemStack stack = menu.getDankInventory().getStackInSlot(slot);
        if (stack.isEmpty()) {
            return;
        }
        customExtractionSlot = slot;
        customExtractionAnchorX = mouseX;
        customExtractionAnchorY = mouseY;
        customExtractionApplyAll = applyAll;
        int currentMinimum = currentCustomExtractionEditorValue(slot, stack);
        customExtractionInitialValue = currentMinimum;
        customExtractionBox.setValue(Integer.toString(currentMinimum));
        updateCustomExtractionBox();
        customExtractionBox.visible = true;
        customExtractionBox.active = true;
        customExtractionBox.setFocused(true);
        customExtractionBox.setCursorPosition(0);
        customExtractionBox.setHighlightPos(customExtractionBox.getValue().length());
        if (stoneworksAmountBox != null) {
            stoneworksAmountBox.setFocused(false);
        }
    }

    private void closeCustomExtractionEditor(boolean apply) {
        if (customExtractionBox == null) {
            return;
        }
        if (apply && customExtractionSlot >= 0 && customExtractionSlot < menu.getStorageSlotCount()) {
            ItemStack stack = menu.getDankInventory().getStackInSlot(customExtractionSlot);
            if (!stack.isEmpty() && !customExtractionBox.getValue().isEmpty()) {
                try {
                    int amount = Integer.parseInt(customExtractionBox.getValue());
                    if (amount != customExtractionInitialValue) {
                        ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuCustomExtractionPayload(customExtractionSlot, amount, customExtractionApplyAll));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        customExtractionSlot = -1;
        customExtractionAnchorX = 0;
        customExtractionAnchorY = 0;
        customExtractionInitialValue = 0;
        customExtractionApplyAll = false;
        customExtractionBox.setFocused(false);
        customExtractionBox.visible = false;
        customExtractionBox.active = false;
    }

    private void updateCustomExtractionBox() {
        if (customExtractionBox == null) {
            return;
        }
        if (customExtractionSlot < 0 || customExtractionSlot >= menu.getStorageSlotCount()) {
            customExtractionBox.visible = false;
            customExtractionBox.active = false;
            return;
        }
        ItemStack stack = menu.getDankInventory().getStackInSlot(customExtractionSlot);
        if (stack.isEmpty()) {
            closeCustomExtractionEditor(false);
            return;
        }
        Rect2i bounds = customExtractionDialogBounds();
        customExtractionBox.setX(bounds.getX() + DIALOGUE_TEXT_PADDING_X + 2);
        customExtractionBox.setY(bounds.getY() + DIALOGUE_TEXT_PADDING_Y + 2);
        customExtractionBox.visible = true;
        customExtractionBox.active = true;
        if (!customExtractionBox.isFocused()) {
            customExtractionBox.setValue(Integer.toString(currentCustomExtractionEditorValue(customExtractionSlot, stack)));
        }
    }

    private int currentCustomExtractionEditorValue(int slot, ItemStack stack) {
        return switch (menu.getDankInventory().getExtractionMode(slot)) {
            case KEEP_ALL -> Math.min(stack.getCount(), menu.getDankInventory().getSlotLimit(slot));
            default -> menu.getDankInventory().getExtractionMinimum(slot);
        };
    }

    private void renderStoneworksAmountEditor(GuiGraphicsExtractor graphics) {
        if (stoneworksAmountBox == null || !stoneworksAmountBox.visible) {
            return;
        }
        Rect2i bounds = stoneworksDialogBounds();
        renderDialogue(graphics, bounds, stoneworksAmountBox.isFocused());
        renderStepButton(graphics, stoneworksMinusButtonBounds(), MINUS_BUTTON_TEXTURE, MINUS_BUTTON_U, MINUS_BUTTON_V);
        renderStepButton(graphics, stoneworksPlusButtonBounds(), PLUS_BUTTON_TEXTURE, PLUS_BUTTON_U, PLUS_BUTTON_V);
    }

    private void renderCustomExtractionEditor(GuiGraphicsExtractor graphics) {
        if (customExtractionBox == null || !customExtractionBox.visible) {
            return;
        }
        Rect2i popup = customExtractionPopupBounds();
        Rect2i dialog = customExtractionDialogBounds();
        graphics.blit(RenderPipelines.GUI_TEXTURED, EXTRACT_DIALOG_TEXTURE, popup.getX(), popup.getY(), EXTRACT_DIALOG_U, EXTRACT_DIALOG_V, EXTRACT_DIALOG_WIDTH, EXTRACT_DIALOG_HEIGHT, EXTRACT_DIALOG_TEXTURE_SIZE, EXTRACT_DIALOG_TEXTURE_SIZE);
        graphics.text(font, Component.translatable("dn.custom_extract_limit.desc"), popup.getX() + EXTRACT_DIALOG_TITLE_X, popup.getY() + EXTRACT_DIALOG_TITLE_Y, 0xFFFFFFFF, false);
        renderDialogue(graphics, dialog, customExtractionBox.isFocused());
        renderStepButton(graphics, customExtractionMinusButtonBounds(), MINUS_BUTTON_TEXTURE, MINUS_BUTTON_U, MINUS_BUTTON_V);
        renderStepButton(graphics, customExtractionPlusButtonBounds(), PLUS_BUTTON_TEXTURE, PLUS_BUTTON_U, PLUS_BUTTON_V);
    }

    private void renderDialogue(GuiGraphicsExtractor graphics, Rect2i bounds, boolean active) {
        Identifier texture = active ? DIALOGUE_ACTIVE_TEXTURE : DIALOGUE_INACTIVE_TEXTURE;
        int u = active ? DIALOGUE_ACTIVE_U : DIALOGUE_INACTIVE_U;
        int v = active ? DIALOGUE_ACTIVE_V : DIALOGUE_INACTIVE_V;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, bounds.getX(), bounds.getY(), u, v, DIALOGUE_WIDTH, DIALOGUE_HEIGHT, DIALOGUE_TEXTURE_SIZE, DIALOGUE_TEXTURE_SIZE);
    }

    private void renderStepButton(GuiGraphicsExtractor graphics, Rect2i bounds, Identifier texture, int u, int v) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, bounds.getX(), bounds.getY(), u, v, STEP_BUTTON_SIZE, STEP_BUTTON_SIZE, STEP_BUTTON_TEXTURE_SIZE, STEP_BUTTON_TEXTURE_SIZE);
    }

    private boolean handleStoneworksAmountClick(MouseButtonEvent event) {
        if (!ClientModEvents.isPrimaryGuiButton(event.button()) || stoneworksAmountBox == null || !stoneworksAmountBox.visible) {
            return false;
        }
        if (isWithin(stoneworksMinusButtonBounds(), event.x(), event.y())) {
            adjustStoneworksAmount(-stoneworksDialogStep());
            stoneworksAmountBox.setFocused(true);
            return true;
        }
        if (isWithin(stoneworksPlusButtonBounds(), event.x(), event.y())) {
            adjustStoneworksAmount(stoneworksDialogStep());
            stoneworksAmountBox.setFocused(true);
            return true;
        }
        if (isWithin(stoneworksDialogBounds(), event.x(), event.y())) {
            stoneworksAmountBox.setFocused(true);
            return stoneworksAmountBox.mouseClicked(event, false);
        }
        return false;
    }

    private boolean handleCustomExtractionEditorClick(MouseButtonEvent event) {
        if (!ClientModEvents.isPrimaryGuiButton(event.button()) || customExtractionBox == null || !customExtractionBox.visible) {
            return false;
        }
        if (isWithin(customExtractionMinusButtonBounds(), event.x(), event.y())) {
            adjustCustomExtractionAmount(-customExtractionDialogStep());
            customExtractionBox.setFocused(true);
            return true;
        }
        if (isWithin(customExtractionPlusButtonBounds(), event.x(), event.y())) {
            adjustCustomExtractionAmount(customExtractionDialogStep());
            customExtractionBox.setFocused(true);
            return true;
        }
        if (isWithin(customExtractionDialogBounds(), event.x(), event.y())) {
            customExtractionBox.setFocused(true);
            return customExtractionBox.mouseClicked(event, false);
        }
        return false;
    }

    private void adjustStoneworksAmount(int delta) {
        int current = parseNumericBox(stoneworksAmountBox, menu.getStoneworksTargetStacks());
        long unclamped = (long) current + delta;
        int next = (int) Math.max(0L, Math.min((long) Integer.MAX_VALUE, unclamped));
        if (next != current) {
            stoneworksAmountBox.setValue(Integer.toString(next));
        }
    }

    private void adjustCustomExtractionAmount(int delta) {
        if (customExtractionSlot < 0 || customExtractionSlot >= menu.getStorageSlotCount()) {
            return;
        }
        ItemStack stack = menu.getDankInventory().getStackInSlot(customExtractionSlot);
        if (stack.isEmpty()) {
            return;
        }
        int current = parseNumericBox(customExtractionBox, currentCustomExtractionEditorValue(customExtractionSlot, stack));
        long unclamped = (long) current + delta;
        int next = (int) Math.max(0L, Math.min((long) menu.getDankInventory().getSlotLimit(customExtractionSlot), unclamped));
        customExtractionBox.setValue(Integer.toString(next));
    }

    private int parseNumericBox(EditBox box, int fallback) {
        if (box == null || box.getValue().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(box.getValue());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private int customExtractionDialogStep() {
        return isShiftDown() ? 10 : 1;
    }

    boolean handleBlockedMiddleClick(double mouseX, double mouseY) {
        if (!isWithin(mouseX, mouseY, leftPos, topPos, baseImageWidth, imageHeight)) {
            return false;
        }
        if (customExtractionBox != null && customExtractionBox.visible) {
            return true;
        }
        Slot slot = findSlotAt(mouseX, mouseY);
        if (slot instanceof DeepNullMenu.StorageSlot && slot.hasItem()) {
            openCustomExtractionEditor(slot.index, (int) Math.round(mouseX), (int) Math.round(mouseY), ScreenActions.hasControlDown());
        }
        return true;
    }

    boolean handleBlockedMiddleRelease(double mouseX, double mouseY) {
        return (customExtractionBox != null && customExtractionBox.visible && isWithin(customExtractionEditorBounds(), mouseX, mouseY))
                || isWithin(mouseX, mouseY, leftPos, topPos, baseImageWidth, imageHeight);
    }

    private int stoneworksDialogStep() {
        return isShiftDown() ? 10 : 1;
    }

    private Rect2i stoneworksDialogBounds() {
        int y = infoPanelY() + 39;
        int x = infoPanelX() + 14 + STEP_BUTTON_SIZE + DIALOGUE_BUTTON_GAP;
        return new Rect2i(x, y, DIALOGUE_WIDTH, DIALOGUE_HEIGHT);
    }

    private Rect2i stoneworksMinusButtonBounds() {
        Rect2i dialog = stoneworksDialogBounds();
        return new Rect2i(dialog.getX() - DIALOGUE_BUTTON_GAP - STEP_BUTTON_SIZE, dialog.getY(), STEP_BUTTON_SIZE, STEP_BUTTON_SIZE);
    }

    private Rect2i stoneworksPlusButtonBounds() {
        Rect2i dialog = stoneworksDialogBounds();
        return new Rect2i(dialog.getX() + dialog.getWidth() + DIALOGUE_BUTTON_GAP, dialog.getY(), STEP_BUTTON_SIZE, STEP_BUTTON_SIZE);
    }

    private Rect2i customExtractionDialogBounds() {
        Rect2i popup = customExtractionPopupBounds();
        return new Rect2i(
                popup.getX() + ((EXTRACT_DIALOG_WIDTH - DIALOGUE_WIDTH) / 2),
                popup.getY() + EXTRACT_DIALOG_CONTROLS_Y,
                DIALOGUE_WIDTH,
                DIALOGUE_HEIGHT
        );
    }

    private Rect2i customExtractionPopupBounds() {
        if (customExtractionSlot < 0 || customExtractionSlot >= menu.getStorageSlotCount()) {
            return new Rect2i(0, 0, 0, 0);
        }
        int targetX = customExtractionAnchorX - (EXTRACT_DIALOG_WIDTH / 2);
        int minX = leftPos + 4;
        int maxX = leftPos + baseImageWidth - EXTRACT_DIALOG_WIDTH - 4;
        int x = Math.max(minX, Math.min(maxX, targetX));
        int y = Math.max(topPos + 4, customExtractionAnchorY - EXTRACT_DIALOG_HEIGHT - 12);
        return new Rect2i(x, y, EXTRACT_DIALOG_WIDTH, EXTRACT_DIALOG_HEIGHT);
    }

    private Rect2i customExtractionMinusButtonBounds() {
        Rect2i dialog = customExtractionDialogBounds();
        return new Rect2i(dialog.getX() - DIALOGUE_BUTTON_GAP - STEP_BUTTON_SIZE, dialog.getY(), STEP_BUTTON_SIZE, STEP_BUTTON_SIZE);
    }

    private Rect2i customExtractionPlusButtonBounds() {
        Rect2i dialog = customExtractionDialogBounds();
        return new Rect2i(dialog.getX() + dialog.getWidth() + DIALOGUE_BUTTON_GAP, dialog.getY(), STEP_BUTTON_SIZE, STEP_BUTTON_SIZE);
    }

    private Rect2i customExtractionEditorBounds() {
        return customExtractionPopupBounds();
    }

    private static boolean isWithin(Rect2i bounds, double mouseX, double mouseY) {
        return bounds != null && isWithin(mouseX, mouseY, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    private static boolean intersects(Rect2i a, Rect2i b) {
        return a.getX() < b.getX() + b.getWidth()
                && a.getX() + a.getWidth() > b.getX()
                && a.getY() < b.getY() + b.getHeight()
                && a.getY() + a.getHeight() > b.getY();
    }

    private int getReorderSourceSlot() {
        if (latchedReorderSlot >= 0 && latchedReorderSlot < menu.getStorageSlotCount()) {
            return latchedReorderSlot;
        }
        Slot hovered = hoveredSlot;
        if (hovered instanceof SlotItemHandler && hovered.index < menu.getStorageSlotCount()) {
            latchedReorderSlot = hovered.index;
            return hovered.index;
        }
        int selectedSlot = menu.getDankInventory().getSelectedSlot();
        if (selectedSlot >= 0 && selectedSlot < menu.getStorageSlotCount()) {
            latchedReorderSlot = selectedSlot;
            return selectedSlot;
        }
        return -1;
    }

    private void latchReorderSlot(int slot) {
        latchedReorderSlot = slot >= 0 && slot < menu.getStorageSlotCount() ? slot : -1;
    }

    private int getReorderTargetSlot(int sourceSlot, int keyCode) {
        if (sourceSlot < 0) {
            return -1;
        }

        int row = sourceSlot / 9;
        int column = sourceSlot % 9;
        return switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> column > 0 ? sourceSlot - 1 : -1;
            case GLFW.GLFW_KEY_RIGHT -> column < 8 ? sourceSlot + 1 : -1;
            case GLFW.GLFW_KEY_UP -> row > 0 ? sourceSlot - 9 : -1;
            case GLFW.GLFW_KEY_DOWN -> row < menu.getTier().rows() - 1 ? sourceSlot + 9 : -1;
            default -> -1;
        };
    }

    private static boolean isWithin(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int imageWidthFor(DeepNullMenu menu) {
        return menu.hasEnergyUpgrade() && menu.getTier().supportsEnergyUpgrade() ? ENERGY_IMAGE_WIDTH : BASE_IMAGE_WIDTH;
    }

    private static int imageHeightFor(DeepNullMenu menu) {
        return 141 + Math.max(0, menu.getTier().rows() - 1) * 21;
    }

    private static Identifier integratedEnergyTexture(DeepNullMenu menu) {
        return switch (menu.getTier()) {
            case DIAMOND -> DeepNullReforged.id("textures/gui/deepnullscreen4_energy.png");
            case EMERALD -> DeepNullReforged.id("textures/gui/deepnullscreen5_energy.png");
            case CREATIVE -> DeepNullReforged.id("textures/gui/deepnullscreen6_energy.png");
            default -> menu.getTier().guiTexture();
        };
    }

    private Identifier integratedEnergyFillTexture() {
        return switch (menu.getTier()) {
            case DIAMOND -> DeepNullReforged.id("textures/gui/deepnullscreen4_energy_fill.png");
            case EMERALD -> DeepNullReforged.id("textures/gui/deepnullscreen5_energy_fill.png");
            case CREATIVE -> DeepNullReforged.id("textures/gui/deepnullscreen6_energy_fill.png");
            default -> DeepNullReforged.id("textures/gui/deepnullscreen4_energy_fill.png");
        };
    }

    private static final class ScreenActions {
        private ScreenActions() {
        }

        private static boolean handle(DeepNullScreen screen, int storageSlot, int button) {
            boolean primary = ClientModEvents.isPrimaryGuiButton(button);
            boolean secondary = ClientModEvents.isSecondaryGuiButton(button);
            if (!primary && !secondary) {
                return false;
            }

            if (hasModeKey(GLFW.GLFW_KEY_O)) {
                if (!screen.menu.getDankInventory().supportsTagMatching(storageSlot)) {
                    return false;
                }
                screen.menu.getDankInventory().toggleTagMatching(storageSlot);
                ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuSlotActionPayload(
                        storageSlot,
                        DeepNullPayloads.MenuSlotAction.TOGGLE_TAG_MATCHING.ordinal()
                ));
                return true;
            }

            if (hasModeKey(GLFW.GLFW_KEY_P)) {
                screen.menu.getDankInventory().cyclePlacementMode(storageSlot, primary);
                ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuSlotActionPayload(
                        storageSlot,
                        (primary ? DeepNullPayloads.MenuSlotAction.CYCLE_PLACEMENT_FORWARD : DeepNullPayloads.MenuSlotAction.CYCLE_PLACEMENT_BACKWARD).ordinal()
                ));
                return true;
            }

            if (hasControlDown()) {
                screen.menu.getDankInventory().cycleExtractionMode(storageSlot, primary);
                ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuSlotActionPayload(
                        storageSlot,
                        (primary ? DeepNullPayloads.MenuSlotAction.CYCLE_EXTRACTION_FORWARD : DeepNullPayloads.MenuSlotAction.CYCLE_EXTRACTION_BACKWARD).ordinal()
                ));
                return true;
            }

            if (hasAltDown()) {
                screen.menu.getDankInventory().setSelectedSlot(storageSlot);
                screen.latchReorderSlot(storageSlot);
                ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuSlotActionPayload(
                        storageSlot,
                        DeepNullPayloads.MenuSlotAction.SELECT.ordinal()
                ));
                return true;
            }

            return false;
        }

        private static boolean hasModeKey(int keyCode) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), keyCode);
        }

        private static boolean hasControlDown() {
            return Minecraft.getInstance().hasControlDown();
        }

        private static boolean hasAltDown() {
            return Minecraft.getInstance().hasAltDown();
        }
    }

    private boolean isShiftDown() {
        return minecraft != null && minecraft.hasShiftDown();
    }

    private boolean isAltDown() {
        return minecraft != null && minecraft.hasAltDown();
    }
}
