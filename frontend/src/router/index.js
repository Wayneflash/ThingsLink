import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import MainLayout from '../layouts/MainLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '用户登录' }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/overview',
    children: [
      {
        path: 'overview',
        name: 'Overview',
        component: () => import('../views/Overview.vue'),
        meta: { title: '设备概览' }
      },
      {
        path: 'devices',
        name: 'Devices',
        component: () => import('../views/DeviceManagement.vue'),
        meta: { title: '设备管理' }
      },
      {
        path: 'devices/detail',
        name: 'DeviceDetail',
        component: () => import('../views/DeviceDetail.vue'),
        meta: { title: '设备详情' }
      },
      {
        path: 'alarms',
        name: 'Alarms',
        component: () => import('../views/AlarmLog.vue'),
        meta: { title: '报警日志' }
      },
      {
        path: 'alarm-rules',
        name: 'AlarmRules',
        component: () => import('../views/AlarmRules.vue'),
        meta: { title: '告警规则', noAuth: true } // 原型页面，免登录
      },
      {
        path: 'alarm-threshold',
        name: 'AlarmThresholdConfig',
        component: () => import('../views/AlarmThresholdConfig.vue'),
        meta: { title: '报警配置' }
      },
      {
        path: 'products',
        redirect: '/products/list',
        meta: { title: '产品管理' }
      },
      {
        path: 'products/list',
        name: 'ProductList',
        component: () => import('../views/ProductList.vue'),
        meta: { title: '产品列表' }
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('../views/ProductDetail.vue'),
        meta: { title: '产品详情' }
      },
      {
        path: 'products/attributes',
        name: 'ProductAttributes',
        component: () => import('../views/ProductAttributes.vue'),
        meta: { title: '产品属性' }
      },
      {
        path: 'groups',
        name: 'Groups',
        component: () => import('../views/DeviceGroups.vue'),
        meta: { title: '设备分组' }
      },
      {
        path: 'system',
        redirect: '/users',
        meta: { title: '系统管理' }
      },
      {
        path: 'system-settings',
        name: 'SystemSettings',
        component: () => import('../views/SystemSettings.vue'),
        meta: { title: '系统设置', adminOnly: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人资料' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/UserManagement.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('../views/RoleManagement.vue'),
        meta: { title: '角色管理' }
      },
      // 兼容旧路由
      {
        path: 'dashboard',
        redirect: '/overview'
      }
    ]
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：强制登录验证
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 设置页面标题（固定显示 ThingsLink）
  document.title = 'ThingsLink'
  
  // 原型页面，免登录
  if (to.meta.noAuth) {
    next()
    return
  }
  
  // admin权限检查
  if (to.meta.adminOnly) {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      try {
        const userInfo = JSON.parse(userInfoStr)
        if (userInfo.username !== 'admin') {
          // 非admin用户访问adminOnly页面，重定向到首页
          ElMessage.error('无权限访问此页面')
          next('/overview')
          return
        }
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
  }
  
  // 登录页逻辑
  if (to.path === '/login') {
    if (token) {
      // 已登录用户访问登录页，重定向到首页
      next('/overview')
    } else {
      next()
    }
    return
  }
  
  // 所有其他页面（包括根路径/）都需要登录验证
  if (!token) {
    // 未登录，重定向到登录页，并保存目标路径
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } else {
    next()
  }
})

export default router
