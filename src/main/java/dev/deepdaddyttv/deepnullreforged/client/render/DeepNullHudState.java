package dev.deepdaddyttv.deepnullreforged.client.render;

import dev.deepdaddyttv.deepnullreforged.client.ClientDeepNullAccess;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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

    private record HudKey(int inventorySlot, int outerHash, int selectedSlot, int selectedHash) {
        private static final HudKey EMPTY = new HudKey(-1, 0, -1, 0);

        private static HudKey from(ClientDeepNullAccess.HeldDeepNull held) {
            if (held == null) {
                return EMPTY;
            }

            return new HudKey(
                    held.inventorySlot(),
                    ItemStack.hashItemAndComponents(held.stack()),
                    held.inventory().getSelectedSlot(),
                    ItemStack.hashItemAndComponents(held.inventory().getSelectedStack())
            );
        }

        private boolean active() {
            return inventorySlot >= 0 && selectedSlot >= 0 && selectedHash != 0;
        }
    }
}
