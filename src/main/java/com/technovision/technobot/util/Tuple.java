package com.technovision.technobot.util;

public class Tuple<T1, T2> {
    public final T1 key;
    public final T2 value;

    public Tuple(T1 key, T2 value) {
        this.key = key;
        this.value = value;
    }
}
