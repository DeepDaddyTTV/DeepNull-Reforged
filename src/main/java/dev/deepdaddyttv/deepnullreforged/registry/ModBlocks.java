package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.DeepNullDockBlock;
import dev.deepdaddyttv.deepnullreforged.block.NullWorkbenchBlock;
import dev.deepdaddyttv.deepnullreforged.compat.registries.DeferredBlock;
import dev.deepdaddyttv.deepnullreforged.compat.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DeepNullReforged.MODID);

    public static final DeferredBlock<DeepNullDockBlock> DEEP_NULL_DOCK = BLOCKS.register("deepnull_dock", DeepNullDockBlock::new);
    public static final DeferredBlock<NullWorkbenchBlock> NULL_WORKBENCH = BLOCKS.register("null_workbench", NullWorkbenchBlock::new);

    private ModBlocks() {
    }

    public static void register() {
        BLOCKS.register();
    }
}
