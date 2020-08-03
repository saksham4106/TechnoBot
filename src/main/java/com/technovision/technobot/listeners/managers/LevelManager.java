package com.technovision.technobot.listeners.managers;

import com.mongodb.client.MongoCollection;
import com.technovision.technobot.TechnoBot;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manager for member levels and ranks.
 * @author Sparky
 * @author TechnoVision
 */
public class LevelManager extends ListenerAdapter {

    private final MongoCollection<Document> levels;

    public LevelManager() {
        levels = TechnoBot.getInstance().getMongoDatabase().getCollection("levels");
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {

        if (event.getAuthor().isBot()) { return; }
        if (event.getMessage().getContentRaw().startsWith("!")) { return; }
        if (event.getChannel().getParent() != null) {
            if (event.getChannel().getParent().getIdLong() == 729856082410864690L) { return; } // Bot Category
            if (event.getChannel().getParent().getIdLong() == 599346627131605015L) { return; } // Staff Category
            if (event.getChannel().getParent().getIdLong() == 739158546469486614L) { return; } // Info Category
        }

        // Access Profile
        Long id = event.getAuthor().getIdLong();
        Document profile = (Document) levels.find(new Document("id", id)).first();
        if (profile == null) {
            profile = new Document("id", id);
            profile.append("color", "#8394eb");
            profile.append("lastTalked", 0L);
            profile.append("level", 0);
            profile.append("rank", 150);
            profile.append("background", "");
            profile.append("xp", 0);
            profile.append("totalXP", 0);
            profile.append("opacity", 0.5);
            profile.append("accent", "#FFFFFF");
            levels.insertOne(profile);
        }

        // Add XP
        long exactMilli = event.getMessage().getTimeCreated().toInstant().toEpochMilli();
        if (exactMilli - 60000 >= profile.getLong("lastTalked")) {
            List<Bson> updates = new ArrayList<>();
            updates.add(new Document("$set", new Document("lastTalked", exactMilli)));
            int xp = profile.getInteger("xp") + (ThreadLocalRandom.current().nextInt(10) + 15);
            int lvl = profile.getInteger("level");

            // Check for Level Up
            if (xp >= getMaxXP(lvl)) {
                String levelUp = "Congrats <@!" + event.getAuthor().getId() + ">" + ", you just advanced to **Level " + (lvl + 1) + "**! :tada:";
                event.getChannel().sendMessage(levelUp).queue();
                xp -= getMaxXP(lvl);
                updates.add(new Document("$set", new Document("level", lvl + 1)));
            }
            updates.add(new Document("$set", new Document("xp", xp)));
            updates.add(new Document("$set", new Document("totalXP", profile.getInteger("totalXP") + xp)));
            levels.updateMany(profile, updates);
        }
    }

    public int getMaxXP(int level) {
        return (int) (5 * Math.pow(level, 2) + 50 * level + 100);
    }

    public Document getProfile(long id) {
        return (Document) levels.find(new Document("id", id)).first();
    }

    public void update(Document profile, List<Bson> updates) {
        levels.updateMany(profile, updates);
    }

    public MongoCollection<Document> getProfiles() {
        return levels;
    }
}
