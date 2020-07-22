package com.technovision.technobot.listeners;

import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

/**
 * Member Event Listener.
 * @author TechnoVision
 */
public class GuildMemberEvents extends ListenerAdapter {

    public static String JOIN_CHANNEL = "NEW-MEMBERS";
    public static String JOIN_ROLE = "MEMBER";

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        // Join Message
        TextChannel channel = event.getGuild().getTextChannelsByName(JOIN_CHANNEL, true).get(0);
        EmbedBuilder embed = new EmbedBuilder();
        User user = event.getMember().getUser();
        embed.setAuthor(user.getAsTag(), null, user.getAvatarUrl());
        embed.setDescription("Welcome, <@!"+user.getId()+">" + " to the Server!");
        embed.setColor(Command.EMBED_COLOR);
        channel.sendMessage(embed.build()).queue();
        // Add Role
        event.getGuild().addRoleToMember(event.getMember().getId(), event.getGuild().getRolesByName(JOIN_ROLE, true).get(0)).queue();
    }
}
