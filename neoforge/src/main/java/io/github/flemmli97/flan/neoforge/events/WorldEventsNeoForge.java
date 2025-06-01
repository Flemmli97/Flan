package io.github.flemmli97.flan.neoforge.events;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

public class WorldEventsNeoForge {

    public static void modifyExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel)
            WorldEvents.modifyExplosion(event.getAffectedBlocks(), (ServerLevel) event.getLevel());
    }

    public static void preventMobSpawn(FinalizeSpawnEvent event) {
        if (!(event.getLevel() instanceof ServerLevel) || event.getSpawnType() != EntitySpawnReason.NATURAL)
            return;
        if (WorldEvents.preventMobSpawn((ServerLevel) event.getLevel(), event.getEntity()))
            event.setSpawnCancelled(true);
    }
}
