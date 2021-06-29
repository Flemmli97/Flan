package io.github.flemmli97.flan;

import io.github.flemmli97.flan.forgeevent.BlockInteractEventsForge;
import io.github.flemmli97.flan.forgeevent.EntityInteractEventsForge;
import io.github.flemmli97.flan.forgeevent.ItemInteractEventsForge;
import io.github.flemmli97.flan.forgeevent.ServerEvents;
import io.github.flemmli97.flan.forgeevent.WorldEventsForge;
import net.minecraftforge.common.MinecraftForge;
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
        forge.addListener(ItemInteractEventsForge::useItem);
        forge.addListener(BlockInteractEventsForge::startBreakBlocks);
        forge.addListener(BlockInteractEventsForge::breakBlocks);
        forge.addListener(EntityInteractEventsForge::attackEntity);
        forge.addListener(EntityInteractEventsForge::useAtEntity);
        forge.addListener(EntityInteractEventsForge::useEntity);
        forge.addListener(EntityInteractEventsForge::projectileHit);
        forge.addListener(EntityInteractEventsForge::preventDamage);
        forge.addListener(EntityInteractEventsForge::xpAbsorb);
        forge.addListener(EntityInteractEventsForge::canDropItem);
        forge.addListener(EntityInteractEventsForge::mobGriefing);

        forge.addListener(ServerEvents::serverStart);
        forge.addListener(ServerEvents::commands);
    }
}
