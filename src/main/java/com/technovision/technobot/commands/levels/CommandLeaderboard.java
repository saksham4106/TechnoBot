package com.technovision.technobot.commands.levels;

import com.google.common.collect.Sets;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.LevelManager;
import com.technovision.technobot.util.Tuple;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

public class CommandLeaderboard extends Command {

    private final DecimalFormat formatter;

    public CommandLeaderboard() {
        super("leaderboard", "Shows the level Leaderboard", "{prefix}leaderboard <page>", Command.Category.LEVELS);
        formatter = new DecimalFormat("#,###");
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        int usersPerPage = 20;
        int start = 0;
        List<Tuple<Integer, Integer>> tuples = LevelManager.getInstance().tupleList;
        if (args.length > 0) {
            try {
                int page = Integer.parseInt(args[0]);
                if (page > 1) {
                    int comparison = (tuples.size() / usersPerPage) + 1;
                    if (tuples.size() % usersPerPage != 0) { comparison++; }
                    if (page >= comparison) {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(ERROR_EMBED_COLOR)
                                .setDescription(":x: That page doesn't exist!");
                        event.getChannel().sendMessage(embed.build()).queue();
                        return true;
                    }
                    start = (usersPerPage * (page - 1)) - 1;
                }
            } catch (NumberFormatException e) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(ERROR_EMBED_COLOR)
                        .setDescription(":x: That is not a valid page number!");
                event.getChannel().sendMessage(embed.build()).queue();
                return true;
            }
        }
        String msg = "";
        int finish = start + usersPerPage;
        if (start != 0) { finish++; }
        if (start != 0) { start++; }

        for (int i = start; i < finish; i++) {
            try {
                Tuple<Integer, Integer> tup = tuples.get(i);
                User u = LevelManager.getInstance().userList.get(i);
                msg += (i + 1) + ". <@!"+u.getId()+"> " + formatter.format(tup.value) + "xp " + "lvl " + tup.key + "\n";
            } catch (IndexOutOfBoundsException ignored) {}
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(":trophy: Rank Leaderboard");
        builder.setColor(EMBED_COLOR);
        builder.setDescription(msg);
        int maxPage = tuples.size() / usersPerPage;
        if (maxPage * usersPerPage != tuples.size()) { maxPage++; }
        builder.setFooter("Page " + (1 + (start / usersPerPage)) + "/" + maxPage);
        event.getChannel().sendMessage(builder.build()).queue();
        return true;
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("ranks", "lvls", "leaderboards");
    }
}
