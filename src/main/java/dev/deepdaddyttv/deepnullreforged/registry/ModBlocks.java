package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.DeepNullDockBlock;
import dev.deepdaddyttv.deepnullreforged.block.NullWorkbenchBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DeepNullReforged.MODID);

    public static final DeferredBlock<Block> DEEP_NULL_DOCK = BLOCKS.register("deepnull_dock", DeepNullDockBlock::new);
    public static final DeferredBlock<Block> NULL_WORKBENCH = BLOCKS.register("null_workbench", NullWorkbenchBlock::new);

    private ModBlocks() {
    }
}
