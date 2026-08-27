package dev.deepdaddyttv.deepnullreforged.recipe;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModContent;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class DeepNullUpgradeRecipe extends SpecialRecipe {
    public DeepNullUpgradeRecipe(ResourceLocation id) { super(id); }

    @Override
    public boolean matches(CraftingInventory input, World world) {
        if (input.getWidth() < 3 || input.getHeight() < 3) return false;
        ItemStack center = input.getItem(4);
        if (!(center.getItem() instanceof DeepNullItem)) return false;
        DeepNullTier tier = ((DeepNullItem) center.getItem()).getTier();
        DeepNullTier next = tier.next();
        if (next == tier) return false;
        return panel(input.getItem(1), next) && panel(input.getItem(3), next)
                && panel(input.getItem(5), next) && panel(input.getItem(7), next)
                && input.getItem(0).isEmpty() && input.getItem(2).isEmpty()
                && input.getItem(6).isEmpty() && input.getItem(8).isEmpty();
    }

    private boolean panel(ItemStack stack, DeepNullTier tier) {
        return tier.id() < 6 && stack.getItem() == ModContent.PANELS[tier.id()].get();
    }

    @Override
    public ItemStack assemble(CraftingInventory input) {
        ItemStack center = input.getItem(4);
        if (!(center.getItem() instanceof DeepNullItem)) return ItemStack.EMPTY;
        DeepNullTier next = ((DeepNullItem) center.getItem()).getTier().next();
        ItemStack result = new ItemStack(ModContent.DEEP_NULLS[next.id()].get());
        if (center.hasTag()) result.setTag(center.getTag().copy());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width >= 3 && height >= 3; }

    @Override
    public IRecipeSerializer<?> getSerializer() { return ModContent.DEEP_NULL_UPGRADE_RECIPE.get(); }
}
