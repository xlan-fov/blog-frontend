// API基础配置
export const API_CONFIG = {
    BASE_URL: 'http://localhost:8080',
    TIMEOUT: 10000,
    HEADERS: {
        'Content-Type': 'application/json'
    }
}

// API路径配置
export const API_PATHS = {
    USERS: {
        CAPTCHA: '/users/captcha',
        REGISTER: '/users/register',
        REGISTER_BY_PHONE: '/users/registerByphone',
        LOGIN: '/users/loginByname',
        LOGIN_BY_PHONE: '/users/loginByphone',
        SEND_CODE: '/users/code'
    }
} 