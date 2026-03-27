package net.neoforged.neoforge.registries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public final class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> {
    public DeferredBlock(ResourceLocation id, Supplier<? extends T> supplier) {
        super(id, supplier);
    }
}
