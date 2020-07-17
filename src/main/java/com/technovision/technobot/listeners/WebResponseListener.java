package com.technovision.technobot.listeners;

import com.technovision.technobot.TechnoBot;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class WebResponseListener extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().contains("<@!"+ TechnoBot.getInstance().getJDA().getSelfUser().getId()+">")) {
            event.getChannel().sendMessage("y u ping").queue();
        }
        if(event.getMessage().getContentRaw().contains("<@!595024631438508070>")) {
            event.getChannel().sendMessage("dont ping techno.. its against rule 2").queue();
        }

        if(event.getMessage().getContentRaw().contains("will this work")) {
            event.getChannel().sendMessage("<@!"+event.getAuthor().getId()+">\nhttps://tryitands.ee/").queue();
        } else if(event.getMessage().getContentRaw().equalsIgnoreCase("i need help")) {
            event.getChannel().sendMessage("<@!"+event.getAuthor().getId()+">\nhttps://dontasktoask.com").queue();
        }
    }
}
