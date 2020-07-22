package com.technovision.technobot.commands;

import com.google.common.collect.Sets;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.technovision.technobot.listeners.CommandEventListener.EMBED_COLOR;

public class CommandPing extends Command {

    public CommandPing() {
        super("ping","Pings the Discord API","{prefix}ping", Command.Category.OTHER);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        long time = System.currentTimeMillis();
        Message msg = event.getChannel().sendMessage(":signal_strength: Ping").complete();
        long latency = System.currentTimeMillis() - time;
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(":ping_pong: Pong!");
        embed.addField("Latency", latency + "ms", false);
        embed.addField("API","2ms", false);
        embed.setColor(EMBED_COLOR);
        event.getChannel().sendMessage(embed.build()).queue();
        msg.delete().queue();
        return true;
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet();
    }
}
