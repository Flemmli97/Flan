package io.github.flemmli97.flan.forgeevent;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.ObjectToPermissionMap;
import io.github.flemmli97.flan.commands.CommandClaim;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

public class ServerEvents {

    public static void serverStart(FMLServerAboutToStartEvent event) {
        ConfigHandler.serverLoad(event.getServer());
        ObjectToPermissionMap.reload(event.getServer());
        Flan.lockRegistry(event.getServer());
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
}
