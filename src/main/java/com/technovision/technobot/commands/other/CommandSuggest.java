package com.technovision.technobot.commands.other;

import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandSuggest extends Command {
    public CommandSuggest() {
        super("suggest", "Suggest a feature or idea related to the server", "{prefix}suggest [content]", Command.Category.OTHER);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        return false;
    }
}
