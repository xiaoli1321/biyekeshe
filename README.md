
# 基于知识图谱的编程学习平台

## 项目概述

本项目是一个基于Spring Boot的知识图谱驱动编程学习平台，旨在通过可视化的知识图谱帮助用户理解学习路径和知识点之间的先修关系。

## 技术架构

### 后端技术
- **框架**: Spring Boot 4.0.2
- **安全**: Spring Security
- **数据访问**: Spring Data MongoDB
- **模板引擎**: Thymeleaf
- **数据库**: MongoDB
- **构建工具**: Maven

### 前端技术
- **UI框架**: Bootstrap 5.3
- **图标**: Bootstrap Icons
- **图谱可视化**: D3.js v7
- **图表**: Chart.js

### 核心功能

1. **用户认证系统**
   - 用户注册/登录
   - 基于Spring Security的身份验证和授权
   - 密码加密存储 (BCrypt)

2. **课程管理**
   - 课程的创建、编辑、删除
   - 分页显示课程列表
   - 按难度等级和关键词筛选
   - 课程发布控制

3. **章节学习**
   - 章节内容展示
   - 学习进度跟踪
   - 学习笔记功能
   - 章节导航

4. **知识图谱可视化**
   - D3.js力导向图可视化
   - 知识点关关系展示
   - 先修依赖路径可视化
   - 交互式图谱操作

5. **学习路径推荐**
   - 基于知识图谱的个性化推荐
   - 学习进度分析
   - 智能学习建议

## 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse)

### 安装与运行

1. **克隆项目**
```bash
git clone <repository-url>
cd knowledge-learning-platform
```

2. **配置数据库**
项目使用MongoDB作为数据库。请确保已安装并启动MongoDB服务（默认端口27017）。MongoDB连接配置在`application.properties`中。

3. **构建和运行**
```bash
# 编译项目
mvn clean compile

# 运行应用程序
mvn spring-boot:run
```

4. **访问应用**
- 网址: http://localhost:8084
- 管理员账号: admin / admin123
- 普通用户: user / password

### 测试运行

您可以使用提供的演示账号登录：

1. **管理员**:
   - 用户名: admin
   - 密码: admin123
   - 权限: 完整的课程管理和知识图谱管理权限

2. **用户**:
   - 用户名: user
   - 密码: password
   - 权限: 学习课程和查看个人进度

## 数据库设计

主要数据结构：

- **users**: 用户信息
- **courses**: 课程信息
- **chapters**: 章节内容
- **concepts**: 知识点 (知识图谱节点)
- **relationships**: 知识点关系 (知识图谱边)
- **progress**: 学习进度

## 项目结构

```
src/main/java/com/learnplatform/
├── config/          # 配置类
│   ├── JpaConfig
│   ├── SecurityConfig
│   └── DataInitializer
├── controller/      # 控制器层
│   ├── AuthController
│   ├── CourseController
│   ├── ChapterController
│   ├── DashboardController
│   └── GraphController
├── service/         # 业务逻辑层
│   ├── UserService
│   ├── CourseService
│   ├── ChapterService
│   ├── ProgressService
│   ├── ConceptService
│   ├── RelationshipService
│   ├── GraphService
│   └── LearningPathService
├── repository/      # 数据访问层
│   ├── UserRepository
│   ├── CourseRepository
│   ├── ChapterRepository
│   ├── ConceptRepository
│   ├── RelationshipRepository
│   └── ProgressRepository
├── entity/          # 实体类
│   ├── User
│   ├── Course
│   ├── Chapter
│   ├── Concept
│   ├── Relationship
│   └── Progress
├── dto/             # 数据传输对象
│   └── GraphData
└── LearnCourseApplication.java

src/main/resources/
├── application.properties
├── templates/
│   ├── layout/          # 布局模板
│   ├── auth/            # 认证页面
│   ├── courses/         # 课程页面
│   ├── dashboard/       # 用户仪表板
│   ├── knowledge-graph/ # 知识图谱页面
│   └── fragments/       # 可复用片段
└── static/
    ├── css/             # 样式文件
    └── js/              # JavaScript文件
```

## 主要功能使用指南

### 1. 用户注册与登录
- 访问首页，系统会重定向到登录页
- 新用户需先在 `/register` 完成注册流程
- 已有账号的用户在登录页输入用户名/邮箱和密码

### 2. 课程浏览
- 点击导航栏中的"课程"进入课程中心
- 使用搜索框和难度筛选器查找课程
- 点击课程卡片进入课程详情页

### 3. 学习章节
- 在课程详情页选择要学习的章节
- 点击"开始学习"进入章节学习页面
- 完成学习后点击"标记为已完成"

### 4. 知识图谱可视化
- 管理员可通过导航栏进入"知识图谱"页面
- 选择课程查看其知识点关系图谱
- 支持缩放、拖拽和点击查看节点详情

### 5. 学习路径推荐
- 在个人信息中查看推荐课程
- 基于学习历史生成个性化学习路径

## 核心特色功能

### D3.js知识图谱可视化
- 力导向图算法实现
- 支持缩放、拖拽和点击交互
- 根据知识点的重要程度动态调整节点大小

### 学习路径推荐算法
- 考虑先修关系排序
- 结合用户的学习进度和知识水平
- 提供个性化的学习建议

## 开发测试说明

### 环境配置
在 `resources/application.properties` 中可修改配置：
- MongoDB连接URI：`spring.data.mongodb.uri=mongodb://localhost:27017/learning_platform`
- 服务器端口：`server.port=8084`

### 数据库管理工具
推荐使用以下工具管理MongoDB数据库：
- **MongoDB Compass**（官方GUI工具）：https://www.mongodb.com/products/compass
- **命令行**: 使用 `mongosh` 连接到 `mongodb://localhost:27017`
- 数据库名称: `learning_platform`

## 扩展功能建议

1. **用户个性化**
   - 学习偏好设置
   - 每日学习计划
   - 学习提醒邮件

2. **社交学习**
   - 学习小组
   - 讨论区
   - 用户进度分享

3. **评测系统**
   - 在线练习题
   - 代码提交和测试
   - 成绩分析

4. **高级学习分析**
   - 学习报告生成
   - 薄弱知识点识别
   - 学习效率评估

## 常见问题

### Q: 如何更换MongoDB连接？
A: 在 `application.properties` 中修改 `spring.data.mongodb.uri`，格式为 `mongodb://[username:password@]host[:port]/database`。

### Q: 如何添加新用户角色？
A: 在 `User` 实体类中扩展 `UserRole` 枚举，在 `SecurityConfig` 中配置相应的URL授权规则。

### Q: 知识图谱不显示？
A: 检查浏览器控制台是否有D3.js加载错误，确保已为课程添加知识点和关系数据。

## 致谢

感谢Spring Boot、Spring Security、D3.js和Bootstrap等开源项目。

## 许可证

本项目采用 MIT 许可证 - 详见 LICENSE 文件。