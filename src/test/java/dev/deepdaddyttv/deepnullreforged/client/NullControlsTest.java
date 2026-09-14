package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.inventory.ItemExtractionMode;
import net.minecraft.client.renderer.Rect2i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NullControlsTest {
    @Test
    void shortcutStateKeepsPendingAndArmedActionsVisibleWithoutActivatingPendingInput() {
        NullShortcutLegendState state = new NullShortcutLegendState();
        NullShortcutLegendState.Frame pending = state.advance(false, false, false, true, 0.5F);

        assertTrue(pending.active() > 0.0F);
        assertTrue(NullShortcutLegendState.isVisualActive(false, false, true));
        assertFalse(NullShortcutLegendState.isInteractionActive(false, false));
        assertTrue(NullShortcutLegendState.isInteractionActive(false, true));
        assertTrue(NullShortcutLegendState.isInteractionActive(true, false));
    }

    @Test
    void reboundKeyLabelsStayCompact() {
        assertEquals("`", ClientUiText.shortcutKeyToken("Grave Accent"));
        assertEquals("Del", ClientUiText.shortcutKeyToken("Delete"));
        assertEquals("Alt", ClientUiText.shortcutKeyToken("Right Alt"));
        assertEquals("Ctrl", ClientUiText.shortcutKeyToken("Left Control"));
        assertEquals("K", ClientUiText.shortcutKeyToken("k"));
    }

    @Test
    void extractionStopsUseTrueKeepAllAndPreserveCustomValues() {
        assertEquals(ItemExtractionMode.KEEP_NONE, NullExtractionPresets.modeAt(0));
        assertEquals(ItemExtractionMode.KEEP_1, NullExtractionPresets.modeAt(1));
        assertEquals(ItemExtractionMode.KEEP_16, NullExtractionPresets.modeAt(2));
        assertEquals(ItemExtractionMode.KEEP_64, NullExtractionPresets.modeAt(3));
        assertEquals(ItemExtractionMode.KEEP_ALL, NullExtractionPresets.modeAt(4));
        assertEquals(32_768, NullExtractionPresets.amountFor(ItemExtractionMode.KEEP_ALL, 32_768));
        assertEquals(ItemExtractionMode.CUSTOM, NullExtractionPresets.modeForTypedAmount(37));
        assertEquals(3, NullExtractionPresets.indexOf(ItemExtractionMode.CUSTOM, 37));
    }

    @Test
    void animationTimingsAndMathMatchTheFeatureContract() {
        assertEquals(400L, NullShortcutActionAnimations.SELECT_MS);
        assertEquals(250L, NullShortcutActionAnimations.SWAP_MS);
        assertEquals(430L, NullShortcutActionAnimations.MERGE_WINDUP_MS + NullShortcutActionAnimations.MERGE_TRAVEL_MS);
        assertEquals(500L, NullShortcutActionAnimations.TANK_MERGE_MS);
        assertEquals(520L, NullShortcutActionAnimations.DELETE_MS);
        assertEquals(28, NullShortcutActionAnimations.DELETE_FRAGMENT_COUNT);
        assertEquals(360L, NullShortcutActionAnimations.CYCLE_TWEEN_MS + NullShortcutActionAnimations.CYCLE_RETURN_MS);

        NullShortcutActionAnimations.SlotRect bounds = NullShortcutActionAnimations.SlotRect.from(new Rect2i(0, 0, 16, 16));
        assertEquals(1.5F, NullShortcutActionAnimations.selectFrame(bounds, 200L).scale(), 0.02F);
        assertEquals(500, NullShortcutActionAnimations.tankMergeAmount(0, 1000, 250L), 2);
        assertEquals(180L, NullShortcutActionAnimations.tankSwapFlowDurationMs(1, 0, 1000));
        assertEquals(1000L, NullShortcutActionAnimations.tankSwapFlowDurationMs(1000, 1000, 1000));
        assertEquals(28, NullShortcutActionAnimations.deleteFragments(bounds, 0xFF336699).size());
    }
}
