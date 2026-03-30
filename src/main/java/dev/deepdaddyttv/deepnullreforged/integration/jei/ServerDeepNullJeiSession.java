package dev.deepdaddyttv.deepnullreforged.integration.jei;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerDeepNullJeiSession {
    private static final Map<UUID, Session> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    private ServerDeepNullJeiSession() {
    }

    public static void mark(Player player, AbstractContainerMenu menu) {
        mark(player, menu, List.of());
    }

    public static void mark(Player player, AbstractContainerMenu menu, List<Integer> preferredInventorySlots) {
        ACTIVE_SESSIONS.put(player.getUUID(), new Session(menu.containerId, List.copyOf(preferredInventorySlots)));
    }

    public static boolean shouldReturn(Player player, AbstractContainerMenu menu) {
        Session session = ACTIVE_SESSIONS.get(player.getUUID());
        return session != null && session.menuId() == menu.containerId;
    }

    public static List<Integer> preferredInventorySlots(Player player, AbstractContainerMenu menu) {
        Session session = ACTIVE_SESSIONS.get(player.getUUID());
        if (session == null || session.menuId() != menu.containerId) {
            return List.of();
        }
        return session.preferredInventorySlots();
    }

    public static void clear(Player player) {
        ACTIVE_SESSIONS.remove(player.getUUID());
    }

    private record Session(int menuId, List<Integer> preferredInventorySlots) {
    }
}
