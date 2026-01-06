<template>
  <div class="alarm-log-page">
    <el-card>
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-lg font-semibold">报警日志</span>
          <div class="flex gap-2">
            <el-select v-model="filterLevel" placeholder="告警级别" clearable class="w-32">
              <el-option label="严重" value="critical" />
              <el-option label="警告" value="warning" />
              <el-option label="提示" value="info" />
            </el-select>
            <el-button type="primary">查询</el-button>
          </div>
        </div>
      </template>

      <!-- 告警列表 -->
      <el-table :data="alarmList" border>
        <el-table-column prop="deviceName" label="设备名称" width="180" />
        <el-table-column prop="alarmType" label="告警类型" width="150" />
        <el-table-column label="告警级别" width="120">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="告警内容" />
        <el-table-column prop="createTime" label="发生时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const filterLevel = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(50)

const alarmList = ref([
  {
    deviceName: '温湿度传感器001',
    alarmType: '温度超限',
    level: 'critical',
    message: '当前温度45℃，超过阈值40℃',
    createTime: '2024-01-15 10:30:00'
  },
  {
    deviceName: '智能开关002',
    alarmType: '设备离线',
    level: 'warning',
    message: '设备连接超时，已离线',
    createTime: '2024-01-15 09:15:00'
  }
])

const getLevelType = (level) => {
  const types = {
    critical: 'danger',
    warning: 'warning',
    info: 'info'
  }
  return types[level] || 'info'
}

const getLevelText = (level) => {
  const texts = {
    critical: '严重',
    warning: '警告',
    info: '提示'
  }
  return texts[level] || '未知'
}
</script>
