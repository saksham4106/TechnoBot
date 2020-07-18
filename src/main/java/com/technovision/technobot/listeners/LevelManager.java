package com.technovision.technobot.listeners;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.data.Configuration;
import com.technovision.technobot.util.Tuple;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Manager for member levels and ranks.
 * @author Sparky
 */
public class LevelManager extends ListenerAdapter {

    public final List<Tuple<Integer,Integer>> tupleList = new ArrayList<>();
    public final List<User> userList = new ArrayList<>();

    public static final String RANK_CHANNEL = "RANKS-AND-ROLES";
    private static LevelManager instance;

    private final Map<Long, Long> lastTalked = new HashMap<Long, Long>();
    public final Configuration levelSave = new Configuration("data/","levels.json") {
        @Override
        public void load() {
            super.load();

            if (!getJson().has("users")) getJson().put("users", new JSONArray());
        }
    };

    public static LevelManager getInstance() {
        return instance;
    }

    public LevelManager() {
        instance = this;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                tupleList.clear();
                userList.clear();

                for(Object o : LevelManager.getInstance().levelSave.getJson().getJSONArray("users")) {
                    JSONObject jsonUser = (JSONObject) o;

                    int ind = 0;
                    for(Tuple<Integer, Integer> tup : tupleList) {
                        int realXpValue = 0;
                        for(int a = 0; a < jsonUser.getInt("level");a++) {
                            realXpValue += (a)*300;
                        }
                        realXpValue += jsonUser.getInt("xp");

                        int realXpValueFromTuple = 0;
                        for(int a = 0; a < tup.key; a++) {
                            realXpValueFromTuple += (a)*300;
                        }
                        realXpValueFromTuple += tup.value;

                        if(realXpValue>realXpValueFromTuple) {
                            ind = tupleList.indexOf(tup);
                            break;
                        } else {
                            ind = tupleList.indexOf(tup)+1;
                        }

                    }



                    tupleList.add(ind, new Tuple<>(jsonUser.getInt("level"),jsonUser.getInt("xp")));
                    userList.add(ind, TechnoBot.getInstance().getJDA().retrieveUserById(jsonUser.getLong("id")).complete());
                }
            }
        },5000L,30000L);
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {

        if (event.getAuthor().isBot()) { return; }
        if (event.getMessage().getContentRaw().startsWith("!")) { return; }
        long exactMilli = event.getMessage().getTimeCreated().toInstant().toEpochMilli();

        boolean exists = false;

        for (Object o : levelSave.getJson().getJSONArray("users")) {
            if(((JSONObject)o).getLong("id")==event.getAuthor().getIdLong()) exists = true;
        }

        if (!exists) {
            levelSave.getJson().getJSONArray("users").put(new JSONObject() {{
                put("id", event.getAuthor().getIdLong());
                put("lastTalked", 0L);
                put("xp", 0);
                put("level", 1);
                put("rank", event.getGuild().getMembers().size());
                put("opacity", 0.5);
                put("color", "#8394eb");
                put("accent", "#FFFFFF");
                put("background", "");

                lastTalked.putIfAbsent(event.getAuthor().getIdLong(), 0L);
            }});
        }

        for (Object o : levelSave.getJson().getJSONArray("users")) {
            if(((JSONObject)o).getLong("id")==event.getAuthor().getIdLong())
                lastTalked.putIfAbsent(event.getAuthor().getIdLong(), ((JSONObject)o).getLong("lastTalked"));
        }

        if (exactMilli-60000 > lastTalked.get(event.getAuthor().getIdLong())) {
            lastTalked.put(event.getAuthor().getIdLong(), exactMilli);

            for(Object o : levelSave.getJson().getJSONArray("users")) {
                JSONObject player = ((JSONObject)o);
                if(player.getLong("id")==event.getAuthor().getIdLong()) {
                    player.put("lastTalked", exactMilli);
                    player.put("xp", player.getInt("xp")+Math.floor(Math.random()*20)+1);
                    if(player.getInt("xp") >= player.getInt("level")*300) {
                        String levelUp = "Congrats <@!" + event.getAuthor().getId() + ">" + ", you just advanced to level " + (player.getInt("level")+1)+"!";
                        event.getGuild().getTextChannelsByName(RANK_CHANNEL, true).get(0).sendMessage(levelUp).queue();
                        player.put("xp", player.getInt("xp")-(player.getInt("level")*300));
                        player.put("level", player.getInt("level")+1);
                    }

                    levelSave.save();
                }
            }
        }
    }
}
