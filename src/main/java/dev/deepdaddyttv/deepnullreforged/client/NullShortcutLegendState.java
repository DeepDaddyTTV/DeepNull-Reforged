package dev.deepdaddyttv.deepnullreforged.client;

final class NullShortcutLegendState {
    private static final float VISIBILITY_SNAP_EPSILON = 0.08F;
    private float revealProgress;
    private float activeProgress;
    private float keyVisibility = 1.0F;
    private float actionVisibility;

    Frame advance(boolean hovered, boolean keyDown, boolean armed, boolean pending, float step) {
        boolean active = isVisualActive(keyDown, armed, pending);
        revealProgress = approach(revealProgress, hovered || active ? 1.0F : 0.0F, step);
        activeProgress = approach(activeProgress, active ? 1.0F : 0.0F, step);
        keyVisibility = approachVisibility(keyVisibility, active ? 0.0F : hovered ? 0.4F : 1.0F, step);
        actionVisibility = approachVisibility(actionVisibility, hovered || active ? 1.0F : 0.0F, step);
        return new Frame(revealProgress, activeProgress, keyVisibility, actionVisibility);
    }

    static boolean isVisualActive(boolean keyDown, boolean armed, boolean pending) {
        return keyDown || armed || pending;
    }

    static boolean isInteractionActive(boolean keyDown, boolean armed) {
        return keyDown || armed;
    }

    private static float approach(float current, float target, float step) {
        return current + (target - current) * step;
    }

    private static float approachVisibility(float current, float target, float step) {
        float next = approach(current, target, step);
        if (target <= 0.0F && next <= VISIBILITY_SNAP_EPSILON) {
            return 0.0F;
        }
        if (target >= 1.0F && next >= 1.0F - VISIBILITY_SNAP_EPSILON) {
            return 1.0F;
        }
        return next;
    }

    record Frame(float reveal, float active, float keyVisibility, float actionVisibility) {
    }
}
