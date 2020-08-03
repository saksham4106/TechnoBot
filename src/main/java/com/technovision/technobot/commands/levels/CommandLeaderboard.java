package com.technovision.technobot.commands.levels;

import com.google.common.collect.Sets;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Set;

public class CommandLeaderboard extends Command {

    private final DecimalFormat formatter;

    public CommandLeaderboard() {
        super("leaderboard", "Shows the level Leaderboard", "{prefix}leaderboard <page>", Command.Category.LEVELS);
        formatter = new DecimalFormat("#,###");
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        LinkedList<Document> leaderboard = TechnoBot.getInstance().getLevelManager().getLeaderboard();

        String msg = "";
        int counter = 1;
        for (Document doc: leaderboard) {
            int totalXP = doc.getInteger("totalXP");
            int lvl = doc.getInteger("level");
            long id = doc.getLong("id");
            msg += (counter) + ". <@!"+id+"> " + formatter.format(totalXP) + "xp " + "lvl " + lvl + "\n";
            counter++;
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(":trophy: Rank Leaderboard");
        builder.setColor(EMBED_COLOR);
        builder.setDescription(msg);
        event.getChannel().sendMessage(builder.build()).queue();
        return true;
    }


    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("ranks", "lvls", "leaderboards");
    }
}
