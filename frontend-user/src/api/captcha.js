import request from './request'
import { API_PATHS } from './config'
import axios from 'axios' // 新增导入axios

/**
 * 获取验证码
 * @param {string} captchaId - 验证码ID
 * @returns {Promise} 返回验证码图片数据
 */
export const getCaptcha = async (captchaId) => {
    try {
        // 添加时间戳防止缓存
        const timestamp = new Date().getTime();
        const url = `${API_PATHS.USERS.CAPTCHA}?captchaId=${captchaId}&t=${timestamp}`;
        
        console.log('请求验证码URL:', url);
        
        // 尝试使用原始axios请求，避免拦截器干扰
        const response = await axios.get(url);
        
        console.log('验证码原始响应:', response);
        
        // 检查不同可能的响应格式
        let imageData = null;
        
        if (response.data) {
            // 情况1: 直接返回base64图片字符串
            if (typeof response.data === 'string' && 
                response.data.startsWith('data:image')) {
                imageData = response.data;
            } 
            // 情况2: 标准API响应格式 {code: 200, data: {img: 'base64...'}}
            else if (response.data.code === 200 && response.data.data) {
                imageData = response.data.data.img || response.data.data;
            }
            // 情况3: 简化格式 {code: 200, data: 'base64...'}
            else if (response.data.code === 200) {
                imageData = response.data.data;
            }
        }
        
        // 如果找到了图片数据，返回标准格式
        if (imageData) {
            return {
                code: 200,
                data: { img: imageData }
            };
        }
        
        // 如果没有找到图片数据但响应成功，尝试返回原始响应
        return response.data || { 
            code: 500, 
            message: '无法解析验证码响应' 
        };
    } catch (error) {
        console.error('获取验证码失败:', error);
        // 返回错误信息，避免组件崩溃
        return {
            code: 500,
            message: error.message,
            error: true
        };
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