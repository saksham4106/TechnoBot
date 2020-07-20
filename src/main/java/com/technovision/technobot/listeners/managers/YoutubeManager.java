package com.technovision.technobot.listeners.managers;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.util.Track;
import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class YoutubeManager {

    private final String key;
    private final YouTube youtube;
    private final YouTube.Search.List search;

    public YoutubeManager() {
        try {
            key = TechnoBot.getInstance().getBotConfig().getJson().getString("youtube-api-key");
            youtube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), new JacksonFactory(), request -> {}).setApplicationName("technobot-discord-bot").build();
            List<String> properties = new ArrayList<>();
            properties.add("id");
            properties.add("snippet");
            search = youtube.search().list(properties);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public YouTube getYoutube() { return youtube; }

    public Track search(String keywords, User author) {
        try {
            // Call the API and get first result.
            SearchListResponse searchResponse = search
                    .setMaxResults(25L)
                    .setKey(key)
                    .setQ(keywords)
                    .execute();
            if (searchResponse.getItems().size() == 0) { return null; }

            // Loop through results until you find a video
            for (SearchResult result : searchResponse.getItems()) {
                ResourceId rId = result.getId();
                if (rId.getKind().equals("youtube#video")) {
                    String url = "https://www.youtube.com/watch?v=" + rId.getVideoId();
                    String thumb = String.format("https://img.youtube.com/vi/%s/0.jpg", rId.getVideoId());
                    Track track = new Track(url, thumb, author);
                    return track;
                }
            }
        } catch (IOException ignored) { }
        return null;
    }
}
