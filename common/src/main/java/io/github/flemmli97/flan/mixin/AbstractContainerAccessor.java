package io.github.flemmli97.flan.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public interface AbstractContainerAccessor {

    @Accessor("containerListeners")
    List<ContainerListener> listeners();

    @Accessor("synchronizer")
    ContainerSynchronizer containerSync();

}
