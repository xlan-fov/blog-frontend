# 管理员界面 API 文档与前端组件对应关系

本文档详细说明博客系统管理员界面需要的所有后端接口及其与前端组件的对应关系。

## 1. 异常感知接口

### 1.1 账号异常登录记录查询

```
GET /api/admin/anomaly/logins
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                                 |
|------------|---------|-----|-------------------------------------|
| startDate  | String  | 是  | 开始日期，格式：YYYY-MM-DD             |
| endDate    | String  | 是  | 结束日期，格式：YYYY-MM-DD             |
| username   | String  | 否  | 用户名，用于筛选特定用户                |
| page       | Integer | 否  | 页码，默认1                           |
| pageSize   | Integer | 否  | 每页数量，默认10                       |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 56,
    "list": [
      {
        "id": 1,
        "username": "user1",
        "ip": "192.168.1.1",
        "location": "北京",
        "time": "2023-05-08 12:30:45",
        "attemptCount": 5,
        "status": "normal",
        "device": "Windows Chrome",
        "attempts": [
          {
            "time": "2023-05-08 12:25:30",
            "status": "failed",
            "message": "密码错误"
          },
          {
            "time": "2023-05-08 12:30:45",
            "status": "success",
            "message": "登录成功"
          }
        ]
      }
    ]
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend\src\views\admin\AnomalyDetection.vue`
- **组件位置**：`filter-section` 区域的日期筛选和用户名搜索
- **函数调用**：`queryAnomalies()` 函数中应调用此接口

### 1.2 违禁内容记录查询

```
GET /api/admin/anomaly/contents
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                                 |
|------------|---------|-----|-------------------------------------|
| startDate  | String  | 是  | 开始日期，格式：YYYY-MM-DD             |
| endDate    | String  | 是  | 结束日期，格式：YYYY-MM-DD             |
| username   | String  | 否  | 用户名，用于筛选特定用户                |
| reason     | String  | 否  | 拒绝原因，用于筛选特定类型的违禁内容      |
| page       | Integer | 否  | 页码，默认1                           |
| pageSize   | Integer | 否  | 每页数量，默认10                       |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 78,
    "list": [
      {
        "id": 1,
        "username": "user1",
        "title": "被拒绝的博客标题",
        "type": "博客文章",
        "createTime": "2023-05-01 10:25:30",
        "rejectTime": "2023-05-01 14:30:45",
        "rejectReason": "political",
        "userStatus": "normal",
        "content": "<p>这是被拒绝的内容...</p>",
        "reviewer": "admin1",
        "reviewComment": "内容包含敏感词，多次警告未改正"
      }
    ]
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\admin\AnomalyDetection.vue`
- **组件位置**：`el-tab-pane name="prohibitedContent"` 标签页
- **函数调用**：`searchProhibitedContent()` 函数中应调用此接口

### 1.3 踢出用户

```
POST /api/admin/users/{username}/kick
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                  |
|------------|---------|-----|----------------------|
| username   | String  | 是  | 用户名 (路径参数)        |

**对应前端组件：**
- **文件路径**：`d:\blog\frontend\src\views\admin\AnomalyDetection.vue`
- **组件位置**：异常数据表格的操作列
- **函数调用**：`kickUser(user)` 函数中应调用此接口

### 1.4 封禁用户

```
POST /api/admin/users/{username}/ban
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                  |
|------------|---------|-----|----------------------|
| username   | String  | 是  | 用户名 (路径参数)        |
| reason     | String  | 是  | 封禁原因                |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "username": "user1",
    "status": "banned",
    "banTime": "2023-05-10 14:30:45"
  }
}
```

**对应前端组件：**
- **文件路径1**：`d:\blog\frontend\src\views\admin\AnomalyDetection.vue`
- **组件位置**：异常数据表格的操作列
- **函数调用**：`banUser(user)` 函数中应调用此接口

- **文件路径2**：`d:\blog\frontend-user\src\views\admin\AnomalyDetection.vue`
- **组件位置**：封禁对话框
- **函数调用**：`confirmBan()` 函数中应调用此接口

## 2. 文章管理接口

### 2.1 文章列表查询

```
GET /api/admin/articles
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                                 |
|------------|---------|-----|-------------------------------------|
| keyword    | String  | 否  | 搜索关键词，用于标题和内容的模糊匹配      |
| status     | String  | 否  | 文章状态：published(已发布)/draft(未发布)|
| author     | String  | 否  | 作者用户名                            |
| page       | Integer | 否  | 页码，默认1                           |
| pageSize   | Integer | 否  | 每页数量，默认10                       |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "list": [
      {
        "id": "1",
        "title": "文章标题",
        "content": "文章内容摘要...",
        "status": "published",
        "author": "张三",
        "createTime": "2023-05-01 10:00:00",
        "updateTime": "2023-05-02 14:30:00"
      }
    ]
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend\src\views\admin\ArticleList.vue`
- **组件位置**：文章列表上方的搜索框
- **函数调用**：`searchArticles()` 函数中应调用此接口

### 2.2 发布文章

```
POST /api/admin/articles/{id}/publish
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                  |
|------------|---------|-----|----------------------|
| id         | String  | 是  | 文章ID (路径参数)       |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "1",
    "status": "published",
    "publishTime": "2023-05-10 15:00:00"
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend\src\views\admin\ArticleList.vue`
- **组件位置**：文章表格的操作列
- **函数调用**：文章状态为"未发布"时的发布按钮点击事件

### 2.3 下架文章

```
POST /api/admin/articles/{id}/withdraw
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                  |
|------------|---------|-----|----------------------|
| id         | String  | 是  | 文章ID (路径参数)       |
| reason     | String  | 否  | 下架原因               |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "1",
    "status": "draft"
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend\src\views\admin\ArticleList.vue`
- **组件位置**：文章表格的操作列
- **函数调用**：下架或撤回功能，当前界面可能未明确实现

### 2.4 删除文章

```
DELETE /api/admin/articles/{id}
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                  |
|------------|---------|-----|----------------------|
| id         | String  | 是  | 文章ID (路径参数)       |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend\src\views\admin\ArticleList.vue`
- **组件位置**：文章表格的操作列
- **函数调用**：删除按钮点击事件

## 3. 账号管理接口

### 3.1 账号列表查询

```
GET /api/admin/users
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                                 |
|------------|---------|-----|-------------------------------------|
| keyword    | String  | 否  | 搜索关键词，用于用户名和手机号的模糊匹配   |
| status     | String  | 否  | 账号状态：normal(正常)/banned(已封禁)   |
| page       | Integer | 否  | 页码，默认1                           |
| pageSize   | Integer | 否  | 每页数量，默认10                       |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "list": [
      {
        "username": "张三",
        "password": "******",
        "phone": "159****7777",
        "status": "normal",
        "createTime": "2023-01-01 10:00:00",
        "lastLoginTime": "2023-05-10 14:30:00"
      }
    ]
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend\src\views\admin\AccountManage.vue`
- **组件位置**：顶部搜索框
- **函数调用**：`searchAccounts()` 函数中应调用此接口

### 3.2 创建账号

```
POST /api/admin/users
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                  |
|------------|---------|-----|----------------------|
| username   | String  | 是  | 用户名                 |
| password   | String  | 是  | 密码                  |
| phone      | String  | 是  | 手机号                 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "username": "新用户",
    "phone": "13800138000",
    "status": "normal",
    "createTime": "2023-05-10 15:00:00"
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend\src\views\admin\AccountManage.vue`
- **组件位置**：创建账号对话框
- **函数调用**：`confirmCreate()` 函数中应调用此接口

### 3.3 封禁账号

```
POST /api/admin/users/{username}/ban
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                  |
|------------|---------|-----|----------------------|
| username   | String  | 是  | 用户名 (路径参数)        |
| reason     | String  | 是  | 封禁原因                |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "username": "张三",
    "status": "banned",
    "banTime": "2023-05-10 15:00:00"
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend\src\views\admin\AccountManage.vue`
- **组件位置**：账号表格的操作列
- **函数调用**：`banAccount(account)` 函数中应调用此接口

### 3.4 解封账号

```
POST /api/admin/users/{username}/unban
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                  |
|------------|---------|-----|----------------------|
| username   | String  | 是  | 用户名 (路径参数)        |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "username": "张三",
    "status": "normal",
    "unbanTime": "2023-05-10 15:00:00"
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend\src\views\admin\AccountManage.vue`
- **组件位置**：账号表格的操作列，状态为"已封禁"的账号
- **函数调用**：`unbanAccount(account)` 函数中应调用此接口

### 3.5 账号详情查询

```
GET /api/admin/users/{username}
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                  |
|------------|---------|-----|----------------------|
| username   | String  | 是  | 用户名 (路径参数)        |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "username": "张三",
    "phone": "15900007777",
    "email": "zhangsan@example.com",
    "status": "normal",
    "createTime": "2023-01-01 10:00:00",
    "lastLoginTime": "2023-05-10 14:30:00",
    "lastLoginIp": "192.168.1.1",
    "articleCount": 10,
    "banHistory": [
      {
        "banTime": "2023-04-01 10:00:00",
        "unbanTime": "2023-04-10 10:00:00",
        "reason": "发布违规内容",
        "operator": "admin1"
      }
    ]
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend\src\views\admin\AccountManage.vue`
- **组件位置**：查看账号详情功能
- **函数调用**：`viewDetail(account)` 函数中应调用此接口

## 4. 统计分析接口

### 4.1 系统概况统计

```
GET /api/admin/stats/overview
```

**请求参数：** 无

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userCount": 1000,
    "articleCount": 5000,
    "todayNewUsers": 10,
    "todayNewArticles": 50,
    "todayActiveUsers": 100,
    "todayAbnormalLogins": 5,
    "pendingReviews": 20
  }
}
```

### 4.2 用户活跃度统计

```
GET /api/admin/stats/users/active
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                                 |
|------------|---------|-----|-------------------------------------|
| period     | String  | 否  | 统计周期：day/week/month/year，默认week |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "period": "week",
    "labels": ["周一", "周二", "周三", "周四", "周五", "周六", "周日"],
    "activeUsers": [120, 132, 101, 134, 90, 230, 210],
    "newUsers": [20, 25, 18, 26, 15, 30, 28]
  }
}
```

### 4.3 内容发布统计

```
GET /api/admin/stats/contents
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                                 |
|------------|---------|-----|-------------------------------------|
| period     | String  | 否  | 统计周期：day/week/month/year，默认week |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "period": "week",
    "labels": ["周一", "周二", "周三", "周四", "周五", "周六", "周日"],
    "publishedCount": [40, 35, 50, 45, 60, 80, 70],
    "rejectedCount": [5, 6, 8, 7, 9, 11, 10]
  }
}
```

## 5. 日志查询接口

### 5.1 系统操作日志查询

```
GET /api/admin/logs
```

**请求参数：**

| 参数名      | 类型     | 必填 | 描述                                  |
|------------|---------|-----|--------------------------------------|
| startDate  | String  | 是  | 开始日期，格式：YYYY-MM-DD               |
| endDate    | String  | 是  | 结束日期，格式：YYYY-MM-DD               |
| type       | String  | 否  | 操作类型：login/logout/ban/unban/publish|
| operator   | String  | 否  | 操作者用户名                             |
| page       | Integer | 否  | 页码，默认1                             |
| pageSize   | Integer | 否  | 每页数量，默认10                         |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 200,
    "list": [
      {
        "id": 1,
        "type": "ban",
        "operator": "admin1",
        "target": "user1",
        "detail": "封禁用户，原因：发布违规内容",
        "ip": "192.168.1.1",
        "time": "2023-05-10 14:30:45"
      }
    ]
  }
}
```
