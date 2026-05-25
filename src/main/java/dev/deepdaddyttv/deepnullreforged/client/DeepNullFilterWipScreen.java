package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemCatalog;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemCatalogEntry;
import dev.deepdaddyttv.deepnullreforged.dumpnull.DumpNullItemCatalogView;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class DeepNullFilterWipScreen extends AbstractContainerScreen<DeepNullMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = DeepNullReforged.id("textures/gui/deepnull_filter.png");
    private static final int SLOT_SPACING = 21;
    private static final int FILTER_COLUMNS = 9;
    private static final int FILTER_TOP = 19;
    private static final int FILTER_LEFT = 9;

    private final Inventory playerInventory;
    private DumpNullItemCatalogView selectedView = DumpNullItemCatalogView.MOB_DROPS;
    private List<DumpNullItemCatalogEntry> previewEntries = List.of();

    public DeepNullFilterWipScreen(DeepNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.playerInventory = playerInventory;
        this.imageWidth = 202;
        this.imageHeight = 183;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = this.imageHeight - 103;
        this.titleLabelX = 7;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        refreshPreview();

        addRenderableWidget(Button.builder(Component.translatable("dn.back.desc"), button ->
                        Minecraft.getInstance().setScreen(new DeepNullFilterScreen(menu, playerInventory, title)))
                .bounds(leftPos, topPos - 20, 50, 18)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Apply"), button -> applySelectedView())
                .bounds(leftPos + 54, topPos - 20, 50, 18)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Clear"), button -> clearFilters())
                .bounds(leftPos + 108, topPos - 20, 50, 18)
                .build());

        int buttonY = topPos + 12;
        for (DumpNullItemCatalogView view : DumpNullItemCatalogView.all()) {
            addRenderableWidget(Button.builder(Component.literal(view.label()), button -> {
                        selectedView = view;
                        refreshPreview();
                    })
                    .bounds(leftPos + 8, buttonY, 70, 18)
                    .build());
            buttonY += 20;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND_TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        guiGraphics.fill(leftPos + 83, topPos + 18, leftPos + 194, topPos + 167, 0xC0141C27);
        guiGraphics.fill(leftPos + 8, topPos + 136, leftPos + 78, topPos + 167, 0xB01A2430);

        for (int slot = 0; slot < menu.getDankInventory().getFilterSlotCount(); slot++) {
            int x = leftPos + FILTER_LEFT + (slot % FILTER_COLUMNS) * SLOT_SPACING;
            int y = topPos + FILTER_TOP + (slot / FILTER_COLUMNS) * SLOT_SPACING;
            ItemStack filterStack = menu.getFilterStack(slot);
            if (!filterStack.isEmpty()) {
                guiGraphics.renderItem(filterStack, x, y);
                guiGraphics.fill(x, y, x + 16, y + 16, 0x55000000);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, Component.literal("Filter Upgrade WIP"), titleLabelX, titleLabelY, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, Component.literal("Dump catalog seeds stable ghost filters"), 84, 22, 0xFFD6DEE9, false);
        guiGraphics.drawString(font, Component.literal("Selected: " + selectedView.label()), 84, 36, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, Component.literal(selectedView.description()), 84, 50, 0xFF9EB0C5, false);
        guiGraphics.drawString(font, Component.literal("Preview"), 84, 66, 0xFFE8EDF5, false);
        guiGraphics.drawString(font, Component.literal("Mode stays on the main filter screen"), 10, 142, 0xFFB7C3D3, false);

        int lineY = 78;
        int shown = Math.min(previewEntries.size(), 7);
        for (int index = 0; index < shown; index++) {
            DumpNullItemCatalogEntry entry = previewEntries.get(index);
            guiGraphics.drawString(font, trim(entry.label(), 102), 84, lineY, 0xFFFFFFFF, false);
            lineY += 12;
        }
        if (previewEntries.size() > shown) {
            guiGraphics.drawString(font, Component.literal("+" + (previewEntries.size() - shown) + " more"), 84, lineY + 2, 0xFF9EB0C5, false);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void refreshPreview() {
        previewEntries = List.copyOf(DumpNullItemCatalog.build(selectedView, playerInventory));
    }

    private void applySelectedView() {
        List<ItemStack> filterStacks = buildFilterStacks();
        for (int slot = 0; slot < menu.getDankInventory().getFilterSlotCount(); slot++) {
            ItemStack stack = slot < filterStacks.size() ? filterStacks.get(slot) : ItemStack.EMPTY;
            menu.setFilterStack(slot, stack);
            PacketDistributor.sendToServer(new DeepNullPayloads.MenuFilterSlotPayload(slot, stack));
        }
        Minecraft.getInstance().setScreen(new DeepNullFilterScreen(menu, playerInventory, title));
    }

    private void clearFilters() {
        for (int slot = 0; slot < menu.getDankInventory().getFilterSlotCount(); slot++) {
            menu.setFilterStack(slot, ItemStack.EMPTY);
            PacketDistributor.sendToServer(new DeepNullPayloads.MenuFilterSlotPayload(slot, ItemStack.EMPTY));
        }
    }

    private List<ItemStack> buildFilterStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (DumpNullItemCatalogEntry entry : DumpNullItemCatalog.build(selectedView, playerInventory)) {
            Item item = BuiltInRegistries.ITEM.get(entry.itemId());
            if (item == Items.AIR) {
                continue;
            }
            stacks.add(new ItemStack(item));
            if (stacks.size() >= menu.getDankInventory().getFilterSlotCount()) {
                break;
            }
        }
        return stacks;
    }

    private Component trim(String value, int maxWidth) {
        return Component.literal(font.plainSubstrByWidth(value, maxWidth));
    }
}
