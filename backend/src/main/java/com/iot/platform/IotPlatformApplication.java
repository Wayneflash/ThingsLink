package com.iot.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IoT 平台启动类
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan("com.iot.platform.mapper")
public class IotPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(IotPlatformApplication.class, args);
        System.out.println("======================================");
        System.out.println("IoT Platform Started Successfully!");
        System.out.println("======================================");
    }
}
