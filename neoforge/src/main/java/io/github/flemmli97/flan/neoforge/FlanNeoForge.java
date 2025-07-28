package io.github.flemmli97.flan.neoforge;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.api.permission.PermissionManager;
import io.github.flemmli97.flan.neoforge.events.BlockInteractEventsNeoForge;
import io.github.flemmli97.flan.neoforge.events.EntityInteractEventsNeoForge;
import io.github.flemmli97.flan.neoforge.events.ItemInteractEventsNeoForge;
import io.github.flemmli97.flan.neoforge.events.ServerEvents;
import io.github.flemmli97.flan.neoforge.events.WorldEventsNeoForge;
import io.github.flemmli97.flan.platform.integration.webmap.DynmapIntegration;
import io.github.flemmli97.flan.scoreboard.ClaimCriterias;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

@Mod(FlanNeoForge.MODID)
public class FlanNeoForge {

    public static final String MODID = "flan";

    public FlanNeoForge() {
        Flan.ftbRanks = ModList.get().isLoaded("ftbranks");
        Flan.diceMCMoneySign = ModList.get().isLoaded("dicemcmm");
        Flan.ftbChunks = ModList.get().isLoaded("ftbchunks");
        Flan.mineColonies = ModList.get().isLoaded("minecolonies");
        Flan.impactor = ModList.get().isLoaded("impactor");

        IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(WorldEventsNeoForge::modifyExplosion);
        bus.addListener(WorldEventsNeoForge::preventMobSpawn);
        bus.addListener(ItemInteractEventsNeoForge::useItem);

        bus.addListener(EventPriority.HIGHEST, BlockInteractEventsNeoForge::startBreakBlocks);
        bus.addListener(EventPriority.HIGHEST, BlockInteractEventsNeoForge::breakBlocks);
        bus.addListener(EventPriority.HIGHEST, BlockInteractEventsNeoForge::useBlocks);
        bus.addListener(EventPriority.HIGHEST, BlockInteractEventsNeoForge::placeBlock);
        bus.addListener(EventPriority.HIGHEST, BlockInteractEventsNeoForge::placeBlocks);
        bus.addListener(EventPriority.HIGHEST, EntityInteractEventsNeoForge::useAtEntity);
        bus.addListener(EventPriority.HIGHEST, EntityInteractEventsNeoForge::useEntity);
        bus.addListener(EventPriority.HIGHEST, EntityInteractEventsNeoForge::projectileHit);
        bus.addListener(EventPriority.HIGHEST, EntityInteractEventsNeoForge::preventDamage);

        bus.addListener(EntityInteractEventsNeoForge::xpAbsorb);
        bus.addListener(EntityInteractEventsNeoForge::canDropItem);
        bus.addListener(EntityInteractEventsNeoForge::mobGriefing);
        bus.addListener(EntityInteractEventsNeoForge::entityLightningHit);

        bus.addListener(ServerEvents::serverStart);
        bus.addListener(ServerEvents::commands);
        bus.addListener(ServerEvents::savePlayer);
        bus.addListener(ServerEvents::readPlayer);
        bus.addListener(ServerEvents::serverFinishLoad);
        bus.addListener(ServerEvents::disconnect);
        bus.addListener(ServerEvents::serverTick);
        bus.addListener(this::addReloadListener);

        if (ModList.get().isLoaded("dynmap"))
            DynmapIntegration.reg();
        Flan.create = ModList.get().isLoaded("create");

        ClaimCriterias.init();
    }

    public void addReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(PermissionManager.ID.location(), PermissionManager.create(event.getServerResources().getRegistryLookup()));
        event.addListener(InteractionOverrideManager.ID.location(), InteractionOverrideManager.create(event.getServerResources().getRegistryLookup()));
    }
}