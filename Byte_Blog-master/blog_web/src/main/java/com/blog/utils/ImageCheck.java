package com.blog.utils;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class ImageCheck {

    static class ApiResponse {
        public String input;
        public String cleaned;
        public Map<String, Double> probabilities;
        public double inference_time_ms;
    }

    private static final String API_URL = "http://localhost:8000/predict/";

    /**
     * 获取图像分类结果中的最大标签（支持远程 URL）
     *
     * @param imagePath 图像路径（可以是远程 URL）
     * @return 最大概率的标签
     */
    public static String imageTag(String imagePath) throws IOException {
        Gson gson = new Gson();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadRequest = new HttpPost(API_URL);

            // 判断是否为远程 URL
            if (imagePath.startsWith("http")) {
                // 从远程 URL 加载图片数据
                try (InputStream in = new URL(imagePath).openStream()) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    byte[] imageData = out.toByteArray();

                    // 使用 InputStreamEntity 上传图片字节流
                    InputStreamEntity entity = new InputStreamEntity(
                            new java.io.ByteArrayInputStream(imageData),
                            ContentType.IMAGE_JPEG
                    );
                    uploadRequest.setEntity(MultipartEntityBuilder.create()
                            .addBinaryBody("file", imageData, ContentType.IMAGE_JPEG, "image.jpg")
                            .build());
                }
            } else {
                // 如果是本地文件路径
                File imageFile = new File(imagePath);
                uploadRequest.setEntity(MultipartEntityBuilder.create()
                        .addBinaryBody("file", imageFile, ContentType.IMAGE_JPEG, "image.jpg")
                        .build());
            }

            return httpClient.execute(uploadRequest, response -> {
                int statusCode = response.getCode();
                if (statusCode == 200) {
                    String body = EntityUtils.toString(response.getEntity());
                    JsonObject jsonObject = gson.fromJson(body, JsonObject.class);

                    // 提取 predictions 数组
                    JsonArray predictions = jsonObject.getAsJsonArray("predictions");

                    String maxKey = null;
                    double maxValue = Double.MIN_VALUE;

                    for (int i = 0; i < predictions.size(); i++) {
                        JsonObject pred = predictions.get(i).getAsJsonObject();
                        String tag = pred.get("tag").getAsString();
                        double prob = pred.get("probability").getAsDouble();

                        if (prob > maxValue) {
                            maxValue = prob;
                            maxKey = tag;
                        }
                    }

                    System.out.println("maxKey:" + maxKey);
                    System.out.println("Inference Time: " + jsonObject.get("inference_time").getAsDouble() + " seconds");
                    return maxKey;
                } else {
                    System.err.println("请求失败，状态码：" + statusCode);
                    return "请求失败";
                }
            });
        }
    }

    /**
     * 测试入口
     */
//    public static void main(String[] args) throws IOException {
//        String result = imageTag("https://sentiblog-repo.oss-cn-wuhan-lr.aliyuncs.com/34717ac0-1395-4f32-93fd-80ed9c36c4ae.jpg");
//        System.out.println("预测结果：" + result);
//    }
}
