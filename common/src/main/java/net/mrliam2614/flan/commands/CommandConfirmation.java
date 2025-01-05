package net.mrliam2614.flan.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

import java.util.HashMap;
import java.util.UUID;

/**
 * The {@code CommandConfirmation} class manages command operations that require user confirmation before being executed.
 * It maintains a registry of commands awaiting confirmation and provides mechanisms for requesting and confirming such commands.
 */
public class CommandConfirmation {
    public static CommandConfirmation INSTANCE;
    private HashMap<UUID, CommandCallback> awaitingConfirmation;

    public CommandConfirmation() {
        INSTANCE = this;
        awaitingConfirmation = new HashMap<>();
    }

    /**
     * Requests confirmation from a player for a specific command operation. If the player is active, it adds the command
     * callback to a list awaiting confirmation and sends a confirmation message to the player.
     *
     * @param context The {@link CommandContext} containing information about the command execution,
     *                including the source of the command (player or console).
     * @param commandCallback The {@link CommandCallback} instance representing the action to be executed upon confirmation.
     */
    public void requestConfirmation(CommandContext<CommandSourceStack> context, CommandCallback commandCallback) {
        if(context.getSource().getPlayer() == null){
            return;
        }
        UUID uuid = context.getSource().getPlayer().getUUID();
        awaitingConfirmation.put(uuid, commandCallback);
        context.getSource().sendSuccess(() -> PermHelper.simpleColoredText(ConfigHandler.LANG_MANAGER.get("command.confirmation.confirmMessage"), ChatFormatting.AQUA), true);
    }

    /**
     * Processes a confirmation action for a command that is awaiting user confirmation.
     * If the command source is eligible (e.g., a player), it retrieves and executes the
     * associated {@link CommandCallback} if present.
     *
     * @param context The {@link CommandContext} containing information about the command execution,
     *                including the source of the command (e.g., player or console).
     * @return An integer value indicating the outcome of the confirmed command execution.
     *         Returns 0 if no confirmation action was processed, such as when the source
     *         is not a player or no command callback is awaiting confirmation.
     */
    public int confirmCommand(CommandContext<CommandSourceStack> context){
        if(context.getSource().getPlayer() == null){
            return 0;
        }
        UUID uuid = context.getSource().getPlayer().getUUID();
        CommandCallback commandCallback = awaitingConfirmation.remove(uuid);
        if(commandCallback == null){
            return 0;
        }
        return commandCallback.onConfirm(context);
    }
}
