package com.technovision.technobot;

import com.technovision.technobot.commands.CommandRegistry;
import com.technovision.technobot.data.Configuration;
import com.technovision.technobot.listeners.*;
import com.technovision.technobot.logging.Loggable;
import com.technovision.technobot.logging.Logger;
import com.technovision.technobot.util.BotRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;

/**
 * Multi-purpose bot for TechnoVision Discord
 * @author TechnVision
 * @author Sparky
 * @version 1.0
 */
@Loggable(display = "TechnoBot")
public class TechnoBot {

    private static TechnoBot instance;

    private JDA jda;
    private Logger logger;
    private final BotRegistry registry;
    private final Configuration config = new Configuration("data/config/","botconfig.json"){
        @Override
        public void load() {
            super.load();
            if(!getJson().has("token")) getJson().put("token", "");
            if(!getJson().has("logs-webhook")) getJson().put("logs-webhook", "");
            if(!getJson().has("guildlogs-webhook")) getJson().put("guildlogs-webhook", "");
        }
    };

    public static TechnoBot getInstance() {
        return instance;
    }

    public TechnoBot() throws LoginException {
        instance = this;

        JDABuilder builder = JDABuilder.createDefault(getToken());
        builder.setStatus(OnlineStatus.ONLINE)
        .setActivity(Activity.watching("TechnoVisionTV"));
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES);
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

    public BotRegistry getRegistry() {
        return registry;
    }

    private String getToken() { return getInstance().getBotConfig().getJson().getString("token"); }

    public static void main(String[] args) {
        try {
            TechnoBot bot = new TechnoBot();
            getInstance().logger = new Logger(bot);
        } catch(LoginException e) { throw new RuntimeException(e); }

        System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2");
        getInstance().getLogger().log(Logger.LogLevel.INFO, "Bot Starting...");

        new CommandRegistry();
        getInstance().getRegistry().registerEventListeners(new MusicManager(), new GuildLogEventListener(), new LevelManager(), new CommandEventListener(), new GuildMemberEvents());
        getInstance().getRegistry().addListeners(getInstance().getJDA());
    }
}
