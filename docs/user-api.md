# 用户登录注册和普通用户界面 API 文档

本文档详细说明博客系统用户登录注册和普通用户界面相关的前后端接口。

## 1. 用户认证接口

### 1.1 获取图片验证码

```
GET /api/users/captcha
```

**请求参数：**

| 参数名     | 类型    | 必填 | 描述        |
|-----------|--------|-----|------------|
| captchaId | String | 否  | 验证码ID，用于标识特定的验证码实例 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaId": "a1b2c3d4",
    "imageBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhE..."
  }
}
```

**对应前端组件：**
- **文件路径1**：`d:\blog\frontend-user\src\views\Register.vue`
- **组件位置**：注册表单中的图片验证码组件
- **调用位置**：`Captcha` 组件内部

- **文件路径2**：`d:\blog\frontend-user\src\views\Login.vue`
- **组件位置**：登录表单中的图片验证码组件
- **调用位置**：`Captcha` 组件内部

### 1.2 发送手机验证码

```
POST /api/users/code
```

**请求参数：**

| 参数名  | 类型    | 必填 | 描述     |
|--------|--------|-----|---------|
| phone  | String | 是  | 手机号    |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "expired": 300
  }
}
```

**对应前端组件：**
- **文件路径1**：`d:\blog\frontend-user\src\views\Register.vue`
- **组件位置**：手机验证码注册表单
- **函数调用**：`sendCode()` 函数中通过 `sendPhoneCode` API 调用

- **文件路径2**：`d:\blog\frontend-user\src\views\Login.vue`
- **组件位置**：手机验证码登录表单
- **函数调用**：`sendCode()` 函数中通过 `sendPhoneCode` API 调用

### 1.3 用户名密码注册

```
POST /api/users/register
```

**请求参数：**

| 参数名          | 类型    | 必填 | 描述           |
|----------------|--------|-----|---------------|
| username       | String | 是  | 用户名         |
| password       | String | 是  | 密码           |
| captcha        | String | 是  | 验证码         |
| captchaId      | String | 是  | 验证码ID       |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "username": "newuser",
    "createTime": "2023-05-10 14:30:45"
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\Register.vue`
- **组件位置**：账号密码注册表单
- **函数调用**：`handleRegister()` 函数中通过 `registerByUsername` API 调用

### 1.4 手机号码注册

```
POST /api/users/registerByphone
```

**请求参数：**

| 参数名     | 类型    | 必填 | 描述       |
|-----------|--------|-----|-----------|
| phone     | String | 是  | 手机号     |
| code      | String | 是  | 短信验证码  |
| captcha   | String | 是  | 图片验证码  |
| captchaId | String | 是  | 验证码ID   |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "username": "user_85939522",
    "phone": "159****7777",
    "createTime": "2023-05-10 14:30:45"
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\Register.vue`
- **组件位置**：手机验证码注册表单
- **函数调用**：`handlePhoneRegister()` 函数中通过 `registerByPhone` API 调用

### 1.5 用户名密码登录

```
POST /api/users/loginByname
```

**请求参数：**

| 参数名       | 类型    | 必填 | 描述                     |
|-------------|--------|-----|-------------------------|
| username    | String | 是  | 用户名                    |
| password    | String | 是  | 密码                     |
| sliderToken | String | 否  | 滑块验证token（多次登录失败时需要）|
| sliderX     | Number | 否  | 滑块验证X坐标（多次登录失败时需要）|

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "username",
    "avatar": "https://example.com/avatar.jpg",
    "role": "user",
    "banned": false
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\Login.vue`
- **组件位置**：账号密码登录表单
- **函数调用**：`handleLogin()` 函数中通过 `loginByUsername` API 调用

### 1.6 手机号码登录

```
POST /api/users/loginByphone
```

**请求参数：**

| 参数名  | 类型    | 必填 | 描述      |
|--------|--------|-----|----------|
| phone  | String | 是  | 手机号    |
| code   | String | 是  | 短信验证码 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "username",
    "avatar": "https://example.com/avatar.jpg",
    "role": "user",
    "banned": false
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\Login.vue`
- **组件位置**：手机验证码登录表单
- **函数调用**：`handlePhoneLogin()` 函数中通过 `loginByPhone` API 调用

## 2. 用户资料接口

### 2.1 获取用户资料

```
GET /api/users/profile
```

**请求头：**

| 参数名        | 描述                       |
|--------------|---------------------------|
| Authorization| Bearer {token}            |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "username": "username",
    "email": "user@example.com",
    "phone": "159****7777",
    "avatar": "https://example.com/avatar.jpg",
    "bio": "这是我的个人简介",
    "registerTime": "2023-01-01 10:00:00",
    "lastLoginTime": "2023-05-10 14:30:00",
    "blogCount": 8
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\Profile.vue`
- **组件位置**：个人资料卡片
- **加载时机**：组件挂载时 `onMounted()` 钩子中

### 2.2 更新用户资料

```
PUT /api/users/profile
```

**请求头：**

| 参数名        | 描述                       |
|--------------|---------------------------|
| Authorization| Bearer {token}            |

**请求参数：**

| 参数名  | 类型    | 必填 | 描述        |
|--------|--------|-----|------------|
| email  | String | 否  | 用户邮箱     |
| bio    | String | 否  | 个人简介     |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "username": "username",
    "email": "new_email@example.com",
    "bio": "新的个人简介"
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\Profile.vue`
- **组件位置**：个人资料编辑表单
- **函数调用**：`saveProfile()` 函数中

### 2.3 上传用户头像

```
POST /api/users/avatar
```

**请求头：**

| 参数名        | 描述                       |
|--------------|---------------------------|
| Authorization| Bearer {token}            |
| Content-Type | multipart/form-data       |

**请求参数：**

| 参数名 | 类型   | 必填 | 描述      |
|-------|-------|-----|----------|
| file  | File  | 是  | 头像文件   |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "avatar": "https://example.com/avatars/new_avatar.jpg"
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\Profile.vue`
- **组件位置**：头像上传组件
- **函数调用**：`uploadAvatar()` 函数中

### 2.4 修改密码

```
PUT /api/users/password
```

**请求头：**

| 参数名        | 描述                       |
|--------------|---------------------------|
| Authorization| Bearer {token}            |

**请求参数：**

| 参数名        | 类型    | 必填 | 描述       |
|--------------|--------|-----|-----------|
| oldPassword  | String | 是  | 原密码      |
| newPassword  | String | 是  | 新密码      |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\Profile.vue`
- **组件位置**：修改密码对话框
- **函数调用**：`changePassword()` 函数中

### 2.5 修改手机号

```
PUT /api/users/phone
```

**请求头：**

| 参数名        | 描述                       |
|--------------|---------------------------|
| Authorization| Bearer {token}            |

**请求参数：**

| 参数名    | 类型    | 必填 | 描述        |
|----------|--------|-----|------------|
| newPhone | String | 是  | 新手机号     |
| code     | String | 是  | 短信验证码    |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "phone": "138****8000"
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\Profile.vue`
- **组件位置**：修改手机号对话框
- **函数调用**：`changePhone()` 函数中

## 3. 用户内容管理接口

### 3.1 获取用户博客列表

```
GET /api/blogs
```

**请求头：**

| 参数名        | 描述                       |
|--------------|---------------------------|
| Authorization| Bearer {token}            |

**请求参数：**

| 参数名    | 类型     | 必填 | 描述                   |
|----------|---------|-----|------------------------|
| keyword  | String  | 否  | 搜索关键词              |
| status   | String  | 否  | 状态：draft/published   |
| page     | Integer | 否  | 页码，默认1             |
| pageSize | Integer | 否  | 每页数量，默认10        |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 18,
    "list": [
      {
        "id": "1",
        "title": "我的第一篇博客",
        "summary": "这是摘要...",
        "createTime": "2023-05-01 10:00:00",
        "updateTime": "2023-05-02 14:30:00",
        "status": "published",
        "viewCount": 120,
        "commentCount": 5,
        "likeCount": 10
      }
    ]
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\ContentManagement.vue`
- **组件位置**：博客列表页面
- **函数调用**：组件挂载时以及搜索/筛选时

### 3.2 创建博客

```
POST /api/blogs
```

**请求头：**

| 参数名        | 描述                       |
|--------------|---------------------------|
| Authorization| Bearer {token}            |

**请求参数：**

| 参数名    | 类型    | 必填 | 描述                           |
|----------|--------|-----|-------------------------------|
| title    | String | 是  | 博客标题                        |
| content  | String | 是  | 博客内容，支持HTML或Markdown格式  |
| summary  | String | 否  | 摘要，不提供时系统自动截取        |
| tags     | Array  | 否  | 标签列表                        |
| status   | String | 否  | 状态：draft(草稿)/published(发布)|

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "123",
    "title": "新博客标题",
    "createTime": "2023-05-10 14:30:45"
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\BlogCreate.vue`
- **组件位置**：博客创建页面
- **函数调用**：提交表单时

### 3.3 获取博客详情

```
GET /api/blogs/{id}
```

**请求头：**

| 参数名        | 描述                       |
|--------------|---------------------------|
| Authorization| Bearer {token}            |

**请求参数：**

| 参数名 | 类型    | 必填 | 描述      |
|-------|--------|-----|----------|
| id    | String | 是  | 博客ID    |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "123",
    "title": "博客标题",
    "content": "<p>博客内容...</p>",
    "summary": "这是摘要...",
    "createTime": "2023-05-01 10:00:00",
    "updateTime": "2023-05-02 14:30:00",
    "status": "published",
    "tags": ["技术", "编程"],
    "viewCount": 120,
    "commentCount": 5,
    "likeCount": 10,
    "author": {
      "username": "author",
      "avatar": "https://example.com/avatar.jpg"
    }
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\BlogView.vue`
- **组件位置**：博客查看页面
- **函数调用**：组件挂载时

### 3.4 更新博客

```
PUT /api/blogs/{id}
```

**请求头：**

| 参数名        | 描述                       |
|--------------|---------------------------|
| Authorization| Bearer {token}            |

**请求参数：**

| 参数名    | 类型    | 必填 | 描述                           |
|----------|--------|-----|-------------------------------|
| id       | String | 是  | 博客ID (路径参数)                |
| title    | String | 否  | 博客标题                        |
| content  | String | 否  | 博客内容                        |
| summary  | String | 否  | 摘要                           |
| tags     | Array  | 否  | 标签列表                        |
| status   | String | 否  | 状态：draft/published          |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "123",
    "title": "更新后的标题",
    "updateTime": "2023-05-10 14:30:45"
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\BlogEdit.vue`
- **组件位置**：博客编辑页面
- **函数调用**：提交表单时

### 3.5 删除博客

```
DELETE /api/blogs/{id}
```

**请求头：**

| 参数名        | 描述                       |
|--------------|---------------------------|
| Authorization| Bearer {token}            |

**请求参数：**

| 参数名 | 类型    | 必填 | 描述      |
|-------|--------|-----|----------|
| id    | String | 是  | 博客ID    |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\ContentManagement.vue`
- **组件位置**：博客列表中的删除按钮
- **函数调用**：点击删除按钮时

## 4. 用户操作日志接口

### 4.1 获取登录日志

```
GET /api/users/login-logs
```

**请求头：**

| 参数名        | 描述                       |
|--------------|---------------------------|
| Authorization| Bearer {token}            |

**请求参数：**

| 参数名    | 类型     | 必填 | 描述             |
|----------|---------|-----|-----------------|
| page     | Integer | 否  | 页码，默认1       |
| pageSize | Integer | 否  | 每页数量，默认10  |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "list": [
      {
        "id": 1,
        "time": "2023-05-10 14:30:45",
        "ip": "192.168.1.1",
        "location": "北京",
        "device": "Windows Chrome",
        "status": "success"
      }
    ]
  }
}
```

**对应前端组件：**
- **文件路径**：`d:\blog\frontend-user\src\views\Profile.vue`
- **组件位置**：登录日志对话框
- **函数调用**：打开登录日志对话框时
