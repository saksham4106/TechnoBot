package com.technovision.technobot.listeners;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.logging.Logger;
import com.technovision.technobot.util.BotRegistry;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

/**
 * Command Listener and Executor.
 * @author TechnoVision
 * @author Sparky
 */
public class CommandEventListener extends ListenerAdapter {

    public static final String PREFIX = "!";
    public static final int EMBED_COLOR = 0x7289da;

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) { return; }
        String[] mArray = event.getMessage().getContentRaw().split(" ");
        String command = mArray[0];
        if (command.startsWith(PREFIX)) {
            String[] args = new String[mArray.length - 1];
            for (int i = 0; i < mArray.length; i++) {
                if (i > 0) args[i - 1] = mArray[i];
            }

            BotRegistry registry = TechnoBot.getInstance().getRegistry();

            for (Command cmd : registry.getCommands()) {
                if ((PREFIX + cmd.name).equalsIgnoreCase(command)) {
                    if (!cmd.execute(event, args)) {
                        TechnoBot.getInstance().getLogger().log(Logger.LogLevel.SEVERE, "Command failed to execute!");
                    }
                    return;
                }
            }
            event.getChannel().sendMessage("Unknown Command!").queue();
        }
    }
}
