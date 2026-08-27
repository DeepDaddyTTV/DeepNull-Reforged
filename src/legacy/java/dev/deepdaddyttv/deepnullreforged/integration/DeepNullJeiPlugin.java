package dev.deepdaddyttv.deepnullreforged.integration;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.registry.ModContent;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class DeepNullJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return DeepNullReforged.id("jei");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<ItemStack> deepNulls = new ArrayList<ItemStack>();
        List<ItemStack> dampNulls = new ArrayList<ItemStack>();
        for (int i = 0; i < 7; i++) {
            deepNulls.add(new ItemStack(ModContent.DEEP_NULLS[i].get()));
            dampNulls.add(new ItemStack(ModContent.DAMP_NULLS[i].get()));
        }
        registration.addIngredientInfo(deepNulls, VanillaTypes.ITEM,
                new TranslationTextComponent("jei.deepnullreforged.desc"));
        registration.addIngredientInfo(dampNulls, VanillaTypes.ITEM,
                new TranslationTextComponent("dn.fluid_empty_hint.desc"));
        registration.addIngredientInfo(new ItemStack(ModContent.DEEP_NULL_DOCK_ITEM.get()), VanillaTypes.ITEM,
                new TranslationTextComponent("jei.deepnull_dock.desc"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModContent.NULL_WORKBENCH_ITEM.get()), VanillaRecipeCategoryUid.CRAFTING);
    }
}
