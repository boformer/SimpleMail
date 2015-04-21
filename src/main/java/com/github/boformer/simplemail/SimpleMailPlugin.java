/*
 * This file is part of SimpleMail, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 Felix Schmidt <https://github.com/boformer>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.boformer.simplemail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.ConfigDir;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.args.ChildCommandElementExecutor;
import org.spongepowered.api.util.command.args.GenericArguments;
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.Subscribe;

import com.github.boformer.simplemail.command.ClearSubcommand;
import com.github.boformer.simplemail.command.ReadSubcommand;
import com.github.boformer.simplemail.command.SendSubcommand;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.sun.javafx.collections.MappingChange.Map;

@Plugin(id = "SimpleMail", name = "SimpleMail", version = "0.1.0")
public class SimpleMailPlugin {
	// Injected by Sponge on server startup
	@Inject
	private Game game;

	@Inject
	private Logger logger;

	// The configuration folder for this plugin
	@Inject
	@ConfigDir(sharedRoot = false)
	private File configDir;

	// The config manager for the mail storage file
	private ConfigurationLoader<CommentedConfigurationNode> mailStorageConfigLoader;

	// The in-memory version of the mail storage file
	private CommentedConfigurationNode mailStorageConfig;

	/**
	 * Called on server startup.
	 * 
	 * @param event
	 *            The server startup event
	 */
	@Subscribe
	public void onPreInitialization(PreInitializationEvent event) {
		// Create a custom configuration file for mail storage
		// {server-root}/config/SimpleMail/mails.conf
		File mailStorageFile = new File(configDir, "mails.conf");

		this.mailStorageConfigLoader = HoconConfigurationLoader.builder()
				.setFile(mailStorageFile).build();

		try {
			// Create the folder if it does not exist
			if (!configDir.isDirectory())
				configDir.mkdirs();

			// Create the file if it does not exist
			if (!mailStorageFile.isFile())
				mailStorageFile.createNewFile();

			// Load the stored mails
			mailStorageConfig = mailStorageConfigLoader.load();
		} catch (IOException e) {
			logger.error("Unable to create or load mail storage file!");
			e.printStackTrace();

			// Cancel plugin startup
			return;
		}

		// Build the command structure
		
		// 1) subcommands of the /mail command
		HashMap<List<String>, CommandSpec> subcommands = new HashMap<>();
		
		// 1a) /mail read
		subcommands.put(Arrays.asList("read", "inbox"), CommandSpec.builder()
		        .setPermission("simplemail.read")
		        .setDescription(Texts.of("Read your inbox"))
		        .setExtendedDescription(Texts.of("Displays the server mails you received."))
		        .setExecutor(new ReadSubcommand(this)) // <-- command logic is in there
		        .build());
		
        // 1b) /mail clear
        subcommands.put(Arrays.asList("clear", "delete"), CommandSpec.builder()
                .setPermission("simplemail.clear")
                .setDescription(Texts.of("Clear your inbox"))
                .setExtendedDescription(Texts.of("This will delete all messages!"))
                .setExecutor(new ClearSubcommand(this))
                .build());
        
		// 1c) /mail send <player> <msg>
        subcommands.put(Arrays.asList("send", "write"), CommandSpec.builder()
                .setPermission("simplemail.send")
                .setDescription(Texts.of("Send a mail"))
                .setExtendedDescription(Texts.of("Mails will only be visible to players on this server"))
                .setArguments(GenericArguments.seq(
                        GenericArguments.string(Texts.of("player")), // "string(...)" instead of "player(...)" to support offline players
                        GenericArguments.remainingJoinedStrings(Texts.of("msg"))))
                .setExecutor(new SendSubcommand(this))
                .build());
		
        // 2) main command
		CommandSpec mailCommand = CommandSpec
				.builder()
				.setDescription(Texts.of("Send and receive mails"))
				.setExtendedDescription(Texts.of("Mails will only be visible to players on this server"))
				.setChildren(subcommands) // register subcommands
				.build();

		// Register the mail command
		game.getCommandDispatcher().register(this, mailCommand, "mail");

	}

	/**
	 * Called when a player logs in.
	 * 
	 * @param event
	 *            The player join event
	 */
	@Subscribe(order = Order.POST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Inform player about new mails
		sendMailNotification(event.getPlayer());
	}

	/**
	 * Sends the mail text to the recipient.
	 * 
	 * @param recipient
	 *            The recipient name
	 * @param mail
	 *            The mail text
	 */
	public void sendMail(String recipient, String mail) {
		// Config section where the mails of the recipient are saved
		CommentedConfigurationNode recipientNode = this.mailStorageConfig
				.getNode(recipient.toLowerCase());

		// Get the list of mails for this player from the storage
		@SuppressWarnings("unchecked")
		List<String> mails = (List<String>) recipientNode.getValue();

		// No mails found?
		if (mails == null)
			mails = new ArrayList<>();

		// Add the new mail to the list of mails
		mails.add(mail);

		// TODO is this necessary?
		recipientNode.setValue(mails);

		// Save the changed mail storage file
		saveMailStorageConfig();

		// Check if recipient is a logged in player
		Optional<Player> optional = game.getServer().getPlayer(recipient);
		if (optional.isPresent()) {
			// Send notification to recipient
			sendMailNotification(optional.get());
		}
	}

	/**
	 * Returns the list of mails of a recipient.
	 * 
	 * @param recipient
	 *            The inbox owner name
	 * @return List of mails
	 */
	public List<String> getMails(String recipient) {
		// Get the list of mails for this player from the storage
		@SuppressWarnings("unchecked")
		List<String> mails = (List<String>) this.mailStorageConfig.getNode(
				recipient.toLowerCase()).getValue();

		// Return list of mails or empty list if no mails found
		if (mails == null)
			return Collections.emptyList();
		else
			return Collections.unmodifiableList(mails);
	}

	/**
	 * Clears the inbox of a recipient
	 * 
	 * @param recipient
	 *            The inbox owner name
	 */
	public void clearMails(String recipient) {
		// Remove config section where the mails of the recipient are saved
		this.mailStorageConfig.removeChild(recipient.toLowerCase());

		// Save the changed mail storage file
		saveMailStorageConfig();
	}

	/**
	 * Saves the mail storage config file.
	 */
	private void saveMailStorageConfig() {
		try {
			this.mailStorageConfigLoader.save(this.mailStorageConfig);
		} catch (IOException e) {
			logger.error("Unable to save mail storage file!");
			e.printStackTrace();
		}
	}

	/**
	 * Sends a notification message to a player who has new mails.
	 * 
	 * @param player
	 *            The player
	 */
	private void sendMailNotification(Player player) {
		// Get mails for player
		List<String> mails = getMails(player.getName());

		// No mails --> do nothing
		if (mails.isEmpty())
			return;

		// Inform player about new mails
		player.sendMessage(Texts.of("You got new mails! Read with /mail read"));
	}

}
