package com.technovision.technobot;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.data.Configuration;
import com.technovision.technobot.listeners.CommandEventListener;
import com.technovision.technobot.logging.Loggable;
import com.technovision.technobot.logging.Logger;
import com.technovision.technobot.util.BotRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.security.auth.login.LoginException;

import static com.technovision.technobot.listeners.CommandEventListener.EMBED_COLOR;

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

        getInstance().getRegistry().registerCommands(new Command("ping","Pings the Discord API","{prefix}ping", Command.Category.OTHER) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                long time = System.currentTimeMillis();
                Message msg = event.getChannel().sendMessage(":signal_strength: Ping").complete();
                long latency = System.currentTimeMillis() - time;
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(":ping_pong: Pong!");
                embed.addField("Latency", latency + "ms", false);
                embed.addField("API","2ms", false);
                embed.setColor(EMBED_COLOR);
                event.getChannel().sendMessage(embed.build()).queue();
                msg.delete().queue();
                return true;
            }
        }, new Command("help", "Displays a list of available commands","{prefix}help (optional: category/command)", Command.Category.OTHER) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(":robot: TechnoBot Commands");
                embed.addField("Moderator", "`!help moderator`", false);
                embed.addField("Levels", "`!help levels`", false);
                embed.addField("Commands", "`!help commands`", false);
                embed.addField("Music", "`!help music`", false);
                embed.setColor(EMBED_COLOR);
                event.getChannel().sendMessage(embed.build()).queue();
                return true;
            }
        }, new Command("suggest", "Suggest a feature or idea related to the server", "{prefix}suggest <idea>", Command.Category.OTHER) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if (args.length > 0) {
                    EmbedBuilder embed = new EmbedBuilder();
                    StringBuilder msg = new StringBuilder();
                    for (int i = 0; i < args.length; i++) {
                        msg.append(args[i]).append(" ");
                    }
                    embed.setTitle("Suggestion");
                    embed.setFooter(event.getAuthor().getAsTag(), event.getAuthor().getAvatarUrl());
                    embed.setDescription(msg.toString());
                    embed.setColor(EMBED_COLOR);
                    TextChannel channel = event.getGuild().getTextChannelsByName("SUGGESTIONS", true).get(0);
                    channel.sendMessage(embed.build()).queue(message -> {
                        message.addReaction(":upvote:733030671802695860").queue();
                        message.addReaction(":downvote:733030678832087120").queue();
                    });
                } else {
                    event.getChannel().sendMessage("USAGE: !suggest <message>").queue();
                }
                return true;
            }
        }, new Command("youtube", "Sends a link to TechnoVision's YouTube Channel", "{prefix}youtube", Command.Category.OTHER) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                event.getChannel().sendMessage("Check out TechnoVision's YouTube channel: https://youtube.com/c/TechnoVisionTV").queue();
                return true;
            }
        });

        getInstance().getRegistry().addListeners(getInstance().getJDA());
    }

    private static String getToken() {
        return getInstance().getBotConfig().getJson().getString("token");
    }

    public BotRegistry getRegistry() {
        return registry;
    }
}
