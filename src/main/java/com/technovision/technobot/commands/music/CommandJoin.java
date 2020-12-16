package com.technovision.technobot.commands.music;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandJoin extends Command {
    private final MusicManager musicManager;

    public CommandJoin(final MusicManager musicManager) {
        super("join", "Joins your current voice channel", "{prefix}join", Command.Category.MUSIC);
        this.musicManager = musicManager;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if(event.getMember()==null||event.getMember().getVoiceState()==null||!event.getMember().getVoiceState().inVoiceChannel()||event.getMember().getVoiceState().getChannel()==null) {
            event.getChannel().sendMessage("You are not in a voice channel!").queue();
            return true;
        }
        musicManager.joinVoiceChannel(event.getGuild(), event.getMember().getVoiceState().getChannel(), event.getChannel());
        event.getChannel().sendMessage("Joined `"+event.getMember().getVoiceState().getChannel().getName()+"`").queue();
        return true;
    }
}
