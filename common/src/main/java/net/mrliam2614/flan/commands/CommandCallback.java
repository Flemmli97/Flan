package net.mrliam2614.flan.commands;

import com.mojang.brigadier.context.CommandContext;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

/**
 * Interface representing a callback mechanism for commands that require user confirmation before proceeding.
 * <p>
 * Provides a method to handle the confirmation action (`onConfirm`) and a default implementation to handle the
 * denial or cancellation of the operation (`onDeny`).
 * <p>
 * If the command is called not a player (console) is automatically called the (`onConfirm`)
 */
public interface CommandCallback {

    /**
     * Called when the command is confirmed by the user.
     *
     * @param context The {@link CommandContext} for the current command execution,
     *                containing information about the source and arguments of the command.
     * @return An integer value typically used to indicate the result of command execution.
     */
    int onConfirm(CommandContext<CommandSourceStack> context);

    /**
     * Called when the command is denied or canceled by the user.
     * This method provides a default implementation that sends a cancellation message
     * to the command source using the configured language manager and displays it in red formatting.
     *
     * @param context The {@link CommandContext} for the current command execution,
     *                containing information about the source and arguments of the command.
     */
    default void onDeny(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
                PermHelper.simpleColoredText(
                        ConfigHandler.LANG_MANAGER.get("command.confirmation.cancelled"),
                        ChatFormatting.RED
                ), true
        );
    }
}