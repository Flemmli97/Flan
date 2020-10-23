package com.flemmli97.flan;

import com.flemmli97.flan.claim.BlockToPermissionMap;
import com.flemmli97.flan.commands.CommandClaim;
import com.flemmli97.flan.config.ConfigHandler;
import com.flemmli97.flan.event.BlockInteractEvents;
import com.flemmli97.flan.event.EntityInteractEvents;
import com.flemmli97.flan.event.ItemInteractEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Flan implements ModInitializer {

    public static final Logger logger = LogManager.getLogger("flan");

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.BEFORE.register(BlockInteractEvents::breakBlocks);
        UseBlockCallback.EVENT.register(BlockInteractEvents::useBlocks);
        UseEntityCallback.EVENT.register(EntityInteractEvents::useAtEntity);
        AttackEntityCallback.EVENT.register(EntityInteractEvents::attackEntity);
        UseItemCallback.EVENT.register(ItemInteractEvents::useItem);
        ServerLifecycleEvents.SERVER_STARTING.register(ConfigHandler::serverLoad);
        ServerLifecycleEvents.SERVER_STARTING.register(BlockToPermissionMap::reload);

        CommandRegistrationCallback.EVENT.register(CommandClaim::register);
    }

    public static void log(String msg, Object... o){
        logger.info(msg, o);
    }

}
