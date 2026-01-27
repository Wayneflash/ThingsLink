package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.dto.energy.EnergyDailyStatDTO;
import com.iot.platform.entity.DeviceData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备数据 Mapper
 */
@Mapper
public interface DeviceDataMapper extends BaseMapper<DeviceData> {
    
    /**
     * 能耗统计聚合查询（按日期+设备分组，计算每天的第一条和最后一条数据）
     * 使用子查询方式，兼容MySQL 5.7+
     * 
     * @param deviceIds 设备ID列表
     * @param addr 属性标识符（如 total_energy）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param dateFormat 日期格式化格式（如 '%Y-%m-%d' 用于日报，'%Y-%m' 用于月报，'%Y' 用于年报）
     * @return 聚合统计结果
     */
    @Select("<script>" +
            "SELECT " +
            "  stat.statDate, " +
            "  stat.deviceId, " +
            "  first_val.addrv_value as startValue, " +
            "  last_val.addrv_value as endValue, " +
            "  (last_val.addrv_value - first_val.addrv_value) as consumption " +
            "FROM ( " +
            "  SELECT " +
            "    DATE_FORMAT(ctime, #{dateFormat}) as statDate, " +
            "    device_id as deviceId, " +
            "    MIN(ctime) as first_time, " +
            "    MAX(ctime) as last_time " +
            "  FROM tb_device_data " +
            "  WHERE addr = #{addr} " +
            "    AND ctime &gt;= #{startTime} " +
            "    AND ctime &lt;= #{endTime} " +
            "    <if test=\"deviceIds != null and deviceIds.size() &gt; 0\">" +
            "      AND device_id IN " +
            "      <foreach collection=\"deviceIds\" item=\"id\" open=\"(\" separator=\",\" close=\")\">" +
            "        #{id}" +
            "      </foreach>" +
            "    </if>" +
            "  GROUP BY DATE_FORMAT(ctime, #{dateFormat}), device_id " +
            ") stat " +
            "LEFT JOIN ( " +
            "  SELECT device_id, ctime, CAST(addrv AS DECIMAL(20,2)) as addrv_value " +
            "  FROM tb_device_data " +
            "  WHERE addr = #{addr} " +
            ") first_val ON stat.deviceId = first_val.device_id " +
            "  AND stat.first_time = first_val.ctime " +
            "LEFT JOIN ( " +
            "  SELECT device_id, ctime, CAST(addrv AS DECIMAL(20,2)) as addrv_value " +
            "  FROM tb_device_data " +
            "  WHERE addr = #{addr} " +
            ") last_val ON stat.deviceId = last_val.device_id " +
            "  AND stat.last_time = last_val.ctime " +
            "ORDER BY stat.statDate, stat.deviceId" +
            "</script>")
    List<EnergyDailyStatDTO> selectEnergyDailyStats(
            @Param("deviceIds") List<Long> deviceIds,
            @Param("addr") String addr,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("dateFormat") String dateFormat
    );
}
