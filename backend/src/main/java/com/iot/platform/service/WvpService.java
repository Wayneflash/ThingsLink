package com.iot.platform.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * WVP服务封装
 * 用于调用WVP平台的API接口
 * 
 * @author IoT Platform
 * @since 2026-01-14
 */
@Slf4j
@Service
public class WvpService {
    
    @Autowired
    private WvpConfigService wvpConfigService;
    
    /**
     * WVP访问Token（缓存）
     */
    private String accessToken = null;
    
    /**
     * Token过期时间
     */
    private long tokenExpireTime = 0;
    
    /**
     * HTTP客户端（支持HTTPS自签名证书）
     */
    private final OkHttpClient httpClient;
    
    public WvpService() {
        try {
            // 创建信任所有证书的TrustManager
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                new javax.net.ssl.X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }
                    
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }
                    
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };
            
            // 创建SSLContext
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // 创建OkHttpClient
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .sslSocketFactory(sslContext.getSocketFactory(), (javax.net.ssl.X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true) // 信任所有主机名
                    .build();
            
            log.info("WvpService初始化成功，已配置HTTPS支持");
        } catch (Exception e) {
            log.error("WvpService初始化失败", e);
            throw new RuntimeException("WvpService初始化失败", e);
        }
    }
    
    /**
     * 获取WVP访问Token
     * 
     * @return 访问Token
     */
    public String getAccessToken() {
        // Token有效期内直接返回
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }
        
        // Token过期，重新登录
        return login();
    }
    
    /**
     * 登录WVP获取Token
     * 
     * 注意：
     * 1. WVP登录接口要求使用GET方法，参数通过query传递
     * 2. 密码直接使用配置中的MD5加密值，不进行二次加密
     * 3. 用户需要在系统配置中配置MD5加密后的密码（32位小写十六进制字符串）
     *
     * @return 访问Token
     */
    private String login() {
        try {
            // 构建URL，参数通过query传递
            // 注意：密码直接使用配置中的MD5加密值，不进行二次加密
            String baseUrl = wvpConfigService.getWvpServerUrl() + "/api/user/login";
            HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                    .addQueryParameter("username", wvpConfigService.getWvpUsername())
                    .addQueryParameter("password", wvpConfigService.getWvpPassword()) // 直接使用MD5加密后的密码值
                    .build();

            // 发送GET请求
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            Response response = httpClient.newCall(request).execute();
            String responseBody = response.body().string();

            log.info("WVP登录响应: {}", responseBody);

            // 解析响应
            JSONObject json = JSON.parseObject(responseBody);
            if (json.getInteger("code") == 0) {
                JSONObject data = json.getJSONObject("data");
                accessToken = data.getString("accessToken"); // 注意：字段名是accessToken（驼峰）

                // 从响应中读取有效期（秒），默认30分钟，提前1分钟过期
                Long expiresIn = data.getLong("expiresIn");
                if (expiresIn == null || expiresIn <= 0) {
                    expiresIn = 1800L; // 默认30分钟
                }
                tokenExpireTime = System.currentTimeMillis() + (expiresIn - 60) * 1000; // 提前1分钟过期

                log.info("WVP登录成功, Token: {}, 有效期: {}秒", accessToken, expiresIn);
                return accessToken;
            } else {
                log.error("WVP登录失败: {}", json.getString("msg"));
                throw new RuntimeException("WVP登录失败: " + json.getString("msg"));
            }
        } catch (IOException e) {
            log.error("WVP登录请求失败", e);
            throw new RuntimeException("WVP登录请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询设备状态
     * 
     * @param deviceId GB28181设备编码
     * @return 设备状态信息
     */
    public JSONObject queryDeviceStatus(String deviceId) {
        try {
            String url = wvpConfigService.getWvpServerUrl() + "/api/device/query/devices/" + deviceId;
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("access-token", getAccessToken())
                    .get()
                    .build();
            
            Response response = httpClient.newCall(request).execute();
            String responseBody = response.body().string();
            
            log.info("WVP设备状态响应: deviceId={}, response={}", deviceId, responseBody);
            
            JSONObject json = JSON.parseObject(responseBody);
            if (json.getInteger("code") == 0) {
                JSONObject data = json.getJSONObject("data");
                if (data != null) {
                    return data;
                } else {
                    log.warn("WVP返回数据为空: deviceId={}", deviceId);
                    return null;
                }
            } else {
                String errorMsg = json.getString("msg");
                log.error("查询设备状态失败: deviceId={}, error={}", deviceId, errorMsg);
                throw new RuntimeException("WVP查询设备状态失败: " + errorMsg);
            }
        } catch (RuntimeException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("查询设备状态异常: deviceId={}", deviceId, e);
            throw new RuntimeException("查询设备状态异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取实时视频流地址
     * 
     * @param deviceId GB28181设备编码
     * @param channelId GB28181通道编码
     * @return 视频流信息
     */
    public JSONObject getPlayUrl(String deviceId, String channelId) {
        try {
            String url = wvpConfigService.getWvpServerUrl() + "/api/play/start/" + deviceId + "/" + channelId;
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("access-token", getAccessToken())
                    .get()
                    .build();
            
            Response response = httpClient.newCall(request).execute();
            String responseBody = response.body().string();
            
            log.debug("WVP播放地址响应: {}", responseBody);
            
            JSONObject json = JSON.parseObject(responseBody);
            if (json.getInteger("code") == 0) {
                return json.getJSONObject("data");
            } else {
                log.error("获取播放地址失败: {}", json.getString("msg"));
                throw new RuntimeException("获取播放地址失败: " + json.getString("msg"));
            }
        } catch (IOException e) {
            log.error("获取播放地址异常: deviceId={}, channelId={}", deviceId, channelId, e);
            throw new RuntimeException("获取播放地址失败: " + e.getMessage());
        }
    }
}
