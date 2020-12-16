package com.technovision.technobot.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class TranscriptUtils {
    public static String threadToTranscript(List<Message> messages) {
        String s = "";
        for(int i = messages.size() - 1; i >= 0; i--) {
            if(messages.get(i).getContentRaw().length() > 0) s += messageToTranscript(messages.get(i));
        }
        return s;
    }

    public static String messageToTranscript(Message message) {
        String s = message.getMember().getEffectiveName() + " - " + message.getTimeCreated().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.MEDIUM).withZone(ZoneId.of("UTC"))) + "\n" +
                message.getContentRaw() + "\n";
        for(MessageEmbed embed : message.getEmbeds()) {
            s += embedToTranscript(embed);
        }
        return s + "\n\n";
    }

    public static String embedToTranscript(MessageEmbed embed) {
        String s =  "<embed>\n" +
                "    <title>"+embed.getTitle()+"</title>\n" +
                "    <description>"+embed.getDescription()+"</description>\n" +
                "    <author>"+embed.getAuthor()+"</author>\n" +
                "    <footer>"+embed.getFooter()+"</footer>\n" +
                "    <thumbnail>"+embed.getThumbnail()+"</thumbnail>\n" +
                "    <image>"+embed.getImage()+"</image>\n" +
                "    <color>"+embed.getColorRaw()+"</color>\n" +
                "    <fields>\n";
        for(MessageEmbed.Field field : embed.getFields()) {
            s += "        <field>\n" +
                    "            <name>"+field.getName()+"</name>\n" +
                    "            <value>"+field.getValue()+"</value>\n" +
                    "        </field>\n";
        }
        s += "    </fields>\n" +
                "</embed>";
        return s;
    }
}
