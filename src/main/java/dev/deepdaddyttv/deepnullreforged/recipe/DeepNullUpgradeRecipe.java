package dev.deepdaddyttv.deepnullreforged.recipe;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullPanelItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class DeepNullUpgradeRecipe extends CustomRecipe {
    public static final DeepNullUpgradeRecipe INSTANCE = new DeepNullUpgradeRecipe();

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() < 3 || input.height() < 3) {
            return false;
        }

        ItemStack center = input.getItem(1, 1);
        if (!(center.getItem() instanceof DeepNullItem deepNullItem) || deepNullItem.tier().creative()) {
            return false;
        }

        DeepNullTier currentTier = deepNullItem.tier();
        DeepNullTier nextTier = currentTier.next();
        if (nextTier == currentTier) {
            return false;
        }

        return matchesPanel(input.getItem(1, 0), nextTier)
                && matchesPanel(input.getItem(0, 1), nextTier)
                && matchesPanel(input.getItem(2, 1), nextTier)
                && matchesPanel(input.getItem(1, 2), nextTier)
                && input.getItem(0, 0).isEmpty()
                && input.getItem(2, 0).isEmpty()
                && input.getItem(0, 2).isEmpty()
                && input.getItem(2, 2).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack center = input.getItem(1, 1);
        if (!(center.getItem() instanceof DeepNullItem deepNullItem)) {
            return ItemStack.EMPTY;
        }

        DeepNullTier nextTier = deepNullItem.tier().next();
        if (nextTier == deepNullItem.tier()) {
            return ItemStack.EMPTY;
        }
        ItemStack upgraded = center.transmuteCopy(switch (nextTier) {
            case LAPIS -> dev.deepdaddyttv.deepnullreforged.registry.ModItems.LAPIS_DEEP_NULL.get();
            case IRON -> dev.deepdaddyttv.deepnullreforged.registry.ModItems.IRON_DEEP_NULL.get();
            case GOLD -> dev.deepdaddyttv.deepnullreforged.registry.ModItems.GOLD_DEEP_NULL.get();
            case DIAMOND -> dev.deepdaddyttv.deepnullreforged.registry.ModItems.DIAMOND_DEEP_NULL.get();
            case EMERALD -> dev.deepdaddyttv.deepnullreforged.registry.ModItems.EMERALD_DEEP_NULL.get();
            default -> dev.deepdaddyttv.deepnullreforged.registry.ModItems.REDSTONE_DEEP_NULL.get();
        });
        upgraded.setCount(1);
        return upgraded;
    }

    @Override
    public RecipeSerializer<DeepNullUpgradeRecipe> getSerializer() {
        return ModRecipeSerializers.DEEP_NULL_UPGRADE.get();
    }

    private static boolean matchesPanel(ItemStack stack, DeepNullTier expectedTier) {
        return stack.getItem() instanceof DeepNullPanelItem panelItem && panelItem.tier() == expectedTier;
    }
}
