package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class DeepNullScreen extends AbstractContainerScreen<DeepNullMenu> {
    private static final int BASE_IMAGE_WIDTH = 202;
    private static final int ENERGY_IMAGE_WIDTH = 252;

    private final Identifier backgroundTexture;
    private final boolean integratedEnergyGui;
    private Button lockButton;

    public DeepNullScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, imageWidthFor(menu), imageHeightFor(menu));
        this.integratedEnergyGui = menu.hasEnergyUpgrade() && menu.getTier().supportsEnergyUpgrade();
        this.backgroundTexture = integratedEnergyGui ? integratedEnergyTexture(menu) : menu.getTier().guiTexture();
        this.inventoryLabelX = integratedEnergyGui ? 57 : 7;
        this.inventoryLabelY = this.imageHeight - 103;
        this.titleLabelX = 7;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        if (menu.getDankInventory().supportsLocking()) {
            lockButton = addRenderableWidget(Button.builder(lockLabel(), button -> {
                boolean next = !menu.getDankInventory().isLocked();
                menu.getDankInventory().setLocked(next);
                button.setMessage(lockLabel());
                ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuLockPayload(next));
            }).bounds(this.leftPos + this.imageWidth - 54, this.topPos - 20, 50, 18).build());
        }

        addRenderableWidget(Button.builder(Component.translatable("dn.upgrades_screen.desc"), button ->
                ClientPacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.UPGRADES.ordinal()))
        ).bounds(this.leftPos, this.topPos - 20, 50, 18).build());
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        DeepNullEnergyWidget.render(graphics, menu, leftPos, topPos);
        highlightSelectedSlot(graphics);
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0xFFFFFFFF, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFFFFFFF, false);
        if (lockButton != null) {
            lockButton.setMessage(lockLabel());
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        DeepNullEnergyWidget.renderTooltip(graphics, font, menu, leftPos, topPos, mouseX, mouseY);
    }

    @Override
    protected void renderSlotContents(GuiGraphicsExtractor graphics, ItemStack stack, Slot slot, String countString) {
        if (isStorageItemSlot(slot) && !stack.isEmpty()) {
            super.renderSlotContents(graphics, stack.copyWithCount(1), slot, "");
            String overlay = compactSlotCountText(stack);
            if (overlay != null && !overlay.isEmpty()) {
                graphics.text(font, overlay, leftPos + slot.x + 17 - font.width(overlay), topPos + slot.y + 9, 0xFFFFFFFF, true);
            }
            return;
        }
        super.renderSlotContents(graphics, stack, slot, countString);
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> tooltip = new ArrayList<>(super.getTooltipFromContainerItem(stack));
        if (hoveredSlot == null || !isStorageItemSlot(hoveredSlot) || stack.isEmpty()) {
            return tooltip;
        }

        Component countLine = Component.literal("")
                .append(Component.translatable(
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

    private boolean isStorageItemSlot(Slot slot) {
        int slotIndex = menu.slots.indexOf(slot);
        return slotIndex >= 0 && slotIndex < menu.getStorageSlotCount();
    }

    private Component lockLabel() {
        return Component.translatable(menu.getDankInventory().isLocked() ? "dn.unlock.desc" : "dn.lock.desc");
    }

    public boolean toggleTransferLock() {
        boolean next = !menu.getDankInventory().isTransferLocked();
        menu.setTransferLocked(next);
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuTransferLockPayload(next));
        return next;
    }

    private String compactSlotCountText(ItemStack stack) {
        return menu.getDankInventory().supportsLocking() && menu.getDankInventory().isLocked()
                ? "INF"
                : DeepNullCountFormatter.formatCompact(stack.getCount());
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
}
