# Learning Platform Frontend

基于 Vue 3 + TypeScript + Vite 的前端应用，为学习平台提供现代化的用户体验。

## 功能特性

- 🎨 Vue 3 组合式 API
- 📝 TypeScript 类型安全
- 🚀 Vite 构建工具
- 🗄️ Pinia 状态管理
- 🌐 Vue Router 4 路由
- 🔥 Bootstrap 5 UI 框架
- 📡 Axios HTTP 客户端

## 项目结构

learn-platform-frontend/
├── public/             # 静态资源
├── src/               # 应用源码
│   ├── assets/       # 资源文件
│   ├── components/   # 组件
│   ├── views/        # 页面
│   ├── router/       # 路由配置
│   ├── stores/       # 状态管理
│   ├── services/     # API 服务
│   ├── types/        # 类型定义
│   ├── utils/        # 工具函数
│   ├── App.vue       # 根组件
│   └── main.ts       # 应用入口
└── index.html

## 开发

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

## API 集成

该前端应用与后端 API 通信，后端运行在 http://localhost:8084。

详细 API 文档可通过 http://localhost:8084/swagger-ui/index.html 查看。

## 技术栈

- **框架**: Vue 3
- **语言**: TypeScript
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router
- **UI**: Bootstrap 5
- **HTTP**: Axios
- **图标**: Bootstrap Icons