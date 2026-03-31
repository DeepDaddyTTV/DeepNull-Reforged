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
import org.lwjgl.glfw.GLFW;

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

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void deepnullreforged$handleInvertedDampNullUse(long window, int button, int action, int modifiers, CallbackInfo ci) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_RIGHT || action != GLFW.GLFW_PRESS) {
            return;
        }
        if (ClientGameEvents.handleInvertedDampNullUse()) {
            ci.cancel();
        }
    }
}
