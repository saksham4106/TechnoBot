package com.technovision.technobot.images;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ImageProcessor {

    public static BufferedImage getAvatar(String avatarLink, double scaleX, double scaleY) throws IOException  {
        URL url = new URL(Objects.requireNonNull(avatarLink));
        BufferedImage addon = ImageIO.read(url);
        int w = addon.getWidth();
        int h = addon.getHeight();
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(scaleX, scaleY);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return scaleOp.filter(addon, after);
    }

    public static File saveImage(String path, BufferedImage image) throws IOException {
        ImageIO.write(image, "png", new File(path));
        return new File(path);
    }
}
