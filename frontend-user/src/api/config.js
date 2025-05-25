// API基础配置
export const API_CONFIG = {
    // 使用相对路径，依靠代理转发到后端
    BASE_URL: '',
    TIMEOUT: 10000,
    HEADERS: {
        'Content-Type': 'application/json'
    }
}

// API路径配置
export const API_PATHS = {
    USERS: {
        // 用户相关接口
        CAPTCHA: '/api/users/captcha',
        REGISTER: '/api/users/registerByname',
        REGISTER_BY_PHONE: '/api/users/registerByphone',
        LOGIN: '/api/users/loginByname',
        LOGIN_BY_PHONE: '/api/users/loginByphone',
        LOGOUT: '/api/users/logout',

        SEND_CODE: '/api/users/code',
        SLIDER_IMAGE: '/api/users/slider-image',
        SLIDER_VALIDATE: '/api/users/slider-validate',
        // 用户个人资料相关
        PROFILE: '/api/users/profile',
        UPDATE_PROFILE: '/api/users/profile/update',
        SEND_PHONE_CODE: '/api/users/phone/code',
        CHANGE_PHONE: '/api/users/phone/change',
        LOGIN_LOGS: '/api/users/login/logs',
        UPLOAD_AVATAR: '/api/users/avatar/upload',
    },
    BLOGS: {
        // 博客相关接口 - 修复路径
        LIST: '/api/blogs',
        GET: '/api/blogs/{id}', // 修复：从 /api/getBlogs/{id} 改为 /api/blogs/{id}
        CREATE: '/api/blogs/addBlog',
        UPDATE: '/api/updateBlogs/{id}', // 修复：添加 {id} 参数
        DELETE: '/api/deleteBlogs/{id}',
        PUBLISH: '/api/updateBlogs/{id}', // 修复：添加 {id} 参数
        WITHDRAW: '/api/admin/articles/{id}/withdraw',
        GET_DRAFT: '/api/getDraft',
        SAVE_CONTENT: '/api/saveBlogContent'
    },
    ADMIN: {
        // 管理员相关接口
        LOGIN: '/api/users/adminLogin',

        // 添加管理员个人资料相关接口
        USER_PROFILE: '/api/admin/profile',
        UPDATE_PROFILE: '/api/admin/profile/update',
        CHANGE_PASSWORD: '/api/admin/profile/password',

        // AdminActionsController 接口
        USERS: '/api/admin/users',
        USER_INFO: '/api/admin/users/{username}',
        KICK_USER: '/api/admin/kickUser',
        BAN_USER: '/api/admin/users/{username}/ban',
        UNBAN_USER: '/api/admin/users/{username}/unban',
        ARTICLES: '/api/admin/articleslist',
        GET_BLOG_LIST: '/api/admin/getBlogList',
        GET_USER_BLOG_LIST: '/api/admin/getUserBlogList',
        PUBLISH_ARTICLE: '/api/admin/articles/{blogId}/publish',
        WITHDRAW_ARTICLE: '/api/admin/articles/{blogId}/withdraw',
        DELETE_ARTICLE: '/api/admin/articles/{blogId}',
        GET_SENSITIVE_WORDS: '/api/admin/getSensitiveWordsList',
        ADD_SENSITIVE_WORD: '/api/admin/addSensitiveWord/{sensitiveWord}',
        DELETE_SENSITIVE_WORD: '/api/admin/deleteSensitiveWord/{sensitiveWord}',
        ADD_SENSITIVE_WORDS: '/api/admin/addSensitiveWords',
        DELETE_SENSITIVE_WORDS: '/api/admin/deleteSensitiveWords',
        GET_FAIL_LOGIN_LIST: '/api/admin/getFailLoginList',
        ANOMALY_LOGINS: '/api/admin/anomaly/logins',
        GET_ANOMALIES_BLOG_LIST: '/api/admin/getAnomaliesBlogList',
        ANOMALY_CONTENTS: '/api/admin/anomaly/contents',
        LOGS: '/api/admin/logs',
        STATS_OVERVIEW: '/api/admin/stats/overview',
        USERS_ACTIVE: '/api/admin/stats/users/active',
        CONTENTS_STATS: '/api/admin/stats/contents',

        // AdminController 接口（新增）
        UPDATE_USER_STATUS: '/api/admin-api/users/{id}/status',
        DELETE_USER: '/api/admin-api/users/{id}',
        ADMIN_USERS: '/api/admin-api/users'
    },
    COMMON: {
        UPLOAD: '/api/common/upload'
    }
}