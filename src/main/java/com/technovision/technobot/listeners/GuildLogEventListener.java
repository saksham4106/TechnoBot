package com.technovision.technobot.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.logging.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.annotation.Nonnull;
import java.awt.*;

public class GuildLogEventListener extends ListenerAdapter {
    private final WebhookClient webhook = new WebhookClientBuilder(TechnoBot.getInstance().getBotConfig().getJson().getString("guildlogs-webhook")).build();

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            for (Role role : event.getRoles()) {
                addField(new WebhookEmbed.EmbedField(true, role.getName(), "<@&"+role.getId()+">"));
            }
        }}
                .setTitle(new WebhookEmbed.EmbedTitle("Role"+((event.getRoles().size()>1)?"s":"")+" Added to User",null))
                .setDescription("Added to <@!"+event.getUser().getId()+">")
                .setColor(0x00FF00)
                .build());
    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            for (Role role : event.getRoles()) {
                addField(new WebhookEmbed.EmbedField(true, role.getName(), "<@&"+role.getId()+">"));
            }
        }}
                .setTitle(new WebhookEmbed.EmbedTitle("Role"+((event.getRoles().size()>1)?"s":"")+" Removed from User",null))
                .setDescription("Removed from <@!"+event.getUser().getId()+">")
                .setColor(0xFF0000)
                .build());
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Member Left",null));
            addField(new WebhookEmbed.EmbedField(false, event.getUser().getAsTag(), "<@!"+event.getUser().getId()+">"));
            setColor(0xFF0000);
        }}.build());
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Member Joined",null));
            addField(new WebhookEmbed.EmbedField(false, event.getUser().getAsTag(), "<@!"+event.getUser().getId()+">"));
            setColor(0x00FF00);
        }}.build());
    }

    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Member Nickname Updated", null));
            addField(new WebhookEmbed.EmbedField(false, "Changes", "`"+event.getOldNickname()+"` -> `"+event.getNewNickname()+"`"));
            setColor(0x0000FF);
        }}.build());
    }

    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        String[] perms = new String[event.getRole().getPermissions().size()];
        for(int i = 0; i < event.getRole().getPermissions().size(); i++) {
            perms[i] = ((Permission)event.getRole().getPermissions().toArray()[i]).getName();
        }
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Role Created", null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Name", event.getRole().getName()));
            addField(new WebhookEmbed.EmbedField(false, "Settings", String.join("\n- ", perms)));
            addField(new WebhookEmbed.EmbedField(false, "Hoisted", String.valueOf(event.getRole().isHoisted())));
            addField(new WebhookEmbed.EmbedField(false, "Color", event.getRole().getColorRaw()+" ("+Color.getColor(String.valueOf(event.getRole().getColorRaw()))+")"));
        }}.build());
    }

    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Role Deleted", null));
            setColor(0xFF0000);
            addField(new WebhookEmbed.EmbedField(false, "Name", event.getRole().getName()));
        }}.build());
    }

    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Message Edited",null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Author", "<@!"+event.getAuthor().getId()+">"));
            addField(new WebhookEmbed.EmbedField(false, "New Message", "```\n"+event.getMessage().getContentRaw()+"\n```"));
        }}.build());
    }

    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Message Deleted", null));
            setColor(0xFF0000);
            addField(new WebhookEmbed.EmbedField(false, "Channel", "<#"+event.getChannel().getId()+">"));
        }}.build());
    }
}
