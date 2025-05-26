package io.github.flemmli97.flan.neoforge;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.api.permission.PermissionManager;
import io.github.flemmli97.flan.neoforge.events.BlockInteractEvents;
import io.github.flemmli97.flan.neoforge.events.EntityInteractEvents;
import io.github.flemmli97.flan.neoforge.events.ItemInteractEvents;
import io.github.flemmli97.flan.neoforge.events.ServerEvents;
import io.github.flemmli97.flan.neoforge.events.WorldEvents;
import io.github.flemmli97.flan.platform.integration.webmap.DynmapIntegration;
import io.github.flemmli97.flan.scoreboard.ClaimCriterias;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

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
        bus.addListener(WorldEvents::modifyExplosion);
        bus.addListener(WorldEvents::preventMobSpawn);
        bus.addListener(ItemInteractEvents::useItem);
        bus.addListener(EventPriority.HIGHEST, BlockInteractEvents::startBreakBlocks);
        bus.addListener(EventPriority.HIGHEST, BlockInteractEvents::breakBlocks);
        bus.addListener(EventPriority.HIGHEST, BlockInteractEvents::useBlocks);
        bus.addListener(EventPriority.HIGHEST, BlockInteractEvents::placeBlock);
        bus.addListener(EventPriority.HIGHEST, BlockInteractEvents::placeBlocks);
        bus.addListener(EntityInteractEvents::attackEntity);
        bus.addListener(EntityInteractEvents::useAtEntity);
        bus.addListener(EntityInteractEvents::useEntity);
        bus.addListener(EntityInteractEvents::projectileHit);
        bus.addListener(EntityInteractEvents::preventDamage);
        bus.addListener(EntityInteractEvents::xpAbsorb);
        bus.addListener(EntityInteractEvents::canDropItem);
        bus.addListener(EntityInteractEvents::mobGriefing);
        bus.addListener(EntityInteractEvents::entityLightningHit);

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

    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(PermissionManager.INSTANCE = new PermissionManager(event.getRegistryAccess()));
        event.addListener(InteractionOverrideManager.INSTANCE = new InteractionOverrideManager(event.getRegistryAccess()));
    }
}