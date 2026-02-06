package com.learnplatform.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB配置类
 * 启用MongoDB审计和仓储功能
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.learnplatform.repository")
@EnableMongoAuditing
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "learning_platform";
    }

    @Override
    public MongoClient mongoClient() {
        // 显式配置连接字符串，强制使用 27018 端口和认证信息
        // 这里的 authSource=admin 对应我们在 Docker 中创建用户的方式
        ConnectionString connectionString = new ConnectionString("mongodb://root:root@localhost:27018/learning_platform?authSource=admin");
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(mongoClientSettings);
    }
}