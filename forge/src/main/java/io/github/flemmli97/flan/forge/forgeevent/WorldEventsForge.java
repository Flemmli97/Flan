package io.github.flemmli97.flan.forge.forgeevent;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;

public class WorldEventsForge {

    public static void modifyExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel)
            WorldEvents.modifyExplosion(event.getExplosion(), (ServerLevel) event.getLevel());
    }

    public static void preventMobSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (!(event.getLevel() instanceof ServerLevel))
            return;
        if (WorldEvents.preventMobSpawn((ServerLevel) event.getLevel(), event.getEntity()))
            event.setResult(Event.Result.DENY);
    }
}
