import request from './request'
import { API_PATHS } from './config'

/**
 * 获取验证码
 * @param {string} captchaId - 验证码ID
 * @returns {Promise} 返回验证码图片数据
 */
export const getCaptcha = async (captchaId) => {
    try {
        const response = await request.get(API_PATHS.USERS.CAPTCHA, {
            params: { captchaId }
        })
        return response
    } catch (error) {
        console.error('获取验证码失败:', error)
        throw error
    }
}

/**
 * 用户注册
 * @param {Object} params - 注册参数
 * @param {string} params.username - 用户名
 * @param {string} params.password - 密码
 * @param {string} params.captcha - 验证码
 * @param {string} params.captchaId - 验证码ID
 * @returns {Promise} 返回注册结果
 */
export const register = async (params) => {
    try {
        const response = await request.post(API_PATHS.USERS.REGISTER, params)
        return response
    } catch (error) {
        console.error('注册失败:', error)
        throw error
    }
} 