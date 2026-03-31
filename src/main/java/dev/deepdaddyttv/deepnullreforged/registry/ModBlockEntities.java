package dev.deepdaddyttv.deepnullreforged.registry;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import dev.deepdaddyttv.deepnullreforged.compat.registries.DeferredHolder;
import dev.deepdaddyttv.deepnullreforged.compat.registries.DeferredRegister;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.Set;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DeepNullReforged.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeepNullDockBlockEntity>> DEEP_NULL_DOCK = BLOCK_ENTITY_TYPES.register(
            "deepnull_dock",
            () -> createType(DeepNullDockBlockEntity::new, ModBlocks.DEEP_NULL_DOCK.get())
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NullWorkbenchBlockEntity>> NULL_WORKBENCH = BLOCK_ENTITY_TYPES.register(
            "null_workbench",
            () -> createType(NullWorkbenchBlockEntity::new, ModBlocks.NULL_WORKBENCH.get())
    );

    private ModBlockEntities() {
    }

    public static void register() {
        BLOCK_ENTITY_TYPES.register();
    }

    @SuppressWarnings("unchecked")
    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityType<T> createType(BlockEntityFactory<T> factory, Block... validBlocks) {
        try {
            Constructor<?> constructor = BlockEntityType.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            Class<?> supplierType = constructor.getParameterTypes()[0];
            Object supplier = Proxy.newProxyInstance(
                    supplierType.getClassLoader(),
                    new Class<?>[]{supplierType},
                    (proxy, method, args) -> factory.create((BlockPos) args[0], (BlockState) args[1])
            );
            return (BlockEntityType<T>) constructor.newInstance(supplier, Set.of(validBlocks));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to construct block entity type", exception);
        }
    }

    @FunctionalInterface
    private interface BlockEntityFactory<T extends net.minecraft.world.level.block.entity.BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }
}
