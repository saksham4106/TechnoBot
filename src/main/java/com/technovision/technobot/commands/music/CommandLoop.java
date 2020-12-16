package com.technovision.technobot.commands.music;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandLoop extends Command {
    private final MusicManager musicManager;

    public CommandLoop(final MusicManager musicManager) {
        super("loop", "Loop currently playing song without removing other queued songs", "{prefix}loop", Command.Category.MUSIC);
        this.musicManager = musicManager;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if(musicManager.handlers.get(event.getGuild().getIdLong())==null||musicManager.handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size()==0) {
            event.getChannel().sendMessage("There are no songs playing.").queue();
            return true;
        }
        musicManager.handlers.get(event.getGuild().getIdLong()).trackScheduler.toggleLoop(event.getChannel());
        return true;
    }
}
