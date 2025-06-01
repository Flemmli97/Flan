package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.player.LogoutTracker;
import io.github.flemmli97.flan.utils.LogoutImpl;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements LogoutImpl {

    @Unique
    private final LogoutTracker flan$Logout = new LogoutTracker();

    @Override
    public LogoutTracker flan$getLogoutTracker() {
        return this.flan$Logout;
    }
}
