package dev.deepdaddyttv.deepnullreforged.client;

import net.minecraft.world.inventory.AbstractContainerMenu;

public final class ClientDeepNullJeiSession {
    private static int menuId = -1;

    private ClientDeepNullJeiSession() {
    }

    public static void markTransfer(AbstractContainerMenu menu) {
        menuId = menu.containerId;
    }

    public static boolean shouldReturn(AbstractContainerMenu menu) {
        return menu.containerId == menuId;
    }

    public static void clear() {
        menuId = -1;
    }
}
