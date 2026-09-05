package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullFilterMode;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

public class DeepNullFilterScreen extends AbstractContainerScreen<DeepNullMenu> {
    private static final int SLOT_SPACING = 21;
    private static final int LEFT_PADDING = 9;
    private static final int TOP_PADDING = 19;
    private static final int FILTER_ROWS = 3;
    private static final int FILTER_COLUMNS = 9;
    private static final Identifier BACKGROUND_TEXTURE = DeepNullReforged.id("textures/gui/deepnull_filter.png");
    private static final Identifier ENERGY_BACKGROUND_TEXTURE = DeepNullReforged.id("textures/gui/deepnull_filter_energy.png");
    private static final Identifier ENERGY_FILL_TEXTURE = DeepNullReforged.id("textures/gui/deepnull_filter_energy_fill.png");
    private static final int BASE_IMAGE_WIDTH = 202;
    private static final int ENERGY_IMAGE_WIDTH = 252;
    private static final int IMAGE_HEIGHT = 183;
    private static final int ENERGY_LABEL_X = 57;
    private static final int ENERGY_TOOLTIP_WIDTH = 50;

    private final Inventory playerInventory;
    private final boolean integratedEnergyGui;
    private final Identifier backgroundTexture;
    private Button modeButton;

    public DeepNullFilterScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, imageWidthFor(menu), IMAGE_HEIGHT);
        this.playerInventory = playerInventory;
        this.integratedEnergyGui = menu.hasEnergyUpgrade() && menu.getTier().supportsEnergyUpgrade();
        this.backgroundTexture = integratedEnergyGui ? ENERGY_BACKGROUND_TEXTURE : BACKGROUND_TEXTURE;
        this.inventoryLabelX = integratedEnergyGui ? ENERGY_LABEL_X : 7;
        this.inventoryLabelY = this.imageHeight - 103;
        this.titleLabelX = 7;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(Button.builder(Component.translatable("dn.back.desc"), button ->
                        ClientPacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.UPGRADES.ordinal())))
                .bounds(leftPos, topPos - 20, 50, 18)
                .build());

        modeButton = addRenderableWidget(Button.builder(filterModeLabel(), button -> cycleFilterMode())
                .bounds(leftPos + 55, topPos - 20, 146, 18)
                .build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        boolean shouldUseIntegrated = menu.hasEnergyUpgrade() && menu.getTier().supportsEnergyUpgrade();
        if (shouldUseIntegrated != integratedEnergyGui) {
            Minecraft.getInstance().setScreen(new DeepNullFilterScreen(menu, playerInventory, title));
            return;
        }
        if (isAutoSmeltView() ? !menu.hasUpgrade(DeepNullUpgradeType.AUTO_SMELTING) : !menu.hasUpgrade(DeepNullUpgradeType.FILTER)) {
            ClientPacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.UPGRADES.ordinal()));
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        renderEnergyFill(graphics);

        int hovered = getFilterSlotAt(mouseX, mouseY);
        for (int slot = 0; slot < menu.getDankInventory().getFilterSlotCount(); slot++) {
            int x = leftPos + filterLeftPadding() + (slot % FILTER_COLUMNS) * SLOT_SPACING;
            int y = topPos + TOP_PADDING + (slot / FILTER_COLUMNS) * SLOT_SPACING;
            ItemStack filterStack = displayedFilterStack(slot);
            if (!filterStack.isEmpty()) {
                graphics.item(filterStack, x, y);
                graphics.fill(x, y, x + 16, y + 16, 0x55000000);
            }
            if (slot == hovered) {
                graphics.outline(x - 1, y - 1, 18, 18, 0xFFD8DCE5);
            }
        }

        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (modeButton != null) {
            modeButton.setMessage(filterModeLabel());
        }
        graphics.text(font, Component.translatable(isAutoSmeltView() ? "dn.auto_smelt_filter_screen.desc" : "dn.filter_screen.desc"), titleLabelX, titleLabelY, 0xFFFFFFFF, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFFFFFFF, false);
        graphics.text(font, Component.translatable(isAutoSmeltView() ? "dn.auto_smelt_mode_label.desc" : "dn.filter_mode_label.desc"), 118, 6, 0xFFE8EDF5, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int hovered = getFilterSlotAt(mouseX, mouseY);
        if (hovered >= 0) {
            ItemStack filterStack = displayedFilterStack(hovered);
            if (!filterStack.isEmpty() && minecraft != null) {
                graphics.setTooltipForNextFrame(font, getTooltipFromItem(minecraft, filterStack), filterStack.getTooltipImage(), filterStack, mouseX, mouseY);
            }
        }

        renderEnergyTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 2 && isWithin(event.x(), event.y(), leftPos, topPos, imageWidth, imageHeight)) {
            return true;
        }

        if (event.button() == 0 && event.hasShiftDown()) {
            Slot hoveredSlot = findMenuSlot(event.x(), event.y());
            int hoveredMenuIndex = hoveredSlot == null ? -1 : menu.slots.indexOf(hoveredSlot);
            if (hoveredMenuIndex >= menu.getPlayerInventorySlotStartIndex() && hoveredSlot.hasItem()) {
                int filterSlot = isAutoSmeltView()
                        ? menu.addGhostAutoSmeltFilterStack(hoveredSlot.getItem())
                        : menu.addGhostFilterStack(hoveredSlot.getItem());
                if (filterSlot >= 0) {
                    ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuFilterSlotPayload(filterSlot, hoveredSlot.getItem().copyWithCount(1)));
                    return true;
                }
            }
        }

        int filterSlot = getFilterSlotAt(event.x(), event.y());
        if (filterSlot >= 0) {
            ItemStack carried = menu.getCarried();
            if (event.button() == 1 || carried.isEmpty()) {
                setDisplayedFilterStack(filterSlot, ItemStack.EMPTY);
                ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuFilterSlotPayload(filterSlot, ItemStack.EMPTY));
                return true;
            }
            if (event.button() == 0) {
                ItemStack ghostStack = carried.copyWithCount(1);
                setDisplayedFilterStack(filterSlot, ghostStack);
                ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuFilterSlotPayload(filterSlot, ghostStack));
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private static boolean isWithin(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void cycleFilterMode() {
        DeepNullFilterMode next = isAutoSmeltView()
                ? (menu.getAutoSmeltFilterMode() == DeepNullFilterMode.WHITELIST ? DeepNullFilterMode.BLACKLIST : DeepNullFilterMode.WHITELIST)
                : menu.getFilterMode().cycle(true);
        if (isAutoSmeltView()) {
            menu.setAutoSmeltFilterMode(next);
        } else {
            menu.setFilterMode(next);
        }
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuFilterModePayload(next.ordinal()));
    }

    private Component filterModeLabel() {
        return Component.translatable("dn.filter_mode_cycle.desc", displayedFilterMode().displayName());
    }

    private int getFilterSlotAt(double mouseX, double mouseY) {
        int relativeX = (int) mouseX - leftPos;
        int relativeY = (int) mouseY - topPos;
        for (int slot = 0; slot < FILTER_ROWS * FILTER_COLUMNS; slot++) {
            int x = filterLeftPadding() + (slot % FILTER_COLUMNS) * SLOT_SPACING;
            int y = TOP_PADDING + (slot / FILTER_COLUMNS) * SLOT_SPACING;
            if (relativeX >= x - 1 && relativeX < x + 17 && relativeY >= y - 1 && relativeY < y + 17) {
                return slot;
            }
        }
        return -1;
    }

    private Slot findMenuSlot(double mouseX, double mouseY) {
        for (Slot slot : menu.slots) {
            if (slot.isActive() && isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    private int filterLeftPadding() {
        return integratedEnergyGui ? ENERGY_LABEL_X + 2 : LEFT_PADDING;
    }

    private boolean isAutoSmeltView() {
        return menu.getViewMode() == DeepNullMenu.ViewMode.AUTO_SMELT_FILTER;
    }

    private DeepNullFilterMode displayedFilterMode() {
        return isAutoSmeltView() ? menu.getAutoSmeltFilterMode() : menu.getFilterMode();
    }

    private ItemStack displayedFilterStack(int slot) {
        return isAutoSmeltView() ? menu.getAutoSmeltFilterStack(slot) : menu.getFilterStack(slot);
    }

    private void setDisplayedFilterStack(int slot, ItemStack stack) {
        if (isAutoSmeltView()) {
            menu.setAutoSmeltFilterStack(slot, stack);
        } else {
            menu.setFilterStack(slot, stack);
        }
    }

    private void renderEnergyFill(GuiGraphicsExtractor graphics) {
        if (!integratedEnergyGui || menu.getDisplayedEnergyCapacity() <= 0 || menu.getDisplayedEnergyStored() <= 0) {
            return;
        }
        int fillHeight = Math.max(1, Math.round(imageHeight * Math.min(1.0F, menu.getDisplayedEnergyStored() / (float) menu.getDisplayedEnergyCapacity())));
        int drawY = topPos + (imageHeight - fillHeight);
        int sourceY = imageHeight - fillHeight;
        graphics.blit(RenderPipelines.GUI_TEXTURED, ENERGY_FILL_TEXTURE, leftPos, drawY, 0.0F, sourceY, imageWidth, fillHeight, 256, 256);
    }

    private void renderEnergyTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!integratedEnergyGui) {
            return;
        }
        int x = leftPos;
        int y = topPos;
        if (mouseX < x || mouseX >= x + ENERGY_TOOLTIP_WIDTH || mouseY < y || mouseY >= y + imageHeight) {
            return;
        }
        graphics.setComponentTooltipForNextFrame(
                font,
                List.of(
                        Component.translatable("dn.energy.desc").withStyle(ChatFormatting.AQUA),
                        Component.literal(menu.getDisplayedEnergyStored() + " / " + menu.getDisplayedEnergyCapacity() + " FE").withStyle(ChatFormatting.GRAY)
                ),
                mouseX,
                mouseY
        );
    }

    private static int imageWidthFor(DeepNullMenu menu) {
        return menu.hasEnergyUpgrade() && menu.getTier().supportsEnergyUpgrade() ? ENERGY_IMAGE_WIDTH : BASE_IMAGE_WIDTH;
    }
}
