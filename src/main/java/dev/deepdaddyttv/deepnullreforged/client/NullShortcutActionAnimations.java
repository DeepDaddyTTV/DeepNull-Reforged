package dev.deepdaddyttv.deepnullreforged.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

final class NullShortcutActionAnimations {
    static final long SELECT_MS = 400L;
    static final long SWAP_MS = 250L;
    static final long MERGE_WINDUP_MS = 250L;
    static final long MERGE_TRAVEL_MS = 180L;
    static final long TANK_MERGE_MS = 500L;
    static final long TANK_SWAP_FLOW_MIN_MS = 180L;
    static final long TANK_SWAP_FLOW_MAX_MS = 1000L;
    static final long DELETE_MS = 520L;
    static final long CYCLE_TWEEN_MS = 180L;
    static final long CYCLE_RETURN_MS = 180L;
    static final int DELETE_FRAGMENT_COUNT = 28;
    static final float DEFAULT_SELECT_PEAK_SCALE = 1.5F;
    static final int DAMP_SELECT_PEAK_PAD = 2;
    static final float MERGE_WINDUP_PEAK_SCALE = 1.75F;

    private static final int SELECT_OUTLINE = 0xFFE8F6FF;
    private static final int SELECT_FILL = 0x2450E2E2;
    private static final int SWAP_OUTLINE = 0xFFE6D47B;
    private static final int MERGE_OUTLINE = 0xFFB9F07A;
    private static final int DELETE_OUTLINE = 0xFFFF9F7A;
    private static final int DELETE_MASK = 0xAA050505;
    private static final float TWO_PI = (float) (Math.PI * 2.0);

    private final List<SelectAnimation> selects = new ArrayList<>();
    private final List<ItemActionAnimation> itemActions = new ArrayList<>();
    private final List<DeleteAnimation> deletes = new ArrayList<>();
    private final List<CycleAnimation> cycles = new ArrayList<>();

    void startSelect(int slotIndex, Rect2i bounds) {
        selects.add(new SelectAnimation(slotIndex, SlotRect.from(bounds), System.currentTimeMillis(), false));
    }

    void startSelectPixelPad(int slotIndex, Rect2i bounds) {
        selects.add(new SelectAnimation(slotIndex, SlotRect.from(bounds), System.currentTimeMillis(), true));
    }

    void startSwap(int sourceIndex, int destinationIndex, ItemStack source, ItemStack destination, Rect2i sourceBounds, Rect2i destinationBounds) {
        itemActions.add(new ItemActionAnimation(false, sourceIndex, destinationIndex, source.copy(), destination.copy(), SlotRect.from(sourceBounds), SlotRect.from(destinationBounds), System.currentTimeMillis()));
    }

    void startMerge(int sourceIndex, int destinationIndex, ItemStack source, ItemStack destination, Rect2i sourceBounds, Rect2i destinationBounds) {
        itemActions.add(new ItemActionAnimation(true, sourceIndex, destinationIndex, source.copy(), destination.copy(), SlotRect.from(sourceBounds), SlotRect.from(destinationBounds), System.currentTimeMillis()));
    }

    void startDelete(int slotIndex, ItemStack stack, Rect2i bounds, int seedColor) {
        deletes.add(new DeleteAnimation(slotIndex, stack.copy(), SlotRect.from(bounds), deleteFragments(SlotRect.from(bounds), seedColor), System.currentTimeMillis()));
    }

    void startCycle(int slotIndex, ItemStack stack, Rect2i bounds, boolean forward) {
        cycles.add(new CycleAnimation(slotIndex, stack.copy(), SlotRect.from(bounds), forward, System.currentTimeMillis()));
    }

    boolean suppressesItemSlot(int slotIndex) {
        long now = System.currentTimeMillis();
        for (ItemActionAnimation animation : itemActions) {
            if (animation.active(now) && (animation.sourceIndex == slotIndex || animation.destinationIndex == slotIndex)) {
                return true;
            }
        }
        for (DeleteAnimation animation : deletes) {
            if (animation.active(now) && animation.slotIndex == slotIndex) {
                return true;
            }
        }
        for (CycleAnimation animation : cycles) {
            if (animation.active(now) && animation.slotIndex == slotIndex) {
                return true;
            }
        }
        return false;
    }

    void clear() {
        selects.clear();
        itemActions.clear();
        deletes.clear();
        cycles.clear();
    }

    void render(GuiGraphicsExtractor graphics, Font font) {
        long now = System.currentTimeMillis();
        renderSelections(graphics, now);
        renderItemActions(graphics, font, now);
        renderDeletes(graphics, now);
        renderCycles(graphics, font, now);
        selects.removeIf(animation -> !animation.active(now));
        itemActions.removeIf(animation -> !animation.active(now));
        deletes.removeIf(animation -> !animation.active(now));
        cycles.removeIf(animation -> !animation.active(now));
    }

    private void renderSelections(GuiGraphicsExtractor graphics, long now) {
        for (SelectAnimation animation : selects) {
            long elapsed = now - animation.startMs;
            Rect2i bounds = animation.pixelPad
                    ? selectPixelPadFrame(animation.bounds, elapsed, DAMP_SELECT_PEAK_PAD).bounds()
                    : scaledBounds(animation.bounds, selectFrame(animation.bounds, elapsed).scale());
            graphics.fill(bounds.getX(), bounds.getY(), bounds.getX() + bounds.getWidth(), bounds.getY() + bounds.getHeight(), SELECT_FILL);
            graphics.outline(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), SELECT_OUTLINE);
        }
    }

    private void renderItemActions(GuiGraphicsExtractor graphics, Font font, long now) {
        for (ItemActionAnimation animation : itemActions) {
            long elapsed = now - animation.startMs;
            if (animation.merge) {
                MergeFrame frame = mergeFrame(animation.sourceBounds, animation.destinationBounds, elapsed);
                graphics.outline(animation.destinationBounds.toRect().getX(), animation.destinationBounds.toRect().getY(), animation.destinationBounds.toRect().getWidth(), animation.destinationBounds.toRect().getHeight(), MERGE_OUTLINE);
                renderItem(graphics, font, animation.destination, Transform.identity(animation.destinationBounds));
                if (frame.visible()) {
                    renderItem(graphics, font, animation.source, frame.source());
                }
            } else {
                SwapFrame frame = swapFrame(animation.sourceBounds, animation.destinationBounds, elapsed);
                graphics.outline(animation.sourceBounds.toRect().getX(), animation.sourceBounds.toRect().getY(), animation.sourceBounds.toRect().getWidth(), animation.sourceBounds.toRect().getHeight(), SWAP_OUTLINE);
                graphics.outline(animation.destinationBounds.toRect().getX(), animation.destinationBounds.toRect().getY(), animation.destinationBounds.toRect().getWidth(), animation.destinationBounds.toRect().getHeight(), SWAP_OUTLINE);
                renderItem(graphics, font, animation.source, frame.source());
                renderItem(graphics, font, animation.destination, frame.destination());
            }
        }
    }

    private void renderDeletes(GuiGraphicsExtractor graphics, long now) {
        for (DeleteAnimation animation : deletes) {
            float progress = clamp01((now - animation.startMs) / (float) DELETE_MS);
            Rect2i bounds = animation.bounds.toRect();
            graphics.fill(bounds.getX(), bounds.getY(), bounds.getX() + bounds.getWidth(), bounds.getY() + bounds.getHeight(), withAlpha(DELETE_MASK, Math.round((1.0F - progress) * 170.0F)));
            graphics.outline(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), DELETE_OUTLINE);
            for (DeleteFragment fragment : animation.fragments) {
                DeleteFragmentFrame frame = fragmentFrame(fragment, progress);
                int x = Math.round(frame.x());
                int y = Math.round(frame.y());
                graphics.fill(x, y, x + frame.size(), y + frame.size(), withAlpha(frame.color(), Math.round(frame.alpha() * 255.0F)));
            }
        }
    }

    private void renderCycles(GuiGraphicsExtractor graphics, Font font, long now) {
        for (CycleAnimation animation : cycles) {
            long elapsed = now - animation.startMs;
            float offset;
            if (elapsed <= CYCLE_TWEEN_MS) {
                offset = (animation.forward ? -1.0F : 1.0F) * 5.0F * (float) Math.sin(Math.PI * clamp01(elapsed / (float) CYCLE_TWEEN_MS));
            } else {
                long returning = elapsed - CYCLE_TWEEN_MS;
                offset = (animation.forward ? 1.0F : -1.0F) * 2.0F * (float) Math.sin(Math.PI * clamp01(returning / (float) CYCLE_RETURN_MS));
            }
            renderItem(graphics, font, animation.stack, new Transform(animation.bounds.centerX(), animation.bounds.centerY() + offset, 1.0F));
        }
    }

    private static void renderItem(GuiGraphicsExtractor graphics, Font font, ItemStack stack, Transform transform) {
        if (stack.isEmpty()) {
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(transform.centerX, transform.centerY);
        graphics.pose().scale(transform.scale, transform.scale);
        graphics.item(stack.copyWithCount(1), -8, -8);
        if (stack.getCount() > 1) {
            String count = DeepNullCountFormatter.formatCompact(stack.getCount());
            graphics.text(font, count, 9 - font.width(count), 1, 0xFFFFFFFF, true);
        }
        graphics.pose().popMatrix();
    }

    static SelectFrame selectFrame(SlotRect bounds, long elapsedMs) {
        return selectFrame(bounds, elapsedMs, DEFAULT_SELECT_PEAK_SCALE);
    }

    static SelectFrame selectFrame(SlotRect bounds, long elapsedMs, float peakScale) {
        float progress = clamp01(elapsedMs / (float) SELECT_MS);
        float scale = 1.0F + Math.max(0.0F, peakScale - 1.0F) * (float) Math.sin(Math.PI * easeInOut(progress));
        return new SelectFrame(bounds.centerX(), bounds.centerY(), scale);
    }

    static SelectPixelPadFrame selectPixelPadFrame(SlotRect bounds, long elapsedMs, int peakPad) {
        float progress = clamp01(elapsedMs / (float) SELECT_MS);
        int pad = Math.round(Math.max(0, peakPad) * (float) Math.sin(Math.PI * easeInOut(progress)));
        Rect2i padded = new Rect2i(Math.round(bounds.x) - pad, Math.round(bounds.y) - pad, Math.round(bounds.width) + pad * 2, Math.round(bounds.height) + pad * 2);
        return new SelectPixelPadFrame(padded, pad);
    }

    static SwapFrame swapFrame(SlotRect source, SlotRect destination, long elapsedMs) {
        float progress = easeInOut(clamp01(elapsedMs / (float) SWAP_MS));
        float arc = (float) Math.sin(Math.PI * progress);
        Vector perpendicular = perpendicular(source.centerX(), source.centerY(), destination.centerX(), destination.centerY());
        float distance = distance(source.centerX(), source.centerY(), destination.centerX(), destination.centerY());
        float offset = Math.max(8.0F, Math.min(24.0F, distance * 0.22F)) * arc;
        return new SwapFrame(
                new Transform(lerp(source.centerX(), destination.centerX(), progress) + perpendicular.x * offset, lerp(source.centerY(), destination.centerY(), progress) + perpendicular.y * offset, 1.0F + 0.25F * arc),
                new Transform(lerp(destination.centerX(), source.centerX(), progress) - perpendicular.x * offset, lerp(destination.centerY(), source.centerY(), progress) - perpendicular.y * offset, 1.0F + 0.25F * arc)
        );
    }

    static MergeFrame mergeFrame(SlotRect source, SlotRect destination, long elapsedMs) {
        if (elapsedMs <= MERGE_WINDUP_MS) {
            float progress = clamp01(elapsedMs / (float) MERGE_WINDUP_MS);
            float shake = (float) Math.sin(progress * Math.PI * 8.0F) * 1.5F * (1.0F - progress * 0.25F);
            float scale = 1.0F + (MERGE_WINDUP_PEAK_SCALE - 1.0F) * (float) Math.sin(Math.PI * progress);
            return new MergeFrame(MergePhase.WINDUP, new Transform(source.centerX() + shake, source.centerY(), scale), true);
        }
        float progress = easeInOut(clamp01((elapsedMs - MERGE_WINDUP_MS) / (float) MERGE_TRAVEL_MS));
        Transform transform = new Transform(
                quadratic(source.centerX(), (source.centerX() + destination.centerX()) / 2.0F, destination.centerX(), progress),
                quadratic(source.centerY(), Math.min(source.centerY(), destination.centerY()) - Math.max(source.height, destination.height) * 2.0F, destination.centerY(), progress),
                1.0F
        );
        return new MergeFrame(MergePhase.TRAVEL, transform, progress < 1.0F);
    }

    static int tankMergeAmount(int before, int after, long elapsedMs) {
        return Math.round(lerp(before, after, easeInOut(clamp01(elapsedMs / (float) TANK_MERGE_MS))));
    }

    static long tankSwapFlowDurationMs(long sourceAmount, long destinationAmount, long capacity) {
        long total = saturatedAdd(Math.max(0L, sourceAmount), Math.max(0L, destinationAmount));
        if (total <= 0L || capacity <= 0L) {
            return 0L;
        }
        long combinedCapacity = capacity > Long.MAX_VALUE / 2L ? Long.MAX_VALUE : capacity * 2L;
        if (total >= combinedCapacity) {
            return TANK_SWAP_FLOW_MAX_MS;
        }
        return Math.max(TANK_SWAP_FLOW_MIN_MS, Math.min(TANK_SWAP_FLOW_MAX_MS, Math.round(TANK_SWAP_FLOW_MAX_MS * (total / (double) combinedCapacity))));
    }

    static TankSwapFlowFrame tankSwapFlowFrame(long sourceAmount, long destinationAmount, long elapsedMs, long durationMs) {
        long source = Math.max(0L, sourceAmount);
        long destination = Math.max(0L, destinationAmount);
        if (durationMs <= 0L || saturatedAdd(source, destination) <= 0L) {
            return new TankSwapFlowFrame(0L, 0L, 0L, 0L);
        }
        double half = durationMs / 2.0D;
        if (elapsedMs <= half) {
            double progress = clamp01((float) (elapsedMs / half));
            return new TankSwapFlowFrame(interpolateLong(source, 0L, progress), interpolateLong(destination, 0L, progress), 0L, 0L);
        }
        double progress = clamp01((float) ((elapsedMs - half) / Math.max(1.0D, durationMs - half)));
        return new TankSwapFlowFrame(0L, 0L, interpolateLong(0L, source, progress), interpolateLong(0L, destination, progress));
    }

    static List<DeleteFragment> deleteFragments(SlotRect bounds, int seedColor) {
        int[] palette = {opaque(seedColor), opaque(seedColor ^ 0x00282828), 0xFFFFFFFF};
        Random random = new Random((long) seedColor * 31L + Arrays.hashCode(palette) * 17L + 0x6D657267654CL);
        List<DeleteFragment> fragments = new ArrayList<>(DELETE_FRAGMENT_COUNT);
        for (int index = 0; index < DELETE_FRAGMENT_COUNT; index++) {
            float angle = (float) (random.nextFloat() * Math.PI * 2.0D);
            float speed = 10.0F + random.nextFloat() * 24.0F;
            fragments.add(new DeleteFragment(
                    bounds.x + random.nextFloat() * bounds.width,
                    bounds.y + random.nextFloat() * bounds.height,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed - 10.0F,
                    1 + random.nextInt(3),
                    palette[random.nextInt(palette.length)]
            ));
        }
        return List.copyOf(fragments);
    }

    static DeleteFragmentFrame fragmentFrame(DeleteFragment fragment, float progress) {
        float eased = 1.0F - (1.0F - clamp01(progress)) * (1.0F - clamp01(progress));
        return new DeleteFragmentFrame(fragment.x + fragment.velocityX * eased, fragment.y + fragment.velocityY * eased + progress * progress * 24.0F, fragment.size, fragment.color, 1.0F - progress);
    }

    private static Rect2i scaledBounds(SlotRect bounds, float scale) {
        int width = Math.max(1, Math.round(bounds.width * scale));
        int height = Math.max(1, Math.round(bounds.height * scale));
        return new Rect2i(Math.round(bounds.centerX() - width / 2.0F), Math.round(bounds.centerY() - height / 2.0F), width, height);
    }

    private static float easeInOut(float value) { float t = clamp01(value); return t * t * (3.0F - 2.0F * t); }
    private static float clamp01(float value) { return Math.max(0.0F, Math.min(1.0F, value)); }
    private static float lerp(float from, float to, float progress) { return from + (to - from) * progress; }
    private static float quadratic(float start, float control, float end, float progress) { float inverse = 1.0F - progress; return inverse * inverse * start + 2.0F * inverse * progress * control + progress * progress * end; }
    private static long interpolateLong(long from, long to, double progress) { return Math.round(from + (to - from) * progress); }
    private static long saturatedAdd(long first, long second) { return Long.MAX_VALUE - first < second ? Long.MAX_VALUE : first + second; }
    private static int opaque(int color) { return (color >>> 24) == 0 ? color | 0xFF000000 : color; }
    private static int withAlpha(int color, int alpha) { return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0xFFFFFF); }

    private static Vector perpendicular(float sourceX, float sourceY, float destinationX, float destinationY) {
        float dx = destinationX - sourceX;
        float dy = destinationY - sourceY;
        float length = distance(sourceX, sourceY, destinationX, destinationY);
        return length <= 0.001F ? new Vector(0.0F, -1.0F) : new Vector(-dy / length, dx / length);
    }

    private static float distance(float sourceX, float sourceY, float destinationX, float destinationY) {
        float dx = destinationX - sourceX;
        float dy = destinationY - sourceY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    record SlotRect(float x, float y, float width, float height) {
        static SlotRect from(Rect2i bounds) { return new SlotRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()); }
        float centerX() { return x + width / 2.0F; }
        float centerY() { return y + height / 2.0F; }
        Rect2i toRect() { return new Rect2i(Math.round(x), Math.round(y), Math.round(width), Math.round(height)); }
    }
    record SelectFrame(float centerX, float centerY, float scale) { }
    record SelectPixelPadFrame(Rect2i bounds, int pad) { }
    record Transform(float centerX, float centerY, float scale) { static Transform identity(SlotRect bounds) { return new Transform(bounds.centerX(), bounds.centerY(), 1.0F); } }
    record SwapFrame(Transform source, Transform destination) { }
    enum MergePhase { WINDUP, TRAVEL }
    record MergeFrame(MergePhase phase, Transform source, boolean visible) { }
    record TankSwapFlowFrame(long sourceDrainAmount, long destinationDrainAmount, long sourceFillAmount, long destinationFillAmount) { }
    record DeleteFragment(float x, float y, float velocityX, float velocityY, int size, int color) { }
    record DeleteFragmentFrame(float x, float y, int size, int color, float alpha) { }
    private record Vector(float x, float y) { }
    private record SelectAnimation(int slotIndex, SlotRect bounds, long startMs, boolean pixelPad) { boolean active(long now) { return now - startMs < SELECT_MS; } }
    private record ItemActionAnimation(boolean merge, int sourceIndex, int destinationIndex, ItemStack source, ItemStack destination, SlotRect sourceBounds, SlotRect destinationBounds, long startMs) { boolean active(long now) { return now - startMs < (merge ? MERGE_WINDUP_MS + MERGE_TRAVEL_MS : SWAP_MS); } }
    private record DeleteAnimation(int slotIndex, ItemStack stack, SlotRect bounds, List<DeleteFragment> fragments, long startMs) { boolean active(long now) { return now - startMs < DELETE_MS; } }
    private record CycleAnimation(int slotIndex, ItemStack stack, SlotRect bounds, boolean forward, long startMs) { boolean active(long now) { return now - startMs < CYCLE_TWEEN_MS + CYCLE_RETURN_MS; } }
}
