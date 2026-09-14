package dev.deepdaddyttv.deepnullreforged.client.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NullWorkbenchRendererTest {
    @Test
    void adjacentDisplayIsNotCulledWithTheOwningTableHalf() {
        var context = new BlockEntityRendererProvider.Context(null, null, null, null, null, null, null, null);
        var renderer = new NullWorkbenchRenderer(context);
        assertTrue(renderer.shouldRenderOffScreen());
    }

    @Test
    void boundsIncludeBothHalvesAndTheDisplayForEveryFacing() {
        BlockPos main = new BlockPos(15, 64, 15);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            AABB bounds = NullWorkbenchRenderer.displayBounds(main, facing);
            assertTrue(bounds.contains(Vec3.atCenterOf(main)));
            assertTrue(bounds.contains(Vec3.atCenterOf(main.relative(facing.getCounterClockWise())).add(0, 0.8, 0)));
            assertEquals(2.0, bounds.maxY - bounds.minY);
        }
    }

    @Test
    void modelBottomRestsAboveTableRegardlessOfModelOrigin() {
        for (double minY : new double[] {-0.5, -0.25, 0.0, 0.25}) {
            AABB model = new AABB(-0.25, minY, -0.25, 0.25, minY + 0.5, 0.25);
            double centerY = NullWorkbenchRenderer.displayCenterY(model);
            assertEquals(0.76, centerY + minY * 0.85F, 0.000001);
        }
    }
}
