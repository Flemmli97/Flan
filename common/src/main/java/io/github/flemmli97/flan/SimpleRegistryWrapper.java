package io.github.flemmli97.flan;

import net.minecraft.util.Identifier;

/**
 * Simple structure to get stuff from registries
 */
public interface SimpleRegistryWrapper<T> {

    T getFromId(Identifier id);

    Identifier getIDFrom(T entry);

    Iterable<T> getIterator();
}
