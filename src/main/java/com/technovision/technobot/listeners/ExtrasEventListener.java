package com.technovision.technobot.listeners;

import com.technovision.technobot.TechnoBot;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadLocalRandom;

public class ExtrasEventListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;
        String msg = event.getMessage().getContentRaw().toLowerCase();
        if(msg.contains("why no work")) {
            event.getChannel().sendMessage("Please explain your issue. 'why no work' doesn't help!").queue();
        } else if(msg.contains("will this work")) {
            event.getChannel().sendMessage("https://tryitands.ee/").queue();
        } else if(msg.startsWith("i need help")&&event.getMessage().getContentRaw().split(" ").length<7) {
            event.getChannel().sendMessage("https://dontasktoask.com/").queue();
        } else if (msg.contains("1.12")) {
            event.getChannel().sendMessage("Version 1.12 of Forge is no longer supported! Please update to a newer version (1.14+).").queue();
        } else if (event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser())) {
            event.getChannel().sendMessage("Uhhh, do you need something?").queue();
        } else if(msg.contains("@everyone")) {
            String reply = "";
            switch (ThreadLocalRandom.current().nextInt(4)) {
                case 0:
                    reply = "<@!" + event.getMember().getUser().getId() + ">, did you *really* think that would work?";
                    break;
                case 1:
                    reply = "Nice try, but you have no power here " + "<@!" + event.getMember().getUser().getId() + ">.";
                    break;
                case 2:
                    reply = "That didn't ping anybody genius.";
                    break;
                case 3:
                    reply = "Bet that worked in your head, didn't it " + "<@!" + event.getMember().getUser().getId() + ">?";
                    break;
            }
            event.getChannel().sendMessage(reply).queue();
            event.getMessage().addReaction("\uD83D\uDE20").queue();
        }
    }
}
