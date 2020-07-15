package com.technovision.technobot.listeners;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class CommandEventListener extends ListenerAdapter {

    public static final String PREFIX = "!";

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");
        switch (args[0].toUpperCase()) {
            case PREFIX + "PING":
                event.getChannel().sendMessage("pong").queue();
                break;
            case PREFIX + "YOUTUBE":
            case PREFIX + "TECHNOVISION":
                event.getChannel().sendMessage("Check out TechnoVision's YouTube channel: https://youtube.com/c/TechnoVisionTV").queue();
                break;
        }
    }
}
