package dev.deepdaddyttv.deepnullreforged.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class DeepNullScreen extends AbstractContainerScreen<DeepNullMenu> {
    private static final int BASE_IMAGE_WIDTH = 201;
    private static final int INFO_PANEL_GAP = 8;
    private static final int INFO_PANEL_WIDTH = 142;
    private static final int INFO_PANEL_MIN_HEIGHT = 96;
    private static final int INFO_PANEL_PADDING = 6;
    private static final int INFO_PANEL_LINE_HEIGHT = 10;

    private final ResourceLocation backgroundTexture;
    private int latchedReorderSlot = -1;
    private Button lockButton;

    public DeepNullScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = BASE_IMAGE_WIDTH + INFO_PANEL_GAP + INFO_PANEL_WIDTH;
        this.imageHeight = 141 + Math.max(0, menu.getTier().rows() - 1) * 21;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = this.imageHeight - 103;
        this.titleLabelX = 7;
        this.titleLabelY = 6;
        this.backgroundTexture = menu.getTier().guiTexture();
    }

    @Override
    protected void init() {
        super.init();
        latchedReorderSlot = -1;
        if (menu.getDankInventory().supportsLocking()) {
            lockButton = Button.builder(lockLabel(), button -> {
                        boolean next = !menu.getDankInventory().isLocked();
                        menu.getDankInventory().setLocked(next);
                        button.setMessage(lockLabel());
                        PacketDistributor.sendToServer(new DeepNullPayloads.MenuLockPayload(next));
                    })
                    .bounds(leftPos + BASE_IMAGE_WIDTH - 54, topPos - 20, 50, 18)
                    .build();
            addRenderableWidget(lockButton);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(backgroundTexture, leftPos, topPos, 0.0F, 0.0F, BASE_IMAGE_WIDTH, imageHeight, 256, 256);

        int selectedSlot = menu.getDankInventory().getSelectedSlot();
        if (selectedSlot >= 0 && selectedSlot < menu.getStorageSlotCount()) {
            Slot slot = menu.slots.get(selectedSlot);
            guiGraphics.renderOutline(leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18, menu.getTier().ordinalId() == 0 ? 0xFFE7B623 : 0xFFD8DCE5);
        }

        for (int slotIndex = 0; slotIndex < menu.getStorageSlotCount(); slotIndex++) {
            Slot slot = menu.slots.get(slotIndex);
            if (!slot.hasItem()) {
                continue;
            }
            drawModeMarkers(guiGraphics, slotIndex, leftPos + slot.x, topPos + slot.y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        if (lockButton != null) {
            lockButton.setMessage(lockLabel());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderInfoPanel(guiGraphics);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Slot slot = getSlotUnderMouse();
        if (slot instanceof SlotItemHandler && slot.hasItem()) {
            int storageSlot = slot.index;
            if (ScreenActions.handle(this, storageSlot, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasAltDown()) {
            int sourceSlot = getReorderSourceSlot();
            int targetSlot = getReorderTargetSlot(sourceSlot, keyCode);
            if (sourceSlot >= 0 && targetSlot >= 0 && menu.moveStorageSlot(sourceSlot, targetSlot)) {
                latchedReorderSlot = targetSlot;
                PacketDistributor.sendToServer(new DeepNullPayloads.MenuReorderPayload(sourceSlot, targetSlot));
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT || !Screen.hasAltDown()) {
            latchedReorderSlot = -1;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!Screen.hasAltDown()) {
            latchedReorderSlot = -1;
        }
    }

    private void drawModeMarkers(GuiGraphics guiGraphics, int slotIndex, int x, int y) {
        guiGraphics.fill(x, y, x + 16, y + 2, extractionColor(slotIndex));
        guiGraphics.fill(x, y + 14, x + 16, y + 16, placementColor(slotIndex));
        if (menu.getDankInventory().isTagMatchingEnabled(slotIndex)) {
            guiGraphics.fill(x + 12, y + 2, x + 16, y + 6, 0xFF39C6DD);
        }
    }

    private int extractionColor(int slotIndex) {
        return switch (menu.getDankInventory().getExtractionMode(slotIndex)) {
            case KEEP_ALL -> 0xFFB74040;
            case KEEP_1 -> 0xFFCC7A33;
            case KEEP_16 -> 0xFFD6AE3B;
            case KEEP_64 -> 0xFF6193C5;
            case KEEP_NONE -> 0xFF4FA96A;
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

    private void renderInfoPanel(GuiGraphics guiGraphics) {
        Slot slot = getContextSlot();
        int panelX = leftPos + BASE_IMAGE_WIDTH + INFO_PANEL_GAP;
        int panelY = topPos + 8;
        int panelHeight = Math.max(INFO_PANEL_MIN_HEIGHT, measurePanelHeight(slot));

        guiGraphics.fill(panelX, panelY, panelX + INFO_PANEL_WIDTH, panelY + panelHeight, 0xD0121720);
        guiGraphics.renderOutline(panelX, panelY, INFO_PANEL_WIDTH, panelHeight, 0xFF697487);
        guiGraphics.fill(panelX + 1, panelY + 1, panelX + INFO_PANEL_WIDTH - 1, panelY + 13, 0xC01A2230);
        guiGraphics.drawString(font, Component.translatable("itemGroup." + DeepNullReforged.MODID), panelX + 6, panelY + 4, 0xFFFFFFFF, false);

        if (slot == null || !slot.hasItem() || !(slot instanceof SlotItemHandler)) {
            int lineY = panelY + 22;
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.hover_for_details.desc"), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFC9D0DB);
            lineY += 4;
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.alt_click_set.desc"), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFF99A5B5);
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.alt_arrow_move.desc"), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFF99A5B5);
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.ctrl_click_change.desc"), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFF99A5B5);
            drawWrapped(guiGraphics, Component.translatable("dn.p_click_toggle.desc"), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFF99A5B5);
            return;
        }

        int storageSlot = slot.index;
        ItemStack stack = slot.getItem();
        int itemX = panelX + 8;
        int textX = panelX + 28;
        int lineY = panelY + 22;
        guiGraphics.renderItem(stack.copyWithCount(1), itemX, lineY);
        lineY = drawWrapped(guiGraphics, Component.translatable("dn.slot.desc").append(" ").append(Integer.toString(storageSlot + 1)), textX, lineY, INFO_PANEL_WIDTH - 34, 0xFFE8EDF5);
        lineY = drawWrapped(guiGraphics, stack.getHoverName(), textX, lineY, INFO_PANEL_WIDTH - 34, 0xFFFFFFFF);
        lineY += 4;
        lineY = drawWrapped(guiGraphics, label("dn.count.desc", countText(stack)), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFE8EDF5);
        lineY = drawWrapped(guiGraphics, label("dn.extract.desc", menu.getDankInventory().getExtractionMode(storageSlot).tooltip()), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFE8EDF5);
        lineY = drawWrapped(guiGraphics, label("dn.place.desc", menu.getDankInventory().getPlacementMode(storageSlot).tooltip()), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFE8EDF5);
        drawWrapped(guiGraphics, label("dn.tag_matching.desc", tagText(storageSlot)), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFE8EDF5);
    }

    private Slot getContextSlot() {
        Slot hovered = getSlotUnderMouse();
        if (hovered instanceof SlotItemHandler && hovered.index < menu.getStorageSlotCount()) {
            return hovered;
        }

        int selectedSlot = menu.getDankInventory().getSelectedSlot();
        if (selectedSlot >= 0 && selectedSlot < menu.getStorageSlotCount()) {
            return menu.slots.get(selectedSlot);
        }
        return null;
    }

    private Component label(String key, Component value) {
        return Component.translatable(key).append(": ").append(value);
    }

    private Component countText(ItemStack stack) {
        if (menu.getDankInventory().supportsLocking() && menu.getDankInventory().isLocked()) {
            return Component.translatable("dn.infinite.desc");
        }
        return Component.literal(Integer.toString(stack.getCount()));
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

    private Component trim(Component component, int maxWidth) {
        return Component.literal(font.plainSubstrByWidth(component.getString(), maxWidth));
    }

    private Component lockLabel() {
        return Component.translatable(menu.getDankInventory().isLocked() ? "dn.unlock.desc" : "dn.lock.desc");
    }

    private int measurePanelHeight(Slot slot) {
        int contentLines = 0;
        if (slot == null || !slot.hasItem() || !(slot instanceof SlotItemHandler)) {
            contentLines += wrappedLineCount(Component.translatable("dn.hover_for_details.desc"), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
            contentLines += wrappedLineCount(Component.translatable("dn.alt_click_set.desc"), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
            contentLines += wrappedLineCount(Component.translatable("dn.alt_arrow_move.desc"), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
            contentLines += wrappedLineCount(Component.translatable("dn.ctrl_click_change.desc"), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
            contentLines += wrappedLineCount(Component.translatable("dn.p_click_toggle.desc"), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
            return 22 + (contentLines * INFO_PANEL_LINE_HEIGHT) + 10;
        }

        int storageSlot = slot.index;
        ItemStack stack = slot.getItem();
        contentLines += wrappedLineCount(Component.translatable("dn.slot.desc").append(" ").append(Integer.toString(storageSlot + 1)), INFO_PANEL_WIDTH - 34);
        contentLines += wrappedLineCount(stack.getHoverName(), INFO_PANEL_WIDTH - 34);
        contentLines += wrappedLineCount(label("dn.count.desc", countText(stack)), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
        contentLines += wrappedLineCount(label("dn.extract.desc", menu.getDankInventory().getExtractionMode(storageSlot).tooltip()), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
        contentLines += wrappedLineCount(label("dn.place.desc", menu.getDankInventory().getPlacementMode(storageSlot).tooltip()), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
        contentLines += wrappedLineCount(label("dn.tag_matching.desc", tagText(storageSlot)), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
        return 22 + (contentLines * INFO_PANEL_LINE_HEIGHT) + 16;
    }

    private int drawWrapped(GuiGraphics guiGraphics, Component component, int x, int y, int maxWidth, int color) {
        for (FormattedCharSequence line : font.split(component, maxWidth)) {
            guiGraphics.drawString(font, line, x, y, color, false);
            y += INFO_PANEL_LINE_HEIGHT;
        }
        return y;
    }

    private int wrappedLineCount(Component component, int maxWidth) {
        return Math.max(1, font.split(component, maxWidth).size());
    }

    private int getReorderSourceSlot() {
        if (latchedReorderSlot >= 0 && latchedReorderSlot < menu.getStorageSlotCount()) {
            return latchedReorderSlot;
        }
        Slot hovered = getSlotUnderMouse();
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

    private static final class ScreenActions {
        private ScreenActions() {
        }

        private static boolean handle(DeepNullScreen screen, int storageSlot, int button) {
            if (button != 0 && button != 1) {
                return false;
            }

            if (hasModeKey(GLFW.GLFW_KEY_O)) {
                if (!screen.menu.getDankInventory().supportsTagMatching(storageSlot)) {
                    return false;
                }
                screen.menu.getDankInventory().toggleTagMatching(storageSlot);
                PacketDistributor.sendToServer(new DeepNullPayloads.MenuSlotActionPayload(
                        storageSlot,
                        DeepNullPayloads.MenuSlotAction.TOGGLE_TAG_MATCHING.ordinal()
                ));
                return true;
            }

            if (hasModeKey(GLFW.GLFW_KEY_P)) {
                screen.menu.getDankInventory().cyclePlacementMode(storageSlot, button == 0);
                PacketDistributor.sendToServer(new DeepNullPayloads.MenuSlotActionPayload(
                        storageSlot,
                        (button == 0 ? DeepNullPayloads.MenuSlotAction.CYCLE_PLACEMENT_FORWARD : DeepNullPayloads.MenuSlotAction.CYCLE_PLACEMENT_BACKWARD).ordinal()
                ));
                return true;
            }

            if (hasControlDown()) {
                screen.menu.getDankInventory().cycleExtractionMode(storageSlot, button == 0);
                PacketDistributor.sendToServer(new DeepNullPayloads.MenuSlotActionPayload(
                        storageSlot,
                        (button == 0 ? DeepNullPayloads.MenuSlotAction.CYCLE_EXTRACTION_FORWARD : DeepNullPayloads.MenuSlotAction.CYCLE_EXTRACTION_BACKWARD).ordinal()
                ));
                return true;
            }

            if (hasAltDown()) {
                screen.menu.getDankInventory().setSelectedSlot(storageSlot);
                screen.latchReorderSlot(storageSlot);
                PacketDistributor.sendToServer(new DeepNullPayloads.MenuSlotActionPayload(
                        storageSlot,
                        DeepNullPayloads.MenuSlotAction.SELECT.ordinal()
                ));
                return true;
            }

            return false;
        }

        private static boolean hasModeKey(int keyCode) {
            long window = Minecraft.getInstance().getWindow().getWindow();
            return InputConstants.isKeyDown(window, keyCode);
        }

        private static boolean hasControlDown() {
            return Screen.hasControlDown();
        }

        private static boolean hasAltDown() {
            return Screen.hasAltDown();
        }
    }
}
