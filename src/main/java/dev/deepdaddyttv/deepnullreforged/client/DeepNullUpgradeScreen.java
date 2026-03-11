package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeepNullUpgradeScreen extends AbstractContainerScreen<DeepNullMenu> {
    private final ResourceLocation backgroundTexture = DeepNullTier.REDSTONE.guiTexture();
    private Button filterButton;

    public DeepNullUpgradeScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 201;
        this.imageHeight = 141;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = this.imageHeight - 103;
        this.titleLabelX = 7;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(Button.builder(Component.translatable("dn.back.desc"), button ->
                        PacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.MAIN.ordinal())))
                .bounds(leftPos, topPos - 20, 50, 18)
                .build());

        filterButton = Button.builder(Component.translatable("dn.configure_filter.desc"), button ->
                        PacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.FILTER.ordinal())))
                .bounds(leftPos + 55, topPos - 20, 146, 18)
                .build();
        filterButton.visible = menu.hasUpgrade(DeepNullUpgradeType.FILTER);
        filterButton.active = menu.hasUpgrade(DeepNullUpgradeType.FILTER);
        addRenderableWidget(filterButton);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (filterButton != null) {
            boolean hasFilter = menu.hasUpgrade(DeepNullUpgradeType.FILTER);
            filterButton.visible = hasFilter;
            filterButton.active = hasFilter;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(backgroundTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        DeepNullEnergyWidget.render(guiGraphics, menu.getDankInventory(), leftPos, topPos);
        for (int slot = 0; slot < menu.getUpgradeSlotCount(); slot++) {
            DeepNullUpgradeType type = DeepNullUpgradeType.bySlot(slot);
            if (menu.supportsUpgrade(DeepNullUpgradeType.bySlot(slot))) {
                Slot upgradeSlot = menu.slots.get(menu.getUpgradeSlotStartIndex() + slot);
                renderUpgradePreview(guiGraphics, type, upgradeSlot);
                continue;
            }
            Slot upgradeSlot = menu.slots.get(menu.getUpgradeSlotStartIndex() + slot);
            renderUpgradePreview(guiGraphics, type, upgradeSlot);
            guiGraphics.fill(leftPos + upgradeSlot.x, topPos + upgradeSlot.y, leftPos + upgradeSlot.x + 16, topPos + upgradeSlot.y + 16, 0x88441111);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, Component.translatable("dn.upgrades_screen.desc"), titleLabelX, titleLabelY, 0xFFFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
        DeepNullEnergyWidget.renderTooltip(guiGraphics, font, menu.getDankInventory(), leftPos, topPos, mouseX, mouseY);
        renderUpgradeTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderUpgradePreview(GuiGraphics guiGraphics, DeepNullUpgradeType type, Slot slot) {
        if (slot.hasItem()) {
            return;
        }

        guiGraphics.blit(iconTexture(type), leftPos + slot.x, topPos + slot.y, 0.0F, 0.0F, 16, 16, 16, 16);
        guiGraphics.fill(
                leftPos + slot.x,
                topPos + slot.y,
                leftPos + slot.x + 16,
                topPos + slot.y + 16,
                menu.supportsUpgrade(type) ? 0x88000000 : 0xAA220000
        );
    }

    private ResourceLocation iconTexture(DeepNullUpgradeType type) {
        String placeholderName = switch (type) {
            case FILTER -> "filter_upgrade_placeholder";
            case FLUID -> "fluid_upgrade_placeholder";
            case ENERGY -> "energy_upgrade_placeholder";
        };
        ResourceLocation widgetTexture = DeepNullReforged.id("textures/gui/widgets/" + placeholderName + ".png");
        if (Minecraft.getInstance().getResourceManager().getResource(widgetTexture).isPresent()) {
            return widgetTexture;
        }
        String itemIconName = switch (type) {
            case FILTER -> "filter_upgrade";
            case FLUID -> "fluid_upgrade";
            case ENERGY -> "energy_upgrade";
        };
        return DeepNullReforged.id("textures/item/" + itemIconName + ".png");
    }

    private void renderUpgradeTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Slot hovered = getSlotUnderMouse();
        if (!(hovered instanceof DeepNullMenu.UpgradeSlot) || hovered.hasItem()) {
            return;
        }

        DeepNullUpgradeType type = DeepNullUpgradeType.bySlot(hovered.index);
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("item." + DeepNullReforged.MODID + "." + type.itemId()));
        tooltip.add(Component.translatable("upgrade." + type.itemId() + ".desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("upgrade." + type.itemId() + ".tiers", supportedTierText(type)).withStyle(ChatFormatting.DARK_GRAY));
        if (!menu.supportsUpgrade(type)) {
            tooltip.add(Component.translatable("dn.upgrade_unavailable.desc").withStyle(ChatFormatting.RED));
        }
        guiGraphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
    }

    private Component supportedTierText(DeepNullUpgradeType type) {
        return switch (type) {
            case FILTER -> Component.translatable("upgrade.tiers.iron_plus");
            case FLUID -> Component.translatable("upgrade.tiers.every_tier");
            case ENERGY -> Component.translatable("upgrade.tiers.diamond_plus");
        };
    }
}
