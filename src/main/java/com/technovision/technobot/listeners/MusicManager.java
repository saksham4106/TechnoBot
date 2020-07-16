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
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicManager extends ListenerAdapter {
    public final Map<Long, MusicSendHandler> handlers = new HashMap<Long, MusicSendHandler>();
    private AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    private static MusicManager musicManager;
    public static MusicManager getInstance() {
        return musicManager;
    }

    public MusicManager() {
        musicManager = this;
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    public void joinVoiceChannel(Guild guild, VoiceChannel channel) {
        AudioManager manager = guild.getAudioManager();

        if(manager.getSendingHandler()==null) {
            handlers.putIfAbsent(guild.getIdLong(), new MusicSendHandler(playerManager.createPlayer()));
            manager.setSendingHandler(handlers.get(guild.getIdLong()));
        }

        manager.openAudioConnection(channel);
    }

    public void addTrack(String name, MessageChannel channel, Guild guild) {
        playerManager.loadItem(name, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                channel.sendMessage("Loading track `"+audioTrack.getIdentifier()+"`").queue();
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
                channel.sendMessage("Could not find track!").queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                channel.sendMessage("An error occured!").queue();
                TechnoBot.getInstance().getLogger().log(Logger.LogLevel.SEVERE, e.getMessage());
            }
        });
    }

    private static class MusicSendHandler implements AudioSendHandler {
        private final AudioPlayer player;
        private AudioFrame lastFrame;
        public final TrackScheduler trackScheduler;

        public MusicSendHandler(AudioPlayer player) {
            this.player = player;
            trackScheduler = new TrackScheduler(player);
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

        public TrackScheduler(AudioPlayer player) {
            this.player = player;
        }

        public void queue(AudioTrack track) {
            trackQueue.add(track);
            if(player.getPlayingTrack()==null) player.playTrack(trackQueue.get(0));
        }

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            trackQueue.remove(track);
            if(endReason.mayStartNext&&trackQueue.size()>0) player.playTrack(trackQueue.get(0));
        }
    }
}
