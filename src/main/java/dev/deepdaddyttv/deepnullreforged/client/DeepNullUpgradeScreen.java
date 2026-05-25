package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.client.theme.DeepNullUiPalette;
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
    private static final ResourceLocation BACKGROUND_TEXTURE = DeepNullReforged.id("textures/gui/deepnull_upgrade.png");
    private static final ResourceLocation ENERGY_BACKGROUND_TEXTURE = DeepNullReforged.id("textures/gui/deepnull_upgrade_energy.png");
    private static final ResourceLocation ENERGY_FILL_TEXTURE = DeepNullReforged.id("textures/gui/deepnull_upgrade_energy_fill.png");
    private static final int BASE_IMAGE_WIDTH = 202;
    private static final int ENERGY_IMAGE_WIDTH = 252;
    private static final int ENERGY_LABEL_X = 57;
    private static final int ENERGY_TOOLTIP_WIDTH = 50;

    private final Inventory playerInventory;
    private final boolean integratedEnergyGui;
    private final ResourceLocation backgroundTexture;
    private Button filterButton;
    private Button autoSmeltButton;

    public DeepNullUpgradeScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.playerInventory = playerInventory;
        this.integratedEnergyGui = menu.hasEnergyUpgrade() && menu.getTier().supportsEnergyUpgrade();
        this.backgroundTexture = integratedEnergyGui ? ENERGY_BACKGROUND_TEXTURE : BACKGROUND_TEXTURE;
        this.imageWidth = integratedEnergyGui ? ENERGY_IMAGE_WIDTH : BASE_IMAGE_WIDTH;
        this.imageHeight = 141;
        this.inventoryLabelX = integratedEnergyGui ? ENERGY_LABEL_X : 7;
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
        addRenderableWidget(filterButton);

        autoSmeltButton = Button.builder(Component.translatable("dn.configure_auto_smelt.desc"), button ->
                        PacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.AUTO_SMELT_FILTER.ordinal())))
                .bounds(leftPos + 55, topPos - 20, 146, 18)
                .build();
        addRenderableWidget(autoSmeltButton);
        layoutConfigButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (filterButton != null) {
            layoutConfigButtons();
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        DeepNullUiPalette palette = DeepNullUiPalette.of(DeepNullConfig.uiTheme());
        guiGraphics.blit(backgroundTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        renderEnergyFill(guiGraphics);
        boolean fluidOnly = menu.getDankInventory().isFluidOnly();
        for (int slot = 0; slot < menu.getUpgradeSlotCount(); slot++) {
            DeepNullUpgradeType type = menu.getUpgradeTypeAt(slot);
            if (menu.supportsUpgrade(type)) {
                Slot upgradeSlot = menu.slots.get(menu.getUpgradeSlotStartIndex() + slot);
                renderUpgradePreview(guiGraphics, type, upgradeSlot, palette);
                continue;
            }
            if (fluidOnly) {
                continue;
            }
            Slot upgradeSlot = menu.slots.get(menu.getUpgradeSlotStartIndex() + slot);
            renderUpgradePreview(guiGraphics, type, upgradeSlot, palette);
            guiGraphics.fill(leftPos + upgradeSlot.x, topPos + upgradeSlot.y, leftPos + upgradeSlot.x + 16, topPos + upgradeSlot.y + 16, palette.slotUnavailableOverlayColor());
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component screenTitle = ClientUiText.upgradeScreenTitle(menu.getDankInventory().isFluidOnly());
        guiGraphics.drawString(font, screenTitle, titleLabelX, titleLabelY, DeepNullUiPalette.of(DeepNullConfig.uiTheme()).screenTitleTextColor(), false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderUpgradeTooltip(guiGraphics, mouseX, mouseY);
        renderEnergyTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 2 && isWithin(mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderUpgradePreview(GuiGraphics guiGraphics, DeepNullUpgradeType type, Slot slot, DeepNullUiPalette palette) {
        if (slot.hasItem()) {
            return;
        }

        guiGraphics.blit(iconTexture(type), leftPos + slot.x, topPos + slot.y, 0.0F, 0.0F, 16, 16, 16, 16);
        guiGraphics.fill(
                leftPos + slot.x,
                topPos + slot.y,
                leftPos + slot.x + 16,
                topPos + slot.y + 16,
                menu.supportsUpgrade(type) ? palette.slotPreviewOverlayColor() : palette.slotUnavailableOverlayColor()
        );
    }

    private ResourceLocation iconTexture(DeepNullUpgradeType type) {
        String placeholderName = switch (type) {
            case FILTER -> "filter_upgrade_placeholder";
            case FLUID -> "fluid_upgrade_placeholder";
            case ENERGY -> "energy_upgrade_placeholder";
            case DEEP_ENERGY -> "deep_energy_upgrade_placeholder";
            case AUTO_FEEDING -> "auto_feeding_upgrade_placeholder";
            case AUTO_SMELTING -> "auto_smelting_upgrade_placeholder";
            case BASIC_COMPRESSION -> "basic_compression_upgrade_placeholder";
            case ADVANCED_COMPRESSION -> "advanced_compression_upgrade_placeholder";
            case DIVNULL -> "divnull_upgrade_placeholder";
            case STONEWORKS -> "stoneworks_upgrade_placeholder";
            case STONE_GENERATOR -> "stone_generator_upgrade_placeholder";
            case OBSIDIAN_GENERATOR -> "obsidian_generator_upgrade_placeholder";
            case SPONGE -> "sponge_upgrade_placeholder";
            case BALLOON -> "balloon_upgrade_placeholder";
            case GAS -> "gas_upgrade_placeholder";
            case ENDER -> "ender_upgrade_placeholder";
            case FARM -> "farm_upgrade_placeholder";
        };
        ResourceLocation widgetTexture = DeepNullReforged.id("textures/gui/widgets/" + placeholderName + ".png");
        if (Minecraft.getInstance().getResourceManager().getResource(widgetTexture).isPresent()) {
            return widgetTexture;
        }
        String itemIconName = switch (type) {
            case FILTER -> "filter_upgrade";
            case FLUID -> "fluid_upgrade";
            case ENERGY -> "energy_upgrade";
            case DEEP_ENERGY -> "deep_energy_upgrade";
            case AUTO_FEEDING -> "auto_feeding_upgrade";
            case AUTO_SMELTING -> "auto_smelting_upgrade";
            case BASIC_COMPRESSION -> "basic_compression_upgrade";
            case ADVANCED_COMPRESSION -> "advanced_compression_upgrade";
            case DIVNULL -> "divnull_upgrade";
            case STONEWORKS -> "stoneworks_upgrade";
            case STONE_GENERATOR -> "stone_generator_upgrade";
            case OBSIDIAN_GENERATOR -> "obsidian_generator_upgrade";
            case SPONGE -> "sponge_upgrade";
            case BALLOON -> "balloon_upgrade";
            case GAS -> "gas_upgrade";
            case ENDER -> "ender_upgrade";
            case FARM -> "farm_upgrade";
        };
        if (itemIconName != null) {
            return DeepNullReforged.id("textures/item/" + itemIconName + ".png");
        }
        return DeepNullReforged.id("textures/item/filter_upgrade.png");
    }

    private void layoutConfigButtons() {
        if (filterButton == null || autoSmeltButton == null) {
            return;
        }

        boolean hasFilter = menu.hasUpgrade(DeepNullUpgradeType.FILTER);
        boolean hasAutoSmelt = menu.hasUpgrade(DeepNullUpgradeType.AUTO_SMELTING);

        filterButton.visible = hasFilter;
        filterButton.active = hasFilter;
        autoSmeltButton.visible = hasAutoSmelt;
        autoSmeltButton.active = hasAutoSmelt;

        if (hasFilter && hasAutoSmelt) {
            filterButton.setPosition(leftPos + 55, topPos - 20);
            filterButton.setWidth(71);
            autoSmeltButton.setPosition(leftPos + 130, topPos - 20);
            autoSmeltButton.setWidth(71);
            return;
        }

        if (hasFilter) {
            filterButton.setPosition(leftPos + 55, topPos - 20);
            filterButton.setWidth(146);
        }

        if (hasAutoSmelt) {
            autoSmeltButton.setPosition(leftPos + 55, topPos - 20);
            autoSmeltButton.setWidth(146);
        }
    }

    private void renderUpgradeTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Slot hovered = getSlotUnderMouse();
        if (!(hovered instanceof DeepNullMenu.UpgradeSlot) || hovered.hasItem()) {
            return;
        }

        DeepNullUpgradeType type = ((DeepNullMenu.UpgradeSlot) hovered).getUpgradeType();
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("item." + DeepNullReforged.MODID + "." + type.itemId()));
        tooltip.add(Component.translatable("upgrade." + type.itemId() + ".desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(ClientUiText.upgradeKindText(menu.getDankInventory().isFluidOnly(), type).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("upgrade." + type.itemId() + ".tiers", ClientUiText.supportedTierText(type)).withStyle(ChatFormatting.DARK_GRAY));
        if (!menu.supportsUpgrade(type)) {
            tooltip.add(Component.translatable("dn.upgrade_unavailable.desc").withStyle(ChatFormatting.RED));
        }
        guiGraphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
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
                List.of(
                        Component.translatable("dn.energy.desc"),
                        Component.literal(menu.getDisplayedEnergyStored() + " / " + menu.getDisplayedEnergyCapacity() + " FE")
                ),
                Optional.empty(),
                mouseX,
                mouseY
        );
    }

    private static boolean isWithin(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
