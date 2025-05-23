package com.blog.utils;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * HTTP工具类 - 使用HttpClient 5.x
 */
public class HttpUtil {
    
    /**
     * 发送GET请求
     */
    public static String sendGet(String url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            
            return httpClient.execute(httpGet, response -> {
                int statusCode = response.getCode();
                if (statusCode == 200) {
                    return new Scanner(response.getEntity().getContent())
                            .useDelimiter("\\A").next();
                } else {
                    throw new IOException("HTTP GET请求失败，状态码：" + statusCode);
                }
            });
        }
    }
    
    /**
     * 发送POST请求
     */
    public static String sendPost(String url, String jsonData) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(jsonData, ContentType.APPLICATION_JSON));
            
            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                if (statusCode == 200) {
                    return new Scanner(response.getEntity().getContent())
                            .useDelimiter("\\A").next();
                } else {
                    throw new IOException("HTTP POST请求失败，状态码：" + statusCode);
                }
            });
        }
    }
    
    /**
     * 向后兼容的doPost方法 - 支持表单数据
     */
    public static CloseableHttpResponse doPost(String host, String path, String method, 
                                             Map<String, String> headers, 
                                             Map<Object, Object> querys, 
                                             Map<String, String> bodys) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(host + path);
        
        // 设置请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        
        // 设置表单参数
        if (bodys != null && !bodys.isEmpty()) {
            List<NameValuePair> params = new ArrayList<>();
            for (Map.Entry<String, String> entry : bodys.entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(params));
        }
        
        return httpClient.execute(httpPost);
    }
}
