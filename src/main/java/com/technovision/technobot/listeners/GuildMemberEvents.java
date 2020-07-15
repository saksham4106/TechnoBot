package com.technovision.technobot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

import static com.technovision.technobot.listeners.CommandEventListener.EMBED_COLOR;

public class GuildMemberEvents extends ListenerAdapter {

    public static String JOIN_CHANNEL = "NEW-MEMBERS";

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        TextChannel channel = event.getGuild().getTextChannelsByName(JOIN_CHANNEL, true).get(0);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Welcome @" + event.getMember().getUser().getAsTag());
        embed.setImage(event.getMember().getUser().getAvatarUrl());
        embed.setColor(EMBED_COLOR);
        channel.sendMessage(embed.build()).queue();
    }
}
