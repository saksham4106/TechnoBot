package com.technovision.technobot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandQueue extends Command {

    public CommandQueue() {
        super("queue", "Displays a queue of songs", "{prefix}queue", Command.Category.MUSIC);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embed = new EmbedBuilder();
        if(MusicManager.getInstance().handlers.get(event.getGuild().getIdLong())==null) {
            embed.setDescription(":x: There's no song in the queue for me to play. **!play** a song first.");
            embed.setColor(ERROR_EMBED_COLOR);
            event.getChannel().sendMessage(embed.build()).queue();
            return true;
        }
        List<AudioTrack> tracks = MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy();
        if(tracks.size()==0||tracks.get(0)==null) {
            embed.setDescription(":x: There's no song in the queue for me to play. **!play** a song first.");
            embed.setColor(ERROR_EMBED_COLOR);
            event.getChannel().sendMessage(embed.build()).queue();
            return true;
        }

        int totalLength = 0;
        String description = "";
        for (int i = 0; i < tracks.size(); i++) {
            AudioTrack track = tracks.get(i);
            long msPos = track.getInfo().length;
            long minPos = msPos/60000;
            msPos = msPos%60000;
            int secPos = (int) Math.floor((float)msPos/1000f);
            String length = minPos + ":" + ((secPos < 10) ? "0" + secPos : secPos);
            String song = "[" + track.getInfo().title + "](" + track.getInfo().uri + ")";
            if (i == 0) {
                description += "__Now Playing:__";
            }
            else if (i == 1) {
                description += "\n__Up Next:__";
            }

            if (i == 0) {
                description += String.format("\n%s | `%s`\n", song, length);
            } else {
                description += String.format("\n`%d.` %s | `%s`\n", i, song, length);
            }
            totalLength += track.getInfo().length;
        }
        int minTime = totalLength / 60000;
        totalLength %= 60000;
        int secTime = totalLength / 1000;

        if (tracks.size() > 1) {
            description += "\n**" + (tracks.size() - 1) + " Songs in Queue | " + minTime + ":" + ((secTime < 10) ? "0" + secTime : secTime) + " Total Length**";
        }

        embed.setTitle("Music Queue :musical_note:");
        embed.setColor(EMBED_COLOR);
        embed.setDescription(description);

        event.getChannel().sendMessage(embed.build()).queue();
        return true;
    }
}
