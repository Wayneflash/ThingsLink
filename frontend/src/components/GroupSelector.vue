<!--
  全局分组选择器组件 - GroupSelector
  
  功能：
  - 自动从后端加载分组数据（基于用户权限过滤）
  - 以树形结构展示分组
  - 支持搜索过滤
  - 支持双向数据绑定 (v-model)
  
  使用示例：
  
  1. 基础用法（自动加载）：
  <GroupSelector v-model="form.groupId" />
  
  2. 自定义配置：
  <GroupSelector 
    v-model="form.groupId"
    placeholder="请选择分组"
    :clearable="false"
    :disabled="isSuperAdmin"
  />
  
  3. 手动控制加载：
  <GroupSelector 
    ref="groupSelectorRef"
    v-model="form.groupId"
    :auto-load="false"
  />
  
  // 在需要的时候手动调用
  groupSelectorRef.value.loadGroups()
  
  Props：
  - modelValue: Number - 当前选中的分组ID
  - placeholder: String - 占位符文字，默认"请选择所属分组"
  - disabled: Boolean - 是否禁用，默认false
  - clearable: Boolean - 是否可清空，默认true
  - defaultExpandAll: Boolean - 是否默认展开所有节点，默认true
  - autoLoad: Boolean - 是否自动加载数据，默认true
  
  Events：
  - update:modelValue - 值变化时触发
  - change - 选择变化时触发
  - loaded - 数据加载完成时触发
  
  Methods (通过 ref 访问)：
  - loadGroups() - 手动加载分组数据
  - groupsData - 获取当前加载的分组数据
  
  注意事项：
  - 组件会自动调用 /device-groups/tree 接口
  - 后端会根据当前用户权限过滤分组数据
  - 超级管理员看到所有分组，普通用户只看到自己所属分组及其子分组
  - 如果后端接口修改，只需修改这一个组件即可，无需修改其他页面
-->
<template>
  <el-tree-select
    v-model="selectedValue"
    :data="treeData"
    node-key="id"
    :props="{ label: 'name', children: 'children' }"
    :render-after-expand="false"
    :default-expand-all="defaultExpandAll"
    check-strictly
    filterable
    :filter-node-method="filterNode"
    :placeholder="placeholder"
    :disabled="disabled"
    :clearable="clearable"
    class="group-selector"
    popper-class="group-selector-dropdown"
    @change="handleChange"
  />
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { getGroupTree } from '@/api/group'
import { ElMessage } from 'element-plus'

const props = defineProps({
  // 当前选中的分组ID
  modelValue: {
    type: Number,
    default: null
  },
  // 占位符
  placeholder: {
    type: String,
    default: '请选择所属分组'
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否可清空
  clearable: {
    type: Boolean,
    default: true
  },
  // 是否默认展开所有节点
  defaultExpandAll: {
    type: Boolean,
    default: true
  },
  // 是否自动加载数据
  autoLoad: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'loaded'])

// 分组原始数据（从后端获取）
const groupsData = ref([])
const loading = ref(false)

// 双向绑定的值
const selectedValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 加载分组数据
const loadGroups = async () => {
  try {
    loading.value = true
    const res = await getGroupTree()
    // 直接使用后端返回的树形数据
    groupsData.value = res.tree || []
    console.log('GroupSelector - 加载的分组数据:', groupsData.value)
    emit('loaded', groupsData.value)
  } catch (error) {
    console.error('加载分组数据失败:', error)
    ElMessage.error('加载分组数据失败')
  } finally {
    loading.value = false
  }
}

// 构建树形数据（直接使用后端返回的树形结构）
const treeData = computed(() => {
  const convertToTreeSelectFormat = (nodes) => {
    if (!Array.isArray(nodes)) return []
    return nodes.map(node => {
      const converted = {
        id: node.id,
        name: node.name,
        label: node.name,
        value: node.id
      }
      if (node.children && node.children.length > 0) {
        converted.children = convertToTreeSelectFormat(node.children)
      }
      return converted
    })
  }
  
  const tree = convertToTreeSelectFormat(groupsData.value)
  console.log('GroupSelector - 构建的树形数据:', tree)
  return tree
})

// 暴露加载方法给父组件
defineExpose({
  loadGroups,
  groupsData
})

// 节点过滤方法
const filterNode = (value, data) => {
  if (!value) return true
  return data.name.toLowerCase().includes(value.toLowerCase())
}

// 值变化回调
const handleChange = (value) => {
  emit('change', value)
}

// 组件挂载时自动加载数据
onMounted(() => {
  if (props.autoLoad) {
    loadGroups()
  }
})
</script>

<style scoped>
.group-selector {
  width: 100%;
}
</style>

<style>
/* 下拉框样式（不能scoped） */
.group-selector-dropdown {
  max-height: 500px !important;
  min-width: 300px !important;
}

.group-selector-dropdown .el-scrollbar {
  max-height: 450px !important;
}

.group-selector-dropdown .el-tree {
  max-height: 450px !important;
  overflow-y: auto !important;
  padding: 10px;
}

.group-selector-dropdown .el-tree-node {
  white-space: normal !important;
  min-height: 32px !important;
}

.group-selector-dropdown .el-tree-node__content {
  height: auto !important;
  min-height: 32px !important;
  line-height: 32px !important;
  padding: 4px 0 !important;
}

.group-selector-dropdown .el-select-dropdown__item {
  height: auto !important;
  max-height: none !important;
  padding: 0 !important;
}
</style>
