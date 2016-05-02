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
package com.github.boformer.simplemail.command;

import com.github.boformer.simplemail.SimpleMailPlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import com.github.boformer.simplemail.SimpleMailPlugin;
import org.spongepowered.api.text.Text;

/**
 * The send subcommand.
 */
public class SendSubcommand implements CommandExecutor {

    private SimpleMailPlugin plugin;

    public SendSubcommand(SimpleMailPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String recipient = (String) args.getOne("player").orElse(null);
        String mailContent = (String) args.getOne("msg").orElse(null);

        // If there is no message, return that command was not successful
        if (mailContent == null || mailContent.equals("")) {
            return CommandResult.empty();
        }

        // TODO check if the player name exists, else cancel.

        // Format: "sender: the message"
        String mail = src.getName() + ": " + mailContent;

        // Send the mail
        this.plugin.sendMail(recipient, mail);

        src.sendMessage(Text.of("Mail sent to " + recipient + "."));

        return CommandResult.success();
    }
}
