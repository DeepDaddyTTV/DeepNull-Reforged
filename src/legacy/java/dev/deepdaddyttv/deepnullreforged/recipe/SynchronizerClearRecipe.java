package dev.deepdaddyttv.deepnullreforged.recipe;

import dev.deepdaddyttv.deepnullreforged.registry.ModContent;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class SynchronizerClearRecipe extends SpecialRecipe {
    public SynchronizerClearRecipe(ResourceLocation id) { super(id); }

    @Override
    public boolean matches(CraftingInventory input, World world) {
        ItemStack found = ItemStack.EMPTY;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() != ModContent.SYNCHRONIZER.get() || !found.isEmpty()) return false;
            found = stack;
        }
        return !found.isEmpty() && found.hasTag();
    }

    @Override
    public ItemStack assemble(CraftingInventory input) {
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() == ModContent.SYNCHRONIZER.get()) return new ItemStack(ModContent.SYNCHRONIZER.get());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width * height >= 1; }

    @Override
    public IRecipeSerializer<?> getSerializer() { return ModContent.SYNCHRONIZER_CLEAR_RECIPE.get(); }
}
