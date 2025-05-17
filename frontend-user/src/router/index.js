import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'
import ContentManagement from '@/views/ContentManagement.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import BlogCreate from '@/views/BlogCreate.vue'
import BlogEdit from '@/views/BlogEdit.vue'
import BlogView from '@/views/BlogView.vue'
import ErrorPage from '@/views/ErrorPage.vue'
import Profile from '@/views/Profile.vue'
import { useUserStore } from '@/stores/user'

// 懒加载超管页面组件
const AdminLayout = () => import('@/components/AdminLayout.vue')
const AdminLogin = () => import('@/views/admin/AdminLogin.vue')
const AccountManagement = () => import('@/views/admin/AccountManagement.vue')
const BlogManagement = () => import('@/views/admin/BlogManagement.vue')
const AnomalyDetection = () => import('@/views/admin/AnomalyDetection.vue')
const AdminProfile = () => import('@/views/admin/AdminProfile.vue')

// 获取管理员路径（从环境变量中读取，增加安全性）
const ADMIN_AUTH_PATH = import.meta.env.ADMIN_PATH || 'senti-admin-auth'

// 添加测试路由
const TestLogin = () => import('@/views/TestLogin.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login,
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'register',
      component: Register,
      meta: { requiresAuth: false }
    },
    {
      path: '/error/:type',
      name: 'error',
      component: ErrorPage,
      meta: { requiresAuth: false }
    },
    // 普通用户路由
    {
      path: '/',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/blog'
        },
        {
          path: 'blog',
          name: 'blog',
          component: ContentManagement,
          meta: { requiresAuth: true }
        },
        {
          path: 'blog/create',
          name: 'blog-create',
          component: BlogCreate,
          meta: { requiresAuth: true }
        },
        {
          path: 'blog/edit/:id',
          name: 'blog-edit',
          component: BlogEdit,
          meta: { requiresAuth: true }
        },
        {
          path: 'blog/view/:id',
          name: 'blog-view',
          component: BlogView,
          meta: { requiresAuth: true }
        },
        {
          path: 'profile',
          name: 'profile',
          component: Profile,
          meta: { requiresAuth: true }
        }
      ]
    },
    // 超管登录入口（使用一个不易猜测的路径）
    {
      path: `/${ADMIN_AUTH_PATH}`,
      name: 'admin-login',
      component: AdminLogin,
      meta: { requiresAuth: false }
    },
    // 超管路由
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          redirect: '/admin/blog-management'
        },
        {
          path: 'blog-management',
          name: 'admin-blog-management',
          component: BlogManagement,
          meta: { requiresAuth: true, requiresAdmin: true }
        },
        {
          path: 'account-management',
          name: 'account-management',
          component: AccountManagement,
          meta: { requiresAuth: true, requiresAdmin: true }
        },
        {
          path: 'anomaly-detection',
          name: 'anomaly-detection',
          component: AnomalyDetection,
          meta: { requiresAuth: true, requiresAdmin: true }
        },
        {
          path: 'profile',
          name: 'admin-profile',
          component: AdminProfile,
          meta: { requiresAuth: true, requiresAdmin: true }
        }
      ]
    },
    // 添加测试登录路由
    {
      path: '/test-login',
      name: 'test-login',
      component: TestLogin,
      meta: { requiresAuth: false }
    },
    // 通配符路由，匹配所有未定义的路由
    {
      path: '/:pathMatch(.*)*',
      redirect: '/error/404'
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isAuthenticated = userStore.isLoggedIn
  const isAdmin = userStore.isAdmin()
  const isBanned = userStore.isBanned()

  // 如果账号被封禁，强制登出并跳转到登录页
  if (isAuthenticated && isBanned && to.path !== '/login') {
    userStore.logout()
    next({
      path: '/login',
      query: { banned: true }
    })
    return
  }

  // 如果访问需要管理员权限的页面但不是管理员
  if (to.meta.requiresAdmin && !isAdmin) {
    next('/error/403') // 重定向到403错误页
    return
  }

  // 防止普通用户访问管理员登录页面 (可选，防止通过直接输入URL访问)
  if (to.path === `/${ADMIN_AUTH_PATH}` && isAuthenticated && !isAdmin) {
    next('/error/403')
    return
  }

  // 如果访问需要认证的页面但未登录
  if (to.meta.requiresAuth && !isAuthenticated) {
    // 保存当前要访问的路径，登录后可以跳转回来
    const redirectPath = to.fullPath

    // 如果尝试访问管理员页面，重定向到管理员登录页
    if (to.meta.requiresAdmin) {
      next({
        path: `/${ADMIN_AUTH_PATH}`,
        query: { redirect: redirectPath }
      })
    } else {
      next({
        path: '/login',
        query: { redirect: redirectPath }
      })
    }
  }
  // 如果已登录但访问登录或注册页面
  else if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
    // 根据角色重定向到相应首页
    next(isAdmin ? '/admin' : '/')
  }
  // 如果已登录管理员访问管理员登录页
  else if (to.path === `/${ADMIN_AUTH_PATH}` && isAuthenticated && isAdmin) {
    next('/admin')
  }
  // 其他情况正常放行
  else {
    next()
  }
})

export default router