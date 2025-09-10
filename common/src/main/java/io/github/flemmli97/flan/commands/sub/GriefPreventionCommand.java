package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class GriefPreventionCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("readGriefPrevention").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_GRIEF_PREVENTION, true)).executes(GriefPreventionCommand::readGriefPreventionData));
    }

    private static int readGriefPreventionData(CommandContext<CommandSourceStack> context) {
        CommandSourceStack src = context.getSource();
        src.sendSuccess(() -> ClaimUtils.translatedText("flan.readGriefpreventionData", ChatFormatting.GOLD), true);
        if (ClaimStorage.readGriefPreventionData(src.getServer(), src))
            src.sendSuccess(() -> ClaimUtils.translatedText("flan.readGriefpreventionClaimDataSuccess", ChatFormatting.GOLD), true);
        if (PlayerClaimData.readGriefPreventionPlayerData(src.getServer(), src))
            src.sendSuccess(() -> ClaimUtils.translatedText("flan.readGriefpreventionPlayerDataSuccess", ChatFormatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }
}
