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

import java.util.List;

import com.github.boformer.simplemail.SimpleMailPlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import com.github.boformer.simplemail.SimpleMailPlugin;
import org.spongepowered.api.text.Text;

/**
 * The read subcommand.
 */
public class ReadSubcommand implements CommandExecutor {

    private SimpleMailPlugin plugin;

    public ReadSubcommand(SimpleMailPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        // Get list of mails for user
        List<String> mails = this.plugin.getMails(src.getName());

        src.sendMessage(Text.of("--- Your Inbox ---"));

        // Display list of mails
        if (mails.isEmpty()) {
            src.sendMessage(Text.of("No mails in your inbox."));
        } else {
            for (String mail : mails) {
                src.sendMessage(Text.of(mail));
            }
        }

        // TODO Add paging: /mail read <page>

        // returns the number of mails in the inbox.
        return CommandResult.builder().queryResult(mails.size()).build();
    }

}
