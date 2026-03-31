package dev.deepdaddyttv.deepnullreforged.recipe;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DampNullItem;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullPanelItem;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import dev.deepdaddyttv.deepnullreforged.registry.ModRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class DampNullUpgradeRecipe extends CustomRecipe {
    public static final DampNullUpgradeRecipe INSTANCE = new DampNullUpgradeRecipe();

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() < 3 || input.height() < 3) {
            return false;
        }

        ItemStack center = input.getItem(1, 1);
        if (!(center.getItem() instanceof DampNullItem dampNullItem) || dampNullItem.tier().creative()) {
            return false;
        }

        DeepNullTier currentTier = dampNullItem.tier();
        DeepNullTier nextTier = currentTier.next();
        if (nextTier == currentTier) {
            return false;
        }

        return matchesBucket(input.getItem(0, 0))
                && matchesBucket(input.getItem(2, 0))
                && matchesBucket(input.getItem(0, 2))
                && matchesBucket(input.getItem(2, 2))
                && matchesPanel(input.getItem(1, 0), nextTier)
                && matchesPanel(input.getItem(0, 1), nextTier)
                && matchesPanel(input.getItem(2, 1), nextTier)
                && matchesPanel(input.getItem(1, 2), nextTier);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack center = input.getItem(1, 1);
        if (!(center.getItem() instanceof DampNullItem dampNullItem)) {
            return ItemStack.EMPTY;
        }

        DeepNullTier nextTier = dampNullItem.tier().next();
        if (nextTier == dampNullItem.tier()) {
            return ItemStack.EMPTY;
        }

        ItemStack upgraded = center.transmuteCopy(switch (nextTier) {
            case LAPIS -> ModItems.LAPIS_DAMP_NULL.get();
            case IRON -> ModItems.IRON_DAMP_NULL.get();
            case GOLD -> ModItems.GOLD_DAMP_NULL.get();
            case DIAMOND -> ModItems.DIAMOND_DAMP_NULL.get();
            case EMERALD -> ModItems.EMERALD_DAMP_NULL.get();
            default -> ModItems.REDSTONE_DAMP_NULL.get();
        });
        upgraded.setCount(1);
        return upgraded;
    }

    @Override
    public RecipeSerializer<DampNullUpgradeRecipe> getSerializer() {
        return ModRecipeSerializers.DAMP_NULL_UPGRADE.get();
    }

    private static boolean matchesPanel(ItemStack stack, DeepNullTier expectedTier) {
        return stack.getItem() instanceof DeepNullPanelItem panelItem && panelItem.tier() == expectedTier;
    }

    private static boolean matchesBucket(ItemStack stack) {
        return stack.is(Items.BUCKET);
    }
}
