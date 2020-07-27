package com.technovision.technobot.commands.economy;

import com.google.common.collect.Sets;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.EconManager;
import com.technovision.technobot.util.Tuple;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Set;

public class CommandBalance extends Command {

    public CommandBalance() {
        super("balance", "View your account balance", "{prefix}balance", Command.Category.ECONOMY);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {

        User user = event.getAuthor();
        if (args.length > 0) {
            if (args[0].startsWith("<@!") && args[0].endsWith(">")) {
                user = event.getJDA().retrieveUserById(args[0].substring(3, args[0].length()-1)).complete();
            } else {
                return true;
            }
        }

        Tuple<Long, Long> profile = TechnoBot.getInstance().getEconomy().getBalance(user);
        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl())
                .addField("Cash:", EconManager.SYMBOL + EconManager.FORMATTER.format(profile.key), true)
                .addField("Bank:", EconManager.SYMBOL + EconManager.FORMATTER.format(profile.value), true)
                .addField("Net Worth:", EconManager.SYMBOL + EconManager.FORMATTER.format((profile.key + profile.value)), true)
                .setColor(EMBED_COLOR);
        event.getChannel().sendMessage(embed.build()).queue();
        return true;
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("bal", "money");
    }
}
