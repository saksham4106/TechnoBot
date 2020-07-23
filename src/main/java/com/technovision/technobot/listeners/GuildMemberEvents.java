package com.technovision.technobot.listeners;

import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Member Event Listener.
 * @author TechnoVision
 */
public class GuildMemberEvents extends ListenerAdapter {

    public static String JOIN_CHANNEL = "NEW-MEMBERS";
    public static String JOIN_ROLE = "MEMBER";
    public static String JOIN_MESSAGE;

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

        // Send DM
        user.openPrivateChannel().queue((dm) -> dm.sendMessage(JOIN_MESSAGE).queue());
    }

    public static void loadJoinMessage() {
        StringBuilder msg = new StringBuilder();
        try {
            File file = new File("data/JoinMessage.txt");
            if (!file.exists()) { file.createNewFile(); }
            Scanner s = new Scanner(file);
            while (s.hasNextLine()) {
                String line = s.nextLine();
                if (!line.isEmpty()) {
                    msg.append(line);
                }
                msg.append("\n");
            }
            s.close();
        } catch (IOException ignored) { }
        JOIN_MESSAGE = msg.toString();
    }
}
