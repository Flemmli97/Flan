package io.github.flemmli97.flan.forge.forgeevent;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;

public class WorldEventsForge {

    public static void modifyExplosion(ExplosionEvent.Detonate event) {
        if (event.getWorld() instanceof ServerLevel)
            WorldEvents.modifyExplosion(event.getExplosion(), (ServerLevel) event.getWorld());
    }

    public static void preventMobSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (!(event.getWorld() instanceof ServerLevel) || !(event.getEntityLiving() instanceof Mob) || event.getSpawnReason() != MobSpawnType.NATURAL)
            return;
        if (WorldEvents.preventMobSpawn((ServerLevel) event.getWorld(), (Mob) event.getEntityLiving()))
            event.setResult(Event.Result.DENY);
    }
}
