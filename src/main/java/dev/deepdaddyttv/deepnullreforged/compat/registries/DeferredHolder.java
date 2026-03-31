package dev.deepdaddyttv.deepnullreforged.compat.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class DeferredHolder<R, T extends R> implements Supplier<T> {
    private final Identifier id;
    private final Supplier<? extends T> supplier;
    private T value;

    public DeferredHolder(Identifier id, Supplier<? extends T> supplier) {
        this.id = id;
        this.supplier = supplier;
    }

    Identifier id() {
        return id;
    }

    @SuppressWarnings("unchecked")
    void register(Registry<R> registry) {
        if (value != null) {
            return;
        }

        value = Registry.register((Registry<T>) registry, id, supplier.get());
    }

    @Override
    public T get() {
        if (value == null) {
            throw new IllegalStateException("Deferred entry has not been registered yet: " + id);
        }
        return value;
    }
}
