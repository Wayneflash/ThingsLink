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
    
    <!-- 子分组（递归渲染所有层级） -->
    <transition name="slide-fade">
      <div v-show="expanded" class="tree-children">
        <TreeNode
          v-for="group in childGroups"
          :key="group.id"
          :group="group"
          :all-groups="groups"
          :current-group-id="currentGroupId"
          :show-actions="showActions"
          :show-count="showCount"
          :count-data="countData"
          @select="handleSelect"
          @edit="(g) => $emit('edit', g)"
          @delete="(g) => $emit('delete', g)"
        />
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, defineComponent, h } from 'vue'
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

// 总分组（parentId=0或null的顶级分组，如果有多个则取第一个）
const rootGroup = computed(() => {
  const roots = props.groups.filter(g => g.parentId === 0 || g.parentId === null)
  console.log('GroupTree - 分组数据:', JSON.stringify(props.groups, null, 2))
  console.log('GroupTree - 根节点:', JSON.stringify(roots, null, 2))
  const root = roots.length > 0 ? roots[0] : null
  console.log('GroupTree - 最终根节点:', root)
  console.log('GroupTree - 根节点名称:', root?.name)
  return root
})

// 子分组（父分组是总分组的子节点）
const childGroups = computed(() => {
  const root = rootGroup.value
  if (!root) return []
  return props.groups.filter(g => g.parentId === root.id)
})

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

// 递归树节点组件
const TreeNode = defineComponent({
  name: 'TreeNode',
  props: {
    group: {
      type: Object,
      required: true
    },
    allGroups: {
      type: Array,
      default: () => []
    },
    currentGroupId: {
      type: Number,
      default: null
    },
    showActions: {
      type: Boolean,
      default: true
    },
    showCount: {
      type: Boolean,
      default: false
    },
    countData: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ['select', 'edit', 'delete'],
  setup(props, { emit }) {
    const nodeExpanded = ref(false)
    
    // 获取子分组
    const children = computed(() => {
      return props.allGroups.filter(g => g.parentId === props.group.id)
    })
    
    // 切换展开/收起
    const toggleNodeExpand = (e) => {
      e.stopPropagation()
      nodeExpanded.value = !nodeExpanded.value
    }
    
    // 选择分组
    const handleNodeSelect = () => {
      emit('select', props.group.id)
    }
    
    // 获取计数
    const getCount = (groupId) => {
      return props.countData[groupId] || 0
    }
    
    return {
      nodeExpanded,
      children,
      toggleNodeExpand,
      handleNodeSelect,
      getCount
    }
  },
  render() {
    const { group, nodeExpanded, children, showActions, showCount, countData } = this
    
    // 渲染子节点
    const renderChildren = () => {
      if (!nodeExpanded || children.length === 0) return null
      
      return h('div', { class: 'tree-children' },
        children.map(child =>
          h(TreeNode, {
            key: child.id,
            group: child,
            allGroups: this.allGroups,
            currentGroupId: this.currentGroupId,
            showActions,
            showCount,
            countData,
            onSelect: (id) => this.$emit('select', id),
            onEdit: (g) => this.$emit('edit', g),
            onDelete: (g) => this.$emit('delete', g)
          })
        )
      )
    }
    
    return h('div', { 
      class: ['tree-item', 'child-item', { active: this.currentGroupId === group.id }],
      onClick: (e) => {
        // 防止事件冒泡
        if (e.target.tagName !== 'BUTTON' && !e.target.closest('.tree-item-actions')) {
          this.handleNodeSelect()
        }
      }
    }, [
      // 左侧内容
      h('div', { 
        class: ['tree-item-left', { active: this.currentGroupId === group.id }],
      }, [
        // 展开/收起图标
        children.length > 0 ? h('el-icon', {
          class: ['expand-icon', { expanded: nodeExpanded }],
          onClick: this.toggleNodeExpand
        }, [h(ArrowRight)]) : h('span', { class: 'child-dot' }),
        // 分组名称
        h('span', { class: 'group-name' }, group.name)
      ]),
      // 右侧内容（直接显示按钮）
      h('div', { class: 'tree-item-right' }, [
        showActions ? h('div', { 
          class: 'tree-item-actions',
          style: 'display: flex; gap: 8px;'
        }, [
          h('el-button', {
            size: 'small',
            type: 'primary',
            text: true,
            onClick: (e) => {
              e.stopPropagation()
              this.$emit('edit', group)
            }
          }, '✏️ 编辑'),
          h('el-button', {
            size: 'small',
            type: 'danger',
            text: true,
            onClick: (e) => {
              e.stopPropagation()
              this.$emit('delete', group)
            }
          }, '🗑️ 删除')
        ]) : null
      ]),
      // 子节点
      renderChildren()
    ])
  }
})
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
  background: #f5f7fa;
  color: #303133;
  font-weight: 500;
  margin-bottom: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.root-item:hover {
  background: #e8eaf0;
  border-color: #c0c4cc;
}

.root-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.root-item .item-count {
  color: #909399;
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.root-item.active .item-count {
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.2);
}

/* 展开图标 */
.expand-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
  color: #606266;
}

.root-item.active .expand-icon {
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
  gap: 8px;
  opacity: 1;
  transition: opacity 0.2s;
  margin-left: 12px;
}

.tree-item-actions :deep(.el-button) {
  font-size: 13px;
  padding: 5px 12px;
  margin: 0;
  border-radius: 4px;
  font-weight: 500;
  transition: all 0.2s;
  white-space: nowrap;
}

.tree-item-actions :deep(.el-button--primary) {
  color: #409eff;
  background: transparent;
}

.tree-item-actions :deep(.el-button--primary:hover) {
  color: #fff;
  background: #409eff;
}

.tree-item-actions :deep(.el-button--danger) {
  color: #f56c6c;
  background: transparent;
}

.tree-item-actions :deep(.el-button--danger:hover) {
  color: #fff;
  background: #f56c6c;
}

.child-item:hover .tree-item-actions {
  opacity: 1;
}

.tree-item:hover .tree-item-actions {
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
