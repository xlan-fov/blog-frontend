import { defineStore } from 'pinia'
import { ref } from 'vue'
import blogApi from '@/api/blog'

export const useBlogStore = defineStore('blog', () => {
  const blogs = ref([])
  const totalBlogs = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const loading = ref(false)
  const searchKeyword = ref('')
  const currentBlog = ref(null)
  const autoSaveTimer = ref(null)
  const lastSavedContent = ref('')

  /**
   * 加载博客列表
   * @param {Object} params - 额外的查询参数
   */
  async function getBlogs(params = {}) {
    loading.value = true
    try {
      const response = await blogApi.getBlogs({
        page: currentPage.value,
        pageSize: pageSize.value,
        ...params
      })

      if (response?.code === 200 && response.data) {
        if (Array.isArray(response.data)) {
          blogs.value = response.data
          totalBlogs.value = response.data.length
        } else if (response.data.records && Array.isArray(response.data.records)) {
          blogs.value = response.data.records
          totalBlogs.value = response.data.total || 0
          currentPage.value = response.data.current || 1
          pageSize.value = response.data.size || 10
        } else {
          console.warn('API返回的数据格式不正确:', response.data)
          blogs.value = []
          totalBlogs.value = 0
        }
      } else {
        console.warn('API响应为空或格式错误:', response)
        blogs.value = []
        totalBlogs.value = 0
      }
    } catch (error) {
      console.error('加载博客列表失败:', error)
      blogs.value = []
      totalBlogs.value = 0
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取单个博客
   * @param {number|string} id - 博客ID
   */
  async function getBlogById(id) {
    loading.value = true
    try {
      // 先从本地查找
      const localBlog = blogs.value.find(blog => blog.id === id || blog.id === parseInt(id))
      if (localBlog) {
        currentBlog.value = localBlog
        return localBlog
      }

      // 如果本地没有，从服务器获取
      const response = await blogApi.getBlogById(id)
      if (response?.code === 200 && response.data) {
        currentBlog.value = response.data
        return response.data
      }
      return null
    } catch (error) {
      console.error('获取博客详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建新博客
   * @param {Object} blogData - 博客数据
   */
  async function createBlog(blogData) {
    loading.value = true
    try {
      const response = await blogApi.createBlog({
        ...blogData,
        status: blogData.status || 'draft'
      })

      if (response?.code === 200 && response.data) {
        if (!Array.isArray(blogs.value)) {
          blogs.value = []
        }
        blogs.value.unshift(response.data)
        currentBlog.value = response.data
        return response.data
      }
      throw new Error(response?.message || '创建博客失败')
    } catch (error) {
      console.error('创建博客失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新现有博客
   * @param {number|string} id - 博客ID
   * @param {Object} blogData - 更新的博客数据
   */
  async function updateBlog(id, blogData) {
    loading.value = true
    try {
      const response = await blogApi.updateBlog(id, {
        ...blogData,
        status: blogData.status || 'draft'
      })

      if (response?.code === 200 && response.data) {
        if (Array.isArray(blogs.value)) {
          const index = blogs.value.findIndex(blog => blog.id === id || blog.id === parseInt(id))
          if (index !== -1) {
            blogs.value[index] = { ...blogs.value[index], ...response.data }
          }
        }
        if (currentBlog.value?.id === id) {
          currentBlog.value = { ...currentBlog.value, ...response.data }
        }
        return response.data
      }
      throw new Error(response?.message || '更新博客失败')
    } catch (error) {
      console.error('更新博客失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除博客
   * @param {number|string} id - 博客ID
   */
  async function deleteBlog(id) {
    loading.value = true
    try {
      const response = await blogApi.deleteBlog(id)
      if (response?.code === 200) {
        blogs.value = blogs.value.filter(blog => blog.id !== id && blog.id !== parseInt(id))
        if (currentBlog.value?.id === id) {
          currentBlog.value = null
        }
        return true
      }
      throw new Error(response?.message || '删除博客失败')
    } catch (error) {
      console.error('删除博客失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 发布博客
   * @param {Object} blogData - 博客数据
   */
  async function publishBlog(blogData) {
    try {
      const data = {
        ...blogData,
        status: 'published',
        publishedAt: new Date().toISOString()
      }

      if (!data.id) {
        return await createBlog(data)
      } else {
        return await updateBlog(data.id.toString(), data)
      }
    } catch (error) {
      console.error('发布博客失败:', error)
      throw error
    }
  }

  /**
   * 保存草稿
   * @param {Object} blogData - 博客数据
   */
  async function saveDraft(blogData) {
    try {
      const data = {
        ...blogData,
        status: 'draft'
      }

      if (!data.id) {
        return await createBlog(data)
      } else {
        return await updateBlog(data.id, data)
      }
    } catch (error) {
      console.error('保存草稿失败:', error)
      throw error
    }
  }

  /**
   * 自动保存
   * @param {Object} blogData - 博客数据
   * @param {number} delay - 延迟时间（毫秒）
   */
  function startAutoSave(blogData, delay = 30000) {
    if (autoSaveTimer.value) {
      clearTimeout(autoSaveTimer.value)
    }

    autoSaveTimer.value = setTimeout(async () => {
      if (blogData.content !== lastSavedContent.value) {
        try {
          await saveDraft(blogData)
          lastSavedContent.value = blogData.content
        } catch (error) {
          console.error('自动保存失败:', error)
        }
      }
    }, delay)
  }

  /**
   * 停止自动保存
   */
  function stopAutoSave() {
    if (autoSaveTimer.value) {
      clearTimeout(autoSaveTimer.value)
      autoSaveTimer.value = null
    }
  }

  /**
   * 搜索博客
   * @param {string} keyword - 搜索关键词
   */
  async function searchBlogs(keyword) {
    searchKeyword.value = keyword
    await getBlogs({ keyword })
  }

  /**
   * 重置状态
   */
  function resetState() {
    blogs.value = []
    totalBlogs.value = 0
    currentPage.value = 1
    pageSize.value = 10
    loading.value = false
    searchKeyword.value = ''
    currentBlog.value = null
    stopAutoSave()
    lastSavedContent.value = ''
  }

  /**
   * 撤回博客
   * @param {number|string} id - 博客ID
   */
  async function withdrawBlog(id) {
    loading.value = true
    try {
      const response = await blogApi.withdrawBlog(id)
      if (response?.code === 200) {
        // 更新本地博客状态
        if (Array.isArray(blogs.value)) {
          const index = blogs.value.findIndex(blog => blog.id === id || blog.id === parseInt(id))
          if (index !== -1) {
            blogs.value[index] = { ...blogs.value[index], status: 'draft' }
          }
        }
        if (currentBlog.value?.id === id) {
          currentBlog.value = { ...currentBlog.value, status: 'draft' }
        }
        return true
      }
      throw new Error(response?.message || '撤回博客失败')
    } catch (error) {
      console.error('撤回博客失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  return {
    blogs,
    totalBlogs,
    currentPage,
    pageSize,
    loading,
    searchKeyword,
    currentBlog,
    getBlogs,
    getBlogById,
    createBlog,
    updateBlog,
    deleteBlog,
    publishBlog,
    withdrawBlog,
    saveDraft,
    startAutoSave,
    stopAutoSave,
    searchBlogs,
    resetState
  }
})