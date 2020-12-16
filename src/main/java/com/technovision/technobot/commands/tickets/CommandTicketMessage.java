package com.technovision.technobot.commands.tickets;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandTicketMessage extends Command {
    private final TechnoBot bot;

    public CommandTicketMessage(final TechnoBot bot) {
        super("ticketmessage", "Generates the ticket message in the current channel that can be reacted with to open a ticket.", "{prefix}ticketmessage", Category.TICKETS);
        this.bot = bot;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if(event.getMember().hasPermission(Permission.KICK_MEMBERS)) bot.getTicketManager().createReactionMessage(event.getGuild(), event.getChannel());
        else event.getChannel().sendMessage("❌ You cannot do that!").queue();
        return true;
    }
}
