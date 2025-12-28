package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.Attribute;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品属性 Mapper
 */
@Mapper
public interface AttributeMapper extends BaseMapper<Attribute> {
}
