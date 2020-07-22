package com.technovision.technobot.commands.levels;

import com.google.common.collect.Sets;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.LevelManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

public class CommandRankcard extends Command {

    public CommandRankcard() {
        super("rankcard", "Customize your rank card", "{prefix}rankcard", Command.Category.LEVELS);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if (args.length > 0) {
            for (Object o : LevelManager.getInstance().levelSave.getJson().getJSONArray("users")) {
                if (((JSONObject) o).getLong("id") == event.getAuthor().getIdLong()) {
                    JSONObject player = (JSONObject) o;
                    switch (args[0].toUpperCase()) {
                        case "OPACITY":
                            if (args.length > 1) {
                                try {
                                    double opacity = Double.parseDouble(args[1]);
                                    if (opacity >= 0 && opacity <= 100) {
                                        if (opacity > 1) {
                                            opacity *= 0.01;
                                        }
                                        player.put("opacity", opacity);
                                        event.getChannel().sendMessage("Opacity updated!").queue();
                                    } else {
                                        event.getChannel().sendMessage("Invalid value! Either provide a float [0, 1] or percentage [0, 100]").queue();
                                    }
                                } catch (NumberFormatException e) {
                                    event.getChannel().sendMessage("Invalid value! Either provide a float [0, 1] or percentage [0, 100]").queue();
                                }
                            }
                            break;
                        case "ACCENT":
                            if (args.length > 1) {
                                try {
                                    String accent = args[1];
                                    if (!accent.startsWith("#")) {
                                        accent = "#" + accent;
                                    }
                                    Color.decode(accent);
                                    player.put("accent", accent);
                                    event.getChannel().sendMessage("Accent color updated!").queue();
                                } catch (NumberFormatException e) {
                                    event.getChannel().sendMessage("That is not a valid hex code, please redo the command and pass a valid color.").queue();
                                }
                            }
                            break;
                        case "COLOR":
                            if (args.length > 1) {
                                try {
                                    String color = args[1];
                                    if (!color.startsWith("#")) {
                                        color = "#" + color;
                                    }
                                    Color.decode(color);
                                    player.put("color", color);
                                    event.getChannel().sendMessage("Color updated!").queue();
                                } catch (NumberFormatException e) {
                                    event.getChannel().sendMessage("That is not a valid hex code, please redo the command and pass a valid color.").queue();
                                }
                            }
                            break;
                        case "BG":
                        case "BACKGROUND":
                            if (args.length > 1) {
                                try {
                                    URL url = new URL(args[1]);
                                    ImageIO.read(url);
                                    player.put("background", args[1]);
                                    event.getChannel().sendMessage("Updated your background!").queue();
                                } catch (IOException e) {
                                    event.getChannel().sendMessage("Could not change to that background.").queue();
                                }
                            }
                            break;
                        case "DEFAULT":
                        case "RESET":
                            player.put("opacity", 0.5);
                            player.put("color", "#8394eb");
                            player.put("accent", "#FFFFFF");
                            player.put("background", "");
                            event.getChannel().sendMessage("Reset your rank card to default settings!").queue();
                            break;
                    }
                    return true;
                }
            }
        }
        EmbedBuilder msg = new EmbedBuilder();
        msg.setTitle(":paintbrush: Customize Rank Card");
        msg.addField("rankcard background [url]", "Sets the background of your level card.", false);
        msg.addField("rankcard color <color>", "Sets the base color for your level card.", false);
        msg.addField("rankcard accent <color>", "Sets the accent color for your level card.", false);
        msg.addField("rankcard opacity <opacity>", "Sets the opacity for your level card.", false);
        msg.addField("rankcard reset", "Resets customization to default settings.", false);
        event.getChannel().sendMessage(msg.build()).queue();
        return true;
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("customize", "card");
    }
}
