# 博客系统前端

## 项目运行说明

### 前端启动
```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 后端启动
请确保后端服务已正确启动：

1. 导航到后端项目目录：
```bash
cd d:\work\blog-frontend\Byte_Blog-master\blog_web
```

2. 启动Spring Boot应用：
```bash
mvn spring-boot:run
```
或者在IDE中运行主应用类

### 连接配置
前端默认连接到 `http://localhost:8081`，如需修改，请更新 `src/api/config.js` 中的 `BASE_URL` 配置。

## 开发说明

### 模拟数据
当后端服务不可用时，系统会自动使用模拟数据。这在开发阶段非常有用，但在生产环境中应确保后端服务始终可用。

### 测试账号
- 管理员: admin / 123456
- 普通用户: user / 123456
```
