package com.technovision.technobot.commands.other;

import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandSuggest extends Command {
    public CommandSuggest() {
        super("suggest", "Suggest a feature or idea related to the server", "{prefix}suggest [content]", Command.Category.OTHER);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if (args.length > 0) {
            EmbedBuilder embed = new EmbedBuilder();
            StringBuilder msg = new StringBuilder();
            for (String arg : args) {
                msg.append(arg).append(" ");
            }
            embed.setFooter(event.getAuthor().getAsTag(), event.getAuthor().getEffectiveAvatarUrl());
            embed.addField("Suggestion", msg.toString(), false);
            embed.setColor(EMBED_COLOR);
            TextChannel channel = event.getGuild().getTextChannelsByName("SUGGESTIONS", true).get(0);
            channel.sendMessage(embed.build()).queue(message -> {
                message.addReaction(":upvote:733030671802695860").queue();
                message.addReaction(":downvote:733030678832087120").queue();
            });
        } else {
            event.getChannel().sendMessage("USAGE: !suggest <message>").queue();
        }
        return true;
    }
}
