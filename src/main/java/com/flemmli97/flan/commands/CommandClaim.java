package com.flemmli97.flan.commands;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.ClaimStorage;
import com.flemmli97.flan.claim.EnumPermission;
import com.flemmli97.flan.config.ConfigHandler;
import com.flemmli97.flan.gui.ClaimMenuScreenHandler;
import com.flemmli97.flan.player.EnumEditMode;
import com.flemmli97.flan.player.PlayerClaimData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommandClaim {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {

        LiteralArgumentBuilder<ServerCommandSource> main = CommandManager.literal("claim");
        dispatcher.register(addToMainCommand(CommandManager.literal("claim"),
                CommandManager.literal("menu").executes(context -> {
                    ServerCommandSource src = context.getSource();
                    if (src.getPlayer() == null)
                        return 0;
                    ClaimStorage storage = ClaimStorage.get(context.getSource().getWorld());
                    Claim claim = storage.getClaimAt(src.getPlayer().getBlockPos());
                    if (claim == null || !claim.canInteract(src.getPlayer(), EnumPermission.EDITCLAIM, src.getPlayer().getBlockPos()))
                        return 0;
                    ClaimMenuScreenHandler.openClaimMenu(src.getPlayer(), claim);
                    return Command.SINGLE_SUCCESS;
                }),
                CommandManager.literal("delete").executes(context -> {
                    ServerCommandSource src = context.getSource();
                    if (src.getPlayer() == null)
                        return 0;
                    ClaimStorage storage = ClaimStorage.get(src.getWorld());
                    Claim claim = storage.getClaimAt(src.getPlayer().getBlockPos());
                    if (claim == null || !claim.canInteract(src.getPlayer(), EnumPermission.EDITCLAIM, src.getPlayer().getBlockPos())) {
                        src.getPlayer().sendMessage(Text.of(ConfigHandler.lang.deleteClaimError), false);
                        return 0;
                    }
                    storage.deleteClaim(claim);
                    src.getPlayer().sendMessage(Text.of(ConfigHandler.lang.deleteClaim), false);

                    return Command.SINGLE_SUCCESS;
                }),
                CommandManager.literal("deleteAll").executes(context -> {
                    ServerCommandSource src = context.getSource();
                    if (src.getPlayer() == null)
                        return 0;
                    PlayerClaimData data = PlayerClaimData.get(src.getPlayer());
                    if (data.confirmedDeleteAll()) {
                        for (ServerWorld world : src.getWorld().getServer().getWorlds()) {
                            ClaimStorage storage = ClaimStorage.get(world);
                            storage.allClaimsFromPlayer(src.getPlayer().getUuid()).forEach(claim -> storage.deleteClaim(claim));
                        }
                        src.getPlayer().sendMessage(Text.of(ConfigHandler.lang.deleteAllClaim), false);
                        data.setConfirmDeleteAll(false);
                    } else {
                        data.setConfirmDeleteAll(true);
                        src.getPlayer().sendMessage(Text.of(ConfigHandler.lang.deleteAllClaimConfirm), false);
                    }
                    return Command.SINGLE_SUCCESS;
                }),
                CommandManager.literal("list").executes(context -> {
                    ServerCommandSource src = context.getSource();
                    if (src.getPlayer() == null)
                        return 0;
                    Map<World, Collection<Claim>> claims = Maps.newHashMap();
                    for (ServerWorld world : src.getWorld().getServer().getWorlds()) {
                        ClaimStorage storage = ClaimStorage.get(world);
                        claims.put(world, storage.allClaimsFromPlayer(src.getPlayer().getUuid()));
                    }
                    src.getPlayer().sendMessage(Text.of(ConfigHandler.lang.listClaims), false);
                    for (Map.Entry<World, Collection<Claim>> entry : claims.entrySet())
                        for (Claim claim : entry.getValue())
                            src.getPlayer().sendMessage(Text.of(entry.getKey().getRegistryKey().getValue().toString() + " - " + claim.formattedClaim()), false);
                    return Command.SINGLE_SUCCESS;
                }),
                CommandManager.literal("switchMode").executes(context -> {
                    ServerCommandSource src = context.getSource();
                    if (src.getPlayer() == null)
                        return 0;
                    PlayerClaimData data = PlayerClaimData.get(src.getPlayer());
                    data.setEditMode(data.getEditMode() == EnumEditMode.DEFAULT ? EnumEditMode.SUBCLAIM : EnumEditMode.DEFAULT);
                    src.getPlayer().sendMessage(Text.of(String.format(ConfigHandler.lang.editMode, data.getEditMode())), false);
                    return Command.SINGLE_SUCCESS;
                }),
                CommandManager.literal("adminMode").requires(src -> src.hasPermissionLevel(2)).executes(context -> {
                    ServerCommandSource src = context.getSource();
                    if (src.getPlayer() == null)
                        return 0;
                    PlayerClaimData data = PlayerClaimData.get(src.getPlayer());
                    data.setAdminIgnoreClaim(!data.isAdminIgnoreClaim());
                    src.getPlayer().sendMessage(Text.of(String.format(ConfigHandler.lang.adminMode, data.isAdminIgnoreClaim())), false);
                    return Command.SINGLE_SUCCESS;
                }),
                CommandManager.literal("adminRemove").requires(src -> src.hasPermissionLevel(2)).executes(context -> {
                    ServerCommandSource src = context.getSource();
                    ClaimStorage storage = ClaimStorage.get(src.getWorld());
                    Claim claim = storage.getClaimAt(new BlockPos(src.getPosition()));
                    if (claim == null) {
                        src.sendFeedback(Text.of(ConfigHandler.lang.deleteClaimError), false);
                        return 0;
                    }
                    storage.deleteClaim(claim);
                    src.sendFeedback(Text.of(ConfigHandler.lang.deleteClaim), true);
                    return Command.SINGLE_SUCCESS;
                }).then(CommandManager.literal("all").then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())).executes(context -> {
                    ServerCommandSource src = context.getSource();
                    Iterator<GameProfile> it = GameProfileArgumentType.getProfileArgument(context, "player").iterator();
                    List<String> players = Lists.newArrayList();
                    while (it.hasNext()) {
                        GameProfile prof = it.next();
                        for (ServerWorld world : src.getWorld().getServer().getWorlds()) {
                            ClaimStorage storage = ClaimStorage.get(world);
                            storage.allClaimsFromPlayer(prof.getId()).forEach(claim -> storage.deleteClaim(claim));
                        }
                        players.add(prof.getName());
                    }
                    src.sendFeedback(Text.of(String.format(ConfigHandler.lang.adminDeleteAll, players.toString())), true);
                    return Command.SINGLE_SUCCESS;
                })),
                CommandManager.literal("giveClaimBlocks").requires(src -> src.hasPermissionLevel(2)).then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer()).executes(context -> {
                            ServerCommandSource src = context.getSource();
                            Iterator<GameProfile> it = GameProfileArgumentType.getProfileArgument(context, "player").iterator();
                            List<String> players = Lists.newArrayList();
                            int amount = IntegerArgumentType.getInteger(context, "amount");
                            while (it.hasNext()) {
                                GameProfile prof = it.next();
                                ServerPlayerEntity player = src.getMinecraftServer().getPlayerManager().getPlayer(prof.getId());
                                if (player != null) {
                                    PlayerClaimData data = PlayerClaimData.get(player);
                                    data.setAdditionalClaims(data.getAdditionalClaims() + amount);
                                } else
                                    PlayerClaimData.editForOfflinePlayer(src.getMinecraftServer(), prof.getId(), amount);
                                players.add(prof.getName());
                            }
                            src.sendFeedback(Text.of(String.format(ConfigHandler.lang.giveClaimBlocks, players.toString(), amount)), true);
                            return Command.SINGLE_SUCCESS;
                        }))),
                addToMainCommand(CommandManager.literal("group"),
                        CommandManager.literal("add").then(CommandManager.argument("name", StringArgumentType.word()).executes(context -> {
                            ServerCommandSource src = context.getSource();
                            String group = StringArgumentType.getString(context, "name");
                            if (src.getPlayer() == null)
                                return 0;
                            ClaimStorage storage = ClaimStorage.get(src.getWorld());
                            Claim claim = storage.getClaimAt(src.getPlayer().getBlockPos());
                            if (claim == null || !claim.canInteract(src.getPlayer(), EnumPermission.EDITCLAIM, src.getPlayer().getBlockPos())) {
                                src.getPlayer().sendMessage(Text.of(ConfigHandler.lang.deleteClaimError), false);
                                return 0;
                            }
                            claim.editPerms(src.getPlayer(), group, EnumPermission.EDITCLAIM, -1);
                            src.getPlayer().sendMessage(Text.of(ConfigHandler.lang.deleteClaim), false);
                            return Command.SINGLE_SUCCESS;
                        })),
                        CommandManager.literal("remove").then(CommandManager.argument("name", StringArgumentType.word()).suggests((context, build) -> {
                            List<String> list = Lists.newArrayList();
                            ServerCommandSource src = context.getSource();
                            String group = StringArgumentType.getString(context, "name");
                            if (src.getPlayer() != null) {
                                ClaimStorage storage = ClaimStorage.get(src.getWorld());
                                Claim claim = storage.getClaimAt(src.getPlayer().getBlockPos());
                                if (claim != null && claim.canInteract(src.getPlayer(), EnumPermission.EDITCLAIM, src.getPlayer().getBlockPos())) {
                                    list = claim.groups();
                                }
                            }
                            return CommandSource.suggestMatching(list, build);
                        }).executes(context -> {
                            ServerCommandSource src = context.getSource();
                            String group = StringArgumentType.getString(context, "name");
                            if (src.getPlayer() == null)
                                return 0;
                            ClaimStorage storage = ClaimStorage.get(src.getWorld());
                            Claim claim = storage.getClaimAt(src.getPlayer().getBlockPos());
                            if (claim == null || !claim.canInteract(src.getPlayer(), EnumPermission.EDITCLAIM, src.getPlayer().getBlockPos())) {
                                src.getPlayer().sendMessage(Text.of(ConfigHandler.lang.deleteClaimError), false);
                                return 0;
                            }
                            claim.removePermGroup(src.getPlayer(), group);
                            src.getPlayer().sendMessage(Text.of(ConfigHandler.lang.deleteClaim), false);
                            return Command.SINGLE_SUCCESS;
                        })),
                        CommandManager.literal("player").then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())).executes(context -> {
                            ServerCommandSource src = context.getSource();
                            GameProfileArgumentType.getProfileArgument(context, "player");
                            if (src.getPlayer() == null)
                                return 0;

                            //print
                            return 0;
                        })
                )));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> addToMainCommand(LiteralArgumentBuilder<ServerCommandSource> main, ArgumentBuilder... other) {
        if (other != null)
            for (ArgumentBuilder o : other)
                main.then(o);
        return main;
    }

}
