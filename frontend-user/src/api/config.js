// API基础配置
export const API_CONFIG = {
    BASE_URL: 'http://localhost:8081',
    TIMEOUT: 10000,
    HEADERS: {
        'Content-Type': 'application/json'
    }
}

// API路径配置
export const API_PATHS = {
    USERS: {
        CAPTCHA: '/api/users/captcha',
        REGISTER: '/api/users/register',
        REGISTER_BY_PHONE: '/api/users/registerByphone',
        LOGIN: '/api/users/loginByname',
        LOGIN_BY_PHONE: '/api/users/loginByphone',
        SEND_CODE: '/api/users/code',
        PROFILE: '/api/users/profile',
        UPDATE_PROFILE: '/api/users/profile'
    },
    BLOGS: {
        LIST: '/api/blogs',
        GET: '/api/blogs/{id}',
        CREATE: '/api/blogs',
        UPDATE: '/api/blogs/{id}',
        DELETE: '/api/blogs/{id}',
        PUBLISH: '/api/blogs/{id}/publish',
        WITHDRAW: '/api/blogs/{id}/withdraw',
        SEARCH: '/api/blogs/search'
    },
    ADMIN: {
        USERS: '/api/admin/users',
        USER_INFO: '/api/admin/users/{username}',
        BAN_USER: '/api/admin/users/{username}/ban',
        UNBAN_USER: '/api/admin/users/{username}/unban',
        ARTICLES: '/api/admin/articles',
        PUBLISH_ARTICLE: '/api/admin/articles/{id}/publish',
        WITHDRAW_ARTICLE: '/api/admin/articles/{id}/withdraw',
        DELETE_ARTICLE: '/api/admin/articles/{id}',
        ANOMALY_LOGINS: '/api/admin/anomaly/logins',
        ANOMALY_CONTENTS: '/api/admin/anomaly/contents',
        LOGS: '/api/admin/logs',
        STATS_OVERVIEW: '/api/admin/stats/overview',
        USERS_ACTIVE: '/api/admin/stats/users/active',
        CONTENTS_STATS: '/api/admin/stats/contents'
    }
}