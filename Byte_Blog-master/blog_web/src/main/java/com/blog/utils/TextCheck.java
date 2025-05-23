package com.blog.utils;

/**
 * @Author: kai.hu
 * @Date: 2025-05-19-20:07
 * @Description:
 */

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TextCheck {
    // 自定义响应类
    static class ApiResponse {
        public String input;
        public String cleaned;
        public Map<String, Double> probabilities;
        public double inference_time_ms;

        @Override
        public String toString() {
            return "ApiResponse{" +
                    "input='" + input + '\'' +
                    ", cleaned='" + cleaned + '\'' +
                    ", probabilities=" + probabilities +
                    ", inference_time_ms=" + inference_time_ms +
                    '}';
        }
    }
    /*
     * @Author: kai.hu
     * @Date: 2025-5-20
     * @Description: 测试接口
     */
//    public static void main(String[] args) throws Exception {
//        String text = "到底谁是谁的爹和娘啊？！ //@古长宏:爱疯女，逼疯爹和娘。[泪]";
//        String res = textTag(text);
//        System.out.println(res);
//    }

    public static String textTag(String text) throws Exception {
        String apiUrl = "http://localhost:8000/predict";

        // 使用 Map 替代不可实例化的 Request 类
        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("text", text);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(requestPayload);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(apiUrl);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            return httpClient.execute(post, response -> {
                int statusCode = response.getCode();
                if (statusCode == 200) {
                    String body = new Scanner(response.getEntity().getContent())
                            .useDelimiter("\\A").next();
                    ApiResponse result = mapper.readValue(body, ApiResponse.class);
//                    System.out.println("Input: " + result.input); // 输出
//                    System.out.println("Cleaned: " + result.cleaned); // 清洗后的输出
                    // 返回的标签
                    String maxKey = null;
                    double maxValue = Double.MIN_VALUE;
                    for (Map.Entry<String, Double> entry : result.probabilities.entrySet()) {
                        if (entry.getValue() > maxValue) {
                            maxValue = entry.getValue();
                            maxKey = entry.getKey();
                        }
                    }
                    System.out.println("maxKey:" + maxKey);
                    System.out.println("Inference Time: " + result.inference_time_ms + " ms");
                    return maxKey;
                }else {
                    System.err.println("请求失败，状态码：" + statusCode);
                    return "请求失败";
                }
            });
        }
    }
}

