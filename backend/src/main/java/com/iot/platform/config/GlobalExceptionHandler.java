package com.iot.platform.config;

import com.iot.platform.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：将未捕获异常转为统一 JSON 响应，避免返回 500 时前端无法获取具体错误信息
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("Uncaught exception", e);
        String msg = e.getMessage();
        if (msg == null) {
            msg = e.getClass().getSimpleName();
        }
        // 常见连接错误时给出更友好提示
        if (msg.contains("Connection refused") || msg.contains("connect")) {
            if (msg.contains("6379") || msg.contains("Redis")) {
                msg = "Redis 连接失败，请确认 Docker 中 redis 容器已启动";
            } else if (msg.contains("3306") || msg.contains("MySQL")) {
                msg = "MySQL 连接失败，请确认 Docker 中 mysql 容器已启动";
            }
        }
        return Result.error(msg);
    }
}
