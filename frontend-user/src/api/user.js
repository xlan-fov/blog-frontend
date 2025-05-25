import request from './request'
import { API_PATHS } from './config'
import { ElMessage } from 'element-plus'
import { get, post, put } from './request'

/**
 * 用户名密码注册
 * @param {Object} params - 注册参数
 * @param {string} params.username - 用户名
 * @param {string} params.password - 密码
 * @param {string} params.captcha - 验证码
 * @returns {Promise} 返回注册结果
 */
export const registerByUsername = async (params) => {
    try {
        // 确保验证码ID存在
        if (!params.captchaId) {
            params.captchaId = localStorage.getItem('captchaId') || '';
        }

        // 添加密码格式检查
        const passwordPattern = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{6,16}$/;
        if (!passwordPattern.test(params.password)) {
            ElMessage.warning('密码必须包含数字、小写和大写字母，长度6-16位');
            throw new Error('密码格式不符合要求');
        }

        // 添加用户名长度检查
        if (params.username.length < 6 || params.username.length > 20) {
            ElMessage.warning('用户名长度必须在6-20个字符之间');
            throw new Error('用户名长度不符合要求');
        }

        // 确保发送的请求数据格式与后端DTO匹配
        const requestData = {
            username: params.username,
            password: params.password,
            captcha: params.captcha,
            captchaId: params.captchaId
        };

        console.log('发送注册请求数据:', requestData);
        const response = await request.post(API_PATHS.USERS.REGISTER, requestData);
        return response;
    } catch (error) {
        console.error('注册请求详情:', {
            params: params,
            error: error.response?.data || error.message
        });
        ElMessage.error(error.response?.data?.message || error.response?.data?.msg || '注册失败');
        throw error;
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
export const loginByUsername = (data) => {
    console.log('DEBUG: 登录请求数据:', data)
    return request({
        url: API_PATHS.USERS.LOGIN,
        method: 'post',
        data: {
            username: data.username,
            password: data.password,
            captcha: data.captcha,
            captchaId: data.captchaId
        }
    })
}

/**
 * 手机号码登录
 * @param {Object} params - 登录参数
 * @param {string} params.phone - 手机号
 * @param {string} params.code - 验证码
 * @returns {Promise} 返回登录结果
 */
export const loginByPhone = async (params) => {
    try {
        const response = await request.post(API_PATHS.USERS.LOGIN_BY_PHONE, {
            phone: params.phone,
            code: params.code
        })
        return response
    } catch (error) {
        // 显示具体的错误信息给用户
        ElMessage.error(error.response?.data?.msg || '手机号登录失败')
        throw error
    }
}

/**
 * 获取验证码
 * @param {string} captchaId - 验证码ID
 */
export const getCaptcha = (captchaId) => {
    return request({
        url: API_PATHS.USERS.CAPTCHA,
        method: 'get',
        params: { captchaId }
    })
}

/**
 * 获取滑动验证图片
 */
export const getSliderImage = () => {
    return get(API_PATHS.USERS.SLIDER_IMAGE)
}

/**
 * 验证滑动结果
 * @param {Object} params - 验证参数
 */
export const validateSlider = (params) => {
    return post(API_PATHS.USERS.SLIDER_VALIDATE, params)
}

/**
 * 退出登录
 */
export const logout = () => {
    return post(API_PATHS.USERS.LOGOUT)
}

/**
 * 获取用户个人资料
 * @returns {Promise} 返回用户个人资料
 */
export const getProfile = () => {
    return get(API_PATHS.USERS.PROFILE)
}

/**
 * 更新用户个人资料
 * @param {Object} data - 个人资料数据
 * @returns {Promise} 返回更新结果
 */
export const updateProfile = (data) => {
    return put(API_PATHS.USERS.UPDATE_PROFILE, data)
}

/**
 * 发送验证码（通用）
 * @param {string} phone - 手机号
 * @returns {Promise} 返回发送结果
 */
export const sendCode = (phone) => {
    return post(API_PATHS.USERS.SEND_CODE, { phone })
}

/**
 * 发送手机验证码（用于修改手机号）
 * @param {string} phone - 手机号
 * @returns {Promise} 返回发送结果
 */
export const sendPhoneCode = (phone) => {
    return post(API_PATHS.USERS.SEND_PHONE_CODE, { phone })
}

/**
 * 修改手机号
 * @param {Object} data - 修改手机号数据
 * @param {string} data.newPhone - 新手机号
 * @param {string} data.code - 验证码
 * @returns {Promise} 返回修改结果
 */
export const changePhone = (data) => {
    return post(API_PATHS.USERS.CHANGE_PHONE, data)
}

/**
 * 获取登录日志
 * @returns {Promise} 返回登录日志列表
 */
export const getLoginLogs = () => {
    return get(API_PATHS.USERS.LOGIN_LOGS)
}

/**
 * 上传头像
 * @param {FormData} formData - 包含头像文件的FormData
 * @returns {Promise} 返回上传结果
 */
export const uploadAvatar = (formData) => {
    return post(API_PATHS.USERS.UPLOAD_AVATAR, formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
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
 * 管理员登录
 * @param {Object} params - 登录参数
 * @param {string} params.username - 用户名
 * @param {string} params.password - 密码
 * @param {string} params.authCode - 授权码
 * @returns {Promise} 返回登录结果
 */
export const adminLogin = async (params) => {
    try {
        // 确保字段名称与后端期望的一致
        const response = await request.post(API_PATHS.ADMIN.LOGIN, {
            username: params.username,
            password: params.password,
            // 同时发送两种可能的字段名，确保与后端匹配
            secretCode: params.authCode,
            authCode: params.authCode
        })
        return response
    } catch (error) {
        // 增强错误日志以帮助诊断
        console.error('管理员登录失败:', {
            url: error.config?.url,
            params: params,
            error: error.response?.data || error.message,
            status: error.response?.status
        });
        ElMessage.error(error.response?.data?.message || '管理员登录失败')
        throw error
    }
}

/**
 * 修改密码
 * @param {Object} data - 修改密码数据
 * @param {string} data.id - 用户ID
 * @param {string} data.oldPassword - 原密码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise} 返回修改结果
 */
export const changePassword = async (data) => {
    try {
        const response = await request.put(API_PATHS.USERS.CHANGE_PASSWORD, {
            id: data.id,
            oldPassword: data.oldPassword,
            newPassword: data.newPassword
        });
        return response;
    } catch (error) {
        console.error('修改密码失败:', error);
        ElMessage.error(error.response?.data?.message || '修改密码失败');
        throw error;
    }
}

// 确保在默认导出中包含新方法
export default {
    registerByUsername,
    registerByPhone,
    loginByUsername,
    loginByPhone,
    sendCode,
    sendPhoneCode,
    getCaptcha,
    getSliderImage,
    validateSlider,
    logout,
    adminLogin,
    getProfile,
    updateProfile,
    changePhone,
    getLoginLogs,
    uploadAvatar,
    changePassword
}