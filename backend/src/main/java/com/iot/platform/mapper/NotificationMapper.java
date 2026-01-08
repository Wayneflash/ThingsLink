package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息通知Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
