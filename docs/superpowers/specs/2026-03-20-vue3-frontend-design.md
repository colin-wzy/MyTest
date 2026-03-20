# Vue 3 前端项目设计

**日期**: 2026-03-20
**后端项目**: cn.colin Spring Boot 后端
**状态**: 已批准

---

## 1. 技术栈

| 层级 | 技术选型 | 说明 |
|------|----------|------|
| 构建工具 | Vite | 快速启动，热更新 |
| 前端框架 | Vue 3 | Composition API + `<script setup>` |
| UI 组件库 | Element Plus | Vue 3 企业级组件库 |
| 状态管理 | Pinia | 轻量级，官方推荐 |
| HTTP 客户端 | Axios | 请求拦截、响应拦截 |
| 路由 | Vue Router 4 | 路由守卫、Token 校验 |
| 视觉风格 | 深色科技风 | 暗色背景 + 紫蓝渐变按钮 |

---

## 2. 项目结构

```
src/
├── api/                    # Axios 封装 + API 方法
│   ├── axios.ts           # Axios 实例、拦截器
│   ├── user.ts            # 用户相关 API
│   └── file.ts            # 文件相关 API
├── assets/
│   └── styles/
│       ├── variables.css  # CSS 变量（颜色、字体）
│       └── global.css     # 全局样式
├── components/            # 通用组件
│   └── ...
├── layouts/
│   └── MainLayout.vue     # 主布局（侧边栏 + 内容区）
├── router/
│   └── index.ts           # 路由配置 + 路由守卫
├── stores/                # Pinia stores
│   ├── user.ts            # 用户状态
│   └── file.ts            # 文件状态
├── utils/                 # 工具函数
│   └── token.ts           # Token 存取
└── views/                 # 页面
    ├── Login.vue          # 登录页
    └── User/
        ├── UserList.vue   # 用户列表
        ├── UserAdd.vue    # 新增用户
        └── UserInfo.vue   # 用户详情
    └── File/
        └── FileManage.vue # 文件管理
```

---

## 3. 页面路由

| 路径 | 页面 | 权限 |
|------|------|------|
| `/login` | 登录页 | 公开 |
| `/` | 跳转 `/user` 或 `/login` | - |
| `/user` | 用户列表 | 需登录 |
| `/user/add` | 新增用户 | 需登录 + ADMIN角色 |
| `/user/info/:id` | 用户详情 | 需登录 |
| `/file` | 文件管理 | 需登录 |

---

## 4. 页面功能

### 4.1 登录页 `/login`

- 用户名 + 密码表单
- 登录按钮调用 `POST /user/login`
- 成功 → 保存 JWT → 跳转 `/user`
- 失败 → 显示错误信息
- 背景：深色科技风

### 4.2 用户列表 `/user`

- 表格展示用户（用户名、姓名、性别、创建时间）
- 搜索框：按用户名搜索
- 分页：Element Plus 分页组件
- 操作列：查看详情、删除
- 新增用户按钮 → 跳转 `/user/add`

### 4.3 新增用户 `/user/add`

- 表单：用户名、姓名、密码、性别
- 校验：用户名 4-20 位字母数字下划线，密码 6-20 位
- 提交调用 `POST /user/addUser`
- 成功 → 返回用户列表

### 4.4 用户详情 `/user/info/:id`

- 展示用户完整信息
- 返回按钮

### 4.5 文件管理 `/file`

- 上传区域：拖拽上传或点击上传
- 文件列表：文件名、大小、上传时间、操作
- 操作：下载、预览（图片直接预览）
- 调用后端 MinIO 相关接口

---

## 5. Token 处理

### 5.1 登录流程
1. 调用 `POST /user/login` 获取 JWT
2. 保存到 `localStorage.token`
3. 保存用户名到 `localStorage.username`

### 5.2 Axios 拦截器
- **请求拦截**：所有请求自动携带 `Authorization: Bearer <token>`
- **响应拦截**：
  - 200 → 直接返回
  - 401 → 尝试刷新 Token → 刷新成功重试 → 失败跳转登录
  - 其他 → 提示错误

### 5.3 路由守卫
- 跳转前检查 `localStorage.token`
- 无 token → 跳转 `/login`
- 有 token → 放行

---

## 6. 深色科技风样式

```css
/* 背景色 */
--bg-primary: #0f172a;
--bg-secondary: #1e293b;
--bg-card: #1e293b;

/* 文字色 */
--text-primary: #f1f5f9;
--text-secondary: #94a3b8;

/* 强调色 */
--accent-primary: #6366f1;   /* 紫蓝渐变起点 */
--accent-secondary: #8b5cf6; /* 渐变终点 */

/* 按钮渐变 */
background: linear-gradient(135deg, #6366f1, #8b5cf6);
```

---

## 7. API 接口对接

### 7.1 用户接口

| 前端方法 | 后端 API | 说明 |
|----------|----------|------|
| `userLogin(username, pwd)` | `POST /user/login` | 登录 |
| `getUserList(params)` | `POST /user/findUserByName` | 搜索用户 |
| `getUserById(id)` | `POST /user/findUserById/{id}` | 获取用户 |
| `addUser(data)` | `POST /user/addUser` | 新增用户 |
| `deleteUser(id)` | `POST /user/deleteUserById/{id}` | 删除用户 |
| `getCurrentUser()` | `POST /user/findCurrentUser` | 当前用户 |

### 7.2 响应格式
```json
{
  "code": "000000",
  "msg": "success",
  "data": { ... }
}
```
- `code === '000000'` → 成功
- `code !== '000000'` → 失败，msg 为错误信息

---

## 8. 环境要求

- Node.js >= 18
- npm >= 9
- 后端运行在 `http://localhost:8888`

---

## 9. 开发命令

```bash
# 安装依赖
npm install

# 开发启动
npm run dev

# 构建生产
npm run build
```
