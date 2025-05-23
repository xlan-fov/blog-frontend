import { get, post, put, del } from './request'
import { API_PATHS } from './config'

export default {
  /**
   * 获取博客列表
   * @param {Object} params - 分页和筛选参数
   */
  getBlogs(params) {
    return get(API_PATHS.BLOGS.LIST, params)
  },

  /**
   * 根据ID获取博客详情
   * @param {String} id - 博客ID
   */
  getBlogById(id) {
    return get(API_PATHS.BLOGS.GET.replace('{id}', id))
  },

  /**
   * 创建新博客
   * @param {Object} data - 博客数据
   */
  createBlog(data) {
    return post(API_PATHS.BLOGS.CREATE, data)
  },

  /**
   * 更新博客
   * @param {String} id - 博客ID
   * @param {Object} data - 博客更新数据
   */
  updateBlog(id, data) {
    // 后端不使用路径参数，而是在请求体中包含id
    data.id = id;
    return put(API_PATHS.BLOGS.UPDATE, data)
  },

  /**
   * 删除博客
   * @param {String} id - 博客ID
   */
  deleteBlog(id) {
    return del(API_PATHS.BLOGS.DELETE.replace('{id}', id))
  },

  /**
   * 发布博客 - 通过更新博客状态实现
   * @param {String} id - 博客ID
   */
  publishBlog(id) {
    // 使用更新博客API，设置状态为published
    return this.updateBlog(id, { status: 'published' })
  },

  /**
   * 撤回博客 - 通过更新博客状态实现
   * @param {String} id - 博客ID
   */
  withdrawBlog(id) {
    // 使用更新博客API，设置状态为draft
    return this.updateBlog(id, { status: 'draft' })
  },
  
  /**
   * 获取草稿
   * @param {Number} userId - 用户ID
   */
  getDraft(userId) {
    return get(API_PATHS.BLOGS.GET_DRAFT, { userId })
  },

  /**
   * 实时保存博客内容为草稿
   * @param {Object} data - 博客数据
   */
  saveBlogContent(data) {
    return post(API_PATHS.BLOGS.SAVE_CONTENT, data)
  }
}
