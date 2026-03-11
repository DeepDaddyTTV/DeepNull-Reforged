package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

public class DeepNullFluidScreen extends AbstractContainerScreen<DeepNullMenu> {
    private static final int BASE_IMAGE_WIDTH = 201;
    private static final int INFO_PANEL_GAP = 8;
    private static final int INFO_PANEL_WIDTH = 142;
    private static final int INFO_PANEL_MIN_HEIGHT = 96;
    private static final int INFO_PANEL_PADDING = 6;
    private static final int INFO_PANEL_LINE_HEIGHT = 10;
    private static final int LEFT_PADDING = 9;
    private static final int TOP_PADDING = 19;
    private static final int SLOT_SPACING = 21;
    private static final int TANK_WIDTH = 58;

    private final Inventory playerInventory;
    private final ResourceLocation backgroundTexture;
    private Button itemsButton;
    private Button chargeButton;
    private Button upgradesButton;

    public DeepNullFluidScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.playerInventory = playerInventory;
        this.imageWidth = BASE_IMAGE_WIDTH + INFO_PANEL_GAP + INFO_PANEL_WIDTH;
        this.imageHeight = 141 + Math.max(0, menu.getTier().rows() - 1) * SLOT_SPACING;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = this.imageHeight - 103;
        this.titleLabelX = 7;
        this.titleLabelY = 6;
        this.backgroundTexture = menu.getTier().guiTexture();
    }

    @Override
    protected void init() {
        super.init();
        itemsButton = Button.builder(Component.translatable("dn.items.desc"), button ->
                        PacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.MAIN.ordinal())))
                .bounds(leftPos + BASE_IMAGE_WIDTH + 8, topPos + imageHeight - 68, INFO_PANEL_WIDTH - 16, 18)
                .build();
        addRenderableWidget(itemsButton);

        chargeButton = Button.builder(chargingLabel(), button -> {
                    boolean next = !menu.getDankInventory().isChargingEnabled();
                    menu.getDankInventory().setChargingEnabled(next);
                    button.setMessage(chargingLabel());
                    PacketDistributor.sendToServer(new DeepNullPayloads.MenuChargingPayload(next));
                })
                .bounds(leftPos + BASE_IMAGE_WIDTH + 8, topPos + imageHeight - 46, INFO_PANEL_WIDTH - 16, 18)
                .build();
        addRenderableWidget(chargeButton);

        upgradesButton = Button.builder(Component.translatable("dn.upgrades.desc"), button ->
                        PacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.UPGRADES.ordinal())))
                .bounds(leftPos + BASE_IMAGE_WIDTH + 8, topPos + imageHeight - 24, INFO_PANEL_WIDTH - 16, 18)
                .build();
        addRenderableWidget(upgradesButton);
        syncSideButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!menu.getDankInventory().hasFluidUpgrade()) {
            PacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.MAIN.ordinal()));
        }
        syncSideButtons();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(backgroundTexture, leftPos, topPos, 0.0F, 0.0F, BASE_IMAGE_WIDTH, imageHeight, 256, 256);
        DeepNullEnergyWidget.render(guiGraphics, menu.getDankInventory(), leftPos, topPos);
        renderTank(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, Component.translatable("dn.fluid_screen.desc"), titleLabelX, titleLabelY, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, playerInventory.getDisplayName(), inventoryLabelX, inventoryLabelY, 0xFFFFFFFF, false);
        if (chargeButton != null) {
            chargeButton.setMessage(chargingLabel());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderInfoPanel(guiGraphics);
        renderTooltip(guiGraphics, mouseX, mouseY);
        DeepNullEnergyWidget.renderTooltip(guiGraphics, font, menu.getDankInventory(), leftPos, topPos, mouseX, mouseY);
        renderTankTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderTank(GuiGraphics guiGraphics) {
        int tankX = leftPos + (BASE_IMAGE_WIDTH - TANK_WIDTH) / 2;
        int tankY = topPos + TOP_PADDING;
        int tankHeight = tankHeight();
        int innerX = tankX + 3;
        int innerY = tankY + 3;
        int innerWidth = TANK_WIDTH - 6;
        int innerHeight = tankHeight - 6;

        guiGraphics.fill(tankX, tankY, tankX + TANK_WIDTH, tankY + tankHeight, 0xD0121720);
        guiGraphics.renderOutline(tankX, tankY, TANK_WIDTH, tankHeight, 0xFF697487);
        guiGraphics.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, 0xAA091018);

        FluidStack storedFluid = menu.getDankInventory().getStoredFluid();
        int capacity = menu.getDankInventory().getFluidCapacity();
        if (storedFluid.isEmpty() || capacity <= 0) {
            guiGraphics.drawCenteredString(font, Component.translatable("dn.empty.desc"), tankX + (TANK_WIDTH / 2), tankY + (tankHeight / 2) - 4, 0xFF99A5B5);
            return;
        }

        int fillHeight = Math.max(1, Math.round(innerHeight * Math.min(1.0F, storedFluid.getAmount() / (float) capacity)));
        int fillY = innerY + innerHeight - fillHeight;
        IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(storedFluid.getFluid());
        ResourceLocation texture = clientFluid.getStillTexture(storedFluid);
        int tint = clientFluid.getTintColor(storedFluid);

        if (texture == null) {
            guiGraphics.fill(innerX, fillY, innerX + innerWidth, innerY + innerHeight, tint);
        } else {
            TextureAtlasSprite sprite = FluidSpriteCache.getSprite(texture);
            float alpha = ((tint >> 24) & 0xFF) / 255.0F;
            float red = ((tint >> 16) & 0xFF) / 255.0F;
            float green = ((tint >> 8) & 0xFF) / 255.0F;
            float blue = (tint & 0xFF) / 255.0F;
            for (int x = 0; x < innerWidth; x += 16) {
                for (int y = 0; y < fillHeight; y += 16) {
                    int drawWidth = Math.min(16, innerWidth - x);
                    int drawHeight = Math.min(16, fillHeight - y);
                    int drawX = innerX + x;
                    int drawY = innerY + innerHeight - fillHeight + y;
                    guiGraphics.blit(drawX, drawY, 0, drawWidth, drawHeight, sprite, red, green, blue, alpha <= 0.0F ? 1.0F : alpha);
                }
            }
        }

        guiGraphics.fill(innerX, fillY, innerX + innerWidth, fillY + 1, 0x66FFFFFF);
    }

    private void renderInfoPanel(GuiGraphics guiGraphics) {
        int panelX = leftPos + BASE_IMAGE_WIDTH + INFO_PANEL_GAP;
        int panelY = topPos + 8;
        int panelHeight = Math.max(INFO_PANEL_MIN_HEIGHT, measurePanelHeight());
        FluidStack storedFluid = menu.getDankInventory().getStoredFluid();

        guiGraphics.fill(panelX, panelY, panelX + INFO_PANEL_WIDTH, panelY + panelHeight, 0xD0121720);
        guiGraphics.renderOutline(panelX, panelY, INFO_PANEL_WIDTH, panelHeight, 0xFF697487);
        guiGraphics.fill(panelX + 1, panelY + 1, panelX + INFO_PANEL_WIDTH - 1, panelY + 13, 0xC01A2230);
        guiGraphics.drawString(font, Component.translatable("itemGroup." + DeepNullReforged.MODID), panelX + 6, panelY + 4, 0xFFFFFFFF, false);

        int lineY = panelY + 22;
        lineY = drawWrapped(guiGraphics, Component.translatable("dn.fluid.desc"), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFE8EDF5);

        if (storedFluid.isEmpty()) {
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.empty.desc"), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFFFFFFF);
            drawWrapped(guiGraphics, Component.translatable("dn.fluid_empty_hint.desc"), panelX + INFO_PANEL_PADDING, lineY + 4, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFF99A5B5);
            return;
        }

        lineY = drawWrapped(guiGraphics, storedFluid.getHoverName(), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFFFFFFF);
        lineY += 4;
        lineY = drawWrapped(guiGraphics, Component.translatable("dn.amount.desc").append(": ").append(Component.literal(storedFluid.getAmount() + " mB")), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFE8EDF5);
        lineY = drawWrapped(guiGraphics, Component.translatable("dn.capacity.desc").append(": ").append(fluidCapacityText()), panelX + INFO_PANEL_PADDING, lineY, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFFE8EDF5);
        drawWrapped(guiGraphics, Component.translatable("upgrade.fluid_upgrade.installed"), panelX + INFO_PANEL_PADDING, lineY + 4, INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2), 0xFF8FD8FF);
    }

    private void renderTankTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!isMouseOverTank(mouseX, mouseY)) {
            return;
        }

        FluidStack storedFluid = menu.getDankInventory().getStoredFluid();
        if (storedFluid.isEmpty()) {
            guiGraphics.renderTooltip(font, Component.translatable("dn.empty.desc"), mouseX, mouseY);
            return;
        }

        List<Component> tooltip = List.of(
                storedFluid.getHoverName(),
                Component.translatable("dn.amount.desc").append(": ").append(Component.literal(storedFluid.getAmount() + " mB")),
                Component.translatable("dn.capacity.desc").append(": ").append(fluidCapacityText())
        );
        guiGraphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
    }

    private boolean isMouseOverTank(int mouseX, int mouseY) {
        int tankX = leftPos + (BASE_IMAGE_WIDTH - TANK_WIDTH) / 2;
        int tankY = topPos + TOP_PADDING;
        int tankHeight = tankHeight();
        return mouseX >= tankX && mouseX < tankX + TANK_WIDTH && mouseY >= tankY && mouseY < tankY + tankHeight;
    }

    private int tankHeight() {
        return (menu.getTier().rows() * SLOT_SPACING) + 5;
    }

    private Component fluidCapacityText() {
        int capacity = menu.getDankInventory().getFluidCapacity();
        if (capacity == Integer.MAX_VALUE) {
            return Component.translatable("dn.infinite.desc");
        }
        return Component.literal(capacity + " mB");
    }

    private int measurePanelHeight() {
        FluidStack storedFluid = menu.getDankInventory().getStoredFluid();
        int width = INFO_PANEL_WIDTH - (INFO_PANEL_PADDING * 2);
        int contentLines = wrappedLineCount(Component.translatable("dn.fluid.desc"), width);
        if (storedFluid.isEmpty()) {
            contentLines += wrappedLineCount(Component.translatable("dn.empty.desc"), width);
            contentLines += wrappedLineCount(Component.translatable("dn.fluid_empty_hint.desc"), width);
            return 22 + (contentLines * INFO_PANEL_LINE_HEIGHT) + 12;
        }

        contentLines += wrappedLineCount(storedFluid.getHoverName(), width);
        contentLines += wrappedLineCount(Component.translatable("dn.amount.desc").append(": ").append(Component.literal(storedFluid.getAmount() + " mB")), width);
        contentLines += wrappedLineCount(Component.translatable("dn.capacity.desc").append(": ").append(fluidCapacityText()), width);
        contentLines += wrappedLineCount(Component.translatable("upgrade.fluid_upgrade.installed"), width);
        return 22 + (contentLines * INFO_PANEL_LINE_HEIGHT) + 12;
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

    private Component chargingLabel() {
        return Component.translatable(menu.getDankInventory().isChargingEnabled() ? "dn.charging_on.desc" : "dn.charging_off.desc");
    }

    private void syncSideButtons() {
        if (chargeButton != null) {
            boolean hasEnergyUpgrade = menu.getDankInventory().hasEnergyUpgrade();
            chargeButton.visible = hasEnergyUpgrade;
            chargeButton.active = hasEnergyUpgrade;
        }
    }
}
