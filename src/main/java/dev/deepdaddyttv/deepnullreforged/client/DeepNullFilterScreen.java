package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullFilterMode;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class DeepNullFilterScreen extends AbstractContainerScreen<DeepNullMenu> {
    private static final int SLOT_SPACING = 21;
    private static final int LEFT_PADDING = 9;
    private static final int TOP_PADDING = 19;
    private static final int FILTER_ROWS = 3;
    private static final int FILTER_COLUMNS = 9;
    private static final ResourceLocation BACKGROUND_TEXTURE = DeepNullReforged.id("textures/gui/deepnull_filter.png");
    private static final ResourceLocation ENERGY_BACKGROUND_TEXTURE = DeepNullReforged.id("textures/gui/deepnull_filter_energy.png");
    private static final ResourceLocation ENERGY_FILL_TEXTURE = DeepNullReforged.id("textures/gui/deepnull_filter_energy_fill.png");
    private static final int BASE_IMAGE_WIDTH = 202;
    private static final int ENERGY_IMAGE_WIDTH = 252;
    private static final int ENERGY_LABEL_X = 57;
    private static final int ENERGY_TOOLTIP_WIDTH = 50;

    private final Inventory playerInventory;
    private final boolean integratedEnergyGui;
    private final ResourceLocation backgroundTexture;
    private Button modeButton;

    public DeepNullFilterScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.playerInventory = playerInventory;
        this.integratedEnergyGui = menu.hasUpgrade(DeepNullUpgradeType.ENERGY) && menu.getTier().supportsEnergyUpgrade();
        this.backgroundTexture = integratedEnergyGui ? ENERGY_BACKGROUND_TEXTURE : BACKGROUND_TEXTURE;
        this.imageWidth = integratedEnergyGui ? ENERGY_IMAGE_WIDTH : BASE_IMAGE_WIDTH;
        this.imageHeight = 183;
        this.inventoryLabelX = integratedEnergyGui ? ENERGY_LABEL_X : 7;
        this.inventoryLabelY = this.imageHeight - 103;
        this.titleLabelX = integratedEnergyGui ? ENERGY_LABEL_X : 7;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(Button.builder(Component.translatable("dn.back.desc"), button ->
                        PacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.UPGRADES.ordinal())))
                .bounds(leftPos, topPos - 20, 50, 18)
                .build());

        modeButton = Button.builder(filterModeLabel(), button -> cycleFilterMode())
                .bounds(leftPos + 55, topPos - 20, 146, 18)
                .build();
        addRenderableWidget(modeButton);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        boolean shouldUseIntegrated = menu.hasUpgrade(DeepNullUpgradeType.ENERGY) && menu.getTier().supportsEnergyUpgrade();
        if (shouldUseIntegrated != integratedEnergyGui) {
            Minecraft.getInstance().setScreen(new DeepNullFilterScreen(menu, playerInventory, title));
            return;
        }
        if (!menu.hasUpgrade(DeepNullUpgradeType.FILTER)) {
            PacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.UPGRADES.ordinal()));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(backgroundTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        renderEnergyFill(guiGraphics);

        int hovered = getFilterSlotAt(mouseX, mouseY);
        for (int slot = 0; slot < menu.getDankInventory().getFilterSlotCount(); slot++) {
            int x = leftPos + filterLeftPadding() + (slot % FILTER_COLUMNS) * SLOT_SPACING;
            int y = topPos + TOP_PADDING + (slot / FILTER_COLUMNS) * SLOT_SPACING;
            ItemStack filterStack = menu.getFilterStack(slot);
            if (!filterStack.isEmpty()) {
                guiGraphics.renderItem(filterStack, x, y);
                guiGraphics.fill(x, y, x + 16, y + 16, 0x55000000);
            }
            if (slot == hovered) {
                guiGraphics.renderOutline(x - 1, y - 1, 18, 18, 0xFFD8DCE5);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, Component.translatable("dn.filter_screen.desc"), titleLabelX, titleLabelY, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, playerInventory.getDisplayName(), inventoryLabelX, inventoryLabelY, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, Component.translatable("dn.filter_mode_label.desc"), 118, 6, 0xFFE8EDF5, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (modeButton != null) {
            modeButton.setMessage(filterModeLabel());
        }
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderEnergyTooltip(guiGraphics, mouseX, mouseY);

        int hovered = getFilterSlotAt(mouseX, mouseY);
        if (hovered >= 0) {
            ItemStack filterStack = menu.getFilterStack(hovered);
            if (!filterStack.isEmpty()) {
                guiGraphics.renderTooltip(font, filterStack, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && Screen.hasShiftDown()) {
            Slot hoveredSlot = getSlotUnderMouse();
            int hoveredMenuIndex = hoveredSlot == null ? -1 : menu.slots.indexOf(hoveredSlot);
            if (hoveredMenuIndex >= menu.getPlayerInventorySlotStartIndex() && hoveredSlot.hasItem()) {
                int filterSlot = menu.addGhostFilterStack(hoveredSlot.getItem());
                if (filterSlot >= 0) {
                    PacketDistributor.sendToServer(new DeepNullPayloads.MenuFilterSlotPayload(filterSlot, hoveredSlot.getItem().copyWithCount(1)));
                    return true;
                }
            }
        }

        int filterSlot = getFilterSlotAt(mouseX, mouseY);
        if (filterSlot >= 0) {
            ItemStack carried = menu.getCarried();
            if (button == 1 || carried.isEmpty()) {
                menu.setFilterStack(filterSlot, ItemStack.EMPTY);
                PacketDistributor.sendToServer(new DeepNullPayloads.MenuFilterSlotPayload(filterSlot, ItemStack.EMPTY));
                return true;
            }
            if (button == 0) {
                ItemStack ghostStack = carried.copyWithCount(1);
                menu.setFilterStack(filterSlot, ghostStack);
                PacketDistributor.sendToServer(new DeepNullPayloads.MenuFilterSlotPayload(filterSlot, ghostStack));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void cycleFilterMode() {
        DeepNullFilterMode next = menu.getFilterMode().cycle(true);
        menu.setFilterMode(next);
        PacketDistributor.sendToServer(new DeepNullPayloads.MenuFilterModePayload(next.ordinal()));
    }

    private Component filterModeLabel() {
        return Component.translatable("dn.filter_mode_cycle.desc", menu.getFilterMode().displayName());
    }

    private int getFilterSlotAt(double mouseX, double mouseY) {
        int relativeX = (int) mouseX - leftPos;
        int relativeY = (int) mouseY - topPos;
        for (int slot = 0; slot < menu.getDankInventory().getFilterSlotCount(); slot++) {
            int x = filterLeftPadding() + (slot % FILTER_COLUMNS) * SLOT_SPACING;
            int y = TOP_PADDING + (slot / FILTER_COLUMNS) * SLOT_SPACING;
            if (relativeX >= x - 1 && relativeX < x + 17 && relativeY >= y - 1 && relativeY < y + 17) {
                return slot;
            }
        }
        return -1;
    }

    private int filterLeftPadding() {
        return integratedEnergyGui ? ENERGY_LABEL_X + 2 : LEFT_PADDING;
    }

    private void renderEnergyFill(GuiGraphics guiGraphics) {
        if (!integratedEnergyGui || menu.getDisplayedEnergyCapacity() <= 0 || menu.getDisplayedEnergyStored() <= 0) {
            return;
        }
        int fillHeight = Math.max(1, Math.round(imageHeight * Math.min(1.0F, menu.getDisplayedEnergyStored() / (float) menu.getDisplayedEnergyCapacity())));
        int drawY = topPos + (imageHeight - fillHeight);
        int sourceY = imageHeight - fillHeight;
        guiGraphics.blit(ENERGY_FILL_TEXTURE, leftPos, drawY, 0.0F, sourceY, imageWidth, fillHeight, 256, 256);
    }

    private void renderEnergyTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!integratedEnergyGui) {
            return;
        }
        int x = leftPos;
        int y = topPos;
        if (mouseX < x || mouseX >= x + ENERGY_TOOLTIP_WIDTH || mouseY < y || mouseY >= y + imageHeight) {
            return;
        }
        guiGraphics.renderTooltip(
                font,
                java.util.List.of(
                        Component.translatable("dn.energy.desc"),
                        Component.literal(menu.getDisplayedEnergyStored() + " / " + menu.getDisplayedEnergyCapacity() + " FE")
                ),
                java.util.Optional.empty(),
                mouseX,
                mouseY
        );
    }

}
