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
        // 用户相关接口 - 已经正确配置
        CAPTCHA: '/api/users/captcha',
        REGISTER: '/api/users/registerByname',
        REGISTER_BY_PHONE: '/api/users/registerByphone',
        LOGIN: '/api/users/loginByname',
        LOGIN_BY_PHONE: '/api/users/loginByphone',
        SEND_CODE: '/api/users/code',
        SLIDER_IMAGE: '/api/users/slider-image',
        SLIDER_VALIDATE: '/api/users/slider-validate',
        LOGOUT: '/api/users/logout'
    },
    BLOGS: {
        // 博客相关接口 - 需要小调整
        LIST: '/api/blogs',
        GET: '/api/getBlogs/{id}', 
        CREATE: '/api/blogs/addBlog',
        UPDATE: '/api/updateBlogs',
        DELETE: '/api/deleteBlogs/{id}',
        PUBLISH: '/api/updateBlogs', // 正确 - 通过更新状态实现发布
        WITHDRAW: '/api/admin/articles/{id}/withdraw',
        GET_DRAFT: '/api/getDraft',
        SAVE_CONTENT: '/api/saveBlogContent' // 添加缺少的API
    },
    ADMIN: {
        // 管理员相关接口 - 有些接口需要调整
        LOGIN: '/api/users/adminLogin', // 这里已经正确配置为API路径
        USERS: '/api/admin/users',
        USER_INFO: '/api/admin/users/{username}',
        KICK_USER: '/api/admin/kickUser', // 添加缺少的API
        BAN_USER: '/api/admin/users/{username}/ban',
        UNBAN_USER: '/api/admin/users/{username}/unban',
        // 修改文章列表接口路径，与后端对应
        ARTICLES: '/api/admin/articleslist',
        GET_BLOG_LIST: '/api/admin/getBlogList', // 添加缺少的API
        GET_USER_BLOG_LIST: '/api/admin/getUserBlogList', // 添加缺少的API
        PUBLISH_ARTICLE: '/api/admin/articles/{blogId}/publish', // 修正路径参数名
        WITHDRAW_ARTICLE: '/api/admin/articles/{blogId}/withdraw', // 修正路径参数名
        DELETE_ARTICLE: '/api/admin/articles/{blogId}', // 修正路径参数名
        GET_SENSITIVE_WORDS: '/api/admin/getSensitiveWordsList', // 添加缺少的API
        ADD_SENSITIVE_WORD: '/api/admin/addSensitiveWord/{sensitiveWord}', // 添加缺少的API
        DELETE_SENSITIVE_WORD: '/api/admin/deleteSensitiveWord/{sensitiveWord}', // 添加缺少的API
        ADD_SENSITIVE_WORDS: '/api/admin/addSensitiveWords', // 添加缺少的API
        DELETE_SENSITIVE_WORDS: '/api/admin/deleteSensitiveWords', // 添加缺少的API
        GET_FAIL_LOGIN_LIST: '/api/admin/getFailLoginList', // 添加缺少的API
        ANOMALY_LOGINS: '/api/admin/anomaly/logins',
        GET_ANOMALIES_BLOG_LIST: '/api/admin/getAnomaliesBlogList', // 添加缺少的API
        ANOMALY_CONTENTS: '/api/admin/anomaly/contents',
        LOGS: '/api/admin/logs',
        STATS_OVERVIEW: '/api/admin/stats/overview',
        USERS_ACTIVE: '/api/admin/stats/users/active',
        CONTENTS_STATS: '/api/admin/stats/contents'
    },
    COMMON: {
        UPLOAD: '/api/common/upload'
    }
}