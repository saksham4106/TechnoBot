package com.technovision.technobot.commands.other;

import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandGoogle extends Command {

    public CommandGoogle() {
        super("google","Creates a google search","{prefix}google", Command.Category.OTHER);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("<:google:315469742460633088> Google");
        embed.setColor(ERROR_EMBED_COLOR);
        if(args.length == 1) {
            embed.setDescription(String.format("https://lmgtfy.com/?q=%s&pp=1&iie=1", args[0].replace(" ", "%20")));
            embed.setColor(EMBED_COLOR);
        } else if(args.length < 1) {
            embed.setDescription("Not enough arguments!");
        } else {
            embed.setDescription("Too many arguments!");
        }
        event.getChannel().sendMessage(embed.build()).queue();
        return true;
    }
}
