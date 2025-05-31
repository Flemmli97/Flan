package io.github.flemmli97.flan.commands;

import com.mojang.brigadier.context.CommandContext;
import io.github.flemmli97.flan.claim.ClaimUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Supplier;

public class PendingCommand {

    private final CommandContext<CommandSourceStack> ctx;
    private final Supplier<Integer> runCommand;

    public PendingCommand(CommandContext<CommandSourceStack> ctx, Supplier<Integer> runCommand) {
        this.ctx = ctx;
        this.runCommand = runCommand;
    }

    public int runCommand() {
        return this.runCommand.get();
    }

    public void deny() {
        this.ctx.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.confirmCancelled", ChatFormatting.RED), true);
    }
}
