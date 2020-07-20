package com.technovision.technobot.util;

import net.dv8tion.jda.api.entities.User;

public class Track {

    private final String url;
    private final String thumbnail;
    private final User author;

    public Track(String url, String thumbnail, User author) {
        this.url = url;
        this.thumbnail = thumbnail;
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public User getAuthor() {
        return author;
    }
}
