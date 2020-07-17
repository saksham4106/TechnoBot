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
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.*;

public class MusicManager extends ListenerAdapter {
    public final Map<Long, MusicSendHandler> handlers = new HashMap<Long, MusicSendHandler>();
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    private static MusicManager musicManager;
    public static MusicManager getInstance() {
        return musicManager;
    }

    public MusicManager() {
        musicManager = this;
        AudioSourceManagers.registerRemoteSources(playerManager);
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

    private void addTrack(String name, Guild guild, TrackScheduler scheduler) {
        playerManager.loadItem(name, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
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
                MusicManager.getInstance().addTrack(track.getInfo().uri, guild, this);
            }
            else if(endReason.mayStartNext&&trackQueue.size()>0) player.playTrack(trackQueue.get(0));
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

        public void toggleLoop(MessageChannel channel) {
            if(!loop) {
                loop = true;
                channel.sendMessage(":repeat_one: Loop Enabled!").queue();
            } else {
                loop = false;
                channel.sendMessage(":x: Loop Disabled!").queue();
            }
        }

        private void clearQueue() {
            trackQueue.clear();
            player.stopTrack();
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


    }
}
