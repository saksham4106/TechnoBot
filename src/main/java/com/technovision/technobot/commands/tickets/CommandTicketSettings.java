package com.technovision.technobot.commands.tickets;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandTicketSettings extends Command {
    private final TechnoBot bot;

    public CommandTicketSettings(final TechnoBot bot) {
        super("ticketsettings", "Guild-specific settings for tickets", "{prefix}ticketsettings [argument] [value]", Category.STAFF);
        this.bot = bot;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if(event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            if(args.length==0) {
                GuildChannel inboxChannel = bot.getTicketManager().getInboxChannel(event.getGuild());
                event.getChannel().sendMessage(new EmbedBuilder()
                        .setTitle("\uD83C\uDF9F Ticket Settings")
                        .addField("\uD83D\uDCE8 Inbox Channel (inbox-channel)", ((inboxChannel != null) ? inboxChannel.getName() : "None"), true)
                        .setFooter("Change values with \"ticketsettings (name in parenthesis) (value)")
                        .build()
                ).queue();
            } else {
                if(args[0].equalsIgnoreCase("inbox-channel")) {
                    try {
                        bot.getTicketManager().setInboxChannel(event.getGuild(), event.getGuild().getTextChannelsByName(args[1], true).get(0));
                        event.getChannel().sendMessage("\uD83D\uDCE8 Successfully set the channel!").queue();
                    } catch(StringIndexOutOfBoundsException e) {
                        event.getChannel().sendMessage("Please specify a channel name!").queue();
                    } catch(Exception e) {
                        event.getChannel().sendMessage("Could not find channel!").queue();
                    }
                }
            }
        } else event.getChannel().sendMessage("❌ You cannot do that!").queue();

        return true;
    }
}
