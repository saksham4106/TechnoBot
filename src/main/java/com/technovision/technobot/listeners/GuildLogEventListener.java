package com.technovision.technobot.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.technovision.technobot.TechnoBot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.update.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * Bot Log Handler.
 * Listens to various actions and
 * logs them to a specified channel.
 * @author Sparky
 */
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
        if(event.getMessage().getContentRaw().startsWith("!")||event.getAuthor().isBot()) return;
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

    @Override
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Channel Deleted", null));
            setColor(0xFF0000);
            addField(new WebhookEmbed.EmbedField(false, "Channel", "<#"+event.getChannel().getId()+">"));
            addField(new WebhookEmbed.EmbedField(false, "Category", "**"+event.getChannel().getTopic()+"**"));
        }}.build());
    }

    @Override
    public void onTextChannelUpdateName(@Nonnull TextChannelUpdateNameEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Channel Updated", null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Channel", "<#"+event.getChannel().getId()+">"));
            addField(new WebhookEmbed.EmbedField(true, "Old Name", event.getOldName()));
            addField(new WebhookEmbed.EmbedField(false, "New Name", event.getNewName()));
        }}.build());
    }

    @Override
    public void onTextChannelUpdateTopic(@Nonnull TextChannelUpdateTopicEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Channel Updated", null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Channel", "<#"+event.getChannel().getId()+">"));
            addField(new WebhookEmbed.EmbedField(true, "Old Topic", event.getOldTopic()!=null?event.getOldTopic():"None"));
            addField(new WebhookEmbed.EmbedField(false, "New Topic", event.getNewTopic()!=null?event.getNewTopic():"None"));
        }}.build());
    }

    @Override
    public void onTextChannelUpdatePosition(@Nonnull TextChannelUpdatePositionEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Channel Updated", null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Channel", "<#"+event.getChannel().getId()+">"));
            addField(new WebhookEmbed.EmbedField(true, "Old Position", "#"+event.getOldPosition()));
            addField(new WebhookEmbed.EmbedField(false, "New Position", "#"+event.getNewPosition()));
        }}.build());
    }

    @Override
    public void onTextChannelUpdateNSFW(@Nonnull TextChannelUpdateNSFWEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Channel Updated", null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Channel", "<#"+event.getChannel().getId()+">"));
            addField(new WebhookEmbed.EmbedField(true, "Changed Setting", "Is NSFW: "+event.getOldNSFW()));
        }}.build());
    }

    @Override
    public void onTextChannelUpdateParent(@Nonnull TextChannelUpdateParentEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Channel Updated", null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Channel", "<#"+event.getChannel().getId()+">"));
            addField(new WebhookEmbed.EmbedField(true, "Old Category", event.getOldParent()!=null?event.getOldParent().getName():"None"));
            addField(new WebhookEmbed.EmbedField(false, "New Category", event.getNewParent()!=null?event.getNewParent().getName():"None"));
        }}.build());
    }

    @Override
    public void onTextChannelUpdateSlowmode(@Nonnull TextChannelUpdateSlowmodeEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Channel Updated", null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Channel", "<#"+event.getChannel()+">"));
            addField(new WebhookEmbed.EmbedField(true, "Old Slowmode", event.getOldSlowmode()+"s"));
            addField(new WebhookEmbed.EmbedField(false, "New Slowmode", event.getNewSlowmode()+"s"));
        }}.build());
    }

    @Override
    public void onTextChannelCreate(@Nonnull TextChannelCreateEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Channel Created", null));
            setColor(0x00FF00);
            addField(new WebhookEmbed.EmbedField(true, "Channel", "<#"+event.getChannel().getId()+">"));
            addField(new WebhookEmbed.EmbedField(true, "Topic", event.getChannel().getTopic()!=null?event.getChannel().getTopic():"None"));
            addField(new WebhookEmbed.EmbedField(true, "Slowmode", event.getChannel().getSlowmode()+"s"));
            addField(new WebhookEmbed.EmbedField(true, "Is NSFW", String.valueOf(event.getChannel().isNSFW())));
            addField(new WebhookEmbed.EmbedField(true, "Position", "#"+event.getChannel().getPosition()));
        }}.build());
    }

    @Override
    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Role Updated", null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Role", "<@&"+event.getRole().getId()+">"));
            addField(new WebhookEmbed.EmbedField(true, "Old Color", String.valueOf(event.getOldColorRaw())));
            addField(new WebhookEmbed.EmbedField(true, "New Color", String.valueOf(event.getNewColorRaw())));
        }}.build());
    }

    @Override
    public void onRoleUpdateHoisted(@Nonnull RoleUpdateHoistedEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Role Updated", null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Role", "<@&"+event.getRole().getId()+">"));
            addField(new WebhookEmbed.EmbedField(true, "Old Setting", "Hoisted: " + event.getOldValue()));
            addField(new WebhookEmbed.EmbedField(true, "New Setting", "Hoisted: " + event.getNewValue()));
        }}.build());
    }

    @Override
    public void onRoleUpdateMentionable(@Nonnull RoleUpdateMentionableEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Role Updated", null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Role", "<@&"+event.getRole().getId()+">"));
            addField(new WebhookEmbed.EmbedField(true, "Old Setting", "Mentionable: " + event.getOldValue()));
            addField(new WebhookEmbed.EmbedField(true, "New Setting", "Mentionable: " + event.getNewValue()));
        }}.build());
    }

    @Override
    public void onRoleUpdateName(@Nonnull RoleUpdateNameEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Role Updated", null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Role", "<@&"+event.getRole().getId()+">"));
            addField(new WebhookEmbed.EmbedField(true, "Old Name", event.getOldName()));
            addField(new WebhookEmbed.EmbedField(true, "New Name", event.getNewName()));
        }}.build());
    }

    @Override
    public void onRoleUpdatePermissions(@Nonnull RoleUpdatePermissionsEvent event) {
        String[] oldPerms = new String[event.getOldPermissions().size()];
        for(int i = 0; i < event.getOldPermissions().size(); i++) {
            oldPerms[i] = ((Permission)event.getOldPermissions().toArray()[i]).getName();
        }
        String[] perms = new String[event.getNewPermissions().size()];
        for(int i = 0; i < event.getNewPermissions().size(); i++) {
            perms[i] = ((Permission)event.getNewPermissions().toArray()[i]).getName();
        }
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Role Updated", null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Role", "<@&"+event.getRole().getId()+">"));
            addField(new WebhookEmbed.EmbedField(true, "Old Permissions", "- "+String.join("\n- ",oldPerms)));
            addField(new WebhookEmbed.EmbedField(true, "New Permissions", "- "+String.join("\n- ",perms)));
        }}.build());
    }

    @Override
    public void onRoleUpdatePosition(@Nonnull RoleUpdatePositionEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Role Updated", null));
            setColor(0x0000FF);
            addField(new WebhookEmbed.EmbedField(false, "Role", "<@&"+event.getRole().getId()+">"));
            addField(new WebhookEmbed.EmbedField(true, "Old Position", "#"+event.getOldPosition()));
            addField(new WebhookEmbed.EmbedField(true, "New Position", "#"+event.getNewPosition()));
        }}.build());
    }
}
