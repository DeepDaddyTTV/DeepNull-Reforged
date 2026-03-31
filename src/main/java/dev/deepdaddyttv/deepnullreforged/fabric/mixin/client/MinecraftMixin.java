package dev.deepdaddyttv.deepnullreforged.fabric.mixin.client;

import dev.deepdaddyttv.deepnullreforged.fabric.client.DeepNullCiSmokeClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Inject(method = "setScreen", at = @At("TAIL"))
    private void deepnull$onSetScreen(Screen screen, CallbackInfo ci) {
        DeepNullCiSmokeClient.onScreenSet((Minecraft) (Object) this, screen);
    }
}
