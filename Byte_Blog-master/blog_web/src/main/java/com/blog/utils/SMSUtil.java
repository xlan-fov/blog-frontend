package com.blog.utils;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SMSUtil {
    public static boolean sendSms(String phone, String code) {
        String host = "https://send.market.alicloudapi.com";
        String path = "/sms/send";
        String appcode = "60b994a1492d49caa87c94a0be317d7b";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(host + path);

            // 设置请求头
            httpPost.setHeader("Authorization", "APPCODE " + appcode);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            // 设置请求参数
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("content", "code:" + code));
            params.add(new BasicNameValuePair("templateid", "lxym_20111_sdgsfhwqgvyh"));
            params.add(new BasicNameValuePair("mobile", phone));

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                if (statusCode == 200) {
                    // 可以在这里处理响应内容
                    String responseBody = new Scanner(response.getEntity().getContent())
                            .useDelimiter("\\A").next();
                    System.out.println("SMS发送响应: " + responseBody);
                    return true;
                } else {
                    System.err.println("SMS发送失败，状态码：" + statusCode);
                    return false;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
