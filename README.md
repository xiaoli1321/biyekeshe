# LearnCourse - 基于知识图谱的编程学习平台

## 项目简介
LearnCourse 是一个基于知识图谱驱动的个性化编程学习平台。该平台通过可视化的知识图谱展示知识点之间的逻辑关系和先修依赖，帮助学习者构建系统化的知识体系，并提供个性化的学习路径推荐。

项目采用**前后端分离**架构，提供流畅的用户体验和高效的数据处理能力。

---

## 技术架构

### 后端技术栈 (Spring Boot)
- **核心框架**: Spring Boot 4.0.2
- **安全框架**: Spring Security (基于 JWT 的无状态认证)
- **数据存储**: MongoDB (Spring Data MongoDB)
- **对象映射**: ModelMapper
- **API 文档**: SpringDoc OpenAPI (Swagger UI)
- **构建工具**: Maven
- **核心功能**: 知识图谱算法、学习路径规划、进度追踪、RESTful API

### 前端技术栈 (Vue 3)
- **核心框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由管理**: Vue Router
- **网络请求**: Axios
- **UI 组件**: Bootstrap 5.3 + Bootstrap Icons
- **可视化**: D3.js (用于知识图谱展示)
- **语言**: TypeScript

---

## 核心功能
1.  **知识图谱可视化**: 交互式力导向图，展示知识点、关系及先修路径。
2.  **个性化学习路径**: 根据用户进度和知识图谱自动生成最优学习方案。
3.  **课程与章节管理**: 系统化的课程结构，支持多级章节和学习资料展示。
4.  **进度追踪系统**: 实时记录学习状态，可视化展示已掌握和待学习的知识点。
5.  **权限管理系统**: 区分管理员和普通用户，支持课程发布、知识图谱编辑等管理功能。

---

## 快速开始

### 环境要求
- Java 17+
- Node.js 18+
- MongoDB 6.0+ (默认端口 27018)
- Maven 3.6+

### 1. 数据库配置
确保 MongoDB 已启动。根据 `src/main/resources/application.properties` 中的配置：
- **Host**: localhost
- **Port**: 27018
- **Database**: learning_platform
- **Credentials**: root / root (authSource: admin)

### 2. 后端启动
```bash
# 进入根目录
mvn clean spring-boot:run
```
后端 API 地址: `http://localhost:8084`
Swagger UI: `http://localhost:8084/swagger-ui/index.html`

### 3. 前端启动
```bash
# 进入前端目录
cd learn-platform-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```
前端访问地址: `http://localhost:3000`

---

## 演示账号
| 角色 | 账号 (Email) | 密码 | 权限 |
| :--- | :--- | :--- | :--- |
| 管理员 | `admin@learning.com` | `admin123` | 全量权限 (管理课程、图谱、用户) |
| 普通用户 | `user@learning.com` | `password` | 学习权限 (查看课程、图谱、进度) |

---

## 项目结构
```text
learn__course/
├── src/main/java/          # 后端源码
│   └── com.learnplatform/
│       ├── config/         # 系统配置 (Security, Mongo, OpenAPI)
│       ├── controller/     # REST 控制器
│       ├── dto/            # 数据传输对象
│       ├── entity/         # 领域模型 (MongoDB 文档)
│       ├── repository/     # 数据访问层
│       ├── security/       # JWT 与安全逻辑
│       └── service/        # 业务逻辑层
├── learn-platform-frontend/# 前端源码 (Vue 3 SPA)
│   ├── src/
│   │   ├── components/     # 公用组件
│   │   ├── services/       # API 请求
│   │   ├── stores/         # Pinia 状态管理
│   │   └── views/          # 页面视图
│   └── package.json
└── pom.xml                 # Maven 配置
```
