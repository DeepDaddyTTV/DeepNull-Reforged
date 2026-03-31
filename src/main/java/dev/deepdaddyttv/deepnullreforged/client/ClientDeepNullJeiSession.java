package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.integration.jei.DeepNullCraftingTransferSupport;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ClientDeepNullJeiSession {
    private static int menuId = -1;
    private static List<ItemStack> lastCraftContents = List.of();

    private ClientDeepNullJeiSession() {
    }

    public static void markTransfer(AbstractContainerMenu menu) {
        menuId = menu.containerId;
        captureCraftContents(menu);
    }

    public static boolean shouldReturn(AbstractContainerMenu menu) {
        return menu.containerId == menuId;
    }

    public static void captureCraftContents(AbstractContainerMenu menu) {
        if (!shouldReturn(menu)) {
            return;
        }

        List<ItemStack> snapshot = snapshotCraftContents(menu);
        if (snapshot.stream().anyMatch(stack -> !stack.isEmpty())) {
            lastCraftContents = snapshot;
        }
    }

    public static List<ItemStack> craftingReturnContents(AbstractContainerMenu menu) {
        List<ItemStack> snapshot = snapshotCraftContents(menu);
        if (snapshot.stream().anyMatch(stack -> !stack.isEmpty())) {
            return snapshot;
        }
        return copyContents(lastCraftContents);
    }

    public static void clear() {
        menuId = -1;
        lastCraftContents = List.of();
    }

    private static List<ItemStack> snapshotCraftContents(AbstractContainerMenu menu) {
        DeepNullCraftingTransferSupport.CraftingContext context = DeepNullCraftingTransferSupport.resolveContext(menu);
        if (context == null) {
            return List.of();
        }

        List<ItemStack> snapshot = new ArrayList<>(context.craftSlotIndices().size());
        for (int slotIndex : context.craftSlotIndices()) {
            snapshot.add(menu.getSlot(slotIndex).getItem().copy());
        }
        return snapshot;
    }

    private static List<ItemStack> copyContents(List<ItemStack> contents) {
        List<ItemStack> copy = new ArrayList<>(contents.size());
        for (ItemStack stack : contents) {
            copy.add(stack.copy());
        }
        return copy;
    }
}
