package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
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
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class DeepNullUpgradeScreen extends AbstractContainerScreen<DeepNullMenu> {
    private static final Identifier BACKGROUND_TEXTURE = DeepNullReforged.id("textures/gui/deepnull_upgrade.png");
    private static final Identifier ENERGY_BACKGROUND_TEXTURE = DeepNullReforged.id("textures/gui/deepnull_upgrade_energy.png");
    private static final Identifier ENERGY_FILL_TEXTURE = DeepNullReforged.id("textures/gui/deepnull_upgrade_energy_fill.png");
    private static final int BASE_IMAGE_WIDTH = 202;
    private static final int ENERGY_IMAGE_WIDTH = 252;
    private static final int IMAGE_HEIGHT = 141;
    private static final int ENERGY_LABEL_X = 57;
    private static final int ENERGY_TOOLTIP_WIDTH = 50;

    private final boolean integratedEnergyGui;
    private final Identifier backgroundTexture;
    private Button filterButton;
    private Button autoSmeltButton;

    public DeepNullUpgradeScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, imageWidthFor(menu), IMAGE_HEIGHT);
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
                        ClientPacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.MAIN.ordinal())))
                .bounds(leftPos, topPos - 20, 50, 18)
                .build());

        filterButton = addRenderableWidget(Button.builder(Component.translatable("dn.configure_filter.desc"), button ->
                        ClientPacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.FILTER.ordinal())))
                .bounds(leftPos + 55, topPos - 20, 146, 18)
                .build());

        autoSmeltButton = addRenderableWidget(Button.builder(Component.translatable("dn.configure_auto_smelt.desc"), button ->
                        ClientPacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.AUTO_SMELT_FILTER.ordinal())))
                .bounds(leftPos + 55, topPos - 20, 146, 18)
                .build());

        layoutConfigButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        layoutConfigButtons();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        renderEnergyFill(graphics);

        for (int visibleIndex = 0; visibleIndex < menu.getUpgradeSlotCount(); visibleIndex++) {
            Slot upgradeSlot = menu.slots.get(menu.getUpgradeSlotStartIndex() + visibleIndex);
            DeepNullUpgradeType type = menu.getUpgradeTypeAt(visibleIndex);
            if (upgradeSlot.hasItem()) {
                continue;
            }

            if (menu.supportsUpgrade(type)) {
                renderUpgradePreview(graphics, type, upgradeSlot, 0x88000000);
                continue;
            }

            if (!menu.getDankInventory().isFluidOnly()) {
                renderUpgradePreview(graphics, type, upgradeSlot, 0x88441111);
            }
        }

        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, ClientUiText.upgradeScreenTitle(menu.getDankInventory().isFluidOnly()), titleLabelX, titleLabelY, 0xFFFFFFFF, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        renderUpgradeTooltip(graphics, mouseX, mouseY);
        renderEnergyTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        return event.button() == 2 && isWithin(event.x(), event.y(), leftPos, topPos, imageWidth, imageHeight);
    }

    private void renderUpgradePreview(GuiGraphicsExtractor graphics, DeepNullUpgradeType type, Slot slot, int overlayColor) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, iconTexture(type), leftPos + slot.x, topPos + slot.y, 0.0F, 0.0F, 16, 16, 16, 16);
        graphics.fill(
                leftPos + slot.x,
                topPos + slot.y,
                leftPos + slot.x + 16,
                topPos + slot.y + 16,
                overlayColor
        );
    }

    private Identifier iconTexture(DeepNullUpgradeType type) {
        String placeholderName = switch (type) {
            case FILTER -> "filter_upgrade_placeholder";
            case FLUID -> "fluid_upgrade_placeholder";
            case ENERGY -> "energy_upgrade_placeholder";
            case DEEP_ENERGY -> "deep_energy_upgrade_placeholder";
            case AUTO_FEEDING -> "auto_feeding_upgrade_placeholder";
            case AUTO_SMELTING -> "auto_smelting_upgrade_placeholder";
            case BASIC_COMPRESSION -> "basic_compression_upgrade_placeholder";
            case ADVANCED_COMPRESSION -> "advanced_compression_upgrade_placeholder";
            case STONEWORKS -> "stoneworks_upgrade_placeholder";
            case STONE_GENERATOR -> "stone_generator_upgrade_placeholder";
            case OBSIDIAN_GENERATOR -> "obsidian_generator_upgrade_placeholder";
            case SPONGE -> "sponge_upgrade_placeholder";
            case GAS -> "gas_upgrade_placeholder";
            case ENDER -> "ender_upgrade_placeholder";
        };
        Identifier widgetTexture = DeepNullReforged.id("textures/gui/widgets/" + placeholderName + ".png");
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
            case STONEWORKS -> "stoneworks_upgrade";
            case STONE_GENERATOR -> "stone_generator_upgrade";
            case OBSIDIAN_GENERATOR -> "obsidian_generator_upgrade";
            case SPONGE -> "sponge_upgrade";
            case GAS -> "gas_upgrade";
            case ENDER -> "ender_upgrade";
        };
        return DeepNullReforged.id("textures/item/" + itemIconName + ".png");
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

    private void renderUpgradeTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Slot hovered = hoveredSlot;
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
        graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
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

    private static boolean isWithin(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int imageWidthFor(DeepNullMenu menu) {
        return menu.hasEnergyUpgrade() && menu.getTier().supportsEnergyUpgrade() ? ENERGY_IMAGE_WIDTH : BASE_IMAGE_WIDTH;
    }
}
