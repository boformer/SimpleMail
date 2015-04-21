package com.github.boformer.simplemail.command;

import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

import com.github.boformer.simplemail.SimpleMailPlugin;

/**
 * The clear subcommand.
 */
public class ClearSubcommand implements CommandExecutor {

    private SimpleMailPlugin plugin;

    public ClearSubcommand(SimpleMailPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args)
            throws CommandException {
        // Clear inbox
        this.plugin.clearMails(src.getName());
        src.sendMessage(Texts.of("Inbox cleared!"));

        return CommandResult.success();
    }

}
