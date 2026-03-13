package dev.deepdaddyttv.deepnullreforged.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DeepNullFluidScreen extends AbstractContainerScreen<DeepNullMenu> {
    private static final int BASE_IMAGE_WIDTH = 202;
    private static final int INFO_PANEL_WIDTH = 146;
    private static final int INFO_PANEL_PADDING = 6;
    private static final int INFO_PANEL_LINE_HEIGHT = 10;
    private static final ResourceLocation INFO_BUTTON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_info_button.png");
    private static final ResourceLocation INFO_TAB_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_info_tab.png");
    private static final int TAB_BUTTON_U = 98;
    private static final int INFO_BUTTON_V = 16;
    private static final int TAB_BUTTON_WIDTH = 13;
    private static final int TAB_BUTTON_HEIGHT = 19;
    private static final int INFO_TAB_U = 105;
    private static final int INFO_TAB_V = 6;
    private static final int INFO_TAB_WIDTH = 146;
    private static final int INFO_TAB_HEIGHT = 170;
    private static final int TANK_SCAN_MAX_X = 220;
    private static final int TANK_SCAN_MIN_Y = 10;
    private static final int TANK_SCAN_MAX_Y = 180;
    private static final int TANK_INTERIOR_THRESHOLD = 35;
    private static final int TANK_MIN_WIDTH = 10;
    private static final int TANK_MAX_WIDTH = 18;
    private static final int TANK_MIN_HEIGHT = 18;
    private static final int TANK_FILL_INSET_X = 1;
    private static final int TANK_FILL_INSET_Y = 2;
    private static final Map<ResourceLocation, List<Rect2i>> TANK_WINDOW_CACHE = new ConcurrentHashMap<>();

    private final ResourceLocation backgroundTexture;
    private final ResourceLocation tankOverlayTexture;
    private final List<Rect2i> tankWindows;
    private boolean infoPanelOpen;
    private int hoveredTankIndex = -1;

    public DeepNullFluidScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = BASE_IMAGE_WIDTH;
        this.imageHeight = 141 + Math.max(0, menu.getTier().rows() - 1) * 21;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = this.imageHeight - 103;
        this.titleLabelX = 7;
        this.titleLabelY = 6;
        this.backgroundTexture = DeepNullReforged.id("textures/gui/dampnullscreen" + menu.getTier().ordinalId() + ".png");
        this.tankOverlayTexture = DeepNullReforged.id("textures/gui/dampnullscreen" + menu.getTier().ordinalId() + "_tank.png");
        this.tankWindows = resolveTankWindows(backgroundTexture, menu.getStorageSlotCount());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!menu.getDankInventory().supportsFluidStorage()) {
            PacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.MAIN.ordinal()));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(backgroundTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        renderFluidTanks(guiGraphics);
        guiGraphics.blit(tankOverlayTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        renderInfoButton(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFFFFF, false);
    }

    @Override
    protected void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick) {
        if (slot instanceof DeepNullMenu.FluidStorageSlot) {
            return;
        }
        super.renderSlotHighlight(guiGraphics, slot, mouseX, mouseY, partialTick);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        hoveredTankIndex = getTankIndexAt(mouseX, mouseY);
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (infoPanelOpen) {
            renderInfoPanel(guiGraphics);
        }
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderTankTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isWithin(mouseX, mouseY, infoButtonX(), topPos + 38, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            infoPanelOpen = !infoPanelOpen;
            return true;
        }

        int tankIndex = getTankIndexAt(mouseX, mouseY);
        if (tankIndex >= 0 && menu.getCarried().isEmpty()) {
            if (button == 0 && Screen.hasShiftDown()) {
                if (menu.clearFluidSlot(tankIndex)) {
                    PacketDistributor.sendToServer(new DeepNullPayloads.MenuSlotActionPayload(
                            tankIndex,
                            DeepNullPayloads.MenuSlotAction.CLEAR_FLUID.ordinal()
                    ));
                }
                return true;
            }
            if (button == 0) {
                menu.getDankInventory().setSelectedSlot(tankIndex);
                PacketDistributor.sendToServer(new DeepNullPayloads.MenuSlotActionPayload(
                        tankIndex,
                        DeepNullPayloads.MenuSlotAction.SELECT.ordinal()
                ));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderFluidTanks(GuiGraphics guiGraphics) {
        int capacity = menu.getDankInventory().getFluidCapacity();
        if (capacity <= 0) {
            return;
        }

        for (int slotIndex = 0; slotIndex < tankWindows.size() && slotIndex < menu.getStorageSlotCount(); slotIndex++) {
            FluidStack fluidStack = menu.getDankInventory().getFluidInSlot(slotIndex);
            if (fluidStack.isEmpty()) {
                continue;
            }
            renderFluidInTank(guiGraphics, tankWindows.get(slotIndex), fluidStack, capacity);
        }
    }

    private void renderFluidInTank(GuiGraphics guiGraphics, Rect2i tankWindow, FluidStack fluidStack, int capacity) {
        IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation texture = clientFluid.getStillTexture(fluidStack);
        int tint = clientFluid.getTintColor(fluidStack);
        Rect2i fillWindow = visibleFillWindow(tankWindow);
        int tankWidth = fillWindow.getWidth();
        int tankHeight = fillWindow.getHeight();
        int fillHeight = Math.max(1, Math.round(tankHeight * Math.min(1.0F, fluidStack.getAmount() / (float) capacity)));
        int drawX = leftPos + fillWindow.getX();
        int drawY = topPos + fillWindow.getY() + (tankHeight - fillHeight);

        if (texture == null) {
            guiGraphics.fill(drawX, drawY, drawX + tankWidth, topPos + fillWindow.getY() + tankHeight, tint == 0 ? 0xFF3AA7FF : tint);
            return;
        }

        TextureAtlasSprite sprite = FluidSpriteCache.getSprite(texture);
        float alpha = ((tint >> 24) & 0xFF) / 255.0F;
        float red = ((tint >> 16) & 0xFF) / 255.0F;
        float green = ((tint >> 8) & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;
        for (int offsetY = 0; offsetY < fillHeight; offsetY += 16) {
            int drawHeight = Math.min(16, fillHeight - offsetY);
            guiGraphics.blit(drawX, drawY + offsetY, 0, tankWidth, drawHeight, sprite, red, green, blue, alpha <= 0.0F ? 1.0F : alpha);
        }
    }

    private Rect2i visibleFillWindow(Rect2i tankWindow) {
        int x = tankWindow.getX() + TANK_FILL_INSET_X;
        int y = tankWindow.getY() + TANK_FILL_INSET_Y;
        int width = Math.max(1, tankWindow.getWidth() - (TANK_FILL_INSET_X * 2));
        int height = Math.max(1, tankWindow.getHeight() - (TANK_FILL_INSET_Y * 2));
        return new Rect2i(x, y, width, height);
    }

    private void renderTankTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int tankIndex = getTankIndexAt(mouseX, mouseY);
        if (tankIndex < 0 || tankIndex >= menu.getStorageSlotCount()) {
            return;
        }

        FluidStack fluidStack = menu.getDankInventory().getFluidInSlot(tankIndex);
        if (fluidStack.isEmpty()) {
            guiGraphics.renderTooltip(
                    font,
                    List.of(
                            Component.translatable("dn.empty.desc"),
                            Component.translatable("dn.capacity.desc").append(": ").append(Component.literal(fluidCapacityText())),
                            Component.translatable("dn.shift_click_clear_tank.desc")
                    ),
                    Optional.empty(),
                    mouseX,
                    mouseY
            );
            return;
        }

        guiGraphics.renderTooltip(
                font,
                List.of(
                        fluidStack.getHoverName(),
                        Component.translatable("dn.amount.desc").append(": ").append(Component.literal(fluidAmountText(fluidStack))),
                        Component.translatable("dn.capacity.desc").append(": ").append(Component.literal(fluidCapacityText())),
                        Component.translatable("dn.shift_click_clear_tank.desc")
                ),
                Optional.empty(),
                mouseX,
                mouseY
        );
    }

    private void renderInfoButton(GuiGraphics guiGraphics) {
        guiGraphics.blit(INFO_BUTTON_TEXTURE, infoButtonX(), topPos + 38, TAB_BUTTON_U, INFO_BUTTON_V, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT, 256, 256);
    }

    private void renderInfoPanel(GuiGraphics guiGraphics) {
        int panelX = infoPanelX();
        int panelY = topPos + 4;
        int textX = panelX + 14;
        int lineY = panelY + 12;
        int textWidth = INFO_PANEL_WIDTH - 24;
        int tankIndex = getContextTankIndex();
        FluidStack fluidStack = tankIndex >= 0 && tankIndex < menu.getStorageSlotCount()
                ? menu.getDankInventory().getFluidInSlot(tankIndex)
                : FluidStack.EMPTY;

        guiGraphics.blit(INFO_TAB_TEXTURE, panelX, panelY, INFO_TAB_U, INFO_TAB_V, INFO_TAB_WIDTH, INFO_TAB_HEIGHT, 256, 256);
        guiGraphics.drawString(font, Component.translatable("itemGroup." + DeepNullReforged.MODID), textX, lineY, 0xFFFFFFFF, false);
        lineY += 18;

        if (fluidStack.isEmpty()) {
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.fluid_hover_for_details.desc"), textX, lineY, textWidth, 0xFFC9D0DB);
            lineY += 4;
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.left_click_select_tank.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            lineY = drawWrapped(guiGraphics, Component.translatable("dn.shift_click_clear_tank.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            drawWrapped(guiGraphics, Component.translatable("dn.fluid_empty_hint.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            return;
        }

        lineY = drawWrapped(guiGraphics, fluidStack.getHoverName(), textX, lineY, textWidth, 0xFFFFFFFF);
        lineY += 4;
        lineY = drawWrapped(guiGraphics, Component.translatable("dn.amount.desc").append(": ").append(Component.literal(fluidAmountText(fluidStack))), textX, lineY, textWidth, 0xFFE8EDF5);
        lineY = drawWrapped(guiGraphics, Component.translatable("dn.capacity.desc").append(": ").append(Component.literal(fluidCapacityText())), textX, lineY, textWidth, 0xFFE8EDF5);
        lineY += 4;
        lineY = drawWrapped(guiGraphics, Component.translatable("dn.left_click_select_tank.desc"), textX, lineY, textWidth, 0xFF99A5B5);
        drawWrapped(guiGraphics, Component.translatable("dn.shift_click_clear_tank.desc"), textX, lineY, textWidth, 0xFF99A5B5);
    }

    public Rect2i getInfoPanelArea() {
        if (!infoPanelOpen) {
            return null;
        }
        return new Rect2i(infoPanelX(), topPos + 4, INFO_TAB_WIDTH, INFO_TAB_HEIGHT);
    }

    private int getContextTankIndex() {
        if (hoveredTankIndex >= 0 && hoveredTankIndex < menu.getStorageSlotCount()) {
            return hoveredTankIndex;
        }
        int selectedSlot = menu.getDankInventory().getSelectedSlot();
        return selectedSlot >= 0 && selectedSlot < menu.getStorageSlotCount() ? selectedSlot : -1;
    }

    private String fluidCapacityText() {
        int capacity = menu.getDankInventory().getFluidCapacity();
        return capacity == Integer.MAX_VALUE ? Component.translatable("dn.infinite.desc").getString() : capacity + " mB";
    }

    private String fluidAmountText(FluidStack fluidStack) {
        return menu.getTier().creative()
                ? Component.translatable("dn.infinite.desc").getString()
                : fluidStack.getAmount() + " mB";
    }

    private int infoButtonX() {
        return leftPos + imageWidth - 1;
    }

    private int infoPanelX() {
        int rightSide = leftPos + imageWidth + TAB_BUTTON_WIDTH + 4;
        if (rightSide + INFO_TAB_WIDTH <= width - 4) {
            return rightSide;
        }
        return Math.max(4, leftPos - INFO_TAB_WIDTH - TAB_BUTTON_WIDTH - 4);
    }

    private int getTankIndexAt(double mouseX, double mouseY) {
        int relativeX = (int) mouseX - leftPos;
        int relativeY = (int) mouseY - topPos;
        for (int slotIndex = 0; slotIndex < tankWindows.size() && slotIndex < menu.getStorageSlotCount(); slotIndex++) {
            Rect2i tankWindow = tankWindows.get(slotIndex);
            if (relativeX >= tankWindow.getX() && relativeX < tankWindow.getX() + tankWindow.getWidth()
                    && relativeY >= tankWindow.getY() && relativeY < tankWindow.getY() + tankWindow.getHeight()) {
                return slotIndex;
            }
        }
        return -1;
    }

    private List<Rect2i> resolveTankWindows(ResourceLocation texture, int expectedCount) {
        List<Rect2i> detected = TANK_WINDOW_CACHE.computeIfAbsent(texture, this::detectTankWindows);
        if (detected.size() == expectedCount) {
            return detected;
        }

        DeepNullReforged.LOGGER.warn("Expected {} DampNull tank windows in {}, found {}", expectedCount, texture, detected.size());
        return fallbackTankWindows();
    }

    private List<Rect2i> detectTankWindows(ResourceLocation texture) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return List.of();
        }

        Optional<Resource> resource = minecraft.getResourceManager().getResource(texture);
        if (resource.isEmpty()) {
            return List.of();
        }

        try (var stream = resource.get().open();
             NativeImage image = NativeImage.read(stream)) {
            return measureTankWindows(image);
        } catch (IOException exception) {
            DeepNullReforged.LOGGER.warn("Failed to detect DampNull tank windows from {}", texture, exception);
            return List.of();
        }
    }

    private List<Rect2i> measureTankWindows(NativeImage image) {
        int width = Math.min(image.getWidth(), TANK_SCAN_MAX_X);
        int height = Math.min(image.getHeight(), TANK_SCAN_MAX_Y);
        boolean[] visited = new boolean[width * height];
        List<Rect2i> windows = new java.util.ArrayList<>();

        for (int y = TANK_SCAN_MIN_Y; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (visited[index] || !isTankInterior(image.getPixelRGBA(x, y))) {
                    continue;
                }

                int minX = x;
                int maxX = x;
                int minY = y;
                int maxY = y;
                java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();
                queue.add(new int[]{x, y});
                visited[index] = true;

                while (!queue.isEmpty()) {
                    int[] point = queue.removeFirst();
                    int currentX = point[0];
                    int currentY = point[1];
                    if (currentX < minX) minX = currentX;
                    if (currentX > maxX) maxX = currentX;
                    if (currentY < minY) minY = currentY;
                    if (currentY > maxY) maxY = currentY;

                    visitNeighbor(image, width, height, visited, queue, currentX + 1, currentY);
                    visitNeighbor(image, width, height, visited, queue, currentX - 1, currentY);
                    visitNeighbor(image, width, height, visited, queue, currentX, currentY + 1);
                    visitNeighbor(image, width, height, visited, queue, currentX, currentY - 1);
                }

                int boxWidth = maxX - minX + 1;
                int boxHeight = maxY - minY + 1;
                if (boxWidth >= TANK_MIN_WIDTH && boxWidth <= TANK_MAX_WIDTH && boxHeight >= TANK_MIN_HEIGHT) {
                    windows.add(new Rect2i(minX, minY, boxWidth, boxHeight));
                }
            }
        }

        windows.sort((left, right) -> {
            int yCompare = Integer.compare(left.getY(), right.getY());
            return yCompare != 0 ? yCompare : Integer.compare(left.getX(), right.getX());
        });
        return List.copyOf(windows);
    }

    private void visitNeighbor(
            NativeImage image,
            int width,
            int height,
            boolean[] visited,
            java.util.ArrayDeque<int[]> queue,
            int x,
            int y
    ) {
        if (x < 0 || x >= width || y < TANK_SCAN_MIN_Y || y >= height) {
            return;
        }
        int index = y * width + x;
        if (visited[index] || !isTankInterior(image.getPixelRGBA(x, y))) {
            return;
        }
        visited[index] = true;
        queue.addLast(new int[]{x, y});
    }

    private boolean isTankInterior(int pixel) {
        int alpha = FastColor.ABGR32.alpha(pixel);
        int red = FastColor.ABGR32.red(pixel);
        int green = FastColor.ABGR32.green(pixel);
        int blue = FastColor.ABGR32.blue(pixel);
        return alpha > 240 && Math.max(red, Math.max(green, blue)) <= TANK_INTERIOR_THRESHOLD;
    }

    private List<Rect2i> fallbackTankWindows() {
        java.util.ArrayList<Rect2i> fallback = new java.util.ArrayList<>(menu.getStorageSlotCount());
        for (int slotIndex = 0; slotIndex < menu.getStorageSlotCount(); slotIndex++) {
            int row = slotIndex / 9;
            int column = slotIndex % 9;
            int x = 9 + column * 21;
            int y = 19 + row * 21;
            fallback.add(new Rect2i(x, y, 16, 24));
        }
        return List.copyOf(fallback);
    }

    private int drawWrapped(GuiGraphics guiGraphics, Component component, int x, int y, int maxWidth, int color) {
        for (FormattedCharSequence line : font.split(component, maxWidth)) {
            guiGraphics.drawString(font, line, x, y, color, false);
            y += INFO_PANEL_LINE_HEIGHT;
        }
        return y;
    }

    private static boolean isWithin(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
