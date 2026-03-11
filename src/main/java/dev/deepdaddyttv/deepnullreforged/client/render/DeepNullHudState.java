package dev.deepdaddyttv.deepnullreforged.client.render;

import dev.deepdaddyttv.deepnullreforged.client.ClientDeepNullAccess;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullContentMode;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public final class DeepNullHudState {
    private static final long DISPLAY_MS = 5_000L;

    private static HudKey lastKey = HudKey.EMPTY;
    private static long visibleUntil;

    private DeepNullHudState() {
    }

    public static void tick(Player player) {
        HudKey nextKey = HudKey.from(ClientDeepNullAccess.findHeldDeepNull(player));
        if (!nextKey.equals(lastKey)) {
            lastKey = nextKey;
            visibleUntil = nextKey.active() ? Util.getMillis() + DISPLAY_MS : 0L;
        }
    }

    public static boolean shouldRender() {
        return visibleUntil > Util.getMillis();
    }

    public static void clear() {
        lastKey = HudKey.EMPTY;
        visibleUntil = 0L;
    }

    private record HudKey(int inventorySlot, int outerHash, int selectedSlot, int selectedHash, int selectedFluidHash, DeepNullContentMode mode) {
        private static final HudKey EMPTY = new HudKey(-1, 0, -1, 0, 0, DeepNullContentMode.ITEMS);

        private static HudKey from(ClientDeepNullAccess.HeldDeepNull held) {
            if (held == null) {
                return EMPTY;
            }

            FluidStack selectedFluid = held.inventory().getSelectedFluid();

            return new HudKey(
                    held.inventorySlot(),
                    ItemStack.hashItemAndComponents(held.stack()),
                    held.inventory().getSelectedSlot(),
                    ItemStack.hashItemAndComponents(held.inventory().getSelectedStack()),
                    selectedFluid.isEmpty() ? 0 : selectedFluid.hashCode(),
                    held.inventory().getContentMode()
            );
        }

        private boolean active() {
            return inventorySlot >= 0
                    && selectedSlot >= 0
                    && ((mode == DeepNullContentMode.FLUIDS && selectedFluidHash != 0)
                    || (mode != DeepNullContentMode.FLUIDS && selectedHash != 0));
        }
    }
}
