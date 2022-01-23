package io.github.flemmli97.flan;

import io.github.flemmli97.flan.commands.CommandClaim;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.event.BlockInteractEvents;
import io.github.flemmli97.flan.event.EntityInteractEvents;
import io.github.flemmli97.flan.event.ItemInteractEvents;
import io.github.flemmli97.flan.event.PlayerEvents;
import io.github.flemmli97.flan.event.WorldEvents;
import io.github.flemmli97.flan.integration.playerability.PlayerAbilityEvents;
import io.github.flemmli97.flan.player.PlayerDataHandler;
import io.github.flemmli97.flan.scoreboard.ClaimCriterias;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class FlanFabric implements ModInitializer {

    public static final Identifier EventPhase = new Identifier("flan", "events");

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.BEFORE.register(BlockInteractEvents::breakBlocks);
        AttackBlockCallback.EVENT.register(BlockInteractEvents::startBreakBlocks);
        UseBlockCallback.EVENT.addPhaseOrdering(EventPhase, Event.DEFAULT_PHASE);
        UseBlockCallback.EVENT.register(EventPhase, FlanFabric::useBlocks);
        UseEntityCallback.EVENT.register(EntityInteractEvents::useAtEntity);
        AttackEntityCallback.EVENT.register(EntityInteractEvents::attackEntity);
        UseItemCallback.EVENT.register(ItemInteractEvents::useItem);
        ServerLifecycleEvents.SERVER_STARTING.register(FlanFabric::serverLoad);
        ServerLifecycleEvents.SERVER_STARTED.register(FlanFabric::serverFinishLoad);
        ServerTickEvents.START_SERVER_TICK.register(WorldEvents::serverTick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PlayerEvents.onLogout(handler.player));
        CommandRegistrationCallback.EVENT.register(CommandClaim::register);

        Flan.permissionAPI = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
        Flan.gunpowder = FabricLoader.getInstance().isModLoaded("gunpowder-currency");
        Flan.playerAbilityLib = FabricLoader.getInstance().isModLoaded("playerabilitylib");
        Flan.ftbRanks = FabricLoader.getInstance().isModLoaded("ftbranks");
        if (Flan.playerAbilityLib)
            PlayerAbilityEvents.register();
        ClaimCriterias.init();
    }

    public static void serverLoad(MinecraftServer server) {
        Flan.lockRegistry(server);
        ConfigHandler.serverLoad(server);
    }

    public static void serverFinishLoad(MinecraftServer server) {
        PlayerDataHandler.deleteInactivePlayerData(server);
    }

    public static ActionResult useBlocks(PlayerEntity p, World world, Hand hand, BlockHitResult hitResult) {
        if (p instanceof ServerPlayerEntity) {
            ItemUseBlockFlags flags = ItemUseBlockFlags.fromPlayer((ServerPlayerEntity) p);
            ActionResult res = BlockInteractEvents.useBlocks(p, world, hand, hitResult);
            if (res == ActionResult.SUCCESS)
                return res;
            flags.stopCanUseBlocks(res == ActionResult.FAIL);
            flags.stopCanUseItems(ItemInteractEvents.onItemUseBlock(new ItemUsageContext(p, hand, hitResult)) == ActionResult.FAIL);
        }
        return ActionResult.PASS;
    }
}
