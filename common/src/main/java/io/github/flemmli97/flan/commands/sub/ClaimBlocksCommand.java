package io.github.flemmli97.flan.commands.sub;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.ArrayList;
import java.util.List;

public class ClaimBlocksCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("giveClaimBlocks").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_ADMIN_GIVE, true)).then(Commands.argument("players", GameProfileArgument.gameProfile())
                .then(Commands.argument("amount", IntegerArgumentType.integer()).executes(ClaimBlocksCommand::giveClaimBlocks))
                .then(Commands.literal("base").then(Commands.argument("amount", IntegerArgumentType.integer()).executes(ctx -> ClaimBlocksCommand.giveClaimBlocks(ctx, true))))));
    }

    private static int giveClaimBlocks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return giveClaimBlocks(context, false);
    }

    private static int giveClaimBlocks(CommandContext<CommandSourceStack> context, boolean base) throws CommandSyntaxException {
        CommandSourceStack src = context.getSource();
        List<String> players = new ArrayList<>();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (NameAndId prof : GameProfileArgument.getGameProfiles(context, "players")) {
            ServerPlayer player = src.getServer().getPlayerList().getPlayer(prof.id());
            if (player != null) {
                PlayerClaimData data = PlayerClaimData.get(player);
                if (base)
                    data.addClaimBlocksDirect(amount);
                else
                    data.setAdditionalClaims(data.getAdditionalClaims() + amount);
            } else
                PlayerClaimData.editForOfflinePlayer(src.getServer(), prof.id(), amount, base);
            players.add(prof.name());
        }
        src.sendSuccess(() -> ClaimUtils.translatedText(base ? "flan.giveClaimBlocks" : "flan.giveClaimBlocksBonus", players, amount, ChatFormatting.GOLD), true);
        return players.size();
    }
}
