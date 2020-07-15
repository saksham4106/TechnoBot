package com.technovision.technobot.commands;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.listeners.CommandEventListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.technovision.technobot.listeners.CommandEventListener.EMBED_COLOR;

/**
 * Registers commands and their execution
 */
public class CommandRegistry {

    public CommandRegistry() {
        TechnoBot.getInstance().getRegistry().registerCommands(new Command("ping","Pings the Discord API","{prefix}ping", Command.Category.OTHER) {
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
                Map<Category, List<Command>> categories = new HashMap<Category,List<Command>>();

                for(Category c : Category.values()) {
                    categories.put(c, new ArrayList<Command>());
                }
                for(Command c : TechnoBot.getInstance().getRegistry().getCommands()) {
                    categories.get(c.category).add(c);
                }

                if(args.length==0) {
                    event.getChannel().sendMessage(new EmbedBuilder() {{
                        setTitle(":bot: TechnoBot Commands");
                        setColor(EMBED_COLOR);
                        setThumbnail("https://cdn.discordapp.com/avatars/595024631438508070/08e21a9478909deacd7bebb29e98a329.png");
                        setFooter(event.getAuthor().getAsTag(),event.getAuthor().getAvatarUrl());
                        categories.forEach((category, commands) -> {
                            addField((category.name().charAt(0)+"").toUpperCase()+category.name().substring(1).toLowerCase(), commands.size()+" commands in category | `"+CommandEventListener.PREFIX+"help "+category.name().toLowerCase()+"`",false);
                        });
                    }}.build()).queue();
                } else {
                    event.getChannel().sendMessage("Usage: "+usage).queue();
                }
                return true;
            }
        }, new Command("suggest", "Suggest a feature or idea related to the server", "{prefix}suggest <idea>", Command.Category.OTHER) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if (args.length > 0) {
                    EmbedBuilder embed = new EmbedBuilder();
                    StringBuilder msg = new StringBuilder();
                    for (String arg : args) {
                        msg.append(arg).append(" ");
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
    }
}
