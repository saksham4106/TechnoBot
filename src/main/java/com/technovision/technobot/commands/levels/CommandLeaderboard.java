package com.technovision.technobot.commands.levels;

import com.google.common.collect.Sets;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Set;

public class CommandLeaderboard extends Command {

    private final DecimalFormat formatter;

    public CommandLeaderboard() {
        super("leaderboard", "Shows the level Leaderboard", "{prefix}leaderboard <page>", Command.Category.LEVELS);
        formatter = new DecimalFormat("#,###");
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        MongoCollection<Document> profiles = TechnoBot.getInstance().getLevelManager().getProfiles();
        FindIterable<Document> cursor = profiles.find().sort(new Document("totalXP", -1));

        String msg = "";
        int counter = 1;
        for (Document document : cursor) {
            long id = document.getLong("id");
            int xp = document.getInteger("xp");
            int lvl = document.getInteger("level");
            msg += (counter) + ". <@!"+id+"> " + formatter.format(xp) + "xp " + "lvl " + lvl + "\n";
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
