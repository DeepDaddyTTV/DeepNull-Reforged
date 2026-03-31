package dev.deepdaddyttv.deepnullreforged.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import dev.deepdaddyttv.deepnullreforged.compat.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class ClientFluidRendering {
    private ClientFluidRendering() {
    }

    public static @Nullable TextureAtlasSprite getStillSprite(FluidStack fluidStack) {
        FluidModel model = getModel(fluidStack);
        if (model == null) {
            return null;
        }
        return model.stillMaterial().sprite();
    }

    public static int getTint(FluidStack fluidStack) {
        FluidModel model = getModel(fluidStack);
        FluidState fluidState = fluidStack.getFluid().defaultFluidState();
        BlockState blockState = fluidState.createLegacyBlock();
        int tint = model == null ? -1 : model.tintSource().color(blockState);
        if ((tint >>> 24) == 0) {
            tint |= 0xFF000000;
        }
        return tint;
    }

    private static @Nullable FluidModel getModel(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return null;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return null;
        }

        Fluid fluid = fluidStack.getFluid();
        return minecraft.getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
    }
}
