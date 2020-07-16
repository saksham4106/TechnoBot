package com.technovision.technobot.commands;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.listeners.CommandEventListener;
import com.technovision.technobot.listeners.LevelManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.json.JSONObject;

import java.io.File;
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
                        setTitle(":robot: TechnoBot Commands");
                        setColor(EMBED_COLOR);
                        setThumbnail("https://cdn.discordapp.com/avatars/595024631438508070/08e21a9478909deacd7bebb29e98a329.png");
                        setFooter(event.getAuthor().getAsTag(), event.getAuthor().getAvatarUrl());
                        categories.forEach((category, commands) -> {
                            addField((category.name().charAt(0) + "").toUpperCase() + category.name().substring(1).toLowerCase(), commands.size() + " commands in category | `" + CommandEventListener.PREFIX + "help " + category.name().toLowerCase() + "`", false);
                        });
                    }}.build()).queue();
                } else if(args.length<=2) {
                    for (Category c : categories.keySet()) {
                        if (args[0].equalsIgnoreCase(c.name())) {
                            if(args.length==2) {
                                for(Command cmd : categories.get(c)) {
                                    if(args[1].equalsIgnoreCase(cmd.name)) {
                                        EmbedBuilder builder = new EmbedBuilder()
                                                .setTitle(":robot: TechnoBot Commands")
                                                .setDescription("Category: "+(c.name().charAt(0) + "").toUpperCase() + c.name().substring(1).toLowerCase()+" | Command: "+cmd.name)
                                                .addField("Name", cmd.name, true)
                                                .addField("Description", cmd.description, true)
                                                .addField("Category", (""+cmd.category.name().charAt(0)).toUpperCase()+cmd.category.name().substring(1).toLowerCase(), true)
                                                .addField("Usage", cmd.usage.replaceAll("\\{prefix}",CommandEventListener.PREFIX), true);
                                        event.getChannel().sendMessage(builder.build()).queue();
                                        return true;
                                    }
                                }
                            }
                            EmbedBuilder builder = new EmbedBuilder()
                                    .setTitle(":robot: TechnoBot Commands")
                                    .setDescription("Category: " + (c.name().charAt(0) + "").toUpperCase() + c.name().substring(1).toLowerCase())
                                    .setColor(EMBED_COLOR);
                            for (Command cmd : categories.get(c)) {
                                builder.addField(cmd.name, cmd.description + "\n`" + CommandEventListener.PREFIX + "help " + c.name().toLowerCase() + " " + cmd.name.toLowerCase() + "`", true);
                            }

                            event.getChannel().sendMessage(builder.build()).queue();

                            return true;
                        }
                    }
                    event.getChannel().sendMessage("Unknown Category: `" + args[0].toLowerCase() + "`").queue();
                } else {
                    event.getChannel().sendMessage("Usage: "+usage.replaceAll("\\{prefix}",CommandEventListener.PREFIX)).queue();
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
                    embed.setFooter(event.getAuthor().getAsTag(), event.getAuthor().getAvatarUrl());
                    embed.addField("Suggestion", msg.toString(), false);
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

        TechnoBot.getInstance().getRegistry().registerCommands(new Command("kick", "Kicks the specified user for specified reason", "{prefix}kick <user> (optional reason.. all args)", Command.Category.STAFF) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                Member executor = event.getMember();
                Member target = null;
                try {
                    target = event.getMessage().getMentionedMembers().get(0);
                } catch(Exception e) {
                    // there was no mentioned user, using second check
                }

                if(!executor.hasPermission(Permission.KICK_MEMBERS)) {
                    event.getChannel().sendMessage("You do not have permission to do that!").queue();
                    return true;
                }


                if(target==null) {
                    try {
                        target = event.getGuild().getMemberById(args[0]);
                    } catch(Exception ignored) {}
                }
                if(target==null) {
                    event.getChannel().sendMessage("Could not find user!").queue();
                    return true;
                }
                if(executor.getUser().getId().equalsIgnoreCase(target.getUser().getId())) {
                    event.getChannel().sendMessage("You can't kick yourself \uD83E\uDD26\u200D").queue();
                    return true;
                }
                if(!executor.canInteract(target)) {
                    event.getChannel().sendMessage("You can't kick that user!").queue();
                    return true;
                }

                if(args.length==0) {
                    event.getChannel().sendMessage("Please specify a user and reason!").queue();
                    return true;
                }

                String reason = "Unspecified";

                if(args.length>1) {
                    reason = String.join(" ", args);
                    reason = reason.substring(reason.indexOf(" "));
                }

                target.kick(reason).complete();

                event.getChannel().sendMessage(new EmbedBuilder()
                .setTitle("Success")
                .setDescription("Successfully kicked <@!"+target.getUser().getId()+"> for reason `"+reason.replaceAll("`","")+"`").build()).queue();

                return true;
            }
        }, new Command("ban", "Bans the specified user for specified reason", "{prefix}ban <user> (optional reason.. all args", Command.Category.STAFF) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                Member executor = event.getMember();
                Member target = null;
                try {
                    target = event.getMessage().getMentionedMembers().get(0);
                } catch(Exception e) {
                    // there was no mentioned user, using second check
                }

                if(!executor.hasPermission(Permission.BAN_MEMBERS)) {
                    event.getChannel().sendMessage("You do not have permission to do that!").queue();
                    return true;
                }


                if(target==null) {
                    try {
                        target = event.getGuild().getMemberById(args[0]);
                    } catch(Exception ignored) {}
                }
                if(target==null) {
                    event.getChannel().sendMessage("Could not find user!").queue();
                    return true;
                }
                if(executor.getUser().getId().equalsIgnoreCase(target.getUser().getId())) {
                    event.getChannel().sendMessage("You can't ban yourself \uD83E\uDD26\u200D").queue();
                    return true;
                }
                if(!executor.canInteract(target)) {
                    event.getChannel().sendMessage("You can't ban that user!").queue();
                    return true;
                }

                if(args.length==0) {
                    event.getChannel().sendMessage("Please specify a user and reason!").queue();
                    return true;
                }

                String reason = "Unspecified";

                if(args.length>1) {
                    reason = String.join(" ", args);
                    reason = reason.substring(reason.indexOf(" "));
                }

                target.ban(0, reason).complete();

                event.getChannel().sendMessage(new EmbedBuilder()
                        .setTitle("Success")
                        .setDescription("Successfully banned <@!"+target.getUser().getId()+"> for reason `"+reason.replaceAll("`","")+"`").build()).queue();

                return true;
            }
        });

        TechnoBot.getInstance().getRegistry().registerCommands(new Command("rank", "Displays your levels and rank", "{prefix}rank", Command.Category.LEVELS) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                for(Object o : LevelManager.getInstance().levelSave.getJson().getJSONArray("users")) {
                    if(((JSONObject)o).getLong("id")==event.getAuthor().getIdLong()) {
                        JSONObject player = (JSONObject)o;

                        float percent = ( (float)(player.getInt("xp")*100) / (float)( (player.getInt("level")*300) ) );
                        String percentStr = String.valueOf(percent).substring(0, Math.min(String.valueOf(percent).length(), 4));

                        event.getChannel().sendMessage(new EmbedBuilder()
                                .setTitle("Rank")
                                .setColor(EMBED_COLOR)
                                .setThumbnail(event.getAuthor().getAvatarUrl())
                                .addField("XP", player.getInt("xp")+" / "+(player.getInt("level")*300)+" | "+percentStr+"%", false)
                                .addField("Level", player.getInt("level")+"", false)
                                .setFooter(event.getAuthor().getName(), event.getAuthor().getAvatarUrl())
                                .build()).queue();
                    }
                }
                return true;
            }
        }, new Command("leaderboard", "Shows the level Leaderboard", "{prefix}leaderboard", Command.Category.LEVELS) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                event.getChannel().sendMessage(":construction: Work in Progress :construction:").queue();

                return true;
            }
        });
    }
}
