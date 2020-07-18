package com.technovision.technobot.listeners;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class ExtrasEventListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;
        if(event.getMessage().getContentRaw().contains("<@!595024631438508070>")) {
            event.getChannel().sendMessage("Don't ping techno. It is against rule 2!\n\nPlease go read the rules now, before you break more.").queue();
        } else if(event.getMessage().getContentRaw().toLowerCase().contains("why no work")) {
            event.getChannel().sendMessage("Please explain your issue. 'why no work' doesnt help!").queue();
        } else if(event.getMessage().getContentRaw().toLowerCase().contains("will this work")) {
            event.getChannel().sendMessage("https://tryitands.ee/").queue();
        } else if(event.getMessage().getContentRaw().toLowerCase().startsWith("i need help")&&event.getMessage().getContentRaw().split(" ").length<7) {
            event.getChannel().sendMessage("https://dontasktoask.com/").queue();
        } else if(event.getMessage().getContentRaw().toLowerCase().contains("@everyone")) {
            event.getChannel().sendMessage("Did you really, actually think that would work??").queue();
        }
    }
}
