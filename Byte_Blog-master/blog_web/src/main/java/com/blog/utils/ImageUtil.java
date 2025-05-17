package com.blog.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class ImageUtil {

    private static int lastX;
    private static int lastY;

    public static BufferedImage[] cutByTemplate(BufferedImage bgImage, BufferedImage templateImage) {
        int width = templateImage.getWidth();
        int height = templateImage.getHeight();

        int maxX = bgImage.getWidth() - width;
        int maxY = bgImage.getHeight() - height;

        Random random = new Random();
        lastX = 30 + random.nextInt(Math.max(1, maxX - 60));
        lastY = 30 + random.nextInt(Math.max(1, maxY - 60));

        BufferedImage block = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage shadowBg = deepCopy(bgImage);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int templatePixel = templateImage.getRGB(x, y);
                int alpha = (templatePixel >> 24) & 0xff;

                if (alpha > 0) {
                    int rgb = bgImage.getRGB(lastX + x, lastY + y);
                    block.setRGB(x, y, rgb);

                    // 加阴影（变暗）
                    int r = (rgb >> 16) & 0xff;
                    int g = (rgb >> 8) & 0xff;
                    int b = rgb & 0xff;
                    int dark = (alpha << 24) | ((r / 3) << 16) | ((g / 3) << 8) | (b / 3);
                    shadowBg.setRGB(lastX + x, lastY + y, dark);
                }
            }
        }

        return new BufferedImage[]{shadowBg, block};
    }

    public static int getLastX() {
        return lastX;
    }

    public static int getLastY() {
        return lastY;
    }

    public static String toBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("图片转Base64失败", e);
        }
    }

    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
