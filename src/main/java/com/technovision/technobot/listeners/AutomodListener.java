package com.technovision.technobot.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class AutomodListener extends ListenerAdapter {

    public static final String ADVERTISE_CHANNEL = "collab-advertise";

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (event.getAuthor().isBot()) { return; }
        if(message.toLowerCase().contains("discord.gg/")) {
            if (!event.getChannel().getName().equals(ADVERTISE_CHANNEL)) {
                event.getMessage().delete().queue();
                event.getChannel().sendMessage("<@!" + event.getAuthor().getId() + ">, " + "please only post invites in <#730661431703502959>!").queue();
            }
        }
        // More to come soon
    }
}
