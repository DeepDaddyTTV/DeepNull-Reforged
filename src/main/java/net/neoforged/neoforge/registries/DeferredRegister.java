package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DeferredRegister<T> {
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String namespace;
    private final List<DeferredHolder<T, ? extends T>> entries = new ArrayList<>();
    private boolean registered;

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this.registryKey = registryKey;
        this.namespace = namespace;
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        return new DeferredRegister<>(registryKey, namespace);
    }

    public static Items createItems(String namespace) {
        return new Items(namespace);
    }

    public static Blocks createBlocks(String namespace) {
        return new Blocks(namespace);
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
        return add(new DeferredHolder<>(id(name), supplier));
    }

    public void register() {
        if (registered) {
            return;
        }

        Registry<T> registry = resolveRegistry();
        for (DeferredHolder<T, ? extends T> entry : entries) {
            entry.register(registry);
        }

        registered = true;
    }

    protected final ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(namespace, name);
    }

    protected final <H extends DeferredHolder<T, ? extends T>> H add(H holder) {
        entries.add(holder);
        return holder;
    }

    @SuppressWarnings("unchecked")
    private Registry<T> resolveRegistry() {
        if (Registries.ITEM.equals(registryKey)) {
            return (Registry<T>) BuiltInRegistries.ITEM;
        }
        if (Registries.BLOCK.equals(registryKey)) {
            return (Registry<T>) BuiltInRegistries.BLOCK;
        }
        if (Registries.BLOCK_ENTITY_TYPE.equals(registryKey)) {
            return (Registry<T>) BuiltInRegistries.BLOCK_ENTITY_TYPE;
        }
        if (Registries.MENU.equals(registryKey)) {
            return (Registry<T>) BuiltInRegistries.MENU;
        }
        if (Registries.RECIPE_SERIALIZER.equals(registryKey)) {
            return (Registry<T>) BuiltInRegistries.RECIPE_SERIALIZER;
        }
        if (Registries.CREATIVE_MODE_TAB.equals(registryKey)) {
            return (Registry<T>) BuiltInRegistries.CREATIVE_MODE_TAB;
        }

        throw new IllegalStateException("Unsupported registry key: " + registryKey.location());
    }

    public static final class Items extends DeferredRegister<Item> {
        private Items(String namespace) {
            super(Registries.ITEM, namespace);
        }

        public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> supplier) {
            return add(new DeferredItem<>(id(name), supplier));
        }
    }

    public static final class Blocks extends DeferredRegister<Block> {
        private Blocks(String namespace) {
            super(Registries.BLOCK, namespace);
        }

        public <B extends Block> DeferredBlock<B> register(String name, Supplier<? extends B> supplier) {
            return add(new DeferredBlock<>(id(name), supplier));
        }
    }
}
