package com.technovision.technobot.listeners.managers;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.data.Configuration;
import com.technovision.technobot.util.Tuple;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manager for member levels and ranks.
 * @author Sparky
 */
public class LevelManager extends ListenerAdapter {

    public final List<Tuple<Integer,Integer>> tupleList = new ArrayList<>();
    public final List<User> userList = new ArrayList<>();

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
                            realXpValue += getMaxXP(a);
                        }
                        realXpValue += jsonUser.getInt("xp");

                        int realXpValueFromTuple = 0;
                        for(int a = 0; a < tup.key; a++) {
                            realXpValueFromTuple += getMaxXP(a);
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
        if (event.getChannel().getParent() != null) {
            if (event.getChannel().getParent().getIdLong() == 729856082410864690L) { return; }
            if (event.getChannel().getParent().getIdLong() == 599346627131605015L) { return; }
            if (event.getChannel().getParent().getIdLong() == 599345340742762496L) { return; }
        }
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
                    player.put("xp", player.getInt("xp") + (ThreadLocalRandom.current().nextInt(10) + 15));
                    if(player.getInt("xp") >= getMaxXP(player.getInt("level"))) {
                        String levelUp = "Congrats <@!" + event.getAuthor().getId() + ">" + ", you just advanced to level " + (player.getInt("level")+1)+"!";
                        event.getChannel().sendMessage(levelUp).queue();
                        player.put("xp", player.getInt("xp")-getMaxXP(player.getInt("level")));
                        int level = player.getInt("level") + 1;
                        player.put("level", level);
                        List<Role> roles = event.getMember().getRoles();
                        if (level >= 5) {
                            Role elite = event.getGuild().getRoleById(739016880236527678L);
                            if (roles.contains(elite)) {
                                event.getGuild().addRoleToMember(event.getMember(), elite).queue();
                            }
                        }
                        if (level >= 10) {
                            Role heroic = event.getGuild().getRoleById(739016981621243998L);
                            if (roles.contains(heroic)) {
                                event.getGuild().addRoleToMember(event.getMember(), heroic).queue();
                            }
                        }
                        if (level >= 20) {
                            Role ultimate = event.getGuild().getRoleById(737482202421526651L);
                            if (roles.contains(ultimate)) {
                                event.getGuild().addRoleToMember(event.getMember(), ultimate).queue();
                            }
                        }
                        if (level >= 30) {
                            Role legendary = event.getGuild().getRoleById(737482254497874011L);
                            if (roles.contains(legendary)) {
                                event.getGuild().addRoleToMember(event.getMember(), legendary).queue();
                            }
                        }
                    }
                    levelSave.save();
                }
            }
        }
    }

    public int getMaxXP(int level) {
        return (int) (5 * Math.pow(level, 2) + 50 * level + 100);
    }
}
