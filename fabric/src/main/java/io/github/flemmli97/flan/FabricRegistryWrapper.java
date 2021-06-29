package io.github.flemmli97.flan;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FabricRegistryWrapper<T> implements SimpleRegistryWrapper<T> {

    private final Registry<T> delegate;

    public FabricRegistryWrapper(Registry<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T getFromId(Identifier id) {
        return this.delegate.get(id);
    }

    @Override
    public Identifier getIDFrom(T entry) {
        return this.delegate.getId(entry);
    }

    @Override
    public Iterable<T> getIterator() {
        return this.delegate;
    }
}
