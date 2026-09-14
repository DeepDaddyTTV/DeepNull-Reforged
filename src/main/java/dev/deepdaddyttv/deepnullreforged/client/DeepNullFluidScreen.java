package dev.deepdaddyttv.deepnullreforged.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.integration.mekanism.MekanismClientCompat;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.inventory.NullSlotDomain;
import dev.deepdaddyttv.deepnullreforged.inventory.NullStorageAction;
import dev.deepdaddyttv.deepnullreforged.inventory.StoneGeneratorVariant;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferDirectionMode;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferOutputMode;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DeepNullFluidScreen extends AbstractContainerScreen<DeepNullMenu> implements StorageActionResultListener {
    private static final List<NullShortcutController.Action> SHORTCUT_ACTIONS = List.of(
            NullShortcutController.Action.SWAP,
            NullShortcutController.Action.MERGE,
            NullShortcutController.Action.CLEAR,
            NullShortcutController.Action.SELECT
    );
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
    private static final @Nullable Method FLUID_STACK_STILL_TEXTURE = findClientFluidMethod("getStillTexture", FluidStack.class);
    private static final @Nullable Method SIMPLE_STILL_TEXTURE = findClientFluidMethod("getStillTexture");
    private static final @Nullable Method FLUID_STACK_TINT_COLOR = findClientFluidMethod("getTintColor", FluidStack.class);
    private static final @Nullable Method SIMPLE_TINT_COLOR = findClientFluidMethod("getTintColor");

    private final Identifier backgroundTexture;
    private final Identifier tankOverlayTexture;
    private final List<Rect2i> tankWindows;
    private boolean infoPanelOpen;
    private boolean stonePanelOpen;
    private int hoveredTankIndex = -1;
    private final NullShortcutController shortcutController = new NullShortcutController();
    private final NullShortcutActionAnimations shortcutAnimations = new NullShortcutActionAnimations();
    private final Map<Long, PendingTankAction> pendingTankActions = new HashMap<>();
    private final List<TankVisualAnimation> tankAnimations = new ArrayList<>();
    private long nextStorageNonce = 1L;
    private int pendingSwapTank = -1;
    private int pendingMergeTank = -1;
    private int pendingClearTank = -1;
    private TankContents pendingClearContents = TankContents.EMPTY;

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
        if (pendingClearTank >= 0
                && shortcutController.activeAction(SHORTCUT_ACTIONS) != NullShortcutController.Action.CLEAR
                && !isShiftDown()) {
            clearPendingClear();
        } else if (pendingClearTank >= 0 && !tankContents(pendingClearTank).matches(pendingClearContents)) {
            clearPendingClear();
        }
        if (!menu.getDankInventory().supportsFluidStorage()) {
            ClientPacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.MAIN.ordinal()));
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        renderSelectedTank(graphics);
        renderTankContents(graphics);
        renderTankActionContents(graphics);
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
        graphics.nextStratum();
        shortcutAnimations.render(graphics, font);
        shortcutController.render(
                graphics,
                font,
                mouseX,
                mouseY,
                leftPos,
                topPos,
                imageWidth,
                imageHeight,
                SHORTCUT_ACTIONS,
                this::hasPendingAction
        );
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
        shortcutController.renderTooltip(graphics, font, mouseX, mouseY, SHORTCUT_ACTIONS);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        NullShortcutController.Action chip = shortcutController.handleClick(event.x(), event.y(), event.button(), SHORTCUT_ACTIONS);
        if (chip != null) {
            if (shortcutController.isArmed(chip)) {
                cancelPendingExcept(chip);
            } else {
                cancelAllPendingActions();
            }
            return true;
        }
        if (shortcutController.consumedLastClick()) {
            return true;
        }
        if (ClientModEvents.isTertiaryGuiButton(event.button()) && isWithin(event.x(), event.y(), leftPos, topPos, imageWidth, imageHeight)) {
            return true;
        }
        int tankIndex = getTankIndexAt(event.x(), event.y());
        if (pendingClearTank >= 0 && tankIndex != pendingClearTank) {
            clearPendingClear();
        }
        if (ClientModEvents.isPrimaryGuiButton(event.button()) && isWithin(event.x(), event.y(), infoButtonX(), topPos + 38, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            cancelAllPendingActions();
            infoPanelOpen = !infoPanelOpen;
            if (infoPanelOpen) {
                stonePanelOpen = false;
            }
            return true;
        }
        if (ClientModEvents.isPrimaryGuiButton(event.button()) && isWithin(event.x(), event.y(), infoButtonX(), topPos + 59, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            cancelAllPendingActions();
            toggleTransferOutputMode();
            return true;
        }
        if (ClientModEvents.isPrimaryGuiButton(event.button()) && isWithin(event.x(), event.y(), infoButtonX(), topPos + 80, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            cancelAllPendingActions();
            ClientPacketDistributor.sendToServer(new DeepNullPayloads.OpenMenuViewPayload(DeepNullMenu.ViewMode.UPGRADES.ordinal()));
            return true;
        }
        if (menu.hasUpgrade(DeepNullUpgradeType.STONE_GENERATOR)
                && ClientModEvents.isPrimaryGuiButton(event.button())
                && isWithin(event.x(), event.y(), infoButtonX(), topPos + 101, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)) {
            cancelAllPendingActions();
            stonePanelOpen = !stonePanelOpen;
            if (stonePanelOpen) {
                infoPanelOpen = false;
            }
            return true;
        }

        if (stonePanelOpen && ClientModEvents.isPrimaryGuiButton(event.button())) {
            StoneGeneratorVariant clickedVariant = stoneVariantAt(event.x(), event.y());
            if (clickedVariant != null) {
                menu.getDankInventory().setStoneGeneratorVariant(clickedVariant);
                ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuStoneVariantPayload(clickedVariant.ordinal()));
                return true;
            }
        }

        if (tankIndex >= 0 && menu.getCarried().isEmpty()) {
            if (ClientModEvents.isPrimaryGuiButton(event.button()) && event.hasShiftDown()) {
                handleTankClearConfirmation(tankIndex);
                return true;
            }
            if (handleShortcutTankClick(tankIndex, event.button())) {
                return true;
            }
            if (ClientModEvents.isPrimaryGuiButton(event.button())) {
                requestStorageAction(NullStorageAction.SELECT, tankIndex, tankIndex);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (shortcutController.handleEscape(event.key())) {
            cancelAllPendingActions();
            return true;
        }
        if (shortcutController.handleKeyPressed(event, SHORTCUT_ACTIONS) != null) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        shortcutController.handleKeyReleased(event, SHORTCUT_ACTIONS);
        if (shortcutController.shouldCancelPendingOnRelease(NullShortcutController.Action.SWAP)) {
            pendingSwapTank = -1;
        }
        if (shortcutController.shouldCancelPendingOnRelease(NullShortcutController.Action.MERGE)) {
            pendingMergeTank = -1;
        }
        if (shortcutController.shouldCancelPendingOnRelease(NullShortcutController.Action.CLEAR)) {
            clearPendingClear();
        }
        return super.keyReleased(event);
    }

    private boolean handleShortcutTankClick(int tankIndex, int button) {
        boolean primary = ClientModEvents.isPrimaryGuiButton(button);
        boolean secondary = ClientModEvents.isSecondaryGuiButton(button);
        if (!primary && !secondary) {
            return false;
        }
        TankContents contents = tankContents(tankIndex);
        NullShortcutController.Action activeAction = shortcutController.activeAction(SHORTCUT_ACTIONS);
        if (activeAction == NullShortcutController.Action.CLEAR) {
            if (!primary || contents.isEmpty()) {
                clearPendingClear();
                return true;
            }
            handleTankClearConfirmation(tankIndex);
            return true;
        }
        if (activeAction == NullShortcutController.Action.SELECT) {
            if (!contents.isEmpty()) {
                requestStorageAction(NullStorageAction.SELECT, tankIndex, tankIndex);
            }
            return true;
        }
        if (activeAction == NullShortcutController.Action.MERGE) {
            if (contents.isEmpty()) {
                pendingMergeTank = -1;
                return true;
            }
            if (pendingMergeTank < 0) {
                pendingMergeTank = tankIndex;
                return true;
            }
            int source = pendingMergeTank;
            pendingMergeTank = -1;
            if (source != tankIndex) {
                requestStorageAction(NullStorageAction.MERGE, source, tankIndex);
            }
            return true;
        }
        if (activeAction == NullShortcutController.Action.SWAP) {
            if (pendingSwapTank < 0) {
                pendingSwapTank = tankIndex;
                return true;
            }
            int source = pendingSwapTank;
            pendingSwapTank = -1;
            if (source != tankIndex) {
                requestStorageAction(NullStorageAction.SWAP, source, tankIndex);
            }
            return true;
        }
        return false;
    }

    private void handleTankClearConfirmation(int tankIndex) {
        TankContents contents = tankContents(tankIndex);
        if (contents.isEmpty()) {
            clearPendingClear();
            return;
        }
        if (pendingClearTank < 0) {
            pendingClearTank = tankIndex;
            pendingClearContents = contents;
            return;
        }
        if (pendingClearTank != tankIndex || !contents.matches(pendingClearContents)) {
            clearPendingClear();
            return;
        }
        requestStorageAction(NullStorageAction.CLEAR, tankIndex, tankIndex);
    }

    private void requestStorageAction(NullStorageAction action, int sourceTank, int targetTank) {
        long nonce = nextStorageNonce++;
        pendingTankActions.put(nonce, new PendingTankAction(
                action,
                sourceTank,
                targetTank,
                tankContents(sourceTank),
                tankContents(targetTank)
        ));
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.StorageActionRequestPayload(
                menu.containerId,
                nonce,
                action.id(),
                NullSlotDomain.FLUID_STORAGE.id(),
                sourceTank,
                targetTank
        ));
    }

    @Override
    public void deepNullReforged$handleStorageActionResult(DeepNullPayloads.StorageActionResultPayload payload) {
        PendingTankAction pending = pendingTankActions.remove(payload.nonce());
        if (pending == null
                || payload.domainId() != NullSlotDomain.FLUID_STORAGE.id()
                || payload.actionId() != pending.action().id()
                || payload.sourceSlot() != pending.sourceTank()
                || payload.targetSlot() != pending.targetTank()
                || !payload.success()) {
            if (pending != null) {
                cancelPendingFor(pending.action());
            }
            return;
        }
        Rect2i sourceBounds = tankBounds(pending.sourceTank());
        Rect2i targetBounds = tankBounds(pending.targetTank());
        long startMs = System.currentTimeMillis();
        switch (pending.action()) {
            case SWAP -> {
                long duration = NullShortcutActionAnimations.tankSwapFlowDurationMs(
                        pending.sourceBefore().amount(),
                        pending.targetBefore().amount(),
                        menu.getDankInventory().getFluidCapacity()
                );
                tankAnimations.add(new TankVisualAnimation(
                        pending.action(), pending.sourceTank(), pending.targetTank(), pending.sourceBefore(), pending.targetBefore(),
                        tankContents(pending.sourceTank()), tankContents(pending.targetTank()), startMs, duration
                ));
            }
            case MERGE -> tankAnimations.add(new TankVisualAnimation(
                    pending.action(), pending.sourceTank(), pending.targetTank(), pending.sourceBefore(), pending.targetBefore(),
                    tankContents(pending.sourceTank()), tankContents(pending.targetTank()), startMs, NullShortcutActionAnimations.TANK_MERGE_MS
            ));
            case CLEAR -> {
                tankAnimations.add(new TankVisualAnimation(
                        pending.action(), pending.sourceTank(), pending.targetTank(), pending.sourceBefore(), pending.targetBefore(),
                        TankContents.EMPTY, TankContents.EMPTY, startMs, NullShortcutActionAnimations.DELETE_MS
                ));
                shortcutAnimations.startDelete(pending.targetTank(), ItemStack.EMPTY, targetBounds, pending.targetBefore().tint());
            }
            case SELECT -> shortcutAnimations.startSelectPixelPad(pending.targetTank(), targetBounds);
            case SORT, CYCLE_FORWARD, CYCLE_BACKWARD -> { }
        }
        cancelPendingFor(pending.action());
        if (pending.action() != NullStorageAction.SORT) {
            shortcutController.clearArmed(actionFor(pending.action()));
        }
    }

    private void renderSelectedTank(GuiGraphicsExtractor graphics) {
        int selected = menu.getDankInventory().getSelectedSlot();
        if (selected < 0 || selected >= tankWindows.size()) {
            return;
        }
        Rect2i bounds = tankBounds(selected);
        graphics.fillGradient(bounds.getX() - 1, bounds.getY() - 1, bounds.getX() + bounds.getWidth() + 1, bounds.getY() + bounds.getHeight() + 1, selectedPaletteColor(0x48), selectedPaletteColor(0x18));
        graphics.outline(bounds.getX() - 1, bounds.getY() - 1, bounds.getWidth() + 2, bounds.getHeight() + 2, selectedPaletteColor(0xFF));
    }

    private int selectedPaletteColor(int alpha) {
        int rgb = switch (menu.getTier()) {
            case REDSTONE -> 0xD44848;
            case LAPIS -> 0x4878D4;
            case IRON -> 0xD8DCE5;
            case GOLD -> 0xE7B623;
            case DIAMOND -> 0x42C8D8;
            case EMERALD -> 0x3ED47A;
            case CREATIVE -> 0xC767E8;
        };
        return (alpha << 24) | rgb;
    }

    private void renderTankActionContents(GuiGraphicsExtractor graphics) {
        long now = System.currentTimeMillis();
        for (TankVisualAnimation animation : tankAnimations) {
            long elapsed = Math.max(0L, now - animation.startMs());
            if (animation.action() == NullStorageAction.SWAP) {
                NullShortcutActionAnimations.TankSwapFlowFrame frame = NullShortcutActionAnimations.tankSwapFlowFrame(
                        animation.sourceBefore().amount(), animation.targetBefore().amount(), elapsed, animation.durationMs());
                if (frame.sourceDrainAmount() > 0L || frame.destinationDrainAmount() > 0L) {
                    renderTankSnapshot(graphics, animation.sourceTank(), animation.sourceBefore(), frame.sourceDrainAmount());
                    renderTankSnapshot(graphics, animation.targetTank(), animation.targetBefore(), frame.destinationDrainAmount());
                } else {
                    renderTankSnapshot(graphics, animation.sourceTank(), animation.targetBefore(), frame.destinationFillAmount());
                    renderTankSnapshot(graphics, animation.targetTank(), animation.sourceBefore(), frame.sourceFillAmount());
                }
            } else if (animation.action() == NullStorageAction.MERGE) {
                int sourceAmount = NullShortcutActionAnimations.tankMergeAmount(
                        saturatedInt(animation.sourceBefore().amount()), saturatedInt(animation.sourceAfter().amount()), elapsed);
                int targetAmount = NullShortcutActionAnimations.tankMergeAmount(
                        saturatedInt(animation.targetBefore().amount()), saturatedInt(animation.targetAfter().amount()), elapsed);
                renderTankSnapshot(graphics, animation.sourceTank(), animation.sourceBefore(), sourceAmount);
                renderTankSnapshot(graphics, animation.targetTank(), animation.targetAfter(), targetAmount);
            }
        }
        tankAnimations.removeIf(animation -> now - animation.startMs() >= animation.durationMs());
    }

    private void renderTankSnapshot(GuiGraphicsExtractor graphics, int tankIndex, TankContents contents, long amount) {
        if (amount <= 0L || contents.isEmpty() || tankIndex < 0 || tankIndex >= tankWindows.size()) {
            return;
        }
        int capacity = menu.getDankInventory().getFluidCapacity();
        if (!contents.fluid().isEmpty()) {
            renderFluidInTank(graphics, tankWindows.get(tankIndex), contents.fluid().copyWithAmount(saturatedInt(amount)), capacity);
        } else if (!contents.chemical().isEmpty()) {
            renderChemicalInTank(graphics, tankWindows.get(tankIndex), contents.chemical().copyWithAmount(amount), capacity);
        }
    }

    private boolean isTankSuppressed(int tankIndex) {
        long now = System.currentTimeMillis();
        for (TankVisualAnimation animation : tankAnimations) {
            if (animation.active(now) && (animation.sourceTank() == tankIndex || animation.targetTank() == tankIndex)) {
                return true;
            }
        }
        return false;
    }

    private TankContents tankContents(int tankIndex) {
        if (tankIndex < 0 || tankIndex >= menu.getStorageSlotCount()) {
            return TankContents.EMPTY;
        }
        return new TankContents(
                menu.getDankInventory().getFluidInSlot(tankIndex).copy(),
                menu.getDankInventory().getChemicalInSlot(tankIndex).copy()
        );
    }

    private Rect2i tankBounds(int tankIndex) {
        if (tankIndex < 0 || tankIndex >= tankWindows.size()) {
            return new Rect2i(0, 0, 1, 1);
        }
        Rect2i relative = tankWindows.get(tankIndex);
        return new Rect2i(leftPos + relative.getX(), topPos + relative.getY(), relative.getWidth(), relative.getHeight());
    }

    private boolean hasPendingAction(NullShortcutController.Action action) {
        return switch (action) {
            case SWAP -> pendingSwapTank >= 0;
            case MERGE -> pendingMergeTank >= 0;
            case CLEAR -> pendingClearTank >= 0;
            case SELECT, CYCLE -> false;
        };
    }

    private void cancelPendingExcept(NullShortcutController.Action action) {
        if (action != NullShortcutController.Action.SWAP) pendingSwapTank = -1;
        if (action != NullShortcutController.Action.MERGE) pendingMergeTank = -1;
        if (action != NullShortcutController.Action.CLEAR) clearPendingClear();
    }

    private void cancelPendingFor(NullStorageAction action) {
        switch (action) {
            case SWAP -> pendingSwapTank = -1;
            case MERGE -> pendingMergeTank = -1;
            case CLEAR -> clearPendingClear();
            default -> { }
        }
    }

    private void clearPendingClear() {
        pendingClearTank = -1;
        pendingClearContents = TankContents.EMPTY;
    }

    private void cancelAllPendingActions() {
        pendingSwapTank = -1;
        pendingMergeTank = -1;
        clearPendingClear();
    }

    private static NullShortcutController.Action actionFor(NullStorageAction action) {
        return switch (action) {
            case SWAP -> NullShortcutController.Action.SWAP;
            case MERGE -> NullShortcutController.Action.MERGE;
            case CLEAR -> NullShortcutController.Action.CLEAR;
            case SELECT -> NullShortcutController.Action.SELECT;
            case CYCLE_FORWARD, CYCLE_BACKWARD -> NullShortcutController.Action.CYCLE;
            case SORT -> NullShortcutController.Action.SWAP;
        };
    }

    private static int saturatedInt(long value) {
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, value));
    }

    private void renderTankContents(GuiGraphicsExtractor graphics) {
        int capacity = menu.getDankInventory().getFluidCapacity();
        if (capacity <= 0) {
            return;
        }

        for (int slotIndex = 0; slotIndex < tankWindows.size() && slotIndex < menu.getStorageSlotCount(); slotIndex++) {
            if (isTankSuppressed(slotIndex)) {
                continue;
            }
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
        IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        Identifier texture = resolveStillTexture(clientFluid, fluidStack);
        int tint = ensureOpaque(resolveTintColor(clientFluid, fluidStack));
        Rect2i fillWindow = visibleFillWindow(tankWindow);
        int tankWidth = fillWindow.getWidth();
        int tankHeight = fillWindow.getHeight();
        int fillHeight = Math.max(1, Math.round(tankHeight * Math.min(1.0F, fluidStack.getAmount() / (float) capacity)));
        int drawX = leftPos + fillWindow.getX();
        int drawY = topPos + fillWindow.getY() + (tankHeight - fillHeight);

        if (texture == null) {
            graphics.fill(drawX, drawY, drawX + tankWidth, topPos + fillWindow.getY() + tankHeight, tint == 0 ? 0xFF3AA7FF : tint);
            return;
        }

        TextureAtlasSprite sprite = resolveFluidSprite(texture);
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

    private static @Nullable Method findClientFluidMethod(String name, Class<?>... parameterTypes) {
        try {
            return IClientFluidTypeExtensions.class.getMethod(name, parameterTypes);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static @Nullable Identifier resolveStillTexture(IClientFluidTypeExtensions clientFluid, FluidStack fluidStack) {
        Object resolved = invokeClientFluidMethod(clientFluid, FLUID_STACK_STILL_TEXTURE, fluidStack);
        if (resolved == null) {
            resolved = invokeClientFluidMethod(clientFluid, SIMPLE_STILL_TEXTURE);
        }
        return resolved instanceof Identifier identifier ? identifier : null;
    }

    private static int resolveTintColor(IClientFluidTypeExtensions clientFluid, FluidStack fluidStack) {
        Object resolved = invokeClientFluidMethod(clientFluid, FLUID_STACK_TINT_COLOR, fluidStack);
        if (!(resolved instanceof Integer)) {
            resolved = invokeClientFluidMethod(clientFluid, SIMPLE_TINT_COLOR);
        }
        return resolved instanceof Integer tint ? tint : 0xFFFFFFFF;
    }

    private static @Nullable TextureAtlasSprite resolveFluidSprite(Identifier texture) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return null;
        }
        try {
            Object atlas = minecraft.getModelManager()
                    .getClass()
                    .getMethod("getAtlas", TextureAtlas.LOCATION_BLOCKS.getClass())
                    .invoke(minecraft.getModelManager(), TextureAtlas.LOCATION_BLOCKS);
            Object sprite = atlas.getClass().getMethod("getSprite", texture.getClass()).invoke(atlas, texture);
            return sprite instanceof TextureAtlasSprite textureAtlasSprite ? textureAtlasSprite : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static @Nullable Object invokeClientFluidMethod(IClientFluidTypeExtensions clientFluid, @Nullable Method method, Object... args) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(clientFluid, args);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
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
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuTransferModePayload(next.ordinal()));
        return next;
    }

    public TransferDirectionMode toggleTransferDirectionMode() {
        TransferDirectionMode next = menu.getDankInventory().cycleTransferDirectionMode();
        ClientPacketDistributor.sendToServer(new DeepNullPayloads.MenuTransferDirectionPayload(next.ordinal()));
        return next;
    }

    @Override
    public void removed() {
        shortcutAnimations.clear();
        tankAnimations.clear();
        pendingTankActions.clear();
        cancelAllPendingActions();
        shortcutController.clearAll();
        super.removed();
    }

    private record PendingTankAction(
            NullStorageAction action,
            int sourceTank,
            int targetTank,
            TankContents sourceBefore,
            TankContents targetBefore
    ) {
    }

    private record TankVisualAnimation(
            NullStorageAction action,
            int sourceTank,
            int targetTank,
            TankContents sourceBefore,
            TankContents targetBefore,
            TankContents sourceAfter,
            TankContents targetAfter,
            long startMs,
            long durationMs
    ) {
        boolean active(long now) {
            return now - startMs < durationMs;
        }
    }

    private record TankContents(FluidStack fluid, StoredChemical chemical) {
        private static final TankContents EMPTY = new TankContents(FluidStack.EMPTY, StoredChemical.EMPTY);

        boolean isEmpty() {
            return fluid.isEmpty() && chemical.isEmpty();
        }

        long amount() {
            return !fluid.isEmpty() ? fluid.getAmount() : chemical.amount();
        }

        int tint() {
            if (!fluid.isEmpty()) {
                return ensureOpaque(resolveTintColor(IClientFluidTypeExtensions.of(fluid.getFluid()), fluid));
            }
            return ensureOpaque(chemical.tint());
        }

        boolean matches(TankContents other) {
            if (other == null || fluid.isEmpty() != other.fluid.isEmpty() || chemical.isEmpty() != other.chemical.isEmpty()) {
                return false;
            }
            boolean sameFluid = fluid.isEmpty() || (fluid.getAmount() == other.fluid.getAmount()
                    && FluidStack.isSameFluidSameComponents(fluid, other.fluid));
            boolean sameChemical = chemical.isEmpty() || (chemical.amount() == other.chemical.amount()
                    && chemical.chemicalId().equals(other.chemical.chemicalId())
                    && chemical.iconPath().equals(other.chemical.iconPath())
                    && chemical.tint() == other.chemical.tint()
                    && chemical.translationKey().equals(other.chemical.translationKey())
                    && chemical.gaseous() == other.chemical.gaseous());
            return sameFluid && sameChemical;
        }
    }

    private static int imageHeightFor(DeepNullMenu menu) {
        return 141 + Math.max(0, menu.getTier().rows() - 1) * 21;
    }

    private boolean isShiftDown() {
        return minecraft != null && minecraft.hasShiftDown();
    }

    private static boolean isWithin(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
