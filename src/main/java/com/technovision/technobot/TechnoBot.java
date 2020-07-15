package com.technovision.technobot;

import com.technovision.technobot.data.Configuration;
import com.technovision.technobot.listeners.CommandEventListener;
import com.technovision.technobot.logging.Loggable;
import com.technovision.technobot.logging.Logger;
import com.technovision.technobot.util.BotRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;

@Loggable(display = "TechnoBot")
public class TechnoBot {

    private JDA jda;
    private final Configuration config = new Configuration("data/config/","botconfig.json"){
        @Override
        public void load() {
            super.load();
            if(!getJson().has("token")) getJson().put("token", "");
            if(!getJson().has("logs-webhook")) getJson().put("logs-webhook", "");
        }
    };
    private Logger logger;
    private final BotRegistry registry;

    private static TechnoBot instance;

    public static TechnoBot getInstance() {
        return instance;
    }

    public TechnoBot() throws LoginException {
        instance = this;

        JDABuilder builder = JDABuilder.createDefault(getToken());

        builder.setStatus(OnlineStatus.ONLINE)
        .setActivity(Activity.watching("TechnoVisionTV"));

        jda = builder.build();

        registry = new BotRegistry();
    }

    public Configuration getBotConfig() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }

    public JDA getJDA() {
        return jda;
    }

    public static void main(String[] args) {

        try {
            TechnoBot bot = new TechnoBot();
            while(bot.getBotConfig()==null) {}
            getInstance().logger = new Logger(bot);
        } catch(LoginException e) {
            throw new RuntimeException(e);
        }
        getInstance().getLogger().log(Logger.LogLevel.INFO, "Bot Starting...");

        getInstance().getRegistry().registerEventListeners(new CommandEventListener());

        getInstance().getRegistry().addListeners(getInstance().getJDA());
    }

    private static String getToken() {
        return getInstance().getBotConfig().getJson().getString("token");
    }

    public BotRegistry getRegistry() {
        return registry;
    }
}
