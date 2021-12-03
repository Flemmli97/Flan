package io.github.flemmli97.flan;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class FabricRegistryWrapper<T> implements SimpleRegistryWrapper<T> {

    private final Registry<T> delegate;

    public FabricRegistryWrapper(Registry<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T getFromId(ResourceLocation id) {
        return this.delegate.get(id);
    }

    @Override
    public ResourceLocation getIDFrom(T entry) {
        return this.delegate.getKey(entry);
    }

    @Override
    public Iterable<T> getIterator() {
        return this.delegate;
    }
}
