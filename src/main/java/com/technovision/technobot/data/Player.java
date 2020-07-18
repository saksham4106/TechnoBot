package com.technovision.technobot.data;

public class Player {

    private long id;
    private int level;
    private int xp;

    public Player(long id, int level, int xp) {
        this.id = id;
        this.level = level;
        this.xp = xp;
    }

    public long getId() {
        return id;
    }

    public long getLevel() {
        return level;
    }
    public long getXP() {
        return xp;
    }

}
