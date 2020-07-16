package com.technovision.technobot.listeners;

import com.technovision.technobot.data.Configuration;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class LevelManager extends ListenerAdapter {
    private static LevelManager instance;
    private final Map<Long, Long> lastTalked = new HashMap<Long, Long>();
    public final Configuration levelSave = new Configuration("data/","levels.json") {
        @Override
        public void load() {
            super.load();

            if(!getJson().has("users")) getJson().put("users", new JSONArray());
        }
    };

    public static LevelManager getInstance() {
        return instance;
    }

    public LevelManager() {
        instance = this;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {

        long exactMilli = event.getMessage().getTimeCreated().toInstant().toEpochMilli();


        boolean exists = false;

        for(Object o : levelSave.getJson().getJSONArray("users")) {
            if(((JSONObject)o).getLong("id")==event.getAuthor().getIdLong()) exists = true;
        }

        if(!exists) {
            levelSave.getJson().getJSONArray("users").put(new JSONObject() {{
                put("id", event.getAuthor().getIdLong());
                put("lastTalked", 0L);
                put("xp", 0);
                put("level", 1);

                lastTalked.putIfAbsent(event.getAuthor().getIdLong(), 0L);
            }});
        }

        for(Object o : levelSave.getJson().getJSONArray("users")) {
            if(((JSONObject)o).getLong("id")==event.getAuthor().getIdLong())
                lastTalked.putIfAbsent(event.getAuthor().getIdLong(), ((JSONObject)o).getLong("lastTalked"));
        }

        if(exactMilli-60000 > lastTalked.get(event.getAuthor().getIdLong())) {
            lastTalked.put(event.getAuthor().getIdLong(), exactMilli);

            for(Object o : levelSave.getJson().getJSONArray("users")) {
                JSONObject player = ((JSONObject)o);
                if(player.getLong("id")==event.getAuthor().getIdLong()) {
                    player.put("lastTalked", exactMilli);
                    player.put("xp", player.getInt("xp")+Math.floor(Math.random()*49)+1);
                    if(player.getInt("xp") >= player.getInt("level")*300) {
                        event.getChannel().sendMessage(event.getAuthor().getName()+", you just leveled up to level "+(player.getInt("level")+1)+"!").queue();
                        player.put("xp", player.getInt("xp")-(player.getInt("level")*300));
                        player.put("level", player.getInt("level")+1);
                    }

                    levelSave.save();
                }
            }
        }
    }
}
