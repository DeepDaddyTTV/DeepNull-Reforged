package dev.deepdaddyttv.deepnullreforged.recipe;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModContent;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class DampNullUpgradeRecipe extends SpecialRecipe {
    public DampNullUpgradeRecipe(ResourceLocation id) { super(id); }

    @Override
    public boolean matches(CraftingInventory input, World world) {
        if (input.getWidth() < 3 || input.getHeight() < 3) return false;
        ItemStack center = input.getItem(4);
        if (!(center.getItem() instanceof DampNullItem)) return false;
        DeepNullTier tier = ((DampNullItem) center.getItem()).getTier();
        DeepNullTier next = tier.next();
        if (next == tier) return false;
        return bucket(input.getItem(0)) && bucket(input.getItem(2))
                && bucket(input.getItem(6)) && bucket(input.getItem(8))
                && panel(input.getItem(1), next) && panel(input.getItem(3), next)
                && panel(input.getItem(5), next) && panel(input.getItem(7), next);
    }

    private boolean bucket(ItemStack stack) { return stack.getItem() == Items.BUCKET; }

    private boolean panel(ItemStack stack, DeepNullTier tier) {
        return tier.id() < 6 && stack.getItem() == ModContent.PANELS[tier.id()].get();
    }

    @Override
    public ItemStack assemble(CraftingInventory input) {
        ItemStack center = input.getItem(4);
        if (!(center.getItem() instanceof DampNullItem)) return ItemStack.EMPTY;
        DeepNullTier next = ((DampNullItem) center.getItem()).getTier().next();
        ItemStack result = new ItemStack(ModContent.DAMP_NULLS[next.id()].get());
        if (center.hasTag()) result.setTag(center.getTag().copy());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width >= 3 && height >= 3; }

    @Override
    public IRecipeSerializer<?> getSerializer() { return ModContent.DAMP_NULL_UPGRADE_RECIPE.get(); }
}
