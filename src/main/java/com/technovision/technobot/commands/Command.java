package com.technovision.technobot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import javax.smartcardio.CommandAPDU;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Discord Executable Command
 * @author Sparky
 */
public abstract class Command {
    public final String name;
    public final Category category;
    public final String description;
    public final String usage;

    public Command(String name, String description, String usage, Category category) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.usage = usage;
    }

    public abstract boolean execute(MessageReceivedEvent event, String[] args);

    public abstract @NotNull Set<String> getAliases();

    public enum Category {
        STAFF,LEVELS,MUSIC,OTHER,ECONOMY,FUN
    }
}
