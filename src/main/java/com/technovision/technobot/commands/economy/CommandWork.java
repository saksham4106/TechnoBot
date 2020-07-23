package com.technovision.technobot.commands.economy;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.EconManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

public class CommandWork extends Command {

    public CommandWork() {
        super("work", "Work for some cash", "{prefix}work", Command.Category.ECONOMY);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embed = new EmbedBuilder();
        JSONObject profile = TechnoBot.getInstance().getEconomy().getProfile(event.getAuthor());
        long timestamp = profile.getLong("work-timestamp");
        int cooldown = 14400000;
        if (System.currentTimeMillis() >= timestamp + cooldown) {
            TechnoBot.getInstance().getEconomy().addMoney(event.getAuthor(), 100, EconManager.Activity.WORK);
            embed.setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getEffectiveAvatarUrl());
            embed.setDescription("You work for the day and receive " + EconManager.SYMBOL +  "100");
            embed.setColor(0x33cc33);
        } else {
            embed.setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl());
            embed.setDescription(":stopwatch: You cannot work for " + TechnoBot.getInstance().getEconomy().getCooldown(timestamp, cooldown) + ".");
            embed.setColor(EMBED_COLOR);
        }
        event.getChannel().sendMessage(embed.build()).queue();
        return true;
    }
}
