package dev.deepdaddyttv.deepnullreforged.fabric.mixin.client;

import dev.deepdaddyttv.deepnullreforged.client.DeepNullScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenMixin {
    @Redirect(
            method = "renderSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"
            )
    )
    private void deepnullreforged$renderCompactStorageCounts(
            GuiGraphics instance,
            Font font,
            ItemStack itemStack,
            int x,
            int y,
            String countString,
            GuiGraphics guiGraphics,
            Slot slot
    ) {
        if (!((Object) this instanceof DeepNullScreen deepNullScreen) || !deepNullScreen.isStorageItemSlot(slot) || itemStack.isEmpty()) {
            instance.renderItemDecorations(font, itemStack, x, y, countString);
            return;
        }

        instance.renderItemDecorations(font, itemStack.copyWithCount(1), x, y, "");
        String overlay = deepNullScreen.compactSlotCountText(itemStack);
        if (overlay != null && !overlay.isEmpty()) {
            deepNullScreen.renderStorageCountOverlay(guiGraphics, slot, overlay);
        }
    }
}
