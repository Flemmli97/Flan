package io.github.flemmli97.flan.forge.forgeevent;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.commands.CommandClaim;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.event.PlayerEvents;
import io.github.flemmli97.flan.platform.integration.webmap.BluemapIntegration;
import io.github.flemmli97.flan.player.LogoutTracker;
import io.github.flemmli97.flan.player.PlayerDataHandler;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.ModList;

public class ServerEvents {

    public static void serverStart(ServerAboutToStartEvent event) {
        Flan.lockRegistry(event.getServer());
        ConfigHandler.serverLoad(event.getServer());

        if (ModList.get().isLoaded("bluemap"))
            BluemapIntegration.reg(event.getServer());
    }

    public static void serverFinishLoad(ServerStartedEvent event) {
        PlayerDataHandler.deleteInactivePlayerData(event.getServer());
    }

    public static void commands(RegisterCommandsEvent event) {
        CommandClaim.register(event.getDispatcher(), false);
    }

    public static void savePlayer(PlayerEvent.SaveToFile event) {
        PlayerEvents.saveClaimData(event.getPlayer());
    }

    public static void readPlayer(PlayerEvent.LoadFromFile event) {
        PlayerEvents.readClaimData(event.getPlayer());
    }

    public static void disconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerEvents.onLogout(event.getPlayer());
    }

    public static void serverTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.world.getServer() != null && event.world.dimension() == Level.OVERWORLD)
            LogoutTracker.getInstance(event.world.getServer()).tick();
    }
}
