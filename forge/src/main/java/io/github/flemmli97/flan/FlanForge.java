package io.github.flemmli97.flan;

import io.github.flemmli97.flan.forgeevent.WorldEventsForge;
import net.minecraft.world.explosion.Explosion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod(FlanForge.MODID)
public class FlanForge {

    public static final String MODID = "flan";

    public FlanForge() {
        IEventBus forge = MinecraftForge.EVENT_BUS;
        forge.addListener(WorldEventsForge::modifyExplosion);
        forge.addListener(WorldEventsForge::pistonCanPush);
        forge.addListener(WorldEventsForge::preventMobSpawn);
    }
}
