package com.technovision.technobot;

import com.technovision.technobot.commands.CommandRegistry;
import com.technovision.technobot.data.Configuration;
import com.technovision.technobot.images.ImageProcessor;
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

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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

    public static void main(String[] args) throws MalformedURLException {
        try {
            TechnoBot bot = new TechnoBot();
            getInstance().logger = new Logger(bot);
        } catch(LoginException e) { throw new RuntimeException(e); }

        getInstance().getLogger().log(Logger.LogLevel.INFO, "Bot Starting...");
        TechnoBot.getInstance().setupImages();

        new CommandRegistry();
        getInstance().getRegistry().registerEventListeners(new MusicManager(), new GuildLogEventListener(), new LevelManager(), new CommandEventListener(), new GuildMemberEvents());
        getInstance().getRegistry().addListeners(getInstance().getJDA());
    }

    private void setupImages() {
        try {
            System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2");
            BufferedImage base = ImageIO.read(new URL("https://i.imgur.com/HktDs1Y.png"));
            BufferedImage outline = ImageIO.read(new URL("https://i.imgur.com/oQhl6yW.png"));
            File file = new File("data/images/rankCardOutline.png");
            if (!file.exists()) {
                file.mkdirs();
            }
            ImageProcessor.saveImage("data/images/rankCardBase.png", base);
            ImageProcessor.saveImage("data/images/rankCardOutline.png", outline);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
