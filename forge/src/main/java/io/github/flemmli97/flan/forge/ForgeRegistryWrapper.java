package io.github.flemmli97.flan.forge;

import io.github.flemmli97.flan.SimpleRegistryWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public record ForgeRegistryWrapper<T>(
        IForgeRegistry<T> registry) implements SimpleRegistryWrapper<T> {

    @Override
    public T getFromId(ResourceLocation id) {
        return this.registry.getValue(id);
    }

    @Override
    public ResourceLocation getIDFrom(T entry) {
        return this.registry.getKey(entry);
    }

    @Override
    public Iterable<T> getIterator() {
        return this.registry;
    }
}
