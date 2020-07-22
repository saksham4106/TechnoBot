package com.technovision.technobot.commands.economy;

import com.google.common.collect.Sets;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.EconManager;
import com.technovision.technobot.util.Tuple;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CommandBalance extends Command {

    public CommandBalance() {
        super("balance", "View your account balance", "{prefix}balance", Command.Category.ECONOMY);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        Tuple<Long, Long> profile = TechnoBot.getInstance().getEconomy().getBalance(event.getAuthor());
        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl())
                .addField("Cash:", EconManager.SYMBOL + profile.key, true)
                .addField("Bank:", EconManager.SYMBOL + profile.value, true)
                .addField("Net Worth:", EconManager.SYMBOL + (profile.key + profile.value), true)
                .setColor(EMBED_COLOR);
        event.getChannel().sendMessage(embed.build()).queue();
        return true;
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("bal", "money");
    }
}
