package com.github.boformer.simplemail.command;

import java.util.List;

import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

import com.github.boformer.simplemail.SimpleMailPlugin;

/**
 * The read subcommand.
 */
public class ReadSubcommand implements CommandExecutor {
	
    private SimpleMailPlugin plugin;

	public ReadSubcommand(SimpleMailPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) {
		// Get list of mails for user
		List<String> mails = this.plugin.getMails(src.getName());

		src.sendMessage(Texts.of("--- Your Inbox ---"));

		// Display list of mails
		if (mails.isEmpty()) {
			src.sendMessage(Texts.of("No mails in your inbox."));
		} else {
			for (String mail : mails) {
				src.sendMessage(Texts.of(mail));
			}
		}

		// TODO Add paging: /mail read <page>

		// returns the number of mails in the inbox.
		return CommandResult.builder().queryResult(mails.size()).build();
	}

}
