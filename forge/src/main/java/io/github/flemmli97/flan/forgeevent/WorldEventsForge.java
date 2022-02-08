package io.github.flemmli97.flan.forgeevent;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;

public class WorldEventsForge {

    public static void modifyExplosion(ExplosionEvent.Detonate event) {
        if (event.getWorld() instanceof ServerWorld)
            WorldEvents.modifyExplosion(event.getExplosion(), (ServerWorld) event.getWorld());
    }

    public static void preventMobSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (!(event.getWorld() instanceof ServerWorld) || !(event.getEntityLiving() instanceof MobEntity))
            return;
        if (WorldEvents.preventMobSpawn((ServerWorld) event.getWorld(), (MobEntity) event.getEntityLiving()))
            event.setResult(Event.Result.DENY);
    }
}
