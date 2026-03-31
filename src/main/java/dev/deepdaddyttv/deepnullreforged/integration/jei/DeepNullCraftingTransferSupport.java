package dev.deepdaddyttv.deepnullreforged.integration.jei;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DeepNullCraftingTransferSupport {
    private DeepNullCraftingTransferSupport() {
    }

    public static @Nullable TransferPlan planTransfer(AbstractContainerMenu menu, Player player, RecipeHolder<CraftingRecipe> recipeHolder, boolean maxTransfer) {
        CraftingContext context = resolveContext(menu);
        if (context == null) {
            return null;
        }

        Ingredient[] layout = buildLayout(recipeHolder.value(), context.gridWidth(), context.gridHeight());
        if (layout == null) {
            return null;
        }

        SourceSnapshot sourceSnapshot = scanSources(player, context);
        Selection selection = selectLayout(layout, sourceSnapshot.availableCounts(), maxTransfer);
        if (selection == null) {
            return null;
        }

        return new TransferPlan(context, layout, selection.slotItems(), selection.craftCount());
    }

    public static boolean executeTransfer(AbstractContainerMenu menu, Player player, RecipeHolder<CraftingRecipe> recipeHolder, boolean maxTransfer) {
        TransferPlan plan = planTransfer(menu, player, recipeHolder, maxTransfer);
        if (plan == null) {
            return false;
        }

        List<ItemStack> temporaryCraftContents = new ArrayList<>(plan.context().craftSlotIndices().size());
        for (int slotIndex : plan.context().craftSlotIndices()) {
            Slot slot = menu.getSlot(slotIndex);
            ItemStack stack = slot.getItem();
            temporaryCraftContents.add(stack.copy());
            if (!stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }
        }

        LinkedHashSet<Integer> preferredDeepNullInventorySlots = new LinkedHashSet<>();
        for (int gridIndex = 0; gridIndex < plan.slotItems().length; gridIndex++) {
            int slotIndex = plan.context().craftSlotIndices().get(gridIndex);
            ItemStack slotItem = plan.slotItems()[gridIndex];
            Slot slot = menu.getSlot(slotIndex);
            if (slotItem.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
                continue;
            }

            if (!pullMatchingItems(player, slotItem, temporaryCraftContents, preferredDeepNullInventorySlots)) {
                restoreCraftingContents(menu, player, plan.context(), temporaryCraftContents);
                return false;
            }

            slot.setByPlayer(slotItem.copy());
        }

        ServerDeepNullJeiSession.mark(player, menu, List.copyOf(preferredDeepNullInventorySlots));
        List<Integer> preferredInventorySlots = ServerDeepNullJeiSession.preferredInventorySlots(player, menu);
        for (ItemStack leftover : temporaryCraftContents) {
            if (!leftover.isEmpty()) {
                returnStackToPlayer(player, leftover, preferredInventorySlots);
            }
        }

        menu.slotsChanged(plan.context().craftMatrix());
        menu.broadcastChanges();
        player.getInventory().setChanged();
        return true;
    }

    private static void restoreCraftingContents(AbstractContainerMenu menu, Player player, CraftingContext context, List<ItemStack> temporaryCraftContents) {
        for (int gridIndex = 0; gridIndex < context.craftSlotIndices().size(); gridIndex++) {
            int slotIndex = context.craftSlotIndices().get(gridIndex);
            Slot slot = menu.getSlot(slotIndex);
            ItemStack stack = temporaryCraftContents.get(gridIndex);
            slot.setByPlayer(stack.copy());
            temporaryCraftContents.set(gridIndex, ItemStack.EMPTY);
        }
        menu.slotsChanged(context.craftMatrix());
        menu.broadcastChanges();
        player.getInventory().setChanged();
    }

    public static @Nullable CraftingContext resolveContext(AbstractContainerMenu menu) {
        if (menu instanceof AbstractCraftingMenu craftingMenu) {
            Slot resultSlot = craftingMenu.getResultSlot();
            if (!(resultSlot.container instanceof ResultContainer resultContainer)) {
                return null;
            }

            List<Slot> inputSlots = craftingMenu.getInputGridSlots();
            if (inputSlots.isEmpty() || !(inputSlots.getFirst().container instanceof CraftingContainer craftMatrix)) {
                return null;
            }

            List<Integer> craftSlotIndices = new ArrayList<>(inputSlots.size());
            for (Slot inputSlot : inputSlots) {
                int menuSlotIndex = menu.slots.indexOf(inputSlot);
                if (menuSlotIndex < 0) {
                    return null;
                }
                craftSlotIndices.add(menuSlotIndex);
            }

            int resultSlotIndex = menu.slots.indexOf(resultSlot);
            if (resultSlotIndex < 0 || craftSlotIndices.size() != craftMatrix.getContainerSize()) {
                return null;
            }

            return new CraftingContext(
                    craftMatrix,
                    resultContainer,
                    craftSlotIndices,
                    resultSlotIndex,
                    craftMatrix.getWidth(),
                    craftMatrix.getHeight()
            );
        }

        return null;
    }

    private static @Nullable Ingredient[] buildLayout(CraftingRecipe recipe, int gridWidth, int gridHeight) {
        Ingredient[] layout = new Ingredient[gridWidth * gridHeight];

        if (recipe instanceof ShapedRecipe shapedRecipe) {
            if (shapedRecipe.getWidth() > gridWidth || shapedRecipe.getHeight() > gridHeight) {
                return null;
            }
            List<java.util.Optional<Ingredient>> ingredients = shapedRecipe.getIngredients();
            for (int y = 0; y < shapedRecipe.getHeight(); y++) {
                for (int x = 0; x < shapedRecipe.getWidth(); x++) {
                    int recipeIndex = y * shapedRecipe.getWidth() + x;
                    if (recipeIndex < ingredients.size()) {
                        layout[y * gridWidth + x] = ingredients.get(recipeIndex).orElse(null);
                    }
                }
            }
            return layout;
        }

        List<Ingredient> ingredients = recipe.placementInfo().ingredients();
        if (ingredients.size() > layout.length) {
            return null;
        }

        int nextSlot = 0;
        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                continue;
            }
            while (nextSlot < layout.length && layout[nextSlot] != null) {
                nextSlot++;
            }
            if (nextSlot >= layout.length) {
                return null;
            }
            layout[nextSlot++] = ingredient;
        }
        return layout;
    }

    private static SourceSnapshot scanSources(Player player, CraftingContext context) {
        Map<StackKey, Integer> availableCounts = new LinkedHashMap<>();
        for (int slotIndex : context.craftSlotIndices()) {
            ItemStack stack = context.craftMatrix().getItem(slotIndexForCraftSlot(context, slotIndex));
            addCount(availableCounts, stack, stack.getCount());
        }

        Inventory inventory = player.getInventory();
        for (int rawSlot : eligibleInventorySlots(inventory)) {
            ItemStack stack = inventory.getItem(rawSlot);
            addCount(availableCounts, stack, stack.getCount());

            if (stack.getItem() instanceof DeepNullItem deepNullItem) {
                DeepNullInventory deepNullInventory = new DeepNullInventory(deepNullItem.tier(), stack, player.level().registryAccess(), null);
                if (deepNullInventory.isFluidOnly()) {
                    continue;
                }
                for (int deepSlot = 0; deepSlot < deepNullInventory.getSlots(); deepSlot++) {
                    ItemStack stored = deepNullInventory.getStackInSlot(deepSlot);
                    addCount(availableCounts, stored, deepNullInventory.getExtractableAmount(deepSlot));
                }
            }
        }

        return new SourceSnapshot(availableCounts);
    }

    private static int slotIndexForCraftSlot(CraftingContext context, int menuSlotIndex) {
        return context.craftSlotIndices().indexOf(menuSlotIndex);
    }

    private static void addCount(Map<StackKey, Integer> availableCounts, ItemStack stack, int count) {
        if (stack.isEmpty() || count <= 0) {
            return;
        }
        availableCounts.merge(new StackKey(stack), count, Integer::sum);
    }

    private static @Nullable Selection selectLayout(Ingredient[] layout, Map<StackKey, Integer> availableCounts, boolean maxTransfer) {
        List<SlotRequirement> requirements = new ArrayList<>();
        for (int index = 0; index < layout.length; index++) {
            Ingredient ingredient = layout[index];
            if (ingredient == null || ingredient.isEmpty()) {
                continue;
            }
            List<StackKey> candidates = new ArrayList<>();
            for (StackKey key : availableCounts.keySet()) {
                if (ingredient.test(key.sample())) {
                    candidates.add(key);
                }
            }
            if (candidates.isEmpty()) {
                return null;
            }
            requirements.add(new SlotRequirement(index, candidates));
        }

        requirements.sort(Comparator.comparingInt(requirement -> requirement.candidates().size()));
        SearchState state = new SearchState(layout.length, availableCounts, maxTransfer);
        searchAssignments(0, requirements, new HashMap<>(), new HashMap<>(), state);
        return state.bestSelection;
    }

    private static void searchAssignments(
            int requirementIndex,
            List<SlotRequirement> requirements,
            Map<Integer, StackKey> slotAssignments,
            Map<StackKey, Integer> perCraftUsage,
            SearchState state
    ) {
        if (requirementIndex >= requirements.size()) {
            int craftCount = Integer.MAX_VALUE;
            ItemStack[] slotItems = new ItemStack[state.gridSize];
            Arrays.fill(slotItems, ItemStack.EMPTY);
            for (Map.Entry<StackKey, Integer> entry : perCraftUsage.entrySet()) {
                int available = state.availableCounts.getOrDefault(entry.getKey(), 0);
                craftCount = Math.min(craftCount, available / entry.getValue());
            }
            for (Map.Entry<Integer, StackKey> entry : slotAssignments.entrySet()) {
                craftCount = Math.min(craftCount, entry.getValue().sample().getMaxStackSize());
            }
            if (craftCount <= 0) {
                return;
            }
            if (!state.maxTransfer) {
                craftCount = 1;
            }

            for (Map.Entry<Integer, StackKey> entry : slotAssignments.entrySet()) {
                slotItems[entry.getKey()] = entry.getValue().sample().copyWithCount(craftCount);
            }

            if (state.bestSelection == null || craftCount > state.bestSelection.craftCount()) {
                state.bestSelection = new Selection(slotItems, craftCount);
            }
            return;
        }

        SlotRequirement requirement = requirements.get(requirementIndex);
        for (StackKey candidate : requirement.candidates()) {
            slotAssignments.put(requirement.slotIndex(), candidate);
            perCraftUsage.merge(candidate, 1, Integer::sum);
            searchAssignments(requirementIndex + 1, requirements, slotAssignments, perCraftUsage, state);
            perCraftUsage.computeIfPresent(candidate, (key, value) -> value == 1 ? null : value - 1);
            slotAssignments.remove(requirement.slotIndex());
            if (!state.maxTransfer && state.bestSelection != null) {
                return;
            }
        }
    }

    private static boolean pullMatchingItems(
            Player player,
            ItemStack target,
            List<ItemStack> temporaryCraftContents,
            LinkedHashSet<Integer> preferredDeepNullInventorySlots
    ) {
        int remaining = target.getCount();

        for (int index = 0; index < temporaryCraftContents.size() && remaining > 0; index++) {
            ItemStack stack = temporaryCraftContents.get(index);
            if (!ItemStack.isSameItemSameComponents(stack, target)) {
                continue;
            }
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            if (stack.isEmpty()) {
                temporaryCraftContents.set(index, ItemStack.EMPTY);
            }
            remaining -= taken;
        }

        Inventory inventory = player.getInventory();
        for (int rawSlot : eligibleInventorySlots(inventory)) {
            ItemStack stack = inventory.getItem(rawSlot);
            if (!(stack.getItem() instanceof DeepNullItem deepNullItem)) {
                continue;
            }
            DeepNullInventory deepNullInventory = new DeepNullInventory(deepNullItem.tier(), stack, player.level().registryAccess(), null);
            if (deepNullInventory.isFluidOnly() || !deepNullInventory.containsExtractableMatchingStack(target)) {
                continue;
            }
            preferredDeepNullInventorySlots.add(rawSlot);
        }

        for (int rawSlot : eligibleInventorySlots(inventory)) {
            if (remaining <= 0) {
                break;
            }
            ItemStack stack = inventory.getItem(rawSlot);
            if (!ItemStack.isSameItemSameComponents(stack, target)) {
                continue;
            }
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            if (stack.isEmpty()) {
                inventory.setItem(rawSlot, ItemStack.EMPTY);
            }
            remaining -= taken;
        }

        for (int rawSlot : eligibleInventorySlots(inventory)) {
            if (remaining <= 0) {
                break;
            }
            ItemStack stack = inventory.getItem(rawSlot);
            if (!(stack.getItem() instanceof DeepNullItem deepNullItem)) {
                continue;
            }
            DeepNullInventory deepNullInventory = new DeepNullInventory(deepNullItem.tier(), stack, player.level().registryAccess(), null);
            if (deepNullInventory.isFluidOnly()) {
                continue;
            }
            for (int deepSlot = 0; deepSlot < deepNullInventory.getSlots() && remaining > 0; deepSlot++) {
                ItemStack stored = deepNullInventory.getStackInSlot(deepSlot);
                if (!ItemStack.isSameItemSameComponents(stored, target)) {
                    continue;
                }
                ItemStack extracted = deepNullInventory.extractItem(deepSlot, remaining, false);
                if (!extracted.isEmpty()) {
                    preferredDeepNullInventorySlots.add(rawSlot);
                }
                remaining -= extracted.getCount();
            }
        }

        return remaining <= 0;
    }

    public static boolean returnCurrentCraftingContents(AbstractContainerMenu menu, Player player) {
        CraftingContext context = resolveContext(menu);
        if (context == null) {
            return false;
        }

        List<Integer> preferredInventorySlots = ServerDeepNullJeiSession.preferredInventorySlots(player, menu);
        boolean changed = false;
        for (int slotIndex : context.craftSlotIndices()) {
            Slot slot = menu.getSlot(slotIndex);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }

            ItemStack remaining = returnStackToDeepNulls(player, stack.copy(), preferredInventorySlots);
            if (remaining.getCount() == stack.getCount()) {
                continue;
            }

            slot.setByPlayer(remaining);
            changed = true;
        }

        if (changed) {
            menu.slotsChanged(context.craftMatrix());
            menu.broadcastChanges();
            player.getInventory().setChanged();
        }
        return changed;
    }

    public static boolean returnCraftingSnapshotContents(AbstractContainerMenu menu, Player player, List<ItemStack> craftContents) {
        if (craftContents.isEmpty()) {
            return false;
        }

        return returnCraftingSnapshotContents(player, menu.containerId, craftContents);
    }

    public static boolean returnCraftingSnapshotContents(Player player, int menuId, List<ItemStack> craftContents) {
        if (craftContents.isEmpty()) {
            return false;
        }

        List<Integer> preferredInventorySlots = ServerDeepNullJeiSession.preferredInventorySlots(player, menuId);
        if (preferredInventorySlots.isEmpty()) {
            return false;
        }

        boolean changed = false;
        Inventory inventory = player.getInventory();
        for (ItemStack snapshotStack : craftContents) {
            if (snapshotStack.isEmpty()) {
                continue;
            }

            ItemStack extracted = extractMatchingPlayerInventoryItems(inventory, snapshotStack, snapshotStack.getCount());
            if (extracted.isEmpty()) {
                continue;
            }

            ItemStack remaining = returnStackToDeepNulls(player, extracted, preferredInventorySlots);
            if (!remaining.isEmpty()) {
                player.getInventory().placeItemBackInInventory(remaining);
            }
            changed = true;
        }

        if (changed) {
            player.getInventory().setChanged();
        }
        return changed;
    }

    private static void returnStackToPlayer(Player player, ItemStack stack) {
        returnStackToPlayer(player, stack, List.of());
    }

    private static void returnStackToPlayer(Player player, ItemStack stack, List<Integer> preferredInventorySlots) {
        if (stack.isEmpty()) {
            return;
        }

        ItemStack remaining = returnStackToDeepNulls(player, stack, preferredInventorySlots);
        if (!remaining.isEmpty()) {
            player.getInventory().placeItemBackInInventory(remaining);
        }
    }

    private static ItemStack returnStackToDeepNulls(Player player, ItemStack stack) {
        return returnStackToDeepNulls(player, stack, List.of());
    }

    private static ItemStack returnStackToDeepNulls(Player player, ItemStack stack, List<Integer> preferredInventorySlots) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        Inventory inventory = player.getInventory();
        ItemStack remaining = stack.copy();
        for (int rawSlot : preferredInventorySlots) {
            remaining = returnToDeepNullAtSlot(player, inventory, rawSlot, remaining, true);
            if (remaining.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        for (int rawSlot : eligibleInventorySlots(inventory)) {
            if (remaining.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (preferredInventorySlots.contains(rawSlot)) {
                continue;
            }
            remaining = returnToDeepNullAtSlot(player, inventory, rawSlot, remaining, true);
        }

        for (int rawSlot : preferredInventorySlots) {
            remaining = returnToDeepNullAtSlot(player, inventory, rawSlot, remaining, false);
            if (remaining.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        for (int rawSlot : eligibleInventorySlots(inventory)) {
            if (remaining.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (preferredInventorySlots.contains(rawSlot)) {
                continue;
            }
            remaining = returnToDeepNullAtSlot(player, inventory, rawSlot, remaining, false);
        }

        return remaining;
    }

    private static ItemStack returnToDeepNullAtSlot(
            Player player,
            Inventory inventory,
            int rawSlot,
            ItemStack stack,
            boolean matchingOnly
    ) {
        if (stack.isEmpty() || rawSlot < 0 || rawSlot >= inventory.getContainerSize()) {
            return stack;
        }

        ItemStack containerStack = inventory.getItem(rawSlot);
        if (!(containerStack.getItem() instanceof DeepNullItem deepNullItem)) {
            return stack;
        }
        DeepNullInventory deepNullInventory = new DeepNullInventory(deepNullItem.tier(), containerStack, player.level().registryAccess(), null);
        if (deepNullInventory.isFluidOnly()) {
            return stack;
        }
        if (matchingOnly && !deepNullInventory.containsMatchingStack(stack)) {
            return stack;
        }
        return deepNullInventory.insertReturnedCraftingStack(stack, false);
    }

    private static ItemStack extractMatchingPlayerInventoryItems(Inventory inventory, ItemStack target, int amount) {
        if (target.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack extracted = target.copyWithCount(0);
        int remaining = amount;
        for (int rawSlot : eligibleInventorySlots(inventory)) {
            if (remaining <= 0) {
                break;
            }

            ItemStack stack = inventory.getItem(rawSlot);
            if (stack.getItem() instanceof DeepNullItem || !ItemStack.isSameItemSameComponents(stack, target)) {
                continue;
            }

            int taken = Math.min(remaining, stack.getCount());
            if (taken <= 0) {
                continue;
            }

            if (extracted.isEmpty()) {
                extracted = stack.copyWithCount(taken);
            } else {
                extracted.grow(taken);
            }

            stack.shrink(taken);
            if (stack.isEmpty()) {
                inventory.setItem(rawSlot, ItemStack.EMPTY);
            }
            remaining -= taken;
        }
        return extracted;
    }

    private static List<Integer> eligibleInventorySlots(Inventory inventory) {
        List<Integer> slots = new ArrayList<>(37);
        int selected = inventory.getSelectedSlot();
        if (selected >= 0 && selected < 36) {
            slots.add(selected);
        }
        for (int slot = 0; slot < 36; slot++) {
            if (slot == selected) {
                continue;
            }
            slots.add(slot);
        }
        slots.add(40);
        return slots;
    }

    public record CraftingContext(
            CraftingContainer craftMatrix,
            ResultContainer resultContainer,
            List<Integer> craftSlotIndices,
            int resultSlotIndex,
            int gridWidth,
            int gridHeight
    ) {
    }

    public record TransferPlan(CraftingContext context, Ingredient[] layout, ItemStack[] slotItems, int craftCount) {
    }

    private record SourceSnapshot(Map<StackKey, Integer> availableCounts) {
    }

    private record Selection(ItemStack[] slotItems, int craftCount) {
    }

    private record SlotRequirement(int slotIndex, List<StackKey> candidates) {
    }

    private static final class SearchState {
        private final int gridSize;
        private final Map<StackKey, Integer> availableCounts;
        private final boolean maxTransfer;
        private @Nullable Selection bestSelection;

        private SearchState(int gridSize, Map<StackKey, Integer> availableCounts, boolean maxTransfer) {
            this.gridSize = gridSize;
            this.availableCounts = availableCounts;
            this.maxTransfer = maxTransfer;
        }
    }

    private static final class StackKey {
        private final ItemStack sample;
        private final int hash;

        private StackKey(ItemStack sample) {
            this.sample = sample.copyWithCount(1);
            this.hash = ItemStack.hashItemAndComponents(this.sample);
        }

        public ItemStack sample() {
            return sample;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof StackKey other)) {
                return false;
            }
            return ItemStack.isSameItemSameComponents(sample, other.sample);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
