package dev.deepdaddyttv.deepnullreforged.fabric.mixin.client;

import dev.deepdaddyttv.deepnullreforged.client.ClientGameEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void deepnullreforged$handleShiftScrollSelection(long window, double xOffset, double yOffset, CallbackInfo ci) {
        if (ClientGameEvents.handleShiftScrollSelection(minecraft.player, yOffset)) {
            ci.cancel();
        }
    }
}
