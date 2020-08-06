package com.technovision.technobot.commands.other;

import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandReport extends Command {

    public CommandReport() {
        super("report","Report a user to a staff","{prefix}report", Command.Category.OTHER);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Report");
        embed.setColor(ERROR_EMBED_COLOR);
        if(args.length > 0) {
            Guild guild = event.getGuild();
            List<Member> staffList = guild.getMembersWithRoles(guild.getRoleById("599344898856189984"));
            Member staffMember = getOnlineStaff(staffList);
            String message = String.join(" ", args);
            embed.setColor(EMBED_COLOR);
            embed.setFooter("report by "+event.getAuthor().getAsTag());
            embed.setDescription(message);
            staffMember.getUser().openPrivateChannel().complete().sendMessage(embed.build()).queue();
            embed.setDescription("Successfully sent report to "+staffMember.getUser().getAsTag());
        } else {
            embed.setDescription("Not enough arguments!");
        }
        event.getTextChannel().sendMessage(embed.build()).queue();
        return true;
    }

    private Member getOnlineStaff(List<Member> staffs) {
        for(Member staff : staffs) {
            if(staff.getOnlineStatus() != OnlineStatus.OFFLINE) return staff;
        }
        //If no staff online, send report to Techno.
        return staffs.get(0).getGuild().getMemberById("595024631438508070");
    }
}
