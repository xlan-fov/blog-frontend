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
      
      // 确保响应数据格式正确
      if (response && response.data) {
        if (Array.isArray(response.data)) {
          blogs.value = response.data
        } else if (response.data.records && Array.isArray(response.data.records)) {
          blogs.value = response.data.records
          totalBlogs.value = response.data.total || 0
        } else {
          console.warn('API返回的数据格式不正确:', response.data)
          blogs.value = []
        }
      } else {
        console.warn('API响应为空或格式错误:', response)
        blogs.value = []
      }
    } catch (error) {
      console.error('加载博客列表失败:', error)
      blogs.value = [] // 确保出错时也是数组
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
        return localBlog
      }
      
      // 如果本地没有，从服务器获取
      const response = await blogApi.getBlogById(id)
      if (response && response.data) {
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
      // 后端会从登录上下文中获取用户ID，不需要前端传递
      const response = await blogApi.createBlog(blogData)
      
      if (response && response.data) {
        // 将新博客添加到列表前端
        if (!Array.isArray(blogs.value)) {
          blogs.value = []
        }
        blogs.value.unshift(response.data)
      }
      
      return response
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
      const response = await blogApi.updateBlog(id, blogData)
      
      if (response && response.data) {
        // 更新本地列表中的博客
        if (Array.isArray(blogs.value)) {
          const index = blogs.value.findIndex(blog => blog.id === id || blog.id === parseInt(id))
          if (index !== -1) {
            blogs.value[index] = { ...blogs.value[index], ...response.data }
          }
        }
      }
      
      return response
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
      await blogApi.deleteBlog(id)
      
      // 从本地列表中删除
      if (Array.isArray(blogs.value)) {
        blogs.value = blogs.value.filter(blog => blog.id !== id && blog.id !== parseInt(id))
      }
    } catch (error) {
      console.error('删除博客失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 发布博客 - 设置为已发布状态
   * @param {Object} blogData - 博客数据，包含id、title、content等
   */
  async function publishBlog(blogData) {
    try {
      console.log('开始发布博客:', blogData);
      
      // 确保状态为已发布
      const data = {
        ...blogData,
        status: 'published' // 显式设置状态为已发布
      };
      
      // 如果是新博客，使用创建API
      if (!data.id) {
        const response = await blogApi.createBlog(data);
        if (response.code === 200) {
          // 发布成功后刷新博客列表
          await getBlogs();
        }
        return response;
      } 
      // 如果是更新现有博客，使用更新API
      else {
        const response = await blogApi.updateBlog(data.id, data);
        if (response.code === 200) {
          // 发布成功后刷新博客列表
          await getBlogs();
        }
        return response;
      }
    } catch (error) {
      console.error('发布博客失败:', error);
      throw error;
    }
  }

  // 搜索博客
  async function searchBlogs(keyword) {
    searchKeyword.value = keyword
    await getBlogs({ keyword })
  }

  // 重置状态
  function resetState() {
    blogs.value = []
    searchKeyword.value = ''
    currentPage.value = 1
    pageSize.value = 10
    totalBlogs.value = 0
  }

  // 添加 loadBlogs 作为 getBlogs 的别名
  const loadBlogs = getBlogs

  return {
    blogs,
    totalBlogs,
    currentPage,
    pageSize,
    loading,
    getBlogs,
    loadBlogs, // 添加这个导出
    getBlogById,
    createBlog,
    updateBlog,
    deleteBlog,
    publishBlog
  }
})