package io.github.flemmli97.flan.commands.sub;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UnlockDropsCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("unlockDrops").executes(UnlockDropsCommand::unlockDrops)
                .then(Commands.argument("players", GameProfileArgument.gameProfile()).requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_UNLOCK_ALL, true)).executes(UnlockDropsCommand::unlockDropsPlayers)));
    }

    private static int unlockDrops(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        data.unlockDeathItems();
        context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.unlockDrops", ConfigHandler.CONFIG.dropTicks, ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int unlockDropsPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<NameAndId> profs = GameProfileArgument.getGameProfiles(context, "players");
        List<String> success = new ArrayList<>();
        for (NameAndId prof : profs) {
            ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(prof.id());
            if (player != null) {
                PlayerClaimData data = PlayerClaimData.get(player);
                data.unlockDeathItems();
                success.add(prof.name());
            }
        }
        context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.unlockDropsMulti", success, ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }
}
