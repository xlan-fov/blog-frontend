import request from './request'
import { API_PATHS } from './config'
import { ElMessage } from 'element-plus'

/**
 * 用户名密码注册
 * @param {Object} params - 注册参数
 * @param {string} params.username - 用户名
 * @param {string} params.password - 密码
 * @param {string} params.captcha - 验证码
 * @param {string} params.captchaId - 验证码ID
 * @returns {Promise} 返回注册结果
 */
export const registerByUsername = async (params) => {
    try {
        const response = await request.post(API_PATHS.USERS.REGISTER, params)
        return response
    } catch (error) {
        // 显示具体的错误信息给用户
        ElMessage.error(error.response?.data?.msg || '注册失败')
        throw error
    }
}

/**
 * 手机号注册
 * @param {Object} params - 注册参数
 * @param {string} params.phone - 手机号
 * @param {string} params.code - 手机验证码
 * @param {string} params.captcha - 图片验证码
 * @param {string} params.captchaId - 验证码ID
 * @returns {Promise} 返回注册结果
 */
export const registerByPhone = async (params) => {
    try {
        const response = await request.post(API_PATHS.USERS.REGISTER_BY_PHONE, {
            phone: params.phone,
            code: params.code,
            captcha: params.captcha,
            captchaId: params.captchaId
        })
        return response
    } catch (error) {
        // 显示具体的错误信息给用户
        ElMessage.error(error.response?.data?.msg || '手机号注册失败')
        throw error
    }
}

/**
 * 用户名密码登录
 * @param {Object} params - 登录参数
 * @param {string} params.username - 用户名
 * @param {string} params.password - 密码
 * @param {string} [params.sliderToken] - 滑块验证token
 * @param {number} [params.sliderX] - 滑块验证X坐标
 * @returns {Promise} 返回登录结果
 */
export const loginByUsername = async (params) => {
    try {
        const response = await request.post(API_PATHS.USERS.LOGIN, {
            username: params.username,
            password: params.password,
            sliderToken: params.sliderToken,
            sliderX: params.sliderX
        })
        return response
    } catch (error) {
        // 显示具体的错误信息给用户
        ElMessage.error(error.response?.data?.msg || '登录失败')
        throw error
    }
}

/**
 * 手机号登录
 * @param {Object} params - 登录参数
 * @param {string} params.phone - 手机号
 * @param {string} params.code - 手机验证码
 * @returns {Promise} 返回登录结果
 */
export const loginByPhone = async (params) => {
    try {
        const response = await request.post(API_PATHS.USERS.LOGIN_BY_PHONE, params)
        return response
    } catch (error) {
        // 显示具体的错误信息给用户
        ElMessage.error(error.response?.data?.msg || '手机号登录失败')
        throw error
    }
}

/**
 * 发送手机验证码
 * @param {string} phone - 手机号
 * @returns {Promise} 返回发送结果
 */
export const sendPhoneCode = async (phone) => {
    try {
        const response = await request.post(API_PATHS.USERS.SEND_CODE, null, {
            params: { phone }
        })
        return response
    } catch (error) {
        // 显示具体的错误信息给用户
        ElMessage.error(error.response?.data?.msg || '发送验证码失败')
        throw error
    }
} 