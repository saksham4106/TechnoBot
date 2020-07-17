package com.technovision.technobot.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.logging.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.datatransfer.SystemFlavorMap;
import java.nio.ByteBuffer;
import java.util.*;

public class MusicManager extends ListenerAdapter {
    public final Map<User, Message> djMessages = new HashMap<>();
    public final Map<Long, MusicSendHandler> handlers = new HashMap<Long, MusicSendHandler>();
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    private static MusicManager musicManager;
    public static MusicManager getInstance() {
        return musicManager;
    }

    public MusicManager() {
        musicManager = this;
        AudioSourceManagers.registerRemoteSources(playerManager);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handlers.forEach((gId,handler) -> {
                    if(handler.trackScheduler.getQueueCopy().size()==0) return;
                    djMessages.forEach((user, message) -> {message.editMessage(assembleEmbed(message.getEmbeds().get(0),handlers.get(gId).trackScheduler).build()).queue();});
                });

            }
        },1000L,5000L);
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if(!djMessages.containsKey(event.getUser())) return;
        if(event.getMessageIdLong()==djMessages.get(event.getUser()).getIdLong()) {
            event.getReaction().removeReaction(event.getUser()).queue();
            if(event.getReaction().getReactionEmote().getEmoji().equalsIgnoreCase("⏯")) {
                handlers.get(event.getGuild().getIdLong()).trackScheduler.setPaused(!handlers.get(event.getGuild().getIdLong()).trackScheduler.isPaused());
                MessageEmbed embed = djMessages.get(event.getUser()).getEmbeds().get(0);
                TrackScheduler sch = handlers.get(event.getGuild().getIdLong()).trackScheduler;
                EmbedBuilder builder = assembleEmbed(embed, sch);
                djMessages.get(event.getUser()).editMessage(builder.build()).queue();
            } else if(event.getReaction().getReactionEmote().getEmoji().equalsIgnoreCase("\uD83D\uDD02")) {
                handlers.get(event.getGuild().getIdLong()).trackScheduler.toggleLoop(null);
                MessageEmbed embed = djMessages.get(event.getUser()).getEmbeds().get(0);
                TrackScheduler sch = handlers.get(event.getGuild().getIdLong()).trackScheduler;
                EmbedBuilder builder = assembleEmbed(embed, sch);
                djMessages.get(event.getUser()).editMessage(builder.build()).queue();
            } else if(event.getReaction().getReactionEmote().getEmoji().equalsIgnoreCase("⏭")) {
                handlers.get(event.getGuild().getIdLong()).trackScheduler.skip();
            } else if(event.getReaction().getReactionEmote().getEmoji().equalsIgnoreCase("\uD83D\uDD01")) {
                handlers.get(event.getGuild().getIdLong()).trackScheduler.toggleLoopQueue(null);
            } else if(event.getReaction().getReactionEmote().getEmoji().equalsIgnoreCase("\uD83D\uDD00")) {
                handlers.get(event.getGuild().getIdLong()).trackScheduler.shuffle();
            }
        }



    }

    public EmbedBuilder assembleEmbed(MessageEmbed embedOriginal, TrackScheduler sch) {
        String[] posString = new String[] {"⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯","⎯",};
        try {
            posString[(int) Math.floor((float) sch.trackQueue.get(0).getPosition() / (float) sch.trackQueue.get(0).getDuration() * 30F)] = "◉";
        } catch(Exception e) {posString[29] = "◉";}

        long msPos = sch.trackQueue.get(0).getPosition();
        long minPos = msPos/60000;
        msPos = msPos%60000;
        int secPos = (int) Math.floor((float)msPos/1000f);

        long msDur = sch.trackQueue.get(0).getDuration();
        long minDur = msDur/60000;
        msDur = msDur%60000;
        int secDur = (int) Math.floor((float)msDur/1000f);

        return new EmbedBuilder()
                .setTitle(embedOriginal.getTitle())
                .setDescription(embedOriginal.getDescription())
                .setColor(embedOriginal.getColor())
                .addField("Player Status", ((sch.isPaused())?"Paused":((sch.loop)?"Looping 1 song.":((sch.loopQueue)?"Looping Queue.":"Playing Queue"))), false)
                .addField("Current Song", "["+sch.trackQueue.get(0).getInfo().title+"]("+sch.trackQueue.get(0).getInfo().uri+")", false)
                .addField("Loop Mode", ((sch.loop)?"Loop Song":((sch.loopQueue)?"Loop Playlist":"Loop Off")), true)
                .addField("Position", minPos+":"+((secPos<10)?"0"+secPos:secPos)+" / "+minDur+":"+((secDur<10)?"0"+secDur:secDur), false)
                .addField("Progress", String.join("", posString), false);
    }

    public void joinVoiceChannel(Guild guild, VoiceChannel channel, MessageChannel mChannel) {
        AudioManager manager = guild.getAudioManager();

        if(manager.getSendingHandler()==null) {
            handlers.putIfAbsent(guild.getIdLong(), new MusicSendHandler(playerManager.createPlayer(), guild));
            manager.setSendingHandler(handlers.get(guild.getIdLong()));
        }

        handlers.get(guild.getIdLong()).trackScheduler.logChannel = mChannel;
        manager.openAudioConnection(channel);
    }

    public void leaveVoiceChannel(Guild guild, VoiceChannel channel) {
        guild.getAudioManager().closeAudioConnection();
        handlers.get(guild.getIdLong()).trackScheduler.clearQueue();
    }

    public void addTrack(String name, MessageChannel channel, Guild guild) {
        playerManager.loadItem(name, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                channel.sendMessage("Loading song `"+audioTrack.getInfo().title+"`").queue();
                handlers.get(guild.getIdLong()).trackScheduler.queue(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                channel.sendMessage("Loading playlist `"+audioPlaylist.getName()+"`").queue();
                for(AudioTrack track : audioPlaylist.getTracks()) {
                    handlers.get(guild.getIdLong()).trackScheduler.queue(track);
                }
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Could not find song!").queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                channel.sendMessage("An error occurred!").queue();
                TechnoBot.getInstance().getLogger().log(Logger.LogLevel.SEVERE, e.getMessage());
            }
        });
    }

    private void addTrack(String name, Guild guild, TrackScheduler scheduler, boolean queueSong) {
        playerManager.loadItem(name, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                if(queueSong) {
                    scheduler.queue(audioTrack);
                }
                scheduler.trackQueue.add(0, audioTrack);
                scheduler.player.playTrack(scheduler.trackQueue.get(0));
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                for(AudioTrack track : audioPlaylist.getTracks()) {
                    handlers.get(guild.getIdLong()).trackScheduler.queue(track);
                }
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });
    }

    public static class MusicSendHandler implements AudioSendHandler {
        private final AudioPlayer player;
        private AudioFrame lastFrame;
        public final TrackScheduler trackScheduler;

        public MusicSendHandler(AudioPlayer player, Guild guild) {
            this.player = player;
            trackScheduler = new TrackScheduler(player, guild);
            player.addListener(trackScheduler);
        }

        @Override
        public boolean canProvide() {
            lastFrame = player.provide();
            return lastFrame != null;
        }

        @Nullable
        @Override
        public ByteBuffer provide20MsAudio() {
            return ByteBuffer.wrap(lastFrame.getData());
        }

        @Override
        public boolean isOpus() {
            return true;
        }
    }

    public static class TrackScheduler extends AudioEventAdapter {
        private final List<AudioTrack> trackQueue = new ArrayList<>();
        private final AudioPlayer player;
        private boolean loop = false;
        private boolean loopQueue = false;
        private final Guild guild;
        private MessageChannel logChannel;

        public List<AudioTrack> getQueueCopy() {
            return new ArrayList<>(trackQueue);
        }

        public TrackScheduler(AudioPlayer player, Guild guild) {
            this.player = player;
            this.guild = guild;
        }

        public void queue(AudioTrack track) {
            trackQueue.add(track);
            if(player.getPlayingTrack()==null) player.playTrack(trackQueue.get(0));
        }

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            trackQueue.remove(track);
            if(loop&&endReason.mayStartNext) {
                MusicManager.getInstance().addTrack(track.getInfo().uri, guild, this, false);
            } else if(loopQueue&&endReason.mayStartNext) {
                MusicManager.getInstance().addTrack(track.getInfo().uri, guild, this, true);
            } else if(endReason.mayStartNext&&trackQueue.size()>0) player.playTrack(trackQueue.get(0));
        }

        public void skip() {
            player.getPlayingTrack().setPosition(player.getPlayingTrack().getDuration());
        }

        public void skipTo(int pos) {
            for(int i = 1; i < pos; i++) {
                trackQueue.remove(1);
            }
            skip();
        }

        public void toggleLoop(@Nullable MessageChannel channel) {
            if(loopQueue&&!loop) toggleLoopQueue(channel);
            if(!loop) {
                loop = true;
                if(channel!=null)channel.sendMessage(":repeat_one: Loop Enabled!").queue();
            } else {
                loop = false;
                if(channel!=null)channel.sendMessage(":x: Loop Disabled!").queue();
            }
        }

        public void toggleLoopQueue(@Nullable MessageChannel channel) {
            if(loop&&!loopQueue) toggleLoop(channel);
            if(!loopQueue) {
                loopQueue = true;
                if(channel!=null)channel.sendMessage(":repeat: Loop Queue Enabled!").queue();
            } else {
                loopQueue = false;
                if(channel!=null)channel.sendMessage(":x: Loop Queue Disabled!").queue();
            }

        }

        private void clearQueue() {
            trackQueue.clear();
            player.stopTrack();
        }

        public void setVolume(int volume) {

            player.setVolume(volume);
        }

        public void setPaused(boolean paused) {
            player.setPaused(paused);
        }

        public boolean isPaused() {
            return player.isPaused();
        }

        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            long msPos = track.getInfo().length;
            long minPos = msPos/60000;
            msPos = msPos%60000;
            int secPos = (int) Math.floor((float)msPos/1000f);

            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("Song Started :musical_note:")
                    .setDescription("["+track.getInfo().title+"]("+track.getInfo().uri+")")
                    .addField("Length", minPos+":"+((secPos<10)?"0"+secPos:secPos), true);
            builder.addField("Up Next", (trackQueue.size()>1)?("["+trackQueue.get(1).getInfo().title+"]("+trackQueue.get(1).getInfo().uri+")"):"Nothing", true);

            logChannel.sendMessage(builder.build()).queue();
        }


        public void shuffle() {
            Collections.shuffle(trackQueue.subList(1,trackQueue.size()));
        }
    }
}
