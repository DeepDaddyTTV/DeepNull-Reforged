package dev.deepdaddyttv.deepnullreforged.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullNetwork;
import dev.deepdaddyttv.deepnullreforged.network.SelectSlotPacket;
import net.minecraft.util.Hand;

public class DeepNullScreen extends ContainerScreen<DeepNullMenu> {
    private final ResourceLocation texture;

    public DeepNullScreen(DeepNullMenu menu, PlayerInventory inventory, ITextComponent title) {
        super(menu, inventory, title);
        int textureIndex = menu.getTier().creative() ? 6 : menu.getTier().rows() - 1;
        this.texture = DeepNullReforged.id("textures/gui/deepnullscreen" + textureIndex + ".png");
        this.imageWidth = 176;
        this.imageHeight = 114 + menu.getTier().rows() * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrix);
        super.render(matrix, mouseX, mouseY, partialTicks);
        renderTooltip(matrix, mouseX, mouseY);
    }

    @Override
    protected void renderBg(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bind(texture);
        blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 2 && hoveredSlot != null) {
            int slot = menu.slots.indexOf(hoveredSlot);
            if (slot >= 0 && slot < menu.getTier().slots()) {
                minecraft.player.getMainHandItem().getOrCreateTag().putInt(
                        dev.deepdaddyttv.deepnullreforged.item.DeepNullItem.SELECTED_SLOT_TAG, slot);
                DeepNullNetwork.CHANNEL.sendToServer(new SelectSlotPacket(slot, Hand.MAIN_HAND));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
