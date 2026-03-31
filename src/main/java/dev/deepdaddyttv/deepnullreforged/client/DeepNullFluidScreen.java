package dev.deepdaddyttv.deepnullreforged.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.integration.mekanism.MekanismClientCompat;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.inventory.StoneGeneratorVariant;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferDirectionMode;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferOutputMode;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import dev.deepdaddyttv.deepnullreforged.compat.fml.ModList;
import dev.deepdaddyttv.deepnullreforged.compat.fluids.FluidStack;
import dev.deepdaddyttv.deepnullreforged.compat.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DeepNullFluidScreen extends AbstractContainerScreen<DeepNullMenu> {
    private static final int BASE_IMAGE_WIDTH = 202;
    private static final int INFO_PANEL_WIDTH = 146;
    private static final int INFO_PANEL_PADDING = 6;
    private static final int INFO_PANEL_LINE_HEIGHT = 10;
    private static final Identifier INFO_BUTTON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_info_button.png");
    private static final Identifier LOCK_BUTTON_OFF_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_lock_button_off.png");
    private static final Identifier LOCK_BUTTON_ON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_lock_button_on.png");
    private static final Identifier UPGRADE_BUTTON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_upgrade_button.png");
    private static final Identifier STONE_BUTTON_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_stone_generator_button.png");
    private static final Identifier INFO_TAB_TEXTURE = DeepNullReforged.id("textures/gui/widgets/deepnull_info_tab.png");
    private static final int TAB_BUTTON_U = 98;
    private static final int INFO_BUTTON_V = 16;
    private static final int LOCK_BUTTON_V = 37;
    private static final int UPGRADE_BUTTON_V = 37;
    private static final int STONE_BUTTON_V = 37;
    private static final int TAB_BUTTON_WIDTH = 13;
    private static final int TAB_BUTTON_HEIGHT = 19;
    private static final int INFO_TAB_U = 105;
    private static final int INFO_TAB_V = 6;
    private static final int INFO_TAB_WIDTH = 146;
    private static final int INFO_TAB_HEIGHT = 170;
    private static final int STONE_PANEL_LABEL_Y = 30;
    private static final int STONE_GRID_START_Y = 44;
    private static final int STONE_GRID_SPACING = 22;
    private static final int STONE_GRID_COLUMNS = 3;
    private static final int TANK_SCAN_MAX_X = 220;
    private static final int TANK_SCAN_MIN_Y = 10;
    private static final int TANK_SCAN_MAX_Y = 180;
    private static final int TANK_INTERIOR_THRESHOLD = 35;
    private static final int TANK_MIN_WIDTH = 10;
    private static final int TANK_MAX_WIDTH = 18;
    private static final int TANK_MIN_HEIGHT = 18;
    private static final int TANK_FILL_INSET_X = 1;
    private static final int TANK_FILL_INSET_Y = 2;
    private static final Map<Identifier, List<Rect2i>> TANK_WINDOW_CACHE = new ConcurrentHashMap<>();

    private final Identifier backgroundTexture;
    private final Identifier tankOverlayTexture;
    private final List<Rect2i> tankWindows;
    private boolean infoPanelOpen;
    private boolean stonePanelOpen;
    private int hoveredTankIndex = -1;

    public DeepNullFluidScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, BASE_IMAGE_WIDTH, imageHeightFor(menu));
        this.inventoryLabelX = 7;
        this.inventoryLabelY = this.imageHeight - 103;
        this.titleLabelX = 7;
        this.titleLabelY = 6;
        this.backgroundTexture = DeepNullReforged.id("textures/gui/dampnullscreen" + menu.getTier().ordinalId() + ".png");
        this.tankOverlayTexture = DeepNullReforged.id("textures/gui/dampnullscreen" + menu.getTier().ordinalId() + "_tank.png");
        this.tankWindows = resolveTankWindows(backgroundTexture, menu.getStorageSlotCount());
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (!menu.getDankInventory().supportsFluidStorage()) {
            PacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.MAIN.ordinal()));
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        renderTankContents(graphics);
        graphics.blit(RenderPipelines.GUI_TEXTURED, tankOverlayTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        renderSideButtons(graphics);

        super.extractContents(graphics, mouseX, mouseY, partialTick);

        if (infoPanelOpen) {
            graphics.nextStratum();
            renderInfoPanel(graphics);
        } else if (stonePanelOpen) {
            graphics.nextStratum();
            renderStoneGeneratorPanel(graphics);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0xFFFFFFFF, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        hoveredTankIndex = getTankIndexAt(mouseX, mouseY);
        renderTankTooltip(graphics, mouseX, mouseY);
        renderSideButtonTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 2 && isWithin(event.x(), event.y(), leftPos, topPos, imageWidth, imageHeight)) {
            return true;
        }
        if (event.button() == 0 && isWithin(event.x(), event.y(), infoButtonX(), topPos + 38, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            infoPanelOpen = !infoPanelOpen;
            if (infoPanelOpen) {
                stonePanelOpen = false;
            }
            return true;
        }
        if (event.button() == 0 && isWithin(event.x(), event.y(), infoButtonX(), topPos + 59, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            toggleTransferOutputMode();
            return true;
        }
        if (event.button() == 0 && isWithin(event.x(), event.y(), infoButtonX(), topPos + 80, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            PacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.UPGRADES.ordinal()));
            return true;
        }
        if (menu.hasUpgrade(DeepNullUpgradeType.STONE_GENERATOR)
                && event.button() == 0
                && isWithin(event.x(), event.y(), infoButtonX(), topPos + 101, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            stonePanelOpen = !stonePanelOpen;
            if (stonePanelOpen) {
                infoPanelOpen = false;
            }
            return true;
        }

        if (stonePanelOpen && event.button() == 0) {
            StoneGeneratorVariant clickedVariant = stoneVariantAt(event.x(), event.y());
            if (clickedVariant != null) {
                menu.getDankInventory().setStoneGeneratorVariant(clickedVariant);
                PacketDistributor.sendToServer(new DeepNullPayloads.MenuStoneVariantPayload(clickedVariant.ordinal()));
                return true;
            }
        }

        int tankIndex = getTankIndexAt(event.x(), event.y());
        if (tankIndex >= 0 && menu.getCarried().isEmpty()) {
            if (event.button() == 0 && event.hasShiftDown()) {
                if (menu.clearFluidSlot(tankIndex)) {
                    PacketDistributor.sendToServer(new DeepNullPayloads.MenuSlotActionPayload(
                            tankIndex,
                            DeepNullPayloads.MenuSlotAction.CLEAR_FLUID.ordinal()
                    ));
                }
                return true;
            }
            if (event.button() == 0) {
                menu.getDankInventory().setSelectedSlot(tankIndex);
                PacketDistributor.sendToServer(new DeepNullPayloads.MenuSlotActionPayload(
                        tankIndex,
                        DeepNullPayloads.MenuSlotAction.SELECT.ordinal()
                ));
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void renderTankContents(GuiGraphicsExtractor graphics) {
        int capacity = menu.getDankInventory().getFluidCapacity();
        if (capacity <= 0) {
            return;
        }

        for (int slotIndex = 0; slotIndex < tankWindows.size() && slotIndex < menu.getStorageSlotCount(); slotIndex++) {
            FluidStack fluidStack = menu.getDankInventory().getFluidInSlot(slotIndex);
            if (!fluidStack.isEmpty()) {
                renderFluidInTank(graphics, tankWindows.get(slotIndex), fluidStack, capacity);
                continue;
            }
            StoredChemical chemicalStack = menu.getDankInventory().getChemicalInSlot(slotIndex);
            if (!chemicalStack.isEmpty()) {
                renderChemicalInTank(graphics, tankWindows.get(slotIndex), chemicalStack, capacity);
            }
        }
    }

    private void renderFluidInTank(GuiGraphicsExtractor graphics, Rect2i tankWindow, FluidStack fluidStack, int capacity) {
        TextureAtlasSprite sprite = ClientFluidRendering.getStillSprite(fluidStack);
        int tint = ensureOpaque(ClientFluidRendering.getTint(fluidStack));
        Rect2i fillWindow = visibleFillWindow(tankWindow);
        int tankWidth = fillWindow.getWidth();
        int tankHeight = fillWindow.getHeight();
        int fillHeight = Math.max(1, Math.round(tankHeight * Math.min(1.0F, fluidStack.getAmount() / (float) capacity)));
        int drawX = leftPos + fillWindow.getX();
        int drawY = topPos + fillWindow.getY() + (tankHeight - fillHeight);

        if (sprite == null) {
            graphics.fill(drawX, drawY, drawX + tankWidth, topPos + fillWindow.getY() + tankHeight, tint == 0 ? 0xFF3AA7FF : tint);
            return;
        }
        int color = tint == 0 ? 0xFFFFFFFF : tint;
        for (int offsetY = 0; offsetY < fillHeight; offsetY += 16) {
            int drawHeight = Math.min(16, fillHeight - offsetY);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, drawX, drawY + offsetY, tankWidth, drawHeight, color);
        }
    }

    private Rect2i visibleFillWindow(Rect2i tankWindow) {
        int x = tankWindow.getX() + TANK_FILL_INSET_X;
        int y = tankWindow.getY() + TANK_FILL_INSET_Y;
        int width = Math.max(1, tankWindow.getWidth() - (TANK_FILL_INSET_X * 2));
        int height = Math.max(1, tankWindow.getHeight() - (TANK_FILL_INSET_Y * 2));
        return new Rect2i(x, y, width, height);
    }

    private void renderChemicalInTank(GuiGraphicsExtractor graphics, Rect2i tankWindow, StoredChemical chemicalStack, int capacity) {
        Rect2i fillWindow = visibleFillWindow(tankWindow);
        int tankWidth = fillWindow.getWidth();
        int tankHeight = fillWindow.getHeight();
        int fillHeight = Math.max(1, Math.round(tankHeight * Math.min(1.0F, chemicalStack.amount() / (float) capacity)));
        int drawX = leftPos + fillWindow.getX();
        int drawY = topPos + fillWindow.getY() + (tankHeight - fillHeight);

        TextureAtlasSprite sprite = MekanismClientCompat.getChemicalSprite(chemicalStack);
        if (sprite == null) {
            int tint = ensureOpaque(chemicalStack.tint());
            graphics.fill(drawX, drawY, drawX + tankWidth, topPos + fillWindow.getY() + tankHeight, tint == 0 ? 0xFFFFFFFF : tint);
            return;
        }

        int color = ensureOpaque(chemicalStack.tint());
        if (color == 0) {
            color = 0xFFFFFFFF;
        }
        for (int offsetY = 0; offsetY < fillHeight; offsetY += 16) {
            int drawHeight = Math.min(16, fillHeight - offsetY);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, drawX, drawY + offsetY, tankWidth, drawHeight, color);
        }
    }

    private void renderTankTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int tankIndex = getTankIndexAt(mouseX, mouseY);
        if (tankIndex < 0 || tankIndex >= menu.getStorageSlotCount()) {
            return;
        }

        FluidStack fluidStack = menu.getDankInventory().getFluidInSlot(tankIndex);
        StoredChemical chemicalStack = menu.getDankInventory().getChemicalInSlot(tankIndex);
        if (fluidStack.isEmpty() && chemicalStack.isEmpty()) {
            graphics.setComponentTooltipForNextFrame(
                    font,
                    List.of(
                            Component.translatable("dn.empty.desc"),
                            Component.translatable("dn.capacity.desc").append(": ").append(Component.literal(fluidCapacityText())),
                            Component.translatable(chemicalTransferHintKey()),
                            Component.translatable("dn.shift_click_clear_tank.desc")
                    ),
                    mouseX,
                    mouseY
            );
            return;
        }

        graphics.setComponentTooltipForNextFrame(
                font,
                List.of(
                        fluidStack.isEmpty() ? chemicalStack.getHoverName() : fluidStack.getHoverName(),
                        Component.translatable("dn.amount.desc").append(": ").append(Component.literal(fluidAmountText(fluidStack, chemicalStack))),
                        Component.translatable("dn.capacity.desc").append(": ").append(Component.literal(fluidCapacityText())),
                        Component.translatable("dn.shift_click_clear_tank.desc")
                ),
                mouseX,
                mouseY
        );
    }

    private void renderSideButtons(GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, INFO_BUTTON_TEXTURE, infoButtonX(), topPos + 38, TAB_BUTTON_U, INFO_BUTTON_V, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT, 256, 256);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                menu.getDankInventory().getTransferOutputMode().isLocked() ? LOCK_BUTTON_ON_TEXTURE : LOCK_BUTTON_OFF_TEXTURE,
                infoButtonX(),
                topPos + 59,
                TAB_BUTTON_U,
                LOCK_BUTTON_V,
                TAB_BUTTON_WIDTH,
                TAB_BUTTON_HEIGHT,
                256,
                256
        );
        graphics.blit(RenderPipelines.GUI_TEXTURED, UPGRADE_BUTTON_TEXTURE, infoButtonX(), topPos + 80, TAB_BUTTON_U, UPGRADE_BUTTON_V, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT, 256, 256);
        if (menu.hasUpgrade(DeepNullUpgradeType.STONE_GENERATOR)) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, STONE_BUTTON_TEXTURE, infoButtonX(), topPos + 101, TAB_BUTTON_U, STONE_BUTTON_V, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT, 256, 256);
        }
    }

    private void renderSideButtonTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (isWithin(mouseX, mouseY, infoButtonX(), topPos + 38, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            graphics.setTooltipForNextFrame(font, Component.translatable("dn.info.desc"), mouseX, mouseY);
        } else if (isWithin(mouseX, mouseY, infoButtonX(), topPos + 59, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            graphics.setTooltipForNextFrame(font, transferLockLabel(), mouseX, mouseY);
        } else if (isWithin(mouseX, mouseY, infoButtonX(), topPos + 80, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            graphics.setTooltipForNextFrame(font, Component.translatable("dn.upgrades_screen.desc"), mouseX, mouseY);
        } else if (menu.hasUpgrade(DeepNullUpgradeType.STONE_GENERATOR)
                && isWithin(mouseX, mouseY, infoButtonX(), topPos + 101, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            graphics.setTooltipForNextFrame(font, Component.translatable("upgrade.stone_generator_upgrade.installed"), mouseX, mouseY);
        }
    }

    private void renderInfoPanel(GuiGraphicsExtractor graphics) {
        int panelX = infoPanelX();
        int panelY = topPos + 4;
        int textX = panelX + 14;
        int lineY = panelY + 12;
        int textWidth = INFO_PANEL_WIDTH - 24;
        int tankIndex = getContextTankIndex();
        FluidStack fluidStack = tankIndex >= 0 && tankIndex < menu.getStorageSlotCount()
                ? menu.getDankInventory().getFluidInSlot(tankIndex)
                : FluidStack.EMPTY;
        StoredChemical chemicalStack = tankIndex >= 0 && tankIndex < menu.getStorageSlotCount()
                ? menu.getDankInventory().getChemicalInSlot(tankIndex)
                : StoredChemical.EMPTY;

        graphics.blit(RenderPipelines.GUI_TEXTURED, INFO_TAB_TEXTURE, panelX, panelY, INFO_TAB_U, INFO_TAB_V, INFO_TAB_WIDTH, INFO_TAB_HEIGHT, 256, 256);
        graphics.text(font, infoPanelTitle(), textX, lineY, 0xFFFFFFFF, false);
        lineY += 18;

        if (fluidStack.isEmpty() && chemicalStack.isEmpty()) {
            lineY = drawWrapped(graphics, Component.translatable("dn.fluid_hover_for_details.desc"), textX, lineY, textWidth, 0xFFC9D0DB);
            lineY += 4;
            lineY = drawWrapped(graphics, Component.translatable("dn.left_click_select_tank.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            lineY = drawWrapped(graphics, Component.translatable("dn.shift_click_clear_tank.desc"), textX, lineY, textWidth, 0xFF99A5B5);
            drawWrapped(graphics, Component.translatable(chemicalTransferHintKey()), textX, lineY, textWidth, 0xFF99A5B5);
            return;
        }

        lineY = drawWrapped(graphics, fluidStack.isEmpty() ? chemicalStack.getHoverName() : fluidStack.getHoverName(), textX, lineY, textWidth, 0xFFFFFFFF);
        lineY += 4;
        lineY = drawWrapped(graphics, Component.translatable("dn.amount.desc").append(": ").append(Component.literal(fluidAmountText(fluidStack, chemicalStack))), textX, lineY, textWidth, 0xFFE8EDF5);
        lineY = drawWrapped(graphics, Component.translatable("dn.capacity.desc").append(": ").append(Component.literal(fluidCapacityText())), textX, lineY, textWidth, 0xFFE8EDF5);
        lineY += 4;
        lineY = drawWrapped(graphics, Component.translatable("dn.left_click_select_tank.desc"), textX, lineY, textWidth, 0xFF99A5B5);
        drawWrapped(graphics, Component.translatable("dn.shift_click_clear_tank.desc"), textX, lineY, textWidth, 0xFF99A5B5);
    }

    private void renderStoneGeneratorPanel(GuiGraphicsExtractor graphics) {
        int panelX = infoPanelX();
        int panelY = topPos + 4;
        int textX = panelX + 14;
        int lineY = panelY + 12;

        graphics.blit(RenderPipelines.GUI_TEXTURED, INFO_TAB_TEXTURE, panelX, panelY, INFO_TAB_U, INFO_TAB_V, INFO_TAB_WIDTH, INFO_TAB_HEIGHT, 256, 256);
        graphics.text(font, Component.translatable("upgrade.stone_generator_upgrade.installed"), textX, lineY, 0xFFFFFFFF, false);
        graphics.text(font, Component.translatable("dn.stone_generator_select.desc"), textX, panelY + STONE_PANEL_LABEL_Y, 0xFFC9D0DB, false);

        StoneGeneratorVariant selected = menu.getStoneGeneratorVariant();
        int startX = textX;
        int startY = panelY + STONE_GRID_START_Y;
        for (int index = 0; index < StoneGeneratorVariant.values().length; index++) {
            StoneGeneratorVariant variant = StoneGeneratorVariant.values()[index];
            int x = startX + (index % STONE_GRID_COLUMNS) * STONE_GRID_SPACING;
            int y = startY + (index / STONE_GRID_COLUMNS) * STONE_GRID_SPACING;
            graphics.item(variant.stack(), x, y);
            if (variant == selected) {
                graphics.outline(x - 1, y - 1, 18, 18, 0xFFE7F2FF);
            }
        }

        int detailY = startY + 54;
        drawWrapped(graphics, Component.translatable("dn.stone_generator_rate.desc", menu.getDankInventory().getStoneGenerationRate()), textX, detailY, INFO_PANEL_WIDTH - 24, 0xFFE8EDF5);
    }

    private Component infoPanelTitle() {
        return Component.translatable("item.deepnullreforged.damp_null_" + menu.getTier().ordinalId());
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

    private String fluidAmountText(FluidStack fluidStack, StoredChemical chemicalStack) {
        return menu.getTier().creative()
                ? Component.translatable("dn.infinite.desc").getString()
                : (!fluidStack.isEmpty() ? fluidStack.getAmount() : chemicalStack.amount()) + " mB";
    }

    private String chemicalTransferHintKey() {
        return menu.hasUpgrade(DeepNullUpgradeType.GAS) && ModList.get().isLoaded("mekanism")
                ? "dn.chemical_transfer_only.desc"
                : "dn.fluid_empty_hint.desc";
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

    public Rect2i getInfoPanelArea() {
        if (!infoPanelOpen && !stonePanelOpen) {
            return null;
        }
        return new Rect2i(infoPanelX(), topPos + 4, INFO_TAB_WIDTH, INFO_TAB_HEIGHT);
    }

    private @Nullable StoneGeneratorVariant stoneVariantAt(double mouseX, double mouseY) {
        int panelX = infoPanelX();
        int panelY = topPos + 4;
        int startX = panelX + 14;
        int startY = panelY + STONE_GRID_START_Y;
        for (int index = 0; index < StoneGeneratorVariant.values().length; index++) {
            int x = startX + (index % STONE_GRID_COLUMNS) * STONE_GRID_SPACING;
            int y = startY + (index / STONE_GRID_COLUMNS) * STONE_GRID_SPACING;
            if (isWithin(mouseX, mouseY, x, y, 16, 16)) {
                return StoneGeneratorVariant.values()[index];
            }
        }
        return null;
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

    private List<Rect2i> resolveTankWindows(Identifier texture, int expectedCount) {
        List<Rect2i> detected = TANK_WINDOW_CACHE.computeIfAbsent(texture, this::detectTankWindows);
        if (detected.size() == expectedCount) {
            return detected;
        }

        DeepNullReforged.LOGGER.warn("Expected {} DampNull tank windows in {}, found {}", expectedCount, texture, detected.size());
        return fallbackTankWindows();
    }

    private List<Rect2i> detectTankWindows(Identifier texture) {
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
        List<Rect2i> windows = new ArrayList<>();

        for (int y = TANK_SCAN_MIN_Y; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (visited[index] || !isTankInterior(image.getPixel(x, y))) {
                    continue;
                }

                int minX = x;
                int maxX = x;
                int minY = y;
                int maxY = y;
                ArrayDeque<int[]> queue = new ArrayDeque<>();
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
            ArrayDeque<int[]> queue,
            int x,
            int y
    ) {
        if (x < 0 || x >= width || y < TANK_SCAN_MIN_Y || y >= height) {
            return;
        }
        int index = y * width + x;
        if (visited[index] || !isTankInterior(image.getPixel(x, y))) {
            return;
        }
        visited[index] = true;
        queue.addLast(new int[]{x, y});
    }

    private boolean isTankInterior(int pixel) {
        int alpha = ARGB.alpha(pixel);
        int red = ARGB.red(pixel);
        int green = ARGB.green(pixel);
        int blue = ARGB.blue(pixel);
        return alpha > 240 && Math.max(red, Math.max(green, blue)) <= TANK_INTERIOR_THRESHOLD;
    }

    private static int ensureOpaque(int tint) {
        return (tint >>> 24) == 0 ? tint | 0xFF000000 : tint;
    }

    private List<Rect2i> fallbackTankWindows() {
        ArrayList<Rect2i> fallback = new ArrayList<>(menu.getStorageSlotCount());
        for (int slotIndex = 0; slotIndex < menu.getStorageSlotCount(); slotIndex++) {
            int row = slotIndex / 9;
            int column = slotIndex % 9;
            int x = 9 + column * 21;
            int y = 19 + row * 21;
            fallback.add(new Rect2i(x, y, 16, 24));
        }
        return List.copyOf(fallback);
    }

    private int drawWrapped(GuiGraphicsExtractor graphics, Component component, int x, int y, int maxWidth, int color) {
        for (FormattedCharSequence line : font.split(component, maxWidth)) {
            graphics.text(font, line, x, y, color, false);
            y += INFO_PANEL_LINE_HEIGHT;
        }
        return y;
    }

    private Component transferLockLabel() {
        return ClientUiText.transferOutputModeMessage(true, menu.getDankInventory().getTransferOutputMode());
    }

    public TransferOutputMode toggleTransferOutputMode() {
        TransferOutputMode next = menu.getDankInventory().cycleTransferOutputMode();
        PacketDistributor.sendToServer(new DeepNullPayloads.MenuTransferModePayload(next.ordinal()));
        return next;
    }

    public TransferDirectionMode toggleTransferDirectionMode() {
        TransferDirectionMode next = menu.getDankInventory().cycleTransferDirectionMode();
        PacketDistributor.sendToServer(new DeepNullPayloads.MenuTransferDirectionPayload(next.ordinal()));
        return next;
    }

    private static int imageHeightFor(DeepNullMenu menu) {
        return 141 + Math.max(0, menu.getTier().rows() - 1) * 21;
    }

    private static boolean isWithin(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
