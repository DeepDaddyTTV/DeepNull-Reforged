package dev.deepdaddyttv.deepnullreforged.fabric.mixin.client;

import dev.deepdaddyttv.deepnullreforged.client.DeepNullScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenMixin {
    @Redirect(
            method = "extractSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"
            )
    )
    private void deepnullreforged$renderCompactStorageCounts(
            GuiGraphicsExtractor instance,
            Font font,
            ItemStack itemStack,
            int x,
            int y,
            String countString,
            GuiGraphicsExtractor guiGraphics,
            Slot slot,
            int mouseX,
            int mouseY
    ) {
        if (!((Object) this instanceof DeepNullScreen deepNullScreen) || !deepNullScreen.isStorageItemSlot(slot) || itemStack.isEmpty()) {
            instance.itemDecorations(font, itemStack, x, y, countString);
            return;
        }

        instance.itemDecorations(font, itemStack.copyWithCount(1), x, y, "");
        String overlay = deepNullScreen.compactSlotCountText(itemStack);
        if (overlay != null && !overlay.isEmpty()) {
            deepNullScreen.renderStorageCountOverlay(guiGraphics, slot, overlay);
        }
    }
}
