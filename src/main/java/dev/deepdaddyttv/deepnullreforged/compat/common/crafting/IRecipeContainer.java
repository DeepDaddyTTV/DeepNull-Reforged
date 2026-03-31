package dev.deepdaddyttv.deepnullreforged.compat.common.crafting;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;

public interface IRecipeContainer {
    CraftingContainer getCraftMatrix();

    ResultContainer getCraftResult();
}
