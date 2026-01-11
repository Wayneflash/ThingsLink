package com.iot.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iot.platform.entity.AlarmLog;
import com.iot.platform.mapper.AlarmLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报警分析服务
 * 用于统计和分析设备的报警数据
 */
@Slf4j
@Service
public class AlarmAnalysisService {
    
    @Resource
    private AlarmLogMapper alarmLogMapper;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 获取报警分析数据
     * @param deviceCodes 设备编码列表
     * @param timeRange 时间范围：today/yesterday/7days/30days/custom
     * @param startTime 自定义开始时间
     * @param endTime 自定义结束时间
     * @return 分析数据
     */
    public Map<String, Object> getAlarmAnalysis(List<String> deviceCodes, String timeRange, 
                                                  String startTime, String endTime) {
        log.info("========== 开始查询报警分析数据 ==========");
        log.info("设备编码列表: {}", deviceCodes);
        log.info("时间范围: {}, 开始时间: {}, 结束时间: {}", timeRange, startTime, endTime);
        
        // 计算时间范围
        LocalDateTime start = calculateStartTime(timeRange, startTime);
        LocalDateTime end = calculateEndTime(timeRange, endTime);
        
        log.info("查询时间范围: {} - {}", start, end);
        
        // 查询报警日志
        LambdaQueryWrapper<AlarmLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AlarmLog::getDeviceCode, deviceCodes)
                   .ge(AlarmLog::getTriggerTime, start)
                   .le(AlarmLog::getTriggerTime, end)
                   .orderByAsc(AlarmLog::getTriggerTime);
        
        List<AlarmLog> alarmLogs = alarmLogMapper.selectList(queryWrapper);
        
        Map<String, Object> result = new HashMap<>();
        
        // 1. 查询报警趋势
        Map<String, Object> trend = calculateTrend(alarmLogs, start, end);
        result.put("trend", trend);
        
        // 2. 查询级别分布
        Map<String, Object> levelDistribution = calculateLevelDistribution(alarmLogs);
        result.put("levelDistribution", levelDistribution);
        
        // 3. 查询处理效率
        Map<String, Object> efficiency = calculateEfficiency(alarmLogs);
        result.put("efficiency", efficiency);
        
        log.info("报警趋势数据点数: {}", ((List<?>) trend.get("dates")).size());
        log.info("级别分布数据: {}", levelDistribution);
        log.info("处理效率数据: {}", efficiency);
        log.info("========== 报警分析数据查询完成 ==========");
        
        return result;
    }
    
    /**
     * 计算报警趋势
     */
    private Map<String, Object> calculateTrend(List<AlarmLog> alarmLogs, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> trend = new HashMap<>();
        
        // 计算天数
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        
        // 初始化日期列表
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        List<Long> criticals = new ArrayList<>();
        List<Long> warnings = new ArrayList<>();
        List<Long> infos = new ArrayList<>();
        
        for (int i = 0; i < days; i++) {
            LocalDateTime dayStart = start.plusDays(i);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            
            String dateStr = dayStart.format(DATE_FORMATTER);
            dates.add(dateStr);
            
            // 统计当天的报警数据
            long count = 0;
            long critical = 0;
            long warning = 0;
            long info = 0;
            
            for (AlarmLog alarm : alarmLogs) {
                if (!alarm.getTriggerTime().isBefore(dayStart) && alarm.getTriggerTime().isBefore(dayEnd)) {
                    count++;
                    String level = alarm.getAlarmLevel();
                    if ("critical".equals(level)) {
                        critical++;
                    } else if ("warning".equals(level)) {
                        warning++;
                    } else if ("info".equals(level)) {
                        info++;
                    }
                }
            }
            
            counts.add(count);
            criticals.add(critical);
            warnings.add(warning);
            infos.add(info);
        }
        
        Map<String, Object> levels = new HashMap<>();
        levels.put("critical", criticals);
        levels.put("warning", warnings);
        levels.put("info", infos);
        
        trend.put("dates", dates);
        trend.put("counts", counts);
        trend.put("levels", levels);
        
        return trend;
    }
    
    /**
     * 计算级别分布
     */
    private Map<String, Object> calculateLevelDistribution(List<AlarmLog> alarmLogs) {
        Map<String, Object> levelDistribution = new HashMap<>();
        
        long critical = 0;
        long warning = 0;
        long info = 0;
        
        for (AlarmLog alarm : alarmLogs) {
            String level = alarm.getAlarmLevel();
            if ("critical".equals(level)) {
                critical++;
            } else if ("warning".equals(level)) {
                warning++;
            } else if ("info".equals(level)) {
                info++;
            }
        }
        
        levelDistribution.put("critical", critical);
        levelDistribution.put("warning", warning);
        levelDistribution.put("info", info);
        
        return levelDistribution;
    }
    
    /**
     * 计算处理效率
     */
    private Map<String, Object> calculateEfficiency(List<AlarmLog> alarmLogs) {
        Map<String, Object> efficiency = new HashMap<>();
        
        long totalCount = alarmLogs.size();
        long handledCount = 0;
        long totalHandleMinutes = 0;
        
        for (AlarmLog alarm : alarmLogs) {
            if (alarm.getStatus() == 1 && alarm.getHandleTime() != null) {
                handledCount++;
                long minutes = ChronoUnit.MINUTES.between(alarm.getTriggerTime(), alarm.getHandleTime());
                totalHandleMinutes += minutes;
            }
        }
        
        long handlingRate = totalCount > 0 ? (handledCount * 100 / totalCount) : 0;
        long avgHandleTime = handledCount > 0 ? (totalHandleMinutes / handledCount) : 0;
        
        efficiency.put("totalCount", totalCount);
        efficiency.put("handledCount", handledCount);
        efficiency.put("handlingRate", handlingRate);
        efficiency.put("avgHandleTime", avgHandleTime);
        
        return efficiency;
    }
    
    /**
     * 计算开始时间
     */
    private LocalDateTime calculateStartTime(String timeRange, String startTimeStr) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (timeRange) {
            case "today":
                return LocalDateTime.of(now.toLocalDate(), java.time.LocalTime.MIN);
            case "yesterday":
                LocalDateTime yesterday = now.minusDays(1);
                return LocalDateTime.of(yesterday.toLocalDate(), java.time.LocalTime.MIN);
            case "7days":
                return now.minusDays(7);
            case "30days":
                return now.minusDays(30);
            case "custom":
                if (startTimeStr != null && !startTimeStr.isEmpty()) {
                    return LocalDateTime.parse(startTimeStr, FORMATTER);
                }
                return now.minusDays(30);
            default:
                return now.minusDays(7);
        }
    }
    
    /**
     * 计算结束时间
     */
    private LocalDateTime calculateEndTime(String timeRange, String endTimeStr) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (timeRange) {
            case "yesterday":
                LocalDateTime yesterday = now.minusDays(1);
                return LocalDateTime.of(yesterday.toLocalDate(), java.time.LocalTime.MAX);
            case "custom":
                if (endTimeStr != null && !endTimeStr.isEmpty()) {
                    return LocalDateTime.parse(endTimeStr, FORMATTER);
                }
                return now;
            default:
                return now;
        }
    }
}
