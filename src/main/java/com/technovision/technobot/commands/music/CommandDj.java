package com.technovision.technobot.commands.music;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandDj extends Command {
    private final MusicManager musicManager;

    public CommandDj(final MusicManager musicManager) {
        super("dj", "Opens the DJ Panel", "{prefix}dj", Command.Category.MUSIC);
        this.musicManager = musicManager;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if(musicManager.handlers.get(event.getGuild().getIdLong())==null||musicManager.handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size()==0) {
            event.getChannel().sendMessage("There are no songs playing.").queue();
            return true;
        }
        MusicManager.TrackScheduler sch = musicManager.handlers.get(event.getGuild().getIdLong()).trackScheduler;
        EmbedBuilder emb = new EmbedBuilder()
                .setTitle("DJ Panel")
                .setDescription("<@!"+event.getAuthor().getId()+">'s DJ Panel")
                .setColor(EMBED_COLOR);
        emb = musicManager.assembleEmbed(emb.build(), sch);
        event.getMessage().delete().complete();
        if(musicManager.djMessages.containsKey(event.getAuthor())) musicManager.djMessages.get(event.getAuthor()).delete().complete();
        Message msg = event.getChannel().sendMessage(emb.build()).complete();
        msg.addReaction("⏯").queue();
        msg.addReaction("\uD83D\uDD02").queue();
        msg.addReaction("⏭").queue();
        msg.addReaction("\uD83D\uDD01").queue();
        msg.addReaction("\uD83D\uDD00").queue();

        musicManager.djMessages.put(event.getAuthor(), msg);
        return true;
    }
}
