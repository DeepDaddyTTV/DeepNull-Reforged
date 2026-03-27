package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DeepNullReforged.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeepNullDockBlockEntity>> DEEP_NULL_DOCK = BLOCK_ENTITY_TYPES.register(
            "deepnull_dock",
            () -> new BlockEntityType<>(DeepNullDockBlockEntity::new, ModBlocks.DEEP_NULL_DOCK.get())
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NullWorkbenchBlockEntity>> NULL_WORKBENCH = BLOCK_ENTITY_TYPES.register(
            "null_workbench",
            () -> new BlockEntityType<>(NullWorkbenchBlockEntity::new, ModBlocks.NULL_WORKBENCH.get())
    );

    private ModBlockEntities() {
    }
}
