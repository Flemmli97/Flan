package io.github.flemmli97.flan;

import net.minecraft.util.Identifier;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ForgeRegistryWrapper<T extends IForgeRegistryEntry<T>> implements SimpleRegistryWrapper<T> {

    private final IForgeRegistry<T> registry;

    public ForgeRegistryWrapper(IForgeRegistry<T> registry) {
        this.registry = registry;
    }

    @Override
    public T getFromId(Identifier id) {
        return this.registry.getValue(id);
    }

    @Override
    public Identifier getIDFrom(T entry) {
        return entry.getRegistryName();
    }

    @Override
    public Iterable<T> getIterator() {
        return this.registry;
    }
}
