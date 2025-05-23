import { get, post, put, del } from './request'
import { API_PATHS } from './config'

export default {
  /**
   * 获取用户列表
   * @param {Object} params - 查询参数
   */
  getUsers(params) {
    return get(API_PATHS.ADMIN.USERS, params)
  },

  /**
   * 获取用户详情
   * @param {String} username - 用户名
   */
  getUserInfo(username) {
    return get(API_PATHS.ADMIN.USER_INFO.replace('{username}', username))
  },

  /**
   * 创建用户
   * @param {Object} data - 用户数据
   */
  createUser(data) {
    return post(API_PATHS.ADMIN.USERS, data)
  },

  /**
   * 封禁用户
   * @param {String} username - 用户名
   * @param {Object} data - 封禁原因等数据
   */
  banUser(username, data) {
    return post(API_PATHS.ADMIN.BAN_USER.replace('{username}', username), data)
  },

  /**
   * 解封用户
   * @param {String} username - 用户名
   * @param {Object} data - 解封原因等数据
   */
  unbanUser(username, data) {
    return post(API_PATHS.ADMIN.UNBAN_USER.replace('{username}', username), data)
  },

  /**
   * 踢出用户
   * @param {Number} userId - 用户ID
   * @param {String} reason - 踢出原因
   */
  kickUser(userId, reason) {
    return post(API_PATHS.ADMIN.KICK_USER, { id: userId, reason })
  },

  /**
   * 获取博客列表
   */
  getBlogList() {
    return get(API_PATHS.ADMIN.GET_BLOG_LIST)
  },

  /**
   * 获取用户博客列表
   * @param {Number} userId - 用户ID
   */
  getUserBlogList(userId) {
    return get(API_PATHS.ADMIN.GET_USER_BLOG_LIST, { userId })
  },

  /**
   * 获取敏感词列表
   */
  getSensitiveWordsList() {
    return get(API_PATHS.ADMIN.GET_SENSITIVE_WORDS)
  },

  /**
   * 添加敏感词
   * @param {String} word - 敏感词
   */
  addSensitiveWord(word) {
    return post(API_PATHS.ADMIN.ADD_SENSITIVE_WORD.replace('{sensitiveWord}', word))
  },

  /**
   * 获取文章列表
   * @param {Object} params - 查询参数
   */
  getArticles(params) {
    return get(API_PATHS.ADMIN.ARTICLES, params)
  },

  /**
   * 发布文章
   * @param {String} id - 文章ID
   */
  publishArticle(id) {
    return post(API_PATHS.ADMIN.PUBLISH_ARTICLE.replace('{id}', id))
  },

  /**
   * 撤回文章
   * @param {String} id - 文章ID
   * @param {Object} data - 撤回原因等数据
   */
  withdrawArticle(id, data) {
    return post(API_PATHS.ADMIN.WITHDRAW_ARTICLE.replace('{id}', id), data)
  },

  /**
   * 删除文章
   * @param {String} id - 文章ID
   * @param {Object} data - 删除原因等数据
   */
  deleteArticle(id, data) {
    return del(API_PATHS.ADMIN.DELETE_ARTICLE.replace('{id}', id), data)
  },

  /**
   * 获取异常登录记录
   * @param {Object} params - 查询参数
   */
  getAnomalyLogins(params) {
    return get(API_PATHS.ADMIN.ANOMALY_LOGINS, params)
  },

  /**
   * 获取异常内容记录
   * @param {Object} params - 查询参数
   */
  getAnomalyContents(params) {
    return get(API_PATHS.ADMIN.ANOMALY_CONTENTS, params)
  }
}
