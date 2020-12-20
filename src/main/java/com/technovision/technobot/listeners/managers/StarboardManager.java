package com.technovision.technobot.listeners.managers;

import com.technovision.technobot.data.Configuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StarboardManager extends ListenerAdapter {

    public static final String EMOTE = "\u2B50";
    public static final long SHOWCASE = 790017209245368341L;
    public static final long STARBOARD = 790017510468223006L;

    private final Configuration data;
    private final JSONObject posts;

    public StarboardManager() {
        data = new Configuration("data/", "starboard.json") {
            @Override
            public void load() {
                super.load();
                if(!getJson().has("posts")) getJson().put("posts", new JSONObject());
            }
        };
        posts = data.getJson().getJSONObject("posts");
        data.save();
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        Message msg = event.getMessage();
        if (event.getChannel().getIdLong() == SHOWCASE) {
            if (!msg.getAttachments().isEmpty()) {
                if (msg.getAttachments().get(0).isImage()) {
                    String msgID = String.valueOf(msg.getIdLong());
                    posts.put(msgID, new JSONObject());
                    posts.getJSONObject(msgID).put("stars", 0);
                    posts.getJSONObject(msgID).put("onBoard", false);
                    data.save();
                    msg.addReaction(EMOTE).queue();
                }
            }
        }
    }

    @Override
    public synchronized void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        //Excludes bot/self reacts and anything outside of #showcase
        if (event.getUser().isBot()) { return; }
        if (event.getChannel().getIdLong() == SHOWCASE) {
            //Checks for star emoji
            if (event.getReactionEmote().getAsReactionCode().equalsIgnoreCase(EMOTE)) {
                //Add react to post
                String msgID = String.valueOf(event.getMessageIdLong());
                int stars = posts.getJSONObject(msgID).getInt("stars");
                stars++;
                posts.getJSONObject(msgID).put("stars", stars);
                data.save();

                //Add to starboard if stars == 3
                if (stars == 3 && !posts.getJSONObject(msgID).getBoolean("onBoard")) {
                    event.getGuild().getTextChannelById(STARBOARD).sendMessage(":star: **" + stars + "** <#" + SHOWCASE + ">").queue((message) -> {
                        event.getGuild().getTextChannelById(SHOWCASE).retrieveMessageById(msgID).queue((originalMessage) -> {
                            message.editMessage(new EmbedBuilder()
                                    .setAuthor(originalMessage.getAuthor().getName(), null, originalMessage.getAuthor().getEffectiveAvatarUrl())
                                    .setDescription(originalMessage.getContentRaw())
                                    .setImage(originalMessage.getAttachments().get(0).getUrl())
                                    .addField("**Source**", "[Jump to Message!](" + originalMessage.getJumpUrl() + ")", false)
                                    .setFooter(msgID)
                                    .setTimestamp(new Date().toInstant())
                                    .setColor(0xfffa74)
                                    .build()).queue();
                            posts.getJSONObject(msgID).put("boardID", message.getIdLong());
                            posts.getJSONObject(msgID).put("onBoard", true);
                        });
                    });
                }
                //Update existing starboard
                else if (posts.getJSONObject(msgID).getBoolean("onBoard")) {
                    long id = posts.getJSONObject(msgID).getLong("boardID");
                    int finalStars = stars;
                    event.getGuild().getTextChannelById(STARBOARD).retrieveMessageById(id).queue((message) -> {
                        message.editMessage( getStarEmoji(finalStars) + " **" + finalStars + "** <#" + SHOWCASE + ">").queue();
                    });
                }
                data.save();
            }
        }
    }

    @Override
    public synchronized void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        //Excludes bot/self reacts and anything outside of #showcase
        if (event.getChannel().getIdLong() == SHOWCASE) {
            //Checks for star emoji
            if (event.getReactionEmote().getAsReactionCode().equalsIgnoreCase(EMOTE)) {
                String msgID = String.valueOf(event.getMessageIdLong());
                int stars = posts.getJSONObject(msgID).getInt("stars");
                stars--;
                posts.getJSONObject(msgID).put("stars", stars);
                if (!posts.getJSONObject(msgID).getBoolean("onBoard")) { return; }

                long boardID = posts.getJSONObject(msgID).getLong("boardID");
                int finalStars = stars;
                //Delete starboard if stars <= 0
                if (finalStars <= 0) {
                    event.getGuild().getTextChannelById(STARBOARD).deleteMessageById(boardID).queue();
                    posts.remove(msgID);
                    posts.put(msgID, new JSONObject());
                    posts.getJSONObject(msgID).put("stars", 0);
                    posts.getJSONObject(msgID).put("onBoard", false);
                }
                //Update existing starboard
                else {
                    event.getGuild().getTextChannelById(STARBOARD).retrieveMessageById(boardID).queue((message) -> {
                        message.editMessage( getStarEmoji(finalStars) + " **" + finalStars + "** <#" + SHOWCASE + ">").queue();
                    });
                }
                data.save();
            }
        }
    }

    @Override
    public synchronized void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        if (event.getChannel().getIdLong() == SHOWCASE) {
            String msgID = String.valueOf(event.getMessageIdLong());
            if (posts.has(msgID)) {
                if (posts.getJSONObject(msgID).getBoolean("onBoard")) {
                    long boardID = posts.getJSONObject(msgID).getLong("boardID");
                    event.getGuild().getTextChannelById(STARBOARD).deleteMessageById(boardID).queue();
                }
                posts.remove(msgID);
                data.save();
            }
        }
    }

    public String getStarEmoji(int stars) {
        String emoji = ":star:";
        if (stars >= 10 && stars < 15) {
            emoji = ":star2:";
        }
        else if (stars >= 15 && stars < 20) {
            emoji = ":dizzy:";
        }
        else if (stars >= 20) {
            emoji = ":star_struck:";
        }
        return emoji;
    }
}
