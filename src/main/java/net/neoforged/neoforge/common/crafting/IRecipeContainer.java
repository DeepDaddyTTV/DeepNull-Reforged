package net.neoforged.neoforge.common.crafting;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;

public interface IRecipeContainer {
    CraftingContainer getCraftMatrix();

    ResultContainer getCraftResult();
}
