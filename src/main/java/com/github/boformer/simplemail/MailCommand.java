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

import java.util.List;

import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.dispatcher.SimpleDispatcher;

/**
 * The main command class.
 */
public class MailCommand extends SimpleDispatcher 
// Class extends SimpleDispatcher which allows the registration of subcommands.
{
    private final SimpleMailPlugin plugin;
    
    public MailCommand(SimpleMailPlugin plugin) 
    {
        super(  // description
                "Send and receive mails", 
                // help
                Texts.of("Send and receive server mails.\n" 
                        + "Mails will only be visible to players on this server "
                        + "and will not persist when the server restarts!"));
        
        // Save plugin instance (needed for mail methods)
        this.plugin = plugin;
        
        // Register the subcommands
        // /mail read,  /mail send <player> <msg>,  /mail clear
        this.register(new ReadSubcommand(), "read", "inbox");
        this.register(new SendSubcommand(), "send", "write");
        this.register(new ClearSubcommand(), "clear", "delete");
    }
    
    @Override
    public boolean testPermission(CommandSource source) 
    {
        return true; //TODO right now there is a bug in SpongeAPI (https://github.com/SpongePowered/SpongeAPI/pull/533) that makes this necessary!
    }
    
    /**
     * The read subcommand.
     */
    private class ReadSubcommand extends SimpleCommandBase
    // Class extends AbstractCommand, a helper class defined in a separate file
    {
        ReadSubcommand()
        {
            super(  // permission
                    "simplemail.read", 
                    // description
                    "Read your inbox", 
                    //help
                    Texts.of("Displays the server mails you received."), 
                    // usage
                    "");
        }

        @Override
        public boolean call(CommandSource source, String arguments, List<String> parents) throws CommandException
        {
            // Get list of mails for user
            List<String> mails = plugin.getMails(source.getName());
            
            source.sendMessage(Texts.of("--- Your Inbox ---"));
            
            // Display list of mails
            if(mails.isEmpty()) source.sendMessage(Texts.of("No mails in your inbox."));
            else
            {
                for(String mail : mails) 
                {
                    source.sendMessage(Texts.of(mail));
                }
            }
            
            // TODO Add paging: /mail read <page>
            
            return true;
        }
    }
    
    /**
     * The send subcommand.
     */
    private class SendSubcommand extends SimpleCommandBase
    // Class extends AbstractCommand, a helper class defined in a separate file
    {
        SendSubcommand()
        {
            super(  // permission
                    "simplemail.write", 
                    // description
                    "Send a mail", 
                    // help
                    Texts.of("Send a server mail to a player."), 
                    // usage
                    "<player> <msg>");
        }
    
        @Override
        public boolean call(CommandSource source, String arguments, List<String> parents) throws CommandException
        {
            // Split command arguments into <recipient> and <message>
            String[] parts = arguments.split(" +", 2);
            
            // If there is no message, return that command was not successful
            if(parts.length < 2 || parts[1].equals("")) return false;
            
            String recipient = parts[0];
            String mailContent = parts[1];
            
            // TODO check if the player name exists, else cancel.
            
            
            // Format: "sender: the message"
            String mail = source.getName() + ": " + mailContent;
            
            // Send the mail
            plugin.sendMail(recipient, mail);
            
            source.sendMessage(Texts.of("Mail sent to " + recipient + "."));
            
            return true;
        }
        
        // TODO TAB completion for recipient argument
    }
    
    /**
     * The clear subcommand.
     */
    private class ClearSubcommand extends SimpleCommandBase
    // Class extends AbstractCommand, a helper class defined in a separate file
    {
        ClearSubcommand()
        {
            super(  // permission
                    "simplemail.clear", 
                    // description
                    "Clear your inbox", 
                    // help
                    Texts.of("Clear your server mail inbox. This will delete all messages!"), 
                    // usage
                    "");
        }
    
        @Override
        public boolean call(CommandSource source, String arguments, List<String> parents) throws CommandException
        {
            // Clear inbox
            plugin.clearMails(source.getName());
            source.sendMessage(Texts.of("Inbox cleared!"));
            
            return true;
        }
    }
}
