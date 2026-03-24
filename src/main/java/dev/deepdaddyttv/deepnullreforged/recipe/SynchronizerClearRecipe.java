package dev.deepdaddyttv.deepnullreforged.recipe;

import dev.deepdaddyttv.deepnullreforged.item.SynchronizerItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import dev.deepdaddyttv.deepnullreforged.registry.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class SynchronizerClearRecipe extends CustomRecipe {
    public SynchronizerClearRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack synchronizer = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (!stack.is(ModItems.SYNCHRONIZER.get()) || !synchronizer.isEmpty()) {
                return false;
            }
            synchronizer = stack;
        }
        return !synchronizer.isEmpty() && SynchronizerItem.hasConfiguration(synchronizer);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.is(ModItems.SYNCHRONIZER.get())) {
                continue;
            }
            ItemStack cleared = stack.copyWithCount(1);
            SynchronizerItem.clearConfiguration(cleared);
            return cleared;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SYNCHRONIZER_CLEAR.get();
    }
}
