package io.github.flemmli97.flan.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.api.data.IPlayerData;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.ClaimMenuScreenHandler;
import io.github.flemmli97.flan.gui.PersonalGroupScreenHandler;
import io.github.flemmli97.flan.integration.gunpowder.CommandCurrency;
import io.github.flemmli97.flan.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.EnumDisplayType;
import io.github.flemmli97.flan.player.EnumEditMode;
import io.github.flemmli97.flan.player.OfflinePlayerData;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CommandClaim {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("flan")
                .then(CommandManager.literal("reload").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdReload, true)).executes(CommandClaim::reloadConfig))
                .then(CommandManager.literal("addClaim").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.claimCreate))
                        .then(CommandManager.argument("from", BlockPosArgumentType.blockPos()).then(CommandManager.argument("to", BlockPosArgumentType.blockPos()).executes(CommandClaim::addClaim)))
                        .then(CommandManager.literal("all").executes(CommandClaim::addClaimAll))
                        .then(CommandManager.literal("rect").then(CommandManager.argument("x", IntegerArgumentType.integer()).then(CommandManager.argument("z", IntegerArgumentType.integer()).executes(ctx -> CommandClaim.addClaimRect(ctx, IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "z")))))))
                .then(CommandManager.literal("menu").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdMenu)).executes(CommandClaim::openMenu))
                .then(CommandManager.literal("setHome").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdHome)).executes(CommandClaim::setClaimHome))
                .then(CommandManager.literal("trapped").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdTrapped)).executes(CommandClaim::trapped))
                .then(CommandManager.literal("name").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdName)).then(CommandManager.argument("name", StringArgumentType.string()).executes(CommandClaim::nameClaim)))
                .then(CommandManager.literal("unlockDrops").executes(CommandClaim::unlockDrops)
                        .then(CommandManager.argument("players", GameProfileArgumentType.gameProfile()).requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdUnlockAll, true)).executes(CommandClaim::unlockDropsPlayers)))
                .then(CommandManager.literal("personalGroups").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdPGroup)).executes(CommandClaim::openPersonalGroups))
                .then(CommandManager.literal("claimInfo").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdInfo)).executes(ctx -> CommandClaim.claimInfo(ctx, Claim.InfoType.ALL))
                        .then(CommandManager.argument("type", StringArgumentType.word()).suggests((src, b) -> CommandHelpers.enumSuggestion(Claim.InfoType.class, b)).executes(CommandClaim::claimInfo)))
                .then(CommandManager.literal("transferClaim").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdTransfer)).then(CommandManager.argument("player", GameProfileArgumentType.gameProfile()).executes(CommandClaim::transferClaim)))
                .then(CommandManager.literal("delete").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdTransfer)).executes(CommandClaim::deleteClaim))
                .then(CommandManager.literal("deleteAll").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdTransfer)).executes(CommandClaim::deleteAllClaim))
                .then(CommandManager.literal("deleteSubClaim").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdTransfer)).executes(CommandClaim::deleteSubClaim))
                .then(CommandManager.literal("deleteAllSubClaims").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdTransfer)).executes(CommandClaim::deleteAllSubClaim))
                .then(CommandManager.literal("list").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdList)).executes(CommandClaim::listClaims).then(CommandManager.argument("player", GameProfileArgumentType.gameProfile()).requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdListAll, true))
                        .executes(cmd -> listClaims(cmd, GameProfileArgumentType.getProfileArgument(cmd, "player")))))
                .then(CommandManager.literal("switchMode").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdClaimMode)).executes(CommandClaim::switchClaimMode))
                .then(CommandManager.literal("adminMode").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdAdminMode, true)).executes(CommandClaim::switchAdminMode))
                .then(CommandManager.literal("readGriefPrevention").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdGriefPrevention, true)).executes(CommandClaim::readGriefPreventionData))
                .then(CommandManager.literal("setAdminClaim").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdAdminSet, true)).then(CommandManager.argument("toggle", BoolArgumentType.bool()).executes(CommandClaim::toggleAdminClaim)))
                .then(CommandManager.literal("listAdminClaims").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdAdminList, true)).executes(CommandClaim::listAdminClaims))
                .then(CommandManager.literal("adminDelete").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdAdminDelete, true)).executes(CommandClaim::adminDelete)
                        .then(CommandManager.literal("all").then(CommandManager.argument("players", GameProfileArgumentType.gameProfile())
                                .executes(CommandClaim::adminDeleteAll))))
                .then(CommandManager.literal("giveClaimBlocks").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdAdminGive, true)).then(CommandManager.argument("players", GameProfileArgumentType.gameProfile())
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer()).executes(CommandClaim::giveClaimBlocks))))
                .then(CommandManager.literal("buyBlocks").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdBuy, false))
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer()).executes(CommandClaim::buyClaimBlocks)))
                .then(CommandManager.literal("sellBlocks").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdSell, false))
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer()).executes(CommandClaim::sellClaimBlocks)))
                .then(CommandManager.literal("claimMessage").then(CommandManager.argument("type", StringArgumentType.word()).suggests((ctx, b) -> CommandSource.suggestMatching(new String[]{"enter", "leave"}, b))
                        .then(CommandManager.argument("title", StringArgumentType.word()).suggests((ctx, b) -> CommandSource.suggestMatching(new String[]{"title", "subtitle"}, b))
                                .then(CommandManager.literal("text").then(CommandManager.argument("component", TextArgumentType.text()).executes(ctx -> CommandClaim.editClaimMessages(ctx, TextArgumentType.getTextArgument(ctx, "component")))))
                                .then(CommandManager.literal("string").then(CommandManager.argument("message", StringArgumentType.string()).executes(CommandClaim::editClaimMessages))))))
                .then(CommandManager.literal("group").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdGroup))
                        .then(CommandManager.literal("add").then(CommandManager.argument("group", StringArgumentType.string()).executes(CommandClaim::addGroup)))
                        .then(CommandManager.literal("remove").then(CommandManager.argument("group", StringArgumentType.string())
                                .suggests(CommandHelpers::groupSuggestion).executes(CommandClaim::removeGroup)))
                        .then(CommandManager.literal("players")
                                .then(CommandManager.literal("add").then(CommandManager.argument("group", StringArgumentType.word()).suggests(CommandHelpers::groupSuggestion)
                                        .then(CommandManager.argument("players", GameProfileArgumentType.gameProfile()).executes(CommandClaim::addPlayer)
                                                .then(CommandManager.literal("overwrite").executes(CommandClaim::forceAddPlayer)))))
                                .then(CommandManager.literal("remove").then(CommandManager.argument("group", StringArgumentType.word()).suggests(CommandHelpers::groupSuggestion)
                                        .then(CommandManager.argument("players", GameProfileArgumentType.gameProfile()).suggests((context, build) -> {
                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                            List<String> list = new ArrayList<>();
                                            ServerCommandSource src = context.getSource();
                                            ClaimStorage storage = ClaimStorage.get(src.getWorld());
                                            Claim claim = storage.getClaimAt(src.getPlayer().getBlockPos());
                                            if (claim != null && claim.canInteract(src.getPlayer(), PermissionRegistry.EDITPERMS, src.getPlayer().getBlockPos())) {
                                                list = claim.playersFromGroup(player.getServer(), "");
                                            }
                                            return CommandSource.suggestMatching(list, build);
                                        }).executes(CommandClaim::removePlayer))))))
                .then(CommandManager.literal("teleport").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdTeleport))
                        .then(CommandManager.literal("self").then(CommandManager.argument("claim", StringArgumentType.string()).suggests((ctx, b) -> CommandHelpers.claimSuggestions(ctx, b, ctx.getSource().getPlayer().getUuid()))
                                .executes(CommandClaim::teleport)))
                        .then(CommandManager.literal("admin").then(CommandManager.argument("claim", StringArgumentType.string()).suggests((ctx, b) -> CommandHelpers.claimSuggestions(ctx, b, null))
                                .executes(CommandClaim::teleportAdminClaims)))
                        .then(CommandManager.literal("other").then(CommandManager.argument("player", GameProfileArgumentType.gameProfile()).then(CommandManager.argument("claim", StringArgumentType.string()).suggests((ctx, b) -> CommandHelpers.claimSuggestions(ctx, b, CommandHelpers.singleProfile(ctx, "player").getId()))
                                .executes(src -> CommandClaim.teleport(src, CommandHelpers.singleProfile(src, "player").getId()))))))
                .then(CommandManager.literal("permission").requires(src -> PermissionNodeHandler.perm(src, PermissionNodeHandler.cmdPermission))
                        .then(CommandManager.literal("personal").then(CommandManager.argument("group", StringArgumentType.string()).suggests(CommandHelpers::personalGroupSuggestion)
                                .then(CommandManager.argument("permission", StringArgumentType.word()).suggests((ctx, b) -> CommandHelpers.permSuggestions(ctx, b, true))
                                        .then(CommandManager.argument("toggle", StringArgumentType.word())
                                                .suggests((ctx, b) -> CommandSource.suggestMatching(new String[]{"default", "true", "false"}, b)).executes(CommandClaim::editPersonalPerm)))))
                        .then(CommandManager.literal("global").then(CommandManager.argument("permission", StringArgumentType.word()).suggests((ctx, b) -> CommandHelpers.permSuggestions(ctx, b, false))
                                .then(CommandManager.argument("toggle", StringArgumentType.word()).suggests((ctx, b) -> CommandSource.suggestMatching(new String[]{"default", "true", "false"}, b)).executes(CommandClaim::editGlobalPerm))))
                        .then(CommandManager.literal("group").then(CommandManager.argument("group", StringArgumentType.string()).suggests(CommandHelpers::groupSuggestion)
                                .then(CommandManager.argument("permission", StringArgumentType.word()).suggests((ctx, b) -> CommandHelpers.permSuggestions(ctx, b, true))
                                        .then(CommandManager.argument("toggle", StringArgumentType.word())
                                                .suggests((ctx, b) -> CommandSource.suggestMatching(new String[]{"default", "true", "false"}, b)).executes(CommandClaim::editGroupPerm))))));
        builder.then(CommandManager.literal("help").executes(ctx -> CommandHelp.helpMessage(ctx, 0, builder.getArguments()))
                .then(CommandManager.argument("page", IntegerArgumentType.integer()).executes(ctx -> CommandHelp.helpMessage(ctx, builder.getArguments())))
                .then(CommandManager.literal("cmd").then(CommandManager.argument("command", StringArgumentType.word()).suggests((ctx, sb) -> CommandSource.suggestMatching(CommandHelp.registeredCommands(ctx, builder.getArguments()), sb)).executes(CommandHelp::helpCmd))));
        builder.then(CommandManager.literal("?").executes(ctx -> CommandHelp.helpCmd(ctx, "help")));
        dispatcher.register(builder);
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        ConfigHandler.reloadConfigs(context.getSource().getServer());
        context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("configReload")), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int addClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
        BlockPos from = BlockPosArgumentType.getLoadedBlockPos(context, "from");
        BlockPos to = BlockPosArgumentType.getLoadedBlockPos(context, "to");
        storage.createClaim(from, to, player);
        return Command.SINGLE_SUCCESS;
    }

    private static int addClaimAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerClaimData data = PlayerClaimData.get(player);
        int usable = data.getClaimBlocks() + data.getAdditionalClaims() - data.usedClaimBlocks();
        int size = (int) Math.floor(Math.sqrt(usable));
        return addClaimRect(context, size, size);
    }

    private static int addClaimRect(CommandContext<ServerCommandSource> context, int x, int z) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
        boolean evenX = x % 2 == 0;
        boolean evenZ = z % 2 == 0;
        BlockPos from = player.getBlockPos().add(evenX ? -(int) ((x - 1) * 0.5) : -(int) (x * 0.5), -5, evenZ ? -(int) ((z - 1) * 0.5) : -(int) (z * 0.5));
        BlockPos to = player.getBlockPos().add((int) (x * 0.5), -5, (int) (z * 0.5));
        storage.createClaim(from, to, player);
        return Command.SINGLE_SUCCESS;
    }

    private static int transferClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Collection<GameProfile> profs = GameProfileArgumentType.getProfileArgument(context, "player");
        if (profs.size() != 1) {
            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("onlyOnePlayer"), Formatting.RED), false);
            return 0;
        }
        GameProfile prof = profs.iterator().next();
        ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
        Claim claim = storage.getClaimAt(player.getBlockPos());
        if (claim == null) {
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noClaim"), Formatting.RED), false);
            return 0;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        boolean enoughBlocks = true;
        if (!data.isAdminIgnoreClaim()) {
            MinecraftServer server = context.getSource().getServer();
            ServerPlayerEntity newOwner = server.getPlayerManager().getPlayer(prof.getId());
            IPlayerData newData = newOwner != null ? PlayerClaimData.get(newOwner) : new OfflinePlayerData(server, prof.getId());
            enoughBlocks = newData.canUseClaimBlocks(claim.getPlane());
        }
        if (!enoughBlocks) {
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("ownerTransferNoBlocks"), Formatting.RED), false);
            if (PermissionNodeHandler.perm(context.getSource(), PermissionNodeHandler.cmdAdminMode, true))
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("ownerTransferNoBlocksAdmin"), Formatting.RED), false);
            return 0;
        }
        if (!storage.transferOwner(claim, player, prof.getId())) {
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("ownerTransferFail"), Formatting.RED), false);
            return 0;
        }
        player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("ownerTransferSuccess"), prof.getName()), Formatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int openMenu(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerClaimData data = PlayerClaimData.get(player);
        Claim claim = ClaimStorage.get(player.getServerWorld()).getClaimAt(player.getBlockPos());
        if (claim == null) {
            PermHelper.noClaimMessage(player);
            return 0;
        }
        if (data.getEditMode() == EnumEditMode.DEFAULT) {
            ClaimMenuScreenHandler.openClaimMenu(player, claim);
            data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
        } else {
            Claim sub = claim.getSubClaim(player.getBlockPos());
            ClaimMenuScreenHandler.openClaimMenu(player, Objects.requireNonNullElse(sub, claim));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int trapped(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.setTrappedRescue()) {
            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("trappedRescue"), Formatting.GOLD), false);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("trappedFail"), Formatting.RED), false);
        }
        return 0;
    }

    private static int nameClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.getEditMode() == EnumEditMode.DEFAULT) {
            Claim claim = PermHelper.checkReturn(player, PermissionRegistry.EDITPERMS, PermHelper.genericNoPermMessage(player));
            if (claim == null)
                return 0;
            boolean nameUsed = ClaimStorage.get(player.getServerWorld()).allClaimsFromPlayer(claim.getOwner())
                    .stream().map(Claim::getClaimName).anyMatch(name -> name.equals(StringArgumentType.getString(context, "name")));
            if (!nameUsed) {
                String name = StringArgumentType.getString(context, "name");
                claim.setClaimName(name);
                player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimNameSet"), name), Formatting.GOLD), false);
            } else {
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("claimNameUsed"), Formatting.DARK_RED), false);
            }
        } else {
            Claim claim = ClaimStorage.get(player.getServerWorld()).getClaimAt(player.getBlockPos());
            Claim sub = claim.getSubClaim(player.getBlockPos());
            if (sub != null && (claim.canInteract(player, PermissionRegistry.EDITPERMS, player.getBlockPos()) || sub.canInteract(player, PermissionRegistry.EDITPERMS, player.getBlockPos()))) {
                boolean nameUsed = claim.getAllSubclaims()
                        .stream().map(Claim::getClaimName).anyMatch(name -> name.equals(StringArgumentType.getString(context, "name")));
                if (!nameUsed) {
                    String name = StringArgumentType.getString(context, "name");
                    sub.setClaimName(name);
                    player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimNameSet"), name), Formatting.GOLD), false);
                } else {
                    player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("claimNameUsedSub"), Formatting.DARK_RED), false);
                }
            } else if (claim.canInteract(player, PermissionRegistry.EDITPERMS, player.getBlockPos())) {
                boolean nameUsed = ClaimStorage.get(player.getServerWorld()).allClaimsFromPlayer(claim.getOwner())
                        .stream().map(Claim::getClaimName).anyMatch(name -> name.equals(StringArgumentType.getString(context, "name")));
                if (!nameUsed) {
                    String name = StringArgumentType.getString(context, "name");
                    claim.setClaimName(name);
                    player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimNameSet"), name), Formatting.GOLD), false);
                } else {
                    player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("claimNameUsed"), Formatting.DARK_RED), false);
                }
            } else
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermission"), Formatting.DARK_RED), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int unlockDrops(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerClaimData data = PlayerClaimData.get(player);
        data.unlockDeathItems();
        context.getSource().sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("unlockDrops"), ConfigHandler.config.dropTicks), Formatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int unlockDropsPlayers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<GameProfile> profs = GameProfileArgumentType.getProfileArgument(context, "players");
        List<String> success = new ArrayList<>();
        for (GameProfile prof : profs) {
            ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(prof.getId());
            if (player != null) {
                PlayerClaimData data = PlayerClaimData.get(player);
                data.unlockDeathItems();
                success.add(prof.getName());
            }
        }
        context.getSource().sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("unlockDropsMulti"), success), Formatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int openPersonalGroups(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PersonalGroupScreenHandler.openGroupMenu(player);
        return Command.SINGLE_SUCCESS;
    }

    private static int claimInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return claimInfo(context, CommandHelpers.parseEnum(Claim.InfoType.class, StringArgumentType.getString(context, "type"), Claim.InfoType.ALL));
    }

    private static int claimInfo(CommandContext<ServerCommandSource> context, Claim.InfoType infoType) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Claim claim = ClaimStorage.get(player.getServerWorld()).getClaimAt(player.getBlockPos());
        PlayerClaimData data = PlayerClaimData.get(player);
        if (claim == null) {
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noClaim"), Formatting.RED), false);
            return 0;
        }
        if (data.getEditMode() == EnumEditMode.SUBCLAIM) {
            Claim sub = claim.getSubClaim(player.getBlockPos());
            if (sub != null) {
                List<Text> info = sub.infoString(player, infoType);
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("claimSubHeader"), Formatting.AQUA), false);
                for (Text text : info)
                    player.sendMessage(text, false);
                return Command.SINGLE_SUCCESS;
            }
        }
        List<Text> info = claim.infoString(player, infoType);
        for (Text text : info)
            player.sendMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
        Claim claim = storage.getClaimAt(player.getBlockPos());
        boolean check = PermHelper.check(player, player.getBlockPos(), claim, PermissionRegistry.EDITCLAIM, b -> {
            if (b.isEmpty())
                PermHelper.noClaimMessage(player);
            else if (!b.get())
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteClaimError"), Formatting.DARK_RED), false);
            else
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteClaim"), Formatting.RED), false);
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
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteAllClaim"), Formatting.GOLD), false);
            data.setConfirmDeleteAll(false);
        } else {
            data.setConfirmDeleteAll(true);
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteAllClaimConfirm"), Formatting.DARK_RED), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteSubClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
        Claim claim = storage.getClaimAt(player.getBlockPos());
        if (claim == null) {
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noClaim"), Formatting.RED), false);
            return 0;
        }
        Claim sub = claim.getSubClaim(player.getBlockPos());
        if (sub == null) {
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noClaim"), Formatting.RED), false);
            return 0;
        }
        boolean check = PermHelper.check(player, player.getBlockPos(), claim, PermissionRegistry.EDITCLAIM, b -> {
            if (b.isEmpty())
                PermHelper.noClaimMessage(player);
            else if (!b.get())
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteClaimError"), Formatting.DARK_RED), false);
            else
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteSubClaim"), Formatting.DARK_RED), false);
        });
        if (!check)
            return 0;
        claim.deleteSubClaim(sub);
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteAllSubClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Claim claim = PermHelper.checkReturn(player, PermissionRegistry.EDITCLAIM, PermHelper.genericNoPermMessage(player));
        if (claim == null)
            return 0;
        List<Claim> subs = claim.getAllSubclaims();
        subs.forEach(claim::deleteSubClaim);
        player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteSubClaimAll"), Formatting.DARK_RED), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int listClaims(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return listClaimsFromUUID(context, null);
    }

    private static int listClaims(CommandContext<ServerCommandSource> context, Collection<GameProfile> profs) throws CommandSyntaxException {
        if (profs.size() != 1) {
            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("onlyOnePlayer"), Formatting.RED), false);
            return 0;
        }
        GameProfile prof = profs.iterator().next();
        if (prof == null || prof.getId() == null)
            return 0;
        return listClaimsFromUUID(context, prof.getId());
    }

    private static int listClaimsFromUUID(CommandContext<ServerCommandSource> context, UUID of) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayerEntity player = of == null ? context.getSource().getPlayer() : server.getPlayerManager().getPlayer(of);
        Map<World, Collection<Claim>> claims = new HashMap<>();
        for (ServerWorld world : server.getWorlds()) {
            ClaimStorage storage = ClaimStorage.get(world);
            claims.put(world, storage.allClaimsFromPlayer(player != null ? player.getUuid() : of));
        }
        if (ConfigHandler.config.maxClaimBlocks != -1) {
            if (player != null) {
                PlayerClaimData data = PlayerClaimData.get(player);
                context.getSource().sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimBlocksFormat"),
                        data.getClaimBlocks(), data.getAdditionalClaims(), data.usedClaimBlocks()), Formatting.GOLD), false);
            } else {
                OfflinePlayerData data = new OfflinePlayerData(server, of);
                context.getSource().sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimBlocksFormat"),
                        data.claimBlocks, data.getAdditionalClaims(), data.usedClaimBlocks()), Formatting.GOLD), false);
            }
        }
        context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("listClaims"), Formatting.GOLD), false);
        for (Map.Entry<World, Collection<Claim>> entry : claims.entrySet())
            for (Claim claim : entry.getValue())
                context.getSource().sendFeedback(PermHelper.simpleColoredText(
                        entry.getKey().getRegistryKey().getValue().toString() + " # " + claim.formattedClaim(), Formatting.YELLOW), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int switchClaimMode(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerClaimData data = PlayerClaimData.get(player);
        data.setEditMode(data.getEditMode() == EnumEditMode.DEFAULT ? EnumEditMode.SUBCLAIM : EnumEditMode.DEFAULT);
        player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("editMode"), data.getEditMode()), Formatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int switchAdminMode(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerClaimData data = PlayerClaimData.get(player);
        data.setAdminIgnoreClaim(!data.isAdminIgnoreClaim());
        player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("adminMode"), data.isAdminIgnoreClaim()), Formatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int adminDelete(CommandContext<ServerCommandSource> context) {
        ServerCommandSource src = context.getSource();
        ClaimStorage storage = ClaimStorage.get(src.getWorld());
        Claim claim = storage.getClaimAt(new BlockPos(src.getPosition()));
        if (claim == null) {
            src.sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noClaim"), Formatting.RED), false);
            return 0;
        }
        storage.deleteClaim(claim, true, EnumEditMode.DEFAULT, src.getWorld());
        src.sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteClaim"), Formatting.RED), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int adminDeleteAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource src = context.getSource();
        if (src.getEntity() instanceof ServerPlayerEntity player) {
            PlayerClaimData data = PlayerClaimData.get(player);
            if (!data.confirmedDeleteAll()) {
                data.setConfirmDeleteAll(true);
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteAllClaimConfirm"), Formatting.DARK_RED), false);
                return Command.SINGLE_SUCCESS;
            }
        }
        List<String> players = new ArrayList<>();
        for (GameProfile prof : GameProfileArgumentType.getProfileArgument(context, "players")) {
            for (ServerWorld world : src.getWorld().getServer().getWorlds()) {
                ClaimStorage storage = ClaimStorage.get(world);
                storage.allClaimsFromPlayer(prof.getId()).forEach((claim) -> storage.deleteClaim(claim, true, EnumEditMode.DEFAULT, world));
            }
            players.add(prof.getName());
        }
        src.sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("adminDeleteAll"), players), Formatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int toggleAdminClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
        Claim claim = storage.getClaimAt(player.getBlockPos());
        if (claim == null) {
            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noClaim"), Formatting.RED), false);
            return 0;
        }
        storage.toggleAdminClaim(player, claim, BoolArgumentType.getBool(context, "toggle"));
        context.getSource().sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("setAdminClaim"), claim.isAdminClaim()), Formatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int listAdminClaims(CommandContext<ServerCommandSource> context) {
        ServerCommandSource src = context.getSource();
        Collection<Claim> claims = ClaimStorage.get(src.getWorld()).getAdminClaims();
        src.sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("listAdminClaims"), src.getWorld().getRegistryKey().getValue()), Formatting.GOLD), false);
        for (Claim claim : claims)
            src.sendFeedback(PermHelper.simpleColoredText(claim.formattedClaim(), Formatting.YELLOW), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int readGriefPreventionData(CommandContext<ServerCommandSource> context) {
        ServerCommandSource src = context.getSource();
        src.sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("readGriefpreventionData"), Formatting.GOLD), true);
        if (ClaimStorage.readGriefPreventionData(src.getServer(), src))
            src.sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("readGriefpreventionClaimDataSuccess"), Formatting.GOLD), true);
        if (PlayerClaimData.readGriefPreventionPlayerData(src.getServer(), src))
            src.sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("readGriefpreventionPlayerDataSuccess"), Formatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int giveClaimBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource src = context.getSource();
        List<String> players = new ArrayList<>();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (GameProfile prof : GameProfileArgumentType.getProfileArgument(context, "players")) {
            ServerPlayerEntity player = src.getServer().getPlayerManager().getPlayer(prof.getId());
            if (player != null) {
                PlayerClaimData data = PlayerClaimData.get(player);
                data.setAdditionalClaims(data.getAdditionalClaims() + amount);
            } else
                PlayerClaimData.editForOfflinePlayer(src.getServer(), prof.getId(), amount);
            players.add(prof.getName());
        }
        src.sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("giveClaimBlocks"), players, amount), Formatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
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
                player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("groupRemove"), group), Formatting.GOLD), false);
            else {
                PermHelper.genericNoPermMessage(player);
                return 0;
            }
        } else {
            if (claim.groups().contains(group)) {
                player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("groupExist"), group), Formatting.RED), false);
                return 0;
            } else if (claim.editPerms(player, group, PermissionRegistry.EDITPERMS, -1))
                player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("groupAdd"), group), Formatting.GOLD), false);
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
        if (!claim.canInteract(player, PermissionRegistry.EDITPERMS, player.getBlockPos())) {
            PermHelper.genericNoPermMessage(player);
            return 0;
        }
        List<String> modified = new ArrayList<>();
        for (GameProfile prof : GameProfileArgumentType.getProfileArgument(context, "players")) {
            if (claim.setPlayerGroup(prof.getId(), group, force))
                modified.add(prof.getName());
        }
        if (!modified.isEmpty())
            player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("playerModify"), group, modified), Formatting.GOLD), false);
        else
            player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("playerModifyNo"), group, modified), Formatting.RED), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int editGlobalPerm(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int mode = switch (StringArgumentType.getString(context, "toggle")) {
            case "true" -> 1;
            case "default" -> -1;
            default -> 0;
        };
        return editPerms(context, null, mode);
    }

    private static int editGroupPerm(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int mode = switch (StringArgumentType.getString(context, "toggle")) {
            case "true" -> 1;
            case "default" -> -1;
            default -> 0;
        };
        return editPerms(context, StringArgumentType.getString(context, "group"), mode);
    }

    private static int editPerms(CommandContext<ServerCommandSource> context, String group, int mode) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Claim claim = ClaimStorage.get(player.getServerWorld()).getClaimAt(player.getBlockPos());
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.getEditMode() == EnumEditMode.SUBCLAIM) {
            Claim sub = claim.getSubClaim(player.getBlockPos());
            if (sub != null)
                claim = sub;
        }
        if (claim == null) {
            PermHelper.noClaimMessage(player);
            return 0;
        }
        if (!claim.canInteract(player, PermissionRegistry.EDITPERMS, player.getBlockPos())) {
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermission"), Formatting.DARK_RED), false);
            return 0;
        }
        ClaimPermission perm;
        String p = StringArgumentType.getString(context, "permission");
        try {
            perm = PermissionRegistry.get(p);
            if (group != null && PermissionRegistry.globalPerms().contains(perm))
                throw new IllegalArgumentException();
        } catch (NullPointerException e) {
            player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("noSuchPerm"), p), Formatting.DARK_RED), false);
            return 0;
        }
        String setPerm = mode == 1 ? "true" : mode == 0 ? "false" : "default";
        if (group == null) {
            claim.editGlobalPerms(player, perm, mode);
            player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("editPerm"), perm, setPerm), Formatting.GOLD), false);
        } else {
            claim.editPerms(player, group, perm, mode);
            player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("editPermGroup"), perm, group, setPerm), Formatting.GOLD), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int editPersonalPerm(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String group = StringArgumentType.getString(context, "group");
        int mode = switch (StringArgumentType.getString(context, "toggle")) {
            case "true" -> 1;
            case "default" -> -1;
            default -> 0;
        };
        ClaimPermission perm;
        String p = StringArgumentType.getString(context, "permission");
        try {
            perm = PermissionRegistry.get(p);
            if (PermissionRegistry.globalPerms().contains(perm))
                throw new IllegalArgumentException();
        } catch (NullPointerException e) {
            player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("noSuchPerm"), p), Formatting.DARK_RED), false);
            return 0;
        }
        String setPerm = mode == 1 ? "true" : mode == 0 ? "false" : "default";
        if (PlayerClaimData.get(player).editDefaultPerms(group, perm, mode))
            player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("editPersonalGroup"), group, perm, setPerm), Formatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int setClaimHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Claim claim = PermHelper.checkReturn(player, PermissionRegistry.EDITCLAIM, PermHelper.genericNoPermMessage(player));
        if (claim == null)
            return 0;
        claim.setHomePos(player.getBlockPos());
        context.getSource().sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("setHome"), player.getBlockPos().getX(), player.getBlockPos().getY(), player.getBlockPos().getZ()), Formatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int teleport(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return teleport(context, context.getSource().getPlayer().getUuid());
    }

    public static int teleportAdminClaims(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return teleport(context, null);
    }

    public static int teleport(CommandContext<ServerCommandSource> context, UUID owner) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String name = StringArgumentType.getString(context, "claim");
        return ClaimStorage.get(player.getServerWorld()).allClaimsFromPlayer(owner)
                .stream().filter(claim -> {
                    if (claim.getClaimName().isEmpty())
                        return claim.getClaimID().toString().equals(name);
                    return claim.getClaimName().equals(name);
                }).findFirst().map(claim -> {
                    BlockPos pos = claim.getHomePos();
                    if (claim.canInteract(player, PermissionRegistry.TELEPORT, pos, false)) {
                        PlayerClaimData data = PlayerClaimData.get(player);
                        if (data.setTeleportTo(pos)) {
                            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("teleportHome"), Formatting.GOLD), false);
                            return Command.SINGLE_SUCCESS;
                        }
                        context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("teleportHomeFail"), Formatting.RED), false);
                    } else
                        context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermissionSimple"), Formatting.DARK_RED), false);
                    return 0;
                }).orElse(0);
    }

    public static int editClaimMessages(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return editClaimMessages(context, new LiteralText(StringArgumentType.getString(context, "message")));
    }

    public static int editClaimMessages(CommandContext<ServerCommandSource> context, Text text) throws CommandSyntaxException {
        if (text instanceof MutableText) {
            Style style = text.getStyle();
            if (style.isEmpty())
                style = style.withFormatting(Formatting.WHITE);
            if (!style.isItalic())
                style = style.withItalic(false);
            ((MutableText) text).setStyle(style);
        }
        ServerPlayerEntity player = context.getSource().getPlayer();
        Claim claim = PermHelper.checkReturn(player, PermissionRegistry.EDITPERMS, PermHelper.genericNoPermMessage(player));
        if (claim == null)
            return 0;
        boolean sub = StringArgumentType.getString(context, "title").equals("subtitle");
        boolean enter = StringArgumentType.getString(context, "type").equals("enter");
        String feedback;
        if (enter) {
            if (sub) {
                claim.setEnterTitle(claim.enterTitle, text);
                feedback = ConfigHandler.langManager.get("setEnterSubMessage");
            } else {
                claim.setEnterTitle(text, claim.enterSubtitle);
                feedback = ConfigHandler.langManager.get("setEnterMessage");
            }
        } else {
            if (sub) {
                claim.setLeaveTitle(claim.leaveTitle, text);
                feedback = ConfigHandler.langManager.get("setLeaveSubMessage");
            } else {
                claim.setLeaveTitle(text, claim.leaveSubtitle);
                feedback = ConfigHandler.langManager.get("setLeaveMessage");
            }
        }
        String[] unf = feedback.split("%s", 2);
        MutableText cmdFeed = new LiteralText(unf[0]).formatted(Formatting.GOLD)
                .append(text);
        if (unf.length > 1)
            cmdFeed.append(new LiteralText(unf[1])).formatted(Formatting.GOLD);
        context.getSource().sendFeedback(cmdFeed, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int sellClaimBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        boolean b = ConfigHandler.config.buySellHandler.sell(context.getSource().getPlayer(), Math.max(0, IntegerArgumentType.getInteger(context, "amount")), m -> context.getSource().sendFeedback(m, false));
        return b ? Command.SINGLE_SUCCESS : 0;
    }

    private static int buyClaimBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        boolean b = ConfigHandler.config.buySellHandler.buy(context.getSource().getPlayer(), Math.max(0, IntegerArgumentType.getInteger(context, "amount")), m -> context.getSource().sendFeedback(m, false));
        return b ? Command.SINGLE_SUCCESS : 0;
    }
}
