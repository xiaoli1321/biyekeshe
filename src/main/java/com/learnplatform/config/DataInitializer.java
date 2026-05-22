package com.learnplatform.config;

import com.learnplatform.entity.*;
import com.learnplatform.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;



/**
 * 数据初始化器
 * 在应用启动时自动创建示例数据
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository,
                              CourseRepository courseRepository,
                              ChapterRepository chapterRepository,
                              ConceptRepository conceptRepository,
                              RelationshipRepository relationshipRepository,
                              ProgressRepository progressRepository,
                              AgentRepository agentRepository,
                              PasswordEncoder passwordEncoder) {
        return args -> {
            // 开发环境 - 只在用户不存在时创建，避免 ID 变化导致关联数据失效
            if (userRepository.count() == 0) {
                // 创建管理员用户 - 使用PasswordEncoder加密密码admin123
                User admin = new User("admin", "admin@learning.com",
                        passwordEncoder.encode("admin123"), User.UserRole.ADMIN);
                admin.setEnabled(true);
                userRepository.save(admin);
                System.out.println("✓ 管理员用户已重新初始化: admin@learning.com / admin123");

                // 创建普通用户 - 使用PasswordEncoder加密密码password
                User user = new User("user", "user@learning.com",
                        passwordEncoder.encode("password"), User.UserRole.USER);
                user.setEnabled(true);
                userRepository.save(user);
                System.out.println("✓ 普通用户已重新初始化: user@learning.com / password");
            } else {
                System.out.println("✓ 用户数据已存在，跳过初始化 (保持 ID 稳定)");
            }

            // 创建示例课程
            if (courseRepository.count() == 0) {
                Course javaCourse = new Course("Java编程基础", "从头学习Java编程语言的基础知识，包括语法、面向对象编程和核心库。", Course.DifficultyLevel.BEGINNER);
                javaCourse.setPublished(true);
                javaCourse.setInstructor("张老师");
                javaCourse.setEstimatedHours(40);
                javaCourse.setTags("Java,编程,面向对象,入门");
                javaCourse = courseRepository.save(javaCourse);

                // 创建章节
                Chapter chapter1 = new Chapter(javaCourse, "Java简介与环境配置", "Java语言的介绍，包括JVM、JDK和JRE的概念，以及如何配置开发环境。", 1);
                chapter1.setType(Chapter.ChapterType.TEXT);
                chapter1.setEstimatedMinutes(30);
                chapter1.setSkippable(false);
                chapterRepository.save(chapter1);

                Chapter chapter2 = new Chapter(javaCourse, "Java基础语法", "Java的基本语法，包括变量、数据类型、运算符等。", 2);
                chapter2.setType(Chapter.ChapterType.TEXT);
                chapter2.setEstimatedMinutes(45);
                chapter2.setSkippable(false);
                chapterRepository.save(chapter2);

                Chapter chapter3 = new Chapter(javaCourse, "面向对象编程", "Java的面向对象特性，包括类、对象、继承、封装和多态。", 3);
                chapter3.setType(Chapter.ChapterType.TEXT);
                chapter3.setEstimatedMinutes(60);
                chapter3.setSkippable(false);
                chapterRepository.save(chapter3);

                // 创建知识点
                Concept concept1 = new Concept(javaCourse, chapter1, "JVM (Java虚拟机)", "JVM是Java Virtual Machine的缩写，是Java平台的核心组件，负责执行字节码。");
                concept1.setDifficultyLevel(2);
                concept1.setImportanceWeight(80);
                conceptRepository.save(concept1);

                Concept concept2 = new Concept(javaCourse, chapter1, "JDK (Java开发工具包)", "JDK是Java Development Kit的缩写，包含JRE和开发工具。");
                concept2.setDifficultyLevel(2);
                concept2.setImportanceWeight(70);
                conceptRepository.save(concept2);

                Concept concept3 = new Concept(javaCourse, chapter2, "Java变量", "Java中用于存储数据和值的命名容器。");
                concept3.setDifficultyLevel(1);
                concept3.setImportanceWeight(85);
                conceptRepository.save(concept3);

                Concept concept4 = new Concept(javaCourse, chapter2, "数据类型", "Java中的基本数据类型和引用数据类型。");
                concept4.setDifficultyLevel(1);
                concept4.setImportanceWeight(90);
                conceptRepository.save(concept4);

                Concept concept5 = new Concept(javaCourse, chapter3, "类和对象", "Java中面向对象编程的基础概念。");
                concept5.setDifficultyLevel(3);
                concept5.setImportanceWeight(95);
                conceptRepository.save(concept5);

                Concept concept6 = new Concept(javaCourse, chapter3, "继承", "Java中实现代码重用的机制，允许子类继承父类的属性和方法。");
                concept6.setDifficultyLevel(3);
                concept6.setImportanceWeight(85);
                conceptRepository.save(concept6);

                Concept concept7 = new Concept(javaCourse, chapter3, "多态", "面向对象编程的另一个重要概念，允许不同的对象对同一消息做出响应。");
                concept7.setDifficultyLevel(4);
                concept7.setImportanceWeight(90);
                conceptRepository.save(concept7);

                // 创建知识关系
                Relationship rel1 = new Relationship(concept2, concept1, Relationship.RelationshipType.USES, 0.9, "JDK使用JVM执行代码");
                relationshipRepository.save(rel1);

                Relationship rel2 = new Relationship(concept4, concept3, Relationship.RelationshipType.PREREQUISITE, 0.8, "变量依赖于数据类型");
                relationshipRepository.save(rel2);

                Relationship rel3 = new Relationship(concept3, concept5, Relationship.RelationshipType.PREREQUISITE, 0.7, "类和对象使用变量");
                relationshipRepository.save(rel3);

                Relationship rel4 = new Relationship(concept5, concept6, Relationship.RelationshipType.PREREQUISITE, 0.9, "继承基于类的概念");
                relationshipRepository.save(rel4);

                Relationship rel5 = new Relationship(concept5, concept7, Relationship.RelationshipType.PREREQUISITE, 0.8, "多态使用类的概念");
                relationshipRepository.save(rel5);

                Relationship rel6 = new Relationship(concept7, concept6, Relationship.RelationshipType.USES, 0.7, "多态常与继承结合使用");
                relationshipRepository.save(rel6);

                System.out.println("Created Java programming course with chapters and concepts");
            }

            // 创建另一个示例课程（前端开发）
            if (courseRepository.count() < 2) {
                Course webCourse = new Course("前端开发入门", "学习HTML、CSS和JavaScript基础，开启前端开发之旅。", Course.DifficultyLevel.BEGINNER);
                webCourse.setPublished(true);
                webCourse.setInstructor("李老师");
                webCourse.setEstimatedHours(30);
                webCourse.setTags("前端,HTML,CSS,JavaScript,Web");
                webCourse = courseRepository.save(webCourse);

                Chapter wChapter1 = new Chapter(webCourse, "HTML基础", "学习HTML标记语言的基本结构和常用标签。", 1);
                wChapter1.setType(Chapter.ChapterType.TEXT);
                wChapter1.setEstimatedMinutes(40);
                wChapter1.setSkippable(false);
                chapterRepository.save(wChapter1);

                Chapter wChapter2 = new Chapter(webCourse, "CSS样式", "学习CSS选择器、属性和布局。", 2);
                wChapter2.setType(Chapter.ChapterType.TEXT);
                wChapter2.setEstimatedMinutes(45);
                wChapter2.setSkippable(false);
                chapterRepository.save(wChapter2);

                System.out.println("Created frontend development course");
            }

            // --- AgentX 默认智能体初始化 ---
            if (agentRepository.count() == 0) {
                User admin = userRepository.findByEmail("admin@learning.com").orElse(null);
                if (admin != null) {
                    Agent dsAgent = new Agent("DeepSeek V3 助手", 
                        "你是一个全能的编程和生活助手，由 DeepSeek 提供动力。请以专业、友好的姿态回答用户问题。", 
                        "default-v3", admin.getId());
                    dsAgent.setDescription("基于 DeepSeek V3 的全能型 AI 助手，擅长编程、写作和逻辑分析。");
                    dsAgent.setWelcomeMessage("你好！我是您的 DeepSeek 助手。今天有什么可以帮您的？");
                    dsAgent.setEnabled(true);
                    agentRepository.save(dsAgent);

                    Agent javaAgent = new Agent("Java 学习导师", 
                        "你是一个资深的 Java 开发工程师和教育者。你的目标是帮助学生理解 Java 的核心概念、最佳实践和底层原理。", 
                        "default-v3", admin.getId());
                    javaAgent.setDescription("专注于 Java 编程教学的 AI 导师，能够深入浅出地讲解 JVM、并发模型和设计模式。");
                    javaAgent.setWelcomeMessage("你好，同学！我是你的 Java 学习导师。让我们一起攻克 Java 的难点吧！");
                    javaAgent.setEnabled(true);
                    agentRepository.save(javaAgent);

                    System.out.println("✓ AgentX 默认智能体已初始化");
                }
            }

            normalizeSampleGraphRelationships(relationshipRepository);
        };
    }

    private void normalizeSampleGraphRelationships(RelationshipRepository relationshipRepository) {
        relationshipRepository.findByRelationshipType(Relationship.RelationshipType.PREREQUISITE)
                .forEach(rel -> {
                    String description = rel.getDescription();
                    if (description == null) {
                        return;
                    }

                    boolean shouldSwap =
                            ("变量依赖于数据类型".equals(description)
                                    && "变量".equals(rel.getFromConcept().getName())
                                    && "数据类型".equals(rel.getToConcept().getName()))
                            || ("类和对象使用变量".equals(description)
                                    && "类和对象".equals(rel.getFromConcept().getName())
                                    && "变量".equals(rel.getToConcept().getName()))
                            || ("继承基于类的概念".equals(description)
                                    && "继承".equals(rel.getFromConcept().getName())
                                    && "类和对象".equals(rel.getToConcept().getName()))
                            || ("多态使用类的概念".equals(description)
                                    && "多态".equals(rel.getFromConcept().getName())
                                    && "类和对象".equals(rel.getToConcept().getName()));

                    if (!shouldSwap) {
                        return;
                    }

                    Concept originalFrom = rel.getFromConcept();
                    rel.setFromConcept(rel.getToConcept());
                    rel.setToConcept(originalFrom);
                    relationshipRepository.save(rel);
                    System.out.println("✓ 已纠正示例知识图谱关系方向: " + description);
                });
    }
}
