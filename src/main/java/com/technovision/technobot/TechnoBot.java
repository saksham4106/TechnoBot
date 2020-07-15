package com.technovision.technobot;

import com.technovision.technobot.data.Configuration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class TechnoBot {

    public static JDA jda;
    public static Configuration config;

    public static void main(String[] args) {
        createConfig();
        try {
            jda = JDABuilder.createDefault(getToken()).build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    private static void createConfig() {
        config = new Configuration("data/config/","botconfig.json"){
            @Override
            public void load() {
                super.load();
                if(getJson().getString("token")==null) getJson().put("token", "");
                if(getJson().getString("logs-webhook")==null) getJson().put("logs-webhook", "");
            }
        };
    }

    private static String getToken() {
        return config.getJson().getString("token");
    }
}
