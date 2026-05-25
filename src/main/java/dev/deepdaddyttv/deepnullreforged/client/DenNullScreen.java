package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.dennull.DenNullCaptureFilterMode;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullCaptureNormalizer;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullData;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullEntry;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullSpawnerEntry;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeData;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.menu.DenNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DenNullPayloads;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DenNullScreen extends AbstractContainerScreen<DenNullMenu> {
    private static final int GRID_COLUMNS = 9;
    private static final int CELL_SIZE = 24;
    private static final int CELL_CONTENT = 20;
    private final Map<String, Entity> entityPreviewCache = new HashMap<>();
    private EditBox tagTemplateBox;
    private EditBox captureFilterBox;
    private Button dyeButton;
    private Button babyButton;
    private Button captureButton;
    private Button captureModeButton;
    private int lastMouseX;
    private int lastMouseY;

    public DenNullScreen(DenNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        switch (menu.getViewMode()) {
            case GRID -> {
                this.imageWidth = 244;
                this.imageHeight = 54 + menu.getTier().rows() * CELL_SIZE;
                this.titleLabelX = 10;
                this.titleLabelY = 8;
                this.inventoryLabelY = this.imageHeight - 94;
            }
            case UPGRADES -> {
                this.imageWidth = 220;
                this.imageHeight = 160;
                this.titleLabelX = 10;
                this.titleLabelY = 8;
                this.inventoryLabelX = 14;
                this.inventoryLabelY = 62;
            }
            case SETTINGS -> {
                this.imageWidth = 230;
                this.imageHeight = 184;
                this.titleLabelX = 10;
                this.titleLabelY = 8;
                this.inventoryLabelX = 14;
                this.inventoryLabelY = 86;
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        addNavButton(DenNullMenu.ViewMode.GRID, leftPos, topPos - 20, 42, Component.translatable("dn.dennull.grid"));
        addNavButton(DenNullMenu.ViewMode.UPGRADES, leftPos + 46, topPos - 20, 62, Component.translatable("dn.upgrades.desc"));
        addNavButton(DenNullMenu.ViewMode.SETTINGS, leftPos + 112, topPos - 20, 62, Component.translatable("dn.dennull.settings"));
        if (menu.getViewMode() == DenNullMenu.ViewMode.SETTINGS) {
            initSettingsWidgets();
        }
    }

    private void addNavButton(DenNullMenu.ViewMode viewMode, int x, int y, int width, Component label) {
        Button button = Button.builder(label, ignored -> PacketDistributor.sendToServer(new DenNullPayloads.OpenViewPayload(viewMode.ordinal())))
                .bounds(x, y, width, 18)
                .build();
        button.active = menu.getViewMode() != viewMode;
        addRenderableWidget(button);
    }

    private void initSettingsWidgets() {
        DenNullUpgradeData upgrades = menu.getData().upgrades();
        dyeButton = Button.builder(dyeLabel(), ignored -> {
                    int next = upgrades().dyeColorId() + 1;
                    if (next >= DyeColor.values().length) {
                        next = -1;
                    }
                    PacketDistributor.sendToServer(new DenNullPayloads.SetDyePayload(next));
                })
                .bounds(leftPos + 66, topPos + 30, 68, 18)
                .build();
        addRenderableWidget(dyeButton);

        babyButton = Button.builder(babyLabel(), ignored ->
                        PacketDistributor.sendToServer(new DenNullPayloads.ToggleBabyPayload(!upgrades().babyEnabled())))
                .bounds(leftPos + 138, topPos + 30, 76, 18)
                .build();
        addRenderableWidget(babyButton);

        tagTemplateBox = new EditBox(font, leftPos + 66, topPos + 52, 148, 18, Component.translatable("dn.dennull.tag_template"));
        tagTemplateBox.setMaxLength(128);
        tagTemplateBox.setValue(upgrades.tagTemplate());
        addRenderableWidget(tagTemplateBox);
        addRenderableWidget(Button.builder(Component.translatable("gui.deepnullreforged.apply"), ignored ->
                        PacketDistributor.sendToServer(new DenNullPayloads.SetTagTemplatePayload(tagTemplateBox.getValue())))
                .bounds(leftPos + 66, topPos + 72, 52, 18)
                .build());

        captureButton = Button.builder(captureLabel(), ignored ->
                        PacketDistributor.sendToServer(new DenNullPayloads.ToggleCapturePayload(!upgrades().captureEnabled())))
                .bounds(leftPos + 122, topPos + 72, 92, 18)
                .build();
        addRenderableWidget(captureButton);

        captureModeButton = Button.builder(captureModeLabel(), ignored ->
                        PacketDistributor.sendToServer(new DenNullPayloads.SetCaptureFilterModePayload(upgrades().captureFilterMode().cycle().ordinal())))
                .bounds(leftPos + 66, topPos + 92, 72, 18)
                .build();
        addRenderableWidget(captureModeButton);

        captureFilterBox = new EditBox(font, leftPos + 142, topPos + 92, 72, 18, Component.translatable("dn.dennull.capture_filter"));
        captureFilterBox.setMaxLength(128);
        addRenderableWidget(captureFilterBox);
        addRenderableWidget(Button.builder(Component.translatable("dn.dumpnull.add_mob"), ignored -> {
                    if (!captureFilterBox.getValue().isBlank()) {
                        PacketDistributor.sendToServer(new DenNullPayloads.AddCaptureFilterPayload(captureFilterBox.getValue().trim()));
                        captureFilterBox.setValue("");
                    }
                })
                .bounds(leftPos + 66, topPos + 112, 52, 18)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("dn.dumpnull.clear_mobs"), ignored ->
                        PacketDistributor.sendToServer(DenNullPayloads.ClearCaptureFilterPayload.INSTANCE))
                .bounds(leftPos + 122, topPos + 112, 52, 18)
                .build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (menu.getViewMode() == DenNullMenu.ViewMode.SETTINGS) {
            if (dyeButton != null) {
                dyeButton.setMessage(dyeLabel());
            }
            if (babyButton != null) {
                babyButton.setMessage(babyLabel());
            }
            if (captureButton != null) {
                captureButton.setMessage(captureLabel());
            }
            if (captureModeButton != null) {
                captureModeButton.setMessage(captureModeLabel());
            }
            if (tagTemplateBox != null && !tagTemplateBox.isFocused() && !tagTemplateBox.getValue().equals(upgrades().tagTemplate())) {
                tagTemplateBox.setValue(upgrades().tagTemplate());
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF0181D24);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF6A7485);
        if (menu.getViewMode() == DenNullMenu.ViewMode.GRID) {
            renderGrid(graphics);
        } else {
            renderMenuSlotFrames(graphics);
        }
    }

    private void renderMenuSlotFrames(GuiGraphics graphics) {
        for (Slot slot : menu.slots) {
            renderSlotFrame(graphics, leftPos + slot.x - 1, topPos + slot.y - 1, isLockedSlot(slot));
            if (slot instanceof DenNullMenu.DenUpgradeSlot upgradeSlot && !slot.hasItem()) {
                renderUpgradePlaceholder(graphics, upgradeSlot.getType(), leftPos + slot.x, topPos + slot.y);
            }
        }
    }

    private void renderSlotFrame(GuiGraphics graphics, int x, int y, boolean locked) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF0B0F15);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, locked ? 0xFF1F2024 : 0xFF151A22);
        graphics.renderOutline(x, y, 18, 18, locked ? 0xFF7A5F5F : 0xFF465366);
        if (locked) {
            graphics.fill(x + 2, y + 2, x + 16, y + 16, 0x55331111);
        }
    }

    private void renderUpgradePlaceholder(GuiGraphics graphics, DenNullUpgradeType type, int x, int y) {
        String marker = type.name().substring(0, 1);
        graphics.drawCenteredString(font, marker, x + 8, y + 4, 0xFF65758A);
    }

    private boolean isLockedSlot(Slot slot) {
        return minecraft != null && minecraft.player != null && !slot.mayPickup(minecraft.player);
    }

    private void renderGrid(GuiGraphics graphics) {
        int gridX = gridX();
        int gridY = gridY();
        for (int slot = 0; slot < menu.getTier().slotCount(); slot++) {
            int x = gridX + (slot % GRID_COLUMNS) * CELL_SIZE;
            int y = gridY + (slot / GRID_COLUMNS) * CELL_SIZE;
            graphics.fill(x, y, x + CELL_CONTENT, y + CELL_CONTENT, 0xFF10151C);
            graphics.renderOutline(x, y, CELL_CONTENT, CELL_CONTENT, slot == menu.getData().selectedIndex() ? 0xFFE8F3FF : 0xFF405062);
            if (slot < activeEntryCount()) {
                if (!menu.getData().spawners().isEmpty()) {
                    renderSpawnerEntry(graphics, menu.getData().spawners().get(slot), x, y);
                    continue;
                }
                renderEntry(graphics, menu.getData().entries().get(slot), x, y);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFFFFF, false);
        if (menu.getViewMode() == DenNullMenu.ViewMode.GRID) {
            graphics.drawString(font, Component.translatable("item.deepnullreforged.den_null.entries",
                    activeEntryCount(), menu.getTier().slotCount()), titleLabelX, titleLabelY + 12, 0xFFB8C4D6, false);
        } else if (menu.getViewMode() == DenNullMenu.ViewMode.UPGRADES) {
            graphics.drawString(font, Component.translatable("dn.dennull.upgrades_hint"), titleLabelX, titleLabelY + 14, 0xFFB8C4D6, false);
            graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFB8C4D6, false);
        } else {
            graphics.drawString(font, Component.translatable("dn.dennull.milk"), titleLabelX, 26, 0xFFB8C4D6, false);
            graphics.drawString(font, Component.translatable("dn.dennull.tags"), titleLabelX + 24, 26, 0xFFB8C4D6, false);
            graphics.drawString(font, Component.translatable("dn.dennull.breeding_buffer"), titleLabelX, 52, 0xFFB8C4D6, false);
            graphics.drawString(font, Component.translatable("dn.dennull.capture_filters", upgrades().captureFilter().size()), titleLabelX, 124, 0xFF8EA1B8, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderEntryTooltip(graphics, mouseX, mouseY);
        renderUpgradeTooltip(graphics, mouseX, mouseY);
        renderSettingsTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.getViewMode() == DenNullMenu.ViewMode.GRID && ClientModEvents.isPrimaryGuiButton(button)) {
            int slot = slotAt(mouseX, mouseY);
            if (slot >= 0 && slot < activeEntryCount()) {
                if (minecraft != null && minecraft.player != null) {
                    menu.setSelectedIndex(minecraft.player, slot);
                }
                PacketDistributor.sendToServer(new DenNullPayloads.SetSelectedPayload(menu.containerId, slot));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (menu.getViewMode() == DenNullMenu.ViewMode.GRID
                && minecraft != null
                && minecraft.options.keyDrop.matches(keyCode, scanCode)) {
            int slot = slotAt(lastMouseX, lastMouseY);
            if (slot >= 0 && slot < activeEntryCount()) {
                PacketDistributor.sendToServer(new DenNullPayloads.ReleaseEntryPayload(menu.containerId, slot));
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderEntry(GuiGraphics graphics, DenNullEntry entry, int x, int y) {
        Entity entity = previewEntity(entry);
        if (entity instanceof LivingEntity livingEntity) {
            float entityHeight = Math.max(0.6F, livingEntity.getBbHeight());
            float entityWidth = Math.max(0.6F, livingEntity.getBbWidth());
            int scale = Math.max(13, Math.min(24, Math.round(18.0F / Math.max(entityHeight, entityWidth * 1.15F))));
            graphics.enableScissor(x + 1, y + 1, x + CELL_CONTENT - 1, y + CELL_CONTENT - 1);
            InventoryScreen.renderEntityInInventoryFollowsAngle(graphics, x + 1, y + 1, x + CELL_CONTENT - 1, y + CELL_CONTENT - 1, scale, 0.15F, 0.0F, 0.0F, livingEntity);
            graphics.disableScissor();
        } else {
            graphics.drawCenteredString(font, "?", x + CELL_CONTENT / 2, y + 5, 0xFFE8EDF5);
        }
        String count = compactCount(entry.count());
        graphics.drawString(font, count, x + CELL_CONTENT - font.width(count), y + CELL_CONTENT - 8, 0xFFFFFFFF, true);
    }

    private void renderSpawnerEntry(GuiGraphics graphics, DenNullSpawnerEntry entry, int x, int y) {
        graphics.renderItem(new ItemStack(Items.SPAWNER), x + 2, y + 2);
        String count = compactCount(entry.count());
        graphics.drawString(font, count, x + CELL_CONTENT - font.width(count), y + CELL_CONTENT - 8, 0xFFFFFFFF, true);
    }

    private String compactCount(int count) {
        if (count > 999) {
            return "999+";
        }
        return Integer.toString(count);
    }

    private void renderEntryTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.getViewMode() != DenNullMenu.ViewMode.GRID) {
            return;
        }
        int slot = slotAt(mouseX, mouseY);
        if (slot < 0 || slot >= activeEntryCount()) {
            return;
        }
        if (!menu.getData().spawners().isEmpty()) {
            DenNullSpawnerEntry entry = menu.getData().spawners().get(slot);
            graphics.renderTooltip(font, List.of(
                    Component.literal(entry.displayName()).withStyle(ChatFormatting.WHITE),
                    Component.literal(entry.entityType().toString()).withStyle(ChatFormatting.GRAY),
                    Component.translatable("item.deepnullreforged.den_null.count", entry.count()).withStyle(ChatFormatting.GRAY),
                    Component.literal(entry.summary()).withStyle(ChatFormatting.DARK_GRAY)
            ), Optional.empty(), mouseX, mouseY);
            return;
        }
        DenNullEntry entry = menu.getData().entries().get(slot);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(entry.displayName()).withStyle(ChatFormatting.WHITE));
        lines.add(Component.literal(entry.entityType().toString()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("item.deepnullreforged.den_null.count", entry.count()).withStyle(ChatFormatting.GRAY));
        if (!entry.summary().isBlank()) {
            lines.add(Component.literal(entry.summary()).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (Screen.hasShiftDown()) {
            for (String detail : DenNullCaptureNormalizer.detailLines(entry)) {
                lines.add(Component.literal(detail).withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            lines.add(Component.translatable("item.deepnullreforged.den_null.shift_details").withStyle(ChatFormatting.DARK_GRAY));
        }
        if (!(previewEntity(entry) instanceof LivingEntity)) {
            lines.add(Component.translatable("item.deepnullreforged.den_null.preview_unavailable").withStyle(ChatFormatting.RED));
        }
        graphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
    }

    private void renderUpgradeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.getViewMode() != DenNullMenu.ViewMode.UPGRADES) {
            return;
        }
        Slot hovered = getSlotUnderMouse();
        if (!(hovered instanceof DenNullMenu.DenUpgradeSlot upgradeSlot) || hovered.hasItem()) {
            return;
        }
        DenNullUpgradeType type = upgradeSlot.getType();
        graphics.renderTooltip(font, List.of(
                Component.translatable("item.deepnullreforged." + type.itemId()),
                Component.translatable("upgrade." + type.itemId() + ".desc").withStyle(ChatFormatting.GRAY)
        ), Optional.empty(), mouseX, mouseY);
    }

    private void renderSettingsTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.getViewMode() != DenNullMenu.ViewMode.SETTINGS) {
            return;
        }
        if (tagTemplateBox != null && isWithin(mouseX, mouseY, tagTemplateBox.getX(), tagTemplateBox.getY(), tagTemplateBox.getWidth(), tagTemplateBox.getHeight())) {
            graphics.renderTooltip(font, List.of(
                    Component.translatable("dn.dennull.tag_template"),
                    Component.literal("{player}, {type}, {id}, {counter}").withStyle(ChatFormatting.GRAY)
            ), Optional.empty(), mouseX, mouseY);
        }
    }

    private Entity previewEntity(DenNullEntry entry) {
        return DenNullEntityPreview.get(minecraft, entry, entityPreviewCache);
    }

    private String previewKey(DenNullEntry entry) {
        return DenNullEntityPreview.previewKey(entry);
    }

    public void acceptState(int containerId, DenNullData data) {
        if (containerId != menu.containerId) {
            return;
        }
        menu.updateData(data);
        entityPreviewCache.keySet().removeIf(key -> data.entries().stream().noneMatch(entry -> previewKey(entry).equals(key)));
    }

    private int activeEntryCount() {
        return menu.getData().spawners().isEmpty() ? menu.getData().entries().size() : menu.getData().spawners().size();
    }

    private DenNullUpgradeData upgrades() {
        return menu.getData().upgrades();
    }

    private Component dyeLabel() {
        if (upgrades().dyeColorId() < 0) {
            return Component.translatable("dn.dennull.dye.none");
        }
        return Component.translatable("dn.dennull.dye", DyeColor.byId(upgrades().dyeColorId()).getName());
    }

    private Component babyLabel() {
        return Component.translatable(upgrades().babyEnabled() ? "dn.dennull.baby.on" : "dn.dennull.baby.off");
    }

    private Component captureLabel() {
        return Component.translatable(upgrades().captureEnabled() ? "dn.dennull.capture.on" : "dn.dennull.capture.off");
    }

    private Component captureModeLabel() {
        DenNullCaptureFilterMode mode = upgrades().captureFilterMode();
        return Component.translatable("dn.dennull.capture_mode." + mode.name().toLowerCase());
    }

    private int slotAt(double mouseX, double mouseY) {
        int relativeX = (int) mouseX - gridX();
        int relativeY = (int) mouseY - gridY();
        if (relativeX < 0 || relativeY < 0) {
            return -1;
        }
        int column = relativeX / CELL_SIZE;
        int row = relativeY / CELL_SIZE;
        if (column < 0 || column >= GRID_COLUMNS || row < 0 || row >= menu.getTier().rows()) {
            return -1;
        }
        int innerX = relativeX % CELL_SIZE;
        int innerY = relativeY % CELL_SIZE;
        if (innerX >= CELL_CONTENT || innerY >= CELL_CONTENT) {
            return -1;
        }
        return row * GRID_COLUMNS + column;
    }

    private int gridX() {
        return leftPos + 14;
    }

    private int gridY() {
        return topPos + 34;
    }

    private static boolean isWithin(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
