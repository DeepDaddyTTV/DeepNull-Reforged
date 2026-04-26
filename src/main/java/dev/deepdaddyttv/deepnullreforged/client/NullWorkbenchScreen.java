package dev.deepdaddyttv.deepnullreforged.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.StyleGlassVariant;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu;
import dev.deepdaddyttv.deepnullreforged.network.NullWorkbenchPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

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
    private static final int TAB_START_X = 16;
    private static final int TAB_GAP = 2;
    private static final int TAB_Y_OFFSET = 13;
    private WorkbenchTab activeTab = WorkbenchTab.CRAFT;
    private StyleTarget selectedStyleTarget = StyleTarget.FRAME;
    private Button backupButton;
    private Button restoreButton;
    private EditBox frameColorBox;
    private EditBox glassColorBox;
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
        } else {
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
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x + 1, y + 1, 200.0F);
        poseStack.scale(2.0F, 2.0F, 1.0F);
        graphics.renderItem(stack, 0, 0);
        poseStack.popPose();
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

    private boolean clickTab(double mouseX, double mouseY, int x, int y, WorkbenchTab target) {
        if (mouseX < x || mouseX >= x + TAB_WIDTH || mouseY < y || mouseY >= y + TAB_HEIGHT) {
            return false;
        }
        if (activeTab != target) {
            activeTab = target;
            refreshStyleFields();
            updateWidgetVisibility();
            updateMachineSlotLayout();
        }
        return true;
    }

    private void updateWidgetVisibility() {
        boolean sync = activeTab == WorkbenchTab.SYNC;
        boolean style = activeTab == WorkbenchTab.STYLE && hasStyledNull();
        backupButton.visible = sync;
        backupButton.active = sync && menu.canBackup();
        restoreButton.visible = sync;
        restoreButton.active = sync && menu.canRestore();
        frameColorBox.visible = style;
        frameColorBox.setEditable(style);
        glassColorBox.visible = style;
        glassColorBox.setEditable(style);
    }

    private void refreshStyleFields() {
        ItemStack stack = menu.getNullStack();
        if (!(stack.getItem() instanceof DeepNullItem deepNullItem) || minecraft == null || minecraft.level == null) {
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
        if (!(stack.getItem() instanceof DeepNullItem deepNullItem) || minecraft == null || minecraft.level == null) {
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
        return !menu.getNullStack().isEmpty();
    }

    private static boolean insideAbsolute(double mouseX, double mouseY, int areaX, int areaY, int areaWidth, int areaHeight) {
        return mouseX >= areaX && mouseX < areaX + areaWidth && mouseY >= areaY && mouseY < areaY + areaHeight;
    }

    private enum WorkbenchTab {
        CRAFT,
        SYNC,
        STYLE
    }

    private enum StyleTarget {
        FRAME,
        GLASS
    }
}
