// 这是Vue Router的配置文件，用于设置应用的路由(不同URL对应不同页面)

// 从vue-router库导入创建路由器和历史模式的函数
import { createRouter, createWebHistory } from 'vue-router'
// 导入各个页面组件
import ArticleList       from '@/views/ArticleList.vue'
import ArticleDetail     from '@/views/ArticleDetail.vue'
import Register          from '@/views/Register.vue'
import Login             from '@/views/Login.vue'
import Profile           from '@/views/Profile.vue'
import Editor            from '@/views/Editor.vue'
import AdminUserList     from '@/views/AdminUserList.vue'
import AdminArticleAudit from '@/views/AdminArticleAudit.vue'
import AdminLayout from '../layout/AdminLayout.vue'
import UserLayout from '../layout/UserLayout.vue'
import AdminArticleList from '../views/admin/ArticleList.vue'
import UserHome from '../views/user/UserHome.vue'

// 导入布局和页面组件
import Layout from '../layout/Layout.vue'

// 定义路由配置数组，每个对象对应一个路由
const routes = [
  // 根路径直接重定向到登录页
  { 
    path: '/',           
    name: 'Root',
    redirect: { name: 'Login' }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      {
        path: 'articles',
        name: 'AdminArticleList',
        component: () => import('../views/admin/ArticleList.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      },
      {
        path: 'accounts',
        name: 'AdminAccountManage',
        component: () => import('../views/admin/AccountManage.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      },
      {
        path: 'anomaly',
        name: 'AnomalyDetection',
        component: () => import('../views/admin/AnomalyDetection.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      },
      // 默认重定向到文章列表
      {
        path: '',
        redirect: { name: 'AdminArticleList' }
      }
    ]
  },
  {
    path: '/articles',
    component: Layout,
    redirect: '/articles',
    children: [
      {
        path: '',
        name: 'ArticleList',
        component: ArticleList,
        meta: { requiresAuth: true }
      },
      // 可以添加更多需要布局的路由...
    ]
  },
  // 文章详情路由，:id是动态参数，会根据实际URL中的值变化
  { path: '/articles/:id', component: ArticleDetail,   name: 'ArticleDetail' },
  // 注册页面路由
  { path: '/register',   component: Register,          name: 'Register'      },
  // 登录页面路由
  { path: '/login',      component: Login,             name: 'Login'         },
  // 个人资料页面路由
  { path: '/profile',    component: Profile,           name: 'Profile'       },
  // 编辑器路由，:id?表示id参数是可选的
  { path: '/editor/:id?', component: Editor,           name: 'Editor'        },
  // 管理员用户列表路由
  { path: '/admin/users', component: AdminUserList,    name: 'AdminUserList' },
  // 管理员文章审核路由
  { path: '/admin/articles', component: AdminArticleAudit, name: 'AdminArticleAudit' },
  // 普通用户路由
  {
    path: '/user',
    component: () => import('../layout/UserLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: { name: 'UserContentManage' }
      },
      {
        path: 'content',
        name: 'UserContentManage',
        component: () => import('../views/user/ContentManage.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'editor/:id',
        name: 'BlogEditor',
        component: () => import('../views/user/BlogEditor.vue'),
        meta: { requiresAuth: true }
      }
      // 暂时注释掉博客详情页路由，等组件开发好后再启用
      // {
      //   path: 'blog/:id',
      //   name: 'BlogDetail',
      //   component: () => import('../views/user/BlogDetail.vue'),
      //   meta: { requiresAuth: true }
      // }
    ]
  },
  // 通配符路由，匹配所有未定义的路径，重定向到登录页
  { 
    path: '/:pathMatch(.*)*', 
    redirect: { name: 'Login' } 
  }
]

// 创建路由器实例
const router = createRouter({
  // 使用历史模式而不是哈希模式，这样URL中就不会包含#符号
  history: createWebHistory(),
  // 应用上面定义的路由配置
  routes
})

// 全局前置守卫，检查登录状态和权限
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')
  
  // 登录页和注册页不需要验证token
  if (to.name === 'Login' || to.name === 'Register' || !to.meta.requiresAuth) {
    next()
    return
  }
  
  // 其他页面需要验证token
  if (!token) {
    // 没有token，重定向到登录页
    next({ name: 'Login' })
  } else if (to.meta.role && to.meta.role !== userRole) {
    // 需要特定角色但角色不匹配
    if (userRole === 'admin') {
      next({ name: 'AdminArticleList' })
    } else {
      next({ name: 'UserContentManage' })
    }
  } else {
    // 通过验证
    next()
  }
})

export default router
