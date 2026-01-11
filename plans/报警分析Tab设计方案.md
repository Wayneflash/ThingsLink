# 报警分析Tab设计方案

## 一、需求概述

### 1.1 功能定位
在设备详情页添加"报警分析"tab，用于展示单个设备的报警数据分析，帮助用户了解设备的报警趋势、报警级别分布和处理效率。

### 1.2 页面位置
- **位置**：设备详情页（DeviceDetail.vue）
- **Tab顺序**：实时数据 → 历史数据 → 命令控制 → 设备日志 → **报警分析**
- **页面布局**：点击报警分析tab后，页面放大空间（参考历史数据tab的布局）

---

## 二、功能设计

### 2.1 界面布局

```
┌─────────────────────────────────────────────────────────────────┐
│  报警分析                                                      │
├─────────────────────────────────────────────────────────────────┤
│  时间范围：[今日▼]  [最近7天▼]  [最近30天▼]  [自定义▼]        │
├─────────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌───────────────────────────────────────────────────────┐    │
│  │  处理效率                                             │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐            │    │
│  │  │ 总报警数  │ │ 处理率   │ │ 平均处理  │            │    │
│  │  │   25     │ │  80%     │ │ 120分钟   │            │    │
│  │  └──────────┘ └──────────┘ └──────────┘            │    │
│  └───────────────────────────────────────────────────────┘    │
│                                                               │
│  ┌──────────────────────────────────┐ ┌────────────────────┐  │
│  │  报警趋势                        │ │  级别分布           │  │
│  │  [折线图 - 按日期统计]           │ │  [饼图 - 环形]      │  │
│  └──────────────────────────────────┘ └────────────────────┘  │
│                                                               │
│  ┌───────────────────────────────────────────────────────┐    │
│  │  报警统计                                             │    │
│  │  筛选：级别[全部▼] 状态[全部▼]                        │    │
│  │  ┌─────────────────────────────────────────────┐      │    │
│  │  │ 时间       │ 级别   │ 属性名称 │ 状态  │ 操作 │      │    │
│  │  ├─────────────────────────────────────────────┤      │    │
│  │  │ 01-11 10:00 │ 严重   │ 温度   │ 未处理 │ 详情 │      │    │
│  │  │ 01-11 09:30 │ 警告   │ 湿度   │ 已处理 │ 详情 │      │    │
│  │  │ 01-10 22:15 │ 提示   │ 离线   │ 已恢复 │ 详情 │      │    │
│  │  └─────────────────────────────────────────────┘      │    │
│  │  分页器：[上一页] [1] [2] [3] [下一页]              │    │
│  └───────────────────────────────────────────────────────┘    │
│                                                               │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 功能模块

#### 2.2.1 时间范围筛选
- **选项**：今日、昨日、最近7天、最近30天、自定义
- **默认值**：最近7天
- **自定义范围**：支持日期时间选择器

#### 2.2.2 处理效率卡片
显示3个关键指标：
- **总报警数**：时间范围内的报警总数
- **处理率**：已处理报警占比（已处理/总数 × 100%）
- **平均处理时间**：从触发到处理的平均时长（分钟）

#### 2.2.3 报警趋势图
- **图表类型**：折线图
- **X轴**：日期（根据时间范围自动调整粒度）
- **Y轴**：报警数量
- **多条线**：分别显示严重、警告、提示三个级别
- **交互**：支持缩放、拖拽、Tooltip显示详情

#### 2.2.4 级别分布图
- **图表类型**：环形饼图
- **数据**：严重、警告、提示的报警数量和占比
- **中心显示**：总报警数
- **图例**：显示级别名称和百分比

#### 2.2.5 报警统计表格
- **功能**：显示该设备的详细报警记录列表，支持直接处理报警
- **筛选条件**：
  - 报警级别：全部、严重、警告、提示
  - 处理状态：全部、未处理、已处理
- **表格列**：
  - 触发时间
  - 报警级别（标签显示）
  - 属性名称（物模型属性名称，如"温度"、"湿度"）
  - 报警消息
  - 处理状态（标签显示）
  - 处理人（已处理时显示）
  - 操作（未处理显示"处理"，已处理显示"查看详情"）
- **分页**：支持分页查询
- **排序**：默认按触发时间倒序
- **处理功能**：
  - 点击"处理"按钮：弹出处理对话框
  - 处理对话框包含：处理描述（必填）、处理图片（可选）
  - 提交后更新报警状态为"已处理"，记录处理人和处理时间
  - 刷新表格数据
- **查看详情**：
  - 点击"查看详情"按钮：显示完整的报警信息
  - 包括：触发时间、报警级别、属性名称、报警消息、处理记录

---

## 三、技术实现

### 3.1 数据来源
使用现有的 [`tb_alarm_log`](backend/src/main/java/com/iot/platform/entity/AlarmLog.java) 表，无需新增表结构。

### 3.2 后端接口设计

#### 3.2.1 新增接口
**路径**：`POST /alarm-log/analysis`

**请求参数**：
```json
{
  "deviceCodes": ["device001"],     // 设备编码数组，支持单个或多个设备（必填）
  "timeRange": "7days",           // 时间范围：today/yesterday/7days/30days/custom
  "startTime": "2026-01-01 00:00:00",  // 自定义开始时间（timeRange=custom时必填）
  "endTime": "2026-01-11 23:59:59"      // 自定义结束时间（timeRange=custom时必填）
}
```

**说明**：
- `deviceCodes` 使用数组格式，支持单个设备（`["device001"]`）或多个设备（`["device001", "device002", "device003"]`）
- 本次实现：只传入单个设备编码
- 后续扩展：Dashboard可以传入多个设备编码，实现全局统计

**返回数据**：
```json
{
  "trend": {
    "dates": ["2026-01-05", "2026-01-06", "2026-01-07", "2026-01-08", "2026-01-09", "2026-01-10", "2026-01-11"],
    "counts": [3, 5, 2, 8, 4, 2, 1],
    "levels": {
      "critical": [1, 2, 0, 3, 1, 0, 0],
      "warning": [1, 2, 1, 4, 2, 1, 1],
      "info": [1, 1, 1, 1, 1, 1, 0]
    }
  },
  "levelDistribution": {
    "critical": 7,
    "warning": 12,
    "info": 6
  },
  "efficiency": {
    "totalCount": 25,
    "handledCount": 20,
    "handlingRate": 80,
    "avgHandleTime": 120
  }
}
```

#### 3.2.2 查询逻辑

**1. 时间范围计算**
```java
LocalDateTime startTime = null;
LocalDateTime endTime = LocalDateTime.now();

switch (timeRange) {
    case "today":
        startTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        break;
    case "yesterday":
        LocalDate yesterday = LocalDate.now().minusDays(1);
        startTime = LocalDateTime.of(yesterday, LocalTime.MIN);
        endTime = LocalDateTime.of(yesterday, LocalTime.MAX);
        break;
    case "7days":
        startTime = LocalDateTime.now().minusDays(7);
        break;
    case "30days":
        startTime = LocalDateTime.now().minusDays(30);
        break;
    case "custom":
        startTime = parse(startTimeStr);
        endTime = parse(endTimeStr);
        break;
}
```

**2. 报警趋势查询**
```sql
-- 按日期统计报警数量（支持单个或多个设备）
SELECT 
    DATE(trigger_time) as date,
    COUNT(*) as count,
    SUM(CASE WHEN alarm_level = 'critical' THEN 1 ELSE 0 END) as critical,
    SUM(CASE WHEN alarm_level = 'warning' THEN 1 ELSE 0 END) as warning,
    SUM(CASE WHEN alarm_level = 'info' THEN 1 ELSE 0 END) as info
FROM tb_alarm_log
WHERE device_code IN (?, ?, ?) AND trigger_time >= ? AND trigger_time <= ?
GROUP BY DATE(trigger_time)
ORDER BY date
```

**3. 级别分布查询**
```sql
-- 级别分布（支持单个或多个设备）
SELECT 
    alarm_level,
    COUNT(*) as count
FROM tb_alarm_log
WHERE device_code IN (?, ?, ?) AND trigger_time >= ? AND trigger_time <= ?
GROUP BY alarm_level
```

**4. 处理效率查询**
```sql
-- 处理效率（支持单个或多个设备）
SELECT 
    COUNT(*) as total_count,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as handled_count,
    AVG(TIMESTAMPDIFF(MINUTE, trigger_time, handle_time)) as avg_handle_time
FROM tb_alarm_log
WHERE device_code IN (?, ?, ?) AND trigger_time >= ? AND trigger_time <= ?
```

**说明**：
- 使用 `IN` 子句支持多个设备编码
- Java后端需要动态构建SQL或使用MyBatis的动态SQL功能
- 单个设备时传入一个参数，多个设备时传入多个参数

### 3.3 前端实现

#### 3.3.1 技术栈
- Vue 3 Composition API
- ECharts 5（图表库）
- Element Plus（UI组件）

#### 3.3.2 组件结构
```vue
<!-- 报警分析Tab -->
<div class="tab-content" :class="{ active: activeTab === 'alarm' }">
  <!-- 时间范围筛选 -->
  <div class="alarm-toolbar">
    <el-radio-group v-model="alarmTimeRange" size="small">
      <el-radio-button label="today">今日</el-radio-button>
      <el-radio-button label="7days">最近7天</el-radio-button>
      <el-radio-button label="30days">最近30天</el-radio-button>
      <el-radio-button label="custom">自定义</el-radio-button>
    </el-radio-group>
    <el-date-picker
      v-if="alarmTimeRange === 'custom'"
      v-model="alarmCustomRange"
      type="datetimerange"
      range-separator="至"
      start-placeholder="开始时间"
      end-placeholder="结束时间"
      format="YYYY-MM-DD HH:mm:ss"
      value-format="YYYY-MM-DD HH:mm:ss"
    />
    <el-button type="primary" @click="loadAlarmAnalysis">查询</el-button>
  </div>

  <!-- 处理效率卡片 -->
  <div class="efficiency-cards">
    <div class="efficiency-card">
      <div class="card-label">总报警数</div>
      <div class="card-value">{{ alarmAnalysis.efficiency.totalCount }}</div>
    </div>
    <div class="efficiency-card">
      <div class="card-label">处理率</div>
      <div class="card-value">{{ alarmAnalysis.efficiency.handlingRate }}%</div>
    </div>
    <div class="efficiency-card">
      <div class="card-label">平均处理时间</div>
      <div class="card-value">{{ alarmAnalysis.efficiency.avgHandleTime }}分钟</div>
    </div>
  </div>

  <!-- 图表区域 -->
  <div class="charts-container">
    <!-- 报警趋势图 -->
    <div class="chart-card">
      <div class="chart-title">报警趋势</div>
      <div ref="trendChartRef" class="chart"></div>
    </div>

    <!-- 级别分布图 -->
    <div class="chart-card">
      <div class="chart-title">级别分布</div>
      <div ref="levelChartRef" class="chart"></div>
    </div>
  </div>

  <!-- 报警统计表格 -->
  <div class="table-card">
    <div class="table-header">
      <div class="table-title">报警统计</div>
      <div class="table-filters">
        <el-select v-model="alarmTableFilter.level" placeholder="报警级别" clearable style="width: 120px;">
          <el-option label="全部" value="" />
          <el-option label="严重" value="critical" />
          <el-option label="警告" value="warning" />
          <el-option label="提示" value="info" />
        </el-select>
        <el-select v-model="alarmTableFilter.status" placeholder="处理状态" clearable style="width: 120px;">
          <el-option label="全部" value="" />
          <el-option label="未处理" :value="0" />
          <el-option label="已处理" :value="1" />
        </el-select>
        <el-button type="primary" @click="loadAlarmTable">查询</el-button>
      </div>
    </div>
    <el-table :data="alarmTableData" stripe v-loading="alarmTableLoading" style="width: 100%">
      <el-table-column prop="triggerTime" label="触发时间" width="180" />
      <el-table-column prop="alarmLevel" label="报警级别" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.alarmLevel === 'critical'" type="danger">严重</el-tag>
          <el-tag v-else-if="row.alarmLevel === 'warning'" type="warning">警告</el-tag>
          <el-tag v-else type="info">提示</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="attrName" label="属性名称" width="150" />
      <el-table-column prop="alarmMessage" label="报警消息" min-width="200" />
      <el-table-column prop="status" label="处理状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="danger">未处理</el-tag>
          <el-tag v-else type="success">已处理</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handler" label="处理人" width="100" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" text type="primary" @click="handleAlarm(row)">处理</el-button>
          <el-button v-else text type="primary" @click="viewAlarmDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 处理报警对话框 -->
    <el-dialog v-model="handleDialogVisible" title="处理报警" width="600px">
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="报警信息">
          <div class="alarm-info">
            <div><strong>触发时间：</strong>{{ currentAlarm.triggerTime }}</div>
            <div><strong>报警级别：</strong>{{ currentAlarm.alarmLevel }}</div>
            <div><strong>属性名称：</strong>{{ currentAlarm.attrName }}</div>
            <div><strong>报警消息：</strong>{{ currentAlarm.alarmMessage }}</div>
          </div>
        </el-form-item>
        <el-form-item label="处理描述" required>
          <el-input v-model="handleForm.handleDescription" type="textarea" :rows="4" placeholder="请输入处理描述" />
        </el-form-item>
        <el-form-item label="处理图片">
          <el-upload
            v-model:file-list="handleForm.handleImages"
            action="/api/upload"
            list-type="picture-card"
            :limit="5"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandleAlarm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="报警详情" width="700px">
      <div class="alarm-detail">
        <div class="detail-row">
          <span class="detail-label">触发时间：</span>
          <span class="detail-value">{{ currentAlarm.triggerTime }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">报警级别：</span>
          <el-tag v-if="currentAlarm.alarmLevel === 'critical'" type="danger">严重</el-tag>
          <el-tag v-else-if="currentAlarm.alarmLevel === 'warning'" type="warning">警告</el-tag>
          <el-tag v-else type="info">提示</el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">属性名称：</span>
          <span class="detail-value">{{ currentAlarm.attrName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">报警消息：</span>
          <span class="detail-value">{{ currentAlarm.alarmMessage }}</span>
        </div>
        <div v-if="currentAlarm.status === 1" class="detail-section">
          <div class="detail-section-title">处理记录</div>
          <div class="detail-row">
            <span class="detail-label">处理人：</span>
            <span class="detail-value">{{ currentAlarm.handler }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">处理时间：</span>
            <span class="detail-value">{{ currentAlarm.handleTime }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">处理描述：</span>
            <span class="detail-value">{{ currentAlarm.handleDescription }}</span>
          </div>
          <div v-if="currentAlarm.handleImages" class="detail-row">
            <span class="detail-label">处理图片：</span>
            <div class="detail-images">
              <el-image
                v-for="(img, index) in JSON.parse(currentAlarm.handleImages)"
                :key="index"
                :src="img"
                :preview-src-list="JSON.parse(currentAlarm.handleImages)"
                fit="cover"
                style="width: 100px; height: 100px; margin-right: 10px;"
              />
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
    <el-pagination
      v-if="alarmTableData.length > 0"
      v-model:current-page="alarmTablePagination.currentPage"
      v-model:page-size="alarmTablePagination.pageSize"
      :total="alarmTablePagination.total"
      :page-sizes="[20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      class="table-pagination"
      @size-change="loadAlarmTable"
      @current-change="loadAlarmTable"
    />
  </div>
</div>
```

#### 3.3.3 ECharts配置

**1. 报警趋势图（折线图）**
```javascript
const trendOption = {
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'cross' }
  },
  legend: {
    data: ['严重', '警告', '提示'],
    top: 10
  },
  grid: {
    left: 50,
    right: 30,
    bottom: 50,
    top: 40
  },
  xAxis: {
    type: 'category',
    data: alarmAnalysis.trend.dates,
    axisLabel: { rotate: 45 }
  },
  yAxis: {
    type: 'value'
  },
  dataZoom: [
    { type: 'inside' },
    { type: 'slider', bottom: 10 }
  ],
  series: [
    {
      name: '严重',
      type: 'line',
      data: alarmAnalysis.trend.levels.critical,
      itemStyle: { color: '#F56C6C' }
    },
    {
      name: '警告',
      type: 'line',
      data: alarmAnalysis.trend.levels.warning,
      itemStyle: { color: '#E6A23C' }
    },
    {
      name: '提示',
      type: 'line',
      data: alarmAnalysis.trend.levels.info,
      itemStyle: { color: '#409EFF' }
    }
  ]
}
```

**2. 级别分布图（环形饼图）**
```javascript
const levelOption = {
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    right: 10,
    top: 'center'
  },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      data: [
        { value: alarmAnalysis.levelDistribution.critical, name: '严重', itemStyle: { color: '#F56C6C' } },
        { value: alarmAnalysis.levelDistribution.warning, name: '警告', itemStyle: { color: '#E6A23C' } },
        { value: alarmAnalysis.levelDistribution.info, name: '提示', itemStyle: { color: '#409EFF' } }
      ],
      label: { show: false }
    }
  ]
}
```

### 3.4 样式设计

```css
/* 报警分析Tab */
.alarm-toolbar {
  background: white;
  border: 1px solid #e5e5e7;
  border-radius: 8px;
  padding: 6px 14px;
  margin-bottom: 4px;
  display: flex;
  gap: 12px;
  align-items: center;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.efficiency-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.efficiency-card {
  background: white;
  border: 1px solid #e5e5e7;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.card-label {
  font-size: 14px;
  color: #86868b;
  margin-bottom: 8px;
}

.card-value {
  font-size: 32px;
  font-weight: 600;
  color: #007AFF;
}

.charts-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-card {
  background: white;
  border: 1px solid #e5e5e7;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 16px;
}

.chart {
  width: 100%;
  height: 300px;
}

/* 报警统计表格 */
.table-card {
  background: white;
  border: 1px solid #e5e5e7;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  margin-top: 16px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.table-filters {
  display: flex;
  gap: 12px;
  align-items: center;
}

.table-pagination {
  padding: 8px 16px;
  flex-shrink: 0;
  border-top: 1px solid #e5e5e7;
}

/* 报警信息 */
.alarm-info {
  background: #f5f5f7;
  padding: 12px;
  border-radius: 8px;
  line-height: 1.8;
}

.alarm-info div {
  margin-bottom: 8px;
}

.alarm-info strong {
  color: #666;
  margin-right: 8px;
}

/* 报警详情 */
.alarm-detail {
  padding: 16px;
}

.detail-row {
  display: flex;
  margin-bottom: 16px;
  line-height: 1.6;
}

.detail-label {
  width: 100px;
  color: #86868b;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #1d1d1f;
}

.detail-section {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e5e5e7;
}

.detail-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 16px;
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
```

---

## 四、实施计划

### 4.1 开发步骤

| 阶段 | 任务 | 文件 |
|-----|------|------|
| 后端 | 创建AlarmAnalysisService | `AlarmAnalysisService.java` |
| 后端 | 创建AlarmAnalysisController | `AlarmAnalysisController.java` |
| 后端 | 创建AlarmAnalysisMapper | `AlarmAnalysisMapper.java` |
| 后端 | 创建AlarmAnalysisMapper.xml | `AlarmAnalysisMapper.xml` |
| 前端 | 创建报警分析API | `frontend/src/api/alarmAnalysis.js` |
| 前端 | 创建报警分析组件 | `frontend/src/components/AlarmAnalysis.vue` |
| 前端 | 集成到DeviceDetail.vue | 修改 `DeviceDetail.vue` |

### 4.2 数据库优化
检查并添加必要的索引（如果尚未存在）：
```sql
-- 检查现有索引
SHOW INDEX FROM tb_alarm_log;

-- 添加复合索引（如果不存在）
CREATE INDEX idx_device_time ON tb_alarm_log(device_code, trigger_time);
CREATE INDEX idx_device_level_time ON tb_alarm_log(device_code, alarm_level, trigger_time);
```

---

## 五、注意事项

1. **空数据处理**：当设备没有报警数据时，显示友好的空状态提示
2. **时间格式**：统一使用 `yyyy-MM-dd HH:mm:ss` 格式
3. **权限控制**：复用现有的数据权限机制
4. **性能优化**：大数据量时考虑分页或采样
5. **响应式**：适配不同屏幕尺寸
6. **图表响应**：窗口大小变化时自动调整图表尺寸
7. **属性名称显示**：报警统计表格中显示物模型属性名称（attrName），而不是metric字段
8. **数组参数设计**：后端接口使用数组格式，支持单个或多个设备，为Dashboard全局统计做准备

---

## 六、后续扩展

- 支持导出分析报告（PDF/Excel）
- 支持多设备对比分析
- 支持报警预测（基于历史数据）
- 支持自定义报警阈值建议
