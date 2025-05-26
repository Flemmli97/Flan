package io.github.flemmli97.flan.neoforge.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

public class WorldEvents {

    public static void modifyExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel)
            io.github.flemmli97.flan.event.WorldEvents.modifyExplosion(event.getExplosion(), (ServerLevel) event.getLevel());
    }

    public static void preventMobSpawn(FinalizeSpawnEvent event) {
        if (!(event.getLevel() instanceof ServerLevel) || event.getSpawnType() != MobSpawnType.NATURAL)
            return;
        if (io.github.flemmli97.flan.event.WorldEvents.preventMobSpawn((ServerLevel) event.getLevel(), event.getEntity()))
            event.setSpawnCancelled(true);
    }
}
