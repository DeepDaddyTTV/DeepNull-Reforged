package net.neoforged.neoforge.registries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public final class DeferredItem<T extends Item> extends DeferredHolder<Item, T> {
    public DeferredItem(ResourceLocation id, Supplier<? extends T> supplier) {
        super(id, supplier);
    }
}
