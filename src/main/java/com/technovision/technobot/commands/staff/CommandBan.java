package com.technovision.technobot.commands.staff;

import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandBan extends Command {


    public CommandBan() {
        super("ban", "Bans the specified user for specified reason", "{prefix}ban <user> [reason]", Command.Category.STAFF);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        Member executor = event.getMember();
        Member target = null;
        try {
            target = event.getMessage().getMentionedMembers().get(0);
        } catch(Exception e) {
            // there was no mentioned user, using second check
        }

        if(!executor.hasPermission(Permission.BAN_MEMBERS)) {
            event.getChannel().sendMessage("You do not have permission to do that!").queue();
            return true;
        }


        if(target==null) {
            try {
                target = event.getGuild().getMemberById(args[0]);
            } catch(Exception ignored) {}
        }
        if(target==null) {
            event.getChannel().sendMessage("Could not find user!").queue();
            return true;
        }
        if(executor.getUser().getId().equalsIgnoreCase(target.getUser().getId())) {
            event.getChannel().sendMessage("You can't ban yourself \uD83E\uDD26\u200D").queue();
            return true;
        }
        if(!executor.canInteract(target)) {
            event.getChannel().sendMessage("You can't ban that user!").queue();
            return true;
        }

        if(args.length==0) {
            event.getChannel().sendMessage("Please specify a user and reason!").queue();
            return true;
        }

        String reason = "Unspecified";

        if(args.length>1) {
            reason = String.join(" ", args);
            reason = reason.substring(reason.indexOf(" "));
        }

        target.ban(0, reason).complete();

        event.getChannel().sendMessage(new EmbedBuilder()
                .setTitle("Success")
                .setDescription("Successfully banned <@!"+target.getUser().getId()+"> for reason `"+reason.replaceAll("`","")+"`").build()).queue();

        return true;
    }
}
