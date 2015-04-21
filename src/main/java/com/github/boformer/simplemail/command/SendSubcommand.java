package com.github.boformer.simplemail.command;

import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

import com.github.boformer.simplemail.SimpleMailPlugin;

/**
 * The send subcommand.
 */
public class SendSubcommand implements CommandExecutor {

	private SimpleMailPlugin plugin;

	public SendSubcommand(SimpleMailPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args)
			throws CommandException {
		String recipient = (String) args.getOne("player").orNull();
		String mailContent = (String) args.getOne("msg").orNull();

		// If there is no message, return that command was not successful
		if (mailContent == null || mailContent == "") {
			return CommandResult.empty();
		}

		// TODO check if the player name exists, else cancel.

		// Format: "sender: the message"
		String mail = src.getName() + ": " + mailContent;

		// Send the mail
		this.plugin.sendMail(recipient, mail);

		src.sendMessage(Texts.of("Mail sent to " + recipient + "."));

		return CommandResult.success();
	}
}
