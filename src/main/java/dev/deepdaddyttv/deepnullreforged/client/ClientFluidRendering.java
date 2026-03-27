package dev.deepdaddyttv.deepnullreforged.client;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class ClientFluidRendering {
    private ClientFluidRendering() {
    }

    public static @Nullable TextureAtlasSprite getStillSprite(FluidStack fluidStack) {
        FluidRenderHandler handler = getHandler(fluidStack);
        if (handler == null) {
            return null;
        }

        TextureAtlasSprite[] sprites = handler.getFluidSprites(null, null, fluidStack.getFluid().defaultFluidState());
        if (sprites == null || sprites.length == 0) {
            return null;
        }
        return sprites[0];
    }

    public static int getTint(FluidStack fluidStack) {
        FluidRenderHandler handler = getHandler(fluidStack);
        int tint = handler == null ? -1 : handler.getFluidColor(null, null, fluidStack.getFluid().defaultFluidState());
        if ((tint >>> 24) == 0) {
            tint |= 0xFF000000;
        }
        return tint;
    }

    private static @Nullable FluidRenderHandler getHandler(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return null;
        }

        Fluid fluid = fluidStack.getFluid();
        return FluidRenderHandlerRegistry.INSTANCE.get(fluid);
    }
}
