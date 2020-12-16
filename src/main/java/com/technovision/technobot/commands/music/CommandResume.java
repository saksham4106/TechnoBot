package com.technovision.technobot.commands.music;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandResume extends Command {
    private final MusicManager musicManager;

    public CommandResume(final MusicManager musicManager) {
        super("resume", "Resumes the player", "{prefix}resume", Command.Category.MUSIC);
        this.musicManager = musicManager;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if(musicManager.handlers.get(event.getGuild().getIdLong())==null||musicManager.handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size()==0) {
            event.getChannel().sendMessage("There are no songs playing.").queue();
            return true;
        }
        MusicManager.TrackScheduler sch = musicManager.handlers.get(event.getGuild().getIdLong()).trackScheduler;
        sch.setPaused(false);
        event.getChannel().sendMessage(":arrow_forward: Unpaused the Player!").queue();
        return true;
    }
}
