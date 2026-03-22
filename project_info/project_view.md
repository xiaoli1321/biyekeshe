# LearnCourse 项目概览 (Project View)

## 1. 项目简介
**LearnCourse** 是一个基于知识图谱驱动的个性化编程学习平台。该平台通过可视化的方式展示各个知识点之间的逻辑与先修依赖关系，帮助用户构建系统化的知识体系，并提供个性化的学习路径推荐。

项目采用了**前后端分离**架构，以保证流畅的用户体验和高效的数据处理能力。

## 2. 核心系统特性
*   **知识图谱可视化**：展示核心知识点映射、各学习阶段之间的前后置关联，交互式动态图形（力导向图）。
*   **个性化路径推演**：根据用户进度与图谱算法，智能推荐学习内容或相关复习路径。
*   **多元化结构管理**：支持多级层状内容结构，包含课程、章节内容以及相关的学习辅助资料。
*   **细粒度进度记录**：全面追踪学习状态数据，帮助用户直观监控所掌握技能节点及盲区。
*   **完备的权限隔离**：基于 JWT 无状态校验，区别普通学习者功能与系统管理员的内容编排权限。

## 3. 架构与技术栈选型

### 3.1 前端应用系统 (`learn-platform-frontend`)
*   **核心框架**：Vue 3.4 (采用 Composition API) + TypeScript
*   **应用构建工具**：Vite 5.2
*   **状态与路由管理**：Pinia 2.1 (跨组件状态)、Vue Router 4.3 (SPA 路由调度)
*   **UI 与基础组件**：Bootstrap 5.3 + Bootstrap Icons
*   **图表与可视化**：ECharts 6.0 结合 D3.js，主要处理复杂数据可视化（例如图谱渲染等）
*   **API 请求处理**：Axios 1.6

### 3.2 后端服务架构 (`src/main/java/...`)
*   **核心应用框架**：Spring Boot 4.0.2 (依托 Java 17)
*   **数据持久化**：MongoDB 6.0+（利用 Spring Data MongoDB 执行 ORM 映射与查询）
*   **请求安全认证**：Spring Security 集成 JWT（无状态认证架构）
*   **WebFlux 集成**：包含针对响应式编程（如 SSE 数据流）的支持，提升某些功能（大语言模型请求或进度推流）的实时并发处理能力
*   **辅助工具库**：ModelMapper（处理 DTO 对象快速转换）、SpringDoc OpenAPI 3（用于生成 Swagger API 自动文档）、Maven（构建生命周期管理）

## 4. 关键项目模块设计
*   **业务逻辑实现 (`controller` -> `service` -> `repository`)**：
    *   `Auth`：注册、登录授权中心，生成和验证 Token。
    *   `Course/Chapter`：课程和具体章节的 CRUD 与检索、分页等功能。
    *   `Dashboard`：用户控制台的数据整合，统计图表展示。
    *   `Graph`：系统知识图谱核心接口与处理逻辑。
*   **前端工程模块**：
    *   `components`：通用的导航栏（NavBar）、页脚、加载等共用组件。
    *   `views`：主要业务视图，包括 Login、Register、Dashboard、CourseList/Detail、ChapterView、KnowledgeGraph。
    *   `stores`：Auth、Course、Dashboard 模块的 Pinia 状态树。

## 5. 开发调试与运行环境
系统当前处于全栈连通阶段。
*   **后端调试端口**：默认 `8084` (`application.properties`)
*   **前端开发端口**：默认 `3000` (配置于 Vite)
*   **数据库接入点**：默认由本地的 MongoDB 服务 `mongodb://localhost:27018/learning_platform` 提供支持。
*   **文档与接口列表**：可在服务运行后访问 `http://localhost:8084/swagger-ui/index.html` 阅览。
*   **环境初始化**：使用了一键脚本 `START_SYSTEM.bat`，可实现便捷的开箱测试。

## 6. 核心功能及对应 API 接口
以下是项目中各个核心学习功能模块及其对应调用的主要后端 API 接口概览：

### 6.1 认证与用户管理 (AuthApiController)
主要处理用户登录、注册、注销及个人信息获取功能，采用无状态的 JWT 方案。
*   `POST /api/auth/login`：用户登录，验证邮箱与密码，通过后返回 JWT 的令牌 Token。
*   `POST /api/auth/register`：用户注册，创建新的用户账户。
*   `GET /api/auth/profile`：获取当前登录用户的详细个人基本信息与角色权限。
*   `POST /api/auth/logout`：用户登出，前端执行 Token 清除策略。

### 6.2 课程管理 (CourseApiController)
主要涵盖全局课程库的浏览、检索，以及管理员后台对课程的编排与调度能力。
*   **普通功能**：
    *   `GET /api/courses`：获取课程列表，支持根据关键词或难度级别进行分页检索查询。
    *   `GET /api/courses/{id}`：获取单个指定课程的详情概览信息。
    *   `GET /api/courses/{courseId}/chapters`：获取某特定课程内下挂的结构化章节列表。
    *   `GET /api/courses/{courseId}/progress`：请求当前登录用户在该特定课程中的总体学习进度。
    *   `GET /api/courses/popular`：获取全局最热门推荐的课程列表榜单。
    *   `GET /api/courses/search`：基于传入的关键词参数进行全局范围搜索特定课程。
*   **管理员专有接口**：
    *   `GET /api/courses/all`：请求所有课程清单（包括草稿与未上架的）。
    *   `POST /api/courses`：根据表单数据创建新的草稿状态课程。
    *   `PUT /api/courses/{id}`：对现有课程内容和基础设定进行更新修改。
    *   `DELETE /api/courses/{id}`：彻底删除指定课程及其连带内容资产。
    *   `POST /api/courses/{id}/toggle-publish`：切换单课状态，发布（外部可见）或下架。

### 6.3 章节与学习跟进记录 (ChapterApiController)
深入至课程微观环节，管理章节详细讲义阅读、用户进度锚点与个人的学习日志（随堂记录）。
*   **普通功能**：
    *   `GET /api/chapters/{id}`：调取请求这一章节的具体详细内容及挂靠的多媒体资源。
    *   `POST /api/chapters/{chapterId}/progress`：向后端系统提交更新目前用户在某章节的耗时（分钟，elapsedMinutes）以及是否完成（completed）。
    *   `GET /api/chapters/{chapterId}/progress`：获取当前用户在该细分章节的学习完成率历史快照。
    *   `PUT /api/chapters/{chapterId}/notes`：保存当前用户在学习这一章节过程中创建的随处笔记体裁。
    *   `GET /api/chapters/{id}/prerequisites`：基于其关联概念的知识图谱，智能查询并返回：如果在阅读本章节觉得有些吃力，系统建议您需要优先梳理一遍的一组“前置引导知识点”列表。
*   **管理员专有接口**：
    *   `POST /api/chapters/course/{courseId}`：向现存某课程内增加一个新结构章节模块。
    *   `PUT /api/chapters/{id}` / `DELETE /api/chapters/{id}`：维护或摘除单个章节属性资料。

### 6.4 智慧引导控制台 (DashboardApiController)
主要服务于用户首页或主面板（Dashboard），用于生成针对个人的聚合学习简报与专属成长引航策略。
*   `GET /api/dashboard/stats`：收集并产出当前用户的总体学习统计数据面板仪表（包含：总公开课规模、自己看过的通关章节数、学习累计转化耗时图表分析等核心元信息）。
*   `GET /api/dashboard/recommended-courses`：系统智能算法逻辑，基于用户之前所处阶段与历史学习特性偏好为其自动生成专属推荐的几门课程。
*   `GET /api/dashboard/learning-path/{courseId}`：利用系统的“选课机制”与知识图谱依赖逻辑运算树，专门为某个大课程生成一条符合此人的最佳继续深入的后续“学习路径图/进阶清单”预测。
*   `GET /api/dashboard/learning-suggestions`：提取产出自然文字化维度的针对性学习策略或告警文字建议短语（例如“建议先巩固XX点”等）。

### 6.5 知识图谱运算中枢 API (GraphApiController)
这是本教培平台最核心驱动层 API，直接暴露底层能力模型操作算法给包含 D3 在内的各类视图组件，令抽象内容直观具象化。
*   `GET /api/graphs/concepts/{courseId}`：拉取某一完整课程所蕴藏对应的抽象、有向图状（树图交叉）的核心知识面概念拓扑集结构（数据节点集合 list nodes，关系边集合 list edges）。
*   `POST /api/graphs/generate-path`：客户端（可能提供用户目标）在界面上发出请求后，底层计算在包含已习状态和未来图谱目标的加权状态下，输出一张专门反映“最佳连通学习顺序”的新路径子图节点树图。
*   `GET /api/graphs/related-concepts`：用户自由图谱漫游与发散工具，给定一个核心知识点坐标，向外扩散询问它的直系相关关联点，方便展开延伸视野阅读。
*   `GET /api/graphs/concept-detail`：点按大图谱某单个悬浮气泡节点时，调起此接口获取详细的唯一词条（概念百科知识）说明小窗数据。
