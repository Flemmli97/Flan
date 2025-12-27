package io.github.flemmli97.flan.commands.sub;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.OfflinePlayerData;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ListClaimCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("list").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_LIST)).executes(ListClaimCommand::listClaims).then(Commands.argument("player", GameProfileArgument.gameProfile()).requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_LIST_ALL, true))
                        .executes(cmd -> ListClaimCommand.listClaims(cmd, GameProfileArgument.getGameProfiles(cmd, "player")))))
                .then(Commands.literal("listAdminClaims").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_ADMIN_LIST, true)).executes(ListClaimCommand::listAdminClaims));
    }

    private static int listClaims(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return listClaimsFromUUID(context, null);
    }

    private static int listClaims(CommandContext<CommandSourceStack> context, Collection<NameAndId> profs) throws CommandSyntaxException {
        if (profs.size() != 1) {
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.onlyOnePlayer", ChatFormatting.RED), false);
            return 0;
        }
        NameAndId prof = profs.iterator().next();
        if (prof == null || prof.id() == null)
            return 0;
        return listClaimsFromUUID(context, prof.id());
    }

    private static int listClaimsFromUUID(CommandContext<CommandSourceStack> context, UUID of) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayer player = of == null ? context.getSource().getPlayerOrException() : server.getPlayerList().getPlayer(of);
        Map<Level, Collection<Claim>> claims = new HashMap<>();
        for (ServerLevel level : server.getAllLevels()) {
            ClaimStorage storage = ClaimStorage.get(level);
            claims.put(level, storage.allClaimsFromPlayer(player != null ? player.getUUID() : of));
        }
        if (ConfigHandler.CONFIG.maxClaimBlocks != -1) {
            if (player != null) {
                PlayerClaimData data = PlayerClaimData.get(player);
                context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.claimBlocksFormat",
                        data.getClaimBlocks(), data.getAdditionalClaims(), data.usedClaimBlocks(), data.remainingClaimBlocks(), ChatFormatting.GOLD), false);
            } else {
                OfflinePlayerData data = new OfflinePlayerData(server, of);
                context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.claimBlocksFormat",
                        data.claimBlocks, data.getAdditionalClaims(), data.usedClaimBlocks(), data.remainingClaimBlocks(), ChatFormatting.GOLD), false);
            }
        }
        context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.listClaims", ChatFormatting.GOLD), false);
        for (Map.Entry<Level, Collection<Claim>> entry : claims.entrySet()) {
            for (Claim claim : entry.getValue()) {
                context.getSource().sendSuccess(() -> ClaimUtils.translatedText(
                        entry.getKey().dimension().location() + " # " + claim.formattedClaim(), ChatFormatting.YELLOW), false);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int listAdminClaims(CommandContext<CommandSourceStack> context) {
        CommandSourceStack src = context.getSource();
        Map<Level, Collection<Claim>> claims = new HashMap<>();
        for (ServerLevel level : src.getServer().getAllLevels()) {
            claims.put(level, ClaimStorage.get(level).getAdminClaims());
        }
        src.sendSuccess(() -> ClaimUtils.translatedText("flan.listAdminClaims", src.getLevel().dimension().location(), ChatFormatting.GOLD), false);
        for (Map.Entry<Level, Collection<Claim>> entry : claims.entrySet()) {
            for (Claim claim : entry.getValue()) {
                src.sendSuccess(() -> ClaimUtils.translatedText(
                        entry.getKey().dimension().location() + " # " + claim.formattedClaim(), ChatFormatting.YELLOW), false);
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
