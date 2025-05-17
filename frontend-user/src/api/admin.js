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
