package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.SceneLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 场景联动执行日志Mapper
 */
@Mapper
public interface SceneLogMapper extends BaseMapper<SceneLog> {

    /**
     * 根据规则ID查询日志
     */
    @Select("SELECT * FROM tb_scene_log WHERE rule_id = #{ruleId} ORDER BY create_time DESC")
    List<SceneLog> selectByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 查询最近日志
     */
    @Select("SELECT * FROM tb_scene_log ORDER BY create_time DESC LIMIT #{limit}")
    List<SceneLog> selectRecent(@Param("limit") int limit);
}
