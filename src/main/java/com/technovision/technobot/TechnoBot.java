package com.technovision.technobot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.Scanner;

public class TechnoBot {

    public static JDA jda;

    public static void main(String[] args) {
        try {
            jda = JDABuilder.createDefault(getToken()).build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    private static String getToken() {
        return null;
    }
}
