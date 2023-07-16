package io.github.flemmli97.flan.fabric;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.fabric.ItemUseBlockFlags;
import io.github.flemmli97.flan.commands.CommandClaim;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.event.BlockInteractEvents;
import io.github.flemmli97.flan.event.EntityInteractEvents;
import io.github.flemmli97.flan.event.ItemInteractEvents;
import io.github.flemmli97.flan.event.PlayerEvents;
import io.github.flemmli97.flan.event.WorldEvents;
import io.github.flemmli97.flan.fabric.integration.HarvestWithEase;
import io.github.flemmli97.flan.fabric.platform.integration.claiming.FlanProtectionProvider;
import io.github.flemmli97.flan.fabric.platform.integration.playerability.PlayerAbilityEvents;
import io.github.flemmli97.flan.platform.integration.webmap.BluemapIntegration;
import io.github.flemmli97.flan.platform.integration.webmap.DynmapIntegration;
import io.github.flemmli97.flan.player.PlayerDataHandler;
import io.github.flemmli97.flan.scoreboard.ClaimCriterias;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class FlanFabric implements ModInitializer {

    public static final ResourceLocation EventPhase = new ResourceLocation("flan", "events");

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
        CommandRegistrationCallback.EVENT.register((dispatcher, reg, env) -> CommandClaim.register(dispatcher, env == Commands.CommandSelection.DEDICATED));

        Flan.permissionAPI = FabricLoader.getInstance()
                .getModContainer("fabric-permissions-api-v0")
                .map(c -> c.getMetadata().getVersion().getFriendlyString().equals("0.2-SNAPSHOT")).orElse(false);
        Flan.playerAbilityLib = FabricLoader.getInstance().isModLoaded("playerabilitylib");
        Flan.ftbRanks = FabricLoader.getInstance().isModLoaded("ftbranks");
        Flan.octoEconomy = FabricLoader.getInstance().isModLoaded("octo-economy-api");
        Flan.diamondCurrency = FabricLoader.getInstance().isModLoaded("diamondeconomy");
        Flan.ftbChunks = FabricLoader.getInstance().isModLoaded("ftbchunks");
        Flan.gomlServer = FabricLoader.getInstance().isModLoaded("goml");
        Flan.commonProtApi = FabricLoader.getInstance().isModLoaded("common-protection-api");

        if (Flan.playerAbilityLib)
            PlayerAbilityEvents.register();
        if (FabricLoader.getInstance().isModLoaded("dynmap"))
            DynmapIntegration.reg();
        if (FabricLoader.getInstance().isModLoaded("harvestwithease"))
            HarvestWithEase.init();
        if (Flan.commonProtApi)
            FlanProtectionProvider.register();
        ClaimCriterias.init();
    }

    public static void serverLoad(MinecraftServer server) {
        Flan.lockRegistry(server);
        ConfigHandler.serverLoad(server);
        if (FabricLoader.getInstance().isModLoaded("bluemap"))
            BluemapIntegration.reg(server);
    }

    public static void serverFinishLoad(MinecraftServer server) {
        PlayerDataHandler.deleteInactivePlayerData(server);
    }

    public static InteractionResult useBlocks(Player p, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (p instanceof ServerPlayer serverPlayer) {
            ItemUseBlockFlags flags = ItemUseBlockFlags.fromPlayer(serverPlayer);
            InteractionResult res = BlockInteractEvents.useBlocks(p, world, hand, hitResult);
            if (res == InteractionResult.SUCCESS)
                return res;
            flags.stopCanUseBlocks(res == InteractionResult.FAIL);
            flags.stopCanUseItems(ItemInteractEvents.onItemUseBlock(new UseOnContext(p, hand, hitResult)) == InteractionResult.FAIL);
            if (!flags.allowUseBlocks() && !flags.allowUseItems())
                return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}
