package io.github.flemmli97.flan.forge;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.forge.forgeevent.BlockInteractEventsForge;
import io.github.flemmli97.flan.forge.forgeevent.EntityInteractEventsForge;
import io.github.flemmli97.flan.forge.forgeevent.ItemInteractEventsForge;
import io.github.flemmli97.flan.forge.forgeevent.ServerEvents;
import io.github.flemmli97.flan.forge.forgeevent.WorldEventsForge;
import io.github.flemmli97.flan.platform.integration.webmap.DynmapIntegration;
import io.github.flemmli97.flan.scoreboard.ClaimCriterias;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(FlanForge.MODID)
public class FlanForge {

    public static final String MODID = "flan";

    public FlanForge() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "*", (s1, s2) -> true));
        Flan.ftbRanks = ModList.get().isLoaded("ftbranks");
        Flan.diceMCMoneySign = ModList.get().isLoaded("dicemcmm");

        IEventBus forge = MinecraftForge.EVENT_BUS;
        forge.addListener(WorldEventsForge::modifyExplosion);
        forge.addListener(WorldEventsForge::preventMobSpawn);
        forge.addListener(ItemInteractEventsForge::useItem);
        forge.addListener(BlockInteractEventsForge::startBreakBlocks);
        forge.addListener(BlockInteractEventsForge::breakBlocks);
        forge.addListener(EventPriority.HIGHEST, BlockInteractEventsForge::useBlocks);
        forge.addListener(EntityInteractEventsForge::attackEntity);
        forge.addListener(EntityInteractEventsForge::useAtEntity);
        forge.addListener(EntityInteractEventsForge::useEntity);
        forge.addListener(EntityInteractEventsForge::projectileHit);
        forge.addListener(EntityInteractEventsForge::preventDamage);
        forge.addListener(EntityInteractEventsForge::xpAbsorb);
        forge.addListener(EntityInteractEventsForge::canDropItem);
        forge.addListener(EntityInteractEventsForge::mobGriefing);
        forge.addListener(EntityInteractEventsForge::entityLightningHit);

        forge.addListener(ServerEvents::serverStart);
        forge.addListener(ServerEvents::commands);
        forge.addListener(ServerEvents::savePlayer);
        forge.addListener(ServerEvents::readPlayer);
        forge.addListener(ServerEvents::serverFinishLoad);
        forge.addListener(ServerEvents::disconnect);
        forge.addListener(ServerEvents::serverTick);

        if (ModList.get().isLoaded("dynmap"))
            DynmapIntegration.reg();

        ClaimCriterias.init();
    }
}
