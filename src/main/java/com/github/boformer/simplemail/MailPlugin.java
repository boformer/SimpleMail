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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.spongepowered.api.Game;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.event.Subscribe;

import com.google.inject.Inject;

@Plugin(id = "boformer.simplemail", name = "SimpleMail", version = "0.1.0")
public class MailPlugin 
{
    // Injected by Sponge on server startup
    @Inject
    private Game game;
    
    // Mail storage: player name --> list of mails
    private HashMap<String, ArrayList<Text>> mailMap;
    
    // TODO store mails in config/database when server shuts down!

    
    /**
     * Called on server startup.
     * 
     * @param event
     */
    @Subscribe
    private void onPreInitialization(PreInitializationEvent event) 
    {
        
        // Initialize mail storage
        mailMap = new HashMap<>();
       
        // Register the mail command
    	game.getCommandDispatcher().register(this, new MailCommand(this), "mail");
    }
    
    
    /**
     * Sends the mail text to the recipient.
     * 
     * @param recipient The recipient name
     * @param mail The mail text
     */
    public void sendMail(String recipient, Text mail) 
    {
        ArrayList<Text> mails = mailMap.get(recipient.toLowerCase());
        
        if(mails == null) 
        {
            mails = new ArrayList<>();
            mailMap.put(recipient.toLowerCase(), mails);
        }
        
        mails.add(mail);
    }
    
    
    /**
     * Returns the list of mails of a recipient.
     * 
     * @param recipient The inbox owner name
     * @return List of mails
     */
    public List<Text> getMails(String recipient) 
    {
        ArrayList<Text> mails = mailMap.get(recipient.toLowerCase());
        
        if(mails == null) return Collections.emptyList();
        else return Collections.unmodifiableList(mails);
    }
    
    
    /**
     * Clears the inbox of a recipient
     * 
     * @param recipient The inbox owner name
     */
    public void clearMails(String recipient) 
    {
        mailMap.remove(recipient.toLowerCase());
    }
    
}
