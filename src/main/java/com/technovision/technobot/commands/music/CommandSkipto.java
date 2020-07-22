package com.technovision.technobot.commands.music;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Timer;
import java.util.TimerTask;

public class CommandSkipto extends Command {

    public CommandSkipto() {
        super("skipto", "Skips to song index in queue", "{prefix}skipto <number>", Command.Category.MUSIC);
    }

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
}
