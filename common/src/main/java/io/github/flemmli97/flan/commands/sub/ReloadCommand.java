package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ReloadCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("reload").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_RELOAD, true)).executes(ReloadCommand::reloadConfig));
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        ConfigHandler.reloadConfigs(context.getSource().getServer());
        context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.configReload"), true);
        return Command.SINGLE_SUCCESS;
    }
}
