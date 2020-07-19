package com.technovision.technobot.listeners.managers;

import com.technovision.technobot.data.Configuration;
import org.json.JSONObject;

public class SuggestionManager {

    private final Configuration suggestions;

    public SuggestionManager() {
        suggestions = new Configuration("data/","suggestions.json") {
            @Override
            public void load() {
                super.load();
                if (!getJson().has("amount")) getJson().put("amount", 0);
                if (!getJson().has("suggestions")) getJson().put("suggestions", new JSONObject());
            }
        };
        suggestions.save();
    }

    public void addSuggestion(String id) {
        int amount = suggestions.getJson().getInt("amount") + 1;
        suggestions.getJson().getJSONObject("suggestions").put(String.valueOf(amount), id);
        suggestions.getJson().put("amount", amount);
        suggestions.save();
    }

    public int getAmount() {
        return suggestions.getJson().getInt("amount");
    }

    public int getSuggestion(int num) {
        return suggestions.getJson().getJSONObject("suggestions").getInt(String.valueOf(num));
    }
}
