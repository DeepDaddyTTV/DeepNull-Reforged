package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.DeepNullDockBlock;
import dev.deepdaddyttv.deepnullreforged.block.NullWorkbenchBlock;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DeepNullReforged.MODID);

    public static final DeferredBlock<DeepNullDockBlock> DEEP_NULL_DOCK = BLOCKS.register("deepnull_dock", () -> new DeepNullDockBlock(DeepNullDockBlock.createProperties()));
    public static final DeferredBlock<NullWorkbenchBlock> NULL_WORKBENCH = BLOCKS.register("null_workbench", () -> new NullWorkbenchBlock(NullWorkbenchBlock.createProperties()));

    private ModBlocks() {
    }
}
