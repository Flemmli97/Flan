package com.flemmli97.flan.commands;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.ClaimStorage;
import com.flemmli97.flan.claim.EnumPermission;
import com.flemmli97.flan.claim.PermHelper;
import com.flemmli97.flan.config.ConfigHandler;
import com.flemmli97.flan.gui.ClaimMenuScreenHandler;
import com.flemmli97.flan.player.EnumDisplayType;
import com.flemmli97.flan.player.EnumEditMode;
import com.flemmli97.flan.player.PlayerClaimData;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CommandClaim {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(addToMainCommand(CommandManager.literal("flan"),
                CommandManager.literal("reload").executes(CommandClaim::reloadConfig),
                CommandManager.literal("menu").executes(CommandClaim::openMenu),
                CommandManager.literal("claimInfo").executes(CommandClaim::claimInfo),
                CommandManager.literal("delete").executes(CommandClaim::deleteClaim),
                CommandManager.literal("deleteAll").executes(CommandClaim::deleteAllClaim),
                CommandManager.literal("deleteSubClaim").executes(CommandClaim::deleteSubClaim),
                CommandManager.literal("deleteAllSubClaims").executes(CommandClaim::deleteAllSubClaim),
                CommandManager.literal("list").executes(CommandClaim::listClaims),
                CommandManager.literal("switchMode").executes(CommandClaim::switchClaimMode),
                CommandManager.literal("adminMode").requires(src -> src.hasPermissionLevel(2)).executes(CommandClaim::switchAdminMode),
                CommandManager.literal("readGriefPrevention").requires(src -> src.hasPermissionLevel(2)).executes(CommandClaim::readGriefPreventionData),
                CommandManager.literal("setAdminClaim").requires(src -> src.hasPermissionLevel(2)).executes(CommandClaim::setAdminClaim),
                CommandManager.literal("listAdminClaims").requires(src -> src.hasPermissionLevel(2)).executes(CommandClaim::listAdminClaims),
                CommandManager.literal("adminDelete").requires(src -> src.hasPermissionLevel(2)).executes(CommandClaim::adminDelete)
                        .then(CommandManager.literal("all").then(CommandManager.argument("players", GameProfileArgumentType.gameProfile())
                                .executes(CommandClaim::adminDeleteAll))),
                CommandManager.literal("giveClaimBlocks").requires(src -> src.hasPermissionLevel(2)).then(CommandManager.argument("players", GameProfileArgumentType.gameProfile())
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer()).executes(CommandClaim::giveClaimBlocks))),
                addToMainCommand(CommandManager.literal("group"),
                        CommandManager.literal("add").then(CommandManager.argument("group", StringArgumentType.word()).executes(CommandClaim::addGroup)),
                        CommandManager.literal("remove").then(CommandManager.argument("group", StringArgumentType.word())
                                .suggests(CommandClaim::groupSuggestion).executes(CommandClaim::removeGroup)),
                        addToMainCommand(CommandManager.literal("players"),
                                CommandManager.literal("add").then(CommandManager.argument("group", StringArgumentType.word()).suggests(CommandClaim::groupSuggestion)
                                        .then(CommandManager.argument("players", GameProfileArgumentType.gameProfile()).executes(CommandClaim::addPlayer)
                                                .then(CommandManager.literal("overwrite").executes(CommandClaim::forceAddPlayer)))),
                                CommandManager.literal("remove").then(CommandManager.argument("group", StringArgumentType.word()).suggests(CommandClaim::groupSuggestion)
                                        .then(CommandManager.argument("players", GameProfileArgumentType.gameProfile()).suggests((context, build) -> {
                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                            List<String> list = Lists.newArrayList();
                                            ServerCommandSource src = context.getSource();
                                            ClaimStorage storage = ClaimStorage.get(src.getWorld());
                                            Claim claim = storage.getClaimAt(src.getPlayer().getBlockPos());
                                            if (claim != null && claim.canInteract(src.getPlayer(), EnumPermission.EDITCLAIM, src.getPlayer().getBlockPos())) {
                                                list = claim.playersFromGroup(player.getServer(), "");
                                            }
                                            return CommandSource.suggestMatching(list, build);
                                        }).executes(CommandClaim::removePlayer))))
                )));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> addToMainCommand(LiteralArgumentBuilder<ServerCommandSource> main, ArgumentBuilder... other) {
        if (other != null)
            for (ArgumentBuilder o : other)
                main.then(o);
        return main;
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        ConfigHandler.reloadConfigs();
        context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.configReload), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int openMenu(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.getEditMode() == EnumEditMode.DEFAULT) {
            Claim claim = PermHelper.checkReturn(player, EnumPermission.EDITPERMS, PermHelper.genericNoPermMessage(player));
            if (claim == null)
                return 0;
            ClaimMenuScreenHandler.openClaimMenu(player, claim);
            data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
        } else {
            Claim claim = ClaimStorage.get(player.getServerWorld()).getClaimAt(player.getBlockPos());
            Claim sub = claim.getSubClaim(player.getBlockPos());
            if (sub != null && (claim.canInteract(player, EnumPermission.EDITPERMS, player.getBlockPos()) || sub.canInteract(player, EnumPermission.EDITPERMS, player.getBlockPos())))
                ClaimMenuScreenHandler.openClaimMenu(player, sub);
            else if (claim.canInteract(player, EnumPermission.EDITPERMS, player.getBlockPos()))
                ClaimMenuScreenHandler.openClaimMenu(player, claim);
            else
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noPermission, Formatting.DARK_RED), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int claimInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Claim claim = ClaimStorage.get(player.getServerWorld()).getClaimAt(player.getBlockPos());
        PlayerClaimData data = PlayerClaimData.get(player);
        if (claim == null)
            return 0;
        if (data.getEditMode() == EnumEditMode.SUBCLAIM) {
            Claim sub = claim.getSubClaim(player.getBlockPos());
            if (sub != null) {
                List<Text> info = sub.infoString(player);
                player.sendMessage(PermHelper.simpleColoredText("==SubclaimInfo==", Formatting.AQUA), false);
                for (Text text : info)
                    player.sendMessage(text, false);
                return Command.SINGLE_SUCCESS;
            }
        }
        List<Text> info = claim.infoString(player);
        for (Text text : info)
            player.sendMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
        Claim claim = storage.getClaimAt(player.getBlockPos());
        boolean check = PermHelper.check(player, player.getBlockPos(), claim, EnumPermission.EDITCLAIM, b -> {
            if (!b.isPresent())
                PermHelper.noClaimMessage(player);
            else if (!b.get())
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.deleteClaimError, Formatting.DARK_RED), false);
            else
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.deleteClaim, Formatting.RED), false);
        });
        if (!check)
            return 0;
        storage.deleteClaim(claim, true, PlayerClaimData.get(player).getEditMode(), player.getServerWorld());
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteAllClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.confirmedDeleteAll()) {
            for (ServerWorld world : player.getServer().getWorlds()) {
                ClaimStorage storage = ClaimStorage.get(world);
                storage.allClaimsFromPlayer(player.getUuid()).forEach((claim) -> storage.deleteClaim(claim, true, PlayerClaimData.get(player).getEditMode(), player.getServerWorld()));
            }
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.deleteAllClaim, Formatting.GOLD), false);
            data.setConfirmDeleteAll(false);
        } else {
            data.setConfirmDeleteAll(true);
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.deleteAllClaimConfirm, Formatting.DARK_RED), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteSubClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
        Claim claim = storage.getClaimAt(player.getBlockPos());
        if (claim == null) {
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noClaim, Formatting.RED), false);
            return 0;
        }
        Claim sub = claim.getSubClaim(player.getBlockPos());
        if (sub == null) {
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noClaim, Formatting.RED), false);
            return 0;
        }
        boolean check = PermHelper.check(player, player.getBlockPos(), sub, EnumPermission.EDITCLAIM, b -> {
            if (!b.isPresent())
                PermHelper.noClaimMessage(player);
            else if (!b.get())
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.deleteClaimError, Formatting.DARK_RED), false);
            else
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.deleteSubClaim, Formatting.DARK_RED), false);
        });
        if (!check)
            return 0;
        claim.deleteSubClaim(sub);
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteAllSubClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Claim claim = PermHelper.checkReturn(player, EnumPermission.EDITCLAIM, PermHelper.genericNoPermMessage(player));
        if (claim == null)
            return 0;
        List<Claim> subs = claim.getAllSubclaims();
        subs.forEach(claim::deleteSubClaim);
        player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.deleteSubClaimAll, Formatting.DARK_RED), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int listClaims(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Map<World, Collection<Claim>> claims = Maps.newHashMap();
        for (ServerWorld world : player.getServer().getWorlds()) {
            ClaimStorage storage = ClaimStorage.get(world);
            claims.put(world, storage.allClaimsFromPlayer(player.getUuid()));
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.claimBlocksFormat,
                data.getClaimBlocks(), data.getAdditionalClaims(), data.usedClaimBlocks()), Formatting.GOLD), false);
        player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.listClaims, Formatting.GOLD), false);
        for (Map.Entry<World, Collection<Claim>> entry : claims.entrySet())
            for (Claim claim : entry.getValue())
                player.sendMessage(PermHelper.simpleColoredText(
                        entry.getKey().getRegistryKey().getValue().toString() + " # " + claim.formattedClaim(), Formatting.YELLOW), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int switchClaimMode(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerClaimData data = PlayerClaimData.get(player);
        data.setEditMode(data.getEditMode() == EnumEditMode.DEFAULT ? EnumEditMode.SUBCLAIM : EnumEditMode.DEFAULT);
        player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.editMode, data.getEditMode()), Formatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int switchAdminMode(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerClaimData data = PlayerClaimData.get(player);
        data.setAdminIgnoreClaim(!data.isAdminIgnoreClaim());
        player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.adminMode, data.isAdminIgnoreClaim()), Formatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int adminDelete(CommandContext<ServerCommandSource> context) {
        ServerCommandSource src = context.getSource();
        ClaimStorage storage = ClaimStorage.get(src.getWorld());
        Claim claim = storage.getClaimAt(new BlockPos(src.getPosition()));
        if (claim == null) {
            src.sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.noClaim, Formatting.RED), false);
            return 0;
        }
        if (src.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) src.getEntity();
            PlayerClaimData data = PlayerClaimData.get(player);
            if (!data.confirmedDeleteAll()) {
                data.setConfirmDeleteAll(true);
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.deleteAllClaimConfirm, Formatting.DARK_RED), false);
                return Command.SINGLE_SUCCESS;
            }
        }
        storage.deleteClaim(claim, true, EnumEditMode.DEFAULT, src.getWorld());
        src.sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.deleteClaim, Formatting.RED), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int adminDeleteAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource src = context.getSource();
        if (src.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) src.getEntity();
            PlayerClaimData data = PlayerClaimData.get(player);
            if (!data.confirmedDeleteAll()) {
                data.setConfirmDeleteAll(true);
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.deleteAllClaimConfirm, Formatting.DARK_RED), false);
                return Command.SINGLE_SUCCESS;
            }
        }
        List<String> players = Lists.newArrayList();
        for (GameProfile prof : GameProfileArgumentType.getProfileArgument(context, "players")) {
            for (ServerWorld world : src.getWorld().getServer().getWorlds()) {
                ClaimStorage storage = ClaimStorage.get(world);
                storage.allClaimsFromPlayer(prof.getId()).forEach((claim) -> storage.deleteClaim(claim, true, EnumEditMode.DEFAULT, world));
            }
            players.add(prof.getName());
        }
        src.sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.adminDeleteAll, players.toString()), Formatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setAdminClaim(CommandContext<ServerCommandSource> context) {
        ServerCommandSource src = context.getSource();
        ClaimStorage storage = ClaimStorage.get(src.getWorld());
        Claim claim = storage.getClaimAt(new BlockPos(src.getPosition()));
        if (claim == null) {
            src.sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.noClaim, Formatting.RED), false);
            return 0;
        }
        claim.setAdminClaim();
        src.sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.setAdminClaim, Formatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int listAdminClaims(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource src = context.getSource();
        Collection<Claim> claims = ClaimStorage.get(src.getWorld()).getAdminClaims();
        src.sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.listAdminClaims, src.getWorld().getRegistryKey().getValue()), Formatting.GOLD), false);
        for (Claim claim : claims)
            src.sendFeedback(PermHelper.simpleColoredText(claim.formattedClaim(), Formatting.YELLOW), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int readGriefPreventionData(CommandContext<ServerCommandSource> context) {
        ServerCommandSource src = context.getSource();
        src.sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.readGriefpreventionData, Formatting.GOLD), true);
        Set<String> errors = ClaimStorage.readGriefPreventionData(src.getMinecraftServer());
        PlayerClaimData.readGriefPreventionPlayerData(src.getMinecraftServer());
        if (errors == null)
            src.sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.readGriefpreventionDataSuccess, Formatting.GOLD), true);
        else
            src.sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.readGriefpreventionDataFail, errors), Formatting.RED), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int giveClaimBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource src = context.getSource();
        List<String> players = Lists.newArrayList();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (GameProfile prof : GameProfileArgumentType.getProfileArgument(context, "players")) {
            ServerPlayerEntity player = src.getMinecraftServer().getPlayerManager().getPlayer(prof.getId());
            if (player != null) {
                PlayerClaimData data = PlayerClaimData.get(player);
                data.setAdditionalClaims(data.getAdditionalClaims() + amount);
            } else
                PlayerClaimData.editForOfflinePlayer(src.getMinecraftServer(), prof.getId(), amount);
            players.add(prof.getName());
        }
        src.sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.giveClaimBlocks, players.toString(), amount), Formatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> groupSuggestion(CommandContext<ServerCommandSource> context, SuggestionsBuilder build) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        List<String> list = Lists.newArrayList();
        ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
        Claim claim = storage.getClaimAt(player.getBlockPos());
        if (claim != null && claim.canInteract(player, EnumPermission.EDITCLAIM, player.getBlockPos())) {
            list = claim.groups();
        }
        return CommandSource.suggestMatching(list, build);
    }

    private static int addGroup(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return modifyGroup(context, false);

    }

    private static int removeGroup(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return modifyGroup(context, true);
    }

    private static int modifyGroup(CommandContext<ServerCommandSource> context, boolean remove) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String group = StringArgumentType.getString(context, "group");
        ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
        Claim claim = storage.getClaimAt(player.getBlockPos());
        if (claim == null) {
            PermHelper.noClaimMessage(player);
            return 0;
        }
        if (remove) {
            if (claim.removePermGroup(player, group))
                player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.groupRemove, group), Formatting.GOLD), false);
            else {
                PermHelper.genericNoPermMessage(player);
                return 0;
            }
        } else {
            if (claim.groups().contains(group)) {
                player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.groupExist, group), Formatting.RED), false);
                return 0;
            } else if (claim.editPerms(player, group, EnumPermission.EDITCLAIM, -1))
                player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.groupAdd, group), Formatting.GOLD), false);
            else {
                PermHelper.genericNoPermMessage(player);
                return 0;
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int forceAddPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String group = StringArgumentType.getString(context, "group");
        return modifyPlayer(context, group, true);
    }

    private static int addPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String group = StringArgumentType.getString(context, "group");
        return modifyPlayer(context, group, false);
    }

    private static int removePlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return modifyPlayer(context, null, false);
    }

    private static int modifyPlayer(CommandContext<ServerCommandSource> context, String group, boolean force) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
        Claim claim = storage.getClaimAt(player.getBlockPos());
        if (claim == null) {
            PermHelper.noClaimMessage(player);
            return 0;
        }
        if (!claim.canInteract(player, EnumPermission.EDITPERMS, player.getBlockPos())) {
            PermHelper.genericNoPermMessage(player);
            return 0;
        }
        List<String> modified = Lists.newArrayList();
        for (GameProfile prof : GameProfileArgumentType.getProfileArgument(context, "players")) {
            if (claim.setPlayerGroup(prof.getId(), group, force))
                modified.add(prof.getName());
        }
        if (!modified.isEmpty())
            player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.playerModify, group, modified), Formatting.GOLD), false);
        else
            player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.playerModifyNo, group, modified), Formatting.RED), false);
        return Command.SINGLE_SUCCESS;
    }
}
