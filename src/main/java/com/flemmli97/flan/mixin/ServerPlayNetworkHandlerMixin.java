package com.flemmli97.flan.mixin;

import com.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fabric APIs UseEntityCallback is at Entity#interactAt and thats only used for armor stand. Why its only there idk...
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onPlayerInteractEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket;handle(Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket$Handler;)V"), cancellable = true)
    public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet, CallbackInfo info) {
        ServerWorld world = this.player.getServerWorld();

        Entity entity = packet.getEntity(world);
        AtomicBoolean bool = new AtomicBoolean(false);
        if (entity != null) {
            ServerPlayerEntity player = this.player;
            packet.handle(new PlayerInteractEntityC2SPacket.Handler() {

                @Override
                public void interact(Hand hand) {
                    ActionResult result = EntityInteractEvents.useEntity(player, world, hand, entity);
                    if (result != ActionResult.PASS) {
                        bool.set(true);
                    }
                }

                @Override
                public void interactAt(Hand hand, Vec3d pos) {
                    ActionResult result = EntityInteractEvents.useEntity(player, world, hand, entity);
                    if (result != ActionResult.PASS) {
                        bool.set(true);
                    }
                }

                @Override
                public void attack() {
                }
            });
            if (bool.get()) {
                info.cancel();
            }
        }
    }
}
