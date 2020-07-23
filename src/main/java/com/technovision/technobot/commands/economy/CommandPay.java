package com.technovision.technobot.commands.economy;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.EconManager;
import com.technovision.technobot.logging.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.omg.CORBA.DynAnyPackage.InvalidValue;

public class CommandPay extends Command {

    public CommandPay() {
        super("pay", "Send cash to a friend", "{prefix}pay [user] <amount>", Category.ECONOMY);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl());
        if (args.length > 1) {

            User receiver;
            if (args[0].startsWith("<@!") && args[0].endsWith(">")) {
                receiver = event.getJDA().retrieveUserById(args[0].substring(3, args[0].length()-1)).complete();
            } else {
                receiver = event.getGuild().getMembersByName(args[0], true).get(0).getUser();
            }
            try {
                long amt = Integer.parseInt(args[1]);
                try {
                    TechnoBot.getInstance().getEconomy().pay(event.getAuthor(), receiver, amt);
                    embed.setColor(EMBED_COLOR);
                    embed.setDescription(":white_check_mark: <@!" + receiver.getId() + "> has received your " + EconManager.SYMBOL + args[1]);
                    event.getChannel().sendMessage(embed.build()).queue();
                    return true;
                } catch (InvalidValue e) {
                    embed.setColor(ERROR_EMBED_COLOR);
                    long bal = TechnoBot.getInstance().getEconomy().getBalance(event.getAuthor()).key;
                    embed.setDescription(String.format(":x: You don't have that much money to give! You currently have %s%d on hand", EconManager.SYMBOL, bal));
                    event.getChannel().sendMessage(embed.build()).queue();
                    return true;
                }
            } catch (NumberFormatException e) {
                embed.setColor(ERROR_EMBED_COLOR);
                embed.setDescription(":x: Invalid `<amount>` argument given\n\nUsage:\n`pay [user] <amount>`");
                event.getChannel().sendMessage(embed.build()).queue();
                return true;
            }
        }
        embed.setColor(ERROR_EMBED_COLOR);
        embed.setDescription(":x: Too few arguments given.\n\nUsage:\n`pay [user] <amount>`");
        event.getChannel().sendMessage(embed.build()).queue();
        return true;
    }
}
