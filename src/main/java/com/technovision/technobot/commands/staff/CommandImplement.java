package com.technovision.technobot.commands.staff;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.util.enums.SuggestionResponse;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandImplement extends Command {
    private final TechnoBot bot;

    public CommandImplement(final TechnoBot bot) {
        super("implement", "Implements a suggestion", "{prefix}implement <id> [reason]", Category.STAFF);
        this.bot = bot;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        bot.getSuggestionManager().respond(event, args, SuggestionResponse.IMPLEMENTED);
        return true;
    }
}
