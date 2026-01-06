/**
 * 树形数据工具函数
 * 统一处理树形数据的扁平化、转换等操作
 */

/**
 * 扁平化树形数据为列表
 * @param {Array} tree - 树形数据
 * @returns {Array} 扁平化后的列表数据
 */
export function flattenTree(tree) {
  const result = []
  const flatten = (nodes) => {
    if (!Array.isArray(nodes)) return
    nodes.forEach(node => {
      result.push({
        id: node.id,
        name: node.name,
        parentId: node.parentId || 0,
        path: node.path,
        level: node.level,
        deviceCount: node.deviceCount || 0,
        desc: node.description || ''
      })
      if (node.children && node.children.length > 0) {
        flatten(node.children)
      }
    })
  }
  flatten(tree)
  return result
}

/**
 * 将列表数据转换为树形结构
 * @param {Array} list - 列表数据
 * @param {Object} options - 配置选项
 * @param {string} options.idKey - ID字段名，默认'id'
 * @param {string} options.parentKey - 父ID字段名，默认'parentId'
 * @param {string} options.childrenKey - 子节点字段名，默认'children'
 * @returns {Array} 树形数据
 */
export function listToTree(list, options = {}) {
  const {
    idKey = 'id',
    parentKey = 'parentId',
    childrenKey = 'children'
  } = options

  const map = {}
  const roots = []

  // 创建映射
  list.forEach(item => {
    map[item[idKey]] = { ...item, [childrenKey]: [] }
  })

  // 构建树
  list.forEach(item => {
    const node = map[item[idKey]]
    const parentId = item[parentKey]

    if (parentId && map[parentId]) {
      map[parentId][childrenKey].push(node)
    } else {
      roots.push(node)
    }
  })

  return roots
}

