package com.technovision.technobot.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class AutomodListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();

        if(message.toLowerCase().contains("https://discord.gg/")) {
            event.getMessage().delete().queue();
            event.getChannel().sendMessage("<@!"+event.getAuthor().getId()+">, please don't post invites here!").queue();
        }
        // More to come soon
        // Note; it isn't really possible to "censor" profanities, because you cannot edit people's messages
    }
}
