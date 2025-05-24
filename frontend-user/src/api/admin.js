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
   * 更新用户状态（封禁/解封）- AdminController接口
   * @param {Number} userId - 用户ID
   * @param {Object} data - 状态数据
   */
  updateUserStatus(userId, data) {
    return put(API_PATHS.ADMIN.UPDATE_USER_STATUS.replace('{id}', userId), data)
  },

  /**
   * 删除用户 - AdminController接口
   * @param {Number} userId - 用户ID
   */
  deleteUser(userId) {
    return del(API_PATHS.ADMIN.DELETE_USER.replace('{id}', userId))
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
    return get(`${API_PATHS.ADMIN.GET_USER_BLOG_LIST}?userId=${userId}`)
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
   * 删除敏感词
   * @param {String} word - 敏感词
   */
  deleteSensitiveWord(word) {
    return post(API_PATHS.ADMIN.DELETE_SENSITIVE_WORD.replace('{sensitiveWord}', word))
  },

  /**
   * 批量添加敏感词
   * @param {Array} words - 敏感词数组
   */
  addSensitiveWords(words) {
    return post(API_PATHS.ADMIN.ADD_SENSITIVE_WORDS, { sensitiveWord: words })
  },

  /**
   * 批量删除敏感词
   * @param {Array} words - 敏感词数组
   */
  deleteSensitiveWords(words) {
    return post(API_PATHS.ADMIN.DELETE_SENSITIVE_WORDS, { sensitiveWord: words })
  },

  /**
   * 获取失败登录列表
   */
  getFailLoginList() {
    return get(API_PATHS.ADMIN.GET_FAIL_LOGIN_LIST)
  },

  /**
   * 获取异常博客列表
   */
  getAnomaliesBlogList() {
    return get(API_PATHS.ADMIN.GET_ANOMALIES_BLOG_LIST)
  },

  /**
   * 获取操作日志
   * @param {Object} params - 查询参数
   */
  getLogs(params) {
    return get(API_PATHS.ADMIN.LOGS, params)
  },

  /**
   * 获取统计概览
   */
  getStatsOverview() {
    return get(API_PATHS.ADMIN.STATS_OVERVIEW)
  },

  /**
   * 获取用户活跃度统计
   * @param {String} period - 时间段（day/week/month/year）
   */
  getUsersActive(period = 'week') {
    return get(API_PATHS.ADMIN.USERS_ACTIVE, { period })
  },

  /**
   * 获取内容统计
   * @param {String} period - 时间段（day/week/month/year）
   */
  getContentsStats(period = 'week') {
    return get(API_PATHS.ADMIN.CONTENTS_STATS, { period })
  },

  /**
   * 获取文章列表
   * @param {Object} params - 查询参数
   */
  getArticles(params = {}) {
    const query = new URLSearchParams()

    if (params.author) query.append('author', params.author)
    if (params.keyword) query.append('keyword', params.keyword)
    if (params.status) query.append('status', params.status)
    if (params.page) query.append('page', params.page)
    if (params.pageSize) query.append('pageSize', params.pageSize)

    const queryString = query.toString()
    return get(`${API_PATHS.ADMIN.ARTICLES}${queryString ? '?' + queryString : ''}`)
  },

  /**
   * 发布文章
   * @param {String} id - 文章ID
   */
  publishArticle(id) {
    return post(API_PATHS.ADMIN.PUBLISH_ARTICLE.replace('{blogId}', id))
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
    return del(API_PATHS.ADMIN.DELETE_ARTICLE.replace('{blogId}', id), data)
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
