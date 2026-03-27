package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.StyleGlassVariant;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu;
import dev.deepdaddyttv.deepnullreforged.network.NullWorkbenchPayloads;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

public class NullWorkbenchScreen extends AbstractContainerScreen<NullWorkbenchMenu> {
    private static final Identifier CRAFT_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_crafting_gui.png");
    private static final Identifier SYNC_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_sync_gui.png");
    private static final Identifier STYLE_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_style_gui.png");

    private static final int GUI_WIDTH = 252;
    private static final int GUI_HEIGHT = 246;
    private static final int SLOT_SIZE = 16;
    private static final int NULL_SLOT_X = 24;
    private static final int NULL_SLOT_Y = 64;
    private static final int SYNC_SLOT_X = 64;
    private static final int SYNC_SLOT_Y = 64;
    private static final int PREVIEW_LEFT_X = 167;
    private static final int PREVIEW_LEFT_Y = 64;
    private static final int PREVIEW_RIGHT_X = 207;
    private static final int PREVIEW_RIGHT_Y = 64;
    private static final int STYLE_NULL_SLOT_X = 44;
    private static final int STYLE_NULL_SLOT_Y = 33;
    private static final int STYLE_MODIFIER_SLOT_X = 84;
    private static final int STYLE_MODIFIER_SLOT_Y = 33;
    private static final int STYLE_OUTPUT_PREVIEW_X = 187;
    private static final int STYLE_OUTPUT_PREVIEW_Y = 33;
    private static final int CRAFT_PROGRESS_X = 61;
    private static final int CRAFT_PROGRESS_Y = 93;
    private static final int CRAFT_PROGRESS_WIDTH = 133;
    private static final int CRAFT_PROGRESS_HEIGHT = 26;
    private static final int SYNC_PROGRESS_X = 103;
    private static final int SYNC_PROGRESS_Y = 60;
    private static final int SYNC_PROGRESS_WIDTH = 46;
    private static final int SYNC_PROGRESS_HEIGHT = 23;
    private static final int STYLE_FRAME_BOX_X = 141;
    private static final int STYLE_FRAME_BOX_Y = 76;
    private static final int STYLE_GLASS_BOX_X = 141;
    private static final int STYLE_GLASS_BOX_Y = 104;
    private static final int STYLE_BOX_WIDTH = 42;
    private static final int STYLE_BOX_HEIGHT = 12;

    private WorkbenchTab activeTab = WorkbenchTab.CRAFT;
    private Button craftTabButton;
    private Button syncTabButton;
    private Button styleTabButton;
    private Button backupButton;
    private Button restoreButton;
    private Button applyStyleButton;
    private Button resetStyleButton;
    private EditBox frameColorBox;
    private EditBox glassColorBox;
    private ItemStack styleSourceSnapshot = ItemStack.EMPTY;

    public NullWorkbenchScreen(NullWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, GUI_WIDTH, GUI_HEIGHT);
        this.inventoryLabelX = 32;
        this.inventoryLabelY = 140;
        this.titleLabelX = 16;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        craftTabButton = addRenderableWidget(Button.builder(Component.literal("Craft"), button -> setActiveTab(WorkbenchTab.CRAFT))
                .bounds(leftPos, topPos - 20, 50, 18)
                .build());
        syncTabButton = addRenderableWidget(Button.builder(Component.literal("Sync"), button -> setActiveTab(WorkbenchTab.SYNC))
                .bounds(leftPos + 54, topPos - 20, 50, 18)
                .build());
        styleTabButton = addRenderableWidget(Button.builder(Component.literal("Style"), button -> setActiveTab(WorkbenchTab.STYLE))
                .bounds(leftPos + 108, topPos - 20, 50, 18)
                .build());

        backupButton = addRenderableWidget(Button.builder(Component.translatable("container.deepnullreforged.null_workbench.backup"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, NullWorkbenchMenu.BUTTON_BACKUP);
            }
        }).bounds(leftPos + 34, topPos + 105, 64, 20).build());

        restoreButton = addRenderableWidget(Button.builder(Component.translatable("container.deepnullreforged.null_workbench.restore"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, NullWorkbenchMenu.BUTTON_RESTORE);
            }
        }).bounds(leftPos + 108, topPos + 105, 64, 20).build());

        frameColorBox = addRenderableWidget(new EditBox(font, leftPos + STYLE_FRAME_BOX_X + 1, topPos + STYLE_FRAME_BOX_Y + 1, STYLE_BOX_WIDTH, STYLE_BOX_HEIGHT, Component.literal("Frame")));
        frameColorBox.setMaxLength(7);
        frameColorBox.setFilter(value -> value.isEmpty() || value.matches("#?[0-9a-fA-F]{0,6}"));
        frameColorBox.setBordered(false);
        frameColorBox.setTextColor(0xFFFFFFFF);
        frameColorBox.setTextColorUneditable(0xFFFFFFFF);

        glassColorBox = addRenderableWidget(new EditBox(font, leftPos + STYLE_GLASS_BOX_X + 1, topPos + STYLE_GLASS_BOX_Y + 1, STYLE_BOX_WIDTH, STYLE_BOX_HEIGHT, Component.literal("Glass")));
        glassColorBox.setMaxLength(7);
        glassColorBox.setFilter(value -> value.isEmpty() || value.matches("#?[0-9a-fA-F]{0,6}"));
        glassColorBox.setBordered(false);
        glassColorBox.setTextColor(0xFFFFFFFF);
        glassColorBox.setTextColorUneditable(0xFFFFFFFF);

        applyStyleButton = addRenderableWidget(Button.builder(Component.literal("Apply"), button -> applyStyle(false))
                .bounds(leftPos + 142, topPos + 128, 35, 16)
                .build());
        resetStyleButton = addRenderableWidget(Button.builder(Component.literal("Reset"), button -> applyStyle(true))
                .bounds(leftPos + 181, topPos + 128, 40, 16)
                .build());

        refreshStyleFields();
        updateWidgetVisibility();
        updateMachineSlotState();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (activeTab == WorkbenchTab.STYLE) {
            ItemStack current = menu.getNullStack();
            if (!ItemStack.isSameItemSameComponents(current, styleSourceSnapshot) || current.getCount() != styleSourceSnapshot.getCount()) {
                refreshStyleFields();
            }
        }
        updateWidgetVisibility();
        updateMachineSlotState();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Identifier background = switch (activeTab) {
            case CRAFT -> CRAFT_TEXTURE;
            case SYNC -> SYNC_TEXTURE;
            case STYLE -> STYLE_TEXTURE;
        };
        graphics.blit(RenderPipelines.GUI_TEXTURED, background, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);

        if (activeTab == WorkbenchTab.CRAFT) {
            renderProgressBar(
                    graphics,
                    leftPos + CRAFT_PROGRESS_X,
                    topPos + CRAFT_PROGRESS_Y,
                    CRAFT_PROGRESS_WIDTH,
                    CRAFT_PROGRESS_HEIGHT,
                    menu.getCraftProgress(),
                    menu.getCraftDuration(),
                    0xFF7CA9FF
            );
        } else if (activeTab == WorkbenchTab.SYNC) {
            renderProgressBar(
                    graphics,
                    leftPos + SYNC_PROGRESS_X,
                    topPos + SYNC_PROGRESS_Y,
                    SYNC_PROGRESS_WIDTH,
                    SYNC_PROGRESS_HEIGHT,
                    menu.getSyncProgress(),
                    menu.getSyncDuration(),
                    0xFF78D3B0
            );
            renderCustomSlot(graphics, menu.getNullStack(), leftPos + NULL_SLOT_X, topPos + NULL_SLOT_Y, isHoveringCustom(mouseX, mouseY, NULL_SLOT_X, NULL_SLOT_Y));
            renderCustomSlot(graphics, menu.getSynchronizerStack(), leftPos + SYNC_SLOT_X, topPos + SYNC_SLOT_Y, isHoveringCustom(mouseX, mouseY, SYNC_SLOT_X, SYNC_SLOT_Y));
            renderCustomSlot(graphics, menu.getSyncNullOutputStack(), leftPos + PREVIEW_LEFT_X, topPos + PREVIEW_LEFT_Y, isHoveringCustom(mouseX, mouseY, PREVIEW_LEFT_X, PREVIEW_LEFT_Y));
            renderCustomSlot(graphics, menu.getSyncSynchronizerOutputStack(), leftPos + PREVIEW_RIGHT_X, topPos + PREVIEW_RIGHT_Y, isHoveringCustom(mouseX, mouseY, PREVIEW_RIGHT_X, PREVIEW_RIGHT_Y));
        } else {
            renderCustomSlot(graphics, menu.getNullStack(), leftPos + STYLE_NULL_SLOT_X, topPos + STYLE_NULL_SLOT_Y, isHoveringCustom(mouseX, mouseY, STYLE_NULL_SLOT_X, STYLE_NULL_SLOT_Y));
            renderCustomSlot(graphics, menu.getStyleModifierStack(), leftPos + STYLE_MODIFIER_SLOT_X, topPos + STYLE_MODIFIER_SLOT_Y, isHoveringCustom(mouseX, mouseY, STYLE_MODIFIER_SLOT_X, STYLE_MODIFIER_SLOT_Y));

            ItemStack output = menu.getOutputStack();
            ItemStack preview = output.isEmpty() ? previewStack() : output;
            renderCustomSlot(graphics, preview, leftPos + STYLE_OUTPUT_PREVIEW_X, topPos + STYLE_OUTPUT_PREVIEW_Y, isHoveringCustom(mouseX, mouseY, STYLE_OUTPUT_PREVIEW_X, STYLE_OUTPUT_PREVIEW_Y));
        }

        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0xFFFFFFFF, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFFFFFFF, false);

        if (activeTab == WorkbenchTab.SYNC && menu.isSyncing()) {
            graphics.text(font, Component.literal("Processing"), 34, 32, 0xFFE8EDF5, false);
        }
        if (activeTab == WorkbenchTab.STYLE) {
            graphics.text(font, Component.literal("Frame"), STYLE_FRAME_BOX_X, STYLE_FRAME_BOX_Y - 10, 0xFFE8EDF5, false);
            graphics.text(font, Component.literal("Glass"), STYLE_GLASS_BOX_X, STYLE_GLASS_BOX_Y - 10, 0xFFE8EDF5, false);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        renderCustomTooltips(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }

        if (activeTab == WorkbenchTab.SYNC) {
            return clickCustomSlot(event, NULL_SLOT_X, NULL_SLOT_Y, NullWorkbenchBlockEntity.NULL_SLOT)
                    || clickCustomSlot(event, SYNC_SLOT_X, SYNC_SLOT_Y, NullWorkbenchBlockEntity.SYNCHRONIZER_SLOT)
                    || clickCustomSlot(event, PREVIEW_LEFT_X, PREVIEW_LEFT_Y, NullWorkbenchBlockEntity.SYNC_NULL_OUTPUT_SLOT)
                    || clickCustomSlot(event, PREVIEW_RIGHT_X, PREVIEW_RIGHT_Y, NullWorkbenchBlockEntity.SYNC_SYNCHRONIZER_OUTPUT_SLOT);
        }

        if (activeTab == WorkbenchTab.STYLE) {
            return clickCustomSlot(event, STYLE_NULL_SLOT_X, STYLE_NULL_SLOT_Y, NullWorkbenchBlockEntity.NULL_SLOT)
                    || clickCustomSlot(event, STYLE_MODIFIER_SLOT_X, STYLE_MODIFIER_SLOT_Y, NullWorkbenchBlockEntity.STYLE_MODIFIER_SLOT)
                    || clickCustomSlot(event, STYLE_OUTPUT_PREVIEW_X, STYLE_OUTPUT_PREVIEW_Y, NullWorkbenchBlockEntity.OUTPUT_SLOT);
        }

        return false;
    }

    private void setActiveTab(WorkbenchTab tab) {
        if (activeTab == tab) {
            return;
        }
        activeTab = tab;
        if (activeTab == WorkbenchTab.STYLE) {
            refreshStyleFields();
        }
        updateWidgetVisibility();
        updateMachineSlotState();
    }

    private void updateWidgetVisibility() {
        boolean sync = activeTab == WorkbenchTab.SYNC;
        boolean style = activeTab == WorkbenchTab.STYLE;
        boolean hasStyledNull = style && hasStyledNull();

        craftTabButton.active = activeTab != WorkbenchTab.CRAFT;
        syncTabButton.active = activeTab != WorkbenchTab.SYNC;
        styleTabButton.active = activeTab != WorkbenchTab.STYLE;

        backupButton.visible = sync;
        backupButton.active = sync && menu.canBackup();
        restoreButton.visible = sync;
        restoreButton.active = sync && menu.canRestore();

        frameColorBox.visible = hasStyledNull;
        frameColorBox.setEditable(hasStyledNull);
        glassColorBox.visible = hasStyledNull;
        glassColorBox.setEditable(hasStyledNull);
        applyStyleButton.visible = hasStyledNull;
        applyStyleButton.active = hasStyledNull;
        resetStyleButton.visible = hasStyledNull;
        resetStyleButton.active = hasStyledNull;
    }

    private void updateMachineSlotState() {
        menu.setCraftSlotsActive(activeTab == WorkbenchTab.CRAFT);
        menu.setOutputSlotActive(activeTab == WorkbenchTab.CRAFT);
    }

    private void refreshStyleFields() {
        ItemStack stack = menu.getNullStack();
        styleSourceSnapshot = stack.copy();
        if (!(stack.getItem() instanceof DeepNullItem deepNullItem) || minecraft == null || minecraft.level == null) {
            frameColorBox.setValue("#FFFFFF");
            glassColorBox.setValue("#FFFFFF");
            return;
        }
        DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), stack.copy(), minecraft.level.registryAccess(), null);
        frameColorBox.setValue(formatHex(inventory.getFrameColor()));
        glassColorBox.setValue(formatHex(inventory.getGlassColor()));
    }

    private void applyStyle(boolean reset) {
        if (!menu.hasWorkbench()) {
            return;
        }
        ClientPacketDistributor.sendToServer(new NullWorkbenchPayloads.ApplyStylePayload(
                menu.getBlockPos(),
                parseHex(frameColorBox.getValue(), 0xFFFFFF),
                parseHex(glassColorBox.getValue(), 0xFFFFFF),
                reset
        ));
    }

    private ItemStack previewStack() {
        ItemStack stack = menu.getNullStack();
        if (!(stack.getItem() instanceof DeepNullItem deepNullItem) || minecraft == null || minecraft.level == null) {
            return stack;
        }
        ItemStack preview = stack.copy();
        DeepNullInventory inventory = new DeepNullInventory(deepNullItem.tier(), preview, minecraft.level.registryAccess(), null);
        StyleGlassVariant variant = menu.getStyleModifierStack().isEmpty()
                ? DeepNullInventory.getStyleVariant(preview)
                : StyleGlassVariant.fromModifier(preview, menu.getStyleModifierStack());
        inventory.setStyle(
                parseHex(frameColorBox.getValue(), inventory.getFrameColor()),
                parseHex(glassColorBox.getValue(), inventory.getGlassColor()),
                variant
        );
        return preview;
    }

    private void renderProgressBar(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int progress, int maxProgress, int fillColor) {
        if (maxProgress <= 0) {
            return;
        }
        graphics.outline(x - 1, y - 1, width + 2, height + 2, 0xFF4A5568);
        graphics.fill(x, y, x + width, y + height, 0x33121720);
        int filled = Math.max(0, Math.min(width, Math.round(width * (progress / (float) maxProgress))));
        if (filled > 0) {
            graphics.fill(x, y, x + filled, y + height, fillColor);
        }
    }

    private void renderCustomSlot(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, boolean hovered) {
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, hovered ? 0x33FFFFFF : 0x22000000);
        graphics.outline(x - 1, y - 1, SLOT_SIZE + 2, SLOT_SIZE + 2, hovered ? 0xFFFFFFFF : 0xFF8C8C8C);
        if (!stack.isEmpty()) {
            graphics.item(stack, x, y);
            graphics.itemDecorations(font, stack, x, y);
        }
    }

    private void renderCustomTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (minecraft == null) {
            return;
        }

        if (activeTab == WorkbenchTab.SYNC) {
            renderItemTooltip(graphics, mouseX, mouseY, NULL_SLOT_X, NULL_SLOT_Y, menu.getNullStack());
            renderItemTooltip(graphics, mouseX, mouseY, SYNC_SLOT_X, SYNC_SLOT_Y, menu.getSynchronizerStack());
            renderItemTooltip(graphics, mouseX, mouseY, PREVIEW_LEFT_X, PREVIEW_LEFT_Y, menu.getSyncNullOutputStack());
            renderItemTooltip(graphics, mouseX, mouseY, PREVIEW_RIGHT_X, PREVIEW_RIGHT_Y, menu.getSyncSynchronizerOutputStack());
        } else if (activeTab == WorkbenchTab.STYLE) {
            renderItemTooltip(graphics, mouseX, mouseY, STYLE_NULL_SLOT_X, STYLE_NULL_SLOT_Y, menu.getNullStack());
            renderItemTooltip(graphics, mouseX, mouseY, STYLE_MODIFIER_SLOT_X, STYLE_MODIFIER_SLOT_Y, menu.getStyleModifierStack());
            ItemStack output = menu.getOutputStack();
            renderItemTooltip(graphics, mouseX, mouseY, STYLE_OUTPUT_PREVIEW_X, STYLE_OUTPUT_PREVIEW_Y, output.isEmpty() ? previewStack() : output);
        }
    }

    private void renderItemTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int localX, int localY, ItemStack stack) {
        if (!stack.isEmpty() && isHoveringCustom(mouseX, mouseY, localX, localY)) {
            graphics.setTooltipForNextFrame(font, getTooltipFromItem(minecraft, stack), stack.getTooltipImage(), stack, mouseX, mouseY);
        }
    }

    private boolean clickCustomSlot(MouseButtonEvent event, int localX, int localY, int slotIndex) {
        if (!isHoveringCustom(event.x(), event.y(), localX, localY)) {
            return false;
        }
        if (event.button() != 0 && event.button() != 1) {
            return false;
        }
        if (slotIndex < 0 || slotIndex >= menu.slots.size()) {
            return false;
        }
        Slot slot = menu.slots.get(slotIndex);
        ContainerInput input = event.hasShiftDown() && event.button() == 0 ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP;
        slotClicked(slot, slot.index, event.button(), input);
        if (activeTab == WorkbenchTab.STYLE) {
            refreshStyleFields();
        }
        return true;
    }

    private boolean isHoveringCustom(double mouseX, double mouseY, int localX, int localY) {
        return insideAbsolute(mouseX, mouseY, leftPos + localX, topPos + localY, SLOT_SIZE, SLOT_SIZE);
    }

    private boolean hasStyledNull() {
        return !menu.getNullStack().isEmpty();
    }

    private static int parseHex(String value, int fallback) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (normalized.length() != 6) {
            return fallback;
        }
        try {
            return Integer.parseInt(normalized, 16) & 0xFFFFFF;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String formatHex(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }

    private static boolean insideAbsolute(double mouseX, double mouseY, int areaX, int areaY, int areaWidth, int areaHeight) {
        return mouseX >= areaX && mouseX < areaX + areaWidth && mouseY >= areaY && mouseY < areaY + areaHeight;
    }

    private enum WorkbenchTab {
        CRAFT,
        SYNC,
        STYLE
    }
}
