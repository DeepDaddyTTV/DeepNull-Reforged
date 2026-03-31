package dev.deepdaddyttv.deepnullreforged.compat.registries;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public final class DeferredItem<T extends Item> extends DeferredHolder<Item, T> {
    public DeferredItem(Identifier id, Supplier<? extends T> supplier) {
        super(id, supplier);
    }
}
