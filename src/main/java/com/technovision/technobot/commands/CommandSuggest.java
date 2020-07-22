package com.technovision.technobot.commands;

import com.google.common.collect.Sets;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CommandSuggest extends Command {
    public CommandSuggest() {
        super("suggest", "Suggest a feature or idea related to the server", "{prefix}suggest [content]", Command.Category.OTHER);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        return false;
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet();
    }
}
