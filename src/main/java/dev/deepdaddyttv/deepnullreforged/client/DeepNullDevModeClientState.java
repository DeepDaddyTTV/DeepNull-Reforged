package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.network.DeepNullDevModePayloads;

public final class DeepNullDevModeClientState {
    private static boolean enabled;

    private DeepNullDevModeClientState() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void handleState(DeepNullDevModePayloads.StatePayload payload) {
        enabled = payload.enabled();
    }
}
