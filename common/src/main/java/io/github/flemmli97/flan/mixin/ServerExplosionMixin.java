package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {

    @Shadow
    private ServerLevel level;

    @Inject(method = "Lnet/minecraft/world/level/ServerExplosion;calculateExplodedPositions()Ljava/util/List;",
            at = @At("RETURN"),
            order = 1100)//inject after lithium
    public void shouldBlockExplodeHook(CallbackInfoReturnable<List<BlockPos>> info) {
        ClaimStorage storage = ClaimStorage.get(level);
        info.getReturnValue().removeIf(pos -> {
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            return claim != null && !claim.canInteract(null, BuiltinPermission.EXPLOSIONS, pos);
        });
    }
}
