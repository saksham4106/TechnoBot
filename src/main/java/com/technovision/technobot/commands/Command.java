package com.technovision.technobot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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

    public boolean execute(MessageReceivedEvent event, String[] args) {
        return false;
    }

    public enum Category {
        STAFF,LEVELS,MUSIC,OTHER
    }
}
