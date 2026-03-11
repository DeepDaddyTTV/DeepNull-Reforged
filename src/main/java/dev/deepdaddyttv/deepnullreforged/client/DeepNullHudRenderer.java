package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.client.render.DeepNullHudState;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class DeepNullHudRenderer {
    private static final int PANEL_WIDTH = 146;
    private static final int HEADER_HEIGHT = 14;
    private static final int PADDING = 6;
    private static final int LINE_HEIGHT = 10;

    private DeepNullHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null) {
            return;
        }

        ClientDeepNullAccess.HeldDeepNull held = ClientDeepNullAccess.findHeldDeepNull(player);
        if (held == null) {
            return;
        }

        if (!DeepNullHudState.shouldRender()) {
            return;
        }

        DeepNullInventory inventory = held.inventory();
        int selectedSlot = inventory.getSelectedSlot();
        ItemStack selectedStack = inventory.getSelectedStack();
        if (selectedSlot < 0 || selectedStack.isEmpty()) {
            return;
        }

        Font font = minecraft.font;
        List<FormattedCharSequence> nameLines = font.split(selectedStack.getHoverName(), PANEL_WIDTH - 34);
        List<FormattedCharSequence> detailLines = new ArrayList<>();
        appendWrapped(detailLines, font, Component.translatable("dn.slot.desc").append(" ").append(Integer.toString(selectedSlot + 1)), PANEL_WIDTH - 34);
        appendWrapped(detailLines, font, label("dn.count.desc", countText(inventory, selectedStack)), PANEL_WIDTH - 34);
        appendWrapped(detailLines, font, label("dn.extract.desc", inventory.getExtractionMode(selectedSlot).tooltip()), PANEL_WIDTH - 34);
        appendWrapped(detailLines, font, label("dn.place.desc", inventory.getPlacementMode(selectedSlot).tooltip()), PANEL_WIDTH - 34);
        appendWrapped(detailLines, font, label("dn.tag_matching.desc", tagText(inventory, selectedSlot)), PANEL_WIDTH - 34);

        int lineCount = detailLines.size() + nameLines.size();
        int panelHeight = HEADER_HEIGHT + PADDING + Math.max(16, lineCount * LINE_HEIGHT) + PADDING;
        int x = Math.max(8, guiGraphics.guiWidth() - PANEL_WIDTH - 8);
        int y = Math.max(8, guiGraphics.guiHeight() - panelHeight - 8);

        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + panelHeight, 0xD0121720);
        guiGraphics.renderOutline(x, y, PANEL_WIDTH, panelHeight, 0xFF697487);
        guiGraphics.fill(x + 1, y + 1, x + PANEL_WIDTH - 1, y + HEADER_HEIGHT - 1, 0xC01A2230);
        guiGraphics.drawString(font, Component.literal("DeepNull"), x + PADDING, y + 4, 0xFFFFFFFF, false);

        guiGraphics.renderItem(selectedStack.copyWithCount(1), x + PADDING, y + HEADER_HEIGHT + PADDING);
        guiGraphics.renderItemDecorations(font, selectedStack, x + PADDING, y + HEADER_HEIGHT + PADDING);

        int textX = x + 28;
        int textY = y + HEADER_HEIGHT + PADDING;
        for (FormattedCharSequence line : nameLines) {
            guiGraphics.drawString(font, line, textX, textY, 0xFFFFFFFF, false);
            textY += LINE_HEIGHT;
        }
        for (FormattedCharSequence line : detailLines) {
            guiGraphics.drawString(font, line, textX, textY, 0xFFE8EDF5, false);
            textY += LINE_HEIGHT;
        }
    }

    private static void appendWrapped(List<FormattedCharSequence> lines, Font font, Component component, int width) {
        lines.addAll(font.split(component, width));
    }

    private static Component label(String key, Object value) {
        if (value instanceof Component component) {
            return Component.translatable(key).append(": ").append(component);
        }
        return Component.translatable(key).append(": ").append(value.toString());
    }

    private static Component countText(DeepNullInventory inventory, ItemStack selectedStack) {
        if (inventory.supportsLocking() && inventory.isLocked()) {
            return Component.translatable("dn.infinite.desc").withStyle(ChatFormatting.LIGHT_PURPLE);
        }
        return Component.literal(Integer.toString(selectedStack.getCount()));
    }

    private static Component tagText(DeepNullInventory inventory, int selectedSlot) {
        if (!DeepNullConfig.isTagMatchingEnabled()) {
            return Component.translatable("dn.disabled_by_config.desc").withStyle(ChatFormatting.GRAY);
        }
        if (!inventory.supportsTagMatching(selectedSlot)) {
            return Component.translatable("dn.not_oredicted.desc").withStyle(ChatFormatting.GRAY);
        }
        return Component.translatable(inventory.isTagMatchingEnabled(selectedSlot) ? "dn.enabled.desc" : "dn.disabled.desc");
    }

}
