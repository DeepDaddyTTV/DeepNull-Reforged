package dev.deepdaddyttv.deepnullreforged.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DeepNullScreen extends AbstractContainerScreen<DeepNullMenu> {
    private static final int BASE_IMAGE_WIDTH = 202;
    private static final int ENERGY_IMAGE_WIDTH = 252;
    private static final int INFO_PANEL_WIDTH = 146;
    private static final int INFO_PANEL_PADDING = 6;
    private static final int INFO_PANEL_LINE_HEIGHT = 10;
    private static final ResourceLocation INFO_BUTTON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_info_button.png");
    private static final ResourceLocation LOCK_BUTTON_OFF_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_lock_button_off.png");
    private static final ResourceLocation LOCK_BUTTON_ON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_lock_button_on.png");
    private static final ResourceLocation UPGRADE_BUTTON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_upgrade_button.png");
    private static final ResourceLocation CHARGING_BUTTON_OFF_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_charging_button_off.png");
    private static final ResourceLocation CHARGING_BUTTON_ON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_charging_button_on.png");
    private static final ResourceLocation INFO_TAB_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_info_tab.png");
    private static final int TAB_BUTTON_U = 98;
    private static final int INFO_BUTTON_V = 16;
    private static final int LOCK_BUTTON_V = 37;
    private static final int UPGRADE_BUTTON_V = 37;
    private static final int CHARGING_BUTTON_V = 58;
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

    private final ResourceLocation backgroundTexture;
    private final int baseImageWidth;
    private final boolean integratedEnergyGui;
    private int latchedReorderSlot = -1;
    private boolean shiftQuickMoveDragging;
    private int shiftQuickMoveButton = -1;
    private final Set<Integer> shiftQuickMovedSlots = new HashSet<>();
    private boolean infoPanelOpen;
    private Button lockButton;

    public DeepNullScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.integratedEnergyGui = menu.hasUpgrade(DeepNullUpgradeType.ENERGY) && menu.getTier().supportsEnergyUpgrade();
        this.baseImageWidth = integratedEnergyGui ? ENERGY_IMAGE_WIDTH : BASE_IMAGE_WIDTH;
        this.imageWidth = baseImageWidth;
        this.imageHeight = 141 + Math.max(0, menu.getTier().rows() - 1) * 21;
        this.inventoryLabelX = integratedEnergyGui ? INTEGRATED_LAYOUT_LABEL_X : 7;
        this.inventoryLabelY = this.imageHeight - 103;
        this.titleLabelX = 7;
        this.titleLabelY = 6;
        this.backgroundTexture = integratedEnergyGui ? integratedEnergyTexture() : menu.getTier().guiTexture();
    }

    @Override
    protected void init() {
        super.init();
        latchedReorderSlot = -1;
        clearShiftQuickMoveState();
        if (menu.getDankInventory().supportsLocking()) {
            lockButton = Button.builder(lockLabel(), button -> {
                        boolean next = !menu.getDankInventory().isLocked();
                        menu.getDankInventory().setLocked(next);
                        button.setMessage(lockLabel());
                        PacketDistributor.sendToServer(new DeepNullPayloads.MenuLockPayload(next));
                    })
                    .bounds(leftPos + baseImageWidth - 54, topPos - 20, 50, 18)
                    .build();
            addRenderableWidget(lockButton);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(backgroundTexture, leftPos, topPos, 0.0F, 0.0F, baseImageWidth, imageHeight, 256, 256);
        if (integratedEnergyGui) {
            renderIntegratedEnergyFill(guiGraphics);
        }

        int selectedSlot = menu.getDankInventory().getSelectedSlot();
        if (selectedSlot >= 0 && selectedSlot < menu.getStorageSlotCount()) {
            Slot slot = menu.slots.get(selectedSlot);
            guiGraphics.renderOutline(leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18, menu.getTier().ordinalId() == 0 ? 0xFFE7B623 : 0xFFD8DCE5);
        }

        for (int slotIndex = 0; slotIndex < menu.getStorageSlotCount(); slotIndex++) {
            Slot slot = menu.slots.get(slotIndex);
            if (isFluidView()) {
                renderFluidSlot(guiGraphics, slotIndex, leftPos + slot.x, topPos + slot.y);
            } else if (slot.hasItem()) {
                drawModeMarkers(guiGraphics, slotIndex, leftPos + slot.x, topPos + slot.y);
            }
        }
        renderIconButtons(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFFFFFFF, false);
        if (lockButton != null) {
            lockButton.setMessage(lockLabel());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (infoPanelOpen) {
            renderInfoPanel(guiGraphics);
        }
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderStorageTooltip(guiGraphics, mouseX, mouseY);
        if (integratedEnergyGui) {
            renderIntegratedCreativeEnergyTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 2 && isWithin(mouseX, mouseY, leftPos, topPos, baseImageWidth, imageHeight)) {
            return true;
        }
        if (handleIconButtonClick(mouseX, mouseY, button)) {
            return true;
        }
        clearShiftQuickMoveState();
        Slot slot = getSlotUnderMouse();
        boolean shouldBeginShiftQuickMoveDrag = canStartShiftQuickMoveDrag(slot, button);
        boolean fluidStorageClick = isFluidView() && slot instanceof DeepNullMenu.FluidStorageSlot;
        if ((slot instanceof DeepNullMenu.StorageSlot && slot.hasItem()) || fluidStorageClick) {
            int storageSlot = slot.index;
            if (ScreenActions.handle(this, storageSlot, button)) {
                return true;
            }
        }
        if (shouldBeginShiftQuickMoveDrag) {
            shiftQuickMoveDragging = true;
            shiftQuickMoveButton = button;
            shiftQuickMovedSlots.add(slot.index);
        }
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (!handled) {
            clearShiftQuickMoveState();
        }
        return handled;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (shiftQuickMoveDragging && button == shiftQuickMoveButton && Screen.hasShiftDown() && menu.getCarried().isEmpty()) {
            Slot hoveredSlot = findSlotAt(mouseX, mouseY);
            if (canShiftQuickMoveSlot(hoveredSlot) && shiftQuickMovedSlots.add(hoveredSlot.index)) {
                slotClicked(hoveredSlot, hoveredSlot.index, button, ClickType.QUICK_MOVE);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        clearShiftQuickMoveState();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFluidView() && Screen.hasAltDown()) {
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
        if (!Screen.hasShiftDown()) {
            clearShiftQuickMoveState();
        }
    }

    private boolean canStartShiftQuickMoveDrag(Slot slot, int button) {
        if ((button != 0 && button != 1) || !Screen.hasShiftDown() || !menu.getCarried().isEmpty()) {
            return false;
        }
        return canShiftQuickMoveSlot(slot);
    }

    private boolean canShiftQuickMoveSlot(Slot slot) {
        if (slot == null || !slot.hasItem()) {
            return false;
        }
        int index = slot.index;
        return index >= menu.getPlayerInventorySlotStartIndex()
                && index < menu.getPlayerInventorySlotStartIndex() + menu.getPlayerSlotCount()
                && slot.mayPickup(Minecraft.getInstance().player);
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

    private void renderIntegratedEnergyFill(GuiGraphics guiGraphics) {
        int capacity = menu.getDisplayedEnergyCapacity();
        int stored = menu.getDisplayedEnergyStored();
        if (capacity <= 0 || stored <= 0) {
            return;
        }

        int maxFillHeight = imageHeight;
        int fillHeight = Math.max(1, Math.round(maxFillHeight * Math.min(1.0F, stored / (float) capacity)));
        int drawY = topPos + (imageHeight - fillHeight);
        int sourceY = imageHeight - fillHeight;
        guiGraphics.blit(
                integratedEnergyFillTexture(),
                leftPos,
                drawY,
                0.0F,
                sourceY,
                baseImageWidth,
                fillHeight,
                256,
                256
        );
    }

    private void renderIntegratedCreativeEnergyTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!menu.hasUpgrade(DeepNullUpgradeType.ENERGY)) {
            return;
        }
        int x = leftPos;
        int y = topPos;
        if (mouseX < x || mouseX >= x + ENERGY_TOOLTIP_WIDTH || mouseY < y || mouseY >= y + imageHeight) {
            return;
        }

        List<Component> tooltip = List.of(
                Component.translatable("dn.energy.desc"),
                Component.literal(menu.getDisplayedEnergyStored() + " / " + menu.getDisplayedEnergyCapacity() + " FE"),
                Component.translatable("dn.charging.desc").append(": ").append(Component.translatable(menu.isChargingEnabledDisplayed() ? "dn.enabled.desc" : "dn.disabled.desc"))
        );
        guiGraphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
    }

    private void drawModeMarkers(GuiGraphics guiGraphics, int slotIndex, int x, int y) {
        guiGraphics.fill(x, y, x + 16, y + 2, extractionColor(slotIndex));
        guiGraphics.fill(x, y + 14, x + 16, y + 16, placementColor(slotIndex));
        if (menu.getDankInventory().isTagMatchingEnabled(slotIndex)) {
            guiGraphics.fill(x + 12, y + 2, x + 16, y + 6, 0xFF39C6DD);
        }
    }

    private boolean isFluidView() {
        return false;
    }

    private void renderFluidSlot(GuiGraphics guiGraphics, int slotIndex, int x, int y) {
        FluidStack fluidStack = menu.getDankInventory().getFluidInSlot(slotIndex);
        if (fluidStack.isEmpty()) {
            return;
        }

        IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation texture = clientFluid.getStillTexture(fluidStack);
        int tint = clientFluid.getTintColor(fluidStack);
        if (texture == null) {
            guiGraphics.fill(x, y, x + 16, y + 16, tint == 0 ? 0xFF3AA7FF : tint);
        } else {
            TextureAtlasSprite sprite = FluidSpriteCache.getSprite(texture);
            float alpha = ((tint >> 24) & 0xFF) / 255.0F;
            float red = ((tint >> 16) & 0xFF) / 255.0F;
            float green = ((tint >> 8) & 0xFF) / 255.0F;
            float blue = (tint & 0xFF) / 255.0F;
            guiGraphics.blit(x, y, 0, 16, 16, sprite, red, green, blue, alpha <= 0.0F ? 1.0F : alpha);
        }

        guiGraphics.fill(x, y, x + 16, y + 16, 0x33000000);
        String amount = fluidAmountText(fluidStack);
        guiGraphics.drawString(font, amount, x + 16 - font.width(amount), y + 9, 0xFFFFFFFF, true);
    }

    private String fluidAmountText(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return "";
        }
        if (fluidStack.getAmount() == Integer.MAX_VALUE) {
            return "inf";
        }
        int buckets = fluidStack.getAmount() / 1000;
        return Integer.toString(Math.max(1, buckets));
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
        int panelX = infoPanelX();
        int panelY = infoPanelY();
        int textX = panelX + 14;
        int lineY = panelY + 12;
        int textWidth = INFO_TAB_WIDTH - 24;

        guiGraphics.blit(INFO_TAB_TEXTURE, panelX, panelY, INFO_TAB_U, INFO_TAB_V, INFO_TAB_WIDTH, INFO_TAB_HEIGHT, 256, 256);
        guiGraphics.drawString(font, Component.translatable("itemGroup." + DeepNullReforged.MODID), textX, lineY, 0xFFFFFFFF, false);
        lineY += 18;

        if (isFluidView()) {
            renderFluidInfoPanel(guiGraphics, slot, panelX, panelY);
            return;
        }

        if (slot == null || !slot.hasItem() || !(slot instanceof SlotItemHandler)) {
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.hover_for_details.desc"), textX, lineY, textWidth, 0xFFC9D0DB);
            lineY += 4;
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.alt_click_set.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.alt_arrow_move.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.ctrl_click_change.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.p_click_toggle.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            lineY += 4;
            drawWrapped(guiGraphics, Component.translatable("dn.upgrades_hint.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            return;
        }

        int storageSlot = slot.index;
        ItemStack stack = slot.getItem();
        int itemX = textX;
        textX = panelX + 34;
        guiGraphics.renderItem(stack.copyWithCount(1), itemX, lineY);
        lineY = drawWrapped(guiGraphics, Component.translatable("dn.slot.desc").append(" ").append(Integer.toString(storageSlot + 1)), textX, lineY, INFO_TAB_WIDTH - 40, 0xFFE8EDF5);
        lineY = drawWrapped(guiGraphics, stack.getHoverName(), textX, lineY, INFO_TAB_WIDTH - 40, 0xFFFFFFFF);
        lineY += 4;
        lineY = drawWrapped(guiGraphics, label("dn.count.desc", countText(stack)), textX - 20, lineY, textWidth, 0xFFE8EDF5);
        lineY = drawWrapped(guiGraphics, label("dn.extract.desc", menu.getDankInventory().getExtractionMode(storageSlot).tooltip()), textX - 20, lineY, textWidth, 0xFFE8EDF5);
        lineY = drawWrapped(guiGraphics, label("dn.place.desc", menu.getDankInventory().getPlacementMode(storageSlot).tooltip()), textX - 20, lineY, textWidth, 0xFFE8EDF5);
        lineY = drawWrapped(guiGraphics, label("dn.tag_matching.desc", tagText(storageSlot)), textX - 20, lineY, textWidth, 0xFFE8EDF5);
        renderUpgradeSummary(guiGraphics, panelX, panelY);
    }

    private void renderFluidInfoPanel(GuiGraphics guiGraphics, Slot slot, int panelX, int panelY) {
        int slotIndex = slot == null ? menu.getDankInventory().getSelectedSlot() : slot.index;
        FluidStack fluidStack = slotIndex >= 0 && slotIndex < menu.getStorageSlotCount()
                ? menu.getDankInventory().getFluidInSlot(slotIndex)
                : FluidStack.EMPTY;

        int lineY = panelY + 22;
        lineY = drawWrapped(guiGraphics, Component.translatable("dn.slot.desc").append(" ").append(Integer.toString(Math.max(1, slotIndex + 1))), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFE8EDF5);
        if (fluidStack.isEmpty()) {
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.empty.desc"), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFFFFFFF);
            lineY += 4;
            drawWrapped(guiGraphics, Component.translatable("dn.fluid_empty_hint.desc"), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFF99A5B5);
            return;
        }

        lineY = drawWrapped(guiGraphics, fluidStack.getHoverName(), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFFFFFFF);
        lineY += 4;
        lineY = drawWrapped(guiGraphics, label("dn.amount.desc", Component.literal(fluidStack.getAmount() + " mB")), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFE8EDF5);
        lineY = drawWrapped(guiGraphics, label("dn.capacity.desc", Component.literal(fluidCapacityText())), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFE8EDF5);
        renderUpgradeSummary(guiGraphics, panelX, panelY);
    }

    private Slot getContextSlot() {
        Slot hovered = getSlotUnderMouse();
        if (hovered != null && hovered.index >= 0 && hovered.index < menu.getStorageSlotCount()) {
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

    private Component chargingLabel() {
        return Component.translatable(menu.isChargingEnabledDisplayed() ? "dn.charging_on.desc" : "dn.charging_off.desc");
    }

    private Component transferLockLabel() {
        return Component.translatable(menu.getDankInventory().isTransferLocked() ? "dn.transfer_locked.desc" : "dn.transfer_unlocked.desc");
    }

    public boolean toggleTransferLock() {
        boolean next = !menu.getDankInventory().isTransferLocked();
        menu.getDankInventory().setTransferLocked(next);
        PacketDistributor.sendToServer(new DeepNullPayloads.MenuTransferLockPayload(next));
        return next;
    }

    private int measurePanelHeight(Slot slot) {
        if (isFluidView()) {
            return measureFluidPanelHeight(slot);
        }

        int contentLines = 0;
        if (slot == null || !slot.hasItem() || !(slot instanceof SlotItemHandler)) {
            contentLines += wrappedLineCount(Component.translatable("dn.hover_for_details.desc"), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
            contentLines += wrappedLineCount(Component.translatable("dn.alt_click_set.desc"), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
            contentLines += wrappedLineCount(Component.translatable("dn.alt_arrow_move.desc"), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
            contentLines += wrappedLineCount(Component.translatable("dn.ctrl_click_change.desc"), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
            contentLines += wrappedLineCount(Component.translatable("dn.p_click_toggle.desc"), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
            contentLines += wrappedLineCount(Component.translatable("dn.upgrades_hint.desc"), INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2));
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
        contentLines += upgradeSummaryLineCount();
        return 22 + (contentLines * INFO_PANEL_LINE_HEIGHT) + 16;
    }

    private int measureFluidPanelHeight(Slot slot) {
        int width = INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2);
        int contentLines = wrappedLineCount(Component.translatable("dn.slot.desc").append(" ").append(Integer.toString(Math.max(1, (slot == null ? menu.getDankInventory().getSelectedSlot() : slot.index) + 1))), width);
        int slotIndex = slot == null ? menu.getDankInventory().getSelectedSlot() : slot.index;
        FluidStack fluidStack = slotIndex >= 0 && slotIndex < menu.getStorageSlotCount()
                ? menu.getDankInventory().getFluidInSlot(slotIndex)
                : FluidStack.EMPTY;
        if (fluidStack.isEmpty()) {
            contentLines += wrappedLineCount(Component.translatable("dn.empty.desc"), width);
            contentLines += wrappedLineCount(Component.translatable("dn.fluid_empty_hint.desc"), width);
        } else {
            contentLines += wrappedLineCount(fluidStack.getHoverName(), width);
            contentLines += wrappedLineCount(label("dn.amount.desc", Component.literal(fluidStack.getAmount() + " mB")), width);
            contentLines += wrappedLineCount(label("dn.capacity.desc", Component.literal(fluidCapacityText())), width);
            contentLines += upgradeSummaryLineCount();
        }
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

    private void renderStorageTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!isFluidView()) {
            return;
        }

        Slot hovered = getSlotUnderMouse();
        if (!(hovered instanceof DeepNullMenu.FluidStorageSlot) || hovered.index < 0 || hovered.index >= menu.getStorageSlotCount()) {
            return;
        }

        FluidStack fluidStack = menu.getDankInventory().getFluidInSlot(hovered.index);
        if (fluidStack.isEmpty()) {
            guiGraphics.renderTooltip(
                    font,
                    List.of(
                            Component.translatable("dn.slot.desc").append(" ").append(Integer.toString(hovered.index + 1)),
                            Component.translatable("dn.empty.desc"),
                            Component.translatable("dn.capacity.desc").append(": ").append(Component.literal(fluidCapacityText()))
                    ),
                    java.util.Optional.empty(),
                    mouseX,
                    mouseY
            );
            return;
        }

        guiGraphics.renderTooltip(
                font,
                List.of(
                        fluidStack.getHoverName(),
                        Component.translatable("dn.slot.desc").append(" ").append(Integer.toString(hovered.index + 1)),
                        Component.translatable("dn.amount.desc").append(": ").append(Component.literal(fluidStack.getAmount() + " mB")),
                        Component.translatable("dn.capacity.desc").append(": ").append(Component.literal(fluidCapacityText()))
                ),
                java.util.Optional.empty(),
                mouseX,
                mouseY
        );
    }

    private void renderUpgradeSummary(GuiGraphics guiGraphics, int panelX, int panelY) {
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
            guiGraphics.renderItem(upgradeIcon(installed.get(index)), x, y);
        }
    }

    private int upgradeSummaryLineCount() {
        int count = 0;
        for (DeepNullUpgradeType type : DeepNullUpgradeType.values()) {
            if (menu.hasUpgrade(type)) {
                count += wrappedLineCount(Component.translatable("upgrade." + type.itemId() + ".installed"), INFO_TAB_WIDTH - 24);
            }
        }
        return count;
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
        };
    }

    private ResourceLocation integratedEnergyTexture() {
        return switch (menu.getTier()) {
            case DIAMOND -> DeepNullReforged.id("textures/gui/deepnullscreen4_energy.png");
            case EMERALD -> DeepNullReforged.id("textures/gui/deepnullscreen5_energy.png");
            case CREATIVE -> DeepNullReforged.id("textures/gui/deepnullscreen6_energy.png");
            default -> menu.getTier().guiTexture();
        };
    }

    private ResourceLocation integratedEnergyFillTexture() {
        return switch (menu.getTier()) {
            case DIAMOND -> DeepNullReforged.id("textures/gui/deepnullscreen4_energy_fill.png");
            case EMERALD -> DeepNullReforged.id("textures/gui/deepnullscreen5_energy_fill.png");
            case CREATIVE -> DeepNullReforged.id("textures/gui/deepnullscreen6_energy_fill.png");
            default -> DeepNullReforged.id("textures/gui/deepnullscreen4_energy_fill.png");
        };
    }

    private void renderIconButtons(GuiGraphics guiGraphics) {
        int x = leftPos + baseImageWidth - 1;
        renderIconButton(guiGraphics, INFO_BUTTON_TEXTURE, x, topPos + 38, INFO_BUTTON_V);
        renderIconButton(guiGraphics, menu.getDankInventory().isTransferLocked() ? LOCK_BUTTON_ON_TEXTURE : LOCK_BUTTON_OFF_TEXTURE, x, topPos + 59, LOCK_BUTTON_V);
        renderIconButton(guiGraphics, UPGRADE_BUTTON_TEXTURE, x, topPos + 80, UPGRADE_BUTTON_V);
        if (menu.hasUpgrade(DeepNullUpgradeType.ENERGY)) {
            renderIconButton(
                    guiGraphics,
                    menu.isChargingEnabledDisplayed() ? CHARGING_BUTTON_ON_TEXTURE : CHARGING_BUTTON_OFF_TEXTURE,
                    x,
                    topPos + 101,
                    CHARGING_BUTTON_V
            );
        }
    }

    private void renderIconButton(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int v) {
        guiGraphics.blit(texture, x, y, TAB_BUTTON_U, v, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT, 256, 256);
    }

    private boolean handleIconButtonClick(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        int x = leftPos + baseImageWidth - 1;
        if (isWithin(mouseX, mouseY, x, topPos + 38, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            infoPanelOpen = !infoPanelOpen;
            return true;
        }
        if (isWithin(mouseX, mouseY, x, topPos + 59, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            toggleTransferLock();
            return true;
        }
        if (isWithin(mouseX, mouseY, x, topPos + 80, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            PacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.UPGRADES.ordinal()));
            return true;
        }
        if (menu.hasUpgrade(DeepNullUpgradeType.ENERGY) && isWithin(mouseX, mouseY, x, topPos + 101, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            boolean next = !menu.isChargingEnabledDisplayed();
            menu.getDankInventory().setChargingEnabled(next);
            PacketDistributor.sendToServer(new DeepNullPayloads.MenuChargingPayload(next));
            return true;
        }
        return false;
    }

    private static boolean isWithin(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public Rect2i getInfoPanelArea() {
        if (!infoPanelOpen) {
            return null;
        }
        return new Rect2i(infoPanelX(), infoPanelY(), INFO_TAB_WIDTH, INFO_TAB_HEIGHT);
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

    private int getReorderSourceSlot() {
        if (isFluidView()) {
            return -1;
        }
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

    private String fluidCapacityText() {
        int capacity = menu.getDankInventory().getFluidCapacity();
        return capacity == Integer.MAX_VALUE ? Component.translatable("dn.infinite.desc").getString() : capacity + " mB";
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

            if (screen.isFluidView()) {
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
