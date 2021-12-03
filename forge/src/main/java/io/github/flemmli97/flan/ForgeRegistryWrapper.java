package io.github.flemmli97.flan;

import io.github.flemmli97.flan.SimpleRegistryWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ForgeRegistryWrapper<T extends IForgeRegistryEntry<T>> implements SimpleRegistryWrapper<T> {

    private final IForgeRegistry<T> registry;

    public ForgeRegistryWrapper(IForgeRegistry<T> registry) {
        this.registry = registry;
    }

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
