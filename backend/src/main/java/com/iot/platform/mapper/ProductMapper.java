package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品 Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
