package io.github.flemmli97.flan.commands.sub;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.commands.CommandHelpers;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class ClaimGroupCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("group").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_GROUP))
                .then(Commands.literal("add").then(Commands.argument("group", StringArgumentType.string()).executes(ClaimGroupCommand::addGroup)))
                .then(Commands.literal("remove").then(Commands.argument("group", StringArgumentType.string())
                        .suggests(CommandHelpers::groupSuggestion).executes(ClaimGroupCommand::removeGroup)))
                .then(Commands.literal("players")
                        .then(Commands.literal("add").then(Commands.argument("group", StringArgumentType.word()).suggests(CommandHelpers::groupSuggestion)
                                .then(Commands.argument("players", GameProfileArgument.gameProfile()).executes(ClaimGroupCommand::addPlayer)
                                        .then(Commands.literal("overwrite").executes(ClaimGroupCommand::forceAddPlayer)))))
                        .then(Commands.literal("remove").then(Commands.argument("group", StringArgumentType.word()).suggests(CommandHelpers::groupSuggestion)
                                .then(Commands.argument("players", GameProfileArgument.gameProfile()).suggests((context, build) -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    String group = StringArgumentType.getString(context, "group");
                                    List<String> list = new ArrayList<>();
                                    CommandSourceStack src = context.getSource();
                                    ClaimStorage storage = ClaimStorage.get(src.getLevel());
                                    Claim claim = storage.getClaimAt(src.getPlayerOrException().blockPosition());
                                    if (claim != null && claim.canInteract(src.getPlayerOrException(), BuiltinPermission.EDITPERMS, src.getPlayerOrException().blockPosition())) {
                                        list = claim.playersFromGroup(player.getServer(), group).stream().map(GameProfile::getName).toList();
                                    }
                                    return SharedSuggestionProvider.suggest(list, build);
                                }).executes(ClaimGroupCommand::removePlayer))))));
    }

    private static int addGroup(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return modifyGroup(context, false);
    }

    private static int removeGroup(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return modifyGroup(context, true);
    }

    private static int modifyGroup(CommandContext<CommandSourceStack> context, boolean remove) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String group = StringArgumentType.getString(context, "group");
        ClaimStorage storage = ClaimStorage.get(player.serverLevel());
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim == null) {
            ClaimUtils.noClaimMessage(player);
            return 0;
        }
        if (PlayerClaimData.get(player).getClaimMode().isSubclaim) {
            Claim sub = claim.getSubClaim(player.blockPosition());
            if (sub != null)
                claim = sub;
        }
        if (remove) {
            if (claim.removePermGroup(player, group))
                player.displayClientMessage(ClaimUtils.translatedText("flan.groupRemove", group, ChatFormatting.GOLD), false);
            else {
                player.displayClientMessage(ClaimUtils.translatedText("flan.noPermission", ChatFormatting.DARK_RED), false);
                return 0;
            }
        } else {
            if (claim.groups().contains(group)) {
                player.displayClientMessage(ClaimUtils.translatedText("flan.groupExist", group, ChatFormatting.RED), false);
                return 0;
            } else if (claim.editPerms(player, group, BuiltinPermission.EDITPERMS, -1))
                player.displayClientMessage(ClaimUtils.translatedText("flan.groupAdd", group, ChatFormatting.GOLD), false);
            else {
                player.displayClientMessage(ClaimUtils.translatedText("flan.noPermission", ChatFormatting.DARK_RED), false);
                return 0;
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int forceAddPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String group = StringArgumentType.getString(context, "group");
        return modifyPlayer(context, group, true);
    }

    private static int addPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String group = StringArgumentType.getString(context, "group");
        return modifyPlayer(context, group, false);
    }

    private static int removePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return modifyPlayer(context, null, false);
    }

    private static int modifyPlayer(CommandContext<CommandSourceStack> context, String group, boolean force) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ClaimStorage storage = ClaimStorage.get(player.serverLevel());
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim == null) {
            ClaimUtils.noClaimMessage(player);
            return 0;
        }
        if (PlayerClaimData.get(player).getClaimMode().isSubclaim) {
            Claim sub = claim.getSubClaim(player.blockPosition());
            if (sub != null)
                claim = sub;
        }
        if (!claim.canInteract(player, BuiltinPermission.EDITPERMS, player.blockPosition())) {
            player.displayClientMessage(ClaimUtils.translatedText("flan.noPermission", ChatFormatting.DARK_RED), false);
            return 0;
        }
        List<String> modified = new ArrayList<>();
        for (GameProfile prof : GameProfileArgument.getGameProfiles(context, "players")) {
            if (claim.setPlayerGroup(prof.getId(), group, force))
                modified.add(prof.getName());
        }
        if (group == null) {
            if (!modified.isEmpty())
                player.displayClientMessage(ClaimUtils.translatedText("flan.playerRemove", modified, ChatFormatting.GOLD), false);
            else
                player.displayClientMessage(ClaimUtils.translatedText("flan.playerRemoveNo", ChatFormatting.RED), false);
        } else {
            if (!modified.isEmpty())
                player.displayClientMessage(ClaimUtils.translatedText("flan.playerModify", group, modified, ChatFormatting.GOLD), false);
            else
                player.displayClientMessage(ClaimUtils.translatedText("flan.playerModifyNo", group, ChatFormatting.RED), false);
        }
        return modified.size();
    }
}
