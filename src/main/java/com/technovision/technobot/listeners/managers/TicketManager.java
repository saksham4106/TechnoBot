package com.technovision.technobot.listeners.managers;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.data.Configuration;
import com.technovision.technobot.logging.Logger;
import com.technovision.technobot.util.TranscriptUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TicketManager extends ListenerAdapter {
    protected static final Timer timer = new Timer();
    private static final long TICKET_CREATE_COOLDOWN = 120000;
    private static final long TICKET_ACTION_COOLDOWN = 3000;
    private final Map<Long, Long> TICKET_CREATE_CDMAP = new HashMap<>();
    private final Map<Long, Long> TICKET_ACTION_CDMAP = new HashMap<>();

    private final TechnoBot bot;
    private final Map<Long, GuildTicketManager> guildMap = new HashMap<>();
    private final Configuration data = new Configuration("data/", "tickets.json") {
        @Override
        public void load() {
            super.load();
            if(!getJson().has("guilds")) getJson().put("guilds", new JSONArray());
        }
    };

    public TicketManager(final TechnoBot bot) {
        this.bot = bot;
        for(Object no : data.getJson().getJSONArray("guilds")) {
            if(!(no instanceof JSONObject)) {
                bot.getLogger().log(Logger.LogLevel.SEVERE, "Failed to initialize guilds from TicketManager config!");
                return;
            }
            JSONObject obj = (JSONObject) no;
            GuildTicketManager guildTicketManager = new GuildTicketManager(bot, this, bot.getJDA().getGuildById(obj.getLong("guildId")), obj.getInt("currentId"));
            guildTicketManager.guild.getTextChannelById(obj.getLong("reactionMessageChannelId")).retrieveMessageById(obj.getLong("reactionMessageId")).queue(message -> guildTicketManager.reactionMessage = message);
            if(obj.has("inboxChannelId") && obj.getLong("inboxChannelId") != -1)
                guildTicketManager.inboxChannel = guildTicketManager.guild.getTextChannelById(obj.getLong("inboxChannelId"));
            else {
                guildTicketManager.inboxChannel = null;
                obj.put("inboxChannelId", -1);
            }

            for(Object no2 : obj.getJSONArray("tickets")) {
                if(!(no2 instanceof JSONObject)) {
                    bot.getLogger().log(Logger.LogLevel.SEVERE, "Failed to initialize guilds from TicketManger config!");
                    return;
                }
                JSONObject ticket = (JSONObject) no2;
                guildTicketManager.createTicketFromConfig(ticket);
            }
            guildMap.put(obj.getLong("guildId"), guildTicketManager);
        }
    }

    private void initGuild(Guild guild) {
        guildMap.putIfAbsent(guild.getIdLong(), new GuildTicketManager(bot, this, guild, 0));
        data.getJson().getJSONArray("guilds").put(new JSONObject() {{
            put("guildId", guild.getIdLong());
            put("currentId", 0);
        }});
        data.save();
    }

    public boolean createReactionMessage(Guild guild, MessageChannel channel) {
        if(!guildMap.containsKey(guild.getIdLong())) initGuild(guild);
        return guildMap.get(guild.getIdLong()).createReactionMessage(channel);
    }

    public boolean setInboxChannel(Guild guild, TextChannel channel) {
        if(!guildMap.containsKey(guild.getIdLong())) initGuild(guild);
        guildMap.get(guild.getIdLong()).inboxChannel = channel;
        guildMap.get(guild.getIdLong()).save();
        return true;
    }

    @Nullable
    public GuildChannel getInboxChannel(Guild guild) {
        if(!guildMap.containsKey(guild.getIdLong())) initGuild(guild);
        return guildMap.get(guild.getIdLong()).inboxChannel;
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if(event.getUser().isBot()) return;
        if(guildMap.containsKey(event.getGuild().getIdLong())) guildMap.get(event.getGuild().getIdLong()).ticketReactionAdded(event);
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;
        if(guildMap.containsKey(event.getGuild().getIdLong())) guildMap.get(event.getGuild().getIdLong()).ticketMessageReceived(event);
    }

    public JSONObject getGuildConfigData(long guildId) {
        for(Object no : data.getJson().getJSONArray("guilds")) {
            if(!(no instanceof  JSONObject)) {
                bot.getLogger().log(Logger.LogLevel.WARNING, "Failed to get config data for "+guildId+"!");
                return null;
            }
            return (JSONObject) no;
        }
        return null;
    }

    private static class GuildTicketManager {
        private int idCurrent = 0;
        private final TechnoBot bot;
        private final TicketManager ticketManager;
        private final Guild guild;
        private Message reactionMessage;
        private TextChannel inboxChannel;
        private final Set<Ticket> tickets = new HashSet<Ticket>();

        protected GuildTicketManager(final TechnoBot bot, final TicketManager ticketManager, Guild guild, int idCurrent) {
            this.bot = bot;
            this.ticketManager = ticketManager;
            this.guild = guild;
            this.idCurrent = idCurrent;
        }

        public void createTicket(Member member) {
            idCurrent++;
            tickets.add(new Ticket(this, member, bot.getLogger()).id(idCurrent).init());
        }

        public void createTicketFromConfig(JSONObject ticketConf) {
            guild.retrieveMemberById(ticketConf.getLong("openerId")).queue(member -> {
                Ticket ticket = new Ticket(this, member, bot.getLogger()).id(ticketConf.getInt("ticketId")).subject(ticketConf.getString("subject")).description(ticketConf.getString("description"));
                ticket.channel = guild.getTextChannelById(ticketConf.getLong("channelId"));
                if(ticket.channel==null) {
                    bot.getLogger().log(Logger.LogLevel.SEVERE, "Could not find ticket channel!");
                    return;
                }
                ticket.channel.retrieveMessageById(ticketConf.getLong("splashMessageId")).queue(message -> {
                    ticket.splashMessage = message;
                    if(!ticketConf.has("inviteMessageId")) ticketConf.put("inviteMessageId", -1L);
                    inboxChannel.retrieveMessageById(ticketConf.getLong("inviteMessageId")).queue(message1 -> {
                        ticket.inviteMessage = message1;
                        ticket.locked = ticketConf.getBoolean("locked");
                        ticket.initialized = true;
                        tickets.add(ticket);
                    },throwable -> {ticket.locked = ticketConf.getBoolean("locked");ticket.initialized = true;tickets.add(ticket);});
                });
            });
        }

        public boolean createReactionMessage(MessageChannel channel) {
            AtomicBoolean ret = new AtomicBoolean(false);
            channel.sendMessage(new EmbedBuilder()
                    .setTitle("\uD83C\uDF9F Create A Support Ticket")
                    .setDescription("Create a ticket to report a user or get support.\n\n**DO NOT USE THIS FOR MODDING SUPPORT!**\n*For modding support, please use a support channel. Thanks!*\n")
                    .addField("Reactions", "Once you're in the ticket, there will be a few reactions!" +
                            "\n\uD83D\uDED1 - Closes the ticket\n" +
                            "\uD83D\uDD10 - Locks the ticket (staff only)" +
                            "\n\uD83D\uDCF0 - Ticket Editing (subject & description)" +
                            "\n\uD83D\uDCE8 - Send To Staff (after subject/description are filled out)\n\n", false)
                    .setFooter("Click The Ticket Emoji Below to Open a Ticket!")
                    .setColor(Command.EMBED_COLOR)
                    .build()
            ).queue(message -> message.addReaction("\uD83C\uDF9F").queue(aVoid -> {
                    reactionMessage = message;
                    ret.set(true);
                }));
            return ret.get();
        }

        public void ticketReactionAdded(@Nonnull final GuildMessageReactionAddEvent event) {
            long time = System.currentTimeMillis();
            if(reactionMessage != null && event.getMessageIdLong() == reactionMessage.getIdLong() && ((!ticketManager.TICKET_CREATE_CDMAP.containsKey(event.getUserIdLong())) || time > TICKET_CREATE_COOLDOWN + ticketManager.TICKET_CREATE_CDMAP.get(event.getUserIdLong()))) {
                event.getReaction().removeReaction(event.getUser()).queue();
                createTicket(event.getMember());
                ticketManager.TICKET_CREATE_CDMAP.put(event.getUserIdLong(), time);
            } else for(Ticket ticket : tickets) {
                if(ticket.channel.getIdLong() == event.getChannel().getIdLong() || event.getChannel().getIdLong() == inboxChannel.getIdLong() && ((!ticketManager.TICKET_ACTION_CDMAP.containsKey(event.getUserIdLong())) || time > TICKET_ACTION_COOLDOWN + ticketManager.TICKET_ACTION_CDMAP.get(event.getUserIdLong()))) {
                    ticket.reactionAdded(event);
                    ticketManager.TICKET_ACTION_CDMAP.put(event.getUserIdLong(), time);
                }
            }
        }

        public void ticketMessageReceived(@Nonnull final GuildMessageReceivedEvent event) {
            long time = System.currentTimeMillis();
            for(Ticket ticket : tickets) {
                if(ticket.channel.getIdLong() == event.getChannel().getIdLong() && ((!ticketManager.TICKET_ACTION_CDMAP.containsKey(event.getAuthor().getIdLong())) || time > TICKET_ACTION_COOLDOWN + ticketManager.TICKET_ACTION_CDMAP.get(event.getAuthor().getIdLong()))) {
                    ticket.messageReceived(event);
                    ticketManager.TICKET_ACTION_CDMAP.put(event.getAuthor().getIdLong(), time);
                }
            }
        }

        public void close(Ticket ticket, Member closer) {
            ticket.channel.sendMessage("Creating transcript...").queue();
            ticket.channel.getHistory().retrievePast(100).queue(messages -> {
                String s = TranscriptUtils.threadToTranscript(messages);
                tickets.remove(ticket);
                inboxChannel.sendMessage(new EmbedBuilder().setTitle("Creating Transcript...").build()).addFile(s.getBytes(), "transcript_ticket-"+ticket.idFormatted()+".txt").queue(message -> {
                    message.editMessage(new EmbedBuilder()
                            .setTitle("\uD83C\uDF9F Ticket Closed")
                            .addField("Subject", ticket.subject, false)
                            .addField("Description", ticket.description, false)
                            .addField("Closed By", closer.getEffectiveName(), false)
                            .addField("Transcript", "[Download]("+message.getAttachments().get(0).getUrl()+")\nAlternate view soon?", false)
                            .build()
                    ).queue(message1 -> {
                        ((GuildChannel)ticket.channel).delete().queue();
                    });
                });
                save();
            });
        }

        public void save() {
            JSONObject o = ticketManager.getGuildConfigData(guild.getIdLong());
            o.put("currentId", idCurrent);
            o.put("guildId", guild.getIdLong());
            if(inboxChannel!=null) o.put("inboxChannelId", inboxChannel.getIdLong());
            o.put("tickets", new JSONArray());
            JSONArray ticketArray = o.getJSONArray("tickets");
            for(Ticket ticket : tickets) {
                ticketArray.put(new JSONObject() {{
                    put("ticketId", ticket.id);
                    put("subject", ticket.subject);
                    put("description", ticket.description);
                    put("channelId", ticket.channel.getIdLong());
                    put("splashMessageId", ticket.splashMessage.getIdLong());
                    put("locked", ticket.locked);
                    put("openerId", ticket.opener.getIdLong());
                    if(ticket.inviteMessage != null) put("inviteMessageId", ticket.inviteMessage.getIdLong());
                }});
            }
            ticketManager.data.save();
        }
    }

    public static class Ticket {
        private final Logger logger;
        public int id;
        public String subject = "None";
        public String description = "None";
        public GuildTicketManager guildTicketManager;
        public MessageChannel channel;
        public Message splashMessage;
        public Member opener;
        private boolean locked;
        private Message inviteMessage;
        private boolean sentToStaff;
        private boolean closing;

        public Ticket(GuildTicketManager guildTicketManager, Member member, Logger logger) {
            this.logger = logger;
            opener = member;
            this.guildTicketManager = guildTicketManager;

        }

        private boolean initialized = false;

        public Ticket init() {
            if(initialized) return this;
            initialized = true;
            Guild guild = guildTicketManager.guild;
            Category category = guild.getCategoriesByName("tickets", true).get(0);

            try {
                if (category == null) category = guild.createCategory("tickets").complete(true);
            } catch(Exception e) {
                logger.log(Logger.LogLevel.SEVERE, e.getMessage());
                e.printStackTrace();
                return this;
            }

            final String finalIdStr = idFormatted();
            category.createTextChannel("ticket-"+finalIdStr).queue(textChannel -> {
                channel = textChannel;
                channel.sendMessage("Please wait...").queue(message -> {
                    // TODO: 12/15/2020 Add message reactions for splash messages (close ticket, etc)
                    message.addReaction("\uD83D\uDED1").queue();
                    message.addReaction("\uD83D\uDD10").queue();
                    message.addReaction("\uD83D\uDCF0").queue();
                    message.addReaction("\uD83D\uDCE8").queue();
                    splashMessage = message;
                    refreshSplash();
                    ((GuildChannel)channel).upsertPermissionOverride(opener).grant(Permission.VIEW_CHANNEL).queue();
                });
            });
            guildTicketManager.save();
            return this;
        }

        public Ticket subject(String sub) {
            subject = sub;
            refreshSplash();
            return this;
        }

        public Ticket description(String desc) {
            description = desc;
            refreshSplash();
            return this;
        }

        public Ticket id(int id) {
            this.id = id;
            return this;
        }

        /**
         * Check if the ticket is in lock mode.
         * @return Whether or not the ticket is locked.
         */
        public boolean locked() {
            return locked;
        }

        /**
         * Lock the thread. Essentially just kicks out the original opener.
         * @return The ticket to allow for method chaining.
         */
        public Ticket lock() {
            if(closing) return this;
            channel.sendMessage("Locking thread...").queue();
            ((GuildChannel)channel).upsertPermissionOverride(opener).deny(Permission.VIEW_CHANNEL).queue();
            locked = true;
            guildTicketManager.save();
            return this;
        }

        /**
         * Unlock the thread. Essentially just lets the original opener into the thread.
         * @return The ticket to allow for method chaining.
         */
        public Ticket unlock() {
            if(closing) return this;
            channel.sendMessage("Unlocking thread...").queue();
            ((GuildChannel)channel).upsertPermissionOverride(opener).grant(Permission.VIEW_CHANNEL).queue();
            locked = false;
            guildTicketManager.save();
            return this;
        }

        /**
         * Adds a member to the thread. Used to let staff into the thread and can be used to let others into the thread.
         * @param member The member to opt in.
         * @return The ticket to allow for method chaining.
         */
        public Ticket optIn(Member member) {
            if(((GuildChannel)channel).getPermissionOverride(member) != null && ((GuildChannel)channel).getPermissionOverride(member).getAllowed().contains(Permission.VIEW_CHANNEL)) return this;
            if(opener.getIdLong() == member.getIdLong()) return this;
            channel.sendMessage(new EmbedBuilder() {{
                        if(member.hasPermission(Permission.KICK_MEMBERS)) addField("Staff Member", member.getEffectiveName()+" is a staff member and will assist you!", false);
                        else addField("Non-Staff", member.getEffectiveName()+" is a third party member of the ticket.", false);
                    }}
                    .setTitle(member.getEffectiveName()+" has joined the ticket")
                    .setColor(Color.GREEN)
                    .build()
            ).queue(message -> {
                message.addReaction("\uD83D\uDEAA").queue();
            });
            ((GuildChannel)channel).upsertPermissionOverride(member).grant(Permission.VIEW_CHANNEL).queue();
            guildTicketManager.save();
            return this;
        }

        /**
         * Removes a member from the thread. Used for staff members who no longer wish to be a part of a thread.
         * @param member The member to opt out.
         * @return The ticket to allow for method chaining.
         */
        public Ticket optOut(Member member) {
            if(!((GuildChannel)channel).getPermissionOverride(member).getAllowed().contains(Permission.VIEW_CHANNEL)) return this;
            if(opener.getIdLong() == member.getIdLong()) return this;
            channel.sendMessage(new EmbedBuilder()
                    .setTitle(member.getEffectiveName()+" has left the ticket")
                    .setColor(Color.RED)
                    .build()
            ).queue();
            ((GuildChannel)channel).upsertPermissionOverride(member).deny(Permission.VIEW_CHANNEL).queue();
            guildTicketManager.save();
            return this;
        }

        public void close(Member closer) {
            if(closing) return;
            closing = true;
            channel.sendMessage("Closing ticket in 5 seconds...").queue();
            if(inviteMessage != null && !inviteMessage.getReactions().isEmpty()) {inviteMessage.removeReaction("\uD83D\uDED1").queue();
            inviteMessage.removeReaction("\uD83D\uDEC3").queue();}
            if(inviteMessage != null) inviteMessage.editMessage("This ticket has been closed.").queue();

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    guildTicketManager.close(Ticket.this, closer);
                }
            }, 5000);
        }

        public String idFormatted() {
            String idStr = "";
            for(int i = 0; i < 4-(""+id).length(); i++) idStr += "0";
            return idStr + id;
        }

        public void inviteStaff() {
            if(closing) return;
            if(sentToStaff) return;
            if(subject.equalsIgnoreCase("None") || description.equalsIgnoreCase("None")) {
                channel.sendMessage("❌ Please fill out the subject and description before sending to staff.").queue();
                return;
            }
            sentToStaff = true;
            guildTicketManager.inboxChannel.sendMessage(new EmbedBuilder()
                    .setTitle("\uD83D\uDCE8 Ticket "+idFormatted()+" Needs Staff")
                    .addField("Subject", subject, false)
                    .addField("Description", description, false)
                    .addField("React to help!", "\uD83D\uDEC3 - Join Ticket\n\uD83D\uDED1 - Flag as Spam", false)
                    .build()
            ).queue(message -> {
                inviteMessage = message;
                message.addReaction("\uD83D\uDEC3").queue();
                message.addReaction("\uD83D\uDED1").queue();
                guildTicketManager.save();
            });
            channel.sendMessage("☑ Your ticket has been sent to staff!").queue();
        }

        public Ticket refreshSplash() {
            if(splashMessage==null) return this;
            String idStr = "";
            for(int i = 0; i < 4-(""+id).length();i++) idStr += "0";
            idStr += id;
            final String finalIdStr = idStr;
            splashMessage.editMessage(new EmbedBuilder()
                    .setTitle("Ticket "+finalIdStr)
                    .setDescription("Welcome to the support ticket thread! Set the subject and description with the reactions, and click done when ready!")
                    .addField("Subject", subject, false)
                    .addField("Description", description, false)
                    .setColor(Color.CYAN)
                    .build()
            ).queue(message -> {
                splashMessage = message;
                channel.retrievePinnedMessages().queue(messages -> {
                    if(!messages.contains(message)) channel.pinMessageById(message.getIdLong()).queue();
                });

                guildTicketManager.save();
            });
            return this;
        }

        /**
         * Runs when a message in this ticket is reacted on.
         * @param event The reaction event (context).
         */
        public void reactionAdded(@Nonnull final GuildMessageReactionAddEvent event) {
            if(event.getMessageIdLong() == splashMessage.getIdLong()) {
                event.getReaction().removeReaction(event.getUser()).queue();
                // do reaction stuff here for splash message
                switch(event.getReactionEmote().getEmoji()) {
                    case "\uD83D\uDED1":
                        close(event.getMember());
                        break;
                    case "\uD83D\uDD10":
                        if (event.getMember().hasPermission(Permission.KICK_MEMBERS)) {if (locked()) unlock(); else lock();}
                        else channel.sendMessage("❌ You cannot do that!").queue();
                        break;
                    case "\uD83D\uDCE8":
                        if(event.getMember().getIdLong() == opener.getIdLong()) {
                            inviteStaff();
                        } else {
                            channel.sendMessage("You cannot do that!").queue();
                        }
                        break;
                    case "\uD83D\uDCF0":
                        switch (awaitingMessageMode) {
                            case SUBJECT:
                            case DESCRIPTION:
                            case SUBJECT_OR_DESCRIPTION:
                                awaitingMessageMode = AwaitingMessageMode.NOT;
                                channel.sendMessage("Stopped editing ticket info!").queue();
                                break;
                            case NOT:
                                awaitingMessageMode = AwaitingMessageMode.SUBJECT_OR_DESCRIPTION;
                                channel.sendMessage("What would you like to edit?: `subject`, `description`.").queue();
                                break;
                            default:
                                channel.sendMessage("You are currently editing something else!").queue();
                                break;
                        }
                        break;
                }
            } else if(inviteMessage != null && event.getMessageIdLong() == inviteMessage.getIdLong()) {
                event.getReaction().removeReaction(event.getUser()).queue();
                if(event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
                    switch(event.getReactionEmote().getEmoji()) {
                        case "\uD83D\uDEC3":
                            optIn(event.getMember());
                            break;
                        case "\uD83D\uDED1":
                            inviteMessage.removeReaction("\uD83D\uDED1").queue();
                            inviteMessage.removeReaction("\uD83D\uDEC3").queue();
                            inviteMessage.editMessage("This ticket has been closed.").queue();
                            channel.sendMessage("Your thread has been marked as spam and will be deleted in 10 seconds!").queue();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    close(event.getMember());
                                }
                            }, 5000);
                    }
                }
            } else if(event.getChannel().getIdLong() == channel.getIdLong()) {
                channel.retrieveMessageById(event.getMessageIdLong()).queue(message -> {
                    if(!message.getEmbeds().isEmpty()) {
                        MessageEmbed embed = message.getEmbeds().get(0);
                        if(embed.getTitle().contains(event.getMember().getEffectiveName())) {
                            optOut(event.getMember());
                            message.removeReaction("\uD83D\uDEAA").queue();
                        }
                    }
                });
            }
        }

        private enum AwaitingMessageMode {NOT, SUBJECT_OR_DESCRIPTION, SUBJECT, DESCRIPTION}
        private AwaitingMessageMode awaitingMessageMode = AwaitingMessageMode.NOT;

        public void messageReceived(@Nonnull final GuildMessageReceivedEvent event) {
            String message = event.getMessage().getContentRaw();
            switch (awaitingMessageMode) {
                case SUBJECT_OR_DESCRIPTION:
                    if(message.equalsIgnoreCase("subject")) {
                        awaitingMessageMode = AwaitingMessageMode.SUBJECT;
                        channel.sendMessage("What would you like to set the subject to?").queue();
                    } else if(message.equalsIgnoreCase("description")) {
                        awaitingMessageMode = AwaitingMessageMode.DESCRIPTION;
                        channel.sendMessage("What would you like to set the description to?").queue();
                    }
                    break;
                case SUBJECT:
                    message = message.replaceAll("`", "");
                    awaitingMessageMode = AwaitingMessageMode.NOT;
                    subject(message);
                    channel.sendMessage("Changed the ticket subject to `"+message+"`").queue();
                    break;
                case DESCRIPTION:
                    message = message.replaceAll("`", "");
                    awaitingMessageMode = AwaitingMessageMode.NOT;
                    description(message);
                    channel.sendMessage("Changed the ticket description to `"+message+"`").queue();
                    break;
                default:
                    // nothing (would record sent message, but this can be done by simply reading message history)
                    break;
            }
        }
    }
}
