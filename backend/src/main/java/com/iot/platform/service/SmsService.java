package com.iot.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 短信服务（短信宝）
 */
@Slf4j
@Service
public class SmsService {
    
    @Value("${smsbao.username:waynewyq}")
    private String username;
    
    @Value("${smsbao.password:500a7f062bd94dc1881742c0d1b7850e}")
    private String password;
    
    @Value("${smsbao.api-url:https://api.smsbao.com/sms}")
    private String apiUrl;
    
    /**
     * 发送短信
     * 
     * @param mobile 手机号
     * @param content 短信内容
     * @return 发送是否成功
     */
    public boolean sendSms(String mobile, String content) {
        try {
            // 构造请求URL
            String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8);
            String requestUrl = String.format("%s?u=%s&p=%s&m=%s&c=%s", 
                    apiUrl, username, password, mobile, encodedContent);
            
            // 发送HTTP请求
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            // 读取响应
            int responseCode = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();
            
            // 判断是否发送成功（短信宝返回0表示成功）
            String responseBody = response.toString();
            if (responseCode == 200 && responseBody.startsWith("0")) {
                log.info("短信发送成功：手机号={}, 内容={}", mobile, content);
                return true;
            } else {
                log.error("短信发送失败：手机号={}, 响应码={}, 响应内容={}", mobile, responseCode, responseBody);
                return false;
            }
            
        } catch (Exception e) {
            log.error("短信发送失败：手机号={}, 错误信息={}", mobile, e.getMessage(), e);
            return false;
        }
    }
}
