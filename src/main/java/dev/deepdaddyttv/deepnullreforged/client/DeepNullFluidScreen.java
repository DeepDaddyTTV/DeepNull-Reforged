package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
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
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class DeepNullFluidScreen extends AbstractContainerScreen<DeepNullMenu> {
    private static final int BASE_IMAGE_WIDTH = 202;

    private final Identifier backgroundTexture;
    private final Identifier tankOverlayTexture;
    private Button transferButton;

    public DeepNullFluidScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, BASE_IMAGE_WIDTH, imageHeightFor(menu));
        this.backgroundTexture = DeepNullReforged.id("textures/gui/dampnullscreen" + menu.getTier().ordinalId() + ".png");
        this.tankOverlayTexture = DeepNullReforged.id("textures/gui/dampnullscreen" + menu.getTier().ordinalId() + "_tank.png");
        this.inventoryLabelX = 7;
        this.inventoryLabelY = this.imageHeight - 103;
        this.titleLabelX = 7;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(Button.builder(Component.translatable("dn.main_screen.desc"), button ->
                ClientPacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.MAIN.ordinal()))
        ).bounds(this.leftPos, this.topPos - 20, 50, 18).build());

        addRenderableWidget(Button.builder(Component.translatable("dn.upgrades_screen.desc"), button ->
                ClientPacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.UPGRADES.ordinal()))
        ).bounds(this.leftPos + 55, this.topPos - 20, 70, 18).build());

        transferButton = addRenderableWidget(Button.builder(transferLabel(), button -> {
            boolean next = !menu.getDankInventory().isTransferLocked();
            menu.getDankInventory().setTransferLocked(next);
            button.setMessage(transferLabel());
            ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuTransferLockPayload(next));
        }).bounds(this.leftPos + 130, this.topPos - 20, 72, 18).build());
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        renderTankContents(graphics);
        graphics.blit(RenderPipelines.GUI_TEXTURED, tankOverlayTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0xFFFFFFFF, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFFFFFFF, false);
        if (transferButton != null) {
            transferButton.setMessage(transferLabel());
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int hoveredSlot = getHoveredTankSlot(mouseX, mouseY);
        if (hoveredSlot < 0) {
            return;
        }

        FluidStack fluidStack = menu.getDankInventory().getFluidInSlot(hoveredSlot);
        if (!fluidStack.isEmpty()) {
            graphics.setComponentTooltipForNextFrame(
                    font,
                    List.of(
                            fluidStack.getHoverName().copy().withStyle(ChatFormatting.AQUA),
                            Component.literal(DeepNullCountFormatter.formatExact(fluidStack.getAmount()) + " mB").withStyle(ChatFormatting.GRAY)
                    ),
                    mouseX,
                    mouseY
            );
            return;
        }

        StoredChemical chemical = menu.getDankInventory().getChemicalInSlot(hoveredSlot);
        if (!chemical.isEmpty()) {
            graphics.setComponentTooltipForNextFrame(
                    font,
                    List.of(
                            chemical.getHoverName().copy().withStyle(ChatFormatting.AQUA),
                            Component.literal(Long.toString(chemical.amount())).withStyle(ChatFormatting.GRAY)
                    ),
                    mouseX,
                    mouseY
            );
        }
    }

    private void renderTankContents(GuiGraphicsExtractor graphics) {
        for (int slotIndex = 0; slotIndex < menu.getStorageSlotCount(); slotIndex++) {
            Slot slot = menu.slots.get(slotIndex);
            FluidStack fluidStack = menu.getDankInventory().getFluidInSlot(slotIndex);
            if (!fluidStack.isEmpty()) {
                ItemStack bucket = fluidStack.getFluidType().getBucket(fluidStack);
                if (!bucket.isEmpty()) {
                    graphics.item(bucket, leftPos + slot.x, topPos + slot.y);
                }
                continue;
            }

            StoredChemical chemical = menu.getDankInventory().getChemicalInSlot(slotIndex);
            if (!chemical.isEmpty()) {
                int x = leftPos + slot.x;
                int y = topPos + slot.y;
                graphics.fill(x + 2, y + 2, x + 14, y + 14, 0xCC6FA7D8);
                graphics.text(font, "C", x + 5, y + 4, 0xFFFFFFFF, false);
            }
        }
    }

    private int getHoveredTankSlot(int mouseX, int mouseY) {
        for (int slotIndex = 0; slotIndex < menu.getStorageSlotCount(); slotIndex++) {
            Slot slot = menu.slots.get(slotIndex);
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                return slotIndex;
            }
        }
        return -1;
    }

    private Component transferLabel() {
        return Component.translatable(menu.getDankInventory().isTransferLocked() ? "dn.unlock.desc" : "dn.lock.desc");
    }

    public boolean toggleTransferLock() {
        boolean next = !menu.getDankInventory().isTransferLocked();
        menu.setTransferLocked(next);
        if (transferButton != null) {
            transferButton.setMessage(transferLabel());
        }
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuTransferLockPayload(next));
        return next;
    }

    private static int imageHeightFor(DeepNullMenu menu) {
        return 141 + Math.max(0, menu.getTier().rows() - 1) * 21;
    }
}
