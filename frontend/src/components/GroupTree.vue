<template>
  <div class="group-tree">
    <!-- 总分组 -->
    <div 
      class="tree-item root-item"
      :class="{ active: currentGroupId === rootGroup?.id }"
    >
      <div class="tree-item-left" @click="handleSelect(rootGroup?.id)">
        <el-icon 
          class="expand-icon" 
          :class="{ expanded: expanded }"
          @click.stop="toggleExpand"
        >
          <ArrowRight />
        </el-icon>
        <span class="group-name">{{ rootGroup?.name || '总分组' }}</span>
      </div>
      <div class="tree-item-right">
        <span v-if="showCount" class="item-count">{{ getCount(rootGroup?.id) }}</span>
      </div>
    </div>
    
    <!-- 子分组 -->
    <transition name="slide-fade">
      <div v-show="expanded" class="tree-children">
        <div 
          v-for="group in childGroups" 
          :key="group.id" 
          class="tree-item child-item"
          :class="{ active: currentGroupId === group.id }"
          @click="handleSelect(group.id)"
        >
          <div class="tree-item-left">
            <span class="child-dot"></span>
            <span class="group-name">{{ group.name }}</span>
          </div>
          <div class="tree-item-right">
            <span v-if="showCount" class="item-count">{{ getCount(group.id) }}</span>
            <div v-if="showActions" class="tree-item-actions">
              <slot name="actions" :group="group">
                <el-button size="small" text @click.stop="$emit('edit', group)">编辑</el-button>
                <el-button size="small" text type="danger" @click.stop="$emit('delete', group)">删除</el-button>
              </slot>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'

const props = defineProps({
  // 分组列表
  groups: {
    type: Array,
    default: () => []
  },
  // 当前选中的分组ID
  currentGroupId: {
    type: Number,
    default: null
  },
  // 是否显示操作按钮
  showActions: {
    type: Boolean,
    default: true
  },
  // 是否显示计数
  showCount: {
    type: Boolean,
    default: false
  },
  // 计数数据（用于显示每个分组的数量）
  countData: {
    type: Object,
    default: () => ({})
  },
  // 默认是否展开
  defaultExpanded: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['select', 'edit', 'delete'])

// 展开/收起状态
const expanded = ref(props.defaultExpanded)

// 总分组（ID=1）
const rootGroup = computed(() => props.groups.find(g => g.id === 1))

// 子分组（parentId=1）
const childGroups = computed(() => props.groups.filter(g => g.parentId === 1))

// 切换展开/收起
const toggleExpand = () => {
  expanded.value = !expanded.value
}

// 选择分组
const handleSelect = (groupId) => {
  if (groupId) {
    emit('select', groupId)
  }
}

// 获取计数
const getCount = (groupId) => {
  return props.countData[groupId] || 0
}
</script>

<style scoped>
.group-tree {
  display: flex;
  flex-direction: column;
}

.tree-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  color: #303133;
  user-select: none;
}

.tree-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.tree-item-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 总分组样式 */
.root-item {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 500;
  margin-bottom: 8px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.root-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.root-item.active {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

.root-item .item-count {
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

/* 展开图标 */
.expand-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
  color: rgba(255, 255, 255, 0.9);
}

.expand-icon.expanded {
  transform: rotate(90deg);
}

/* 子分组容器 */
.tree-children {
  margin-left: 12px;
  padding-left: 20px;
  border-left: 2px solid #e4e7ed;
  position: relative;
}

/* 子分组样式 */
.child-item {
  margin: 4px 0;
  background: #fafafa;
  border: 1px solid #e4e7ed;
}

.child-item:hover {
  background: #f0f2f5;
  border-color: #409eff;
  transform: translateX(2px);
}

.child-item.active {
  background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
  color: white;
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.child-item.active .item-count {
  color: rgba(255, 255, 255, 0.9);
}

.child-item.active .child-dot {
  background: white;
}

/* 子节点圆点 */
.child-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #409eff;
  flex-shrink: 0;
}

.group-name {
  flex: 1;
  font-size: 14px;
}

.tree-item-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.child-item:hover .tree-item-actions {
  opacity: 1;
}

.item-count {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  min-width: 20px;
  text-align: center;
}

/* 动画 */
.slide-fade-enter-active {
  transition: all 0.3s ease;
}

.slide-fade-leave-active {
  transition: all 0.2s ease;
}

.slide-fade-enter-from {
  transform: translateY(-10px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateY(-5px);
  opacity: 0;
}
</style>
