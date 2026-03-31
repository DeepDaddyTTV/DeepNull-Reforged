package dev.deepdaddyttv.deepnullreforged.recipe;

import dev.deepdaddyttv.deepnullreforged.item.SynchronizerItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import dev.deepdaddyttv.deepnullreforged.registry.ModRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class SynchronizerClearRecipe extends CustomRecipe {
    public static final SynchronizerClearRecipe INSTANCE = new SynchronizerClearRecipe();

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
    public ItemStack assemble(CraftingInput input) {
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
    public RecipeSerializer<SynchronizerClearRecipe> getSerializer() {
        return ModRecipeSerializers.SYNCHRONIZER_CLEAR.get();
    }
}
