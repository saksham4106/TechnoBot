package com.technovision.technobot.listeners.managers;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.logging.Logger;
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
    private final LinkedList<Document> leaderboard;
    private final HashMap<Long, Integer> ranks;

    public LevelManager() {
        levels = TechnoBot.getInstance().getMongoDatabase().getCollection("levels");
        leaderboard = new LinkedList<>();
        ranks = new HashMap<>();
        FindIterable<Document> cursor = levels.find().sort(new Document("totalXP", -1));
        int counter = 1;
        for (Document document : cursor) {
            leaderboard.add(document);
            ranks.put(document.getLong("id"), counter);
            counter++;
        }
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
        Document profile = levels.find(new Document("id", id)).first();
        if (profile == null) {
            profile = new Document("id", id);
            profile.append("color", "#8394eb");
            profile.append("lastTalked", 0L);
            profile.append("level", 0);
            profile.append("background", "");
            profile.append("xp", 0);
            profile.append("totalXP", 0);
            profile.append("opacity", 0.5);
            profile.append("accent", "#FFFFFF");
            levels.insertOne(profile);
            leaderboard.add(profile);
        }

        // Add XP
        long exactMilli = event.getMessage().getTimeCreated().toInstant().toEpochMilli();
        if (exactMilli - 60000 >= profile.getLong("lastTalked")) {
            List<Bson> updates = new ArrayList<>();
            updates.add(new Document("$set", new Document("lastTalked", exactMilli)));
            int xpIncrease = ThreadLocalRandom.current().nextInt(10) + 15;
            int xp = profile.getInteger("xp") + xpIncrease;
            int lvl = profile.getInteger("level");

            // Check for Level Up
            if (xp >= getMaxXP(lvl)) {
                xp -= getMaxXP(lvl);
                lvl++;
                String levelUp = "Congrats <@!" + event.getAuthor().getId() + ">" + ", you just advanced to **Level " + lvl + "**! :tada:";
                event.getChannel().sendMessage(levelUp).queue();
                updates.add(new Document("$set", new Document("level", lvl)));
            }
            updates.add(new Document("$set", new Document("xp", xp)));
            int totalXP = profile.getInteger("totalXP") + xpIncrease;
            updates.add(new Document("$set", new Document("totalXP", totalXP)));
            levels.updateMany(profile, updates);

            int originalIndex = leaderboard.indexOf(profile);
            profile.put("lastTalked", exactMilli);
            profile.put("xp", xp);
            profile.put("totalXP", totalXP);
            profile.put("level", lvl);
            updateLeaderboard(profile, originalIndex);
        }
    }

    private void updateLeaderboard(Document profile, int originalIndex) {
        TechnoBot.getInstance().getLogger().log(Logger.LogLevel.INFO, String.valueOf(originalIndex));
        int index = originalIndex;
        if (index <= 0) {
            leaderboard.remove(originalIndex);
            leaderboard.add(index, profile);
            return;
        }
        Document ahead = leaderboard.get(index-1);
        int aheadTotalXP = ahead.getInteger("totalXP");
        int totalXP = profile.getInteger("totalXP");
        while (totalXP > aheadTotalXP) {
            index--;
            if (index <= 0) { break; }
            ahead = leaderboard.get(index-1);
            aheadTotalXP = ahead.getInteger("totalXP");
        }
        leaderboard.remove(originalIndex);
        leaderboard.add(index, profile);
    }

    public int getMaxXP(int level) {
        return (int) (5 * Math.pow(level, 2) + 50 * level + 100);
    }

    public Document getProfile(long id) {
        return levels.find(new Document("id", id)).first();
    }

    public void update(Document profile, List<Bson> updates) {
        levels.updateMany(profile, updates);
    }

    public LinkedList<Document> getLeaderboard() { return leaderboard; }

    public HashMap<Long, Integer> getRanks() { return ranks; }

    public int getRank(long id) {
        Document doc;
        long currID;
        for (int i = 0; i < leaderboard.size(); i++) {
            doc = leaderboard.get(i);
            currID = doc.getLong("id");
            if (currID == id) {
                return i+1;
            }
        }
        return leaderboard.size();
    }

}
