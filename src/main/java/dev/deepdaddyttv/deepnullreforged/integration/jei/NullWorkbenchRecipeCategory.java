package dev.deepdaddyttv.deepnullreforged.integration.jei;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.recipe.NullWorkbenchRecipes;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class NullWorkbenchRecipeCategory implements IRecipeCategory<NullWorkbenchRecipes.CraftRecipe> {
    public static final IRecipeType<NullWorkbenchRecipes.CraftRecipe> RECIPE_TYPE =
            IRecipeType.create(DeepNullReforged.id("null_workbench"), NullWorkbenchRecipes.CraftRecipe.class);

    private static final Identifier BACKGROUND_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_crafting_gui.png");
    private static final Identifier PROGRESS_TEXTURE = DeepNullReforged.id("textures/gui/null_workbench_crafting_gui_bar_progress.png");
    private static final int BACKGROUND_U = 40;
    private static final int BACKGROUND_V = 48;
    private static final int BACKGROUND_WIDTH = 160;
    private static final int BACKGROUND_HEIGHT = 80;
    private static final int INPUT_X = 14;
    private static final int INPUT_Y = 24;
    private static final int INPUT_SPACING = 21;
    private static final int OUTPUT_X = 140;
    private static final int OUTPUT_Y = 24;
    private static final int PROGRESS_X = 21;
    private static final int PROGRESS_Y = 45;
    private static final int PROGRESS_WIDTH = 133;
    private static final int PROGRESS_HEIGHT = 26;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progress;

    public NullWorkbenchRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(BACKGROUND_TEXTURE, BACKGROUND_U, BACKGROUND_V, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.NULL_WORKBENCH.get());
        IDrawableStatic progressStatic = guiHelper.createDrawable(PROGRESS_TEXTURE, 61, 93, PROGRESS_WIDTH, PROGRESS_HEIGHT);
        this.progress = guiHelper.createAnimatedDrawable(progressStatic, 72, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public IRecipeType<NullWorkbenchRecipes.CraftRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("container.deepnullreforged.null_workbench");
    }

    @Override
    public int getWidth() {
        return BACKGROUND_WIDTH;
    }

    @Override
    public int getHeight() {
        return BACKGROUND_HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, NullWorkbenchRecipes.CraftRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.ingredients().size(); i++) {
            builder.addInputSlot(INPUT_X + (i * INPUT_SPACING), INPUT_Y)
                    .addItemStack(recipe.ingredients().get(i).stack())
                    .setStandardSlotBackground();
        }
        builder.addOutputSlot(OUTPUT_X, OUTPUT_Y)
                .addItemStack(recipe.result())
                .setOutputSlotBackground();
    }

}
