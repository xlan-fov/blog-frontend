package com.blog.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SMSUtil {
    public static boolean sendSms(String phone, String code) {
        String host = "https://send.market.alicloudapi.com";
        String path = "/sms/send";
        String method = "POST";
        String appcode = "60b994a1492d49caa87c94a0be317d7b";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "APPCODE " + appcode);
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        Map<String, String> bodys = new HashMap<>();
        bodys.put("content", "code:" + code);
        bodys.put("templateid", "lxym_20111_sdgsfhwqgvyh");
        bodys.put("mobile", phone);

        try {
            HttpResponse response = HttpUtil.doPost(host, path, method, headers, new HashMap<>(), bodys);
            log.info(EntityUtils.toString(response.getEntity()));
            log.info("SMS发送成功");
            return response.getStatusLine().getStatusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("SMS发送失败");
            return false;
        }
    }
}