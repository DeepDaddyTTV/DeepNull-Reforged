package dev.deepdaddyttv.deepnullreforged.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

final class NullShortcutController {
    static final int CHIP_WIDTH = 42;
    static final int CHIP_HEIGHT = 15;
    static final int CHIP_GAP = 3;
    static final int EDGE_GAP = 11;

    private final Map<Action, NullShortcutLegendState> states = new EnumMap<>(Action.class);
    private final Map<Action, Bounds> bounds = new EnumMap<>(Action.class);
    private final EnumSet<Action> eventHeldActions = EnumSet.noneOf(Action.class);
    private @Nullable Action armedAction;
    private Anchor anchor = Anchor.LEFT;
    private boolean clickConsumed;

    NullShortcutController() {
        for (Action action : Action.values()) {
            states.put(action, new NullShortcutLegendState());
        }
    }

    boolean isActive(Action action) {
        return !selectSuppresses(action) && NullShortcutLegendState.isInteractionActive(isKeyHeld(action), armedAction == action);
    }

    boolean isArmed(Action action) {
        return armedAction == action;
    }

    @Nullable Action activeAction(List<Action> actions) {
        for (Action action : actions) {
            if (!selectSuppresses(action) && isKeyHeld(action)) {
                return action;
            }
        }
        if (armedAction != null && actions.contains(armedAction) && !selectSuppresses(armedAction)) {
            return armedAction;
        }
        return null;
    }

    boolean matchesShortcut(KeyEvent event, List<Action> actions) {
        return handleKeyPressed(event, actions) != null;
    }

    @Nullable Action handleKeyPressed(KeyEvent event, List<Action> actions) {
        for (Action action : actions) {
            if (action.keyMapping().matches(event)) {
                eventHeldActions.add(action);
                return action;
            }
        }
        return null;
    }

    void handleKeyReleased(KeyEvent event, List<Action> actions) {
        for (Action action : actions) {
            if (action.keyMapping().matches(event)) {
                eventHeldActions.remove(action);
            }
        }
    }

    boolean isKeyHeld(Action action) {
        Minecraft minecraft = Minecraft.getInstance();
        return switch (action) {
            case SELECT -> eventHeldActions.contains(action) || action.keyMapping().isDown() || minecraft.hasAltDown();
            case CYCLE -> eventHeldActions.contains(action) || action.keyMapping().isDown() || minecraft.hasControlDown();
            default -> eventHeldActions.contains(action) || action.keyMapping().isDown();
        };
    }

    @Nullable Action handleClick(double mouseX, double mouseY, int button, List<Action> actions) {
        clickConsumed = false;
        if (!ClientModEvents.isPrimaryGuiButton(button)) {
            return null;
        }
        for (Action action : actions) {
            Bounds bound = bounds.get(action);
            if (bound != null && bound.contains(mouseX, mouseY)) {
                armedAction = armedAction == action ? null : action;
                clickConsumed = true;
                return action;
            }
        }
        Bounds anchorBounds = anchorBounds(actions);
        if (anchorBounds != null && anchorBounds.contains(mouseX, mouseY)) {
            anchor = anchor.next();
            bounds.clear();
            clickConsumed = true;
        }
        return null;
    }

    boolean consumedLastClick() {
        return clickConsumed;
    }

    boolean handleEscape(int keyCode) {
        if (keyCode != org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE || armedAction == null) {
            return false;
        }
        armedAction = null;
        return true;
    }

    void clearArmed(Action action) {
        if (armedAction == action) {
            armedAction = null;
        }
    }

    void clearAll() {
        armedAction = null;
        eventHeldActions.clear();
    }

    boolean shouldCancelPendingOnRelease(Action action) {
        return !isKeyHeld(action) && armedAction != action;
    }

    Anchor anchor() {
        return anchor;
    }

    void render(
            GuiGraphicsExtractor graphics,
            Font font,
            int mouseX,
            int mouseY,
            int left,
            int top,
            int screenWidth,
            int screenHeight,
            List<Action> actions,
            java.util.function.Predicate<Action> pending
    ) {
        layout(left, top, screenWidth, screenHeight, actions);
        for (Action action : actions) {
            Bounds bound = bounds.get(action);
            boolean hovered = bound.contains(mouseX, mouseY);
            boolean keyDown = isKeyHeld(action);
            boolean isPending = pending.test(action);
            NullShortcutLegendState.Frame frame = states.get(action).advance(hovered, keyDown, armedAction == action, isPending, 0.24F);
            int background = blend(0xCC17202A, action.color(), Math.max(frame.reveal(), frame.active()));
            graphics.fill(bound.x(), bound.y(), bound.x() + bound.width(), bound.y() + bound.height(), background);
            graphics.outline(bound.x(), bound.y(), bound.width(), bound.height(), blend(0xFF607080, 0xFFFFFFFF, frame.active()));
            String key = ClientUiText.shortcutKeyToken(action.keyMapping());
            String label = Component.translatable(action.shortLabelKey()).getString();
            if (frame.keyVisibility() > 0.0F) {
                drawCentered(graphics, font, key, bound, withAlpha(0xFFFFFFFF, Math.round(frame.keyVisibility() * 255.0F)));
            }
            if (frame.actionVisibility() > 0.0F) {
                drawCentered(graphics, font, label, bound, withAlpha(0xFFFFFFFF, Math.round(frame.actionVisibility() * 255.0F)));
            }
        }
        Bounds anchorBound = anchorBounds(actions);
        if (anchorBound != null) {
            graphics.fill(anchorBound.x(), anchorBound.y(), anchorBound.x() + anchorBound.width(), anchorBound.y() + anchorBound.height(), 0xCC10161D);
            graphics.outline(anchorBound.x(), anchorBound.y(), anchorBound.width(), anchorBound.height(), 0xFF607080);
            drawCentered(graphics, font, anchor.shortLabel(), anchorBound, 0xFFDCE6F2);
        }
    }

    void renderTooltip(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, List<Action> actions) {
        for (Action action : actions) {
            Bounds bound = bounds.get(action);
            if (bound != null && bound.contains(mouseX, mouseY)) {
                graphics.setComponentTooltipForNextFrame(font, List.of(Component.translatable(action.tooltipKey())), mouseX, mouseY);
                return;
            }
        }
        Bounds anchorBound = anchorBounds(actions);
        if (anchorBound != null && anchorBound.contains(mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font, Component.translatable("dn.shortcut_anchor.tooltip"), mouseX, mouseY);
        }
    }

    private void layout(int left, int top, int screenWidth, int screenHeight, List<Action> actions) {
        int totalWidth = actions.size() * CHIP_WIDTH + Math.max(0, actions.size() - 1) * CHIP_GAP;
        int totalHeight = actions.size() * CHIP_HEIGHT + Math.max(0, actions.size() - 1) * CHIP_GAP;
        int x = switch (anchor) {
            case LEFT -> left - CHIP_WIDTH - EDGE_GAP;
            case TOP -> Math.max(4, left + (screenWidth - totalWidth) / 2);
            case RIGHT, GAP -> left + screenWidth + EDGE_GAP;
        };
        int y = switch (anchor) {
            case LEFT, RIGHT -> top + 19;
            case TOP -> Math.max(4, top - CHIP_HEIGHT - EDGE_GAP);
            case GAP -> Math.min(top + screenHeight - totalHeight - 8, top + Math.max(54, screenHeight / 2));
        };
        for (int index = 0; index < actions.size(); index++) {
            int drawX = anchor == Anchor.TOP ? x + index * (CHIP_WIDTH + CHIP_GAP) : x;
            int drawY = anchor == Anchor.TOP ? y : y + index * (CHIP_HEIGHT + CHIP_GAP);
            bounds.put(actions.get(index), new Bounds(drawX, drawY, CHIP_WIDTH, CHIP_HEIGHT));
        }
    }

    private @Nullable Bounds anchorBounds(List<Action> actions) {
        if (actions.isEmpty() || !bounds.containsKey(actions.get(actions.size() - 1))) {
            return null;
        }
        Bounds last = bounds.get(actions.get(actions.size() - 1));
        return anchor == Anchor.TOP
                ? new Bounds(last.x() + last.width() + CHIP_GAP, last.y(), 18, CHIP_HEIGHT)
                : new Bounds(last.x(), last.y() + last.height() + CHIP_GAP, CHIP_WIDTH, 12);
    }

    private boolean selectSuppresses(Action action) {
        return action == Action.MERGE && (Action.SELECT.keyMapping().isDown() || Minecraft.getInstance().hasAltDown());
    }

    private static void drawCentered(GuiGraphicsExtractor graphics, Font font, String text, Bounds bound, int color) {
        graphics.text(font, text, bound.x() + (bound.width() - font.width(text)) / 2, bound.y() + 3, color, false);
    }

    private static int blend(int from, int to, float progress) {
        float t = Mth.clamp(progress, 0.0F, 1.0F);
        int a = Math.round(((from >>> 24) & 0xFF) + ((((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * t));
        int r = Math.round(((from >>> 16) & 0xFF) + ((((to >>> 16) & 0xFF) - ((from >>> 16) & 0xFF)) * t));
        int g = Math.round(((from >>> 8) & 0xFF) + ((((to >>> 8) & 0xFF) - ((from >>> 8) & 0xFF)) * t));
        int b = Math.round((from & 0xFF) + (((to & 0xFF) - (from & 0xFF)) * t));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int withAlpha(int color, int alpha) {
        return (Mth.clamp(alpha, 0, 255) << 24) | (color & 0xFFFFFF);
    }

    enum Action {
        SWAP(ClientModEvents.NULL_SHORTCUT_SWAP, 0xFF7256C7, "dn.shortcut_legend.swap.short", "dn.shortcut_legend.swap.tooltip"),
        MERGE(ClientModEvents.NULL_SHORTCUT_MERGE, 0xFF2F9A62, "dn.shortcut_legend.merge.short", "dn.shortcut_legend.merge.tooltip"),
        CLEAR(ClientModEvents.NULL_SHORTCUT_CLEAR, 0xFFC94545, "dn.shortcut_legend.clear.short", "dn.shortcut_legend.clear.tooltip"),
        SELECT(ClientModEvents.NULL_SHORTCUT_SELECT, 0xFF397DCC, "dn.shortcut_legend.select.short", "dn.shortcut_legend.select.tooltip"),
        CYCLE(ClientModEvents.NULL_SHORTCUT_CYCLE, 0xFFD9822B, "dn.shortcut_legend.cycle.short", "dn.shortcut_legend.cycle.tooltip");

        private final KeyMapping keyMapping;
        private final int color;
        private final String shortLabelKey;
        private final String tooltipKey;

        Action(KeyMapping keyMapping, int color, String shortLabelKey, String tooltipKey) {
            this.keyMapping = keyMapping;
            this.color = color;
            this.shortLabelKey = shortLabelKey;
            this.tooltipKey = tooltipKey;
        }

        KeyMapping keyMapping() { return keyMapping; }
        int color() { return color; }
        String shortLabelKey() { return shortLabelKey; }
        String tooltipKey() { return tooltipKey; }
    }

    enum Anchor {
        LEFT("L"), TOP("T"), RIGHT("R"), GAP("G");

        private final String shortLabel;

        Anchor(String shortLabel) { this.shortLabel = shortLabel; }
        String shortLabel() { return shortLabel; }
        Anchor next() { return values()[(ordinal() + 1) % values().length]; }
    }

    private record Bounds(int x, int y, int width, int height) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}
