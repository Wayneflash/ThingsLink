/**
 * SVG图标映射 - 将Element Plus图标转换为SVG
 * 所有图标都是24x24 viewBox
 */

export const Icons = {
  // 连接/网络（两个相互交错的圆角矩形，链接符号）
  Connection: {
    viewBox: '0 0 24 24',
    path: '<rect x="4" y="6" width="12" height="8" rx="2" ry="2"></rect><rect x="8" y="10" width="12" height="8" rx="2" ry="2"></rect>'
  },
  
  // 用户
  User: {
    viewBox: '0 0 24 24',
    path: '<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle>'
  },
  
  // 锁定
  Lock: {
    viewBox: '0 0 24 24',
    path: '<rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path>'
  },
  
  // 显示器/设备
  Monitor: {
    viewBox: '0 0 24 24',
    path: '<rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line>'
  },
  
  // 圆形对勾
  CircleCheck: {
    viewBox: '0 0 24 24',
    path: '<circle cx="12" cy="12" r="10"></circle><polyline points="9 12 11 14 15 10"></polyline>'
  },
  
  // 铃铛
  Bell: {
    viewBox: '0 0 24 24',
    path: '<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path>'
  },
  
  // 填充铃铛
  BellFilled: {
    viewBox: '0 0 24 24',
    path: '<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path>'
  },
  
  // 数据线/图表
  DataLine: {
    viewBox: '0 0 24 24',
    path: '<polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>'
  },
  
  // 警告填充
  WarningFilled: {
    viewBox: '0 0 24 24',
    path: '<path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line>'
  },
  
  // 警告
  Warning: {
    viewBox: '0 0 24 24',
    path: '<path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line>'
  },
  
  // 信息填充
  InfoFilled: {
    viewBox: '0 0 24 24',
    path: '<circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line>'
  },
  
  // 搜索
  Search: {
    viewBox: '0 0 24 24',
    path: '<circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path>'
  },
  
  // 刷新
  Refresh: {
    viewBox: '0 0 24 24',
    path: '<path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"></path><path d="M21 3v5h-5"></path><path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"></path><path d="M3 21v-5h5"></path>'
  },
  
  // 首页填充
  HomeFilled: {
    viewBox: '0 0 24 24',
    path: '<path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline>'
  },
  
  // 左箭头
  ArrowLeft: {
    viewBox: '0 0 24 24',
    path: '<line x1="19" y1="12" x2="5" y2="12"></line><polyline points="12 19 5 12 12 5"></polyline>'
  },
  
  // 右箭头
  ArrowRight: {
    viewBox: '0 0 24 24',
    path: '<line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline>'
  },
  
  // 设置
  Setting: {
    viewBox: '0 0 24 24',
    path: '<circle cx="12" cy="12" r="3"></circle><path d="M12 1v6m0 6v6M5.64 5.64l4.24 4.24m4.24 4.24l4.24 4.24M1 12h6m6 0h6M5.64 18.36l4.24-4.24m4.24-4.24l4.24-4.24"></path>'
  },
  
  // 时钟
  Clock: {
    viewBox: '0 0 24 24',
    path: '<circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline>'
  },
  
  // 数据分析
  DataAnalysis: {
    viewBox: '0 0 24 24',
    path: '<path d="M3 3v18h18"></path><path d="M7 12l4-4 4 4 6-6"></path>'
  },
  
  // 文档
  Document: {
    viewBox: '0 0 24 24',
    path: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline>'
  },
  
  // 眼睛（显示密码）
  Eye: {
    viewBox: '0 0 24 24',
    path: '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle>'
  },
  
  // 眼睛关闭（隐藏密码）
  EyeOff: {
    viewBox: '0 0 24 24',
    path: '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line>'
  },
  
  // 圆形关闭
  CircleClose: {
    viewBox: '0 0 24 24',
    path: '<circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line>'
  },
  
  // 加号
  Plus: {
    viewBox: '0 0 24 24',
    path: '<line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line>'
  },
  
  // 闪电（命令控制）
  Lightning: {
    viewBox: '0 0 24 24',
    path: '<polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>'
  }
}

/**
 * 渲染SVG图标组件
 */
export function renderIcon(iconName, size = 24, color = 'currentColor') {
  const icon = Icons[iconName]
  if (!icon) {
    console.warn(`Icon ${iconName} not found`)
    return ''
  }
  
  return `<svg width="${size}" height="${size}" viewBox="${icon.viewBox || '0 0 24 24'}" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">${icon.path}</svg>`
}

export default Icons
