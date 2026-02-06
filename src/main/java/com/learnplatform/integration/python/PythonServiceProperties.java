package com.learnplatform.integration.python;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Python 微服务配置
 */
@ConfigurationProperties(prefix = "python.service")
public class PythonServiceProperties {

    /**
     * Python 服务基础地址
     */
    private String baseUrl = "http://localhost:8000";

    /**
     * Python 服务用户名
     */
    private String username = "python_service";

    /**
     * Python 服务密码
     */
    private String password = "python_service";

    /**
     * 登录失败时是否自动注册
     */
    private boolean autoRegister = true;

    /**
     * 令牌有效期（用于本地缓存）
     */
    private Duration tokenTtl = Duration.ofDays(6);

    /**
     * 提前刷新令牌的时间偏移
     */
    private Duration tokenRefreshSkew = Duration.ofHours(2);

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAutoRegister() {
        return autoRegister;
    }

    public void setAutoRegister(boolean autoRegister) {
        this.autoRegister = autoRegister;
    }

    public Duration getTokenTtl() {
        return tokenTtl;
    }

    public void setTokenTtl(Duration tokenTtl) {
        this.tokenTtl = tokenTtl;
    }

    public Duration getTokenRefreshSkew() {
        return tokenRefreshSkew;
    }

    public void setTokenRefreshSkew(Duration tokenRefreshSkew) {
        this.tokenRefreshSkew = tokenRefreshSkew;
    }
}
