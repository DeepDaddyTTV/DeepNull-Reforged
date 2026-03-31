package dev.deepdaddyttv.deepnullreforged.compat.registries;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public final class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> {
    public DeferredBlock(Identifier id, Supplier<? extends T> supplier) {
        super(id, supplier);
    }
}
