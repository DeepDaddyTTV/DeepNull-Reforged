package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.inventory.NullSlotDomain;
import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

final class NullSlotSwapClientState {
    private static final long FLASH_DURATION_MS = 5_000L;
    private static final int PENDING_COLOR = 0xFFE8F3FF;
    private static final int FLASH_COLOR_RGB = 0x72D8FF;

    private Pending pending;
    private final Map<Key, Long> flashes = new HashMap<>();

    boolean hasPending(NullSlotDomain domain, int slot) {
        return pending != null && pending.domain == domain && pending.slot == slot;
    }

    int pendingSlot(NullSlotDomain domain) {
        return pending != null && pending.domain == domain ? pending.slot : -1;
    }

    void setPending(NullSlotDomain domain, int slot) {
        pending = new Pending(domain, slot);
    }

    void clearPending() {
        pending = null;
    }

    void flash(NullSlotDomain domain, int firstSlot, int secondSlot) {
        long now = System.currentTimeMillis();
        flashes.put(new Key(domain, firstSlot), now);
        flashes.put(new Key(domain, secondSlot), now);
    }

    void renderHighlight(GuiGraphics graphics, NullSlotDomain domain, int slot, int x, int y, int width, int height) {
        if (hasPending(domain, slot)) {
            graphics.renderOutline(x, y, width, height, PENDING_COLOR);
        }

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Key, Long>> iterator = flashes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Long> entry = iterator.next();
            long elapsed = now - entry.getValue();
            if (elapsed >= FLASH_DURATION_MS) {
                iterator.remove();
                continue;
            }
            if (entry.getKey().domain == domain && entry.getKey().slot == slot) {
                float remaining = 1.0F - (elapsed / (float) FLASH_DURATION_MS);
                int alpha = Math.max(24, Math.min(180, Math.round(180.0F * remaining)));
                graphics.renderOutline(x, y, width, height, (alpha << 24) | FLASH_COLOR_RGB);
            }
        }
    }

    private record Pending(NullSlotDomain domain, int slot) {
    }

    private record Key(NullSlotDomain domain, int slot) {
    }
}
