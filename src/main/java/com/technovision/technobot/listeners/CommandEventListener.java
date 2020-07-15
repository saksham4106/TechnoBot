package com.technovision.technobot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.requests.Route;

import javax.annotation.Nonnull;

public class CommandEventListener extends ListenerAdapter {

    public static final String PREFIX = "!";
    public static final int EMBED_COLOR = 0xd256e8;

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");
        switch (args[0].toUpperCase()) {
            case PREFIX + "SUGGEST":
                suggestion(event, args);
                break;
            case PREFIX + "HELP":
                help(event.getChannel());
                break;
            case PREFIX + "PING":
                ping(event.getChannel());
                break;
            case PREFIX + "YOUTUBE":
            case PREFIX + "TECHNOVISION":
                event.getChannel().sendMessage("Check out TechnoVision's YouTube channel: https://youtube.com/c/TechnoVisionTV").queue();
                break;
        }
    }

    private void suggestion(GuildMessageReceivedEvent event, String[] args) {
        if (args.length > 1) {
            EmbedBuilder embed = new EmbedBuilder();
            StringBuilder msg = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    msg.append(args[i]).append(" ");
                }
            }
            embed.setTitle("Suggestion");
            embed.setFooter(event.getAuthor().getAsTag(), event.getAuthor().getAvatarUrl());
            embed.setDescription(msg.toString());
            embed.setColor(EMBED_COLOR);
            TextChannel channel = event.getGuild().getTextChannelsByName("SUGGESTIONS", true).get(0);
            channel.sendMessage(embed.build()).queue(message -> {
                message.addReaction(":upvote:733030671802695860").queue();
                message.addReaction(":downvote:733030678832087120").queue();
            });
        } else {
            event.getChannel().sendMessage("USAGE: !suggest <message>").queue();
        }
    }

    private void help(TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(":robot: TechnoBot Commands");
        embed.addField("Moderator", "`!help moderator`", false);
        embed.addField("Levels", "`!help levels`", false);
        embed.addField("Commands", "`!help commands`", false);
        embed.addField("Music", "`!help music`", false);
        embed.setColor(EMBED_COLOR);
        channel.sendMessage(embed.build()).queue();
    }

    private void ping(TextChannel channel) {
        long time = System.currentTimeMillis();
        Message msg = channel.sendMessage(":signal_strength: Ping").complete();
        long latency = System.currentTimeMillis() - time;
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(":ping_pong: Pong!");
        embed.addField("Latency", latency + "ms", false);
        embed.addField("API","2ms", false);
        embed.setColor(EMBED_COLOR);
        channel.sendMessage(embed.build()).queue();
        msg.delete().queue();
    }
}
