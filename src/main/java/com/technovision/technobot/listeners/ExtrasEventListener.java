package com.technovision.technobot.listeners;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadLocalRandom;

public class ExtrasEventListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;
        if(event.getMessage().getContentRaw().toLowerCase().contains("why no work")) {
            event.getChannel().sendMessage("Please explain your issue. 'why no work' doesn't help!").queue();
        } else if(event.getMessage().getContentRaw().toLowerCase().contains("will this work")) {
            event.getChannel().sendMessage("https://tryitands.ee/").queue();
        } else if(event.getMessage().getContentRaw().toLowerCase().startsWith("i need help")&&event.getMessage().getContentRaw().split(" ").length<7) {
            event.getChannel().sendMessage("https://dontasktoask.com/").queue();
        } else if(event.getMessage().getContentRaw().toLowerCase().contains("@everyone")) {
            String msg = "";
            switch (ThreadLocalRandom.current().nextInt(4)) {
                case 0:
                    msg = "<@!" + event.getMember().getUser().getId() + ">, did you *really* think that would work?";
                    break;
                case 1:
                    msg = "Nice try, but you have no power here " + "<@!" + event.getMember().getUser().getId() + ">.";
                    break;
                case 2:
                    msg = "That didn't ping anybody genius.";
                    break;
                case 3:
                    msg = "Bet that worked in your head, didn't it " + "<@!" + event.getMember().getUser().getId() + ">?";
                    break;
            }
            event.getChannel().sendMessage(msg).queue();
            event.getMessage().addReaction("\uD83D\uDE20").queue();
        } else if (event.getMessage().getContentRaw().toLowerCase().contains("<@!"+ event.getJDA().getSelfUser() + ">")) {
            event.getChannel().sendMessage("Uhh, do you need something?").queue();
        }
    }
}
