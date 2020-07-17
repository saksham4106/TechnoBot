package com.technovision.technobot.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.images.ImageProcessor;
import com.technovision.technobot.listeners.CommandEventListener;
import com.technovision.technobot.listeners.LevelManager;
import com.technovision.technobot.listeners.MusicManager;
import com.technovision.technobot.logging.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.json.JSONObject;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static com.technovision.technobot.listeners.CommandEventListener.EMBED_COLOR;
import static com.technovision.technobot.listeners.CommandEventListener.PREFIX;

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

        TechnoBot.getInstance().getRegistry().registerCommands(new Command("rank", "Displays your levels and server rank", "{prefix}rank", Command.Category.LEVELS) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                for(Object o : LevelManager.getInstance().levelSave.getJson().getJSONArray("users")) {
                    if(((JSONObject)o).getLong("id")==event.getAuthor().getIdLong()) {
                        JSONObject player = (JSONObject) o;
                        float percent = ((float) (player.getInt("xp") * 100) / (float) ((player.getInt("level") * 300)));
                        String percentStr = String.valueOf((int) percent);
                        try {
                            //Get Graphics
                            BufferedImage base = ImageIO.read(new File("data/images/rankCardBase.png"));
                            BufferedImage outline = ImageIO.read(new File("data/images/rankCardOutline.png"));
                            Graphics2D g = (Graphics2D) base.getGraphics();
                            g.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
                            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

                            //Add Background
                            BufferedImage background;
                            if (player.getString("background").isEmpty()) {
                                background = ImageIO.read(new File("data/images/rankCardBackground.png"));
                            } else {
                                background = ImageIO.read(new URL(player.getString("background")));
                            }
                            BufferedImage rectBuffer = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
                            Graphics2D g2 = rectBuffer.createGraphics();
                            g2.setClip(new Rectangle2D.Float(0, 0, base.getWidth(), base.getHeight()));
                            int x = base.getWidth() - background.getWidth();
                            int y = base.getHeight() - background.getHeight();
                            if (background.getWidth() >= 934 && background.getHeight() >= 282) {
                                g2.drawImage(background, x / 2, y / 2, null);
                            } else {
                                g2.drawImage(background, 0, 0, base.getWidth(), base.getHeight(), null);
                            }
                            g2.dispose();
                            g.drawImage(rectBuffer, 0, 0, base.getWidth(), base.getHeight(), null);

                            //Add Outline
                            float opacity = player.getFloat("opacity");
                            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
                            g.setComposite(ac);
                            g.drawImage(outline, 0, 0, null);
                            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

                            //Text
                            g.setStroke(new BasicStroke(3));
                            g.setColor(Color.decode(player.getString("accent")));
                            g.setFont(new Font("Helvetica", Font.PLAIN, 52));
                            g.drawLine(300, 140, 870, 140);
                            g.drawString(event.getAuthor().getName(), 300, 110);
                            g.setFont(new Font("Helvetica", Font.PLAIN, 35));
                            g.drawString("Rank #24", 720, 110);
                            g.drawString("Level " + player.getInt("level"), 300, 180);
                            g.setFont(new Font("Helvetica", Font.PLAIN, 25));
                            g.drawString(player.getInt("xp") + " / " + (player.getInt("level") * 300), 750, 180);

                            //XP Bar
                            g.drawRoundRect(300, 200, 570, 40, 20, 20);
                            g.setColor(Color.decode("#101636"));
                            g.fillRoundRect(300, 200, 570, 40, 20, 20);
                            g.setColor(Color.decode(player.getString("color")));
                            g.fillRoundRect(300, 200, (int) (570 * (percent * 0.01)), 40, 20, 20);
                            g.setColor(Color.decode(player.getString("accent")));
                            g.setFont(new Font("Helvetica", Font.PLAIN, 30));
                            g.drawString(percentStr + "%", 560, 230);

                            //Add Avatar
                            BufferedImage avatar = ImageProcessor.getAvatar(event.getAuthor().getAvatarUrl(), 1.62, 1.62);
                            g.setStroke(new BasicStroke(4));
                            int width = avatar.getWidth();
                            BufferedImage circleBuffer = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
                            Graphics2D g3 = circleBuffer.createGraphics();
                            g3.setClip(new Ellipse2D.Float(0, 0, width, width));
                            g3.drawImage(avatar, 0, 0, width, width, null);
                            g3.dispose();
                            g.drawImage(circleBuffer, 55, 38, null);
                            g.setColor(Color.decode(player.getString("color")));
                            g.drawOval(55, 38, width, width);
                            g.dispose();

                            //Save File
                            File rankCard = ImageProcessor.saveImage("data/images/rankCard.png", base);
                            event.getChannel().sendFile(rankCard, "rankCard.png").queue();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                }
                return true;
            }
        }, new Command("rankcard", "Customize your rank card", "{prefix}rankcard", Command.Category.LEVELS) {
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
        }, new Command("leaderboard", "Shows the level Leaderboard", "{prefix}leaderboard", Command.Category.LEVELS) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                event.getChannel().sendMessage(":construction: Work in Progress :construction:").queue();

                return true;
            }
        });

        TechnoBot.getInstance().getRegistry().registerCommands(new Command("join", "Joins your current voice channel", "{prefix}join", Command.Category.MUSIC) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if(event.getMember()==null||event.getMember().getVoiceState()==null||!event.getMember().getVoiceState().inVoiceChannel()||event.getMember().getVoiceState().getChannel()==null) {
                    event.getChannel().sendMessage("You are not in a voice channel!").queue();
                    return true;
                }
                MusicManager.getInstance().joinVoiceChannel(event.getGuild(), event.getMember().getVoiceState().getChannel(), event.getChannel());
                event.getChannel().sendMessage("Joined `"+event.getMember().getVoiceState().getChannel().getName()+"`").queue();
                return true;
            }
        }, new Command("leave", "Leaves the voice channel", "{prefix}join", Command.Category.MUSIC) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if(event.getMember()==null||event.getMember().getVoiceState()==null||!event.getMember().getVoiceState().inVoiceChannel()||event.getMember().getVoiceState().getChannel()==null) {
                    event.getChannel().sendMessage("You are not in a voice channel!").queue();
                    return true;
                }
                MusicManager.getInstance().leaveVoiceChannel(event.getGuild(), event.getMember().getVoiceState().getChannel());
                event.getChannel().sendMessage("Left voice channel!").queue();
                return true;
            }
        }, new Command("play", "Plays music in voice channel", "{prefix}play", Command.Category.MUSIC) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if(event.getMember()==null||event.getMember().getVoiceState()==null||!event.getMember().getVoiceState().inVoiceChannel()||event.getMember().getVoiceState().getChannel()==null) {
                    event.getChannel().sendMessage("You are not in a voice channel!").queue();
                    return true;
                }
                MusicManager.getInstance().joinVoiceChannel(event.getGuild(), event.getMember().getVoiceState().getChannel(), event.getChannel());
                try {
                    MusicManager.getInstance().addTrack(args[0], event.getChannel(), event.getGuild());
                    MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.setPaused(false);
                } catch(IndexOutOfBoundsException e) {
                    event.getChannel().sendMessage("What do you want me to play?").queue();
                }
                return true;
            }
        }, new Command("queue", "Displays a queue of songs", "{prefix}queue", Command.Category.MUSIC) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if(MusicManager.getInstance().handlers.get(event.getGuild().getIdLong())==null) {
                    event.getChannel().sendMessage("Queue is empty.").queue();
                    return true;
                }
                List<AudioTrack> tracks = MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy();
                if(tracks.size()==0||tracks.get(0)==null) {
                    event.getChannel().sendMessage("Queue is empty.").queue();
                    return true;
                }

                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle(event.getGuild().getName()+"'s Queue")
                        .setDescription(tracks.size()+" songs in queue")
                        .addField("Now Playing :musical_note:", "["+tracks.get(0).getInfo().title+"]("+tracks.get(0).getInfo().uri+")\nBy: "+tracks.get(0).getInfo().author, false);
                tracks.remove(0);
                builder.addField("All Songs","All Songs in queue listed below.",false);
                int c = 1;
                int fulltime = 0;
                for(AudioTrack track : tracks) {
                    if(c<=20)
                    builder.addField(c+". "+track.getInfo().title, track.getInfo().uri, false);
                    c++;
                    fulltime += track.getInfo().length;
                }

                int minTime = fulltime / 60000;
                fulltime %= 60000;
                int secTime = fulltime / 1000;


                builder.addField("...and "+(c-20)+" more songs", "Left in queue: "+minTime+":"+((secTime<10)?"0"+secTime:secTime), false);

                event.getChannel().sendMessage(builder.build()).queue();
                return true;
            }
        }, new Command("skip", "Skips the currently playing song", "{prefix}skip", Command.Category.MUSIC) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if(event.getMember()==null||event.getMember().getVoiceState()==null||!event.getMember().getVoiceState().inVoiceChannel()||event.getMember().getVoiceState().getChannel()==null) {
                    event.getChannel().sendMessage("You are not in a voice channel!").queue();
                    return true;
                }
                if(MusicManager.getInstance().handlers.get(event.getGuild().getIdLong())==null) {
                    event.getChannel().sendMessage("Please use `!join` first!").queue();
                    return true;
                }
                if(MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size()==0) {
                    event.getChannel().sendMessage("There are no songs playing.").queue();
                    return true;
                }
                event.getChannel().sendMessage("Skipping...").queue();

                MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.skip();
                return true;
            }
        }, new Command("skipto", "Skips to song index in queue", "{prefix}skipto <number>", Command.Category.MUSIC) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if(MusicManager.getInstance().handlers.get(event.getGuild().getIdLong())==null||MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size()==0) {
                    event.getChannel().sendMessage("There are no songs playing.").queue();
                    return true;
                }

                try {
                    MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.skipTo(Math.min(Integer.parseInt(args[0]), MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size()));
                } catch(IndexOutOfBoundsException e) {
                    event.getChannel().sendMessage("Please specify a position to skip to!").queue();
                } catch(NumberFormatException e) {
                    event.getChannel().sendMessage("That is not a number!").queue();
                }

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        event.getChannel().sendMessage("Skipped to "+MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().get(0).getInfo().title).queue();
                    }
                }, 1000L);

                return true;
            }
        }, new Command("np", "Displays the currently playing song and its duration/position", "{prefix}np", Command.Category.MUSIC) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if(MusicManager.getInstance().handlers.get(event.getGuild().getIdLong())==null||MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size()==0) {
                    event.getChannel().sendMessage("There are no songs playing.").queue();
                    return true;
                }
                AudioTrack currentPlaying = MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().get(0);
                String[] posString = new String[] {"⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯",};
                try {
                    posString[(int) Math.floor((float) currentPlaying.getPosition() / (float) currentPlaying.getDuration() * 30F)] = "~~◉~~";
                } catch(Exception e) {e.printStackTrace();}

                long msPos = currentPlaying.getPosition();
                long minPos = msPos/60000;
                msPos = msPos%60000;
                int secPos = (int) Math.floor((float)msPos/1000f);

                long msDur = currentPlaying.getDuration();
                long minDur = msDur/60000;
                msDur = msDur%60000;
                int secDur = (int) Math.floor((float)msDur/1000f);

                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("Now Playing :musical_note:", currentPlaying.getInfo().uri)
                        .setDescription(currentPlaying.getInfo().title)
                        .setColor(0x00FFFF)
                        .addField("Position", String.join("",posString),false)
                        .addField("Progress", minPos+":"+((secPos<10)?"0"+secPos:secPos)+" / "+minDur+":"+((secDur<10)?"0"+secDur:secDur), false);
                event.getChannel().sendMessage(builder.build()).queue();
                return true;
            }
        }, new Command("seek", "Seek to a position in the currently playing song", "{prefix}seek <seconds>", Command.Category.MUSIC) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if(MusicManager.getInstance().handlers.get(event.getGuild().getIdLong())==null||MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size()==0) {
                    event.getChannel().sendMessage("There are no songs playing.").queue();
                    return true;
                }
                try {
                    MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().get(0).setPosition(Math.min(Integer.parseInt(args[0]) * 1000, MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().get(0).getDuration()));
                } catch(IndexOutOfBoundsException e) {
                    event.getChannel().sendMessage("Please specify a time to seek to!").queue();
                    return true;
                } catch(NumberFormatException e) {
                    event.getChannel().sendMessage("Please specify a *number*!").queue();
                    return true;
                }
                event.getChannel().sendMessage("Seeked to "+args[0]+" seconds on song `"+MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().get(0).getInfo().title+"`!").queue();
                return true;
            }
        }, new Command("loop", "Loop currently playing song without removing other queued songs", "{prefix}loop", Command.Category.MUSIC) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if(MusicManager.getInstance().handlers.get(event.getGuild().getIdLong())==null||MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size()==0) {
                    event.getChannel().sendMessage("There are no songs playing.").queue();
                    return true;
                }
                MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.toggleLoop(event.getChannel());
                return true;
            }
        }, new Command("pause", "Pauses the player", "{prefix}pause", Command.Category.MUSIC) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if(MusicManager.getInstance().handlers.get(event.getGuild().getIdLong())==null||MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size()==0) {
                    event.getChannel().sendMessage("There are no songs playing.").queue();
                    return true;
                }
                MusicManager.TrackScheduler sch = MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler;
                sch.setPaused(true);
                event.getChannel().sendMessage(":pause_button: Paused the Player!").queue();
                return true;
            }
        }, new Command("resume", "Resumes the player", "{prefix}resume", Command.Category.MUSIC) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if(MusicManager.getInstance().handlers.get(event.getGuild().getIdLong())==null||MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size()==0) {
                    event.getChannel().sendMessage("There are no songs playing.").queue();
                    return true;
                }
                MusicManager.TrackScheduler sch = MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler;
                sch.setPaused(false);
                event.getChannel().sendMessage(":arrow_forward: Unpaused the Player!").queue();
                return true;
            }
        }, new Command("dj", "Opens the DJ Panel", "{prefix}dj", Command.Category.MUSIC) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if(MusicManager.getInstance().handlers.get(event.getGuild().getIdLong())==null||MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size()==0) {
                    event.getChannel().sendMessage("There are no songs playing.").queue();
                    return true;
                }
                MusicManager.TrackScheduler sch = MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler;
                EmbedBuilder emb = new EmbedBuilder()
                        .setTitle("DJ Panel")
                        .setDescription("<@!"+event.getAuthor().getId()+">'s DJ Panel")
                        .setColor(EMBED_COLOR);
                emb = MusicManager.getInstance().assembleEmbed(emb.build(), sch);
                event.getMessage().delete().complete();
                if(MusicManager.getInstance().djMessages.containsKey(event.getAuthor())) MusicManager.getInstance().djMessages.get(event.getAuthor()).delete().complete();
                Message msg = event.getChannel().sendMessage(emb.build()).complete();
                msg.addReaction("⏯").queue();
                msg.addReaction("\uD83D\uDD02").queue();
                msg.addReaction("⏭").queue();
                msg.addReaction("\uD83D\uDD01").queue();
                msg.addReaction("\uD83D\uDD00").queue();

                MusicManager.getInstance().djMessages.put(event.getAuthor(), msg);
                return true;
            }
        }, new Command("shuffle", "Shuffles queue", "{prefix}shuffle", Command.Category.MUSIC) {
            @Override
            public boolean execute(MessageReceivedEvent event, String[] args) {
                if(MusicManager.getInstance().handlers.get(event.getGuild().getIdLong())==null||MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size()==0) {
                    event.getChannel().sendMessage("There are no songs playing.").queue();
                    return true;
                }

                MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.shuffle();
                event.getChannel().sendMessage("\uD83D\uDD00 Shuffled Queue!").queue();

                return true;
            }
        });
    }
}
