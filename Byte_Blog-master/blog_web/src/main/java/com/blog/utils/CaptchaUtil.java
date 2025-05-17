package com.blog.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

public class CaptchaUtil {

    public static String generateCode(){
        // 生成验证码内容
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder codeBuilder = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            char ch = chars.charAt(random.nextInt(chars.length()));
            codeBuilder.append(ch);
        }
        String code = codeBuilder.toString();
        return code;
    }

    public static String createBase64Image(String code) throws IOException {
        int width = 120;
        int height = 40;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 填充背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Arial", Font.BOLD, 28));

        Random random = new Random();
        g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
        g.drawString(code, 20 , 30);
        g.dispose();

        // 图片转为 Base64

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", os);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
