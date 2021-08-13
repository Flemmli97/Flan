package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.player.LogoutImpl;
import io.github.flemmli97.flan.player.LogoutTracker;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements LogoutImpl {

    @Unique
    private final LogoutTracker flanLogout = new LogoutTracker();

    @Override
    public LogoutTracker getInstance() {
        return this.flanLogout;
    }
}
