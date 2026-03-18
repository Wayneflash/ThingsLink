package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.SceneRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 场景联动规则Mapper
 */
@Mapper
public interface SceneRuleMapper extends BaseMapper<SceneRule> {

    /**
     * 查询所有启用的规则（包含设备信息）
     */
    @Select("SELECT r.*, " +
            "td.device_name AS trigger_device_name, td.device_code AS trigger_device_code, " +
            "tdd.device_name AS target_device_name, tdd.device_code AS target_device_code " +
            "FROM tb_scene_rule r " +
            "LEFT JOIN tb_device td ON r.trigger_device_id = td.id " +
            "LEFT JOIN tb_device tdd ON r.target_device_id = tdd.id " +
            "WHERE r.enabled = 1 " +
            "ORDER BY r.id DESC")
    List<SceneRule> selectEnabledRulesWithDevice();

    /**
     * 分页查询规则（包含设备信息）
     */
    @Select("SELECT r.*, " +
            "td.device_name AS trigger_device_name, td.device_code AS trigger_device_code, " +
            "tdd.device_name AS target_device_name, tdd.device_code AS target_device_code, " +
            "ta.attr_name AS trigger_attr_name, ta.unit AS trigger_attr_unit, " +
            "tda.attr_name AS target_attr_name, tda.unit AS target_attr_unit " +
            "FROM tb_scene_rule r " +
            "LEFT JOIN tb_device td ON r.trigger_device_id = td.id " +
            "LEFT JOIN tb_device tdd ON r.target_device_id = tdd.id " +
            "LEFT JOIN tb_attribute ta ON td.product_id = ta.product_id AND r.trigger_attr COLLATE utf8mb4_unicode_ci = ta.addr COLLATE utf8mb4_unicode_ci " +
            "LEFT JOIN tb_attribute tda ON tdd.product_id = tda.product_id AND r.target_attr COLLATE utf8mb4_unicode_ci = tda.addr COLLATE utf8mb4_unicode_ci " +
            "ORDER BY r.id DESC")
    List<SceneRule> selectRulesWithDevice();
}
