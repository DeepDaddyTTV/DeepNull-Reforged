package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public final class DeepNullEnergyWidget {
    private static final ResourceLocation FRAME_TEXTURE = DeepNullReforged.id("textures/gui/widgets/energy_bar_frame.png");
    private static final ResourceLocation FILL_TEXTURE = DeepNullReforged.id("textures/gui/widgets/energy_bar_fill.png");
    private static final int FRAME_WIDTH = 18;
    private static final int FRAME_HEIGHT = 72;
    private static final int FILL_WIDTH = 12;
    private static final int FILL_HEIGHT = 64;
    private static final int X_OFFSET = -22;
    private static final int Y_OFFSET = 19;
    private static final int INNER_X_OFFSET = 3;
    private static final int INNER_Y_OFFSET = 4;

    private DeepNullEnergyWidget() {
    }

    public static void render(GuiGraphics guiGraphics, DeepNullMenu menu, int leftPos, int topPos) {
        if (!menu.hasEnergyUpgrade()) {
            return;
        }

        int x = leftPos + X_OFFSET;
        int y = topPos + Y_OFFSET;
        guiGraphics.blit(FRAME_TEXTURE, x, y, 0.0F, 0.0F, FRAME_WIDTH, FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);

        int capacity = menu.getDisplayedEnergyCapacity();
        int stored = menu.getDisplayedEnergyStored();
        if (capacity <= 0 || stored <= 0) {
            return;
        }

        int fillHeight = Math.max(1, Math.round(FILL_HEIGHT * Math.min(1.0F, stored / (float) capacity)));
        int drawX = x + INNER_X_OFFSET;
        int drawY = y + INNER_Y_OFFSET + (FILL_HEIGHT - fillHeight);
        int sourceY = FILL_HEIGHT - fillHeight;
        guiGraphics.blit(FILL_TEXTURE, drawX, drawY, 0.0F, sourceY, FILL_WIDTH, fillHeight, FILL_WIDTH, FILL_HEIGHT);
    }

    public static void renderTooltip(GuiGraphics guiGraphics, Font font, DeepNullMenu menu, int leftPos, int topPos, int mouseX, int mouseY) {
        if (!menu.hasEnergyUpgrade() || !isHovering(leftPos, topPos, mouseX, mouseY)) {
            return;
        }

        List<Component> tooltip = List.of(
                Component.translatable("dn.energy.desc"),
                Component.literal(menu.getDisplayedEnergyStored() + " / " + menu.getDisplayedEnergyCapacity() + " FE"),
                Component.translatable("dn.charging.desc").append(": ").append(Component.translatable(menu.isChargingEnabledDisplayed() ? "dn.enabled.desc" : "dn.disabled.desc"))
        );
        guiGraphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
    }

    private static boolean isHovering(int leftPos, int topPos, int mouseX, int mouseY) {
        int x = leftPos + X_OFFSET;
        int y = topPos + Y_OFFSET;
        return mouseX >= x && mouseX < x + FRAME_WIDTH && mouseY >= y && mouseY < y + FRAME_HEIGHT;
    }
}
