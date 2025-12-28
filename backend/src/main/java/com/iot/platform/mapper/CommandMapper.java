package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.Command;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品命令 Mapper
 */
@Mapper
public interface CommandMapper extends BaseMapper<Command> {
}
