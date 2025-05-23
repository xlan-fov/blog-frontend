import { defineStore } from 'pinia'
import { blogApi } from '@/api'
import { useUserStore } from '@/stores/user' // 添加用户存储导入

export const useBlogStore = defineStore('blog', {
  state: () => ({
    blogs: [], // 确保初始值是数组
    currentBlog: null,
    loading: false,
    searchKeyword: '',
    pagination: {
      page: 1,
      pageSize: 10,
      total: 0
    }
  }),

  getters: {
    // 确保返回数组
    blogList: (state) => Array.isArray(state.blogs) ? state.blogs : [],
    
    // 搜索过滤后的博客
    filteredBlogs: (state) => {
      const blogs = Array.isArray(state.blogs) ? state.blogs : []
      if (!state.searchKeyword) return blogs
      
      const keyword = state.searchKeyword.toLowerCase()
      return blogs.filter(blog => {
        if (!blog) return false
        const title = blog.title ? blog.title.toLowerCase() : ''
        const content = blog.content ? blog.content.toLowerCase() : ''
        return title.includes(keyword) || content.includes(keyword)
      })
    }
  },

  actions: {
    // 加载博客列表
    async loadBlogs(params = {}) {
      this.loading = true
      try {
        const response = await blogApi.getBlogs({
          page: this.pagination.page,
          pageSize: this.pagination.pageSize,
          ...params
        })
        
        // 确保响应数据格式正确
        if (response && response.data) {
          if (Array.isArray(response.data)) {
            this.blogs = response.data
          } else if (response.data.records && Array.isArray(response.data.records)) {
            this.blogs = response.data.records
            this.pagination.total = response.data.total || 0
          } else {
            console.warn('API返回的数据格式不正确:', response.data)
            this.blogs = []
          }
        } else {
          console.warn('API响应为空或格式错误:', response)
          this.blogs = []
        }
      } catch (error) {
        console.error('加载博客列表失败:', error)
        this.blogs = [] // 确保出错时也是数组
        throw error
      } finally {
        this.loading = false
      }
    },

    // 获取单个博客
    async getBlogById(id) {
      this.loading = true
      try {
        // 先从本地查找
        const localBlog = this.blogs.find(blog => blog.id === id || blog.id === parseInt(id))
        if (localBlog) {
          this.currentBlog = localBlog
          return localBlog
        }
        
        // 如果本地没有，从服务器获取
        const response = await blogApi.getBlogById(id)
        if (response && response.data) {
          this.currentBlog = response.data
          return response.data
        }
        return null
      } catch (error) {
        console.error('获取博客详情失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 创建博客
    async createBlog(blogData) {
      this.loading = true
      try {
        // 后端会从登录上下文中获取用户ID，不需要前端传递
        const response = await blogApi.createBlog(blogData)
        
        if (response && response.data) {
          // 将新博客添加到列表前端
          if (!Array.isArray(this.blogs)) {
            this.blogs = []
          }
          this.blogs.unshift(response.data)
        }
        
        return response
      } catch (error) {
        console.error('创建博客失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 更新博客
    async updateBlog(id, blogData) {
      this.loading = true
      try {
        const response = await blogApi.updateBlog(id, blogData)
        
        if (response && response.data) {
          // 更新本地列表中的博客
          if (Array.isArray(this.blogs)) {
            const index = this.blogs.findIndex(blog => blog.id === id || blog.id === parseInt(id))
            if (index !== -1) {
              this.blogs[index] = { ...this.blogs[index], ...response.data }
            }
          }
        }
        
        return response
      } catch (error) {
        console.error('更新博客失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 删除博客
    async deleteBlog(id) {
      this.loading = true
      try {
        await blogApi.deleteBlog(id)
        
        // 从本地列表中删除
        if (Array.isArray(this.blogs)) {
          this.blogs = this.blogs.filter(blog => blog.id !== id && blog.id !== parseInt(id))
        }
      } catch (error) {
        console.error('删除博客失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 发布博客
    async publishBlog(id) {
      try {
        const response = await blogApi.publishBlog(id)
        
        if (response && response.code === 200) {
          // 更新本地博客状态
          if (Array.isArray(this.blogs)) {
            const blog = this.blogs.find(blog => blog.id === id || blog.id === parseInt(id))
            if (blog) {
              blog.status = 'published'
            }
          }
        }
        
        return response
      } catch (error) {
        console.error('发布博客失败:', error)
        throw error
      }
    },

    // 撤回博客
    async withdrawBlog(id) {
      try {
        const response = await blogApi.withdrawBlog(id)
        
        if (response && response.code === 200) {
          // 更新本地博客状态
          if (Array.isArray(this.blogs)) {
            const blog = this.blogs.find(blog => blog.id === id || blog.id === parseInt(id))
            if (blog) {
              blog.status = 'draft'
            }
          }
        }
        
        return response
      } catch (error) {
        console.error('撤回博客失败:', error)
        throw error
      }
    },

    // 搜索博客
    async searchBlogs(keyword) {
      this.searchKeyword = keyword
      await this.loadBlogs({ keyword })
    },

    // 重置状态
    resetState() {
      this.blogs = []
      this.currentBlog = null
      this.searchKeyword = ''
      this.pagination = {
        page: 1,
        pageSize: 10,
        total: 0
      }
    }
  }
})