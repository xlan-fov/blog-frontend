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
  
  // 如果访问需要认证的页面但未登录
  if (to.meta.requiresAuth && !isAuthenticated) {
    // 保存当前要访问的路径，登录后可以跳转回来
    const redirectPath = to.fullPath
    next({
      path: '/login',
      query: { redirect: redirectPath }
    })
  } 
  // 如果已登录但访问登录或注册页面
  else if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
    next('/')
  }
  // 其他情况正常放行
  else {
    next()
  }
})

export default router 