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


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 默认路由重定向到登录页面
    {
      path: '/',
      redirect: '/login'
    },
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
      path: '/dashboard',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/dashboard/blog'
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
    // 添加管理员登录的别名路由
    {
      path: '/admin/login',
      name: 'admin-login-alias',
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
    // 通配符路由，匹配所有未定义的路由
    {
      path: '/:pathMatch(.*)*',
      redirect: '/error/404'
    }
  ]
})

// 添加一个简单的路由守卫，确保默认访问登录页面
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isAuthenticated = userStore.isLoggedIn

  // 如果用户已登录且访问登录或注册页面，重定向到仪表板
  if (isAuthenticated && (to.path === '/login' || to.path === '/register')) {
    if (userStore.isAdmin()) {
      next('/admin')
    } else {
      next('/dashboard')
    }
    return
  }

  // 如果用户未登录且访问需要认证的页面，重定向到登录页面
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
    return
  }

  // 其他情况正常放行
  next()
})

export default router