package com.technovision.technobot.util;

import com.technovision.technobot.logging.Loggable;
import com.technovision.technobot.logging.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Loggable(display = "BotRegistry")
public class BotRegistry {
    private final Logger logger = new Logger(this);
    private final List<ListenerAdapter> eventListeners = new ArrayList<ListenerAdapter>();

    public BotRegistry() {}

    public void registerEventListener(@NotNull ListenerAdapter listener) {
        eventListeners.add(listener);
    }

    public void registerEventListeners(@NotNull ListenerAdapter... listeners) {
        Arrays.asList(listeners).forEach(this::registerEventListener);
    }

    public void addListeners(@NotNull JDA jda) {
        logger.log(Logger.LogLevel.INFO, "Beginning EventListener Initialization Process");

        for(ListenerAdapter listener : eventListeners) {
            logger.log(Logger.LogLevel.INFO, "Adding EventListener: "+listener.getClass().getName());
            jda.addEventListener(listener);
        }

        logger.log(Logger.LogLevel.INFO, "Finished EventListener Initialization Process");
    }
}
