package dev.deepdaddyttv.deepnullreforged.menu;

import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullItemHandler;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.item.DeepNullItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public class DeepNullMenuProvider implements INamedContainerProvider {
    private final ItemStack stack;
    private final Hand hand;
    private final DeepNullTier tier;

    public DeepNullMenuProvider(ItemStack stack, Hand hand, DeepNullTier tier) {
        this.stack = stack;
        this.hand = hand;
        this.tier = tier;
    }

    @Override
    public ITextComponent getDisplayName() {
        return stack.getHoverName();
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        DeepNullItemHandler handler = ((DeepNullItem) stack.getItem()).createHandler(stack);
        return new DeepNullMenu(id, inventory, handler, hand, tier);
    }
}
