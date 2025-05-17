import { defineStore } from 'pinia'
import { ref } from 'vue'
import blogApi from '@/api/blog'
import { ElMessage } from 'element-plus'

// 格式化时间的函数
function formatDate(date) {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

export const useBlogStore = defineStore('blog', () => {
  const blogs = ref([])
  const loading = ref(false)
  const searchKeyword = ref('')

  // 获取博客列表
  async function fetchBlogs() {
    loading.value = true
    try {
      const res = await blogApi.getBlogs()
      if (res.code === 200 && res.data) {
        blogs.value = res.data
      } else {
        ElMessage.error(res.message || '获取博客列表失败')
        // 如果API调用失败，保留使用模拟数据以避免UI空白
        blogs.value = getMockBlogs()
      }
    } catch (error) {
      console.error('获取博客列表失败:', error)
      blogs.value = getMockBlogs()
    } finally {
      loading.value = false
    }
  }

  // 搜索博客
  async function searchBlogs(keyword) {
    searchKeyword.value = keyword
    loading.value = true
    try {
      if (!keyword) {
        return await fetchBlogs()
      }
      
      const res = await blogApi.searchBlogs({ keyword })
      if (res.code === 200 && res.data) {
        blogs.value = res.data
      } else {
        ElMessage.error(res.message || '搜索博客失败')
      }
    } catch (error) {
      console.error('搜索博客失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 创建博客
  async function createBlog(blogData) {
    try {
      const res = await blogApi.createBlog(blogData)
      if (res.code === 200 && res.data) {
        // 添加新博客到列表顶部
        blogs.value = [res.data, ...blogs.value]
        return res.data
      } else {
        ElMessage.error(res.message || '创建博客失败')
        throw new Error(res.message || '创建博客失败')
      }
    } catch (error) {
      console.error('创建博客失败:', error)
      throw error
    }
  }

  // 获取单个博客
  async function getBlogById(id) {
    try {
      // 先从本地已加载的博客中查找
      const localBlog = blogs.value.find(b => b.id === id)
      if (localBlog) {
        return localBlog
      }
      
      // 如果本地没有，则从服务器获取
      const res = await blogApi.getBlogById(id)
      if (res.code === 200 && res.data) {
        return res.data
      } else {
        ElMessage.error(res.message || '获取博客详情失败')
        throw new Error(res.message || '获取博客详情失败')
      }
    } catch (error) {
      console.error('获取博客失败:', error)
      throw error
    }
  }

  // 更新博客
  async function updateBlog(id, blogData) {
    try {
      const res = await blogApi.updateBlog(id, blogData)
      if (res.code === 200 && res.data) {
        // 更新本地列表中的博客
        const index = blogs.value.findIndex(blog => blog.id === id)
        if (index !== -1) {
          blogs.value[index] = {
            ...blogs.value[index],
            ...res.data,
            updatedAt: res.data.updatedAt || formatDate(new Date())
          }
        }
        return res.data
      } else {
        ElMessage.error(res.message || '更新博客失败')
        throw new Error(res.message || '更新博客失败')
      }
    } catch (error) {
      console.error('更新博客失败:', error)
      throw error
    }
  }

  // 删除博客
  async function deleteBlog(id) {
    try {
      const res = await blogApi.deleteBlog(id)
      if (res.code === 200) {
        // 从本地列表中移除博客
        blogs.value = blogs.value.filter(blog => blog.id !== id)
        return true
      } else {
        ElMessage.error(res.message || '删除博客失败')
        throw new Error(res.message || '删除博客失败')
      }
    } catch (error) {
      console.error('删除博客失败:', error)
      throw error
    }
  }

  // 发布博客
  async function publishBlog(id) {
    try {
      const res = await blogApi.publishBlog(id)
      if (res.code === 200 && res.data) {
        // 更新本地列表中的博客状态
        const index = blogs.value.findIndex(blog => blog.id === id)
        if (index !== -1) {
          blogs.value[index] = {
            ...blogs.value[index],
            ...res.data,
            status: 'published',
            updatedAt: res.data.updatedAt || formatDate(new Date())
          }
        }
        return true
      } else {
        ElMessage.error(res.message || '发布博客失败')
        throw new Error(res.message || '发布博客失败')
      }
    } catch (error) {
      console.error('发布博客失败:', error)
      throw error
    }
  }

  // 撤回博客
  async function withdrawBlog(id) {
    try {
      const res = await blogApi.withdrawBlog(id)
      if (res.code === 200 && res.data) {
        // 更新本地列表中的博客状态
        const index = blogs.value.findIndex(blog => blog.id === id)
        if (index !== -1) {
          blogs.value[index] = {
            ...blogs.value[index],
            ...res.data,
            status: 'draft',
            updatedAt: res.data.updatedAt || formatDate(new Date())
          }
        }
        return true
      } else {
        ElMessage.error(res.message || '撤回博客失败')
        throw new Error(res.message || '撤回博客失败')
      }
    } catch (error) {
      console.error('撤回博客失败:', error)
      throw error
    }
  }

  // 获取模拟博客数据（API调用失败时使用）
  function getMockBlogs() {
    return [
      {
        id: '1',
        title: 'Vue3 组合式API最佳实践',
        content: 'Vue3的组合式API为我们提供了更灵活的代码组织方式，本文将详细介绍如何在实际项目中更好地使用组合式API...',
        status: 'published',
        updatedAt: '2024-05-08 14:30:00'
      },
      {
        id: '2',
        title: 'Element Plus 组件库使用指南',
        content: 'Element Plus是一个基于Vue3的组件库，本文将从基础组件到高级组件，全面介绍其使用方法...',
        status: 'published',
        updatedAt: '2024-05-08 13:20:00'
      },
      {
        id: '3',
        title: '前端性能优化实战',
        content: '本文将分享一些实用的前端性能优化技巧，包括代码分割、懒加载、缓存策略等...',
        status: 'draft',
        updatedAt: '2024-05-08 12:15:00'
      },
      {
        id: '4',
        title: 'TypeScript 高级类型详解',
        content: '深入理解TypeScript的高级类型系统，包括泛型、条件类型、映射类型等概念...',
        status: 'published',
        updatedAt: '2024-05-08 11:30:00'
      },
      {
        id: '5',
        title: 'Vite 构建工具配置指南',
        content: '详细介绍Vite的配置选项，以及如何根据项目需求进行优化...',
        status: 'draft',
        updatedAt: '2024-05-08 10:45:00'
      },
      {
        id: '6',
        title: 'Pinia 状态管理最佳实践',
        content: '探讨Pinia在Vue3项目中的使用方式，以及如何组织和管理状态...',
        status: 'published',
        updatedAt: '2024-05-08 09:30:00'
      },
      {
        id: '7',
        title: 'Vue Router 4.x 新特性介绍',
        content: 'Vue Router 4.x带来了许多新特性，本文将详细介绍这些变化...',
        status: 'draft',
        updatedAt: '2024-05-07 16:20:00'
      },
      {
        id: '8',
        title: '响应式设计原则与实践',
        content: '探讨响应式设计的基本原则，以及如何在不同设备上提供最佳用户体验...',
        status: 'published',
        updatedAt: '2024-05-07 15:10:00'
      },
      {
        id: '9',
        title: 'CSS Grid 布局详解',
        content: '深入理解CSS Grid布局系统，掌握其强大的布局能力...',
        status: 'draft',
        updatedAt: '2024-05-07 14:00:00'
      },
      {
        id: '10',
        title: '前端工程化实践',
        content: '分享前端工程化的实践经验，包括项目结构、构建流程、自动化部署等...',
        status: 'published',
        updatedAt: '2024-05-07 13:00:00'
      },
      {
        id: '11',
        title: 'Vue3 性能优化技巧',
        content: '介绍Vue3中的性能优化方法，包括组件优化、渲染优化等...',
        status: 'draft',
        updatedAt: '2024-05-07 12:00:00'
      },
      {
        id: '12',
        title: '前端安全最佳实践',
        content: '探讨前端开发中的安全问题，以及如何防范常见的安全威胁...',
        status: 'published',
        updatedAt: '2024-05-07 11:00:00'
      },
      {
        id: '13',
        title: 'Webpack 5 新特性解析',
        content: '详细介绍Webpack 5的新特性和改进，以及如何迁移到新版本...',
        status: 'draft',
        updatedAt: '2024-05-07 10:00:00'
      },
      {
        id: '14',
        title: '前端测试策略',
        content: '探讨前端测试的方法和工具，包括单元测试、集成测试和端到端测试...',
        status: 'published',
        updatedAt: '2024-05-07 09:00:00'
      },
      {
        id: '15',
        title: 'CSS 预处理器对比',
        content: '对比Sass、Less、Stylus等CSS预处理器的特点和使用场景...',
        status: 'draft',
        updatedAt: '2024-05-06 16:00:00'
      },
      {
        id: '16',
        title: '前端监控系统搭建',
        content: '介绍如何搭建前端监控系统，包括性能监控、错误监控等...',
        status: 'published',
        updatedAt: '2024-05-06 15:00:00'
      },
      {
        id: '17',
        title: '微前端架构实践',
        content: '探讨微前端架构的设计和实现，以及如何解决跨应用通信等问题...',
        status: 'draft',
        updatedAt: '2024-05-06 14:00:00'
      },
      {
        id: '18',
        title: '前端代码规范指南',
        content: '制定前端代码规范，提高代码质量和可维护性...',
        status: 'published',
        updatedAt: '2024-05-06 13:00:00'
      },
      {
        id: '19',
        title: '浏览器渲染原理',
        content: '深入理解浏览器渲染过程，掌握页面性能优化的关键点...',
        status: 'draft',
        updatedAt: '2024-05-06 12:00:00'
      },
      {
        id: '20',
        title: '前端国际化解决方案',
        content: '介绍前端国际化的实现方案，包括多语言切换、日期时间处理等...',
        status: 'published',
        updatedAt: '2024-05-06 11:00:00'
      }
    ]
  }

  return {
    blogs,
    loading,
    searchKeyword,
    fetchBlogs,
    searchBlogs,
    createBlog,
    getBlogById,
    updateBlog,
    deleteBlog,
    publishBlog,
    withdrawBlog
  }
})