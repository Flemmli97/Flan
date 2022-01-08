package io.github.flemmli97.flan.forge;

import io.github.flemmli97.flan.SimpleRegistryWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public record ForgeRegistryWrapper<T extends IForgeRegistryEntry<T>>(
        IForgeRegistry<T> registry) implements SimpleRegistryWrapper<T> {

    @Override
    public T getFromId(ResourceLocation id) {
        return this.registry.getValue(id);
    }

    @Override
    public ResourceLocation getIDFrom(T entry) {
        return entry.getRegistryName();
    }

    @Override
    public Iterable<T> getIterator() {
        return this.registry;
    }
}
