package com.technovision.technobot.listeners.managers;

import com.technovision.technobot.data.Configuration;
import com.technovision.technobot.util.Tuple;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.omg.CORBA.DynAnyPackage.InvalidValue;

public class EconManager {

    public static final String SYMBOL = ":sparkles:";

    private final Configuration economy;

    public EconManager() {
        economy = new Configuration("data/","economy.json") {
            @Override
            public void load() {
                super.load();
                if (!getJson().has("users")) getJson().put("users", new JSONArray());
            }
        };
        economy.save();
    }

    public Tuple<Long, Long> getBalance(User user) {
        JSONObject profile = getProfile(user);
        long bal = profile.getLong("balance");
        long bank = profile.getLong("bank");
        return new Tuple<>(bal, bank);
    }

    public void removeMoney(User user, long amount) {
        JSONObject profile = getProfile(user);
        long bal = profile.getLong("balance");
        long remaining = bal - amount;
        if (remaining < 0) { remaining = 0; }
        profile.put("balance", remaining);
    }

    public void pay(User sender, User receiver, long amount) throws InvalidValue {
        JSONObject senderProfile = getProfile(sender);
        JSONObject receiverProfile = getProfile(receiver);

        long senderBal = senderProfile.getLong("balance");
        if (senderBal - amount < 0) { throw new InvalidValue(); }
        senderProfile.put("balance", senderBal - amount);

        long receiverBal = receiverProfile.getLong("balance");
        receiverProfile.put("balance", receiverBal + amount);
        economy.save();
    }

    public void addMoney(User user, long amount, Activity activity) {
        JSONObject profile = getProfile(user);
        long bal = profile.getLong("balance");
        profile.put("balance", bal + amount);
        switch (activity) {
            case WORK:
                profile.put("work-timestamp", System.currentTimeMillis());
                break;
            case CRIME:
                profile.put("crime-timestamp", System.currentTimeMillis());
                break;
        }
        economy.save();
    }

    public JSONObject getProfile(User user) {
        JSONArray profiles = economy.getJson().getJSONArray("users");
        for (Object o : profiles) {
            if (((JSONObject) o).getLong("id") == user.getIdLong()) {
                return (JSONObject) o;
            }
        }
        profiles.put(new JSONObject() {{
            put("id", user.getIdLong());
            put("balance", 0);
            put("bank", 0);
            put("work-timestamp", 0);
            put("crime-timestamp", 0);
        }});
        economy.save();
        return (JSONObject) profiles.get(profiles.length() - 1);
    }

    public String getCooldown(long timestamp, int cooldown) {
        long milliseconds = cooldown - (System.currentTimeMillis() - timestamp);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        if (minutes == 0) {
            return hours + " hours";
        }
        return hours + " hours and " + minutes + " minutes";
    }

    public enum Activity {
        WORK, CRIME;
    }
}
