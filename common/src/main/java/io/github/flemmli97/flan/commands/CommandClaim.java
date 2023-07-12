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
import io.github.flemmli97.flan.event.ItemInteractEvents;
import io.github.flemmli97.flan.gui.ClaimMenuScreenHandler;
import io.github.flemmli97.flan.gui.PersonalGroupScreenHandler;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.EnumEditMode;
import io.github.flemmli97.flan.player.OfflinePlayerData;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.player.display.EnumDisplayType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CommandClaim {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("flan")
                .then(Commands.literal("reload").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdReload, true)).executes(CommandClaim::reloadConfig))
                .then(Commands.literal("addClaim").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.claimCreate))
                        .then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos()).executes(CommandClaim::addClaim)))
                        .then(Commands.literal("all").executes(CommandClaim::addClaimAll))
                        .then(Commands.literal("rect").then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("z", IntegerArgumentType.integer()).executes(ctx -> CommandClaim.addClaimRect(ctx, IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "z")))))))
                .then(Commands.literal("expand").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.claimCreate))
                        .then(Commands.argument("distance", IntegerArgumentType.integer()).executes(CommandClaim::expandClaim)))
                .then(Commands.literal("menu").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdMenu)).executes(CommandClaim::openMenu))
                .then(Commands.literal("setHome").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdHome)).executes(CommandClaim::setClaimHome))
                .then(Commands.literal("trapped").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdTrapped)).executes(CommandClaim::trapped))
                .then(Commands.literal("name").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdName)).then(Commands.argument("name", StringArgumentType.string()).executes(CommandClaim::nameClaim)))
                .then(Commands.literal("unlockDrops").executes(CommandClaim::unlockDrops)
                        .then(Commands.argument("players", GameProfileArgument.gameProfile()).requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdUnlockAll, true)).executes(CommandClaim::unlockDropsPlayers)))
                .then(Commands.literal("personalGroups").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdPGroup)).executes(CommandClaim::openPersonalGroups))
                .then(Commands.literal("claimInfo").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdInfo)).executes(ctx -> CommandClaim.claimInfo(ctx, Claim.InfoType.ALL))
                        .then(Commands.argument("type", StringArgumentType.word()).suggests((src, b) -> CommandHelpers.enumSuggestion(Claim.InfoType.class, b)).executes(CommandClaim::claimInfo)))
                .then(Commands.literal("transferClaim").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdTransfer)).then(Commands.argument("player", GameProfileArgument.gameProfile()).executes(CommandClaim::transferClaim)))
                .then(Commands.literal("delete").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdDelete)).executes(CommandClaim::deleteClaim))
                .then(Commands.literal("deleteAll").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdDeleteAll)).executes(CommandClaim::deleteAllClaim))
                .then(Commands.literal("deleteSubClaim").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdDeleteSub)).executes(CommandClaim::deleteSubClaim))
                .then(Commands.literal("deleteAllSubClaims").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdDeleteSubAll)).executes(CommandClaim::deleteAllSubClaim))
                .then(Commands.literal("list").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdList)).executes(CommandClaim::listClaims).then(Commands.argument("player", GameProfileArgument.gameProfile()).requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdListAll, true))
                        .executes(cmd -> listClaims(cmd, GameProfileArgument.getGameProfiles(cmd, "player")))))
                .then(Commands.literal("switchMode").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdClaimMode)).executes(CommandClaim::switchClaimMode))
                .then(Commands.literal("adminMode").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdAdminMode, true)).executes(CommandClaim::switchAdminMode))
                .then(Commands.literal("readGriefPrevention").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdGriefPrevention, true)).executes(CommandClaim::readGriefPreventionData))
                .then(Commands.literal("setAdminClaim").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdAdminSet, true)).then(Commands.argument("toggle", BoolArgumentType.bool()).executes(CommandClaim::toggleAdminClaim)))
                .then(Commands.literal("listAdminClaims").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdAdminList, true)).executes(CommandClaim::listAdminClaims))
                .then(Commands.literal("adminDelete").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdAdminDelete, true)).executes(CommandClaim::adminDelete)
                        .then(Commands.literal("all").then(Commands.argument("players", GameProfileArgument.gameProfile())
                                .executes(CommandClaim::adminDeleteAll))))
                .then(Commands.literal("giveClaimBlocks").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdAdminGive, true)).then(Commands.argument("players", GameProfileArgument.gameProfile())
                        .then(Commands.argument("amount", IntegerArgumentType.integer()).executes(CommandClaim::giveClaimBlocks))))
                .then(Commands.literal("buyBlocks").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdBuy, false))
                        .then(Commands.argument("amount", IntegerArgumentType.integer()).executes(CommandClaim::buyClaimBlocks)))
                .then(Commands.literal("sellBlocks").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdSell, false))
                        .then(Commands.argument("amount", IntegerArgumentType.integer()).executes(CommandClaim::sellClaimBlocks)))
                .then(Commands.literal("claimMessage").then(Commands.argument("type", StringArgumentType.word()).suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"enter", "leave"}, b))
                        .then(Commands.argument("title", StringArgumentType.word()).suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"title", "subtitle"}, b))
                                .then(Commands.literal("text").then(Commands.argument("component", ComponentArgument.textComponent()).executes(ctx -> CommandClaim.editClaimMessages(ctx, ComponentArgument.getComponent(ctx, "component")))))
                                .then(Commands.literal("string").then(Commands.argument("message", StringArgumentType.string()).executes(CommandClaim::editClaimMessages))))))
                .then(Commands.literal("group").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdGroup))
                        .then(Commands.literal("add").then(Commands.argument("group", StringArgumentType.string()).executes(CommandClaim::addGroup)))
                        .then(Commands.literal("remove").then(Commands.argument("group", StringArgumentType.string())
                                .suggests(CommandHelpers::groupSuggestion).executes(CommandClaim::removeGroup)))
                        .then(Commands.literal("players")
                                .then(Commands.literal("add").then(Commands.argument("group", StringArgumentType.word()).suggests(CommandHelpers::groupSuggestion)
                                        .then(Commands.argument("players", GameProfileArgument.gameProfile()).executes(CommandClaim::addPlayer)
                                                .then(Commands.literal("overwrite").executes(CommandClaim::forceAddPlayer)))))
                                .then(Commands.literal("remove").then(Commands.argument("group", StringArgumentType.word()).suggests(CommandHelpers::groupSuggestion)
                                        .then(Commands.argument("players", GameProfileArgument.gameProfile()).suggests((context, build) -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            String group = StringArgumentType.getString(context, "group");
                                            List<String> list = new ArrayList<>();
                                            CommandSourceStack src = context.getSource();
                                            ClaimStorage storage = ClaimStorage.get(src.getLevel());
                                            Claim claim = storage.getClaimAt(src.getPlayerOrException().blockPosition());
                                            if (claim != null && claim.canInteract(src.getPlayerOrException(), PermissionRegistry.EDITPERMS, src.getPlayerOrException().blockPosition())) {
                                                list = claim.playersFromGroup(player.getServer(), group);
                                            }
                                            return SharedSuggestionProvider.suggest(list, build);
                                        }).executes(CommandClaim::removePlayer))))))
                .then(Commands.literal("fakePlayer").executes(CommandClaim::toggleFakePlayer)
                        .then(Commands.literal("add").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdFakePlayer, true)).then(Commands.argument("uuid", UuidArgument.uuid()).executes(CommandClaim::addFakePlayer)))
                        .then(Commands.literal("remove").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdFakePlayer, true))
                                .then(Commands.argument("uuid", UuidArgument.uuid()).suggests((context, build) -> {
                                    List<String> list = new ArrayList<>();
                                    CommandSourceStack src = context.getSource();
                                    ClaimStorage storage = ClaimStorage.get(src.getLevel());
                                    Claim claim = storage.getClaimAt(src.getPlayerOrException().blockPosition());
                                    if (claim != null && claim.canInteract(src.getPlayerOrException(), PermissionRegistry.EDITPERMS, src.getPlayerOrException().blockPosition())) {
                                        list = claim.getAllowedFakePlayerUUID();
                                    }
                                    return SharedSuggestionProvider.suggest(list, build);
                                }).executes(CommandClaim::removeFakePlayer))))
                .then(Commands.literal("teleport").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdTeleport))
                        .then(Commands.literal("self").then(Commands.argument("claim", StringArgumentType.string()).suggests((ctx, b) -> CommandHelpers.claimSuggestions(ctx, b, ctx.getSource().getPlayerOrException().getUUID()))
                                .executes(CommandClaim::teleport)))
                        .then(Commands.literal("global").then(Commands.argument("claim", StringArgumentType.string()).suggests((ctx, b) -> CommandHelpers.claimSuggestions(ctx, b, null))
                                .executes(CommandClaim::teleportAdminClaims)))
                        .then(Commands.literal("other").then(Commands.argument("player", GameProfileArgument.gameProfile()).then(Commands.argument("claim", StringArgumentType.string()).suggests((ctx, b) -> CommandHelpers.claimSuggestions(ctx, b, CommandHelpers.singleProfile(ctx, "player").getId()))
                                .executes(src -> CommandClaim.teleport(src, CommandHelpers.singleProfile(src, "player").getId()))))))
                .then(Commands.literal("permission").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.cmdPermission))
                        .then(Commands.literal("personal").then(Commands.argument("group", StringArgumentType.string()).suggests(CommandHelpers::personalGroupSuggestion)
                                .then(Commands.argument("permission", StringArgumentType.word()).suggests((ctx, b) -> CommandHelpers.permSuggestions(ctx, b, true))
                                        .then(Commands.argument("toggle", StringArgumentType.word())
                                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"default", "true", "false"}, b)).executes(CommandClaim::editPersonalPerm)))))
                        .then(Commands.literal("global").then(Commands.argument("permission", StringArgumentType.word()).suggests((ctx, b) -> CommandHelpers.permSuggestions(ctx, b, false))
                                .then(Commands.argument("toggle", StringArgumentType.word()).suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"default", "true", "false"}, b)).executes(CommandClaim::editGlobalPerm))))
                        .then(Commands.literal("group").then(Commands.argument("group", StringArgumentType.string()).suggests(CommandHelpers::groupSuggestion)
                                .then(Commands.argument("permission", StringArgumentType.word()).suggests((ctx, b) -> CommandHelpers.permSuggestions(ctx, b, true))
                                        .then(Commands.argument("toggle", StringArgumentType.word())
                                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"default", "true", "false"}, b)).executes(CommandClaim::editGroupPerm))))));
        builder.then(Commands.literal("help").executes(ctx -> CommandHelp.helpMessage(ctx, 0, builder.getArguments()))
                .then(Commands.argument("page", IntegerArgumentType.integer()).executes(ctx -> CommandHelp.helpMessage(ctx, builder.getArguments())))
                .then(Commands.literal("cmd").then(Commands.argument("command", StringArgumentType.word()).suggests((ctx, sb) -> SharedSuggestionProvider.suggest(CommandHelp.registeredCommands(ctx, builder.getArguments()), sb)).executes(CommandHelp::helpCmd))));
        builder.then(Commands.literal("?").executes(ctx -> CommandHelp.helpCmd(ctx, "help")));
        dispatcher.register(builder);
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        ConfigHandler.reloadConfigs(context.getSource().getServer());
        context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("configReload")), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int addClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!ItemInteractEvents.canClaimWorld(player.getLevel(), player))
            return 0;
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        BlockPos from = BlockPosArgument.getLoadedBlockPos(context, "from");
        BlockPos to = BlockPosArgument.getLoadedBlockPos(context, "to");
        storage.createClaim(from, to, player);
        return Command.SINGLE_SUCCESS;
    }

    private static int addClaimAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        int usable = data.getClaimBlocks() + data.getAdditionalClaims() - data.usedClaimBlocks();
        int size = (int) Math.floor(Math.sqrt(usable));
        return addClaimRect(context, size, size);
    }

    private static int addClaimRect(CommandContext<CommandSourceStack> context, int x, int z) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!ItemInteractEvents.canClaimWorld(player.getLevel(), player))
            return 0;
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        boolean evenX = x % 2 == 0;
        boolean evenZ = z % 2 == 0;
        BlockPos from = player.blockPosition().offset(evenX ? -(int) ((x - 1) * 0.5) : -(int) (x * 0.5), -5, evenZ ? -(int) ((z - 1) * 0.5) : -(int) (z * 0.5));
        BlockPos to = player.blockPosition().offset((int) (x * 0.5), -5, (int) (z * 0.5));
        storage.createClaim(from, to, player);
        return Command.SINGLE_SUCCESS;
    }

    private static int transferClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Collection<GameProfile> profs = GameProfileArgument.getGameProfiles(context, "player");
        if (profs.size() != 1) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("onlyOnePlayer"), ChatFormatting.RED), false);
            return 0;
        }
        GameProfile prof = profs.iterator().next();
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim == null) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noClaim"), ChatFormatting.RED), false);
            return 0;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        boolean enoughBlocks = true;
        if (!data.isAdminIgnoreClaim()) {
            MinecraftServer server = context.getSource().getServer();
            ServerPlayer newOwner = server.getPlayerList().getPlayer(prof.getId());
            IPlayerData newData = newOwner != null ? PlayerClaimData.get(newOwner) : new OfflinePlayerData(server, prof.getId());
            enoughBlocks = newData.canUseClaimBlocks(claim.getPlane());
        }
        if (!enoughBlocks) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("ownerTransferNoBlocks"), ChatFormatting.RED), false);
            if (PermissionNodeHandler.INSTANCE.perm(context.getSource(), PermissionNodeHandler.cmdAdminMode, true))
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("ownerTransferNoBlocksAdmin"), ChatFormatting.RED), false);
            return 0;
        }
        if (!storage.transferOwner(claim, player, prof.getId())) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("ownerTransferFail"), ChatFormatting.RED), false);
            return 0;
        }
        player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("ownerTransferSuccess"), prof.getName()), ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int openMenu(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        Claim claim = ClaimStorage.get(player.getLevel()).getClaimAt(player.blockPosition());
        if (claim == null) {
            PermHelper.noClaimMessage(player);
            return 0;
        }
        if (data.getEditMode() == EnumEditMode.DEFAULT) {
            ClaimMenuScreenHandler.openClaimMenu(player, claim);
            data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
        } else {
            Claim sub = claim.getSubClaim(player.blockPosition());
            if (sub != null)
                ClaimMenuScreenHandler.openClaimMenu(player, sub);
            else
                ClaimMenuScreenHandler.openClaimMenu(player, claim);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int trapped(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.setTrappedRescue()) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("trappedRescue"), ChatFormatting.GOLD), false);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("trappedFail"), ChatFormatting.RED), false);
        }
        return 0;
    }

    private static int nameClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.getEditMode() == EnumEditMode.DEFAULT) {
            Claim claim = PermHelper.checkReturn(player, PermissionRegistry.EDITPERMS, PermHelper.genericNoPermMessage(player));
            if (claim == null)
                return 0;
            boolean nameUsed = ClaimStorage.get(player.getLevel()).allClaimsFromPlayer(claim.getOwner())
                    .stream().map(Claim::getClaimName).anyMatch(name -> name.equals(StringArgumentType.getString(context, "name")));
            if (!nameUsed) {
                String name = StringArgumentType.getString(context, "name");
                claim.setClaimName(name);
                player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimNameSet"), name), ChatFormatting.GOLD), false);
            } else {
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("claimNameUsed"), ChatFormatting.DARK_RED), false);
            }
        } else {
            Claim claim = ClaimStorage.get(player.getLevel()).getClaimAt(player.blockPosition());
            Claim sub = claim.getSubClaim(player.blockPosition());
            if (sub != null && (claim.canInteract(player, PermissionRegistry.EDITPERMS, player.blockPosition()) || sub.canInteract(player, PermissionRegistry.EDITPERMS, player.blockPosition()))) {
                boolean nameUsed = claim.getAllSubclaims()
                        .stream().map(Claim::getClaimName).anyMatch(name -> name.equals(StringArgumentType.getString(context, "name")));
                if (!nameUsed) {
                    String name = StringArgumentType.getString(context, "name");
                    sub.setClaimName(name);
                    player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimNameSet"), name), ChatFormatting.GOLD), false);
                } else {
                    player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("claimNameUsedSub"), ChatFormatting.DARK_RED), false);
                }
            } else if (claim.canInteract(player, PermissionRegistry.EDITPERMS, player.blockPosition())) {
                boolean nameUsed = ClaimStorage.get(player.getLevel()).allClaimsFromPlayer(claim.getOwner())
                        .stream().map(Claim::getClaimName).anyMatch(name -> name.equals(StringArgumentType.getString(context, "name")));
                if (!nameUsed) {
                    String name = StringArgumentType.getString(context, "name");
                    claim.setClaimName(name);
                    player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimNameSet"), name), ChatFormatting.GOLD), false);
                } else {
                    player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("claimNameUsed"), ChatFormatting.DARK_RED), false);
                }
            } else
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermission"), ChatFormatting.DARK_RED), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int unlockDrops(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        data.unlockDeathItems();
        context.getSource().sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("unlockDrops"), ConfigHandler.config.dropTicks), ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int unlockDropsPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<GameProfile> profs = GameProfileArgument.getGameProfiles(context, "players");
        List<String> success = new ArrayList<>();
        for (GameProfile prof : profs) {
            ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(prof.getId());
            if (player != null) {
                PlayerClaimData data = PlayerClaimData.get(player);
                data.unlockDeathItems();
                success.add(prof.getName());
            }
        }
        context.getSource().sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("unlockDropsMulti"), success), ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int openPersonalGroups(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PersonalGroupScreenHandler.openGroupMenu(player);
        return Command.SINGLE_SUCCESS;
    }

    private static int claimInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return claimInfo(context, CommandHelpers.parseEnum(Claim.InfoType.class, StringArgumentType.getString(context, "type"), Claim.InfoType.ALL));
    }

    private static int claimInfo(CommandContext<CommandSourceStack> context, Claim.InfoType infoType) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Claim claim = ClaimStorage.get(player.getLevel()).getClaimAt(player.blockPosition());
        PlayerClaimData data = PlayerClaimData.get(player);
        if (claim == null) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noClaim"), ChatFormatting.RED), false);
            return 0;
        }
        if (data.getEditMode() == EnumEditMode.SUBCLAIM) {
            Claim sub = claim.getSubClaim(player.blockPosition());
            if (sub != null) {
                List<Component> info = sub.infoString(player, infoType);
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("claimSubHeader"), ChatFormatting.AQUA), false);
                for (Component text : info)
                    player.displayClientMessage(text, false);
                return Command.SINGLE_SUCCESS;
            }
        }
        List<Component> info = claim.infoString(player, infoType);
        for (Component text : info)
            player.displayClientMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        Claim claim = storage.getClaimAt(player.blockPosition());
        boolean check = PermHelper.check(player, player.blockPosition(), claim, PermissionRegistry.EDITCLAIM, b -> {
            if (!b.isPresent())
                PermHelper.noClaimMessage(player);
            else if (!b.get())
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteClaimError"), ChatFormatting.DARK_RED), false);
        });
        if (!check)
            return 0;
        if (!storage.deleteClaim(claim, true, PlayerClaimData.get(player).getEditMode(), player.getLevel())) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteSubClaimError"), ChatFormatting.DARK_RED), false);
        } else {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteClaim"), ChatFormatting.RED), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteAllClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.confirmedDeleteAll()) {
            for (ServerLevel world : player.getServer().getAllLevels()) {
                ClaimStorage storage = ClaimStorage.get(world);
                storage.allClaimsFromPlayer(player.getUUID()).forEach((claim) -> storage.deleteClaim(claim, true, PlayerClaimData.get(player).getEditMode(), player.getLevel()));
            }
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteAllClaim"), ChatFormatting.GOLD), false);
            data.setConfirmDeleteAll(false);
        } else {
            data.setConfirmDeleteAll(true);
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteAllClaimConfirm"), ChatFormatting.DARK_RED), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteSubClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim == null) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noClaim"), ChatFormatting.RED), false);
            return 0;
        }
        Claim sub = claim.getSubClaim(player.blockPosition());
        if (sub == null) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noClaim"), ChatFormatting.RED), false);
            return 0;
        }
        boolean check = PermHelper.check(player, player.blockPosition(), claim, PermissionRegistry.EDITCLAIM, b -> {
            if (!b.isPresent())
                PermHelper.noClaimMessage(player);
            else if (!b.get())
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteClaimError"), ChatFormatting.DARK_RED), false);
            else
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteSubClaim"), ChatFormatting.DARK_RED), false);
        });
        if (!check)
            return 0;
        claim.deleteSubClaim(sub);
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteAllSubClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Claim claim = PermHelper.checkReturn(player, PermissionRegistry.EDITCLAIM, PermHelper.genericNoPermMessage(player));
        if (claim == null)
            return 0;
        List<Claim> subs = claim.getAllSubclaims();
        subs.forEach(claim::deleteSubClaim);
        player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteSubClaimAll"), ChatFormatting.DARK_RED), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int listClaims(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return listClaimsFromUUID(context, null);
    }

    private static int listClaims(CommandContext<CommandSourceStack> context, Collection<GameProfile> profs) throws CommandSyntaxException {
        if (profs.size() != 1) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("onlyOnePlayer"), ChatFormatting.RED), false);
            return 0;
        }
        GameProfile prof = profs.iterator().next();
        if (prof == null || prof.getId() == null)
            return 0;
        return listClaimsFromUUID(context, prof.getId());
    }

    private static int listClaimsFromUUID(CommandContext<CommandSourceStack> context, UUID of) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayer player = of == null ? context.getSource().getPlayerOrException() : server.getPlayerList().getPlayer(of);
        Map<Level, Collection<Claim>> claims = new HashMap<>();
        for (ServerLevel world : server.getAllLevels()) {
            ClaimStorage storage = ClaimStorage.get(world);
            claims.put(world, storage.allClaimsFromPlayer(player != null ? player.getUUID() : of));
        }
        if (ConfigHandler.config.maxClaimBlocks != -1) {
            if (player != null) {
                PlayerClaimData data = PlayerClaimData.get(player);
                context.getSource().sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimBlocksFormat"),
                        data.getClaimBlocks(), data.getAdditionalClaims(), data.usedClaimBlocks(), data.remainingClaimBlocks()), ChatFormatting.GOLD), false);
            } else {
                OfflinePlayerData data = new OfflinePlayerData(server, of);
                context.getSource().sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimBlocksFormat"),
                        data.claimBlocks, data.getAdditionalClaims(), data.usedClaimBlocks(), data.remainingClaimBlocks()), ChatFormatting.GOLD), false);
            }
        }
        context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("listClaims"), ChatFormatting.GOLD), false);
        for (Map.Entry<Level, Collection<Claim>> entry : claims.entrySet())
            for (Claim claim : entry.getValue())
                context.getSource().sendSuccess(PermHelper.simpleColoredText(
                        entry.getKey().dimension().location().toString() + " # " + claim.formattedClaim(), ChatFormatting.YELLOW), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int switchClaimMode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        data.setEditMode(data.getEditMode() == EnumEditMode.DEFAULT ? EnumEditMode.SUBCLAIM : EnumEditMode.DEFAULT);
        player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("editMode"), data.getEditMode()), ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int switchAdminMode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        data.setAdminIgnoreClaim(!data.isAdminIgnoreClaim());
        player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("adminMode"), data.isAdminIgnoreClaim()), ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int adminDelete(CommandContext<CommandSourceStack> context) {
        CommandSourceStack src = context.getSource();
        ClaimStorage storage = ClaimStorage.get(src.getLevel());
        Claim claim = storage.getClaimAt(new BlockPos(src.getPosition()));
        if (claim == null) {
            src.sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noClaim"), ChatFormatting.RED), false);
            return 0;
        }
        storage.deleteClaim(claim, true, EnumEditMode.DEFAULT, src.getLevel());
        src.sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteClaim"), ChatFormatting.RED), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int adminDeleteAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack src = context.getSource();
        if (src.getEntity() instanceof ServerPlayer player) {
            PlayerClaimData data = PlayerClaimData.get(player);
            if (!data.confirmedDeleteAll()) {
                data.setConfirmDeleteAll(true);
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteAllClaimConfirm"), ChatFormatting.DARK_RED), false);
                return Command.SINGLE_SUCCESS;
            }
        }
        List<String> players = new ArrayList<>();
        for (GameProfile prof : GameProfileArgument.getGameProfiles(context, "players")) {
            for (ServerLevel world : src.getLevel().getServer().getAllLevels()) {
                ClaimStorage storage = ClaimStorage.get(world);
                storage.allClaimsFromPlayer(prof.getId()).forEach((claim) -> storage.deleteClaim(claim, true, EnumEditMode.DEFAULT, world));
            }
            players.add(prof.getName());
        }
        src.sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("adminDeleteAll"), players), ChatFormatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int toggleAdminClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim == null) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noClaim"), ChatFormatting.RED), false);
            return 0;
        }
        storage.toggleAdminClaim(player, claim, BoolArgumentType.getBool(context, "toggle"));
        context.getSource().sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("setAdminClaim"), claim.isAdminClaim()), ChatFormatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int listAdminClaims(CommandContext<CommandSourceStack> context) {
        CommandSourceStack src = context.getSource();
        Collection<Claim> claims = ClaimStorage.get(src.getLevel()).getAdminClaims();
        src.sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("listAdminClaims"), src.getLevel().dimension().location()), ChatFormatting.GOLD), false);
        for (Claim claim : claims)
            src.sendSuccess(PermHelper.simpleColoredText(claim.formattedClaim(), ChatFormatting.YELLOW), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int readGriefPreventionData(CommandContext<CommandSourceStack> context) {
        CommandSourceStack src = context.getSource();
        src.sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("readGriefpreventionData"), ChatFormatting.GOLD), true);
        if (ClaimStorage.readGriefPreventionData(src.getServer(), src))
            src.sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("readGriefpreventionClaimDataSuccess"), ChatFormatting.GOLD), true);
        if (PlayerClaimData.readGriefPreventionPlayerData(src.getServer(), src))
            src.sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("readGriefpreventionPlayerDataSuccess"), ChatFormatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int giveClaimBlocks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack src = context.getSource();
        List<String> players = new ArrayList<>();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (GameProfile prof : GameProfileArgument.getGameProfiles(context, "players")) {
            ServerPlayer player = src.getServer().getPlayerList().getPlayer(prof.getId());
            if (player != null) {
                PlayerClaimData data = PlayerClaimData.get(player);
                data.setAdditionalClaims(data.getAdditionalClaims() + amount);
            } else
                PlayerClaimData.editForOfflinePlayer(src.getServer(), prof.getId(), amount);
            players.add(prof.getName());
        }
        src.sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("giveClaimBlocks"), players, amount), ChatFormatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
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
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim == null) {
            PermHelper.noClaimMessage(player);
            return 0;
        }
        if (PlayerClaimData.get(player).getEditMode() == EnumEditMode.SUBCLAIM) {
            Claim sub = claim.getSubClaim(player.blockPosition());
            if (sub != null)
                claim = sub;
        }
        if (remove) {
            if (claim.removePermGroup(player, group))
                player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("groupRemove"), group), ChatFormatting.GOLD), false);
            else {
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermission"), ChatFormatting.DARK_RED), false);
                return 0;
            }
        } else {
            if (claim.groups().contains(group)) {
                player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("groupExist"), group), ChatFormatting.RED), false);
                return 0;
            } else if (claim.editPerms(player, group, PermissionRegistry.EDITPERMS, -1))
                player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("groupAdd"), group), ChatFormatting.GOLD), false);
            else {
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermission"), ChatFormatting.DARK_RED), false);
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
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim == null) {
            PermHelper.noClaimMessage(player);
            return 0;
        }
        if (PlayerClaimData.get(player).getEditMode() == EnumEditMode.SUBCLAIM) {
            Claim sub = claim.getSubClaim(player.blockPosition());
            if (sub != null)
                claim = sub;
        }
        if (!claim.canInteract(player, PermissionRegistry.EDITPERMS, player.blockPosition())) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermission"), ChatFormatting.DARK_RED), false);
            return 0;
        }
        List<String> modified = new ArrayList<>();
        for (GameProfile prof : GameProfileArgument.getGameProfiles(context, "players")) {
            if (claim.setPlayerGroup(prof.getId(), group, force))
                modified.add(prof.getName());
        }
        if (!modified.isEmpty())
            player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("playerModify"), group, modified), ChatFormatting.GOLD), false);
        else
            player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("playerModifyNo"), group), ChatFormatting.RED), false);
        return modified.size();
    }

    private static int toggleFakePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        data.setFakePlayerNotif(!data.hasFakePlayerNotificationOn());
        player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("fakePlayerNotification"), data.hasFakePlayerNotificationOn()), ChatFormatting.GOLD), false);
        return 1;
    }

    private static int addFakePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return modifyFakePlayer(context, false);
    }

    private static int removeFakePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return modifyFakePlayer(context, true);
    }

    private static int modifyFakePlayer(CommandContext<CommandSourceStack> context, boolean remove) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim == null) {
            PermHelper.noClaimMessage(player);
            return 0;
        }
        if (PlayerClaimData.get(player).getEditMode() == EnumEditMode.SUBCLAIM) {
            Claim sub = claim.getSubClaim(player.blockPosition());
            if (sub != null)
                claim = sub;
        }
        if (!claim.canInteract(player, PermissionRegistry.EDITPERMS, player.blockPosition())) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermission"), ChatFormatting.DARK_RED), false);
            return 0;
        }
        UUID uuid = UuidArgument.getUuid(context, "uuid");
        if (claim.modifyFakePlayerUUID(uuid, remove)) {
            if (!remove)
                player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("uuidFakeAdd"), uuid), ChatFormatting.GOLD), false);
            else
                player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("uuidFakeRemove"), uuid), ChatFormatting.GOLD), false);
            return 1;
        }
        player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("uuidFakeModifyNo"), ChatFormatting.RED), false);
        return 0;
    }

    private static int editGlobalPerm(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int mode = switch (StringArgumentType.getString(context, "toggle")) {
            case "true" -> 1;
            case "false" -> 0;
            case "default" -> -1;
            default -> 0;
        };
        return editPerms(context, null, mode);
    }

    private static int editGroupPerm(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int mode = switch (StringArgumentType.getString(context, "toggle")) {
            case "true" -> 1;
            case "false" -> 0;
            case "default" -> -1;
            default -> 0;
        };
        return editPerms(context, StringArgumentType.getString(context, "group"), mode);
    }

    private static int editPerms(CommandContext<CommandSourceStack> context, String group, int mode) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Claim claim = ClaimStorage.get(player.getLevel()).getClaimAt(player.blockPosition());
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.getEditMode() == EnumEditMode.SUBCLAIM) {
            Claim sub = claim.getSubClaim(player.blockPosition());
            if (sub != null)
                claim = sub;
        }
        if (claim == null) {
            PermHelper.noClaimMessage(player);
            return 0;
        }
        if (!claim.canInteract(player, PermissionRegistry.EDITPERMS, player.blockPosition())) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermission"), ChatFormatting.DARK_RED), false);
            return 0;
        }
        ClaimPermission perm;
        String p = StringArgumentType.getString(context, "permission");
        try {
            perm = PermissionRegistry.get(p);
            if (group != null && PermissionRegistry.globalPerms().contains(perm))
                throw new IllegalArgumentException();
        } catch (NullPointerException e) {
            player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("noSuchPerm"), p), ChatFormatting.DARK_RED), false);
            return 0;
        }
        String setPerm = mode == 1 ? "true" : mode == 0 ? "false" : "default";
        if (group == null) {
            claim.editGlobalPerms(player, perm, mode);
            player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("editPerm"), perm, setPerm), ChatFormatting.GOLD), false);
        } else {
            claim.editPerms(player, group, perm, mode);
            player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("editPermGroup"), perm, group, setPerm), ChatFormatting.GOLD), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int editPersonalPerm(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String group = StringArgumentType.getString(context, "group");
        int mode = switch (StringArgumentType.getString(context, "toggle")) {
            case "true" -> 1;
            case "false" -> 0;
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
            player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("noSuchPerm"), p), ChatFormatting.DARK_RED), false);
            return 0;
        }
        String setPerm = mode == 1 ? "true" : mode == 0 ? "false" : "default";
        if (PlayerClaimData.get(player).editDefaultPerms(group, perm, mode))
            player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("editPersonalGroup"), group, perm, setPerm), ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int setClaimHome(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Claim claim = PermHelper.checkReturn(player, PermissionRegistry.EDITCLAIM, PermHelper.genericNoPermMessage(player));
        if (claim == null)
            return 0;
        claim.setHomePos(player.blockPosition());
        context.getSource().sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("setHome"), player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ()), ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int expandClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Claim claim = PermHelper.checkReturn(player, PermissionRegistry.EDITCLAIM, PermHelper.genericNoPermMessage(player));
        if (claim == null)
            return 0;

        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        int amount = IntegerArgumentType.getInteger(context, "distance");
        int[] dims = claim.getDimensions();
        int x = dims[0];
        int X = dims[1];
        int z = dims[2];
        int Z = dims[3];
        int y = dims[4];

        Direction facing = player.getDirection();

        Tuple<BlockPos, BlockPos> cornerPair = switch (facing) {
            case SOUTH -> new Tuple<>(new BlockPos(X, y, Z), new BlockPos(X, y, Z + amount));
            case EAST -> new Tuple<>(new BlockPos(X, y, Z), new BlockPos(X + amount, y, Z));
            case NORTH -> new Tuple<>(new BlockPos(x, y, z), new BlockPos(x, y, z - amount));
            case WEST -> new Tuple<>(new BlockPos(x, y, z), new BlockPos(x - amount, y, z));
            default -> throw new IllegalStateException("Unexpected value: " + facing);
        };

        return storage.resizeClaim(claim, cornerPair.getA(), cornerPair.getB(), player) ? Command.SINGLE_SUCCESS : 0;
    }

    public static int teleport(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return teleport(context, context.getSource().getPlayerOrException().getUUID());
    }

    public static int teleportAdminClaims(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return teleport(context, null);
    }

    public static int teleport(CommandContext<CommandSourceStack> context, UUID owner) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(context, "claim");
        Optional<Claim> claims = ClaimStorage.get(player.getLevel()).allClaimsFromPlayer(owner)
                .stream().filter(claim -> {
                    if (claim.getClaimName().isEmpty())
                        return claim.getClaimID().toString().equals(name);
                    return claim.getClaimName().equals(name);
                }).findFirst();
        if (claims.isEmpty()) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("teleportNoClaim"), ChatFormatting.RED), false);
            return 0;
        }
        return claims.map(claim -> {
            BlockPos pos = claim.getHomePos();
            if (claim.canInteract(player, PermissionRegistry.TELEPORT, pos, false)) {
                PlayerClaimData data = PlayerClaimData.get(player);
                if (data.setTeleportTo(pos)) {
                    context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("teleportHome"), ChatFormatting.GOLD), false);
                    return Command.SINGLE_SUCCESS;
                }
                context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("teleportHomeFail"), ChatFormatting.RED), false);
            } else
                context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermissionSimple"), ChatFormatting.DARK_RED), false);
            return 0;
        }).orElse(0);
    }

    public static int editClaimMessages(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return editClaimMessages(context, new TextComponent(StringArgumentType.getString(context, "message")));
    }

    public static int editClaimMessages(CommandContext<CommandSourceStack> context, Component text) throws CommandSyntaxException {
        if (text instanceof MutableComponent) {
            Style style = text.getStyle();
            if (style.isEmpty())
                style = style.applyFormat(ChatFormatting.WHITE);
            if (!style.isItalic())
                style = style.withItalic(false);
            ((MutableComponent) text).setStyle(style);
        }
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        Claim rootClaim = PermHelper.checkReturn(player, PermissionRegistry.CLAIMMESSAGE, PermHelper.genericNoPermMessage(player));
        if (rootClaim == null)
            return 0;
        Claim claim = data.getEditMode() == EnumEditMode.SUBCLAIM ? rootClaim.getSubClaim(player.blockPosition()) : rootClaim;
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
        MutableComponent cmdFeed = new TranslatableComponent(feedback, text).withStyle(ChatFormatting.GOLD);
        context.getSource().sendSuccess(cmdFeed, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int sellClaimBlocks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean b = ConfigHandler.config.buySellHandler.sell(context.getSource().getPlayerOrException(), Math.max(0, IntegerArgumentType.getInteger(context, "amount")), m -> context.getSource().sendSuccess(m, false));
        return b ? Command.SINGLE_SUCCESS : 0;
    }

    private static int buyClaimBlocks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean b = ConfigHandler.config.buySellHandler.buy(context.getSource().getPlayerOrException(), Math.max(0, IntegerArgumentType.getInteger(context, "amount")), m -> context.getSource().sendSuccess(m, false));
        return b ? Command.SINGLE_SUCCESS : 0;
    }
}
