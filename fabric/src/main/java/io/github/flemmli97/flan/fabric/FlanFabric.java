package io.github.flemmli97.flan.fabric;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.fabric.ItemUseBlockFlags;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.api.permission.PermissionManager;
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
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class FlanFabric implements ModInitializer {

    public static final Identifier EVENT_PHASE = Identifier.fromNamespaceAndPath("flan", "events");

    @Override
    public void onInitialize() {
        applyPriorityListener(PlayerBlockBreakEvents.BEFORE, BlockInteractEvents::breakBlocks);
        applyPriorityListener(AttackBlockCallback.EVENT, BlockInteractEvents::startBreakBlocks);
        applyPriorityListener(UseBlockCallback.EVENT, FlanFabric::useBlocks);
        applyPriorityListener(UseEntityCallback.EVENT, ((player, world, hand, entity, hitResult) -> {
            if (hitResult != null)
                return EntityInteractEvents.useAtEntity(player, world, hand, entity, null);
            return EntityInteractEvents.useEntity(player, world, hand, entity);
        }));
        applyPriorityListener(UseItemCallback.EVENT, ItemInteractEvents::useItem);

        ServerLifecycleEvents.SERVER_STARTING.register(FlanFabric::serverLoad);
        ServerLifecycleEvents.SERVER_STARTED.register(FlanFabric::serverFinishLoad);
        ServerTickEvents.START_SERVER_TICK.register(WorldEvents::serverTick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PlayerEvents.onLogout(handler.player));
        CommandRegistrationCallback.EVENT.register((dispatcher, reg, env) -> CommandClaim.register(dispatcher, reg, env == Commands.CommandSelection.DEDICATED));

        DataResourceLoader.get().registerReloadListener(PermissionManager.ID.identifier(), PermissionManager::create);
        DataResourceLoader.get().registerReloadListener(InteractionOverrideManager.ID.identifier(), InteractionOverrideManager::create);

        Flan.permissionAPI = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
        Flan.playerAbilityLib = FabricLoader.getInstance().isModLoaded("playerabilitylib");
        Flan.ftbRanks = FabricLoader.getInstance().isModLoaded("ftbranks");
        Flan.diamondCurrency = FabricLoader.getInstance().isModLoaded("diamondeconomy");
        Flan.ftbChunks = FabricLoader.getInstance().isModLoaded("ftbchunks");
        Flan.gomlServer = FabricLoader.getInstance().isModLoaded("goml");
        Flan.commonProtApi = FabricLoader.getInstance().isModLoaded("common-protection-api");
        Flan.impactor = FabricLoader.getInstance().isModLoaded("impactor");
        Flan.create = FabricLoader.getInstance().isModLoaded("create");
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

    private static <T> void applyPriorityListener(Event<T> event, T listener) {
        event.addPhaseOrdering(EVENT_PHASE, Event.DEFAULT_PHASE);
        event.register(EVENT_PHASE, listener);
    }

    public static void serverLoad(MinecraftServer server) {
        ConfigHandler.reloadConfigs(server);
        if (FabricLoader.getInstance().isModLoaded("bluemap"))
            BluemapIntegration.reg(server);
    }

    public static void serverFinishLoad(MinecraftServer server) {
        PlayerDataHandler.deleteInactivePlayerData(server);
    }

    public static InteractionResult useBlocks(Player p, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (p instanceof ServerPlayer serverPlayer) {
            ItemUseBlockFlags flags = ItemUseBlockFlags.fromPlayer(serverPlayer);
            InteractionResult res = BlockInteractEvents.useBlocks(p, level, hand, hitResult);
            if (res == InteractionResult.SUCCESS)
                return res;
            flags.flan$stopCanUseBlocks(res == InteractionResult.FAIL);
            flags.flan$stopCanUseItems(ItemInteractEvents.onItemUseBlock(new UseOnContext(p, hand, hitResult), res != InteractionResult.FAIL) == InteractionResult.FAIL);
            if (!flags.flan$allowUseBlocks() && !flags.flan$allowUseItems())
                return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}
