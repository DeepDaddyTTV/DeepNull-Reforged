package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.dripnull.DripNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripProfile;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripSlotAssignment;
import dev.deepdaddyttv.deepnullreforged.dripnull.DripSlotRef;
import dev.deepdaddyttv.deepnullreforged.menu.DripNullMenu;
import dev.deepdaddyttv.deepnullreforged.network.DripNullPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class DripNullScreen extends AbstractContainerScreen<DripNullMenu> {
    private final List<Button> profileButtons = new ArrayList<>();
    private Button swapButton;
    private Button clearButton;
    private Button mendButton;
    private ItemStack hoveredProfileStack = ItemStack.EMPTY;

    public DripNullScreen(DripNullMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 232;
        this.imageHeight = switch (menu.getViewMode()) {
            case PROFILES -> 176;
            case SETTINGS -> 136;
            case RECOVERY -> 236;
        };
        this.titleLabelX = 10;
        this.titleLabelY = 8;
        this.inventoryLabelX = 14;
        this.inventoryLabelY = menu.getViewMode() == DripNullMenu.ViewMode.RECOVERY ? 148 : 72;
    }

    @Override
    protected void init() {
        super.init();
        profileButtons.clear();
        addNavButton(DripNullMenu.ViewMode.PROFILES, leftPos, topPos - 20, 58, Component.translatable("dn.dripnull.profiles"));
        addNavButton(DripNullMenu.ViewMode.SETTINGS, leftPos + 62, topPos - 20, 58, Component.translatable("dn.dripnull.settings"));
        addNavButton(DripNullMenu.ViewMode.RECOVERY, leftPos + 124, topPos - 20, 62, Component.translatable("dn.dripnull.recovery"));
        switch (menu.getViewMode()) {
            case PROFILES -> initProfileWidgets();
            case SETTINGS -> initSettingsWidgets();
            case RECOVERY -> initRecoveryWidgets();
        }
    }

    private void addNavButton(DripNullMenu.ViewMode viewMode, int x, int y, int width, Component label) {
        Button button = Button.builder(label, ignored -> PacketDistributor.sendToServer(new DripNullPayloads.OpenViewPayload(viewMode.ordinal())))
                .bounds(x, y, width, 18)
                .build();
        button.active = menu.getViewMode() != viewMode;
        addRenderableWidget(button);
    }

    private void initProfileWidgets() {
        int x = leftPos + 12;
        int y = topPos + 30;
        for (int i = 0; i < menu.getData().profiles().size(); i++) {
            int profile = i;
            Button button = Button.builder(profileLabel(i), ignored -> PacketDistributor.sendToServer(new DripNullPayloads.SetProfilePayload(profile)))
                    .bounds(x + (i % 6) * 34, y + (i / 6) * 18, 30, 16)
                    .build();
            button.active = i != menu.getData().selectedProfile();
            profileButtons.add(button);
            addRenderableWidget(button);
        }
        swapButton = Button.builder(profileActionLabel(),
                        ignored -> PacketDistributor.sendToServer(DripNullPayloads.RunSwapPayload.INSTANCE))
                .bounds(leftPos + 12, topPos + 150, 82, 20)
                .build();
        addRenderableWidget(swapButton);
        clearButton = Button.builder(Component.translatable("dn.dripnull.clear_profile"),
                        ignored -> PacketDistributor.sendToServer(DripNullPayloads.ClearProfilePayload.INSTANCE))
                .bounds(leftPos + 100, topPos + 150, 78, 20)
                .build();
        clearButton.active = !menu.selectedProfile().empty();
        addRenderableWidget(clearButton);
    }

    private void initSettingsWidgets() {
        addScopeButton("hotbar", leftPos + 12, topPos + 32);
        addScopeButton("inventory", leftPos + 88, topPos + 32);
        addScopeButton("armor", leftPos + 12, topPos + 58);
        addScopeButton("offhand", leftPos + 88, topPos + 58);
        addScopeButton("curios", leftPos + 164, topPos + 58);
    }

    private void initRecoveryWidgets() {
        mendButton = Button.builder(mendLabel(), ignored -> PacketDistributor.sendToServer(DripNullPayloads.InstallMendPayload.INSTANCE))
                .bounds(leftPos + 12, topPos + 32, 96, 20)
                .build();
        mendButton.active = !menu.getData().upgrades().has(DripNullUpgradeType.MEND);
        addRenderableWidget(mendButton);
        addRenderableWidget(Button.builder(Component.translatable("dn.dripnull.recover"), ignored ->
                        PacketDistributor.sendToServer(DripNullPayloads.RecoverPayload.INSTANCE))
                .bounds(leftPos + 112, topPos + 32, 96, 20)
                .build());
    }

    private void addScopeButton(String scope, int x, int y) {
        Button button = Button.builder(scopeLabel(scope), ignored -> PacketDistributor.sendToServer(new DripNullPayloads.ToggleScopePayload(scope)))
                .bounds(x, y, 70, 20)
                .build();
        addRenderableWidget(button);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        for (int i = 0; i < profileButtons.size(); i++) {
            Button button = profileButtons.get(i);
            button.setMessage(profileLabel(i));
            button.active = i != menu.getData().selectedProfile();
        }
        if (swapButton != null) {
            swapButton.setMessage(profileActionLabel());
        }
        if (clearButton != null) {
            clearButton.active = !menu.selectedProfile().empty();
        }
        if (mendButton != null) {
            mendButton.setMessage(mendLabel());
            mendButton.active = !menu.getData().upgrades().has(DripNullUpgradeType.MEND);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        hoveredProfileStack = ItemStack.EMPTY;
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF0181D24);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF6A7485);
        if (menu.getViewMode() == DripNullMenu.ViewMode.RECOVERY) {
            for (Slot slot : menu.slots) {
                renderSlotFrame(graphics, leftPos + slot.x - 1, topPos + slot.y - 1);
            }
        } else if (menu.getViewMode() == DripNullMenu.ViewMode.PROFILES) {
            renderProfilePreview(graphics, mouseX, mouseY);
        }
    }

    private void renderProfilePreview(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = leftPos + 12;
        int y = topPos + 56;
        int columns = 9;
        int cell = 18;
        DripProfile profile = menu.selectedProfile();
        for (int i = 0; i < 45; i++) {
            int cellX = x + (i % columns) * cell;
            int cellY = y + (i / columns) * cell;
            renderSlotFrame(graphics, cellX, cellY);
        }
        List<DripSlotAssignment> assignments = profile.slots();
        for (int i = 0; i < Math.min(assignments.size(), 45); i++) {
            DripSlotAssignment assignment = assignments.get(i);
            int cellX = x + (i % columns) * cell + 1;
            int cellY = y + (i / columns) * cell + 1;
            ItemStack stack = previewStack(assignment);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, cellX, cellY);
                if (inside(mouseX, mouseY, cellX, cellY, 16, 16)) {
                    hoveredProfileStack = stack;
                }
            } else {
                graphics.drawCenteredString(font, slotLabel(assignment.slot()), cellX + 8, cellY + 4, 0xFF65758A);
            }
        }
        if (assignments.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("dn.dripnull.profile_empty"), leftPos + imageWidth / 2, topPos + 104, 0xFF8B96A3);
        }
    }

    private void renderSlotFrame(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF0B0F15);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF151A22);
        graphics.renderOutline(x, y, 18, 18, 0xFF465366);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFFFFF, false);
        if (menu.getViewMode() == DripNullMenu.ViewMode.PROFILES) {
            graphics.drawString(font, Component.translatable("dn.dripnull.equipped", menu.getData().equippedProfile() + 1), titleLabelX, 20, 0xFFB8C4D6, false);
            graphics.drawString(font, Component.translatable("dn.dripnull.profile_preview", menu.selectedProfile().name()), titleLabelX, 48, 0xFFB8C4D6, false);
        } else if (menu.getViewMode() == DripNullMenu.ViewMode.SETTINGS) {
            DripProfile profile = menu.selectedProfile();
            graphics.drawString(font, Component.translatable("dn.dripnull.selected_profile", profile.name()), titleLabelX, 20, 0xFFB8C4D6, false);
        } else {
            graphics.drawString(font, Component.translatable("dn.dripnull.recovery_counts", menu.getData().looseItems().size(), idleVaultCount()), titleLabelX, 58, 0xFFB8C4D6, false);
            graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFB8C4D6, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (!hoveredProfileStack.isEmpty()) {
            graphics.renderTooltip(font, hoveredProfileStack, mouseX, mouseY);
        }
    }

    private Component profileLabel(int index) {
        String prefix = index == menu.getData().selectedProfile() ? "* " : "";
        return Component.literal(prefix + (index + 1));
    }

    private Component scopeLabel(String scope) {
        DripProfile profile = menu.selectedProfile();
        boolean enabled = switch (scope) {
            case "hotbar" -> profile.includeHotbar();
            case "inventory" -> profile.includeInventory();
            case "armor" -> profile.includeArmor();
            case "offhand" -> profile.includeOffhand();
            case "curios" -> profile.includeCurios();
            default -> false;
        };
        return Component.translatable("dn.dripnull.scope." + scope, enabled ? "On" : "Off");
    }

    private Component profileActionLabel() {
        if (menu.selectedProfile().empty()) {
            return Component.translatable("dn.dripnull.capture");
        }
        if (menu.getData().equippedProfile() == menu.getData().selectedProfile()) {
            return Component.translatable("dn.dripnull.stow");
        }
        return Component.translatable("dn.dripnull.swap");
    }

    private ItemStack previewStack(DripSlotAssignment assignment) {
        if (menu.getData().equippedProfile() == menu.getData().selectedProfile()) {
            ItemStack live = playerSlotStack(assignment.slot());
            if (!live.isEmpty()) {
                return live;
            }
        }
        ItemStack stored = menu.getData().vaultItems().get(assignment.ref());
        return stored == null ? ItemStack.EMPTY : stored;
    }

    private ItemStack playerSlotStack(DripSlotRef ref) {
        Player player = minecraft == null ? null : minecraft.player;
        if (player == null || ref == null) {
            return ItemStack.EMPTY;
        }
        return switch (ref.provider()) {
            case DripSlotRef.INVENTORY_PROVIDER -> ref.slot() >= 0 && ref.slot() < 36 ? player.getInventory().getItem(ref.slot()) : ItemStack.EMPTY;
            case DripSlotRef.ARMOR_PROVIDER -> ref.slot() >= 0 && ref.slot() < player.getInventory().armor.size() ? player.getInventory().armor.get(ref.slot()) : ItemStack.EMPTY;
            case DripSlotRef.OFFHAND_PROVIDER -> ref.slot() == 0 ? player.getOffhandItem() : ItemStack.EMPTY;
            default -> ItemStack.EMPTY;
        };
    }

    private Component slotLabel(DripSlotRef ref) {
        if (ref == null) {
            return Component.literal("?");
        }
        return switch (ref.provider()) {
            case DripSlotRef.INVENTORY_PROVIDER -> Component.literal(ref.slot() < 9 ? "H" + (ref.slot() + 1) : "I");
            case DripSlotRef.ARMOR_PROVIDER -> Component.literal("A" + (ref.slot() + 1));
            case DripSlotRef.OFFHAND_PROVIDER -> Component.literal("O");
            default -> Component.literal("?");
        };
    }

    private boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private Component mendLabel() {
        return Component.translatable(menu.getData().upgrades().has(DripNullUpgradeType.MEND)
                ? "dn.dripnull.mend_installed"
                : "dn.dripnull.install_mend");
    }

    private int idleVaultCount() {
        var assigned = menu.getData().assignedRefs();
        int count = 0;
        for (String ref : menu.getData().vaultItems().keySet()) {
            if (!assigned.contains(ref)) {
                count++;
            }
        }
        return count;
    }
}
