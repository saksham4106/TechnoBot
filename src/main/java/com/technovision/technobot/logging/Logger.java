package com.technovision.technobot.logging;


import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.technovision.technobot.TechnoBot;
import net.dv8tion.jda.api.EmbedBuilder;

public class Logger {
    private WebhookClient client;

    public Logger() {
        client = new WebhookClientBuilder(TechnoBot.config.getJson().getString("logs-webhook")).build();
        client.send(new WebhookEmbedBuilder().setTitle(new WebhookEmbed.EmbedTitle("PAYLOAD TEST", "https://google.com")).addField(new WebhookEmbed.EmbedField(false, "(link 1)[https://youtube.com/c/TechnoVisionTV]", "hi")).build());
    }

}
