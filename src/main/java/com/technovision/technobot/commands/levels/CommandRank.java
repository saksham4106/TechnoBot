package com.technovision.technobot.commands.levels;

import com.google.common.collect.Sets;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.images.ImageProcessor;
import com.technovision.technobot.listeners.managers.LevelManager;
import com.technovision.technobot.logging.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Set;

public class CommandRank extends Command {

    private final DecimalFormat formatter;

    public CommandRank() {
        super("rank", "Displays your levels and server rank", "{prefix}rank", Command.Category.LEVELS);
        formatter = new DecimalFormat("#,###");
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {

        User user = event.getAuthor();
        long id = user.getIdLong();
        if (args.length > 0) {
            if (args[0].startsWith("<@!") && args[0].endsWith(">")) {
                id = Long.parseLong(args[0].substring(3, args[0].length()-1));
                user = event.getJDA().retrieveUserById(id).complete();
            } else {
                return true;
            }
        }

        for(Object o : LevelManager.getInstance().levelSave.getJson().getJSONArray("users")) {
            if(((JSONObject)o).getLong("id") == id) {
                JSONObject player = (JSONObject) o;
                float percent = ((float) (player.getInt("xp") * 100) / (float) (LevelManager.getInstance().getMaxXP(player.getInt("level"))));
                String percentStr = String.valueOf((int) percent);
                try {
                    //Get Graphics
                    BufferedImage base = ImageIO.read(new File("data/images/rankCardBase.png"));
                    BufferedImage outline = ImageIO.read(new File("data/images/rankCardOutline.png"));
                    Graphics2D g = (Graphics2D) base.getGraphics();
                    g.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

                    //Add Background
                    BufferedImage background;
                    if (player.getString("background").isEmpty()) {
                        background = ImageIO.read(new File("data/images/rankCardBackground.png"));
                    } else {
                        background = ImageIO.read(new URL(player.getString("background")));
                    }
                    BufferedImage rectBuffer = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = rectBuffer.createGraphics();
                    g2.setClip(new Rectangle2D.Float(0, 0, base.getWidth(), base.getHeight()));
                    int x = base.getWidth() - background.getWidth();
                    int y = base.getHeight() - background.getHeight();
                    if (background.getWidth() >= 934 && background.getHeight() >= 282) {
                        g2.drawImage(background, x / 2, y / 2, null);
                    } else {
                        g2.drawImage(background, 0, 0, base.getWidth(), base.getHeight(), null);
                    }
                    g2.dispose();
                    g.drawImage(rectBuffer, 0, 0, base.getWidth(), base.getHeight(), null);

                    //Add Outline
                    float opacity = player.getFloat("opacity");
                    AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
                    g.setComposite(ac);
                    g.drawImage(outline, 0, 0, null);
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

                    //Text
                    g.setStroke(new BasicStroke(3));
                    g.setColor(Color.decode(player.getString("accent")));
                    g.setFont(new Font("Helvetica", Font.PLAIN, 52));
                    g.drawLine(300, 140, 870, 140);
                    g.drawString(user.getName(), 300, 110);
                    g.setFont(new Font("Helvetica", Font.PLAIN, 35));

                    int rankNum = LevelManager.getInstance().userList.indexOf(user) + 1;
                    int xModifier = 0;
                    if (rankNum >= 10) { xModifier += 15; }
                    if (rankNum >= 100) { xModifier += 15; }
                    if (rankNum >= 1000) { xModifier += 15; }
                    if (rankNum >= 10000) { xModifier += 15; }
                    g.drawString("Rank #" + (LevelManager.getInstance().userList.indexOf(user) + 1), 740 - xModifier, 110);

                    g.drawString("Level " + player.getInt("level"), 300, 180);
                    g.setFont(new Font("Helvetica", Font.PLAIN, 25));
                    String xp = format(player.getInt("xp"));
                    String maxXP = format(LevelManager.getInstance().getMaxXP(player.getInt("level")));
                    xModifier = 0;
                    if (xp.length() > 2) { xModifier += 10; }
                    if (xp.length() > 3 || maxXP.length() > 3) { xModifier += 10; }
                    if (xp.length() > 4 || maxXP.length() > 4) { xModifier += 10; }
                    g.drawString(xp + " / " + maxXP, 775 - xModifier, 180);

                    //XP Bar
                    g.drawRoundRect(300, 200, 570, 40, 20, 20);
                    g.setColor(Color.decode("#101636"));
                    g.fillRoundRect(300, 200, 570, 40, 20, 20);
                    g.setColor(Color.decode(player.getString("color")));
                    g.fillRoundRect(300, 200, (int) (570 * (percent * 0.01)), 40, 20, 20);
                    g.setColor(Color.decode(player.getString("accent")));
                    g.setFont(new Font("Helvetica", Font.PLAIN, 30));
                    g.drawString(percentStr + "%", 560, 230);

                    //Add Avatar
                    BufferedImage avatar;
                    avatar = ImageProcessor.getAvatar(user);
                    g.setStroke(new BasicStroke(4));
                    int width = avatar.getWidth();
                    BufferedImage circleBuffer = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g3 = circleBuffer.createGraphics();
                    g3.setClip(new Ellipse2D.Float(0, 0, width, width));
                    g3.drawImage(avatar, 0, 0, width, width, null);
                    g3.dispose();
                    g.drawImage(circleBuffer, 55, 38, null);
                    g.setColor(Color.decode(player.getString("color")));
                    g.drawOval(55, 38, width, width);
                    g.dispose();

                    //Save File
                    File rankCard = ImageProcessor.saveImage("data/images/rankCard.png", base);
                    event.getChannel().sendFile(rankCard, "rankCard.png").queue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return true;
    }

    private String format(int num) {
        if (num >= 1000) {
            String[] numArray = String.valueOf(num).split("");
            String formatted = numArray[0];
            if (num >= 10000) {
                if (Integer.parseInt(numArray[1]) != 0) {
                    formatted += numArray[1];
                }
                if (Integer.parseInt(numArray[2]) != 0) {
                    formatted += "." + numArray[1];
                }
            } else {
                if (Integer.parseInt(numArray[1]) != 0) {
                    formatted += "." + numArray[1];
                }
            }
            formatted += "k";
            return formatted;
        }
        return String.valueOf(num);
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("lvl", "level");
    }
}
